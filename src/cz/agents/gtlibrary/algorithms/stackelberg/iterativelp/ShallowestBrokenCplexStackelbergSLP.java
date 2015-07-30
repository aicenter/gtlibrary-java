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

public class ShallowestBrokenCplexStackelbergSLP extends ForbiddingStackelbergLP {

    public ShallowestBrokenCplexStackelbergSLP(Player leader, GameInfo info) {
        super(leader, info);
        this.eps = 1e-5;
    }

    @Override
    protected Pair<Map<Sequence, Double>, Double> handleBrokenStrategyCause(double lowerBound, double upperBound, LPData lpData, double value, Set<Sequence> brokenStrategyCauses) {
        SequenceInformationSet lastSet = (SequenceInformationSet) brokenStrategyCauses.iterator().next().getLastInformationSet();

        for (Sequence outgoingSequence : brokenStrategyCauses) {
            addEqualityToBinaryVariableFor(outgoingSequence, lpData);
        }
        controlBinaryVariables(brokenStrategyCauses);
        Pair<Map<Sequence, Double>, Double> currentBest = solve(lowerBound, upperBound);

        removeBinaryConstraints(brokenStrategyCauses, lpData);
        return currentBest;
    }

    protected void removeBinaryConstraints(Set<Sequence> brokenStrategyCauses, LPData lpData) {
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

    protected void controlBinaryVariables(Set<Sequence> brokenStrategyCauses) {
        Pair<String, InformationSet> binarySumKey = new Pair<>("binarySum", brokenStrategyCauses.iterator().next().getLastInformationSet());

        for (Sequence brokenStrategyCause : brokenStrategyCauses) {
            lpTable.setConstraint(binarySumKey, new Pair<>("binary", brokenStrategyCause), 1);
        }
        lpTable.setConstraintType(binarySumKey, 1);
        lpTable.setConstant(binarySumKey, 1);
    }

    protected void addEqualityToBinaryVariableFor(Sequence brokenStrategyCause, LPData lpData) {
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
                        lpTable.watchPrimalVariable(binaryVarKey, binaryVarKey);
                        lpTable.setConstraintType(eqKey, 0);
                    }
                }
            }
        }
    }
}
