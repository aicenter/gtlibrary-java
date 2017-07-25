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


package cz.agents.gtlibrary.algorithms.sequenceform.refinements.quasiperfect;

import cz.agents.gtlibrary.algorithms.sequenceform.refinements.quasiperfect.lp.LPDictionary;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.quasiperfect.lp.SimplexData;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.quasiperfect.numbers.EpsilonReal;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.quasiperfect.numbers.Real;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPData;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class EpsilonLPTable {

    protected Map<Object, EpsilonReal> objective;
    protected Map<Object, Map<Object, EpsilonReal>> constraints;
    protected Map<Object, EpsilonReal> constants;

    protected Map<Object, Integer> equationIndices;
    protected Map<Object, Integer> variableIndices;
    protected Map<Object, Integer> primalWatch;
    protected Map<Object, Integer> dualWatch;

    protected Map<Object, Integer> constraintTypes;
    protected Map<Object, Double> lb;
    protected Map<Object, Double> ub;

    public EpsilonLPTable() {
        constants = new LinkedHashMap<Object, EpsilonReal>();
        constraints = new LinkedHashMap<Object, Map<Object, EpsilonReal>>();
        objective = new LinkedHashMap<Object, EpsilonReal>();

        equationIndices = new LinkedHashMap<Object, Integer>();
        variableIndices = new LinkedHashMap<Object, Integer>();
        primalWatch = new LinkedHashMap<Object, Integer>();
        dualWatch = new LinkedHashMap<Object, Integer>();

        constraintTypes = new LinkedHashMap<Object, Integer>();
        lb = new LinkedHashMap<Object, Double>();
        ub = new LinkedHashMap<Object, Double>();
    }

    public EpsilonLPTable(int m, int n) {
        constants = new LinkedHashMap<Object, EpsilonReal>(m);
        constraints = new LinkedHashMap<Object, Map<Object, EpsilonReal>>(m);
        objective = new LinkedHashMap<Object, EpsilonReal>(n);

        equationIndices = new LinkedHashMap<Object, Integer>(m);
        variableIndices = new LinkedHashMap<Object, Integer>(n);
        primalWatch = new LinkedHashMap<Object, Integer>();
        dualWatch = new LinkedHashMap<Object, Integer>();

        constraintTypes = new LinkedHashMap<Object, Integer>(m);
        lb = new LinkedHashMap<Object, Double>(n);
        ub = new LinkedHashMap<Object, Double>();
    }

    public EpsilonReal get(Object eqKey, Object varKey) {
        EpsilonReal value = constraints.get(eqKey).get(varKey);

        return value == null ? EpsilonReal.ZERO : value;
    }

    protected void updateEquationIndices(Object eqKey) {
        getEqIndex(eqKey);
    }

    protected void updateVariableIndices(Object varKey) {
        getVarIndex(varKey);
    }

    public void setObjective(Object varKey, EpsilonReal value) {
        objective.put(varKey, value);
        updateVariableIndices(varKey);
    }

    public EpsilonReal getObjective(Object varKey) {
        EpsilonReal value = objective.get(varKey);

        return value == null ? EpsilonReal.ZERO : value;
    }

    public void setConstant(Object eqKey, EpsilonReal value) {
        constants.put(eqKey, value);
        updateEquationIndices(eqKey);
    }

    public EpsilonReal getConstant(Object eqKey) {
        EpsilonReal value = constants.get(eqKey);

        return value == null ? EpsilonReal.ZERO : value;
    }

    public void setConstraint(Object eqKey, Object varKey, EpsilonReal value) {
        Map<Object, EpsilonReal> row = constraints.get(eqKey);

        if (row == null) {
            row = new LinkedHashMap<Object, EpsilonReal>();
            constraints.put(eqKey, row);
        }
        row.put(varKey, value);
        updateEquationIndices(eqKey);
        updateVariableIndices(varKey);
    }

    public void addToConstraint(Object eqKey, Object varKey, EpsilonReal value) {
        setConstraint(eqKey, varKey, get(eqKey, varKey).add(value));
    }

    public void substractFromConstraint(Object eqKey, Object varKey, EpsilonReal value) {
        setConstraint(eqKey, varKey, get(eqKey, varKey).subtract(value));
    }

    public int rowCount() {
        return constraints.size();
    }

    public int columnCount() {
        return variableIndices.size();
    }

    protected int getEqIndex(Object eqKey) {
        return getIndex(eqKey, equationIndices);
    }

    protected int getVarIndex(Object varKey) {
        return getIndex(varKey, variableIndices);
    }

    protected int getIndex(Object key, Map<Object, Integer> map) {
        Integer result = map.get(key);

        if (result == null) {
            result = map.size();
            map.put(key, result);
        }
        return result + 1;
    }

    public void watchPrimalVariable(Object varKey, Object watchKey) {
        primalWatch.put(watchKey, getVarIndex(varKey) - 1);
    }

    public void watchDualVariable(Object eqKey, Object watchKey) {
        dualWatch.put(watchKey, getEqIndex(eqKey) - 1);
    }

    public LPData toCplex() throws IloException {
        double[] ub = getUpperBounds();
        double[] lb = getLowerBounds();
        IloCplex cplex = new IloCplex();
        String[] variableNames = getVariableNames();
        IloNumVar[] variables = cplex.numVarArray(variableNames.length, lb, ub, variableNames);
        IloRange[] constraints = addConstraints(cplex, variables);

        addObjective(cplex, variables);
        return new LPData(cplex, variables, constraints, new HashMap<Object, IloRange>(), getWatchedPrimalVars(variables), getWatchedDualVars(constraints));
    }

    public SimplexData toPeturbedSimplex() {
        EpsilonReal[][] lpTable = initRealTable();

        addObjective(lpTable);
        addConstraints(lpTable);
        return new SimplexData(new LPDictionary<EpsilonReal>(lpTable), primalWatch, dualWatch);
    }

    private EpsilonReal[][] initRealTable() {
        EpsilonReal[][] lpTable = new EpsilonReal[equationIndices.size() + 1][variableIndices.size() + 1];

        for (EpsilonReal[] epsilonReals : lpTable) {
            Arrays.fill(epsilonReals, EpsilonReal.ZERO);
        }
        return lpTable;
    }

    private void addConstraints(Real[][] lpTable) {
        for (Entry<Object, Map<Object, EpsilonReal>> constEntry : constraints.entrySet()) {
            for (Entry<Object, EpsilonReal> varEntry : constEntry.getValue().entrySet()) {
                addEntryToTable(lpTable, getEqIndex(constEntry.getKey()), getVarIndex(varEntry.getKey()), varEntry.getValue().negate());
            }
        }
        addConstants(lpTable);
    }

    private void addConstants(Real[][] lpTable) {
        for (Entry<Object, EpsilonReal> constantEntry : constants.entrySet()) {
            addEntryToTable(lpTable, getEqIndex(constantEntry.getKey()), 0, constantEntry.getValue());
        }
    }

    private void addObjective(Real[][] lpTable) {
        for (Entry<Object, EpsilonReal> entry : objective.entrySet()) {
            addEntryToTable(lpTable, 0, getVarIndex(entry.getKey()), entry.getValue());
        }
    }

    private void addEntryToTable(Real[][] lpTable, int eqIndex, int varIndex, EpsilonReal value) {
        lpTable[eqIndex][varIndex] = value;
    }

    protected String[] getVariableNames() {
        String[] variableNames = new String[columnCount()];

        for (Entry<Object, Integer> entry : variableIndices.entrySet()) {
            variableNames[entry.getValue()] = entry.getKey().toString();
        }
        return variableNames;
    }

    protected double[] getLowerBounds() {
        double[] lb = new double[columnCount()];

        for (Entry<Object, Double> entry : this.lb.entrySet()) {
            lb[getVarIndex(entry.getKey()) - 1] = entry.getValue();
        }
        return lb;
    }

    protected double[] getUpperBounds() {
        double[] ub = new double[columnCount()];

        for (int i = 0; i < columnCount(); i++) {
            ub[i] = Double.POSITIVE_INFINITY;
        }
        for (Entry<Object, Double> entry : this.ub.entrySet()) {
            ub[getVarIndex(entry.getKey()) - 1] = entry.getValue();
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

    protected IloRange[] addConstraints(IloCplex cplex, IloNumVar[] x) throws IloException {
        IloRange[] cplexConstraints = new IloRange[rowCount()];

        for (Entry<Object, Map<Object, EpsilonReal>> rowEntry : constraints.entrySet()) {
            IloLinearNumExpr rowExpr = createRowExpresion(cplex, x, rowEntry);
            Integer constraintType = getConstraintType(rowEntry);
            int equationIndex = getEqIndex(rowEntry.getKey()) - 1;

            switch (constraintType) {
                case 0:
                    cplexConstraints[equationIndex] = cplex.addLe(rowExpr, getConstant(rowEntry.getKey()).doubleValue());
                    break;
                case 1:
                    cplexConstraints[equationIndex] = cplex.addEq(rowExpr, getConstant(rowEntry.getKey()).doubleValue());
                    break;
                case 2:
                    cplexConstraints[equationIndex] = cplex.addGe(rowExpr, getConstant(rowEntry.getKey()).doubleValue());
                    break;
                default:
                    break;
            }
        }
        return cplexConstraints;
    }

    private Integer getConstraintType(Entry<Object, Map<Object, EpsilonReal>> rowEntry) {
        Integer constraintType = constraintTypes.get(rowEntry.getKey());

        return constraintType == null ? 0 : constraintType;
    }

    private IloLinearNumExpr createRowExpresion(IloCplex cplex, IloNumVar[] x, Entry<Object, Map<Object, EpsilonReal>> rowEntry) throws IloException {
        IloLinearNumExpr rowExpr = cplex.linearNumExpr();

        for (Entry<Object, EpsilonReal> memberEntry : rowEntry.getValue().entrySet()) {
            rowExpr.addTerm(memberEntry.getValue().doubleValue(), x[getVarIndex(memberEntry.getKey()) - 1]);
        }
        return rowExpr;
    }

    protected void addObjective(IloCplex cplex, IloNumVar[] x) throws IloException {
        double[] objCoef = new double[x.length];

        for (Entry<Object, EpsilonReal> entry : objective.entrySet()) {
            objCoef[variableIndices.get(entry.getKey())] = entry.getValue().doubleValue();
        }
        cplex.addMaximize(cplex.scalProd(x, objCoef));
    }

    /**
     * Set constraint for equation represented by eqKey, default constraint is ge
     *
     * @param eqKey
     * @param type  0 ... le, 1 .. eq, 2 ... ge
     */
    public void setConstraintType(Object eqKey, int type) {
        constraintTypes.put(eqKey, type);
    }

    /**
     * Set lower bound for variable represented by varKey, default reward is 0
     *
     * @param varKey
     * @param value
     */
    public void setLowerBound(Object varKey, double value) {
        lb.put(varKey, value);
    }

    /**
     * Set upper bound for variable represented by varKey, default reward is POSITIVE_INFINITY
     *
     * @param varKey
     * @param value
     */
    public void setUpperBound(Object varKey, double value) {
        ub.put(varKey, value);
    }

    public void clearTable() {
        constants = new LinkedHashMap<Object, EpsilonReal>();
        constraints = new LinkedHashMap<Object, Map<Object, EpsilonReal>>();
        objective = new LinkedHashMap<Object, EpsilonReal>();

        equationIndices = new LinkedHashMap<Object, Integer>();
        variableIndices = new LinkedHashMap<Object, Integer>();
        primalWatch = new LinkedHashMap<Object, Integer>();
        dualWatch = new LinkedHashMap<Object, Integer>();

        constraintTypes = new LinkedHashMap<Object, Integer>();
        lb = new LinkedHashMap<Object, Double>();
        ub = new LinkedHashMap<Object, Double>();
    }
}
