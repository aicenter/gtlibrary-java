package cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.memeff;

import cz.agents.gtlibrary.algorithms.cfr.ir.IRCFRInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.domain.poker.generic.GPGameInfo;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerExpander;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerGameState;
import cz.agents.gtlibrary.domain.poker.kuhn.KPGameInfo;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerExpander;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerGameState;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameExpander;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameInfo;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameState;
import cz.agents.gtlibrary.domain.wichardtne.PerfectInformationWichardtState;
import cz.agents.gtlibrary.domain.wichardtne.WichardtExpander;
import cz.agents.gtlibrary.domain.wichardtne.WichardtGameInfo;
import cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.CFRBRData;
import cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.StrategyDiffs;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.iinodes.ImperfectRecallISKey;
import cz.agents.gtlibrary.iinodes.PerfectRecallISKey;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.io.GambitEFG;

import java.util.*;

public class FPIRA extends AutomatedAbstractionAlgorithm {

    public static void main(String[] args) {
        runGenericPoker();
//        runKuhnPoker();
//        runRandomGame();
//        runWichardtCounterexample();
    }

    public static void runGenericPoker() {
        GameState root = new GenericPokerGameState();
        Expander<MCTSInformationSet> expander = new GenericPokerExpander<>(new FPIRAConfig());
        FPIRA fpira = new FPIRA(root, expander, new GPGameInfo());

        fpira.runIterations(100000);
    }

    public static void runKuhnPoker() {
        GameState root = new KuhnPokerGameState();
        Expander<MCTSInformationSet> expander = new KuhnPokerExpander<>(new FPIRAConfig());
        FPIRA fpira = new FPIRA(root, expander, new KPGameInfo());

        fpira.runIterations(100000);
    }

    public static void runRandomGame() {
        GameState root = new RandomGameState();
        Expander<MCTSInformationSet> expander = new RandomGameExpander<>(new FPIRAConfig());
        FPIRA fpira = new FPIRA(root, expander, new RandomGameInfo());

        fpira.runIterations(100000);
        GambitEFG gambit = new GambitEFG();

        gambit.write("FPIRAtest.gbt", root, expander);
    }

    public static void runWichardtCounterexample() {
        GameState root = new PerfectInformationWichardtState();
        Expander<MCTSInformationSet> expander = new WichardtExpander<>(new FPIRAConfig());
        FPIRA fpira = new FPIRA(root, expander, new WichardtGameInfo());

        fpira.runIterations(100000);
    }

    private final FPIRADeltaCalculator p0Delta;
    private final FPIRADeltaCalculator p1Delta;
    private final FPIRABestResponse p0BR;
    private final FPIRABestResponse p1BR;

