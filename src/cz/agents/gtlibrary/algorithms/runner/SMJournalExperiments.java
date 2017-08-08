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


package cz.agents.gtlibrary.algorithms.runner;

import cz.agents.gtlibrary.algorithms.mcts.oos.SMOOSAlgorithm;
import cz.agents.gtlibrary.algorithms.mcts.SMMCTSAlgorithm;
import cz.agents.gtlibrary.algorithms.mcts.oos.OOSSimulator;
import cz.agents.gtlibrary.algorithms.cfr.CFRAlgorithm;
import cz.agents.gtlibrary.algorithms.mcts.*;
import cz.agents.gtlibrary.algorithms.mcts.distribution.Distribution;
import cz.agents.gtlibrary.algorithms.mcts.distribution.MeanStratDist;
import cz.agents.gtlibrary.algorithms.mcts.distribution.StrategyCollector;
import cz.agents.gtlibrary.algorithms.mcts.nodes.ChanceNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.Node;
import cz.agents.gtlibrary.algorithms.mcts.oos.OOSAlgorithmData;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.BackPropFactory;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.Exp3BackPropFactory;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.UCTBackPropFactory;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.UCTSelector;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.sm.SMRMBackPropFactory;
import cz.agents.gtlibrary.algorithms.sequenceform.SQFBestResponseAlgorithm;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.domain.exploitabilityGame.ExploitExpander;
import cz.agents.gtlibrary.domain.exploitabilityGame.ExploitGameInfo;
import cz.agents.gtlibrary.domain.exploitabilityGame.ExploitGameState;
import cz.agents.gtlibrary.domain.goofspiel.GSGameInfo;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielExpander;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielGameState;
import cz.agents.gtlibrary.domain.oshizumo.OZGameInfo;
import cz.agents.gtlibrary.domain.oshizumo.OshiZumoExpander;
import cz.agents.gtlibrary.domain.oshizumo.OshiZumoGameState;
import cz.agents.gtlibrary.domain.pursuit.PursuitExpander;
import cz.agents.gtlibrary.domain.pursuit.PursuitGameInfo;
import cz.agents.gtlibrary.domain.pursuit.PursuitGameState;
import cz.agents.gtlibrary.domain.randomgame.RandomGameExpander;
import cz.agents.gtlibrary.domain.randomgame.RandomGameInfo;
import cz.agents.gtlibrary.domain.randomgame.SimRandomGameState;
import cz.agents.gtlibrary.domain.rps.RPSExpander;
import cz.agents.gtlibrary.domain.rps.RPSGameInfo;
import cz.agents.gtlibrary.domain.rps.RPSGameState;
import cz.agents.gtlibrary.domain.tron.TronExpander;
import cz.agents.gtlibrary.domain.tron.TronGameInfo;
import cz.agents.gtlibrary.domain.tron.TronGameState;
import cz.agents.gtlibrary.iinodes.ConfigImpl;
import cz.agents.gtlibrary.iinodes.SimultaneousGameState;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.nfg.simalphabeta.SimABConfig;
import cz.agents.gtlibrary.nfg.simalphabeta.SimABInformationSet;
import cz.agents.gtlibrary.nfg.simalphabeta.SimAlphaBeta;
import cz.agents.gtlibrary.nfg.simalphabeta.oracle.SimOracleImpl;
import cz.agents.gtlibrary.strategy.Strategy;
import cz.agents.gtlibrary.utils.HighQualityRandom;
import cz.agents.gtlibrary.utils.Pair;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: bosansky
 * Date: 3/8/13
 * Time: 10:09 AM
 * To change this template use File | Settings | File Templates.
 */
public class SMJournalExperiments {

    static GameInfo gameInfo;
    static SimultaneousGameState rootState;
    static SQFBestResponseAlgorithm brAlg0;
    static SQFBestResponseAlgorithm brAlg1;
    static Expander expander;

    static long samplingTimeLimit = 1800000; // default: 30min

    static enum MCTStype {UCT, EXP3, RM}

    ;

