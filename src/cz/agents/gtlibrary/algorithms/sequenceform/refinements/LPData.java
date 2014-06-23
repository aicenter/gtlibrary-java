package cz.agents.gtlibrary.algorithms.sequenceform.refinements;

import ilog.concert.IloNumVar;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;

import java.util.Map;
import java.util.Set;

public class LPData {

	private IloCplex solver;
	private IloNumVar[] variables;
	private IloRange[] constraints;
	private Map<Object, IloRange> relaxableConstraints;
	private Set<Object> relaxableConstraintKeys;
	private Map<Object, IloNumVar> watchedPrimalVars;
	private Map<Object, IloRange> watchedDualVars;
	private int[] algorithms;

	public LPData(IloCplex solver, IloNumVar[] variables, IloRange[] constraints, Map<Object, IloRange> relaxableConstraints, 
			Map<Object, IloNumVar> watchedPrimalVars, Map<Object, IloRange> watchedDualVars) {
		super();
		this.solver = solver;
		this.variables = variables;
		this.constraints = constraints;
		this.relaxableConstraints = relaxableConstraints;
		this.watchedDualVars = watchedDualVars;
		this.watchedPrimalVars = watchedPrimalVars;
		algorithms = new int[]{IloCplex.Algorithm.Network, IloCplex.Algorithm.Barrier, IloCplex.Algorithm.Auto, IloCplex.Algorithm.Concurrent, IloCplex.Algorithm.Dual, 
				IloCplex.Algorithm.Primal, IloCplex.Algorithm.Sifting};
	}

	public IloRange[] getConstraints() {
		return constraints;
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
	
	public Map<Object, IloRange> getRelaxableConstraints() {
		return relaxableConstraints;
	}
	
	public int[] getAlgorithms() {
		return algorithms;
	}
	
	public Set<Object> getRelaxableConstraintKeys() {
		return relaxableConstraintKeys;
	}

}
