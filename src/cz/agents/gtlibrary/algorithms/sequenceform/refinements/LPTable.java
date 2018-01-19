/*
Copyright 2014 Faculty of Electrical Engineering at CTU in Prague

This file is part of Game Theoretic Library.

Game Theoretic Library is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Game Theoretic Library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with Game Theoretic Library.  If not, see <http://www.gnu.org/licenses/>.*/


package cz.agents.gtlibrary.algorithms.sequenceform.refinements;

import ilog.concert.*;
import ilog.cplex.IloCplex;

import java.util.*;
import java.util.Map.Entry;

public class LPTable {

    public enum ConstraintType {LE, EQ, GE}

    public static boolean USE_CUSTOM_NAMES = true;
    public static int CPLEXALG = IloCplex.Algorithm.Auto;
    public static int CPLEXTHREADS = 1; // change to 0 to have no restrictions

    protected Map<Object, Double> objective;
    protected Map<Object, Map<Object, Double>> constraints;
    protected Map<Object, Double> constants;

    protected Map<Object, Integer> equationIndices;
    protected Map<Object, Integer> variableIndices;
    protected Map<Object, Integer> primalWatch;
    protected Map<Object, Integer> dualWatch;
    protected Set<Object> relaxableConstraints;

    protected Map<Object, Integer> constraintTypes;
    protected Map<Object, Double> lb;
    protected Map<Object, Double> ub;

    protected IloCplex cplex;

    public LPTable() {
        constants = new LinkedHashMap<>();
        constraints = new LinkedHashMap<>();
        objective = new LinkedHashMap<>();

        equationIndices = new LinkedHashMap<>();
        variableIndices = new LinkedHashMap<>();
        primalWatch = new LinkedHashMap<>();
        dualWatch = new LinkedHashMap<>();
        relaxableConstraints = new HashSet<>();

        constraintTypes = new LinkedHashMap<>();
        lb = new LinkedHashMap<>();
        ub = new LinkedHashMap<>();
        try {
            cplex = new IloCplex();
        } catch (IloException e) {
            e.printStackTrace();
        }
    }

    public boolean existsEqKey(Object eqKey){
        if (equationIndices.get(eqKey) != null) return true;
        return false;
    }

    public LPTable(int m, int n) {
        constants = new LinkedHashMap<>(m);
        constraints = new LinkedHashMap<>(m);
        objective = new LinkedHashMap<>(n);

        equationIndices = new LinkedHashMap<>(m);
        variableIndices = new LinkedHashMap<>(n);
        primalWatch = new LinkedHashMap<>();
        dualWatch = new LinkedHashMap<>();
        relaxableConstraints = new HashSet<>();

        constraintTypes = new LinkedHashMap<>(m);
        lb = new LinkedHashMap<>(n);
        ub = new LinkedHashMap<>();
        try {
            cplex = new IloCplex();
        } catch (IloException e) {
            e.printStackTrace();
        }
    }

    public double get(Object eqKey, Object varKey) {
        Double value = constraints.get(eqKey).get(varKey);

        return value == null ? 0 : value;
    }

    protected void updateEquationIndices(Object eqKey) {
        getEquationIndex(eqKey);
    }

    protected void updateVariableIndices(Object varKey) {
        getVariableIndex(varKey);
    }

    public void setObjective(Object varKey, double value) {
        objective.put(varKey, value);
        updateVariableIndices(varKey);
    }

    public void addToObjective(Object varKey, double value) {
        Double oldValue = objective.get(varKey);

        objective.put(varKey, oldValue == null ? value : oldValue + value);
        updateVariableIndices(varKey);
    }

    public double getObjective(Object varKey) {
        Double value = objective.get(varKey);

        return value == null ? 0 : value;
    }

    public void setConstant(Object eqKey, double value) {
        if (Math.abs(value) < Double.MIN_VALUE)
            return;
        constants.put(eqKey, value);
        updateEquationIndices(eqKey);
    }

