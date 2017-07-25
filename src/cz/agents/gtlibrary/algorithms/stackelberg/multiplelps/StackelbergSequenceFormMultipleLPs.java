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


package cz.agents.gtlibrary.algorithms.stackelberg.multiplelps;

import cz.agents.gtlibrary.algorithms.sequenceform.SQFBestResponseAlgorithm;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.stackelberg.GeneralSumBestResponse;
import cz.agents.gtlibrary.algorithms.stackelberg.StackelbergConfig;
import cz.agents.gtlibrary.algorithms.stackelberg.StackelbergSequenceFormLP;
import cz.agents.gtlibrary.algorithms.stackelberg.multiplelps.rpiterator.NoCutDepthPureRealPlanIterator;
import cz.agents.gtlibrary.algorithms.stackelberg.multiplelps.rpiterator.PureRealPlanIterator;
import cz.agents.gtlibrary.experimental.utils.UtilityCalculator;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.strategy.NoMissingSeqStrategy;
import cz.agents.gtlibrary.strategy.Strategy;
import ilog.concert.*;
import ilog.cplex.IloCplex;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.*;

public class StackelbergSequenceFormMultipleLPs extends StackelbergSequenceFormLP {

    protected Player[] players;
    protected GameInfo info;
    protected Expander<SequenceInformationSet> expander;
    protected ThreadMXBean mxBean;

    protected IloRange leaderObj = null;

    protected Map<Object, IloNumVar> slackVariables = new HashMap<>();
    protected int totalRPCount;
    protected int feasibilityCuts;
    private int evaluatedLPCount;


    public StackelbergSequenceFormMultipleLPs(Player[] players, Player leader, Player follower, GameInfo info, Expander<SequenceInformationSet> expander) {
        super(players, leader, follower);
        this.players = players;
        this.follower = follower;
        this.info = info;
        this.expander = expander;
        mxBean = ManagementFactory.getThreadMXBean();
    }


    protected void resetModel(IloCplex cplex, Player player) throws IloException {
        cplex.clearModel();
        cplex.setParam(IloCplex.IntParam.RootAlg, CPLEXALG);
        cplex.setParam(IloCplex.IntParam.Threads, CPLEXTHREADS);
        if (CPLEXTHREADS == 1)
            cplex.setParam(IloCplex.IntParam.AuxRootThreads, -1);
        IloNumVar v0 = cplex.numVar(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, IloNumVarType.Float, "v0");
        cplex.setOut(null);
        cplex.addMaximize(v0);
        objectiveForPlayers.put(player, v0);
    }

    public double calculateLeaderStrategies(StackelbergConfig algConfig, Expander<SequenceInformationSet> expander) {
        double maxValue = Double.NEGATIVE_INFINITY;
        int upperBoundCut = 0;
        int feasibilityCut = 0;
        int iteration = 0;
        int feasibilityCutWithoutObjective = 0;
        FeasibilitySequenceFormLP feasibilitySolver = new FeasibilitySequenceFormLP(leader, follower, algConfig, informationSets, sequences);
        Set<Sequence> followerBR = new HashSet<>();
        Map<Sequence, Double> leaderResult = new HashMap<>();

        totalRPCount = 0;
        try {
            IloCplex cplex = modelsForPlayers.get(leader);
            IloNumVar v0 = objectiveForPlayers.get(leader);
            long startTime = mxBean.getCurrentThreadCpuTime();

            createVariables(cplex, algConfig);
            createConstraintsForSets(leader, cplex, informationSets.get(leader));
            createConstraintsForSequences(algConfig, cplex, algConfig.getSequencesFor(follower));
            debugOutput.println("phase 1 done");
            overallConstraintGenerationTime += mxBean.getCurrentThreadCpuTime() - startTime;

            PureRealPlanIterator iterator = algConfig.getIterator(follower, expander, feasibilitySolver);

            while (true) {
                Set<Sequence> pureRP = iterator.next();
//                debugOutput.println(iteration);

//                debugOutput.println("---");
//                for (Sequence sequence : pureRP) {
//                    debugOutput.println(sequence);
//                }
//                assert Math.abs(getUpperBound(pureRP, algConfig) - iterator.getCurrentUpperBound()) < 1e-8;
                totalRPCount++;
                if (maxValue == info.getMaxUtility()) {//TODO: max utility for both players
                    break;
                }
                iteration++;
                if (feasibilitySolver.checkFeasibilityFor(pureRP, maxValue)) {
                    setValueForBRSlack(cplex, pureRP, 0);
                    updateObjective(cplex, v0, pureRP, algConfig);
                    addBestValueConstraint(cplex, v0, maxValue + 1e-5);
//                  cplex.exportModel("stck-" + leader + ".lp"); // uncomment for model export
                    startTime = mxBean.getCurrentThreadCpuTime();
//                  debugOutput.println("Solving");
                    cplex.solve();
                    overallConstraintLPSolvingTime += mxBean.getCurrentThreadCpuTime() - startTime;
//                    debugOutput.println("Status: " + cplex.getCplexStatus());

                    if (cplex.getStatus() == IloCplex.Status.Optimal) {
                        double v = cplex.getValue(v0);

                        evaluatedLPCount++;
                        System.out.println(iteration);
                        debugOutput.println("Best reward is " + v + " for follower strategy: ");
                        for (Sequence sequence : pureRP) {
                            debugOutput.println(sequence);
                        }
                        debugOutput.println("Leader's strategy: ");
                        for (Map.Entry<Sequence, Double> entry : createSolution(algConfig, leader, cplex).entrySet()) {
                            if (entry.getValue() > 0)
                                debugOutput.println(entry);
                        }
                        if (v > maxValue) {
                            maxValue = v;
                            iterator.setBestValue(maxValue);
                            resultStrategies.put(leader, createSolution(algConfig, leader, cplex));
                            followerBR = pureRP;
                            leaderResult = createSolution(algConfig, leader, cplex);
                        }
                    } else {
                        feasibilityCut++;
                    }
                } else {
                    feasibilityCutWithoutObjective++;
                }
                setValueForBRSlack(cplex, pureRP, 1);

            }
        } catch (IloException e) {
            e.printStackTrace();
        } catch (NoSuchElementException e) {

        }
        resultStrategies.put(leader, leaderResult);
        resultValues.put(leader, maxValue);
        System.out.println("final result with reward " + maxValue + ": ");
        for (Map.Entry<Sequence, Double> entry : leaderResult.entrySet()) {
            if (entry.getValue() > 0)
                System.out.println(entry);
        }
        System.out.println("Upper bound cuts: " + upperBoundCut);
        System.out.println("Feasibility cuts: " + feasibilityCut);
        System.out.println("Total RP count: " + totalRPCount);
        System.out.println("Feasibility cut without obj: " + feasibilityCutWithoutObjective);
        System.out.println("Feasibility time: " + feasibilitySolver.getCplexSolvingTime() / 1000000l);
        feasibilityCuts = feasibilityCut + feasibilityCutWithoutObjective;
        return maxValue;
    }

