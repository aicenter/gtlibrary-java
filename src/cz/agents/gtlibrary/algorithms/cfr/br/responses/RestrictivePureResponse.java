package cz.agents.gtlibrary.algorithms.cfr.br.responses;

import cz.agents.gtlibrary.algorithms.cfr.br.CFRBRAlgorithmData;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.nodes.ChanceNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.LeafNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.Node;
import cz.agents.gtlibrary.domain.flipit.FlipItAction;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Created by Jakub Cerny on 31/07/2018.
 */
public class RestrictivePureResponse extends BestResponse {


    protected int iteration;
    protected final double EPS = 1e-12;
    protected HashMap<MCTSInformationSet,HashMap<Node, Double>> EVs;
    protected HashMap<String,HashMap<String,HashMap<String,Double>>> strategy;

    protected HashSet<Action> bannedActions;


    public RestrictivePureResponse(Player respondingPlayer, Node root,
                                   HashMap<String,HashMap<String,HashMap<String,Double>>> strategy) {
        super(respondingPlayer, root);
        this.iteration = 0;
        this.EVs = new HashMap<>();
        this.strategy = strategy;
        this.bannedActions = new HashSet<>();
    }

    public RestrictivePureResponse(Player respondingPlayer, int respondingPlayerIndex, Node root,
                                   HashMap<String,HashMap<String,HashMap<String,Double>>> strategy) {
        super(respondingPlayer, respondingPlayerIndex, root);
        this.iteration = 0;
        this.EVs = new HashMap<>();
        this.strategy = strategy;
        this.bannedActions = new HashSet<>();
    }

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

//            double[] strategy = data.getStrategyOfPlayerAsList(FAKE_LEADER_IDX);
            double[] strategy = getStrategy(in.getActions(), in);
            if(strategy == null) {
//                System.out.println(in.getInformationSet().getPlayersHistory());
//                System.out.println(in.getParent().getInformationSet().getPlayersHistory());
//                System.out.println("*************************************************************");
                bannedActions.add(in.getLastAction());
                return Double.NEGATIVE_INFINITY;
            }
            double ev = 0.0;

            double sum = 0.0;
            for(double d : strategy) sum+=d;
            if(sum <= EPS){System.out.println("Chyba ");strategy[0] = 1.0; sum+=1.0;}
            for(int i = 0; i < strategy.length; i++){ strategy[i] /= sum;}
//            System.out.println(Arrays.toString(strategy));

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
                    if(bannedActions.contains(a)) continue;
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
                if(bannedActions.contains(maxAction)){
//                    System.out.println(in.getInformationSet().getPlayersHistory());
//                    System.out.println(in.getParent().getInformationSet().getPlayersHistory());
//                    System.out.println("*************************************************************");
//                    bannedActions.add(in.getInformationSet().getPlayersHistory().getLast());
                    return Double.NEGATIVE_INFINITY;
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


    protected double[] getStrategy(List<Action> actions, InnerNode in){
        String defenderSequenceString = "";
        Sequence defenderSequence = in.getParent().getInformationSet().getPlayersHistory();
        if(defenderSequence.isEmpty())
            defenderSequenceString = "EMPTY";

        String attackerSequenceString = "";
        Sequence attackerSequence = in.getInformationSet().getPlayersHistory();
        if(attackerSequence.isEmpty())
            attackerSequenceString = "EMPTY";

        for(int i = 0; i < defenderSequence.size(); i++){
            if(i < defenderSequence.size()-1)
                defenderSequenceString += String.valueOf(getActionID(defenderSequence.get(i))) + ",";
            else
                defenderSequenceString += String.valueOf(getActionID(defenderSequence.get(i)));
        }

        for(int i = 0; i < attackerSequence.size(); i++){
            if(i < defenderSequence.size()-1)
                attackerSequenceString += String.valueOf(getActionID(attackerSequence.get(i))) + ",";
            else
                attackerSequenceString += String.valueOf(getActionID(attackerSequence.get(i)));
        }

        if(strategy.containsKey(defenderSequenceString) && strategy.get(defenderSequenceString).containsKey(attackerSequenceString)){
            double[] localStrategy = new double[actions.size()];
            for(int i = 0; i < actions.size(); i++){
                String actionID = getActionID(actions.get(i));
                if(strategy.get(defenderSequenceString).get(attackerSequenceString).containsKey(actionID))
                    localStrategy[i] = strategy.get(defenderSequenceString).get(attackerSequenceString).get(actionID);
            }
            return localStrategy;
        }
        else{
//            System.out.println(defenderSequenceString);
//            System.out.println(attackerSequenceString);
//            System.out.println("xxxxxxxxxxx");
            return null;
        }

    }


    protected String getActionID(Action action){
        if(action instanceof FlipItAction){
            if(((FlipItAction) action).isNoop()) return "5";
            else return String.valueOf(((FlipItAction) action).getControlNode().getIntID());
        }
        else throw new NotImplementedException();
    }


}
