package cz.agents.gtlibrary.algorithms.cfr.br;

import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.nodes.ChanceNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.LeafNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.Node;
import cz.agents.gtlibrary.algorithms.mcts.oos.OOSAlgorithmData;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.utils.Pair;
import cz.agents.gtlibrary.utils.Triplet;

import java.util.*;

public class QuantalResponse extends BestResponse {

    protected double lambda;
    protected int iteration;
    protected final double EPS = 1e-12;
    protected HashMap<Node, Double> EVs;

    public QuantalResponse(Player exploringPlayer, Node rootNode, double lambda){
        super(exploringPlayer, rootNode);
        this.lambda = lambda;
        this.iteration = 0;
        this.EVs = new HashMap<>();
    }

    @Override
    public void computeBR(Node root) {
//        System.out.println("Computing BR");
//        computeQR(root, 1.0, 1.0, respondingPlayer);
        computeQRRecursive(root, 1.0, respondingPlayer);
        iteration++;
        EVs.clear();
    }

    public double computeQRRecursive(Node node, double pi, Player expPlayer) {

        // node not reachable -> return
        if(pi <= 0.0){
            return 0.0;
        }

        // leaf -> return utility
        if (node instanceof LeafNode){
            return ((LeafNode) node).getUtilities()[expPlayer.getId()];
        }

        // chance -> compute EV weighted by probability
        if (node instanceof ChanceNode) {
            // chance
            ChanceNode cn = (ChanceNode) node;
            double ev = 0.0;
            for (Action ai : cn.getActions()) {
                final double p = cn.getGameState().getProbabilityOfNatureFor(ai);
                double new_p1 = p * pi;
                ev += p * computeQRRecursive(cn.getChildFor(ai), new_p1, expPlayer);
            }
            return ev;
        }

        InnerNode in = (InnerNode) node;
        MCTSInformationSet is = in.getInformationSet();
        CFRBRAlgorithmData data = (CFRBRAlgorithmData) is.getAlgorithmData();

        if(node.getGameState().getPlayerToMove().getId() != expPlayer.getId()) {

            double[] strategy = data.getStrategyAsList();
            double ev = 0.0;

            int i = -1;
            for (Action ai : in.getActions()) {
                i++;
                ev += strategy[i] * computeQRRecursive(in.getChildFor(ai), pi * strategy[i], expPlayer);
            }
            return ev;
        }
        else {
            // strategy already computed
            if(data.getIteration() == iteration){
                double value = EVs.get(node);
                EVs.remove(node);
                return value;
            }
            else{

                // compute evs of all actions of all nodes in the IS

//                System.out.println(in.getActions().size() + " / " + is.getAllNodes().size());

//                System.out.println("Setting strategy");
                HashMap<Node, HashMap<Action, Double>> nodeEVs = new HashMap<>();
                HashMap<Node, Double> beliefs = new HashMap<>();
                double beliefSum = 0.0;
                for(Node isnode : is.getAllNodes()){
                    HashMap<Action, Double> actionEVs = new HashMap<>();
                    double belief = getNodeBelief(isnode);
                    beliefSum += belief;
                    beliefs.put(isnode, belief);
//                    System.out.println("Node " + isnode.getGameState().toString() + " BELIEF = " + belief);
                    for (Action ai : in.getActions()) {
                        actionEVs.put(ai, computeQRRecursive(((InnerNode)isnode).getChildFor(ai), belief , expPlayer));
//                        System.out.println("\t Action " + ai + " EV = " + actionEVs.get(ai));
                    }
                    nodeEVs.put(isnode, actionEVs);
                }

                double[] strategy = new double[in.getActions().size()];
                if (beliefSum <= 0) {
                    // set default
                    strategy[0] = 1.0;
                }
                else {
                    double strategySum = 0;
                    int i = 0;
                    for(Action a : in.getActions()){
                        double EV = 0.0;
                        for(Node isnode : is.getAllNodes()){
                            EV += beliefs.get(isnode) * nodeEVs.get(isnode).get(a);
                        }
                        EV /= beliefSum;
                        strategy[i] = Math.exp(lambda * EV);
                        strategySum += strategy[i];
                        i++;
                    }
                    for(i = 0; i < strategy.length; i++) {
                        strategy[i] /= strategySum;
                    }
                }

                // store strategy to data
                data.setIteration(iteration);
                for(int i = 0; i < strategy.length; i++) {
                    data.setData(i, strategy[i]);
                }

                for(Node isnode : is.getAllNodes()) {
                    double EV = 0.0;
                    int i = 0;
                    for (Action ai : in.getActions()) {
                        EV += strategy[i] * nodeEVs.get(isnode).get(ai);
                        i++;
                    }
                    EVs.put(isnode, EV);
                }

                double value = EVs.get(node);
                EVs.remove(node);

                return value;
            }
        }
    }

