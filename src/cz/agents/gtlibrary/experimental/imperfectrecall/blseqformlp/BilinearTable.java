package cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp;

import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPData;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.RecyclingLPTable;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.change.Change;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.utils.DummyMap;
import cz.agents.gtlibrary.utils.DummySet;
import cz.agents.gtlibrary.utils.Pair;
import ilog.concert.*;
import ilog.cplex.IloCplex;

import java.util.*;

public class BilinearTable extends RecyclingLPTable {

    public static final boolean DELETE_PRECISION_CONSTRAINTS_ONLY = true;

    protected Map<Object, Pair<Object, Object>> bilinearVars;
    protected Map<Object, Pair<Object, Object>> bilinearVarsToUpdate;
    protected Map<Object, Integer> precision;
    protected Map<Object, Integer> highestPrecision;

    protected Map<Object, IloNumVar[][]> wVariables; // information set -> binary variables for the bilinear terms for that IS
    protected Map<Object, double[][]> wVariableLBs;
    protected Map<Object, double[][]> wVariableUBs;
    protected Map<Object, IloNumVar[][]> rHatVariables; // sequence -> rHat variables for sequence
    protected Map<Object, IloNumVar> deltaBehavioralVariables; // action -> delta x variable
    protected Map<Object, IloNumVar> deltaSequenceVariables; // sequence -> delta x variable
    protected Map<Object, IloRange> outgoingBilinearConstraints; // information set -> all constraints for the bilinear terms for that IS
    protected Map<Object, IloRange> behavioralBilinearConstraints; // information set -> all constraints for the bilinear terms for that IS

    protected Map<Object, IloNumVar> bilinVariableBackup;
    protected Map<Object, IloNumVar[][]> wVariableBackup;
    protected Map<Object, IloNumVar[][]> rHatVariableBackup;

    protected Set<IloConstraint> precisionConstraints;

    final int digits = 10;
    final int maxPrecision = 7;

    public final int INITIAL_MDT_PRECISION = 2;

    public BilinearTable() {
        super();
        bilinearVars = new HashMap<>();
        bilinearVarsToUpdate = new HashMap<>();
        precision = new HashMap<>();
        highestPrecision = new HashMap<>();
        wVariables = new HashMap<>();
        wVariableLBs = new HashMap<>();
        wVariableUBs = new HashMap<>();
        rHatVariables = new HashMap<>();
        deltaBehavioralVariables = new HashMap<>();
        deltaSequenceVariables = new HashMap<>();
        outgoingBilinearConstraints = new HashMap<>();
        behavioralBilinearConstraints = new HashMap<>();
        bilinVariableBackup = USE_VAR_BACKUP ? new HashMap<>() : new DummyMap<>();
        rHatVariableBackup = USE_VAR_BACKUP ? new HashMap<>() : new DummyMap<>();
        wVariableBackup = USE_VAR_BACKUP ? new HashMap<>() : new DummyMap<>();
        precisionConstraints = USE_VAR_BACKUP ? new HashSet<>() : new DummySet<>();
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

        deleteBilinearConstraints(data);
        for (Map.Entry<Object, Pair<Object, Object>> bilinKey : bilinearVars.entrySet()) {
            addBilinearConstraint(data, bilinKey.getKey(), bilinKey.getValue().getLeft(), bilinKey.getValue().getRight(),
                    getPrecisionFor(bilinKey.getValue().getRight()), getHighestPrecisionFor(bilinKey.getValue().getRight()));
        }
        bilinearVarsToUpdate.clear();
        updateWBounds();
        return data;
    }

    private void deleteBilinearConstraints(LPData data) throws IloException {
        Set<IloCopyable> toDelete = new HashSet<>();

        for (Map.Entry<Object, Pair<Object, Object>> bilinKey : bilinearVars.entrySet()) {
            if (outgoingBilinearConstraints.containsKey(bilinKey.getKey()))
                toDelete.add(outgoingBilinearConstraints.get(bilinKey.getKey()));
            if (behavioralBilinearConstraints.containsKey(bilinKey.getValue().getRight()))
                toDelete.add(behavioralBilinearConstraints.get(bilinKey.getValue().getRight()));
        }
        data.getSolver().delete(toDelete.toArray(new IloCopyable[toDelete.size()]));
    }

