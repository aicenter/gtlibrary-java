package cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.memeff.cfr;

import cz.agents.gtlibrary.algorithms.cfr.ir.IRCFRInformationSet;
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
import cz.agents.gtlibrary.domain.pursuit.PursuitExpander;
import cz.agents.gtlibrary.domain.pursuit.PursuitGameInfo;
import cz.agents.gtlibrary.domain.pursuit.VisibilityPursuitGameState;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameExpander;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameInfo;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameState;
import cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.memeff.AutomatedAbstractionData;
import cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.memeff.MemEffAbstractedInformationSet;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.iinodes.ImperfectRecallISKey;
import cz.agents.gtlibrary.iinodes.PerfectRecallISKey;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.io.GambitEFG;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MaxRegretIRCFR extends IRCFR {
    public static boolean LOG_REGRETS = false;
    public static boolean SIMULTANEOUS_PR_IR = true;
    public static boolean CLEAR_DATA = true;
    public static boolean DELETE_REGRETS = true;
    //    public static boolean USE_AVG_STRAT = false;
    public static boolean USE_SPLIT_TOLERANCE = true;
    public static double ITERATION_MULTIPLIER = 100;

    protected Map<ISKey, double[]> prRegrets;
    protected Map<ISKey, double[]> regretLog;

    public static void main(String[] args) {
//        runRandomGame();
//        runKuhnPoker();
//        runGenericPoker();
//        runGenericPoker("backup.ser");
        runIIGoofspiel();
//        runIIGoofspiel("backup.ser");
    }

    public static void runIIGoofspiel() {
        GameState root = new IIGoofSpielGameState();
        MCTSConfig config = new MCTSConfig();
        Expander<MCTSInformationSet> expander = new GoofSpielExpander<>(config);
        GameInfo info = new GSGameInfo();
        MaxRegretIRCFR alg = new MaxRegretIRCFR(root, expander, info, config);

        alg.runIterations(10000000);
    }

    public static void runIIGoofspiel(String backupFileName) {
        GameState root = new IIGoofSpielGameState();
        MCTSConfig config = new MCTSConfig();
        Expander<MCTSInformationSet> expander = new GoofSpielExpander<>(config);
        GameInfo info = new GSGameInfo();
        try {
            FileInputStream fin = new FileInputStream(backupFileName);
            ObjectInputStream oos = new ObjectInputStream(fin);
            AutomatedAbstractionData data = (AutomatedAbstractionData) oos.readObject();
            MaxRegretIRCFR alg = new MaxRegretIRCFR(root, expander, info, config, data);

            alg.runIterations(10000000);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void runRandomGame() {
        GameState root = new RandomGameState();
        MCTSConfig config = new MCTSConfig();
        Expander<MCTSInformationSet> expander = new RandomGameExpander<>(config);

        IRCFR.prepareGame(root, config, expander);
        MaxRegretIRCFR alg = new MaxRegretIRCFR(root, expander, new RandomGameInfo(), config);

        alg.runIterations(100000);
        GambitEFG gambit = new GambitEFG();

        gambit.buildAndWrite("rgIRCFR.gbt", root, expander);
    }

    public static void runGenericPoker() {
        GameState root = new GenericPokerGameState();
        MCTSConfig config = new MCTSConfig();
        Expander<MCTSInformationSet> expander = new GenericPokerExpander<>(config);
        GameInfo info = new GPGameInfo();
        MaxRegretIRCFR alg = new MaxRegretIRCFR(root, expander, info, config);

        alg.runIterations(10000000);
    }

    public static void runGenericPoker(String backupFileName) {
        GameState root = new GenericPokerGameState();
        MCTSConfig config = new MCTSConfig();
        Expander<MCTSInformationSet> expander = new GenericPokerExpander<>(config);
        GameInfo info = new GPGameInfo();
        try {
            FileInputStream fin = new FileInputStream(backupFileName);
            ObjectInputStream oos = new ObjectInputStream(fin);
            AutomatedAbstractionData data = (AutomatedAbstractionData) oos.readObject();
            MaxRegretIRCFR alg = new MaxRegretIRCFR(root, expander, info, config, data);

            alg.runIterations(10000000);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void runVisibilityPursuit() {
        GameState root = new VisibilityPursuitGameState();
        MCTSConfig config = new MCTSConfig();
        Expander<MCTSInformationSet> expander = new PursuitExpander<>(config);
        GameInfo info = new PursuitGameInfo();
        MaxRegretIRCFR alg = new MaxRegretIRCFR(root, expander, info, config);

        alg.runIterations(10000000);
    }

    private static void runKuhnPoker() {
        GameState root = new KuhnPokerGameState();
        MCTSConfig config = new MCTSConfig();
        GameInfo info = new KPGameInfo();
        Expander<MCTSInformationSet> expander = new KuhnPokerExpander<>(config);
        MaxRegretIRCFR alg = new MaxRegretIRCFR(root, expander, info, config);

        alg.runIterations(100000);
    }

    public MaxRegretIRCFR(GameState rootState, Expander<? extends InformationSet> perfectRecallExpander, GameInfo info, MCTSConfig perfectRecallConfig) {
        super(rootState, perfectRecallExpander, info, perfectRecallConfig);
        prRegrets = new HashMap<>();
        if (LOG_REGRETS)
            regretLog = new HashMap<>();
    }

    public MaxRegretIRCFR(GameState rootState, Expander<? extends InformationSet> perfectRecallExpander, GameInfo info, MCTSConfig perfectRecallConfig, AutomatedAbstractionData data) {
        super(rootState, perfectRecallExpander, info, perfectRecallConfig, data);
        prRegrets = new HashMap<>();
        if (LOG_REGRETS)
            regretLog = new HashMap<>();
    }


    @Override
    protected void iteration(Player player) {
        if (SIMULTANEOUS_PR_IR)
            perfectAndImperfectRecallIteration(rootState, 1, 1, player);
        else
            imperfectRecallIteration(rootState, 1, 1, player);
        updateImperfectRecallData();
        if (!SIMULTANEOUS_PR_IR)
            computeCurrentRegrets(rootState, 1, 1, player);
        if (REGRET_MATCHING_PLUS)
            removeNegativePRRegrets();
        if (USE_ABSTRACTION)
            updateAbstraction();
        if (DELETE_REGRETS)
            prRegrets.clear();
        System.gc();
    }

    @Override
    protected void printStatistics() {
        super.printStatistics();
        if (LOG_REGRETS)
            printRegretStat();
    }

    protected void printRegretStat() {
        System.out.println("Max immediate regret: " + regretLog.values().stream()
                .mapToDouble(regrets -> Arrays.stream(regrets).max().getAsDouble() / iteration).max().getAsDouble());
        System.out.println("Regret bound without constant and actions: " + 1. / Math.sqrt(iteration));
    }

    protected double perfectAndImperfectRecallIteration(GameState node, double pi1, double pi2, Player expPlayer) {
        if (pi1 <= 1e-6 && pi2 <= 1e-6)
            return 0;
        if (node.isGameEnd())
            return node.getUtilities()[expPlayer.getId()];
        List<Action> actions = perfectRecallExpander.getActions(node);

        if (node.isPlayerToMoveNature()) {
            double expectedValue = 0;

            for (Action ai : actions) {
                final double p = node.getProbabilityOfNatureFor(ai);
                double new_p1 = expPlayer.getId() == 1 ? pi1 * p : pi1;
                double new_p2 = expPlayer.getId() == 0 ? pi2 * p : pi2;
                GameState newState = node.performAction(ai);

                expectedValue += p * perfectAndImperfectRecallIteration(newState, new_p1, new_p2, expPlayer);
            }
            return expectedValue;
        }
        MemEffAbstractedInformationSet informationSet = getAbstractedInformationSet(node);
        OOSAlgorithmData data = informationSet.getData();

        double[] currentStrategy = getStrategy(data);
        double[] expectedValuesForActions = new double[currentStrategy.length];
        double expectedValue = 0;
        int i = -1;

        storeProbabilityForReachingIS(node, pi1, pi2);
        if (informationSet.getPlayer().getId() == 0) {
            for (Action ai : actions) {
                i++;
                GameState newState = node.performAction(ai);

                expectedValuesForActions[i] = perfectAndImperfectRecallIteration(newState, pi1 * currentStrategy[i], pi2, expPlayer);
                expectedValue += currentStrategy[i] * expectedValuesForActions[i];
            }
        } else {
            for (Action ai : actions) {
                i++;
                GameState newState = node.performAction(ai);

                expectedValuesForActions[i] = perfectAndImperfectRecallIteration(newState, pi1, currentStrategy[i] * pi2, expPlayer);
                expectedValue += currentStrategy[i] * expectedValuesForActions[i];
            }
        }
        if (informationSet.getPlayer().equals(expPlayer)) {
            updateData(node, pi1, pi2, expPlayer, data, expectedValuesForActions, expectedValue);
            if (informationSet.getAbstractedKeys().size() > 1)
                updateCurrentRegrets(node, pi1, pi2, expPlayer, expectedValuesForActions, expectedValue);
            if (LOG_REGRETS)
                updateRegretLog(node, pi1, pi2, expPlayer, expectedValuesForActions, expectedValue);
        }
        return expectedValue;
    }

    protected void storeProbabilityForReachingIS(GameState node, double pi1, double pi2) {
    }

    private void updateRegretLog(GameState node, double pi1, double pi2, Player expPlayer, double[] expectedValuesForActions, double expectedValue) {
        double[] regret = regretLog.computeIfAbsent(node.getISKeyForPlayerToMove(),
                k -> new double[expectedValuesForActions.length]);
        double currentProb = (expPlayer.getId() == 0 ? pi2 : pi1);

        for (int i = 0; i < regret.length; i++) {
            regret[i] += currentProb * (expectedValuesForActions[i] - expectedValue);
        }
    }

    protected void removeNegativePRRegrets() {
        prRegrets.values().forEach(array ->
                IntStream.range(0, array.length).forEach(i -> array[i] = Math.max(array[i], 0))
        );

    }

    protected void updateAbstraction() {
        new ArrayList<>(currentAbstractionInformationSets.values()).stream().forEach(i -> {
            assert i.getPlayer().getId() != 2;
            Map<Set<Integer>, Set<ISKey>> compatibleISs = new HashMap<>();
            Set<ISKey> notVisitedISs = new HashSet<>();

            i.getAbstractedKeys().forEach(key -> {
                double[] regrets = prRegrets.get(key);

                if (regrets != null) {
                    double max = Double.NEGATIVE_INFINITY;

                    for (int j = 0; j < regrets.length; j++) {
                        if (regrets[j] > max)
                            max = regrets[j];
                    }
                    Set<Integer> maxRegretActionIndices = new HashSet<>();

                    for (int j = 0; j < regrets.length; j++) {
                        if (regrets[j] > max - (USE_SPLIT_TOLERANCE ? 0.2 / Math.sqrt(iteration) : 1e-8))
                            maxRegretActionIndices.add(j);
                    }
                    compatibleISs.computeIfAbsent(maxRegretActionIndices, k -> new HashSet<>()).add(key);
                } else {
                    notVisitedISs.add(key);
                }
            });
            Set<GameState> isStates = i.getAllStates();
            Set<Integer> maxSizeKey = distributeUnvisited(compatibleISs, notVisitedISs);

            if (CLEAR_DATA) {
                updateWithClearData(i, compatibleISs, isStates, maxSizeKey);
            } else {
                updateWithReusedData(i, compatibleISs, isStates, maxSizeKey);
            }
        });
    }

    protected void updateWithReusedData(MemEffAbstractedInformationSet i, Map<Set<Integer>, Set<ISKey>> compatibleISs, Set<GameState> isStates, Set<Integer> maxSizeKey) {
        compatibleISs.forEach((maxRegretActionIndices, isKeys) -> {
            Set<GameState> toRemove = isStates.stream().filter(isState -> isKeys.contains(isState.getISKeyForPlayerToMove())).collect(Collectors.toSet());

            if (!maxRegretActionIndices.equals(maxSizeKey)) {
                if (toRemove.size() < isStates.size()) {
                    isStates.removeAll(toRemove);
                    i.getAbstractedKeys().removeAll(isKeys);
                    createNewIS(toRemove, i.getData());
                }
            }
        });
    }

    protected void updateWithClearData(MemEffAbstractedInformationSet i, Map<Set<Integer>, Set<ISKey>> compatibleISs, Set<GameState> isStates, Set<Integer> maxSizeKey) {
        compatibleISs.forEach((maxRegretActionIndices, isKeys) -> {
            Set<GameState> toRemove = isStates.stream().filter(isState -> isKeys.contains(isState.getISKeyForPlayerToMove())).collect(Collectors.toSet());

            if (!maxRegretActionIndices.equals(maxSizeKey)) {
                if (toRemove.size() < isStates.size()) {
                    isStates.removeAll(toRemove);
                    i.getAbstractedKeys().removeAll(isKeys);
                    createNewISNoDataCopy(toRemove, new IRCFRData(i.getData().getActionCount()));
                }
            }
        });
    }


    protected IRCFRInformationSet createNewISNoDataCopy(Set<GameState> states, OOSAlgorithmData data) {
        GameState state = states.stream().findAny().get();
        ImperfectRecallISKey newISKey = createCounterISKey(state.getPlayerToMove());
        MemEffAbstractedInformationSet is = createInformationSet(state, newISKey);

        is.addAllStatesToIS(states);
        is.setData(data);
        currentAbstractionInformationSets.put(newISKey, is);
        states.forEach(s -> currentAbstractionISKeys.put((PerfectRecallISKey) s.getISKeyForPlayerToMove(), newISKey));
        return is;
    }


    protected Set<Integer> distributeUnvisited(Map<Set<Integer>, Set<ISKey>> compatibleISs, Set<ISKey> notVisitedISs) {
        if (compatibleISs.isEmpty())
            return null;
        Set<Integer> maxSizeKey = null;
        int maxSize = -1;

        for (Map.Entry<Set<Integer>, Set<ISKey>> entry : compatibleISs.entrySet()) {
            if (maxSize < entry.getValue().size()) {
                maxSize = entry.getValue().size();
                maxSizeKey = entry.getKey();
            }
        }
        compatibleISs.get(maxSizeKey).addAll(notVisitedISs);
        return maxSizeKey;
    }

    private Set<ISKey>[] createNonCompatibleISs(IRCFRInformationSet i) {
        Set<ISKey>[] nonCompatibleISs = new Set[i.getData().getActionCount()];

        for (int j = 0; j < nonCompatibleISs.length; j++) {
            nonCompatibleISs[j] = new HashSet<>();
        }
        return nonCompatibleISs;
    }

    protected double computeCurrentRegrets(GameState node, double pi1, double pi2, Player expPlayer) {
        if (pi1 <= 1e-6 && pi2 <= 1e-6)
            return 0;
        if (node.isGameEnd())
            return node.getUtilities()[expPlayer.getId()];
        List<Action> actions = perfectRecallExpander.getActions(node);

        if (node.isPlayerToMoveNature()) {
            double expectedValue = 0;

            for (Action ai : actions) {
                final double p = node.getProbabilityOfNatureFor(ai);
                double new_p1 = expPlayer.getId() == 1 ? pi1 * p : pi1;
                double new_p2 = expPlayer.getId() == 0 ? pi2 * p : pi2;
                GameState newState = node.performAction(ai);

                expectedValue += p * computeCurrentRegrets(newState, new_p1, new_p2, expPlayer);
            }
            return expectedValue;
        }
        MemEffAbstractedInformationSet informationSet = getAbstractedInformationSet(node);
        OOSAlgorithmData data = informationSet.getData();
        double[] currentStrategy = getStrategy(data);
        double[] expectedValuesForActions = new double[currentStrategy.length];
        double expectedValue = 0;
        int i = -1;

        for (Action ai : actions) {
            i++;
            GameState newState = node.performAction(ai);

            if (informationSet.getPlayer().getId() == 0) {
                expectedValuesForActions[i] = computeCurrentRegrets(newState, pi1 * currentStrategy[i], pi2, expPlayer);
            } else {
                expectedValuesForActions[i] = computeCurrentRegrets(newState, pi1, currentStrategy[i] * pi2, expPlayer);
            }
            expectedValue += currentStrategy[i] * expectedValuesForActions[i];
        }
        if (informationSet.getPlayer().equals(expPlayer) && informationSet.getAbstractedKeys().size() > 1) {
            updateCurrentRegrets(node, pi1, pi2, expPlayer, expectedValuesForActions, expectedValue);
            if (LOG_REGRETS)
                updateRegretLog(node, pi1, pi2, expPlayer, expectedValuesForActions, expectedValue);
        }
        return expectedValue;
    }

    protected void updateCurrentRegrets(GameState node, double pi1, double pi2, Player expPlayer,
                                        double[] expectedValuesForActions, double expectedValue) {
        double[] regret = prRegrets.computeIfAbsent(node.getISKeyForPlayerToMove(),
                k -> new double[expectedValuesForActions.length]);
        double currentProb = (expPlayer.getId() == 0 ? pi2 : pi1);

        for (int i = 0; i < regret.length; i++) {
            regret[i] += currentProb * (expectedValuesForActions[i] - expectedValue);
        }
    }

    protected double[] getStrategy(OOSAlgorithmData data) {
//        if (USE_AVG_STRAT && iteration > 1)
//            return ((IRCFRData) data).getNormalizedMeanStrategy();
        return data.getRMStrategy();
    }

    private double computeRegret(Map<ISKey, double[]> currentISPRRegrets) {
        return currentISPRRegrets.values().stream().flatMapToDouble(regret -> Arrays.stream(regret)).sum();
    }

    private double computeRegret(double[] irRegret) {
        return Arrays.stream(irRegret).sum();
    }


}
