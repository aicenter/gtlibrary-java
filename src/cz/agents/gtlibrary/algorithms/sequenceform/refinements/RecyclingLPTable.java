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

import cz.agents.gtlibrary.utils.DummyMap;
import ilog.concert.*;
import ilog.cplex.IloCplex;

import java.util.*;
import java.util.Map.Entry;

public class RecyclingLPTable extends LPTable {

    protected static final boolean USE_VAR_BACKUP = true;
    protected Map<Object, Double> newObjective;
    protected Map<Object, Map<Object, Double>> newConstraints;
    protected Map<Object, Map<Object, Double>> updatedConstraints;
    protected Set<Object> removedConstraints;
    protected Map<Object, Double> updatedConstants;

    protected Map<Object, IloNumVar> variableBackup;

    protected IloObjective lpObj;
    protected IloRange[] lpConstraints;
    protected IloNumVar[] lpVariables;

    public RecyclingLPTable() {
        super();
        newConstraints = new LinkedHashMap<>();
        newObjective = new LinkedHashMap<>();
        updatedConstraints = new LinkedHashMap<>();
        removedConstraints = new HashSet<>();
        updatedConstants = new HashMap<>();
        variableBackup = USE_VAR_BACKUP ? new HashMap<>() : new DummyMap<>();
    }

    public RecyclingLPTable(int m, int n) {
        super(m, n);
        newConstraints = new LinkedHashMap<>(m);
        newObjective = new LinkedHashMap<>(n);
        updatedConstraints = new LinkedHashMap<>();
        removedConstraints = new HashSet<>();
        updatedConstants = new HashMap<>();
        variableBackup = USE_VAR_BACKUP ? new HashMap<>() : new DummyMap<>();
    }

    public void setObjective(Object varKey, double value) {
        if (Math.abs(value) < Double.MIN_VALUE)
            return;
        if (objective.put(varKey, value) == null)
            newObjective.put(varKey, value);
        updateVariableIndices(varKey);
    }

//    public void setConstraint(Object eqKey, Object varKey, double reward) {
//        if (Math.abs(reward) < Double.MIN_VALUE)
//            return;
//        Map<Object, Double> row = constraints.get(eqKey);
//
//        if (row == null) {
//            row = new LinkedHashMap<Object, Double>();
//            constraints.put(eqKey, row);
//            newConstraints.put(eqKey, row);
//            row.put(varKey, reward);
//        } else {
//            if (newConstraints.containsKey(eqKey)) {
//                row.put(varKey, reward);
//            } else {
//                Map<Object, Double> rowDiff = new LinkedHashMap<Object, Double>();
//
//                if (row.containsKey(varKey)) {
//                    if (Math.abs(reward - row.get(varKey)) < 1e-10)
//                        return;
//                    rowDiff.put(varKey, reward - row.get(varKey));
//                } else {
//                    rowDiff.put(varKey, reward);
//                }
//                updatedConstraints.put(eqKey, rowDiff);
//                row.put(varKey, reward);
//            }
//        }
//
//        updateEquationIndices(eqKey);
//        updateVariableIndices(varKey);
//    }

    public void setConstraint(Object eqKey, Object varKey, double value) {
//        if (Math.abs(reward) < Double.MIN_VALUE)
//            return;
        Map<Object, Double> row = constraints.get(eqKey);

        if (row == null) {
            row = new LinkedHashMap<>();
            constraints.put(eqKey, row);
            newConstraints.put(eqKey, row);
            row.put(varKey, value);
        } else {
            if (newConstraints.containsKey(eqKey)) {
                row.put(varKey, value);
            } else {
                if (row.containsKey(varKey)) {
                    if (Math.abs(value - row.get(varKey)) < 1e-10)
                        return;
                }
                Map<Object, Double> updatedRow = updatedConstraints.get(eqKey);

                if (updatedRow == null)
                    updatedRow = new HashMap<>();
                updatedRow.put(varKey, value);
                updatedConstraints.put(eqKey, updatedRow);
                row.put(varKey, value);
            }
        }

        updateEquationIndices(eqKey);
        updateVariableIndices(varKey);
    }