    protected void updateWBounds() throws IloException {
        for (Map.Entry<Object, IloNumVar[][]> entry : wVariables.entrySet()) {
            double[][] lbs = getLBs(entry.getKey());
            double[][] ubs = getUBs(entry.getKey());
            IloNumVar[][] wVariableArray = entry.getValue();

            for (int i = 0; i < wVariableArray.length; i++) {
                for (int j = 0; j < wVariableArray[0].length; j++) {
                    if (wVariableArray[i][j] == null)
                        continue;
                    wVariableArray[i][j].setLB(lbs[i][j]);
                    wVariableArray[i][j].setUB(ubs[i][j]);
                }
            }
        }
    }

    protected void addBilinearConstraint(LPData data, Object bilinVarKey, Object factor1, Object factor2, int precision, int highestPrecision) throws IloException {
        IloRange[] newConstraints = addMDTConstraints(data, bilinVarKey, factor1, factor2, precision);

        behavioralBilinearConstraints.put(factor2, newConstraints[0]);
        outgoingBilinearConstraints.put(bilinVarKey, newConstraints[1]);
    }


    public void refinePrecisionOfRelevantBilinearVars(Object factor2) {
        int newPrecision = getPrecisionFor(factor2) + 1;
        precision.put(factor2, newPrecision);
        updateHighestPrecision(factor2, newPrecision);
        bilinearVars.entrySet().stream()
                .filter(bilinearVar -> bilinearVar.getValue().getRight().equals(factor2))
                .forEach(bilinearVar -> bilinearVarsToUpdate.put(bilinearVar.getKey(), bilinearVar.getValue()));
    }

    protected void updateHighestPrecision(Object factor2, int newPrecision) {
        int current = getHighestPrecisionFor(factor2);

        highestPrecision.put(factor2, Math.max(current, newPrecision));
    }

