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


package cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle;

import cz.agents.gtlibrary.algorithms.cfr.CFRAlgorithm;
import cz.agents.gtlibrary.algorithms.cfr.CFRISAlgorithm;
import cz.agents.gtlibrary.algorithms.mcts.ISMCTSExploitability;
import cz.agents.gtlibrary.algorithms.mcts.MCTSConfig;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.distribution.Distribution;
import cz.agents.gtlibrary.algorithms.mcts.distribution.MeanStratDist;
import cz.agents.gtlibrary.algorithms.mcts.distribution.StrategyCollector;
import cz.agents.gtlibrary.algorithms.mcts.nodes.ChanceNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.Node;
import cz.agents.gtlibrary.algorithms.mcts.oos.OOSAlgorithmData;
import cz.agents.gtlibrary.algorithms.sequenceform.FullSequenceEFG;
import cz.agents.gtlibrary.algorithms.sequenceform.SQFBestResponseAlgorithm;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.improvedBR.DoubleOracleWithBestMinmaxImprovement;
import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.unprunning.UnprunningDoubleOracle;
import cz.agents.gtlibrary.domain.bpg.BPGExpander;
import cz.agents.gtlibrary.domain.bpg.BPGGameInfo;
import cz.agents.gtlibrary.domain.bpg.BPGGameState;
import cz.agents.gtlibrary.domain.goofspiel.GSGameInfo;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielExpander;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielGameState;
import cz.agents.gtlibrary.domain.phantomTTT.TTTExpander;
import cz.agents.gtlibrary.domain.phantomTTT.TTTInfo;
import cz.agents.gtlibrary.domain.phantomTTT.TTTState;
import cz.agents.gtlibrary.domain.poker.generic.GPGameInfo;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerExpander;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerGameState;
import cz.agents.gtlibrary.domain.randomgame.RandomGameExpander;
import cz.agents.gtlibrary.domain.randomgame.RandomGameInfo;
import cz.agents.gtlibrary.domain.randomgame.RandomGameState;
import cz.agents.gtlibrary.iinodes.ConfigImpl;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.strategy.Strategy;
import cz.agents.gtlibrary.utils.HighQualityRandom;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayDeque;

/**
 * Created with IntelliJ IDEA.
 * User: bosansky
 * Date: 3/8/13
 * Time: 10:09 AM
 * To change this template use File | Settings | File Templates.
 */
public class DoubleOracleExperiments {

