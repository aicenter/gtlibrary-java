package cz.agents.gtlibrary.algorithms.runner;

import cz.agents.gtlibrary.algorithms.cfr.CFRAlgorithm;
import cz.agents.gtlibrary.algorithms.cfr.CFRISAlgorithm;
import cz.agents.gtlibrary.algorithms.mcts.*;
import cz.agents.gtlibrary.algorithms.mcts.distribution.Distribution;
import cz.agents.gtlibrary.algorithms.mcts.distribution.MeanStratDist;
import cz.agents.gtlibrary.algorithms.mcts.distribution.StrategyCollector;
import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.Node;
import cz.agents.gtlibrary.algorithms.mcts.nodes.oos.OOSAlgorithmData;
import cz.agents.gtlibrary.algorithms.sequenceform.FullSequenceEFG;
import cz.agents.gtlibrary.algorithms.sequenceform.SQFBestResponseAlgorithm;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.domain.goofspiel.GSGameInfo;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielExpander;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielGameState;
import cz.agents.gtlibrary.domain.pursuit.PursuitExpander;
import cz.agents.gtlibrary.domain.pursuit.PursuitGameInfo;
import cz.agents.gtlibrary.domain.pursuit.PursuitGameState;
import cz.agents.gtlibrary.domain.randomgame.RandomGameExpander;
import cz.agents.gtlibrary.domain.randomgame.RandomGameInfo;
import cz.agents.gtlibrary.domain.randomgame.RandomGameState;
import cz.agents.gtlibrary.domain.randomgame.SimRandomGameState;
import cz.agents.gtlibrary.iinodes.ConfigImpl;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.nfg.simalphabeta.SimAlphaBeta;
import cz.agents.gtlibrary.strategy.Strategy;
import cz.agents.gtlibrary.utils.HighQualityRandom;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayDeque;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: bosansky
 * Date: 3/8/13
 * Time: 10:09 AM
 * To change this template use File | Settings | File Templates.
 */
public class SMJournalExperiments {