    public double getConstant(Object eqKey) {
        Double value = constants.get(eqKey);

        return value == null ? 0 : value.doubleValue();
    }

    public void setConstraint(Object eqKey, Object varKey, double value) {
        Map<Object, Double> row = constraints.get(eqKey);

        if (row == null) {
            row = new LinkedHashMap<>();
            constraints.put(eqKey, row);
        }
        row.put(varKey, value);
        updateEquationIndices(eqKey);
        updateVariableIndices(varKey);
    }

    public void addToConstraint(Object eqKey, Object varKey, double value) {
        setConstraint(eqKey, varKey, get(eqKey, varKey) + value);
    }

    public void substractFromConstraint(Object eqKey, Object varKey, double value) {
        if (Math.abs(value) < Double.MIN_VALUE)
            return;
        setConstraint(eqKey, varKey, get(eqKey, varKey) - value);
    }

    public int rowCount() {
        return equationIndices.size();
    }

    public int columnCount() {
        return variableIndices.size();
    }

    public int getEquationIndex(Object eqKey) {
        return getIndex(eqKey, equationIndices);
    }

    public int getVariableIndex(Object varKey) {
        return getIndex(varKey, variableIndices);
    }

    protected int getIndex(Object key, Map<Object, Integer> map) {
        Integer result = map.get(key);

        if (result == null) {
            result = map.size();
            map.put(key, result);
        }
        return result;
    }

    public void watchPrimalVariable(Object varKey, Object watchKey) {
        primalWatch.put(watchKey, getVariableIndex(varKey));
    }

    public void watchDualVariable(Object eqKey, Object watchKey) {
        dualWatch.put(watchKey, getEquationIndex(eqKey));
    }

    public LPData toCplex() throws IloException {
        cplex.clearModel();
        cplex.setParam(IloCplex.IntParam.RootAlg, CPLEXALG);
        cplex.setParam(IloCplex.IntParam.Threads, CPLEXTHREADS);
        cplex.setParam(IloCplex.IntParam.MIPEmphasis, IloCplex.MIPEmphasis.Balanced);
//        cplex.setParam(IloCplex.DoubleParam.EpMrk, 0.99999);
//		cplex.setParam(IloCplex.DoubleParam.BarEpComp, 1e-4);
//		System.out.println("BarEpComp: " + cplex.getParam(IloCplex.DoubleParam.BarEpComp));
//        cplex.setParam(IloCplex.BooleanParam.NumericalEmphasis, true);
        cplex.setOut(null);

        System.out.println("Getting variables.");
        IloNumVar[] variables = getVariables();
//        IloRange[] constraints = addConstraints(cplex, variables);
        IloRange[] constraints = addConstraintsViaMatrix(cplex, variables);
//        System.out.println("Var len:" + variables.length);
//        System.out.println("Con len:" + constraints.length);

        addObjective(variables);
        return new LPData(cplex, variables, constraints, getRelaxableConstraints(constraints), getWatchedPrimalVars(variables), getWatchedDualVars(constraints));
    }

    protected IloNumVar[] getVariables() throws IloException {
        double[] ub = getUpperBounds();
        double[] lb = getLowerBounds();
        if(USE_CUSTOM_NAMES) {
            String[] variableNames = getVariableNames();

            return cplex.numVarArray(variableNames.length, lb, ub, variableNames);
        } else {
            return cplex.numVarArray(lb.length, lb, ub);
        }
    }

    protected Map<Object, IloRange> getRelaxableConstraints(IloRange[] constraints) {
        Map<Object, IloRange> relaxableConstraints = new HashMap<Object, IloRange>(this.relaxableConstraints.size());

        for (Object eqKey : this.relaxableConstraints) {
            relaxableConstraints.put(eqKey, constraints[getEquationIndex(eqKey)]);
        }
        return relaxableConstraints;
    }

