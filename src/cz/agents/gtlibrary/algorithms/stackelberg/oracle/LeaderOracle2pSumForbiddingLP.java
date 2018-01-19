package cz.agents.gtlibrary.algorithms.stackelberg.oracle;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.ColumnGenerationLPTable;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPData;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPTable;
import cz.agents.gtlibrary.algorithms.stackelberg.StackelbergConfig;
import cz.agents.gtlibrary.algorithms.stackelberg.correlated.twoplayer.iterative.LeaderGenerationTwoPlayerSefceLP;
import cz.agents.gtlibrary.iinodes.ArrayListSequenceImpl;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.Pair;
import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

import java.util.*;

/**
 * Created by Jakub Cerny on 20/09/2017.
 */
public class LeaderOracle2pSumForbiddingLP extends LeaderGenerationTwoPlayerSefceLP {

    protected int bnbBranchingCount = 0;
    protected int generationCount = 0;
    protected HashMap<Object,Integer> restrictions;

    protected boolean MEASURE_TIME = true;
    protected boolean GENERATION_BEFORE_FIXING = true;

    protected long deviationIdentificationTime = 0;
    protected long brokenStrategyIdentificationTime = 0;
    protected long restrictedGameGenerationTime = 0;

    protected double eps_;
//    private long

    public LeaderOracle2pSumForbiddingLP(Player leader, GameInfo info) {
        super(leader, info);
        restrictions =  new HashMap<>();
//        lpTable = new RecyclingLPTable();
//        System.out.println(lpTable.getClass().getCanonicalName());
//        System.exit(0);
    }

    public LeaderOracle2pSumForbiddingLP(Player leader, GameInfo info, boolean useColumnGenTable) {
        super(leader, info);
        restrictions =  new HashMap<>();
        if (useColumnGenTable) lpTable = new ColumnGenerationLPTable();
        else
            lpTable = new LPTable();
//        lpTable = new RecyclingLPTable();
//        System.out.println(lpTable.getClass().getCanonicalName());
//        System.exit(0);
    }

    public LeaderOracle2pSumForbiddingLP(Player leader, GameInfo info, boolean useColumnGenTable, boolean sequentialGeneration) {
        super(leader, info);
        restrictions =  new HashMap<>();
        if (useColumnGenTable) lpTable = new ColumnGenerationLPTable();
        else
            lpTable = new LPTable();
        GENERATION_BEFORE_FIXING = sequentialGeneration;
//        eps = 1e-8;
//        lpTable = new RecyclingLPTable();
//        System.out.println(lpTable.getClass().getCanonicalName());
//        System.exit(0);
    }

    public LeaderOracle2pSumForbiddingLP(Player leader, GameInfo info, boolean useColumnGenTable, boolean sequentialGeneration, boolean addAllPrefixes) {
        super(leader, info);
        restrictions =  new HashMap<>();
        if (useColumnGenTable) lpTable = new ColumnGenerationLPTable();
        else
            lpTable = new LPTable();
        GENERATION_BEFORE_FIXING = sequentialGeneration;
        ADD_ALL_PREFIXES = addAllPrefixes;
//        eps = 1e-8;
//        lpTable = new RecyclingLPTable();
//        System.out.println(lpTable.getClass().getCanonicalName());
//        System.exit(0);
    }


    @Override
    public double calculateLeaderStrategies(StackelbergConfig algConfig, Expander<SequenceInformationSet> expander) {
        this.algConfig = algConfig;
        this.expander = expander;
//        followerBestResponse = new FollowerBestResponse(algConfig.getRootState(), expander, algConfig, leader, follower);
        long startTime = threadBean.getCurrentThreadCpuTime();

        generateRelevantSequences();

        // construct initial LP
        generateFollowerConstraints();

//        System.out.println("////////////////////////////////////");
//        System.out.println("          RELEVANT SEQUENCES");
//        System.out.println("////////////////////////////////////");
//        for (Sequence seq : relevantForLeaderInP.keySet()){
//            System.out.println(seq);
//            for (Sequence s : relevantForLeaderInP.get(seq))
//                System.out.println("    " + s);
//        }
//        System.out.println("////////////////////////////////////");


        HashSet<Sequence> leaderSequences = findLeaderInitialRG();
        addLeaderSequencesToLP(leaderSequences);

//        System.out.println("LP build...");
//        lpTable.watchAllPrimalVariables();
        overallConstraintGenerationTime += threadBean.getCurrentThreadCpuTime() - startTime;
        Pair<Map<Sequence, Double>, Double> result = solve(-info.getMaxUtility(), info.getMaxUtility());

        resultStrategies.put(leader, result.getLeft());
        resultValues.put(leader, result.getRight());
        System.out.println("RG size: " +leaderRG.size()+ "/"+((StackelbergConfig) algConfig).getSequencesFor(leader).size());
        gameValue = result.getRight();
        return result.getRight();
    }