    public static void main(String[] args) {
        System.setProperty("EXPLOIT", "FALSE");
        if (args.length < 2) {
            System.err.println("Missing Arguments: SMJournalExperiments {BI|BIAB|DO|DOAB|DOSAB|CFR|OOS|MCTS} {GS|OZ|PE|RG|RPS|Tron} [domain parameters].");
            System.exit(-1);
        }
        String alg = args[0];
        SMJournalExperiments exp = new SMJournalExperiments();
        exp.handleDomain(args);
        exp.runAlgorithm(alg, args[1]);
    }


    public void handleDomain(String[] args) {
        if (args[1].equalsIgnoreCase("GS")) {  // Goofspiel
            if (args.length != 6) {
                throw new IllegalArgumentException("Illegal domain arguments count: 4 parameters are required {SEED} {DEPTH} {BIN_UTIL} {FIXED_CARDS}");
            }
            GSGameInfo.seed = new Integer(args[2]);
            int depth = new Integer(args[3]);
            GSGameInfo.depth = depth;
            boolean binUtil = new Boolean(args[4]);
            GSGameInfo.BINARY_UTILITIES = binUtil;

            boolean fixedCards = new Boolean(args[5]);
            GSGameInfo.useFixedNatureSequence = fixedCards;

            GSGameInfo.regenerateCards = true;
        } else if (args[1].equalsIgnoreCase("OZ")) { // Oshi Zumo
            if (args.length != 7) {
                throw new IllegalArgumentException("Illegal domain arguments count: 4 parameters are required {SEED} {COINS} {LOC_K} {MIN_BID} {BIN_UTIL}");
            }
            OZGameInfo.seed = new Integer(args[2]);
            OZGameInfo.startingCoins = new Integer(args[3]);
            OZGameInfo.locK = new Integer(args[4]);
            OZGameInfo.minBid = new Integer(args[5]);

            boolean binUtil = new Boolean(args[6]);
            OZGameInfo.BINARY_UTILITIES = binUtil;

        } else if (args[1].equalsIgnoreCase("PE")) { // Pursuit Evasion Game
            if (args.length != 5) {
                throw new IllegalArgumentException("Illegal PEG domain arguments count: 3 parameters are required {SEED} {DEPTH} {GRAPH}");
            }
            PursuitGameInfo.seed = new Integer(args[2]);
            PursuitGameInfo.depth = new Integer(args[3]);
            PursuitGameInfo.graphFile = args[4];
        } else if (args[1].equalsIgnoreCase("RG")) { // Random Games
            if (args.length != 8) {
                throw new IllegalArgumentException("Illegal random game domain arguments count. 7 are required {SEED} {DEPTH} {BF} {CENTER_MODIFICATION} {BINARY_UTILITY} {FIXED BF}");
            }
            RandomGameInfo.seed = new Integer(args[2]);
            RandomGameInfo.rnd = new HighQualityRandom(RandomGameInfo.seed);
            RandomGameInfo.MAX_DEPTH = new Integer(args[3]);
            RandomGameInfo.MAX_BF = new Integer(args[4]);
            RandomGameInfo.MAX_CENTER_MODIFICATION = new Integer(args[5]);
            RandomGameInfo.BINARY_UTILITY = new Boolean(args[6]);
            RandomGameInfo.FIXED_SIZE_BF = new Boolean(args[7]);
        } else if (args[1].equalsIgnoreCase("Tron")) { // Tron
            if (args.length != 6) {
                throw new IllegalArgumentException("Illegal domain arguments count: 4 parameters are required {SEED} {BOARDTYPE} {ROWS} {COLUMNS}");
            }
            TronGameInfo.seed = new Integer(args[2]);
            TronGameInfo.BOARDTYPE = args[3].charAt(0);
            TronGameInfo.ROWS = new Integer(args[4]);
            TronGameInfo.COLS = new Integer(args[5]);
        } else if (args[1].equalsIgnoreCase("RPS")) { // Tron
            if (args.length != 3) {
                throw new IllegalArgumentException("Illegal domain arguments count: 4 parameters are required {SEED}");
            }
            RPSGameInfo.seed = new Integer(args[2]);
        } else throw new IllegalArgumentException("Illegal domain: " + args[1]);
    }

