package cz.agents.gtlibrary.algorithms.cfr.br.experiments;

import cz.agents.gtlibrary.algorithms.cfr.br.responses.BestResponse;
import cz.agents.gtlibrary.algorithms.cfr.br.CFRBR;
import cz.agents.gtlibrary.algorithms.cfr.br.responses.PureResponse;
import cz.agents.gtlibrary.algorithms.cfr.br.responses.QuantalResponse;
import cz.agents.gtlibrary.algorithms.cfr.br.responses.SUQuantalResponse;
import cz.agents.gtlibrary.algorithms.mcts.MCTSConfig;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.gensum.experiments.StrategyStrengthLargeExperiments;
import cz.agents.gtlibrary.domain.flipit.*;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.utils.HighQualityRandom;
import cz.agents.gtlibrary.utils.graph.Graph;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;

/**
 * Created by Jakub Cerny on 25/04/2018.
 */
public class CFRBRExperiment {

    public static void main(String[] args) {
        if(args.length > 0){
            runFlipIt(args);
        }
        else{
            runFlipIt(new String[]{"F", "4", "3", "F", "-1", "1", "1000", "S",
                    "-0.9", "0.6", "-0.2", "0.7", "-0.4"});
        }
    }

    public static void runFlipIt(String[] args){
        FlipItGameInfo gameInfo;
        int seed = 10;
        String responseVersion = "P";
        int BRplayer = 1;
        int iterations = 100000;
        double lambda = 0.001;
        double[] ws = null;

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
            if(args.length > 5){
                BRplayer = Integer.parseInt(args[5]);
            }
            if(args.length > 6){
                iterations = Integer.parseInt(args[6]);
            }
            if(args.length > 7){
                responseVersion = args[7];
            }
            if(responseVersion.equals("Q")){
                lambda = Double.parseDouble(args[8]);
            }
            if(responseVersion.equals("S")){
                ws = new double[5];
                ws[0] = Double.parseDouble(args[8]);
                ws[1] = Double.parseDouble(args[9]);
                ws[2] = Double.parseDouble(args[10]);
                ws[3] = Double.parseDouble(args[11]);
                ws[4] = Double.parseDouble(args[12]);
            }

        }
        gameInfo.ZERO_SUM_APPROX = true;
        gameInfo.ENABLE_PASS = true;
        GameState rootState;

        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        rootState = initGame(gameInfo, seed);

        System.out.println("CFR BR. Setting: ");
        System.out.println("BR Player = " + gameInfo.getAllPlayers()[BRplayer] + ", BR Type = " + responseVersion);
        if (responseVersion.equals("Q")){
            System.out.println("Response setting: Lambda = " + lambda);
        }
        if (responseVersion.equals("S")){
            System.out.println("Response setting: W = " + Arrays.toString(ws));
        }
        System.out.println(gameInfo.getInfo());

        FlipItExpander<MCTSInformationSet> expander = new FlipItExpander<>(new MCTSConfig());

        CFRBR cfr = new CFRBR(rootState.getAllPlayers()[1-BRplayer], rootState, expander, BRplayer);
        StrategyStrengthLargeExperiments.buildCFRBRCompleteTree(cfr.getRootNode(), BRplayer);

        BestResponse response = null;
        switch (responseVersion) {
            case "P":
                response = new PureResponse(rootState.getAllPlayers()[BRplayer],
                        cfr.getRootNode());
                break;
            case "Q":
                response = new QuantalResponse(rootState.getAllPlayers()[BRplayer],
                        cfr.getRootNode(), lambda);
                break;
            case "S":
                response = new SUQuantalResponse(rootState.getAllPlayers()[BRplayer],
                        cfr.getRootNode(), ws[0], ws[1], ws[2], ws[3], ws[4]);

        }

        long brTime = 0;
        long cfrTime = 0;
        long startTime;
        double cfrValue, brValue;
        for(int i = 0; i < iterations; i++){
            startTime = threadBean.getCurrentThreadCpuTime();
            cfrValue = cfr.runCFRIteration();
            cfrTime += (threadBean.getCurrentThreadCpuTime() - startTime) / 1000000l;
            startTime = threadBean.getCurrentThreadCpuTime();
            brValue = cfr.runBRIteration(response);
            brTime += (threadBean.getCurrentThreadCpuTime() - startTime) / 1000000l;
            if (i % 100 == 0) printStatistics(brTime, cfrTime, i, cfrValue, brValue);
        }

    }

    protected static void printStatistics(long brTime, long cfrTime, int iteration, double cfrValue, double brValue){
        String timeStamp = new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime());
        System.out.println("Iteration " + iteration);
        System.out.println("\t time = " + timeStamp);
        System.out.println("\t cfr value = " + cfrValue);
        System.out.println("\t br value  = " + brValue);
        System.out.println("\t average brtime  = " + ((double)brTime / (iteration+1)));
        System.out.println("\t average cfrtime = " + ((double)cfrTime / (iteration+1)));
        System.out.println("\t full brtime     = " + brTime);
        System.out.println("\t full cfrtime    = " + cfrTime);
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
