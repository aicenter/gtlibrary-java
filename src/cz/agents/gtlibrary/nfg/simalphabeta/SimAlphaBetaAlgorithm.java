package cz.agents.gtlibrary.nfg.simalphabeta;

import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNode;
import cz.agents.gtlibrary.domain.goofspiel.GSGameInfo;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielExpander;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielGameState;
import cz.agents.gtlibrary.iinodes.PlayerImpl;
import cz.agents.gtlibrary.iinodes.SimultaneousGameState;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.nfg.ActionPureStrategy;
import cz.agents.gtlibrary.nfg.MixedStrategy;
import cz.agents.gtlibrary.utils.io.EmptyPrintStream;

import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Map;
import java.util.Random;

public class SimAlphaBetaAlgorithm implements GamePlayingAlgorithm {

    private boolean alphaBetaBounds;
    private boolean doubleOracle;
    private boolean sortingOwnActions;
    private boolean useGlobalCache;
    private final GameInfo gameInfo;
    private final Player player;
    private final Random random;
    private final Expander<SimABInformationSet> expander;
    private final PrintStream debugOutput = new PrintStream(EmptyPrintStream.getInstance());
    private volatile MixedStrategy<ActionPureStrategy> currentBest;

    public static void main(String[] args) {
        SimAlphaBetaAlgorithm algorithm = new SimAlphaBetaAlgorithm(new PlayerImpl(1), new GoofSpielExpander<SimABInformationSet>(new SimABConfig()), new GSGameInfo(), true, true, true, false);
        GoofSpielGameState root = new GoofSpielGameState();
        long start = System.currentTimeMillis();

        algorithm.runMiliseconds(2000, root.performAction(root.getNatureSequence().getFirst()));
        System.out.println("Actual time needed " + (System.currentTimeMillis() - start));
    }

    public SimAlphaBetaAlgorithm(Player player, Expander<SimABInformationSet> expander, GameInfo gameInfo, boolean alphaBetaBounds,
                                 boolean doubleOracle, boolean sortingOwnActions, boolean useGlobalCache) {
        this.player = player;
        this.expander = expander;
        this.gameInfo = gameInfo;
        this.alphaBetaBounds = alphaBetaBounds;
        this.doubleOracle = doubleOracle;
        this.sortingOwnActions = sortingOwnActions;
        this.useGlobalCache = useGlobalCache;
        this.random = new Random();
    }

    public SimAlphaBetaAlgorithm(Player player, Expander<SimABInformationSet> expander, GameInfo gameInfo, boolean alphaBetaBounds,
                                 boolean doubleOracle, boolean sortingOwnActions, boolean useGlobalCache, int seed) {
        this.player = player;
        this.expander = expander;
        this.gameInfo = gameInfo;
        this.alphaBetaBounds = alphaBetaBounds;
        this.doubleOracle = doubleOracle;
        this.sortingOwnActions = sortingOwnActions;
        this.useGlobalCache = useGlobalCache;
        this.random = new Random(seed);
    }

    public Action runMiliseconds(final int miliseconds, final GameState state) {
        Killer.kill = false;
        Thread thread = new Thread(new Runner(state, ((long) miliseconds) * 1000000l));

        thread.start();
        try {
            Thread.currentThread().sleep(miliseconds - Math.min(((int) (miliseconds / 10.)), 100));
            Killer.kill = true;
            thread.join();
            return chooseAction(currentBest);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Action chooseAction(MixedStrategy<ActionPureStrategy> bestStrategy) {
        if (bestStrategy == null)
            throw new IllegalStateException("Simultaneous alpha-beta was unable to solve the game in given time...");
        double randomValue = random.nextDouble();

        for (Map.Entry<ActionPureStrategy, Double> strategyEntry : bestStrategy) {
            randomValue -= strategyEntry.getValue();
            if (randomValue <= 1e-12)
                return strategyEntry.getKey().getAction();
        }
        return null;
    }

    @Override
    public Action runMiliseconds(int miliseconds) {
        return null;
    }

    public void setCurrentIS(InformationSet currentIS) {

    }

    public InnerNode getRootNode() {
        return null;
    }

    public class Runner implements Runnable {

        private GameState state;
        private long limit;
        private ThreadMXBean threadBean;


        public Runner(GameState state, long limit) {
            this.limit = limit;
            this.state = state;
            this.threadBean = ManagementFactory.getThreadMXBean();
        }

        @Override
        public void run() {
            int depth = 1;
            long start = threadBean.getCurrentThreadCpuTime();

            while (true) {
                debugOutput.println("Running with depth " + depth);
                ((SimultaneousGameState) state).setDepth(depth++);
                SimAlphaBeta solver = new SimAlphaBeta();
                long currentIterationStart = threadBean.getCurrentThreadCpuTime();
                MixedStrategy<ActionPureStrategy> currentStrategy = solver.runSimAlpabeta(state, expander, player, alphaBetaBounds, doubleOracle, sortingOwnActions, useGlobalCache, gameInfo);
                long currentIterationTime = threadBean.getCurrentThreadCpuTime() - currentIterationStart;

                debugOutput.println("Iteration for depth " + (depth - 1) + " ended in " + (threadBean.getCurrentThreadCpuTime() - start));
                if (threadBean.getCurrentThreadCpuTime() - start > limit) {
                    System.out.println("limit: " + limit + " time taken: " + (threadBean.getCurrentThreadCpuTime() - start));
                    debugOutput.println("Time run out for depth " + depth);
                    System.out.println("Depth " + (depth - 1) + " finnished");
                    return;
                }
                if (Killer.kill)
                    return;
                currentBest = currentStrategy;
                if (isTimeLeftSmallerThanTimeNeededToFinnishLastIteration(limit, start, currentIterationTime)) {
                    System.out.println("limit: " + limit + " time taken: " + (threadBean.getCurrentThreadCpuTime() - start));
                    debugOutput.println("Time run out for depth " + depth);
                    System.out.println("Depth " + (depth) + " finnished");
                    return;
                }
            }
        }

        private boolean isTimeLeftSmallerThanTimeNeededToFinnishLastIteration(long limit, long start, long currentIterationTime) {
            return limit - (threadBean.getCurrentThreadCpuTime() - start) < currentIterationTime;
        }

    }
}
