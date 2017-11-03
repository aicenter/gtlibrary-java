package cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.memeff;

import cz.agents.gtlibrary.algorithms.cfr.ir.IRCFRInformationSet;
import cz.agents.gtlibrary.domain.randomabstraction.IDObservation;
import cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.CFRBRData;
import cz.agents.gtlibrary.iinodes.*;
import cz.agents.gtlibrary.interfaces.*;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class AutomatedAbstractionAlgorithm {
    private static final boolean SERIALIZE = false;
    public static boolean USE_ABSTRACTION = true;

    protected final GameState rootState;
    protected final Expander<? extends InformationSet> perfectRecallExpander;
    protected final GameInfo gameInfo;
    protected final Map<ISKey, MemEffAbstractedInformationSet> currentAbstractionInformationSets;
    protected final InformationSetKeyMap currentAbstractionISKeys;
    protected int iteration = 0;
    protected int isKeyCounter = 0;

    protected MemoryMXBean memoryBean;
    protected ThreadMXBean threadBean;
    long startTime;

    public AutomatedAbstractionAlgorithm(GameState rootState, Expander<? extends InformationSet> perfectRecallExpander, GameInfo info) {
        this.rootState = rootState;
        this.perfectRecallExpander = perfectRecallExpander;
        this.gameInfo = info;
        currentAbstractionInformationSets = new HashMap<>();
        currentAbstractionISKeys = new InformationSetKeyMap();
        memoryBean = ManagementFactory.getMemoryMXBean();
        threadBean = ManagementFactory.getThreadMXBean();
        if (USE_ABSTRACTION)
            buildInitialAbstraction();
        else
            buildCompleteGame();
    }

    public AutomatedAbstractionAlgorithm(GameState rootState, Expander<? extends InformationSet> perfectRecallExpander, GameInfo info, AutomatedAbstractionData data) {
        this.rootState = rootState;
        this.perfectRecallExpander = perfectRecallExpander;
        this.gameInfo = info;
        memoryBean = ManagementFactory.getMemoryMXBean();
        threadBean = ManagementFactory.getThreadMXBean();
        currentAbstractionISKeys = data.currentAbstractionISKeys;
        currentAbstractionInformationSets = data.currentAbstractionInformationSets;
        iteration = data.iteration;
        isKeyCounter = data.isKeyCounter;
    }

    protected void buildInitialAbstraction() {
        buildInformationSets(rootState);
        addData(currentAbstractionInformationSets.values());
        System.out.println("Init abstr P1 IS: " + currentAbstractionInformationSets.values().stream().filter(i -> i.getPlayer().getId() == 0).count());
        System.out.println("Init abstr P2 IS: " + currentAbstractionInformationSets.values().stream().filter(i -> i.getPlayer().getId() == 1).count());
    }

    protected void buildCompleteGame() {
        buildCompleteGameInformationSets(rootState);
        addData(currentAbstractionInformationSets.values());
        System.out.println("Init abstr P1 IS: " + currentAbstractionInformationSets.values().stream().filter(i -> i.getPlayer().getId() == 0).count());
        System.out.println("Init abstr P2 IS: " + currentAbstractionInformationSets.values().stream().filter(i -> i.getPlayer().getId() == 1).count());
    }


    protected void buildInformationSets(GameState state) {
        if (state.isGameEnd())
            return;
        if (!state.isPlayerToMoveNature()) {
            ImperfectRecallISKey key = getAbstractionISKey(state);
            IRCFRInformationSet set = currentAbstractionInformationSets.computeIfAbsent(key, k -> createInformationSet(state, key));

            set.addStateToIS(state);
        }
        perfectRecallExpander.getActions(state).stream().map(a -> state.performAction(a)).forEach(s -> buildInformationSets(s));
    }

    protected MemEffAbstractedInformationSet createInformationSet(GameState state, ImperfectRecallISKey key) {
        return new MemEffAbstractedInformationSet(state, key);
    }

    protected void buildCompleteGameInformationSets(GameState state) {
        if (state.isGameEnd())
            return;
        ImperfectRecallISKey key = getPerfectKey(state);
        IRCFRInformationSet set = currentAbstractionInformationSets.computeIfAbsent(key, k -> createInformationSet(state, key));

        set.addStateToIS(state);
        perfectRecallExpander.getActions(state).stream().map(a -> state.performAction(a)).forEach(s -> buildCompleteGameInformationSets(s));
    }

    protected ImperfectRecallISKey getPerfectKey(GameState state) {
        Observations observations = new Observations(state.getPlayerToMove(), state.getPlayerToMove());

        observations.add(new PerfectRecallObservation((PerfectRecallISKey) state.getISKeyForPlayerToMove()));
        ImperfectRecallISKey perfectKey = new ImperfectRecallISKey(observations, null, null);

        currentAbstractionISKeys.put((PerfectRecallISKey) state.getISKeyForPlayerToMove(), perfectKey);
        return perfectKey;
    }


    protected void addData(Collection<MemEffAbstractedInformationSet> informationSets) {
        informationSets.stream()
                .filter(i -> i.getPlayer().getId() != 2)
                .forEach(i -> i.setData(new CFRBRData(this.perfectRecallExpander.getActions(i.getAllStates().stream().findAny().get()).size())));
    }

    public void runIterations(int iterations) {
        startTime = threadBean.getCurrentThreadCpuTime();
        for (int i = 0; i < iterations; i++) {
            this.iteration++;
            iteration(rootState.getAllPlayers()[1]);
            iteration(rootState.getAllPlayers()[0]);
            if (isConverged(gameInfo.getMaxUtility() * 1e-5))
                return;
            if (this.iteration % 40 == 0 || iteration == 1)
                printStatistics();
        }
    }

    protected Map<ISKey, double[]> getBehavioralStrategyFor(Player player) {
        Map<ISKey, double[]> strategy = new HashMap<>(currentAbstractionInformationSets.size() / 2);

        currentAbstractionInformationSets.values().stream().filter(is -> is.getPlayer().equals(player)).forEach(is ->
                strategy.put(is.getISKey(), is.getData().getMp())
        );
        return strategy;
    }

    protected ImperfectRecallISKey getAbstractionISKey(GameState state) {
        return currentAbstractionISKeys.get(state, perfectRecallExpander);
    }

    protected ImperfectRecallISKey createCounterISKey(Player player) {
        Observations observations = new Observations(player, player);

        observations.add(new IDObservation(isKeyCounter++));
        return new ImperfectRecallISKey(observations, null, null);
    }

    protected abstract boolean isConverged(double epsilon);

    protected void printStatistics() {
        System.out.println("*************************************************");
        System.out.println("Iteration: " + iteration);
        System.out.println("ISKey map size: " + currentAbstractionISKeys.size());
        System.out.println("Current IS count: " + currentAbstractionInformationSets.values().stream()
                .filter(i -> i.getPlayer().getId() != 2).count());
        System.out.println("Current time: " + (threadBean.getCurrentThreadCpuTime() - startTime) / 1e6);
        System.out.println("Current memory: " + memoryBean.getHeapMemoryUsage().getUsed());
        System.out.println("Max memory: " + memoryBean.getHeapMemoryUsage().getMax());
        System.out.println(memoryBean.getHeapMemoryUsage().toString());
        if(SERIALIZE && iteration % 200 == 0) {
            System.out.println("saving");
            try {
                FileOutputStream fout = new FileOutputStream("backup.ser");

                ObjectOutputStream oos = new ObjectOutputStream(fout);
                oos.writeObject(new AutomatedAbstractionData(currentAbstractionInformationSets, currentAbstractionISKeys, iteration, isKeyCounter));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("saved");
        }
    }

    protected long getReachableAbstractedISCountFromOriginalGame(Map<ISKey, double[]> p0Strategy, Map<ISKey, double[]> p1Strategy) {
        return currentAbstractionInformationSets.values().stream().filter(i -> i.getPlayer().getId() != 2).filter(i ->
                i.getAllStates().stream().map(s -> s.getISKeyForPlayerToMove()).distinct().count() > 1
        ).flatMap(i -> i.getAllStates().stream().filter(s ->
                AbstractedStrategyUtils.getProbability(s.getSequenceFor(gameInfo.getAllPlayers()[0]), p0Strategy, currentAbstractionISKeys, perfectRecallExpander) > 1e-8 &&
                        AbstractedStrategyUtils.getProbability(s.getSequenceFor(gameInfo.getAllPlayers()[1]), p1Strategy, currentAbstractionISKeys, perfectRecallExpander) > 1e-8)
                .map(s -> s.getISKeyForPlayerToMove()).distinct()).count();
    }

    protected long getReachableISCountFromOriginalGame(Map<ISKey, double[]> p0Strategy, Map<ISKey, double[]> p1Strategy) {
        return currentAbstractionInformationSets.values().stream().filter(i -> i.getPlayer().getId() != 2).flatMap(i -> i.getAllStates().stream().filter(s ->
                AbstractedStrategyUtils.getProbability(s.getSequenceFor(gameInfo.getAllPlayers()[0]), p0Strategy, currentAbstractionISKeys, perfectRecallExpander) > 1e-8 &&
                        AbstractedStrategyUtils.getProbability(s.getSequenceFor(gameInfo.getAllPlayers()[1]), p1Strategy, currentAbstractionISKeys, perfectRecallExpander) > 1e-8)
                .map(s -> s.getISKeyForPlayerToMove()).distinct()).count();
    }

    protected abstract void iteration(Player player);

    public int getIteration() {
        return iteration;
    }
}
