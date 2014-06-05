package cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.bothplayerslp;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class ReducedLPTable extends LPTable {
	
	protected double utilityShift;
	protected Map<Object, Set<Object>> utilityMarks;
	
	public ReducedLPTable(double utilityShift) {
		super();
		this.utilityShift = utilityShift;
		utilityMarks = new HashMap<Object, Set<Object>>();
	}
	
	@Override
	public void substractFromConstraint(Object eqKey, Object varKey, double value) {
		super.substractFromConstraint(eqKey, varKey, value);
		addUtilityMark(eqKey, varKey);
	}

	private void addUtilityMark(Object eqKey, Object varKey) {
		Set<Object> row = utilityMarks.get(eqKey);
		
		if(row == null) {
			row = new HashSet<Object>();
			utilityMarks.put(eqKey, row);
		}
		row.add(varKey);
	}
	
	@Override
	protected IloLinearNumExpr createRowExpresion(IloCplex cplex, IloNumVar[] x, Entry<Object, Map<Object, Double>> rowEntry) throws IloException {
		IloLinearNumExpr rowExpr = cplex.linearNumExpr();
		Set<Object> rowUtilityMarks = utilityMarks.get(rowEntry.getKey());

		for (Entry<Object, Double> memberEntry : rowEntry.getValue().entrySet()) {
			if(isUtilityCoefficient(rowUtilityMarks, memberEntry))
				rowExpr.addTerm(-memberEntry.getValue().doubleValue() - utilityShift, x[getVariableIndex(memberEntry.getKey()) - 1]);
			else
				rowExpr.addTerm(-memberEntry.getValue().doubleValue(), x[getVariableIndex(memberEntry.getKey()) - 1]);
		}
		return rowExpr;
	}

	public boolean isUtilityCoefficient(Set<Object> rowUtilityMarks, Entry<Object, Double> memberEntry) {
		return rowUtilityMarks != null && rowUtilityMarks.contains(memberEntry.getKey());
	}

}
