package cz.agents.gtlibrary.algorithms.stackelberg.correlated.twoplayer.iterative.gadgets;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPData;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPTable;
import cz.agents.gtlibrary.algorithms.stackelberg.StackelbergConfig;
import cz.agents.gtlibrary.algorithms.stackelberg.correlated.LeaderGenerationConfig;
import cz.agents.gtlibrary.iinodes.ArrayListSequenceImpl;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.Pair;
import cz.agents.gtlibrary.utils.Triplet;
import ilog.concert.IloException;
import ilog.cplex.IloCplex;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Jakub Cerny on 04/12/2017.
 */
public class GadgetSefceLP implements Solver {

    protected HashSet<Sequence> gadgetRootsSequences;
    protected HashMap<Sequence, HashSet<GameState>> gadgetRoots;
    protected HashMap<GameState,HashMap<Object, HashSet<Object>>> varsToDelete;
    protected HashMap<GameState, HashSet<Object>> eqsToDelete;
    protected HashMap<GameState, HashSet<Object>> utilityToDelete;
    // sets for gadget vars and gadget cons

    protected double eps;
    protected GadgetLPTable lpTable;
    protected Player leader;
    protected Player follower;
    protected GameInfo info;
    protected ThreadMXBean threadBean;
    protected LeaderGenerationConfig algConfig;
    protected Expander<SequenceInformationSet> expander;
    protected int iteration;

    protected double gameValue;
    protected int gadgetsDismissed;

    protected long overallConstraintGenerationTime;
    protected long overallConstraintLPSolvingTime;
    protected long overallGadgetMakingTime;

    protected final boolean USE_PARETO_LEAVES = true;
    protected final boolean EXPORT_LP = false;

    protected final boolean FIX_LARGEST_DEVIATION_FIRST = false;
    protected final boolean TUNE_LP = false;

    protected final boolean CREATE_GADGETS = true;
    protected final boolean GENERATE_ALL_GADGETS = false;
    protected boolean DISCOUNT_GADGETS = true;
    protected final double GADGET_DISCOUNT = 1e-3;

    protected final boolean PRINT_PROGRESS = false;
    protected final boolean PRINT_SOLVING = false;

    protected final boolean MAKE_GADGET_STATS = false;
    protected ArrayList<String> gadgetStats;

    protected final double INITIAL_GADGET_DEPTH_RATIO = 0.3;
    protected final double INITIAL_GADGET_DEPTH;

    protected final boolean APPROX_HULL = true;
    protected double HULL_DELTA = 1e-2;
    protected final double DELTA_BY_UTILITY_COEF = 0.1;
    protected final boolean USE_CURRENT_LEAF_LEADER_UTILITY = true;
    protected final boolean DISTANCE_TO_PROJECTION = false;

    public GadgetSefceLP(Player leader, GameInfo info){
        this.info = info;
        this.leader = leader;
        this.follower = info.getOpponent(leader);
        this.eps = 1e-8;
        this.iteration = 0;
        this.lpTable = new GadgetLPTable();
        this.threadBean = ManagementFactory.getThreadMXBean();
        this.gadgetRootsSequences = new HashSet<>();
        this.gadgetRoots = new HashMap<>();
        this.varsToDelete = new HashMap<>();
        this.eqsToDelete = new HashMap<>();
        this.utilityToDelete = new HashMap<>();
        this.gadgetsDismissed = 0;
        if (MAKE_GADGET_STATS)
            this.gadgetStats = new ArrayList<>();
        this.INITIAL_GADGET_DEPTH = INITIAL_GADGET_DEPTH_RATIO*info.getMaxDepth()/2.0;
        this.HULL_DELTA = info.getMaxUtility() * DELTA_BY_UTILITY_COEF;
    }

    public void setLPSolvingMethod(int alg){
        LPTable.CPLEXALG = alg;
    }

    public void setEpsilonDiscounts(boolean useDiscounts){
        DISCOUNT_GADGETS = useDiscounts;
    }

    public void setEps(double eps){
        this.eps = eps;
    }

    public double getExpectedGadgetDepth(){
        if (gadgetRootsSequences.isEmpty())
            return info.getMaxDepth()/2.0;
        int size = 0;
        for (Sequence seq : gadgetRootsSequences)
            size += seq.size();
        return (double)size/gadgetRootsSequences.size();
    }

