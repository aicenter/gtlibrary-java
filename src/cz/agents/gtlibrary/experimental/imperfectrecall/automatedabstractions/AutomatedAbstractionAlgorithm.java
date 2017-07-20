package cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions;

import cz.agents.gtlibrary.algorithms.cfr.ir.IRCFRInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.MCTSConfig;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.domain.poker.generic.GPGameInfo;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerExpander;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerGameState;
import cz.agents.gtlibrary.domain.randomabstraction.IDObservation;
import cz.agents.gtlibrary.domain.randomgameimproved.io.BasicGameBuilder;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.iinodes.ImperfectRecallISKey;
import cz.agents.gtlibrary.iinodes.Observations;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.io.GambitEFG;

import java.util.*;

public abstract class AutomatedAbstractionAlgorithm {

    protected final GameState rootState;
    protected final Expander<? extends InformationSet> expander;
    protected final GameInfo gameInfo;
    protected final Map<ISKey, IRCFRInformationSet> currentAbstractionInformationSets;
    protected final Map<GameState, ISKey> currentAbstractionISKeys;
    protected int iteration = 1;
    protected int isKeyCounter = 0;

//    public static void main(String[] args) {
//        runGenericPoker();
//    }
//
//    protected static void runGenericPoker() {
//        GameState root = new GenericPokerGameState();
//        Expander<MCTSInformationSet> expander = new GenericPokerExpander<>(new MCTSConfig());
//
//        System.out.println("Abstracted IS count: " + expander.getAlgorithmConfig().getAllInformationSets().size());
//        AutomatedAbstractionAlgorithm cfr = new AutomatedAbstractionAlgorithm(root, expander, new GPGameInfo());
//
//        cfr.runIterations(1000000);
//        GambitEFG gambit = new GambitEFG();
//
//        gambit.write("cfrbrtest.gbt", root, expander);
//    }


    public AutomatedAbstractionAlgorithm(GameState rootState, Expander<? extends InformationSet> expander, GameInfo info) {
        BasicGameBuilder.build(rootState, expander.getAlgorithmConfig(), expander);
        this.rootState = rootState;
        this.expander = expander;
        this.gameInfo = info;
        currentAbstractionInformationSets = new HashMap<>();
        currentAbstractionISKeys = new HashMap<>();
        buildInitialAbstraction();
    }

    protected void buildInitialAbstraction() {
        buildInformationSets();
        addData(currentAbstractionInformationSets.values());
    }

    protected void buildInformationSets() {
        expander.getAlgorithmConfig().getAllInformationSets().values().stream().forEach(i -> {
                    GameState state = i.getAllStates().stream().findAny().get();
                    ImperfectRecallISKey key = createISKey(state);
                    IRCFRInformationSet set = currentAbstractionInformationSets.computeIfAbsent(key, k -> new IRCFRInformationSet(state, key));

                    i.getAllStates().forEach(s -> currentAbstractionISKeys.put(s, key));
                    set.addAllStatesToIS(i.getAllStates());
                }
        );
    }

    protected ImperfectRecallISKey createISKey(GameState state) {
        Observations observations = new Observations(state.getPlayerToMove(), state.getPlayerToMove());

        observations.add(new IDObservation(state.getSequenceForPlayerToMove().size()));
        return new ImperfectRecallISKey(observations, null, null);
    }

    protected void addData(Collection<IRCFRInformationSet> informationSets) {
        informationSets.forEach(i -> i.setData(new CFRBRData(this.expander.getActions(i.getAllStates().stream().findAny().get()))));
    }

    public void runIterations(int iterations) {
        for (int i = 0; i < iterations; i++) {
            this.iteration++;
            iteration(rootState.getAllPlayers()[1]);
            iteration(rootState.getAllPlayers()[0]);
            if (i % 20 == 0 || iteration == 1) {
               printStatistics();
            }
        }
    }

    protected abstract void printStatistics();

