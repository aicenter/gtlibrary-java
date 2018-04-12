package cz.agents.gtlibrary.algorithms.stackelberg.correlated.multiplayer.iterative;

import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPData;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPTable;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.quasiperfect.numbers.DoubleReal;
import cz.agents.gtlibrary.algorithms.stackelberg.correlated.multiplayer.CompleteSefceLP;
import cz.agents.gtlibrary.algorithms.stackelberg.correlated.multiplayer.lpTable.ConstraintGeneratingLPTable;
import cz.agents.gtlibrary.algorithms.stackelberg.correlated.strategy.PureStrategyImpl;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.nfg.PureStrategy;
import ilog.cplex.IloCplex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by Jakub Cerny on 29/08/2017.
 */
public class LeaderGenerationSefceLP extends CompleteSefceLP {

    protected HashSet<PureStrategyImpl> leaderRestrictedGame;
    protected double EPS;

    protected double restrictedGameRatio;

    protected final boolean GREEDY = true;


    public LeaderGenerationSefceLP(Player leader, GameInfo info) {
        super(leader, info);
        this.leaderRestrictedGame = new HashSet<>();
        this.lpTable = new ConstraintGeneratingLPTable();
        this.EPS = 0.0001;
    }


    protected void findInitialLeaderRestrictedGame(){
        HashSet<GameState> maxLeaves = new HashSet<>();
        double maxUtility = Double.NEGATIVE_INFINITY;
        // find leaves with maximum utility for leader
        for (GameState leaf : algConfig.getAllLeafs()){
            if (Math.abs(algConfig.getActualNonzeroUtilityValues(leaf, leader) - maxUtility) < EPS)
                maxLeaves.add(leaf);

            if (algConfig.getActualNonzeroUtilityValues(leaf, leader) > maxUtility + EPS){
                maxLeaves = new HashSet<>();
                maxLeaves.add(leaf);
                maxUtility = algConfig.getActualNonzeroUtilityValues(leaf, leader);
            }
        }

        // generate leader strategies leading to these leaves
        for (GameState leaf : maxLeaves)
            for (PureStrategyImpl strategy : agrees.get(leaf.getSequenceFor(leader))) {
                leaderRestrictedGame.add(strategy);
                setActiveProfilesWithLeaderStrategy(strategy);
            }


    }

    protected boolean checkLeaderPossibleDeviation(LPData lpData){
        System.out.printf("Checking leader's possible deviation...");
        HashSet<Object> constraints = ((ConstraintGeneratingLPTable) lpTable).getCons();
        HashMap<Object, Double> duals = new HashMap<>();
        try {
            for (Object con : constraints)
                duals.put(con, lpData.getSolver().getDual(lpData.getWatchedDualVariables().get(con)));
        }
        catch (Exception e){e.printStackTrace();}
        double minC = Double.POSITIVE_INFINITY;
        PureStrategyImpl minStrategy = null;
        for (PureStrategyImpl leaderStrategy : strategiesOfAllPlayers.get(leader.getId()))
            if (!leaderRestrictedGame.contains(leaderStrategy)){
                ArrayList<PureStrategyImpl> strategy = initStrategyProfile();
                strategy.set(leader.getId(), leaderStrategy);
                double c = generateAllProfilesWithLeaderStrategyForDeviation(duals, strategy, 0);
                if ( c < minC){
                    minStrategy = leaderStrategy;
                    minC = c;
                }

                if (GREEDY && minC < -EPS) break;
            }
        if (Math.abs(minC) < EPS || minC > EPS) {
            System.out.println("done.");
            return false;
        }
        System.out.println("done.");
        setActiveProfilesWithLeaderStrategy(minStrategy);
        leaderRestrictedGame.add(minStrategy);
        System.out.println("Strategy = " + minStrategy.toString() + "Deviation = " + minC);
        return true;
    }

