package cz.agents.gtlibrary.algorithms.cfr.br.responses;

import cz.agents.gtlibrary.algorithms.cfr.br.CFRBRAlgorithmData;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.nodes.ChanceNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.LeafNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.Node;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Player;

import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by Jakub Cerny on 31/07/2018.
 */
public class PartialPureResponse extends RestrictivePureResponse {

//    protected int iteration;
//    protected final double EPS = 1e-12;
//    protected HashMap<MCTSInformationSet,HashMap<Node, Double>> EVs;
//    protected HashMap<String,HashMap<String,HashMap<String,Double>>> strategy;

    public PartialPureResponse(Player respondingPlayer, Node root,
                               HashMap<String,HashMap<String,HashMap<String,Double>>> strategy) {
        super(respondingPlayer, root, strategy);
    }

    public PartialPureResponse(Player respondingPlayer, int respondingPlayerIndex, Node root,
                               HashMap<String,HashMap<String,HashMap<String,Double>>> strategy) {
        super(respondingPlayer, respondingPlayerIndex, root, strategy);
    }

    @Override
    public double computeBR(Node root) {
//        computeBR(root, 1.0, 1.0, respondingPlayer);
        double value = computeBRRecursive(root, 1.0);
        iteration++;
        EVs.clear();
        return value;
    }

    @Override
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

            double[] localStrategy = getStrategy(in.getActions(), in);
            // preset strategy
            if(localStrategy != null){
                double ev = 0.0;

                // normalize
                double sum = 0.0;
                for(double d : localStrategy) sum+=d;
                if(sum <= EPS){localStrategy[0] = 1.0; sum+=1.0;System.out.println("chybka");}
                for(int i = 0; i < localStrategy.length; i++){ localStrategy[i] /= sum;}

                data.resetDataOfPlayer(respondingPlayerIndex, 0.0);
                for(int i = 0; i < localStrategy.length; i++){data.setDataOfPlayerIdx(respondingPlayerIndex, i, localStrategy[i]);}
                data.setIterationForPlayerIdx(respondingPlayerIndex, iteration);

//                System.out.println(Arrays.toString(localStrategy));

                int i = -1;
                for (Action ai : in.getActions()) {
                    i++;
                    ev += localStrategy[i] * computeBRRecursive(in.getChildFor(ai), pi * localStrategy[i]);
                }
                return ev;
            }

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
}
