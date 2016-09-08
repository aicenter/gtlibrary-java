package cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp;

import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPData;
import cz.agents.gtlibrary.interfaces.Action;
import ilog.concert.*;
import ilog.cplex.IloCplex;

import java.util.*;

@Deprecated
public class ReusingBilinearTable extends BilinearTable {

    protected Map<Object, List<List<IloConstraint>>> addedPrecisionConstraints;

    public ReusingBilinearTable() {
        super();
        this.addedPrecisionConstraints = new HashMap<>();
    }

    protected void addBilinearConstraint(LPData data, Object bilinVarKey, Object factor1, Object factor2, int precision, int highestPrecision) throws IloException {
        if (outgoingBilinearConstraints.containsKey(bilinVarKey))
            data.getSolver().delete(outgoingBilinearConstraints.get(bilinVarKey));

        if (behavioralBilinearConstraints.containsKey(factor2))
            data.getSolver().delete(behavioralBilinearConstraints.get(factor2));
        removeUnusedPrecisionConstraints();
        IloRange[] newConstraints = addMDTConstraints(data, bilinVarKey, factor1, factor2, precision);

        behavioralBilinearConstraints.put(factor2, newConstraints[0]);
        outgoingBilinearConstraints.put(bilinVarKey, newConstraints[1]);
    }