    /*
        Making roots of gadgets from a given IS.
     */
    protected void makeGadget(GameState state){
//        if (gadgetRoots.contains(set)) return;
        HashMap<Object, HashSet<Object>> varsToDeleteForState = new HashMap<>();
        varsToDelete.put(state, varsToDeleteForState);
        HashSet<Object> utilityToDeleteForState = new HashSet<>();
        utilityToDelete.put(state, utilityToDeleteForState);
        HashSet<Object> blackList = new HashSet<>();
        Sequence followerSequence = state.getSequenceFor(follower);

        // zpracuj i root !!
        createPContinuationConstraint(blackList, state.getSequenceFor(leader), followerSequence, null);
        createSequenceConstraint(algConfig, followerSequence);

        // update constraints and OBJECTIVE
        // remember which were updated so that they can be later discarded

        // 1. layer (4,5)
        Sequence leaderSequence = new ArrayListSequenceImpl(state.getSequenceForPlayerToMove());
        GadgetAction middleAction = new GadgetAction(algConfig.getInformationSetFor(state), state.getISKeyForPlayerToMove());
        leaderSequence.addLast(middleAction);
        createPContinuationConstraintInState(blackList, new ArrayList<Action>(){{add(middleAction);}}, state.getSequenceForPlayerToMove(), followerSequence, state.getISKeyForPlayerToMove());
        createPContinuationConstraint(blackList, leaderSequence, followerSequence, null);

        gadgetRootsSequences.add(leaderSequence);
        if (!gadgetRoots.containsKey(leaderSequence))
            gadgetRoots.put(leaderSequence, new HashSet<>());
        gadgetRoots.get(leaderSequence).add(state);
//        System.out.println(gadgetRoots.get(leaderSequence).size());


        // 2. layer (4,5,6,7)
        SequenceInformationSet gadgetSet = new GadgetInformationSet(state, leaderSequence);

        // 6, 7 :
        ArrayList<double[]> leavesUnder = getLeavesUnder(state);
        ArrayList<Action> actions = new ArrayList<>();
        LinkedHashSet<Sequence> outgoingSeqs = new LinkedHashSet<>();
        for (int i = 0; i < leavesUnder.size(); i++){
            leaderSequence = new ArrayListSequenceImpl(state.getSequenceForPlayerToMove());
            GadgetAction leafAction = new GadgetAction(gadgetSet, state, i);
            actions.add(new GadgetAction(gadgetSet, state, i));
            leaderSequence.addLast(middleAction);
            leaderSequence.addLast(leafAction);
            outgoingSeqs.add(leaderSequence);
            double[] u = leavesUnder.get(i);

            if (DISCOUNT_GADGETS)
                lpTable.setObjective(createSeqPairVarKey(leaderSequence, followerSequence), u[leader.getId()] - GADGET_DISCOUNT);
            else
                lpTable.setObjective(createSeqPairVarKey(leaderSequence, followerSequence), u[leader.getId()]);
            utilityToDeleteForState.add(createSeqPairVarKey(leaderSequence, followerSequence));

            if (u[follower.getId()] != 0.0){
                lpTable.setConstraint(followerSequence, createSeqPairVarKey(leaderSequence, followerSequence), -u[follower.getId()]);

                if (!varsToDeleteForState.containsKey(followerSequence))
                    varsToDeleteForState.put(followerSequence, new HashSet<>());
                varsToDeleteForState.get(followerSequence).add(createSeqPairVarKey(leaderSequence, followerSequence));


                // 7 :
                for (Action action : followerSequence) {
                    for (Sequence relevantSequence : ((SequenceInformationSet) action.getInformationSet()).getOutgoingSequences()) {
                        Object eqKey = new Triplet<>(followerSequence.getLastInformationSet().getISKey(), followerSequence, relevantSequence);
                        lpTable.setConstraint(eqKey, createSeqPairVarKeyCheckExistence(leaderSequence, relevantSequence), -u[follower.getId()]);

                        if (!varsToDeleteForState.containsKey(eqKey))
                            varsToDeleteForState.put(eqKey, new HashSet<>());
                        varsToDeleteForState.get(eqKey).add(createSeqPairVarKeyCheckExistence(leaderSequence, relevantSequence));
                    }
                }

                Object eqKey = new Triplet<>(followerSequence.getLastInformationSet().getISKey(), followerSequence, new ArrayListSequenceImpl(follower));
                lpTable.setConstraint(eqKey, createSeqPairVarKeyCheckExistence(leaderSequence, new ArrayListSequenceImpl(follower)), -u[follower.getId()]);
                if (!varsToDeleteForState.containsKey(eqKey))
                    varsToDeleteForState.put(eqKey, new HashSet<>());
                varsToDeleteForState.get(eqKey).add(createSeqPairVarKeyCheckExistence(leaderSequence, new ArrayListSequenceImpl(follower)));

            }
        }
        leaderSequence = new ArrayListSequenceImpl(state.getSequenceForPlayerToMove());
        leaderSequence.addLast(middleAction);
        createPContinuationConstraintInState(blackList, actions, leaderSequence, followerSequence, state.getISKeyForPlayerToMove());
//        System.out.println(outgoingSeqs.size() + " " + actions.size());
        for(Sequence outgoing : outgoingSeqs){
            createPContinuationConstraint(blackList, outgoing, followerSequence, outgoingSeqs);
        }

        eqsToDelete.put(state, blackList);
//        System.out.println("BS///");
//        for (Object o : blackList)
//            System.out.println(o);
//        System.out.println("BE///");

    }

    protected void findInitialRestrictedGame(){
        createInitPConstraint();
//        createSequenceConstraint(algConfig, new ArrayListSequenceImpl(follower));
        expandAfter(algConfig.getRootState());
    }

    protected void deleteOldGadgetRootConstraintsAndVariables(GameState state){
        if (state.equals(algConfig.getRootState())) return;
//        System.out.println("Deleting: " + state.hashCode() + " / " + eqsToDelete.get(state).size());
        for (Object eqKey: varsToDelete.get(state).keySet())
            for (Object varKey: varsToDelete.get(state).get(eqKey)) {
                lpTable.setConstraint(eqKey, varKey, 0);
                lpTable.deleteVar(varKey);
            }
        for (Object var : utilityToDelete.get(state)) {
            lpTable.setObjective(var, 0);
            lpTable.deleteVar(var);
        }
        for (Object eqKey : eqsToDelete.get(state)){
            lpTable.deleteConstraint(eqKey);
        }
        eqsToDelete.remove(state);
        utilityToDelete.remove(state);
        varsToDelete.remove(state);
        gadgetsDismissed++;
    }

