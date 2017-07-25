package cz.agents.gtlibrary.algorithms.stackelberg.multiplelps;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.stackelberg.StackelbergConfig;
import cz.agents.gtlibrary.algorithms.stackelberg.multiplelps.rpiterator.PureRealPlanIterator;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import ilog.concert.*;
import ilog.cplex.IloCplex;

import java.util.*;

public class StackelbergMultipleLPs extends StackelbergSequenceFormMultipleLPs {


    public StackelbergMultipleLPs(Player[] players, Player leader, Player follower, GameInfo info, Expander<SequenceInformationSet> expander) {
        super(players, leader, follower, info, expander);
    }

    @Override
    public double calculateLeaderStrategies(StackelbergConfig algConfig, Expander<SequenceInformationSet> expander) {
        IloCplex cplex = modelsForPlayers.get(leader);
        IloNumVar v0 = objectiveForPlayers.get(leader);
        double maxValue = Double.NEGATIVE_INFINITY;
        int upperBoundCuts = 0;
        int feasibilityCuts = 0;

        try {
            long startTime = mxBean.getCurrentThreadCpuTime();

            buildInformationSets(algConfig);
            createVariables(cplex, algConfig);
            createConstraintsForSets(leader, cplex, informationSets.get(leader));
            createRPConstraints(algConfig.getIterator(follower, expander, new EmptyFeasibilitySequenceFormLP(leader, follower, algConfig, informationSets, sequences)), cplex, algConfig);
            overallConstraintGenerationTime += mxBean.getCurrentThreadCpuTime() - startTime;
            PureRealPlanIterator iterator = algConfig.getIterator(follower, expander, new EmptyFeasibilitySequenceFormLP(leader, follower, algConfig, informationSets, sequences));

            while (true) {
                Set<Sequence> pureRP = iterator.next();

                assert Math.abs(getUpperBound(pureRP, algConfig) - iterator.getCurrentUpperBound()) < 1e-8 ;
//                debugOutput.println(iteration);

//                debugOutput.println("---");
//                for (Sequence sequence : pureRP) {
//                    debugOutput.println(sequence);
//                }
                if (maxValue == info.getMaxUtility())
                    break;
                IloNumExpr pureRPAddition = addLeftSideOfRPConstraints(pureRP, cplex, algConfig);

                setObjectiveConstraint(pureRP, v0, cplex, algConfig);
                addBestValueConstraint(cplex, v0, maxValue + 1e-5);
//                cplex.exportModel("multipleLP.lp");
                startTime = mxBean.getCurrentThreadCpuTime();
                cplex.solve();
                overallConstraintLPSolvingTime += mxBean.getCurrentThreadCpuTime() - startTime;
                totalRPCount++;
//                System.out.println(cplex.getStatus());
                if (cplex.getStatus() == IloCplex.Status.Optimal) {
                    double v = cplex.getObjValue();

//                      for (Sequence sequence : pureRP) {
//                          debugOutput.println(sequence);
//                      }
//                      debugOutput.println("Leader's strategy: ");
//                      for (Map.Entry<Sequence, Double> entry : createSolution(algConfig, leader, cplex).entrySet()) {
//                          if (entry.getValue() > 0)
//                              debugOutput.println(entry);
//                      }
                    if (v > maxValue) {
                        debugOutput.println("Best reward is " + v + " for follower strategy: ");
                        maxValue = v;
                        resultValues.put(leader, maxValue);
                        iterator.setBestValue(maxValue);
                        resultStrategies.put(leader, createSolution(algConfig, leader, cplex));
                        resultStrategies.put(follower, getRP(pureRP));
                    }
                } else {
                    feasibilityCuts++;
                }
                removeLeftSideOfRPConstraints(pureRPAddition, cplex);
            }
        } catch (NoSuchElementException e) {

        } catch (IloException e) {
            e.printStackTrace();
        }
        System.out.println("RP count: " + totalRPCount);
        System.out.println("Upper bound cuts: " + upperBoundCuts);
        System.out.println("Feasibility cuts: " + feasibilityCuts);
        return maxValue;
    }

