package cz.agents.gtlibrary.algorithms.flipit.bayesian.iterative;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPData;
import cz.agents.gtlibrary.algorithms.stackelberg.iterativelp.RecyclingMILPTable;
import cz.agents.gtlibrary.domain.flipit.FlipItGameInfo;
import cz.agents.gtlibrary.domain.flipit.types.FollowerType;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.Pair;
import cz.agents.gtlibrary.utils.Triplet;

import java.util.Map;

public class ShallowestBrokenCplexBayesianStackelbergLP extends SumForbiddingBayesianStackelbergLP {

    public ShallowestBrokenCplexBayesianStackelbergLP(FlipItGameInfo info, Expander expander) {
        super(info, expander);
        this.eps = 1e-5;
        this.lpTable = new RecyclingMILPTable();
    }

    @Override
    protected Pair<Map<Sequence, Double>, Double> handleBrokenStrategyCause(double lowerBound, double upperBound, LPData lpData, double value, Iterable<Pair<FollowerType,Sequence>> brokenStrategyCauses) {
        SequenceInformationSet lastSet = (SequenceInformationSet) brokenStrategyCauses.iterator().next().getRight().getLastInformationSet();

        for (Pair<FollowerType,Sequence> outgoingSequence : brokenStrategyCauses) {
            addEqualityToBinaryVariableFor(outgoingSequence, lpData);
        }
        controlBinaryVariables(brokenStrategyCauses);
        Pair<Map<Sequence, Double>, Double> currentBest = solve(lowerBound, upperBound);

        removeBinaryConstraints(brokenStrategyCauses, lpData);
        return currentBest;
    }

    protected void removeBinaryConstraints(Iterable<Pair<FollowerType, Sequence>> brokenStrategyCauses, LPData lpData) {
        Pair<FollowerType, Sequence> cause = brokenStrategyCauses.iterator().next();
        Triplet<String, InformationSet, FollowerType> binarySumKey = new Triplet<>("binarySum", cause.getRight().getLastInformationSet(), cause.getLeft());

        for (Pair<FollowerType,Sequence> brokenStrategyCause : brokenStrategyCauses) {
            lpTable.removeFromConstraint(binarySumKey, new Triplet<>("binary", brokenStrategyCause.getRight(), brokenStrategyCause.getLeft()));
        }
        for (Pair<FollowerType, Sequence> brokenStrategyCause : brokenStrategyCauses) {
            for (Object varKey : lpData.getWatchedPrimalVariables().keySet()) {
                if (varKey instanceof Triplet) {
                    if (((Triplet) varKey).getFirst() instanceof Sequence && ((Triplet) varKey).getSecond() instanceof Sequence && ((Triplet) varKey).getThird() instanceof FollowerType && ((Triplet) varKey).getThird().equals(brokenStrategyCause.getLeft())) {
                        Triplet<Sequence, Sequence,FollowerType> p = (Triplet<Sequence, Sequence, FollowerType>) varKey;

                        if (p.getSecond().equals(brokenStrategyCause.getRight())) {
                            Pair<String, Triplet<Sequence, Sequence, FollowerType>> eqKey = new Pair<>("restr", p);
                            Triplet<String, Sequence, FollowerType> binaryVarKey = new Triplet<>("binary", brokenStrategyCause.getRight(), brokenStrategyCause.getLeft());

                            lpTable.removeFromConstraint(eqKey, p);
                            lpTable.removeFromConstraint(eqKey, binaryVarKey);
                        }
                    }
                }
            }
        }
    }

    protected void controlBinaryVariables(Iterable<Pair<FollowerType, Sequence>> brokenStrategyCauses) {
        Pair<FollowerType, Sequence> cause = brokenStrategyCauses.iterator().next();
        Triplet<String, InformationSet, FollowerType> binarySumKey = new Triplet<>("binarySum", cause.getRight().getLastInformationSet(), cause.getLeft());

        for (Pair<FollowerType, Sequence> brokenStrategyCause : brokenStrategyCauses) {
            lpTable.setConstraint(binarySumKey, new Triplet<>("binary", brokenStrategyCause.getRight(), brokenStrategyCause.getLeft()), 1);
        }
        lpTable.setConstraintType(binarySumKey, 1);
        lpTable.setConstant(binarySumKey, 1);
    }

    protected void addEqualityToBinaryVariableFor(Pair<FollowerType,Sequence> brokenStrategyCause, LPData lpData) {
        Triplet<String, Sequence, FollowerType> binaryVarKey = new Triplet<>("binary", brokenStrategyCause.getRight(), brokenStrategyCause.getLeft());

        for (Object varKey : lpData.getWatchedPrimalVariables().keySet()) {
            if (varKey instanceof Triplet) {
                if (((Triplet) varKey).getFirst() instanceof Sequence && ((Triplet) varKey).getSecond() instanceof Sequence && ((Triplet) varKey).getThird() instanceof FollowerType && ((Triplet) varKey).getThird().equals(brokenStrategyCause.getLeft())) {
                    Triplet<Sequence, Sequence,FollowerType> p = (Triplet<Sequence, Sequence, FollowerType>) varKey;

                    if (p.getSecond().equals(brokenStrategyCause.getRight())) {
                        Pair<String, Triplet<Sequence, Sequence, FollowerType>> eqKey = new Pair<>("restr", p);

                        lpTable.setConstraint(eqKey, p, 1);
                        lpTable.setConstraint(eqKey, binaryVarKey, -1);
                        ((RecyclingMILPTable)lpTable).markAsBinary(binaryVarKey);
                        lpTable.watchPrimalVariable(binaryVarKey, binaryVarKey);
                        lpTable.setConstraintType(eqKey, 0);
                    }
                }
            }
        }
    }
}
