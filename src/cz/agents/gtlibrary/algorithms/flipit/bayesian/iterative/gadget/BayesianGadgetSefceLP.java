package cz.agents.gtlibrary.algorithms.flipit.bayesian.iterative.gadget;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPData;
import cz.agents.gtlibrary.algorithms.stackelberg.StackelbergConfig;
import cz.agents.gtlibrary.algorithms.stackelberg.correlated.LeaderGenerationConfig;
import cz.agents.gtlibrary.algorithms.stackelberg.correlated.twoplayer.iterative.gadgets.GadgetAction;
import cz.agents.gtlibrary.algorithms.stackelberg.correlated.twoplayer.iterative.gadgets.GadgetInformationSet;
import cz.agents.gtlibrary.algorithms.stackelberg.correlated.twoplayer.iterative.gadgets.tables.GadgetLPTable;
import cz.agents.gtlibrary.domain.flipit.FlipItGameInfo;
import cz.agents.gtlibrary.domain.flipit.types.FollowerType;
import cz.agents.gtlibrary.iinodes.ArrayListSequenceImpl;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.Pair;
import cz.agents.gtlibrary.utils.Quadruple;
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
public class BayesianGadgetSefceLP implements Solver {

    protected HashMap<FollowerType, HashSet<Sequence>> gadgetRootsSequences;
    protected HashMap<FollowerType, HashMap<Sequence, HashSet<GameState>>> gadgetRoots;
    protected HashMap<FollowerType, HashMap<GameState, HashMap<Object, HashSet<Object>>>> varsToDelete;
    protected HashMap<FollowerType, HashMap<GameState, HashSet<Object>>> eqsToDelete;
    protected HashMap<FollowerType, HashMap<GameState, HashSet<Object>>> utilityToDelete;
    // sets for gadget vars and gadget cons

    protected double eps;
    protected GadgetLPTable lpTable;
    protected Player leader;
    protected Player follower;
    protected GameInfo info;
    protected ThreadMXBean threadBean;
    protected ArrayList<LeaderGenerationConfig> algConfigs;
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
    protected final boolean PRINT_PROGRESS = false;
    protected final boolean PRINT_SOLVING = false;

    protected final boolean MAKE_GADGET_STATS = false;
    protected ArrayList<String> gadgetStats;

    protected final double INITIAL_GADGET_DEPTH_RATIO = 0.4;
    protected final double INITIAL_GADGET_DEPTH;

    public BayesianGadgetSefceLP(Player leader, GameInfo info) {
        this.info = info;
        this.leader = leader;
        this.follower = info.getOpponent(leader);
        this.eps = 1e-8;
        this.iteration = 0;
        this.lpTable = new GadgetLPTable();
        this.threadBean = ManagementFactory.getThreadMXBean();
        this.gadgetRootsSequences = new HashMap<>();
        this.gadgetRoots = new HashMap<>();
        this.varsToDelete = new HashMap<>();
        this.eqsToDelete = new HashMap<>();
        this.utilityToDelete = new HashMap<>();

        for (FollowerType type : FlipItGameInfo.types) {
            this.gadgetRootsSequences.put(type, new HashSet<>());
            this.gadgetRoots.put(type, new HashMap<>());
            this.varsToDelete.put(type, new HashMap<>());
            this.eqsToDelete.put(type, new HashMap<>());
            this.utilityToDelete.put(type, new HashMap<>());
        }
        this.gadgetsDismissed = 0;
        if (MAKE_GADGET_STATS)
            this.gadgetStats = new ArrayList<>();
        this.INITIAL_GADGET_DEPTH = INITIAL_GADGET_DEPTH_RATIO * info.getMaxDepth() / 2.0;
    }

    public double getExpectedGadgetDepth() {
        if (gadgetRootsSequences.isEmpty())
            return info.getMaxDepth() / 2.0;
        int size = 0;
        for (FollowerType type : FlipItGameInfo.types) {
            for (Sequence seq : gadgetRootsSequences.get(type))
                size += seq.size();
        }
        return (double) size / gadgetRootsSequences.size();
    }

