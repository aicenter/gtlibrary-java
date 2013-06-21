package cz.agents.gtlibrary.algorithms.sequenceform.refinements;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class FastLPTable {
	
	public int CPLEXALG = IloCplex.Algorithm.Barrier;
	public int CPLEXTHREADS = 0; // change to 0 to have no restrictions

	protected Map<Object, Double> objective;
	protected Map<Object, Map<Object, Double>> constraints;
	protected Map<Object, Double> constants;

	protected Map<Object, Integer> equationIndices;
	protected Map<Object, Integer> variableIndices;
	protected Map<Object, Integer> primalWatch;
	protected Map<Object, Integer> dualWatch;

	protected Map<Object, Integer> constraintTypes;
	protected Map<Object, Double> lb;
	protected Map<Object, Double> ub;

	protected IloCplex cplex;

	public FastLPTable() {
		constants = new LinkedHashMap<Object, Double>();
		constraints = new LinkedHashMap<Object, Map<Object, Double>>();
		objective = new LinkedHashMap<Object, Double>();

		equationIndices = new LinkedHashMap<Object, Integer>();
		variableIndices = new LinkedHashMap<Object, Integer>();
		primalWatch = new LinkedHashMap<Object, Integer>();
		dualWatch = new LinkedHashMap<Object, Integer>();

		constraintTypes = new LinkedHashMap<Object, Integer>();
		lb = new LinkedHashMap<Object, Double>();
		ub = new LinkedHashMap<Object, Double>();
		try {
			cplex = new IloCplex();
		} catch (IloException e) {
			e.printStackTrace();
		}
	}

	public FastLPTable(int m, int n) {
		constants = new LinkedHashMap<Object, Double>(m);
		constraints = new LinkedHashMap<Object, Map<Object, Double>>(m);
		objective = new LinkedHashMap<Object, Double>(n);

		equationIndices = new LinkedHashMap<Object, Integer>(m);
		variableIndices = new LinkedHashMap<Object, Integer>(n);
		primalWatch = new LinkedHashMap<Object, Integer>();
		dualWatch = new LinkedHashMap<Object, Integer>();

		constraintTypes = new LinkedHashMap<Object, Integer>(m);
		lb = new LinkedHashMap<Object, Double>(n);
		ub = new LinkedHashMap<Object, Double>();
	}

	public double get(Object eqKey, Object varKey) {
		Double value = constraints.get(eqKey).get(varKey);

		return value == null ? 0 : value;
	}

	private void updateEquationIndices(Object eqKey) {
		getEquationIndex(eqKey);
	}

	private void updateVariableIndices(Object varKey) {
		getVariableIndex(varKey);
	}

	public void setObjective(Object varKey, double value) {
		objective.put(varKey, value);
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
		if (Math.abs(value) < Double.MIN_VALUE)
			return;
		Map<Object, Double> row = constraints.get(eqKey);

		if (row == null) {
			row = new LinkedHashMap<Object, Double>();
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
		return constraints.size();
	}

	public int columnCount() {
		return variableIndices.size();
	}

	protected int getEquationIndex(Object eqKey) {
		return getIndex(eqKey, equationIndices);
	}

	protected int getVariableIndex(Object varKey) {
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
		primalWatch.put(watchKey, getVariableIndex(varKey) - 1);
	}

	public void watchDualVariable(Object eqKey, Object watchKey) {
		dualWatch.put(watchKey, getEquationIndex(eqKey) - 1);
	}

	public LPData toCplex() throws IloException {
		cplex.clearModel();
		cplex.setParam(IloCplex.IntParam.RootAlg, CPLEXALG);
		cplex.setParam(IloCplex.IntParam.Threads, CPLEXTHREADS);
		cplex.setOut(null);
		
		double[] ub = getUpperBounds();
		double[] lb = getLowerBounds();
		IloCplex cplex = new IloCplex();
		String[] variableNames = getVariableNames();
		IloNumVar[] variables = cplex.numVarArray(variableNames.length, lb, ub, variableNames);
		IloRange[] constraints = addConstraints(cplex, variables);

		addObjective(cplex, variables);
		return new LPData(cplex, variables, constraints, getWatchedPrimalVars(variables), getWatchedDualVars(constraints));
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
			lb[getVariableIndex(entry.getKey()) - 1] = entry.getValue();
		}
		return lb;
	}

	private double[] getUpperBounds() {
		double[] ub = new double[columnCount()];

		for (int i = 0; i < columnCount(); i++) {
			ub[i] = Double.POSITIVE_INFINITY;
		}
		for (Entry<Object, Double> entry : this.ub.entrySet()) {
			ub[getVariableIndex(entry.getKey()) - 1] = entry.getValue();
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

		for (Entry<Object, Map<Object, Double>> rowEntry : constraints.entrySet()) {
			IloLinearNumExpr rowExpr = createRowExpresion(cplex, x, rowEntry);
			Integer constraintType = getConstraintType(rowEntry);
			int equationIndex = getEquationIndex(rowEntry.getKey()) - 1;

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

	private int getConstraintType(Entry<Object, Map<Object, Double>> rowEntry) {
		Integer constraintType = constraintTypes.get(rowEntry.getKey());

		return constraintType == null ? 0 : constraintType;
	}

	private IloLinearNumExpr createRowExpresion(IloCplex cplex, IloNumVar[] x, Entry<Object, Map<Object, Double>> rowEntry) throws IloException {
		IloLinearNumExpr rowExpr = cplex.linearNumExpr();

		for (Entry<Object, Double> memberEntry : rowEntry.getValue().entrySet()) {
			rowExpr.addTerm(-memberEntry.getValue().doubleValue(), x[getVariableIndex(memberEntry.getKey()) - 1]);
		}
		return rowExpr;
	}

	protected void addObjective(IloCplex cplex, IloNumVar[] x) throws IloException {
		double[] objCoef = new double[x.length];

		for (Entry<Object, Double> entry : objective.entrySet()) {
			objCoef[variableIndices.get(entry.getKey())] = entry.getValue().doubleValue();
		}
		cplex.addMaximize(cplex.scalProd(x, objCoef));
	}

	/**
	 * Set constraint for equation represented by eqObject, default constraint is ge
	 * 
	 * @param eqKey
	 * @param type
	 *            0 ... le, 1 .. eq, 2 ... ge
	 */
	public void setConstraintType(Object eqObject, int type) {
		constraintTypes.put(eqObject, type);
	}

	/**
	 * Set lower bound for variable represented by varObject, default value is 0
	 * 
	 * @param varKey
	 * @param value
	 */
	public void setLowerBound(Object varObject, double value) {
		lb.put(varObject, value);
	}

	/**
	 * Set upper bound for variable represented by varObject, default value is POSITIVE_INFINITY
	 * 
	 * @param varKey
	 * @param value
	 */
	public void setUpperBound(Object varObject, double value) {
		ub.put(varObject, value);
	}

	public void clearTable() {
		try {
			cplex.clearModel();
			cplex.setParam(IloCplex.IntParam.RootAlg, CPLEXALG);
			cplex.setParam(IloCplex.IntParam.Threads, CPLEXTHREADS);
		} catch (IloException e) {
			e.printStackTrace();
		}
		cplex.setOut(null);
		constants = new LinkedHashMap<Object, Double>();
		constraints = new LinkedHashMap<Object, Map<Object, Double>>();
		objective = new LinkedHashMap<Object, Double>();

		equationIndices = new LinkedHashMap<Object, Integer>();
		variableIndices = new LinkedHashMap<Object, Integer>();
		primalWatch = new LinkedHashMap<Object, Integer>();
		dualWatch = new LinkedHashMap<Object, Integer>();

		constraintTypes = new LinkedHashMap<Object, Integer>();
		lb = new LinkedHashMap<Object, Double>();
		ub = new LinkedHashMap<Object, Double>();
	}

	public void clearConstraint(Object eqKey, Object varKey) {
		Map<Object, Double> row = constraints.get(eqKey);
		
		if(row != null)
			row.remove(varKey);
	}
}
