package cz.agents.gtlibrary.algorithms.stackelberg;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.iinodes.ArrayListSequenceImpl;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.DummyPrintStream;
import ilog.concert.*;
import ilog.cplex.IloCplex;

import java.util.*;

/**
 * Created by bosansky on 7/24/14.
 */
public class StackelbergSequenceFormMILPIncremental extends StackelbergSequenceFormMILP{
    public StackelbergSequenceFormMILPIncremental(Player[] players, GameInfo info, Expander expander) {
        super(players, info, expander);
    }

    public double calculateLeaderStrategies(int leaderIdx, int followerIdx, StackelbergConfig algConfig, Expander expander) {

        leader = players[leaderIdx];
        follower = players[followerIdx];

        double maxValue = Double.NEGATIVE_INFINITY;
        Set<Sequence> followerBR = new HashSet<Sequence>();
        Map<Sequence, Double> leaderResult = new HashMap<Sequence, Double>();
        Map<Sequence, Double> followerResult = new HashMap<Sequence, Double>();

        Map<Sequence, Double> firstRP = new HashMap<>();
        firstRP.put(algConfig.getRootState().getSequenceFor(follower), 1d);

        int iterations = 0;

        try {
            IloCplex cplex = modelsForPlayers.get(leader);
            IloNumVar v0 = objectiveForPlayers.get(leader);

            long startTime = threadBean.getCurrentThreadCpuTime();
            createVariables(cplex, algConfig);
            createConstraintsForSets(cplex, algConfig.getAllInformationSets().values());
            createConstraintsForStates(cplex, algConfig.getAllLeafs());

//            createVariables(cplex, algConfig);
//            createConstraintsForSets(cplex, algConfig.getAllInformationSets().values());
//            createConstraintsForStates(cplex, algConfig.getAllLeafs());

            for (Sequence firstPlayerSequence : algConfig.getSequencesFor(follower)) {
                createConstraintForSequence(cplex, firstPlayerSequence, algConfig);
//                createSlackConstraintForSequence(cplex, firstPlayerSequence);
            }

//            createConstraintsForSequences(algConfig, cplex, algConfig.getSequencesFor(follower));
            setObjective(cplex, v0, algConfig);
            debugOutput.println("phase 1 done");
            overallConstraintGenerationTime += threadBean.getCurrentThreadCpuTime() - startTime;
            GeneralSumBestResponse gBR = new GeneralSumBestResponse(expander, follower.getId(), players, algConfig, info);

            while (true) {
                iterations++;
                boolean tmp = false;
                followerBR.clear();
                cplex.exportModel("stck-" + leader + "-i" + iterations + ".lp"); // uncomment for model export
                startTime = threadBean.getCurrentThreadCpuTime();
                debugOutput.println("Solving");
                cplex.solve();
                overallConstraintLPSolvingTime += threadBean.getCurrentThreadCpuTime() - startTime;
                debugOutput.println("Status: " + cplex.getCplexStatus());

                if (cplex.getCplexStatus() == IloCplex.CplexStatus.Optimal || cplex.getCplexStatus() == IloCplex.CplexStatus.OptimalTol) {
                    double v = cplex.getValue(v0);
                    debugOutput.println("Best value is " + v);

                    maxValue = v;

                    for (Map.Entry<Object, IloNumVar> ee : variables.entrySet()) {
                        try {
                            debugOutput.println(ee.getKey().toString() + "=" + cplex.getValue(ee.getValue()));
                        } catch (IloCplex.UnknownObjectException e) {
                            continue;
                        }
                    }
                    debugOutput.println("-------");
                    for (Map.Entry<Object, IloNumVar> ee : slackVariables.entrySet()) {
                        try {
                            debugOutput.println(ee.getKey().toString() + "=" + cplex.getValue(ee.getValue()));
                        } catch (IloCplex.UnknownObjectException e) {
                            continue;
                        }
                    }



                    leaderResult = createSolution(algConfig, leader, cplex);
                    followerResult = createSolution(algConfig, follower, cplex);
//                    HashSet<Sequence> tmptmp = new HashSet<>();
//                    for (Map.Entry<Sequence,Double> entry : followerResult.entrySet()) {
//                        if (entry.getValue() > 0) tmptmp.add(entry.getKey());
//                    }
//                    tmp = addFollowerSequences(algConfig, cplex, tmptmp);

                    HashSet<GameState> addedLeafs = new HashSet<>();
                    for (GameState gs : algConfig.getAllLeafs()) {
//                        if (gs.getUtilities()[leaderIdx] >= maxValue) {
//                            followerBR.addAll(gs.getSequenceFor(follower).getAllPrefixes());
//                            addedLeafs.add(gs);
//                        } else {
                            try {
                                double p = cplex.getValue(variables.get(gs));
                                if (p > 0) {
                                    followerBR.addAll(gs.getSequenceFor(follower).getAllPrefixes());
                                    addedLeafs.add(gs);
                                }

                            } catch (IloCplex.UnknownObjectException e) {

                            }
//                        }
                    }
                    tmp = addFollowerSequences(algConfig, cplex, followerBR);


                }


                if (!tmp)
                    break;
            }
        } catch (IloException e) {
            e.printStackTrace();
        }


        resultStrategies.put(leader, leaderResult);
        resultValues.put(leader, maxValue);

        return maxValue;
    }

