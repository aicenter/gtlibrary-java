package cz.agents.gtlibrary.algorithms.sequenceform.refinements;

import ilog.concert.IloException;
import ilog.concert.IloLPMatrix;
import ilog.concert.IloNumVar;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;

import java.util.*;

/**
 * Created by Jakub Cerny on 26/09/2017.
 */
public class ColumnGenerationLPTable extends LPTable {

    protected IloLPMatrix matrix;
//    protected ArrayList<Object> newConstraints;
//    protected ArrayList<Object> newVariables;
    protected HashSet<Object> newConstraints;
    protected HashSet<Object> newVariables;
    protected HashMap<Object, HashSet> consOfNewVars;
//    protected ArrayList<Object> newVariablesOutsideOriginalCons;
    protected HashMap<Object, Double> newObjective;

    protected int initialVarIndex;
    protected int initialConIndex;

//    protected HashMap<Object, Integer> newConIndices;
//    protected HashMap<Object, Integer> newVarIndices;

    protected boolean DEBUG = false;

 public ColumnGenerationLPTable(){
     super();
     matrix = null;
//     newConstraints = new ArrayList<>();
//     newVariables = new ArrayList<>();
     newConstraints = new HashSet<>();
     newVariables = new HashSet<>();
//     newVariablesOutsideOriginalCons = new ArrayList<>();
     newObjective = new HashMap<>();
     consOfNewVars = new HashMap<>();
//     newConstraints = new LinkedHashMap<Object, Map<Object,Double>>();

 }

    @Override
    public LPData toCplex() throws IloException {
//        cplex.clearModel();
//        cplex.setParam(IloCplex.IntParam.RootAlg, CPLEXALG);
//        cplex.setParam(IloCplex.IntParam.Threads, CPLEXTHREADS);
//        cplex.setParam(IloCplex.IntParam.MIPEmphasis, IloCplex.MIPEmphasis.Balanced);
//        cplex.setParam(IloCplex.DoubleParam.EpMrk, 0.99999);
//		cplex.setParam(IloCplex.DoubleParam.BarEpComp, 1e-4);
//		System.out.println("BarEpComp: " + cplex.getParam(IloCplex.DoubleParam.BarEpComp));
//        cplex.setParam(IloCplex.BooleanParam.NumericalEmphasis, true);
        cplex.setOut(null);

        IloNumVar[] variables = null;
        IloRange[] constraints = null;

        if (matrix == null) {
            System.out.printf("initial...");
//            cplex.clearModel();
            cplex.setParam(IloCplex.IntParam.RootAlg, CPLEXALG);
            cplex.setParam(IloCplex.IntParam.Threads, CPLEXTHREADS);
            cplex.setParam(IloCplex.IntParam.MIPEmphasis, IloCplex.MIPEmphasis.Balanced);
            variables = getVariables();
//        IloRange[] constraints = addConstraints(cplex, variables);
            constraints = addConstraintsViaMatrix(cplex, variables);
//        System.out.println("Var len:" + variables.length);
//        System.out.println("Con len:" + constraints.length);
//            System.out.println(Arrays.toString(variables));
            addObjective(variables);
            for (Object var : newObjective.keySet())
                cplex.setLinearCoef(cplex.getObjective(), matrix.getNumVar(variableIndices.get(var)), newObjective.get(var));

//            constraints = addNewConstraintsViaMatrix();
//            variables = matrix.getNumVars();
//            for (Object var : newObjective.keySet())
//                cplex.setLinearCoef(cplex.getObjective(), matrix.getNumVar(variableIndices.get(var)), newObjective.get(var));

        }
        else {
//            System.out.println("Adding new rows and cols.");
            System.out.printf("iterative...");
            constraints = addNewConstraintsViaMatrix();
            variables = matrix.getNumVars();
            for (Object var : newObjective.keySet())
                cplex.setLinearCoef(cplex.getObjective(), matrix.getNumVar(variableIndices.get(var)), newObjective.get(var));
        }

//        System.out.println(matrix.toString());
//        System.out.println("Vars: " + variableIndices.size() + "; cons: " + this.constraints.size());
        newObjective.clear();
        newConstraints.clear();
        newVariables.clear();
        consOfNewVars.clear();

        System.out.printf("lpData...");

        return new LPData(cplex, variables, constraints, getRelaxableConstraints(constraints), getWatchedPrimalVars(variables), getWatchedDualVars(constraints));
    }

