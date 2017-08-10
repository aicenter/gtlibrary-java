package cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.memeff;

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
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameExpander;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameInfo;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameState;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.io.GambitEFG;

import java.util.*;
import java.util.stream.Collectors;

public class MaxRegretIRCFR extends IRCFR {

    public static boolean DELETE_REGRETS = true;
    public static boolean USE_AVG_STRAT = false;
    public static double ITERATION_MULTIPLIER = 100;

    private Map<ISKey, double[]> prRegrets;

    public static void main(String[] args) {
//        runRandomGame();
//        runKuhnPoker();
//        runGenericPoker();
        runIIGoofspiel();
    }

    public static void runIIGoofspiel() {
        GameState root = new IIGoofSpielGameState();
        MCTSConfig config = new MCTSConfig();
        Expander<MCTSInformationSet> expander = new GoofSpielExpander<>(config);
        GameInfo info = new GSGameInfo();
        MaxRegretIRCFR alg = new MaxRegretIRCFR(root, expander, info, config);

        alg.runIterations(10000000);
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
    }

    @Override
    protected void iteration(Player player) {
        imperfectRecallIteration(rootState, 1, 1, player);
        updateImperfectRecallData();
//        if (iteration % 1 == 0) {
        computeCurrentRegrets(rootState, 1, 1, rootState.getAllPlayers()[0]);
        computeCurrentRegrets(rootState, 1, 1, rootState.getAllPlayers()[1]);
        updateAbstraction();
        if (DELETE_REGRETS)
            prRegrets.clear();
//        }
    }

    private void updateAbstraction() {
        new HashMap<>(currentAbstractionInformationSets).values().stream().filter(i -> i.getPlayer().getId() != 2).forEach(i -> {
            Map<Set<Integer>, Set<ISKey>> compatibleISs = new HashMap<>();
            Set<ISKey> notVisitedISs = new HashSet<>();

            i.getAllStates().stream().map(s -> s.getISKeyForPlayerToMove()).distinct().forEach(key -> {
                double[] regrets = prRegrets.get(key);

                if (regrets != null) {
                    double max = Double.NEGATIVE_INFINITY;

                    for (int j = 0; j < regrets.length; j++) {
                        if (regrets[j] > max)
                            max = regrets[j];
                    }
                    Set<Integer> maxRegretActionIndices = new HashSet<>();

                    for (int j = 0; j < regrets.length; j++) {
                        if (regrets[j] > max - 1e-8)
                            maxRegretActionIndices.add(j);
                    }
                    compatibleISs.computeIfAbsent(maxRegretActionIndices, k -> new HashSet<>()).add(key);
                } else {
                    notVisitedISs.add(key);
                }
            });
            Set<GameState> isStates = i.getAllStates();

            distributeUnvisited(compatibleISs, notVisitedISs);
            compatibleISs.forEach((maxRegretActionIndices, isKeys) -> {
                Set<GameState> toRemove = isStates.stream().filter(isState -> isKeys.contains(isState.getISKeyForPlayerToMove())).collect(Collectors.toSet());

                if (!toRemove.isEmpty()) {
                    if (toRemove.size() < isStates.size()) {
                        isStates.removeAll(toRemove);
                        createNewIS(toRemove, new IRCFRData(i.getData().getActionCount()));
//                        isKeys.forEach(key -> prRegrets.remove(key));
                    }
                }
            });
        });
    }

    private void distributeUnvisited(Map<Set<Integer>, Set<ISKey>> compatibleISs, Set<ISKey> notVisitedISs) {
        if (compatibleISs.isEmpty())
            return;
        Set<Integer> maxSizeKey = null;
        int maxSize = -1;

        for (Map.Entry<Set<Integer>, Set<ISKey>> entry : compatibleISs.entrySet()) {
            if (maxSize < entry.getValue().size()) {
                maxSize = entry.getValue().size();
                maxSizeKey = entry.getKey();
            }
        }
        compatibleISs.get(maxSizeKey).addAll(notVisitedISs);
    }


//    private void updateAbstraction() {
//        new HashMap<>(currentAbstractionInformationSets).values().stream().filter(i -> i.getPlayer().getId() != 2).forEach(i -> {
//            Set<ISKey>[] nonCompatibleISs = createNonCompatibleISs(i);
//
//            i.getAllStates().stream().map(s -> s.getISKeyForPlayerToMove()).distinct().forEach(key -> {
//                double[] regrets = prRegrets.get(key);
//
//                if (regrets != null) {
//                    double max = Double.NEGATIVE_INFINITY;
//
//                    for (int j = 0; j < regrets.length; j++) {
//                        if (regrets[j] > max)
//                            max = regrets[j];
//                    }
//                    for (int j = 0; j < regrets.length; j++) {
//                        if (regrets[j] < max - 1e-8)
//                            nonCompatibleISs[j].add(key);
//                    }
//                }
//            });
//            Arrays.sort(nonCompatibleISs, (o1, o2) -> o1.size() - o2.size());
//            Set<GameState> isStates = i.getAllStates();
//
//            for (int j = 0; j < nonCompatibleISs.length; j++) {
//                int actionIndex = j;
//                Set<GameState> toRemove = isStates.stream().filter(isState -> !nonCompatibleISs[actionIndex].contains(isState.getISKeyForPlayerToMove())).collect(Collectors.toSet());
//
//                if (!toRemove.isEmpty()) {
//                    if (toRemove.size() < isStates.size()) {
//                        isStates.removeAll(toRemove);
//                        createNewIS(toRemove, i.getData());
//                    } else {
//                        break;
//                    }
//                }
//            }
//        });
//    }

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

