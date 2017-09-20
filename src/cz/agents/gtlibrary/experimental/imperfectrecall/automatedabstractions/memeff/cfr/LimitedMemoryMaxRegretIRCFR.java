package cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.memeff.cfr;

import cz.agents.gtlibrary.algorithms.mcts.MCTSConfig;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.domain.goofspiel.GSGameInfo;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielExpander;
import cz.agents.gtlibrary.domain.goofspiel.IIGoofSpielGameState;
import cz.agents.gtlibrary.domain.poker.generic.GPGameInfo;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerExpander;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerGameState;
import cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.memeff.AutomatedAbstractionData;
import cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.memeff.MemEffAbstractedInformationSet;
import cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.memeff.cfr.regretcheck.RegretCheck;
import cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.memeff.cfr.regretcheck.SquareRootCheck;
import cz.agents.gtlibrary.iinodes.ISKey;
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
    }

    public static void runGenericPoker() {
        GameState root = new GenericPokerGameState();
        MCTSConfig config = new MCTSConfig();
        Expander<MCTSInformationSet> expander = new GenericPokerExpander<>(config);
        GameInfo info = new GPGameInfo();
        MaxRegretIRCFR alg = new LimitedMemoryMaxRegretIRCFR(root, expander, info, config);

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
            MaxRegretIRCFR alg = new LimitedMemoryMaxRegretIRCFR(root, expander, info, config, data);

            alg.runIterations(10000000);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void runIIGoofspiel() {
        GameState root = new IIGoofSpielGameState();
        MCTSConfig config = new MCTSConfig();
        Expander<MCTSInformationSet> expander = new GoofSpielExpander<>(config);
        GameInfo info = new GSGameInfo();
        MaxRegretIRCFR alg = new LimitedMemoryMaxRegretIRCFR(root, expander, info, config);

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
            MaxRegretIRCFR alg = new LimitedMemoryMaxRegretIRCFR(root, expander, info, config, data);

            alg.runIterations(10000000);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static double INIT_REGRET_WEIGHT = 0.99;
    public static int sizeLimitHeuristic = 0;
    public static int sizeLimitBound = 500;
    private Random random;
    private Set<ISKey> toUpdate;
    private Map<ISKey, double[]> regretsForRegretCheck;
    private RegretCheck regretCheck = new SquareRootCheck();
    private boolean bellowLimitHeuristic;
    private boolean bellowLimitBound;
    private int iterationsBeforeBoundResample;
    private int currentSampleIterations;

    public LimitedMemoryMaxRegretIRCFR(GameState rootState, Expander<? extends InformationSet> perfectRecallExpander, GameInfo info, MCTSConfig perfectRecallConfig) {
        super(rootState, perfectRecallExpander, info, perfectRecallConfig);
        random = new Random(1);
        toUpdate = new HashSet<>(sizeLimitHeuristic);
        regretsForRegretCheck = new HashMap<>(sizeLimitBound);
        bellowLimitHeuristic = false;
        bellowLimitBound = false;
        iterationsBeforeBoundResample = 1;
        currentSampleIterations = 0;
    }

    public LimitedMemoryMaxRegretIRCFR(GameState rootState, Expander<? extends InformationSet> perfectRecallExpander, GameInfo info, MCTSConfig perfectRecallConfig, AutomatedAbstractionData data) {
        super(rootState, perfectRecallExpander, info, perfectRecallConfig, data);
        random = new Random(1);
        toUpdate = new HashSet<>(sizeLimitHeuristic);
        regretsForRegretCheck = new HashMap<>(sizeLimitBound);
        bellowLimitHeuristic = false;
        bellowLimitBound = false;
        iterationsBeforeBoundResample = 1;
        currentSampleIterations = 0;
    }

    @Override
    protected void iteration(Player player) {
        if (sizeLimitBound > 0)
            boundCheck();
        findISsToUpdate(player);
        if (SIMULTANEOUS_PR_IR)
            perfectAndImperfectRecallIteration(rootState, 1, 1, player);
        else
            imperfectRecallIteration(rootState, 1, 1, player);
        updateImperfectRecallData();
        if (!SIMULTANEOUS_PR_IR)
            computeCurrentRegrets(rootState, 1, 1, player);
        if (REGRET_MATCHING_PLUS)
            removeNegativePRRegrets();
        updateAbstraction();
        if (DELETE_REGRETS)
            prRegrets.clear();
        toUpdate.clear();
        System.gc();
    }

    private void boundCheck() {
        if(iteration < delay)
            return;
        currentSampleIterations++;
        iterationsBeforeBoundResample--;
        if (iterationsBeforeBoundResample == 0) {
            resampleInformationSetsForRegretCheck();
            iterationsBeforeBoundResample = (int) Math.round(Math.sqrt(iteration * 2));
            System.err.println("setting sequence length " + iterationsBeforeBoundResample);
            currentSampleIterations = 1;
        } else {
            checkRegretBoundsAndUpdate();
        }
    }

    protected void updateWithReusedData(MemEffAbstractedInformationSet i, Map<Set<Integer>, Set<ISKey>> compatibleISs, Set<GameState> isStates) {
        compatibleISs.forEach((maxRegretActionIndices, isKeys) -> {
            Set<GameState> toRemove = isStates.stream().filter(isState -> isKeys.contains(isState.getISKeyForPlayerToMove())).collect(Collectors.toSet());

            if (!toRemove.isEmpty()) {
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

    protected void updateWithClearData(MemEffAbstractedInformationSet i, Map<Set<Integer>, Set<ISKey>> compatibleISs, Set<GameState> isStates) {
        compatibleISs.forEach((maxRegretActionIndices, isKeys) -> {
            Set<GameState> toRemove = isStates.stream().filter(isState -> isKeys.contains(isState.getISKeyForPlayerToMove())).collect(Collectors.toSet());

            if (!toRemove.isEmpty()) {
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
                System.err.println("updating");
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
            createNewIS(toRemove, informationSet.getData());
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

        double currentRegretBound = Math.sqrt(informationSet.getData().getActionCount()) / Math.sqrt(iteration);
        double[] initRegrets = new double[informationSet.getData().getActionCount()];

        Arrays.fill(initRegrets, INIT_REGRET_WEIGHT * currentRegretBound * iteration);
        return initRegrets;
    }

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
                k -> new double[expectedValuesForActions.length]);
        double currentProb = (expPlayer.getId() == 0 ? pi2 : pi1);

        for (int i = 0; i < regret.length; i++) {
            regret[i] += currentProb * (expectedValuesForActions[i] - expectedValue);
        }
    }

    public int getCurrentSampleIterations() {
        return currentSampleIterations;
    }
}
