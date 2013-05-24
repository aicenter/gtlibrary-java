package cz.agents.gtlibrary.algorithms.sequenceform.refinements;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class LPTable {

	protected Map<Key, Number> objective;
	protected Map<Key, Map<Key, Number>> constraints;
	protected Map<Key, Number> constants;

	protected Map<Key, Integer> equationIndices;
	protected Map<Key, Integer> variableIndices;
	protected Map<Object, Integer> primalWatch;
	protected Map<Object, Integer> dualWatch;

	protected byte[] constraintTypes;
	protected double[] lb;

	protected double maxCoefficient;
	protected int rowCount;
	protected int columnCount;

	public LPTable(int m, int n) {
		constants = new LinkedHashMap<Key, Number>(m);
		constraints = new LinkedHashMap<Key, Map<Key, Number>>(m);
		objective = new LinkedHashMap<Key, Number>(n);

		equationIndices = new LinkedHashMap<Key, Integer>(m);
		variableIndices = new LinkedHashMap<Key, Integer>(n);
		primalWatch = new LinkedHashMap<Object, Integer>();
		dualWatch = new LinkedHashMap<Object, Integer>();

		constraintTypes = new byte[m - 1];
		lb = new double[n - 1];

		maxCoefficient = Double.NEGATIVE_INFINITY;
		rowCount = m;
		columnCount = n;
	}

	public double get(Key eqKey, Key varKey) {
		Number value = constraints.get(eqKey).get(varKey);

		return value == null ? 0 : value.doubleValue();
	}

	public void updateMaxCoefficient(Number value) {
		double absValue = Math.abs(value.doubleValue());

		if (maxCoefficient < absValue)
			maxCoefficient = absValue;
	}
	
	private void updateEquationIndices(Key eqKey) {
		getEquationIndex(eqKey);
	}
	
	private void updateVariableIndices(Key varKey) {
		getVariableIndex(varKey);
	}

	public void setObjective(Key varKey, Number value) {
		objective.put(varKey, value);
		updateMaxCoefficient(value);
		updateVariableIndices(varKey);
	}

	public double getObjective(Key varKey) {
		Number value = objective.get(varKey);

		return value == null ? 0 : value.doubleValue();
	}

	public void setConstant(Key eqKey, Number value) {
		constants.put(eqKey, value);
		updateMaxCoefficient(value);
		updateEquationIndices(eqKey);
	}

	public double getConstant(Key eqKey) {
		Number value = constants.get(eqKey);

		return value == null ? 0 : value.doubleValue();
	}

	public void setConstraint(Key eqKey, Key varKey, Number value) {
		Map<Key, Number> row = constraints.get(eqKey);

		if (row == null) {
			row = new LinkedHashMap<Key, Number>();
			constraints.put(eqKey, row);
		}
		row.put(varKey, value);
		updateMaxCoefficient(value);
		updateEquationIndices(eqKey);
		updateVariableIndices(varKey);
	}

	public void addToConstraint(Key eqKey, Key varKey, Number value) {
		setConstraint(eqKey, varKey, get(eqKey, varKey) + value.doubleValue());
	}

	public void substractFromConstraint(Key eqKey, Key varKey, Number value) {
		setConstraint(eqKey, varKey, get(eqKey, varKey) - value.doubleValue());
	}

	public int rowCount() {
		return rowCount;
	}

	public int columnCount() {
		return columnCount;
	}

	protected int getEquationIndex(Key eqKey) {
		return getIndex(eqKey, equationIndices);
	}

	protected int getVariableIndex(Key varKey) {
		return getIndex(varKey, variableIndices);
	}

	protected int getIndex(Key key, Map<Key, Integer> map) {
		Integer result = map.get(key);

		if (result == null) {
			result = map.size();
			map.put(key, result);
		}
		return result + 1;
	}

	public void watchPrimalVariable(Key varKey, Object watchKey) {
		primalWatch.put(watchKey, getVariableIndex(varKey) - 1);
	}

	public void watchDualVariable(Key eqKey, Object watchKey) {
		dualWatch.put(watchKey, getEquationIndex(eqKey) - 1);
	}

	public LPData toCplex() throws IloException {
		double[] ub = new double[columnCount() - 1];
		String[] variableNames = new String[columnCount() - 1];
		IloCplex cplex = new IloCplex();

		for (int i = 1; i < columnCount(); i++) {
			ub[i - 1] = Double.POSITIVE_INFINITY;
		}

		for (Entry<Key, Integer> entry : variableIndices.entrySet()) {
			variableNames[entry.getValue()] = entry.getKey().toString();
		}
		IloNumVar[] variables = cplex.numVarArray(variableNames.length, lb, ub, variableNames);
		IloRange[] constraints = addConstraints(cplex, variables);

		addObjective(cplex, variables);
		return new LPData(cplex, variables, constraints, getWatchedPrimalVars(variables), getWatchedDualVars(constraints));
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
		IloRange[] cplexConstraints = new IloRange[rowCount() - 1];

		for (Entry<Key, Map<Key, Number>> rowEntry : constraints.entrySet()) {
			IloLinearNumExpr rowExpr = createRowExpresion(cplex, x, rowEntry);
			int equationIndex = getEquationIndex(rowEntry.getKey()) - 1;

			switch (constraintTypes[equationIndex]) {
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

	public IloLinearNumExpr createRowExpresion(IloCplex cplex, IloNumVar[] x, Entry<Key, Map<Key, Number>> rowEntry) throws IloException {
		IloLinearNumExpr rowExpr = cplex.linearNumExpr();

		for (Entry<Key, Number> memberEntry : rowEntry.getValue().entrySet()) {
			rowExpr.addTerm(-memberEntry.getValue().doubleValue(), x[getVariableIndex(memberEntry.getKey()) - 1]);
		}
		return rowExpr;
	}

	protected void addObjective(IloCplex cplex, IloNumVar[] x) throws IloException {
		double[] objCoef = new double[x.length];

		for (Entry<Key, Number> entry : objective.entrySet()) {
			objCoef[variableIndices.get(entry.getKey())] = entry.getValue().doubleValue();
		}
		cplex.addMaximize(cplex.scalProd(x, objCoef));
	}

	/**
	 * Set constraint for equation represented by eqKey, default constraint is ge
	 * 
	 * @param eqKey
	 * @param type
	 *            0 ... le, 1 .. eq, 2 ... ge
	 */
	public void setConstraintType(Key eqKey, int type) {
		constraintTypes[getEquationIndex(eqKey) - 1] = (byte) type;
	}

	/**
	 * Set lower bound for variable represented by varKey, default value is 0
	 * 
	 * @param varKey
	 * @param value
	 */
	public void setLowerBound(Key varKey, double value) {
		lb[getVariableIndex(varKey) - 1] = value;
	}

	public double getMaxCoefficient() {
		return maxCoefficient;
	}
}
