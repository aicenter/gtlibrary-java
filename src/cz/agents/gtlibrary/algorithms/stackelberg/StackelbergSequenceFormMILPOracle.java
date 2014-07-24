package cz.agents.gtlibrary.algorithms.stackelberg;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.interfaces.*;
import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by bosansky on 7/24/14.
 */
public class StackelbergSequenceFormMILPOracle extends StackelbergSequenceFormMILP{
    public StackelbergSequenceFormMILPOracle(Player[] players, GameInfo info, Expander expander) {
        super(players, info, expander);
    }

    public double calculateLeaderStrategies(int leaderIdx, int followerIdx, StackelbergConfig algConfig, Expander expander) {

        Set<Sequence> followerSequences = new HashSet<>();
        leader = players[leaderIdx];
        follower = players[followerIdx];

        double maxValue = Double.NEGATIVE_INFINITY;
        Set<Sequence> followerBR = new HashSet<Sequence>();
        Map<Sequence, Double> leaderResult = new HashMap<Sequence, Double>();
        Map<Sequence, Double> followerResult = new HashMap<Sequence, Double>();


        try {
            IloCplex cplex = modelsForPlayers.get(leader);
            IloNumVar v0 = objectiveForPlayers.get(leader);

            long startTime = threadBean.getCurrentThreadCpuTime();
            createVariables(cplex, algConfig);
            createConstraintsForSets(cplex, algConfig.getAllInformationSets().values());
            createConstraintsForStates(cplex, algConfig.getActualNonZeroUtilityValuesInLeafsSE().keySet());

            for (Sequence firstPlayerSequence : algConfig.getSequencesFor(follower)) {
                if (constraints.containsKey(firstPlayerSequence)) {
                    cplex.delete(constraints.get(firstPlayerSequence));
                    constraints.remove(firstPlayerSequence);
                }
                createConstraintForSequence(cplex, firstPlayerSequence, algConfig);
            }

//            createConstraintsForSequences(algConfig, cplex, algConfig.getSequencesFor(follower));
            setObjective(cplex, v0, algConfig);
            debugOutput.println("phase 1 done");
            overallConstraintGenerationTime += threadBean.getCurrentThreadCpuTime() - startTime;
            GeneralSumBestResponse gBR = new GeneralSumBestResponse(expander, follower.getId(), players, algConfig, info);

            while (true) {
                boolean tmp = false;
                followerBR.clear();
//                cplex.exportModel("stck-" + leader + ".lp"); // uncomment for model export
                startTime = threadBean.getCurrentThreadCpuTime();
                debugOutput.println("Solving");
                cplex.solve();
                overallConstraintLPSolvingTime += threadBean.getCurrentThreadCpuTime() - startTime;
                debugOutput.println("Status: " + cplex.getCplexStatus());

                if (cplex.getCplexStatus() == IloCplex.CplexStatus.Optimal || cplex.getCplexStatus() == IloCplex.CplexStatus.OptimalTol) {
                    double v = cplex.getValue(v0);
                    debugOutput.println("Best value is " + v);

                    maxValue = v;

//                for (Map.Entry<Object, IloNumVar> ee : variables.entrySet()) {
//                    try {
//                        debugOutput.println(ee.getKey().toString() + "=" + cplex.getValue(ee.getValue()));
//                    } catch (IloCplex.UnknownObjectException e) {
//                        continue;
//                    }
//                }
//                debugOutput.println("-------");
//                for (Map.Entry<Object, IloNumVar> ee : slackVariables.entrySet()) {
//                    try {
//                        debugOutput.println(ee.getKey().toString() + "=" + cplex.getValue(ee.getValue()));
//                    } catch (IloCplex.UnknownObjectException e) {
//                        continue;
//                    }
//                }

//                resultStrategies.put(leader, createSolution(algConfig, leader, cplex));
                leaderResult = createSolution(algConfig, leader, cplex);
//                for (Map.Entry<Sequence, Double> entry : resultStrategies.get(leader).entrySet()) {
//                    if (entry.getValue() > 0)
//                        System.out.println(entry);
//                }
//                System.out.println("*********");
//                for (Map.Entry<Sequence, Double> entry : createSolution(algConfig, follower, cplex).entrySet()) {
//                    if (entry.getValue() > 0)
//                        System.out.println(entry);
//                }
//                UtilityCalculator calculator = new UtilityCalculator(algConfig.getRootState(), expander);
//
//                System.out.println(calculator.computeUtility(new NoMissingSeqStrategy(createSolution(algConfig, follower, cplex)), new NoMissingSeqStrategy(resultStrategies.get(leader))));
//                    gBR.calculateBR(algConfig.getRootState(), leaderResult);
//                    followerBR = gBR.getBRSequences();
                    followerResult = createSolution(algConfig, follower, cplex);
                    for (Map.Entry<Sequence,Double> entry : followerResult.entrySet()) {
                        if (entry.getValue() > 0) followerBR.add(entry.getKey());
                    }
//                    System.out.println(followerBR);
                    tmp = addFollowerSequences(algConfig, cplex, followerBR);
//                    cplex.exportModel("stck-" + leader + "-step1.lp");
                }
//                cplex.solve();
//                double v = cplex.getValue(v0);
//                debugOutput.println("Best value after modification is " + v);
//                for (Map.Entry<Object, IloNumVar> ee : variables.entrySet()) {
//                    try {
//                        debugOutput.println(ee.getKey().toString() + "=" + cplex.getValue(ee.getValue()));
//                    } catch (IloCplex.UnknownObjectException e) {
//                        continue;
//                    }
//                }
//                debugOutput.println("-------");
//                for (Map.Entry<Object, IloNumVar> ee : slackVariables.entrySet()) {
//                    try {
//                        debugOutput.println(ee.getKey().toString() + "=" + cplex.getValue(ee.getValue()));
//                    } catch (IloCplex.UnknownObjectException e) {
//                        continue;
//                    }
//                }

                if (!tmp)
                break;
//                if (followerBR.size() == 0 && followerSequences.containsAll(followerBR)) {
//                    break;
//                }
//                followerSequences.addAll(followerBR);

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

        for (GameState gs : algConfig.getActualNonZeroUtilityValuesInLeafsSE().keySet()) {
            createStateProbVariable(model, gs);
        }

        for (SequenceInformationSet informationSet : algConfig.getAllInformationSets().values()) {
            if (!variables.containsKey(informationSet)) {
                if (informationSet.getPlayer().equals(leader)) {
                    //nothing
                } else {
                    createVariableForIS(model, informationSet);
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
        slackConstraints.put(state, new IloRange[]{null, cL});
    }

    protected boolean addFollowerSequences(StackelbergConfig algConfig, IloCplex model, Set<Sequence> sequences) throws IloException{
        boolean somethingAdded = false;

//        Set<SequenceInformationSet> newInfoSets = new HashSet<>();
//        Set<SequenceInformationSet> newLastInfoSets = new HashSet<>();
//        Set<GameState> newLeafs = new HashSet<>();
//        for (Sequence s : sequences) {
//            if (variables.containsKey(s)) continue;
//            createIntegerVariableForSequence(model, s);
//            createSlackVariableForSequence(model, s);
//            if (s.size() > 0) newInfoSets.add((SequenceInformationSet)s.getLastInformationSet());
//            newLastInfoSets.addAll(algConfig.getReachableSets(s));
//        }

        for (Sequence firstPlayerSequence : sequences) {
//            if (constraints.containsKey(firstPlayerSequence)) {
//                model.delete(constraints.get(firstPlayerSequence));
//                constraints.remove(firstPlayerSequence);
//            }
//
//            createConstraintForSequence(model, firstPlayerSequence, algConfig);

//            if (slackConstraints.containsKey(firstPlayerSequence)) {
//                model.delete(slackConstraints.get(firstPlayerSequence));
//                slackConstraints.remove(firstPlayerSequence);
//            }
            if (!slackConstraints.containsKey(firstPlayerSequence)) {
                somethingAdded = true;
            }
            createSlackConstraintForSequence(model, firstPlayerSequence);
        }

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
//
//        for (GameState gs : newLeafs) {
//            IloNumVar LS = variables.get(gs);
//            IloNumVar RSF = variables.get(gs.getSequenceFor(follower));
//            IloRange cF = model.addLe(model.diff(LS, RSF), 0, "LBC:F:" + gs);
//            slackConstraints.put(gs, new IloRange[]{cF, slackConstraints.get(gs)[1]});
//        }
        return somethingAdded;
    }
}