    protected abstract void iteration(Player player);



//    protected Map<Sequence, Double> getIRDiffProbs(Map<Sequence, Map<Action, Double>> irStrategyDiff, Map<Action, Double> strategy) {
//        Map<Sequence, Double> diffProbs = new HashMap<>();
//        for (Sequence sequence : irStrategyDiff.keySet()) {
//            diffProbs.put(sequence, getProbability(sequence, strategy));
//        }
//        return diffProbs;
//    }
//
//    protected Double getProbability(Sequence sequence, Map<Action, Double> strategy) {
//        double prob = 1;
//        for (Action action : sequence) {
//            prob *= strategy.getOrDefault(action, 0d);
//        }
//        return prob;
//    }
//
//    protected boolean valid(StrategyDiffs strategyDiffs, Map<Action, Double> strategy) {
//        for (Map<Action, Double> actionDoubleMap : strategyDiffs.prStrategyDiff.values()) {
//            for (Map.Entry<Action, Double> actionDoubleEntry : actionDoubleMap.entrySet()) {
//                if (strategy.getOrDefault(actionDoubleEntry.getKey(), 0d) + actionDoubleEntry.getValue() < 0)
//                    return false;
//            }
//        }
//        for (Map<Action, Double> actionDoubleMap : strategyDiffs.irStrategyDiff.values()) {
//            for (Map.Entry<Action, Double> actionDoubleEntry : actionDoubleMap.entrySet()) {
//                if (strategy.getOrDefault(actionDoubleEntry.getKey(), 0d) + actionDoubleEntry.getValue() < 0)
//                    return false;
//            }
//        }
//        return true;
//    }
//
//
//    protected IRCFRInformationSet createNewIS(Set<GameState> states, Player player, CFRBRData data) {
//        ImperfectRecallISKey newISKey = createCounterISKey(player);
//        GameState state = states.stream().findAny().get();
//
//        IRCFRInformationSet is = new IRCFRInformationSet(state);
//
//        is.addAllStatesToIS(states);
//        currentAbstractionInformationSets.put(newISKey, is);
//        is.setData(new CFRBRData(data));
//        return is;
//    }
//
//    protected ImperfectRecallISKey createCounterISKey(Player player) {
//        Observations observations = new Observations(player, player);
//
//        observations.add(new IDObservation(isKeyCounter++));
//        return new ImperfectRecallISKey(observations, null, null);
//    }
//
//    protected StrategyDiffs getStrategyDiffs(Map<InformationSet, Map<Integer, Map<Sequence, double[]>>> toSplit) {
//        StrategyDiffs strategyDiffs = new StrategyDiffs();
//
//        for (Map.Entry<InformationSet, Map<Integer, Map<Sequence, double[]>>> actionMapEntry : toSplit.entrySet()) {
//            for (Map.Entry<Integer, Map<Sequence, double[]>> entry : actionMapEntry.getValue().entrySet()) {
//                List<Action> actions = ((IRCFRInformationSet) actionMapEntry.getKey()).getData().getActions();
//                double[] meanStratDiffForAction = new double[actions.size()];
//                double[] meanStrategy = ((IRCFRInformationSet) actionMapEntry.getKey()).getData().getMp();
//                double meanStratDiffForActionNormalizer = 0;
//                int actionIndex = entry.getKey();
//
//                for (Map.Entry<Sequence, double[]> sequenceValuesEntry : entry.getValue().entrySet()) {
//                    double[] meanStratDiffForSequence = new double[actions.size()];
//
////                    if(sequenceValuesEntry.getValue()[0] + sequenceValuesEntry.getValue()[1] < 1e-3)
////                        continue;
//                    for (int i = 0; i < actions.size(); i++) {
//                        meanStratDiffForSequence[i] = sequenceValuesEntry.getValue()[0] * ((i == actionIndex ? 1 : 0) - meanStrategy[i]);
//                        meanStratDiffForAction[i] += meanStratDiffForSequence[i];
//                        meanStratDiffForSequence[i] /= sequenceValuesEntry.getValue()[0] + sequenceValuesEntry.getValue()[1];
//                    }
//                    meanStratDiffForActionNormalizer += sequenceValuesEntry.getValue()[0] + sequenceValuesEntry.getValue()[1];
//                    strategyDiffs.prStrategyDiff.put(sequenceValuesEntry.getKey(), toMapNoNorm(actions, meanStratDiffForSequence));
//                }
////                if(meanStratDiffForActionNormalizer < 1e-3)
////                    continue;
//                for (int i = 0; i < meanStratDiffForAction.length; i++) {
//                    meanStratDiffForAction[i] /= meanStratDiffForActionNormalizer;
//                }
//                actionMapEntry.getKey().getAllStates().stream().map(s -> s.getSequenceForPlayerToMove()).forEach(sequence -> {
//                    strategyDiffs.irStrategyDiff.put(sequence, toMapNoNorm(actions, meanStratDiffForAction));
//                });
//            }
//        }
//        return strategyDiffs;
//    }
//
//    protected Map<Action, Double> toMap(List<Action> actions, double[] meanStrat) {
//        int index = 0;
//        double sum = Arrays.stream(meanStrat).sum();
//        Map<Action, Double> actionMap = new HashMap<>(meanStrat.length);
//
//        for (Action action : actions) {
//            actionMap.put(action, meanStrat[index++] / sum);
//        }
//        return actionMap;
//    }
//
//    protected Map<Action, Double> toMapNoNorm(List<Action> actions, double[] meanStrat) {
//        int index = 0;
//        Map<Action, Double> actionMap = new HashMap<>(meanStrat.length);
//
//        for (Action action : actions) {
//            actionMap.put(action, meanStrat[index++]);
//        }
//        return actionMap;
//    }
}
