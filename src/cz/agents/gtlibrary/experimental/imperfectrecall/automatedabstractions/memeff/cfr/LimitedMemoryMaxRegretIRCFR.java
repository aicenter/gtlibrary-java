package cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.memeff.cfr;

import cz.agents.gtlibrary.algorithms.mcts.MCTSConfig;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.domain.goofspiel.GSGameInfo;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielExpander;
import cz.agents.gtlibrary.domain.goofspiel.IIGoofSpielGameState;
import cz.agents.gtlibrary.domain.poker.generic.GPGameInfo;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerExpander;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerGameState;
import cz.agents.gtlibrary.domain.pursuit.PursuitExpander;
import cz.agents.gtlibrary.domain.pursuit.PursuitGameInfo;
import cz.agents.gtlibrary.domain.pursuit.PursuitGameState;
import cz.agents.gtlibrary.domain.pursuit.VisibilityPursuitGameState;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameExpander;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameInfo;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameState;
import cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.memeff.AutomatedAbstractionData;
import cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.memeff.MemEffAbstractedInformationSet;
import cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.memeff.cfr.regretcheck.RegretCheck;
import cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.memeff.cfr.regretcheck.SquareRootCheck;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.iinodes.ImperfectRecallISKey;
import cz.agents.gtlibrary.interfaces.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.*;
import java.util.stream.Collectors;

public class LimitedMemoryMaxRegretIRCFR extends MaxRegretIRCFR {

    public static void main(String[] args) {
        runGenericPoker();
//        runIIGoofspiel();
//        runRandomGame();
//        runRandomGame("backup.ser");
//        runVisibilityPursuit();
    }

    public static void runGenericPoker() {
        GameState root = new GenericPokerGameState();
        MCTSConfig config = new MCTSConfig();
        Expander<MCTSInformationSet> expander = new GenericPokerExpander<>(config);
        GameInfo info = new GPGameInfo();
        MaxRegretIRCFR alg = new LimitedMemoryMaxRegretIRCFR(root, expander, info, config);

        alg.runIterations(1000000000);
    }

    public static void runGenericPoker(String backupFileName) {
        GameState root = new GenericPokerGameState();
        MCTSConfig config = new MCTSConfig();
        Expander<MCTSInformationSet> expander = new GenericPokerExpander<>(config);
        GameInfo info = new GPGameInfo();
        loadFromBackupAndRun(backupFileName, root, config, expander, info);
    }

    public static void runIIGoofspiel() {
        GameState root = new IIGoofSpielGameState();
        MCTSConfig config = new MCTSConfig();
        Expander<MCTSInformationSet> expander = new GoofSpielExpander<>(config);
        GameInfo info = new GSGameInfo();
        MaxRegretIRCFR alg = new LimitedMemoryMaxRegretIRCFR(root, expander, info, config);

        alg.runIterations(1000000000);
    }

    public static void runIIGoofspiel(String backupFileName) {
        GameState root = new IIGoofSpielGameState();
        MCTSConfig config = new MCTSConfig();
        Expander<MCTSInformationSet> expander = new GoofSpielExpander<>(config);
        GameInfo info = new GSGameInfo();

        loadFromBackupAndRun(backupFileName, root, config, expander, info);
    }

    public static void runRandomGame() {
        GameState root = new RandomGameState();
        MCTSConfig config = new MCTSConfig();
        Expander<MCTSInformationSet> expander = new RandomGameExpander<>(config);
        GameInfo info = new RandomGameInfo();
        MaxRegretIRCFR alg = new LimitedMemoryMaxRegretIRCFR(root, expander, info, config);

        alg.runIterations(1000000000);
    }

    public static void runRandomGame(String backupFileName) {
        GameState root = new RandomGameState();
        MCTSConfig config = new MCTSConfig();
        Expander<MCTSInformationSet> expander = new RandomGameExpander<>(config);
        GameInfo info = new RandomGameInfo();

        loadFromBackupAndRun(backupFileName, root, config, expander, info);
    }