    @Override
    protected IloRange[] addConstraintsViaMatrix(IloCplex cplex, IloNumVar[] x) throws IloException {
        matrix = cplex.addLPMatrix();
        matrix.addCols(x);
        int[][] idxs = new int[constraints.keySet().size()][];
        double[][] vals = new double[idxs.length][];
        double[] lbs = new double[idxs.length];
        double[] ubs = new double[idxs.length];
        int j = 0;
        for(Object con : constraints.keySet()){
            j = equationIndices.get(con);
            int[] idx = new int[constraints.get(con).keySet().size()];
            double[] val = new double[idx.length];
            int i = 0;
            for(Object var : constraints.get(con).keySet()){
                idx[i] = variableIndices.get(var);
                val[i] = constraints.get(con).get(var);
                i++;
            }
            idxs[j] = idx;
            vals[j] = val;
            if (constraintTypes.get(con) == null) {
                System.out.println(con);
                for (Object var : constraints.get(con).keySet())
                    System.out.println(var);
            }
            switch (constraintTypes.get(con)){
//                case 0 : matrix.addRow(Double.NEGATIVE_INFINITY, getConstant(con), idx, val); break;
//                case 1 : matrix.addRow(getConstant(con), getConstant(con), idx, val); break;
//                case 2 : matrix.addRow(getConstant(con), Double.POSITIVE_INFINITY, idx, val); break;
                case 0 : lbs[j] = Double.NEGATIVE_INFINITY; ubs[j] = getConstant(con); break;
                case 1 : lbs[j] = getConstant(con); ubs[j] =  getConstant(con); break;
                case 2 : lbs[j] = getConstant(con); ubs[j] =  Double.POSITIVE_INFINITY; break;
            }
            j++;
//            matrix.addRow(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, idx, vals);
        }
        matrix.addRows(lbs, ubs, idxs, vals);
        if (DEBUG) System.out.println("Initial vars: " + variableIndices.size() + "; cons: " + constraints.size());
        if (DEBUG) System.out.println("Initial vars in matrix: " + matrix.getNcols() + "; cons: " + matrix.getRanges().length);
//        matrix.
//        matrix.addRow(); CpxLPMatrix
        return matrix.getRanges();
    }

    private int[] createPrimitiveArrayForInt(ArrayList<Integer> list){
        int[] array = new int[list.size()];
        for (int i = 0; i < list.size(); i++) array[i] = list.get(i);
        return  array;
    }

    private double[] createPrimitiveArrayForDouble(ArrayList<Double> list){
        double[] array = new double[list.size()];
        for (int i = 0; i < list.size(); i++) array[i] = list.get(i);
        return  array;
    }

