package cz.agents.gtlibrary.algorithms.stackelberg.correlated.multiplayer.lpTable;

import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPTable;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by Jakub Cerny on 30/08/2017.
 */
public class ConstraintGeneratingLPTable extends LPTable {

    protected HashSet<Object> activeVariables = new HashSet<>();

    @Override
    protected IloLinearNumExpr createRowExpresion(IloCplex cplex, IloNumVar[] x, Map.Entry<Object, Map<Object, Double>> rowEntry) throws IloException {
        IloLinearNumExpr rowExpr = cplex.linearNumExpr();

        for (Map.Entry<Object, Double> memberEntry : rowEntry.getValue().entrySet()) {
            if (activeVariables.contains(memberEntry.getKey()))
                rowExpr.addTerm(memberEntry.getValue(), x[getVariableIndex(memberEntry.getKey())]);
        }
        return rowExpr;
    }

    public HashSet<Object> getCons(){
        return new HashSet<>(constraints.keySet());
    }

    @Override
    protected void addObjective(IloNumVar[] x) throws IloException {
        IloLinearNumExpr objExpr = cplex.linearNumExpr();

        for (Map.Entry<Object, Double> entry : objective.entrySet()) {
            if (activeVariables.contains(entry.getKey()))
                objExpr.addTerm(entry.getValue(), x[variableIndices.get(entry.getKey())]);
        }
        cplex.addMaximize(objExpr);
    }

    public void addActiveVariable(Object variable){
        activeVariables.add(variable);
    }

    public void watchAllDualVariables(){
        for (Object eqKey : constraints.keySet())
            watchDualVariable(eqKey, eqKey);
    }

    public HashSet<Object> getPrimalVariables(){
        return  new HashSet<>(primalWatch.keySet());
    }

    public HashMap<Object, Double> getConstraintsWithValueFor(Object varKey){
        HashMap<Object, Double> constrains =  new HashMap<>();
        for (Map.Entry<Object, Map<Object, Double>> entry : constraints.entrySet())
            if (entry.getValue().containsKey(varKey))
                constrains.put(entry.getKey(), entry.getValue().get(varKey));
        return  constrains;
    }


}
