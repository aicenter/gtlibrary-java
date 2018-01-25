package cz.agents.gtlibrary.algorithms.flipit.bayesian.iterative.gadget;

import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPData;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPTable;
import cz.agents.gtlibrary.algorithms.stackelberg.correlated.LeaderGenerationConfig;
import cz.agents.gtlibrary.algorithms.stackelberg.correlated.twoplayer.iterative.gadgets.GadgetAction;
import cz.agents.gtlibrary.algorithms.stackelberg.correlated.twoplayer.iterative.gadgets.GadgetLPTable;
import cz.agents.gtlibrary.domain.flipit.FlipItGameInfo;
import cz.agents.gtlibrary.domain.flipit.types.FollowerType;
import cz.agents.gtlibrary.iinodes.ArrayListSequenceImpl;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.Pair;
import cz.agents.gtlibrary.utils.Triplet;
import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import org.jacop.constraints.In;

import java.util.*;

/**
 * Created by Jakub Cerny on 03/01/2018.
 */
public class SFGadgetBayesianStackelbergLP extends BayesianGadgetSefceLPWithoutMiddleState {

    protected int bnbBranchingCount = 0;
    protected long brokenStrategyIdentificationTime = 0;
    protected long solvingForSefceTime = 0;

    protected int expandingAfterBnB = 0;

    protected final boolean ALL_TYPES_BNB = false;

    protected final boolean MAKE_BNB_STATS = true;
    protected HashMap<Pair, Integer> fixingInIS;

    protected Map<Object, Integer> primalWatch;

    protected Pair<Map<Sequence, Double>, Double> dummyResult = new Pair<>(null, Double.NEGATIVE_INFINITY);

    @Override
    public String getInfo(){
        return "Bayesian SSE Gadget BnB Solver";
    }

    public SFGadgetBayesianStackelbergLP(Player leader, GameInfo info) {
        super(leader, info);
        eps =1e-7;
        if(MAKE_BNB_STATS) fixingInIS = new HashMap<>();
    }

    public SFGadgetBayesianStackelbergLP(Player leader, GameInfo info, GadgetLPTable table, Map<Object, Integer> primalWatch) {
        super(leader, info);
        eps =1e-8;
//        lpTable = table;
//        this.primalWatch = primalWatch;
        lpTable.updatePrimalWatch(primalWatch);
    }

    @Override
    public double calculateLeaderStrategies(AlgorithmConfig algConfig, Expander expander) {
        this.algConfigs = new ArrayList<>();
        for (FollowerType type : FlipItGameInfo.types)
            algConfigs.add((LeaderGenerationConfig) algConfig);//new LeaderGenerationConfig(((LeaderGenerationConfig) algConfig).getRootState()));

        this.expander = expander;

        findInitialRestrictedGame();

        Pair<Map<Sequence, Double>, Double> solution = solve(-info.getMaxUtility(), info.getMaxUtility());



        gameValue = solution.getRight();
        System.out.println("final number of gadgets created: " + (gadgetsDismissed+gadgetRootsSequences.size()));

        if (MAKE_BNB_STATS){
            for (Pair key : fixingInIS.keySet())
                System.out.println(key + "\t\t : " + fixingInIS.get(key));
        }
        return gameValue;
    }