    private void buildInformationSets(StackelbergConfig algConfig) {
        for (SequenceInformationSet informationSet : algConfig.getAllInformationSets().values()) {
            if (!variables.containsKey(informationSet)) {
                informationSets.get(informationSet.getPlayer()).add(informationSet);
            }
        }
    }

    private void setObjectiveConstraint(Set<Sequence> pureRP, IloNumVar v0, IloCplex cplex, StackelbergConfig algConfig) throws IloException {
        IloNumExpr expr = cplex.constant(0d);

        expr = cplex.sum(expr, cplex.prod(1d, v0));
        for (Sequence followerSequence : pureRP) {
            for (Sequence leaderSequence : algConfig.getCompatibleSequencesFor(followerSequence)) {
                double utility = getUtilityFor(algConfig, followerSequence, leaderSequence, leader);

                expr = cplex.sum(expr, cplex.prod(-utility, variables.get(leaderSequence)));
            }
        }
        IloRange objConst = constraints.get("objConst");

        if (objConst == null) {
            objConst = cplex.addEq(expr, 0, "objConst");
            constraints.put("objConst", objConst);
        } else {
            objConst.setExpr(expr);
        }
    }

    private void removeLeftSideOfRPConstraints(IloNumExpr pureRPAddition, IloCplex cplex) {
        try {
            for (Map.Entry<Object, IloRange> entry : constraints.entrySet()) {
                if (entry.getKey() instanceof Set)
                    cplex.addToExpr(entry.getValue(), cplex.negative(pureRPAddition));
            }
        } catch (IloException e) {
            e.printStackTrace();
        }
    }

    private IloNumExpr addLeftSideOfRPConstraints(Set<Sequence> pureRP, IloCplex cplex, StackelbergConfig algConfig) {
        try {
            IloNumExpr expr = cplex.constant(0d);

            for (Sequence followerSequence : pureRP) {
                for (Sequence leaderSequence : algConfig.getCompatibleSequencesFor(followerSequence)) {
                    double utility = getUtilityFor(algConfig, followerSequence, leaderSequence, follower);

                    expr = cplex.sum(expr, cplex.prod(utility, variables.get(leaderSequence)));
                }
            }
            for (Map.Entry<Object, IloRange> entry : constraints.entrySet()) {
                if (entry.getKey() instanceof Set) {
                    cplex.addToExpr(entry.getValue(), expr);
                }
            }
            return expr;
        } catch (IloException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void createRPConstraints(PureRealPlanIterator iterator, IloCplex cplex, StackelbergConfig algConfig) throws IloException {
        try {
            while (true) {
                createRightSideOfRPConstraint(iterator.next(), cplex, algConfig);
            }
        } catch (NoSuchElementException e) {
        }
    }

    private void createRightSideOfRPConstraint(Set<Sequence> pureRP, IloCplex cplex, StackelbergConfig algConfig) throws IloException {
        IloNumExpr expr = cplex.constant(0d);

        for (Sequence followerSequence : pureRP) {
            for (Sequence leaderSequence : algConfig.getCompatibleSequencesFor(followerSequence)) {
                double utility = getUtilityFor(algConfig, followerSequence, leaderSequence, follower);

                expr = cplex.sum(expr, cplex.prod(-utility, variables.get(leaderSequence)));
            }
        }
        IloRange rpConstraint = cplex.addGe(expr, 0);
        constraints.put(pureRP, rpConstraint);
    }

    private double getUtilityFor(StackelbergConfig algConfig, Sequence followerSequence, Sequence leaderSequence, Player player) {
        Double utility = algConfig.getUtilityFor(followerSequence, leaderSequence, player);

        return utility == null ? 0 : utility;
    }

    protected void createVariables(IloCplex model, StackelbergConfig algConfig) throws IloException {
        for (Sequence sequence : algConfig.getAllSequences()) {
            if (variables.containsKey(sequence))
                continue;
            if (sequence.getPlayer().equals(leader)) {
                createVariableForSequence(model, sequence);
                sequences.get(sequence.getPlayer()).add(sequence);
            }
        }
        debugOutput.println("variables created");
    }
}
