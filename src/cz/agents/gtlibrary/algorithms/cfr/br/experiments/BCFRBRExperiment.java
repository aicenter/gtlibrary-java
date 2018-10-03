package cz.agents.gtlibrary.algorithms.cfr.br.experiments;

import cz.agents.gtlibrary.algorithms.cfr.br.responses.BestResponse;
import cz.agents.gtlibrary.algorithms.cfr.br.CFRBR;
import cz.agents.gtlibrary.algorithms.cfr.br.responses.PureResponse;
import cz.agents.gtlibrary.algorithms.cfr.br.responses.QuantalResponse;
import cz.agents.gtlibrary.algorithms.mcts.MCTSConfig;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.gensum.experiments.StrategyStrengthLargeExperiments;
import cz.agents.gtlibrary.domain.flipit.*;
import cz.agents.gtlibrary.domain.flipit.types.FollowerType;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.utils.HighQualityRandom;
import cz.agents.gtlibrary.utils.graph.Graph;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Jakub Cerny on 25/04/2018.
 */
public class BCFRBRExperiment {

    protected static boolean ZERO_SUM = true;

    public static void main(String[] args) {
        if(args.length > 0){
            runFlipIt(args);
        }
        else{
            runFlipIt(new String[]{"F", "3", "3", "2", "0.91", "0.09", "AP", "-1", "1", "1000", "P", "Q", "0.1"});
        }
    }

    public static void runFlipIt(String[] args){
        FlipItGameInfo gameInfo;
        int seed = 10;
        String responseVersion = "P";
        int BRplayer = 1;
        int iterations = 100000;
        double lambda = 0.001;
        int numTypes = 1;

        ArrayList<BestResponse> responses = new ArrayList<>();

        if (args.length == 0)
            gameInfo = new FlipItGameInfo();
        else {
            int depth = Integer.parseInt(args[1]);
            String graphSize = args[2];
            String graphFile = "flipit_empty" + graphSize + ".txt";
//            String graphFile = (graphSize == 3 ) ? "flipit_empty3.txt" : (graphSize == 4 ? "flipit_empty4.txt" : (graphSize == 5 ? "flipit_empty5.txt" : ""));

            numTypes = Integer.parseInt(args[3]);

            double[] typesPrior = new double[numTypes];
            for(int i = 0; i < numTypes; i++)
                typesPrior[i] = Double.parseDouble(args[4+i]);

            gameInfo = new FlipItGameInfo(depth, numTypes, graphFile, typesPrior);

            FlipItGameInfo.OUTPUT_STRATEGY = false;
            if (args.length > 4+numTypes) {
                String version = args[4+numTypes];
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
            if(args.length > 5+numTypes){
                seed = Integer.parseInt(args[5+numTypes]);
            }
            if(args.length > 6+numTypes){
                BRplayer = Integer.parseInt(args[6+numTypes]);
            }
            if(args.length > 7+numTypes){
                iterations = Integer.parseInt(args[7+numTypes]);
            }
        }
        // if true, all follower types have utility -u[0]
        gameInfo.ZERO_SUM_APPROX = ZERO_SUM;
        gameInfo.ENABLE_PASS = true;
        GameState rootState;

        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        rootState = initGame(gameInfo, seed);


        FlipItExpander<MCTSInformationSet> expander = new FlipItExpander<>(new MCTSConfig());

        CFRBR cfr = new CFRBR(rootState.getAllPlayers()[1-BRplayer], rootState, expander, BRplayer);
        cfr.setIsZeroSum(ZERO_SUM); // set partially 0-SUM

        int i = 8+numTypes;

        /* idx has to start at position describing utility */
        int k = 1;
        while (i < args.length){
            responseVersion = args[i];
            i++;
            if(responseVersion.equals("Q")){
                lambda = Double.parseDouble(args[i]);
                i++;
            }

            switch (responseVersion) {
                case "P":
                    responses.add(new PureResponse(rootState.getAllPlayers()[BRplayer], k,
                            cfr.getRootNode()));
                    break;
                case "Q":
                    responses.add(new QuantalResponse(rootState.getAllPlayers()[BRplayer], k,
                            cfr.getRootNode(), lambda));
                    break;
            }

            k++;

        }

        System.out.println("Bayesian CFR BR. Setting: ");
        System.out.printf("BR Player = " + gameInfo.getAllPlayers()[BRplayer] + ", BR Types: {");
        for(i = 0; i < responses.size(); i++){
            if(responses.get(i) instanceof PureResponse)
                System.out.printf("[P]");
            if(responses.get(i) instanceof QuantalResponse)
                System.out.printf("[Q : %.5f]", ((QuantalResponse) responses.get(i)).getLambda());
            if ( i < responses.size() - 1)
                System.out.printf(", ");
            else
                System.out.printf("}\n");
        }
        System.out.println(gameInfo.getInfo());

        int[] types = new int[numTypes];
        for (int l = 1; l <= numTypes; l++ )
            types[l-1] = l;
        StrategyStrengthLargeExperiments.buildBCFRBRCompleteTree(cfr.getRootNode(), BRplayer, types);

        long[] brTimes = new long[numTypes];
        long cfrTime = 0;
        long startTime;
        double cfrValue; double[] cfrValues = new double[numTypes+1];
        double[] brValues = new double[numTypes];

        int typeIdx;
        for(i = 0; i < iterations; i++){

            if (i % 300 == 0){
                System.gc();
            }

            startTime = threadBean.getCurrentThreadCpuTime();
            typeIdx = 1;
            cfrValue = 0.0;
            for(FollowerType type : FlipItGameInfo.types) {
                cfrValues[typeIdx-1] = cfr.runCFRIterationAgainst(typeIdx, type.getPrior());
                cfrValue += type.getPrior() * cfrValues[typeIdx-1];
                typeIdx++;
            }
            cfrValues[numTypes] = cfrValue;
            cfrTime += (threadBean.getCurrentThreadCpuTime() - startTime) / 1000000l;

            typeIdx = 0;
            for(BestResponse response : responses) {
                startTime = threadBean.getCurrentThreadCpuTime();
                brValues[typeIdx] = cfr.runBRIteration(response);
                brTimes[typeIdx] += (threadBean.getCurrentThreadCpuTime() - startTime) / 1000000l;
                typeIdx++;
            }


            printStatistics(brTimes, cfrTime, i, cfrValues, brValues);
        }

    }

    protected static void printStatistics(long[] brTime, long cfrTime, int iteration, double[] cfrValues, double[] brValue){
        String cfr = ""; for (int i = 0; i < cfrValues.length-1; i++) cfr+=String.format(i != cfrValues.length-2 ? "%.5f, " : "%.5f ",cfrValues[i]);
        String timeStamp = new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime());
        System.out.println("Iteration " + iteration);
        System.out.println("\t time = " + timeStamp);
        System.out.println("\t cfr value = " + cfrValues[cfrValues.length-1] + "; per type = " +cfr);
        System.out.println("\t average cfrtime = " + ((double)cfrTime / (iteration+1)));
        System.out.println("\t full cfrtime    = " + cfrTime);

        for(int i = 0; i < brTime.length; i++) {
            System.out.println("\t Type: " + i);
            System.out.println("\t\t br value  = " + brValue[i]);
            System.out.println("\t\t average brtime  = " + ((double) brTime[i]/ (iteration + 1)));
            System.out.println("\t\t full brtime     = " + brTime[i]);
        }
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
