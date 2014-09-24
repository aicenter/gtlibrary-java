package cz.agents.gtlibrary.algorithms.sequenceform.gensum;

import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPTable;
import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloNumVar;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MILPTable extends LPTable {

    private Set<Object> binaryVariables;

    public MILPTable() {
        super();
        binaryVariables = new HashSet<>();
    }

    public void markAsBinary(Object varKey) {
        binaryVariables.add(varKey);
    }

    @Override
    protected IloNumVar[] getVariables() throws IloException {
        IloNumVar[] variables = new IloNumVar[columnCount()];

        for (Object variable : variableIndices.keySet()) {
            if (binaryVariables.contains(variable))
                variables[getVariableIndex(variable)] = createBinaryVar(variable);
            else
                variables[getVariableIndex(variable)] = createNumericVar(variable);
        }
        return variables;
    }

    private IloNumVar createNumericVar(Object variable) throws IloException {
        return cplex.numVar(getLowerBound(variable), getUpperBound(variable), variable.toString());
    }

    private double getLowerBound(Object variable) {
        Double lb = this.lb.get(variable);

        return lb == null ? 0 : lb;
    }

    private double getUpperBound(Object variable) {
        Double ub = this.ub.get(variable);

        return ub == null ? Double.POSITIVE_INFINITY : ub;
    }

    private IloNumVar createBinaryVar(Object variable) throws IloException {
        return cplex.boolVar(variable.toString());
    }
}
