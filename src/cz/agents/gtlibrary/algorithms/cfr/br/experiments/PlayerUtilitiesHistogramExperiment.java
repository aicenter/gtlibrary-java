package cz.agents.gtlibrary.algorithms.cfr.br.experiments;

import cz.agents.gtlibrary.algorithms.cfr.br.CFRBR;
import cz.agents.gtlibrary.algorithms.cfr.br.CFRBRAlgorithmData;
import cz.agents.gtlibrary.algorithms.cfr.br.responses.BestResponse;
import cz.agents.gtlibrary.algorithms.cfr.br.responses.PureResponse;
import cz.agents.gtlibrary.algorithms.cfr.br.responses.QuantalResponse;
import cz.agents.gtlibrary.algorithms.mcts.MCTSConfig;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.nodes.ChanceNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.LeafNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.NodeImpl;
import cz.agents.gtlibrary.algorithms.sequenceform.gensum.experiments.StrategyStrengthLargeExperiments;
import cz.agents.gtlibrary.domain.flipit.*;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.utils.HighQualityRandom;
import cz.agents.gtlibrary.utils.graph.Graph;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Jakub Cerny on 24/06/2018.
 */
public class PlayerUtilitiesHistogramExperiment {



    public static void main(String[] args) {
        if(args.length > 0){
            runFlipIt(args);
        }
        else{
            runFlipIt(new String[]{"F", "4", "3", "AP", "-1"});
        }
    }

    public static void runFlipIt(String[] args){
        FlipItGameInfo gameInfo;
        int seed = 10;
        String responseVersion = "P";
        ArrayList<String> responseVersionExploiting = new ArrayList<>();
        ArrayList<Double> lambdaExploiting = new ArrayList<>();
        int BRplayer = 1;
        int iterations = 100000;
        double lambda = 0.001;

        if (args.length == 0)
            gameInfo = new FlipItGameInfo();
        else {
            int depth = Integer.parseInt(args[1]);
            String graphSize = args[2];
            String graphFile = "flipit_empty" + graphSize + ".txt";
//            String graphFile = (graphSize == 3 ) ? "flipit_empty3.txt" : (graphSize == 4 ? "flipit_empty4.txt" : (graphSize == 5 ? "flipit_empty5.txt" : ""));
            gameInfo = new FlipItGameInfo(depth, 1, graphFile, 1);
            FlipItGameInfo.OUTPUT_STRATEGY = false;
            if (args.length > 3) {
                String version = args[3];
                switch (version) {
                    case "F":
                        FlipItGameInfo.gameVersion = FlipItGameInfo.FlipItInfo.FULL;
                        break;
                    case "N":
                        FlipItGameInfo.gameVersion = FlipItGameInfo.FlipItInfo.NO;
                        break;
                    case "NP":
                        FlipItGameInfo.gameVersion = FlipItGameInfo.FlipItInfo.REVEALED_NODE_POINTS;
                        break;
                    case "AP":
                        FlipItGameInfo.gameVersion = FlipItGameInfo.FlipItInfo.REVEALED_ALL_POINTS;
                        break;//
                }
            }
            if(args.length > 4){
                seed = Integer.parseInt(args[4]);
            }

        }
        gameInfo.ZERO_SUM_APPROX = true;
        gameInfo.ENABLE_PASS = true;
        GameState rootState;

        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        rootState = initGame(gameInfo, seed);

        FlipItExpander<MCTSInformationSet> expander = new FlipItExpander<>(new MCTSConfig());

        CFRBR cfr = new CFRBR(rootState.getAllPlayers()[1-BRplayer], rootState, expander, BRplayer);
        buildCFRBRCompleteTree(cfr.getRootNode(), BRplayer);

    }

    public static void buildCFRBRCompleteTree(InnerNode r, int brPlayer) {
        int nodes = 0, infosets = 0;
        ArrayDeque<InnerNode> q = new ArrayDeque<InnerNode>();
        q.add(r);
        while (!q.isEmpty()) {
            if(nodes % 100000 == 0) {
                System.gc();
            }
            nodes++;
            InnerNode n = q.removeFirst();
            MCTSInformationSet is = n.getInformationSet();
            if (!(n instanceof ChanceNode)) {
                if (is.getAlgorithmData() == null) {
                    infosets++;
                    is.setAlgorithmData(new CFRBRAlgorithmData(n.getActions(), is.getPlayer().getId() == brPlayer));
                }
            }
            else{
                ((ChanceNode) n).setActionProbabilities();
            }
            for (Action a : n.getActions()) {
                NodeImpl ch = (NodeImpl) n.getChildFor(a);
                if (ch instanceof InnerNode) {
                    q.add((InnerNode) ch);
                }
                else{
                    ch.deleteGameState();
                    System.out.println(((LeafNode)ch).getUtilities()[1-brPlayer]);
                }
            }
            n.deleteGameState();
        }

        System.gc();
//        System.out.println("Created nodes: " + nodes + "; infosets: " + infosets);
    }

    protected static GameState initGame(FlipItGameInfo gameInfo, int seed) {

        int rounding = 3;
        final double MAX_COST = 10, MAX_REWARD = 10;//MAX_COST;
        int numberOfNodes = (new Graph(gameInfo.graphFile)).getAllNodes().size();//Integer.parseInt(gameInfo.graphFile.substring(gameInfo.graphFile.length() - 5, gameInfo.graphFile.length() - 4));
//        System.out.println(numberOfNodes);
        HighQualityRandom random = new HighQualityRandom(seed);
        double[] costs = new double[numberOfNodes];
        double[] rewards = new double[numberOfNodes];
        for (int i = 0; i < numberOfNodes; i++) {
            int type = random.nextInt(4);
            switch (type) {
                // HW [6..10], HC[6..10]
                case 0:
                    costs[i] = 6 + random.nextInt(5);
                    rewards[i] = 6 + random.nextInt(5);
                    break;
                // HW[6..10], LC[3..6]
                case 1:
                    costs[i] = 3 + random.nextInt(4);
                    rewards[i] = 6 + random.nextInt(5);
                    break;
                // LW[3..6], HC[5..9]
                case 2:
                    costs[i] = 5 + random.nextInt(5);
                    rewards[i] = 3 + random.nextInt(4);
                    break;
                // LW[1..4], LC[1..4]
                case 3:
                    costs[i] = 1 + random.nextInt(4);
                    rewards[i] = 1 + random.nextInt(4);
                    break;
            }

//            costs[i] = Math.round(((int) Math.pow(10, rounding)) * MAX_COST * random.nextDouble()) / Math.pow(10, rounding);
//            rewards[i] = Math.round(((int) Math.pow(10, rounding)) * MAX_REWARD * random.nextDouble()) / Math.pow(10, rounding);
        }

        if (seed != -1) {
            gameInfo.graph = new FlipItGraph(gameInfo.graphFile, costs, rewards);
        }
        else{
            gameInfo.graph = new FlipItGraph(gameInfo.graphFile);
        }

        GameState rootState = null;
        if (FlipItGameInfo.CALCULATE_UTILITY_BOUNDS) gameInfo.calculateMinMaxBounds();

        switch (FlipItGameInfo.gameVersion) {
            case NO:
                rootState = new NoInfoFlipItGameState();
                break;
            case FULL:
                rootState = new FullInfoFlipItGameState();
                break;
            case REVEALED_ALL_POINTS:
                rootState = new AllPointsFlipItGameState();
                break;
            case REVEALED_NODE_POINTS:
                rootState = new NodePointsFlipItGameState();
                break;

        }
        return rootState;
    }
}
