package cz.agents.gtlibrary.algorithms.stackelberg.multiplelps;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.stackelberg.StackelbergConfig;
import cz.agents.gtlibrary.algorithms.stackelberg.StackelbergSequenceFormLP;
import cz.agents.gtlibrary.algorithms.stackelberg.multiplelps.rpiterator.PureRealPlanIterator;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import ilog.concert.*;
import ilog.cplex.IloCplex;

import java.util.*;

public class StackelbergMultipleLPs extends StackelbergSequenceFormLP {

    protected Map<Set<Sequence>, IloRange> rpConstraints;

    public StackelbergMultipleLPs(Player[] players, Player leader, Player follower) {
        super(players, leader, follower);
        rpConstraints = new HashMap<>();
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

    @Override
    public double calculateLeaderStrategies(StackelbergConfig algConfig, Expander<SequenceInformationSet> expander) {
        IloCplex cplex = modelsForPlayers.get(leader);
        double maxValue = Double.NEGATIVE_INFINITY;
        int rpCount = 0;

        try {
            buildInformationSets(algConfig);
            createVariables(cplex, algConfig);
            createConstraintsForSets(leader, cplex, informationSets.get(leader));
            createRPConstraints(algConfig.getIterator(follower, expander, new EmptyFeasibilitySequenceFormLP(leader, follower, algConfig, informationSets, sequences)), cplex, algConfig);
            PureRealPlanIterator iterator = algConfig.getIterator(follower, expander, new EmptyFeasibilitySequenceFormLP(leader, follower, algConfig, informationSets, sequences));

            while (true) {
                Set<Sequence> pureRP = iterator.next();
//                cplex.exportModel("beforeLP.lp");
                IloNumExpr pureRPAddition = addLeftSideOfRPConstraints(pureRP, cplex, algConfig);

                setObjective(pureRP, cplex, algConfig);
//                cplex.exportModel("multipleLP.lp");
                cplex.solve();
                rpCount++;
                System.out.println(cplex.getStatus());
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
                        debugOutput.println("Best value is " + v + " for follower strategy: ");
                        maxValue = v;
                        resultValues.put(leader, maxValue);
                        iterator.setBestValue(maxValue);
                        resultStrategies.put(leader, createSolution(algConfig, leader, cplex));
                        resultStrategies.put(follower, getRP(pureRP));
                    }
                }
                removeLeftSideOfRPConstraints(pureRPAddition, cplex);
            }
        } catch (NoSuchElementException e) {

        } catch (IloException e) {
            e.printStackTrace();
        }
        System.out.println("RP count: " + rpCount);

        return maxValue;
    }

    private Map<Sequence, Double> getRP(Set<Sequence> followerBR) {
        Map<Sequence, Double> rp = new HashMap<>(followerBR.size());

        for (Sequence sequence : followerBR) {
            rp.put(sequence, 1d);
        }
        return rp;
    }

    private void buildInformationSets(StackelbergConfig algConfig) {
        for (SequenceInformationSet informationSet : algConfig.getAllInformationSets().values()) {
            if (!variables.containsKey(informationSet)) {
                informationSets.get(informationSet.getPlayer()).add(informationSet);
            }
        }
    }

    private void setObjective(Set<Sequence> pureRP, IloCplex cplex, StackelbergConfig algConfig) throws IloException {
        IloNumExpr expr = cplex.constant(0d);

        for (Sequence followerSequence : pureRP) {
            for (Sequence leaderSequence : algConfig.getCompatibleSequencesFor(followerSequence)) {
                double utility = getUtilityFor(algConfig, followerSequence, leaderSequence, leader);

                expr = cplex.sum(expr, cplex.prod(utility, variables.get(leaderSequence)));
            }
        }
        cplex.getObjective().setExpr(expr);
    }

    private void removeLeftSideOfRPConstraints(IloNumExpr pureRPAddition, IloCplex cplex) {
        try {
            int count = 0;
            for (Map.Entry<Object, IloRange> entry : constraints.entrySet()) {
                if (entry.getKey() instanceof Set) {
                    count++;
                    cplex.addToExpr(entry.getValue(), cplex.negative(pureRPAddition));
                }
            }
            System.out.println(count + " constraints modified");
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
        int count = 0;
        Set<Set<Sequence>> rps = new HashSet<>();

        try {
            while (true) {
                Set<Sequence> rp = new HashSet<>(iterator.next());

                count++;
                rps.add(rp);
                createRightSideOfRPConstraint(rp, cplex, algConfig);
            }
        } catch (NoSuchElementException e) {
            System.err.println(count + " vs " + rps.size());
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
        rpConstraints.put(pureRP, rpConstraint);
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

    @Override
    public Double getResultForPlayer(Player player) {
        return resultValues.get(player);
    }

    @Override
    public Map<Sequence, Double> getResultStrategiesForPlayer(Player player) {
        return resultStrategies.get(player);
    }

    @Override
    public long getOverallConstraintGenerationTime() {
        return 0;
    }

    @Override
    public long getOverallConstraintLPSolvingTime() {
        return 0;
    }
}