    public void setConstraintIfNotPresent(Object eqKey, Object varKey, double value) {
        if (constraints.get(eqKey).containsKey(varKey))
            return;
        setConstraint(eqKey, varKey, value);

    }

    @Override
    public void setConstant(Object eqKey, double value) {
        updatedConstants.put(eqKey, value);
    }

    public LPData toCplex() throws IloException {
        double[] ub = getUpperBounds();
        double[] lb = getLowerBounds();
        Object[] keys = getKeys();

        cplex.setParam(IloCplex.IntParam.RootAlg, CPLEXALG);
        cplex.setParam(IloCplex.IntParam.Threads, CPLEXTHREADS);
        cplex.setOut(null);
        if (USE_CUSTOM_NAMES) {
            String[] variableNames = getVariableNames();


//        cplex.setParam(IloCplex.BooleanParam.Parallel, IloCplex.ParallelMode.Opportunistic);
//        cplex.setParam(IloCplex.DoubleParam.EpMrk, 0.99999);
            //		cplex.setParam(IloCplex.DoubleParam.BarEpComp, 1e-4);
            //		System.out.println("BarEpComp: " + cplex.getParam(IloCplex.DoubleParam.BarEpComp));
//        cplex.setParam(IloCplex.BooleanParam.NumericalEmphasis, true);

            lpVariables = updateVariables(keys, variableNames, lb, ub);
        } else {
            lpVariables = updateVariables(keys, lb, ub);
        }
        lpConstraints = addConstraints(lpVariables);

        addObjective(lpVariables);
        return new LPData(cplex, lpVariables, lpConstraints, getRelaxableConstraints(lpConstraints), getWatchedPrimalVars(lpVariables), getWatchedDualVars(lpConstraints));
    }

    protected IloNumVar[] updateVariables(Object[] keys, String[] variableNames, double[] lb, double[] ub) throws IloException {
        if (lpVariables == null)
            lpVariables = new IloNumVar[0];

        IloNumVar[] newVariables = new IloNumVar[variableNames.length];

        for (int i = 0; i < lpVariables.length; i++) {
            newVariables[i] = lpVariables[i];
        }
        for (int i = lpVariables.length; i < newVariables.length; i++) {
            IloNumVar var = variableBackup.getOrDefault(keys[i], cplex.numVar(lb[i], ub[i], variableNames[i]));

            variableBackup.putIfAbsent(keys[i], var);
            newVariables[i] = var;
        }
        return newVariables;
    }

    protected IloNumVar[] updateVariables(Object[] keys, double[] lb, double[] ub) throws IloException {
        if (lpVariables == null)
            lpVariables = new IloNumVar[0];

        IloNumVar[] newVariables = new IloNumVar[lb.length];

        for (int i = 0; i < lpVariables.length; i++) {
            newVariables[i] = lpVariables[i];
        }
        for (int i = lpVariables.length; i < newVariables.length; i++) {
            IloNumVar var = variableBackup.getOrDefault(keys[i], cplex.numVar(lb[i], ub[i]));

            variableBackup.putIfAbsent(keys[i], var);
            newVariables[i] = var;
        }
        return newVariables;
    }

