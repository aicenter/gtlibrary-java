package cz.agents.gtlibrary.experimental.imperfectRecall.blseqformlp;

import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPData;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPTable;
import cz.agents.gtlibrary.experimental.imperfectRecall.BilinearTermsMDT;
import cz.agents.gtlibrary.utils.Pair;
import ilog.concert.*;
import ilog.cplex.IloCplex;

import java.util.HashMap;
import java.util.Map;

public class BilinearTable extends LPTable {
    private Map<Object, Pair<Object, Object>> bilinearVars;
    private Map<Object, Integer> bilinearPrecision;
    private Map<Object, IloRange[]> bilinearConstraints;
    private final int INITIAL_MDT_PRECISION = 2;

    public BilinearTable() {
        bilinearConstraints = new HashMap<>();
        bilinearVars = new HashMap<>();
        bilinearPrecision = new HashMap<>();
    }

    public void markAsBilinear(Object bilinearVarKey, Object cause1Key, Object cause2Key) {
        bilinearVars.put(bilinearVarKey, new Pair<>(cause1Key, cause2Key));
    }

    @Override
    public LPData toCplex() throws IloException {
        LPData data = super.toCplex();

        for (Map.Entry<Object,Pair<Object,Object>> entry : bilinearVars.entrySet()) {
            addBilinearConstraint(data,entry.getKey(),entry.getValue().getLeft(),entry.getValue().getRight(),INITIAL_MDT_PRECISION);
        }
        return data;
    }

    public void addBilinearConstraint(LPData data, Object product, Object sequence, Object behavioral, int precision) throws IloException {
        if (bilinearConstraints.containsKey(product))
            for (IloRange constraint : bilinearConstraints.get(product)) {
                data.getSolver().delete(constraint);
            }
        IloRange[] newConstraints = addMDTConstraints(data.getSolver(), data.getVariables()[getVariableIndex(sequence)],
                data.getVariables()[getVariableIndex(behavioral)], data.getVariables()[getVariableIndex(product)], precision);
        bilinearConstraints.put(product, newConstraints);
        bilinearPrecision.put(product, precision);
    }

    public IloRange[] addMDTConstraints(IloCplex cplex, IloNumVar x, IloNumVar y, IloNumVar xy, int precision) throws IloException{
        final int digits = 10;
        IloRange[] result = new IloRange[2];
        IloNumVar[][] w = new IloNumVar[digits][precision];
        IloNumVar[][] xHat = new IloNumVar[digits][precision];

        IloNumExpr zSum = cplex.numExpr();
        IloNumExpr ySum = cplex.numExpr();
        for (int l=0; l<precision; l++) {
            IloNumExpr xSum = cplex.numExpr();
            for (int k = 0; k < digits; k++) {
                if ((l == 0) && (k > 1)) continue;
                w[k][l] = cplex.numVar(0,1, IloNumVarType.Bool,"W_" + xy.getName() + "_"+ k + "_"+ l );
                xHat[k][l] = cplex.numVar(0,1,IloNumVarType.Float, "XHAT_" + xy.getName() + "_"+ k + "_"+ l );

                zSum = cplex.sum(zSum, cplex.prod(cplex.constant(Math.pow(10, -(l)) * k), xHat[k][l]));
                xSum = cplex.sum(xSum, xHat[k][l]);
                ySum = cplex.sum(ySum, cplex.prod(cplex.constant(Math.pow(10, -(l)) * k), w[k][l]));

                cplex.addLe(xHat[k][l],w[k][l]);
            }
            cplex.addEq(x,xSum);
        }

        result[0] = cplex.addEq(cplex.diff(y,ySum),0);
        result[1] = cplex.addEq(cplex.diff(xy,zSum),0);
        return result;
    }

    public Map<Object, Pair<Object, Object>> getBilinearVars() {
        return bilinearVars;
    }

    public void refinePrecision(LPData data, Object productSequence) throws IloException{
        addBilinearConstraint(data, productSequence , bilinearVars.get(productSequence).getLeft(),bilinearVars.get(productSequence).getRight(),bilinearPrecision.get(productSequence)+1);
    }
}