    public void loadGame(String domain) {
        String tmp = System.getProperty("EXPLOIT");
        boolean expl = (tmp != null && new Boolean(tmp));
        if (domain.equals("GS")) {
            gameInfo = new GSGameInfo();
            rootState = new GoofSpielGameState();
            expander = (!expl) ? new GoofSpielExpander<MCTSInformationSet>(new MCTSConfig()) : new GoofSpielExpander<SimABInformationSet>(new SimABConfig());
        } else if (domain.equals("PE")) {
            gameInfo = new PursuitGameInfo();
            rootState = new PursuitGameState();
            expander = (!expl) ? new PursuitExpander<MCTSInformationSet>(new MCTSConfig()) : new PursuitExpander<SimABInformationSet>(new SimABConfig());
        } else if (domain.equals("OZ")) {
            gameInfo = new OZGameInfo();
            rootState = new OshiZumoGameState();
            expander = (!expl) ? new OshiZumoExpander<MCTSInformationSet>(new MCTSConfig()) : new OshiZumoExpander<SimABInformationSet>(new SimABConfig());
        } else if (domain.equals("RG")) {
            gameInfo = new RandomGameInfo();
            rootState = new SimRandomGameState();
            expander = (!expl) ? new RandomGameExpander<MCTSInformationSet>(new MCTSConfig()) : new RandomGameExpander<SimABInformationSet>(new SimABConfig());
        } else if (domain.equals("Tron")) {
            gameInfo = new TronGameInfo();
            rootState = new TronGameState();
            expander = (!expl) ? new TronExpander<MCTSInformationSet>(new MCTSConfig()) : new TronExpander<SimABInformationSet>(new SimABConfig());
        } else if (domain.equals("RPS")) {
            gameInfo = new RPSGameInfo();
            rootState = new RPSGameState();
            expander = new RPSExpander<MCTSInformationSet>(new MCTSConfig());
        } else {
            throw new IllegalArgumentException("Incorrect game:" + domain);
        }
        if (expl) {
            ExploitGameInfo newGameInfo = new ExploitGameInfo(rootState, expander, gameInfo);
            Expander newExpander = new ExploitExpander<MCTSInformationSet>(new MCTSConfig(),newGameInfo);
            SimultaneousGameState newRootState = new ExploitGameState(newGameInfo);

            rootState = newRootState;
            expander = newExpander;
            gameInfo = newGameInfo;
        }
        System.out.println(gameInfo.getInfo());
    }

    public void runAlgorithm(String alg, String domain) {
        if (alg.equals("CFR") || alg.equals("OOS") || alg.startsWith("MCTS") || alg.equals("RS")) {
            loadGame(domain);
            String tl = System.getProperty("tLimit");
            if (tl != null) samplingTimeLimit = new Long(tl);
            if (alg.startsWith("MCTS")) {
                if (alg.equals("MCTS-UCT")) {
                    UCTSelector.useDeterministicUCT = true;
                    runMCTS(MCTStype.UCT);
                } else if (alg.equals("MCTS-RUCT")) {
                    UCTSelector.useDeterministicUCT = false;
                    runMCTS(MCTStype.UCT);
                } else if (alg.equals("MCTS-EXP3"))
                    runMCTS(MCTStype.EXP3);
                else if (alg.equals("MCTS-RM")) 
                    runMCTS(MCTStype.RM);
                else {
                    throw new IllegalArgumentException("MCTS requires selector function specified {MCTS-UCT,MCTS-EXP3,MCTS-RM}");
                }
            } else if (alg.equals("RS"))
                runRandomSim();  // mlanctot: you could leave this here for me? :) 
            else runCFR(alg.equals("OOS"));
        } else { // backward induction algorithms
            boolean AB = alg.endsWith("AB");
            boolean DO = alg.contains("DO");
            boolean SORT = alg.contains("DOS");
            boolean CACHE = alg.startsWith("CDO");
            boolean NOTUSEBOUNDS = !alg.startsWith("NUDO");
            SimOracleImpl.USE_INCREASING_BOUND = NOTUSEBOUNDS;
            if (!DO && (SORT || CACHE)) {
                throw new IllegalArgumentException("Illegal Argument Combination for Algorithm");
            }
            if (domain.equals("GS"))
                if (GSGameInfo.useFixedNatureSequence)
                    SimAlphaBeta.runGoofSpielWithFixedNatureSequence(AB, DO, SORT, CACHE, Integer.MAX_VALUE);
                else
                    SimAlphaBeta.runGoofSpielWithNature(AB, DO, SORT, CACHE);
            else if (domain.equals("PE"))
                SimAlphaBeta.runPursuit(AB, DO, SORT, CACHE);
            else if (domain.equals("RG"))
                SimAlphaBeta.runSimRandomGame(AB, DO, SORT, CACHE);
            else if (domain.equals("OZ"))
                SimAlphaBeta.runOshiZumo(AB, DO, SORT, CACHE);
            else if (domain.equals("Tron"))
                SimAlphaBeta.runTron(AB, DO, SORT, CACHE);
            else if (domain.equals("RPS"))
                SimAlphaBeta.runRPS(AB, DO, SORT, CACHE);
        }
    }

