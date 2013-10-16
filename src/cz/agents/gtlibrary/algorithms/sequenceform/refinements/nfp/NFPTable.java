package cz.agents.gtlibrary.algorithms.sequenceform.refinements.nfp;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

import java.util.Map;
import java.util.Map.Entry;

import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.bothplayerslp.LPTable;

public class NFPTable extends LPTable {
	
	protected IloLinearNumExpr createRowExpresion(IloCplex cplex, IloNumVar[] x, Entry<Object, Map<Object, Double>> rowEntry) throws IloException {
		IloLinearNumExpr rowExpr = cplex.linearNumExpr();

		for (Entry<Object, Double> memberEntry : rowEntry.getValue().entrySet()) {
			rowExpr.addTerm(memberEntry.getValue().doubleValue(), x[getVariableIndex(memberEntry.getKey()) - 1]);
		}
		return rowExpr;
	}

}
