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


package cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.undominated;

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
            objExpr.addTerm(entry.getValue(), x[getVariableIndex(entry.getKey())]);
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
            cplex.setLinearCoef(lpObj, x[getVariableIndex(objectDoubleEntry.getKey())], objectDoubleEntry.getValue());
        }
        updatedObjective.clear();
    }
}
