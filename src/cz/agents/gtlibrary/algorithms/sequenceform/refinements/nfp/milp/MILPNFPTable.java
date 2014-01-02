package cz.agents.gtlibrary.algorithms.sequenceform.refinements.nfp.milp;

import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;

import java.util.Map.Entry;

import cz.agents.gtlibrary.algorithms.sequenceform.refinements.Key;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPData;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.nfp.NFPTable;


public class MILPNFPTable extends NFPTable {

	public enum VarType {
		Binary, Real
	}

	@Override
	public LPData toCplex() throws IloException {
		cplex.clearModel();
		cplex.setParam(IloCplex.IntParam.RootAlg, CPLEXALG);
		cplex.setParam(IloCplex.IntParam.Threads, CPLEXTHREADS);
		cplex.setParam(IloCplex.DoubleParam.EpMrk, 0.99999);
		cplex.setParam(IloCplex.BooleanParam.NumericalEmphasis, true);
		cplex.setOut(null);

		double[] ub = getUpperBounds();
		double[] lb = getLowerBounds();
		String[] variableNames = getVariableNames();
		VarType[] variableTypes = getVariableTypes();
		IloNumVar[] variables = new IloNumVar[variableNames.length];

		for (int i = 0; i < variableTypes.length; i++) {
			if (variableTypes[i].equals(VarType.Binary))
				variables[i] = cplex.boolVar(variableNames[i]);
			else
				variables[i] = cplex.numVar(lb[i], ub[i], variableNames[i]);
		}
		IloRange[] constraints = addConstraints(cplex, variables);

		addObjective(variables);
		return new LPData(cplex, variables, constraints, getRelaxableConstraints(constraints), getWatchedPrimalVars(variables), getWatchedDualVars(constraints));
	}

	private VarType[] getVariableTypes() {
		VarType[] variableTypes = new VarType[variableIndices.size()];

		for (Entry<Object, Integer> entry : variableIndices.entrySet()) {
			if (primalWatch.containsKey(entry.getKey()) && entry.getKey() instanceof Key)
				variableTypes[entry.getValue()] = VarType.Binary;
			else
				variableTypes[entry.getValue()] = VarType.Real;
		}
		return variableTypes;
	}

}
