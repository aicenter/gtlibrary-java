package cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp;

import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPData;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.RecyclingLPTable;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.change.Change;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.utils.Pair;
import ilog.concert.*;
import ilog.cplex.IloCplex;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

public class BilinearTable extends RecyclingLPTable {

    private Map<Object, Pair<Object, Object>> bilinearVars;
    private Map<Object, Pair<Object, Object>> bilinearVarsToUpdate;
    private Map<Object, Integer> precision;

    private Map<Object, IloNumVar[][]> wVariables; // information set -> binary variables for the bilinear terms for that IS
    private Map<Object, double[][]> wVariableLBs;
    private Map<Object, double[][]> wVariableUBs;
    private Map<Object, IloNumVar[][]> rHatVariables; // sequence -> rHat variables for sequence
    private Map<Object, IloNumVar> deltaBehavioralVariables; // action -> delta x variable
    private Map<Object, IloNumVar> deltaSequenceVariables; // sequence -> delta x variable
    private Map<Object, IloRange> outgoingBilinearConstraints; // information set -> all constraints for the bilinear terms for that IS
    private Map<Object, IloRange> behavioralBilinearConstraints; // information set -> all constraints for the bilinear terms for that IS
    final int digits = 10;
    final int maxPrecision = 7;

    private final int INITIAL_MDT_PRECISION = 2;

    public BilinearTable() {
        super();
        bilinearVars = new HashMap<>();
        bilinearVarsToUpdate = new HashMap<>();
        precision = new HashMap<>();
        wVariables = new HashMap<>();
        wVariableLBs = new HashMap<>();
        wVariableUBs = new HashMap<>();
        rHatVariables = new HashMap<>();
        deltaBehavioralVariables = new HashMap<>();
        deltaSequenceVariables = new HashMap<>();
        outgoingBilinearConstraints = new HashMap<>();
        behavioralBilinearConstraints = new HashMap<>();
    }

    public void markAsBilinear(Object bilinearVarKey, Object factor1, Object factor2) {
        getVariableIndex(bilinearVarKey);
        Pair<Object, Object> factors = new Pair<>(factor1, factor2);

        bilinearVars.put(bilinearVarKey, factors);
        bilinearVarsToUpdate.put(bilinearVarKey, factors);
    }

    @Override
    public LPData toCplex() throws IloException {
        LPData data = super.toCplex();

        for (Map.Entry<Object, Pair<Object, Object>> bilinKey : bilinearVarsToUpdate.entrySet()) {
            addBilinearConstraint(data, bilinKey.getKey(), bilinKey.getValue().getLeft(), bilinKey.getValue().getRight(),
                    getPrecisionFor(bilinKey.getValue().getRight()));
        }
        bilinearVarsToUpdate.clear();
        updateWBounds();
        return data;
    }

    private void updateWBounds() throws IloException {
        for (Map.Entry<Object, IloNumVar[][]> entry : wVariables.entrySet()) {
            double[][] lbs = getLBs(entry.getKey());
            double[][] ubs = getUBs(entry.getKey());
            IloNumVar[][] wVariableArray = entry.getValue();

            for (int i = 0; i < wVariableArray.length; i++) {
                for (int j = 0; j <wVariableArray[0].length; j++) {
                    if(wVariableArray[i][j] == null)
                        continue;
                    wVariableArray[i][j].setLB(lbs[i][j]);
                    wVariableArray[i][j].setUB(ubs[i][j]);
                }
            }
        }
    }

    private void addBilinearConstraint(LPData data, Object bilinVarKey, Object factor1, Object factor2, int precision) throws IloException {
        if (outgoingBilinearConstraints.containsKey(bilinVarKey))
            data.getSolver().delete(outgoingBilinearConstraints.get(bilinVarKey));

        if (behavioralBilinearConstraints.containsKey(factor2))
            data.getSolver().delete(behavioralBilinearConstraints.get(factor2));
        IloRange[] newConstraints = addMDTConstraints(data, bilinVarKey, factor1, factor2, precision);

        behavioralBilinearConstraints.put(factor2, newConstraints[0]);
        outgoingBilinearConstraints.put(bilinVarKey, newConstraints[1]);
    }

