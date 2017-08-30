package cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.memeff;

import cz.agents.gtlibrary.algorithms.cfr.ir.IRCFRInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.AlgorithmData;
import cz.agents.gtlibrary.algorithms.mcts.MCTSConfig;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.oos.OOSAlgorithmData;
import cz.agents.gtlibrary.domain.goofspiel.GSGameInfo;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielExpander;
import cz.agents.gtlibrary.domain.goofspiel.IIGoofSpielGameState;
import cz.agents.gtlibrary.domain.poker.generic.GPGameInfo;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerExpander;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerGameState;
import cz.agents.gtlibrary.domain.poker.kuhn.KPGameInfo;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerExpander;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerGameState;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameExpander;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameInfo;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameState;
import cz.agents.gtlibrary.domain.randomgameimproved.io.BasicGameBuilder;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.iinodes.ImperfectRecallISKey;
import cz.agents.gtlibrary.iinodes.PerfectRecallISKey;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.io.GambitEFG;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class IRCFR extends AutomatedAbstractionAlgorithm {
    public static boolean REGRET_MATCHING_PLUS = false;
    protected final MCTSConfig perfectRecallConfig;
    private final FPIRABestResponse p0BR;
    private final FPIRABestResponse p1BR;

    public enum SPLIT_HEURISTIC {
        NONE, AVG_VISITED, VISITED, UPDATED
    }

    public static SPLIT_HEURISTIC heuristic = SPLIT_HEURISTIC.NONE;

    public static void main(String[] args) {
//                runKuhnPoker();
        runGenericPoker();
//        runRandomGame();
    }

    public static void runGenericPoker() {
        GameState root = new GenericPokerGameState();
        MCTSConfig config = new MCTSConfig();
        Expander<MCTSInformationSet> expander = new GenericPokerExpander<>(config);
        GameInfo info = new GPGameInfo();

        prepareGame(root, config, expander);
        IRCFR alg = new IRCFR(root, expander, info, config);

        alg.runIterations(100000);
    }

    public static void runIIGoofspiel() {
        GameState root = new IIGoofSpielGameState();
        MCTSConfig config = new MCTSConfig();
        Expander<MCTSInformationSet> expander = new GoofSpielExpander<>(config);
        GameInfo info = new GSGameInfo();

        prepareGame(root, config, expander);
        IRCFR alg = new IRCFR(root, expander, info, config);

        alg.runIterations(100000);
    }

    public static void runRandomGame() {
        GameState root = new RandomGameState();
        MCTSConfig config = new MCTSConfig();
        Expander<MCTSInformationSet> expander = new RandomGameExpander<>(config);

        prepareGame(root, config, expander);
        IRCFR alg = new IRCFR(root, expander, new RandomGameInfo(), config);

        alg.runIterations(100000);
        GambitEFG gambit = new GambitEFG();

        gambit.buildAndWrite("rgIRCFR.gbt", root, expander);
    }

    private static void runKuhnPoker() {
        GameState root = new KuhnPokerGameState();
        MCTSConfig config = new MCTSConfig();
        GameInfo info = new KPGameInfo();
        Expander<MCTSInformationSet> expander = new KuhnPokerExpander<>(config);

        prepareGame(root, config, expander);
        IRCFR alg = new IRCFR(root, expander, info, config);

        alg.runIterations(100000);
    }

    public static void prepareGame(GameState root, MCTSConfig config, Expander<MCTSInformationSet> expander) {
        BasicGameBuilder builder = new BasicGameBuilder();

        builder.buildWithoutTerminalIS(root, config, expander);
        config.getAllInformationSets().values().stream().filter(i -> i.getPlayer().getId() != 2).forEach(i -> i.setAlgorithmData(new IRCFRData(expander.getActions(i.getAllStates().stream().findAny().get()))));
    }

    public IRCFR(GameState rootState, Expander<? extends InformationSet> perfectRecallExpander, GameInfo info, MCTSConfig perfectRecallConfig) {
        super(rootState, perfectRecallExpander, info);
        this.perfectRecallConfig = perfectRecallConfig;
        p0BR = new FPIRABestResponse(this.rootState, this.perfectRecallExpander, 0, new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]}, perfectRecallExpander.getAlgorithmConfig(), info, false, currentAbstractionISKeys);
        p1BR = new FPIRABestResponse(this.rootState, this.perfectRecallExpander, 1, new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]}, perfectRecallExpander.getAlgorithmConfig(), info, false, currentAbstractionISKeys);
    }

    @Override
    protected boolean isConverged(double v) {
        return false;
    }

    @Override
    protected void printStatistics() {
        super.printStatistics();
        Map<ISKey, double[]> p0Strategy = getBehavioralStrategyFor(rootState.getAllPlayers()[0]);
        Map<ISKey, double[]> p1Strategy = getBehavioralStrategyFor(rootState.getAllPlayers()[1]);

        assert validStrategy(p0Strategy);
        assert validStrategy(p1Strategy);
        removeSmallValues(p0Strategy);
        removeSmallValues(p1Strategy);

//        p0Strategy.forEach((k, v) -> System.out.print(k+ ": " + Arrays.toString(v)));
        if (iteration > 1) {
            System.out.println("p0BR: " + p0BR.calculateBRForAbstractedStrategy(rootState, p1Strategy));
            System.out.println("p1BR: " + -p1BR.calculateBRForAbstractedStrategy(rootState, p0Strategy));
        }
        System.out.println("ISs in perfect recall config: " + perfectRecallConfig.getAllInformationSets().values().stream().filter(i -> i.getPlayer().getId() != 2).count());
        System.out.println("Reachable IS count: " + getReachableISCountFromOriginalGame(p0Strategy, p1Strategy));
        System.out.println("Reachable abstracted IS count: " + getReachableAbstractedISCountFromOriginalGame(p0Strategy, p1Strategy));
    }

    private boolean validStrategy(Map<ISKey, double[]> strategy) {
        return strategy.values().stream().allMatch(array -> Math.abs(Arrays.stream(array).sum() - 1) < 1e-8);
    }

    private void removeSmallValues(Map<ISKey, double[]> strategy) {
        strategy.values().forEach(array -> {
            IntStream.range(0, array.length).forEach(i -> {
                if (array[i] < 1e-4)
                    array[i] = 0;
            });
            normalize(array);
        });
    }

    private void normalize(double[] array) {
        double sum = Arrays.stream(array).sum();

        IntStream.range(0, array.length).forEach(i -> array[i] /= sum);
    }

    protected Map<ISKey, double[]> getBehavioralStrategyFor(Player player) {
        Map<ISKey, double[]> strategy = new HashMap<>(currentAbstractionInformationSets.size() / 2);

        currentAbstractionInformationSets.values().stream().filter(is -> is.getPlayer().equals(player)).forEach(is ->
                strategy.put(is.getISKey(), ((IRCFRData) is.getData()).getNormalizedMeanStrategy())
        );
        return strategy;
    }

    @Override
    protected void iteration(Player player) {
        replacePerfectRecallDataWithImperfectRecall();
        perfectRecallIteration(rootState, 1, 1, player);
        updatePerfectRecallData();
        setVisitedByAvgStrategy();
        imperfectRecallIteration(rootState, 1, 1, player);
        updateImperfectRecallData();
        Map<ImperfectRecallISKey, Map<PerfectRecallISKey, OOSAlgorithmData>> regretDifferences = getRegretDifferences();

        updateAbstraction(regretDifferences);
        markUnvisited();
    }

    protected void setVisitedByAvgStrategy() {
        Map<ISKey, double[]> p0Strategy = getBehavioralStrategyFor(rootState.getAllPlayers()[0]);
        Map<ISKey, double[]> p1Strategy = getBehavioralStrategyFor(rootState.getAllPlayers()[1]);

        setVisitedByAvgStrategy(rootState, p0Strategy, p1Strategy, 1d);
    }

    private void setVisitedByAvgStrategy(GameState state, Map<ISKey, double[]> p0Strategy, Map<ISKey, double[]> p1Strategy, double prob) {
        if (state.isGameEnd())
            return;
        if (prob < 1e-6)
            return;
        if (!state.isPlayerToMoveNature())
            ((IRCFRData) perfectRecallConfig.getAllInformationSets().get(state.getISKeyForPlayerToMove()).getAlgorithmData()).setVisitedByAvgStrategy(true);
        perfectRecallExpander.getActions(state).forEach(a ->
                setVisitedByAvgStrategy(state.performAction(a), p0Strategy, p1Strategy, prob * getProbabilityOf(a, state, p0Strategy, p1Strategy))
        );
    }

    private double getProbabilityOf(Action action, GameState state, Map<ISKey, double[]> p0Strategy, Map<ISKey, double[]> p1Strategy) {
        if (state.isPlayerToMoveNature())
            return 1;
        if (state.getPlayerToMove().getId() == 0)
            return AbstractedStrategyUtils.getProbabilityForAction(action, p0Strategy, currentAbstractionISKeys, perfectRecallExpander);
        return AbstractedStrategyUtils.getProbabilityForAction(action, p1Strategy, currentAbstractionISKeys, perfectRecallExpander);
    }

    private void markUnvisited() {
        perfectRecallConfig.getAllInformationSets().values().stream().filter(i -> i.getPlayer().getId() != 2)
                .forEach(i -> {
                    ((IRCFRData) i.getAlgorithmData()).setVisitedInLastIteration(false);
                    ((IRCFRData) i.getAlgorithmData()).setVisitedByAvgStrategy(false);
                });
    }

    protected void replacePerfectRecallDataWithImperfectRecall() {
        perfectRecallConfig.getAllInformationSets().values().stream().filter(i -> i.getPlayer().getId() != 2)
                .forEach(i ->
                        ((OOSAlgorithmData) i.getAlgorithmData())
                                .setFrom(getAbstractedInformationSet(i.getAllStates().stream().findAny().get()).getData())
                );
    }

    protected void updatePerfectRecallData() {
        perfectRecallConfig.getAllInformationSets().values().stream().filter(i -> i.getPlayer().getId() != 2)
                .forEach(i -> ((IRCFRData) i.getAlgorithmData()).applyUpdate());
    }

    private void updateAbstraction(Map<ImperfectRecallISKey, Map<PerfectRecallISKey, OOSAlgorithmData>> regretDifferences) {
        regretDifferences.forEach((irISKey, splitMap) -> {
            IRCFRInformationSet setToSplit = currentAbstractionInformationSets.get(irISKey);
            Set<GameState> isStates = setToSplit.getAllStates();

            splitMap.forEach((prISKey, data) -> {
                Set<GameState> toRemove = isStates.stream().filter(isState -> isState.getISKeyForPlayerToMove().equals(prISKey)).collect(Collectors.toSet());

                if (!toRemove.isEmpty()) {
                    if (toRemove.size() < isStates.size()) {
                        isStates.removeAll(toRemove);
                        createNewIS(toRemove, data);
                    } else {
                        setToSplit.setData(new IRCFRData(data));
                    }
                }
            });
        });
    }

    protected IRCFRInformationSet createNewIS(Set<GameState> states, OOSAlgorithmData data) {
        GameState state = states.stream().findAny().get();
        ImperfectRecallISKey newISKey = createCounterISKey(state.getPlayerToMove());
        IRCFRInformationSet is = new IRCFRInformationSet(state, newISKey);

        is.addAllStatesToIS(states);
        is.setData(new IRCFRData(data));
        currentAbstractionInformationSets.put(newISKey, is);
        states.forEach(s -> currentAbstractionISKeys.put((PerfectRecallISKey) s.getISKeyForPlayerToMove(), newISKey));
        return is;
    }

    protected Map<ImperfectRecallISKey, Map<PerfectRecallISKey, OOSAlgorithmData>> getRegretDifferences() {
        Map<ImperfectRecallISKey, Map<PerfectRecallISKey, OOSAlgorithmData>> toSplit = new HashMap<>();

        currentAbstractionInformationSets.values().stream().filter(i -> i.getPlayer().getId() != 2).forEach(i -> {
            double[] abstractionRegrets = i.getData().getRegrets();
            Set<ISKey> isKeys = i.getAllStates().stream().filter(s -> !s.isPlayerToMoveNature())
                    .map(s -> s.getISKeyForPlayerToMove()).collect(Collectors.toSet());

            isKeys.forEach(k -> {
                IRCFRData algorithmData = (IRCFRData) perfectRecallConfig.getAllInformationSets().get(k).getAlgorithmData();

                if ((splitAccordingToHeuristic(algorithmData)) &&
                        IntStream.range(0, abstractionRegrets.length).anyMatch(j -> Math.abs(abstractionRegrets[j] - algorithmData.getRegrets()[j]) >= 1e-6)) {
                    Map<PerfectRecallISKey, OOSAlgorithmData> dataMap = toSplit.computeIfAbsent((ImperfectRecallISKey) i.getISKey(), key -> new HashMap<>());

                    dataMap.put((PerfectRecallISKey) k, algorithmData);
                }
            });
        });
        return toSplit;
    }

    private boolean splitAccordingToHeuristic(IRCFRData algorithmData) {
        if (heuristic == SPLIT_HEURISTIC.AVG_VISITED)
            return algorithmData.getVisitedByAvgStrategy();
        if (heuristic == SPLIT_HEURISTIC.UPDATED)
            return algorithmData.updatedInLastIteration;
        if (heuristic == SPLIT_HEURISTIC.VISITED)
            return algorithmData.visitedInLastIteration;
        else
            return true;
    }

    protected void updateImperfectRecallData() {
        currentAbstractionInformationSets.values().forEach(i ->
            ((IRCFRData) i.getData()).applyUpdate()
        );
    }

    protected double perfectRecallIteration(GameState node, double pi1, double pi2, Player expPlayer) {
        if (pi1 <= 1e-6 && pi2 <= 1e-6)
            return 0;
        if (node.isGameEnd())
            return node.getUtilities()[expPlayer.getId()];
        MCTSInformationSet informationSet = perfectRecallConfig.getAllInformationSets().get(node.getISKeyForPlayerToMove());

        if (informationSet == null) {
            informationSet = perfectRecallConfig.createInformationSetFor(node);
            perfectRecallConfig.addInformationSetFor(node, informationSet);
            informationSet.setAlgorithmData(createPerfectRecallAlgData(node));
        }
        if (!informationSet.getAllStates().contains(node)) {
            perfectRecallConfig.addInformationSetFor(node, informationSet);
        }
        OOSAlgorithmData data = (OOSAlgorithmData) informationSet.getAlgorithmData();
        List<Action> actions = data.getActions();

        if (pi1 > 1e-6 && pi2 > 1e-6)
            ((IRCFRData) data).setVisitedInLastIteration(true);
        if (node.isPlayerToMoveNature()) {
            double expectedValue = 0;

            for (Action ai : actions) {
                final double p = node.getProbabilityOfNatureFor(ai);
                double new_p1 = expPlayer.getId() == 1 ? pi1 * p : pi1;
                double new_p2 = expPlayer.getId() == 0 ? pi2 * p : pi2;
                GameState newState = node.performAction(ai);

                expectedValue += p * perfectRecallIteration(newState, new_p1, new_p2, expPlayer);
            }
            return expectedValue;
        }
        double[] currentStrategy = getStrategy(data);
        double[] expectedValuesForActions = new double[currentStrategy.length];
        double expectedValue = 0;
        int i = -1;

        for (Action ai : actions) {
            i++;
            GameState newState = node.performAction(ai);

            if (informationSet.getPlayer().getId() == 0) {
                expectedValuesForActions[i] = perfectRecallIteration(newState, pi1 * currentStrategy[i], pi2, expPlayer);
            } else {
                expectedValuesForActions[i] = perfectRecallIteration(newState, pi1, currentStrategy[i] * pi2, expPlayer);
            }
            expectedValue += currentStrategy[i] * expectedValuesForActions[i];
        }
        if (informationSet.getPlayer().equals(expPlayer))
            updateData(node, pi1, pi2, expPlayer, data, expectedValuesForActions, expectedValue);
        return expectedValue;
    }

    protected double imperfectRecallIteration(GameState node, double pi1, double pi2, Player expPlayer) {
        if (pi1 <= 1e-6 && pi2 <= 1e-6)
            return 0;
        if (node.isGameEnd())
            return node.getUtilities()[expPlayer.getId()];
        IRCFRInformationSet informationSet = getAbstractedInformationSet(node);
        OOSAlgorithmData data = informationSet.getData();
        List<Action> actions = perfectRecallExpander.getActions(node);

        if (node.isPlayerToMoveNature()) {
            double expectedValue = 0;

            for (Action ai : actions) {
                final double p = node.getProbabilityOfNatureFor(ai);
                double new_p1 = expPlayer.getId() == 1 ? pi1 * p : pi1;
                double new_p2 = expPlayer.getId() == 0 ? pi2 * p : pi2;
                GameState newState = node.performAction(ai);

                expectedValue += p * imperfectRecallIteration(newState, new_p1, new_p2, expPlayer);
            }
            return expectedValue;
        }

        double[] currentStrategy = getStrategy(data);
        double[] expectedValuesForActions = new double[currentStrategy.length];
        double expectedValue = 0;
        int i = -1;

        for (Action ai : actions) {
            i++;
            GameState newState = node.performAction(ai);

            if (informationSet.getPlayer().getId() == 0) {
                expectedValuesForActions[i] = imperfectRecallIteration(newState, pi1 * currentStrategy[i], pi2, expPlayer);
            } else {
                expectedValuesForActions[i] = imperfectRecallIteration(newState, pi1, currentStrategy[i] * pi2, expPlayer);
            }
            expectedValue += currentStrategy[i] * expectedValuesForActions[i];
        }
        if (informationSet.getPlayer().equals(expPlayer))
            updateData(node, pi1, pi2, expPlayer, data, expectedValuesForActions, expectedValue);
        return expectedValue;
    }

    protected IRCFRInformationSet getAbstractedInformationSet(GameState node) {
        return currentAbstractionInformationSets.get(currentAbstractionISKeys.get((PerfectRecallISKey) node.getISKeyForPlayerToMove(), perfectRecallExpander.getActions(node)));
    }

    protected void updateData(GameState state, double pi1, double pi2, Player expPlayer, OOSAlgorithmData data,
                              double[] expectedValuesForActions, double expectedValue) {
        ((IRCFRData) data).updateAllRegrets(expectedValuesForActions, expectedValue, (expPlayer.getId() == 0 ? pi2 : pi1), state, (expPlayer.getId() == 0 ? pi1 : pi2));
    }

    protected AlgorithmData createPerfectRecallAlgData(GameState node) {
        IRCFRData prData = new IRCFRData(perfectRecallExpander.getActions(node));

        if (!node.isPlayerToMoveNature()) {
            OOSAlgorithmData data = getAbstractedInformationSet(node).getData();

            prData.setFrom(data);
        }
        return prData;
    }

    protected void addData(Collection<IRCFRInformationSet> informationSets) {
        informationSets.forEach(i -> i.setData(new IRCFRData(this.perfectRecallExpander.getActions(i.getAllStates().stream().findAny().get()).size())));
    }

    protected double[] getStrategy(OOSAlgorithmData data) {
        return data.getRMStrategy();
    }
}