    protected void deleteObjectiveConstraint(IloCplex cplex) throws IloException {
        if (leaderObj != null)
            cplex.delete(leaderObj);
    }

    protected double getUtility(Map<Sequence, Double> leaderStrategy, Map<Sequence, Double> followerStrategy, StackelbergConfig algConfig) {
        UtilityCalculator calculator = new UtilityCalculator(algConfig.getRootState(), expander);
        Strategy leaderStrat = new NoMissingSeqStrategy(leaderStrategy);
        Strategy followerStrat = new NoMissingSeqStrategy(followerStrategy);

        leaderStrat.sanityCheck(algConfig.getRootState(), expander);
        if (leader.getId() == 0)
            return calculator.computeUtility(leaderStrat, followerStrat);
        else
            return calculator.computeUtility(followerStrat, leaderStrat);
    }

    protected double getUpperBound(Set<Sequence> pureRP, StackelbergConfig config) {
        SQFBestResponseAlgorithm bestResponse = new GeneralSumBestResponse(expander, leader.getId(), players, config, info);

        return bestResponse.calculateBR(config.getRootState(), getRP(pureRP));
    }

    protected Map<Sequence, Double> getRP(Iterable<Sequence> sequences) {
        Map<Sequence, Double> rp = new HashMap<>();

        for (Sequence sequence : sequences) {
            rp.put(sequence, 1d);
        }
        return rp;
    }

    protected void addBestValueConstraint(IloCplex cplex, IloNumVar v0, double maxValue) throws IloException {
        IloRange maxValueConstraint = constraints.get("maxVal");

        if (maxValueConstraint == null) {
            IloLinearNumExpr rowExpr = cplex.linearNumExpr();

            rowExpr.addTerm(1, v0);
            maxValueConstraint = cplex.addGe(rowExpr, maxValue);
            constraints.put("maxVal", maxValueConstraint);
        }
        maxValueConstraint.setLB(maxValue);
    }

    protected Map<Object, Double> getSlackVariableValues(IloCplex cplex) {
        Map<Object, Double> slackValues = new HashMap<>();

        try {
            for (Map.Entry<Object, IloNumVar> slackEntry : slackVariables.entrySet()) {
                slackValues.put(slackEntry.getKey(), cplex.getValue(slackEntry.getValue()));
            }
        } catch (IloException e) {
            e.printStackTrace();
        }
        return slackValues;
    }

    protected void createConstraintsForSequences(StackelbergConfig algConfig, IloCplex cplex, Collection<Sequence> VConstraints) throws IloException {
        for (Sequence firstPlayerSequence : VConstraints) {
            if (constraints.containsKey(firstPlayerSequence)) {
                cplex.delete(constraints.get(firstPlayerSequence));
                constraints.remove(firstPlayerSequence);
            }
            createConstraintForSequence(cplex, firstPlayerSequence, algConfig);
        }
    }

