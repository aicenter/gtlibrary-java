package cz.agents.gtlibrary.algorithms.stackelberg.correlated.twoplayer.iterative.gadgets;

import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPData;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPTable;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.Pair;
import ilog.concert.*;
import ilog.cplex.IloCplex;

import java.util.*;

/**
 * Created by Jakub Cerny on 11/12/2017.
 */
public class GadgetLPTable extends LPTable {

//    protected HashSet<Object> removedVars;

    protected HashSet<Object> binaryVariables;

    protected boolean isFirstSolution;
    protected IloNumVar[] variables;

    protected final boolean USE_PREVIOUS_SOLUTION = false;
    protected final boolean DELETE_SINGLE_VAR_CONSTRAINTS = false;

    public GadgetLPTable(){
        super();
//        removedVars = new HashSet<>();
        CPLEXALG = IloCplex.Algorithm.Auto;//Primal;//Barrier;
        isFirstSolution = true;
        binaryVariables = new HashSet<>();
    }


    protected void setStart() throws IloException {

//        if(true) return;

        double[] costs = cplex.getReducedCosts(variables);
        double[] values = cplex.getValues(variables);

        cplex.clearModel();
//        cplex.setVectors(values, costs, variables, null, null, null);

    }

    @Override
    public LPData toCplex() throws IloException {

        double[] costs = null;
        double[] values = null;
        IloNumVar[] oldVars = null;

        if (USE_PREVIOUS_SOLUTION && !isFirstSolution){
            costs = cplex.getReducedCosts(variables);
            values = cplex.getValues(variables);
            oldVars = Arrays.copyOf(variables, variables.length);

            cplex.clearModel();
        }
        else cplex.clearModel();

//        cplex.clearModel();
        cplex.setParam(IloCplex.IntParam.RootAlg, CPLEXALG);
        cplex.setParam(IloCplex.IntParam.Threads, CPLEXTHREADS);
        cplex.setParam(IloCplex.IntParam.MIPEmphasis, IloCplex.MIPEmphasis.Balanced);
//        cplex.setParam(IloCplex.DoubleParam.EpMrk, 0.99999);
//		cplex.setParam(IloCplex.DoubleParam.BarEpComp, 1e-4);
//		System.out.println("BarEpComp: " + cplex.getParam(IloCplex.DoubleParam.BarEpComp));
//        cplex.setParam(IloCplex.BooleanParam.NumericalEmphasis, true);
        cplex.setOut(null);

        System.out.println("Getting variables.");
        variables = getVariables();
//        for (IloNumVar v : variables) System.out.println(v);
//        IloRange[] constraints = addConstraints(cplex, variables);
        IloRange[] constraints = addConstraintsViaMatrix(cplex, variables);
//        System.out.println("Var len:" + variables.length);
//        System.out.println("Con len:" + constraints.length);

        if (USE_PREVIOUS_SOLUTION && costs != null){
            ArrayList<Double> newCosts = new ArrayList<Double>();
            ArrayList<Double> newValues = new ArrayList<>();
            ArrayList<IloNumVar> newVars = new ArrayList<>();
            ArrayList<String> currentVars = new ArrayList<>();
            for (IloNumVar v : variables) currentVars.add(v.getName());
            int idx;
            for (int i = 0; i < oldVars.length; i++) {
                idx = currentVars.indexOf(oldVars[i].getName());
                if (idx != -1) {
                    newCosts.add(costs[i]);
                    newValues.add(values[i]);
                    newVars.add(variables[idx]);
                }
            }
            double[] nC = new double[newCosts.size()];
            double[] nVl = new double[newValues.size()];
            IloNumVar[] nVr = new IloNumVar[newVars.size()];
            for (int i = 0; i < nC.length; i++){
                nC[i] = newCosts.get(i);
                nVl[i] = newValues.get(i);
                nVr[i] = newVars.get(i);
            }
            System.out.printf("Setting initial(" + nC.length+")...");
//            cplex.setVectors(nVl, nC, nVr, null, null, null);
        }

        addObjective(variables);
        isFirstSolution = false;
        return new LPData(cplex, variables, constraints, getRelaxableConstraints(constraints), getWatchedPrimalVars(variables), getWatchedDualVars(constraints));
    }

