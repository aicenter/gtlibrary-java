package cz.agents.gtlibrary.algorithms.flipit.bayesian.iterative.gadget;

import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPData;
import cz.agents.gtlibrary.algorithms.stackelberg.correlated.twoplayer.iterative.gadgets.GadgetAction;
import cz.agents.gtlibrary.domain.flipit.types.FollowerType;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.Pair;
import cz.agents.gtlibrary.utils.Triplet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by Jakub Cerny on 16/01/2018.
 */
public class ShallowestBrokenCplexGadgetBayesianStackelbergLP extends SFGadgetBayesianStackelbergLP{

    protected final boolean CLEAN_MILP = false;

    protected HashMap<FollowerType,HashMap<GameState, HashSet<Object>>> eqsToDeleteWithoutDeletingVars;

    @Override
    public String getInfo(){
        return "Bayesian SSE Gadget MILP Solver";
    }

    public ShallowestBrokenCplexGadgetBayesianStackelbergLP(Player leader, GameInfo info) {
        super(leader, info);
        eqsToDeleteWithoutDeletingVars = new HashMap<>();
    }

    @Override
    protected void deleteOldGadgetRootConstraintsAndVariables(GameState state, FollowerType type) {
        super.deleteOldGadgetRootConstraintsAndVariables(state, type);
        for(Object eqKey : eqsToDeleteWithoutDeletingVars.get(type).get(state))
            lpTable.deleteConstraintWithoutVars(eqKey);
    }

    @Override
    protected Pair<Map<Sequence, Double>, Double> handleBrokenStrategyCause(double lowerBound, double upperBound, LPData lpData, double value, Iterable<Pair<FollowerType,Sequence>> brokenStrategyCauses) {
        bnbBranchingCount++;
        for (Pair<FollowerType,Sequence> outgoingSequence : brokenStrategyCauses) {
            addEqualityToBinaryVariableFor(outgoingSequence, lpData);
        }
        controlBinaryVariables(brokenStrategyCauses);
        Pair<Map<Sequence, Double>, Double> currentBest = solve(lowerBound, upperBound);

        if (CLEAN_MILP) removeBinaryConstraints(brokenStrategyCauses, lpData);
        return currentBest;
    }

    protected void removeBinaryConstraints(Iterable<Pair<FollowerType, Sequence>> brokenStrategyCauses, LPData lpData) {
        Pair<FollowerType, Sequence> cause = brokenStrategyCauses.iterator().next();
        Triplet<String, ISKey, FollowerType> binarySumKey = new Triplet<>("binarySum", cause.getRight().getLastInformationSet().getISKey(), cause.getLeft());

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
        Triplet<String, ISKey, FollowerType> binarySumKey = new Triplet<>("binarySum", cause.getRight().getLastInformationSet().getISKey(), cause.getLeft());

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
                        lpTable.markAsBinary(binaryVarKey);
                        lpTable.watchPrimalVariable(binaryVarKey, binaryVarKey);
                        lpTable.setConstraintType(eqKey, 0);

                        if (!p.getFirst().isEmpty() && p.getFirst().getLast() instanceof GadgetAction) {
                            if (!eqsToDeleteWithoutDeletingVars.containsKey(p.getThird())) eqsToDeleteWithoutDeletingVars.put(p.getThird(), new HashMap<>());
                            if(!eqsToDeleteWithoutDeletingVars.get(p.getThird()).containsKey(((GadgetAction) p.getFirst().getLast()).getState()))
                                eqsToDeleteWithoutDeletingVars.get(p.getThird()).put(((GadgetAction) p.getFirst().getLast()).getState(), new HashSet<>());
                            eqsToDeleteWithoutDeletingVars.get(p.getThird()).get(((GadgetAction) p.getFirst().getLast()).getState()).add(eqKey);
                            if(!varsToDelete.get(p.getThird()).get(((GadgetAction) p.getFirst().getLast()).getState()).containsKey(eqKey))
                                varsToDelete.get(p.getThird()).get(((GadgetAction) p.getFirst().getLast()).getState()).put(eqKey, new HashSet<>());
                            varsToDelete.get(p.getThird()).get(((GadgetAction) p.getFirst().getLast()).getState()).get(eqKey).add(p);
                        }


                    }
                }
            }
        }
    }
}
