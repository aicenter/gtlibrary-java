package cz.agents.gtlibrary.nfg.sce;

import cz.agents.gtlibrary.algorithms.sequenceform.gensum.GenSumSequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPData;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPTable;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.RecyclingLPTable;
import cz.agents.gtlibrary.algorithms.stackelberg.correlated.strategy.PureStrategyImpl;
import cz.agents.gtlibrary.domain.flipit.*;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.utils.HighQualityRandom;
import cz.agents.gtlibrary.utils.Pair;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by Jakub Cerny on 17/08/2017.
 */
public class SCERunner {

    double[][] utilitiesLeader;
    double[][] utilitiesFollower;
    boolean[] restrictedGameLeader;
    boolean[] restrictedGameFollower;
    ArrayList<Integer> followerBR;

    protected LPTable lpTable;

    protected int seed = 0;

    protected final boolean readFile = false;
//    protected final String fileName = "flipit_vetsi.nfg";
    protected final String fileName = "flipit_1x3.nfg";

    protected final boolean generateNFG = !readFile;

    protected final double EPS = 0.001;

    public static void main(String[] args) {
        boolean RUN_TESTS = false;
        if (RUN_TESTS)
            runTests(15);
        else{
            SCERunner runner = new SCERunner(15);
            runner.buildNFG();
            runner.printUtilities();
//            runner.runFullGame();
            runner.runIterativeWithBestResponse();
        }
    }

    public static void runTests(int maxseed){
        SCERunner runner;
        double fullGameValue;
        double iterativeValue;
        ArrayList<Integer> notConvergedSeeds = new ArrayList<>();
        double ratio = 0.0;
        for (int seed = 0; seed < maxseed; seed++){
            System.out.println("Seed = "+seed + ", current number of NC cases = "+notConvergedSeeds.size());
            runner = new SCERunner(seed);
            runner.buildNFG();
            fullGameValue = runner.runFullGame();
            runner = new SCERunner(seed);
            runner.buildNFG();
            iterativeValue = runner.runIterativeWithBestResponse();
            System.out.println("Full game value = " + fullGameValue + ", iterative game value = " + iterativeValue);
            if (Math.abs(iterativeValue - fullGameValue) > 0.001) {
                notConvergedSeeds.add(seed);
            }
            ratio += runner.getRGRatio();
        }
        System.out.println("Average RG ratio = " + ratio / maxseed);
//        notConvergedSeeds.add(1); notConvergedSeeds.add(2);
        System.out.println("Number of not converged cases = " + notConvergedSeeds.size() + "; seeds = "+ notConvergedSeeds.toString());
    }

    public SCERunner(int seed){
        this.seed = seed;
        lpTable = new LPTable();//LPTable();

        utilitiesLeader = new double[][]{{1, 2, 3}, {4, 5, 6}};
        utilitiesFollower = new double[][]{{2, 2, 4}, {3, 5, -2}};

        followerBR = new ArrayList<>();
//        printUtilities();
//        runFullGame();
//        runIterative();
//        runIterativeWithBestResponse();
    }

    public SCERunner(int seed, double[][] utilitiesLeader, double[][] utilitiesFollower){
        this.seed = seed;
        lpTable = new LPTable();//LPTable();

        this.utilitiesLeader = utilitiesLeader;
        this.utilitiesFollower = utilitiesFollower;

        followerBR = new ArrayList<>();

        restrictedGameLeader = new boolean[utilitiesLeader.length];
        restrictedGameFollower = new boolean[utilitiesLeader[0].length];

//        printUtilities();
//        runFullGame();
//        runIterative();
//        runIterativeWithBestResponse();
    }

    protected void buildNFG(){
        if (readFile)
            readNFGFile(fileName);
        if (generateNFG)
            generateNFG();

        restrictedGameLeader = new boolean[utilitiesLeader.length];
        restrictedGameFollower = new boolean[utilitiesLeader[0].length];
    }


    protected void printUtilities(){
        for (int leaderAction = 0; leaderAction <  utilitiesLeader.length; leaderAction++) {
            for (int followerAction = 0; followerAction < utilitiesLeader[0].length; followerAction++) {
                System.out.printf("%.2f %.2f\t\t", utilitiesLeader[leaderAction][followerAction], utilitiesFollower[leaderAction][followerAction]);
            }
            System.out.printf("\n");
        }
    }

