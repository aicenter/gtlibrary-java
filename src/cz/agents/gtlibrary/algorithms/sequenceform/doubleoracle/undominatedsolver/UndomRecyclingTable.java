package cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.undominatedsolver;

import cz.agents.gtlibrary.algorithms.sequenceform.refinements.RecyclingLPTable;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;

import java.util.HashMap;
import java.util.Map;

public class UndomRecyclingTable extends RecyclingLPTable {

    protected Map<Object, Double> updatedObjective;
    protected Map<Object, Double> newObjective;
    protected boolean clearObjective;
    protected boolean maximize;

    public UndomRecyclingTable() {
        super();
        newObjective = new HashMap<Object, Double>();
        updatedObjective = new HashMap<Object, Double>();
        clearObjective = false;
        maximize = true;
    }

    public void addToObjective(Object varKey, double value) {
        Double oldValue = objective.get(varKey);

        if (oldValue == null) {
            objective.put(varKey, value);
            newObjective.put(varKey, value);
        } else {
            objective.put(varKey, oldValue + value);
            updatedObjective.put(varKey, oldValue + value);
        }
        updateVariableIndices(varKey);
    }

    public void clearObjective() {
        objective.clear();
        newObjective.clear();
        updatedObjective.clear();
        clearObjective = true;
    }

    public void setMaximize(boolean maximize) {
        this.maximize = maximize;
    }

    @Override
    protected void addObjective(IloNumVar[] x) throws IloException {
        if (clearObjective)
            cplex.remove(lpObj);

        IloLinearNumExpr objExpr = cplex.linearNumExpr();

        for (Map.Entry<Object, Double> entry : newObjective.entrySet()) {
            objExpr.addTerm(entry.getValue(), x[getVariableIndex(entry.getKey()) - 1]);
        }
        if (lpObj == null || clearObjective) {
            if (maximize)
                lpObj = cplex.addMaximize(objExpr);
            else
                lpObj = cplex.addMinimize(objExpr);
            clearObjective = false;
        } else {
            cplex.addToExpr(lpObj, objExpr);
        }
        newObjective.clear();

        for (Map.Entry<Object, Double> objectDoubleEntry : updatedObjective.entrySet()) {
            cplex.setLinearCoef(lpObj, x[getVariableIndex(objectDoubleEntry.getKey()) - 1], objectDoubleEntry.getValue());
        }
        updatedObjective.clear();
    }
}