    protected IloRange[] addConstraints(IloNumVar[] x) throws IloException {
        IloRange[] cplexConstraints = createConstraintsFromLastIteration();

        for (Entry<Object, Map<Object, Double>> rowEntry : updatedConstraints.entrySet()) {
            assert rowEntry.getValue().get("t") == null || Math.abs(rowEntry.getValue().get("t")) == 1 || Math.abs(rowEntry.getValue().get("t")) == 0;
            modifyExistingConstraint(x, cplexConstraints, rowEntry, getEquationIndex(rowEntry.getKey()));
        }
        for (Object eqKey : removedConstraints) {
            int equationIndex = getEquationIndex(eqKey);

            cplex.remove(cplexConstraints[equationIndex]);
            cplexConstraints[equationIndex] = null;
        }
        updatedConstraints.clear();
        removedConstraints.clear();

        for (Entry<Object, Map<Object, Double>> rowEntry : newConstraints.entrySet()) {
            int equationIndex = getEquationIndex(rowEntry.getKey());

            if (cplexConstraints[equationIndex] == null) {
                assert rowEntry.getValue().get("t") == null || rowEntry.getValue().get("t") == 1;
                createNewConstraint(x, cplexConstraints, rowEntry.getKey(), rowEntry.getValue(), equationIndex);
            } else {
                //				modifyExistingConstraint(x, cplexConstraints, rowEntry, equationIndex);
//                assert false;
            }
        }
        updateConstants(cplexConstraints);
        updatedConstants.clear();
        newConstraints.clear();

        return cplexConstraints;
    }

    private void updateConstants(IloRange[] cplexConstraints) throws IloException {
        for (Entry<Object, Double> entry : updatedConstants.entrySet()) {
            updateConstant(cplexConstraints, entry.getKey(), entry.getValue());
        }
    }

    private void updateConstant(IloRange[] cplexConstraints, Object eqKey, Double constant) throws IloException {
        if (constraintTypes.get(eqKey) == 0)
            cplexConstraints[getEquationIndex(eqKey)].setUB(constant);
        else if (constraintTypes.get(eqKey) == 2)
            cplexConstraints[getEquationIndex(eqKey)].setLB(constant);
        else
            cplexConstraints[getEquationIndex(eqKey)].setBounds(constant, constant);
    }

    private void modifyExistingConstraint(IloNumVar[] x, IloRange[] cplexConstraints, Entry<Object, Map<Object, Double>> rowEntry, int equationIndex) throws IloException {
        for (Entry<Object, Double> update : rowEntry.getValue().entrySet()) {
            cplex.setLinearCoef(cplexConstraints[equationIndex], x[getVariableIndex(update.getKey())], update.getValue());
        }
    }

    protected void createNewConstraint(IloNumVar[] x, IloRange[] cplexConstraints, Object key, Map<Object, Double> row, int equationIndex) throws IloException {
        IloLinearNumExpr rowExpr = createRowExpresion(x, row);
        Integer constraintType = getConstraintType(key);

        switch (constraintType) {
            case 0:
                cplexConstraints[equationIndex] = cplex.addLe(rowExpr, 0);
                break;
            case 1:
                cplexConstraints[equationIndex] = cplex.addEq(rowExpr, 0);
                break;
            case 2:
                cplexConstraints[equationIndex] = cplex.addGe(rowExpr, 0);
                break;
            default:
                break;
        }
    }

    protected IloRange[] createConstraintsFromLastIteration() {
        IloRange[] cplexConstraints = new IloRange[rowCount()];

        if (lpConstraints != null)
            for (int i = 0; i < lpConstraints.length; i++) {
                cplexConstraints[i] = lpConstraints[i];
            }
        return cplexConstraints;
    }

    protected int getConstraintType(Object eqKey) {
        Integer constraintType = constraintTypes.get(eqKey);

        return constraintType == null ? 0 : constraintType;
    }

    protected IloLinearNumExpr createRowExpresion(IloNumVar[] x, Map<Object, Double> row) throws IloException {
        IloLinearNumExpr rowExpr = cplex.linearNumExpr();

        for (Entry<Object, Double> memberEntry : row.entrySet()) {
            rowExpr.addTerm(memberEntry.getValue().doubleValue(), x[getVariableIndex(memberEntry.getKey())]);
        }
        return rowExpr;
    }