    protected void generateNFG(){
        generateFlipIt();
    }

    protected void generateFlipIt(){

        FlipItGameInfo gameInfo = new FlipItGameInfo();

        final double MAX_COST = 5, MAX_REWARD = 10;//MAX_COST;
        int numberOfNodes = Integer.parseInt(gameInfo.graphFile.substring(gameInfo.graphFile.length()-5, gameInfo.graphFile.length()-4));
//        System.out.println(numberOfNodes);
        HighQualityRandom random = new HighQualityRandom(seed);
        double[] costs = new double[numberOfNodes];
        double[] rewards = new double[numberOfNodes];
        for (int i = 0; i < numberOfNodes; i++){
            costs[i] = Math.round(100*MAX_COST * random.nextDouble())/100.0;
            rewards[i] = Math.round(100*MAX_REWARD * random.nextDouble())/100.0;
        }

        System.out.println(Arrays.toString(costs));
        System.out.println(Arrays.toString(rewards));

//        costs = new double[]{6.0,10.0};
//        rewards = new double[]{6.0,2.0};

        gameInfo.ZERO_SUM_APPROX = false;
        gameInfo.graph = new FlipItGraph(gameInfo.graphFile, costs, rewards);

        GameState rootState = null;

        switch (FlipItGameInfo.gameVersion){
            case NO:                    rootState = new NoInfoFlipItGameState(); break;
            case FULL:                  rootState = new FullInfoFlipItGameState(); break;
            case REVEALED_ALL_POINTS:   rootState = new AllPointsFlipItGameState(); break;
            case REVEALED_NODE_POINTS:  rootState = new NodePointsFlipItGameState(); break;
        }
        GenSumSequenceFormConfig algConfig = new GenSumSequenceFormConfig();
        Efg2Nfg generator = new Efg2Nfg(rootState, new FlipItExpander<>(algConfig), gameInfo, algConfig);
        HashMap<ArrayList<PureStrategyImpl>, double[]> utility = generator.getUtility();
        int index = 0;
        HashMap<PureStrategyImpl, Integer> leaderActions = new HashMap<>();
        for (PureStrategyImpl leaderStrat : generator.getStrategiesOfAllPlayers().get(0)) {
            leaderActions.put(leaderStrat,index); index++;
        }
        index = 0;
        HashMap<PureStrategyImpl, Integer> followerActions = new HashMap<>();
        for (PureStrategyImpl followerStrat : generator.getStrategiesOfAllPlayers().get(1)) {
            followerActions.put(followerStrat,index); index++;
        }
//        System.out.println("Utility size : " + utility.keySet().size());
        utilitiesLeader = new double[generator.getStrategiesOfAllPlayers().get(0).size()][generator.getStrategiesOfAllPlayers().get(1).size()];
        utilitiesFollower = new double[generator.getStrategiesOfAllPlayers().get(0).size()][generator.getStrategiesOfAllPlayers().get(1).size()];
        for (ArrayList<PureStrategyImpl> profile : utility.keySet()){
            int leaderAction = leaderActions.get(profile.get(0));
            int followerAction = followerActions.get(profile.get(1));
            utilitiesLeader[leaderAction][followerAction] = utility.get(profile)[0];
            utilitiesFollower[leaderAction][followerAction] = utility.get(profile)[1];
        }

//        GambitEFG gambit = new GambitEFG();
//        gambit.buildAndWrite("flipit.gbt", rootState, new FlipItExpander<>(algConfig));
    }