    // Expand and generate constraints
    protected void expandAfter(GameState state){

        deleteOldGadgetRootConstraintsAndVariables(state);

        LinkedList<GameState> queue = new LinkedList<>();
        queue.add(state);
        while (queue.size() > 0) {
            GameState currentState = queue.removeFirst();

            if (isPossibleToCreateGadget(state, currentState)){
//                algConfig.addStateToSequenceForm(currentState);
                if (PRINT_PROGRESS) System.out.printf("Making gadget...");
                long startTime = threadBean.getCurrentThreadCpuTime();
                makeGadget(currentState);
                overallGadgetMakingTime += threadBean.getCurrentThreadCpuTime() - startTime;
                if (PRINT_PROGRESS) System.out.println("done.");
                continue;
            }

            if (PRINT_PROGRESS) System.out.printf("Adding to config...");
            algConfig.addStateToSequenceForm(currentState);
            if (!currentState.isGameEnd())
                algConfig.setOutgoingSequencesImmediately(currentState, expander);
            if (PRINT_PROGRESS) System.out.println("done.");

//            System.out.println(algConfig.getInformationSetFor(currentState).getOutgoingSequences().size());

            // IS action constraints and sequence constraints
            if (PRINT_PROGRESS) System.out.printf("Generating IS constraints...");
            createISActionConstraints(algConfig.getInformationSetFor(currentState));
            if (PRINT_PROGRESS) System.out.println("done.");

            // NF constraints
            if (PRINT_PROGRESS) System.out.printf("Generating NF constraints...");
            createPContinuationConstraints(currentState);
            if (PRINT_PROGRESS) System.out.println("done.");


            if (currentState.isGameEnd()) {
                final double[] utilities = currentState.getUtilities();
                Double[] u = new Double[utilities.length];

                for (Player p : currentState.getAllPlayers()){
                    if(utilities.length > p.getId())
                        u[p.getId()] = utilities[p.getId()] * currentState.getNatureProbability()*info.getUtilityStabilizer();
                }
                lpTable.setObjective(createSeqPairVarKey(currentState.getSequenceFor(leader), currentState.getSequenceFor(follower)), u[leader.getId()]);
                algConfig.setUtility(currentState, u);

                if (PRINT_PROGRESS) System.out.printf("Generating seq constrain...");
                createSequenceConstraint(algConfig, currentState.getSequenceFor(follower));
                if (PRINT_PROGRESS) System.out.println("done.");


                // add to constraints 6,7
                double utility = u[follower.getId()];
                if (utility != 0) {
                    // 6 :
                    lpTable.setConstraint(currentState.getSequenceFor(follower), createSeqPairVarKey(currentState.getSequenceFor(leader), currentState.getSequenceFor(follower)), -utility);
                    // 7 :
                    for (Action action : currentState.getSequenceFor(follower)) {
                        for (Sequence relevantSequence : ((SequenceInformationSet) action.getInformationSet()).getOutgoingSequences()) {
                            Object eqKey = new Triplet<>(currentState.getSequenceFor(follower).getLastInformationSet().getISKey(), currentState.getSequenceFor(follower), relevantSequence);
                            lpTable.setConstraint(eqKey, createSeqPairVarKeyCheckExistence(currentState.getSequenceFor(leader), relevantSequence), -utility);
                        }
                    }
                    Object eqKey = new Triplet<>(currentState.getSequenceFor(follower).getLastInformationSet().getISKey(), currentState.getSequenceFor(follower), new ArrayListSequenceImpl(follower));
//                    Object eqKey = new Triplet<>(algConfig.getInformationSetFor(currentState).getISKey(), currentState.getSequenceFor(follower), new ArrayListSequenceImpl(follower));
                    lpTable.setConstraint(eqKey, createSeqPairVarKeyCheckExistence(currentState.getSequenceFor(leader), new ArrayListSequenceImpl(follower)), -utility);
                }
                continue;
            }
            for (Action action : expander.getActions(currentState)) {
                queue.add(currentState.performAction(action));
            }
        }
    }

    protected boolean isPossibleToCreateGadget(GameState state, GameState currentState) {
        return CREATE_GADGETS && !currentState.isGameEnd()
                && !currentState.equals(state) && currentState.getPlayerToMove().equals(leader)
                && currentState.getSequenceFor(leader).size() >= INITIAL_GADGET_DEPTH;
    }

    protected boolean findReachableGadgetRoots(Map<Sequence, Double> strategy){
        boolean reachableGadget = false;
        HashSet<Sequence> currentGadgetRootSequences;
        if (!FIX_LARGEST_DEVIATION_FIRST) currentGadgetRootSequences = new HashSet<>(gadgetRootsSequences);
        else currentGadgetRootSequences = getLargestDeviation(strategy);
        HashMap<Sequence, HashSet<GameState>> currentGadgetRoots = new HashMap<>(gadgetRoots);
        for (Sequence sequence : currentGadgetRootSequences) {
            if (strategy.containsKey(sequence) && (GENERATE_ALL_GADGETS ? true : strategy.get(sequence) > eps)) {
                reachableGadget = true;
//                for (GameState setState : currentGadgetRoots.get(sequence)) {
//                    deleteOldGadgetRootConstraintsAndVariables(setState);
////                    expandAfter(setState);
////                    gadgetRoots.get(sequence).remove(setState);
//                }
                for (GameState setState : currentGadgetRoots.get(sequence)) {
//                    deleteOldGadgetRootConstraintsAndVariables(setState);
                    expandAfter(setState);
//                    gadgetRoots.get(sequence).remove(setState);
                }
                gadgetRootsSequences.remove(sequence);
                gadgetRoots.remove(sequence);
            }
        }
        return reachableGadget;
    }

    private HashSet<Sequence> getLargestDeviation(Map<Sequence, Double> strategy) {
        Sequence maxSequence = null;
        double maxDeviation = Double.NEGATIVE_INFINITY;
        for (Sequence seq : gadgetRootsSequences)
            if (strategy.get(seq) > maxDeviation){
                maxDeviation = strategy.get(seq);
                maxSequence = seq;
            }
        HashSet<Sequence> bestDeviation = new HashSet<>();
        bestDeviation.add(maxSequence);
        return bestDeviation;
    }


    @Override
    public double calculateLeaderStrategies(AlgorithmConfig algConfig, Expander expander) {
        this.algConfig = (LeaderGenerationConfig)algConfig;
        this.expander = expander;

        findInitialRestrictedGame();

        boolean reachableGadget = true;

        while (reachableGadget){
            iteration++;
            reachableGadget = findReachableGadgetRoots(solve());
        }
        if (MAKE_GADGET_STATS) writeGadgetStats();
        System.out.println("final number of gadgets created: " + (gadgetsDismissed+gadgetRootsSequences.size()));
        return gameValue;
    }

