package cz.agents.gtlibrary.algorithms.cfr.br.responses;

import cz.agents.gtlibrary.algorithms.cfr.br.CFRBRAlgorithmData;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.nodes.ChanceNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.LeafNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.Node;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;

import java.util.HashMap;
import java.util.Set;

/**
 * Created by Jakub Cerny on 26/07/2018.
 */
public class SUQuantalResponse extends BestResponse {

    protected double w_1;
    protected double w_2;
    protected double w_3;
    protected double w_4;
    protected double w_5;

    protected int iteration;
    protected final double EPS = 1e-12;

    protected HashMap<MCTSInformationSet,HashMap<Node, Double>> EVs;


    public SUQuantalResponse(Player exploringPlayer, Node rootNode, double w1, double w2, double w3, double w4, double w5){
        super(exploringPlayer, rootNode);
        this.iteration = 0;
        this.w_1 = w1;
        this.w_2 = w2;
        this.w_3 = w3;
        this.w_4 = w4;
        this.w_5 = w5;
        EVs = new HashMap<>();
    }

    public SUQuantalResponse(Player exploringPlayer, int playerIdx, Node rootNode, double w1, double w2, double w3, double w4, double w5){
        super(exploringPlayer, playerIdx, rootNode);
        this.iteration = 0;
        this.w_1 = w1;
        this.w_2 = w2;
        this.w_3 = w3;
        this.w_4 = w4;
        this.w_5 = w5;
        EVs = new HashMap<>();
    }

    @Override
    public double computeBR(Node node) {
        double res = computeBR(node, 1.0);
        iteration++;
        EVs.clear();
        return res;
    }

    public double computeBR(Node node, double pi) {

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
                ev += p * computeBR(cn.getChildFor(ai), new_p1);
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
                if(strategy[i] > 0) {
                    ev += strategy[i] * computeBR(in.getChildFor(ai), pi * strategy[i]);
                }
            }
            return ev;
        }
        else {
            // strategy already computed
            if (data.getIterationForPlayerIdx(respondingPlayerIndex) == iteration) {
                double value = EVs.get(is).get(node);
                EVs.remove(node);
                return value;
            } else {

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
                        actionEVs.put(ai, computeBR(((InnerNode)isnode).getChildFor(ai), belief ));
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
                        strategy[i] = getActionUtility(a, is.getAllNodes(), beliefs);//Math.exp(lambda * EV);
                        strategySum += strategy[i];
                        i++;
                    }
                    if (strategySum <= 0.0) System.out.println("ZERO SUM");
                    for(i = 0; i < strategy.length; i++) {
                        strategy[i] /= strategySum;
                    }
                }

                // store strategy to data
                data.setIterationForPlayerIdx(respondingPlayerIndex, iteration);
                for(int i = 0; i < strategy.length; i++) {
                    data.setDataOfPlayerIdx(respondingPlayerIndex, i, strategy[i]);
                }

                HashMap<Node, Double> localIsEvs = new HashMap<>();
                for(Node isnode : is.getAllNodes()) {
                    double EV = 0.0;
                    int i = 0;
                    for (Action ai : in.getActions()) {
                        EV += strategy[i] * nodeEVs.get(isnode).get(ai);
                        i++;
                    }
                    localIsEvs.put(isnode, EV);
                }

                double value = localIsEvs.get(node);
                localIsEvs.remove(node);
                EVs.put(is, localIsEvs);

                return value;

            }
        }
    }

    public double getActionUtility(Action action, Set<InnerNode> nodes, HashMap<Node, Double> beliefs){
        double reward = ((ImmediateActionOutcomeProvider)action).getImmediateReward();
        double cost = ((ImmediateActionOutcomeProvider)action).getImmediateCost();
        double coverage = 0.0;
        double success = 0.0;
        double fail = 0.0;

        Object abstractAction = ((AbstractActionProvider)action).getActionAbstraction();

        double sumOfBeliefs = 0.0;
        Action parentAction;
        Sequence isSequence = nodes.iterator().next().getInformationSet().getPlayersHistory();

        // TODO: create a generalization in non-simultaneous games (possibly with chance)
        for(InnerNode node : nodes){
            // coverage = sum of beliefs where last opponent action = action / sum of all beliefs
            // fail = unweighted (?) sum of all fails
            // fail = same action in the same round (?)

            sumOfBeliefs += beliefs.get(node);
            parentAction = node.getLastAction();
            if (abstractAction.equals(((AbstractActionProvider)parentAction).getActionAbstraction())){
                coverage += beliefs.get(node);
            }

            Sequence parentSequence = node.getParent().getInformationSet().getPlayersHistory();
            for(int a = 0; a < isSequence.size(); a++){
                Object aAbstraction = ((AbstractActionProvider)isSequence.get(a)).getActionAbstraction();
                Object bAbstraction = ((AbstractActionProvider)parentSequence.get(a)).getActionAbstraction();
                if(aAbstraction.equals(abstractAction)){
                    if (aAbstraction.equals(bAbstraction))
                        fail++;
                    else
                        success++;
                }
            }
        }

        if(fail + success > 0.0){
            fail /= (fail+success);
            success /= (fail+success);
        }

        coverage /= sumOfBeliefs;

        return Math.exp(w_1 * coverage + w_2 * reward + w_3 * cost + w_4 * success + w_5 * fail);
    }


}