    /*
        Making roots of gadgets from a given IS.
     */
    protected void makeGadget(GameState state, FollowerType type) {
//        if (gadgetRoots.contains(set)) return;
        HashMap<Object, HashSet<Object>> varsToDeleteForState = new HashMap<>();
        varsToDelete.get(type).put(state, varsToDeleteForState);
        HashSet<Object> utilityToDeleteForState = new HashSet<>();
        utilityToDelete.get(type).put(state, utilityToDeleteForState);
        HashSet<Object> blackList = new HashSet<>();
        Sequence followerSequence = state.getSequenceFor(follower);

        // zpracuj i root !!
        createPContinuationConstraint(blackList, state.getSequenceFor(leader), followerSequence, null, type);
        createSequenceConstraint(algConfigs.get(type.getID()), followerSequence, type);

        // update constraints and OBJECTIVE
        // remember which were updated so that they can be later discarded

        // 1. layer (4,5)
        Sequence leaderSequence = new ArrayListSequenceImpl(state.getSequenceForPlayerToMove());
        GadgetAction middleAction = new GadgetAction(algConfigs.get(type.getID()).getInformationSetFor(state), state.getISKeyForPlayerToMove());
        leaderSequence.addLast(middleAction);
        createPContinuationConstraintInState(blackList, new ArrayList<Action>() {{
            add(middleAction);
        }}, state.getSequenceForPlayerToMove(), followerSequence, state.getISKeyForPlayerToMove(), type);
        createPContinuationConstraint(blackList, leaderSequence, followerSequence, null, type);

        gadgetRootsSequences.get(type).add(leaderSequence);
        if (!gadgetRoots.get(type).containsKey(leaderSequence))
            gadgetRoots.get(type).put(leaderSequence, new HashSet<>());
        gadgetRoots.get(type).get(leaderSequence).add(state);
//        System.out.println(gadgetRoots.get(leaderSequence).size());


        // 2. layer (4,5,6,7)
        SequenceInformationSet gadgetSet = new GadgetInformationSet(state, leaderSequence);

        // 6, 7 :
        ArrayList<double[]> leavesUnder = getLeavesUnder(state, type);
        ArrayList<Action> actions = new ArrayList<>();
        LinkedHashSet<Sequence> outgoingSeqs = new LinkedHashSet<>();
        for (int i = 0; i < leavesUnder.size(); i++) {
            leaderSequence = new ArrayListSequenceImpl(state.getSequenceForPlayerToMove());
            GadgetAction leafAction = new GadgetAction(gadgetSet, state, i);
            actions.add(new GadgetAction(gadgetSet, state, i));
            leaderSequence.addLast(middleAction);
            leaderSequence.addLast(leafAction);
            outgoingSeqs.add(leaderSequence);
            double[] u = leavesUnder.get(i);

            lpTable.setObjective(createSeqPairVarKey(leaderSequence, followerSequence, type), type.getPrior() * u[leader.getId()]);
            utilityToDeleteForState.add(createSeqPairVarKey(leaderSequence, followerSequence, type));

            if (u[follower.getId()] != 0.0) {
                Pair<Sequence, FollowerType> folSeqKey = new Pair<>(followerSequence, type);
                lpTable.setConstraint(folSeqKey, createSeqPairVarKey(leaderSequence, followerSequence, type), -u[follower.getId()]);

                if (!varsToDeleteForState.containsKey(folSeqKey))
                    varsToDeleteForState.put(folSeqKey, new HashSet<>());
                varsToDeleteForState.get(folSeqKey).add(createSeqPairVarKey(leaderSequence, followerSequence, type));


                // 7 :
                for (Action action : followerSequence) {
                    for (Sequence relevantSequence : ((SequenceInformationSet) action.getInformationSet()).getOutgoingSequences()) {
                        Object eqKey = new Quadruple<>(followerSequence.getLastInformationSet().getISKey(), followerSequence, relevantSequence, type);
                        lpTable.setConstraint(eqKey, createSeqPairVarKeyCheckExistence(leaderSequence, relevantSequence, type), -u[follower.getId()]);

                        if (!varsToDeleteForState.containsKey(eqKey))
                            varsToDeleteForState.put(eqKey, new HashSet<>());
                        varsToDeleteForState.get(eqKey).add(createSeqPairVarKeyCheckExistence(leaderSequence, relevantSequence, type));
                    }
                }

                Object eqKey = new Quadruple<>(followerSequence.getLastInformationSet().getISKey(), followerSequence, new ArrayListSequenceImpl(follower), type);
                lpTable.setConstraint(eqKey, createSeqPairVarKeyCheckExistence(leaderSequence, new ArrayListSequenceImpl(follower), type), -u[follower.getId()]);
                if (!varsToDeleteForState.containsKey(eqKey))
                    varsToDeleteForState.put(eqKey, new HashSet<>());
                varsToDeleteForState.get(eqKey).add(createSeqPairVarKeyCheckExistence(leaderSequence, new ArrayListSequenceImpl(follower), type));

            }
        }
        leaderSequence = new ArrayListSequenceImpl(state.getSequenceForPlayerToMove());
        leaderSequence.addLast(middleAction);
        createPContinuationConstraintInState(blackList, actions, leaderSequence, followerSequence, state.getISKeyForPlayerToMove(), type);
//        System.out.println(outgoingSeqs.size() + " " + actions.size());
        for (Sequence outgoing : outgoingSeqs) {
            createPContinuationConstraint(blackList, outgoing, followerSequence, outgoingSeqs, type);
        }

        eqsToDelete.get(type).put(state, blackList);
//        System.out.println("BS///");
//        for (Object o : blackList)
//            System.out.println(o);
//        System.out.println("BE///");

    }