    protected Pair<Map<Sequence, Double>, Double> solve(double lowerBound, double upperBound) {
        try {
            long startTime = threadBean.getCurrentThreadCpuTime();

//            updateLowerBound(lowerBound);
//            lpTable.clearPrimalWatch();
//            lpTable.watchAllPrimalVariables();
            LPData lpData = lpTable.toCplex();

            overallConstraintGenerationTime += threadBean.getCurrentThreadCpuTime() - startTime;
//            lpData.getSolver().exportModel("BSSE_LP.lp");
            startTime = threadBean.getCurrentThreadCpuTime();
            IloCplex cplex = lpData.getSolver();
            if (EXPORT_LP) lpData.getSolver().exportModel("Gadget2pBSEFCE.lp");
            cplex.solve();
            overallConstraintLPSolvingTime += threadBean.getCurrentThreadCpuTime() - startTime;
//            printBinaryVariableValues(lpData);
            if (lpData.getSolver().getStatus() == IloCplex.Status.Optimal) {
                double value = lpData.getSolver().getObjValue();

                System.out.println("-----------------------");
                System.out.println("LP reward: " + value + " lower bound: " + lowerBound);

                startTime = threadBean.getCurrentThreadCpuTime();
                System.out.printf("Finding current sefce....");
                lpData = solveForSefce(lpData);
                value = lpData.getSolver().getObjValue();
                System.out.println("done.");
                System.out.println("LP reward after fixing sefce: " + value + " with lower bound: " + lowerBound);
                solvingForSefceTime += threadBean.getCurrentThreadCpuTime() - startTime;

                startTime = threadBean.getCurrentThreadCpuTime();
                Map<Pair<FollowerType,Sequence>,Double> causes = new HashMap<>();
//                Map<FollowerType,Map<InformationSet, Map<Sequence, Double>>> leaderStrategies = new HashMap<>();
//                Map<FollowerType,Map<InformationSet, Map<Sequence, Double>>> followersStrategies = new HashMap<>();
//                int causeDepth = Integer.MAX_VALUE;
                int supportSize = 0;
                for (FollowerType type : FlipItGameInfo.types) {
                    Map<ISKey, Map<Sequence, Double>> followerBehavStrat = getFollowerBehavioralStrategy(lpData, type);
//                    System.out.println(followerBehavStrat.size());


//                    System.out.println(type);
//                    for (ISKey set : followerBehavStrat.keySet())
//                        for (Sequence seq : followerBehavStrat.get(set).keySet())
//                            if (followerBehavStrat.get(set).get(seq) > eps)
//                                System.out.println(seq + " : " + followerBehavStrat.get(set).get(seq));

//                    followersStrategies.put(type,followerBehavStrat);
                    Map<Sequence, Double> cause = getBrokenStrategyCausesWithTypes(followerBehavStrat, lpData, type);

                    if (MAKE_BNB_STATS){
                        for(ISKey key : followerBehavStrat.keySet())
                            supportSize += followerBehavStrat.get(key).size();
                    }


                    if (cause != null){
                        if(ALL_TYPES_BNB || causes.isEmpty() || causes.keySet().iterator().next().getRight().size() > cause.keySet().iterator().next().size()) {
                            if (!ALL_TYPES_BNB) causes.clear();
                            for (Sequence sequence : cause.keySet()) {
                                causes.put(new Pair(type, sequence), cause.get(sequence));
                            }
                        }
                    }


//                    followerBrokenStrategyCauses.put(type,brokenStrategyCauses);
//                    if (brokenStrategyCauses != null) noBrokenStrategies = false;
                }

                if (MAKE_BNB_STATS){
                    System.out.println("Iteration " + bnbBranchingCount + " support size = " + supportSize);
                    if (causes!=null && !causes.isEmpty()){
                        ISKey iskey = causes.keySet().iterator().next().getRight().getLastInformationSet().getISKey();
                        FollowerType type = causes.keySet().iterator().next().getLeft();
                        Pair key = new Pair(type, iskey);
                        Integer count = fixingInIS.get(key);
                        if (count == null) count = 0;
                        fixingInIS.put(key, count+1);
                    }
                }

//                checkLeaderStrategiesConsistent(leaderStrategies);
                Iterable<Pair<FollowerType,Sequence>> brokenStrategyCauses = sortWithTypes(causes,causes.keySet());

                brokenStrategyIdentificationTime += threadBean.getCurrentThreadCpuTime() - startTime;

                if (causes.isEmpty()) {

                    System.out.println("Found solution candidate with reward: " + value);

                    return new Pair<Map<Sequence, Double>, Double>(new HashMap<Sequence, Double>(), value);
                } else {
                    if (value <= lowerBound + eps) {
                        System.out.println("***********lower bound " + lowerBound + " not exceeded, cutting***********");
                        return dummyResult;
                    }
                    return handleBrokenStrategyCause(lowerBound, upperBound, lpData, value, brokenStrategyCauses);
                }
            } else {
                System.err.println(lpData.getSolver().getStatus());
                return dummyResult;
            }
        } catch (IloException e) {
            e.printStackTrace();
        }
        return dummyResult;
    }

    private LPData solveForSefce(LPData lpData) {
        boolean reachableGadget = findReachableGadgetRoots(getThreats(lpData));
        Pair<LPData,Map<FollowerType, Map<Sequence, Double>>> solution = null;
        if (reachableGadget) expandingAfterBnB++;

        while (reachableGadget){
            iteration++;
            solution = getCurrentSolution();
            reachableGadget = findReachableGadgetRoots(solution.getRight());
        }
        return solution == null ? lpData : solution.getLeft();
    }