    protected IloRange[] addNewConstraintsViaMatrix() throws IloException {
        // add new cols and rows
//        matrix.addCols(getNewVariables());
//        matrix.addRows(y);

        System.out.printf("newVars...");
        IloNumVar[] x = getNewVariables();
        int[][] idxs = new int[newVariables.size()][];
        double[][] vals = new double[idxs.length][];
        int j = 0;
        for(Object var : newVariables){
            j = variableIndices.get(var) - matrix.getNcols();
//            ArrayList<Integer> idx = new ArrayList<Integer>();
//            ArrayList<Double> val = new ArrayList();
            int k = 0;
            idxs[j] = new int[consOfNewVars.get(var).size()];
            vals[j] = new double[idxs[j].length];
            for(Object con : consOfNewVars.get(var)){//constraints.keySet()){
//                if (newConstraints.contains(con)) continue;
//                if(constraints.get(con).containsKey(var)){
                idxs[j][k] = equationIndices.get(con);
                vals[j][k] = constraints.get(con).get(var);
                k++;
//                    idx.add(equationIndices.get(con));
//                    val.add(constraints.get(con).get(var));
//                }
            }
//            idxs[j] = createPrimitiveArrayForInt(idx);
//            vals[j] = createPrimitiveArrayForDouble(val);
//            j++;
        }


//        matrix.addCols(x);
//        System.out.println(x.length + " " + idxs.length);
        if (DEBUG) System.out.println("Number of new vars:" + newVariables.size());
        if (DEBUG) System.out.println("Vars: " + matrix.getNcols() + "; rows:" + matrix.getRanges().length);
        if (DEBUG) System.out.println("Idx: " + Arrays.deepToString(idxs));
        if (DEBUG) System.out.println("Vals: " + Arrays.deepToString(vals));
        matrix.addCols(x, idxs, vals);
        if (DEBUG) System.out.println("Vars: " + matrix.getNcols() + "; rows:" + matrix.getRanges().length);
        if (DEBUG) System.out.println("Variables: " + variableIndices.size());
//        System.out.println("Idx: " + Arrays.deepToString(idxs));
//        System.out.println("Vals: " + Arrays.deepToString(vals));
        if (DEBUG) System.out.println("");
//        System.out.println(Collections.min(equationIndices.values()) + " " + Collections.min(variableIndices.values()));

        // generate new rows
        System.out.printf("newCons...");
//        System.out.println("Current number of rows: "+ matrix.getRanges().length + ". Adding new rows to matrix: " + newConstraints.size());
        idxs = new int[newConstraints.size()][];
        vals = new double[idxs.length][];
        double[] lbs = new double[idxs.length];
        double[] ubs = new double[idxs.length];
        j = 0;
        for(Object con : newConstraints){
            j = equationIndices.get(con) - matrix.getRanges().length;
            int[] idx = new int[constraints.get(con).keySet().size()];
            double[] val = new double[idx.length];
            int i = 0;
            for(Object var : constraints.get(con).keySet()){
                idx[i] = variableIndices.get(var);
                val[i] = constraints.get(con).get(var);
                i++;
            }
            idxs[j] = idx;
            vals[j] = val;
            switch (constraintTypes.get(con)){
//                case 0 : matrix.addRow(Double.NEGATIVE_INFINITY, getConstant(con), idx, val); break;
//                case 1 : matrix.addRow(getConstant(con), getConstant(con), idx, val); break;
//                case 2 : matrix.addRow(getConstant(con), Double.POSITIVE_INFINITY, idx, val); break;
                case 0 : lbs[j] = Double.NEGATIVE_INFINITY; ubs[j] = getConstant(con); break;
                case 1 : lbs[j] = getConstant(con); ubs[j] =  getConstant(con); break;
                case 2 : lbs[j] = getConstant(con); ubs[j] =  Double.POSITIVE_INFINITY; break;
            }
//            j++;
//            matrix.addRow(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, idx, vals);
        }
        if (DEBUG) System.out.println("Vars: " + matrix.getNcols() + "; rows:" + matrix.getRanges().length);
        if (DEBUG) System.out.println("Lbs: " + Arrays.toString(lbs));
        if (DEBUG) System.out.println("Ubs: " + Arrays.toString(ubs));
        if (DEBUG) System.out.println("Idx: " + Arrays.deepToString(idxs));
        if (DEBUG) System.out.println("Vals: " + Arrays.deepToString(vals));
        matrix.addRows(lbs, ubs, idxs, vals);
//        matrix.addRow
//        matrix.
//        matrix.addRow(); CpxLPMatrix
        return matrix.getRanges();
    }

