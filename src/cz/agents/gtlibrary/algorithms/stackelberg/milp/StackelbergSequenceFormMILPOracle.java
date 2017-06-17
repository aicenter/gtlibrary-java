package cz.agents.gtlibrary.algorithms.stackelberg.milp;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.stackelberg.GeneralSumBestResponse;
import cz.agents.gtlibrary.algorithms.stackelberg.StackelbergConfig;
import cz.agents.gtlibrary.iinodes.ArrayListSequenceImpl;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.DummyPrintStream;
import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;

import java.util.*;

/**
 * Created by bosansky on 7/24/14.
 */
public class StackelbergSequenceFormMILPOracle extends StackelbergSequenceFormMILP{
    public StackelbergSequenceFormMILPOracle(Player[] players, Player leader, Player follower, GameInfo info, Expander expander) {
        super(players, leader, follower, info, expander);
    }

    public double calculateLeaderStrategies(StackelbergConfig algConfig, Expander<SequenceInformationSet> expander) {

        Set<Sequence> neverBR = new HashSet<>();
        double maxValue = Double.NEGATIVE_INFINITY;
//        Set<Sequence> followerBR = new HashSet<>();
        Map<Sequence, Double> leaderResult = new HashMap<>();
//        Map<Sequence, Double> followerResult = new HashMap<>();

        Map<Sequence, Double> firstRP = new HashMap<>();
        firstRP.put(algConfig.getRootState().getSequenceFor(follower), 1d);
        TreeSet<FollowerRP> set = new TreeSet<>(new FollowerRPComparator());
        set.add(new FollowerRP(info.getMaxUtility(), firstRP));

        try {
            IloCplex cplex = modelsForPlayers.get(leader);
            IloNumVar v0 = objectiveForPlayers.get(leader);

            long startTime = threadBean.getCurrentThreadCpuTime();
            createVariables(cplex, algConfig);
            createConstraintsForSets(cplex, algConfig.getAllInformationSets().values());
            createConstraintsForStates(cplex, algConfig.getAllLeafs());

            for (Sequence firstPlayerSequence : algConfig.getSequencesFor(follower)) {
                createConstraintForSequence(cplex, firstPlayerSequence, algConfig);
//                createSlackConstraintForSequence(cplex, firstPlayerSequence);
            }

//            createConstraintsForSequences(algConfig, cplex, algConfig.getSequencesFor(follower));
            setObjective(cplex, v0, algConfig);
            debugOutput.println("phase 1 done");
            overallConstraintGenerationTime += threadBean.getCurrentThreadCpuTime() - startTime;
            GeneralSumBestResponse gBR = new GeneralSumBestResponse(expander, follower.getId(), players, algConfig, info);

            int iterations = 0;

            while (!set.isEmpty()) {
//                boolean tmp = false;
//                followerBR.clear();
//                cplex.exportModel("stck-" + leader + ".lp"); // uncomment for model export
                FollowerRP currentBR = set.pollFirst();

                if (currentBR.leaderUpperBound < maxValue) { // the best upper bound is strictly worse than current best result -- we can stop
                    break;
                }

                iterations++;
                if (iterations % 1000 == 0)
                    System.out.println("Iterations: " + iterations);

                Map<Sequence, Double> nextBR = new HashMap<>();
                nextBR.putAll(currentBR.realizationPlan);
                SequenceInformationSet isToBeExtended = null;
                mainloop:
                for (Sequence s : nextBR.keySet()) {
                    isloop:
                    for (SequenceInformationSet i : algConfig.getReachableSets(s)) {
                        if (i.getPlayer().equals(leader)) continue;
                        if (!expander.getActions(i).isEmpty()) {
                            for (Action a : (List<Action>)expander.getActions(i)) {
                                Sequence newSequence = new ArrayListSequenceImpl(i.getAllStates().iterator().next().getSequenceForPlayerToMove());
                                newSequence.addLast(a);
                                if (nextBR.containsKey(newSequence)) {
                                     continue isloop;
                                }
                            }
                            isToBeExtended = i;
                            break mainloop;
                        }
                    }
                }
                if (isToBeExtended == null) { // there is no IS to be extended -> we have a final RP and
                    if (currentBR.leaderUpperBound > maxValue) {
                        maxValue = currentBR.leaderUpperBound;
                        leaderResult = currentBR.realizationPlan;
                        debugOutput.println("Best reward is " + maxValue);
                    }
                    continue;
                }

                tightBoundsForSequences(cplex, nextBR.keySet());

                for (Action a : expander.getActions(isToBeExtended)) {
                    Sequence newSequence = new ArrayListSequenceImpl(isToBeExtended.getAllStates().iterator().next().getSequenceForPlayerToMove());
                    newSequence.addLast(a);

                    if (neverBR.contains(newSequence)) continue;

                    tightBoundsForSequence(cplex, newSequence);



                    startTime = threadBean.getCurrentThreadCpuTime();
//                    debugOutput.println("Solving");
                    cplex.setOut(DummyPrintStream.getDummyPS());
                    cplex.setWarning(DummyPrintStream.getDummyPS());
                    cplex.solve();
                    overallConstraintLPSolvingTime += threadBean.getCurrentThreadCpuTime() - startTime;
//                    debugOutput.println("Status: " + cplex.getCplexStatus());

                    if (cplex.getCplexStatus() == IloCplex.CplexStatus.Optimal || cplex.getCplexStatus() == IloCplex.CplexStatus.OptimalTol) {
                        double v = cplex.getValue(v0);
//                        debugOutput.println("Best reward is " + v);
                        HashMap<Sequence, Double> tmp = new HashMap<>();
                        tmp.putAll(nextBR);
                        tmp.put(newSequence,1d);

                        set.add(new FollowerRP(v, tmp));
                    } else {
                        neverBR.add(newSequence);
                    }
                    unTightBoundsForSequence(cplex, newSequence);

                }
                unTightBoundsForSequences(cplex, nextBR.keySet());
            }
        } catch (IloException e) {
            e.printStackTrace();
        }

        System.out.println("Final NVBR Size : " + neverBR.size());
        System.out.println(neverBR);

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

//    protected boolean addFollowerSequences(StackelbergConfig algConfig, IloCplex model, Set<Sequence> sequences) throws IloException{
//        boolean somethingAdded = false;
//
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
//
//        for (Sequence firstPlayerSequence : sequences) {
//            if (constraints.containsKey(firstPlayerSequence)) {
//                model.delete(constraints.get(firstPlayerSequence));
//                constraints.remove(firstPlayerSequence);
//            }
//
//            createConstraintForSequence(model, firstPlayerSequence, algConfig);
//
//            if (slackConstraints.containsKey(firstPlayerSequence)) {
//                model.delete(slackConstraints.get(firstPlayerSequence));
//                slackConstraints.remove(firstPlayerSequence);
//            }
//            if (!slackConstraints.containsKey(firstPlayerSequence)) {
//                somethingAdded = true;
//            }
//            createSlackConstraintForSequence(model, firstPlayerSequence);
//        }
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
//
//        for (GameState gs : newLeafs) {
//            IloNumVar LS = variables.get(gs);
//            IloNumVar RSF = variables.get(gs.getSequenceFor(follower));
//            IloRange cF = model.addLe(model.diff(LS, RSF), 0, "LBC:F:" + gs);
//            slackConstraints.put(gs, new IloRange[]{cF, slackConstraints.get(gs)[1]});
//        }
//        return somethingAdded;
//    }

    protected void tightBoundsForSequences(IloCplex cplex, Collection<Sequence> sequences) throws IloException{
        for (Sequence s : sequences) {
            tightBoundsForSequence(cplex, s);
        }
    }

    protected void tightBoundsForSequence(IloCplex cplex, Sequence sequence) throws IloException{
            createSlackConstraintForSequence(cplex, sequence);
            variables.get(sequence).setLB(1d);
    }

    protected void unTightBoundsForSequences(IloCplex cplex, Collection<Sequence> sequences) throws IloException{
        for (Sequence s : sequences) {
            unTightBoundsForSequence(cplex, s);
        }
    }

    protected void unTightBoundsForSequence(IloCplex cplex, Sequence sequence) throws IloException{
        cplex.delete(slackConstraints.get(sequence));
        slackConstraints.remove(sequence);
        variables.get(sequence).setLB(0d);
    }

    protected class FollowerRP {
        final protected double leaderUpperBound;
        final Map<Sequence, Double> realizationPlan;

        public FollowerRP(double leaderUpperBound, Map<Sequence, Double> realizationPlan) {
            this.leaderUpperBound = leaderUpperBound;
            this.realizationPlan = realizationPlan;
        }
    }

    protected class FollowerRPComparator implements Comparator<FollowerRP> {


        @Override
        public int compare(FollowerRP o1, FollowerRP o2) {
            if (o1.leaderUpperBound > o2.leaderUpperBound)
                return -1;
            if (o1.leaderUpperBound < o2.leaderUpperBound)
                return 1;
            if (o1.leaderUpperBound == o2.leaderUpperBound) {
                if (o1.realizationPlan.size() > o2.realizationPlan.size())
                    return -1;
                if (o1.realizationPlan.size() < o2.realizationPlan.size())
                    return 1;
                if (o1.realizationPlan.size() == o2.realizationPlan.size()) {
                    if (o1.realizationPlan.hashCode() > o2.realizationPlan.hashCode())
                        return -1;
                    if (o1.realizationPlan.hashCode() < o2.realizationPlan.hashCode())
                        return 1;
                    if (o1.realizationPlan.hashCode() == o2.realizationPlan.hashCode())
                        return 0;
                }
            }
            return 0;
        }
    }
}