    @Override
    protected IloRange[] addConstraintsViaMatrix(IloCplex cplex, IloNumVar[] x) throws IloException {
        System.out.println("# of vars: " + columnCount() + "; # of cons: " + constraints.keySet().size());

//        ArrayList<String> names = new ArrayList<>();
//        for (IloNumVar v : x) {
//            names.add(v.toString());
//            for (Object w : x)
//                if ( w != v && w.toString().equals(v.toString()))
//                    System.out.println(w);
//        }
//        System.out.println(names.size() - (new HashSet<>(names)).size());
//
//        names = new ArrayList<>();
//        for (Object v : constraints.keySet()) {
//            names.add(v.toString());
////            for (Object w : constraints.keySet())
////                if ( w != v && w.toString().equals(v.toString()))
////                    if(w instanceof Pair){
////                        System.out.println("Pair!"); System.exit(0);
////                    }
//        }
//        System.out.println(names.size() - (new HashSet<>(names)).size());

//        HashSet<Double> coefs = new HashSet<>();

        IloLPMatrix matrix = cplex.addLPMatrix();
        matrix.addCols(x);
        System.out.println("Adding constraints.");
        int[][] idxs = new int[constraints.keySet().size()][];
        double[][] vals = new double[idxs.length][];
        double[] lbs = new double[idxs.length];
        double[] ubs = new double[idxs.length];
        int j = 0;
        for(Object con : constraints.keySet()){
//            if (constraints.get(con).size() == 1) System.out.println(con + " : " + constraints.get(con).keySet().iterator().next());
            int[] idx = new int[constraints.get(con).keySet().size()];
            double[] val = new double[idx.length];
            int i = 0;
//            if (constraintTypes.get(con) == 2) System.out.println(con);
            for(Object var : constraints.get(con).keySet()){
//                coefs.add(constraints.get(con).get(var));
//                if (constraintTypes.get(con) == 2) System.out.println("\t"+var);
//                if (!variableIndices.containsKey(var)) {
//                    System.out.println(var);
//                    System.out.println(con);
//                    for (Object v : constraints.get(con).keySet())
//                        System.out.printf(constraints.get(con).get(v) + "x" + v + " + ");
//                    System.out.println();
//                    System.exit(0);
//                }
                idx[i] = variableIndices.get(var);
                val[i] = constraints.get(con).get(var);
                i++;
            }
            idxs[j] = idx;
            vals[j] = val;
//            if (!constraintTypes.containsKey(con)){ System.out.println(con + " : " + constraints.get(con).size()); j++; continue;}
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

//        for(double d :coefs) System.out.printf(d + " ");

//        removedVars.clear();
        return matrix.getRanges();
    }

    public void deleteConstraint(Object eqKey){
        if (constraints.get(eqKey) != null){
//            System.out.println("deleting: " + eqKey);
            for (Object varKey : constraints.get(eqKey).keySet())
                deleteVar(varKey);
            constraints.remove(eqKey);
            constraintTypes.remove(eqKey);
            constants.remove(eqKey);
            equationIndices.remove(eqKey);
//            removedVars.add(eqKey);
        }
        else{
//            System.out.println("unable to delete: " + eqKey);
//            System.exit(0);
        }
    }

    public void deleteConstraintWithoutVars(Object eqKey){
        if (constraints.get(eqKey) != null){
            constraints.remove(eqKey);
            constraintTypes.remove(eqKey);
            constants.remove(eqKey);
            equationIndices.remove(eqKey);
        }
    }

    public void deleteVar(Object varKey){
        if (varKey instanceof Pair){
            Pair p = (Pair)varKey;
            if(p.getLeft() instanceof Sequence && p.getRight() instanceof Sequence)
                if((((Sequence)p.getRight()).isEmpty() || !(((Sequence)p.getRight()).getLast() instanceof GadgetAction)) &&
                        (((Sequence)p.getLeft()).isEmpty() || !(((Sequence)p.getLeft()).getLast() instanceof GadgetAction)))
                    return;
        }
        if (variableIndices.get(varKey) != null){
            variableIndices.remove(varKey);
            ub.remove(varKey);
            lb.remove(varKey);
            primalWatch.remove(varKey);
        }
    }

    @Override
    protected IloNumVar[] getVariables() throws IloException {
        updateVariableIndices();
        if(binaryVariables.isEmpty()) {
            double[] ub = getUpperBounds();
            double[] lb = getLowerBounds();
            if (USE_CUSTOM_NAMES) {
                String[] variableNames = getVariableNames();
                return cplex.numVarArray(variableNames.length, lb, ub, variableNames);
            } else {
                return cplex.numVarArray(lb.length, lb, ub);
            }
        }
        else {
            IloNumVar[] vars = new IloNumVar[variableIndices.size()];
            if (USE_CUSTOM_NAMES) {
                for (Object var : variableIndices.keySet()) {
                    int idx = variableIndices.get(var);
                    if (binaryVariables.contains(var))
                        vars[idx] = cplex.numVar(getLB(var), getUB(var), IloNumVarType.Bool, var.toString());
                    else
                        vars[idx] = cplex.numVar(getLB(var), getUB(var), var.toString());
                }
            }
            else {
                for (Object var : variableIndices.keySet()) {
                    int idx = variableIndices.get(var);
                    if (binaryVariables.contains(var))
                        vars[idx] = cplex.numVar(getLB(var), getUB(var), IloNumVarType.Bool);
                    else
                        vars[idx] = cplex.numVar(getLB(var), getUB(var));
                }
            }
            return vars;
        }
    }

    protected double getUB(Object var){
        Double bound = ub.get(var);
        if (bound == null) bound = Double.POSITIVE_INFINITY;
        return bound;
    }

    protected double getLB(Object var){
        Double bound = lb.get(var);
        if (bound == null) bound = 0.0;
        return bound;
    }

    protected void updateVariableIndices(){
//        if(true)return;
        if (DELETE_SINGLE_VAR_CONSTRAINTS) {
            HashSet<Object> emptyCons = new HashSet<>();
            for (Object con : constraints.keySet()) {
                if (constraints.get(con).size() <= 1)
                    emptyCons.add(con);
            }
            for (Object con : emptyCons) constraints.remove(con);//deleteConstraint(con);
        }

        ArrayList list = new ArrayList(variableIndices.keySet());
        Collections.sort(list, new Comparator() {
            public int compare(Object o1, Object o2) {
                return variableIndices.get(o1).compareTo(variableIndices.get(o2));
            }
        });
        variableIndices.clear();
        for(int i = 0; i < list.size(); i++) {
            variableIndices.put(list.get(i), i);
            if (primalWatch.containsKey(list.get(i)))
                primalWatch.put(list.get(i), i);
        }
    }

    public IloNumVar[] getVars(){
        return variables;
    }

    public void markAsBinary(Object var){
        binaryVariables.add(var);
    }

}
