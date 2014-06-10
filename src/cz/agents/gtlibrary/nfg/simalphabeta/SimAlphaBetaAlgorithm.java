package cz.agents.gtlibrary.nfg.simalphabeta;

import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNode;
import cz.agents.gtlibrary.domain.goofspiel.GSGameInfo;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielExpander;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielGameState;
import cz.agents.gtlibrary.iinodes.PlayerImpl;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.nfg.ActionPureStrategy;
import cz.agents.gtlibrary.nfg.MixedStrategy;
import cz.agents.gtlibrary.utils.io.EmptyPrintStream;

import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Map;
import java.util.Random;

public class SimAlphaBetaAlgorithm implements GamePlayingAlgorithm{

    private boolean alphaBetaBounds;
    private boolean doubleOracle;
    private boolean sortingOwnActions;
    private boolean useGlobalCache;
    private GameInfo gameInfo;
    private Player player;
    private Random random;
    private Expander<SimABInformationSet> expander;
    private PrintStream debugOutput = new PrintStream(EmptyPrintStream.getInstance());
    private ThreadMXBean threadBean;

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
        this.threadBean = ManagementFactory.getThreadMXBean();
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
        this.threadBean = ManagementFactory.getThreadMXBean();
    }

    public Action runMiliseconds(int miliseconds, GameState state) {
        int depth = 1;
        long nanoseconds = miliseconds * 1000000;
        long start = threadBean.getCurrentThreadCpuTime();
        MixedStrategy<ActionPureStrategy> bestStrategy = null;

        while (true) {
            debugOutput.println("Running with depth " + depth);
            ((GoofSpielGameState) state).setDepth(depth++);
            SimAlphaBeta solver = new SimAlphaBeta();
            long currentIterationStart = threadBean.getCurrentThreadCpuTime();
            MixedStrategy<ActionPureStrategy> currentStrategy = solver.runSimAlpabeta(state, expander, player, alphaBetaBounds, doubleOracle, sortingOwnActions, useGlobalCache, gameInfo);
            long currentIterationTime = threadBean.getCurrentThreadCpuTime() - currentIterationStart;
            debugOutput.println("Iteration for depth " + (depth - 1) + " ended in " + (threadBean.getCurrentThreadCpuTime() - start));
            if(threadBean.getCurrentThreadCpuTime() - start > nanoseconds) {
                System.out.println("limit: " + nanoseconds + " time taken: " + currentIterationTime);
                debugOutput.println("Time run out for depth " + depth);
                System.out.println("Depth " + (depth - 1) + " finnished");
                return chooseAction(bestStrategy);
            }
            bestStrategy = currentStrategy;
            if (isTimeLeftSmallerThanTimeNeededToFinnishLastIteration(nanoseconds, start, currentIterationTime)) {
                System.out.println("limit: " + nanoseconds + " time taken: " + currentIterationTime);
                debugOutput.println("Time run out for depth " + depth);
                System.out.println("Depth " + (depth) + " finnished");
                return chooseAction(bestStrategy);
            }
        }
    }

    @Override
    public Action runMiliseconds(int miliseconds) {
        return null;
    }

    private boolean isTimeLeftSmallerThanTimeNeededToFinnishLastIteration(long limit, long start, long currentIterationTime) {
        return limit - (threadBean.getCurrentThreadCpuTime() - start) < currentIterationTime;
    }

    private Action chooseAction(MixedStrategy<ActionPureStrategy> bestStrategy) {
        if(bestStrategy == null)
            throw new IllegalStateException("Simultaneous alpha-beta was unable to solve the game in given time...");
        double randomValue = random.nextDouble();

        for (Map.Entry<ActionPureStrategy, Double> strategyEntry : bestStrategy) {
            randomValue -= strategyEntry.getValue();
            if (randomValue <= 1e-12)
                return strategyEntry.getKey().getAction();
        }
        return null;
    }

    public void setCurrentIS(InformationSet currentIS) {

    }

    public InnerNode getRootNode() {
        return null;
    }
}