    protected String[] getVariableNames() {
        String[] variableNames = new String[columnCount()];

        for (Object variable : variableIndices.keySet()) {
//            if (variable instanceof Pair) {
////                if (((Pair)variable).getLeft() == null) variable = new Pair("null", ((Pair)variable).getRight());
//                System.out.println(((Pair)variable).getLeft());
//                System.out.println(((Pair)variable).getRight());
//            }
            variableNames[getVariableIndex(variable)] = variable.toString();
        }
        return variableNames;
    }

    protected double[] getLowerBounds() {
        double[] lb = new double[columnCount()];

        for (Entry<Object, Double> entry : this.lb.entrySet()) {
            lb[getVariableIndex(entry.getKey())] = entry.getValue();
        }
        return lb;
    }

    protected double[] getUpperBounds() {
        double[] ub = new double[columnCount()];

        for (int i = 0; i < columnCount(); i++) {
            ub[i] = Double.POSITIVE_INFINITY;
        }
        for (Entry<Object, Double> entry : this.ub.entrySet()) {
            ub[getVariableIndex(entry.getKey())] = entry.getValue();
        }
        return ub;
    }

    protected Map<Object, IloRange> getWatchedDualVars(IloRange[] constraints) {
        Map<Object, IloRange> watchedDualVars = new LinkedHashMap<Object, IloRange>();

        for (Entry<Object, Integer> entry : dualWatch.entrySet()) {
            watchedDualVars.put(entry.getKey(), constraints[entry.getValue()]);
        }
        return watchedDualVars;
    }

    protected Map<Object, IloNumVar> getWatchedPrimalVars(IloNumVar[] variables) {
        Map<Object, IloNumVar> watchedPrimalVars = new LinkedHashMap<Object, IloNumVar>();

        for (Entry<Object, Integer> entry : primalWatch.entrySet()) {
            watchedPrimalVars.put(entry.getKey(), variables[entry.getValue()]);
        }
        return watchedPrimalVars;
    }

    protected IloRange[] addConstraintsViaMatrix(IloCplex cplex, IloNumVar[] x) throws IloException {
        IloLPMatrix matrix = cplex.addLPMatrix();
        matrix.addCols(x);
        System.out.println("Adding constraints.");
        int[][] idxs = new int[constraints.keySet().size()][];
        double[][] vals = new double[idxs.length][];
        double[] lbs = new double[idxs.length];
        double[] ubs = new double[idxs.length];
        int j = 0;
        for(Object con : constraints.keySet()){
            int[] idx = new int[constraints.get(con).keySet().size()];
            double[] val = new double[idx.length];
            int i = 0;
            for(Object var : constraints.get(con).keySet()){
                idx[i] = variableIndices.get(var);
                val[i] = constraints.get(con).get(var);
                i++;
            }
            idxs[j] = idx;
            vals[j] = val;
            if (!constraintTypes.containsKey(con)) System.out.println(con);
            switch (constraintTypes.get(con)){
//                case 0 : matrix.addRow(Double.NEGATIVE_INFINITY, getConstant(con), idx, val); break;
//                case 1 : matrix.addRow(getConstant(con), getConstant(con), idx, val); break;
//                case 2 : matrix.addRow(getConstant(con), Double.POSITIVE_INFINITY, idx, val); break;
                case 0 : lbs[j] = Double.NEGATIVE_INFINITY; ubs[j] = getConstant(con); break;
                case 1 : lbs[j] = getConstant(con); ubs[j] =  getConstant(con); break;
                case 2 : lbs[j] = getConstant(con); ubs[j] =  Double.POSITIVE_INFINITY; break;
            }
            j++;
//            matrix.addRow(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, idx, vals);
        }
        matrix.addRows(lbs, ubs, idxs, vals);
//        System.out.println("LPTABLE : adding cons.");
//        System.out.println(matrix.toString());
//        System.out.println("Vars: " + variableIndices.size() + "; cons: " + constraints.size());
//        System.out.println("Generating ranges.");
//        matrix.
//        matrix.addRow(); CpxLPMatrix
        return matrix.getRanges();
    }

