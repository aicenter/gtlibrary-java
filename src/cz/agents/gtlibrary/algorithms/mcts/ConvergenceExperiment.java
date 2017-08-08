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


/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.agents.gtlibrary.algorithms.mcts;

import cz.agents.gtlibrary.algorithms.cfr.CFRAlgorithm;
import cz.agents.gtlibrary.algorithms.cfr.CFRISAlgorithm;
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
import cz.agents.gtlibrary.domain.bpg.BPGExpander;
import cz.agents.gtlibrary.domain.bpg.BPGGameInfo;
import cz.agents.gtlibrary.domain.bpg.BPGGameState;
import cz.agents.gtlibrary.domain.flipit.*;
import cz.agents.gtlibrary.domain.goofspiel.GSGameInfo;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielExpander;
import cz.agents.gtlibrary.domain.goofspiel.IIGoofSpielGameState;
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
import cz.agents.gtlibrary.utils.io.GambitEFG;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayDeque;
import java.util.Map;

/**
 * @author vilo
 */
public class ConvergenceExperiment {

    static boolean buildCompleteTree = false;
    static GameInfo gameInfo;
    static GameState rootState;
    static SequenceFormConfig<SequenceInformationSet> sfAlgConfig;
    static Expander<SequenceInformationSet> sfExpander;
    static FullSequenceEFG efg;
    static Map<Player, Map<Sequence, Double>> optStrategies;
    static SQFBestResponseAlgorithm brAlg0;
    static SQFBestResponseAlgorithm brAlg1;
    static Expander<MCTSInformationSet> expander;


    public static void setupRnd(long seed) {
        RandomGameInfo.MAX_DEPTH = 4;
        RandomGameInfo.MAX_BF = 4;
        RandomGameInfo.BINARY_UTILITY = true;
        RandomGameInfo.seed = seed;
        gameInfo = new RandomGameInfo();
        rootState = new RandomGameState();
        expander = new RandomGameExpander<MCTSInformationSet>(new MCTSConfig());
        sfAlgConfig = new SequenceFormConfig<SequenceInformationSet>();
        sfExpander = new RandomGameExpander<SequenceInformationSet>(sfAlgConfig);
        efg = new FullSequenceEFG(rootState, sfExpander, gameInfo, sfAlgConfig);
        optStrategies = efg.generate();
        new GambitEFG().write("RND" + RandomGameInfo.MAX_BF + RandomGameInfo.MAX_DEPTH + "_" + seed + ".efg", rootState, sfExpander);
    }

    public static void setupIIGoofSpielExpl() {
        gameInfo = new GSGameInfo();
        rootState = new IIGoofSpielGameState();
        expander = new GoofSpielExpander<MCTSInformationSet>(new MCTSConfig());
        sfAlgConfig = new SequenceFormConfig<SequenceInformationSet>();
        sfExpander = new GoofSpielExpander<SequenceInformationSet>(sfAlgConfig);
        efg = new FullSequenceEFG(rootState, sfExpander, gameInfo, sfAlgConfig);
        efg.generateCompleteGame();
    }

    public static void setupPoker() {
        setupPoker(1, 3, 5, 4);
    }

    public static void setupPoker(int row, int bets, int types, int cards) {
        GPGameInfo.MAX_RAISES_IN_ROW = row;
        GPGameInfo.MAX_DIFFERENT_BETS = bets;
        GPGameInfo.MAX_DIFFERENT_RAISES = GPGameInfo.MAX_DIFFERENT_BETS;
        GPGameInfo.MAX_CARD_TYPES = types;
        GPGameInfo.MAX_CARD_OF_EACH_TYPE = cards;
        gameInfo = new GPGameInfo();
        rootState = new GenericPokerGameState();
        expander = new GenericPokerExpander<MCTSInformationSet>(new MCTSConfig());
//        sfAlgConfig = new SequenceFormConfig<SequenceInformationSet>();
//        sfExpander = new GenericPokerExpander<SequenceInformationSet>(sfAlgConfig);
//        efg = new FullSequenceEFG(rootState, sfExpander , gameInfo, sfAlgConfig);
//        efg.generate();
    }

    public static void setupBP() {
        gameInfo = new BPGGameInfo();
        rootState = new BPGGameState();
        expander = new BPGExpander<MCTSInformationSet>(new MCTSConfig());
    }

    public static void setupPTTT() {
        gameInfo = new TTTInfo();
        rootState = new TTTState();
        expander = new TTTExpander<MCTSInformationSet>(new MCTSConfig());
        //sfAlgConfig = new SequenceFormConfig<SequenceInformationSet>();
        //sfExpander = new GenericPokerExpander<SequenceInformationSet>(sfAlgConfig);
        //efg = new FullSequenceEFG(rootState, sfExpander , gameInfo, sfAlgConfig);
        //efg.generateCompleteGame();
    }

