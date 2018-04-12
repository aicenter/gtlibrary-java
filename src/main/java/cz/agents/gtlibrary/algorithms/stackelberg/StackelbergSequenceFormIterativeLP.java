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


package cz.agents.gtlibrary.algorithms.stackelberg;

import cz.agents.gtlibrary.algorithms.sequenceform.SQFBestResponseAlgorithm;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormLP;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.experimental.utils.UtilityCalculator;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.strategy.NoMissingSeqStrategy;
import cz.agents.gtlibrary.strategy.Strategy;
import cz.agents.gtlibrary.utils.Pair;
import ilog.concert.*;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.CplexStatus;

import java.util.*;

public class StackelbergSequenceFormIterativeLP extends SequenceFormLP {

    private Player leader;
    private Player follower;
    private Player[] players;
    private GameInfo info;
    private Expander<SequenceInformationSet> expander;

    private TreeSet<GameState> sortedLeafs = new TreeSet<>(new LeafComparator());
    private GeneralSumBestResponse[] bestResponses = new GeneralSumBestResponse[] {null, null};

    private IloRange leaderObj = null;

    protected Map<Object, IloNumVar> slackVariables = new HashMap<>();


    public StackelbergSequenceFormIterativeLP(Player[] players, GameInfo info, Expander<SequenceInformationSet> expander) {
        super(players);
        this.players = players;
        this.info = info;
        this.expander = expander;
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



    public double calculateLeaderStrategies(int leaderIdx, int followerIdx, StackelbergConfig algConfig, Expander expander) {
        leader = players[leaderIdx];
        follower = players[followerIdx];
        bestResponses[leaderIdx] = new GeneralSumBestResponse(expander, leaderIdx, players, algConfig, info);
        bestResponses[followerIdx] = new GeneralSumBestResponse(expander, followerIdx, players, algConfig, info);

//        double maxValue = Double.NEGATIVE_INFINITY;
//        int upperBoundCut = 0;
//        int feasibilityCut = 0;
//        int totalRPCount = 0;
        int iteration = 0;
        Set<Sequence> followerBR = new HashSet<>();
        Map<Sequence, Double> leaderResult = new HashMap<>();

        sortedLeafs.addAll(algConfig.getAllLeafs());
        double upperBound = Double.MAX_VALUE;
        double lowerBound = -Double.MAX_VALUE;

        try {
            IloCplex cplex = modelsForPlayers.get(leader);
            IloNumVar v0 = objectiveForPlayers.get(leader);


            long startTime = System.currentTimeMillis();
            createVariables(cplex, algConfig);
            createConstraintsForSets(leader, cplex, informationSets.get(leader));
            createConstraintsForSequences(algConfig, cplex, algConfig.getSequencesFor(follower));
            debugOutput.println("phase 1 done");
            overallConstraintGenerationTime += System.currentTimeMillis() - startTime;

            while ((upperBound - lowerBound) > 0.00001 && !sortedLeafs.isEmpty()) {
                GameState leaf = sortedLeafs.pollFirst();
                Set<Sequence> leaderSequences = new HashSet<>();
                leaderSequences.addAll(leaf.getSequenceFor(leader).getAllPrefixes());

                upperBound = leaf.getUtilities()[leaderIdx];
                debugOutput.println("leader sequence : " + leaderSequences);
                debugOutput.println("UB:"+upperBound);

                Pair<Double, Set<Sequence>> tmp = calculateBestResponse(leaderSequences, algConfig, follower);
                followerBR = tmp.getRight();
                debugOutput.println("   follower BR" + followerBR);
//                upperBound = getUpperBound(pureRP, algConfig);
//                debugOutput.println(iteration);

//                debugOutput.println("---");
//                for (Sequence sequence : pureRP) {
//                    debugOutput.println(sequence);
//                }
//                totalRPCount++;
//                if (maxValue == info.getMaxUtility()) {//TODO: max utility for both players
//                    break;
//                }
                iteration++;
//                if (maxValue >= upperBound - 1e-7) {
//                    upperBoundCut++;
//                    continue;
//                }
                setValueForBRSlack(cplex, followerBR, 0);
                updateObjective(cplex, v0, followerBR, algConfig);
//                addBestValueConstraint(cplex, v0, maxValue + 1e-5);

//                cplex.exportModel("stck-" + leader + ".lp"); // uncomment for model export
                startTime = System.currentTimeMillis();
//                debugOutput.println("Solving");
                cplex.solve();
                overallConstraintLPSolvingTime += System.currentTimeMillis() - startTime;
//                debugOutput.println("Status: " + cplex.getCplexStatus());

                if (cplex.getCplexStatus() == CplexStatus.Optimal) {
                    double v = cplex.getValue(v0);
                    debugOutput.println(" LB: " + v /*+ " comp v " + getUtility(createSolution(algConfig, leader, cplex), getRP(pureRP), algConfig)*/);
//                    assert v <= upperBound;
//                    GeneralSumBestResponse br = new GeneralSumBestResponse(expander, followerIdx, players, algConfig, info);

//                    debugOutput.println("Best reward is " + v + " for follower strategy: ");
//                    for (Sequence sequence : pureRP) {
//                        debugOutput.println(sequence);
//                    }
//                    debugOutput.println("Leader's strategy: ");
//                    for (Map.Entry<Sequence, Double> entry : createSolution(algConfig, leader, cplex).entrySet()) {
//                        if (entry.getValue() > 0)
//                            debugOutput.println(entry);
//                    }
                    if (v > lowerBound) {
                        lowerBound = v;
                        resultStrategies.put(leader, createSolution(algConfig, leader, cplex));
//                        resultStrategies.put(follower, followerBR);
                        leaderResult = createSolution(algConfig, leader, cplex);
                    }
                }
                setValueForBRSlack(cplex, followerBR, 1);

            }
        } catch (IloException e) {
            e.printStackTrace();
        } catch (NoSuchElementException e) {

        }
        resultStrategies.put(leader, leaderResult);
        resultValues.put(leader, lowerBound);
        System.out.println("final result with reward " + lowerBound + ": ");
//        for (Map.Entry<Sequence, Double> entry : leaderResult.entrySet()) {
//            if (entry.getValue() > 0)
//                System.out.println(entry);
//        }
//        System.out.println("Upper bound cuts: " + upperBoundCut);
//        System.out.println("Feasibility cuts: " + feasibilityCut);
//        System.out.println("Total RP count: " + totalRPCount);
        return lowerBound;
    }

    public boolean checkFeasibilityFor(Iterable<Sequence> partialPureRp) {
        try {
            IloCplex cplex = modelsForPlayers.get(leader);

            deleteObjectiveConstraint(cplex);
            cplex.getObjective().clearExpr();
            setValueForBRSlack(cplex, partialPureRp, 0);
            cplex.solve();
            setValueForBRSlack(cplex, partialPureRp, 1);
            cplex.getObjective().setExpr(objectiveForPlayers.get(leader));
            if(cplex.getStatus() == IloCplex.Status.Optimal)
                return true;
        } catch (IloException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void deleteObjectiveConstraint(IloCplex cplex) throws IloException {
        if (leaderObj != null)
            cplex.delete(leaderObj);
    }

    private double getUtility(Map<Sequence, Double> leaderStrategy, Map<Sequence, Double> followerStrategy, StackelbergConfig algConfig) {
        UtilityCalculator calculator = new UtilityCalculator(algConfig.getRootState(), expander);
        Strategy leaderStrat = new NoMissingSeqStrategy(leaderStrategy);
        Strategy followerStrat = new NoMissingSeqStrategy(followerStrategy);

        leaderStrat.sanityCheck(algConfig.getRootState(), expander);
        if (leader.getId() == 0)
            return calculator.computeUtility(leaderStrat, followerStrat);
        else
            return calculator.computeUtility(followerStrat, leaderStrat);
    }

    private Pair<Double, Set<Sequence>> calculateBestResponse(Set<Sequence> pureRP, StackelbergConfig config, Player player) {
        double brValue = bestResponses[player.getId()].calculateBR(config.getRootState(), getRP(pureRP));
        Set<Sequence> br = bestResponses[player.getId()].getBRSequences();
        return new Pair<>(brValue, br);
    }

    private Map<Sequence, Double> getRP(Iterable<Sequence> sequences) {
        Map<Sequence, Double> rp = new HashMap<>();

        for (Sequence sequence : sequences) {
            rp.put(sequence, 1d);
        }
        return rp;
    }

    private void addBestValueConstraint(IloCplex cplex, IloNumVar v0, double maxValue) throws IloException {
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

    protected class LeafComparator implements Comparator<GameState> {

        @Override
        public int compare(GameState o1, GameState o2) {
            if (o1.getUtilities()[leader.getId()] > o2.getUtilities()[leader.getId()])
                    return -1;
            if (o1.getUtilities()[leader.getId()] < o2.getUtilities()[leader.getId()])
                return 1;
            if (o1.getUtilities()[leader.getId()] == o2.getUtilities()[leader.getId()]) {
                if (o1.hashCode() > o2.hashCode())
                    return -1;
                if (o1.hashCode() < o2.hashCode())
                    return 1;
                if (o1.hashCode() == o2.hashCode())
                    return 0;
            }
            return 0;
        }
    }
}
