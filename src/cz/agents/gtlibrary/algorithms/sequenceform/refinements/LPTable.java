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

	protected Number[][] table;
	protected Map<Key, Integer> equationIndices;
	protected Map<Key, Integer> variableIndices;
	protected Map<Object, Integer> primalWatch;
	protected Map<Object, Integer> dualWatch;
	protected byte[] constraintTypes;
	protected double[] lb;

	public LPTable(int m, int n) {
		table = new Number[m][n];
		equationIndices = new LinkedHashMap<Key, Integer>(m);
		variableIndices = new LinkedHashMap<Key, Integer>(n);
		primalWatch = new LinkedHashMap<Object, Integer>();
		dualWatch = new LinkedHashMap<Object, Integer>();
		constraintTypes = new byte[m - 1];
		lb = new double[n - 1];
	}

	public double get(int i, int j) {
		return table[i][j] == null ? 0 : table[i][j].doubleValue();
	}

	public double get(Key eqKey, Key varKey) {
		return get(getEquationIndex(eqKey), getVariableIndex(varKey));
	}

	public void set(int i, int j, Number value) {
		table[i][j] = value;
	}

	public void setObjective(Key varKey, Number value) {
		table[0][getVariableIndex(varKey)] = value;
	}

	public void setConstant(Key eqKey, Number value) {
		table[getEquationIndex(eqKey)][0] = value;
	}

	public void set(Key eqKey, Key varKey, Number value) {
		table[getEquationIndex(eqKey)][getVariableIndex(varKey)] = value;
	}

	public void add(int i, int j, Number value) {
		table[i][j] = get(i, j) + value.doubleValue();
	}

	public void add(Key eqKey, Key varKey, Number value) {
		int equationIndex = getEquationIndex(eqKey);
		int variableIndex = getVariableIndex(varKey);

		table[equationIndex][variableIndex] = get(equationIndex, variableIndex) + value.doubleValue();
	}

	public void substract(int i, int j, Number value) {
		table[i][j] = get(i, j) - value.doubleValue();
	}

	public void substract(Key eqKey, Key varKey, Number value) {
		substract(getEquationIndex(eqKey), getVariableIndex(varKey), value);
	}

	public int rowCount() {
		return table.length;
	}

	public int columnCount() {
		return table[0].length;
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
		IloRange[] constraints = new IloRange[rowCount() - 1];

		for (int i = 1; i < rowCount(); i++) {
			IloLinearNumExpr constraint = cplex.linearNumExpr();

			for (int j = 0; j < x.length; j++) {
				constraint.addTerm(x[j], -get(i, j + 1));
			}
			switch (constraintTypes[i - 1]) {
			case 0:
				constraints[i - 1] = cplex.addLe(constraint, get(i, 0));
				break;
			case 1:
				constraints[i - 1] = cplex.addEq(constraint, get(i, 0));
				break;
			case 2:
				constraints[i - 1] = cplex.addGe(constraint, get(i, 0));
				break;
			default:
				break;
			}
		}
		return constraints;
	}

	protected void addObjective(IloCplex cplex, IloNumVar[] x) throws IloException {
		IloLinearNumExpr objective = cplex.linearNumExpr();

		for (int i = 0; i < x.length; i++) {
			objective.addTerm(x[i], get(0, i + 1));
		}
		cplex.addMaximize(objective);
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
	 * @param eqKey
	 * @param type
	 *            0 ... ge, 1 .. eq, 2 ... le
	 */
	public void setLowerBound(Key varKey, double value) {
		lb[getVariableIndex(varKey) - 1] = value;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		for (int i = 0; i < table.length; i++) {
			for (int j = 0; j < table[0].length; j++) {
				builder.append(get(i, j));
				builder.append(" ");
			}
			builder.append("\n");
		}
		return builder.toString();
	}

}
