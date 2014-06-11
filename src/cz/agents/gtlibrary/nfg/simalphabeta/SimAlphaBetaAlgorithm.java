package cz.agents.gtlibrary.nfg.simalphabeta;

import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNode;
import cz.agents.gtlibrary.domain.goofspiel.GSGameInfo;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielExpander;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielGameState;
import cz.agents.gtlibrary.iinodes.PlayerImpl;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.nfg.ActionPureStrategy;
import cz.agents.gtlibrary.nfg.MixedStrategy;

import java.io.PrintStream;
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
    private PrintStream debugOutput = System.out;//EmptyPrintStream.getInstance();

    public static void main(String[] args) {
        SimAlphaBetaAlgorithm algorithm = new SimAlphaBetaAlgorithm(new PlayerImpl(1), new GoofSpielExpander<SimABInformationSet>(new SimABConfig()), new GSGameInfo(), false, true, true, false);
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

    public Action runMiliseconds(int miliseconds, GameState state) {
        int depth = 1;
        long start = System.currentTimeMillis();
        MixedStrategy<ActionPureStrategy> bestStrategy = null;

        while (true) {
            debugOutput.println("Running with depth " + depth);
            ((GoofSpielGameState) state).setDepth(depth++);
            SimAlphaBeta solver = new SimAlphaBeta();
            long currentIterationStart = System.currentTimeMillis();
            MixedStrategy<ActionPureStrategy> currentStrategy = solver.runSimAlpabeta(state, expander, player, alphaBetaBounds, doubleOracle, sortingOwnActions, useGlobalCache, gameInfo);
            long currentIterationTime = System.currentTimeMillis() - currentIterationStart;
            debugOutput.println("Iteration for depth " + depth + " ended in " + (System.currentTimeMillis() - start));
            if (isTimeLeftSmallerThanTimeNeededToFinnishLastIteration(miliseconds, start, currentIterationTime)) {
                debugOutput.println("Time run out for depth " + depth);
                return chooseAction(bestStrategy);
            }
            bestStrategy = currentStrategy;
        }
    }

    @Override
    public Action runMiliseconds(int miliseconds) {
        return null;
    }

    private boolean isTimeLeftSmallerThanTimeNeededToFinnishLastIteration(int limit, long start, long currentIterationTime) {
        return limit - (System.currentTimeMillis() - start) < currentIterationTime;
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
