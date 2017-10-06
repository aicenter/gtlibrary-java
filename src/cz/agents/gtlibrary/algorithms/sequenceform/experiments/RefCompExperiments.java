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


package cz.agents.gtlibrary.algorithms.sequenceform.experiments;
//
//import cz.agents.gtlibrary.algorithms.cfr.CFRConfig;
//import cz.agents.gtlibrary.algorithms.cfr.vanilla.VanillaCFR;
//import cz.agents.gtlibrary.algorithms.cfr.vanilla.VanillaCFRConfig;
//import cz.agents.gtlibrary.algorithms.cfr.vanilla.VanillaInformationSet;
//import cz.agents.gtlibrary.algorithms.mcts.*;
//import cz.agents.gtlibrary.algorithms.mcts.distribution.FrequenceDistribution;
//import cz.agents.gtlibrary.algorithms.mcts.nodes.ChanceNode;
//import cz.agents.gtlibrary.algorithms.mcts.selectstrat.UCTBackPropFactory;
//import cz.agents.gtlibrary.algorithms.sequenceform.*;
//import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.undominated.UndominatedSolver;
//import cz.agents.gtlibrary.domain.goofspiel.GSGameInfo;
//import cz.agents.gtlibrary.domain.goofspiel.GoofSpielExpander;
//import cz.agents.gtlibrary.domain.goofspiel.IIGoofSpielGameState;
//import cz.agents.gtlibrary.domain.poker.generic.GPGameInfo;
//import cz.agents.gtlibrary.domain.poker.generic.GenericPokerExpander;
//import cz.agents.gtlibrary.domain.poker.generic.GenericPokerGameState;
//import cz.agents.gtlibrary.domain.poker.kuhn.KPGameInfo;
//import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerExpander;
//import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerGameState;
//import cz.agents.gtlibrary.domain.randomgame.RandomGameExpander;
//import cz.agents.gtlibrary.domain.randomgame.RandomGameInfo;
//import cz.agents.gtlibrary.domain.randomgame.RandomGameState;
//import cz.agents.gtlibrary.experimental.utils.UtilityCalculator;
//import cz.agents.gtlibrary.strategy.ArrayListSequenceImpl;
//import cz.agents.gtlibrary.interfaces.*;
//import cz.agents.gtlibrary.strategy.NoMissingSeqStrategy;
//import cz.agents.gtlibrary.strategy.NoiseMaker;
//import cz.agents.gtlibrary.strategy.Strategy;
//import cz.agents.gtlibrary.strategy.UniformStrategyForMissingSequences;
//import cz.agents.gtlibrary.utils.Interval;
//import cz.agents.gtlibrary.utils.io.CSVStrategyImport;
//import cz.agents.gtlibrary.utils.io.FileManager;
//import cz.agents.gtlibrary.utils.io.GambitEFG;
//
//import java.io.*;
//import java.util.*;
//
public class RefCompExperiments {
//
//    public static UndominatedSolver undomSolver;
//
//
//    /**
//     * mctsCallCount, mctsItPerCall, mctsRunCount, cfrCallCount, cfrItPerCall, domainType
//     * --> IIGS: number of cards, depth, seedCount
//     * --> KP: ...
//     * --> RG max bf, max depth, max obs, center mod, utility cor, bin utility, seed count
//     * --> GP card types, card count
//     *
//     * @param args
//     */
//    public static void main(String[] args) {
//        if (args[5].equals("IIGS")) {
//            GSGameInfo.CARDS_FOR_PLAYER = new int[Integer.parseInt(args[6])];
//            for (int i = 0; i < GSGameInfo.CARDS_FOR_PLAYER.length; i++) {
//                GSGameInfo.CARDS_FOR_PLAYER[i] = i + 1;
//            }
//            GSGameInfo.depth = Integer.parseInt(args[7]);
//            runIIGoofspiel(Integer.parseInt(args[8]), Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]));
//        } else if (args[5].equals("KP")) {
//            runKuhnPoker(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]));
//        } else if (args[5].equals("RG")) {
//            RandomGameInfo.MAX_BF = Integer.parseInt(args[6]);
//            RandomGameInfo.MAX_DEPTH = Integer.parseInt(args[7]);
//            RandomGameInfo.MAX_OBSERVATION = Integer.parseInt(args[8]);
//            RandomGameInfo.MAX_CENTER_MODIFICATION = Integer.parseInt(args[9]);
//            RandomGameInfo.UTILITY_CORRELATION = Boolean.parseBoolean(args[10]);
//            RandomGameInfo.BINARY_UTILITY = Boolean.parseBoolean(args[11]);
//            RandomGameInfo.MAX_UTILITY = Integer.parseInt(args[12]);
//            runRandomGame(Integer.parseInt(args[12]), Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]));
//        } else if (args[5].equals("GP")) {
//            GPGameInfo.MAX_DIFFERENT_BETS = Integer.parseInt(args[6]);
//            GPGameInfo.MAX_DIFFERENT_RAISES = Integer.parseInt(args[7]);
//            GPGameInfo.MAX_RAISES_IN_ROW = Integer.parseInt(args[8]);
//            GPGameInfo.MAX_CARD_TYPES = Integer.parseInt(args[9]);
//            GPGameInfo.MAX_CARD_OF_EACH_TYPE = Integer.parseInt(args[10]);
//            GPGameInfo.CARD_TYPES = new int[GPGameInfo.MAX_CARD_TYPES];
//            for (int i = 0; i < GPGameInfo.MAX_CARD_TYPES; i++)
//                GPGameInfo.CARD_TYPES[i] = i;
//            GPGameInfo.DECK = new int[GPGameInfo.MAX_CARD_OF_EACH_TYPE * GPGameInfo.MAX_CARD_TYPES];
//            for (int i = 0; i < GPGameInfo.MAX_CARD_TYPES; i++)
//                for (int j = 0; j < GPGameInfo.MAX_CARD_OF_EACH_TYPE; j++) {
//                    GPGameInfo.DECK[i * GPGameInfo.MAX_CARD_OF_EACH_TYPE + j] = i;
//                }
//            GPGameInfo.BETS_FIRST_ROUND = new int[GPGameInfo.MAX_DIFFERENT_BETS];
//            for (int i = 0; i < GPGameInfo.MAX_DIFFERENT_BETS; i++)
//                GPGameInfo.BETS_FIRST_ROUND[i] = (i + 1) * 2;
//            GPGameInfo.RAISES_FIRST_ROUND = new int[GPGameInfo.MAX_DIFFERENT_RAISES];
//            for (int i = 0; i < GPGameInfo.MAX_DIFFERENT_RAISES; i++)
//                GPGameInfo.RAISES_FIRST_ROUND[i] = (i + 1) * 2;
//            GPGameInfo.BETS_SECOND_ROUND = new int[GPGameInfo.BETS_FIRST_ROUND.length];
//            for (int i = 0; i < GPGameInfo.BETS_FIRST_ROUND.length; i++) {
//                GPGameInfo.BETS_SECOND_ROUND[i] = 2 * GPGameInfo.BETS_FIRST_ROUND[i];
//            }
//            GPGameInfo.RAISES_SECOND_ROUND = new int[GPGameInfo.RAISES_FIRST_ROUND.length];
//            for (int i = 0; i < GPGameInfo.RAISES_FIRST_ROUND.length; i++) {
//                GPGameInfo.RAISES_SECOND_ROUND[i] = 2 * GPGameInfo.RAISES_FIRST_ROUND[i];
//            }
//            runGenericPoker(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]));
//        } else {
//            System.err.println("Unsupported domain");
//        }
////        runMPoCHM();
////        runBPG();
////        runKuhnPoker();
////        runGenericPoker();
////        runIIGoofspiel();
////        runRandomGame();
//    }
//
//    private static void runRandomGame(int seedCount, int mctsCallCount, int mctsItPerCall, int mctsRunCount, int cfrCallCount, int cfrItPerCall) {
//        RefCompResultCollection results = new RefCompResultCollection();
//
//        for (int i = 0; i < seedCount; i++) {
//            RandomGameInfo.seed = i;
//            GameInfo gameInfo = new RandomGameInfo();
//            GameState rootState = new RandomGameState();
//            SequenceFormConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
//            Expander<SequenceInformationSet> expander = new RandomGameExpander<SequenceInformationSet>(algConfig);
//            MCTSConfig p1MCTSConfig = new MCTSConfig(new Simulator(1), new UCTBackPropFactory(Math.sqrt(2) * gameInfo.getMaxUtility()), new UniformStrategyForMissingSequences.Factory(), null);
//            MCTSConfig p2MCTSConfig = new MCTSConfig(new Simulator(1), new UCTBackPropFactory(Math.sqrt(2) * gameInfo.getMaxUtility()), new UniformStrategyForMissingSequences.Factory(), null);
//            Expander<MCTSInformationSet> mctsExpader1 = new RandomGameExpander<MCTSInformationSet>(p1MCTSConfig);
//            Expander<MCTSInformationSet> mctsExpander2 = new RandomGameExpander<MCTSInformationSet>(p2MCTSConfig);
//
//            VanillaCFRConfig cfrConfig = new VanillaCFRConfig(rootState);
//            Expander<VanillaInformationSet> cfrExpander = new RandomGameExpander<VanillaInformationSet>(cfrConfig);
//
//            SequenceFormConfig<SequenceInformationSet> quasiConfig = new SequenceFormConfig<SequenceInformationSet>();
//            Expander<SequenceInformationSet> quasiExpander = new RandomGameExpander<SequenceInformationSet>(quasiConfig);
//            undomSolver = null;
//            RefCompResult[] tempResults = runExperiment(rootState, gameInfo, algConfig, expander, p1MCTSConfig, p2MCTSConfig, mctsExpader1, mctsExpander2, cfrExpander, cfrConfig, getDomainType(), mctsCallCount, mctsItPerCall, mctsRunCount, cfrCallCount, cfrItPerCall, quasiExpander);
//            results.addCFRResult(tempResults[0]);
//            results.addQREResult(tempResults[1]);
//            results.addMCTSResult(tempResults[2]);
//        }
//
//        RefCompResult averageResultMCTS = results.getAverageMCTSResult();
//        try {
//            toMatlab(mctsCallCount, mctsItPerCall, mctsRunCount, averageResultMCTS.p1Abs, averageResultMCTS.p1Rel, averageResultMCTS.p2Abs, averageResultMCTS.p2Rel, getColorMap(), new PrintStream(new FileOutputStream(new File(getDomainType() + "depthMCTSAVG.m"))));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        RefCompResult averageResultCFR = results.getAverageCFRResult();
//        try {
//            toMatlab(cfrCallCount, cfrItPerCall, 1, averageResultCFR.p1Abs, averageResultCFR.p1Rel, averageResultCFR.p2Abs, averageResultCFR.p2Rel, getColorMap(), new PrintStream(new FileOutputStream(new File(getDomainType() + "CFRAVG.m"))));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
////        RefCompResult averageResultQRE = results.getAverageQREResult();
////        try {
////            toMatlab(averageResultQRE.p1Abs.values().iterator().next().length, 1, 1, averageResultQRE.p1Abs, averageResultQRE.p1Rel, averageResultQRE.p2Abs, averageResultQRE.p2Rel, getColorMap(), new PrintStream(new FileOutputStream(new File(getDomainType()
////                    + "QREAVG.m"))));
////        } catch (IOException e) {
////            e.printStackTrace();
////        }
//    }
//
//    private static String getDomainType() {
//        return "RGD:" + RandomGameInfo.MAX_DEPTH + " BF:" + RandomGameInfo.MAX_BF + " OBS:" + RandomGameInfo.MAX_OBSERVATION + " BU:" + RandomGameInfo.BINARY_UTILITY + " UC:" + RandomGameInfo.UTILITY_CORRELATION + " MCM:" + RandomGameInfo.MAX_CENTER_MODIFICATION + " seed:" + RandomGameInfo.seed;
//    }
//
//    private static void runIIGoofspiel(int seedCount, int mctsCallCount, int mctsItPerCall, int mctsRunCount, int cfrCallCount, int cfrItPerCall) {
//        RefCompResultCollection results = new RefCompResultCollection();
//        for (int i = 3; i < seedCount + 3; i++) {
//            GSGameInfo.seed = i;
//            GameState rootState = new IIGoofSpielGameState();
//            GameInfo gameInfo = new GSGameInfo();
//            SequenceFormConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
//            GoofSpielExpander<SequenceInformationSet> expander = new GoofSpielExpander<SequenceInformationSet>(algConfig);
//            MCTSConfig p1MCTSConfig = new MCTSConfig(new Simulator(1), new UCTBackPropFactory(Math.sqrt(2) * gameInfo.getMaxUtility()), new UniformStrategyForMissingSequences.Factory(), null);
//            MCTSConfig p2MCTSConfig = new MCTSConfig(new Simulator(1), new UCTBackPropFactory(Math.sqrt(2) * gameInfo.getMaxUtility()), new UniformStrategyForMissingSequences.Factory(), null);
//            Expander<MCTSInformationSet> mctsExpader1 = new GoofSpielExpander<MCTSInformationSet>(p1MCTSConfig);
//            Expander<MCTSInformationSet> mctsExpander2 = new GoofSpielExpander<MCTSInformationSet>(p2MCTSConfig);
//
//            VanillaCFRConfig cfrConfig = new VanillaCFRConfig(rootState);
//            Expander<VanillaInformationSet> cfrExpander = new GoofSpielExpander<VanillaInformationSet>(cfrConfig);
//            SequenceFormConfig<SequenceInformationSet> quasiConfig = new SequenceFormConfig<SequenceInformationSet>();
//            Expander<SequenceInformationSet> quasiExpander = new GoofSpielExpander<SequenceInformationSet>(quasiConfig);
//            undomSolver = null;
//            RefCompResult[] tempResults = runExperiment(rootState, gameInfo, algConfig, expander, p1MCTSConfig, p2MCTSConfig, mctsExpader1, mctsExpander2, cfrExpander, cfrConfig,
//                    "IIGS" + GSGameInfo.CARDS_FOR_PLAYER.length + "" + GSGameInfo.depth + "" + GSGameInfo.seed, mctsCallCount, mctsItPerCall, mctsRunCount, cfrCallCount, cfrItPerCall, quasiExpander);
//            results.addCFRResult(tempResults[0]);
//            results.addQREResult(tempResults[1]);
//            results.addMCTSResult(tempResults[2]);
//        }
//        RefCompResult averageResultMCTS = results.getAverageMCTSResult();
//        try {
//            toMatlab(mctsCallCount, mctsItPerCall, mctsRunCount, averageResultMCTS.p1Abs, averageResultMCTS.p1Rel, averageResultMCTS.p2Abs, averageResultMCTS.p2Rel, getColorMap(), new PrintStream(new FileOutputStream(new File("IIGS" + GSGameInfo.CARDS_FOR_PLAYER.length + "" + GSGameInfo.depth + "depthMCTSAVG.m"))));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        RefCompResult averageResultCFR = results.getAverageCFRResult();
//        try {
//            toMatlab(cfrCallCount, cfrItPerCall, 1, averageResultCFR.p1Abs, averageResultCFR.p1Rel, averageResultCFR.p2Abs, averageResultCFR.p2Rel, getColorMap(), new PrintStream(new FileOutputStream(new File("IIGS" + GSGameInfo.CARDS_FOR_PLAYER.length + "" + GSGameInfo.depth + "CFRAVG.m"))));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
////        RefCompResult averageResultQRE = results.getAverageQREResult();
////        try {
////            toMatlab(averageResultQRE.p1Abs.values().iterator().next().length, 1, 1, averageResultQRE.p1Abs, averageResultQRE.p1Rel, averageResultQRE.p2Abs, averageResultQRE.p2Rel, getColorMap(), new PrintStream(new FileOutputStream(new File("IIGS" + GSGameInfo.CARDS_FOR_PLAYER.length + "" + GSGameInfo.depth + "QREAVG.m"))));
////        } catch (IOException e) {
////            e.printStackTrace();
////        }
//    }
//
////    public static void runAoS() {
////        GameState rootState = new AoSGameState();
////        GameInfo gameInfo = new AoSGameInfo();
////        SequenceFormConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
////        AoSExpander<SequenceInformationSet> expander = new AoSExpander<SequenceInformationSet>(algConfig);
////        MCTSConfig p1MCTSConfig = new MCTSConfig(new Simulator(1), new UCTBackPropFactory(Math.sqrt(2) * gameInfo.getMaxUtility()), new UniformStrategyForMissingSequences.Factory(), null);
////        MCTSConfig p2MCTSConfig = new MCTSConfig(new Simulator(1), new UCTBackPropFactory(Math.sqrt(2) * gameInfo.getMaxUtility()), new UniformStrategyForMissingSequences.Factory(), null);
////        Expander<MCTSInformationSet> mctsExpader1 = new AoSExpander<MCTSInformationSet>(p1MCTSConfig);
////        Expander<MCTSInformationSet> mctsExpander2 = new AoSExpander<MCTSInformationSet>(p2MCTSConfig);
////
////        VanillaCFRConfig cfrConfig = new VanillaCFRConfig(rootState);
////        Expander<VanillaInformationSet> cfrExpander = new AoSExpander<VanillaInformationSet>(cfrConfig);
////
////        runExperiment(rootState, gameInfo, algConfig, expander, p1MCTSConfig, p2MCTSConfig, mctsExpader1, mctsExpander2, cfrExpander, cfrConfig, null, mctsCallCount, mctsItPerCall, mctsRunCount);
////    }
//
////    public static void runMPoCHM() {
////        GameState rootState = new MPoCHMGameState();
////        GameInfo gameInfo = new MPoCHMGameInfo();
////        SequenceFormConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
////        MPoCHMExpander<SequenceInformationSet> expander = new MPoCHMExpander<SequenceInformationSet>(algConfig);
////        FullSequenceEFG efg = new FullSequenceEFG(rootState, expander, gameInfo, algConfig);
////
////        MCTSConfig p1MCTSConfig = new MCTSConfig(new Simulator(1), new UCTBackPropFactory(Math.sqrt(2) * gameInfo.getMaxUtility()), new UniformStrategyForMissingSequences.Factory(), null);
////        MCTSConfig p2MCTSConfig = new MCTSConfig(new Simulator(1), new UCTBackPropFactory(Math.sqrt(2) * gameInfo.getMaxUtility()), new UniformStrategyForMissingSequences.Factory(), null);
////        Expander<MCTSInformationSet> mctsExpader1 = new MPoCHMExpander<MCTSInformationSet>(p1MCTSConfig);
////        Expander<MCTSInformationSet> mctsExpander2 = new MPoCHMExpander<MCTSInformationSet>(p2MCTSConfig);
////
////        VanillaCFRConfig cfrConfig = new VanillaCFRConfig(rootState);
////        Expander<VanillaInformationSet> cfrExpander = new MPoCHMExpander<VanillaInformationSet>(cfrConfig);
////
////        runExperiment(rootState, gameInfo, algConfig, expander, p1MCTSConfig, p2MCTSConfig, mctsExpader1, mctsExpander2, cfrExpander, cfrConfig, null, mctsCallCount, mctsItPerCall, mctsRunCount);
////    }
//
////    public static void runGoofSpiel() {
////        GameState rootState = new GoofSpielGameState();
////        GSGameInfo gameInfo = new GSGameInfo();
////        SequenceFormConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
////        GoofSpielExpander<SequenceInformationSet> expander = new GoofSpielExpander<SequenceInformationSet>(algConfig);
////
////        MCTSConfig p1MCTSConfig = new MCTSConfig(new Simulator(1), new UCTBackPropFactory(Math.sqrt(2) * gameInfo.getMaxUtility()), new UniformStrategyForMissingSequences.Factory(), null);
////        MCTSConfig p2MCTSConfig = new MCTSConfig(new Simulator(1), new UCTBackPropFactory(Math.sqrt(2) * gameInfo.getMaxUtility()), new UniformStrategyForMissingSequences.Factory(), null);
////        Expander<MCTSInformationSet> mctsExpader1 = new GoofSpielExpander<MCTSInformationSet>(p1MCTSConfig);
////        Expander<MCTSInformationSet> mctsExpander2 = new GoofSpielExpander<MCTSInformationSet>(p2MCTSConfig);
////        VanillaCFRConfig cfrConfig = new VanillaCFRConfig(rootState);
////        Expander<VanillaInformationSet> cfrExpander = new GoofSpielExpander<VanillaInformationSet>(cfrConfig);
////
////        runExperiment(rootState, gameInfo, algConfig, expander, p1MCTSConfig, p2MCTSConfig, mctsExpader1, mctsExpander2, cfrExpander, cfrConfig, null, mctsCallCount, mctsItPerCall, mctsRunCount);
////    }
//
//    public static void runGenericPoker(int mctsCallCount, int mctsItPerCall, int mctsRunCount, int cfrCallCount, int cfrItPerCall) {
//        GameState rootState = new GenericPokerGameState();
//        GPGameInfo gameInfo = new GPGameInfo();
//        SequenceFormConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
//        Expander<SequenceInformationSet> expander = new GenericPokerExpander<SequenceInformationSet>(algConfig);
//
//        MCTSConfig p1MCTSConfig = new MCTSConfig(new Simulator(1), new UCTBackPropFactory(Math.sqrt(2) * gameInfo.getMaxUtility()), new UniformStrategyForMissingSequences.Factory(), null);
//        MCTSConfig p2MCTSConfig = new MCTSConfig(new Simulator(1), new UCTBackPropFactory(Math.sqrt(2) * gameInfo.getMaxUtility()), new UniformStrategyForMissingSequences.Factory(), null);
//        Expander<MCTSInformationSet> mctsExpader1 = new GenericPokerExpander<MCTSInformationSet>(p1MCTSConfig);
//        Expander<MCTSInformationSet> mctsExpander2 = new GenericPokerExpander<MCTSInformationSet>(p2MCTSConfig);
//
//        VanillaCFRConfig cfrConfig = new VanillaCFRConfig(rootState);
//        Expander<VanillaInformationSet> cfrExpander = new GenericPokerExpander<VanillaInformationSet>(cfrConfig);
//
//        SequenceFormConfig<SequenceInformationSet> quasiConfig = new SequenceFormConfig<SequenceInformationSet>();
//        Expander<SequenceInformationSet> quasiExpander = new GenericPokerExpander<SequenceInformationSet>(quasiConfig);
//
//        runExperiment(rootState, gameInfo, algConfig, expander, p1MCTSConfig, p2MCTSConfig, mctsExpader1, mctsExpander2, cfrExpander, cfrConfig, "GP" + GPGameInfo.MAX_CARD_TYPES + "" + GPGameInfo.MAX_CARD_OF_EACH_TYPE, mctsCallCount, mctsItPerCall, mctsRunCount, cfrCallCount, cfrItPerCall, quasiExpander);
//    }
//
//    public static void runKuhnPoker(int mctsCallCount, int mctsItPerCall, int mctsRunCount, int cfrCallCount, int cfrItPerCall) {
//        GameState rootState = new KuhnPokerGameState();
//        KPGameInfo gameInfo = new KPGameInfo();
//        SequenceFormConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
//        Expander<SequenceInformationSet> expander = new KuhnPokerExpander<SequenceInformationSet>(algConfig);
//
//        MCTSConfig p1MCTSConfig = new MCTSConfig(new Simulator(1), new UCTBackPropFactory(Math.sqrt(2) * gameInfo.getMaxUtility()), new UniformStrategyForMissingSequences.Factory(), null);
//        MCTSConfig p2MCTSConfig = new MCTSConfig(new Simulator(1), new UCTBackPropFactory(Math.sqrt(2) * gameInfo.getMaxUtility()), new UniformStrategyForMissingSequences.Factory(), null);
//        Expander<MCTSInformationSet> mctsExpader1 = new KuhnPokerExpander<MCTSInformationSet>(p1MCTSConfig);
//        Expander<MCTSInformationSet> mctsExpander2 = new KuhnPokerExpander<MCTSInformationSet>(p2MCTSConfig);
//
//        VanillaCFRConfig cfrConfig = new VanillaCFRConfig(rootState);
//        Expander<VanillaInformationSet> cfrExpander = new KuhnPokerExpander<VanillaInformationSet>(cfrConfig);
//
//        SequenceFormConfig<SequenceInformationSet> quasiConfig = new SequenceFormConfig<SequenceInformationSet>();
//        Expander<SequenceInformationSet> quasiExpander = new KuhnPokerExpander<SequenceInformationSet>(quasiConfig);
//
//        runExperiment(rootState, gameInfo, algConfig, expander, p1MCTSConfig, p2MCTSConfig, mctsExpader1, mctsExpander2, cfrExpander, cfrConfig, "KP", mctsCallCount, mctsItPerCall, mctsRunCount, cfrCallCount, cfrItPerCall, quasiExpander);
//    }
//
////    public static void runBPG() {
////        GameState rootState = new BPGGameState();
////        BPGGameInfo gameInfo = new BPGGameInfo();
////        SequenceFormConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
////        BPGExpander expander = new BPGExpander<SequenceInformationSet>(algConfig);
////
////        MCTSConfig p1MCTSConfig = new MCTSConfig(new Simulator(1), new UCTBackPropFactory(Math.sqrt(2) * gameInfo.getMaxUtility()), new UniformStrategyForMissingSequences.Factory(), null);
////        MCTSConfig p2MCTSConfig = new MCTSConfig(new Simulator(1), new UCTBackPropFactory(Math.sqrt(2) * gameInfo.getMaxUtility()), new UniformStrategyForMissingSequences.Factory(), null);
////        Expander<MCTSInformationSet> mctsExpader1 = new BPGExpander<MCTSInformationSet>(p1MCTSConfig);
////        Expander<MCTSInformationSet> mctsExpander2 = new BPGExpander<MCTSInformationSet>(p2MCTSConfig);
////
////        VanillaCFRConfig cfrConfig = new VanillaCFRConfig(rootState);
////        Expander<VanillaInformationSet> cfrExpander = new BPGExpander<VanillaInformationSet>(cfrConfig);
////
////        runExperiment(rootState, gameInfo, algConfig, expander, p1MCTSConfig, p2MCTSConfig, mctsExpader1, mctsExpander2, cfrExpander, cfrConfig, "BPG", mctsCallCount, mctsItPerCall, mctsRunCount);
////    }
//
//    private static void evaluateStrategiesToConsole(GameState rootState, GameInfo gameInfo, Expander<SequenceInformationSet> expander, Map<String, Strategy> p1NormalStrategies, Map<String, Strategy> p2NormalStrategies,
//                                                    Map<String, Strategy> p1StrategiesWithNoise, Map<String, Strategy> p2StrategiesWithNoise, SequenceFormConfig<SequenceInformationSet> algConfig) {
//        UtilityCalculator calculator = new UtilityCalculator(rootState, expander);
//        SQFBestResponseAlgorithm p1BestResponse = new SQFBestResponseAlgorithm(expander, 0, new Player[]{gameInfo.getAllPlayers()[0], gameInfo.getAllPlayers()[1]}, algConfig, gameInfo);
//        SQFBestResponseAlgorithm p2BestResponse = new SQFBestResponseAlgorithm(expander, 1, new Player[]{gameInfo.getAllPlayers()[0], gameInfo.getAllPlayers()[1]}, algConfig, gameInfo);
//        double gameValue = calculator.computeUtility(p1NormalStrategies.get("p1Normal"), p2NormalStrategies.get("p2Normal"));
//        Map<String, double[]> p1AbsResults = new LinkedHashMap<String, double[]>();
//        Map<String, double[]> p1RelResults = new LinkedHashMap<String, double[]>();
//        Map<String, double[]> p2AbsResults = new LinkedHashMap<String, double[]>();
//        Map<String, double[]> p2RelResults = new LinkedHashMap<String, double[]>();
//
//        for (Map.Entry<String, Strategy> normalStrategyEntry : p1NormalStrategies.entrySet()) {
//            double[] absResults = new double[p2StrategiesWithNoise.size()];
//            double[] relResults = new double[p2StrategiesWithNoise.size()];
//            int index = 0;
//
//            for (Map.Entry<String, Strategy> noisedStrategyEntry : p2StrategiesWithNoise.entrySet()) {
//                Interval p1Interval = new Interval(gameValue /*- epsilon*gameInfo.getMaxUtility()*/, p1BestResponse.calculateBR(rootState, noisedStrategyEntry.getValue()));
//                double strategyValue = calculator.computeUtility(normalStrategyEntry.getValue(), noisedStrategyEntry.getValue());
//
//                absResults[index] = strategyValue;
//                relResults[index] = p1Interval.getRelativePosition(strategyValue);
//                index++;
//                System.out.println(normalStrategyEntry.getKey() + " vs " + noisedStrategyEntry.getKey() + ": " + strategyValue + ", " + p1Interval.getRelativePosition(strategyValue) + " in " + p1Interval);
//                normalStrategyEntry.getValue().sanityCheck(rootState, expander);
//                noisedStrategyEntry.getValue().sanityCheck(rootState, expander);
//            }
//            p1AbsResults.put(normalStrategyEntry.getKey(), absResults);
//            p1RelResults.put(normalStrategyEntry.getKey(), relResults);
//        }
//
//        for (Map.Entry<String, Strategy> normalStrategyEntry : p2NormalStrategies.entrySet()) {
//            double[] absResults = new double[p2StrategiesWithNoise.size()];
//            double[] relResults = new double[p2StrategiesWithNoise.size()];
//            int index = 0;
//
//            for (Map.Entry<String, Strategy> noisedStrategyEntry : p1StrategiesWithNoise.entrySet()) {
//                Interval p2Interval = new Interval(-gameValue/* - epsilon*gameInfo.getMaxUtility()*/, p2BestResponse.calculateBR(rootState, noisedStrategyEntry.getValue()));
//                double strategyValue = -calculator.computeUtility(noisedStrategyEntry.getValue(), normalStrategyEntry.getValue());
//
//                absResults[index] = strategyValue;
//                relResults[index] = p2Interval.getRelativePosition(strategyValue);
//                index++;
//                System.out.println(noisedStrategyEntry.getKey() + " vs " + normalStrategyEntry.getKey() + ": " + strategyValue + ", " + p2Interval.getRelativePosition(strategyValue) + " in " + p2Interval);
//                normalStrategyEntry.getValue().sanityCheck(rootState, expander);
//                noisedStrategyEntry.getValue().sanityCheck(rootState, expander);
//
//            }
//            p2AbsResults.put(normalStrategyEntry.getKey(), absResults);
//            p2RelResults.put(normalStrategyEntry.getKey(), relResults);
//        }
//        exportToCSV(p1AbsResults, p1RelResults, p2AbsResults, p2RelResults, p1StrategiesWithNoise, p2StrategiesWithNoise);
//    }
//
//    private static Map<Sequence, Double> getUniformStrategyForP1(GameState rootState, Expander<SequenceInformationSet> expander) {
//        return getUniformStrategyFor(rootState, expander, rootState.getAllPlayers()[0], new HashMap<Sequence, Double>(), 1d);
//    }
//
//    private static Map<Sequence, Double> getUniformStrategyForP2(GameState rootState, Expander<SequenceInformationSet> expander) {
//        return getUniformStrategyFor(rootState, expander, rootState.getAllPlayers()[1], new HashMap<Sequence, Double>(), 1d);
//    }
//
//    private static Map<Sequence, Double> getUniformStrategyFor(GameState rootState, Expander<SequenceInformationSet> expander, Player player, Map<Sequence, Double> realPlan, double prob) {
//        realPlan.put(rootState.getSequenceFor(player), prob);
//        if (rootState.isGameEnd())
//            return realPlan;
//        List<Action> actions = expander.getActions(rootState);
//
//        for (Action action : actions) {
//            if (rootState.getPlayerToMove().equals(player))
//                getUniformStrategyFor(rootState.performAction(action), expander, player, realPlan, prob / actions.size());
//            else
//                getUniformStrategyFor(rootState.performAction(action), expander, player, realPlan, prob);
//        }
//        return realPlan;
//    }
//
//
//    private static void exportToCSV(Map<String, double[]> p1AbsResults, Map<String, double[]> p1RelResults, Map<String, double[]> p2AbsResults, Map<String, double[]> p2RelResults, Map<String, Strategy> p1StrategiesWithNoise, Map<String, Strategy> p2StrategiesWithNoise) {
//        try {
//            BufferedWriter writer = new BufferedWriter(new FileWriter(new File("result.csv")));
//
//            printTable(p1AbsResults, p2StrategiesWithNoise, writer);
//            printTable(p1RelResults, p2StrategiesWithNoise, writer);
//
//            printTable(p2AbsResults, p1StrategiesWithNoise, writer);
//            printTable(p2RelResults, p1StrategiesWithNoise, writer);
//
//            writer.flush();
//            writer.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private static void printTable(Map<String, double[]> p1AbsResults, Map<String, Strategy> p1StrategiesWithNoise, BufferedWriter writer) throws IOException {
//        for (String string : p1StrategiesWithNoise.keySet()) {
//            writer.write("," + string);
//        }
//        writer.write("\r\n");
//        for (Map.Entry<String, double[]> result : p1AbsResults.entrySet()) {
//            writer.write(result.getKey());
//            for (double reward : result.getValue()) {
//                writer.write("," + reward);
//            }
//            writer.write("\r\n");
//        }
//        writer.write("\r\n");
//    }
//
//    private static void evaluateAgainstMCTS(GameState rootState, GameInfo gameInfo, Expander<SequenceInformationSet> expander, SequenceFormConfig<SequenceInformationSet> algConfig, Expander<MCTSInformationSet> p1Expander, Expander<MCTSInformationSet> p2Expander,
//                                            Map<String, Strategy> p1NormalStrategies, Map<String, Strategy> p2NormalStrategies, MCTSConfig p1MCTSConfig, MCTSConfig p2MCTSConfig, String domainType) {
//        int COUNT = 100;
//        int IT_PER_CALL = 10000;
//        int it = 20;
//        SQFBestResponseAlgorithm p1BestResponse = new SQFBestResponseAlgorithm(expander, 0, new Player[]{gameInfo.getAllPlayers()[0], gameInfo.getAllPlayers()[1]}, algConfig, gameInfo);
//        SQFBestResponseAlgorithm p2BestResponse = new SQFBestResponseAlgorithm(expander, 1, new Player[]{gameInfo.getAllPlayers()[0], gameInfo.getAllPlayers()[1]}, algConfig, gameInfo);
//        UtilityCalculator calculator = new UtilityCalculator(rootState, expander);
//        double gameValue = calculator.computeUtility(p1NormalStrategies.get("p1Normal"), p2NormalStrategies.get("p2Normal"));
//
//        Map<String, double[]> p1absValueVectors = new LinkedHashMap<String, double[]>();
//        Map<String, double[]> p1relativeValuesVector = new LinkedHashMap<String, double[]>();
//        Map<String, double[]> p2absValueVectors = new LinkedHashMap<String, double[]>();
//        Map<String, double[]> p2relativeValuesVector = new LinkedHashMap<String, double[]>();
//        Map<String, String> colors = getColorMap();
//
//        for (int j = 0; j < it; j++) {
//            p1MCTSConfig = new MCTSConfig(new Simulator(j), new UCTBackPropFactory(Math.sqrt(2) * gameInfo.getMaxUtility()), new UniformStrategyForMissingSequences.Factory(), null);
//            p2MCTSConfig = new MCTSConfig(new Simulator(j), new UCTBackPropFactory(Math.sqrt(2) * gameInfo.getMaxUtility()), new UniformStrategyForMissingSequences.Factory(), null);
//            MCTSRunner p1MCTSRunner = new MCTSRunner(p1MCTSConfig, rootState, p1Expander);
//            MCTSRunner p2MCTSRunner = new MCTSRunner(p2MCTSConfig, rootState, p2Expander);
//
//            Strategy[] p1Strategies = new Strategy[COUNT];
//            Strategy[] p2Strategies = new Strategy[COUNT];
//            for (int i = 0; i < COUNT; i++) {
//                p1Strategies[i] = p1MCTSRunner.runMCTS(IT_PER_CALL, gameInfo.getAllPlayers()[0], new FrequenceDistribution());
//                p2Strategies[i] = p2MCTSRunner.runMCTS(IT_PER_CALL, gameInfo.getAllPlayers()[1], new FrequenceDistribution());
////            p1Strategies[i] = p1MCTSRunner.runMCTS(IT_PER_CALL, gameInfo.getAllPlayers()[0], new ValueDistribution());
////            p2Strategies[i] = p2MCTSRunner.runMCTS(IT_PER_CALL, gameInfo.getAllPlayers()[1], new ValueDistribution());
//            }
//            for (Map.Entry<String, Strategy> normalStrategyEntry : p2NormalStrategies.entrySet()) {
//
//                double[] absValues = p2absValueVectors.get(normalStrategyEntry.getKey());
//                double[] relativeValues = p2relativeValuesVector.get(normalStrategyEntry.getKey());
//
//                if (absValues == null) {
//                    absValues = new double[COUNT];
//                    relativeValues = new double[COUNT];
//                }
//
//                for (int i = 0; i < COUNT; i++) {
//                    Strategy p1Strategy = p1Strategies[i];
//                    Interval p2Interval = new Interval(-gameValue/* - epsilon*gameInfo.getMaxUtility()*/, p2BestResponse.calculateBR(rootState, p1Strategy));
//                    double strategyValue = -calculator.computeUtility(p1Strategy, normalStrategyEntry.getValue());
//
//                    absValues[i] += strategyValue;
//                    relativeValues[i] += p2Interval.getRelativePosition(strategyValue);
//                    System.out.println("mcts" + " vs " + normalStrategyEntry.getKey() + ": " + strategyValue + ", " + p2Interval.getRelativePosition(strategyValue) + " in " + p2Interval);
//                }
//                p2absValueVectors.put(normalStrategyEntry.getKey(), absValues);
//                p2relativeValuesVector.put(normalStrategyEntry.getKey(), relativeValues);
//            }
//
//            for (Map.Entry<String, Strategy> normalStrategyEntry : p1NormalStrategies.entrySet()) {
//                double[] absValues = p1absValueVectors.get(normalStrategyEntry.getKey());
//                double[] relativeValues = p1relativeValuesVector.get(normalStrategyEntry.getKey());
//
//                if (absValues == null) {
//                    absValues = new double[COUNT];
//                    relativeValues = new double[COUNT];
//                }
//
//                for (int i = 0; i < COUNT; i++) {
//                    Strategy p2Strategy = p2Strategies[i];
//                    Interval p1Interval = new Interval(gameValue /*- epsilon*gameInfo.getMaxUtility()*/, p1BestResponse.calculateBR(rootState, p2Strategy));
//                    double strategyValue = calculator.computeUtility(normalStrategyEntry.getValue(), p2Strategy);
//
//                    absValues[i] += strategyValue;
//                    relativeValues[i] += p1Interval.getRelativePosition(strategyValue);
//                    System.out.println(normalStrategyEntry.getKey() + " vs " + "mcts" + ": " + strategyValue + ", " + p1Interval.getRelativePosition(strategyValue) + " in " + p1Interval);
//                    normalStrategyEntry.getValue().sanityCheck(rootState, expander);
//                }
//                p1absValueVectors.put(normalStrategyEntry.getKey(), absValues);
//                p1relativeValuesVector.put(normalStrategyEntry.getKey(), relativeValues);
//            }
//        }
//        try {
//            toMatlab(COUNT, IT_PER_CALL, it, p1absValueVectors, p1relativeValuesVector, p2absValueVectors, p2relativeValuesVector, colors, new PrintStream(new FileOutputStream(new File(domainType + "MCTS.m"))));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
////    private static RefCompResult evaluateAgainstDepthMCTS(GameState rootState, GameInfo gameInfo, Expander<SequenceInformationSet> expander, SequenceFormConfig<SequenceInformationSet> algConfig, Expander<MCTSInformationSet> p1Expander, Expander<MCTSInformationSet> p2Expander,
////                                                          Map<String, Strategy> p1NormalStrategies, Map<String, Strategy> p2NormalStrategies, MCTSConfig p1MCTSConfig, MCTSConfig p2MCTSConfig, String domainType, int mctsCallCount, int mctsItPerCall, int mctsRunCount) {
////        int COUNT = mctsCallCount;
////        int IT_PER_CALL = mctsItPerCall;
////        int it = mctsRunCount;
////        SQFBestResponseAlgorithm p1BestResponse = new SQFBestResponseAlgorithm(expander, 0, new Player[]{gameInfo.getAllPlayers()[0], gameInfo.getAllPlayers()[1]}, algConfig, gameInfo);
////        SQFBestResponseAlgorithm p2BestResponse = new SQFBestResponseAlgorithm(expander, 1, new Player[]{gameInfo.getAllPlayers()[0], gameInfo.getAllPlayers()[1]}, algConfig, gameInfo);
////        UtilityCalculator calculator = new UtilityCalculator(rootState, expander);
////        double gameValue = calculator.computeUtility(p1NormalStrategies.get("p1Normal"), p2NormalStrategies.get("p2Normal"));
////
////        Map<String, double[]> p1absValueVectors = new LinkedHashMap<String, double[]>();
////        Map<String, double[]> p1relativeValuesVector = new LinkedHashMap<String, double[]>();
////        Map<String, double[]> p2absValueVectors = new LinkedHashMap<String, double[]>();
////        Map<String, double[]> p2relativeValuesVector = new LinkedHashMap<String, double[]>();
////        Map<String, String> colors = getColorMap();
////
////        testMCTS(rootState, gameInfo, p1Expander, p2Expander, IT_PER_CALL);
////
////        for (int j = 0; j < it; j++) {
////            Strategy[] p1Strategies = new Strategy[COUNT];
////            Strategy[] p2Strategies = new Strategy[COUNT];
////            double[] p1bestResponses = new double[COUNT];
////            double[] p2BestResponses = new double[COUNT];
////            double[] p1worstResponses = new double[COUNT];
////            double[] p2worstResponses = new double[COUNT];
////
////            for (int i = 0; i < COUNT; i++) {
//////                if(i % 10 == 0)
//////                    System.gc();
////                ChanceNode.chanceNodeSeed = j;
////                p1MCTSConfig = new MCTSConfig(new Simulator(j), new UCTBackPropFactory(Math.sqrt(2) * gameInfo.getMaxUtility()), new UniformStrategyForMissingSequences.Factory(), null);
////                p2MCTSConfig = new MCTSConfig(new Simulator(j), new UCTBackPropFactory(Math.sqrt(2) * gameInfo.getMaxUtility()), new UniformStrategyForMissingSequences.Factory(), null);
////                ISMCTSExploitability p1MCTSRunner = new ISMCTSExploitability(rootState, p1Expander, p1MCTSConfig, j);
////                ISMCTSExploitability p2MCTSRunner = new ISMCTSExploitability(rootState, p2Expander, p2MCTSConfig, j);
////                p1Strategies[i] = p1MCTSRunner.run(gameInfo.getAllPlayers()[0], (i + 1) * IT_PER_CALL);
////                p2Strategies[i] = p2MCTSRunner.run(gameInfo.getAllPlayers()[1], (i + 1) * IT_PER_CALL);
////                double[] tempBestResponses = calculateBestResponse(rootState, expander, gameInfo, algConfig, p1Strategies[i], p2Strategies[i]);
////                double[] tempWorstResponses = calculateWorstResponse(rootState, expander, gameInfo, algConfig, p1Strategies[i], p2Strategies[i]);
////                p1bestResponses[i] = tempBestResponses[0];
////                p2BestResponses[i] = tempBestResponses[1];
////                p1worstResponses[i] = tempWorstResponses[0];
////                p2worstResponses[i] = tempWorstResponses[1];
//////            p1Strategies[i] = p1MCTSRunner.runMCTS(IT_PER_CALL, gameInfo.getAllPlayers()[0], new ValueDistribution());
//////            p2Strategies[i] = p2MCTSRunner.runMCTS(IT_PER_CALL, gameInfo.getAllPlayers()[1], new ValueDistribution());
////            }
////
////            testMCTS(rootState, gameInfo, p1Expander, p2Expander, IT_PER_CALL);
//////            double[] absBR = p2absValueVectors.get("bestResponse");
////
//////            if (absBR == null)
//////                absBR = new double[COUNT];
////            Map<String, double[]> tempP1AbsValues = new LinkedHashMap<String, double[]>();
////            Map<String, double[]> tempP1RelValues = new LinkedHashMap<String, double[]>();
////            Map<String, double[]> tempP2AbsValues = new LinkedHashMap<String, double[]>();
////            Map<String, double[]> tempP2RelValues = new LinkedHashMap<String, double[]>();
////
////            for (Map.Entry<String, Strategy> normalStrategyEntry : p2NormalStrategies.entrySet()) {
////
////                double[] absValues = p2absValueVectors.get(normalStrategyEntry.getKey());
////                double[] relativeValues = p2relativeValuesVector.get(normalStrategyEntry.getKey());
////                double[] tempAbsValue = new double[COUNT];
////                double[] tempRelValue = new double[COUNT];
////
////                if (absValues == null) {
////                    absValues = new double[COUNT];
////                    relativeValues = new double[COUNT];
////                }
////
////                for (int i = 0; i < COUNT; i++) {
////                    Strategy p1Strategy = p1Strategies[i];
//////                    double brValue = p2BestResponse.calculateBR(rootState, p1Strategy);
////                    double brValue = -p2BestResponses[i];
////                    double wcValue = -p2worstResponses[i];
//////                    absBR[i] += brValue;
////                    Interval p2Interval = new Interval(wcValue, brValue);
////                    double strategyValue = -calculator.computeUtility(p1Strategy, normalStrategyEntry.getValue());
////
////                    absValues[i] += strategyValue;
////                    tempAbsValue[i] = strategyValue;
////                    relativeValues[i] += p2Interval.getRelativePosition(strategyValue);
////                    tempRelValue[i] = p2Interval.getRelativePosition(strategyValue);
////                    tady
//////                    System.out.println("mcts" + " vs " + normalStrategyEntry.getKey() + ": " + strategyValue + ", " + p2Interval.getRelativePosition(strategyValue) + " in " + p2Interval);
////                }
////                tempP2AbsValues.put(normalStrategyEntry.getKey(), tempAbsValue);
////                tempP2RelValues.put(normalStrategyEntry.getKey(), tempRelValue);
////                p2absValueVectors.put(normalStrategyEntry.getKey(), absValues);
////                p2relativeValuesVector.put(normalStrategyEntry.getKey(), relativeValues);
////            }
//////            p2absValueVectors.put("bestResponse", absBR);
//////            absBR = p1absValueVectors.get("bestResponse");
//////            if (absBR == null)
//////                absBR = new double[COUNT];
////
////            for (Map.Entry<String, Strategy> normalStrategyEntry : p1NormalStrategies.entrySet()) {
////                double[] absValues = p1absValueVectors.get(normalStrategyEntry.getKey());
////                double[] relativeValues = p1relativeValuesVector.get(normalStrategyEntry.getKey());
////                double[] tempAbsValue = new double[COUNT];
////                double[] tempRelValue = new double[COUNT];
////                if (absValues == null) {
////                    absValues = new double[COUNT];
////                    relativeValues = new double[COUNT];
////                }
////
////                for (int i = 0; i < COUNT; i++) {
////                    Strategy p2Strategy = p2Strategies[i];
////                    double brValue = p1bestResponses[i];
////                    double wcValue = p1worstResponses[i];
//////                    absBR[i] += brValue;
////                    Interval p1Interval = new Interval(wcValue /*- epsilon*gameInfo.getMaxUtility()*/, brValue);
////                    double strategyValue = calculator.computeUtility(normalStrategyEntry.getValue(), p2Strategy);
////
////                    absValues[i] += strategyValue;
////                    tempAbsValue[i] = strategyValue;
////                    relativeValues[i] += p1Interval.getRelativePosition(strategyValue);
////                    tempRelValue[i] = p1Interval.getRelativePosition(strategyValue);
////                    tady
//////                    System.out.println(normalStrategyEntry.getKey() + " vs " + "mcts" + ": "
////// + strategyValue + ", " + p1Interval.getRelativePosition(strategyValue) + " in " + p1Interval);
////                    normalStrategyEntry.getValue().sanityCheck(rootState, expander);
////                }
////                tempP1AbsValues.put(normalStrategyEntry.getKey(), tempAbsValue);
////                tempP1RelValues.put(normalStrategyEntry.getKey(), tempRelValue);
////                p1absValueVectors.put(normalStrategyEntry.getKey(), absValues);
////                p1relativeValuesVector.put(normalStrategyEntry.getKey(), relativeValues);
////            }
////            addBestResponses(rootState, p1bestResponses, p2BestResponses, p1absValueVectors, p2absValueVectors);
////            addBestResponses(rootState, p1bestResponses, p2BestResponses, tempP1AbsValues, tempP2AbsValues);
////            addWorstResponses(rootState, p1worstResponses, p2worstResponses, p1absValueVectors, p2absValueVectors);
////            addWorstResponses(rootState, p1worstResponses, p2worstResponses, tempP1AbsValues, tempP2AbsValues);
////
////            try {
////                toMatlab(COUNT, IT_PER_CALL, 1, tempP1AbsValues, tempP1RelValues, tempP2AbsValues, tempP2RelValues, colors, new PrintStream(new FileOutputStream(domainType + "depthMCTSseed" + j + ".m")));
////            } catch (IOException e) {
////                e.printStackTrace();
////            }
//////            p1absValueVectors.put("bestResponse", absBR);
////        }
//////        p1absValueVectors.put("bestResponse", divide(p1absValueVectors.get("bestResponse"), p1NormalStrategies.size()));
//////        p2absValueVectors.put("bestResponse", divide(p2absValueVectors.get("bestResponse"), p2NormalStrategies.size()));
////        try {
////            toMatlab(COUNT, IT_PER_CALL, it, p1absValueVectors, p1relativeValuesVector, p2absValueVectors, p2relativeValuesVector, colors, new PrintStream(new FileOutputStream(new File(domainType + "depthMCTS.m"))));
////        } catch (IOException e) {
////            e.printStackTrace();
////        }
////        return new RefCompResult(p1absValueVectors, p1relativeValuesVector, p2absValueVectors, p2relativeValuesVector);
////    }
//
//    private static RefCompResult evaluateAgainstDepthMCTS(GameState rootState, GameInfo gameInfo, Expander<SequenceInformationSet> expander, SequenceFormConfig<SequenceInformationSet> algConfig, Expander<MCTSInformationSet> p1Expander, Expander<MCTSInformationSet> p2Expander,
//                                                          Map<String, Strategy> p1NormalStrategies, Map<String, Strategy> p2NormalStrategies, MCTSConfig p1MCTSConfig, MCTSConfig p2MCTSConfig, String domainType, int mctsCallCount, int mctsItPerCall, int mctsRunCount) {
//        int COUNT = mctsCallCount;
//        int IT_PER_CALL = mctsItPerCall;
//        int it = mctsRunCount;
////        SQFBestResponseAlgorithm p1BestResponse = new SQFBestResponseAlgorithm(expander, 0, new Player[]{gameInfo.getAllPlayers()[0], gameInfo.getAllPlayers()[1]}, algConfig, gameInfo);
////        SQFBestResponseAlgorithm p2BestResponse = new SQFBestResponseAlgorithm(expander, 1, new Player[]{gameInfo.getAllPlayers()[0], gameInfo.getAllPlayers()[1]}, algConfig, gameInfo);
//        UtilityCalculator calculator = new UtilityCalculator(rootState, expander);
////        double gameValue = calculator.computeUtility(p1NormalStrategies.get("p1Normal"), p2NormalStrategies.get("p2Normal"));
//
//        Map<String, double[]> p1absValueVectors = new LinkedHashMap<String, double[]>();
//        Map<String, double[]> p1relativeValuesVector = new LinkedHashMap<String, double[]>();
//        Map<String, double[]> p2absValueVectors = new LinkedHashMap<String, double[]>();
//        Map<String, double[]> p2relativeValuesVector = new LinkedHashMap<String, double[]>();
//        Map<String, String> colors = getColorMap();
//
//        testMCTS(rootState, gameInfo, p1Expander, p2Expander, IT_PER_CALL);
//
//        for (int j = 0; j < it; j++) {
//            Strategy[] p1Strategies = new Strategy[COUNT];
//            Strategy[] p2Strategies = new Strategy[COUNT];
//            double[] p1bestResponses = new double[COUNT];
//            double[] p2BestResponses = new double[COUNT];
//            double[] p1worstResponses = new double[COUNT];
//            double[] p2worstResponses = new double[COUNT];
//
//            BufferedWriter writer = null;
//            try {
//                writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(domainType + "depthMCTSseed" + GSGameInfo.seed + "j" + j + ".csv"))));
//                writer.write("p1Worst,p1Best");
//                for (String strategyName : p1NormalStrategies.keySet()) {
//                    writer.write("," + strategyName + "," + strategyName + "Rel");
//                }
//                writer.write(",p2Worst,p2Best");
//                for (String strategyName : p2NormalStrategies.keySet()) {
//                    writer.write("," + strategyName + "," + strategyName + "Rel");
//                }
//                writer.newLine();
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//
//            for (int i = 0; i < COUNT; i++) {
////                if(i % 10 == 0)
////                    System.gc();
//                ChanceNode.chanceNodeSeed = j;
//                p1MCTSConfig = new MCTSConfig(new Simulator(j), new UCTBackPropFactory(Math.sqrt(2) * gameInfo.getMaxUtility()), new UniformStrategyForMissingSequences.Factory(), null);
//                p2MCTSConfig = new MCTSConfig(new Simulator(j), new UCTBackPropFactory(Math.sqrt(2) * gameInfo.getMaxUtility()), new UniformStrategyForMissingSequences.Factory(), null);
//                ISMCTSExploitability p1MCTSRunner = new ISMCTSExploitability(rootState, p1Expander, p1MCTSConfig, j);
//                ISMCTSExploitability p2MCTSRunner = new ISMCTSExploitability(rootState, p2Expander, p2MCTSConfig, j);
//                p1Strategies[i] = p1MCTSRunner.run(gameInfo.getAllPlayers()[0], (i + 1) * IT_PER_CALL);
//                p2Strategies[i] = p2MCTSRunner.run(gameInfo.getAllPlayers()[1], (i + 1) * IT_PER_CALL);
//                double[] tempBestResponses = calculateBestResponse(rootState, expander, gameInfo, algConfig, p1Strategies[i], p2Strategies[i]);
//                double[] tempWorstResponses = calculateWorstResponse(rootState, expander, gameInfo, algConfig, p1Strategies[i], p2Strategies[i]);
//                p1bestResponses[i] = tempBestResponses[0];
//                p2BestResponses[i] = tempBestResponses[1];
//                p1worstResponses[i] = tempWorstResponses[0];
//                p2worstResponses[i] = tempWorstResponses[1];
//
//                try {
//                    writer.write(p1worstResponses[i] + "," + p1bestResponses[i]);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//                for (Map.Entry<String, Strategy> normalStrategyEntry : p1NormalStrategies.entrySet()) {
////                    double[] absValues = p1absValueVectors.get(normalStrategyEntry.getKey());
////                    double[] relativeValues = p1relativeValuesVector.get(normalStrategyEntry.getKey());
////                    double[] tempAbsValue = new double[COUNT];
////                    double[] tempRelValue = new double[COUNT];
////                    if (absValues == null) {
////                        absValues = new double[COUNT];
////                        relativeValues = new double[COUNT];
////                    }
//
//                    Strategy p2Strategy = p2Strategies[i];
//                    double brValue = p1bestResponses[i];
//                    double wcValue = p1worstResponses[i];
////                    absBR[i] += brValue;
//                    Interval p1Interval = new Interval(wcValue /*- epsilon*gameInfo.getMaxUtility()*/, brValue);
//                    double strategyValue = calculator.computeUtility(normalStrategyEntry.getValue(), p2Strategy);
//
//
//                    try {
//                        writer.write("," + strategyValue + "," + p1Interval.getRelativePosition(strategyValue));
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//
////                    absValues[i] += strategyValue;
////                    tempAbsValue[i] = strategyValue;
////                    relativeValues[i] += p1Interval.getRelativePosition(strategyValue);
////                    tempRelValue[i] = p1Interval.getRelativePosition(strategyValue);
//                }
//
//                try {
//                    writer.write("," + -p2worstResponses[i] + "," + -p2BestResponses[i]);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                for (Map.Entry<String, Strategy> normalStrategyEntry : p2NormalStrategies.entrySet()) {
//
//                    double[] absValues = p2absValueVectors.get(normalStrategyEntry.getKey());
//                    double[] relativeValues = p2relativeValuesVector.get(normalStrategyEntry.getKey());
//                    double[] tempAbsValue = new double[COUNT];
//                    double[] tempRelValue = new double[COUNT];
//
//                    if (absValues == null) {
//                        absValues = new double[COUNT];
//                        relativeValues = new double[COUNT];
//                    }
//
//                    Strategy p1Strategy = p1Strategies[i];
////                    double brValue = p2BestResponse.calculateBR(rootState, p1Strategy);
//                    double brValue = -p2BestResponses[i];
//                    double wcValue = -p2worstResponses[i];
////                    absBR[i] += brValue;
//                    Interval p2Interval = new Interval(wcValue, brValue);
//                    double strategyValue = -calculator.computeUtility(p1Strategy, normalStrategyEntry.getValue());
//
//                    try {
//                        writer.write("," + strategyValue + "," + p2Interval.getRelativePosition(strategyValue));
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//
////                    absValues[i] += strategyValue;
////                    tempAbsValue[i] = strategyValue;
////                    relativeValues[i] += p2Interval.getRelativePosition(strategyValue);
////                    tempRelValue[i] = p2Interval.getRelativePosition(strategyValue);
//                }
//                try {
//                    writer.newLine();
//                    writer.flush();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
////            p1Strategies[i] = p1MCTSRunner.runMCTS(IT_PER_CALL, gameInfo.getAllPlayers()[0], new ValueDistribution());
////            p2Strategies[i] = p2MCTSRunner.runMCTS(IT_PER_CALL, gameInfo.getAllPlayers()[1], new ValueDistribution());
////            }
//
////            testMCTS(rootState, gameInfo, p1Expander, p2Expander, IT_PER_CALL);
////            double[] absBR = p2absValueVectors.get("bestResponse");
//
////            if (absBR == null)
////                absBR = new double[COUNT];
////            Map<String, double[]> tempP1AbsValues = new LinkedHashMap<String, double[]>();
////            Map<String, double[]> tempP1RelValues = new LinkedHashMap<String, double[]>();
////            Map<String, double[]> tempP2AbsValues = new LinkedHashMap<String, double[]>();
////            Map<String, double[]> tempP2RelValues = new LinkedHashMap<String, double[]>();
//
//
////            p2absValueVectors.put("bestResponse", absBR);
////            absBR = p1absValueVectors.get("bestResponse");
////            if (absBR == null)
////                absBR = new double[COUNT];
//
//
////                addBestResponses(rootState, p1bestResponses, p2BestResponses, p1absValueVectors, p2absValueVectors);
////                addBestResponses(rootState, p1bestResponses, p2BestResponses, tempP1AbsValues, tempP2AbsValues);
////                addWorstResponses(rootState, p1worstResponses, p2worstResponses, p1absValueVectors, p2absValueVectors);
////                addWorstResponses(rootState, p1worstResponses, p2worstResponses, tempP1AbsValues, tempP2AbsValues);
//
////                try {
////                    toMatlab(COUNT, IT_PER_CALL, 1, tempP1AbsValues, tempP1RelValues, tempP2AbsValues, tempP2RelValues, colors, new PrintStream(new FileOutputStream(domainType + "depthMCTSseed" + j + ".m")));
////                } catch (IOException e) {
////                    e.printStackTrace();
////                }
////            p1absValueVectors.put("bestResponse", absBR);
//            }
//            try {
//                writer.flush();
//                writer.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
////        p1absValueVectors.put("bestResponse", divide(p1absValueVectors.get("bestResponse"), p1NormalStrategies.size()));
////        p2absValueVectors.put("bestResponse", divide(p2absValueVectors.get("bestResponse"), p2NormalStrategies.size()));
////            try {
////                toMatlab(COUNT, IT_PER_CALL, it, p1absValueVectors, p1relativeValuesVector, p2absValueVectors, p2relativeValuesVector, colors, new PrintStream(new FileOutputStream(new File(domainType + "depthMCTS.m"))));
////            } catch (IOException e) {
////                e.printStackTrace();
////            }
//        return new RefCompResult(p1absValueVectors, p1relativeValuesVector, p2absValueVectors, p2relativeValuesVector);
//    }
//
//    private static double[] calculateBestResponse(GameState
//                                                          rootState, Expander<SequenceInformationSet> expander, GameInfo
//                                                          gameInfo, SequenceFormConfig<SequenceInformationSet> algConfig, Strategy p1Strategy, Strategy p2Strategy) {
////        FullSequenceEFG efg = new FullSequenceEFG(rootState, expander, gameInfo, algConfig);
//        if (undomSolver == null)
//            undomSolver = new BRFullUndominatedSolver(new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]}, p1Strategy, p2Strategy);
//        else
//            undomSolver.setObjectivesMax(p1Strategy, p2Strategy);
//
//        undomSolver.calculateBothPlStrategy(rootState, algConfig);
//        Map<Sequence, Double> p1RealPlan = undomSolver.p1RealizationPlan;
//        Map<Sequence, Double> p2RealPlan = undomSolver.p2RealizationPlan;
//        UtilityCalculator calculator = new UtilityCalculator(rootState, expander);
//
//        return new double[]{calculator.computeUtility(new NoMissingSeqStrategy(p1RealPlan), p2Strategy), calculator.computeUtility(p1Strategy, new NoMissingSeqStrategy(p2RealPlan))};
//    }
//
//    private static double[] calculateWorstResponse(GameState
//                                                           rootState, Expander<SequenceInformationSet> expander, GameInfo
//                                                           gameInfo, SequenceFormConfig<SequenceInformationSet> algConfig, Strategy p1Strategy, Strategy p2Strategy) {
//        if (undomSolver == null)
//            undomSolver = new BRFullUndominatedSolver(new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]}, p1Strategy, p2Strategy);
//
//        undomSolver.setObjectivesMin(p1Strategy, p2Strategy);
//
//        undomSolver.calculateBothPlStrategy(rootState, algConfig);
//        Map<Sequence, Double> p1RealPlan = undomSolver.p1RealizationPlan;
//        Map<Sequence, Double> p2RealPlan = undomSolver.p2RealizationPlan;
//        UtilityCalculator calculator = new UtilityCalculator(rootState, expander);
//
//        return new double[]{calculator.computeUtility(new NoMissingSeqStrategy(p1RealPlan), p2Strategy), calculator.computeUtility(p1Strategy, new NoMissingSeqStrategy(p2RealPlan))};
//    }
//
//    private static void testMCTS(GameState rootState, GameInfo gameInfo, Expander<MCTSInformationSet> p1Expander, Expander<MCTSInformationSet> p2Expander, int IT_PER_CALL) {
//        MCTSConfig p1MCTSConfig;
//        MCTSConfig p2MCTSConfig;
//        ChanceNode.chanceNodeSeed = 1;
//        p1MCTSConfig = new MCTSConfig(new Simulator(1), new UCTBackPropFactory(Math.sqrt(2) * gameInfo.getMaxUtility()), new UniformStrategyForMissingSequences.Factory(), null);
//        p2MCTSConfig = new MCTSConfig(new Simulator(1), new UCTBackPropFactory(Math.sqrt(2) * gameInfo.getMaxUtility()), new UniformStrategyForMissingSequences.Factory(), null);
//        ISMCTSExploitability p1MCTSRunner = new ISMCTSExploitability(rootState, p1Expander, p1MCTSConfig, 1);
//        ISMCTSExploitability p2MCTSRunner = new ISMCTSExploitability(rootState, p2Expander, p2MCTSConfig, 1);
//        Strategy p1Strategy1 = p1MCTSRunner.run(gameInfo.getAllPlayers()[0], IT_PER_CALL);
//        Strategy p2Strategy1 = p2MCTSRunner.run(gameInfo.getAllPlayers()[1], IT_PER_CALL);
//        ChanceNode.chanceNodeSeed = 1;
//        p1MCTSConfig = new MCTSConfig(new Simulator(1), new UCTBackPropFactory(Math.sqrt(2) * gameInfo.getMaxUtility()), new UniformStrategyForMissingSequences.Factory(), null);
//        p2MCTSConfig = new MCTSConfig(new Simulator(1), new UCTBackPropFactory(Math.sqrt(2) * gameInfo.getMaxUtility()), new UniformStrategyForMissingSequences.Factory(), null);
//        p1MCTSRunner = new ISMCTSExploitability(rootState, p1Expander, p1MCTSConfig, 1);
//        p2MCTSRunner = new ISMCTSExploitability(rootState, p2Expander, p2MCTSConfig, 1);
//        Strategy p1Strategy2 = p1MCTSRunner.run(gameInfo.getAllPlayers()[0], IT_PER_CALL);
//        Strategy p2Strategy2 = p2MCTSRunner.run(gameInfo.getAllPlayers()[1], IT_PER_CALL);
//
//        assert p1Strategy1.equals(p1Strategy2);
//        assert p2Strategy1.equals(p2Strategy2);
//
//        ChanceNode.chanceNodeSeed = 1;
//        p1MCTSConfig = new MCTSConfig(new Simulator(1), new UCTBackPropFactory(Math.sqrt(2) * gameInfo.getMaxUtility()), new UniformStrategyForMissingSequences.Factory(), null);
//        p2MCTSConfig = new MCTSConfig(new Simulator(1), new UCTBackPropFactory(Math.sqrt(2) * gameInfo.getMaxUtility()), new UniformStrategyForMissingSequences.Factory(), null);
//        p1MCTSRunner = new ISMCTSExploitability(rootState, p1Expander, p1MCTSConfig, 1);
//        p2MCTSRunner = new ISMCTSExploitability(rootState, p2Expander, p2MCTSConfig, 1);
//        p1Strategy1 = p1MCTSRunner.run(gameInfo.getAllPlayers()[0], 2 * IT_PER_CALL);
//        p2Strategy1 = p2MCTSRunner.run(gameInfo.getAllPlayers()[1], 2 * IT_PER_CALL);
//
//        ChanceNode.chanceNodeSeed = 1;
//        p1MCTSConfig = new MCTSConfig(new Simulator(1), new UCTBackPropFactory(Math.sqrt(2) * gameInfo.getMaxUtility()), new UniformStrategyForMissingSequences.Factory(), null);
//        p2MCTSConfig = new MCTSConfig(new Simulator(1), new UCTBackPropFactory(Math.sqrt(2) * gameInfo.getMaxUtility()), new UniformStrategyForMissingSequences.Factory(), null);
//        p1MCTSRunner = new ISMCTSExploitability(rootState, p1Expander, p1MCTSConfig, 1);
//        p2MCTSRunner = new ISMCTSExploitability(rootState, p2Expander, p2MCTSConfig, 1);
//        p1Strategy2 = p1MCTSRunner.run(gameInfo.getAllPlayers()[0], 2 * IT_PER_CALL);
//        p2Strategy2 = p2MCTSRunner.run(gameInfo.getAllPlayers()[1], 2 * IT_PER_CALL);
//
//        assert p1Strategy1.equals(p1Strategy2);
//        assert p2Strategy1.equals(p2Strategy2);
//    }
//
//    private static void addBestResponses(GameState rootState, double[] p1BestResponses, double[] p2BestResponses, Map<String, double[]> p1absValueVectors, Map<String, double[]> p2absValueVectors) {
//        double[] p1Br = p1absValueVectors.get("bestResponse");
//        double[] p2Br = p2absValueVectors.get("bestResponse");
//
//        if (p1Br == null)
//            p1Br = new double[p1BestResponses.length];
//        if (p2Br == null)
//            p2Br = new double[p2BestResponses.length];
//        for (int i = 0; i < p1Br.length; i++) {
//            p1Br[i] += p1BestResponses[i];
//            p2Br[i] += -p2BestResponses[i];
//        }
//        p1absValueVectors.put("bestResponse", p1Br);
//        p2absValueVectors.put("bestResponse", p2Br);
//    }
//
//    private static void addWorstResponses(GameState rootState, double[] p1WorstResponses, double[] p2WorstResponses, Map<String, double[]> p1absValueVectors, Map<String, double[]> p2absValueVectors) {
//        double[] p1Br = p1absValueVectors.get("worstResponse");
//        double[] p2Br = p2absValueVectors.get("worstResponse");
//
//        if (p1Br == null)
//            p1Br = new double[p1WorstResponses.length];
//        if (p2Br == null)
//            p2Br = new double[p2WorstResponses.length];
//        for (int i = 0; i < p1Br.length; i++) {
//            p1Br[i] += p1WorstResponses[i];
//            p2Br[i] += -p2WorstResponses[i];
//        }
//        p1absValueVectors.put("worstResponse", p1Br);
//        p2absValueVectors.put("worstResponse", p2Br);
//    }
//
//
//    private static double[] divide(double[] absBR, int denominator) {
//        for (int i = 0; i < denominator; i++) {
//            absBR[i] /= denominator;
//        }
//        return absBR;
//    }
//
////    private static RefCompResult evaluateAgainstCFR(GameState rootState, GameInfo gameInfo, Expander<SequenceInformationSet> expander, SequenceFormConfig<SequenceInformationSet> algConfig, Expander<VanillaInformationSet> cfrExpander,
////                                                    Map<String, Strategy> p1NormalStrategies, Map<String, Strategy> p2NormalStrategies, CFRConfig<VanillaInformationSet> config, String domainType, int cfrCallCount, int cfrItPerCall) {
////        int COUNT = cfrCallCount;
////        int IT_PER_CALL = cfrItPerCall;
////        int it = 1;
////        SQFBestResponseAlgorithm p1BestResponse = new SQFBestResponseAlgorithm(expander, 0, new Player[]{gameInfo.getAllPlayers()[0], gameInfo.getAllPlayers()[1]}, algConfig, gameInfo);
////        SQFBestResponseAlgorithm p2BestResponse = new SQFBestResponseAlgorithm(expander, 1, new Player[]{gameInfo.getAllPlayers()[0], gameInfo.getAllPlayers()[1]}, algConfig, gameInfo);
////        UtilityCalculator calculator = new UtilityCalculator(rootState, expander);
////        double gameValue = calculator.computeUtility(p1NormalStrategies.get("p1Normal"), p2NormalStrategies.get("p2Normal"));
////
////        Map<String, double[]> p1absValueVectors = new LinkedHashMap<String, double[]>();
////        Map<String, double[]> p1relativeValuesVector = new LinkedHashMap<String, double[]>();
////        Map<String, double[]> p2absValueVectors = new LinkedHashMap<String, double[]>();
////        Map<String, double[]> p2relativeValuesVector = new LinkedHashMap<String, double[]>();
////        Map<String, String> colors = getColorMap();
////
////        for (int j = 0; j < it; j++) {
////            config.clear();
////            VanillaCFR cfr = new VanillaCFR(config, cfrExpander);
////            Strategy[] p1Strategies = new Strategy[COUNT];
////            Strategy[] p2Strategies = new Strategy[COUNT];
////            double[] p1BestResponses = new double[COUNT];
////            double[] p2BestResponses = new double[COUNT];
////            double[] p1worstResponses = new double[COUNT];
////            double[] p2worstResponses = new double[COUNT];
////
////            cfr.buildGameTree();
////            for (int i = 0; i < COUNT; i++) {
////                cfr.updateTree(IT_PER_CALL);
////                p1Strategies[i] = new NoMissingSeqStrategy(cfr.getP1RealizationPlan());
////                p2Strategies[i] = new NoMissingSeqStrategy(cfr.getP2RealizationPlan());
////                double[] tempBestResponses = calculateBestResponse(rootState, expander, gameInfo, algConfig, p1Strategies[i], p2Strategies[i]);
////                double[] tempWorstResponses = calculateWorstResponse(rootState, expander, gameInfo, algConfig, p1Strategies[i], p2Strategies[i]);
////
////                p1BestResponses[i] = tempBestResponses[0];
////                p2BestResponses[i] = tempBestResponses[1];
////                p1worstResponses[i] = tempWorstResponses[0];
////                p2worstResponses[i] = tempWorstResponses[1];
////            }
////            for (Map.Entry<String, Strategy> normalStrategyEntry : p2NormalStrategies.entrySet()) {
////
////                double[] absValues = p1absValueVectors.get(normalStrategyEntry.getKey());
////                double[] relativeValues = p1relativeValuesVector.get(normalStrategyEntry.getKey());
////
////                if (absValues == null) {
////                    absValues = new double[COUNT];
////                    relativeValues = new double[COUNT];
////                }
////
////                for (int i = 0; i < COUNT; i++) {
////                    Strategy p1Strategy = p1Strategies[i];
////                    Interval p2Interval = new Interval(-p2worstResponses[i]/* - epsilon*gameInfo.getMaxUtility()*/, -p2BestResponses[i]);
////                    double strategyValue = -calculator.computeUtility(p1Strategy, normalStrategyEntry.getValue());
////
////                    absValues[i] += strategyValue;
////                    relativeValues[i] += p2Interval.getRelativePosition(strategyValue);
////                    System.out.println("cfr" + " vs " + normalStrategyEntry.getKey() + ": " + strategyValue + ", " + p2Interval.getRelativePosition(strategyValue) + " in " + p2Interval);
////                }
////
////                p2absValueVectors.put(normalStrategyEntry.getKey(), absValues);
////                p2relativeValuesVector.put(normalStrategyEntry.getKey(), relativeValues);
////            }
////
////            for (Map.Entry<String, Strategy> normalStrategyEntry : p1NormalStrategies.entrySet()) {
////                double[] absValues = p2absValueVectors.get(normalStrategyEntry.getKey());
////                double[] relativeValues = p2relativeValuesVector.get(normalStrategyEntry.getKey());
////
////                if (absValues == null) {
////                    absValues = new double[COUNT];
////                    relativeValues = new double[COUNT];
////                }
////
////                for (int i = 0; i < COUNT; i++) {
////                    Strategy p2Strategy = p2Strategies[i];
////                    Interval p1Interval = new Interval(p1worstResponses[i] /*- epsilon*gameInfo.getMaxUtility()*/, p1BestResponses[i]);
////                    double strategyValue = calculator.computeUtility(normalStrategyEntry.getValue(), p2Strategy);
////
////                    absValues[i] += strategyValue;
////                    relativeValues[i] += p1Interval.getRelativePosition(strategyValue);
////                    System.out.println(normalStrategyEntry.getKey() + " vs " + "cfr" + ": " + strategyValue + ", " + p1Interval.getRelativePosition(strategyValue) + " in " + p1Interval);
////                    normalStrategyEntry.getValue().sanityCheck(rootState, expander);
////                }
////                p1absValueVectors.put(normalStrategyEntry.getKey(), absValues);
////                p1relativeValuesVector.put(normalStrategyEntry.getKey(), relativeValues);
////            }
////            addBestResponses(rootState, p1BestResponses, p2BestResponses, p1absValueVectors, p2absValueVectors);
////            addWorstResponses(rootState, p1worstResponses, p2worstResponses, p1absValueVectors, p2absValueVectors);
////            tady zavolat to matlab
////        }
////        try {
////            toMatlab(COUNT, IT_PER_CALL, it, p1absValueVectors, p1relativeValuesVector, p2absValueVectors, p2relativeValuesVector, colors, new PrintStream(new FileOutputStream(new File(domainType + "CFR.m"))));
////        } catch (IOException e) {
////            e.printStackTrace();
////        }
////        return new RefCompResult(p1absValueVectors, p1relativeValuesVector, p2absValueVectors, p2relativeValuesVector);
////    }
//
//    private static RefCompResult evaluateAgainstCFR(GameState rootState, GameInfo gameInfo, Expander<SequenceInformationSet> expander, SequenceFormConfig<SequenceInformationSet> algConfig, Expander<VanillaInformationSet> cfrExpander,
//                                                    Map<String, Strategy> p1NormalStrategies, Map<String, Strategy> p2NormalStrategies, CFRConfig<VanillaInformationSet> config, String domainType, int cfrCallCount, int cfrItPerCall) {
//        int COUNT = cfrCallCount;
//        int IT_PER_CALL = cfrItPerCall;
//        SQFBestResponseAlgorithm p1BestResponse = new SQFBestResponseAlgorithm(expander, 0, new Player[]{gameInfo.getAllPlayers()[0], gameInfo.getAllPlayers()[1]}, algConfig, gameInfo);
//        SQFBestResponseAlgorithm p2BestResponse = new SQFBestResponseAlgorithm(expander, 1, new Player[]{gameInfo.getAllPlayers()[0], gameInfo.getAllPlayers()[1]}, algConfig, gameInfo);
//        UtilityCalculator calculator = new UtilityCalculator(rootState, expander);
//        double gameValue = calculator.computeUtility(p1NormalStrategies.get("p1Normal"), p2NormalStrategies.get("p2Normal"));
//
//        Map<String, double[]> p1absValueVectors = new LinkedHashMap<String, double[]>();
//        Map<String, double[]> p1relativeValuesVector = new LinkedHashMap<String, double[]>();
//        Map<String, double[]> p2absValueVectors = new LinkedHashMap<String, double[]>();
//        Map<String, double[]> p2relativeValuesVector = new LinkedHashMap<String, double[]>();
//        Map<String, String> colors = getColorMap();
//
//        config.clear();
//        VanillaCFR cfr = new VanillaCFR(config, cfrExpander);
//        Strategy[] p1Strategies = new Strategy[COUNT];
//        Strategy[] p2Strategies = new Strategy[COUNT];
//        double[] p1BestResponses = new double[COUNT];
//        double[] p2BestResponses = new double[COUNT];
//        double[] p1worstResponses = new double[COUNT];
//        double[] p2worstResponses = new double[COUNT];
//
//        cfr.buildGameTree();
//        BufferedWriter writer = null;
//        try {
//            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(domainType + "CFR.csv"))));
//            writer.write("p1Worst,p1Best");
//            for (String strategyName : p1NormalStrategies.keySet()) {
//                writer.write("," + strategyName + "," + strategyName + "Rel");
//            }
//            writer.write(",p2Worst,p2Best");
//            for (String strategyName : p2NormalStrategies.keySet()) {
//                writer.write("," + strategyName + "," + strategyName + "Rel");
//            }
//            writer.newLine();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        for (int i = 0; i < COUNT; i++) {
//            cfr.updateTree(IT_PER_CALL);
//            p1Strategies[i] = new NoMissingSeqStrategy(cfr.getP1RealizationPlan());
//            p2Strategies[i] = new NoMissingSeqStrategy(cfr.getP2RealizationPlan());
//            double[] tempBestResponses = calculateBestResponse(rootState, expander, gameInfo, algConfig, p1Strategies[i], p2Strategies[i]);
//            double[] tempWorstResponses = calculateWorstResponse(rootState, expander, gameInfo, algConfig, p1Strategies[i], p2Strategies[i]);
//
//            p1BestResponses[i] = tempBestResponses[0];
//            p2BestResponses[i] = tempBestResponses[1];
//            p1worstResponses[i] = tempWorstResponses[0];
//            p2worstResponses[i] = tempWorstResponses[1];
//
//            try {
//                writer.write(p1worstResponses[i] + "," + p1BestResponses[i]);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            for (Map.Entry<String, Strategy> normalStrategyEntry : p1NormalStrategies.entrySet()) {
//                Strategy p2Strategy = p2Strategies[i];
//                Interval p1Interval = new Interval(p1worstResponses[i] /*- epsilon*gameInfo.getMaxUtility()*/, p1BestResponses[i]);
//                double strategyValue = calculator.computeUtility(normalStrategyEntry.getValue(), p2Strategy);
//
//                try {
//                    writer.write("," + strategyValue + "," + p1Interval.getRelativePosition(strategyValue));
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                System.out.println(normalStrategyEntry.getKey() + " vs " + "cfr" + ": " + strategyValue + ", " + p1Interval.getRelativePosition(strategyValue) + " in " + p1Interval);
//            }
//            try {
//                writer.write("," + -p2worstResponses[i] + "," + -p2BestResponses[i]);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            for (Map.Entry<String, Strategy> normalStrategyEntry : p2NormalStrategies.entrySet()) {
//                Strategy p1Strategy = p1Strategies[i];
//                Interval p2Interval = new Interval(-p2worstResponses[i]/* - epsilon*gameInfo.getMaxUtility()*/, -p2BestResponses[i]);
//                double strategyValue = -calculator.computeUtility(p1Strategy, normalStrategyEntry.getValue());
//
//                try {
//                    writer.write("," + strategyValue + "," + p2Interval.getRelativePosition(strategyValue));
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                System.out.println("cfr" + " vs " + normalStrategyEntry.getKey() + ": " + strategyValue + ", " + p2Interval.getRelativePosition(strategyValue) + " in " + p2Interval);
//            }
//            try {
//                writer.newLine();
//                writer.flush();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//        try {
//            writer.flush();
//            writer.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        return new RefCompResult(p1absValueVectors, p1relativeValuesVector, p2absValueVectors, p2relativeValuesVector);
//    }
//
//    private static RefCompResult evaluateAgainstQuantalResponses(GameState rootState, GameInfo gameInfo, Expander<SequenceInformationSet> expander, SequenceFormConfig<SequenceInformationSet> algConfig,
//                                                                 Map<String, Strategy> p1NormalStrategies, Map<String, Strategy> p2NormalStrategies, String domainType) {
//        System.out.println("starting qre");
//        SQFBestResponseAlgorithm p1BestResponse = new SQFBestResponseAlgorithm(expander, 0, new Player[]{gameInfo.getAllPlayers()[0], gameInfo.getAllPlayers()[1]}, algConfig, gameInfo);
//        SQFBestResponseAlgorithm p2BestResponse = new SQFBestResponseAlgorithm(expander, 1, new Player[]{gameInfo.getAllPlayers()[0], gameInfo.getAllPlayers()[1]}, algConfig, gameInfo);
//        UtilityCalculator calculator = new UtilityCalculator(rootState, expander);
//        double gameValue = calculator.computeUtility(p1NormalStrategies.get("p1Normal"), p2NormalStrategies.get("p2Normal"));
//
//        Map<String, double[]> p1absValueVectors = new LinkedHashMap<String, double[]>();
//        Map<String, double[]> p1relativeValuesVector = new LinkedHashMap<String, double[]>();
//        Map<String, double[]> p2absValueVectors = new LinkedHashMap<String, double[]>();
//        Map<String, double[]> p2relativeValuesVector = new LinkedHashMap<String, double[]>();
//        Map<String, String> colors = getColorMap();
//
//        List<Map<Sequence, Double>> quantalResponses = CSVStrategyImport.readStrategyFromCSVForEFG(domainType + "QRE.csv", rootState, expander);
//
//        if (quantalResponses == null) {
//            System.err.println("QRE not found, invoking gambit solver " + "gambit-logit < " + domainType + "Gambit > " + domainType + "QRE.csv");
//            try {
//                exportToGambit(rootState, expander, domainType);
//                new ProcessBuilder().command("gambit-logit").redirectInput(new File(domainType + "Gambit")).redirectOutput(new File(domainType + "QRE.csv")).start().waitFor();
//                quantalResponses = CSVStrategyImport.readStrategyFromCSVForEFG(domainType + "QRE.csv", rootState, expander);
//            } catch (IOException e) {
//                e.printStackTrace();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//        double[] p1BestResponses = new double[quantalResponses.size()];
//        double[] p2BestResponses = new double[quantalResponses.size()];
//        double[] p1WorstResponses = new double[quantalResponses.size()];
//        double[] p2WorstResponses = new double[quantalResponses.size()];
//        int index = 0;
//
//        for (Map<Sequence, Double> quantalResponse : quantalResponses) {
//            double[] tempBestResponses = calculateBestResponse(rootState, expander, gameInfo, algConfig, new NoMissingSeqStrategy(getRealPlan(quantalResponse, rootState.getAllPlayers()[0])), new NoMissingSeqStrategy(getRealPlan(quantalResponse, rootState.getAllPlayers()[1])));
//            double[] tempWorstResponses = calculateWorstResponse(rootState, expander, gameInfo, algConfig, new NoMissingSeqStrategy(getRealPlan(quantalResponse, rootState.getAllPlayers()[0])), new NoMissingSeqStrategy(getRealPlan(quantalResponse, rootState.getAllPlayers()[1])));
//
//            p1BestResponses[index] = tempBestResponses[0];
//            p2BestResponses[index] = tempBestResponses[1];
//            p1WorstResponses[index] = tempWorstResponses[0];
//            p2WorstResponses[index] = tempWorstResponses[1];
//            index++;
//        }
//
//        for (Map.Entry<String, Strategy> normalStrategyEntry : p2NormalStrategies.entrySet()) {
//
//            double[] absValues = p1absValueVectors.get(normalStrategyEntry.getKey());
//            double[] relativeValues = p1relativeValuesVector.get(normalStrategyEntry.getKey());
//
//            if (absValues == null) {
//                absValues = new double[quantalResponses.size()];
//                relativeValues = new double[quantalResponses.size()];
//            }
//
//            for (int i = 0; i < quantalResponses.size(); i++) {
//                Strategy p1Strategy = new NoMissingSeqStrategy(getRealPlan(quantalResponses.get(i), rootState.getAllPlayers()[0]));
//
//                p1Strategy.sanityCheck(rootState, expander);
//                Interval p2Interval = new Interval(-p2WorstResponses[i]/* - epsilon*gameInfo.getMaxUtility()*/, -p2BestResponses[i]);
//                double strategyValue = -calculator.computeUtility(p1Strategy, normalStrategyEntry.getValue());
//
//                absValues[i] += strategyValue;
//                relativeValues[i] += p2Interval.getRelativePosition(strategyValue);
////                    System.out.println("cfr" + " vs " + normalStrategyEntry.getKey() + ": " + strategyValue + ", " + p2Interval.getRelativePosition(strategyValue) + " in " + p2Interval);
//            }
//            p2absValueVectors.put(normalStrategyEntry.getKey(), absValues);
//            p2relativeValuesVector.put(normalStrategyEntry.getKey(), relativeValues);
//        }
//
//        for (Map.Entry<String, Strategy> normalStrategyEntry : p1NormalStrategies.entrySet()) {
//            double[] absValues = p2absValueVectors.get(normalStrategyEntry.getKey());
//            double[] relativeValues = p2relativeValuesVector.get(normalStrategyEntry.getKey());
//
//            if (absValues == null) {
//                absValues = new double[quantalResponses.size()];
//                relativeValues = new double[quantalResponses.size()];
//            }
//
//            for (int i = 0; i < quantalResponses.size(); i++) {
//                Strategy p2Strategy = new NoMissingSeqStrategy(getRealPlan(quantalResponses.get(i), rootState.getAllPlayers()[1]));
//                Interval p1Interval = new Interval(p1WorstResponses[i] /*- epsilon*gameInfo.getMaxUtility()*/, p1BestResponses[i]);
//                double strategyValue = calculator.computeUtility(normalStrategyEntry.getValue(), p2Strategy);
//
//                absValues[i] += strategyValue;
//                relativeValues[i] += p1Interval.getRelativePosition(strategyValue);
////                    System.out.println(normalStrategyEntry.getKey() + " vs " + "cfr" + ": " + strategyValue + ", " + p1Interval.getRelativePosition(strategyValue) + " in " + p1Interval);
//                normalStrategyEntry.getValue().sanityCheck(rootState, expander);
//            }
//            p1absValueVectors.put(normalStrategyEntry.getKey(), absValues);
//            p1relativeValuesVector.put(normalStrategyEntry.getKey(), relativeValues);
//        }
//        addBestResponses(rootState, p1BestResponses, p2BestResponses, p1absValueVectors, p2absValueVectors);
//        addWorstResponses(rootState, p1WorstResponses, p2WorstResponses, p1absValueVectors, p2absValueVectors);
//        try {
//            toMatlab(quantalResponses.size(), 1, 1, p1absValueVectors, p1relativeValuesVector, p2absValueVectors, p2relativeValuesVector, colors, new PrintStream(new FileOutputStream(new File(domainType + "QRE.m"))));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return new RefCompResult(p1absValueVectors, p1relativeValuesVector, p2absValueVectors, p2relativeValuesVector);
//    }
//
//    private static Map<Sequence, Double> getRealPlan(Map<Sequence, Double> quantalResponse, Player player) {
//        Map<Sequence, Double> realPlan = new HashMap<Sequence, Double>();
//
//        realPlan.put(new ArrayListSequenceImpl(player), 1d);
//        for (Map.Entry<Sequence, Double> entry : quantalResponse.entrySet()) {
//            if (entry.getKey().getPlayer().equals(player))
//                realPlan.put(entry.getKey(), entry.getValue());
//        }
//        return realPlan;
//    }
//
////    private static void toMatlab(int COUNT, int IT_PER_CALL, int it, Map<String, double[]> p1absValueVectors, Map<String, double[]> p1relativeValuesVector, Map<String, double[]> p2absValueVectors, Map<String, double[]> p2relativeValuesVector, Map<String, String> colors) {
////        System.out.println("close all");
////        System.out.println("figure");
////        System.out.println("hold on");
////        for (Map.Entry<String, double[]> entry : p1absValueVectors.entrySet()) {
////            System.out.print("plot(");
////            System.out.print(Arrays.toString(getXCoor(COUNT, IT_PER_CALL)) + ", ");
////            System.out.print(Arrays.toString(weight(entry.getValue(), it)) + ", ");
////            System.out.print(colors.get(entry.getKey()));
////            System.out.print(")\r\n");
////        }
////        addLegend(p1absValueVectors.keySet());
////        System.out.println("title('Absolute values for p1')");
////        System.out.println("hold off");
////        System.out.println("figure");
////        System.out.println("hold on");
////        for (Map.Entry<String, double[]> entry : p1relativeValuesVector.entrySet()) {
////            System.out.print("plot(");
////            System.out.print(Arrays.toString(getXCoor(COUNT, IT_PER_CALL)) + ", ");
////            System.out.print(Arrays.toString(weight(entry.getValue(), it)) + ", ");
////            System.out.print(colors.get(entry.getKey()));
////            System.out.print(")\r\n");
////        }
////        addLegend(p1relativeValuesVector.keySet());
////        System.out.println("title('Relative values for p1')");
////        System.out.println("hold off");
////        System.out.println("figure");
////        System.out.println("hold on");
////        for (Map.Entry<String, double[]> entry : p2absValueVectors.entrySet()) {
////            System.out.print("plot(");
////            System.out.print(Arrays.toString(getXCoor(COUNT, IT_PER_CALL)) + ", ");
////            System.out.print(Arrays.toString(weight(entry.getValue(), it)) + ", ");
////            System.out.print(colors.get(entry.getKey()));
////            System.out.print(")\r\n");
////        }
////        addLegend(p2absValueVectors.keySet());
////        System.out.println("title('Absolute values for p2')");
////        System.out.println("hold off");
////        System.out.println("figure");
////        System.out.println("hold on");
////        for (Map.Entry<String, double[]> entry : p2relativeValuesVector.entrySet()) {
////            System.out.print("plot(");
////            System.out.print(Arrays.toString(getXCoor(COUNT, IT_PER_CALL)) + ", ");
////            System.out.print(Arrays.toString(weight(entry.getValue(), it)) + ", ");
////            System.out.print(colors.get(entry.getKey()));
////            System.out.print(")\r\n");
////        }
////        addLegend(p2relativeValuesVector.keySet());
////        System.out.println("title('Relative values for p2')");
////        System.out.println("hold off");
////    }
//
//    private static void toMatlab(int COUNT, int IT_PER_CALL, int it, Map<String, double[]> p1absValueVectors, Map<String, double[]> p1relativeValuesVector, Map<String, double[]> p2absValueVectors, Map<String, double[]> p2relativeValuesVector, Map<String, String> colors, PrintStream printStream) throws IOException {
//        printStream.println("close all" + "\r\n");
//        printStream.println("figure" + "\r\n");
//        printStream.println("hold on" + "\r\n");
//        for (Map.Entry<String, double[]> entry : p1absValueVectors.entrySet()) {
//            printStream.print("plot(");
//            printStream.print(Arrays.toString(getXCoor(COUNT, IT_PER_CALL)) + ", ");
//            printStream.print(Arrays.toString(weight(entry.getValue(), it)) + ", ");
//            printStream.print(colors.get(entry.getKey()));
//            printStream.print(")\r\n");
//        }
//        addLegend(p1absValueVectors.keySet(), printStream);
//        printStream.println("title('Absolute values for p1')" + "\r\n");
//        printStream.println("hold off" + "\r\n");
//        printStream.println("figure" + "\r\n");
//        printStream.println("hold on" + "\r\n");
//        for (Map.Entry<String, double[]> entry : p1relativeValuesVector.entrySet()) {
//            printStream.print("plot(");
//            printStream.print(Arrays.toString(getXCoor(COUNT, IT_PER_CALL)) + ", ");
//            printStream.print(Arrays.toString(weight(entry.getValue(), it)) + ", ");
//            printStream.print(colors.get(entry.getKey()));
//            printStream.print(")\r\n");
//        }
//        addLegend(p1relativeValuesVector.keySet(), printStream);
//        printStream.println("title('Relative values for p1')" + "\r\n");
//        printStream.println("hold off" + "\r\n");
//        printStream.println("figure" + "\r\n");
//        printStream.println("hold on" + "\r\n");
//        for (Map.Entry<String, double[]> entry : p2absValueVectors.entrySet()) {
//            printStream.print("plot(");
//            printStream.print(Arrays.toString(getXCoor(COUNT, IT_PER_CALL)) + ", ");
//            printStream.print(Arrays.toString(weight(entry.getValue(), it)) + ", ");
//            printStream.print(colors.get(entry.getKey()));
//            printStream.print(")\r\n");
//        }
//        addLegend(p2absValueVectors.keySet(), printStream);
//        printStream.println("title('Absolute values for p2')" + "\r\n");
//        printStream.println("hold off" + "\r\n");
//        printStream.println("figure" + "\r\n");
//        printStream.println("hold on" + "\r\n");
//        for (Map.Entry<String, double[]> entry : p2relativeValuesVector.entrySet()) {
//            printStream.print("plot(");
//            printStream.print(Arrays.toString(getXCoor(COUNT, IT_PER_CALL)) + ", ");
//            printStream.print(Arrays.toString(weight(entry.getValue(), it)) + ", ");
//            printStream.print(colors.get(entry.getKey()));
//            printStream.print(")\r\n");
//        }
//        addLegend(p2relativeValuesVector.keySet(), printStream);
//        printStream.println("title('Relative values for p2')");
//        printStream.println("hold off");
//    }
//
//    private static void addLegend(Iterable<String> names, PrintStream printStream) {
//        printStream.print("legend(");
//        Iterator<String> iterator = names.iterator();
//
//        while (iterator.hasNext()) {
//            printStream.print("'" + iterator.next() + "'");
//            if (iterator.hasNext())
//                printStream.print(", ");
//        }
//        printStream.print(")\r\n");
//    }
//
//    private static Map<String, String> getColorMap() {
//        Map<String, String> colors = new HashMap<String, String>();
//
//        colors.put("p1Normal", "'G'");
//        colors.put("p2Normal", "'G'");
//        colors.put("p1NFP", "'B'");
//        colors.put("p2NFP", "'B'");
//        colors.put("p1Undom", "'R'");
//        colors.put("p2Undom", "'R'");
//        colors.put("p1UndomvsNormal", "'Y'");
//        colors.put("p2UndomvsNormal", "'Y'");
//        colors.put("p1UndomvsNFP", "'K'");
//        colors.put("p2UndomvsNFP", "'K'");
//        colors.put("p1UndomvsUndom", "'C'");
//        colors.put("p2UndomvsUndom", "'C'");
//        colors.put("p1QuasiPerfect", "'M'");
//        colors.put("p2QuasiPerfect", "'M'");
//        colors.put("bestResponse", "'K'");
//        colors.put("worstResponse", "'K'");
//        return colors;
//    }
//
//    private static double[] weight(double[] array, int it) {
//        double[] weightedArray = new double[array.length];
//
//        for (int i = 0; i < array.length; i++) {
//            weightedArray[i] = array[i] / it;
//        }
//        return weightedArray;
//    }
//
//    private static int[] getXCoor(int count, int it_per_call) {
//        int[] xCoor = new int[count];
//
//        for (int i = 0; i < count; i++) {
//            xCoor[i] = (i + 1) * it_per_call;
//        }
//        return xCoor;
//    }
//
//    public static RefCompResult[] runExperiment(GameState rootState, GameInfo gameInfo, SequenceFormConfig<SequenceInformationSet> algConfig, Expander<SequenceInformationSet> expander, MCTSConfig p1MCTSConfig,
//                                                MCTSConfig p2MCTSConfig, Expander<MCTSInformationSet> mctsExpader1, Expander<MCTSInformationSet> mctsExpander2, Expander<VanillaInformationSet> cfrExpander, CFRConfig<VanillaInformationSet> cfrConfig, String domainType
//            , int mctsCallCount, int mctsItPerCall, int mctsRunCount, int cfrCallCount, int cfrItPerCall, Expander<SequenceInformationSet> quasiExpander) {
//        double epsilon = 0.05;
//        FullSequenceEFG efg = new FullSequenceEFG(rootState, expander, gameInfo, algConfig);
//        Player[] actingPlayers = new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]};
//        Map<Player, Map<Sequence, Double>> rps = efg.generate(new SequenceFormLP(actingPlayers));
//        Map<Player, Map<Sequence, Double>> nfpRps = null;//efg.generate(new FullNFPSolver(actingPlayers, gameInfo));
//        Map<Player, Map<Sequence, Double>> undominatedRps = efg.generate(new FullUndominatedSolver(actingPlayers));
//        FileManager<Map<Sequence, Double>> fileManager = new FileManager<Map<Sequence, Double>>();
//
//        Map<Sequence, Double> p1QuasiPerfect = null;
//        Map<Sequence, Double> p2QuasiPerfect = null;
//
////        try {
////            p1QuasiPerfect = fileManager.loadObject(domainType + "quasiPerfectP1RealPlan");
////            p2QuasiPerfect = fileManager.loadObject(domainType + "quasiPerfectP2RealPlan");
////        } catch (IOException e) {
////            System.err.println("Quasi-perfect strategy not found, starting qp solver...");
////            QuasiPerfectBuilder builder = new QuasiPerfectBuilder(quasiExpander, rootState, gameInfo);
////
////            builder.buildLP();
////            builder.solve(domainType);
////            try {
////                p1QuasiPerfect = fileManager.loadObject(domainType + "quasiPerfectP1RealPlan");
////                p2QuasiPerfect = fileManager.loadObject(domainType + "quasiPerfectP2RealPlan");
////            } catch (IOException e1) {
////                e1.printStackTrace();
////            }
////        }
//
////        Map<String, Strategy> p1NormalStrategies = getStrategyMap(rps.get(gameInfo.getAllPlayers()[0]), nfpRps.get(gameInfo.getAllPlayers()[0]), p1QuasiPerfect, undominatedRps.get(gameInfo.getAllPlayers()[0]),
////                "p1Normal", "p1NFP", "p1QuasiPerfect", "p1Undom");
////        Map<String, Strategy> p2NormalStrategies = getStrategyMap(rps.get(gameInfo.getAllPlayers()[1]), nfpRps.get(gameInfo.getAllPlayers()[1]), p2QuasiPerfect, undominatedRps.get(gameInfo.getAllPlayers()[1]),
////                "p2Normal", "p2NFP", "p2QuasiPerfect", "p2Undom");
//
//        Map<String, Strategy> p1NormalStrategies = getStrategyMap(rps.get(gameInfo.getAllPlayers()[0]), undominatedRps.get(gameInfo.getAllPlayers()[0]),
//                "p1Normal", "p1Undom");
//        Map<String, Strategy> p2NormalStrategies = getStrategyMap(rps.get(gameInfo.getAllPlayers()[1]), undominatedRps.get(gameInfo.getAllPlayers()[1]),
//                "p2Normal", "p2Undom");
//
////        Map<String, Strategy> p1StrategiesWithNoise = createP1StrategiesWithNoise(rootState, gameInfo, expander, epsilon, rps, nfpRps, undominatedRps);
////        Map<String, Strategy> p2StrategiesWithNoise = createP2StrategiesWithNoise(rootState, gameInfo, expander, epsilon, rps, nfpRps, undominatedRps);
////
////        Map<Player, Map<Sequence, Double>> undominatedRpsvsNormal = efg.generate(new FullUndominatedSolver(actingPlayers, p1StrategiesWithNoise.get("p1NormalWithNoise"), p2StrategiesWithNoise.get("p2NormalWithNoise")));
////        Map<Player, Map<Sequence, Double>> undominatedRpsvsUndominated = efg.generate(new FullUndominatedSolver(actingPlayers, p1StrategiesWithNoise.get("p1UndominatedWithNoise"), p2StrategiesWithNoise.get("p2UndominatedWithNoise")));
////        Map<Player, Map<Sequence, Double>> undominatedRpsvsNFP = efg.generate(new FullUndominatedSolver(actingPlayers, p1StrategiesWithNoise.get("p1NFPWithNoise"), p2StrategiesWithNoise.get("p2NFPWithNoise")));
////
////
////        p1NormalStrategies.put("p1UndomvsNormal", new NoMissingSeqStrategy(undominatedRpsvsNormal.get(actingPlayers[0])));
////        p1NormalStrategies.put("p1UndomvsNFP", new NoMissingSeqStrategy(undominatedRpsvsNFP.get(actingPlayers[0])));
////        p1NormalStrategies.put("p1UndomvsUndom", new NoMissingSeqStrategy(undominatedRpsvsUndominated.get(actingPlayers[0])));
////        p2NormalStrategies.put("p2UndomvsNormal", new NoMissingSeqStrategy(undominatedRpsvsNormal.get(actingPlayers[1])));
////        p2NormalStrategies.put("p2UndomvsNFP", new NoMissingSeqStrategy(undominatedRpsvsNFP.get(actingPlayers[1])));
////        p2NormalStrategies.put("p2UndomvsUndom", new NoMissingSeqStrategy(undominatedRpsvsUndominated.get(actingPlayers[1])));
//
//        RefCompResult[] results = new RefCompResult[3];
////        exportToGambit(rootState, expander, domainType);
////        evaluateStrategiesToConsole(rootState, gameInfo, expander, p1NormalStrategies, p2NormalStrategies, p1StrategiesWithNoise, p2StrategiesWithNoise, algConfig, domainType);
////        evaluateAgainstMCTS(rootState, gameInfo, expander, algConfig, mctsExpader1, mctsExpander2, p1NormalStrategies, p2NormalStrategies, p1MCTSConfig, p2MCTSConfig, domainType);
////        results[0] = evaluateAgainstCFR(rootState, gameInfo, expander, algConfig, cfrExpander, p1NormalStrategies, p2NormalStrategies, cfrConfig, domainType, cfrCallCount, cfrItPerCall);
////        System.out.println("CFR done");
////        results[2] = evaluateAgainstDepthMCTS(rootState, gameInfo, expander, algConfig, mctsExpader1, mctsExpander2, p1NormalStrategies, p2NormalStrategies,
////                p1MCTSConfig, p2MCTSConfig, domainType, mctsCallCount, mctsItPerCall, mctsRunCount);
////        System.out.println("MCTS done");
//        results[1] = evaluateAgainstQuantalResponses(rootState, gameInfo, expander, algConfig, p1NormalStrategies, p2NormalStrategies, domainType);
//        return results;
//    }
//
//    private static void exportToGambit(GameState rootState, Expander<SequenceInformationSet> expander, String domainType) {
//        GambitEFG gambit = new GambitEFG();
//
//        gambit.write(domainType + "Gambit", rootState, expander);
//    }
//
//    private static LinkedHashMap<String, Strategy> createP2StrategiesWithNoise(GameState rootState, GameInfo gameInfo, Expander<SequenceInformationSet> expander, double epsilon, Map<Player, Map<Sequence, Double>> rps, Map<Player, Map<Sequence, Double>> nfpRps, Map<Player, Map<Sequence, Double>> undominatedRps) {
//        LinkedHashMap<String, Strategy> p2StrategiesWithNoise = new LinkedHashMap<String, Strategy>();
//
//        p2StrategiesWithNoise.put("p2NormalWithNoise", NoiseMaker.addStaticNoise(gameInfo.getAllPlayers()[1], rootState, expander, new NoMissingSeqStrategy(rps.get(gameInfo.getAllPlayers()[1])), epsilon));
//        p2StrategiesWithNoise.put("p2NFPWithNoise", NoiseMaker.addStaticNoise(gameInfo.getAllPlayers()[1], rootState, expander, new NoMissingSeqStrategy(nfpRps.get(gameInfo.getAllPlayers()[1])), epsilon));
//        p2StrategiesWithNoise.put("p2UndominatedWithNoise", NoiseMaker.addStaticNoise(gameInfo.getAllPlayers()[1], rootState, expander, new NoMissingSeqStrategy(undominatedRps.get(gameInfo.getAllPlayers()[1])), epsilon));
//        p2StrategiesWithNoise.put("p2Uniform", new NoMissingSeqStrategy(getUniformStrategyForP2(rootState, expander)));
//        return p2StrategiesWithNoise;
//    }
//
//    private static LinkedHashMap<String, Strategy> createP1StrategiesWithNoise(GameState rootState, GameInfo gameInfo, Expander<SequenceInformationSet> expander, double epsilon, Map<Player, Map<Sequence, Double>> rps, Map<Player, Map<Sequence, Double>> nfpRps, Map<Player, Map<Sequence, Double>> undominatedRps) {
//        LinkedHashMap<String, Strategy> p1StrategiesWithNoise = new LinkedHashMap<String, Strategy>();
//
//        p1StrategiesWithNoise.put("p1NormalWithNoise", NoiseMaker.addStaticNoise(gameInfo.getAllPlayers()[0], rootState, expander, new NoMissingSeqStrategy(rps.get(gameInfo.getAllPlayers()[0])), epsilon));
//        p1StrategiesWithNoise.put("p1NFPWithNoise", NoiseMaker.addStaticNoise(gameInfo.getAllPlayers()[0], rootState, expander, new NoMissingSeqStrategy(nfpRps.get(gameInfo.getAllPlayers()[0])), epsilon));
//        p1StrategiesWithNoise.put("p1UndominatedWithNoise", NoiseMaker.addStaticNoise(gameInfo.getAllPlayers()[0], rootState, expander, new NoMissingSeqStrategy(undominatedRps.get(gameInfo.getAllPlayers()[0])), epsilon));
//        p1StrategiesWithNoise.put("p1Uniform", new NoMissingSeqStrategy(getUniformStrategyForP1(rootState, expander)));
//        return p1StrategiesWithNoise;
//    }
//
//    private static Map<String, Strategy> getStrategyMap(Map<Sequence, Double> map1, Map<Sequence, Double> map2, Map<Sequence, Double> map3, Map<Sequence, Double> map4, String name1, String name2, String name3, String name4) {
//        Map<String, Strategy> strategyMap = new LinkedHashMap<String, Strategy>();
//
//        strategyMap.put(name1, new NoMissingSeqStrategy(map1));
//        strategyMap.put(name2, new NoMissingSeqStrategy(map2));
//        strategyMap.put(name3, new NoMissingSeqStrategy(map3));
//        strategyMap.put(name4, new NoMissingSeqStrategy(map4));
//        return strategyMap;
//    }
//
//    private static Map<String, Strategy> getStrategyMap(Map<Sequence, Double> map1, Map<Sequence, Double> map2, Map<Sequence, Double> map3, String name1, String name2, String name3) {
//        Map<String, Strategy> strategyMap = new LinkedHashMap<String, Strategy>();
//
//        strategyMap.put(name1, new NoMissingSeqStrategy(map1));
//        strategyMap.put(name2, new NoMissingSeqStrategy(map2));
//        strategyMap.put(name3, new NoMissingSeqStrategy(map3));
//        return strategyMap;
//    }
//
//    private static Map<String, Strategy> getStrategyMap(Map<Sequence, Double> map1, Map<Sequence, Double> map2, String name1, String name2) {
//        Map<String, Strategy> strategyMap = new LinkedHashMap<String, Strategy>();
//
//        strategyMap.put(name1, new NoMissingSeqStrategy(map1));
//        strategyMap.put(name2, new NoMissingSeqStrategy(map2));
//        return strategyMap;
//    }
//
//
}