    protected Pair<LPData,Map<FollowerType, Map<Sequence, Double>>> getCurrentSolution(){
        try {
            long startTime = threadBean.getCurrentThreadCpuTime();

//            lpTable.watchAllPrimalVariables();
            LPData lpData = lpTable.toCplex();

            overallConstraintGenerationTime += threadBean.getCurrentThreadCpuTime() - startTime;
            if (EXPORT_LP) lpData.getSolver().exportModel("Gadget2pBSEFCE.lp");
            startTime = threadBean.getCurrentThreadCpuTime();
            if (PRINT_PROGRESS || PRINT_SOLVING) System.out.printf("Solving...");
            lpData.getSolver().solve();
            if (PRINT_PROGRESS || PRINT_SOLVING) System.out.println("done");
            overallConstraintLPSolvingTime += threadBean.getCurrentThreadCpuTime() - startTime;
            if (lpData.getSolver().getStatus() == IloCplex.Status.Optimal) {
                gameValue = lpData.getSolver().getObjValue();
//                System.out.println("-----------------------");
//                System.out.println("LP reward: " + gameValue);

                // compute RPs
                Map<FollowerType, Map<Sequence, Double>> leaderRealPlan = null;
                leaderRealPlan = getThreats(lpData);
//                leaderRealPlan = behavioralToRealizationPlan(getBehavioralStrategy(lpData, leader));

                if (leaderRealPlan != null)
                    return new Pair<>(lpData,leaderRealPlan);
                return new Pair<>(null, null);
            } else {
                System.err.println(lpData.getSolver().getStatus());
            }
        } catch (IloException e) {
            e.printStackTrace();
        }
        return new Pair<>(null, null);
    }

