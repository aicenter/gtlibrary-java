package cz.agents.gtlibrary.algorithms.stackelberg.oracle;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPData;
import cz.agents.gtlibrary.algorithms.stackelberg.correlated.twoplayer.iterative.gadgets.GadgetAction;
import cz.agents.gtlibrary.algorithms.stackelberg.correlated.twoplayer.iterative.gadgets.GadgetLPTable;
import cz.agents.gtlibrary.domain.flipit.types.FollowerType;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.Pair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by Jakub Cerny on 17/01/2018.
 */
public class GadgetOracle2pShallowestBrokenCplexLP extends GadgetOracle2pSumForbiddingLP {

    protected HashMap<GameState, HashSet<Object>> eqsToDeleteWithoutDeletingVars;

    protected final boolean CLEAR_LP = false;

    public GadgetOracle2pShallowestBrokenCplexLP(Player leader, GameInfo info) {
        super(leader, info);
        eqsToDeleteWithoutDeletingVars = new HashMap<>();
    }

    @Override
    public void deleteOldGadgetRootConstraintsAndVariables(GameState state){
        if (state.equals(algConfig.getRootState())) return;
        super.deleteOldGadgetRootConstraintsAndVariables(state);
        if(eqsToDeleteWithoutDeletingVars.containsKey(state))
            for (Object eqKey : eqsToDeleteWithoutDeletingVars.get(state))
                if (lpTable instanceof GadgetLPTable)
                    ((GadgetLPTable)lpTable).deleteConstraintWithoutVars(eqKey);
    }

    @Override
    public String getInfo(){
        return "Gadget Oracle 2p Shallowest Broken Cplex BnB";
    }

    @Override
    protected Pair<Map<Sequence, Double>, Double> handleBrokenStrategyCause(double lowerBound, double upperBound, LPData lpData, double value, Iterable<Sequence> brokenStrategyCauses) {
//        SequenceInformationSet lastSet = (SequenceInformationSet) brokenStrategyCauses.iterator().next().getLastInformationSet();
//        bnbBranchingCount++;

        for (Sequence outgoingSequence : brokenStrategyCauses) {
            addEqualityToBinaryVariableFor(outgoingSequence, lpData);
        }
        controlBinaryVariables(brokenStrategyCauses);
        Pair<Map<Sequence, Double>, Double> currentBest = solve(lowerBound, upperBound);

        if (CLEAR_LP) removeBinaryConstraints(brokenStrategyCauses, lpData);
        return currentBest;
    }

    protected void removeBinaryConstraints(Iterable<Sequence> brokenStrategyCauses, LPData lpData) {
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

    protected void controlBinaryVariables(Iterable<Sequence> brokenStrategyCauses) {
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
                        if (lpTable instanceof GadgetLPTable)
                            ((GadgetLPTable)lpTable).markAsBinary(binaryVarKey);
                        lpTable.watchPrimalVariable(binaryVarKey, binaryVarKey);
                        lpTable.setConstraintType(eqKey, 0);

                        if (!p.getLeft().isEmpty() && p.getLeft().getLast() instanceof GadgetAction) {
                            if (!eqsToDeleteWithoutDeletingVars.containsKey(((GadgetAction) p.getLeft().getLast()).getState()))
                                eqsToDeleteWithoutDeletingVars.put(((GadgetAction) p.getLeft().getLast()).getState(), new HashSet<>());
                            eqsToDeleteWithoutDeletingVars.get(((GadgetAction) p.getLeft().getLast()).getState()).add(eqKey);
                        }

                    }
                }
            }
        }
    }

}