    protected HashSet<Integer> findFollowersInitialRG(){
        double maxUtility = Double.NEGATIVE_INFINITY;
        HashSet<Integer> maxActions = new HashSet<>();
//        System.out.println(utilitiesLeader.length + " " + utilitiesLeader[0].length);
        for (int leaderAction = 0; leaderAction <  utilitiesLeader.length; leaderAction++)
            for (int followerAction = 0; followerAction < utilitiesLeader[0].length; followerAction++) {
//                System.out.println(maxUtility + " " + utilitiesFollower[leaderAction][followerAction] + "; nerovnost : " + (utilitiesFollower[leaderAction][followerAction] > maxUtility));
                if (utilitiesFollower[leaderAction][followerAction] > maxUtility) {
//                    maxAction = followerAction;
                    maxUtility = utilitiesFollower[leaderAction][followerAction];
                }
            }
        System.out.println("Followers initial max utility = " + maxUtility);
        System.out.printf("Max actions = ");
        for (int leaderAction = 0; leaderAction <  utilitiesLeader.length; leaderAction++)
            for (int followerAction = 0; followerAction < utilitiesLeader[0].length; followerAction++) {
//                System.out.println(maxUtility + " " + utilitiesFollower[leaderAction][followerAction] + "; nerovnost : " + (utilitiesFollower[leaderAction][followerAction] > maxUtility));
                if (utilitiesFollower[leaderAction][followerAction] >= maxUtility) {
                    maxActions.add(followerAction);
                    System.out.printf("%d ",followerAction);
                }
            }
        System.out.println();
        return maxActions;
    }

    protected HashSet<Integer> findLeadersInitialRG(){
        double maxUtility = Double.NEGATIVE_INFINITY;
        HashSet<Integer> maxActions = new HashSet<>();
//        System.out.println(utilitiesLeader.length + " " + utilitiesLeader[0].length);
        for (int leaderAction = 0; leaderAction <  utilitiesLeader.length; leaderAction++)
            for (int followerAction = 0; followerAction < utilitiesLeader[0].length; followerAction++) {
//                System.out.println(maxUtility + " " + utilitiesFollower[leaderAction][followerAction] + "; nerovnost : " + (utilitiesFollower[leaderAction][followerAction] > maxUtility));
                if (utilitiesLeader[leaderAction][followerAction] > maxUtility) {
//                    maxAction = followerAction;
                    maxUtility = utilitiesLeader[leaderAction][followerAction];
                }
            }
        System.out.println("Leader's initial max utility = " + maxUtility);
        System.out.printf("Max actions = ");
        for (int leaderAction = 0; leaderAction <  utilitiesLeader.length; leaderAction++)
            for (int followerAction = 0; followerAction < utilitiesLeader[0].length; followerAction++) {
//                System.out.println(maxUtility + " " + utilitiesFollower[leaderAction][followerAction] + "; nerovnost : " + (utilitiesFollower[leaderAction][followerAction] > maxUtility));
                if (utilitiesLeader[leaderAction][followerAction] >= maxUtility) {
                    maxActions.add(leaderAction);
                    System.out.printf("%d ",leaderAction);
                }
            }
        System.out.println();
        return maxActions;
    }

