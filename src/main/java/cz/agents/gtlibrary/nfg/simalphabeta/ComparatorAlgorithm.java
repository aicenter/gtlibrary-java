/*
Copyright 2014 Faculty of Electrical Engineering at CTU in Prague

This file is part of Game Theoretic Library.

Game Theoretic Library is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Game Theoretic Library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with Game Theoretic Library.  If not, see <http://www.gnu.org/licenses/>.*/


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

public class ComparatorAlgorithm implements GamePlayingAlgorithm {

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
    private ThreadMXBean threadBean;

    public static void main(String[] args) {
        SimAlphaBetaAlgorithm algorithm = new SimAlphaBetaAlgorithm(new PlayerImpl(1), new GoofSpielExpander<SimABInformationSet>(new SimABConfig()), new GSGameInfo(), true, true, true, false);
        GoofSpielGameState root = new GoofSpielGameState();
        long start = System.currentTimeMillis();

        algorithm.runMiliseconds(2000, root.performAction(root.getNatureSequence().getFirst()));
        System.out.println("Actual time needed " + (System.currentTimeMillis() - start));
    }

    public ComparatorAlgorithm(Player player, Expander<SimABInformationSet> expander, GameInfo gameInfo, boolean alphaBetaBounds,
                               boolean doubleOracle, boolean sortingOwnActions, boolean useGlobalCache) {
        this.player = player;
        this.expander = expander;
        this.gameInfo = gameInfo;
        this.alphaBetaBounds = alphaBetaBounds;
        this.doubleOracle = doubleOracle;
        this.sortingOwnActions = sortingOwnActions;
        this.useGlobalCache = useGlobalCache;
        this.random = new Random();
        threadBean = ManagementFactory.getThreadMXBean();
    }

    public ComparatorAlgorithm(Player player, Expander<SimABInformationSet> expander, GameInfo gameInfo, boolean alphaBetaBounds,
                               boolean doubleOracle, boolean sortingOwnActions, boolean useGlobalCache, int seed) {
        this.player = player;
        this.expander = expander;
        this.gameInfo = gameInfo;
        this.alphaBetaBounds = alphaBetaBounds;
        this.doubleOracle = doubleOracle;
        this.sortingOwnActions = sortingOwnActions;
        this.useGlobalCache = useGlobalCache;
        this.random = new Random(seed);
        threadBean = ManagementFactory.getThreadMXBean();
    }

    public Action runMiliseconds(final int miliseconds, final GameState state) {
        Killer.kill = false;
        long nanoLimit = toNanos(miliseconds);
        int sleepTime = miliseconds - milisBuffer(miliseconds);
        Thread thread = new Thread(new Runner(((SimultaneousGameState)state), nanoLimit));
        long threadStart = threadBean.getThreadCpuTime(thread.getId());

        thread.start();

        try {
            thread.join();
            return chooseAction(currentBest);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    private int toMilis(long nanoTime) {
        return ((int) (nanoTime / 1e6));
    }

    private long nanoBuffer(long nanoLimit) {
        return Math.min(((long) (nanoLimit / 20.)), 100000000l);
    }

    private int milisBuffer(int miliseconds) {
        return Math.min(((int) (miliseconds / 10.)), 50);
    }

    private long toNanos(long miliseconds) {
        return miliseconds * 1000000l;
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

        private SimultaneousGameState state;
        private long limit;
        private ThreadMXBean threadBean;


        public Runner(SimultaneousGameState state, long limit) {
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
                state.setDepth(depth++);
                SimAlphaBeta solver = new SimAlphaBeta();
                long currentIterationStart = threadBean.getCurrentThreadCpuTime();
                SimAlphaBetaResult result = solver.runSimAlphabeta(state, expander, player, false, false, sortingOwnActions, false, gameInfo);
                long currentIterationTime = threadBean.getCurrentThreadCpuTime() - currentIterationStart;

                debugOutput.println("Iteration for depth " + (depth - 1) + " ended in " + (threadBean.getCurrentThreadCpuTime() - start));
                if (threadBean.getCurrentThreadCpuTime() - start > limit) {
                    System.out.println("limit: " + limit + " time taken: " + (threadBean.getCurrentThreadCpuTime() - start));
                    debugOutput.println("Time run out for depth " + depth);
                    System.out.println("Depth " + (depth - 1) + " finnished");
                    state.setDepth(depth - 1);
                    double oldValue = result.gameValue;
                    System.out.println("//////////////////////////////////////");
                    solver = new SimAlphaBeta();
                    SimAlphaBetaResult doResult = solver.runSimAlphabeta(state, expander, player, true, true, sortingOwnActions, false, gameInfo);
                    if(Math.abs(oldValue - doResult.gameValue) > 1e-8) {
                        throw new IllegalStateException(oldValue + " vs " + doResult.gameValue);
                    }
                    return;
                }
                currentBest = result.strategy;
                if (isTimeLeftSmallerThanTimeNeededToFinnishLastIteration(limit, start, currentIterationTime)) {
                    System.out.println("limit: " + limit + " time taken: " + (threadBean.getCurrentThreadCpuTime() - start));
                    debugOutput.println("Time run out for depth " + depth);
                    System.out.println("Depth " + (depth) + " finnished");
                    System.out.println();
                    double oldValue = result.gameValue;
                    System.out.println("//////////////////////////////////////");
                    solver = new SimAlphaBeta();
                    SimAlphaBetaResult doResult = solver.runSimAlphabeta(state, expander, player, true, true, sortingOwnActions, false, gameInfo);
                    if(Math.abs(oldValue - doResult.gameValue) > 1e-8) {
                        throw new IllegalStateException(oldValue + " vs " + doResult.gameValue);
                    }
                    return;
                }
            }
        }

        private boolean isTimeLeftSmallerThanTimeNeededToFinnishLastIteration(long limit, long start, long currentIterationTime) {
            return limit - (threadBean.getCurrentThreadCpuTime() - start) < currentIterationTime;
        }

    }
}