    public FPIRA(GameState rootState, Expander<? extends InformationSet> expander, GameInfo info) {
        super(rootState, expander, info);
        p0Delta = new FPIRADeltaCalculator(this.rootState, this.expander, 0, expander.getAlgorithmConfig(), info, false, currentAbstractionISKeys);
        p1Delta = new FPIRADeltaCalculator(this.rootState, this.expander, 1, expander.getAlgorithmConfig(), info, false, currentAbstractionISKeys);
        p0BR = new FPIRABestResponse(this.rootState, this.expander, 0, new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]}, expander.getAlgorithmConfig(), info, false, currentAbstractionISKeys);
        p1BR = new FPIRABestResponse(this.rootState, this.expander, 1, new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]}, expander.getAlgorithmConfig(), info, false, currentAbstractionISKeys);
    }

    @Override
    protected void printStatistics() {
        Map<ISKey, double[]> p0Strategy = getBehavioralStrategyFor(rootState.getAllPlayers()[0]);
        Map<ISKey, double[]> p1Strategy = getBehavioralStrategyFor(rootState.getAllPlayers()[1]);

        System.out.println("Iteration: " + iteration);
        System.out.println("p0BR: " + p0BR.calculateBRForAbstractedStrategy(rootState, p1Strategy));
        System.out.println("p1BR: " + -p1BR.calculateBRForAbstractedStrategy(rootState, p0Strategy));
        System.out.println("Current IS count: " + currentAbstractionInformationSets.size());
        System.out.println("State cache from BR sizes: " + p0BR.maxStateValueCache + ", " + p1BR.maxStateValueCache);
        System.out.println("BR result sizes: " + p0BR.maxBRResultSize + ", " + p1BR.maxBRResultSize);
        System.out.println("State cache from deltaCalc sizes: " + p0Delta.maxStateValueCache + ", " + p1Delta.maxStateValueCache);
        System.out.println("Reachable IS count: " + getReachableISCountFromOriginalGame(p0Strategy, p1Strategy));
        System.out.println("Reachable abstracted IS count: " + getReachableAbstractedISCountFromOriginalGame(p0Strategy, p1Strategy));
        System.out.println("Current memory: " + mxBean.getHeapMemoryUsage().getUsed());
        System.out.println("Max memory: " + mxBean.getHeapMemoryUsage().getMax());
        System.out.println(mxBean.getHeapMemoryUsage().toString());
        System.out.println("Memory measurement from DO: " + ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024));
        System.out.println("*************************************************");
    }

    protected long getReachableISCountFromOriginalGame(Map<ISKey, double[]> p0Strategy, Map<ISKey, double[]> p1Strategy) {
        return currentAbstractionInformationSets.values().stream().flatMap(i -> i.getAllStates().stream().filter(s ->
                AbstractedStrategyUtils.getProbability(s.getSequenceFor(gameInfo.getAllPlayers()[0]), p0Strategy, currentAbstractionISKeys, expander) > 1e-8 &&
                        AbstractedStrategyUtils.getProbability(s.getSequenceFor(gameInfo.getAllPlayers()[1]), p1Strategy, currentAbstractionISKeys, expander) > 1e-8)
                .map(s -> s.getISKeyForPlayerToMove()).distinct()).count();
    }

    protected long getReachableAbstractedISCount(Map<ISKey, double[]> p0Strategy, Map<ISKey, double[]> p1Strategy) {
        return currentAbstractionInformationSets.values().stream().filter(i ->
                i.getAllStates().stream().anyMatch(s ->
                        AbstractedStrategyUtils.getProbability(s.getSequenceFor(gameInfo.getAllPlayers()[0]), p0Strategy, currentAbstractionISKeys, expander) > 1e-8 &&
                                AbstractedStrategyUtils.getProbability(s.getSequenceFor(gameInfo.getAllPlayers()[1]), p1Strategy, currentAbstractionISKeys, expander) > 1e-8) &&
                        i.getAllStates().stream().map(s -> s.getISKeyForPlayerToMove()).distinct().count() > 1
        ).count();
    }

    protected long getReachableAbstractedISCountFromOriginalGame(Map<ISKey, double[]> p0Strategy, Map<ISKey, double[]> p1Strategy) {
        return currentAbstractionInformationSets.values().stream().filter(i ->
                i.getAllStates().stream().map(s -> s.getISKeyForPlayerToMove()).distinct().count() > 1
        ).flatMap(i -> i.getAllStates().stream().filter(s ->
                AbstractedStrategyUtils.getProbability(s.getSequenceFor(gameInfo.getAllPlayers()[0]), p0Strategy, currentAbstractionISKeys, expander) > 1e-8 &&
                        AbstractedStrategyUtils.getProbability(s.getSequenceFor(gameInfo.getAllPlayers()[1]), p1Strategy, currentAbstractionISKeys, expander) > 1e-8)
                .map(s -> s.getISKeyForPlayerToMove()).distinct()).count();
    }


    protected long getReachableISCount(Map<ISKey, double[]> p0Strategy, Map<ISKey, double[]> p1Strategy) {
        return currentAbstractionInformationSets.values().stream().filter(i ->
                i.getAllStates().stream().anyMatch(s ->
                        AbstractedStrategyUtils.getProbability(s.getSequenceFor(gameInfo.getAllPlayers()[0]), p0Strategy, currentAbstractionISKeys, expander) > 1e-8 &&
                                AbstractedStrategyUtils.getProbability(s.getSequenceFor(gameInfo.getAllPlayers()[1]), p1Strategy, currentAbstractionISKeys, expander) > 1e-8)
        ).count();
    }

    @Override
    protected void iteration(Player opponent) {
        Map<ISKey, double[]> strategy = getBehavioralStrategyFor(opponent);
        FPIRABestResponse br = getBestResponseAlg(opponent);
        double value = br.calculateBRForAbstractedStrategy(rootState, strategy);
        Map<Action, Double> bestResponse = br.getBestResponse();

        updateAbstractionInformationSets(rootState, bestResponse, strategy, opponent);
    }

    private FPIRABestResponse getBestResponseAlg(Player opponent) {
        return opponent.getId() == 0 ? p1BR : p0BR;
    }

    protected void updateAbstractionInformationSets(GameState state, Map<Action, Double> bestResponse, Map<ISKey, double[]> opponentStrategy, Player opponent) {
        Map<InformationSet, Map<Integer, Map<PerfectRecallISKey, double[]>>> toSplit = new HashMap<>();
        Player currentPlayer = state.getAllPlayers()[1 - opponent.getId()];

        updateISStructure(state, bestResponse, opponentStrategy, opponent, toSplit, 1, 1);
        currentAbstractionInformationSets.values().forEach(i -> ((CFRBRData) i.getData()).updateMeanStrategy());
        if (aboveDelta(getStrategyDiffs(toSplit), getBehavioralStrategyFor(currentPlayer), currentPlayer)) {
            splitISsToPR(toSplit, currentPlayer);
        } else {
            splitISsAccordingToBR(toSplit, currentPlayer);
        }
    }

    protected int updateISStructure(GameState state, Map<Action, Double> bestResponse, Map<ISKey, double[]> opponentStrategy,
                                    Player opponent, Map<InformationSet, Map<Integer, Map<PerfectRecallISKey, double[]>>> toSplit,
                                    double pBR, double pAvg) {
        if (state.isGameEnd())
            return 0;
        if (state.isPlayerToMoveNature())
            return expander.getActions(state).stream().map(state::performAction).mapToInt(s -> updateISStructure(s, bestResponse, opponentStrategy, opponent, toSplit, pBR, pAvg)).sum();
        if (state.getPlayerToMove().equals(opponent)) {
            List<Action> actions = expander.getActions(state);

            return actions.stream().filter(a -> getProbability(opponentStrategy, a, actions) > 1e-8)
                    .map(state::performAction).mapToInt(s -> updateISStructure(s, bestResponse, opponentStrategy, opponent, toSplit, pBR, pAvg)).sum();
        }
        IRCFRInformationSet is = currentAbstractionInformationSets.get(getAbstractionISKey(state));
        List<Action> actions = expander.getActions(state);
        double[] meanStrategy = is.getData().getMp();

        assert actions.stream().filter(a -> bestResponse.getOrDefault(a, 0d) > 1 - 1e-8).count() <= 1;
        assert Math.abs(Arrays.stream(meanStrategy).sum() - 1) < 1e-8 || (Math.abs(Arrays.stream(meanStrategy).sum()) < 1e-8 && iteration == 0);
        int splitCount = expander.getActions(state).stream().filter(a -> getProbability(meanStrategy, a, actions) > 1e-8 || bestResponse.getOrDefault(a, 0d) > 1 - 1e-8)
                .mapToInt(a -> updateISStructure(state.performAction(a), bestResponse, opponentStrategy, opponent, toSplit, bestResponse.getOrDefault(a, 0d), pAvg * getProbability(meanStrategy, a, actions))).sum();
        Set<GameState> isStates = is.getAllStates();

        if (pBR > 1 - 1e-8 && isStates.stream().filter(isState -> isState.getISKeyForPlayerToMove().equals(state.getISKeyForPlayerToMove())).count() != isStates.size()) {
            Action currentStateBestResponseAction = actions.stream().filter(a -> bestResponse.getOrDefault(a, 0d) > 1 - 1e-8).findAny().get();
            int actionIndex = getIndex(actions, currentStateBestResponseAction);
            Map<Integer, Map<PerfectRecallISKey, double[]>> actionMap = toSplit.compute(is, (k, v) -> v == null ? new HashMap<>() : v);
            Map<PerfectRecallISKey, double[]> currentSplitSequences = actionMap.compute(actionIndex, (k, v) -> v == null ? new HashMap<>() : v);
            double[] currentValuePair = currentSplitSequences.compute((PerfectRecallISKey) state.getISKeyForPlayerToMove(), (k, v) -> v == null ? new double[2] : v);

            currentValuePair[0] += 1. / (iteration + 1) * pBR;
            currentValuePair[1] += ((double) iteration) / (iteration + 1) * pAvg;
            return splitCount + 1;
        } else {
            double brIncrement = 1. / (iteration + 1) * pBR;
            double avgIncrement = ((double) iteration) / (iteration + 1) * pAvg;

            if (pBR > 1 - 1e-8) {
                Action currentStateBestResponseAction = actions.stream().filter(a -> bestResponse.getOrDefault(a, 0d) > 1 - 1e-8).findAny().get();
                int actionIndex = getIndex(actions, currentStateBestResponseAction);

                for (int i = 0; i < actions.size(); i++) {
                    ((CFRBRData) is.getData()).addToMeanStrategyUpdateNumerator(i, brIncrement * ((i == actionIndex ? 1 : 0) - meanStrategy[i]));
                }
            }
            ((CFRBRData) is.getData()).addToMeanStrategyUpdateDenominator(brIncrement + avgIncrement);
        }
        return splitCount;
    }

    private double getProbability(Map<ISKey, double[]> strategy, Action action, List<Action> actions) {
        return getProbability(strategy.get(currentAbstractionISKeys.get((PerfectRecallISKey) action.getInformationSet().getISKey(), actions)), action, actions);
    }

    private double getProbability(double[] strategy, Action action, List<Action> actions) {
        return strategy[getIndex(actions, action)];
    }

    protected int getIndex(List<Action> actions, Action bestResponseAction) {
        int index = -1;

        for (Action action : actions) {
            index++;
            if (bestResponseAction.equals(action))
                return index;
        }
        assert false;
        return -1;
    }

    private boolean aboveDelta(FPIRAStrategyDiffs strategyDiffs, Map<ISKey, double[]> strategy, Player player) {
        double delta;

//        assert valid(strategyDiffs, strategy);
        if (player.getId() == 0)
            delta = Math.max(p1Delta.calculateDeltaForAbstractedStrategy(strategy, strategyDiffs),
                    -p1Delta.calculateNegativeDeltaForAbstractedStrategy(strategy, strategyDiffs));
        else
            delta = Math.max(p0Delta.calculateDeltaForAbstractedStrategy(strategy, strategyDiffs),
                    -p0Delta.calculateNegativeDeltaForAbstractedStrategy(strategy, strategyDiffs));
//        System.out.println(delta);
        return delta > 1e-8;
    }

    private boolean valid(StrategyDiffs strategyDiffs, Map<Action, Double> strategy) {
        for (Map<Action, Double> actionDoubleMap : strategyDiffs.prStrategyDiff.values()) {
            for (Map.Entry<Action, Double> actionDoubleEntry : actionDoubleMap.entrySet()) {
                if (strategy.getOrDefault(actionDoubleEntry.getKey(), 0d) + actionDoubleEntry.getValue() < 0)
                    return false;
            }
        }
        for (Map<Action, Double> actionDoubleMap : strategyDiffs.irStrategyDiff.values()) {
            for (Map.Entry<Action, Double> actionDoubleEntry : actionDoubleMap.entrySet()) {
                if (strategy.getOrDefault(actionDoubleEntry.getKey(), 0d) + actionDoubleEntry.getValue() < 0)
                    return false;
            }
        }
        return true;
    }

    protected FPIRAStrategyDiffs getStrategyDiffs(Map<InformationSet, Map<Integer, Map<PerfectRecallISKey, double[]>>> toSplit) {
        FPIRAStrategyDiffs strategyDiffs = new FPIRAStrategyDiffs();

        for (Map.Entry<InformationSet, Map<Integer, Map<PerfectRecallISKey, double[]>>> actionMapEntry : toSplit.entrySet()) {
            for (Map.Entry<Integer, Map<PerfectRecallISKey, double[]>> entry : actionMapEntry.getValue().entrySet()) {
                int actionCount = ((IRCFRInformationSet) actionMapEntry.getKey()).getData().getActionCount();
                double[] meanStratDiffForAction = new double[actionCount];
                double[] meanStrategy = ((IRCFRInformationSet) actionMapEntry.getKey()).getData().getMp();
                double meanStratDiffForActionNormalizer = 0;
                int actionIndex = entry.getKey();

                for (Map.Entry<PerfectRecallISKey, double[]> keyValuesEntry : entry.getValue().entrySet()) {
                    double[] meanStratDiffForKey = new double[actionCount];

//                    if(keyValuesEntry.getValue()[0] + keyValuesEntry.getValue()[1] < 1e-3)
//                        continue;
                    for (int i = 0; i < actionCount; i++) {
                        meanStratDiffForKey[i] = keyValuesEntry.getValue()[0] * ((i == actionIndex ? 1 : 0) - meanStrategy[i]);
                        meanStratDiffForAction[i] += meanStratDiffForKey[i];
                        meanStratDiffForKey[i] /= keyValuesEntry.getValue()[0] + keyValuesEntry.getValue()[1];
                    }
                    meanStratDiffForActionNormalizer += keyValuesEntry.getValue()[0] + keyValuesEntry.getValue()[1];
                    strategyDiffs.prStrategyDiff.put(keyValuesEntry.getKey(), meanStratDiffForKey);
                }
//                if(meanStratDiffForActionNormalizer < 1e-3)
//                    continue;
                for (int i = 0; i < meanStratDiffForAction.length; i++) {
                    meanStratDiffForAction[i] /= meanStratDiffForActionNormalizer;
                }
                actionMapEntry.getKey().getAllStates().stream().map(s -> (PerfectRecallISKey) s.getISKeyForPlayerToMove()).forEach(key ->
                        strategyDiffs.irStrategyDiff.put(key, meanStratDiffForAction)
                );
            }
        }
        return strategyDiffs;
    }

    private Map<Action, Double> toMapNoNorm(List<Action> actions, double[] meanStrat) {
        int index = 0;
        Map<Action, Double> actionMap = new HashMap<>(meanStrat.length);

        for (Action action : actions) {
            actionMap.put(action, meanStrat[index++]);
        }
        return actionMap;
    }

    protected void splitISsToPR(Map<InformationSet, Map<Integer, Map<PerfectRecallISKey, double[]>>> toSplit, Player player) {
        for (Map.Entry<InformationSet, Map<Integer, Map<PerfectRecallISKey, double[]>>> actionMapEntry : toSplit.entrySet()) {
            for (Map.Entry<Integer, Map<PerfectRecallISKey, double[]>> entry : actionMapEntry.getValue().entrySet()) {
                for (Map.Entry<PerfectRecallISKey, double[]> isKeyEntry : entry.getValue().entrySet()) {
                    Set<GameState> isStates = actionMapEntry.getKey().getAllStates();
                    Set<GameState> toRemove = new HashSet<>();

                    isStates.stream().filter(isState -> isState.getISKeyForPlayerToMove().equals(isKeyEntry.getKey())).forEach(toRemove::add);
                    if (toRemove.isEmpty())
                        continue;
                    int actionIndex = entry.getKey();

                    assert actionIndex > -1;
                    IRCFRInformationSet newIS;
                    if (toRemove.size() == isStates.size())
                        newIS = (IRCFRInformationSet) actionMapEntry.getKey();
                    else {
                        isStates.removeAll(toRemove);
                        newIS = createNewIS(toRemove, player, (CFRBRData) ((IRCFRInformationSet) actionMapEntry.getKey()).getData());
                    }
                    double[] meanStrategy = newIS.getData().getMp();

                    for (int i = 0; i < newIS.getData().getActionCount(); i++) {
                        ((CFRBRData) newIS.getData()).addToMeanStrategyUpdateNumerator(i, isKeyEntry.getValue()[0] * ((i == actionIndex ? 1 : 0) - meanStrategy[i]));
                    }
                    ((CFRBRData) newIS.getData()).addToMeanStrategyUpdateDenominator(isKeyEntry.getValue()[0] + isKeyEntry.getValue()[1]);
                    ((CFRBRData) newIS.getData()).updateMeanStrategy();
                    System.err.println("PR " + newIS.getPlayer() + " creating IS in it " + iteration);
                }
            }
        }
    }

    protected void splitISsAccordingToBR(Map<InformationSet, Map<Integer, Map<PerfectRecallISKey, double[]>>> toSplit, Player player) {
        for (Map.Entry<InformationSet, Map<Integer, Map<PerfectRecallISKey, double[]>>> informationSetMapEntry : toSplit.entrySet()) {
            if (informationSetMapEntry.getValue().size() > 1) {
                for (Map.Entry<Integer, Map<PerfectRecallISKey, double[]>> entry : informationSetMapEntry.getValue().entrySet()) {
                    Set<GameState> isStates = informationSetMapEntry.getKey().getAllStates();
                    Set<GameState> toRemove = new HashSet<>();
                    int actionIndex = entry.getKey();

                    for (PerfectRecallISKey key : entry.getValue().keySet()) {
                        isStates.stream().filter(isState -> isState.getISKeyForPlayerToMove().equals(key)).forEach(toRemove::add);
                    }
                    if (toRemove.isEmpty())
                        continue;
                    IRCFRInformationSet newIS;
                    if (toRemove.size() == isStates.size())
                        newIS = (IRCFRInformationSet) informationSetMapEntry.getKey();
                    else {
                        isStates.removeAll(toRemove);
                        newIS = createNewIS(toRemove, player, (CFRBRData) ((IRCFRInformationSet) informationSetMapEntry.getKey()).getData());
                    }
                    double[] meanStrategy = newIS.getData().getMp();

                    for (Map.Entry<PerfectRecallISKey, double[]> isKeyEntry : entry.getValue().entrySet()) {
                        for (int i = 0; i < ((IRCFRInformationSet) informationSetMapEntry.getKey()).getData().getActionCount(); i++) {
                            ((CFRBRData) newIS.getData()).addToMeanStrategyUpdateNumerator(i, isKeyEntry.getValue()[0] * ((i == actionIndex ? 1 : 0) - meanStrategy[i]));
                        }
                        ((CFRBRData) newIS.getData()).addToMeanStrategyUpdateDenominator(isKeyEntry.getValue()[0] + isKeyEntry.getValue()[1]);
                    }
                    ((CFRBRData) newIS.getData()).updateMeanStrategy();
                    System.err.println("!!!BR creating IS in it " + iteration);
                }
            } else {
                for (Map.Entry<Integer, Map<PerfectRecallISKey, double[]>> entry : informationSetMapEntry.getValue().entrySet()) {
                    CFRBRData data = (CFRBRData) ((IRCFRInformationSet) informationSetMapEntry.getKey()).getData();
                    double[] meanStrategy = data.getMp();
                    int actionIndex = entry.getKey();

                    for (Map.Entry<PerfectRecallISKey, double[]> isKeyEntry : entry.getValue().entrySet()) {
                        for (int i = 0; i < ((IRCFRInformationSet) informationSetMapEntry.getKey()).getData().getActionCount(); i++) {
                            data.addToMeanStrategyUpdateNumerator(i, isKeyEntry.getValue()[0] * ((i == actionIndex ? 1 : 0) - meanStrategy[i]));
                        }
                        data.addToMeanStrategyUpdateDenominator(isKeyEntry.getValue()[0] + isKeyEntry.getValue()[1]);
                    }
                    data.updateMeanStrategy();
                }
            }
        }
    }

    protected IRCFRInformationSet createNewIS(Set<GameState> states, Player player, CFRBRData data) {
        ImperfectRecallISKey newISKey = createCounterISKey(player);
        GameState state = states.stream().findAny().get();
        IRCFRInformationSet is = new IRCFRInformationSet(state, newISKey);

        is.addAllStatesToIS(states);
        is.setData(new CFRBRData(data));
        currentAbstractionInformationSets.put(newISKey, is);
        states.forEach(s -> currentAbstractionISKeys.put((PerfectRecallISKey) s.getISKeyForPlayerToMove(), newISKey));
        return is;
    }


}