    protected void createVariables(IloCplex model, StackelbergConfig algConfig) throws IloException {
        for (Sequence sequence : algConfig.getAllSequences()) {
            if (variables.containsKey(sequence)) continue;
            if (sequence.getPlayer().equals(leader)) {
                createVariableForSequence(model, sequence);
                sequences.get(sequence.getPlayer()).add(sequence);
            } else {
                createSlackVariableForSequence(model, sequence);
            }
        }
        for (SequenceInformationSet informationSet : algConfig.getAllInformationSets().values()) {
            if (!variables.containsKey(informationSet)) {
                createVariableForIS(model, informationSet);
                informationSets.get(informationSet.getPlayer()).add(informationSet);
            }
        }
        debugOutput.println("variables created");
    }

    protected IloNumVar createSlackVariableForSequence(IloCplex cplex, Sequence sequence) throws IloException {
        IloNumVar s = cplex.numVar(0, Double.POSITIVE_INFINITY, IloNumVarType.Float, "SL" + sequence.toString());
        slackVariables.put(sequence, s);
        return s;
    }

    protected static double getUtility(StackelbergConfig algConfig, Map<Player, Sequence> sequenceCombination, Player firstPlayer) {
        Double utility = algConfig.getUtilityFor(sequenceCombination, firstPlayer);

        if (utility == null) {
            utility = 0d;
        }
        return utility;
    }

    protected IloNumExpr computeSumGR(IloCplex cplex, Sequence firstPlayerSequence, StackelbergConfig algConfig, Player firstPlayer) throws IloException {
        IloNumExpr sumGR = cplex.constant(0);
        HashSet<Sequence> secondPlayerSequences = new HashSet<>();

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
                    IloNumVar tmp = variables.get(reachableSet);

                    assert (tmp != null);
                    if (reachableSet.getOutgoingSequences() == null || reachableSet.getOutgoingSequences().size() == 0)
                        continue;
                    sumV = cplex.sum(sumV, tmp);
                }
        }

        IloNumExpr sumGR = computeSumGR(cplex, firstPlayerSequence, algConfig, firstPlayer);
        if (firstPlayer.equals(follower)) {
            IloNumVar slack = slackVariables.get(firstPlayerSequence);
            IloRange con = cplex.addEq(cplex.diff(cplex.diff(cplex.diff(VI, sumV), sumGR), slack), 0, "CON:" + firstPlayerSequence.toString());
            constraints.put(firstPlayerSequence, con);
        } else {
            IloRange con = cplex.addEq(cplex.diff(cplex.diff(VI, sumV), sumGR), 0, "CON:" + firstPlayerSequence.toString());
            constraints.put(firstPlayerSequence, con);
        }

    }

    protected void setValueForBRSlack(IloCplex cplex, Iterable<Sequence> sequences, int value) throws IloException {
        for (Sequence s : sequences) {
            IloRange constraint = constraints.get(s);
            IloNumVar slack = slackVariables.get(s);
            if (constraint == null) {
                if (s.size() == 0) continue;
                assert false;
            }
            cplex.setLinearCoef(constraint, slack, -value);
        }
    }

    protected void updateObjective(IloCplex cplex, IloNumVar v0, Set<Sequence> bestResponse, StackelbergConfig algConfig) throws IloException {
        deleteObjectiveConstraint(cplex);
        IloNumExpr sumG = cplex.constant(0);

        for (Sequence s : bestResponse) {
            HashSet<Sequence> leaderCompSequences = new HashSet<>();

            if (algConfig.getCompatibleSequencesFor(s) != null)
                leaderCompSequences.addAll(algConfig.getCompatibleSequencesFor(s));

            for (Sequence ls : leaderCompSequences) {
                IloNumExpr prob = variables.get(ls);

                if (prob == null)
                    continue;
                Map<Player, Sequence> actions = createActions(ls, s);
                double utility = getUtility(algConfig, actions, leader);

                if (Math.abs(utility) > 1e-13)
                    sumG = cplex.sum(sumG, cplex.prod(utility, prob));
            }
        }
        leaderObj = cplex.addEq(cplex.diff(v0, sumG), 0);
    }

    public int prunnedRPCountWhileBuilding(StackelbergConfig config) {
        return getAllRPCount(config) - totalRPCount;
    }

    public int getAllRPCount(StackelbergConfig config) {
        PureRealPlanIterator iterator = new NoCutDepthPureRealPlanIterator(follower, config, expander, new EmptyFeasibilitySequenceFormLP(leader, follower, config, informationSets, sequences));
        int allRPCount = 0;
        try {
            while (true) {
                iterator.next();
                allRPCount++;
            }
        } catch (NoSuchElementException e) {

        }
        return allRPCount;
    }

    public int getTotalRPCount() {
        return totalRPCount;
    }

    public int getFeasibilityCuts() {
        return feasibilityCuts;
    }

    public int getEvaluatedLPCount() {
        return evaluatedLPCount;
    }
}
