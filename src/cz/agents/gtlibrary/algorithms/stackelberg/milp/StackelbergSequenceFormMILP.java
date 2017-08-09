/*
Copyright 2014 Faculty of Electrical Engineering at CTU in Prague

This file is part of Game Theoretic Library.

Game Theoretic Library is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Game Theoretic Library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with Game Theoretic Library.  If not, see <http://www.gnu.org/licenses/>.*/


package cz.agents.gtlibrary.algorithms.stackelberg.milp;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.stackelberg.StackelbergConfig;
import cz.agents.gtlibrary.algorithms.stackelberg.StackelbergSequenceFormLP;
import cz.agents.gtlibrary.interfaces.*;
import ilog.concert.*;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.CplexStatus;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.*;

public class StackelbergSequenceFormMILP extends StackelbergSequenceFormLP {
    protected double M;

    protected Player[] players;
    protected GameInfo info;

    protected IloRange leaderObj = null;

    protected Map<Object, IloNumVar> slackVariables = new HashMap<>();
    protected Map<Object, IloRange[]> slackConstraints = new HashMap<>(); // constraints for slack variables and p(h)
    protected Expander<SequenceInformationSet> expander;

    protected ThreadMXBean threadBean;


    public StackelbergSequenceFormMILP(Player[] players, Player leader, Player follower, GameInfo info, Expander<SequenceInformationSet> expander) {
        super(players, leader, follower);
        this.players = players;
        this.expander = expander;
        this.info = info;
        this.threadBean = ManagementFactory.getThreadMXBean();
        M = info.getMaxUtility()*info.getUtilityStabilizer()*2 + 1;
    }


    protected void resetModel(IloCplex cplex, Player player) throws IloException {
        cplex.clearModel();
        cplex.setParam(IloCplex.IntParam.RootAlg, CPLEXALG);
        cplex.setParam(IloCplex.IntParam.Threads, CPLEXTHREADS);
        if (CPLEXTHREADS == 1) cplex.setParam(IloCplex.IntParam.AuxRootThreads, -1);
        IloNumVar v0 = cplex.numVar(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, IloNumVarType.Float, "v0");
        cplex.setOut(null);
        cplex.addMaximize(v0);
        objectiveForPlayers.put(player, v0);
    }

    public double calculateLeaderStrategies(StackelbergConfig algConfig, Expander<SequenceInformationSet> expander) {
        double maxValue = Double.NEGATIVE_INFINITY;
        Set<Sequence> followerBR = new HashSet<Sequence>();
        Map<Sequence, Double> leaderResult = new HashMap<Sequence, Double>();

        try {
            IloCplex cplex = modelsForPlayers.get(leader);
            IloNumVar v0 = objectiveForPlayers.get(leader);

            long startTime = threadBean.getCurrentThreadCpuTime();
            createVariables(cplex, algConfig);
            createConstraintsForSets(cplex, algConfig.getAllInformationSets().values());
            createConstraintsForStates(cplex, algConfig.getAllLeafs());
            createConstraintsForSequences(algConfig, cplex, algConfig.getSequencesFor(follower));
            setObjective(cplex, v0, algConfig);
            debugOutput.println("phase 1 done");
            overallConstraintGenerationTime += threadBean.getCurrentThreadCpuTime() - startTime;

//            cplex.exportModel("stck-" + leader + ".lp"); // uncomment for model export
            startTime = threadBean.getCurrentThreadCpuTime();
            debugOutput.println("Solving");
            long cplexTime = threadBean.getCurrentThreadCpuTime();
            cplex.solve();

            System.out.println("cplex solving time: " + (threadBean.getCurrentThreadCpuTime() - cplexTime)/1e6);;
            overallConstraintLPSolvingTime += threadBean.getCurrentThreadCpuTime() - startTime;
            debugOutput.println("Status: " + cplex.getCplexStatus());

            if (cplex.getCplexStatus() == CplexStatus.Optimal || cplex.getCplexStatus() == CplexStatus.OptimalTol) {
                double v = cplex.getValue(v0);
                debugOutput.println("Best reward is " + v);

                maxValue = v;

                for (Map.Entry<Object, IloNumVar> ee : variables.entrySet()) {
                    try {
                        debugOutput.println(ee.getKey().toString() + "=" + cplex.getValue(ee.getValue()));
                    } catch (IloCplex.UnknownObjectException e) {
                        continue;
                    }
                }
//                debugOutput.println("-------");
//                for (Map.Entry<Object, IloNumVar> ee : slackVariables.entrySet()) {
//                    try {
//                        debugOutput.println(ee.getKey().toString() + "=" + cplex.getValue(ee.getValue()));
//                    } catch (IloCplex.UnknownObjectException e) {
//                        continue;
//                    }
//                }
                leaderResult = createSolution(algConfig, leader, cplex);
                debugOutput.println("leader rp: ");
                for (Map.Entry<Sequence, Double> entry : createSolution(algConfig, leader, cplex).entrySet()) {
                    if (entry.getValue() > 0)
                        debugOutput.println(entry);
                }
                debugOutput.println("follower rp: ");
                for (Map.Entry<Sequence, Double> entry : createSolution(algConfig, follower, cplex).entrySet()) {
                    if (entry.getValue() > 0)
                        debugOutput.println(entry);
                }
//                debugOutput.println("Leaf probs");
//                for (Map.Entry<Object, IloNumVar> entry : variables.entrySet()) {
//                    if (entry.getKey() instanceof GameState) {
//                        GameState possibleLeaf = (GameState) entry.getKey();
//
//                        try {
//                            if (cplex.getValue(entry.getValue()) > 0)
//                                debugOutput.println(entry.getKey() + ": " + cplex.getValue(entry.getValue()));
//                        } catch (IloException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
            }
        } catch (IloException e) {
            e.printStackTrace();
        }

        resultStrategies.put(leader, leaderResult);
        resultValues.put(leader, maxValue);

        return maxValue;
    }

