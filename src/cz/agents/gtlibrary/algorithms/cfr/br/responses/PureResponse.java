package cz.agents.gtlibrary.algorithms.cfr.br.responses;

import cz.agents.gtlibrary.algorithms.cfr.br.CFRBRAlgorithmData;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.nodes.ChanceNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.LeafNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.Node;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Player;

import java.util.HashMap;

/**
 * Created by Jakub Cerny on 16/04/2018.
 */
public class PureResponse extends BestResponse {

    protected int iteration;

    protected final double EPS = 1e-12;

    protected HashMap<MCTSInformationSet,HashMap<Node, Double>> EVs;

    public PureResponse(Player respondingPlayer, Node root) {
        super(respondingPlayer, root);
        iteration = 0;
        EVs = new HashMap<>();
    }

    public PureResponse(Player respondingPlayer, int playerIdx, Node root) {
        super(respondingPlayer, playerIdx, root);
        iteration = 0;
        EVs = new HashMap<>();
    }

    public void setIteration(int iteration){ this.iteration = iteration; }

    @Override
    public double computeBR(Node root) {
//        computeBR(root, 1.0, 1.0, respondingPlayer);
        double value = computeBRRecursive(root, 1.0);
        iteration++;
        EVs.clear();
        return value;
    }

    public double computeBRRecursive(Node node, double pi) {

        // node not reachable -> return
        if(pi <= 0.0){
            return 0.0;
        }

        // leaf -> return utility
        if (node instanceof LeafNode){
            return ((LeafNode) node).getUtilities()[respondingPlayerIndex];
        }

        // chance -> compute EV weighted by probability
        if (node instanceof ChanceNode) {
            // chance
            ChanceNode cn = (ChanceNode) node;
            double ev = 0.0;
            for (Action ai : cn.getActions()) {
                final double p = cn.getProbabilityOfNatureFor(ai);
                double new_p1 = p * pi;
                ev += p * computeBRRecursive(cn.getChildFor(ai), new_p1);
            }
            return ev;
        }

        InnerNode in = (InnerNode) node;
        MCTSInformationSet is = in.getInformationSet();
        CFRBRAlgorithmData data = (CFRBRAlgorithmData) is.getAlgorithmData();

        if(((InnerNode) node).getInformationSet().getPlayer().getId() != respondingPlayer.getId()) {

            double[] strategy = data.getStrategyOfPlayerAsList(FAKE_LEADER_IDX);
            double ev = 0.0;

            int i = -1;
            for (Action ai : in.getActions()) {
                i++;
                ev += strategy[i] * computeBRRecursive(in.getChildFor(ai), pi * strategy[i]);
            }
            return ev;
        }
        else {
            // strategy already computed
            if(data.getIterationForPlayerIdx(respondingPlayerIndex) == iteration){
                double value = EVs.get(((InnerNode) node).getInformationSet()).get(node);
                EVs.get(((InnerNode) node).getInformationSet()).remove(node);
                return value;
            }
            else{

                // compute evs of all actions of all nodes in the IS

//                System.out.println(in.getActions().size() + " / " + is.getAllNodes().size());

//                System.out.println("Setting strategy");
                HashMap<Node, HashMap<Action, Double>> nodeEVs = new HashMap<>();
                HashMap<Node, Double> beliefs = new HashMap<>();
                for(Node isnode : is.getAllNodes()){
                    HashMap<Action, Double> actionEVs = new HashMap<>();
                    double belief = getNodeBelief(isnode);
                    beliefs.put(isnode, belief);
//                    System.out.println("Node " + isnode.getGameState().toString() + " BELIEF = " + belief);
                    for (Action ai : in.getActions()) {
                        actionEVs.put(ai, computeBRRecursive(((InnerNode)isnode).getChildFor(ai), belief ));
//                        System.out.println("\t Action " + ai + " EV = " + actionEVs.get(ai));
                    }
                    nodeEVs.put(isnode, actionEVs);
                }

                // default action
                Action maxAction = in.getActions().get(0);
                double maxEV = Double.NEGATIVE_INFINITY;
                for(Action a : in.getActions()){
                    double EV = 0.0;
                    for(Node isnode : is.getAllNodes()){
                        EV += beliefs.get(isnode) * nodeEVs.get(isnode).get(a);
                    }
//                    System.out.println(EV + " " + (EV > maxEV) + " " + maxEV);
                    if (EV > maxEV){
//                        System.out.println("max action set");
                        maxEV = EV;
                        maxAction = a;
                    }
                }

                // store strategy to data
                data.resetDataOfPlayer(respondingPlayerIndex, 0.0);
                data.setDataOfPlayerIdx(respondingPlayerIndex, maxAction, 1.0);
                data.setIterationForPlayerIdx(respondingPlayerIndex, iteration);

                // store EVs for nodes
                HashMap<Node, Double> isEVs = new HashMap<>();
                for(Node isnode : is.getAllNodes()){
//                    if(!isnode.equals(node)) {
//                        EVs.put(isnode, nodeEVs.get(isnode).get(maxAction));
//                    }
                    isEVs.put(isnode, nodeEVs.get(isnode).get(maxAction));
                }
                isEVs.remove(node);
                EVs.put(((InnerNode) node).getInformationSet(), isEVs);

                return nodeEVs.get(node).get(maxAction);
            }
        }
    }


//    public void computeBR(Node node, double pi1, double pi2, Player expPlayer) {
//        // jdu dolu, pocitam psti
//
//        // <list, prob (opponent * chance), ev> -> vsechny do fronty
//        Stack<Pair<Node, Double>> topDownPass = new Stack<>();
//        Queue<Triplet<Node, Double, Double>> bottomUpPass = new LinkedList<>();
//        topDownPass.push(new Pair<>(node, 1.0));
//
//        while(!topDownPass.isEmpty()){
//            Pair<Node, Double> current = topDownPass.pop();
//
////            System.out.println(current.getLeft().getAlgorithmData() == null);
//            if (current.getLeft() instanceof LeafNode) {
//                bottomUpPass.add(new Triplet<>(current.getLeft(),current.getRight(), ((LeafNode) current.getLeft()).getUtilities()[expPlayer.getId()]));
//                continue;
//            }
//            if (current.getLeft() instanceof ChanceNode) {
//                ChanceNode cn = (ChanceNode) current.getLeft();
////                double ev = 0;
//                for (Action ai : cn.getActions()) {
//                    final double p = cn.getGameState().getProbabilityOfNatureFor(ai);
//                    topDownPass.push(new Pair<>(cn.getChildFor(ai), p*current.getRight()));
//                }
//                continue;
//            }
//            InnerNode in = (InnerNode) current.getLeft();
//            MCTSInformationSet is = in.getInformationSet();
//            if (is.getPlayer().equals(expPlayer)) {
//                for (Action ai : in.getActions()) {
//                    topDownPass.push(new Pair<>(in.getChildFor(ai), current.getRight()));
//                }
//            }
//            else{
//                HashMap<Action, Double> rmProbs = ((CFRBRAlgorithmData) is.getAlgorithmData()).getStrategyOfPlayerIdx();
////                int i = 0;
//                for (Action ai : in.getActions()) {
//                    if (true)//rmProbs.get(ai) > 0.0)
//                        topDownPass.push(new Pair<>(in.getChildFor(ai), rmProbs.get(ai) * current.getRight()));
////                    i++;
//                }
//            }
//        }
//
//        // jdu nahoru po parentech, delim nalezenou psti, updatuju ev pokud narazim na chance / opp
//        // pokud narazim na vlastni IS, nevkladam do fronty dokud neni
//
//        // opp / ch - cummulate ev, cummulate number of actions.
//        HashMap<Node, Double> opponentEVs = new HashMap<>();
//        HashMap<Node, Integer> exploredActions = new HashMap<>();
//        //
//
//
//        HashMap<Node, Double> beliefs = new HashMap<>();
//        HashMap<Node, HashMap<Action, Double>> nodeActionEVs = new HashMap<>();
//
//
//        while(true){
//            Triplet<Node, Double, Double> current = bottomUpPass.poll();
////            System.out.println(bottomUpPass.size() +" " + (current == null));
//            if (current.getFirst().equals(node)) break;
//            // update IS EV of predecessor by last action
//            // check if last in IS -> compute EV, propagate up
//
////            System.out.println(current.getFirst().getLastAction().getInformationSet() == null);
////            System.out.println(current.getFirst().getParent().getGameState().isPlayerToMoveNature());
//            if (!current.getFirst().getParent().getGameState().getPlayerToMove().equals(expPlayer)){
//                // opponent / chance
//                if (!opponentEVs.containsKey(current.getFirst().getParent())){
//                    opponentEVs.put(current.getFirst().getParent(),0.0);
//                    exploredActions.put(current.getFirst().getParent(), 0);
//                }
//                double ev = opponentEVs.get(current.getFirst().getParent());
//                int actions = exploredActions.get(current.getFirst().getParent());
//
//                // chance or opponent
////                System.out.println(current.getFirst().getDepth() + " " + current.getFirst().getParent().getDepth());
////                System.out.println(current.getFirst().getAlgorithmData() == null);
//                double strategy = current.getFirst().getParent() instanceof ChanceNode ?
//                        current.getFirst().getParent().getGameState().getProbabilityOfNatureFor(current.getFirst().getLastAction()) :
//                        ((CFRBRAlgorithmData)current.getFirst().getParent().getInformationSet().getAlgorithmData()).
//                                getProbabilityOfPlaying(current.getFirst().getLastAction());
//
//                if(current.getSecond() > EPS && strategy > EPS)
//                    beliefs.put(current.getFirst().getParent(), (1000 * current.getSecond()) / (1000 * strategy));
//
//                ev += current.getThird() * strategy;
//                actions ++;
//
//                if ( actions == current.getFirst().getParent().getActions().size() ){
//                    Double belief = beliefs.get(current.getFirst().getParent());
//                    bottomUpPass.add(new Triplet<>(current.getFirst().getParent(), belief == null ? 0.0 : belief , ev));
//                    opponentEVs.remove(current.getFirst().getParent());
//                    exploredActions.remove(current.getFirst().getParent());
//                    beliefs.remove(current.getFirst().getParent());
//                }
//                else{
//                    opponentEVs.put(current.getFirst().getParent(), ev);
//                    exploredActions.put(current.getFirst().getParent(), actions);
//                }
//            }
//            else{
//                // own IS
//
//                if (!beliefs.containsKey(current.getFirst().getParent())){
//                    if (current.getSecond() > EPS) beliefs.put(current.getFirst().getParent(), current.getSecond());
//                }
//
//                if (!nodeActionEVs.containsKey(current.getFirst().getParent())){
//                    nodeActionEVs.put(current.getFirst().getParent(), new HashMap<>());
//                }
//
//                nodeActionEVs.get(current.getFirst().getParent()).put(current.getFirst().getLastAction(), current.getThird());
//
//                int numActions = current.getFirst().getParent().getActions().size();
//                boolean fullIS = true;
//                for (Node isnode : current.getFirst().getParent().getInformationSet().getAllNodes()){
//                    if (!nodeActionEVs.containsKey(isnode) || nodeActionEVs.get(isnode).size() != numActions){
//                        fullIS = false;
//                        break;
//                    }
//                }
//
//                if(fullIS){
//
//                    // calculate sum of beliefs, calculate ev for every action
////                    double belief = 0.0;
//                    HashMap<Action, Double> behavioralStrategy = new HashMap<>();
//                    for (Action a : current.getFirst().getParent().getActions())
//                        behavioralStrategy.put(a, 0.0);
//                    Double b;
//                    for(Node isnode : current.getFirst().getParent().getInformationSet().getAllNodes()){
//                        b = beliefs.get(isnode);
//                        double nodeBelief = b != null ? b.doubleValue() : 0.0;
////                        belief += nodeBelief;
//                        for (Action a : current.getFirst().getParent().getActions()){
//                            behavioralStrategy.put(a, behavioralStrategy.get(a) + nodeActionEVs.get(isnode).get(a) * nodeBelief);
//                        }
//                    }
//
//                    // compute strategy, update data in algdata
//                    double actionStrategy;
//                    double maxEV = Double.MIN_VALUE;
//                    Action maxAction = null;
//                    for (Action a : current.getFirst().getParent().getActions()) {
//                        actionStrategy = behavioralStrategy.get(a);
//                        if (actionStrategy > maxEV){
//                            maxEV = actionStrategy;
//                            maxAction = a;
//                        }
//                    }
//
//                    for (Action a : current.getFirst().getParent().getActions()) {
//                        if (a.equals(maxAction)){
//                            behavioralStrategy.put(a, 1.0);
//                        }
//                        else{
//                            behavioralStrategy.put(a, 0.0);
//                        }
//                    }
//
//                    // UPDATE CFRDATA
//                    ((CFRBRAlgorithmData)current.getFirst().getParent().getInformationSet().getAlgorithmData()).setDataOfPlayerIdx(behavioralStrategy);
//
//                    // compute ev for all nodes in IS, propagate up
//                    for(Node isnode : current.getFirst().getParent().getInformationSet().getAllNodes()){
//                        double ev = 0;
//                        for (Action a : current.getFirst().getParent().getActions()){
//                            ev += behavioralStrategy.get(a) * nodeActionEVs.get(isnode).get(a);
//                        }
//                        b = beliefs.get(isnode);
//                        bottomUpPass.add(new Triplet<>(isnode, b != null ? b.doubleValue() : 0.0, ev));
//                        nodeActionEVs.remove(isnode);
//                        beliefs.remove(isnode);
//                    }
//                }
//            }
//        }
//    }
}
