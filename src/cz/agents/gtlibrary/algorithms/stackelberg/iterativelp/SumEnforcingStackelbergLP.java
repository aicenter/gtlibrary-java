package cz.agents.gtlibrary.algorithms.stackelberg.iterativelp;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPData;
import cz.agents.gtlibrary.algorithms.stackelberg.iterativelp.sets.SequenceSet;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.InformationSet;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.Pair;

import java.util.*;

public class SumEnforcingStackelbergLP extends SumForbiddingStackelbergLP {

    protected SequenceSet initSet = SequenceSet.empty;
    protected SequenceSet current = SequenceSet.empty;
    protected LinkedHashSet<Sequence> currentSequences;

    public SumEnforcingStackelbergLP(Player leader, GameInfo info) {
        super(leader, info);
        currentSequences = new LinkedHashSet<>();
    }

    @Override
    protected Pair<Map<Sequence, Double>, Double> handleBrokenStrategyCause(double lowerBound, double upperBound, LPData lpData, double value, Iterable<Sequence> brokenStrategyCauses) {
        Pair<Map<Sequence, Double>, Double> currentBest = dummyResult;

//        current.setValue(reward);
        for (Sequence brokenStrategyCause : brokenStrategyCauses) {
            restrictFollowerPlay(brokenStrategyCause, brokenStrategyCauses, lpData);
//            currentSequences.add(brokenStrategyCause);
//            double bestFit = getBestFit(initSet, getLowerBound(lowerBound, currentBest));
//
//            System.err.println("best fit: " + bestFit + ". lower bound: " + getLowerBound(lowerBound, currentBest));
//            if (bestFit <= lowerBound) {
//                System.err.println("duplicity cut");
//                return dummyResult;
//            }
//            SequenceSet temp = current;
//
//            current = current.createSuperSet(brokenStrategyCause, reward);
//            temp.setValue(reward);
            Pair<Map<Sequence, Double>, Double> result = solve(getLowerBound(lowerBound, currentBest), upperBound);

//            current.setValue(result.getRight());
            if (result.getRight() > currentBest.getRight()) {
                currentBest = result;
                if (currentBest.getRight() >= value - eps) {
                    System.out.println("----------------currentBest " + currentBest.getRight() + " reached parent reward " + value + "----------------");
                    return currentBest;
                }
            }

//            current = temp;
//            currentSequences.remove(brokenStrategyCause);
            removeRestriction(brokenStrategyCause, brokenStrategyCauses, lpData);
        }
        return currentBest;
    }

    @Override
    protected void restrictFollowerPlay(Sequence brokenStrategyCause, Iterable<Sequence> brokenStrategyCauses, LPData lpData) {
        System.out.println(brokenStrategyCause + " enforced");
        for (Object varKey : lpData.getWatchedPrimalVariables().keySet()) {
            if (varKey instanceof Pair) {
                if (((Pair) varKey).getLeft() instanceof Sequence && ((Pair) varKey).getRight() instanceof Sequence) {
                    Pair<Sequence, Sequence> p = (Pair<Sequence, Sequence>) varKey;

                    if (p.getRight().equals(brokenStrategyCause)) {
                        Pair<String, Pair<Sequence, Sequence>> eqKey = new Pair<>("restr", p);
                        Pair<Sequence, Sequence> prefixKey = new Pair<>(p.getLeft(), brokenStrategyCause.getSubSequence(brokenStrategyCause.size() - 1));

                        assert lpData.getWatchedPrimalVariables().containsKey(prefixKey);
                        lpTable.setConstraint(eqKey, p, 1);
                        lpTable.setConstraint(eqKey, prefixKey, -1);
                        lpTable.setConstraintType(eqKey, 1);
                    }
                }
            }
        }
    }

    @Override
    protected void removeRestriction(Sequence brokenStrategyCause, Iterable<Sequence> brokenStrategyCauses, LPData lpData) {
        System.out.println(brokenStrategyCause + " released");
        for (Object varKey : lpData.getWatchedPrimalVariables().keySet()) {
            if (varKey instanceof Pair) {
                if (((Pair) varKey).getLeft() instanceof Sequence && ((Pair) varKey).getRight() instanceof Sequence) {
                    Pair<Sequence, Sequence> p = (Pair<Sequence, Sequence>) varKey;

                    if (p.getRight().equals(brokenStrategyCause)) {
                        Pair<String, Pair<Sequence, Sequence>> eqKey = new Pair<>("restr", p);
                        Pair<Sequence, Sequence> prefixKey = new Pair<>(p.getLeft(), brokenStrategyCause.getSubSequence(brokenStrategyCause.size() - 1));

                        lpTable.removeFromConstraint(eqKey, p);
                        lpTable.removeFromConstraint(eqKey, prefixKey);
                        lpTable.removeConstant(eqKey);
                    }
                }
            }
        }
    }

    @Override
    protected Iterable<Sequence> getBrokenStrategyCauses(Map<InformationSet, Map<Sequence, Double>> strategy, LPData lpData) {
        SequenceInformationSet set = null;
        Map<Sequence, Double> shallowestBrokenStrategyCause = null;

        for (Map.Entry<InformationSet, Map<Sequence, Double>> isStrategy : strategy.entrySet()) {
            if (isStrategy.getValue().size() > 1) {
                if (shallowestBrokenStrategyCause == null) {
                    shallowestBrokenStrategyCause = new HashMap<>(isStrategy.getValue());
                    set = (SequenceInformationSet) isStrategy.getKey();
                } else {
                    Sequence candidate = isStrategy.getValue().keySet().iterator().next();
                    Sequence bestSoFar = shallowestBrokenStrategyCause.keySet().iterator().next();

                    if (candidate.size() < bestSoFar.size()) {
                        shallowestBrokenStrategyCause = new HashMap<>(isStrategy.getValue());
                        set = (SequenceInformationSet) isStrategy.getKey();
                    }
                }
            }
        }
        if (set == null)
            return null;
        return sort(shallowestBrokenStrategyCause, set.getOutgoingSequences());
    }

    protected Iterable<Sequence> sort(final Map<Sequence, Double> shallowestBrokenStrategyCause, final Collection<Sequence> outgoing) {
        List<Sequence> list = new ArrayList<>(outgoing);

        Collections.sort(list, new Comparator<Sequence>() {
            @Override
            public int compare(Sequence o1, Sequence o2) {
                Double o1Value = shallowestBrokenStrategyCause.get(o1);
                Double o2Value = shallowestBrokenStrategyCause.get(o2);

                if (o1Value == null)
                    o1Value = 0d;
                if (o2Value == null)
                    o2Value = 0d;
                return Double.compare(o2Value, o1Value);
            }
        });
        return list;
    }

    public double getBestFit(SequenceSet set, double currentBest) {
        double min = set.getValue();

        if (min < currentBest)
            return min;
        for (SequenceSet superSet : set.getDirectSuperSets()) {
            if (currentSequences.contains(superSet.getSequence()))
                min = Math.min(min, getBestFit(superSet, min));
        }
        return min;
    }
}