    // for testing
    public void runRandomSim() {
        GameState gs = rootState;
        Random rng = new Random();

        System.out.println(gameInfo.getInfo());

        while (!gs.isGameEnd()) {
            System.out.println(gs);
            System.out.println("");

            List<Action> list = expander.getActions(gs);
            int idx = rng.nextInt(list.size());
            Action a = list.get(idx);

            System.out.println("Random action: " + a);

            gs = gs.performAction(a);
        }

        System.out.println(gs);
        System.out.println("");

        double[] utilities = gs.getUtilities();
        System.out.println("util: " + utilities[0] + " " + utilities[1]);
    }

    public void runCFR(boolean OOS) {

        double secondsIteration = 0.1;

        expander.getAlgorithmConfig().createInformationSetFor(rootState);

        Double expl = 0.6d;
        String explS = System.getProperty("EXPL");
        if (explS != null) expl = new Double(explS);
        GamePlayingAlgorithm alg = (OOS) ? new SMOOSAlgorithm(rootState.getAllPlayers()[0], new OOSSimulator(expander), rootState, expander, expl, new HighQualityRandom()) : new CFRAlgorithm(rootState.getAllPlayers()[0], rootState, expander);

        Distribution dist = new MeanStratDist();

        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        long start = threadBean.getCurrentThreadCpuTime();
//        buildCompleteTree(alg.getRootNode());
//        System.out.println("Building GT: " + ((threadBean.getCurrentThreadCpuTime() - start) / 1000000));

        alg.runMiliseconds(100);

        brAlg0 = new SQFBestResponseAlgorithm(expander, 0, new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]}, (ConfigImpl) expander.getAlgorithmConfig()/*sfAlgConfig*/, gameInfo);
        brAlg1 = new SQFBestResponseAlgorithm(expander, 1, new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]}, (ConfigImpl) expander.getAlgorithmConfig()/*sfAlgConfig*/, gameInfo);


        Strategy strategy0 = null;
        Strategy strategy1 = null;
        System.out.print("P1BRs: ");

        double br1Val = Double.POSITIVE_INFINITY;
        double br0Val = Double.POSITIVE_INFINITY;
        double cumulativeTime = 0;

        for (int i = 0; cumulativeTime < samplingTimeLimit && (br0Val + br1Val > 0.005); i++) {
            alg.runMiliseconds((int) (secondsIteration * 1000));
            cumulativeTime += secondsIteration * 1000;

            System.out.println("Cumulative Time: " + (Math.ceil(cumulativeTime)));
            strategy0 = StrategyCollector.getStrategyFor(alg.getRootNode(), rootState.getAllPlayers()[0], dist);
            strategy1 = StrategyCollector.getStrategyFor(alg.getRootNode(), rootState.getAllPlayers()[1], dist);

            br1Val = brAlg1.calculateBR(rootState, ISMCTSExploitability.filterLow(strategy0));
            br0Val = brAlg0.calculateBR(rootState, ISMCTSExploitability.filterLow(strategy1));
//            System.out.println("BR1: " + br1Val);
//            System.out.println("BR0: " + br0Val);
            System.out.println("Precision: " + (br0Val + br1Val));
            //System.out.println("Mean OOS leaf depth: " + StrategyCollector.meanLeafDepth(alg.getRootNode()));
            //Pair<Double,Double> supportSize = StrategyCollector.meanSupportSize(alg.getRootNode(), new MeanStratDist());
            //System.out.println("Mean OOS support size : " + supportSize.getLeft() + ", mean num of actions: " + supportSize.getRight());
            //System.out.println("Mean OOS support size : " + StrategyCollector.meanSupportSize(strategy0) + "; " + StrategyCollector.meanSupportSize(strategy1));
            System.out.flush();
            secondsIteration *= 1.2;
        }
        //System.out.println(strategy0.fancyToString(rootState, expander, rootState.getAllPlayers()[0]));
        //System.out.println(strategy1.fancyToString(rootState, expander, rootState.getAllPlayers()[1]));
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

    public void runMCTS(MCTStype type) {
        double secondsIteration = 0.1;

        expander.getAlgorithmConfig().createInformationSetFor(rootState);

        Distribution dist = new MeanStratDist();

        BackPropFactory bpFactory = null;
        GamePlayingAlgorithm alg = null;

        switch (type) {
            case UCT:
                String cS = System.getProperty("EXPL");
                Double c = 2d;
                if (cS != null) c = new Double(cS);
                bpFactory = new UCTBackPropFactory(c);
                System.out.println(UCTSelector.useDeterministicUCT);
//                String determS = System.getProperty("UCTDET");
//                if (determS != null)
//                    UCTSelector.useDeterministicUCT = new Boolean(determS);
                break;
            case EXP3:
                cS = System.getProperty("EXPL");
                c = 0.1d;
                if (cS != null) c = new Double(cS);
                bpFactory = new Exp3BackPropFactory(-1, 1, c);
                break;
            case RM:
                cS = System.getProperty("EXPL");
                c = 0.1d;
                if (cS != null) c = new Double(cS);
                alg = new SMMCTSAlgorithm(
                        rootState.getAllPlayers()[0],
                        new DefaultSimulator(expander),
                        new SMRMBackPropFactory(c),
                        rootState, expander);
                ((SMMCTSAlgorithm) alg).runIterations(2);
        }

        if (!type.equals(MCTStype.RM)) {
            alg = new ISMCTSAlgorithm(
                    rootState.getAllPlayers()[0],
                    new DefaultSimulator(expander),
                    bpFactory,
                    //                new UCTBackPropFactory(2),
                    //                new Exp3BackPropFactory(-1, 1, 0.2),
                    //new RMBackPropFactory(-1,1,0.4),
                    rootState, expander);
            ((ISMCTSAlgorithm) alg).returnMeanValue = false;
            ((ISMCTSAlgorithm) alg).runIterations(2);
        }

        brAlg0 = new SQFBestResponseAlgorithm(expander, 0, new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]}, (ConfigImpl) expander.getAlgorithmConfig()/*sfAlgConfig*/, gameInfo);
        brAlg1 = new SQFBestResponseAlgorithm(expander, 1, new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]}, (ConfigImpl) expander.getAlgorithmConfig()/*sfAlgConfig*/, gameInfo);

        Strategy strategy0 = null;
        Strategy strategy1 = null;
        System.out.print("P1BRs: ");

        double br1Val = Double.POSITIVE_INFINITY;
        double br0Val = Double.POSITIVE_INFINITY;
        double cumulativeTime = 0;

        for (int i = 0; cumulativeTime < samplingTimeLimit && (br0Val + br1Val > 0.005); i++) {
            alg.runMiliseconds((int) (secondsIteration * 1000));
            cumulativeTime += secondsIteration * 1000;

            System.out.println("Cumulative Time: " + (Math.ceil(cumulativeTime)));
            strategy0 = StrategyCollector.getStrategyFor(alg.getRootNode(), rootState.getAllPlayers()[0], dist);
            strategy1 = StrategyCollector.getStrategyFor(alg.getRootNode(), rootState.getAllPlayers()[1], dist);
            
            //System.out.println(strategy0.fancyToString(rootState, expander, rootState.getAllPlayers()[0]));
            //System.out.println(strategy1.fancyToString(rootState, expander, rootState.getAllPlayers()[1]));

            br1Val = brAlg1.calculateBR(rootState, strategy0);
            br0Val = brAlg0.calculateBR(rootState, strategy1);
//            System.out.println("BR1: " + br1Val);
//            System.out.println("BR0: " + br0Val);
            System.out.println("Precision: " + (br0Val + br1Val));
            //System.out.println("Mean SMMCTS leaf depth: " + StrategyCollector.meanLeafDepth(alg.getRootNode()));
            //Pair<Double,Double> supportSize = StrategyCollector.meanSupportSize(alg.getRootNode(), new MeanStratDist());
            //System.out.println("Mean SMMCTS support size : " + supportSize.getLeft() + ", mean num of actions: " + supportSize.getRight());
            //System.out.println("Mean SMMCTS support size : " + StrategyCollector.meanSupportSize(strategy0) + "; " + StrategyCollector.meanSupportSize(strategy1));
            System.out.flush();
            secondsIteration *= 1.2;
        }
    }
    
    // mlanctot: Note, I used this to run batch experiments for Biased RPS
    //           Does experiments by iterations using different parameters
    public void runMCTS_ItersType(MCTStype type, Double c) {
        double secondsIteration = 0.1;

        expander.getAlgorithmConfig().createInformationSetFor(rootState);

        Distribution dist = new MeanStratDist();

        BackPropFactory bpFactory = null;
        GamePlayingAlgorithm alg = null;
        //ISMCTSAlgorithm alg = null

        switch (type) {
            case UCT:
                String cS = System.getProperty("EXPL");
                if (cS != null) c = new Double(cS);
                bpFactory = new UCTBackPropFactory(c);
                break;
            case EXP3:
                cS = System.getProperty("EXPL");
                if (cS != null) c = new Double(cS);
                bpFactory = new Exp3BackPropFactory(-1, 1, c);
                break;
            case RM:
                cS = System.getProperty("EXPL");
                if (cS != null) c = new Double(cS);
                alg = new SMMCTSAlgorithm(
                        rootState.getAllPlayers()[0],
                        new DefaultSimulator(expander),
                        new SMRMBackPropFactory(c),
                        rootState, expander);
                ((SMMCTSAlgorithm) alg).runIterations(2);
        }

        if (!type.equals(MCTStype.RM)) {
            alg = new ISMCTSAlgorithm(
                    rootState.getAllPlayers()[0],
                    new DefaultSimulator(expander),
                    bpFactory,
                    //                new UCTBackPropFactory(2),
                    //                new Exp3BackPropFactory(-1, 1, 0.2),
                    //new RMBackPropFactory(-1,1,0.4),
                    rootState, expander);
            ((ISMCTSAlgorithm) alg).returnMeanValue = false;
            ((ISMCTSAlgorithm) alg).runIterations(2);
        }

        brAlg0 = new SQFBestResponseAlgorithm(expander, 0, new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]}, (ConfigImpl) expander.getAlgorithmConfig()/*sfAlgConfig*/, gameInfo);
        brAlg1 = new SQFBestResponseAlgorithm(expander, 1, new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]}, (ConfigImpl) expander.getAlgorithmConfig()/*sfAlgConfig*/, gameInfo);

        Strategy strategy0 = null;
        Strategy strategy1 = null;
        //System.out.print("P1BRs: ");
        System.out.println("Type: " + type + ", c = " + c);

        double br1Val = Double.POSITIVE_INFINITY;
        double br0Val = Double.POSITIVE_INFINITY;
        double cumulativeTime = 0;
        int iters = 100; 
        int totalIters = 0; 

        for (int i = 0; totalIters < 100000000; i++) {
            //alg.runIterations(iters);
            switch (type) { 
              case RM: 
                ((SMMCTSAlgorithm)alg).runIterations(iters);
                break;
              case UCT:
              case EXP3:
                ((ISMCTSAlgorithm)alg).runIterations(iters);
                break;
            }

            totalIters += iters; 

            //System.out.println("Total Iters: " + totalIters); 
            strategy0 = StrategyCollector.getStrategyFor(alg.getRootNode(), rootState.getAllPlayers()[0], dist);
            strategy1 = StrategyCollector.getStrategyFor(alg.getRootNode(), rootState.getAllPlayers()[1], dist);

            br1Val = brAlg1.calculateBR(rootState, strategy0);
            br0Val = brAlg0.calculateBR(rootState, strategy1);
            //System.out.println("Precision: " + (br0Val + br1Val));

            System.out.println(totalIters + " " + (br0Val + br1Val)); 

            System.out.flush();

            iters = (int)(iters*2);
        }
    }

}
