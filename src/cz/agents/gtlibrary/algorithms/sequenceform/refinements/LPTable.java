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

	private Number[][] table;
	private Map<Key, Integer> equationIndices;
	private Map<Key, Integer> variableIndices;
	private Map<Object, Integer> primalWatch;
	private Map<Object, Integer> dualWatch;

	public LPTable(int m, int n) {
		table = new Number[m][n];
		equationIndices = new LinkedHashMap<Key, Integer>(m);
		variableIndices = new LinkedHashMap<Key, Integer>(n);
		primalWatch = new LinkedHashMap<Object, Integer>();
		dualWatch = new LinkedHashMap<Object, Integer>();
	}

	public double get(int i, int j) {
		return table[i][j] == null ? 0 : table[i][j].doubleValue();
	}

	public double get(Key eqKey, Key varKey) {
		return get(getEquationIndex(eqKey), getVariableIndex(varKey));
	}

	public void set(int i, int j, double value) {
		table[i][j] = value;
	}

	public void setObjective(Key varKey, double value) {
		table[0][getVariableIndex(varKey)] = value;
	}

	public void setConstant(Key eqKey, double value) {
		table[getEquationIndex(eqKey)][0] = value;
	}

	public void set(Key eqKey, Key varKey, double value) {
		table[getEquationIndex(eqKey)][getVariableIndex(varKey)] = value;
	}

	public void add(int i, int j, double value) {
		table[i][j] = get(i, j) + value;
	}

	public void add(Key eqKey, Key varKey, double value) {
		int equationIndex = getEquationIndex(eqKey);
		int variableIndex = getVariableIndex(varKey);

		table[equationIndex][variableIndex] = get(equationIndex, variableIndex) + value;
	}

	public void substract(int i, int j, double value) {
		table[i][j] = get(i, j) - value;
	}

	public void substract(Key eqKey, Key varKey, double value) {
		int equationIndex = getEquationIndex(eqKey);
		int variableIndex = getVariableIndex(varKey);

		table[equationIndex][variableIndex] = get(equationIndex, variableIndex) - value;
	}

	public int rowCount() {
		return table.length;
	}

	public int columnCount() {
		return table[0].length;
	}

	private int getEquationIndex(Key eqKey) {
		return getIndex(eqKey, equationIndices);
	}

	private int getVariableIndex(Key varKey) {
		return getIndex(varKey, variableIndices);
	}

	private int getIndex(Key key, Map<Key, Integer> map) {
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
		double[] lb = new double[columnCount() - 1];
		double[] ub = new double[columnCount() - 1];
		String[] variableNames = new String[columnCount() - 1];
		IloCplex cplex = new IloCplex();

		for (int i = 1; i < columnCount(); i++) {
			ub[i - 1] = Double.POSITIVE_INFINITY;
		}
		for (Entry<Key, Integer> entry : variableIndices.entrySet()) {
			if (entry.getValue() != 0)
				variableNames[entry.getValue() - 1] = entry.getKey().toString();
		}
		IloNumVar[] variables = cplex.numVarArray(variableNames.length, lb, ub, variableNames);
		IloRange[] constrains = addConstrains(cplex, variables);

		addObjective(cplex, variables);
		return new LPData(cplex, variables, constrains, getWatchedPrimalVars(variables), getWatchedDualVars(constrains));
	}

	private Map<Object, IloRange> getWatchedDualVars(IloRange[] constrains) {
		Map<Object, IloRange> watchedDualVars = new LinkedHashMap<Object, IloRange>();

		for (Entry<Object, Integer> entry : dualWatch.entrySet()) {
			watchedDualVars.put(entry.getKey(), constrains[entry.getValue()]);
		}
		return watchedDualVars;
	}

	private Map<Object, IloNumVar> getWatchedPrimalVars(IloNumVar[] variables) {
		Map<Object, IloNumVar> watchedPrimalVars = new LinkedHashMap<Object, IloNumVar>();

		for (Entry<Object, Integer> entry : primalWatch.entrySet()) {
			watchedPrimalVars.put(entry.getKey(), variables[entry.getValue()]);
		}
		return watchedPrimalVars;
	}

	public IloRange[] addConstrains(IloCplex cplex, IloNumVar[] x) throws IloException {
		IloRange[] constrains = new IloRange[rowCount() - 1];

		for (int i = 1; i < rowCount(); i++) {
			IloLinearNumExpr constrain = cplex.linearNumExpr();

			for (int j = 0; j < x.length; j++) {
				constrain.addTerm(x[j], get(i, j + 1));
			}
			constrains[i - 1] = cplex.addGe(constrain, -get(i, 0));
		}
		return constrains;
	}

	public void addObjective(IloCplex cplex, IloNumVar[] x) throws IloException {
		IloLinearNumExpr objective = cplex.linearNumExpr();

		for (int i = 0; i < x.length; i++) {
			objective.addTerm(x[i], get(0, i + 1));
		}
		cplex.addMaximize(objective);
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