    public IloRange[] addMDTConstraints(LPData data, Object product, Object sequence, Object behavioral, int precision) throws IloException {
        IloCplex cplex = data.getSolver();
        IloNumVar rSequenceToIS = data.getVariables()[getVariableIndex(sequence)];
        IloNumVar xBehStrategy = data.getVariables()[getVariableIndex(behavioral)];
        IloNumVar rSequenceFromIS = data.getVariables()[getVariableIndex(product)];

        IloNumVar xBehDelta = deltaBehavioralVariables.get(behavioral);
        if (xBehDelta == null) {
            if (USE_CUSTOM_NAMES)
                xBehDelta = bilinVariableBackup.getOrDefault(behavioral, cplex.numVar(0, 1, IloNumVarType.Float, "DELTA_" + behavioral.toString()));
            else
                xBehDelta = bilinVariableBackup.getOrDefault(behavioral, cplex.numVar(0, 1, IloNumVarType.Float));
            bilinVariableBackup.putIfAbsent(behavioral, xBehDelta);
            deltaBehavioralVariables.put(behavioral, xBehDelta);
        }

        IloNumVar xSeqDelta = deltaSequenceVariables.get(product);
        if (xSeqDelta == null) {
            if (USE_CUSTOM_NAMES)
                xSeqDelta = bilinVariableBackup.getOrDefault(product, cplex.numVar(0, 1, IloNumVarType.Float, "DELTA_" + product.toString()));
            else
                xSeqDelta = bilinVariableBackup.getOrDefault(product, cplex.numVar(0, 1, IloNumVarType.Float));
            bilinVariableBackup.putIfAbsent(product, xSeqDelta);
            deltaSequenceVariables.put(product, xSeqDelta);
        }

        IloRange[] result = new IloRange[2];
        IloNumVar[][] w;

        if (wVariables.containsKey(behavioral)) {
            IloNumVar[][] existingWs = wVariables.get(behavioral);

            w = new IloNumVar[digits][precision];
            for (int d = 0; d < digits; d++)
                for (int existingPrecision = 0; existingPrecision < Math.min(precision, existingWs[d].length); existingPrecision++) {
                    w[d][existingPrecision] = existingWs[d][existingPrecision];
                }
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
        }
        IloNumExpr productSum = xSeqDelta;
        IloNumExpr approxSum = xBehDelta;
        double[][] lbs = getLBs(behavioral);
        double[][] ubs = getUBs(behavioral);
        IloNumVar[][] wBackup = wVariableBackup.getOrDefault(behavioral, new IloNumVar[digits][maxPrecision]);
        IloNumVar[][] rHatBackup = rHatVariableBackup.getOrDefault(product, new IloNumVar[digits][maxPrecision]);

        wVariableBackup.putIfAbsent(behavioral, wBackup);
        rHatVariableBackup.putIfAbsent(product, rHatBackup);
        for (int l = 0; l < precision; l++) {
            IloNumExpr xSum = cplex.numExpr();
            IloNumExpr wSum = cplex.numExpr();
            boolean thisPrecisionExists = false;

            for (int k = 0; k < digits; k++) {
                if ((l == 0) && (k > 1)) continue;
                if (w[k][l] == null) {
                    if (wBackup[k][l] == null) {
                        if (USE_CUSTOM_NAMES)
                            w[k][l] = cplex.numVar(lbs[k][l], ubs[k][l], IloNumVarType.Float, "W_" + behavioral.toString() + "_" + k + "_" + l);
                        else
                            w[k][l] = cplex.numVar(lbs[k][l], ubs[k][l], IloNumVarType.Float);
                        wBackup[k][l] = w[k][l];
                    } else {
                        w[k][l] = wBackup[k][l];
                        w[k][l].setUB(ubs[k][l]);
                        w[k][l].setLB(lbs[k][l]);
                    }
                }
                wSum = cplex.sum(wSum, w[k][l]);
                if (rHat[k][l] == null) {
                    if (rHatBackup[k][l] == null) {
                        if (USE_CUSTOM_NAMES)
                            rHat[k][l] = cplex.numVar(0, 1, IloNumVarType.Float, "RHAT_" + product + "_" + k + "_" + l);
                        else
                            rHat[k][l] = cplex.numVar(0, 1, IloNumVarType.Float);
                        rHatBackup[k][l] = rHat[k][l];
                    } else {
                        rHat[k][l] = rHatBackup[k][l];
                    }
                    precisionConstraints.add(cplex.addLe(rHat[k][l], w[k][l]));
                    precisionConstraints.add(cplex.addLe(rHat[k][l], rSequenceToIS));
                } else {
                    thisPrecisionExists = true;
                }
                productSum = cplex.sum(productSum, cplex.prod(cplex.constant(Math.pow(10, -(l)) * k), rHat[k][l]));
                xSum = cplex.sum(xSum, rHat[k][l]);
                approxSum = cplex.sum(approxSum, cplex.prod(cplex.constant(Math.pow(10, -(l)) * k), w[k][l]));
            }
            if (!thisPrecisionExists) {
                precisionConstraints.add(cplex.addEq(rSequenceToIS, xSum));
                precisionConstraints.add(cplex.addEq(wSum, 1));
                precisionConstraints.add(cplex.addLe(xBehDelta, Math.pow(10, -(precision - 1))));
                precisionConstraints.add(cplex.addLe(xSeqDelta, cplex.prod(Math.pow(10, -(precision - 1)), rSequenceToIS)));
            }
        }
        result[0] = cplex.addEq(cplex.diff(xBehStrategy, approxSum), 0);
        result[1] = cplex.addEq(cplex.diff(rSequenceFromIS, productSum), 0);
        precisionConstraints.add(result[0]);
        precisionConstraints.add(result[1]);
        wVariables.put(behavioral, w);
        rHatVariables.put(product, rHat);
        return result;
    }

    public boolean updateWBoundsForLeft(Change change) {
        double[][] lbs = getLBs(change.getAction());
        double[][] ubs = getUBs(change.getAction());
        boolean changed = false;

        ubs[1][0] = 0;
        lbs[0][0] = 1;
        if (change.getFixedDigitArrayValue().get(0) == 1) {
            assert false;
            return true;
        }

        int digitToFix = getLastFixedDigit(change);

        if (digitToFix == 0)
            return false;
        for (int k = digitToFix; k < digits; k++) {
            if ((change.getFixedDigitArrayValue().size() == 0) && (k > 1))
                continue;
            if (lbs[k][change.getFixedDigitCount()] < 1) {
                if (ubs[k][change.getFixedDigitCount()] == 1)
                    changed = true;
                ubs[k][change.getFixedDigitCount()] = 0;
            }
        }
        return changed;
    }

