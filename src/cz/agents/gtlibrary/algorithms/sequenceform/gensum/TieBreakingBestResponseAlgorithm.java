package cz.agents.gtlibrary.algorithms.sequenceform.gensum;

import cz.agents.gtlibrary.algorithms.sequenceform.SQFBestResponseAlgorithm;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.iinodes.ArrayListSequenceImpl;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.strategy.FirstActionStrategyForMissingSequences;
import cz.agents.gtlibrary.strategy.Strategy;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by Jakub Cerny on 21/07/2018.
 */
public class TieBreakingBestResponseAlgorithm {

    protected Expander<? extends InformationSet> expander;
    final protected int searchingPlayerIndex;
    final protected int opponentPlayerIndex;
    final protected Player[] players;
    final protected AlgorithmConfig<? extends InformationSet> algConfig;
    final protected GameInfo gameInfo;
    protected double MAX_UTILITY_VALUE;
    final protected double EPS_CONSTANT = 1e-12;//0.000000001; // zero for numerical-stability reasons

    protected HashMap<SequenceInformationSet,HashMap<GameState,double[]>> EVs;
    protected HashSet<Sequence> BRSequences;

    public TieBreakingBestResponseAlgorithm(Expander expander, int searchingPlayerIndex, Player[] actingPlayers, AlgorithmConfig<? extends InformationSet> algConfig, GameInfo gameInfo) {
        this.searchingPlayerIndex = searchingPlayerIndex;
        this.opponentPlayerIndex = (1 + searchingPlayerIndex) % 2;
        this.players = actingPlayers;
        assert players.length == 2;
        this.expander = expander;
        this.algConfig = algConfig;
        this.gameInfo = gameInfo;
        this.MAX_UTILITY_VALUE = gameInfo.getMaxUtility();
        this.EVs = new HashMap<>();
        this.BRSequences = new HashSet<>();
    }

    public Double calculateBR(GameState root, Map<Sequence, Double> opponentRealizationPlan) {

        double[] value = bestResponse(root, opponentRealizationPlan);

        return value[searchingPlayerIndex];
    }

    private double[] bestResponse(GameState root, Map<Sequence, Double> opponentRealizationPlan) {

        if(root.isGameEnd())
            return root.getUtilities();

        if(root.isPlayerToMoveNature()){
            double[] EVs = new double[2];
            double[] results;
            for(Action a : expander.getActions(root)){
                results = bestResponse(root.performAction(a), opponentRealizationPlan);
                EVs[0] += root.getProbabilityOfNatureFor(a) * results[0];
                EVs[1] += root.getProbabilityOfNatureFor(a) * results[1];
            }
            return EVs;
        }
        else{
            if(root.getPlayerToMove().getId() != searchingPlayerIndex){
                double[] EVs = new double[2];
                double[] results;
                for(Action a : expander.getActions(root)){
                    Sequence seq = new ArrayListSequenceImpl(root.getSequenceForPlayerToMove());
                    Double probA = opponentRealizationPlan.get(seq);
                    seq.addLast(a);
                    Double prob = opponentRealizationPlan.get(seq);
                    if(prob != null && prob > EPS_CONSTANT) {
                        results = bestResponse(root.performAction(a), opponentRealizationPlan);
                        EVs[0] += prob/probA * results[0];
                        EVs[1] += prob/probA * results[1];
                    }
                }
                return EVs;
            }
            else{
                if(EVs.containsKey(algConfig.getInformationSetFor(root)))
                    return EVs.get(algConfig.getInformationSetFor(root)).get(root);
                else{
                    Action maxAction = null;
                    SequenceInformationSet is = (SequenceInformationSet) algConfig.getInformationSetFor(root);
                    HashMap<GameState, HashMap<Action, double[]>> nodeEVs = new HashMap<>();
                    HashMap<GameState, Double> beliefs = new HashMap<>();
                    for(GameState isnode : is.getAllStates()){
                        HashMap<Action, double[]> actionEVs = new HashMap<>();
                        Double belief = opponentRealizationPlan.get(isnode.getSequenceFor(players[opponentPlayerIndex]));
                        if(belief == null) belief = 0.0;
                        beliefs.put(isnode, belief);
//                    System.out.println("Node " + isnode.getGameState().toString() + " BELIEF = " + belief);
                        for (Action ai : expander.getActions(root)) {
//                            maxAction = ai;
                            if(belief > 0) {
                                actionEVs.put(ai, bestResponse(isnode.performAction(ai), opponentRealizationPlan));
                            }
//                        System.out.println("\t Action " + ai + " EV = " + actionEVs.get(ai));
                        }
                        nodeEVs.put(isnode, actionEVs);
                    }

                    // default action
                    double maxEVs[] = new double[2];
                    Arrays.fill(maxEVs,Double.NEGATIVE_INFINITY);
                    for(Action a : expander.getActions(root)){
                        double[] EVs = new double[2];
//                        Arrays.fill(EVs,Double.NEGATIVE_INFINITY);
                        boolean contains = false;
                        for(GameState isnode : nodeEVs.keySet()){
                            if(nodeEVs.get(isnode).containsKey(a)) contains = true;
                            EVs[0] += beliefs.get(isnode) * (nodeEVs.get(isnode).containsKey(a) ?
                                    nodeEVs.get(isnode).get(a)[0] : 0.0);
                            EVs[1] += beliefs.get(isnode) * (nodeEVs.get(isnode).containsKey(a) ?
                                    nodeEVs.get(isnode).get(a)[1] : 0.0);
                        }
                        if(!contains) continue;
//                    System.out.println(EV + " " + (EV > maxEV) + " " + maxEV);
                        if (EVs[searchingPlayerIndex] > maxEVs[searchingPlayerIndex] + EPS_CONSTANT ||
                                (EVs[searchingPlayerIndex] > maxEVs[searchingPlayerIndex] - EPS_CONSTANT &&
                                EVs[opponentPlayerIndex] > maxEVs[opponentPlayerIndex])){
//                        System.out.println("max action set");
                            maxEVs[0] = EVs[0];
                            maxEVs[1] = EVs[1];
                            maxAction = a;
                        }
                    }

                    // store strategy to data
                    Sequence seq = new ArrayListSequenceImpl(root.getSequenceForPlayerToMove());
                    seq.addLast(maxAction);
                    BRSequences.add(seq);

                    // store EVs for nodes
                    HashMap<GameState, double[]> isEVs = new HashMap<>();
                    for(GameState isnode : is.getAllStates()){
//                    if(!isnode.equals(node)) {
//                        EVs.put(isnode, nodeEVs.get(isnode).get(maxAction));
//                    }
                        isEVs.put(isnode, nodeEVs.get(isnode).get(maxAction));
                    }
                    isEVs.remove(root);
                    EVs.put(is, isEVs);
//                    System.out.println(maxAction + " : " + nodeEVs.get(root).get(maxAction)[0] + " / " + nodeEVs.get(root).get(maxAction)[1]);

                    return nodeEVs.get(root).get(maxAction);
                }
            }
        }
//      return null;
    }

    public Strategy getBRStategy() {
        Strategy out = new FirstActionStrategyForMissingSequences();
        out.put(new ArrayListSequenceImpl(players[searchingPlayerIndex]), 1.0);
        for (Sequence seq : BRSequences) {
//            boolean valid = true;
//            for(Sequence prefix : seq.getAllPrefixes())
//                if(prefix.size() > 0  && !BRSequences.contains(prefix)){valid = false; break;}
//            if(!valid) continue;
//            System.out.println(seq);
            out.put(seq, 1.0);
        }
        return out;
    }

}