    protected void createConstraintsForSequences(StackelbergConfig algConfig, IloCplex cplex, Collection<Sequence> VConstraints) throws IloException {
        for (Sequence firstPlayerSequence : VConstraints) {
            if (constraints.containsKey(firstPlayerSequence)) {
                cplex.delete(constraints.get(firstPlayerSequence));
                constraints.remove(firstPlayerSequence);
            }
            if (slackConstraints.containsKey(firstPlayerSequence)) {
                cplex.delete(slackConstraints.get(firstPlayerSequence));
                slackConstraints.remove(firstPlayerSequence);
            }
            createConstraintForSequence(cplex, firstPlayerSequence, algConfig);
            createSlackConstraintForSequence(cplex, firstPlayerSequence);
        }
    }

    protected void createVariables(IloCplex model, StackelbergConfig algConfig) throws IloException {
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

    protected IloNumVar createSlackVariableForSequence(IloCplex cplex, Sequence sequence) throws IloException {
        IloNumVar s = cplex.numVar(0, Double.POSITIVE_INFINITY, IloNumVarType.Float, "SL" + sequence.toString());
        slackVariables.put(sequence, s);
        return s;
    }

    protected double getUtility(StackelbergConfig algConfig, Map<Player, Sequence> sequenceCombination, Player firstPlayer) {
        Double utility = algConfig.getUtilityFor(sequenceCombination, firstPlayer);

        if (utility == null) {
            utility = 0d;
        }
        return utility;
    }

    protected IloNumExpr computeSumGR(IloCplex cplex, Sequence firstPlayerSequence, StackelbergConfig algConfig, Player firstPlayer) throws IloException {
        IloNumExpr sumGR = cplex.constant(0);
        HashSet<Sequence> secondPlayerSequences = new HashSet<Sequence>();

        if (algConfig.getCompatibleSequencesFor(firstPlayerSequence) != null)
            secondPlayerSequences.addAll(algConfig.getCompatibleSequencesFor(firstPlayerSequence));

        for (Sequence secondPlayerSequence : secondPlayerSequences) {
            IloNumExpr prob = variables.get(secondPlayerSequence);

            if (prob == null)
                continue;
            Map<Player, Sequence> actions = createActions(firstPlayerSequence, secondPlayerSequence);
            double utility = getUtility(algConfig, actions, firstPlayer);
            sumGR = cplex.sum(sumGR, cplex.prod(utility, prob));
        }
        return sumGR;
    }

    protected void createConstraintForSequence(IloCplex cplex, Sequence firstPlayerSequence, StackelbergConfig algConfig) throws IloException {
        Player firstPlayer = firstPlayerSequence.getPlayer();
        InformationSet informationSet = firstPlayerSequence.getLastInformationSet();
        IloNumExpr VI = null;
        IloNumExpr sumV = cplex.constant(0);

        if (informationSet == null) {
            if (firstPlayer.equals(follower)) return;
            VI = objectiveForPlayers.get(firstPlayer);
            for (SequenceInformationSet reachableSet : algConfig.getReachableSets(firstPlayerSequence)) {
                IloNumVar tmp = variables.get(reachableSet);

                assert (tmp != null);

                if (reachableSet.getOutgoingSequences() != null && reachableSet.getOutgoingSequences().size() > 0) {
                    sumV = cplex.sum(sumV, tmp);
                }
            }
        } else {
            VI = variables.get(informationSet);
            if (algConfig.getReachableSets(firstPlayerSequence) != null)
                for (SequenceInformationSet reachableSet : algConfig.getReachableSets(firstPlayerSequence)) {
                    if (reachableSet.getOutgoingSequences() == null || reachableSet.getOutgoingSequences().size() == 0)
                        continue;
                    IloNumVar tmp = variables.get(reachableSet);
                    assert (tmp != null);
                    sumV = cplex.sum(sumV, tmp);
                }
        }

        IloNumExpr sumGR = computeSumGR(cplex, firstPlayerSequence, algConfig, firstPlayer);
        if (firstPlayer.equals(follower)) {
            IloNumVar slack = slackVariables.get(firstPlayerSequence);
            IloRange con = cplex.addEq(cplex.diff(cplex.diff(cplex.diff(VI, sumV), sumGR), slack), 0, "CON:" + firstPlayerSequence.toString());
            constraints.put(firstPlayerSequence, con);
        }

//        IloRange con = cplex.addGe(cplex.diff(cplex.diff(VI, sumV), sumGR), 0, "CON2:" + firstPlayerSequence.toString());
//        constraints.put(firstPlayerSequence, con);
//        }

    }


    protected IloNumVar createIntegerVariableForSequence(IloCplex cplex, Sequence sequence) throws IloException {
        IloNumVar r = cplex.numVar(0, 1, IloNumVarType.Int, "R" + sequence.toString());

        if (sequence.size() == 0)
            r.setLB(1d);
        variables.put(sequence, r);
        return r;
    }

    protected IloNumVar createStateProbVariable(IloCplex cplex, GameState state) throws IloException {
        IloNumVar p = cplex.numVar(0, 1, IloNumVarType.Float, "P" + state.toString());
        variables.put(state, p);
        return p;
    }

    protected void createConstraintsForSets(IloCplex cplex, Collection<SequenceInformationSet> RConstraints) throws IloException {
        for (SequenceInformationSet infoSet : RConstraints) {
            if (constraints.containsKey(infoSet)) {
                cplex.delete(constraints.get(infoSet));
                constraints.remove(infoSet);
            }
            createConstraintForIS(cplex, infoSet);
        }
    }

    protected void createConstraintsForStates(IloCplex cplex, Collection<GameState> states) throws IloException {
//        for (SequenceInformationSet infoSet : infoSets) {
//            for (GameState state : infoSet.getAllStates()) {
        for (GameState state : states) {
            if (slackConstraints.containsKey(state)) {
                cplex.delete(slackConstraints.get(state)[0]);
                cplex.delete(slackConstraints.get(state)[1]);
                slackConstraints.remove(state);
            }
            if (constraints.containsKey(state)) {
                cplex.delete(constraints.get(state));
                constraints.remove(state);
            }
            createBoundConstraintsForState(cplex, state);
        }
//        }
    }

    protected void createBoundConstraintsForState(IloCplex cplex, GameState state) throws IloException {
        IloNumVar LS = variables.get(state);
        IloNumVar RSF = variables.get(state.getSequenceFor(follower));
        IloRange cF = cplex.addLe(cplex.diff(LS, RSF), 0, "LBC:F:" + state);
        IloNumVar RSL = variables.get(state.getSequenceFor(leader));
        IloRange cL = cplex.addLe(cplex.diff(LS, RSL), 0, "LBC:L:" + state);
        slackConstraints.put(state, new IloRange[]{cF, cL});
    }

    protected void createSlackConstraintForSequence(IloCplex cplex, Sequence sequence) throws IloException {
        IloNumVar LS = slackVariables.get(sequence);
        IloNumExpr RS = cplex.prod(M, cplex.diff(1, variables.get(sequence)));
        IloRange c = cplex.addLe(cplex.diff(LS, RS), 0, "SLC:" + sequence);
        slackConstraints.put(sequence, new IloRange[]{c, null});
    }

    protected void setObjective(IloCplex cplex, IloNumVar v0, StackelbergConfig algConfig) throws IloException {
        if (leaderObj != null)
            cplex.delete(leaderObj);
        IloNumExpr sumG = cplex.constant(0);
        IloNumExpr sumP = cplex.constant(0);
        for (Map.Entry<GameState, Double[]> e : algConfig.getActualNonZeroUtilityValuesInLeafsGenSum().entrySet()) {
            sumG = cplex.sum(sumG, cplex.prod(e.getKey().getNatureProbability(), cplex.prod(e.getValue()[leader.getId()], variables.get(e.getKey()))));
        }
        for (GameState gs : algConfig.getAllLeafs()) {
            sumP = cplex.sum(sumP, cplex.prod(gs.getNatureProbability(), variables.get(gs)));
        }
        leaderObj = cplex.addEq(cplex.diff(v0, sumG), 0);
        cplex.addEq(sumP, 1);
    }

}