    public static void runVisibilityPursuit() {
        GameState root = new VisibilityPursuitGameState();
        MCTSConfig config = new MCTSConfig();
        Expander<MCTSInformationSet> expander = new PursuitExpander<>(config);
        GameInfo info = new PursuitGameInfo();
        MaxRegretIRCFR alg = new LimitedMemoryMaxRegretIRCFR(root, expander, info, config);

        alg.runIterations(1000000000);
    }

    public static void runVisibilityPursuit(String backupFileName) {
        GameState root = new VisibilityPursuitGameState();
        MCTSConfig config = new MCTSConfig();
        Expander<MCTSInformationSet> expander = new PursuitExpander<>(config);
        GameInfo info = new PursuitGameInfo();

        loadFromBackupAndRun(backupFileName, root, config, expander, info);
    }

    public static void runPursuit() {
        GameState root = new PursuitGameState();
        MCTSConfig config = new MCTSConfig();
        Expander<MCTSInformationSet> expander = new PursuitExpander<>(config);
        GameInfo info = new PursuitGameInfo();
        MaxRegretIRCFR alg = new LimitedMemoryMaxRegretIRCFR(root, expander, info, config);

        alg.runIterations(1000000000);
    }

    private static void loadFromBackupAndRun(String backupFileName, GameState root, MCTSConfig config, Expander<MCTSInformationSet> expander, GameInfo info) {
        try {
            FileInputStream fin = new FileInputStream(backupFileName);
            ObjectInputStream oos = new ObjectInputStream(fin);
            AutomatedAbstractionData data = (AutomatedAbstractionData) oos.readObject();
            MaxRegretIRCFR alg = new LimitedMemoryMaxRegretIRCFR(root, expander, info, config, data);

            alg.runIterations(1000000000);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean SAMPLE_USING_STRATEGY = true;
    public static double INIT_REGRET_WEIGHT = 0.99;
    public static int sizeLimitHeuristic = 900;
    public static int sizeLimitBound = 100;
    public static long IS_SAMPLING_SEED = 1;
    private Random random;
    private Set<ISKey> toUpdate;
    private Map<ISKey, double[]> regretsForRegretCheck;
    private Map<ISKey, Double> reachProbability;
    private RegretCheck regretCheck = new SquareRootCheck();
    private boolean bellowLimitHeuristic;
    private boolean bellowLimitBound;
    private int iterationsBeforeBoundResample;
    private int currentSampleIterations;
    public long statisticsTime;
    public long abstractionUpdateTime;
    public long regretUpdateTime;
    public long samplingTime;

    public LimitedMemoryMaxRegretIRCFR(GameState rootState, Expander<? extends InformationSet> perfectRecallExpander, GameInfo info, MCTSConfig perfectRecallConfig) {
        super(rootState, perfectRecallExpander, info, perfectRecallConfig);
        random = new Random(IS_SAMPLING_SEED);
        toUpdate = new HashSet<>(sizeLimitHeuristic);
        regretsForRegretCheck = new HashMap<>(sizeLimitBound);
        bellowLimitHeuristic = false;
        bellowLimitBound = false;
        iterationsBeforeBoundResample = 1;
        currentSampleIterations = 0;
        reachProbability = new HashMap<>();
    }

    public LimitedMemoryMaxRegretIRCFR(GameState rootState, Expander<? extends InformationSet> perfectRecallExpander, GameInfo info, MCTSConfig perfectRecallConfig, AutomatedAbstractionData data) {
        super(rootState, perfectRecallExpander, info, perfectRecallConfig, data);
        random = new Random(IS_SAMPLING_SEED);
        toUpdate = new HashSet<>(sizeLimitHeuristic);
        regretsForRegretCheck = new HashMap<>(sizeLimitBound);
        bellowLimitHeuristic = false;
        bellowLimitBound = false;
        iterationsBeforeBoundResample = (int) Math.floor((Math.pow(2, Math.log(2 * (data.iteration - delay + 1)) / Math.log(2))));
        System.err.println("Initializing sequence length " + iterationsBeforeBoundResample);
        currentSampleIterations = 0;
        reachProbability = new HashMap<>();
    }

    @Override
    protected void iteration(Player player) {
        if (sizeLimitBound > 0)
            boundCheck();
        long samplingUpdateStart = threadBean.getCurrentThreadCpuTime();
        findISsToUpdate(player);
        samplingTime += threadBean.getCurrentThreadCpuTime() - samplingUpdateStart;
        long regretUpdateStart = threadBean.getCurrentThreadCpuTime();

        if (SIMULTANEOUS_PR_IR)
            perfectAndImperfectRecallIteration(rootState, 1, 1, player);
        else
            imperfectRecallIteration(rootState, 1, 1, player);
        updateImperfectRecallData();
        if (!SIMULTANEOUS_PR_IR)
            computeCurrentRegrets(rootState, 1, 1, player);
        if (REGRET_MATCHING_PLUS)
            removeNegativePRRegrets();
        regretUpdateTime += threadBean.getCurrentThreadCpuTime() - regretUpdateStart;
        if (USE_ABSTRACTION) {
            long abstractionUpdateStart = threadBean.getCurrentThreadCpuTime();
            updateAbstraction();
            abstractionUpdateTime += threadBean.getCurrentThreadCpuTime() - abstractionUpdateStart;
        }
        if (DELETE_REGRETS)
            prRegrets.clear();
        toUpdate.clear();
        reachProbability.clear();
        System.gc();
    }

    private void boundCheck() {
        if (iteration < delay)
            return;
        currentSampleIterations++;
        iterationsBeforeBoundResample--;
        if (iterationsBeforeBoundResample == 0) {
            long start = threadBean.getCurrentThreadCpuTime();
            resampleInformationSetsForRegretCheck();
            samplingTime += threadBean.getCurrentThreadCpuTime() - start;
            iterationsBeforeBoundResample = 2 * currentSampleIterations;
            System.err.println("setting sequence length " + iterationsBeforeBoundResample);
            currentSampleIterations = 1;
        } else {
            long start = threadBean.getCurrentThreadCpuTime();
            checkRegretBoundsAndUpdate();
            abstractionUpdateTime += threadBean.getCurrentThreadCpuTime() - start;
        }
    }

    @Override
    protected void printStatistics() {
        long statStart = threadBean.getCurrentThreadCpuTime();

        super.printStatistics();
        statisticsTime += threadBean.getCurrentThreadCpuTime() - statStart;
        System.out.println("Abstraction update time: " + abstractionUpdateTime / 1e6);
        System.out.println("Regret update time: " + regretUpdateTime / 1e6);
        System.out.println("Sampling time: " + samplingTime / 1e6);
        System.out.println("Statistics time: " + statisticsTime / 1e6);
    }

    @Override
    protected void printRegretStat() {
        System.out.println("Max immediate regret in checked sets: " + regretLog.entrySet().stream().filter(entry -> regretsForRegretCheck.containsKey(entry.getKey())).map(entry -> entry.getValue())
                .mapToDouble(regrets -> Arrays.stream(regrets).max().getAsDouble() / iteration).max().orElse(20));
        System.out.println("Max immediate regret: " + regretLog.values().stream()
                .mapToDouble(regrets -> Arrays.stream(regrets).max().getAsDouble() / iteration).max().orElse(20));
        System.out.println("Max immediate regret in abstracted sets: " + regretLog.entrySet().stream().filter(entry -> getAbstractedInformationSet(entry.getKey(), entry.getValue().length).getAbstractedKeys().size() > 1).map(entry -> entry.getValue())
                .mapToDouble(regrets -> Arrays.stream(regrets).max().getAsDouble() / iteration).max().orElse(20));
        System.out.println("Regret bound without constant and actions: " + 1. / Math.sqrt(iteration));
        System.out.println("Regret check size: " + regretsForRegretCheck.size());
    }

    protected void updateWithReusedData(MemEffAbstractedInformationSet i, Map<Set<Integer>, Set<ISKey>> compatibleISs, Set<GameState> isStates, Set<Integer> maxSizeKey) {
        compatibleISs.forEach((maxRegretActionIndices, isKeys) -> {
            Set<GameState> toRemove = isStates.stream().filter(isState -> isKeys.contains(isState.getISKeyForPlayerToMove())).collect(Collectors.toSet());

            if (!maxRegretActionIndices.equals(maxSizeKey)) {
                if (isKeys.size() == 1)
                    regretsForRegretCheck.remove(isKeys.stream().findAny().get());
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
                if (isKeys.size() == 1)
                    regretsForRegretCheck.remove(isKeys.stream().findAny().get());
                if (toRemove.size() < isStates.size()) {
                    isStates.removeAll(toRemove);
                    i.getAbstractedKeys().removeAll(isKeys);
                    createNewISNoDataCopy(toRemove, new IRCFRData(i.getData().getActionCount()));
                }
            }
        });
    }

    private void checkRegretBoundsAndUpdate() {
        List<ISKey> toRemove = new ArrayList<>();

        regretsForRegretCheck.forEach((key, regrets) -> {
            if (regretCheck.isAboveBound(regrets, this)) {
                removeAndCreate(key, regrets.length);
                toRemove.add(key);
            }
        });
        toRemove.forEach(key -> regretsForRegretCheck.remove(key));
    }

    private void removeAndCreate(ISKey key, int actionCount) {
        MemEffAbstractedInformationSet informationSet = getAbstractedInformationSet(key, actionCount);

        Set<GameState> toRemove = informationSet.getAllStates().stream().filter(isState -> isState.getISKeyForPlayerToMove().equals(key)).collect(Collectors.toSet());
        assert !toRemove.isEmpty();
        if (toRemove.size() < informationSet.getAllStates().size()) {
            informationSet.getAllStates().removeAll(toRemove);
            informationSet.getAbstractedKeys().remove(key);
            createNewISNoDataCopy(toRemove, new IRCFRData(actionCount));
        }
    }

    private void resampleInformationSetsForRegretCheck() {
        regretsForRegretCheck.clear();
        if (bellowLimitBound)
            return;
        List<MemEffAbstractedInformationSet> imperfectRecallSetsForPlayer = currentAbstractionInformationSets.values().stream()
                .filter(i -> i.getAbstractedKeys().size() > 1)
                .collect(Collectors.toList());
        int abstractedISCount = getAbstractedISCount(imperfectRecallSetsForPlayer);

        if (abstractedISCount <= sizeLimitBound) {
            bellowLimitBound = true;
        } else {
            if (SAMPLE_USING_STRATEGY) {
                sampleAccordingToStrategyForBound(imperfectRecallSetsForPlayer);
            } else {
                Collections.shuffle(imperfectRecallSetsForPlayer, random);
                for (MemEffAbstractedInformationSet informationSet : imperfectRecallSetsForPlayer) {
                    List<ISKey> collect = informationSet.getAbstractedKeys().stream()
                            .collect(Collectors.toList());

                    if (collect.size() + regretsForRegretCheck.size() > sizeLimitBound) {
                        Collections.shuffle(collect, random);
                        collect.subList(Math.min(sizeLimitBound - regretsForRegretCheck.size(), collect.size()), collect.size()).clear();
                    }
                    collect.forEach(key -> regretsForRegretCheck.put(key, initializeRegrets(informationSet)));
                }
            }
        }
    }

    private void sampleAccordingToStrategyForBound(List<MemEffAbstractedInformationSet> imperfectRecallSetsForPlayer) {
        Set<Integer> blackList = new HashSet<>();
        double reachEps = 1. / imperfectRecallSetsForPlayer.size();
        double sum = imperfectRecallSetsForPlayer.stream().mapToDouble(i -> reachProbability.getOrDefault(i.getISKey(), 0d) + reachEps).sum();

        while (regretsForRegretCheck.size() < sizeLimitBound) {
            double randVal = random.nextDouble() * sum;

            for (int i = 0; i < imperfectRecallSetsForPlayer.size(); i++) {
                if (blackList.contains(i))
                    continue;
                double prob = reachProbability.getOrDefault(imperfectRecallSetsForPlayer.get(i).getISKey(), 0d) + reachEps;

                randVal -= prob;
                if (randVal <= 0) {
                    blackList.add(i);
                    sum -= prob;
                    List<ISKey> collect = imperfectRecallSetsForPlayer.get(i).getAbstractedKeys().stream()
                            .collect(Collectors.toList());

                    if (collect.size() + regretsForRegretCheck.size() > sizeLimitBound) {
                        Collections.shuffle(collect, random);
                        collect.subList(Math.min(sizeLimitBound - regretsForRegretCheck.size(), collect.size()), collect.size()).clear();
                    }
                    final int index = i;

                    collect.forEach(key -> regretsForRegretCheck.put(key, initializeRegrets(imperfectRecallSetsForPlayer.get(index))));
                    break;
                }
            }
        }
    }

    private double[] initializeRegrets(MemEffAbstractedInformationSet informationSet) {
//        double[] currentStrategy = informationSet.getData().getRMStrategy();
//        double maxProbability = Arrays.stream(currentStrategy).max().getAsDouble();
//        double currentRegretBound = Math.sqrt(informationSet.getData().getActionCount()) / Math.sqrt(iteration);
//        double[] regrets = new double[currentStrategy.length];
//
//        for (int i = 0; i < regrets.length; i++) {
//            regrets[i] = currentStrategy[i] / maxProbability * currentRegretBound * iteration;
//        }
//        assert Math.abs(Arrays.stream(regrets).max().getAsDouble() - currentRegretBound * iteration) < 1e-8;

        return initializeRegrets(informationSet.getData().getActionCount());
    }

    private double[] initializeRegrets(int actionCount) {
//        double[] currentStrategy = informationSet.getData().getRMStrategy();
//        double maxProbability = Arrays.stream(currentStrategy).max().getAsDouble();
//        double currentRegretBound = Math.sqrt(informationSet.getData().getActionCount()) / Math.sqrt(iteration);
//        double[] regrets = new double[currentStrategy.length];
//
//        for (int i = 0; i < regrets.length; i++) {
//            regrets[i] = currentStrategy[i] / maxProbability * currentRegretBound * iteration;
//        }
//        assert Math.abs(Arrays.stream(regrets).max().getAsDouble() - currentRegretBound * iteration) < 1e-8;

        double currentRegretBound = Math.sqrt(actionCount) / Math.sqrt(iteration);
        double[] initRegrets = new double[actionCount];

        Arrays.fill(initRegrets, INIT_REGRET_WEIGHT * currentRegretBound * iteration);
        return initRegrets;
    }

//    protected void findISsToUpdate(Player player) {
//        if (bellowLimitHeuristic)
//            return;
//        List<MemEffAbstractedInformationSet> imperfectRecallSetsForPlayer = currentAbstractionInformationSets.values().stream()
//                .filter(i -> i.getPlayer().getId() == player.getId())
//                .filter(i -> i.getAbstractedKeys().size() > 1)
//                .collect(Collectors.toList());
//        int abstractedISCount = getAbstractedISCount(imperfectRecallSetsForPlayer);
//
//        if (abstractedISCount <= sizeLimitHeuristic) {
//            bellowLimitHeuristic = true;
//        } else {
//            Collections.shuffle(imperfectRecallSetsForPlayer, random);
//            for (MemEffAbstractedInformationSet informationSet : imperfectRecallSetsForPlayer) {
//                List<ISKey> collect = new ArrayList<>(informationSet.getAbstractedKeys());
//
//                if (collect.size() + toUpdate.size() > sizeLimitHeuristic) {
//                    Collections.shuffle(collect, random);
//                    collect.subList(Math.min(sizeLimitHeuristic - toUpdate.size(), collect.size()), collect.size()).clear();
//                }
//                toUpdate.addAll(collect);
//            }
//        }
//    }

    protected void findISsToUpdate(Player player) {
        if (bellowLimitHeuristic)
            return;
        List<MemEffAbstractedInformationSet> imperfectRecallSetsForPlayer = currentAbstractionInformationSets.values().stream()
                .filter(i -> i.getPlayer().getId() == player.getId())
                .filter(i -> i.getAbstractedKeys().size() > 1)
                .collect(Collectors.toList());
        int abstractedISCount = getAbstractedISCount(imperfectRecallSetsForPlayer);

        if (abstractedISCount <= sizeLimitHeuristic) {
            bellowLimitHeuristic = true;
        } else {
            if (SAMPLE_USING_STRATEGY) {
                sampleAccordingToStrategy(imperfectRecallSetsForPlayer);
            } else {
                Collections.shuffle(imperfectRecallSetsForPlayer, random);
                for (MemEffAbstractedInformationSet informationSet : imperfectRecallSetsForPlayer) {
                    List<ISKey> collect = new ArrayList<>(informationSet.getAbstractedKeys());

                    if (collect.size() + toUpdate.size() > sizeLimitHeuristic) {
                        Collections.shuffle(collect, random);
                        collect.subList(Math.min(sizeLimitHeuristic - toUpdate.size(), collect.size()), collect.size()).clear();
                    }
                    toUpdate.addAll(collect);
                }
            }
        }
    }

    private void sampleAccordingToStrategy(List<MemEffAbstractedInformationSet> imperfectRecallSetsForPlayer) {
        Set<Integer> blackList = new HashSet<>();
        double reachEps = 1. / imperfectRecallSetsForPlayer.size();
        double sum = imperfectRecallSetsForPlayer.stream().mapToDouble(i -> reachProbability.getOrDefault(i.getISKey(), 0d) + reachEps).sum();

        while (toUpdate.size() < sizeLimitHeuristic) {
            double randVal = random.nextDouble() * sum;

            for (int i = 0; i < imperfectRecallSetsForPlayer.size(); i++) {
                if (blackList.contains(i))
                    continue;
                double prob = reachProbability.getOrDefault(imperfectRecallSetsForPlayer.get(i).getISKey(), 0d) + reachEps;

                randVal -= prob;
                if (randVal <= 0) {
                    blackList.add(i);
                    sum -= prob;
                    List<ISKey> collect = new ArrayList<>(imperfectRecallSetsForPlayer.get(i).getAbstractedKeys());

                    if (collect.size() + toUpdate.size() > sizeLimitHeuristic) {
                        Collections.shuffle(collect, random);
                        collect.subList(Math.min(sizeLimitHeuristic - toUpdate.size(), collect.size()), collect.size()).clear();
                    }
                    toUpdate.addAll(collect);
                    break;
                }
            }
        }
    }

    private int getAbstractedISCount(List<MemEffAbstractedInformationSet> abstractedInformationSets) {
        return (int) abstractedInformationSets.stream()
                .flatMap(i -> i.getAbstractedKeys().stream())
                .filter(key -> !regretsForRegretCheck.containsKey(key))
                .count();
    }

    @Override
    protected void updateCurrentRegrets(GameState node, double pi1, double pi2, Player expPlayer, double[] expectedValuesForActions, double expectedValue) {
        if (bellowLimitHeuristic || toUpdate.contains(node.getISKeyForPlayerToMove()))
            super.updateCurrentRegrets(node, pi1, pi2, expPlayer, expectedValuesForActions, expectedValue);
        if (bellowLimitBound || regretsForRegretCheck.containsKey(node.getISKeyForPlayerToMove()))
            updateRegretsForRegretCheck(node, pi1, pi2, expPlayer, expectedValuesForActions, expectedValue);
    }

    private void updateRegretsForRegretCheck(GameState node, double pi1, double pi2, Player expPlayer, double[] expectedValuesForActions, double expectedValue) {
        double[] regret = regretsForRegretCheck.computeIfAbsent(node.getISKeyForPlayerToMove(),
                k -> initializeRegrets(expectedValuesForActions.length));
        double currentProb = (expPlayer.getId() == 0 ? pi2 : pi1);

        for (int i = 0; i < regret.length; i++) {
            regret[i] += currentProb * (expectedValuesForActions[i] - expectedValue);
        }
    }

    @Override
    protected void storeProbabilityForReachingIS(GameState node, double pi1, double pi2) {
        if (SAMPLE_USING_STRATEGY) {
            ImperfectRecallISKey key = getAbstractionISKey(node);

            if (node.getPlayerToMove().getId() == 0)
                reachProbability.compute(key, (k, v) -> v == null ? pi1 * node.getNatureProbability() : v + pi1 * node.getNatureProbability());
            else
                reachProbability.compute(key, (k, v) -> v == null ? pi2 * node.getNatureProbability() : v + pi2 * node.getNatureProbability());
        }
    }

    public int getCurrentSampleIterations() {
        return currentSampleIterations;
    }
}