    protected LPData solveForSefce(HashSet<Sequence> leaderSequences){
        addLeaderSequencesToLP(leaderSequences);

        boolean updated = true;
        long startTime;
        LPData lpData = null;
//        double value = 0.0;
//        int iteration = 0;
        while(updated){
            // solve LP
            try {
                generationCount++;
                System.out.println("-----------------------");
                System.out.println("Iteration "+generationCount);
                System.out.printf("Watching...");
                lpTable.watchAllDualVariables();
                lpTable.watchAllPrimalVariables();
                System.out.println("done.");
                startTime = threadBean.getCurrentThreadCpuTime();
                System.out.printf("Generating cplex...");
                lpData = lpTable.toCplex();
                System.out.println("done.");
                overallConstraintGenerationTime += threadBean.getCurrentThreadCpuTime() - startTime;
//                lpData.getSolver().exportModel("Iter2pSEFCE_" +iteration + ".lp");
                startTime = threadBean.getCurrentThreadCpuTime();
                System.out.printf("Solving...");
                lpData.getSolver().solve();
                System.out.println("done.");
                overallConstraintLPSolvingTime += threadBean.getCurrentThreadCpuTime() - startTime;
                if (lpData.getSolver().getStatus() == IloCplex.Status.Optimal) {
                    gameValue = lpData.getSolver().getObjValue();
                    System.out.println("LP reward: " + gameValue );
                } else {
//                    lpData.getSolver().getConflict();
                    System.err.println(lpData.getSolver().getStatus());
                }
            } catch (IloException e) {
                e.printStackTrace();
            }

            // calculate reduced costs -> find deviation
            startTime = threadBean.getCurrentThreadCpuTime();
            System.out.printf("Finding deviations...");
            leaderSequences = findLeaderDeviation(lpData);
            System.out.println("done.");
            deviationIdentificationTime += threadBean.getCurrentThreadCpuTime() - startTime;
            // update LP
            if (leaderSequences.isEmpty())
                updated = false;
            else {
                startTime = threadBean.getCurrentThreadCpuTime();
                System.out.printf("Adding new sequences...");
                addLeaderSequencesToLP(leaderSequences);
                System.out.println("done.");
                restrictedGameGenerationTime += threadBean.getCurrentThreadCpuTime() - startTime;
            }
            System.out.println("RG size: " +leaderRG.size()+ "/"+((StackelbergConfig) algConfig).getSequencesFor(leader).size());
        }
        return  lpData;
    }

//    @Override
    protected Pair<Map<Sequence, Double>, Double> solve(double lowerBound, double upperBound) {
        try {
            long startTime = threadBean.getCurrentThreadCpuTime();
            System.out.println("-----------------------");
            System.out.println("Invocation: "+ bnbBranchingCount);

//            updateLowerBound(lowerBound);
            System.out.printf("Watching...");
            lpTable.watchAllDualVariables();
            lpTable.watchAllPrimalVariables();
            System.out.println("done.");
            LPData lpData = lpTable.toCplex();

            overallConstraintGenerationTime += threadBean.getCurrentThreadCpuTime() - startTime;
//            lpData.getSolver().exportModel("SSEIter.lp");
            startTime = threadBean.getCurrentThreadCpuTime();
            System.out.printf("Solving...");
            lpData.getSolver().solve();
            System.out.println("done.");
            bnbBranchingCount++;
            overallConstraintLPSolvingTime += threadBean.getCurrentThreadCpuTime() - startTime;
//            printBinaryVariableValues(lpData);
            if (lpData.getSolver().getStatus() == IloCplex.Status.Optimal) {
                double value = lpData.getSolver().getObjValue();

                System.out.println("LP reward: " + value + " lower bound: " + lowerBound);
//                System.out.println("n it: " + lpData.getSolver().getNiterations());
//                System.out.println("n nodes: " + lpData.getSolver().getNnodes());
//                for (Map.Entry<Object, IloNumVar> entry : lpData.getWatchedPrimalVariables().entrySet()) {
//                    if (entry.getKey() instanceof Pair && ((Pair) entry.getKey()).getLeft() instanceof Sequence && ((Pair) entry.getKey()).getRight() instanceof Sequence) {
//                        double variableValue = lpData.getSolver().getValue(entry.getValue());
//
//                        if (variableValue != 0)
//                            System.out.println(entry.getKey() + ": " + variableValue);
//                    }
//                }
//                System.out.println("RG:");
//                for (Sequence seq : leaderRG) System.out.println(seq);
//                System.out.println("///////////////");
//                System.out.println("Primal vars:");
//                for (Object var : lpData.getWatchedPrimalVariables().keySet())
//                    if (var instanceof Pair) System.out.println(var);

                startTime = threadBean.getCurrentThreadCpuTime();
                System.out.printf("Finding deviations....");
                HashSet<Sequence> leaderSequences = findLeaderDeviation(lpData);
                System.out.println("done.");
                deviationIdentificationTime += threadBean.getCurrentThreadCpuTime() - startTime;

                if (GENERATION_BEFORE_FIXING && !leaderSequences.isEmpty()) {
                    lpData = solveForSefce(leaderSequences);
                    value = lpData.getSolver().getObjValue();
                    leaderSequences.clear();
                }

                startTime = threadBean.getCurrentThreadCpuTime();
                System.out.printf("Finding broken strategy...");
                Map<InformationSet, Map<Sequence, Double>> followerBehavStrat = getBehavioralStrategy(lpData, follower);
                Iterable<Sequence> brokenStrategyCauses = getBrokenStrategyCauses(followerBehavStrat, lpData);
                System.out.println("done.");
//                System.out.println("BSC: " + brokenStrategyCauses);
                brokenStrategyIdentificationTime += threadBean.getCurrentThreadCpuTime() - startTime;
//                Map<Sequence, Double> leaderRealPlan = behavioralToRealizationPlan(getLeaderBehavioralStrategy(lpData, leader));

//                GenSumUtilityCalculator calculator = new GenSumUtilityCalculator(algConfig.getRootState(), expander);
//
//                System.out.println(Arrays.toString(calculator.computeUtility(getP1Strategy(leaderRealPlan, followerRealPlan), getP2Strategy(leaderRealPlan, followerRealPlan))));
//                System.out.println("follower behav. strat.");
//                for (Map.Entry<InformationSet, Map<Sequence, Double>> entry : followerBehavStrat.entrySet()) {
//                    System.out.println(entry);
//                }




                if (brokenStrategyCauses == null) {
                    lpData = null;
//                    System.out.println("Found solution candidate with reward: " + reward);
//                    Map<Sequence, Double> leaderRealPlan = behavioralToRealizationPlan(getBehavioralStrategy(lpData, leader));
//                    Map<Sequence, Double> followerRealPlan = behavioralToRealizationPlan(getBehavioralStrategy(lpData, follower));
////
//                    System.out.println("Leader real. plan:");
//                    for (Map.Entry<Sequence, Double> entry : leaderRealPlan.entrySet()) {
//                        System.out.println(entry);
//                    }
//                    System.out.println("Follower real. plan:");
//                    for (Map.Entry<Sequence, Double> entry : followerRealPlan.entrySet()) {
//                        System.out.println(entry);
//                    }
                    if (leaderSequences.isEmpty()) {
//                        Map<Sequence, Double> leaderRealPlan = behavioralToRealizationPlan(getLeaderBehavioralStrategy(lpData, leader));
                        return new Pair<Map<Sequence, Double>, Double>(new HashMap<>(), value);
                    }
                    else{
                        startTime = threadBean.getCurrentThreadCpuTime();
                        addLeaderSequencesToLP(leaderSequences);
                        restrictedGameGenerationTime += threadBean.getCurrentThreadCpuTime() - startTime;
                        leaderSequences = null;
                        return solve(lowerBound, upperBound);
                    }
                } else {
                    if (value <= lowerBound + eps && leaderSequences.isEmpty()) {
                        System.out.println("***********lower bound " + lowerBound + " not exceeded, cutting***********");
                        return dummyResult;
                    }
                    return handleBrokenStrategyCause(lowerBound, upperBound, lpData, value, brokenStrategyCauses);
                }
            } else {
                System.err.println(lpData.getSolver().getStatus());
            }
        } catch (IloException e) {
            e.printStackTrace();
        }
        return dummyResult;
    }