    public double runIterativeWithBestResponse(){
        // find action maximizing follower's utility and add to RG
//        for (int followerIntialRG : findFollowersInitialRG())
//            restrictedGameFollower[followerIntialRG] = true;
        for (int followerIntialRG = 0; followerIntialRG < utilitiesLeader[0].length; followerIntialRG++)
            restrictedGameFollower[followerIntialRG] = true;
        for (int leaderInitialRG : findLeadersInitialRG()){
            restrictedGameLeader[leaderInitialRG] = true;
            addActionToCriterion(leaderInitialRG);
            addActionToRG(leaderInitialRG);
        }
//        restrictedGameLeader[5] = true;
//        restrictedGameFollower
//        for (int i = 0; i < utilitiesLeader[0].length; i++) restrictedGameFollower[i] = true;
        double leaderUtility;// = Double.NEGATIVE_INFINITY;//Double.MIN_VALUE;
        boolean update = true;
        double gameValue = 0;
        LPData lpData = null;
        while (update){
            try {
                int RGSize = Arrays.toString(restrictedGameLeader).replaceAll("[^t]", "").length() * Arrays.toString(restrictedGameFollower).replaceAll("[^t]", "").length();
                System.out.println("RG size = " + RGSize);
                lpTable.watchAllPrimalVariables();
                System.out.println("Watching all primal vars.");
                LPTable.CPLEXALG = IloCplex.Algorithm.Primal;
                lpData = lpTable.toCplex();
                System.out.println("Exported to Cplex.");

                System.out.println("Solving method : " + lpData.getSolver().getParam(IloCplex.IntParam.RootAlg));
                lpData.getSolver().solve();
                System.out.println("LP status: " + lpData.getSolver().getStatus());
                if (lpData.getSolver().getStatus() == IloCplex.Status.Optimal) {
                    gameValue = lpData.getSolver().getObjValue();

                    System.out.println("-----------------------");
                    System.out.println("LP value: " + gameValue);
                    leaderUtility = gameValue;

                    followerBR = followerBestResponse(lpData);
                    for (Integer followerAction : followerBR)
                        restrictedGameFollower[followerAction] = true;
                    System.out.println("BR computed.");
//                    update = findMaxOutOfRG(leaderUtility);
                    update = findMaxOutOfRGWithCosts(lpData);
                    System.out.println("Leader max found. Update = " + update);
                    if (!update && followerBR.size() == 0) break;

                }
                else{
                    System.out.println("Solving failed");
                }
            }
            catch(Exception e){
                e.printStackTrace();
            }

        }

        System.out.println("DUALS : ");
        for (Map.Entry<Object, IloRange> entry : lpData.getWatchedDualVariables().entrySet()){
            try {
                double value = lpData.getSolver().getDual(entry.getValue());
                if (Math.abs(value) > EPS)
                    System.out.println("Con = (" + entry.getKey().toString() + "), Value = " + value);
            }
            catch(Exception e){
                System.out.println("ERR : " + entry.getKey());e.printStackTrace();}
        }

        System.out.println();
        System.out.println("PRIMALS : ");
        for (int i = 0; i < utilitiesLeader.length; i++)
            if(restrictedGameLeader[i])
            for (int j = 0; j < utilitiesLeader[0].length; j++)
                if (restrictedGameFollower[j]){
                Pair varKey = new Pair(i,j);
                try {
                    double value = lpData.getSolver().getValue(lpData.getWatchedPrimalVariables().get(varKey));
                    if (value > 0.001)
                        System.out.println("Profile = (" + i + ", " + j + "), Value = " + value);
                }
                catch(Exception e){
                    System.out.println("ERR = " + varKey);e.printStackTrace();}
            }

        System.out.println();
        System.out.println("Actions in RG : ");
        for (int leaderAction = 0; leaderAction <  utilitiesLeader.length; leaderAction++)
            if (restrictedGameLeader[leaderAction]) System.out.printf("%d ",leaderAction);
        System.out.println();
        for (int followerAction = 0; followerAction <  utilitiesLeader[0].length; followerAction++)
            if (restrictedGameFollower[followerAction]) System.out.printf("%d ",followerAction);
        System.out.println();
//        System.out.println(utilitiesLeader[5][29] + " : " + utilitiesFollower[5][29]);
        int RGSize = Arrays.toString(restrictedGameLeader).replaceAll("[^t]", "").length() * Arrays.toString(restrictedGameFollower).replaceAll("[^t]", "").length();
        System.out.println("Restricted game size : "+ RGSize+"/"+utilitiesLeader.length*utilitiesLeader[0].length);
        return gameValue;//(double)RGSize / (utilitiesLeader.length*utilitiesLeader[0].length);
    }

    public double getRGRatio(){
        int RGSize = Arrays.toString(restrictedGameLeader).replaceAll("[^t]", "").length() * Arrays.toString(restrictedGameFollower).replaceAll("[^t]", "").length();
        return (double)RGSize / (utilitiesLeader.length*utilitiesLeader[0].length);
    }

    protected void runIterative(){
        double leaderUtility = Double.MIN_VALUE;
        boolean update = true;
        while (update){
            update = findMaxOutOfRG(leaderUtility);
            if (!update) break;
            try {
                LPTable.CPLEXALG = IloCplex.Algorithm.Primal;
                LPData lpData = lpTable.toCplex();

                System.out.println("Solving method : " + lpData.getSolver().getParam(IloCplex.IntParam.RootAlg));
                lpData.getSolver().solve();
                System.out.println("LP status: " + lpData.getSolver().getStatus());
                if (lpData.getSolver().getStatus() == IloCplex.Status.Optimal) {
                    double gameValue = lpData.getSolver().getObjValue();

                    System.out.println("-----------------------");
                    System.out.println("LP value: " + gameValue);
                    leaderUtility = gameValue;

                }
                else{
                    System.out.println("Solving failed");
                }
            }
            catch(Exception e){
                e.printStackTrace();
            }

        }
        System.out.println("Restricted game size : "+ Arrays.toString(restrictedGameLeader).replaceAll("[^t]", "").length()+"/"+utilitiesLeader.length);
    }