    protected IloRange[] addConstraints(IloCplex cplex, IloNumVar[] x) throws IloException {
        IloRange[] cplexConstraints = new IloRange[rowCount()];

        for (Entry<Object, Map<Object, Double>> rowEntry : constraints.entrySet()) {
            assert rowEntry.getValue().get("t") == null || rowEntry.getValue().get("t") == 1;
            IloLinearNumExpr rowExpr = createRowExpresion(cplex, x, rowEntry);
            Integer constraintType = getConstraintType(rowEntry);
            int equationIndex = getEquationIndex(rowEntry.getKey());

            switch (constraintType) {
                case 0:
                    if (USE_CUSTOM_NAMES)
                        cplexConstraints[equationIndex] = cplex.addLe(rowExpr, getConstant(rowEntry.getKey()), rowEntry.getKey().toString());
                    else
                        cplexConstraints[equationIndex] = cplex.addLe(rowExpr, getConstant(rowEntry.getKey()));
                    break;
                case 1:
                    if (USE_CUSTOM_NAMES)
                        cplexConstraints[equationIndex] = cplex.addEq(rowExpr, getConstant(rowEntry.getKey()), rowEntry.getKey().toString());
                    else
                        cplexConstraints[equationIndex] = cplex.addEq(rowExpr, getConstant(rowEntry.getKey()));
                    break;
                case 2:
                    if (USE_CUSTOM_NAMES)
                        cplexConstraints[equationIndex] = cplex.addGe(rowExpr, getConstant(rowEntry.getKey()), rowEntry.getKey().toString());
                    else
                        cplexConstraints[equationIndex] = cplex.addGe(rowExpr, getConstant(rowEntry.getKey()));
                    break;
                default:
                    break;
            }
        }
        return cplexConstraints;
    }

    protected int getConstraintType(Entry<Object, Map<Object, Double>> rowEntry) {
        Integer constraintType = constraintTypes.get(rowEntry.getKey());

        return constraintType == null ? 0 : constraintType;
    }

    protected IloLinearNumExpr createRowExpresion(IloCplex cplex, IloNumVar[] x, Entry<Object, Map<Object, Double>> rowEntry) throws IloException {
        IloLinearNumExpr rowExpr = cplex.linearNumExpr();

        for (Entry<Object, Double> memberEntry : rowEntry.getValue().entrySet()) {
            rowExpr.addTerm(memberEntry.getValue(), x[getVariableIndex(memberEntry.getKey())]);
        }
        return rowExpr;
    }

    protected void addObjective(IloNumVar[] x) throws IloException {
        IloLinearNumExpr objExpr = cplex.linearNumExpr();

        for (Entry<Object, Double> entry : objective.entrySet()) {
            objExpr.addTerm(entry.getValue(), x[variableIndices.get(entry.getKey())]);
        }
        cplex.addMaximize(objExpr);
    }

    /**
     * Mark constraint, which might cause infeasibility due to numeric instability
     *
     * @param eqKey
     */
    public void markRelaxableConstraint(Object eqKey) {
        relaxableConstraints.add(eqKey);
    }

    /**
     * Remove constraint from relaxable constraints
     *
     * @param eqKey
     */
    public void unmarkRelaxableConstraint(Object eqKey) {
        relaxableConstraints.remove(eqKey);
    }

    /**
     * Set constraint for equation represented by eqObject, default constraint is ge
     *
     * @param eqKey
     * @param type  0 ... le, 1 .. eq, 2 ... ge
     */
    public void setConstraintType(Object eqKey, int type) {
        constraintTypes.put(eqKey, type);
    }

    public void setConstraintType(Object eqKey, ConstraintType type) {
        setConstraintType(eqKey, type.ordinal());
    }

    /**
     * Set lower bound for variable represented by varObject, default reward is 0
     *
     * @param varKey
     * @param value
     */
    public void setLowerBound(Object varKey, double value) {
        lb.put(varKey, value);
    }