    static GameInfo gameInfo;
    static GameState rootState;
    static SequenceFormConfig<SequenceInformationSet> sfAlgConfig;
    static SQFBestResponseAlgorithm brAlg0;
    static SQFBestResponseAlgorithm brAlg1;
    static Expander<MCTSInformationSet> expander;


    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Missing Arguments: DoubleOracleExperiments {DO|LP} {BP|GP|RG} [algorithm/domain parameters].");
            System.exit(-1);
        }
        String alg = args[0];
        DoubleOracleExperiments exp = new DoubleOracleExperiments();
        exp.handleDomain(args);
        exp.runAlgorithm(alg, args[1]);
    }


    public void handleDomain(String[] args) {
        if (args[1].equalsIgnoreCase("BP")) {  // Border Patrolling
            if (args.length != 5) {
                throw new IllegalArgumentException("Illegal domain arguments count. 3 are required {DEPTH} {GRAPH} {SLOW_MOVES}");
            }
            int depth = new Integer(args[2]);
            BPGGameInfo.graphFile = args[3];
            BPGGameInfo.SLOW_MOVES = new Boolean(args[4]);
            BPGGameInfo.DEPTH = depth;
        } else if (args[1].equalsIgnoreCase("GP")) { // Generic Poker
            if (args.length != 6) {
                throw new IllegalArgumentException("Illegal poker domain arguments count. 4 are required {MAX_RISES} {MAX_BETS} {MAX_CARD_TYPES} {MAX_CARD_OF_EACH_TYPE}");
            }
            GPGameInfo.MAX_RAISES_IN_ROW = new Integer(args[2]);
            GPGameInfo.MAX_DIFFERENT_BETS = new Integer(args[3]);
            GPGameInfo.MAX_DIFFERENT_RAISES = GPGameInfo.MAX_DIFFERENT_BETS;
            GPGameInfo.MAX_CARD_TYPES = new Integer(args[4]);
            GPGameInfo.MAX_CARD_OF_EACH_TYPE = new Integer(args[5]);
        } else if (args[1].equalsIgnoreCase("RG")) { // Random Games
            if (args.length != 9) {
                throw new IllegalArgumentException("Illegal random game domain arguments count. 7 are required {SEED} {DEPTH} {BF} {OBSERVATION} {UTILITY} {BIN} {CORR}");
            }
            RandomGameInfo.seed = new Integer(args[2]);
            RandomGameInfo.rnd = new HighQualityRandom(RandomGameInfo.seed);
            RandomGameInfo.MAX_DEPTH = new Integer(args[3]);
            RandomGameInfo.MAX_BF = new Integer(args[4]);
            RandomGameInfo.MAX_OBSERVATION = new Integer(args[5]);
            RandomGameInfo.MAX_UTILITY = new Integer(args[6]);
            RandomGameInfo.BINARY_UTILITY = new Boolean(args[7]);
            RandomGameInfo.UTILITY_CORRELATION = new Boolean(args[8]);
        } else if (args[1].equalsIgnoreCase("TTT")) { // Phantom TicTacToe
            if (args.length != 3) {
                throw new IllegalArgumentException("Illegal random game domain arguments count. 1 are required {DOMAIN_EXPANDER?}");
            }
            TTTInfo.useDomainDependentExpander = new Boolean(args[2]);
        } else {
            throw new IllegalArgumentException("Illegal domain: " + args[1]);
        }
    }

    public void runAlgorithm(String alg, String domain) {
        if (alg.startsWith("DO")) {
            if (alg.equalsIgnoreCase("DO-B")) {
                GeneralDoubleOracle.playerSelection = GeneralDoubleOracle.PlayerSelection.BOTH;
            } else if (alg.equalsIgnoreCase("DO-SA")) {
                GeneralDoubleOracle.playerSelection = GeneralDoubleOracle.PlayerSelection.SINGLE_ALTERNATING;
            } else if (alg.equalsIgnoreCase("DO-SI")) {
                GeneralDoubleOracle.playerSelection = GeneralDoubleOracle.PlayerSelection.SINGLE_IMPROVED;
            }
            if (domain.equalsIgnoreCase("BP"))
                GeneralDoubleOracle.runBP();
            else if (domain.equalsIgnoreCase("GP"))
                GeneralDoubleOracle.runGenericPoker();
            else if (domain.equalsIgnoreCase("RG"))
                GeneralDoubleOracle.runRandomGame();
            else if (domain.equalsIgnoreCase("TTT"))
                GeneralDoubleOracle.runPhantomTTT();
        } else if (alg.equals("UDO")) {
            UnprunningDoubleOracle.main(null);
        } else if (alg.equals("MIDO")) {
            DoubleOracleWithBestMinmaxImprovement.runRandomGame();
        } else if (alg.equalsIgnoreCase("LP")) {
            if (domain.equalsIgnoreCase("BP"))
                FullSequenceEFG.runBPG();
            else if (domain.equalsIgnoreCase("GP"))
                FullSequenceEFG.runGenericPoker();
            else if (domain.equalsIgnoreCase("RG"))
                FullSequenceEFG.runRandomGame();
        } else if (alg.startsWith("CFR")) {
            boolean buildTree = alg.equals("CFR-TRUE");
            if (domain.equals("BP")) {
                gameInfo = new BPGGameInfo();
                rootState = new BPGGameState();
                expander = new BPGExpander<MCTSInformationSet>(new MCTSConfig());
                sfAlgConfig = new SequenceFormConfig<SequenceInformationSet>();
            } else if (domain.equals("GP")) {
                gameInfo = new GPGameInfo();
                rootState = new GenericPokerGameState();
                expander = new GenericPokerExpander<MCTSInformationSet>(new MCTSConfig());
                sfAlgConfig = new SequenceFormConfig<SequenceInformationSet>();
            } else if (domain.equals("RG")) {
                gameInfo = new RandomGameInfo();
                rootState = new RandomGameState();
                expander = new RandomGameExpander<MCTSInformationSet>(new MCTSConfig());
                sfAlgConfig = new SequenceFormConfig<SequenceInformationSet>();
            } else if (domain.equals("PTTT")) {
                gameInfo = new TTTInfo();
                rootState = new TTTState();
                expander = new TTTExpander<MCTSInformationSet>(new MCTSConfig());
                sfAlgConfig = new SequenceFormConfig<SequenceInformationSet>();
            }
            runCFR(buildTree);
        } else throw new IllegalArgumentException("Illegal algorithm: " + alg);
    }

    public void runCFR(boolean buildTree) {

        double secondsIteration = 0.1;

        expander.getAlgorithmConfig().createInformationSetFor(rootState);

        GamePlayingAlgorithm alg = (buildTree) ? new CFRAlgorithm(
                rootState.getAllPlayers()[0],
                rootState, expander) : new CFRISAlgorithm(
                rootState.getAllPlayers()[0],
                rootState, expander);

        Distribution dist = new MeanStratDist();

        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        if (buildTree) {
            long start = threadBean.getCurrentThreadCpuTime();
            buildCompleteTree(alg.getRootNode());
            System.out.println("Building GT: " + ((threadBean.getCurrentThreadCpuTime() - start) / 1000000));
        }


        alg.runMiliseconds(100);

        brAlg0 = new SQFBestResponseAlgorithm(expander, 0, new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]}, (ConfigImpl) expander.getAlgorithmConfig()/*sfAlgConfig*/, gameInfo);
        brAlg1 = new SQFBestResponseAlgorithm(expander, 1, new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]}, (ConfigImpl) expander.getAlgorithmConfig()/*sfAlgConfig*/, gameInfo);


        Strategy strategy0 = null;
        Strategy strategy1 = null;
        System.out.print("P1BRs: ");

        double br1Val = Double.POSITIVE_INFINITY;
        double br0Val = Double.POSITIVE_INFINITY;
        double cumulativeTime = 0;

        for (int i = 0; cumulativeTime < 1800000 && (br0Val + br1Val > 0.005); i++) {
            alg.runMiliseconds((int) (secondsIteration * 1000));
            cumulativeTime += secondsIteration * 1000;
            System.out.println("Cumulative Time: " + (cumulativeTime));
            if (buildTree) {
                strategy0 = StrategyCollector.getStrategyFor(alg.getRootNode(), rootState.getAllPlayers()[0], dist);
                strategy1 = StrategyCollector.getStrategyFor(alg.getRootNode(), rootState.getAllPlayers()[1], dist);
            } else {
                strategy0 = StrategyCollector.getStrategyFor(rootState, rootState.getAllPlayers()[0], dist, ((CFRISAlgorithm) alg).getInformationSets(), expander);
                strategy1 = StrategyCollector.getStrategyFor(rootState, rootState.getAllPlayers()[1], dist, ((CFRISAlgorithm) alg).getInformationSets(), expander);
            }

            br1Val = brAlg1.calculateBR(rootState, ISMCTSExploitability.filterLow(strategy0));
            br0Val = brAlg0.calculateBR(rootState, ISMCTSExploitability.filterLow(strategy1));
//            System.out.println("BR1: " + br1Val);
//            System.out.println("BR0: " + br0Val);
            System.out.println("Precision: " + (br0Val + br1Val));
            System.out.flush();
            secondsIteration *= 2;
        }
    }

    public static void buildCompleteTree(InnerNode r) {
        System.out.println("Building complete tree.");
        int nodes = 0, infosets = 0;
        ArrayDeque<InnerNode> q = new ArrayDeque<InnerNode>();
        q.add(r);
        while (!q.isEmpty()) {
            nodes++;
            InnerNode n = q.removeFirst();
            MCTSInformationSet is = n.getInformationSet();
            if (!(n instanceof ChanceNode))
                if (is.getAlgorithmData() == null) {
                    infosets++;
                    is.setAlgorithmData(new OOSAlgorithmData(n.getActions()));
                }
            for (Action a : n.getActions()) {
                Node ch = n.getChildFor(a);
                if (ch instanceof InnerNode) {
                    q.add((InnerNode) ch);
                }
            }
        }
        System.out.println("Created nodes: " + nodes + "; infosets: " + infosets);
    }

}