    public void refinePrecisionOfRelevantBilinearVars(Object factor2) {
        int newPrecision = getPrecisionFor(factor2) + 1;

        precision.put(factor2, newPrecision);
        bilinearVars.entrySet().stream()
                .filter(bilinearVar -> bilinearVar.getValue().getRight().equals(factor2))
                .forEach(bilinearVar -> bilinearVarsToUpdate.put(bilinearVar.getKey(), bilinearVar.getValue()));
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
                for (int existingPrecision = 0; existingPrecision < Math.min(existingWs[d].length, precision); existingPrecision++) {
                    w[d][existingPrecision] = existingWs[d][existingPrecision];
                }
        } else {
            w = new IloNumVar[digits][precision];
        }

        IloNumVar[][] rHat = new IloNumVar[digits][precision];

        if (rHatVariables.containsKey(product)) {
            IloNumVar[][] existingRHat = rHatVariables.get(product);
            for (int d = 0; d < digits; d++)
                for (int existingPrecision = 0; existingPrecision < Math.min(existingRHat[d].length, precision); existingPrecision++) {
                    rHat[d][existingPrecision] = existingRHat[d][existingPrecision];
                }
        }

        IloNumExpr productSum = xSeqDelta;
        IloNumExpr approxSum = xBehDelta;
        double[][] lbs = getLBs(behavioral);
        double[][] ubs = getUBs(behavioral);
//        IloNumExpr productSum = cplex.numExpr();
//        IloNumExpr approxSum = cplex.numExpr();
        for (int l = 0; l < precision; l++) {
            IloNumExpr xSum = cplex.numExpr();
            IloNumExpr wSum = cplex.numExpr();
            boolean thisPrecisionExists = false;
            for (int k = 0; k < digits; k++) {
                if ((l == 0) && (k > 1)) continue;
                if (w[k][l] == null)
                    w[k][l] = cplex.numVar(lbs[k][l], ubs[k][l], IloNumVarType.Float, "W_" + behavioral.toString() + "_" + k + "_" + l);
                wSum = cplex.sum(wSum, w[k][l]);
                if (rHat[k][l] == null) {
                    rHat[k][l] = cplex.numVar(0, 1, IloNumVarType.Float, "RHAT_" + product + "_" + k + "_" + l);
                    cplex.addLe(rHat[k][l], w[k][l]);
                    cplex.addLe(rHat[k][l], rSequenceToIS);
                } else {
                    thisPrecisionExists = true;
                }

                productSum = cplex.sum(productSum, cplex.prod(cplex.constant(Math.pow(10, -(l)) * k), rHat[k][l]));
                xSum = cplex.sum(xSum, rHat[k][l]);
                approxSum = cplex.sum(approxSum, cplex.prod(cplex.constant(Math.pow(10, -(l)) * k), w[k][l]));


            }
            if (!thisPrecisionExists) {
                cplex.addEq(rSequenceToIS, xSum);
                cplex.addEq(wSum, 1);
                xBehDelta.setUB(Math.pow(10, -(precision - 1)));
                cplex.addLe(xSeqDelta, cplex.prod(Math.pow(10, -(precision - 1)), rSequenceToIS));
            }
        }

        result[0] = cplex.addEq(cplex.diff(xBehStrategy, approxSum), 0);
//        result[1] = cplex.addLe(cplex.diff(rSequenceFromIS,cplex.sum(productSum, cplex.constant(Math.pow(100, -(precision))))),0);
        result[1] = cplex.addEq(cplex.diff(rSequenceFromIS, productSum), 0);

        wVariables.put(behavioral, w);
        rHatVariables.put(product, rHat);
        return result;
    }

    public IloNumVar[][] getWVariablesFor(Object object) {
        return wVariables.get(object);
    }