    protected Map<Sequence, Double> getBrokenStrategyCausesWithTypes(Map<ISKey, Map<Sequence, Double>> strategy, LPData lpData, FollowerType type) {
        Map<Sequence, Double> shallowestBrokenStrategyCause = null;

        for (Map.Entry<ISKey, Map<Sequence, Double>> isStrategy : strategy.entrySet()) {
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
        if (shallowestBrokenStrategyCause == null) {
//            System.out.println("no broken strategy");
            return null;
        }
//        System.out.println("sorting broken strategy");
        return shallowestBrokenStrategyCause;//sort(shallowestBrokenStrategyCause, shallowestBrokenStrategyCause.keySet());
    }

    protected Iterable<Pair<FollowerType, Sequence>> sortWithTypes(final Map<Pair<FollowerType,Sequence>, Double> shallowestBrokenStrategyCause, final Collection<Pair<FollowerType,Sequence>> allSeq) {
        List<Pair<FollowerType,Sequence>> list = new ArrayList<>(allSeq);

        Collections.sort(list, new Comparator<Pair<FollowerType, Sequence>>() {
            @Override
            public int compare(Pair<FollowerType, Sequence> o1, Pair<FollowerType, Sequence> o2) {
                return Double.compare(shallowestBrokenStrategyCause.get(o1), shallowestBrokenStrategyCause.get(o2));
            }
        });
        return list;
    }

    protected Map<ISKey, Map<Sequence, Double>> getFollowerBehavioralStrategy(LPData lpData, FollowerType type) {
        Map<ISKey, Map<Sequence, Double>> strategy = new HashMap<>();

//        ArrayList<String> values = new ArrayList<>();

        for (Map.Entry<Object, IloNumVar> entry : lpData.getWatchedPrimalVariables().entrySet()) {
            if (entry.getKey() instanceof Triplet) {
                Triplet varKey = (Triplet) entry.getKey();

                if (varKey.getFirst() instanceof Sequence && varKey.getSecond() instanceof Sequence && varKey.getThird() instanceof FollowerType && varKey.getThird().equals(type)) {
//                    System.out.println("Extracting strategy");
                    Sequence playerSequence = (Sequence) varKey.getSecond();
//                    System.out.println();
                    ISKey key = playerSequence.getLastInformationSet() == null ? null : playerSequence.getLastInformationSet().getISKey();
                    Map<Sequence, Double> isStrategy = strategy.get(key);
                    Double currentValue = getValueFromCplex(lpData, entry);
//                    if (currentValue > eps)  System.out.println(entry + " " + currentValue);
//                    if (currentValue > eps) values.add(entry + " : " + currentValue + " " + isSequenceFrom((Sequence) varKey.getFirst(), playerSequence.getLastInformationSet()) + " " + (playerSequence.getLastInformationSet() != null ? playerSequence.getLastInformationSet().getAllStates().size() : -1));

                    if (currentValue > eps)
                        if (isSequenceFrom((Sequence) varKey.getFirst(), playerSequence.getLastInformationSet()))
                            if (isStrategy == null) {
                                if (currentValue > eps) {
                                    isStrategy = new HashMap<>();
                                    double behavioralStrat = getFollowerBehavioralStrategy(lpData, varKey, playerSequence, currentValue);

                                    isStrategy.put(playerSequence, behavioralStrat);
                                    strategy.put(key, isStrategy);
                                }
                            } else {
                                double behavioralStrategy = getFollowerBehavioralStrategy(lpData, varKey, playerSequence, currentValue);

                                if (behavioralStrategy > eps) {
                                    isStrategy.put(playerSequence, behavioralStrategy);
                                }
                            }
                }
            }
        }
//        Collections.sort(values);
//        for (String s : values) System.out.println(s);
        return strategy;
    }

    protected double getFollowerBehavioralStrategy(LPData lpData, Triplet varKey, Sequence playerSequence, Double currentValue) {
        double behavioralStrat = currentValue;

        if (!playerSequence.isEmpty()) {
            Sequence sequenceCopy = new ArrayListSequenceImpl(playerSequence);

            sequenceCopy.removeLast();
            double previousValue = getValueFromCplex(lpData, new Triplet<>(varKey.getFirst(), sequenceCopy, varKey.getThird()));

            if (previousValue == 0) {
                Sequence opponentSequence = new ArrayListSequenceImpl((Sequence) (varKey.getFirst()));

                if (!opponentSequence.isEmpty()) {
                    opponentSequence.removeLast();
                    previousValue = getValueFromCplex(lpData, new Triplet<>(opponentSequence, sequenceCopy, varKey.getThird()));
                }
            }
            behavioralStrat /= previousValue;
        }
//        System.out.println(playerSequence.toString() + "type = " + ((FollowerType)varKey.getThird()).getID() +  " : " + behavioralStrat);
        return behavioralStrat;
    }

    protected boolean isSequenceFrom(Sequence sequence, InformationSet informationSet) {
        if (informationSet == null)
            return sequence.isEmpty();
        assert !sequence.getPlayer().equals(informationSet.getPlayer());
        for (GameState gameState : informationSet.getAllStates()) {
            if (gameState.getSequenceFor(sequence.getPlayer()).equals(sequence))
                return true;
        }
        return false;
    }

    protected Double getValueFromCplex(LPData lpData, Map.Entry<Object, IloNumVar> entry) {
        Double currentValue = null;

        try {
            currentValue = lpData.getSolver().getValue(entry.getValue());
//            if(primalWatch != null) {
////                System.out.println("check");
//                Double nextVar = lpData.getSolver().getValue(lpTable.getVars()[primalWatch.get(entry.getKey())]);
//                if (Math.abs(currentValue - nextVar) > 0.0001) {
//                    System.out.println(entry.getKey());
//                }
//            }
        } catch (IloException e) {
            System.out.println(((Pair)entry.getKey()).getLeft());
            e.printStackTrace();
            System.exit(0);
        }
        return currentValue;
    }

    protected Double getValueFromCplex(LPData lpData, Object varKey) {
        Double currentValue = null;

        try {
//            System.out.println(varKey + " #:" + varKey.hashCode());
//            System.out.println(lpData.getWatchedPrimalVariables().containsKey(varKey));
            currentValue = lpData.getSolver().getValue(lpData.getWatchedPrimalVariables().get(varKey));
//            if(primalWatch != null) {
////                System.out.println("check");
//                Double nextVar = lpData.getSolver().getValue(lpTable.getVars()[primalWatch.get(varKey)]);
//                if (Math.abs(currentValue - nextVar) > 0.0001) {
//                    System.out.println(varKey);
//                }
//                if (!lpTable.getVars()[primalWatch.get(varKey)].getName().equals(varKey.toString())){
//                    System.exit(0);
//                }
//            }
        } catch (IloException e) {
            System.out.println(varKey);
            e.printStackTrace();
        }
        return currentValue;
    }

    protected Pair<Map<Sequence, Double>, Double> handleBrokenStrategyCause(double lowerBound, double upperBound, LPData lpData, double value, Iterable<Pair<FollowerType,Sequence>> followerBrokenStrategyCauses) {
        Pair<Map<Sequence, Double>, Double> currentBest = dummyResult;

        for (Pair<FollowerType,Sequence> brokenStrategyCause : followerBrokenStrategyCauses) {
            restrictFollowerPlay(brokenStrategyCause.getRight(), lpData, brokenStrategyCause.getLeft());
            bnbBranchingCount++;
            Pair<Map<Sequence, Double>, Double> result = solve(getLowerBound(lowerBound, currentBest), upperBound);

            if (result.getRight() > currentBest.getRight()) {
                currentBest = result;
                if (currentBest.getRight()  >= value - eps) {
                    System.out.println("----------------currentBest " + currentBest.getRight() + " reached parent reward " + value + "----------------");
                    removeRestriction(brokenStrategyCause.getRight(), lpData, brokenStrategyCause.getLeft());
                    return currentBest;
                }
            }
            removeRestriction(brokenStrategyCause.getRight(), lpData, brokenStrategyCause.getLeft());
        }
        return currentBest;
    }

    protected double getLowerBound(double lowerBound, Pair<Map<Sequence, Double>, Double> currentBest) {
        return Math.max(lowerBound, currentBest.getRight());
    }

    protected void restrictFollowerPlay(Sequence brokenStrategyCause, LPData lpData, FollowerType type) {
        System.out.println(brokenStrategyCause + " of type " + type.getID() +  " fixed to zero");
        for (Object varKey : lpData.getWatchedPrimalVariables().keySet()) {
            if (varKey instanceof Triplet) {
                if (((Triplet) varKey).getFirst() instanceof Sequence && ((Triplet) varKey).getSecond() instanceof Sequence && ((Triplet) varKey).getThird() instanceof FollowerType && ((Triplet) varKey).getThird().equals(type)) {
                    Triplet<Sequence, Sequence,FollowerType> p = (Triplet<Sequence, Sequence, FollowerType>) varKey;

                    if (p.getSecond().equals(brokenStrategyCause)) {
                        Pair<String, Triplet<Sequence, Sequence, FollowerType>> eqKey = new Pair<>("restr", p);

                        lpTable.setConstraint(eqKey, p, 1);
                        lpTable.setConstraintType(eqKey, 1);

                        if (!p.getFirst().isEmpty() && p.getFirst().getLast() instanceof GadgetAction)
                            eqsToDelete.get(p.getThird()).get(((GadgetAction)p.getFirst().getLast()).getState()).add(eqKey);

                    }
                }
            }
        }
    }

    protected void removeRestriction(Sequence brokenStrategyCause, LPData lpData, FollowerType type) {
        System.out.println(brokenStrategyCause + " of type " + type.getID() +   " released");
        for (Object varKey : lpData.getWatchedPrimalVariables().keySet()) {
            if (varKey instanceof Triplet) {
                if (((Triplet) varKey).getFirst() instanceof Sequence && ((Triplet) varKey).getSecond() instanceof Sequence && ((Triplet) varKey).getThird() instanceof FollowerType && ((Triplet) varKey).getThird().equals(type)) {
                    Triplet<Sequence, Sequence,FollowerType> p = (Triplet<Sequence, Sequence, FollowerType>) varKey;

                    if (p.getSecond().equals(brokenStrategyCause)) {
                        Pair<String, Triplet<Sequence, Sequence, FollowerType>> eqKey = new Pair<>("restr", p);
                        lpTable.deleteConstraintWithoutVars(eqKey);
//                        lpTable.removeFromConstraint(eqKey, p);
//                        lpTable.setConstant(eqKey, 0.0);
                    }
                }
            }
        }
    }

    public LPTable getLpTable(){
        return lpTable;
    }

    public long getSolvingForSefceTime(){
        return solvingForSefceTime;
    }

    public long getBrokenStrategyIdentificationTime(){
        return brokenStrategyIdentificationTime;
    }

    public int getExpandingAfterBnB(){
        return expandingAfterBnB;
    }

    public int getSefceIterations(){
        return iteration;
    }

    public int getBnbBranchingCount(){
        return bnbBranchingCount;
    }


}