    protected Iterable<Sequence> getBrokenStrategyCauses(Map<InformationSet, Map<Sequence, Double>> strategy, LPData lpData) {
        Map<Sequence, Double> shallowestBrokenStrategyCause = null;

        for (Map.Entry<InformationSet, Map<Sequence, Double>> isStrategy : strategy.entrySet()) {
            if (isStrategy.getValue().size() > 1) {
                if (shallowestBrokenStrategyCause == null) {
                    shallowestBrokenStrategyCause = new HashMap<>(isStrategy.getValue());
                } else {
                    Sequence candidate = isStrategy.getValue().keySet().iterator().next();
                    Sequence bestSoFar = shallowestBrokenStrategyCause.keySet().iterator().next();

                    if (candidate.size() < bestSoFar.size()) {
                        shallowestBrokenStrategyCause = new HashMap<>(isStrategy.getValue());
                    }
                }
            }
        }
        if (shallowestBrokenStrategyCause == null)
            return null;
        return sort(shallowestBrokenStrategyCause, shallowestBrokenStrategyCause.keySet());
    }

    protected Iterable<Sequence> sort(final Map<Sequence, Double> shallowestBrokenStrategyCause, final Collection<Sequence> allSeq) {
        List<Sequence> list = new ArrayList<>(allSeq);

        Collections.sort(list, new Comparator<Sequence>() {
            @Override
            public int compare(Sequence o1, Sequence o2) {
                return Double.compare(shallowestBrokenStrategyCause.get(o1), shallowestBrokenStrategyCause.get(o2));
            }
        });
        return list;
    }