    protected void createVariables(IloCplex model, StackelbergConfig algConfig) throws IloException {
//        for (Sequence sequence : algConfig.getSequencesFor(leader)) {
//            assert (sequence.getPlayer().equals(leader));
//            createVariableForSequence(model, sequence);
//        }
        for (Sequence sequence : algConfig.getAllSequences()) {
            if (variables.containsKey(sequence)) continue;
            if (sequence.getPlayer().equals(leader)) {
                createVariableForSequence(model, sequence);
            } else {
                createIntegerVariableForSequence(model, sequence);
                createSlackVariableForSequence(model, sequence);
            }
        }

        for (GameState gs : algConfig.getAllLeafs()) {
            createStateProbVariable(model, gs);
        }

        for (SequenceInformationSet informationSet : algConfig.getAllInformationSets().values()) {
            if (!variables.containsKey(informationSet)) {
                if (informationSet.getPlayer().equals(leader)) {
                    //nothing
                } else {
                    createVariableForIS(model, informationSet);
                    createSlackVariablesForIS(informationSet, model);
                    informationSets.get(informationSet.getPlayer()).add(informationSet);
                }
            }
        }
        debugOutput.println("variables created");
    }

    protected void createBoundConstraintsForState(IloCplex cplex, GameState state) throws IloException {
        IloNumVar LS = variables.get(state);
        IloNumVar RSF = variables.get(state.getSequenceFor(follower));
        IloRange cF = cplex.addLe(cplex.diff(LS, RSF), 0, "LBC:F:" + state);
        IloNumVar RSL = variables.get(state.getSequenceFor(leader));
        IloRange cL = cplex.addLe(cplex.diff(LS, RSL), 0, "LBC:L:" + state);
        slackConstraints.put(state, new IloRange[]{cF, cL});
    }