    protected void findInitialRestrictedGame() {
        createInitPConstraint();
//        createSequenceConstraint(algConfig, new ArrayListSequenceImpl(follower));
        for (FollowerType type : FlipItGameInfo.types) {
            expandAfter(algConfigs.get(type.getID()).getRootState(), type);
        }
//        addIndifferentLeaderRestrictionsDueToChance();
    }

//    protected void addIndifferentLeaderRestrictionsDueToChance(){
//        for (SequenceInformationSet leaderSet : algConfigs.get(0).getAllInformationSets().values()) {
//            if (leaderSet.getPlayer().equals(FlipItGameInfo.DEFENDER)) {
//                for (Sequence leaderSequence : leaderSet.getOutgoingSequences()) {
//                    Triplet setActionVarKey = new Triplet("sIA", leaderSet, leaderSequence);
//                    for (FollowerType type : FlipItGameInfo.types){
//                        Pair eqKey = new Pair(setActionVarKey, type);
//                        HashMap<Sequence,Collection<Triplet>> ps = new HashMap<>();
//                        boolean nonNullRelevant = false;
//
//                        for (GameState gameState : leaderSet.getAllStates()){
//                            Sequence natureSequence = gameState.getSequenceFor(FlipItGameInfo.NATURE);
//                            if (!ps.containsKey(natureSequence))
//                                ps.put(natureSequence, new ArrayList<>());
//                            for (Sequence followerSequence : getRelevantSequencesFor(gameState)){
//                                nonNullRelevant = true;
//                                if (!isPrefixOfRelevant(ps.get(natureSequence), followerSequence)) {
//                                    ps.get(natureSequence).add(new Triplet<Sequence, Sequence, FollowerType>(leaderSequence, followerSequence, type));
//                                }
//                            }
//                        }
//
//
//                        if (nonNullRelevant){
//                            for (Collection<Triplet> relevantPs : ps.values()){
//                                for (Triplet key : relevantPs) {
//                                    lpTable.setConstraint(eqKey, key, -1.0);
//                                }
//                                lpTable.setConstraint(eqKey, setActionVarKey, 1.0);
//                                lpTable.setLowerBound(setActionVarKey, 0.0);
//                                lpTable.setUpperBound(setActionVarKey, 1.0);
//                                lpTable.setConstraintType(eqKey, 1);
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }

    protected void deleteOldGadgetRootConstraintsAndVariables(GameState state, FollowerType type) {
        if (state.equals(algConfigs.get(type.getID()).getRootState())) return;
//        System.out.println("Deleting: " + state.hashCode() + " / " + eqsToDelete.get(type).get(state).size());
        for (Object eqKey : varsToDelete.get(type).get(state).keySet())
            for (Object varKey : varsToDelete.get(type).get(state).get(eqKey)) {
                lpTable.setConstraint(eqKey, varKey, 0);
                lpTable.deleteVar(varKey);
            }
        for (Object var : utilityToDelete.get(type).get(state)) {
            lpTable.setObjective(var, 0);
            lpTable.deleteVar(var);
        }
        for (Object eqKey : eqsToDelete.get(type).get(state)) {
            lpTable.deleteConstraint(eqKey);
        }
        eqsToDelete.get(type).remove(state);
        utilityToDelete.get(type).remove(state);
        varsToDelete.get(type).remove(state);
        gadgetsDismissed++;
    }

    // Expand and generate constraints
    protected void expandAfter(GameState state, FollowerType type) {

        deleteOldGadgetRootConstraintsAndVariables(state, type);

        LinkedList<GameState> queue = new LinkedList<>();
        queue.add(state);
        while (queue.size() > 0) {
            GameState currentState = queue.removeFirst();

            if (isPossibleToCreateGadget(state, currentState)) {
//                algConfig.addStateToSequenceForm(currentState);
                if (PRINT_PROGRESS) System.out.printf("Making gadget...");
                long startTime = threadBean.getCurrentThreadCpuTime();
                makeGadget(currentState, type);
                overallGadgetMakingTime += threadBean.getCurrentThreadCpuTime() - startTime;
                if (PRINT_PROGRESS) System.out.println("done.");
                continue;
            }

            if (PRINT_PROGRESS) System.out.printf("Adding to config...");
            algConfigs.get(type.getID()).addStateToSequenceForm(currentState);
            if (!currentState.isGameEnd())
                algConfigs.get(type.getID()).setOutgoingSequencesImmediately(currentState, expander);
            if (PRINT_PROGRESS) System.out.println("done.");

//            System.out.println(algConfig.getInformationSetFor(currentState).getOutgoingSequences().size());

            // IS action constraints and sequence constraints
            if (PRINT_PROGRESS) System.out.printf("Generating IS constraints...");
            createISActionConstraints(algConfigs.get(type.getID()).getInformationSetFor(currentState), type);
            if (PRINT_PROGRESS) System.out.println("done.");

            // NF constraints
            if (PRINT_PROGRESS) System.out.printf("Generating NF constraints...");
            createPContinuationConstraints(currentState, type);
            if (PRINT_PROGRESS) System.out.println("done.");

            addIndifferentLeaderRestriction(currentState, type);


            if (currentState.isGameEnd()) {
                final double[] utilities = currentState.getUtilities();
                Double[] u = new Double[utilities.length];

                for (int i = 0; i < currentState.getAllPlayers().length + FlipItGameInfo.numTypes - 1; i++) {
                    if (utilities.length > i)
                        u[i] = utilities[i] * currentState.getNatureProbability() * info.getUtilityStabilizer();
                }
                lpTable.setObjective(createSeqPairVarKey(currentState.getSequenceFor(leader), currentState.getSequenceFor(follower), type), type.getPrior() * u[leader.getId()]);
                algConfigs.get(type.getID()).setUtility(currentState, u);

                if (PRINT_PROGRESS) System.out.printf("Generating seq constrain...");
                createSequenceConstraint(algConfigs.get(type.getID()), currentState.getSequenceFor(follower), type);
                if (PRINT_PROGRESS) System.out.println("done.");


                // add to constraints 6,7
                double utility = u[follower.getId()+type.getID()];
                if (utility != 0) {
                    // 6 :
                    lpTable.setConstraint(new Pair(currentState.getSequenceFor(follower), type), createSeqPairVarKey(currentState.getSequenceFor(leader), currentState.getSequenceFor(follower), type), -utility);
                    // 7 :
                    for (Action action : currentState.getSequenceFor(follower)) {
                        for (Sequence relevantSequence : ((SequenceInformationSet) action.getInformationSet()).getOutgoingSequences()) {
                            Object eqKey = new Quadruple<>(currentState.getSequenceFor(follower).getLastInformationSet().getISKey(), currentState.getSequenceFor(follower), relevantSequence, type);
                            lpTable.setConstraint(eqKey, createSeqPairVarKeyCheckExistence(currentState.getSequenceFor(leader), relevantSequence, type), -utility);
                        }
                    }
                    Object eqKey = new Quadruple<>(currentState.getSequenceFor(follower).getLastInformationSet().getISKey(), currentState.getSequenceFor(follower), new ArrayListSequenceImpl(follower), type);
                    lpTable.setConstraint(eqKey, createSeqPairVarKeyCheckExistence(currentState.getSequenceFor(leader), new ArrayListSequenceImpl(follower), type), -utility);
                }
                continue;
            }
            for (Action action : expander.getActions(currentState)) {
                queue.add(currentState.performAction(action));
            }
        }
    }

    protected void addIndifferentLeaderRestriction(GameState gameState, FollowerType type) {
        if (!gameState.getPlayerToMove().equals(leader)) return;
        SequenceInformationSet leaderSet = algConfigs.get(type.getID()).getInformationSetFor(gameState);
        for (Sequence leaderSequence : leaderSet.getOutgoingSequences()) {
            Triplet setActionVarKey = new Triplet("sIA", leaderSet.getISKey(), leaderSequence);
            Pair eqKey = new Pair(setActionVarKey, type);
            HashMap<Sequence, Collection<Triplet>> ps = new HashMap<>();
            boolean nonNullRelevant = false;

//            for (GameState gameState : leaderSet.getAllStates()) {
                Sequence natureSequence = gameState.getSequenceFor(FlipItGameInfo.NATURE);
                if (!ps.containsKey(natureSequence))
                    ps.put(natureSequence, new ArrayList<>());
                for (Sequence followerSequence : getRelevantSequencesFor(gameState)) {
                    nonNullRelevant = true;
                    if (!isPrefixOfRelevant(ps.get(natureSequence), followerSequence)) {
                        ps.get(natureSequence).add(new Triplet<Sequence, Sequence, FollowerType>(leaderSequence, followerSequence, type));
                    }
                }
//            }
            if (nonNullRelevant) {
                for (Collection<Triplet> relevantPs : ps.values()) {
                    for (Triplet key : relevantPs) {
                        lpTable.setConstraint(eqKey, key, -1.0);
                    }
                    lpTable.setConstraint(eqKey, setActionVarKey, 1.0);
                    lpTable.setLowerBound(setActionVarKey, 0.0);
                    lpTable.setUpperBound(setActionVarKey, 1.0);
                    lpTable.setConstraintType(eqKey, 1);
                }
            }
        }
    }

    protected HashSet<Sequence> getRelevantSequencesFor(GameState gameState) {
        HashSet<Sequence> relevantSequences = new HashSet<Sequence>();
        Sequence followerSequence = new ArrayListSequenceImpl(gameState.getSequenceFor(FlipItGameInfo.ATTACKER));
        boolean isLastIS = true;
        while (followerSequence.size() > 0) {
            SequenceInformationSet lastIS = (SequenceInformationSet) followerSequence.getLastInformationSet();
            for (Sequence outgoing : lastIS.getOutgoingSequences()) {
                if (isLastIS || !outgoing.isPrefixOf(gameState.getSequenceFor(FlipItGameInfo.ATTACKER))) {
                    relevantSequences.add(outgoing);
                }
            }
            isLastIS = false;
            followerSequence.removeLast();
        }
        return relevantSequences;
    }

    protected boolean isPrefixOfRelevant(Collection<Triplet> ps, Sequence followerSequence) {
        for (Triplet key : ps)
            if (followerSequence.isPrefixOf((Sequence) key.getSecond()))
                return true;
        return false;
    }

    protected boolean isPossibleToCreateGadget(GameState state, GameState currentState) {
        return CREATE_GADGETS && !currentState.isGameEnd()
                && !currentState.equals(state) && currentState.getPlayerToMove().equals(leader)
                && currentState.getSequenceFor(leader).size() >= INITIAL_GADGET_DEPTH;
    }

    protected boolean findReachableGadgetRoots(Map<FollowerType, Map<Sequence, Double>> strategy) {
        boolean reachableGadget = false;
        HashSet<Sequence> currentGadgetRootSequences;
        for (FollowerType type : FlipItGameInfo.types) {
            if (!FIX_LARGEST_DEVIATION_FIRST)
                currentGadgetRootSequences = new HashSet<>(gadgetRootsSequences.get(type));
            else currentGadgetRootSequences = getLargestDeviation(strategy, type);
            HashMap<Sequence, HashSet<GameState>> currentGadgetRoots = new HashMap<>(gadgetRoots.get(type));
            for (Sequence sequence : currentGadgetRootSequences) {
                if (strategy.get(type).containsKey(sequence) && (GENERATE_ALL_GADGETS ? true : strategy.get(type).get(sequence) > eps)) {
                    reachableGadget = true;
//                for (GameState setState : currentGadgetRoots.get(sequence)) {
//                    deleteOldGadgetRootConstraintsAndVariables(setState);
////                    expandAfter(setState);
////                    gadgetRoots.get(sequence).remove(setState);
//                }
                    for (GameState setState : currentGadgetRoots.get(sequence)) {
//                    deleteOldGadgetRootConstraintsAndVariables(setState);
                        expandAfter(setState, type);
//                    gadgetRoots.get(sequence).remove(setState);
                    }
                    gadgetRootsSequences.get(type).remove(sequence);
                    gadgetRoots.get(type).remove(sequence);
                }
            }
        }
        return reachableGadget;
    }

    private HashSet<Sequence> getLargestDeviation(Map<FollowerType, Map<Sequence, Double>> strategy, FollowerType type) {
        Sequence maxSequence = null;
        double maxDeviation = Double.NEGATIVE_INFINITY;
        for (Sequence seq : gadgetRootsSequences.get(type))
            if (strategy.get(type).get(seq) > maxDeviation) {
                maxDeviation = strategy.get(type).get(seq);
                maxSequence = seq;
            }
        HashSet<Sequence> bestDeviation = new HashSet<>();
        bestDeviation.add(maxSequence);
        return bestDeviation;
    }


    @Override
    public double calculateLeaderStrategies(AlgorithmConfig algConfig, Expander expander) {
        this.algConfigs = new ArrayList<>();
        for (FollowerType type : FlipItGameInfo.types)
            algConfigs.add(new LeaderGenerationConfig(((LeaderGenerationConfig) algConfig).getRootState()));
        this.expander = expander;

        findInitialRestrictedGame();

        boolean reachableGadget = true;

        while (reachableGadget) {
            iteration++;
            reachableGadget = findReachableGadgetRoots(solve());
        }
        if (MAKE_GADGET_STATS) writeGadgetStats();
        System.out.println("final number of gadgets created: " + (gadgetsDismissed + gadgetRootsSequences.size()));
        return gameValue;
    }

    private void writeGadgetStats() {
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

    protected Map<FollowerType, Map<Sequence, Double>> solve() {
        try {
            long startTime = threadBean.getCurrentThreadCpuTime();

//            lpTable.watchAllPrimalVariables();
            LPData lpData = lpTable.toCplex();

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
                Map<FollowerType, Map<Sequence, Double>> leaderRealPlan = null;
                leaderRealPlan = getThreats(lpData);
//                leaderRealPlan = behavioralToRealizationPlan(getBehavioralStrategy(lpData, leader));

                if (leaderRealPlan != null)
                    return leaderRealPlan;
                return new HashMap<FollowerType, Map<Sequence, Double>>();
            } else {
                System.err.println(lpData.getSolver().getStatus());
            }
        } catch (IloException e) {
            e.printStackTrace();
        }
        return new HashMap<FollowerType, Map<Sequence, Double>>();
    }

    protected void tuneSolver(LPData lpData) throws IloException {
        System.out.println("Tuning...");
        String tunedfile = "tuneCplex_";
        tunedfile += new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date()) + "_" + iteration + ".txt";
        IloCplex.ParameterSet paramset = lpData.getSolver().getParameterSet();
        int tunestat = lpData.getSolver().tuneParam();
        if (tunestat == IloCplex.TuningStatus.Complete)
            System.out.println("Tuning complete.");
        else if (tunestat == IloCplex.TuningStatus.Abort)
            System.out.println("Tuning abort.");
        else if (tunestat == IloCplex.TuningStatus.TimeLim)
            System.out.println("Tuning time limit.");
        else
            System.out.println("Tuning status unknown.");

        if (tunedfile != null) {
            lpData.getSolver().writeParam(tunedfile);
            System.out.println("Tuned parameters written to file '" +
                    tunedfile + "'");
        }
        System.out.println("done.");
    }

    protected Map<FollowerType, Map<Sequence, Double>> getThreats(LPData lpData) {
        Map<FollowerType, Map<Sequence, Double>> allTypesThreats = new HashMap<>();
        Map<Sequence, Double> threats = new HashMap<>();
        try {
            for (Object var : lpData.getWatchedPrimalVariables().keySet()) {
                if (var instanceof Triplet && ((Triplet) var).getFirst() instanceof Sequence && ((Triplet) var).getThird() instanceof FollowerType) {
                    Sequence seq = (Sequence) ((Triplet) var).getFirst();
                    FollowerType type = (FollowerType) ((Triplet) var).getThird();
                    double val = lpData.getSolver().getValue(lpData.getWatchedPrimalVariables().get(var));
                    if (!allTypesThreats.containsKey(type)) allTypesThreats.put(type, new HashMap<>());
                    threats = allTypesThreats.get(type);
                    if (!threats.containsKey(seq) || val > threats.get(seq))
                        threats.put(seq, val);
                }
            }
        } catch (IloCplex.UnknownObjectException e) {
            e.printStackTrace();
        } catch (IloException e) {
            e.printStackTrace();
        }
        return allTypesThreats;
    }

    protected void createISActionConstraints(SequenceInformationSet informationSet, FollowerType type) {
        if (!informationSet.getPlayer().equals(follower) || (!informationSet.getOutgoingSequences().isEmpty() && lpTable.existsEqKey(new Quadruple<>(informationSet.getISKey(), informationSet.getOutgoingSequences().iterator().next(), "eq", type))))
            return;

        createSequenceConstraint(algConfigs.get(type.getID()), informationSet, type);

        // set as reachable into previous IS
        for (Action action : informationSet.getPlayersHistory()) {
            for (Sequence relevantSequence : ((SequenceInformationSet) action.getInformationSet()).getOutgoingSequences()) {
                Object eqKey = new Quadruple<>(informationSet.getPlayersHistory().getLastInformationSet().getISKey(), informationSet.getPlayersHistory(), relevantSequence, type);
                lpTable.setConstraint(eqKey, new Triplet<>(informationSet.getISKey(), relevantSequence, type), -1);
            }
            Object eqKey = new Quadruple<>(informationSet.getPlayersHistory().getLastInformationSet().getISKey(), informationSet.getPlayersHistory(), new ArrayListSequenceImpl(follower), type);
            lpTable.setConstraint(eqKey, new Triplet<>(informationSet.getISKey(), new ArrayListSequenceImpl(follower), type), -1);
        }

//        if (informationSet.getPlayer().equals(follower)) {
        if (!informationSet.getOutgoingSequences().isEmpty()) {
            Sequence outgoingSequence = informationSet.getOutgoingSequences().iterator().next();

            for (Action action : outgoingSequence) {
                for (Sequence relevantSequence : ((SequenceInformationSet) action.getInformationSet()).getOutgoingSequences()) {
                    createISActionConstraint(algConfigs.get(type.getID()), relevantSequence, informationSet, type);
                }
            }
            createISActionConstraint(algConfigs.get(type.getID()), new ArrayListSequenceImpl(follower), informationSet, type);
        }
//        }
//        if (informationSet.getPlayer().equals(follower)) {
        for (Sequence sequence : informationSet.getOutgoingSequences()) {

//                createSequenceConstraint(algConfig, sequence);

            Object eqKey = new Quadruple<>(informationSet.getISKey(), sequence, "eq", type);
            Object varKey = new Triplet<>(informationSet.getISKey(), sequence, type);
            Object contVarKey = new Triplet<>("v", sequence, type);

            lpTable.setLowerBound(varKey, Double.NEGATIVE_INFINITY);
            lpTable.setLowerBound(contVarKey, Double.NEGATIVE_INFINITY);
            lpTable.setConstraint(eqKey, varKey, 1);
            lpTable.setConstraint(eqKey, contVarKey, -1);
            lpTable.setConstraintType(eqKey, 1);
        }
//        }
    }

    protected void createISActionConstraint(StackelbergConfig algConfig, Sequence followerSequence, SequenceInformationSet informationSet, FollowerType type) {
        for (Sequence sequence : informationSet.getOutgoingSequences()) {
            Object eqKey = new Quadruple<>(informationSet.getISKey(), sequence, followerSequence, type);
            Object varKey = new Triplet<>(informationSet.getISKey(), followerSequence, type);

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

    protected void createSequenceConstraint(StackelbergConfig algConfig, SequenceInformationSet followerSet, FollowerType type) {
//        System.out.println("Expanding v eq " + followerSet.getOutgoingSequences().size());
        Sequence followerSequence = followerSet.getPlayersHistory();
        Pair eqKey = new Pair(followerSequence, type);
        Object varKey = new Triplet<>("v", followerSequence, type);

//        System.out.println("Creating: " + followerSequence + " / " + type);

//        System.out.println(followerSequence);

//        if (lpTable.existsEqKey(followerSequence)) return;

        lpTable.setLowerBound(varKey, Double.NEGATIVE_INFINITY);
        lpTable.setConstraintType(eqKey, 1);
        lpTable.setConstraint(eqKey, varKey, 1);

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
            Object contVarKey = new Triplet<>("v", sequence, type);

            lpTable.setLowerBound(contVarKey, Double.NEGATIVE_INFINITY);
            lpTable.setConstraint(eqKey, contVarKey, -1);
        }
    }

    protected void createSequenceConstraint(StackelbergConfig algConfig, Sequence followerSequence, FollowerType type) {
        Pair eqKey = new Pair(followerSequence, type);
        Object varKey = new Triplet<>("v", followerSequence, type);

//        System.out.println(followerSequence);

        lpTable.setLowerBound(varKey, Double.NEGATIVE_INFINITY);
        lpTable.setConstraintType(eqKey, 1);
        lpTable.setConstraint(eqKey, varKey, 1);
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

    protected Triplet<Sequence, Sequence, FollowerType> createSeqPairVarKeyCheckExistence(Sequence sequence1, Sequence sequence2, FollowerType type) {
        Triplet<Sequence, Sequence, FollowerType> varKey = sequence1.getPlayer().equals(leader) ? new Triplet<>(sequence1, sequence2, type) : new Triplet<>(sequence2, sequence1, type);
//        Triplet<Sequence, Sequence, FollowerType> watchKey = sequence1.getPlayer().equals(leader) ? new Triplet<>(new ArrayListSequenceImpl(sequence1), new ArrayListSequenceImpl(sequence2), type) : new Triplet<>(new ArrayListSequenceImpl(sequence2), new ArrayListSequenceImpl(sequence1), type);
        assert lpTable.exists(varKey);
        lpTable.watchPrimalVariable(varKey, varKey);
        lpTable.setLowerBound(varKey, 0);
        lpTable.setUpperBound(varKey, 1);
        return varKey;
    }

    protected Triplet<Sequence, Sequence, FollowerType> createSeqPairVarKey(Sequence sequence1, Sequence sequence2, FollowerType type) {
        Triplet<Sequence, Sequence, FollowerType> varKey = sequence1.getPlayer().equals(leader) ? new Triplet<>(sequence1, sequence2, type) : new Triplet<>(sequence2, sequence1, type);

//        Triplet<Sequence, Sequence, FollowerType> watchKey = sequence1.getPlayer().equals(leader) ? new Triplet<>(new ArrayListSequenceImpl(sequence1), new ArrayListSequenceImpl(sequence2), type) : new Triplet<>(new ArrayListSequenceImpl(sequence2), new ArrayListSequenceImpl(sequence1), type);
        lpTable.watchPrimalVariable(varKey, varKey);
        lpTable.setLowerBound(varKey, 0);
        lpTable.setUpperBound(varKey, 1);
        return varKey;
    }

    protected Triplet<Sequence, Sequence, FollowerType> createSeqPairVarKey(GameState gameState, FollowerType type) {
        return createSeqPairVarKey(gameState.getSequenceFor(leader), gameState.getSequenceFor(follower), type);
    }

    protected void createInitPConstraint() {
        for (FollowerType type : FlipItGameInfo.types) {
//            String initP = "initP_" + type.getID();
            Object initP = new Pair("initP",type);
            lpTable.setConstraint(initP, createSeqPairVarKey(new ArrayListSequenceImpl(leader), new ArrayListSequenceImpl(follower), type), 1);
            lpTable.setConstant(initP, 1.0);
            lpTable.setConstraintType(initP, 1);
        }
    }

    protected void createPContinuationConstraints(GameState state, FollowerType type) {
        Set<Object> blackList = new HashSet<>();
        Set<Triplet<Sequence, Sequence, FollowerType>> pStops = new HashSet<>();

//        for (SequenceInformationSet informationSet : algConfig.getAllInformationSets().values()) {
//            List<Action> actions = expander.getActions(informationSet);
//            Player opponent = info.getOpponent(informationSet.getPlayer());
//
//            for (GameState gameState : informationSet.getAllStates()) {
//                if (!gameState.isGameEnd())
//                    createPContinuationConstraintInState(actions, opponent, gameState, blackList, pStops);
//            }
//        }

        if (!state.isGameEnd()) {
            List<Action> actions = expander.getActions(state);
            Player opponent = info.getOpponent(state.getPlayerToMove());
            createPContinuationConstraintInState(actions, opponent, state, blackList, pStops, type);
        }

//        System.out.println(algConfig.getSequencesFor(leader));

        Sequence leaderSequence = state.getSequenceFor(leader);
        {
            Sequence compatibleFollowerSequence = state.getSequenceFor(follower);
            {
                createPContinuationConstraint(blackList, leaderSequence, compatibleFollowerSequence, null, type);
            }
        }
    }

    protected void createPContinuationConstraint(Set<Object> blackList, Sequence leaderSequence, Sequence compatibleFollowerSequence, LinkedHashSet<Sequence> gadgetSeqs, FollowerType type) {
        for (Action action : compatibleFollowerSequence) {
            Sequence actionHistory = ((PerfectRecallInformationSet) action.getInformationSet()).getPlayersHistory();
            Object eqKeyFollower = new Quadruple<>(leaderSequence, actionHistory, action.getInformationSet().getISKey(), type);

            if (!blackList.contains(eqKeyFollower) && !((SequenceInformationSet) action.getInformationSet()).getOutgoingSequences().isEmpty()) {
                blackList.add(eqKeyFollower);
                Triplet<Sequence, Sequence, FollowerType> varKey = createSeqPairVarKey(leaderSequence, actionHistory, type);

                lpTable.setConstraintType(eqKeyFollower, 1);
                lpTable.setConstraint(eqKeyFollower, varKey, -1);
//                if (((SequenceInformationSet) action.getInformationSet()).getOutgoingSequences().isEmpty())
//                    System.out.println(action + " : EMPTY");
                for (Sequence followerSequence : ((SequenceInformationSet) action.getInformationSet()).getOutgoingSequences()) {
                    lpTable.setConstraint(eqKeyFollower, createSeqPairVarKey(leaderSequence, followerSequence, type), 1);
                }
            }

            ListIterator<Action> leaderSeqIterator = leaderSequence.listIterator(leaderSequence.size());
            Action leaderAction;

            while (leaderSeqIterator.hasPrevious()) {
                leaderAction = leaderSeqIterator.previous();
                Sequence leaderHistory = getLeaderHistory(leaderSequence, leaderAction);//(PerfectRecallInformationSet)leaderAction.getInformationSet()).getPlayersHistory();
                Object eqKeyLeader = new Quadruple<>(leaderHistory, actionHistory, leaderAction, type);

                LinkedHashSet<Sequence> outgoingSequences = getOutgoingSequences(leaderSequence, leaderAction, gadgetSeqs);
                if (!blackList.contains(eqKeyLeader) && !outgoingSequences.isEmpty()) {
                    blackList.add(eqKeyLeader);
                    Triplet<Sequence, Sequence, FollowerType> varKey = createSeqPairVarKey(leaderHistory, actionHistory, type);

                    lpTable.setConstraintType(eqKeyLeader, 1);
                    lpTable.setConstraint(eqKeyLeader, varKey, -1);
                    for (Sequence leaderContinuation : outgoingSequences) {//((SequenceInformationSet) leaderAction.getInformationSet()).getOutgoingSequences()) {
                        lpTable.setConstraint(eqKeyLeader, createSeqPairVarKey(leaderContinuation, actionHistory, type), 1);
                    }
                }

                for (Sequence followerSequence : ((SequenceInformationSet) action.getInformationSet()).getOutgoingSequences()) {
                    Object eqKeyLeaderCont = new Quadruple<>(leaderHistory, followerSequence, leaderAction.getInformationSet().getISKey(), type);

                    outgoingSequences = getOutgoingSequences(leaderSequence, leaderAction, gadgetSeqs);
                    if (!blackList.contains(eqKeyLeaderCont) && !outgoingSequences.isEmpty()) {
                        blackList.add(eqKeyLeaderCont);
                        Triplet<Sequence, Sequence, FollowerType> varKeyCont = createSeqPairVarKey(leaderHistory, followerSequence, type);

                        lpTable.setConstraintType(eqKeyLeaderCont, 1);
                        lpTable.setConstraint(eqKeyLeaderCont, varKeyCont, -1);
                        for (Sequence leaderContinuation : outgoingSequences) {//((SequenceInformationSet) leaderAction.getInformationSet()).getOutgoingSequences()) {
                            lpTable.setConstraint(eqKeyLeaderCont, createSeqPairVarKey(leaderContinuation, followerSequence, type), 1);
                        }
                    }
                }
            }
        }
    }


    protected Sequence getLeaderHistory(Sequence leaderSequence, Action leaderAction) {
        if (!(leaderAction instanceof GadgetAction))
            return ((PerfectRecallInformationSet) leaderAction.getInformationSet()).getPlayersHistory();
        else {
            for (int i = 0; i < leaderSequence.size(); i++) {
                if (leaderSequence.get(i).equals(leaderAction))
                    return leaderSequence.getSubSequence(i);
            }
        }
        return null;
    }

    protected LinkedHashSet<Sequence> getOutgoingSequences(Sequence leaderSequence, Action leaderAction, LinkedHashSet<Sequence> finalSet) {
        if (!(leaderAction instanceof GadgetAction))
            return ((SequenceInformationSet) leaderAction.getInformationSet()).getOutgoingSequences();
        else {
            if (((GadgetAction) leaderAction).getIndex() == -1) {
                for (int i = 0; i < leaderSequence.size(); i++) {
                    if (leaderSequence.get(i).equals(leaderAction)) {
                        Sequence seq = leaderSequence.getSubSequence(i + 1);
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


    protected void createPContinuationConstraintInState(List<Action> actions, Player opponent, GameState gameState, Set<Object> blackList, Set<Triplet<Sequence, Sequence, FollowerType>> pStops, FollowerType type) {
        Quadruple<Sequence, Sequence, ISKey, FollowerType> eqKey = new Quadruple<>(gameState.getSequenceFor(leader), gameState.getSequenceFor(follower), gameState.getISKeyForPlayerToMove(), type);


        if (blackList.contains(eqKey))
            return;
        blackList.add(eqKey);
        Triplet<Sequence, Sequence, FollowerType> varKey = createSeqPairVarKey(gameState.getSequenceFor(leader), gameState.getSequenceFor(follower), type);

//        if (actions.isEmpty()) System.out.println("Empty actions");

        pStops.add(varKey);
        lpTable.setConstraint(eqKey, varKey, -1);
        lpTable.setConstraintType(eqKey, 1);
        for (Action action : actions) {
            Sequence sequenceCopy = new ArrayListSequenceImpl(gameState.getSequenceForPlayerToMove());

            sequenceCopy.addLast(action);
            Triplet<Sequence, Sequence, FollowerType> contVarKey = createSeqPairVarKey(sequenceCopy, gameState.getSequenceFor(opponent), type);

            pStops.add(contVarKey);
            lpTable.setConstraint(eqKey, contVarKey, 1);
        }
    }

    protected void createPContinuationConstraintInState(Set<Object> blackList, List<Action> actions, Sequence leaderSequence, Sequence followerSequence, ISKey key, FollowerType type) {
//        Sequence followerSequence = new ArrayListSequenceImpl(followerSequenceOriginal);
        Quadruple<Sequence, Sequence, ISKey, FollowerType> eqKey = new Quadruple<>(leaderSequence, followerSequence, key, type);//leaderSequence.getLastInformationSet());

        if (blackList.contains(eqKey))
            return;
        blackList.add(eqKey);

        Triplet<Sequence, Sequence, FollowerType> varKey = createSeqPairVarKey(leaderSequence, followerSequence, type);

//        if (actions.isEmpty()) System.out.println("Empty actions");

        lpTable.setConstraint(eqKey, varKey, -1);
        lpTable.setConstraintType(eqKey, 1);
        for (Action action : actions) {
            Sequence sequenceCopy = new ArrayListSequenceImpl(leaderSequence);

            sequenceCopy.addLast(action);
            Triplet<Sequence, Sequence, FollowerType> contVarKey = createSeqPairVarKey(sequenceCopy, followerSequence, type);
            lpTable.setConstraint(eqKey, contVarKey, 1);
        }
    }


    protected ArrayList<double[]> getLeavesUnder(GameState state, FollowerType type) {
        ArrayList<double[]> leaves = new ArrayList<>();
        LinkedList<GameState> queue = new LinkedList<>();
        queue.add(state);
        while (queue.size() > 0) {
            GameState currentState = queue.removeFirst();
            if (currentState.isGameEnd()) {
                leaves.add(new double[]{currentState.getUtilities()[leader.getId()], currentState.getUtilities()[follower.getId() + type.getID()]});
                continue;
            }
            for (Action action : expander.getActions(currentState)) {
                queue.add(currentState.performAction(action));
            }
        }
        if (MAKE_GADGET_STATS && USE_PARETO_LEAVES && leaves.size() > 1) {
            gadgetStats.add(state.getSequenceFor(leader).size() + " ");
        }
        return getUpperConvexHullOfLeaves(leaves);
    }

    protected ArrayList<double[]> getUpperConvexHullOfLeaves(ArrayList<double[]> reachableLeaves) {
        if (!USE_PARETO_LEAVES || reachableLeaves.size() == 1) return reachableLeaves;
        int n = reachableLeaves.size(), k = 0;
        double[][] H = new double[2 * n][];

        Collections.sort(reachableLeaves, new Comparator<double[]>() {
            @Override
            public int compare(double[] o1, double[] o2) {
                if (o1[0] == o2[0])
                    return Double.compare(o1[1], o2[1]);
                else
                    return Double.compare(o1[0], o2[0]);
            }
        });

        // Build lower hull
        for (int i = 0; i < n; ++i) {
            while (k >= 2 && cross(H[k - 2], H[k - 1], reachableLeaves.get(i)) <= 0)
                k--;
            H[k++] = reachableLeaves.get(i);
        }

        int upperHullStart = 1;//Math.max(1, k-1);

        // Build upper hull
        for (int i = n - 2, t = k + 1; i >= 0; i--) {
            while (k >= t && cross(H[k - 2], H[k - 1], reachableLeaves.get(i)) <= 0)
                k--;
            H[k++] = reachableLeaves.get(i);
        }

//        System.out.println(k);
//        System.exit(0);

        if (MAKE_GADGET_STATS) {
            String stat = gadgetStats.get(gadgetStats.size() - 1);
            HashSet<double[]> uniqueUtility = new HashSet<>();
            for (double[] utility : reachableLeaves) {
                boolean contains = false;
//                double[] utility = state.getUtilities();
                for (double[] u : uniqueUtility)
                    if (Arrays.equals(u, utility)) {
                        contains = true;
                        break;
                    }
                if (!contains) uniqueUtility.add(utility);
            }
            stat += uniqueUtility.size() + " ";
            stat += k - 1 - (upperHullStart - 1) + " ";
            for (int i = upperHullStart - 1; i < k - 1; i++)
                stat += H[i][0] + " " + H[i][1] + " ";
            gadgetStats.set(gadgetStats.size() - 1, stat);
        }

        if (k > 1) {
            return new ArrayList<>(Arrays.asList(Arrays.copyOfRange(H, upperHullStart - 1, k - 1)));
//            H = H.subList(0, k-1);//Arrays.copyOfRange(H, 0, k - 1); // remove non-hull vertices after k; remove k - 1 which is a duplicate
        }
        return null;//(ArrayList<GameState>) H;
    }

    public double cross(double[] oUtilities, double[] aUtilities, double[] bUtilities) {
//        double[] aUtilities = A.getUtilities();
//        double[] bUtilities = B.getUtilities();
//        double[] oUtilities = O.getUtilities();
        return (aUtilities[0] - oUtilities[0]) * (bUtilities[1] - oUtilities[1])
                - (aUtilities[1] - oUtilities[1]) * (bUtilities[0] - oUtilities[0]);
    }

    public ArrayList<double[]> getUCHApproximation(ArrayList<double[]> hull) {
        if (hull.size() <= 5) return hull;
        ArrayList<double[]> approximation = new ArrayList<>();
        double[] center = new double[]{0.0, 0.0};
        for (double[] h : hull) {
            center[0] += h[0];
            center[1] += h[1];
        }
        center[0] /= hull.size();
        center[1] /= hull.size();
        double[] mostFarState = null;
        double mostFarDistance = 0.0;
        for (double[] h : hull) {
            double dist = Math.sqrt(Math.pow(center[0] - h[0], 2) + Math.pow(center[1] - h[1], 2));
            if (dist > mostFarDistance) {
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

    public long getOverallGadgetMakingTime() {
        return overallGadgetMakingTime;
    }

    @Override
    public String getInfo() {
        return "Complete Sefce solver with gadgets.";
    }

    public double getRestrictedGameRatio() {
        return gadgetRootsSequences.size();
    }

    public int getRestrictedGameSizeWithSingletonLeaves() {
        int size = 0;
        for (FollowerType type : FlipItGameInfo.types) {
            size += algConfigs.get(type.getID()).getAllInformationSets().size();
            for (GameState state : utilityToDelete.get(type).keySet())
                size += utilityToDelete.get(type).get(state).size() + (algConfigs.get(type.getID()).isStateInConfig(state) ? 0 : 1);
            for (SequenceInformationSet set : algConfigs.get(type.getID()).getAllInformationSets().values())
                if (set.getOutgoingSequences().isEmpty()) {
//                System.out.println(set.getAllStates().size());
                    size += Math.max(0, set.getAllStates().size() - 1);
                }
        }
        return size;
    }

    public int getNumberOfSequences() {
        int size = 0;
        for (FollowerType type : FlipItGameInfo.types) {
            size += algConfigs.get(type.getID()).getAllSequences().size();
            for (GameState state : utilityToDelete.get(type).keySet())
                size += utilityToDelete.get(type).get(state).size();
        }
        return size;
    }

    public int getFinalLPSize() {
        return lpTable.getLPSize();
    }

    public double getExpectedGadgetSize() {
        int size = 0;
        int numOfGadgets = 0;
        for (FollowerType type : FlipItGameInfo.types) {
            numOfGadgets += utilityToDelete.get(type).size();
            if (utilityToDelete.get(type).isEmpty()) size += info.getMaxDepth() / 2.0;
            for (GameState state : utilityToDelete.get(type).keySet())
                size += utilityToDelete.get(type).get(state).size();
        }
        return (double) size / numOfGadgets;
    }


}