    /**
     * Set upper bound for variable represented by varObject, default reward is POSITIVE_INFINITY
     *
     * @param varKey
     * @param value
     */
    public void setUpperBound(Object varKey, double value) {
        ub.put(varKey, value);
    }

    public void clearTable() {
        cplex.setOut(null);
        constants = new LinkedHashMap<>();
        constraints = new LinkedHashMap<>();
        objective = new LinkedHashMap<>();

        equationIndices = new LinkedHashMap<>();
        variableIndices = new LinkedHashMap<>();
        primalWatch = new LinkedHashMap<>();
        dualWatch = new LinkedHashMap<>();

        constraintTypes = new LinkedHashMap<>();
        lb = new LinkedHashMap<>();
        ub = new LinkedHashMap<>();
        try {
            cplex.clearModel();
            cplex.setParam(IloCplex.IntParam.RootAlg, CPLEXALG);
            cplex.setParam(IloCplex.IntParam.Threads, CPLEXTHREADS);
        } catch (IloException e) {
            e.printStackTrace();
        }
    }

    public void removeFromConstraint(Object eqKey, Object varKey) {
        Map<Object, Double> row = constraints.get(eqKey);

        if (row != null)
            row.remove(varKey);
    }

    public void watchAllPrimalVariables() {
        for (Object varKey : variableIndices.keySet()) {
            watchPrimalVariable(varKey, varKey);
        }
    }

    public void watchAllDualVariables() {
        for (Object varKey : equationIndices.keySet()) {
            watchDualVariable(varKey, varKey);
        }
    }

