package cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp;

import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPData;
import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;

import java.util.Map;

public class BilinearLPData extends LPData {
    private Map<Object, IloNumVar[][]> wVariables;

    public BilinearLPData(IloCplex solver, IloNumVar[] variables, IloRange[] constraints,
                          Map<Object, IloRange> relaxableConstraints, Map<Object, IloNumVar> watchedPrimalVars,
                          Map<Object, IloRange> watchedDualVars, Map<Object, IloNumVar[][]> wVariables) {
        super(solver, variables, constraints, relaxableConstraints, watchedPrimalVars, watchedDualVars);
        this.wVariables = wVariables;
    }

    public double getNonDeltaValue(Object object) {
        double value = 0;
        IloNumVar[][] actionWValues = wVariables.get(wVariables);

        try {
            for (int i = 0; i < 2; i++) {
                value += getSolver().getValue(actionWValues[i][0]);
            }
            for (int j = 1; j < 10; j++) {
                for (int i = 1; i < actionWValues[j].length; i++) {
                    value += getSolver().getValue(actionWValues[j][i]) * Math.pow(10, -i);
                }
            }
            return value;
        } catch (IloException e) {
            e.printStackTrace();
        }
        return -1;
    }
}
