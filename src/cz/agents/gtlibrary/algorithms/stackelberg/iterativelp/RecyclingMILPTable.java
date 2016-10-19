package cz.agents.gtlibrary.algorithms.stackelberg.iterativelp;

import cz.agents.gtlibrary.algorithms.sequenceform.refinements.RecyclingLPTable;
import ilog.concert.IloException;
import ilog.concert.IloNumVar;

import java.util.HashSet;
import java.util.Set;

public class RecyclingMILPTable extends RecyclingLPTable {

    private Set<Integer> binaryVariables;

    public RecyclingMILPTable() {
        super();
        binaryVariables = new HashSet<>();
    }

    public void markAsBinary(Object varKey) {
        binaryVariables.add(getVariableIndex(varKey));
    }

    @Override
    protected IloNumVar[] updateVariables(Object[] keys, String[] variableNames, double[] lb, double[] ub) throws IloException {
        if (lpVariables == null)
            lpVariables = new IloNumVar[0];
//            return cplex.numVarArray(variableNames.length, lb, ub, variableNames);

        IloNumVar[] newVariables = new IloNumVar[variableNames.length];

        for (int i = 0; i < lpVariables.length; i++) {
            newVariables[i] = lpVariables[i];
        }
        for (int i = lpVariables.length; i < newVariables.length; i++) {
            if(binaryVariables.contains(i))
                newVariables[i] = createBinaryVar(variableNames[i]);
            else
                newVariables[i] = createNumericVar(lb[i], ub[i], variableNames[i]);
        }
        return newVariables;
    }

    private IloNumVar createNumericVar(double lb, double ub, String varName) throws IloException {
        return cplex.numVar(lb, ub, varName);
    }

    private double getLowerBound(Object variable) {
        Double lb = this.lb.get(variable);

        return lb == null ? 0 : lb;
    }

    private double getUpperBound(Object variable) {
        Double ub = this.ub.get(variable);

        return ub == null ? Double.POSITIVE_INFINITY : ub;
    }

    private IloNumVar createBinaryVar(String varName) throws IloException {
        return cplex.boolVar(varName);
    }

    public void clearTable() {
        super.clearTable();
        binaryVariables.clear();
    }
}