    protected void addObjective(IloNumVar[] x) throws IloException {
        IloLinearNumExpr objExpr = cplex.linearNumExpr();

        for (Entry<Object, Double> entry : newObjective.entrySet()) {
            objExpr.addTerm(entry.getValue(), x[getVariableIndex(entry.getKey())]);
        }
        if (lpObj == null)
            lpObj = cplex.addMaximize(objExpr);
        else
            cplex.addToExpr(lpObj, objExpr);
        newObjective.clear();
    }

    public void removeFromConstraint(Object eqKey, Object varKey) {
        Map<Object, Double> row = constraints.get(eqKey);

        if (row != null) {
            Double removedValue = row.remove(varKey);

            if (removedValue == null)
                return;
            if (row.isEmpty()) {
                deleteConstraint(eqKey);
            } else {
                Map<Object, Double> updatedRow = updatedConstraints.get(eqKey);

                if (updatedRow == null)
                    updatedRow = new LinkedHashMap<>();
                updatedRow.put(varKey, 0d);
                updatedConstraints.put(eqKey, updatedRow);
            }
        }

    }

    public void removeConstant(Object eqKey) {
        constants.remove(eqKey, 0d);
        updatedConstants.remove(eqKey, 0d);
    }

    public void deleteConstraint(Object eqKey) {
        Map<Object, Double> row = constraints.get(eqKey);

        if (row != null) {
            constraints.remove(eqKey);
            removedConstraints.add(eqKey);
            updatedConstraints.remove(eqKey);
        }
    }

    public void deleteConstraints(Set<Object> addedConstrKeys) {
        addedConstrKeys.forEach(this::deleteConstraint);
    }

    @Override
    public void clearTable() {
        newConstraints = new LinkedHashMap<>();
        newObjective = new LinkedHashMap<>();
        updatedConstraints = new LinkedHashMap<>();
        removedConstraints = new HashSet<>();
        updatedConstants = new HashMap<>();
        if (USE_VAR_BACKUP)
            try {
                cplex.remove(lpObj);
            } catch (IloException e) {
                e.printStackTrace();
            }
        lpObj = null;
//        try {
//            if (lpConstraints != null)
//                for (IloConstraint lpConstraint : lpConstraints) {
//                    cplex.delete(lpConstraint);
////                    cplex.end(lpConstraint);
//                }
//            if (lpVariables != null)
//                for (IloNumVar lpVariable : lpVariables) {
//                    cplex.delete(lpVariable);
////                    cplex.end(lpVariable);
//                }
//        } catch (IloException e) {
//            e.printStackTrace();
//        }
        if (USE_VAR_BACKUP)
            if (lpConstraints != null)
                try {
                    for (IloRange lpConstraint : lpConstraints) {
                        cplex.delete(lpConstraint);
                    }
                } catch (IloException e) {
                    e.printStackTrace();
                }
        lpConstraints = null;
        lpVariables = null;
        cplex.setOut(null);
        constants = new LinkedHashMap<>();
        constraints = new LinkedHashMap<>();
        objective = new LinkedHashMap<>();

        if(!USE_VAR_BACKUP) {
            equationIndices = new LinkedHashMap<>();
            variableIndices = new LinkedHashMap<>();
        }
        primalWatch = new LinkedHashMap<>();
        dualWatch = new LinkedHashMap<>();

        constraintTypes = new LinkedHashMap<>();
        lb = new LinkedHashMap<>();
        ub = new LinkedHashMap<>();
        if (!USE_VAR_BACKUP)
            super.clearTable();
    }

    protected Object[] getKeys() {
        Object[] keys = new Object[columnCount()];

        variableIndices.entrySet().forEach(entry -> keys[entry.getValue()] = entry.getKey());
        return keys;
    }

    public void removeObjective() {
        objective = new LinkedHashMap<>();
        try {
            cplex.remove(lpObj);
        } catch (IloException e) {
            e.printStackTrace();
        }
        lpObj = null;
    }
}