    public boolean compareConstraintsSize(LPTable table){
        boolean OUTPUT = false;
        int[] typesThis = new int[3];
        int[] typesThat = new int[3];
        for (Object eqKey : constraintTypes.keySet()) typesThis[constraintTypes.get(eqKey)]++;
        for (Object eqKey : table.constraintTypes.keySet()) typesThat[table.constraintTypes.get(eqKey)]++;
        if (constraints.keySet().size() > table.constraints.keySet().size()) {
            System.out.println("Greater number of constraints: ");// + constraints.keySet().size() + "/" + table.constraints.keySet().size());
            System.out.println("Leqs = " + typesThis[0] + "/" + typesThat[0]);
            System.out.println("Eqs = " + typesThis[1] + "/" + typesThat[1]);
            System.out.println("Geqs = " + typesThis[2] + "/" + typesThat[2]);

            // approximate comparison : eqkey2 might have more variables than eqkey !
            for (Object eqkey : constraints.keySet()) {
                if (!table.constraints.containsKey(eqkey)) System.out.println(eqkey);
                boolean hasEquivalent = false;
                for (Object eqkey2 : table.constraints.keySet()) {
                    boolean same = true;
                    for (Object varKey : constraints.get(eqkey).keySet()) {
//                        System.out.println("Controlling var "+varKey);
                        if (!table.constraints.get(eqkey2).containsKey(varKey) || !((Double)Math.abs(table.constraints.get(eqkey2).get(varKey))).equals(Math.abs(constraints.get(eqkey).get(varKey)))) {
                            same = false;
                            break;
                        }
                    }
                    if (same) {
//                        System.out.println("Found same equation of type " + constraintTypes.get(eqkey2));
                        hasEquivalent = true;
                        break;
                    }
                }
                if (!hasEquivalent) {
                    if (OUTPUT) {

                    System.out.println("Found non-equivalent equation: " + eqkey);
                    System.out.println("Vars :");
                    for (Object varKey : table.constraints.get(eqkey).keySet()) {
                        System.out.println(varKey + " : " + table.constraints.get(eqkey).get(varKey));
                    }
                }
                }
            }
        }
        if (constraints.keySet().size() < table.constraints.keySet().size()) {
            System.out.println("Lesser number of constraints:");// + constraints.keySet().size() + "/" + table.constraints.keySet().size());
            System.out.println("Leqs = " + typesThis[0] + "/" + typesThat[0]);
            System.out.println("Eqs = " + typesThis[1] + "/" + typesThat[1]);
            System.out.println("Geqs = " + typesThis[2] + "/" + typesThat[2]);

            HashSet notHavingEquivalent = new HashSet(constraints.keySet());
            notHavingEquivalent.addAll(constraints.keySet());
            for (Object eqkey : table.constraints.keySet()) {
                boolean hasEquivalent = false;
                for (Object eqkey2 : constraints.keySet()) {
                    if (table.constraints.get(eqkey).size() != constraints.get(eqkey2).size()) continue;
                    boolean same = true;
                    for (Object varKey : table.constraints.get(eqkey).keySet()) {
//                        System.out.println("Controlling var "+varKey);
                        if (!constraints.get(eqkey2).containsKey(varKey) || !((Double)Math.abs(constraints.get(eqkey2).get(varKey))).equals(Math.abs(table.constraints.get(eqkey).get(varKey)))) {
                            same = false;
                            break;
                        }
                    }
                    if (same) {
//                        System.out.println("Found equivalent !");
//                        System.out.println("Original: "+eqkey);
//                        for (Object varKey : table.constraints.get(eqkey).keySet()) {
//                            System.out.println(varKey + " : " + table.constraints.get(eqkey).get(varKey));
//                        }
//                        System.out.println("Equivalent: "+eqkey2);
//                        for (Object varKey : constraints.get(eqkey2).keySet()) {
//                            System.out.println(varKey + " : " + constraints.get(eqkey2).get(varKey));
//                        }
//                        System.out.println("Number of not equivalent = " + notHavingEquivalent.size());
                        notHavingEquivalent.remove(eqkey2);
//                        System.out.println("Found same equation of type " + constraintTypes.get(eqkey2));
                        hasEquivalent = true;
                        break;
                    }
                }
                if (!hasEquivalent){
                    if (OUTPUT) {
                        System.out.println("Found non-equivalent equation: " + eqkey + " of type " + table.constraintTypes.get(eqkey));
                        System.out.println("Vars :");
                        for (Object varKey : table.constraints.get(eqkey).keySet()) {
                            System.out.println(varKey + " : " + table.constraints.get(eqkey).get(varKey));
                        }
                    }
                }
            }
            System.out.println("Number of not equivalent = " + notHavingEquivalent.size());
            if(OUTPUT) {
                for (Object eqkey : notHavingEquivalent) {
                    System.out.println("Not equivalent: " + eqkey);
                    for (Object varKey : constraints.get(eqkey).keySet()) {
                        System.out.println(varKey + " : " + constraints.get(eqkey).get(varKey));
                    }
                }
            }
        }
        return constraints.keySet().size() == table.constraints.keySet().size();
    }

    public void compareConstraints(LPTable table){
        for (Object obj : objective.keySet())
            if (!objective.get(obj).equals(table.objective.get(obj)))
                System.out.println("Different objective for " + obj);
        for (Object constraint : constraints.keySet())
            if (!table.constraints.containsKey(constraint))
                System.out.println("Constraint not present : " + constraint);
            else{
                for (Object var : constraints.get(constraint).keySet()) {
                    if (!table.constraints.get(constraint).containsKey(var)){
                        System.out.println("In constraint " + constraint + "; var " + var + " not present.");
                        continue;
                    }
                    if (!constraints.get(constraint).get(var).equals(table.constraints.get(constraint).get(var))){
                        System.out.println("In constraint " + constraint + "; var " + var + " has different value:");
                        System.out.println("Original : " + constraints.get(constraint).get(var));
                        System.out.println("In table : " + table.constraints.get(constraint).get(var));
                    }
                }
            }
    }

    public boolean exists(Object varKey) {
        return variableIndices.containsKey(varKey);
    }
}
