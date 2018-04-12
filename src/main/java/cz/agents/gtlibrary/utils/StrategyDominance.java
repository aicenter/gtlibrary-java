package cz.agents.gtlibrary.utils;

import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPData;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPTable;
import ilog.concert.IloException;
import ilog.cplex.IloCplex;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class StrategyDominance<S> {

    public DominanceResult computeMixedDominance(Map<S, double[]> utilities) {
        DominanceResult pureDominanceResult = computePureDominance(utilities);
        Set<S> pureWeaklyDominatedStrategies = new HashSet<>(pureDominanceResult.weaklyDominatedStrategies);

        for (Map.Entry<S, double[]> entryToCheck : utilities.entrySet()) {
            if (isMixedStronglyDominated(entryToCheck, pureWeaklyDominatedStrategies, utilities)) {
                pureDominanceResult.addStronglyDominatedStrategy(entryToCheck.getKey());
                pureDominanceResult.addWeaklyDominatedStrategy(entryToCheck.getKey());
            } else if (isMixedWeaklyDominated(entryToCheck, pureWeaklyDominatedStrategies, utilities)) {
                pureDominanceResult.addWeaklyDominatedStrategy(entryToCheck.getKey());
            }
        }
        return pureDominanceResult;
    }

    private boolean isMixedWeaklyDominated(Map.Entry<S, double[]> entryToCheck, Set<S> pureWeaklyDominatedStrategies, Map<S, double[]> utilities) {
        LPTable lpTable = new LPTable();

        for (int i = 0; i < entryToCheck.getValue().length; i++) {
            lpTable.setConstant(i, entryToCheck.getValue()[i]);
            lpTable.setConstraint(i, "s" + i, -1);
            lpTable.setConstraintType(i, 1);
            lpTable.addToObjective("s" + i, 1);
        }
        lpTable.setConstant("sum", 1);
        lpTable.setConstraintType("sum", 1);
        for (Map.Entry<S, double[]> entry : utilities.entrySet()) {
            if (!pureWeaklyDominatedStrategies.contains(entry.getKey()) && !entry.equals(entryToCheck))
                for (int i = 0; i < entryToCheck.getValue().length; i++) {
                    lpTable.setConstraint(i, entry.getKey(), entry.getValue()[i]);
                    lpTable.setConstraint("sum", entry.getKey(), 1);
                }
        }
        try {
            LPData data = lpTable.toCplex();

//            data.getSolver().exportModel("wdom.lp");
            data.getSolver().solve();
            return data.getSolver().getStatus() == IloCplex.Status.Optimal && data.getSolver().getObjValue() > 1e-9;
        } catch (IloException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean isMixedStronglyDominated(Map.Entry<S, double[]> entryToCheck, Set<S> pureWeaklyDominatedStrategies, Map<S, double[]> utilities) {
        LPTable lpTable = new LPTable();

        for (int i = 0; i < entryToCheck.getValue().length; i++) {
            lpTable.setConstant(i, entryToCheck.getValue()[i]);
            lpTable.setConstraint(i, "z", -1);
            lpTable.setConstraintType(i, 2);
        }
        lpTable.setConstant("sum", 1);
        lpTable.setConstraintType("sum", 1);
        lpTable.addToObjective("z", 1);
        for (Map.Entry<S, double[]> entry : utilities.entrySet()) {
            if (!pureWeaklyDominatedStrategies.contains(entry.getKey()) && !entry.equals(entryToCheck))
                for (int i = 0; i < entryToCheck.getValue().length; i++) {
                    lpTable.setConstraint(i, entry.getKey(), entry.getValue()[i]);
                    lpTable.setConstraint("sum", entry.getKey(), 1);
                }
        }
        try {
            LPData data = lpTable.toCplex();

//            data.getSolver().exportModel("sdom.lp");
            data.getSolver().solve();
            return data.getSolver().getStatus() == IloCplex.Status.Optimal && data.getSolver().getObjValue() > 1e-9;
        } catch (IloException e) {
            e.printStackTrace();
        }
        return false;
    }

    public DominanceResult computePureDominance(Map<S, double[]> utilities) {
        DominanceResult result = new DominanceResult();

        for (Map.Entry<S, double[]> entryToCheck : utilities.entrySet()) {
            if (isPureStronglyDominated(entryToCheck, utilities)) {
                result.addStronglyDominatedStrategy(entryToCheck.getKey());
                result.addWeaklyDominatedStrategy(entryToCheck.getKey());
            } else if (isPureWeaklyDominated(entryToCheck, utilities)) {
                result.addWeaklyDominatedStrategy(entryToCheck.getKey());
            }
        }
        return result;
    }

    private boolean isPureWeaklyDominated(Map.Entry<S, double[]> entryToCheck, Map<S, double[]> utilities) {
        for (Map.Entry<S, double[]> entry : utilities.entrySet()) {
            if (entry.equals(entryToCheck))
                continue;
            if (weaklyDominates(entry, entryToCheck))
                return true;
        }
        return false;
    }

    public boolean isPureStronglyDominated(Map.Entry<S, double[]> entryToCheck, Map<S, double[]> utilities) {
        for (Map.Entry<S, double[]> entry : utilities.entrySet()) {
            if (entry.equals(entryToCheck))
                continue;
            if (stronglyDominates(entry, entryToCheck))
                return true;
        }
        return false;
    }

    private boolean stronglyDominates(Map.Entry<S, double[]> entry1, Map.Entry<S, double[]> entry2) {
        for (int i = 0; i < entry1.getValue().length; i++) {
            if (entry2.getValue()[i] >= entry1.getValue()[i])
                return false;
        }
        return true;
    }

    private boolean weaklyDominates(Map.Entry<S, double[]> entry1, Map.Entry<S, double[]> entry2) {
        int equalityCount = 0;

        for (int i = 0; i < entry1.getValue().length; i++) {
            if (entry2.getValue()[i] > entry1.getValue()[i])
                return false;
            else if(entry2.getValue()[i] == entry1.getValue()[i])
                equalityCount++;
        }
        return equalityCount != entry1.getValue().length;
    }

    public class DominanceResult {
        public Set<S> weaklyDominatedStrategies;
        public Set<S> stronglyDominatedStrategies;

        public DominanceResult() {
            weaklyDominatedStrategies = new HashSet<>();
            stronglyDominatedStrategies = new HashSet<>();
        }

        public DominanceResult(Set<S> weaklyDominatedStrategies, Set<S> stronglyDominatedStrategies) {
            this.weaklyDominatedStrategies = weaklyDominatedStrategies;
            this.stronglyDominatedStrategies = stronglyDominatedStrategies;
        }

        public void addWeaklyDominatedStrategy(S strategy) {
            weaklyDominatedStrategies.add(strategy);
        }

        public void addStronglyDominatedStrategy(S strategy) {
            stronglyDominatedStrategies.add(strategy);
        }

        @Override
        public String toString() {
            return "[Weakly: " + weaklyDominatedStrategies + ", Strongly: " + stronglyDominatedStrategies + "]";
        }
    }
}