    protected void writeGadgetStats() {
        String fileName = info.getClass().getSimpleName() + "_";
        fileName += new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date()) + ".gstats";
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(fileName, "UTF-8");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        for (String s : gadgetStats)
            writer.println(s);
        writer.close();
    }

    protected Map<Sequence, Double> solve(){
        try {
            long startTime = threadBean.getCurrentThreadCpuTime();



            lpTable.watchAllPrimalVariables();
            LPData lpData = lpTable.toCplex();

            System.out.println("LP size: " + lpTable.getLPSize());

            if (TUNE_LP) tuneSolver(lpData);

            overallConstraintGenerationTime += threadBean.getCurrentThreadCpuTime() - startTime;
            if (EXPORT_LP) lpData.getSolver().exportModel("Gadget2pSEFCE.lp");
            startTime = threadBean.getCurrentThreadCpuTime();
            if (PRINT_PROGRESS || PRINT_SOLVING) System.out.printf("Solving...");
            lpData.getSolver().solve();
            if (PRINT_PROGRESS || PRINT_SOLVING) System.out.println("done");
            overallConstraintLPSolvingTime += threadBean.getCurrentThreadCpuTime() - startTime;
            if (lpData.getSolver().getStatus() == IloCplex.Status.Optimal) {
                gameValue = lpData.getSolver().getObjValue();
                System.out.println("-----------------------");
                System.out.println("LP reward: " + gameValue);

                // compute RPs
                Map<Sequence, Double> leaderRealPlan = null;
                leaderRealPlan = getThreats(lpData);
//                leaderRealPlan = behavioralToRealizationPlan(getBehavioralStrategy(lpData, leader));

                if (leaderRealPlan != null)
                    return leaderRealPlan;
                return new HashMap<Sequence, Double>();
            } else {
                System.err.println(lpData.getSolver().getStatus());
            }
        } catch (IloException e) {
            e.printStackTrace();
        }
        return new HashMap<Sequence, Double>();
    }

    protected void tuneSolver(LPData lpData) throws IloException {
        System.out.println("Tuning...");
        String tunedfile = "tuneCplex_";
        tunedfile += new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date()) + "_" +iteration+".txt";
        IloCplex.ParameterSet paramset = lpData.getSolver().getParameterSet();
        int tunestat = lpData.getSolver().tuneParam();
        if      ( tunestat == IloCplex.TuningStatus.Complete)
            System.out.println("Tuning complete.");
        else if ( tunestat == IloCplex.TuningStatus.Abort)
            System.out.println("Tuning abort.");
        else if ( tunestat == IloCplex.TuningStatus.TimeLim)
            System.out.println("Tuning time limit.");
        else
            System.out.println("Tuning status unknown.");

        if ( tunedfile != null ) {
            lpData.getSolver().writeParam(tunedfile);
                System.out.println("Tuned parameters written to file '" +
                        tunedfile + "'");
        }
        System.out.println("done.");
    }

    protected Map<Sequence, Double> getThreats(LPData lpData){
        Map<Sequence, Double> threats = new HashMap<>();
        try{
         for (Object var : lpData.getWatchedPrimalVariables().keySet()){
             if(var instanceof Pair && ((Pair)var).getLeft() instanceof Sequence){
                 Sequence seq = (Sequence)((Pair)var).getLeft();
                 double val = lpData.getSolver().getValue(lpData.getWatchedPrimalVariables().get(var));
                 if (!threats.containsKey(seq) || val > threats.get(seq))
                     threats.put(seq,val);
             }
         }
        } catch (IloCplex.UnknownObjectException e) {
            e.printStackTrace();
        } catch (IloException e) {
            e.printStackTrace();
        }
        return threats;
    }

    protected void createISActionConstraints(SequenceInformationSet informationSet) {
        if (!informationSet.getPlayer().equals(follower) || (!informationSet.getOutgoingSequences().isEmpty() && lpTable.existsEqKey(new Triplet<>(informationSet.getISKey(), informationSet.getOutgoingSequences().iterator().next(), "eq"))))
            return;

        createSequenceConstraint(algConfig, informationSet);

        // set as reachable into previous IS
        for (Action action : informationSet.getPlayersHistory()) {
            for (Sequence relevantSequence : ((SequenceInformationSet) action.getInformationSet()).getOutgoingSequences()) {
                Object eqKey = new Triplet<>(informationSet.getPlayersHistory().getLastInformationSet().getISKey(), informationSet.getPlayersHistory(), relevantSequence);
                lpTable.setConstraint(eqKey, new Pair<>(informationSet.getISKey(), relevantSequence), -1);
            }
            Object eqKey = new Triplet<>(informationSet.getPlayersHistory().getLastInformationSet().getISKey(), informationSet.getPlayersHistory(), new ArrayListSequenceImpl(follower));
            lpTable.setConstraint(eqKey, new Pair<>(informationSet.getISKey(), new ArrayListSequenceImpl(follower)), -1);
        }

//        if (informationSet.getPlayer().equals(follower)) {
            if (!informationSet.getOutgoingSequences().isEmpty()) {
                Sequence outgoingSequence = informationSet.getOutgoingSequences().iterator().next();

                for (Action action : outgoingSequence) {
                    for (Sequence relevantSequence : ((SequenceInformationSet) action.getInformationSet()).getOutgoingSequences()) {
                        createISActionConstraint(algConfig, relevantSequence, informationSet);
                    }
                }
                createISActionConstraint(algConfig, new ArrayListSequenceImpl(follower), informationSet);
            }
//        }
//        if (informationSet.getPlayer().equals(follower)) {
            for (Sequence sequence : informationSet.getOutgoingSequences()) {

//                createSequenceConstraint(algConfig, sequence);

                Object eqKey = new Triplet<>(informationSet.getISKey(), sequence, "eq");
                Object varKey = new Pair<>(informationSet.getISKey(), sequence);
                Object contVarKey = new Pair<>("v", sequence);

                lpTable.setLowerBound(varKey, Double.NEGATIVE_INFINITY);
                lpTable.setLowerBound(contVarKey, Double.NEGATIVE_INFINITY);
                lpTable.setConstraint(eqKey, varKey, 1);
                lpTable.setConstraint(eqKey, contVarKey, -1);
                lpTable.setConstraintType(eqKey, 1);
            }
//        }
    }

    protected void createISActionConstraint(StackelbergConfig algConfig, Sequence followerSequence, SequenceInformationSet informationSet) {
        for (Sequence sequence : informationSet.getOutgoingSequences()) {
            Object eqKey = new Triplet<>(informationSet.getISKey(), sequence, followerSequence);
            Object varKey = new Pair<>(informationSet.getISKey(), followerSequence);

            lpTable.setLowerBound(varKey, Double.NEGATIVE_INFINITY);
            lpTable.setConstraint(eqKey, varKey, 1);
            lpTable.setConstraintType(eqKey, 2);


            // take care of it in utilities
//            for (Sequence leaderSequence : algConfig.getCompatibleSequencesFor(sequence)) {
//                Double[] seqCombUtilities = algConfig.getGenSumSequenceCombinationUtility(leaderSequence, sequence);
//
//                if (seqCombUtilities != null) {
//                    double utility = seqCombUtilities[follower.getId()];
//
//
//                    if (utility != 0) {
////                        System.out.println(leaderSequence + " : " + followerSequence);
//                        lpTable.setConstraint(eqKey, createSeqPairVarKeyCheckExistence(leaderSequence, followerSequence), -utility);
//                    }
//                }
//            }



//            if (algConfig.getReachableSets(sequence) != null)
//                for (SequenceInformationSet reachableSet : algConfig.getReachableSets(sequence)) {
//                    if (reachableSet.getOutgoingSequences() != null && !reachableSet.getOutgoingSequences().isEmpty())
//                        lpTable.setConstraint(eqKey, new Pair<>(reachableSet, followerSequence), -1);
//                }
        }
    }

    protected void createSequenceConstraint(StackelbergConfig algConfig, SequenceInformationSet followerSet) {
//        System.out.println("Expanding v eq " + followerSet.getOutgoingSequences().size());
        Sequence followerSequence = followerSet.getPlayersHistory();
        Object varKey = new Pair<>("v", followerSequence);

//        System.out.println(followerSequence);

//        if (lpTable.existsEqKey(followerSequence)) return;

        lpTable.setLowerBound(varKey, Double.NEGATIVE_INFINITY);
        lpTable.setConstraintType(followerSequence, 1);
        lpTable.setConstraint(followerSequence, varKey, 1);

        // handled in leafs
//        for (Sequence leaderSequence : algConfig.getCompatibleSequencesFor(followerSequence)) {
//            Double[] seqCombValue = algConfig.getGenSumSequenceCombinationUtility(leaderSequence, followerSequence);
//
//            if (seqCombValue != null) {
//                double followerValue = seqCombValue[follower.getId()];
//
//                if (followerValue != 0)
//                    lpTable.setConstraint(followerSequence, createSeqPairVarKey(leaderSequence, followerSequence), -followerValue);
//            }
//        }

            for (Sequence sequence : followerSet.getOutgoingSequences()) {
                Object contVarKey = new Pair<>("v", sequence);

                lpTable.setLowerBound(contVarKey, Double.NEGATIVE_INFINITY);
                lpTable.setConstraint(followerSequence, contVarKey, -1);
            }
    }

    protected void createSequenceConstraint(StackelbergConfig algConfig, Sequence followerSequence) {
        Object varKey = new Pair<>("v", followerSequence);

//        System.out.println(followerSequence);

        lpTable.setLowerBound(varKey, Double.NEGATIVE_INFINITY);
        lpTable.setConstraintType(followerSequence, 1);
        lpTable.setConstraint(followerSequence, varKey, 1);
    }