//    public boolean updateWBoundsForLeft(Change change) throws IloException {
//        IloNumVar[][] existingWs = wVariables.get(change.getAction());
//        boolean changed = false;
//
//        existingWs[1][0].setUB(0);
//        existingWs[0][0].setLB(1);
//        if (change.getFixedDigitArrayValue()[0] == 1)
//            return true;
//        int digitToFix = getLastFixedDigit(change);
//
//        if (digitToFix == 0)
//            return false;
//        for (int k = digitToFix; k < digits; k++) {
//            if ((change.getFixedDigitArrayValue().length == 0) && (k > 1))
//                continue;
//            if (existingWs[k][change.getFixedDigitCount()].getLB() < 1) {
//                if (existingWs[k][change.getFixedDigitCount()].getUB() == 1)
//                    changed = true;
//                existingWs[k][change.getFixedDigitCount()].setUB(0);
//            }
//        }
//        return changed;
//    }

    public boolean updateWBoundsForLeft(Change change) {
        double[][] lbs = getLBs(change.getAction());
        double[][] ubs = getUBs(change.getAction());
        boolean changed = false;

        ubs[1][0] = 0;
        lbs[0][0] = 1;
        if (change.getFixedDigitArrayValue()[0] == 1)
            return true;
        int digitToFix = getLastFixedDigit(change);

        if (digitToFix == 0)
            return false;
        for (int k = digitToFix; k < digits; k++) {
            if ((change.getFixedDigitArrayValue().length == 0) && (k > 1))
                continue;
            if (lbs[k][change.getFixedDigitCount()] < 1) {
                if (ubs[k][change.getFixedDigitCount()] == 1)
                    changed = true;
                ubs[k][change.getFixedDigitCount()] = 0;
            }
        }
        return changed;
    }

//    public boolean updateWBoundsForRight(Change change) throws IloException {
//        IloNumVar[][] existingWs = wVariables.get(change.getAction());
//        boolean changed = false;
//
//        if (change.getFixedDigitArrayValue()[0] == 1) {
//            existingWs[1][0].setLB(1);
//            existingWs[0][0].setUB(0);
//        } else {
//            existingWs[0][0].setLB(1);
//            existingWs[1][0].setUB(0);
//        }
//        int digitToFix = getLastFixedDigit(change);
//
//        for (int k = 0; k < digitToFix; k++) {
//            if ((change.getFixedDigitCount() == 0) && (k > 1))
//                continue;
//            if (existingWs[k][change.getFixedDigitCount()].getLB() < 1) {
//                if (existingWs[k][change.getFixedDigitCount()].getUB() == 1)
//                    changed = true;
//                existingWs[k][change.getFixedDigitCount()].setUB(0);
//            }
//        }
//        return changed;
//    }

    public boolean updateWBoundsForRight(Change change) {
        double[][] lbs = getLBs(change.getAction());
        double[][] ubs = getUBs(change.getAction());

        boolean changed = false;

        if (change.getFixedDigitArrayValue()[0] == 1) {
            lbs[1][0] = 1;
            ubs[0][0] = 0;
        } else {
            lbs[0][0] = 1;
            ubs[1][0] = 0;
        }
        int digitToFix = getLastFixedDigit(change);

        for (int k = 0; k < digitToFix; k++) {
            if ((change.getFixedDigitCount() == 0) && (k > 1))
                continue;
            if (lbs[k][change.getFixedDigitCount()] < 1) {
                if (ubs[k][change.getFixedDigitCount()] == 1)
                    changed = true;
                ubs[k][change.getFixedDigitCount()] = 0;
            }
        }
        return changed;
    }

    private double[][] getLBs(Object object) {
        double[][] lbs = wVariableLBs.get(object);

        if (lbs == null) {
            lbs = new double[digits][maxPrecision];
            Arrays.stream(lbs).forEach(array -> Arrays.fill(array, 0));
            wVariableLBs.put(object, lbs);
        }
        return lbs;
    }

    private double[][] getUBs(Object object) {
        double[][] ubs = wVariableUBs.get(object);

        if (ubs == null) {
            ubs = new double[digits][maxPrecision];
            Arrays.stream(ubs).forEach(array -> Arrays.fill(array, 1));
            wVariableUBs.put(object, ubs);
        }
        return ubs;
    }

