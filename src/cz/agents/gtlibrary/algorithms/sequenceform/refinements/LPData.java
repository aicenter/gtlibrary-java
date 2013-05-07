package cz.agents.gtlibrary.algorithms.sequenceform.refinements;

import ilog.concert.IloNumVar;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;

import java.util.Map;

public class LPData {
	
	private IloCplex solver;
	private IloNumVar[] variables;
	private IloRange[] constrains;
	private Map<Object, IloNumVar> watchedPrimalVars;
	private Map<Object, IloRange> watchedDualVars;
	
	public LPData(IloCplex solver, IloNumVar[] variables, IloRange[] constrains, Map<Object, IloNumVar> watchedPrimalVars,
			Map<Object, IloRange> watchedDualVars) {
		super();
		this.solver = solver;
		this.variables = variables;
		this.constrains = constrains;
		this.watchedDualVars = watchedDualVars;
		this.watchedPrimalVars = watchedPrimalVars;
	}
	
	public IloRange[] getConstrains() {
		return constrains;
	}
	
	public IloNumVar[] getVariables() {
		return variables;
	}
	
	public IloCplex getSolver() {
		return solver;
	}
	
	public Map<Object, IloRange> getWatchedDualVariables() {
		return watchedDualVars;
	}
	
	public Map<Object, IloNumVar> getWatchedPrimalVariables() {
		return watchedPrimalVars;
	}

}