//    protected void createSequenceConstraint(StackelbergConfig algConfig, Sequence followerSequence) {
//        Object varKey = new Pair<>("v", followerSequence);
//
//        if (lpTable.existsEqKey(followerSequence)) return;
//
//        lpTable.setLowerBound(varKey, Double.NEGATIVE_INFINITY);
//        lpTable.setConstraintType(followerSequence, 1);
//        lpTable.setConstraint(followerSequence, varKey, 1);
//        for (Sequence leaderSequence : algConfig.getCompatibleSequencesFor(followerSequence)) {
//            Double[] seqCombValue = algConfig.getGenSumSequenceCombinationUtility(leaderSequence, followerSequence);
//
//            if (seqCombValue != null) {
//                double followerValue = seqCombValue[follower.getId()];
//
//                if (followerValue != 0)
//                    lpTable.setConstraint(followerSequence, createSeqPairVarKey(leaderSequence, followerSequence), -followerValue);
//            }
//        }
//        for (SequenceInformationSet reachableSet : algConfig.getReachableSets(followerSequence)) {
//            for (Sequence sequence : reachableSet.getOutgoingSequences()) {
//                Object contVarKey = new Pair<>("v", sequence);
//
//                lpTable.setLowerBound(contVarKey, Double.NEGATIVE_INFINITY);
//                lpTable.setConstraint(followerSequence, contVarKey, -1);
//            }
//        }
//    }

    protected Pair<Sequence, Sequence> createSeqPairVarKeyCheckExistence(Sequence sequence1, Sequence sequence2) {
        Pair<Sequence, Sequence> varKey = sequence1.getPlayer().equals(leader) ? new Pair<>(sequence1, sequence2) : new Pair<>(sequence2, sequence1);

//        assert lpTable.exists(varKey);
        lpTable.watchPrimalVariable(varKey, varKey);
        lpTable.setLowerBound(varKey, 0);
        lpTable.setUpperBound(varKey, 1);
        return varKey;
    }

    protected Pair<Sequence, Sequence> createSeqPairVarKey(Sequence sequence1, Sequence sequence2) {
        Pair<Sequence, Sequence> varKey = sequence1.getPlayer().equals(leader) ? new Pair<>(sequence1, sequence2) : new Pair<>(sequence2, sequence1);

        lpTable.watchPrimalVariable(varKey, varKey);
        lpTable.setLowerBound(varKey, 0);
        lpTable.setUpperBound(varKey, 1);
        return varKey;
    }

    protected Pair<Sequence, Sequence> createSeqPairVarKey(GameState gameState) {
        return createSeqPairVarKey(gameState.getSequenceFor(leader), gameState.getSequenceFor(follower));
    }

    protected void createInitPConstraint() {
        lpTable.setConstraint("initP", createSeqPairVarKey(new ArrayListSequenceImpl(leader), new ArrayListSequenceImpl(follower)), 1);
        lpTable.setConstant("initP", 1);
        lpTable.setConstraintType("initP", 1);
    }

    protected void createPContinuationConstraints(GameState state) {
        Set<Object> blackList = new HashSet<>();
        Set<Pair<Sequence, Sequence>> pStops = new HashSet<>();

//        for (SequenceInformationSet informationSet : algConfig.getAllInformationSets().values()) {
//            List<Action> actions = expander.getActions(informationSet);
//            Player opponent = info.getOpponent(informationSet.getPlayer());
//
//            for (GameState gameState : informationSet.getAllStates()) {
//                if (!gameState.isGameEnd())
//                    createPContinuationConstraintInState(actions, opponent, gameState, blackList, pStops);
//            }
//        }

        if (!state.isGameEnd()){
            List<Action> actions = expander.getActions(state);
            Player opponent = info.getOpponent(state.getPlayerToMove());
            createPContinuationConstraintInState(actions, opponent, state, blackList, pStops);
        }

//        System.out.println(algConfig.getSequencesFor(leader));

        Sequence leaderSequence = state.getSequenceFor(leader);{
        Sequence compatibleFollowerSequence = state.getSequenceFor(follower);{
                createPContinuationConstraint(blackList, leaderSequence, compatibleFollowerSequence, null);
            }
        }
    }

    protected void createPContinuationConstraint(Set<Object> blackList, Sequence leaderSequence, Sequence compatibleFollowerSequence, LinkedHashSet<Sequence> gadgetSeqs) {
        for (Action action : compatibleFollowerSequence) {
            Sequence actionHistory = ((PerfectRecallInformationSet)action.getInformationSet()).getPlayersHistory();
            Object eqKeyFollower = new Triplet<>(leaderSequence, actionHistory, action.getInformationSet().getISKey());

            if (!blackList.contains(eqKeyFollower) && !((SequenceInformationSet) action.getInformationSet()).getOutgoingSequences().isEmpty()) {
                blackList.add(eqKeyFollower);
                Pair<Sequence, Sequence> varKey = createSeqPairVarKey(leaderSequence, actionHistory);

                lpTable.setConstraintType(eqKeyFollower, 1);
                lpTable.setConstraint(eqKeyFollower, varKey, -1);
//                if (((SequenceInformationSet) action.getInformationSet()).getOutgoingSequences().isEmpty())
//                    System.out.println(action + " : EMPTY");
                for (Sequence followerSequence : ((SequenceInformationSet) action.getInformationSet()).getOutgoingSequences()) {
                    lpTable.setConstraint(eqKeyFollower, createSeqPairVarKey(leaderSequence, followerSequence), 1);
                }
            }

            ListIterator<Action> leaderSeqIterator = leaderSequence.listIterator(leaderSequence.size());
            Action leaderAction;

            while (leaderSeqIterator.hasPrevious()) {
                leaderAction = leaderSeqIterator.previous();
                Sequence leaderHistory = getLeaderHistory(leaderSequence, leaderAction);//(PerfectRecallInformationSet)leaderAction.getInformationSet()).getPlayersHistory();
                Object eqKeyLeader = new Triplet<>(leaderHistory, actionHistory, leaderAction);

                LinkedHashSet<Sequence> outgoingSequences = getOutgoingSequences(leaderSequence, leaderAction, gadgetSeqs);
                if (!blackList.contains(eqKeyLeader) && !outgoingSequences.isEmpty()) {
                    blackList.add(eqKeyLeader);
                    Pair<Sequence, Sequence> varKey = createSeqPairVarKey(leaderHistory, actionHistory);

                    lpTable.setConstraintType(eqKeyLeader, 1);
                    lpTable.setConstraint(eqKeyLeader, varKey, -1);
                    for (Sequence leaderContinuation : outgoingSequences){//((SequenceInformationSet) leaderAction.getInformationSet()).getOutgoingSequences()) {
                        lpTable.setConstraint(eqKeyLeader, createSeqPairVarKey(leaderContinuation, actionHistory), 1);
                    }
                }

                for (Sequence followerSequence : ((SequenceInformationSet) action.getInformationSet()).getOutgoingSequences()) {
                    Object eqKeyLeaderCont = new Triplet<>(leaderHistory, followerSequence, leaderAction.getInformationSet().getISKey());

                    outgoingSequences = getOutgoingSequences(leaderSequence, leaderAction, gadgetSeqs);
                    if (!blackList.contains(eqKeyLeaderCont) && !outgoingSequences.isEmpty()) {
                        blackList.add(eqKeyLeaderCont);
                        Pair<Sequence, Sequence> varKeyCont = createSeqPairVarKey(leaderHistory, followerSequence);

                        lpTable.setConstraintType(eqKeyLeaderCont, 1);
                        lpTable.setConstraint(eqKeyLeaderCont, varKeyCont, -1);
                        for (Sequence leaderContinuation : outgoingSequences){//((SequenceInformationSet) leaderAction.getInformationSet()).getOutgoingSequences()) {
                            lpTable.setConstraint(eqKeyLeaderCont, createSeqPairVarKey(leaderContinuation, followerSequence), 1);
                        }
                    }
                }
            }
        }
    }


    protected Sequence getLeaderHistory(Sequence leaderSequence, Action leaderAction){
        if (!(leaderAction instanceof GadgetAction))
            return ((PerfectRecallInformationSet)leaderAction.getInformationSet()).getPlayersHistory();
        else{
            for (int i = 0; i < leaderSequence.size(); i++){
                if (leaderSequence.get(i).equals(leaderAction))
                    return leaderSequence.getSubSequence(i);
            }
        }
        return null;
    }

    protected LinkedHashSet<Sequence> getOutgoingSequences(Sequence leaderSequence, Action leaderAction, LinkedHashSet<Sequence> finalSet){
        if (!(leaderAction instanceof GadgetAction))
            return ((SequenceInformationSet) leaderAction.getInformationSet()).getOutgoingSequences();
        else{
            if (((GadgetAction)leaderAction).index == -1) {
                for (int i = 0; i < leaderSequence.size(); i++) {
                    if (leaderSequence.get(i).equals(leaderAction)) {
                        Sequence seq = leaderSequence.getSubSequence(i+1);
//                        System.out.println(leaderAction + " : " + seq);
                        return new LinkedHashSet<Sequence>() {{
                            add(seq);
                        }};
                    }
                }
            }
//            if (!finalSet.isEmpty())
//                System.out.println("NOT EMPTY ! ");
            return finalSet;
        }
    }


    protected void createPContinuationConstraintInState(List<Action> actions, Player opponent, GameState gameState, Set<Object> blackList, Set<Pair<Sequence, Sequence>> pStops) {
        Triplet<Sequence, Sequence, ISKey> eqKey = new Triplet<Sequence, Sequence, ISKey>(gameState.getSequenceFor(leader), gameState.getSequenceFor(follower), gameState.getISKeyForPlayerToMove());


        if (blackList.contains(eqKey))
            return;
        blackList.add(eqKey);
        Pair<Sequence, Sequence> varKey = createSeqPairVarKey(gameState);

//        if (actions.isEmpty()) System.out.println("Empty actions");

        pStops.add(varKey);
        lpTable.setConstraint(eqKey, varKey, -1);
        lpTable.setConstraintType(eqKey, 1);
        for (Action action : actions) {
            Sequence sequenceCopy = new ArrayListSequenceImpl(gameState.getSequenceForPlayerToMove());

            sequenceCopy.addLast(action);
            Pair<Sequence, Sequence> contVarKey = createSeqPairVarKey(sequenceCopy, gameState.getSequenceFor(opponent));

            pStops.add(contVarKey);
            lpTable.setConstraint(eqKey, contVarKey, 1);
        }
    }

    protected void createPContinuationConstraintInState(Set<Object> blackList, List<Action> actions, Sequence leaderSequence, Sequence followerSequence, ISKey key) {
        Triplet<Sequence, Sequence, ISKey> eqKey = new Triplet<Sequence, Sequence, ISKey>(leaderSequence, followerSequence, key);//leaderSequence.getLastInformationSet());

        if (blackList.contains(eqKey))
            return;
        blackList.add(eqKey);

        Pair<Sequence, Sequence> varKey = createSeqPairVarKey(leaderSequence, followerSequence);

//        if (actions.isEmpty()) System.out.println("Empty actions");

        lpTable.setConstraint(eqKey, varKey, -1);
        lpTable.setConstraintType(eqKey, 1);
        for (Action action : actions) {
            Sequence sequenceCopy = new ArrayListSequenceImpl(leaderSequence);

            sequenceCopy.addLast(action);
            Pair<Sequence, Sequence> contVarKey = createSeqPairVarKey(sequenceCopy, followerSequence);
            lpTable.setConstraint(eqKey, contVarKey, 1);
        }
    }



    protected ArrayList<double[]> getLeavesUnder(GameState state){
        ArrayList<double[]> leaves = new ArrayList<>();
        LinkedList<GameState> queue = new LinkedList<>();
        queue.add(state);
        while (queue.size() > 0) {
            GameState currentState = queue.removeFirst();
            if (currentState.isGameEnd()) {
                leaves.add(currentState.getUtilities());
                continue;
            }
            for (Action action : expander.getActions(currentState)) {
                queue.add(currentState.performAction(action));
            }
        }
        if (MAKE_GADGET_STATS && USE_PARETO_LEAVES && leaves.size()>1) {
            gadgetStats.add(state.getSequenceFor(leader).size() + " ");
        }
        return getUpperConvexHullOfLeaves(leaves);
    }

    protected ArrayList<double[]> getUpperConvexHullOfLeaves(ArrayList<double[]> reachableLeavesUnsorted){
        if (!USE_PARETO_LEAVES || reachableLeavesUnsorted.size() == 1) return reachableLeavesUnsorted;
        ArrayList<double[]> reachableLeaves;
        if (leader.getId() == 0) reachableLeaves = reachableLeavesUnsorted;
        else{
            reachableLeaves = new ArrayList<>();
            for(double[] u : reachableLeavesUnsorted)
                reachableLeaves.add(new double[]{u[1], u[0]});
        }

        Collections.sort(reachableLeaves, new Comparator<double[]>() {
            @Override
            public int compare(double[] o1, double[] o2) {
                if (o1[0] == o2[0])
                    return Double.compare(o1[1], o2[1]);
                else
                    return Double.compare(o1[0], o2[0]);
            }
        });

        Stack<double[]> upperHull = new Stack<>();
        int deletedPointsNumber = 0;

        for (int i = reachableLeaves.size() - 1; i >= 0; i--) {
            while (upperHull.size() >= 2 && cross(upperHull.get(upperHull.size() - 2), upperHull.get(upperHull.size() - 1), reachableLeaves.get(i)) <= 0) {
                upperHull.pop();
            }
            if(APPROX_HULL && upperHull.size() > 1){
                double[] preprevious = upperHull.elementAt(upperHull.size()-2);
                double[] previous = upperHull.peek();
                double[] current = reachableLeaves.get(i);
                double distance = 0.0;
                if (DISTANCE_TO_PROJECTION)
                    // difference in both player's utility
                    distance = Math.abs(previous[0]*(current[1]-preprevious[1])
                        - previous[1]*(current[0]-preprevious[0]) + current[0]*preprevious[1] - current[1]*preprevious[0])
                        / Math.sqrt(Math.pow(current[1]-preprevious[1],2) + Math.pow(current[0]-preprevious[0],2));
                else {
                    // difference in leader's utility
                    double slope = (preprevious[1] - current[1])/(preprevious[0] - current[0]);
                    distance = Math.abs(previous[0] - (previous[1]-current[1])/slope - current[0]);
                }
                //
                if (USE_CURRENT_LEAF_LEADER_UTILITY) HULL_DELTA = DELTA_BY_UTILITY_COEF * current[0];
                if (distance < HULL_DELTA){
                    deletedPointsNumber++;
                    upperHull.pop();
                }
            }

            upperHull.push(reachableLeaves.get(i));
        }

//        System.out.println("Number of deleted points = "+deletedPointsNumber + " / " + upperHull.size());

//        for (int i = 0; i < upperHull.size(); i++)
//            System.out.printf("["+upperHull.get(i)[0] + ", " + upperHull.get(i)[1]+"], ");
//        System.out.println();

        if (MAKE_GADGET_STATS){
            String stat = gadgetStats.get(gadgetStats.size()-1);
            HashSet<double[]> uniqueUtility = new HashSet<>();
            for (double[] utility : reachableLeaves) {
                boolean contains = false;
//                double[] utility = state.getUtilities();
                for(double[] u : uniqueUtility)
                    if (Arrays.equals(u, utility)){
                        contains = true; break;
                    }
                if(!contains) uniqueUtility.add(utility);
            }
            stat += uniqueUtility.size() + " ";
            stat += upperHull.size() + " ";
            for (int i = 0; i < upperHull.size(); i++)
                stat += upperHull.get(i)[0] + " " + upperHull.get(i)[1] + " ";
            gadgetStats.set(gadgetStats.size()-1, stat);
        }

        if (leader.getId() == 0) return new ArrayList<>(upperHull);
        else{
            reachableLeaves = new ArrayList<>();
            for(double[] u : upperHull)
                reachableLeaves.add(new double[]{u[1], u[0]});
            return reachableLeaves;
        }
    }

    public double cross(double[] oUtilities, double[] aUtilities, double[] bUtilities) {
//        double[] aUtilities = A.getUtilities();
//        double[] bUtilities = B.getUtilities();
//        double[] oUtilities = O.getUtilities();
        return (aUtilities[0] - oUtilities[0]) * (bUtilities[1] - oUtilities[1])
                - (aUtilities[1] - oUtilities[1]) * (bUtilities[0] - oUtilities[0]);
    }

    public ArrayList<double[]> getUCHApproximation(ArrayList<double[]> hull){
        if (hull.size() <= 5) return hull;
        ArrayList<double[]> approximation = new ArrayList<>();
        double[] center = new double[]{0.0, 0.0};
        for (double[] h : hull){
            center[0] += h[0];
            center[1] += h[1];
        }
        center[0] /= hull.size(); center[1] /= hull.size();
        double[] mostFarState = null;
        double mostFarDistance = 0.0;
        for (double[] h : hull){
            double dist = Math.sqrt(Math.pow(center[0] - h[0],2) + Math.pow(center[1] - h[1],2));
            if (dist > mostFarDistance){
                mostFarDistance = dist;
                mostFarState = h;
            }
        }

        return approximation;
    }

    @Override
    public Double getResultForPlayer(Player leader) {
        if (leader.equals(this.leader))
            return gameValue;
        else return null;
    }

    @Override
    public Map<Sequence, Double> getResultStrategiesForPlayer(Player player) {
        return null;
    }

    @Override
    public long getOverallConstraintGenerationTime() {
        return overallConstraintGenerationTime;
    }

    @Override
    public long getOverallConstraintLPSolvingTime() {
        return overallConstraintLPSolvingTime;
    }

    public long getOverallGadgetMakingTime(){
        return  overallGadgetMakingTime;
    }

    @Override
    public String getInfo() {
        return "Complete Sefce solver with gadgets.\n"+
                "Create gadgets = "+CREATE_GADGETS+", pareto leaves = " + USE_PARETO_LEAVES +
                ", initial gadget depth = " + INITIAL_GADGET_DEPTH_RATIO +
                ", discount leader utilities = " +DISCOUNT_GADGETS +
                ", eps = " + eps + ", approximate hull = " + APPROX_HULL + ", approx coef = " + DELTA_BY_UTILITY_COEF +
                ", use local point utility = " + USE_CURRENT_LEAF_LEADER_UTILITY + ", distance to projection = " + DISTANCE_TO_PROJECTION;
    }

    public double getRestrictedGameRatio(){
        return gadgetRootsSequences.size();
    }

    public int getRestrictedGameSizeWithSingletonLeaves(){
        int size = 0;
        for (GameState state : utilityToDelete.keySet())
            size += utilityToDelete.get(state).size() + (algConfig.isStateInConfig(state) ? 0 : 1);
        for (SequenceInformationSet set : algConfig.getAllInformationSets().values())
            if (set.getOutgoingSequences().isEmpty()) {
//                System.out.println(set.getAllStates().size());
                size += Math.max(0, set.getAllStates().size() - 1);
            }
        return algConfig.getAllInformationSets().size()+size;
    }

    public int getNumberOfSequences(){
        int size = 0;
        for (GameState state : utilityToDelete.keySet())
            size += utilityToDelete.get(state).size();
        return algConfig.getAllSequences().size()+size;
    }

    public int getFinalLPSize(){
        return lpTable.getLPSize();
    }

    public double getExpectedGadgetSize(){
        if(utilityToDelete.isEmpty()) return info.getMaxDepth()/2.0;
        int size = 0;
        for (GameState state : utilityToDelete.keySet())
             size += utilityToDelete.get(state).size();
        return (double)size/utilityToDelete.size();
    }


}
