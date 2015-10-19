package cz.agents.gtlibrary.algorithms.stackelberg.iterativelp;

import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPData;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.InformationSet;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.Pair;
import ilog.concert.IloException;
import ilog.cplex.IloCplex;

import java.util.Comparator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

public class BFSStacklebergLP extends SumEnforcingStackelbergLP {

    public BFSStacklebergLP(Player leader, GameInfo info) {
        super(leader, info);
    }

    @Override
    protected Pair<Map<Sequence, Double>, Double> handleBrokenStrategyCause(double lowerBound, double upperBound, LPData lpData, double value, Iterable<Sequence> brokenStrategyCauses) {
        Pair<Map<Sequence, Double>, Double> currentBest = dummyResult;
        Queue<Pair<Sequence, Double>> queue = new PriorityQueue<>(new Comparator<Pair<Sequence, Double>>() {
            @Override
            public int compare(Pair<Sequence, Double> o1, Pair<Sequence, Double> o2) {
                return Double.compare(o2.getRight(), o1.getRight());
            }
        });

        for (Sequence brokenStrategyCause : brokenStrategyCauses) {
            restrictFollowerPlay(brokenStrategyCause, brokenStrategyCauses, lpData);
            Pair<Iterable<Sequence>, Double> result = probe(getLowerBound(lowerBound, currentBest), upperBound);

            if (result.getRight() > Double.NEGATIVE_INFINITY) {
                if(result.getRight() == null)
                    lowerBound = Math.max(lowerBound, result.getRight());
                queue.add(new Pair<Sequence, Double>(brokenStrategyCause, result.getRight()));
            }
            removeRestriction(brokenStrategyCause, brokenStrategyCauses, lpData);
        }
        for (Sequence brokenStrategyCause : brokenStrategyCauses) {
            restrictFollowerPlay(brokenStrategyCause, brokenStrategyCauses, lpData);
            Pair<Map<Sequence, Double>, Double> result = solve(getLowerBound(lowerBound, currentBest), upperBound);

            if (result.getRight() > currentBest.getRight()) {
                currentBest = result;
                if (currentBest.getRight() >= value - eps) {
                    System.out.println("----------------currentBest " + currentBest.getRight() + " reached parent value " + value + "----------------");
                    return currentBest;
                }
            }
            removeRestriction(brokenStrategyCause, brokenStrategyCauses, lpData);
        }
        return currentBest;
    }

    protected Pair<Iterable<Sequence>, Double> probe(double lowerBound, double upperBound) {
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

                System.out.println("**************");
                System.out.println("prober LP value: " + value);
//                for (Map.Entry<Object, IloNumVar> entry : lpData.getWatchedPrimalVariables().entrySet()) {
//                    if (entry.getKey() instanceof Pair && ((Pair) entry.getKey()).getLeft().equals("v")) {
//                        double variableValue = lpData.getSolver().getValue(entry.getValue());
//
//                        if (variableValue != 0)
//                            System.out.println(entry.getKey() + ": " + variableValue);
//                    }
//                }
                Map<InformationSet, Map<Sequence, Double>> behavStrat = getBehavioralStrategy(lpData, follower);
//                Iterable<Sequence> brokenStrategyCauses = getBrokenStrategyCauses(behavStrat);
//
//                System.out.println("follower behav. strat.");
//                for (Map.Entry<InformationSet, Map<Sequence, Double>> entry : behavStrat.entrySet()) {
//                    System.out.println(entry);
//                }
                return new Pair<>(getBrokenStrategyCauses(behavStrat), value);
            } else {
                System.err.println(lpData.getSolver().getStatus());
            }
        } catch (IloException e) {
            e.printStackTrace();
        }
        return new Pair<>(null, Double.NEGATIVE_INFINITY);
    }
}