    protected Pair<Map<Sequence, Double>, Double> handleBrokenStrategyCause(double lowerBound, double upperBound, LPData lpData, double value, Iterable<Sequence> brokenStrategyCauses) {
        Pair<Map<Sequence, Double>, Double> currentBest = dummyResult;

        for (Sequence brokenStrategyCause : brokenStrategyCauses) {
            restrictFollowerPlay(brokenStrategyCause, brokenStrategyCauses, lpData);
            Pair<Map<Sequence, Double>, Double> result = solve(getLowerBound(lowerBound, currentBest), upperBound);

            if (result.getRight() > currentBest.getRight()) {
                currentBest = result;
                if (currentBest.getRight() >= value - eps) {
                    System.out.println("----------------currentBest " + currentBest.getRight() + " reached parent reward " + value + "----------------");
                    return currentBest;
                }
            }
            removeRestriction(brokenStrategyCause, brokenStrategyCauses, lpData);
        }
        return currentBest;
    }

    protected double getLowerBound(double lowerBound, Pair<Map<Sequence, Double>, Double> currentBest) {
        return Math.max(lowerBound, currentBest.getRight());
    }


    protected void restrictFollowerPlay(Sequence brokenStrategyCause, Iterable<Sequence> brokenStrategyCauses, LPData lpData) {
        System.out.println(brokenStrategyCause + " fixed to zero");
        for (Object varKey : lpData.getWatchedPrimalVariables().keySet()) {
            if (varKey instanceof Pair) {
                if (((Pair) varKey).getLeft() instanceof Sequence && ((Pair) varKey).getRight() instanceof Sequence) {
                    Pair<Sequence, Sequence> p = (Pair<Sequence, Sequence>) varKey;

                    if (p.getRight().equals(brokenStrategyCause)) {
                        if (!restrictions.containsKey(varKey)) restrictions.put(varKey, 0);
                        Pair<String, Pair<Sequence, Sequence>> eqKey = new Pair<>("restr_"+restrictions.get(varKey), p);

//                        lpTable.setConstraint(eqKey, p, 1);
                        setNewConstraint(eqKey, p, 1);
                        lpTable.setConstraintType(eqKey, 1);
                    }
                }
            }
        }
    }