    protected double generateAllProfilesWithLeaderStrategyForDeviation(HashMap<Object, Double> dualData, ArrayList profile, int playerIdx){
        if (playerIdx == info.getAllPlayers().length) {
            double c = 0;
            try {
                c = -lpTable.getObjective(profile.hashCode());
                HashMap<Object, Double> cons = ((ConstraintGeneratingLPTable) lpTable).getConstraintsWithValueFor(profile.hashCode());
                for (Map.Entry<Object, Double> entry : cons.entrySet())
                    c += entry.getValue() * dualData.get(entry.getKey());
            }
            catch (Exception e){e.printStackTrace();System.exit(0);}
            return c;
        }
        if (playerIdx == leader.getId())
            return generateAllProfilesWithLeaderStrategyForDeviation(dualData, profile, playerIdx + 1);
        else{
            double minC = Double.POSITIVE_INFINITY;
            for (PureStrategyImpl strategy : strategiesOfAllPlayers.get(playerIdx)){
                profile.set(playerIdx, strategy);
                double c = generateAllProfilesWithLeaderStrategyForDeviation(dualData, profile, playerIdx + 1);
                if (c < minC) minC = c;
                if (GREEDY && c < -EPS) return c;
            }
            return minC;
        }
    }

    protected void generateAllProfilesWithLeaderStrategy(ArrayList profile, int playerIdx){
        if (playerIdx == info.getAllPlayers().length) {
            ((ConstraintGeneratingLPTable) lpTable).addActiveVariable(profile.hashCode());
            return;
        }
        if (playerIdx == leader.getId())
            generateAllProfilesWithLeaderStrategy(profile, playerIdx + 1);
        else{
            for (PureStrategyImpl strategy : strategiesOfAllPlayers.get(playerIdx)){
                profile.set(playerIdx, strategy);
                generateAllProfilesWithLeaderStrategy(profile, playerIdx + 1);
            }
        }
    }

    protected void setActiveProfilesWithLeaderStrategy(PureStrategyImpl leaderStrategy){
        ArrayList<PureStrategyImpl> profile = initStrategyProfile();
        profile.set(leader.getId(), leaderStrategy);
        generateAllProfilesWithLeaderStrategy(profile, 0);
    }

    protected void setNonProfileVariablesAsActive(){
        for (Object var : ((ConstraintGeneratingLPTable)lpTable).getPrimalVariables()) {
//            System.out.println(var.toString());
            if (!(var instanceof Integer)) {
//                System.out.println("Setting as active " + var);
                ((ConstraintGeneratingLPTable) lpTable).addActiveVariable(var);
            }
        }
    }


    @Override
    protected double solve() {

        ((ConstraintGeneratingLPTable)lpTable).watchAllDualVariables();
        lpTable.watchAllPrimalVariables();

        boolean leaderDeviation = true;
        setNonProfileVariablesAsActive();
        findInitialLeaderRestrictedGame();

        System.out.println("Initialized.");


        while (leaderDeviation){
            try {
                long startTime = threadBean.getCurrentThreadCpuTime();

                LPTable.CPLEXALG = IloCplex.Algorithm.Primal;
                LPData lpData = lpTable.toCplex();

                overallConstraintGenerationTime += threadBean.getCurrentThreadCpuTime() - startTime;
                startTime = threadBean.getCurrentThreadCpuTime();
                System.out.println("Solving method : " + lpData.getSolver().getParam(IloCplex.IntParam.RootAlg));
                lpData.getSolver().solve();
                if (OUTPUT_LP) lpData.getSolver().exportModel("generationSEFCE_mp.lp");
                overallConstraintLPSolvingTime += threadBean.getCurrentThreadCpuTime() - startTime;
                System.out.println("LP status: " + lpData.getSolver().getStatus());
                if (lpData.getSolver().getStatus() == IloCplex.Status.Optimal) {
                    gameValue = lpData.getSolver().getObjValue();

                    System.out.println("LP value: " + gameValue);
                    System.out.println("-----------------------");
                }

                leaderDeviation = checkLeaderPossibleDeviation(lpData);

            }
            catch(Exception e){
                e.printStackTrace();
                System.exit(0);
            }

        }

        System.out.println("Number of leader strategies used : " + leaderRestrictedGame.size() + "/" + strategiesOfAllPlayers.get(leader.getId()).size());
        restrictedGameRatio = (double)leaderRestrictedGame.size() / strategiesOfAllPlayers.get(leader.getId()).size();
        return gameValue;
    }

    public double getRestrictedGameRatio(){
        return restrictedGameRatio;
    }


}