    protected IloNumVar[] getNewVariables() throws IloException {
        double[] ub = getNewUpperBounds();
        double[] lb = getNewLowerBounds();
        if(USE_CUSTOM_NAMES) {
            String[] variableNames = getVariableNames();

            return cplex.numVarArray(variableNames.length, lb, ub, variableNames);
        } else {
            return cplex.numVarArray(lb.length, lb, ub);
        }
    }

//    @Override
//    public void setObjective(Object varKey, double value) {
//        objective.put(varKey, value);
//        Integer result = variableIndices.get(varKey);
//
////        if (varKey == null)
//
//        if (result == null) {
////            newVariables.add(varKey);
//            result = variableIndices.size();
//            variableIndices.put(varKey, result);
//        }
//    }

    public void setNewObjective(Object varKey, double value) {
        newObjective.put(varKey, value);
        Integer result = variableIndices.get(varKey);

//        if (varKey == null)

        if (result == null) {
            newVariables.add(varKey);
            consOfNewVars.put(varKey, new HashSet<>());
            result = variableIndices.size();
            variableIndices.put(varKey, result);
        }
    }

    protected double[] getNewLowerBounds() {
        double[] lb = new double[newVariables.size()];

        int initialIndex = 0;
        try {
            initialIndex = matrix.getNcols();
        } catch (IloException e) {
            e.printStackTrace();
        }

        for (Object var : newVariables){
            Double result = this.lb.get(var);
            if (result != null) lb[variableIndices.get(var) - initialIndex] = result;
        }
//        for (int i = 0; i < newVariables.size(); i++) {
//            Double result = this.lb.get(newVariables.get(i));
//            if (result != null) lb[i] = result;
//        }
        return lb;
    }

    protected double[] getNewUpperBounds() {
//        System.out.prinnewC
        double[] ub = new double[newVariables.size()];

//        for (int i = 0; i < newVariables.size(); i++) {
//            ub[i] = Double.POSITIVE_INFINITY;
//        }
        int initialIndex = 0;
        try {
            initialIndex = matrix.getNcols();
        } catch (IloException e) {
            e.printStackTrace();
        }
        for (Object var : newVariables){
            Double result = this.ub.get(var);
            ub[variableIndices.get(var) - initialIndex] = (result == null) ? Double.POSITIVE_INFINITY : result;
        }
//        for (int i = 0; i < newVariables.size(); i++) {
//            Double result = this.ub.get(newVariables.get(i));
//            ub[i] = (result == null) ? Double.POSITIVE_INFINITY : result;
//        }
        return ub;
    }

    public void setNewConstraint(Object eqKey, Object varKey, double value) {
//        System.out.println("Setting new constraint. Previous number of cons: " + constraints.size() + " : " + equationIndices.size());
        Map<Object, Double> row = constraints.get(eqKey);


        boolean newCon = row == null;
        if (row == null) {
//            System.out.println("Adding new constraint.");
            row = new LinkedHashMap<>();
            constraints.put(eqKey, row);
            newConstraints.add(eqKey);
        }
        row.put(varKey, value);
        updateEquationIndices(eqKey);
//        System.out.println("Current number of cons: " + constraints.size() + " : " + equationIndices.size());
        Integer result = variableIndices.get(varKey);

        if (result == null) {
            newVariables.add(varKey);
            consOfNewVars.put(varKey, new HashSet<>());
//            consOfNewVars.get(varKey).add(eqKey);
            result = variableIndices.size();
            variableIndices.put(varKey, result);
        }
        else {
//            if (newVariables.contains(varKey) && !newCon) {
//                consOfNewVars.get(varKey).add(eqKey);
//            }
        }
        if (!newCon && newVariables.contains(varKey) && !newConstraints.contains(eqKey)) {
            consOfNewVars.get(varKey).add(eqKey);
        }
//        matrix.
    }


}