    protected void removeRestriction(Sequence brokenStrategyCause, Iterable<Sequence> brokenStrategyCauses, LPData lpData) {
        System.out.println(brokenStrategyCause + " released");
        for (Object varKey : lpData.getWatchedPrimalVariables().keySet()) {
            if (varKey instanceof Pair) {
                if (((Pair) varKey).getLeft() instanceof Sequence && ((Pair) varKey).getRight() instanceof Sequence) {
                    Pair<Sequence, Sequence> p = (Pair<Sequence, Sequence>) varKey;

                    if (p.getRight().equals(brokenStrategyCause)) {
                        int key = restrictions.get(varKey);
                        Pair<String, Pair<Sequence, Sequence>> eqKey = new Pair<>("restr_"+key, p);
                        restrictions.replace(varKey, key+1);

//                        lpTable.removeFromConstraint(eqKey, p);
                        removeFromConstraint(eqKey, p);
//                        lpTable.removeConstant(eqKey);
                        lpTable.setConstant(eqKey, 0.0);
                    }
                }
            }
        }
    }

    protected void removeFromConstraint(Object eqKey, Object varKey){

        if (lpTable instanceof ColumnGenerationLPTable)
            setNewConstraint(eqKey, new Pair("neg", varKey), -1);
        else{
            lpTable.removeFromConstraint(eqKey, varKey);
        }
    }

//    protected Map<InformationSet, Map<Sequence, Double>> getLeaderBehavioralStrategy(LPData lpData, Player player) {
//        Map<InformationSet, Map<Sequence, Double>> strategy = new HashMap<>();
////        Map<Pair<Sequence, Sequence>, Double> normalProcessedPairs = new HashMap<>();
////        Map<Pair<Sequence, Sequence>, Double> additionalProcessedPairs = new HashMap<>();
//
//        for (Map.Entry<Object, IloNumVar> entry : lpData.getWatchedPrimalVariables().entrySet()) {
//            if (entry.getKey() instanceof Pair) {
//                Pair varKey = (Pair) entry.getKey();
//
//                if (varKey.getLeft() instanceof Sequence && varKey.getRight() instanceof Sequence) {
//                    Sequence playerSequence = player.equals(leader) ? (Sequence) varKey.getLeft() : (Sequence) varKey.getRight();
//                    Map<Sequence, Double> isStrategy = strategy.get(playerSequence.getLastInformationSet());
//                    Double currentValue = getValueFromCplex(lpData, entry);
//
//                    if (currentValue > eps)
//                        if (isRelevantTo(player.equals(leader) ? (Sequence) varKey.getRight() : (Sequence) varKey.getLeft(), playerSequence.getLastInformationSet())) {
//                            if (isStrategy == null) {
//                                if (currentValue > eps) {
//                                    isStrategy = new HashMap<>();
//                                    double behavioralStrat = getBehavioralStrategy(lpData, varKey, playerSequence, currentValue);
//
//                                    isStrategy.put(playerSequence, behavioralStrat);
//                                    strategy.put(playerSequence.getLastInformationSet(), isStrategy);
//                                }
//                            } else {
//                                double behavioralStrategy = getBehavioralStrategy(lpData, varKey, playerSequence, currentValue);
//
//                                if (behavioralStrategy > eps) {
//                                    isStrategy.put(playerSequence, behavioralStrategy);
//                                }
//                            }
////                            if (isSequenceFrom(player.equals(leader) ? (Sequence) varKey.getRight() : (Sequence) varKey.getLeft(), playerSequence.getLastInformationSet(), followerRealPlan)) {
////                                try {
////                                    normalProcessedPairs.put(varKey, lpData.getSolver().getValue(entry.getValue()));
////                                } catch (IloException e) {
////                                    e.printStackTrace();
////                                }
////                            } else {
////                                try {
////                                    additionalProcessedPairs.put(varKey, lpData.getSolver().getValue(entry.getValue()));
////                                } catch (IloException e) {
////                                    e.printStackTrace();
////                                }
////                            }
//                        }
//                }
//            }
//        }
//        return strategy;
//    }

    protected boolean isRelevantTo(Sequence sequence, InformationSet informationSet) {
        if (informationSet == null)
            return sequence.isEmpty();
        assert !sequence.getPlayer().equals(informationSet.getPlayer());
        for (GameState gameState : informationSet.getAllStates()) {
            if (gameState.getSequenceFor(sequence.getPlayer()).equals(sequence))
                return true;
        }
        for (GameState gameState : informationSet.getAllStates()) {
            if (sequence.isPrefixOf(gameState.getSequenceFor(sequence.getPlayer())))
                return true;
        }
        return false;
    }


