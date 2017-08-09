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

import cz.agents.gtlibrary.algorithms.sequenceform.refinements.quasiperfect.numbers.Rational;
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

    protected Map<Object, Number> objective;
    protected Map<Object, Map<Object, Number>> constraints;
    protected Map<Object, Number> constants;

    protected Map<Object, Integer> equationIndices;
    protected Map<Object, Integer> variableIndices;
    protected Map<Object, Integer> primalWatch;
    protected Map<Object, Integer> dualWatch;

    protected Map<Object, Integer> constraintTypes;
    protected Map<Object, Double> lb;
    protected Map<Object, Double> ub;

    protected double maxCoefficient;

    public EpsilonLPTable() {
        constants = new LinkedHashMap<Object, Number>();
        constraints = new LinkedHashMap<Object, Map<Object, Number>>();
        objective = new LinkedHashMap<Object, Number>();

        equationIndices = new LinkedHashMap<Object, Integer>();
        variableIndices = new LinkedHashMap<Object, Integer>();
        primalWatch = new LinkedHashMap<Object, Integer>();
        dualWatch = new LinkedHashMap<Object, Integer>();

        constraintTypes = new LinkedHashMap<Object, Integer>();
        lb = new LinkedHashMap<Object, Double>();
        ub = new LinkedHashMap<Object, Double>();

        maxCoefficient = Double.NEGATIVE_INFINITY;
    }

    public EpsilonLPTable(int m, int n) {
        constants = new LinkedHashMap<Object, Number>(m);
        constraints = new LinkedHashMap<Object, Map<Object, Number>>(m);
        objective = new LinkedHashMap<Object, Number>(n);

        equationIndices = new LinkedHashMap<Object, Integer>(m);
        variableIndices = new LinkedHashMap<Object, Integer>(n);
        primalWatch = new LinkedHashMap<Object, Integer>();
        dualWatch = new LinkedHashMap<Object, Integer>();

        constraintTypes = new LinkedHashMap<Object, Integer>(m);
        lb = new LinkedHashMap<Object, Double>(n);
        ub = new LinkedHashMap<Object, Double>();

        maxCoefficient = Double.NEGATIVE_INFINITY;
    }

    public double get(Object eqKey, Object varKey) {
        Number value = constraints.get(eqKey).get(varKey);

        return value == null ? 0 : value.doubleValue();
    }

    public void updateMaxCoefficient(Number value) {
        double absValue = Math.abs(value.doubleValue());

        if (maxCoefficient < absValue)
            maxCoefficient = absValue;
    }

    private void updateEquationIndices(Object eqKey) {
        getEqIndex(eqKey);
    }

    private void updateVariableIndices(Object varKey) {
        getVarIndex(varKey);
    }

    public void setObjective(Object varKey, Number value) {
        objective.put(varKey, value);
        updateMaxCoefficient(value);
        updateVariableIndices(varKey);
    }

    public double getObjective(Object varKey) {
        Number value = objective.get(varKey);

        return value == null ? 0 : value.doubleValue();
    }

    public void setConstant(Object eqKey, Number value) {
        if (Math.abs(value.doubleValue()) < Double.MIN_VALUE)
            return;
        constants.put(eqKey, value);
        updateMaxCoefficient(value);
        updateEquationIndices(eqKey);
    }

    public double getConstant(Object eqKey) {
        Number value = constants.get(eqKey);

        return value == null ? 0 : value.doubleValue();
    }

    public void setConstraint(Object eqKey, Object varKey, Number value) {
        if (Math.abs(value.doubleValue()) < Double.MIN_VALUE)
            return;
        Map<Object, Number> row = constraints.get(eqKey);

        if (row == null) {
            row = new LinkedHashMap<Object, Number>();
            constraints.put(eqKey, row);
        }
        row.put(varKey, value);
        updateMaxCoefficient(value);
        updateEquationIndices(eqKey);
        updateVariableIndices(varKey);
    }

    public void addToConstraint(Object eqKey, Object varKey, Number value) {
        setConstraint(eqKey, varKey, get(eqKey, varKey) + value.doubleValue());
    }

    public void substractFromConstraint(Object eqKey, Object varKey, Number value) {
        double dValue = value.doubleValue();

        if (Math.abs(dValue) < Double.MIN_VALUE)
            return;
        setConstraint(eqKey, varKey, get(eqKey, varKey) - dValue);
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

    private Rational[] getPolynomArray(int exponent) {
        Rational[] polynomArray = new Rational[exponent];

        Arrays.fill(polynomArray, Rational.ZERO);
         polynomArray[exponent] = Rational.ONE;
        return polynomArray;
    }

    private String[] getVariableNames() {
        String[] variableNames = new String[columnCount()];

        for (Entry<Object, Integer> entry : variableIndices.entrySet()) {
            variableNames[entry.getValue()] = entry.getKey().toString();
        }
        return variableNames;
    }

    private double[] getLowerBounds() {
        double[] lb = new double[columnCount()];

        for (Entry<Object, Double> entry : this.lb.entrySet()) {
            lb[getVarIndex(entry.getKey()) - 1] = entry.getValue();
        }
        return lb;
    }

    private double[] getUpperBounds() {
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

        for (Entry<Object, Map<Object, Number>> rowEntry : constraints.entrySet()) {
            IloLinearNumExpr rowExpr = createRowExpresion(cplex, x, rowEntry);
            Integer constraintType = getConstraintType(rowEntry);
            int equationIndex = getEqIndex(rowEntry.getKey()) - 1;

            switch (constraintType) {
                case 0:
                    cplexConstraints[equationIndex] = cplex.addLe(rowExpr, getConstant(rowEntry.getKey()));
                    break;
                case 1:
                    cplexConstraints[equationIndex] = cplex.addEq(rowExpr, getConstant(rowEntry.getKey()));
                    break;
                case 2:
                    cplexConstraints[equationIndex] = cplex.addGe(rowExpr, getConstant(rowEntry.getKey()));
                    break;
                default:
                    break;
            }
        }
        return cplexConstraints;
    }

    private Integer getConstraintType(Entry<Object, Map<Object, Number>> rowEntry) {
        Integer constraintType = constraintTypes.get(rowEntry.getKey());

        return constraintType == null ? 0 : constraintType;
    }

    private IloLinearNumExpr createRowExpresion(IloCplex cplex, IloNumVar[] x, Entry<Object, Map<Object, Number>> rowEntry) throws IloException {
        IloLinearNumExpr rowExpr = cplex.linearNumExpr();

        for (Entry<Object, Number> memberEntry : rowEntry.getValue().entrySet()) {
            rowExpr.addTerm(-memberEntry.getValue().doubleValue(), x[getVarIndex(memberEntry.getKey()) - 1]);
        }
        return rowExpr;
    }

    protected void addObjective(IloCplex cplex, IloNumVar[] x) throws IloException {
        double[] objCoef = new double[x.length];

        for (Entry<Object, Number> entry : objective.entrySet()) {
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

    public double getMaxCoefficient() {
        return maxCoefficient;
    }

    public void clearTable() {
        constants = new LinkedHashMap<Object, Number>();
        constraints = new LinkedHashMap<Object, Map<Object, Number>>();
        objective = new LinkedHashMap<Object, Number>();

        equationIndices = new LinkedHashMap<Object, Integer>();
        variableIndices = new LinkedHashMap<Object, Integer>();
        primalWatch = new LinkedHashMap<Object, Integer>();
        dualWatch = new LinkedHashMap<Object, Integer>();

        constraintTypes = new LinkedHashMap<Object, Integer>();
        lb = new LinkedHashMap<Object, Double>();
        ub = new LinkedHashMap<Object, Double>();

        maxCoefficient = Double.NEGATIVE_INFINITY;
    }
}
