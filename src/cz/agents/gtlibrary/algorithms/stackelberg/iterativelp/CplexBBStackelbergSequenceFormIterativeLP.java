package cz.agents.gtlibrary.algorithms.stackelberg.iterativelp;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPData;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.InformationSet;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.Pair;

import java.util.Map;
import java.util.Set;

public class CplexBBStackelbergSequenceFormIterativeLP extends StackelbergSequenceFormIterativeLP {

    public CplexBBStackelbergSequenceFormIterativeLP(Player leader, GameInfo info) {
        super(leader, info);
    }

    @Override
    protected Pair<Map<Sequence, Double>, Double> handleBrokenStrategyCause(double lowerBound, double upperBound, LPData lpData, double value, Set<Sequence> brokenStrategyCauses) {
        SequenceInformationSet lastSet = (SequenceInformationSet) brokenStrategyCauses.iterator().next().getLastInformationSet();
        Set<Sequence> outgoingSequences = lastSet.getOutgoingSequences();

        for (Sequence outgoingSequence : outgoingSequences) {
            addEqualityToBinaryVariableFor(outgoingSequence, lpData);
//            restrictFollowerPlay(brokenStrategyCause, brokenStrategyCauses, lpData);
//            Pair<Map<Sequence, Double>, Double> result = solve(getLowerBound(lowerBound, currentBest), upperBound);
//
//            if (result.getRight() > currentBest.getRight()) {
//                currentBest = result;
//                if(currentBest.getRight() >= value - 1e-8) {
//                    System.out.println("----------------currentBest " + currentBest.getRight() + " reached parent value " + value + "----------------");
//                    return currentBest;
//                }
//            }
//            removeRestriction(brokenStrategyCause, brokenStrategyCauses, lpData);
        }
        forceOnlyOneBinaryToBeActive(outgoingSequences);
        Pair<Map<Sequence, Double>, Double> currentBest = solve(lowerBound, upperBound);

        removeBinaryConstraints(outgoingSequences, lpData);
        return currentBest;
    }

    private void removeBinaryConstraints(Set<Sequence> brokenStrategyCauses, LPData lpData) {
        Pair<String, InformationSet> binarySumKey = new Pair<>("binarySum", brokenStrategyCauses.iterator().next().getLastInformationSet());

        for (Sequence brokenStrategyCause : brokenStrategyCauses) {
            lpTable.removeFromConstraint(binarySumKey, new Pair<>("binary", brokenStrategyCause));
        }
        for (Sequence brokenStrategyCause : brokenStrategyCauses) {
            for (Object varKey : lpData.getWatchedPrimalVariables().keySet()) {
                if (varKey instanceof Pair) {
                    if (((Pair) varKey).getLeft() instanceof Sequence && ((Pair) varKey).getRight() instanceof Sequence) {
                        Pair<Sequence, Sequence> p = (Pair<Sequence, Sequence>) varKey;

                        if (p.getRight().equals(brokenStrategyCause)) {
                            Pair<String, Pair<Sequence, Sequence>> eqKey = new Pair<>("restr", p);
                            Pair<String, Sequence> binaryVarKey = new Pair<>("binary", brokenStrategyCause);

                            lpTable.removeFromConstraint(eqKey, p);
                            lpTable.removeFromConstraint(eqKey, binaryVarKey);
                        }
                    }
                }
            }
        }
    }

    private void forceOnlyOneBinaryToBeActive(Set<Sequence> brokenStrategyCauses) {
        Pair<String, InformationSet> binarySumKey = new Pair<>("binarySum", brokenStrategyCauses.iterator().next().getLastInformationSet());

        for (Sequence brokenStrategyCause : brokenStrategyCauses) {
            lpTable.setConstraint(binarySumKey, new Pair<>("binary", brokenStrategyCause), 1);
        }
        lpTable.setConstraintType(binarySumKey, 1);
        lpTable.setConstant(binarySumKey, 1);
    }

    private void addEqualityToBinaryVariableFor(Sequence brokenStrategyCause, LPData lpData) {
        Pair<String, Sequence> binaryVarKey = new Pair<>("binary", brokenStrategyCause);

        for (Object varKey : lpData.getWatchedPrimalVariables().keySet()) {
            if (varKey instanceof Pair) {
                if (((Pair) varKey).getLeft() instanceof Sequence && ((Pair) varKey).getRight() instanceof Sequence) {
                    Pair<Sequence, Sequence> p = (Pair<Sequence, Sequence>) varKey;

                    if (p.getRight().equals(brokenStrategyCause)) {
                        Pair<String, Pair<Sequence, Sequence>> eqKey = new Pair<>("restr", p);

                        lpTable.setConstraint(eqKey, p, 1);
                        lpTable.setConstraint(eqKey, binaryVarKey, -1);
                        lpTable.markAsBinary(binaryVarKey);
                        lpTable.setConstraintType(eqKey, 0);
                    }
                }
            }
        }
    }
}
