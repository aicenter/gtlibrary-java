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


package cz.agents.gtlibrary.algorithms.flipit.bayesian.milp;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.stackelberg.StackelbergConfig;
import cz.agents.gtlibrary.algorithms.stackelberg.StackelbergSequenceFormLP;
import cz.agents.gtlibrary.domain.flipit.FlipItExpander;
import cz.agents.gtlibrary.domain.flipit.FlipItGameInfo;
import cz.agents.gtlibrary.domain.flipit.types.FollowerType;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.Pair;
import ilog.concert.*;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.CplexStatus;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.*;

public class BayesianStackelbergSequenceFormMILP extends StackelbergSequenceFormLP {
    protected double M;

    protected Player[] players;
    protected FlipItGameInfo info;

    protected IloRange leaderObj = null;

    protected Map<Object, IloNumVar> slackVariables = new HashMap<>();
    protected Map<Object, IloRange[]> slackConstraints = new HashMap<>(); // constraints for slack variables and p(h)
    protected FlipItExpander<SequenceInformationSet> expander;

    protected ThreadMXBean threadBean;

    protected double EPS = 0.00000001;

    protected boolean OUTPUT_STRATEGY = false;


    public BayesianStackelbergSequenceFormMILP(Player[] players, Player leader, Player follower, FlipItGameInfo info, FlipItExpander<SequenceInformationSet> expander) {
        super(players, leader, follower);
        this.players = players;
        this.expander = expander;
        this.info = info;
        this.threadBean = ManagementFactory.getThreadMXBean();
        M = info.getMaxUtility()*info.getUtilityStabilizer()*2 + 1;

//        System.out.println("MILP representation");
    }