                expectedValue += p * computeCurrentRegrets(newState, new_p1, new_p2, expPlayer);
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
                expectedValuesForActions[i] = computeCurrentRegrets(newState, pi1 * currentStrategy[i], pi2, expPlayer);
            } else {
                expectedValuesForActions[i] = computeCurrentRegrets(newState, pi1, currentStrategy[i] * pi2, expPlayer);
            }
            expectedValue += currentStrategy[i] * expectedValuesForActions[i];
        }
        if (informationSet.getPlayer().equals(expPlayer))
            updateCurrentRegrets(node, pi1, pi2, expPlayer, expectedValuesForActions, expectedValue);
        return expectedValue;
    }

    private void updateCurrentRegrets(GameState node, double pi1, double pi2, Player expPlayer,
                                      double[] expectedValuesForActions, double expectedValue) {
        double[] regret = prRegrets.computeIfAbsent(node.getISKeyForPlayerToMove(),
                k -> new double[expectedValuesForActions.length]);

        for (int i = 0; i < regret.length; i++) {
            regret[i] += (expPlayer.getId() == 0 ? pi2 : pi1) * (expectedValuesForActions[i] - expectedValue);
        }
    }

    protected double[] getStrategy(OOSAlgorithmData data) {
        if (USE_AVG_STRAT && iteration > 1)
            return ((IRCFRData) data).getNormalizedMeanStrategy();
        return data.getRMStrategy();
    }


//    @Override
//    protected Map<ImperfectRecallISKey, Map<PerfectRecallISKey, OOSAlgorithmData>> getRegretDifferences() {
//        Map<ImperfectRecallISKey, Map<PerfectRecallISKey, OOSAlgorithmData>> toSplit = new HashMap<>();
//
//        currentAbstractionInformationSets.values().stream().filter(i -> i.getPlayer().getId() != 2).forEach(i -> {  tohle nedelám dobře ne? potřebuju spočítat regret té strategie z IR abstraction jenom nic víc
//                což v těch regretech je ale s špatnym ovážením a dělám to o krok zpět
//            double[] abstractionRegrets = i.getData().getRegrets();
//            Set<ISKey> isKeys = i.getAllStates().stream().filter(s -> !s.isPlayerToMoveNature())
//                    .map(s -> s.getISKeyForPlayerToMove()).collect(Collectors.toSet());
//            Map<ISKey, double[]> prRegretsForIS = isKeys.stream().collect(Collectors.toMap(key -> key,
//                    key -> ((IRCFRData) perfectRecallConfig.getAllInformationSets().get(key).getAlgorithmData()).getRegrets()));   should be weighted?
//
//            if(computeRegret(abstractionRegrets) < computeRegret(prRegretsForIS)) {
//                Map<PerfectRecallISKey, OOSAlgorithmData> dataMap = toSplit.computeIfAbsent((ImperfectRecallISKey) i.getISKey(), key -> new HashMap<>());
//
//                here choose only some
//                isKeys.forEach(key -> dataMap.put((PerfectRecallISKey) key, (OOSAlgorithmData) perfectRecallConfig.getAllInformationSets().get(key).getAlgorithmData()));
//            }
//        });
//        return toSplit;
//    }

//    private void updateAbstractionForRegrets(Map<ISKey, double[]> irRegrets, Map<ISKey, double[]> prRegrets) {
//        currentAbstractionInformationSets.values().stream().filter(i -> i.getPlayer().getId() != 2).forEach(i -> {
//            double[] irRegret = irRegrets.get(i.getISKey());
//            Map<ISKey, double[]> currentISPRRegrets = getCurrentISPRRegrets(prRegrets, i);
//
//            if(computeRegret(irRegret) < computeRegret(currentISPRRegrets)) {
//                updateIS(i, irRegret, currentISPRRegrets);
//            }
//        });
//    }
//
//    private Map<ISKey, double[]> getCurrentISPRRegrets(Map<ISKey, double[]> prRegrets,
//                                                       IRCFRInformationSet informationSet) {
//        return informationSet.getAllStates().stream().map(s -> s.getISKeyForPlayerToMove())
//                .collect(Collectors.toMap(key -> (PerfectRecallISKey) key, key -> prRegrets.get(key)));
//    }

    private double computeRegret(Map<ISKey, double[]> currentISPRRegrets) {
        return currentISPRRegrets.values().stream().flatMapToDouble(regret -> Arrays.stream(regret)).sum();
    }

    private double computeRegret(double[] irRegret) {
        return Arrays.stream(irRegret).sum();
    }


}
