package cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.memeff;

import cz.agents.gtlibrary.algorithms.cfr.ir.IRCFRInformationSet;
import cz.agents.gtlibrary.domain.randomabstraction.IDObservation;
import cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.CFRBRData;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.iinodes.ImperfectRecallISKey;
import cz.agents.gtlibrary.iinodes.Observations;
import cz.agents.gtlibrary.interfaces.*;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class AutomatedAbstractionAlgorithm {

    protected final GameState rootState;
    protected final Expander<? extends InformationSet> expander;
    protected final GameInfo gameInfo;
    protected final Map<ISKey, IRCFRInformationSet> currentAbstractionInformationSets;
    protected final InformationSetKeyMap currentAbstractionISKeys;
    protected int iteration = 0;
    protected int isKeyCounter = 0;

    protected MemoryMXBean memoryBean;
    protected ThreadMXBean threadBean;
    long startTime;

    public AutomatedAbstractionAlgorithm(GameState rootState, Expander<? extends InformationSet> expander, GameInfo info) {
        this.rootState = rootState;
        this.expander = expander;
        this.gameInfo = info;
        currentAbstractionInformationSets = new HashMap<>();
        currentAbstractionISKeys = new InformationSetKeyMap();
        memoryBean = ManagementFactory.getMemoryMXBean();
        threadBean = ManagementFactory.getThreadMXBean();
        buildInitialAbstraction();
    }

    protected void buildInitialAbstraction() {
        buildInformationSets(rootState);
        addData(currentAbstractionInformationSets.values());
        System.out.println("Init abstr P1 IS: " + currentAbstractionInformationSets.values().stream().filter(i -> i.getPlayer().getId() == 0).count());
        System.out.println("Init abstr P2 IS: " + currentAbstractionInformationSets.values().stream().filter(i -> i.getPlayer().getId() == 1).count());
    }

    protected void buildInformationSets(GameState state) {
        if (state.isGameEnd())
            return;
        ImperfectRecallISKey key = getAbstractionISKey(state);
        IRCFRInformationSet set = currentAbstractionInformationSets.computeIfAbsent(key, k -> new IRCFRInformationSet(state, key));

        set.addStateToIS(state);//is it really necessary to store the states here? maybe one for expander is enough
        expander.getActions(state).stream().map(a -> state.performAction(a)).forEach(s -> buildInformationSets(s));
    }


    protected void addData(Collection<IRCFRInformationSet> informationSets) {
        informationSets.forEach(i -> i.setData(new CFRBRData(this.expander.getActions(i.getAllStates().stream().findAny().get()).size())));
    }

    public void runIterations(int iterations) {
        startTime = threadBean.getCurrentThreadCpuTime();
        for (int i = 0; i < iterations; i++) {
            this.iteration++;
            iteration(rootState.getAllPlayers()[1]);
            iteration(rootState.getAllPlayers()[0]);
            if (isConverged(gameInfo.getMaxUtility() * 1e-3))
                return;
            if (i % 20 == 0 || iteration == 1)
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
        return currentAbstractionISKeys.get(state, expander);
    }

    protected ImperfectRecallISKey createCounterISKey(Player player) {
        Observations observations = new Observations(player, player);

        observations.add(new IDObservation(isKeyCounter++));
        return new ImperfectRecallISKey(observations, null, null);
    }

    protected abstract boolean isConverged(double v);

    protected void printStatistics() {
        System.out.println("*************************************************");
        System.out.println("Iteration: " + iteration);
        System.out.println("Current IS count: " + currentAbstractionInformationSets.size());
        System.out.println("Current time: " + (threadBean.getCurrentThreadCpuTime() - startTime) / 1e6);
        System.out.println("Current memory: " + memoryBean.getHeapMemoryUsage().getUsed());
        System.out.println("Max memory: " + memoryBean.getHeapMemoryUsage().getMax());
        System.out.println(memoryBean.getHeapMemoryUsage().toString());
    }

    protected abstract void iteration(Player player);

}