    public IloRange[] addMDTConstraints(LPData data, Object product, Object sequence, Object behavioral, int precision) throws IloException {
        IloCplex cplex = data.getSolver();
        IloNumVar rSequenceToIS = data.getVariables()[getVariableIndex(sequence)];
        IloNumVar xBehStrategy = data.getVariables()[getVariableIndex(behavioral)];
        IloNumVar rSequenceFromIS = data.getVariables()[getVariableIndex(product)];

        IloNumVar xBehDelta = deltaBehavioralVariables.get(behavioral);
        if (xBehDelta == null) {
            xBehDelta = cplex.numVar(0, 1, IloNumVarType.Float, "DELTA_" + behavioral.toString());
            deltaBehavioralVariables.put(behavioral, xBehDelta);
//            cplex.addMaximize(cplex.diff(cplex.getObjective().getExpr(), xBehDelta));
        }

        IloNumVar xSeqDelta = deltaSequenceVariables.get(product);
        if (xSeqDelta == null) {
            xSeqDelta = cplex.numVar(0, 1, IloNumVarType.Float, "DELTA_" + product.toString());
            deltaSequenceVariables.put(product, xSeqDelta);
//            cplex.getObjective().setExpr(cplex.diff(cplex.getObjective().getExpr(), cplex.prod(1,xSeqDelta)));
        }

        IloRange[] result = new IloRange[2];
        IloNumVar[][] w;

        if (wVariables.containsKey(behavioral)) {
            IloNumVar[][] existingWs = wVariables.get(behavioral);

//            precision = Math.max(precision, existingWs[0].length);
            w = new IloNumVar[digits][precision];
            for (int d = 0; d < digits; d++)
                for (int existingPrecision = 0; existingPrecision < Math.min(precision, existingWs[d].length); existingPrecision++) {
                    w[d][existingPrecision] = existingWs[d][existingPrecision];
                }
//            for (int d = 0; d < digits; d++) {
//                for (int i = Math.min(precision, existingWs[d].length); i < existingWs[d].length; i++) {
//                     cplex.remove(existingWs[d][i]);
//                }
//            }
        } else {
            w = new IloNumVar[digits][precision];
        }

        IloNumVar[][] rHat = new IloNumVar[digits][precision];

        if (rHatVariables.containsKey(product)) {
            IloNumVar[][] existingRHat = rHatVariables.get(product);

            for (int d = 0; d < digits; d++)
                for (int existingPrecision = 0; existingPrecision < Math.min(precision, existingRHat[d].length); existingPrecision++) {
                    rHat[d][existingPrecision] = existingRHat[d][existingPrecision];
                }
//            for (int d = 0; d < digits; d++) {
//                for (int i = Math.min(precision, existingRHat[d].length); i < existingRHat[d].length; i++) {
//                    cplex.remove(existingRHat[d][i]);
//                }
//            }
        }

        IloNumExpr productSum = xSeqDelta;
        IloNumExpr approxSum = xBehDelta;
        double[][] lbs = getLBs(behavioral);
        double[][] ubs = getUBs(behavioral);
//        IloNumExpr productSum = cplex.numExpr();
//        IloNumExpr approxSum = cplex.numExpr();
        List<List<IloConstraint>> existingConstraints = addedPrecisionConstraints.get(behavioral);

        if (existingConstraints == null) {
            existingConstraints = new ArrayList<>(maxPrecision);
            addedPrecisionConstraints.put(behavioral, existingConstraints);
        }
        for (int l = 0; l < precision; l++) {
            IloNumExpr xSum = cplex.numExpr();
            IloNumExpr wSum = cplex.numExpr();
            boolean thisPrecisionExists = false;
            List<IloConstraint> currentPrecisionConstraints = new ArrayList<>();

            for (int k = 0; k < digits; k++) {
                if ((l == 0) && (k > 1)) continue;
                if (w[k][l] == null)
                    w[k][l] = cplex.numVar(lbs[k][l], ubs[k][l], IloNumVarType.Float, "W_" + behavioral.toString() + "_" + k + "_" + l);
                wSum = cplex.sum(wSum, w[k][l]);
                if (rHat[k][l] == null) {
                    rHat[k][l] = cplex.numVar(0, 1, IloNumVarType.Float, "RHAT_" + product + "_" + k + "_" + l);
                    currentPrecisionConstraints.add(cplex.addLe(rHat[k][l], w[k][l]));
                    currentPrecisionConstraints.add(cplex.addLe(rHat[k][l], rSequenceToIS));
                } else {
                    thisPrecisionExists = true;
                }
                productSum = cplex.sum(productSum, cplex.prod(cplex.constant(Math.pow(10, -(l)) * k), rHat[k][l]));
                xSum = cplex.sum(xSum, rHat[k][l]);
                approxSum = cplex.sum(approxSum, cplex.prod(cplex.constant(Math.pow(10, -(l)) * k), w[k][l]));
            }
            if (!thisPrecisionExists) {
                currentPrecisionConstraints.add(cplex.addEq(rSequenceToIS, xSum));
                currentPrecisionConstraints.add(cplex.addEq(wSum, 1));
                xBehDelta.setUB(Math.pow(10, -(precision - 1)));
                currentPrecisionConstraints.add(cplex.addLe(xSeqDelta, cplex.prod(Math.pow(10, -(precision - 1)), rSequenceToIS)));
            }
            if (existingConstraints.size() <= l) {
                assert existingConstraints.size() == l;
                existingConstraints.add(currentPrecisionConstraints);
            } else {
                existingConstraints.get(l).addAll(currentPrecisionConstraints);
            }
        }

        result[0] = cplex.addEq(cplex.diff(xBehStrategy, approxSum), 0);
//        result[1] = cplex.addLe(cplex.diff(rSequenceFromIS,cplex.sum(productSum, cplex.constant(Math.pow(100, -(precision))))),0);
        result[1] = cplex.addEq(cplex.diff(rSequenceFromIS, productSum), 0);

        wVariables.put(behavioral, w);
        rHatVariables.put(product, rHat);
        return result;
    }

    protected void removeUnusedPrecisionConstraints() throws IloException {
        for (Map.Entry<Object, List<List<IloConstraint>>> entry : addedPrecisionConstraints.entrySet()) {
            int currentPrecision = getPrecisionFor(entry.getKey());

            for (int i = currentPrecision + 1; i < entry.getValue().size(); i++) {
                for (IloConstraint constraint : entry.getValue().get(i)) {
                    cplex.delete(constraint);
                }
            }
            if (currentPrecision + 1 <= entry.getValue().size())
                entry.getValue().subList(currentPrecision + 1, entry.getValue().size()).clear();
        }
    }

    public void resetVariableBounds() throws IloException {
        for (IloNumVar deltaBehav : deltaBehavioralVariables.values()) {
            deltaBehav.setLB(0);
            deltaBehav.setUB(Math.pow(10, -(INITIAL_MDT_PRECISION - 1)));
        }
    }
}