//    public boolean updateWBoundsForMiddle(Change change) throws IloException {
//        IloNumVar[][] existingWs = wVariables.get(change.getAction());
//        boolean changed = false;
//
//        if (change.getFixedDigitArrayValue()[0] == 1) {
//            existingWs[1][0].setLB(1);
//            existingWs[0][0].setUB(0);
//        } else {
//            existingWs[0][0].setLB(1);
//            existingWs[1][0].setUB(0);
//        }
//        for (int k = 0; k < digits; k++) {
//            for (int l = 1; l < change.getFixedDigitArrayValue().length; l++) {
//                if ((l == 0) && (k > 1)) continue;
//                if (k != change.getFixedDigitArrayValue()[l]) {
//                    if (existingWs[k][l].getUB() > 0) {
//                        changed = true;
//                        existingWs[k][l].setUB(0);
//                    }
//                } else {
//                    if (existingWs[k][l].getUB() == 0)
//                        return false;
//                    else if (existingWs[k][l].getLB() == 0) {
//                        changed = true;
//                        existingWs[k][l].setLB(1);
//                        existingWs[k][l].setUB(1);
//                    }
//                }
//            }
//        }
//        return changed;
//    }

    public boolean updateWBoundsForMiddle(Change change) {
        double[][] lbs = getLBs(change.getAction());
        double[][] ubs = getUBs(change.getAction());
        boolean changed = false;

        if (change.getFixedDigitArrayValue()[0] == 1) {
            lbs[1][0] = 1;
            ubs[0][0] = 0;
        } else {
            lbs[0][0] = 1;
            ubs[1][0] = 0;
        }
        for (int k = 0; k < digits; k++) {
            for (int l = 1; l < change.getFixedDigitArrayValue().length; l++) {
                if ((l == 0) && (k > 1)) continue;
                if (k != change.getFixedDigitArrayValue()[l]) {
                    if (ubs[k][l] > 0) {
                        changed = true;
                        ubs[k][l] = 0;
                    }
                } else {
                    if (ubs[k][l] == 0)
                        return false;
                    else if (lbs[k][l] == 0) {
                        changed = true;
                        lbs[k][l] = 1;
                        ubs[k][l] = 1;
                    }
                }
            }
        }
        return changed;
    }

    private int getLastFixedDigit(Change change) {
        return change.getFixedDigitArrayValue()[change.getFixedDigitArrayValue().length - 1];
    }

    public int getPrecisionFor(Object factor2) {
        Integer precisionValue = precision.get(factor2);

        if (precisionValue == null)
            return INITIAL_MDT_PRECISION;
        return precisionValue;
    }

    public void removeWUpdate(Change change) throws IloException {
//        IloNumVar[][] existingWs = wVariables.get(change.getAction());
//
////        existingWs[1][0].setLB(0);
////        existingWs[1][0].setUB(1);
////        existingWs[0][0].setLB(0);
////        existingWs[0][0].setUB(1);
//        for (int k = 0; k < digits; k++) {
//            for (int l = 0; l < existingWs[0].length; l++) {
//                if (l == 0 && k > 1)
//                    continue;
//                existingWs[k][l].setLB(0);
//                existingWs[k][l].setUB(1);
//            }
//        }
        Arrays.stream(wVariableLBs.get(change.getAction())).forEach(array -> Arrays.fill(array, 0));
        Arrays.stream(wVariableUBs.get(change.getAction())).forEach(array -> Arrays.fill(array, 1));
    }

    public void resetPrecision(Action action) {
        precision.remove(action);
    }
}
