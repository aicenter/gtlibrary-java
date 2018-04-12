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


package cz.agents.gtlibrary.nfg.doubleoracle;

import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.DoubleOracleConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.DoubleOracleInformationSet;
import cz.agents.gtlibrary.domain.goofspiel.GSGameInfo;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielExpander;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielGameState;
import cz.agents.gtlibrary.domain.randomgame.RandomGameExpander;
import cz.agents.gtlibrary.domain.randomgame.RandomGameInfo;
import cz.agents.gtlibrary.domain.randomgame.RandomGameState;
import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.nfg.ActionPureStrategy;
import cz.agents.gtlibrary.nfg.MixedStrategy;
import cz.agents.gtlibrary.nfg.NFGActionUtilityComputer;
import cz.agents.gtlibrary.nfg.PlayerStrategySet;
import cz.agents.gtlibrary.nfg.PureStrategy;
import cz.agents.gtlibrary.nfg.core.ZeroSumGameNESolver;
import cz.agents.gtlibrary.nfg.core.ZeroSumGameNESolverImpl;
import cz.agents.gtlibrary.utils.Pair;

/**
 * Created with IntelliJ IDEA.
 * User: bosansky
 * Date: 5/29/13
 * Time: 10:27 AM
 * To change this template use File | Settings | File Templates.
 */
public class NFGDoubleOracle {

    protected double lowerBound;
    protected double upperBound;

    protected GameState rootState;
    protected Expander<? extends InformationSet> expander;
    protected GameInfo gameInfo;
    protected AlgorithmConfig<? extends InformationSet> algConfig;

    protected PrintStream debugOutput = System.out;

    protected MixedStrategy<ActionPureStrategy> maxPlayerMixedStrategy = new MixedStrategy<ActionPureStrategy>();
    protected MixedStrategy<ActionPureStrategy> minPlayerMixedStrategy = new MixedStrategy<ActionPureStrategy>();

    protected PlayerStrategySet<ActionPureStrategy> maxPlayerStrategySet = new PlayerStrategySet<ActionPureStrategy>();
    protected PlayerStrategySet<ActionPureStrategy> minPlayerStrategySet = new PlayerStrategySet<ActionPureStrategy>();

    protected ZeroSumGameNESolver<ActionPureStrategy, ActionPureStrategy> coreSolver;
    protected double resultValue;

    final protected double EPS = 0.00001;
    final protected static boolean DEBUG = false;
    final protected static boolean MY_RP_BR_ORDERING = false;
    protected ThreadMXBean threadBean ;

    public NFGDoubleOracle(GameState rootState, Expander<? extends InformationSet> expander, GameInfo gameInfo, AlgorithmConfig<? extends InformationSet> algConfig) {
        this.rootState = rootState;
        this.expander = expander;
        this.gameInfo = gameInfo;
        this.algConfig = algConfig;
    }

    public static void main(String[] args) {
        runRandomGame();
    }

    public static void runRandomGame() {
        assert RandomGameInfo.MAX_DEPTH == 1;

        GameState rootState = new RandomGameState();
        GameInfo gameInfo = new RandomGameInfo();
        DoubleOracleConfig algConfig = new DoubleOracleConfig(rootState, gameInfo);
        Expander<DoubleOracleInformationSet> expander = new RandomGameExpander<DoubleOracleInformationSet>(algConfig);
        NFGDoubleOracle doefg = new NFGDoubleOracle(rootState,  expander, gameInfo, algConfig);
        doefg.generate();
    }
    

    public void generate() {
        debugOutput.println("NFG Double Oracle");
        debugOutput.println(gameInfo.getInfo());
        threadBean = ManagementFactory.getThreadMXBean();

        long start = threadBean.getCurrentThreadCpuTime();
        long overallSequenceGeneration = 0;
        long overallBRCalculation = 0;
        long overallCPLEX = 0;
        long overallRGBuilding = 0;
        int iterations = 0;

        Player[] actingPlayers = new Player[] { rootState.getAllPlayers()[0], rootState.getAllPlayers()[1] };
        NFGOracle<ActionPureStrategy>[] brAlgorithms = new NFGBROracle[] {
                new NFGBROracle(gameInfo, rootState, expander, actingPlayers[0], actingPlayers[1]),
                new NFGBROracle(gameInfo, rootState, expander, actingPlayers[1], actingPlayers[0])};

        upperBound = gameInfo.getMaxUtility();
        lowerBound = -upperBound;

        NFGActionUtilityComputer<ActionPureStrategy, ActionPureStrategy> utilityComputer = new NFGActionUtilityComputer<ActionPureStrategy, ActionPureStrategy>(rootState);

        coreSolver = new ZeroSumGameNESolverImpl<ActionPureStrategy, ActionPureStrategy>(utilityComputer);

        while (Math.abs(upperBound - lowerBound) > EPS) {

            iterations++;

            Pair<ActionPureStrategy, Double> maxPlayerOracleResult;
            Pair<ActionPureStrategy, Double> minPlayerOracleResult;

            maxPlayerOracleResult = brAlgorithms[0].getNewStrategy(utilityComputer, minPlayerMixedStrategy);
            minPlayerOracleResult = brAlgorithms[1].getNewStrategy(utilityComputer, maxPlayerMixedStrategy);

            upperBound = Math.min(upperBound, maxPlayerOracleResult.getRight());
            if (maxPlayerStrategySet.add(maxPlayerOracleResult.getLeft())) {
                 coreSolver.addPlayerOneStrategies(maxPlayerStrategySet);
            }

            lowerBound = Math.max(lowerBound, minPlayerOracleResult.getRight());
            if (minPlayerStrategySet.add(minPlayerOracleResult.getLeft())) {
                coreSolver.addPlayerTwoStrategies(minPlayerStrategySet);
            }

            coreSolver.computeNashEquilibrium();
            maxPlayerMixedStrategy = coreSolver.getPlayerOneStrategy();
            minPlayerMixedStrategy = coreSolver.getPlayerTwoStrategy();

            resultValue = coreSolver.getGameValue();
            debugOutput.println("Iteration: " + iterations + " Bounds size: " + Math.abs(upperBound - lowerBound));
        }

        debugOutput.println("Finished.");
        debugOutput.println("final size: FirstPlayer Sequences: " + maxPlayerStrategySet.size() + " \t SecondPlayer Sequences : " + minPlayerStrategySet.size());
        debugOutput.println("final result:" + resultValue);

    }
}