    public double runFullGame(){
        double gameValue = 0;
        for (int i = 0; i < utilitiesLeader[0].length; i++) restrictedGameFollower[i] = true;
        for (int i = 0; i < utilitiesLeader.length; i++){
//            addActionToRG(i);
            addActionToCriterion(i);
        }
        generateFullConstraints();
        System.out.println("LP created.");
        try {

            lpTable.watchAllPrimalVariables();
            LPTable.CPLEXALG = IloCplex.Algorithm.Primal;
            LPData lpData = lpTable.toCplex();

            System.out.println("Solving method : " + lpData.getSolver().getParam(IloCplex.IntParam.RootAlg));
            lpData.getSolver().solve();
//            lpData.getSolver().exportModel("SCE.lp");
            System.out.println("LP status: " + lpData.getSolver().getStatus());
            if (lpData.getSolver().getStatus() == IloCplex.Status.Optimal) {
                gameValue = lpData.getSolver().getObjValue();

                System.out.println("-----------------------");
                System.out.println("LP value: " + gameValue);

//                lpData.getWatchedPrimalVariables().get(new P)
                for (int i = 0; i < utilitiesLeader.length; i++)
                    for (int j = 0; j < utilitiesLeader[0].length; j++){
                        Pair varKey = new Pair(i,j);
                        double value = lpData.getSolver().getValue(lpData.getWatchedPrimalVariables().get(varKey));
                        if (value > 0.001)
                            System.out.println("Profile = ("+i+", "+j+"), Value = "+value);
                    }
            }
            else{
                System.out.println("Solving failed");
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return gameValue;
    }

    protected boolean findMaxOutOfRGWithCosts(LPData lpData){
        // find max out of RG
        HashMap<Integer, Double> costs = new HashMap<>();
        for (int leaderAction = 0; leaderAction <  utilitiesLeader.length; leaderAction++)
            if (!restrictedGameLeader[leaderAction]) {
//                boolean isReachable = false;
//                for (int followerAction = 0; followerAction < utilitiesLeader[0].length; followerAction++) if ()
                for (int followerAction = 0; followerAction < utilitiesLeader[0].length; followerAction++) {
                    if (restrictedGameFollower[followerAction] && !followerBR.contains(followerAction)){
                        double fix = -1;
                        double c = fix * utilitiesLeader[leaderAction][followerAction];
                        try {
                            c -= fix * lpData.getSolver().getDual(lpData.getWatchedDualVariables().get("prob"));
                            for (int followerDeviation = 0; followerDeviation < utilitiesLeader[0].length; followerDeviation++) {
                                if (restrictedGameFollower[followerDeviation] && !followerBR.contains(followerDeviation)) {
                                    Pair eqKey = new Pair(followerAction, followerDeviation);
                                    double dual = lpData.getSolver().getDual(lpData.getWatchedDualVariables().get(eqKey));
                                    double utilityDiff = fix * (utilitiesFollower[leaderAction][followerAction] - utilitiesFollower[leaderAction][followerDeviation]);
                                    c -= utilityDiff * dual;
                                }
                            }
//                            System.out.println(leaderAction + " / "  + followerAction + " : " + c);
                            if (c < EPS){
                                if (!costs.containsKey(leaderAction) || costs.get(leaderAction) > c)
                                    costs.put(leaderAction, c);
                            }
                        }
                        catch (Exception e){e.printStackTrace();}
                    }
                }
            }
        if (costs.isEmpty()){
            if (followerBR.size() > 0){
                addActionToRG(-1);
                addActionToCriterion(-1);
            }
            return false;
        }
        else{
            ArrayList<Map.Entry<Integer, Double>> entries = new ArrayList<>(costs.entrySet());
            Collections.sort(entries,(o1, o2) -> o1.getValue().compareTo(o2.getValue()));
            for (Map.Entry<Integer, Double> entry : entries){
                System.out.println("Leader action = " + entry.getKey() + ", cost = " + entry.getValue());
                restrictedGameLeader[entry.getKey()] = true;
                addActionToRG(entry.getKey());
                // update criterion
                addActionToCriterion(entry.getKey());
                break;
            }
        }
        return true;
    }

    // considering only the actions in the followers restricted game.
    protected boolean findMaxOutOfRG(double leaderUtility){
        // find max out of RG
        double maxUtility = Double.NEGATIVE_INFINITY;//Double.MIN_VALUE;

        HashSet<Integer> maxActions = new HashSet<>();
        for (int leaderAction = 0; leaderAction <  utilitiesLeader.length; leaderAction++)
            if (!restrictedGameLeader[leaderAction]) {
//                boolean isReachable = false;
//                for (int followerAction = 0; followerAction < utilitiesLeader[0].length; followerAction++) if ()
                for (int followerAction = 0; followerAction < utilitiesLeader[0].length; followerAction++)
                    if (restrictedGameFollower[followerAction] && utilitiesLeader[leaderAction][followerAction] > maxUtility && utilitiesLeader[leaderAction][followerAction] + EPS >= leaderUtility) {
                        maxUtility = utilitiesLeader[leaderAction][followerAction];
//                        maxAction = leaderAction;
                    }
            }
        if (maxUtility <= Double.NEGATIVE_INFINITY){
            if (followerBR.size() > 0){
                addActionToRG(-1);
                addActionToCriterion(-1);
            }
            return false;
        }
//        if (!restrictedGameLeader[5] && Arrays.toString(restrictedGameLeader).replaceAll("[^t]", "").length() > 10) maxActions.add(5);

        for (int leaderAction = 0; leaderAction <  utilitiesLeader.length; leaderAction++)
            if (!restrictedGameLeader[leaderAction])
                for (int followerAction = 0; followerAction < utilitiesLeader[0].length; followerAction++)
                    if (restrictedGameFollower[followerAction] && utilitiesLeader[leaderAction][followerAction] + EPS >= maxUtility) {
                        maxActions.add(leaderAction);
                    }

        if (maxActions.size() > 0) {
//            for (int maxAction : maxActions) {
            int maxAction = maxActions.iterator().next();{
                System.out.println("Max action = " + maxAction + ", max utility = " + maxUtility);
                restrictedGameLeader[maxAction] = true;
                addActionToRG(maxAction);
                // update criterion
                addActionToCriterion(maxAction);
            }
            return true;
        }
        else{
            if (followerBR.size() > 0){
                addActionToRG(-1);
                addActionToCriterion(-1);
            }
        }
        return false;
    }

    protected void generateFullConstraints(){
        for (int followerDeviation = 0; followerDeviation < utilitiesLeader[0].length; followerDeviation++)
                for(int followerAction = 0; followerAction < utilitiesLeader[0].length; followerAction++) {
                    Pair eqKey = new Pair(followerAction, followerDeviation);
                    for (int leaderAction = 0; leaderAction < utilitiesLeader.length; leaderAction++) {
                        Pair varKey = new Pair<>(leaderAction, followerAction);
                        lpTable.setConstraint(eqKey, varKey, utilitiesFollower[leaderAction][followerAction] - utilitiesFollower[leaderAction][followerDeviation]);
                        lpTable.setLowerBound(varKey, 0.0);
                        lpTable.setUpperBound(varKey, 1.0);
                        lpTable.setConstraintType(eqKey, 2); // >=
                        lpTable.setConstant(eqKey, 0.0);
                    }
                }
    }

    protected void addActionToCriterion(int leaderBestAction){
        lpTable.watchDualVariable("prob", "prob");
        if (leaderBestAction != -1) {
            for (int followerAction = 0; followerAction < utilitiesLeader[0].length; followerAction++) {
                if (restrictedGameFollower[followerAction]) {
                    Pair varKey = new Pair<>(leaderBestAction, followerAction);
//                    System.out.println(varKey + " : " + utilitiesLeader[leaderBestAction][followerAction]);
                    lpTable.setObjective(varKey, utilitiesLeader[leaderBestAction][followerAction]);
                    lpTable.setConstraint("prob", varKey, 1.0);
                    lpTable.setConstraintType("prob", 1); // =
                    lpTable.setConstant("prob", 1.0);
                }
            }
        }
        for (int leaderAction = 0; leaderAction < utilitiesLeader.length; leaderAction++)
            if (restrictedGameLeader[leaderAction] && leaderAction!=leaderBestAction){
                for (int followerAction : followerBR){
//                for (int followerAction = 0; followerAction < utilitiesFollower[0].length; followerAction++)
//                    if (restrictedGameFollower[followerAction] && !followerBR.contains(followerAction)){
                        Pair varKey = new Pair<>(leaderAction, followerAction);
//                        System.out.println(varKey + " : " + utilitiesLeader[leaderAction][followerAction]);
                        lpTable.setObjective(varKey, utilitiesLeader[leaderAction][followerAction]);
                        lpTable.setConstraint("prob", varKey, 1.0);
                        lpTable.setConstraintType("prob", 1); // =
                        lpTable.setConstant("prob", 1.0);
                    }
            }
    }

    protected void addActionToRG(int leaderBestAction){
        if (leaderBestAction != -1) {
            for (int followerDeviation = 0; followerDeviation < utilitiesLeader[0].length; followerDeviation++)
                if (restrictedGameFollower[followerDeviation])
                    for (int followerAction = 0; followerAction < utilitiesLeader[0].length; followerAction++) {
                        if (restrictedGameFollower[followerAction]) {
                            Pair eqKey = new Pair<>(followerAction, followerDeviation);
                            lpTable.watchDualVariable(eqKey, eqKey);
                            Pair varKey = new Pair<>(leaderBestAction, followerAction);
                            lpTable.setConstraint(eqKey, varKey, utilitiesFollower[leaderBestAction][followerAction] - utilitiesFollower[leaderBestAction][followerDeviation]);
                            lpTable.setLowerBound(varKey, 0.0);
                            lpTable.setUpperBound(varKey, 1.0);
                            lpTable.setConstraintType(eqKey, 2); // >=
                            lpTable.setConstant(eqKey, 0.0);
                        }
                    }
        }
        for(int followerAction = 0; followerAction < utilitiesLeader[0].length; followerAction++) {
            if (restrictedGameFollower[followerAction]) {
                for (int followerBRAction : followerBR) {
                    Pair eqKey1 = new Pair(followerAction, followerBRAction);
                    Pair eqKey2 = new Pair(followerBRAction, followerAction);
                    lpTable.watchDualVariable(eqKey1, eqKey1);
                    lpTable.watchDualVariable(eqKey2, eqKey2);
                    for (int leaderAction = 0; leaderAction < utilitiesLeader.length; leaderAction++) {
                        if (restrictedGameLeader[leaderAction]) {
                            Pair varKey1 = new Pair(leaderAction, followerAction);
                            Pair varKey2 = new Pair(leaderAction, followerBRAction);
                            lpTable.setConstraint(eqKey1, varKey1, utilitiesFollower[leaderAction][followerAction] - utilitiesFollower[leaderAction][followerBRAction]);
                            lpTable.setConstraint(eqKey2, varKey2, utilitiesFollower[leaderAction][followerBRAction] - utilitiesFollower[leaderAction][followerAction]);
                            lpTable.setLowerBound(varKey1, 0.0);
                            lpTable.setUpperBound(varKey1, 1.0);
                            lpTable.setLowerBound(varKey2, 0.0);
                            lpTable.setUpperBound(varKey2, 1.0);
                            lpTable.setConstraintType(eqKey1, 2); // >=
                            lpTable.setConstant(eqKey1, 0.0);
                            lpTable.setConstraintType(eqKey2, 2); // >=
                            lpTable.setConstant(eqKey2, 0.0);
                        }
                    }
                }
            }
        }
//        for (int followerDeviation = 0; followerDeviation < utilitiesLeader[0].length; followerDeviation++)
////            if (restrictedGameLeader[leaderAction])
//                for(int followerAction = 0; followerAction < utilitiesLeader[0].length; followerAction++){
//                        // u(l,f)lambda(l,f) - u(l,a)lambda(l,f) >= 0
//                    Triplet eqKey = new Triplet<>(followerDeviation, followerAction, actionIdx);
//                    Pair varKey = new Pair<>(actionIdx, followerAction);
//                    lpTable.setConstraint(eqKey, varKey, utilitiesFollower[actionIdx][followerAction] - utilitiesFollower[actionIdx][followerDeviation]);
//                    lpTable.setLowerBound(varKey, 0.0);
//                    lpTable.setUpperBound(varKey, 1.0);
//                    lpTable.setConstraintType(eqKey, 2); // >=
//                    lpTable.setConstant(eqKey, 0.0);
//                }
    }

    // TODO : tady to nebude fungovat, musim zavest tu plnou kontrolu pres sumu !
    protected ArrayList<Integer> followerBestResponse(LPData lpData){ // check if other constraints hold -> otherwise add
        double[][] correlations = new double[utilitiesLeader.length][utilitiesLeader[0].length];
        for (int followerAction = 0 ; followerAction < utilitiesLeader[0].length; followerAction++)
            if (restrictedGameFollower[followerAction])
                for (int leaderAction = 0; leaderAction < utilitiesLeader.length; leaderAction++)
                    if (restrictedGameLeader[leaderAction]) {
                        Pair varKey = new Pair(leaderAction,followerAction);
//                        System.out.println(lpData.getWatchedPrimalVariables().get(varKey));
                        try {
//                            System.out.println(varKey);
                            correlations[leaderAction][followerAction] = lpData.getSolver().getValue(lpData.getWatchedPrimalVariables().get(varKey));
                        }
                        catch (Exception e){e.printStackTrace(); System.exit(0);}
                    }
        HashSet<Integer> unsatisfied = new HashSet<>();
        for (int followerDeviation = 0; followerDeviation < utilitiesLeader[0].length; followerDeviation++)
            if (!restrictedGameFollower[followerDeviation])
                for (int followerAction = 0 ; followerAction < utilitiesLeader[0].length; followerAction++)
                    if (restrictedGameFollower[followerAction]) {
                        double sum = 0.0;
                        for (int leaderAction = 0; leaderAction < utilitiesLeader.length; leaderAction++)
                            if (restrictedGameLeader[leaderAction]) {
                                sum += correlations[leaderAction][followerAction]*(utilitiesFollower[leaderAction][followerAction] - utilitiesFollower[leaderAction][followerDeviation]);
//                                Pair varKey = new Pair(leaderAction,followerAction);
//                                try {
//                                    // nestacila by mi ostra nerovnost ?
//                                    double correlation = lpData.getSolver().getValue(lpData.getWatchedPrimalVariables().get(varKey));
//                                    if (correlation > 0.0001 & utilitiesFollower[leaderAction][followerDeviation] + EPS >= utilitiesFollower[leaderAction][followerAction])
//                                        unsatisfied.add(followerDeviation);
//                                } catch (Exception e) {
//                                    e.printStackTrace();
//                                    System.exit(0);
//                                }

                            }
                        if (sum + EPS >= 0) unsatisfied.add(followerDeviation);
                    }
        return  new ArrayList<>(unsatisfied);
    }

    protected void readNFGFile(String fileName){
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            String thisLine = null;
            String nString = null;
            int leaderActions, followerActions;

            br.readLine(); br.readLine();
            nString = br.readLine();
            leaderActions = (nString.split("\"", -1).length - 1) / 2;
            nString = br.readLine();
            followerActions = (nString.split("\"", -1).length - 1) / 2;
            br.readLine(); br.readLine(); br.readLine();

            utilitiesLeader = new double[leaderActions][followerActions];
            utilitiesFollower = new double[leaderActions][followerActions];

            for (int i = 0; i < followerActions; i++)
                for (int j = 0; j < leaderActions; j++){
                    nString = br.readLine();
                    utilitiesLeader[j][i] = Double.parseDouble(nString.split(" ",-1)[0]);
                    utilitiesFollower[j][i] = Double.parseDouble(nString.split(" ",-1)[1]);
//                    System.out.println(utilitiesLeader[j][i] + " / " + utilitiesFollower[j][i]);
                }
            br.close();
        } catch (IOException e) {
            System.err.println("Error: " + e);
        }
    }

}