    public boolean updateWBoundsForRight(Change change) {
        double[][] lbs = getLBs(change.getAction());
        double[][] ubs = getUBs(change.getAction());

        boolean changed = false;

        if (change.getFixedDigitArrayValue().get(0) == 1) {
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

    protected double[][] getLBs(Object object) {
        double[][] lbs = wVariableLBs.get(object);

        if (lbs == null) {
            lbs = new double[digits][maxPrecision];
            Arrays.stream(lbs).forEach(array -> Arrays.fill(array, 0));
            wVariableLBs.put(object, lbs);
        }
        return lbs;
    }

    protected double[][] getUBs(Object object) {
        double[][] ubs = wVariableUBs.get(object);

        if (ubs == null) {
            ubs = new double[digits][maxPrecision];
            Arrays.stream(ubs).forEach(array -> Arrays.fill(array, 1));
            wVariableUBs.put(object, ubs);
        }
        return ubs;
    }

    public boolean updateWBoundsForMiddle(Change change) {
        double[][] lbs = getLBs(change.getAction());
        double[][] ubs = getUBs(change.getAction());
        boolean changed = false;

        if (change.getFixedDigitArrayValue().get(0) == 1) {
            lbs[1][0] = 1;
            ubs[0][0] = 0;
            assert false;
        } else {
            lbs[0][0] = 1;
            ubs[1][0] = 0;
        }
        for (int k = 0; k < digits; k++) {
            for (int l = 1; l < change.getFixedDigitArrayValue().size(); l++) {
                if ((l == 0) && (k > 1)) continue;
                if (k != change.getFixedDigitArrayValue().get(l)) {
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

    protected int getLastFixedDigit(Change change) {
        return change.getFixedDigitArrayValue().get(change.getFixedDigitArrayValue().size() - 1);
    }

    public int getPrecisionFor(Object factor2) {
        Integer precisionValue = precision.get(factor2);

        if (precisionValue == null)
            return INITIAL_MDT_PRECISION;
        return precisionValue;
    }

    public int getHighestPrecisionFor(Object factor2) {
        Integer precisionValue = highestPrecision.get(factor2);

        if (precisionValue == null)
            return INITIAL_MDT_PRECISION;
        return precisionValue;
    }

    public Map<Object, Integer> getHighestPrecision() {
        return highestPrecision;
    }

    public Map<Object, Integer> getPrecision() {
        return precision;
    }

    public void removeWUpdate(Change change) throws IloException {
        Arrays.stream(wVariableLBs.get(change.getAction())).forEach(array -> Arrays.fill(array, 0));
        Arrays.stream(wVariableUBs.get(change.getAction())).forEach(array -> Arrays.fill(array, 1));
    }

    public void resetPrecision(Action action) {
        precision.remove(action);
    }

//    public void removeWVariables() {
//        wVariables.values().forEach(variables -> removeWVariables(variables));
//        wVariables = new HashMap<>();
//    }
//
//    protected void removeWVariables(IloNumVar[][] wVariables) {
//        Arrays.stream(wVariables).forEach(array ->
//                Arrays.stream(array).forEach(wVariable -> {
//                    try {
//                        cplex.remove(wVariable);
//                    } catch (IloException e) {
//                        e.printStackTrace();
//                    }
//                }));
//    }
//
//    public void removeWVariableFor(Object key) {
//        IloNumVar[][] variables = wVariables.get(key);
//
//        if (variables != null) {
//            removeWVariables(variables);
//            wVariables.remove(key);
//        }
//    }

    @Override
    public void clearTable() {
//        bilinearVars = new HashMap<>();
        bilinearVarsToUpdate = new HashMap<>();
        precision = new HashMap<>();
        highestPrecision = new HashMap<>();
        wVariables = new HashMap<>();
        rHatVariables = new HashMap<>();
        deltaBehavioralVariables = new HashMap<>();
        deltaSequenceVariables = new HashMap<>();
        outgoingBilinearConstraints = new HashMap<>();
        behavioralBilinearConstraints = new HashMap<>();
        for (Object key : wVariableLBs.keySet()) {
            Arrays.stream(wVariableLBs.get(key)).forEach(array -> Arrays.fill(array, 0));
            Arrays.stream(wVariableUBs.get(key)).forEach(array -> Arrays.fill(array, 1));
        }
        if (USE_VAR_BACKUP) {
            try {
                for (IloNumVar[][] wVars : wVariableBackup.values()) {
                    for (int i = 0; i < wVars.length; i++) {
                        for (int j = 0; j < wVars[0].length; j++) {
                            if (wVars[i][j] != null) {
                                wVars[i][j].setLB(0);
                                wVars[i][j].setUB(1);
                            }
                        }
                    }
                }
            } catch (IloException e) {
                e.printStackTrace();
            }
            if (precisionConstraints != null)
                try {
                    cplex.remove(precisionConstraints.toArray(new IloAddable[precisionConstraints.size()]));
                } catch (IloException e) {
                    e.printStackTrace();
                }
        }
        precisionConstraints = new HashSet<>();
        if (!DELETE_PRECISION_CONSTRAINTS_ONLY)
            super.clearTable();
    }
}