    protected boolean addFollowerSequences(StackelbergConfig algConfig, IloCplex model, Set<Sequence> sequences) throws IloException{
        boolean somethingAdded = false;

//        Set<SequenceInformationSet> newInfoSets = new HashSet<>();
//        Set<SequenceInformationSet> newLastInfoSets = new HashSet<>();
//        for (Sequence s : sequences) {
//            if (variables.containsKey(s)) continue;
//            createIntegerVariableForSequence(model, s);
////            createSlackVariableForSequence(model, s);
//            if (s.size() > 0) newInfoSets.add((SequenceInformationSet)s.getLastInformationSet());
//            newLastInfoSets.addAll(algConfig.getReachableSets(s));
//        }

        for (Sequence firstPlayerSequence : sequences) {
//            if (constraints.containsKey(firstPlayerSequence)) {
//                model.delete(constraints.get(firstPlayerSequence));
//                constraints.remove(firstPlayerSequence);
//            }
//            createConstraintForSequence(model, firstPlayerSequence, algConfig);
//
//
//
//            if (slackConstraints.containsKey(firstPlayerSequence)) {
//                model.delete(slackConstraints.get(firstPlayerSequence));
//                slackConstraints.remove(firstPlayerSequence);
//            }
            if (!slackConstraints.containsKey(firstPlayerSequence)) {
                somethingAdded = true;
                createSlackConstraintForSequence(model, firstPlayerSequence);
            }

        }
//
//        for (InformationSet i : newLastInfoSets) {
//            for (GameState gs : i.getAllStates()) {
//                if (!sequences.contains(gs.getSequenceFor(follower))) continue;
//                if (algConfig.getActualNonZeroUtilityValuesInLeafsSE().keySet().contains(gs) && slackConstraints.get(gs)[0] == null) {
//                    newLeafs.add(gs);
//                }
//            }
//        }
//
//        for (SequenceInformationSet infoSet : newInfoSets) {
//            if (constraints.containsKey(infoSet)) {
//                model.delete(constraints.get(infoSet));
//                constraints.remove(infoSet);
//            }
//            createConstraintForIS(model, infoSet);
//        }
////
//        for (GameState gs : newLeafs) {
//            IloNumVar LS = variables.get(gs);
//            IloNumVar RSF = variables.get(gs.getSequenceFor(follower));
//            IloRange cF = model.addLe(model.diff(LS, RSF), 0, "LBC:F:" + gs);
//            slackConstraints.put(gs, new IloRange[]{cF, slackConstraints.get(gs)[1]});
//        }
        return somethingAdded;
    }

    protected void createSlackVariablesForIS(InformationSet is, IloCplex cplex) throws IloException {
        IloNumVar v = cplex.numVar(0, Double.POSITIVE_INFINITY, IloNumVarType.Float, "SL" + is.toString());
        slackVariables.put(is, v);
    }

    protected void createConstraintsForSets(IloCplex cplex, Collection<SequenceInformationSet> RConstraints) throws IloException {
        for (SequenceInformationSet infoSet : RConstraints) {
            if (constraints.containsKey(infoSet)) {
                cplex.delete(constraints.get(infoSet));
                constraints.remove(infoSet);
            }
            createConstraintForIS(cplex, infoSet);
            createSlackConstraintsForIS(cplex, infoSet);
        }
    }

    protected void createSlackConstraintsForIS(IloCplex cplex, SequenceInformationSet informationSet) throws IloException {
        IloNumExpr LS = slackVariables.get(informationSet);

        if (informationSet.getOutgoingSequences().isEmpty()) {
            return;
        }
        for (Sequence sequence : informationSet.getOutgoingSequences()) {
            if (slackVariables.get(sequence) == null)
                continue;
            IloNumExpr RS = slackVariables.get(sequence);
            cplex.addLe(cplex.diff(LS,RS),0,"SLCON:" + informationSet.toString() + sequence.toString());
        }

    }

    protected void setObjective(IloCplex cplex, IloNumVar v0, StackelbergConfig algConfig) throws IloException {
        if (leaderObj != null) cplex.delete(leaderObj);
        IloNumExpr sumG = cplex.constant(0);
        IloNumExpr sumP = cplex.constant(0);
        for (Map.Entry<GameState, Double[]> e : algConfig.getActualNonZeroUtilityValuesInLeafsSE().entrySet()) {
            sumG = cplex.sum(sumG, cplex.prod(e.getKey().getNatureProbability(), cplex.prod(e.getValue()[leader.getId()], variables.get(e.getKey()))));
        }
        for (GameState gs : algConfig.getAllLeafs()) {
            sumP = cplex.sum(sumP, variables.get(gs));
        }
        IloNumExpr sumSL_IS = cplex.constant(0);
        for (SequenceInformationSet informationSet : algConfig.getAllInformationSets().values()) {
            if (informationSet.getPlayer().equals(leader)) continue;
            sumSL_IS = cplex.sum(sumSL_IS, slackVariables.get(informationSet));
        }
        sumSL_IS = cplex.prod(-100,sumSL_IS);
        leaderObj = cplex.addEq(cplex.diff(v0, cplex.sum(sumG,sumSL_IS)), 0);
        cplex.addEq(sumP,1);
    }
}
