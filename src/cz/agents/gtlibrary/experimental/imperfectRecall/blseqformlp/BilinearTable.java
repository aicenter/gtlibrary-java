package cz.agents.gtlibrary.experimental.imperfectRecall.blseqformlp;

import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPData;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPTable;
import cz.agents.gtlibrary.experimental.imperfectRecall.BilinearTermsMDT;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.InformationSet;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.Pair;
import ilog.concert.*;
import ilog.cplex.IloCplex;

import java.util.HashMap;
import java.util.Map;

public class BilinearTable extends LPTable {
    private Map<Object, Pair<Object, Object>> bilinearVars;
    private Map<Object, Integer> bilinearPrecision; // infromation set -> precision
    private Map<Object, IloNumVar[][]> wVariables; // information set -> binary variables for the bilinear terms for that IS
    private Map<Object, IloNumVar[][]> rHatVariables; // sequence -> rHat variables for sequence
    private Map<Object, IloRange> outgoingBilinearConstraints; // information set -> all constraints for the bilinear terms for that IS
    private Map<Object, IloRange> behavioralBilinearConstraints; // information set -> all constraints for the bilinear terms for that IS
    private final int INITIAL_MDT_PRECISION = 2;

    public BilinearTable() {
        outgoingBilinearConstraints = new HashMap<>();
        behavioralBilinearConstraints = new HashMap<>();
        bilinearVars = new HashMap<>();
        bilinearPrecision = new HashMap<>();
        wVariables = new HashMap<>();
        rHatVariables = new HashMap<>();
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
        if (outgoingBilinearConstraints.containsKey(product))
            data.getSolver().delete(outgoingBilinearConstraints.get(product));

        if (behavioralBilinearConstraints.containsKey(behavioral))
            data.getSolver().delete(behavioralBilinearConstraints.get(behavioral));

        IloRange[] newConstraints = addMDTConstraints(data, product, sequence, behavioral, precision);
        behavioralBilinearConstraints.put(behavioral,newConstraints[0]);
        outgoingBilinearConstraints.put(product, newConstraints[1]);
        bilinearPrecision.put(product, precision);
    }

    public IloRange[] addMDTConstraints(LPData data, Object product, Object sequence, Object behavioral, int precision) throws IloException{
        IloCplex cplex = data.getSolver();
        IloNumVar rSequenceToIS = data.getVariables()[getVariableIndex(sequence)];
        IloNumVar xBehStrategy = data.getVariables()[getVariableIndex(behavioral)];
        IloNumVar rSequenceFromIS = data.getVariables()[getVariableIndex(product)];



        final int digits = 10;
        IloRange[] result = new IloRange[2];
        IloNumVar[][] w;

        if (wVariables.containsKey(behavioral)) {
            IloNumVar[][] existingWs = wVariables.get(behavioral);
            precision = Math.max(precision, existingWs[0].length);
            w = new IloNumVar[digits][precision];
            for (int d=0; d < digits; d++)
                for (int existingPrecision = 0; existingPrecision < existingWs[d].length; existingPrecision++) {
                    w[d][existingPrecision] = existingWs[d][existingPrecision];
                }
        } else {
            w = new IloNumVar[digits][precision];
        }

        IloNumVar[][] rHat = new IloNumVar[digits][precision];

        if (rHatVariables.containsKey(product)) {
            IloNumVar[][] existingRHat = rHatVariables.get(product);
            for (int d=0; d < digits; d++)
                for (int existingPrecision = 0; existingPrecision < existingRHat[d].length; existingPrecision++) {
                    rHat[d][existingPrecision] = existingRHat[d][existingPrecision];
                }
        }

        IloNumExpr productSum = cplex.numExpr();
        IloNumExpr approxSum = cplex.numExpr();
        for (int l=0; l<precision; l++) {
            IloNumExpr xSum = cplex.numExpr();
            boolean thisPrecisionExists = false;
            for (int k = 0; k < digits; k++) {
                if ((l == 0) && (k > 1)) continue;
                if (w[k][l] == null)
                    w[k][l] = cplex.numVar(0,1, IloNumVarType.Bool,"W_" + behavioral.toString() + "_"+ k + "_"+ l );
                if (rHat[k][l] == null) {
                    rHat[k][l] = cplex.numVar(0, 1, IloNumVarType.Float, "RHAT_" + rSequenceFromIS.getName() + "_" + k + "_" + l);
                    cplex.addLe(rHat[k][l], w[k][l]);
                    thisPrecisionExists = true;
                }

                productSum = cplex.sum(productSum, cplex.prod(cplex.constant(Math.pow(10, -(l)) * k), rHat[k][l]));
                xSum = cplex.sum(xSum, rHat[k][l]);
                approxSum = cplex.sum(approxSum, cplex.prod(cplex.constant(Math.pow(10, -(l)) * k), w[k][l]));


            }
            if (!thisPrecisionExists)
                cplex.addEq(rSequenceToIS,xSum);
        }

        result[0] = cplex.addEq(cplex.diff(xBehStrategy,approxSum),0);
        result[1] = cplex.addLe(cplex.diff(rSequenceFromIS,cplex.sum(productSum, cplex.constant(Math.pow(10, -(precision-1))))),0);

        wVariables.put(behavioral, w);
        rHatVariables.put(product, rHat);
        return result;
    }

    public Map<Object, Pair<Object, Object>> getBilinearVars() {
        return bilinearVars;
    }

    public void refinePrecision(LPData data, Object productSequence) throws IloException{
        addBilinearConstraint(data, productSequence , bilinearVars.get(productSequence).getLeft(),bilinearVars.get(productSequence).getRight(),bilinearPrecision.get(productSequence)+1);
    }
}
