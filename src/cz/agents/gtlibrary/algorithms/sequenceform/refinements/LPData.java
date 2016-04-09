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
}