    public void setOUTPUT_STRATEGY(boolean output_strategy){
        this.OUTPUT_STRATEGY = output_strategy;
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
            createConstraintsForSets(cplex, algConfig.getAllInformationSets().values()); // rps
            createConstraintsForStates(cplex, algConfig.getAllLeafs()); // ps
            createConstraintsForSequences(algConfig, cplex, algConfig.getSequencesFor(follower));
            setObjective(cplex, v0, algConfig);
            debugOutput.println("phase 1 done");
            overallConstraintGenerationTime += threadBean.getCurrentThreadCpuTime() - startTime;

//            cplex.exportModel(info.getLpExportName() + ".lp"); // uncomment for model export
//            cplex.exportModel("BSSE_MILP.lp");

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
                if (OUTPUT_STRATEGY) {
                    leaderResult = createSolution(algConfig, leader, cplex);
                    debugOutput.println("leader rp: ");
                    for (Map.Entry<Sequence, Double> entry : createSolution(algConfig, leader, cplex).entrySet()) {
                        if (entry.getValue() > 0)
                            debugOutput.println(entry);
                    }
                    for (FollowerType type : FlipItGameInfo.types) {
                        debugOutput.println("follower type = " + type.toString() + " rp: ");
                        for (Map.Entry<Sequence, Double> entry : createSolution(algConfig, follower, type, cplex).entrySet()) {
                            if (entry.getValue() > 0)
                                debugOutput.println(entry);
                        }
                    }
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

    protected Map<Sequence, Double> createSolution(SequenceFormConfig<SequenceInformationSet> algConfig, Player secondPlayer, FollowerType type, IloCplex cplex) throws IloException {
        Map<Sequence, Double> solution = new HashMap<Sequence, Double>();

        for (Sequence sequence : algConfig.getSequencesFor(secondPlayer)) {
            try {
                double relPl = cplex.getValue(variables.get(new Pair(sequence,type)));
                if (relPl < EPS) relPl = 0;
                if (sequence.size() == 0)
                    relPl = 1;
                solution.put(sequence, relPl);
            } catch (IloCplex.UnknownObjectException e) {
                if (sequence.size() == 0)
                    solution.put(sequence, 1d);
                else
                    solution.put(sequence, 0d);
            }
        }
        return solution;
    }

    protected void createConstraintsForSequences(StackelbergConfig algConfig, IloCplex cplex, Collection<Sequence> VConstraints) throws IloException {
        for (Sequence firstPlayerSequence : VConstraints) {
            for (FollowerType type : FlipItGameInfo.types) {
                Pair seqType = new Pair(firstPlayerSequence,type);
                if (constraints.containsKey(seqType)) {
                    cplex.delete(constraints.get(seqType));
                    constraints.remove(seqType);
                }
                if (slackConstraints.containsKey(seqType)) {
                    cplex.delete(slackConstraints.get(seqType));
                    slackConstraints.remove(seqType);
                }
                createConstraintForSequence(cplex, firstPlayerSequence, type, algConfig);
                createSlackConstraintForSequence(cplex, type, firstPlayerSequence);
            }
        }
    }

    protected void createVariables(IloCplex model, StackelbergConfig algConfig) throws IloException {
        for (Sequence sequence : algConfig.getAllSequences()) {
            if (variables.containsKey(sequence)) continue;
            if (sequence.getPlayer().equals(leader)) {
                createVariableForSequence(model, sequence);
            } else {
                for (FollowerType type : FlipItGameInfo.types) {
                    createIntegerVariableForSequence(model, sequence, type);
                    createSlackVariableForSequence(model, sequence, type);
                }
            }
        }

        for (GameState gs : algConfig.getAllLeafs()) {
            for (FollowerType type : FlipItGameInfo.types) {
                createStateProbVariable(model, gs, type);
            }
        }

        for (SequenceInformationSet informationSet : algConfig.getAllInformationSets().values()) {
            if (!variables.containsKey(informationSet)) {
                if (informationSet.getPlayer().equals(leader)) {
                    //nothing
                } else {
                    for (FollowerType type : FlipItGameInfo.types) {
                        createVariableForIS(model, informationSet, type);
                    }
                    informationSets.get(informationSet.getPlayer()).add(informationSet);
                }
            }
        }
        debugOutput.println("variables created");
    }

    protected IloNumVar createVariableForIS(IloCplex cplex, InformationSet is, FollowerType type) throws IloException {
        double ub = Double.POSITIVE_INFINITY;
        IloNumVar v = cplex.numVar(Double.NEGATIVE_INFINITY, ub, IloNumVarType.Float, "V" + is.toString() + "_" + type.toString());

        variables.put(new Pair(is,type), v);
        return v;
    }

    protected IloNumVar createSlackVariableForSequence(IloCplex cplex, Sequence sequence, FollowerType type) throws IloException {
        IloNumVar s = cplex.numVar(0, Double.POSITIVE_INFINITY, IloNumVarType.Float, "SL" + sequence.toString() + "_" + type.toString());
        slackVariables.put(new Pair(sequence,type), s);
        return s;
    }

    protected double getUtility(StackelbergConfig algConfig, Map<Player, Sequence> sequenceCombination, Player firstPlayer, FollowerType type) {
        Double utility = 0.0;
        // identify ID
        for (int i = 0; i < FlipItGameInfo.types.length; i++)
            if (FlipItGameInfo.types[i].equals(type))
                utility = algConfig.getUtilityFor(sequenceCombination, i+1);
        if (utility == null) {
            utility = 0d;
        }
        return utility;
    }

    protected IloNumExpr computeSumGR(IloCplex cplex, Sequence firstPlayerSequence, FollowerType type, StackelbergConfig algConfig, Player firstPlayer) throws IloException {
        IloNumExpr sumGR = cplex.constant(0);
        HashSet<Sequence> secondPlayerSequences = new HashSet<Sequence>();

        if (algConfig.getCompatibleSequencesFor(firstPlayerSequence) != null)
            secondPlayerSequences.addAll(algConfig.getCompatibleSequencesFor(firstPlayerSequence));

        for (Sequence secondPlayerSequence : secondPlayerSequences) {
            IloNumExpr prob = variables.get(secondPlayerSequence); // leader rp

            if (prob == null)
                continue;
            Map<Player, Sequence> actions = createActions(firstPlayerSequence, secondPlayerSequence);
            double utility = getUtility(algConfig, actions, firstPlayer,type);
            sumGR = cplex.sum(sumGR, cplex.prod(utility, prob));
        }
        return sumGR;
    }

    protected void createConstraintForSequence(IloCplex cplex, Sequence firstPlayerSequence, FollowerType type, StackelbergConfig algConfig) throws IloException {
        Player firstPlayer = firstPlayerSequence.getPlayer();
        InformationSet informationSet = firstPlayerSequence.getLastInformationSet();
        IloNumExpr VI = null;
        IloNumExpr sumV = cplex.constant(0);

        Pair seqType = new Pair(firstPlayerSequence,type);

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
            VI = variables.get(new Pair(informationSet,type));
            if (algConfig.getReachableSets(firstPlayerSequence) != null)
                for (SequenceInformationSet reachableSet : algConfig.getReachableSets(firstPlayerSequence)) {
                    if (reachableSet.getOutgoingSequences() == null || reachableSet.getOutgoingSequences().size() == 0)
                        continue;
                    IloNumVar tmp = variables.get(new Pair(reachableSet,type));
                    assert (tmp != null);
                    sumV = cplex.sum(sumV, tmp);
                }
        }

        IloNumExpr sumGR = computeSumGR(cplex, firstPlayerSequence, type, algConfig, firstPlayer);
        if (firstPlayer.equals(follower)) {
            IloNumVar slack = slackVariables.get(seqType);
            IloRange con = cplex.addEq(cplex.diff(cplex.diff(cplex.diff(VI, sumV), sumGR), slack), 0, "CON:" + firstPlayerSequence.toString() + "_" + type.toString());
            constraints.put(seqType, con);
        }

//        IloRange con = cplex.addGe(cplex.diff(cplex.diff(VI, sumV), sumGR), 0, "CON2:" + firstPlayerSequence.toString());
//        constraints.put(firstPlayerSequence, con);
//        }

    }


    protected IloNumVar createIntegerVariableForSequence(IloCplex cplex, Sequence sequence, FollowerType type) throws IloException {
        IloNumVar r = cplex.numVar(0, 1, IloNumVarType.Int, "R" + sequence.toString() + "_" + type.toString());

        if (sequence.size() == 0)
            r.setLB(1d);
        variables.put(new Pair(sequence,type), r);
        return r;
    }

    protected IloNumVar createStateProbVariable(IloCplex cplex, GameState state, FollowerType type) throws IloException {
        IloNumVar p = cplex.numVar(0, 1, IloNumVarType.Float, "P" + state.toString() + "_" + type.toString());
        variables.put(new Pair(state,type), p);
        return p;
    }

    protected void createConstraintsForSets(IloCplex cplex, Collection<SequenceInformationSet> RConstraints) throws IloException {
        for (SequenceInformationSet infoSet : RConstraints) {
            if (infoSet.getPlayer().equals(FlipItGameInfo.DEFENDER)){
                if (constraints.containsKey(infoSet)) {
                    cplex.delete(constraints.get(infoSet));
                    constraints.remove(infoSet);
                }
                createConstraintForIS(cplex, infoSet);
            }
            else {
                for (FollowerType type : FlipItGameInfo.types) {
                    Pair ISType = new Pair(infoSet, type);
                    if (constraints.containsKey(ISType)) {
                        cplex.delete(constraints.get(ISType));
                        constraints.remove(ISType);
                    }
                    createConstraintForIS(cplex, infoSet, type);
                }
            }
        }
    }

    protected IloRange createConstraintForIS(IloCplex cplex, SequenceInformationSet informationSet) throws IloException {
        IloNumExpr sumL = cplex.constant(0);

        if (informationSet.getOutgoingSequences().isEmpty()) {
            return null;
        }
        for (Sequence sequence : informationSet.getOutgoingSequences()) {
            if (variables.get(sequence) == null)
                continue;
            sumL = cplex.sum(sumL, variables.get(sequence));
        }

        Sequence sequence = informationSet.getPlayersHistory();
        IloNumExpr sumR = variables.get(sequence);

        if (sumR == null)
            return null;

        IloRange constrain = cplex.addEq(cplex.diff(sumL, sumR), 0, "CON:" + informationSet.toString());

        constraints.put(informationSet, constrain);
        return constrain;
    }

    protected IloRange createConstraintForIS(IloCplex cplex, SequenceInformationSet informationSet, FollowerType type) throws IloException {
        IloNumExpr sumL = cplex.constant(0);

        if (informationSet.getOutgoingSequences().isEmpty()) {
            return null;
        }
        for (Sequence sequence : informationSet.getOutgoingSequences()) {
            if (variables.get(new Pair(sequence,type)) == null)
                continue;
            sumL = cplex.sum(sumL, variables.get(new Pair(sequence,type)));
        }

        Sequence sequence = informationSet.getPlayersHistory();
        IloNumExpr sumR = variables.get(new Pair(sequence,type));

        if (sumR == null)
            return null;

        IloRange constrain = cplex.addEq(cplex.diff(sumL, sumR), 0, "CON:" + informationSet.toString() + "_" + type.toString());

        constraints.put(new Pair(informationSet,type), constrain);
        return constrain;
    }

    protected void createConstraintsForStates(IloCplex cplex, Collection<GameState> states) throws IloException {
//        for (SequenceInformationSet infoSet : infoSets) {
//            for (GameState state : infoSet.getAllStates()) {
        for (GameState state : states) {
            for (FollowerType type : FlipItGameInfo.types) {
                Pair STType = new Pair(state, type);
                if (slackConstraints.containsKey(STType)) {
                    cplex.delete(slackConstraints.get(STType)[0]);
                    cplex.delete(slackConstraints.get(STType)[1]);
                    slackConstraints.remove(STType);
                }
                if (constraints.containsKey(STType)) {
                    cplex.delete(constraints.get(STType));
                    constraints.remove(STType);
                }
                createBoundConstraintsForState(cplex, state, type);
            }
        }
//        }
    }

    protected void createBoundConstraintsForState(IloCplex cplex, GameState state, FollowerType type) throws IloException {
        IloNumVar LS = variables.get(new Pair(state,type)); // p^t(z)
        IloNumVar RSF = variables.get(new Pair(state.getSequenceFor(follower),type));
        IloRange cF = cplex.addLe(cplex.diff(LS, RSF), 0, "LBC:F:" + state + type);
        IloNumVar RSL = variables.get(state.getSequenceFor(leader)); // realizacni plan jen pro leadera
        IloRange cL = cplex.addLe(cplex.diff(LS, RSL), 0, "LBC:L:" + state + type);
        slackConstraints.put(new Pair(state,type), new IloRange[]{cF, cL});
    }

    protected void createSlackConstraintForSequence(IloCplex cplex, FollowerType type, Sequence sequence) throws IloException {
        IloNumVar LS = slackVariables.get(new Pair(sequence,type));
        IloNumExpr RS = cplex.prod(M, cplex.diff(1, variables.get(new Pair(sequence,type))));
        IloRange c = cplex.addLe(cplex.diff(LS, RS), 0, "SLC:" + sequence + "_" + type);
        slackConstraints.put(new Pair(sequence,type), new IloRange[]{c, null});
    }

    protected void setObjective(IloCplex cplex, IloNumVar v0, StackelbergConfig algConfig) throws IloException {
        if (leaderObj != null)
            cplex.delete(leaderObj);
        IloNumExpr sumG = cplex.constant(0);
        for (FollowerType type : FlipItGameInfo.types) {
            IloNumExpr sumP = cplex.constant(0);
            for (Map.Entry<GameState, Double[]> e : algConfig.getActualNonZeroUtilityValuesInLeafsGenSum().entrySet()) {
//                System.out.println("OBJECTIVE : " +  e.getKey().getNatureProbability() + " " + e.getValue()[leader.getId()]);
                sumG = cplex.sum(sumG, cplex.prod(e.getKey().getNatureProbability(), cplex.prod(e.getValue()[leader.getId()], cplex.prod(type.getPrior(),variables.get(new Pair(e.getKey(),type))))));
            }
            for (GameState gs : algConfig.getAllLeafs()) {
                sumP = cplex.sum(sumP, cplex.prod(gs.getNatureProbability(), variables.get(new Pair(gs,type))));
            }
            cplex.addEq(sumP, 1);
        }
        leaderObj = cplex.addEq(cplex.diff(v0, sumG), 0);
    }

}