    protected double getBehavioralStrategy(LPData lpData, Pair varKey, Sequence playerSequence, Double currentValue) {
        double behavioralStrat = currentValue;

        if (!playerSequence.isEmpty()) {
            Sequence sequenceCopy = new ArrayListSequenceImpl(playerSequence);

            sequenceCopy.removeLast();
            double previousValue = getValueFromCplex(lpData, playerSequence.getPlayer().equals(leader) ? new Pair<>(sequenceCopy, varKey.getRight()) : new Pair<>(varKey.getLeft(), sequenceCopy));

            if (previousValue == 0) {
                Sequence opponentSequence = new ArrayListSequenceImpl((Sequence) (playerSequence.getPlayer().equals(leader) ? varKey.getRight() : varKey.getLeft()));

                if (!opponentSequence.isEmpty()) {
                    opponentSequence.removeLast();
                    previousValue = getValueFromCplex(lpData, playerSequence.getPlayer().equals(leader) ? new Pair<>(sequenceCopy, opponentSequence) : new Pair<>(opponentSequence, sequenceCopy));
                }
            }
            behavioralStrat /= previousValue;
        }
        return behavioralStrat;
    }

    protected Map<InformationSet, Map<Sequence, Double>> getLeaderBehavzioralStrategy(LPData lpData, Player player) {
        Map<InformationSet, Map<Sequence, Double>> strategy = new HashMap<>();
//        Map<Pair<Sequence, Sequence>, Double> normalProcessedPairs = new HashMap<>();
//        Map<Pair<Sequence, Sequence>, Double> additionalProcessedPairs = new HashMap<>();

        for (Map.Entry<Object, IloNumVar> entry : lpData.getWatchedPrimalVariables().entrySet()) {
            if (entry.getKey() instanceof Pair) {
                Pair varKey = (Pair) entry.getKey();

                if (varKey.getLeft() instanceof Sequence && varKey.getRight() instanceof Sequence) {
                    Sequence playerSequence = player.equals(leader) ? (Sequence) varKey.getLeft() : (Sequence) varKey.getRight();
                    Map<Sequence, Double> isStrategy = strategy.get(playerSequence.getLastInformationSet());
                    Double currentValue = getValueFromCplex(lpData, entry);

                    if (currentValue > eps)
                        if (isRelevantTo(player.equals(leader) ? (Sequence) varKey.getRight() : (Sequence) varKey.getLeft(), playerSequence.getLastInformationSet())) {
                            if (isStrategy == null) {
                                if (currentValue > eps) {
                                    isStrategy = new HashMap<>();
                                    double behavioralStrat = getBehavioralStrategy(lpData, varKey, playerSequence, currentValue);

                                    isStrategy.put(playerSequence, behavioralStrat);
                                    strategy.put(playerSequence.getLastInformationSet(), isStrategy);
                                }
                            } else {
                                double behavioralStrategy = getBehavioralStrategy(lpData, varKey, playerSequence, currentValue);

                                if (behavioralStrategy > eps) {
                                    isStrategy.put(playerSequence, behavioralStrategy);
                                }
                            }
//                            if (isSequenceFrom(player.equals(leader) ? (Sequence) varKey.getRight() : (Sequence) varKey.getLeft(), playerSequence.getLastInformationSet(), followerRealPlan)) {
//                                try {
//                                    normalProcessedPairs.put(varKey, lpData.getSolver().getValue(entry.getValue()));
//                                } catch (IloException e) {
//                                    e.printStackTrace();
//                                }
//                            } else {
//                                try {
//                                    additionalProcessedPairs.put(varKey, lpData.getSolver().getValue(entry.getValue()));
//                                } catch (IloException e) {
//                                    e.printStackTrace();
//                                }
//                            }
                        }
                }
            }
        }
        return strategy;
    }

    public int getBnbBranchingCount() {
        return bnbBranchingCount;
    }

    public int getGenerationCount() {
        return generationCount;
    }

    public long getDeviationIdentificationTime() {
        return deviationIdentificationTime;
    }

    public long getBrokenStrategyIdentificationTime() {
        return brokenStrategyIdentificationTime;
    }

    public long getRestrictedGameGenerationTime() {
        return restrictedGameGenerationTime;
    }
}
