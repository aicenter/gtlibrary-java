package cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.bothplayerslp;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloObjective;
import ilog.concert.IloRange;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPData;

public class RecyclingLPTable extends LPTable {

	protected Map<Object, Double> newObjective;
	protected Map<Object, Map<Object, Double>> newConstraints;
	protected Map<Object, Map<Object, Double>> updatedConstraints;
	protected Set<Object> removedConstraints;

	protected IloObjective lpObj;
	protected IloRange[] lpConstraints;
	protected IloNumVar[] lpVariables;

	public RecyclingLPTable() {
		super();
		newConstraints = new LinkedHashMap<Object, Map<Object, Double>>();
		newObjective = new LinkedHashMap<Object, Double>();
		updatedConstraints = new LinkedHashMap<Object, Map<Object, Double>>();
		removedConstraints = new HashSet<Object>();
	}

	public RecyclingLPTable(int m, int n) {
		super(m, n);
		newConstraints = new LinkedHashMap<Object, Map<Object, Double>>(m);
		newObjective = new LinkedHashMap<Object, Double>(n);
		updatedConstraints = new LinkedHashMap<Object, Map<Object, Double>>();
		removedConstraints = new HashSet<Object>();
	}

	public void setObjective(Object varKey, double value) {
		if (Math.abs(value) < Double.MIN_VALUE)
			return;
		if (objective.put(varKey, value) == null)
			newObjective.put(varKey, value);
		updateVariableIndices(varKey);
	}

	public void setConstraint(Object eqKey, Object varKey, double value) {
		if (Math.abs(value) < Double.MIN_VALUE)
			return;
		Map<Object, Double> row = constraints.get(eqKey);

		if (row == null) {
			row = new LinkedHashMap<Object, Double>();
			constraints.put(eqKey, row);
		}

		if (row.put(varKey, value) == null) {
			Map<Object, Double> newRow = newConstraints.get(eqKey);

			if (newRow == null)
				newRow = new LinkedHashMap<Object, Double>();

			newRow.put(varKey, value);
			newConstraints.put(eqKey, newRow);
//			newConstraints.put(eqKey, row);
		}
		updateEquationIndices(eqKey);
		updateVariableIndices(varKey);
	}

	public LPData toCplex() throws IloException {
		double[] ub = getUpperBounds();
		double[] lb = getLowerBounds();
		String[] variableNames = getVariableNames();

		lpVariables = updateVariables(variableNames, lb, ub);
		lpConstraints = addConstraints(lpVariables);

		addObjective(lpVariables);
		return new LPData(cplex, lpVariables, lpConstraints, getWatchedPrimalVars(lpVariables), getWatchedDualVars(lpConstraints));
	}

	protected IloNumVar[] updateVariables(String[] variableNames, double[] lb, double[] ub) throws IloException {
		if (lpVariables == null)
			return cplex.numVarArray(variableNames.length, lb, ub, variableNames);

		IloNumVar[] newVariables = new IloNumVar[variableNames.length];

		for (int i = 0; i < lpVariables.length; i++) {
			newVariables[i] = lpVariables[i];
		}
		for (int i = lpVariables.length; i < newVariables.length; i++) {
			newVariables[i] = cplex.numVar(lb[i], ub[i], variableNames[i]);
		}
		return newVariables;
	}

	protected IloRange[] addConstraints(IloNumVar[] x) throws IloException {
		IloRange[] cplexConstraints = createConstraintsFromLastIteration();

		for (Entry<Object, Map<Object, Double>> rowEntry : updatedConstraints.entrySet()) {
//			int equationIndex = getEquationIndex(eqKey) - 1;
//
//			cplex.remove(cplexConstraints[equationIndex]);
//			createNewConstraint(x, cplexConstraints, eqKey, constraints.get(eqKey), equationIndex);
			modifyExistingConstraint(x, cplexConstraints, rowEntry, getEquationIndex(rowEntry.getKey()) - 1);
		}
		for (Object eqKey : removedConstraints) {
			int equationIndex = getEquationIndex(eqKey) - 1;

			cplex.remove(cplexConstraints[equationIndex]);
			cplexConstraints[equationIndex] = null;
		}
		updatedConstraints.clear();
		removedConstraints.clear();

		for (Entry<Object, Map<Object, Double>> rowEntry : newConstraints.entrySet()) {
			int equationIndex = getEquationIndex(rowEntry.getKey()) - 1;

			if (cplexConstraints[equationIndex] == null) {
				createNewConstraint(x, cplexConstraints, rowEntry.getKey(), rowEntry.getValue(), equationIndex);
			} else {
//				cplex.remove(cplexConstraints[equationIndex]);
//				createNewConstraint(x, cplexConstraints, rowEntry.getKey(), rowEntry.getValue(), equationIndex);//teï to tady vymažu a nahradim zkusit to ale jenom editací(tzn v setConstr tam enmùžu vkládat celej øádek ale jenom tu zmìnu)
				modifyExistingConstraint(x, cplexConstraints, rowEntry, equationIndex);
			}
		}
		newConstraints.clear();

		return cplexConstraints;
	}

	protected void modifyExistingConstraint(IloNumVar[] x, IloRange[] cplexConstraints, Entry<Object, Map<Object, Double>> rowEntry, int equationIndex) throws IloException {
		cplex.addToExpr(cplexConstraints[equationIndex], createRowExpresion(x, rowEntry.getValue()));
	}

	protected void createNewConstraint(IloNumVar[] x, IloRange[] cplexConstraints, Object key, Map<Object, Double> row, int equationIndex) throws IloException {
		IloLinearNumExpr rowExpr = createRowExpresion(x, row);
		Integer constraintType = getConstraintType(key);

		switch (constraintType) {
		case 0:
			cplexConstraints[equationIndex] = cplex.addLe(rowExpr, getConstant(key));
			break;
		case 1:
			cplexConstraints[equationIndex] = cplex.addEq(rowExpr, getConstant(key));
			break;
		case 2:
			cplexConstraints[equationIndex] = cplex.addGe(rowExpr, getConstant(key));
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
			rowExpr.addTerm(-memberEntry.getValue().doubleValue(), x[getVariableIndex(memberEntry.getKey()) - 1]);
		}
		return rowExpr;
	}

	protected void addObjective(IloNumVar[] x) throws IloException {
		IloLinearNumExpr objExpr = cplex.linearNumExpr();

		for (Entry<Object, Double> entry : newObjective.entrySet()) {
			objExpr.addTerm(entry.getValue(), x[getVariableIndex(entry.getKey()) - 1]);
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
				constraints.remove(eqKey);
				removedConstraints.add(eqKey);
			} else {
				Map<Object, Double> updatedRow = updatedConstraints.get(eqKey);

				if (updatedRow == null)
					updatedRow = new LinkedHashMap<Object, Double>();
				updatedRow.put(varKey, -removedValue);
				updatedConstraints.put(eqKey, updatedRow);
			}
		}

	}
}