    public static void setupFlipIt(){
        gameInfo = new FlipItGameInfo();
        ((FlipItGameInfo)gameInfo).ZERO_SUM_APPROX = true;
        GameState rootState = null;
        switch (FlipItGameInfo.gameVersion){
            case NO:                    rootState = new NoInfoFlipItGameState(); break;
            case FULL:                  rootState = new FullInfoFlipItGameState(); break;
            case REVEALED_ALL_POINTS:   rootState = new AllPointsFlipItGameState(); break;
            case REVEALED_NODE_POINTS:  rootState = new NodePointsFlipItGameState(); break;

        }
        expander = new FlipItExpander<MCTSInformationSet>(new MCTSConfig());
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

    static double gamma = 0.6;

    public static void runMCTS() throws Exception {

        int secondsIteration = 5;

        expander.getAlgorithmConfig().createInformationSetFor(rootState);

        CFRAlgorithm alg = new CFRAlgorithm(
                rootState.getAllPlayers()[0],
                rootState, expander);

        CFRISAlgorithm algIS = new CFRISAlgorithm(
                rootState.getAllPlayers()[0],
                rootState, expander);

//        OOSAlgorithm alg = new OOSAlgorithm(
//                rootState.getAllPlayers()[0],
//                new OOSSimulator(expander),
//                rootState, expander, 0, gamma);
        Distribution dist = new MeanStratDist();

//        ISMCTSAlgorithm alg = new ISMCTSAlgorithm(
//                    rootState.getAllPlayers()[0],
//                    new DefaultSimulator(expander),
//                    //new UCTBackPropFactory(2),
//                    new Exp3BackPropFactory(-1, 1, 0.05),
//                    //new RMBackPropFactory(-1,1,0.4),
//                    rootState, expander);
        //alg.returnMeanValue=true;
        //Distribution dist = new FrequenceDistribution();

        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        long start = threadBean.getCurrentThreadCpuTime();
        if (buildCompleteTree) {
            buildCompleteTree(alg.getRootNode());
            System.out.println("Building GT: " + ((threadBean.getCurrentThreadCpuTime() - start) / 1000000));
        }

        if (buildCompleteTree)
            alg.runMiliseconds(100);
        else algIS.runMiliseconds(100);

        brAlg0 = new SQFBestResponseAlgorithm(expander, 0, new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]}, (ConfigImpl) expander.getAlgorithmConfig()/*sfAlgConfig*/, gameInfo);
        brAlg1 = new SQFBestResponseAlgorithm(expander, 1, new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]}, (ConfigImpl) expander.getAlgorithmConfig()/*sfAlgConfig*/, gameInfo);


        Strategy strategy0 = null;
        Strategy strategy1 = null;
        String outLine = "";
        System.out.print("P1BRs: ");

        for (int i = 0; i < 100; i++) {
            if (buildCompleteTree)
                alg.runMiliseconds(secondsIteration * 1000);
            else
                algIS.runMiliseconds(secondsIteration * 1000);
            System.out.println("Cumulative Time: " + (secondsIteration * 1000 * (i + 1)));
            if (buildCompleteTree) {
                strategy0 = StrategyCollector.getStrategyFor(alg.getRootNode(), rootState.getAllPlayers()[0], dist);
                strategy1 = StrategyCollector.getStrategyFor(alg.getRootNode(), rootState.getAllPlayers()[1], dist);
            } else {
                strategy0 = StrategyCollector.getStrategyFor(rootState, rootState.getAllPlayers()[0], dist, algIS.getInformationSets(), expander);
                strategy1 = StrategyCollector.getStrategyFor(rootState, rootState.getAllPlayers()[1], dist, algIS.getInformationSets(), expander);
            }
//            checkCompleteTree(alg2.getRootNode(), alg);

            System.out.print(brAlg1.calculateBR(rootState, ISMCTSExploitability.filterLow(strategy0)) + " ");
            System.out.flush();
            outLine += brAlg0.calculateBR(rootState, ISMCTSExploitability.filterLow(strategy1)) + " ";

            //System.out.println("Strat: " + strategy0.fancyToString(rootState, expander, rootState.getAllPlayers()[0]));
            //System.out.println("BR: " + brAlg.getFullBRSequences());
        }
        System.out.println();
        System.out.println("P0BRs: " + outLine);
        //System.out.println("Strat: " + strategy0.fancyToString(rootState, expander, rootState.getAllPlayers()[0]));
        //System.out.println("Strat: " + strategy1.fancyToString(rootState, expander, rootState.getAllPlayers()[1]));
    }

    public static void main(String[] args) throws Exception {
        final String GP = "GP";
        final String RG = "RG";

        if (args.length > 0) {
            if (args[2].equals(GP)) {
                setupPoker(new Integer(args[3]), new Integer(args[4]), new Integer(args[5]), new Integer(args[6]));
            } else if (args[2].equals(RG)) {

            }
            buildCompleteTree = new Boolean(args[1]);
            runMCTS();
        } else {
//          setupIIGoofSpielExpl();
//          setupPoker();
//          setupRnd(13);
//            setupBP();
            setupFlipIt();
            runMCTS();
        }
    }
}