    protected double getNodeBelief(Node node){
        Node parent = node.getParent();
        Action action = node.getLastAction();
        double belief = 1.0;
        while(parent != null){
            if(belief <= 0.0) break;
            if(parent instanceof ChanceNode){
                belief *= parent.getGameState().getProbabilityOfNatureFor(action);
            }
            else if(parent.getGameState().getPlayerToMove().getId() != respondingPlayer.getId()){
                belief *= ((CFRBRAlgorithmData)((InnerNode)parent).getInformationSet().getAlgorithmData())
                        .getProbabilityOfPlaying(action);
            }
            action = parent.getLastAction();
            parent = parent.getParent();
        }
        return belief;
    }


    public void computeQR(Node node, double pi1, double pi2, Player expPlayer) {

        // jdu dolu, pocitam psti

        // <list, prob (opponent * chance), ev> -> vsechny do fronty
        Stack<Pair<Node, Double>> topDownPass = new Stack<>();
        Queue<Triplet<Node, Double, Double>> bottomUpPass = new LinkedList<>();
        topDownPass.push(new Pair<>(node, 1.0));

        while(!topDownPass.isEmpty()){
            Pair<Node, Double> current = topDownPass.pop();

//            System.out.println(current.getLeft().getAlgorithmData() == null);
            if (current.getLeft() instanceof LeafNode) {
                bottomUpPass.add(new Triplet<>(current.getLeft(),current.getRight(), ((LeafNode) current.getLeft()).getUtilities()[expPlayer.getId()]));
                continue;
            }
            if (current.getLeft() instanceof ChanceNode) {
                ChanceNode cn = (ChanceNode) current.getLeft();
//                double ev = 0;
                for (Action ai : cn.getActions()) {
                    final double p = cn.getGameState().getProbabilityOfNatureFor(ai);
                    topDownPass.push(new Pair<>(cn.getChildFor(ai), p*current.getRight()));
                }
                continue;
            }
            InnerNode in = (InnerNode) current.getLeft();
            MCTSInformationSet is = in.getInformationSet();
            if (is.getPlayer().equals(expPlayer)) {
                for (Action ai : in.getActions()) {
                    topDownPass.push(new Pair<>(in.getChildFor(ai), current.getRight()));
                }
            }
            else{
                HashMap<Action, Double> rmProbs = ((CFRBRAlgorithmData) is.getAlgorithmData()).getStrategy();
//                int i = 0;
                for (Action ai : in.getActions()) {
                    if (true)//rmProbs.get(ai) > 0.0)
                        topDownPass.push(new Pair<>(in.getChildFor(ai), rmProbs.get(ai) * current.getRight()));
//                    i++;
                }
            }
        }

        // jdu nahoru po parentech, delim nalezenou psti, updatuju ev pokud narazim na chance / opp
        // pokud narazim na vlastni IS, nevkladam do fronty dokud neni

        // opp / ch - cummulate ev, cummulate number of actions.
        HashMap<Node, Double> opponentEVs = new HashMap<>();
        HashMap<Node, Integer> exploredActions = new HashMap<>();
        //


        HashMap<Node, Double> beliefs = new HashMap<>();
        HashMap<Node, HashMap<Action, Double>> nodeActionEVs = new HashMap<>();


        while(true){
            Triplet<Node, Double, Double> current = bottomUpPass.poll();
//            System.out.println(bottomUpPass.size() +" " + (current == null));
            if (current.getFirst().equals(node)) break;
            // update IS EV of predecessor by last action
            // check if last in IS -> compute EV, propagate up

//            System.out.println(current.getFirst().getLastAction().getInformationSet() == null);
//            System.out.println(current.getFirst().getParent().getGameState().isPlayerToMoveNature());
            if (!current.getFirst().getParent().getGameState().getPlayerToMove().equals(expPlayer)){
                // opponent / chance
                if (!opponentEVs.containsKey(current.getFirst().getParent())){
                    opponentEVs.put(current.getFirst().getParent(),0.0);
                    exploredActions.put(current.getFirst().getParent(), 0);
                }
                double ev = opponentEVs.get(current.getFirst().getParent());
                int actions = exploredActions.get(current.getFirst().getParent());

                // chance or opponent
//                System.out.println(current.getFirst().getDepth() + " " + current.getFirst().getParent().getDepth());
//                System.out.println(current.getFirst().getAlgorithmData() == null);
                double strategy = current.getFirst().getParent() instanceof ChanceNode ?
                        current.getFirst().getParent().getGameState().getProbabilityOfNatureFor(current.getFirst().getLastAction()) :
                        ((CFRBRAlgorithmData)current.getFirst().getParent().getInformationSet().getAlgorithmData()).
                        getProbabilityOfPlaying(current.getFirst().getLastAction());

                if(current.getSecond() > 0 && strategy > 0)
                    beliefs.put(current.getFirst().getParent(), current.getSecond() / strategy);

                ev += current.getThird() * strategy;
                actions ++;

                if ( actions == current.getFirst().getParent().getActions().size() ){
                    Double belief = beliefs.get(current.getFirst().getParent());
                    bottomUpPass.add(new Triplet<>(current.getFirst().getParent(), belief == null ? 0.0 : belief , ev));
                    opponentEVs.remove(current.getFirst().getParent());
                    exploredActions.remove(current.getFirst().getParent());
                    beliefs.remove(current.getFirst().getParent());
                }
                else{
                    opponentEVs.put(current.getFirst().getParent(), ev);
                    exploredActions.put(current.getFirst().getParent(), actions);
                }
            }
            else{
                // own IS

                if (!beliefs.containsKey(current.getFirst().getParent())){
                    beliefs.put(current.getFirst().getParent(), current.getSecond());
                    nodeActionEVs.put(current.getFirst().getParent(), new HashMap<>());
                }

                nodeActionEVs.get(current.getFirst().getParent()).put(current.getFirst().getLastAction(), current.getThird());

                int numActions = current.getFirst().getParent().getActions().size();
//                int numStates = current.getFirst().getParent().getInformationSet().getAllStates().size();
                boolean fullIS = true;
                for (Node isnode : current.getFirst().getParent().getInformationSet().getAllNodes()){
                    if (!nodeActionEVs.containsKey(isnode) || nodeActionEVs.get(isnode).size() != numActions){
                        fullIS = false;
                        break;
                    }
                }

                if(fullIS){

                    // calculate sum of beliefs, calculate ev for every action
                    double belief = 0.0;
                    HashMap<Action, Double> EVs = new HashMap<>();
                    for (Action a : current.getFirst().getParent().getActions())
                        EVs.put(a, 0.0);
                    for(Node isnode : current.getFirst().getParent().getInformationSet().getAllNodes()){
                        double nodeBelief = beliefs.get(isnode);
                        belief += nodeBelief;
                        for (Action a : current.getFirst().getParent().getActions()){
                            EVs.put(a, EVs.get(a) + nodeActionEVs.get(isnode).get(a) * nodeBelief);
                        }
                    }

                    // compute strategy, update data in algdata
//                    System.out.println(current.getFirst().getParent().getActions().size() + " " + ((CFRBRAlgorithmData)current.getFirst().getParent().getInformationSet().getAlgorithmData()).getNumActions());
                    double strategy;
                    double denominator = 0.0;
                    for (Action a : current.getFirst().getParent().getActions()) {
                        strategy = belief > 0 ? Math.exp(lambda * EVs.get(a) / belief) : 1.0;
                        denominator += strategy;
                        EVs.put(a, strategy);
                    }
                    for (Action a : current.getFirst().getParent().getActions()) {
                        EVs.put(a, EVs.get(a)/denominator);
                    }

                    // UPDATE CFRDATA
                    ((CFRBRAlgorithmData)current.getFirst().getParent().getInformationSet().getAlgorithmData()).setData(EVs);

                    // compute ev for all nodes in IS, propagate up
                    for(Node isnode : current.getFirst().getParent().getInformationSet().getAllNodes()){
                        double ev = 0;
                        for (Action a : current.getFirst().getParent().getActions()){
                            ev += EVs.get(a) * nodeActionEVs.get(isnode).get(a);
                        }
                        bottomUpPass.add(new Triplet<>(isnode, beliefs.get(isnode), ev));
                        nodeActionEVs.remove(isnode);
                        beliefs.remove(isnode);
                    }
                }
            }
        }
    }


}
