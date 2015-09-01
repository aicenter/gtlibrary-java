package cz.agents.gtlibrary.algorithms.stackelberg.iterativelp.bfs;

import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPData;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.InformationSet;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.Pair;
import ilog.concert.IloException;
import ilog.cplex.IloCplex;

import java.util.*;

public class CompleteBFSEnforcingStackelbergLP extends BFSEnforcingStackelbergLP {

    private Queue<Pair<Set<Sequence>, Double>> queue;
    private Set<Sequence> currentRestriction;
    private double lowerBound;
    private Pair<Map<Sequence, Double>, Double> currentBest = dummyResult;

    public CompleteBFSEnforcingStackelbergLP(Player leader, GameInfo info) {
        super(leader, info);
        queue = new PriorityQueue<>(new Comparator<Pair<Set<Sequence>, Double>>() {
            @Override
            public int compare(Pair<Set<Sequence>, Double> o1, Pair<Set<Sequence>, Double> o2) {
                return Double.compare(o2.getRight(), o1.getRight());
            }
        });
        lowerBound = -info.getMaxUtility();
        currentRestriction = new HashSet<>();
    }

    @Override
    protected Pair<Map<Sequence, Double>, Double> solve(double lowerBound1, double upperBound) {
        LPData lpData = solveFixedLP(Double.POSITIVE_INFINITY);

        if(currentBest.getRight() > Double.NEGATIVE_INFINITY)
            return currentBest;
        while (!queue.isEmpty()) {
            Pair<Set<Sequence>, Double> current = queue.poll();

            if (current.getRight() < lowerBound + eps)
                break;
//            System.out.println(current.getLeft());
//            System.out.println("UB: " + current.getRight());
            updateRestrictions(lpData, current.getLeft());
            lpData = solveFixedLP(upperBound);
        }
        return currentBest;
    }

    private LPData solveFixedLP(double upperBound) {
        try {
            long startTime = threadBean.getCurrentThreadCpuTime();
            LPData lpData = lpTable.toCplex();

            overallConstraintGenerationTime += threadBean.getCurrentThreadCpuTime() - startTime;
//            lpData.getSolver().exportModel("SSEIter.lp");
            startTime = threadBean.getCurrentThreadCpuTime();
            lpData.getSolver().solve();
            lpInvocationCount++;
            overallConstraintLPSolvingTime += threadBean.getCurrentThreadCpuTime() - startTime;
            printBinaryVariableValues(lpData);
            if (lpData.getSolver().getStatus() == IloCplex.Status.Optimal) {
                double value = lpData.getSolver().getObjValue();

                System.out.println("-----------------------");
                System.out.println("LP value: " + value + " lower bound: " + lowerBound);
//                System.out.println("n it: " + lpData.getSolver().getNiterations());
//                System.out.println("n nodes: " + lpData.getSolver().getNnodes());
//                for (Map.Entry<Object, IloNumVar> entry : lpData.getWatchedPrimalVariables().entrySet()) {
//                    if (entry.getKey() instanceof Pair && ((Pair) entry.getKey()).getLeft() instanceof Sequence && ((Pair) entry.getKey()).getRight() instanceof Sequence) {
//                        double variableValue = lpData.getSolver().getValue(entry.getValue());
//
//                        if (variableValue != 0)
//                            System.out.println(entry.getKey() + ": " + variableValue);
//                    }
//                }
                Map<InformationSet, Map<Sequence, Double>> followerBehavStrat = getBehavioralStrategy(lpData, follower);
                Iterable<Sequence> brokenStrategyCauses = getBrokenStrategyCauses(followerBehavStrat, lpData);
//                Map<Sequence, Double> leaderRealPlan = behavioralToRealizationPlan(getLeaderBehavioralStrategy(lpData, leader));

//                GenSumUtilityCalculator calculator = new GenSumUtilityCalculator(algConfig.getRootState(), expander);
//
//                System.out.println(Arrays.toString(calculator.computeUtility(getP1Strategy(leaderRealPlan, followerRealPlan), getP2Strategy(leaderRealPlan, followerRealPlan))));
//                System.out.println("follower behav. strat.");
//                for (Map.Entry<InformationSet, Map<Sequence, Double>> entry : followerBehavStrat.entrySet()) {
//                    System.out.println(entry);
//                }
                if (brokenStrategyCauses == null) {
//                    System.out.println("Found solution candidate with value: " + value);
//                    Map<Sequence, Double> leaderRealPlan = behavioralToRealizationPlan(getBehavioralStrategy(lpData, leader));
//                    Map<Sequence, Double> followerRealPlan = behavioralToRealizationPlan(getBehavioralStrategy(lpData, follower));
////
//                    System.out.println("Leader real. plan:");
//                    for (Map.Entry<Sequence, Double> entry : leaderRealPlan.entrySet()) {
//                        System.out.println(entry);
//                    }
//                    System.out.println("Follower real. plan:");
//                    for (Map.Entry<Sequence, Double> entry : followerRealPlan.entrySet()) {
//                        System.out.println(entry);
//                    }
                    if (lowerBound < value) {
                        lowerBound = value;
                        currentBest = new Pair<Map<Sequence, Double>, Double>(new HashMap<Sequence, Double>(), value);
                    }
                } else {
                    handleBrokenStrategyCause(lowerBound, upperBound, lpData, value, brokenStrategyCauses);
                }
            } else {
                System.err.println(lpData.getSolver().getStatus());
            }
            return lpData;
        } catch (IloException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void updateRestrictions(LPData lpData, Set<Sequence> newRestrictions) {
        updateLP(newRestrictions, lpData);
        currentRestriction = new HashSet<>(newRestrictions);
    }

    private void updateLP(Set<Sequence> newRestrictions, LPData lpData) {
        for (Sequence sequence : currentRestriction) {
            if (!newRestrictions.contains(sequence))
                removeRestriction(sequence, currentRestriction, lpData);
        }
        for (Sequence newRestriction : newRestrictions) {
            if (!currentRestriction.contains(newRestriction))
                restrictFollowerPlay(newRestriction, newRestrictions, lpData);
        }
    }

    @Override
    protected Pair<Map<Sequence, Double>, Double> handleBrokenStrategyCause(double lowerBound1, double upperBound, LPData lpData, double value, Iterable<Sequence> brokenStrategyCauses) {
        for (Sequence brokenStrategyCause : brokenStrategyCauses) {
            restrictFollowerPlay(brokenStrategyCause, brokenStrategyCauses, lpData);
//            System.out.println(currentRestriction + " " + brokenStrategyCause);
            Pair<Iterable<Sequence>, Double> result = probe();

            if (result.getRight() > Double.NEGATIVE_INFINITY) {
                if (result.getLeft() == null) {
                    if (lowerBound <= result.getRight()) {
                        lowerBound = result.getRight();
                        if (result.getRight() > currentBest.getRight())
                            currentBest = new Pair<>(null, result.getRight());
                    }
                } else {
                    if (result.getRight() > lowerBound + eps) {
                        for (Sequence sequence : result.getLeft()) {
                            Set<Sequence> copy = new HashSet<>(currentRestriction);

                            copy.add(brokenStrategyCause);
                            copy.add(sequence);
                            queue.add(new Pair<>(copy, result.getRight()));
                        }
                    }
                }
            }
            removeRestriction(brokenStrategyCause, brokenStrategyCauses, lpData);
        }
        return currentBest;
    }
}