    static GameInfo gameInfo;
    static GameState rootState;
    static SequenceFormConfig<SequenceInformationSet> sfAlgConfig;
    static SQFBestResponseAlgorithm brAlg0;
    static SQFBestResponseAlgorithm brAlg1;
    static Expander<MCTSInformationSet> expander;


    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Missing Arguments: SMJournalExperiments {BI|BIAB|DO|DOAB|CFR|OOS} {GS|PE|RG} [domain parameters].");
            System.exit(-1);
        }
        String alg = args[0];
        SMJournalExperiments exp = new SMJournalExperiments();
        exp.handleDomain(args);
        exp.runAlgorithm(alg, args[1]);
    }


    public void handleDomain(String[] args) {
        if (args[1].equalsIgnoreCase("GS")) {  // Goofspiel
            if (args.length != 4) {
                throw new IllegalArgumentException("Illegal domain arguments count: 2 parameters are required {SEED} {DEPTH}");
            }
            GSGameInfo.seed = new Integer(args[2]);
            int depth = new Integer(args[3]);
            GSGameInfo.depth = depth;
            GSGameInfo.regenerateCards = true;
        } else if (args[1].equalsIgnoreCase("PE")) { // Generic Poker
            if (args.length != 5) {
                throw new IllegalArgumentException("Illegal poker domain arguments count: 3 parameters are required {SEED} {DEPTH} {GRAPH}");
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
        } else throw new IllegalArgumentException("Illegal domain: " + args[1]);
    }

    public void runAlgorithm(String alg, String domain) {
        if (alg.equals("CFR") || alg.equals("OOS")) {
            if (domain.equals("GS")) {
                gameInfo = new GSGameInfo();
                rootState = new GoofSpielGameState();
                expander = new GoofSpielExpander<MCTSInformationSet>(new MCTSConfig());
                sfAlgConfig = new SequenceFormConfig<SequenceInformationSet>();
            } else if (domain.equals("PE")) {
                gameInfo = new PursuitGameInfo();
                rootState = new PursuitGameState();
                expander = new PursuitExpander<MCTSInformationSet>(new MCTSConfig());
                sfAlgConfig = new SequenceFormConfig<SequenceInformationSet>();
            } else if (domain.equals("RG")) {
                gameInfo = new RandomGameInfo();
                rootState = new SimRandomGameState();
                expander = new RandomGameExpander<MCTSInformationSet>(new MCTSConfig());
                sfAlgConfig = new SequenceFormConfig<SequenceInformationSet>();
            }
            runCFR(alg.equals("OOS"));
        } else { // backward induction algorithms
            boolean AB = alg.endsWith("AB");
            boolean DO = alg.startsWith("DO");
            boolean SORT = alg.startsWith("DOS");
            if (domain.equals("GS"))
                SimAlphaBeta.runGoofSpielWithFixedNatureSequence(AB,DO,SORT);
            else if (domain.equals("PE"))
                SimAlphaBeta.runPursuit(AB, DO, SORT);
            else if (domain.equals("RG"))
                SimAlphaBeta.runSimRandomGame(AB, DO, SORT);
       } 
    }

    public void runCFR(boolean OOS) {

        double secondsIteration = 0.1;

        expander.getAlgorithmConfig().createInformationSetFor(rootState);

        GamePlayingAlgorithm alg = (OOS) ? new OOSAlgorithm(rootState.getAllPlayers()[0],new OOSSimulator(expander),rootState, expander, 0, 0.6) : new CFRAlgorithm(rootState.getAllPlayers()[0],rootState, expander);

        Distribution dist = new MeanStratDist();

        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        long start = threadBean.getCurrentThreadCpuTime();
        buildCompleteTree(alg.getRootNode());
        System.out.println("Building GT: " + ((threadBean.getCurrentThreadCpuTime() - start)/1000000));

        alg.runMiliseconds(100);

        brAlg0 = new SQFBestResponseAlgorithm(expander, 0, new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]}, (ConfigImpl)expander.getAlgorithmConfig()/*sfAlgConfig*/, gameInfo);
        brAlg1 = new SQFBestResponseAlgorithm(expander, 1, new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]}, (ConfigImpl)expander.getAlgorithmConfig()/*sfAlgConfig*/, gameInfo);


        Strategy strategy0 = null;
        Strategy strategy1 = null;
        System.out.print("P1BRs: ");

        double br1Val = Double.POSITIVE_INFINITY;
        double br0Val = Double.POSITIVE_INFINITY;
        double cumulativeTime = 0;

        for (int i = 0; cumulativeTime < 1800000 && (br0Val + br1Val > 0.005); i++) {
            alg.runMiliseconds((int)(secondsIteration*1000));
            cumulativeTime += secondsIteration*1000;

            System.out.println("Cumulative Time: "+(cumulativeTime));
            strategy0 = StrategyCollector.getStrategyFor(alg.getRootNode(), rootState.getAllPlayers()[0], dist);
            strategy1 = StrategyCollector.getStrategyFor(alg.getRootNode(), rootState.getAllPlayers()[1], dist);

            br1Val = brAlg1.calculateBR(rootState, ISMCTSExploitability.filterLow(strategy0));
            br0Val = brAlg0.calculateBR(rootState, ISMCTSExploitability.filterLow(strategy1));
//            System.out.println("BR1: " + br1Val);
//            System.out.println("BR0: " + br0Val);
            System.out.println("Precision: " + (br0Val + br1Val));
            System.out.flush();
            secondsIteration *= 2;
        }
    }

    public static void buildCompleteTree(InnerNode r){
        System.out.println("Building complete tree.");
        int nodes=0, infosets=0;
        ArrayDeque<InnerNode> q = new ArrayDeque<InnerNode>();
        q.add(r);
        while (!q.isEmpty()){
            nodes++;
            InnerNode n = q.removeFirst();
            MCTSInformationSet is = n.getInformationSet();
            if (is.getAlgorithmData() == null) {
                infosets++;
                is.setAlgorithmData(new OOSAlgorithmData(n.getActions()));
            }
            for (Action a : n.getActions()){
                Node ch = n.getChildFor(a);
                if (ch instanceof InnerNode) {
                    q.add((InnerNode)ch);
                }
            }
        }
        System.out.println("Created nodes: " + nodes +"; infosets: " +infosets);
    }

}
