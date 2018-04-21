package cz.agents.gtlibrary.algorithms.stackelberg.correlated.twoplayer.iterative.gadgets.tables;

import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPData;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPTable;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.RecyclingLPTable;
import cz.agents.gtlibrary.algorithms.stackelberg.correlated.twoplayer.iterative.gadgets.GadgetAction;
import cz.agents.gtlibrary.interfaces.GameState;
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
    protected IloNumVar[] cplexVariables;
    protected IloRange[] cplexConstraints;

    protected final boolean USE_PREVIOUS_SOLUTION = false;
    protected final boolean DELETE_SINGLE_VAR_CONSTRAINTS = false;

    protected HashSet<GameState> deletedGadgets;
    protected HashSet<GameState> createdGadgets;
    protected HashMap<Object, Set<Object>> deletedConstraints;
    protected HashSet<Object> createdConstraints;
    protected HashMap<Object, HashSet<Object>> updatedConstraints;
    protected HashSet<Object> createdUtility;

    protected HashMap<GameState,HashMap<Object, HashSet<Object>>> varsToDelete;
    protected HashMap<GameState, HashSet<Object>> eqsToDelete;
    protected HashMap<GameState, HashSet<Object>> utilityToDelete;

    protected Integer gadgetsCreated, gadgetsDismissed;

    protected int lastLPSize = Integer.MAX_VALUE;

    protected final double UPDATE_LP_TABLE_DIFFERENCE = 0.2;
    protected final boolean RESOLVE;

    protected HashMap<Object, Integer> eqToCplexEq = new HashMap<>();
    protected HashMap<Object, Integer> varToCplexVar = new HashMap<>();

    public String getInfo(){
        return this.getClass().getSimpleName() + ": resolve = " + RESOLVE + ", update when diff = " + UPDATE_LP_TABLE_DIFFERENCE;
    }

    public GadgetLPTable(){
        super();
//        removedVars = new HashSet<>();
        CPLEXALG = IloCplex.Algorithm.Auto;//Primal;//Barrier;
        isFirstSolution = true;
        binaryVariables = new HashSet<>();
        RESOLVE = false;
    }

    public GadgetLPTable(HashSet<GameState> deletedGadgets, HashSet<GameState> createdGadgets,
                         HashMap<Object, Set<Object>> deletedConstraints, HashSet<Object> createdConstraints,
                         Integer gadgetsCreated, Integer gadgetsDismissed,
                         HashMap<GameState,HashMap<Object, HashSet<Object>>> varsToDelete,
                         HashMap<GameState, HashSet<Object>> eqsToDelete,
                         HashMap<GameState, HashSet<Object>> utilityToDelete,
                         HashSet<Object> createdUtility,
                         HashMap<Object, HashSet<Object>> updatedConstraints){
        super();
//        removedVars = new HashSet<>();
        CPLEXALG = IloCplex.Algorithm.Auto;//Primal;//Barrier;
        isFirstSolution = true;
        binaryVariables = new HashSet<>();
        this.deletedGadgets = deletedGadgets;
        this.createdGadgets = createdGadgets;
        this.deletedConstraints = deletedConstraints;
        this.createdConstraints = createdConstraints;
        this.gadgetsCreated = gadgetsCreated;
        this.gadgetsDismissed = gadgetsDismissed;
        this.varsToDelete = varsToDelete;
        this.eqsToDelete = eqsToDelete;
        this.utilityToDelete = utilityToDelete;
        this.createdUtility = createdUtility;
        this.updatedConstraints = updatedConstraints;
        RESOLVE = true;
    }


    protected void setStart() throws IloException {

//        if(true) return;

        double[] costs = cplex.getReducedCosts(cplexVariables);
        double[] values = cplex.getValues(cplexVariables);

        cplex.clearModel();
//        cplex.setVectors(values, costs, variables, null, null, null);

    }

    protected void deleteOldGadgets(){
        for (GameState state : deletedGadgets) {
            for (Object eqKey : varsToDelete.get(state).keySet())
                for (Object varKey : varsToDelete.get(state).get(eqKey)) {
                    setConstraint(eqKey, varKey, 0);
                    deleteVar(varKey);
                }
            for (Object var : utilityToDelete.get(state)) {
                setObjective(var, 0);
                deleteVar(var);
            }
            for (Object eqKey : eqsToDelete.get(state)) {
                deleteConstraint(eqKey);
            }
        }
        for (Object eqKey : deletedConstraints.keySet())
            deleteConstraintWithoutVars(eqKey);
    }

    @Override
    public LPData toCplex() throws IloException {
        System.out.println(Math.abs(lastLPSize - getLPSize()) + " / " + UPDATE_LP_TABLE_DIFFERENCE * getLPSize());
        if (!RESOLVE || Math.abs(lastLPSize - getLPSize()) > UPDATE_LP_TABLE_DIFFERENCE * getLPSize()){//createdGadgets.size() > UPDATE_LP_TABLE_DIFFERENCE * (gadgetsCreated - gadgetsDismissed)) {
            System.out.println("Creating new cplex model.");
//            deleteOldGadgets();
            lastLPSize = getLPSize();
            cplex.clearModel();

            cplex.setParam(IloCplex.IntParam.RootAlg, CPLEXALG);
            cplex.setParam(IloCplex.IntParam.Threads, CPLEXTHREADS);
            cplex.setParam(IloCplex.IntParam.MIPEmphasis, IloCplex.MIPEmphasis.Balanced);
            cplex.setOut(null);

            System.out.println("Getting variables.");
            cplexVariables = getVariables();
            varToCplexVar.clear();
            for (Object var : variableIndices.keySet())
                varToCplexVar.put(var, variableIndices.get(var));

            cplexConstraints = addConstraintsViaMatrix(cplex, cplexVariables);

            addObjective(cplexVariables);
            return new LPData(cplex, cplexVariables, cplexConstraints, getRelaxableConstraints(cplexConstraints), getWatchedPrimalVars(cplexVariables), getWatchedDualVars(cplexConstraints));
        }
        else{
            System.out.println("Updating existing cplex model.");
            lastLPSize = getLPSize();
//            updateVariableIndices();
//            updateEquationsIndices();
            // delete old
            ArrayList<IloNumVar> objectiveVars = new ArrayList<>();
            ArrayList<Double> objectiveCoefs = new ArrayList<>();

            ArrayList<IloRange> cplexEqsToDelete = new ArrayList<>();

            IloNumVar[] eqVars;// = new ArrayList<>();
            double[] eqCoefs;// = new ArrayList<>();
            for(GameState state: deletedGadgets){
                for (Object eqKey: varsToDelete.get(state).keySet()) {
                    eqVars = new IloNumVar[varsToDelete.get(state).get(eqKey).size()];
//                    eqCoefs = new double[varsToDelete.get(state).get(eqKey).size()];
                    int i = 0;
                    for (Object varKey : varsToDelete.get(state).get(eqKey)) {
                        eqVars[i] = cplexVariables[varToCplexVar.get(varKey)];
                        i++;
//                        cplex.setLinearCoef(cplexConstraints[eqToCplexEq.get(eqKey)], cplexVariables[varToCplexVar.get(varKey)], 0.0);
                    }
                    cplex.setLinearCoefs(cplexConstraints[eqToCplexEq.get(eqKey)], eqVars, new double[eqVars.length]);
                }
                for (Object var : utilityToDelete.get(state)) {
                    objectiveVars.add(cplexVariables[varToCplexVar.get(var)]);
                    objectiveCoefs.add(0.0);
//                    cplex.setLinearCoef(cplex.getObjective(),cplexVariables[varToCplexVar.get(var)], 0.0);
                }
                for (Object eqKey : eqsToDelete.get(state)){
//                    cplexEqsToDelete.add(cplexConstraints[eqToCplexEq.get(eqKey)]);
                    if (!eqToCplexEq.containsKey(eqKey)) continue;
                    cplex.delete(cplexConstraints[eqToCplexEq.get(eqKey)]);
                    eqToCplexEq.remove(eqKey);
//                    cplex.remove(cplexConstraints[eqToCplexEq.get(eqKey)]);
//                    cplexConstraints[eqToCplexEq.get(eqKey)] = null;
                }
            }
//            cplex.delete(cplexEqsToDelete.toArray(new IloRange[0]));
//            cplexEqsToDelete.clear();
            for(Object eqKey : deletedConstraints.keySet()){
                cplexEqsToDelete.add(cplexConstraints[eqToCplexEq.get(eqKey)]);
//                cplex.delete(cplexConstraints[eqToCplexEq.get(eqKey)]);
//                cplex.remove(cplexConstraints[eqToCplexEq.get(eqKey)]);
//                cplexConstraints[eqToCplexEq.get(eqKey)] = null;
            }
            System.out.println("Deleted obsolete cons and vars.");
            // add new
            HashSet<Object> newVars = new HashSet<>();
            HashSet<Object> newEqs = new HashSet<>();
            HashSet<Object> missingEqs = new HashSet<>();
            for(GameState state: createdGadgets){
                for (Object eqKey: varsToDelete.get(state).keySet()) {
                    if (!eqToCplexEq.containsKey(eqKey)) {
                        newEqs.add(eqKey);
                        missingEqs.add(eqKey);
                    }
                    for (Object varKey : varsToDelete.get(state).get(eqKey)) {
                        if (!varToCplexVar.containsKey(varKey))
                            newVars.add(varKey);
                    }
                }
                for (Object var : utilityToDelete.get(state)) {
                    if (!varToCplexVar.containsKey(var))
                        newVars.add(var);
                }
                for (Object eqKey : eqsToDelete.get(state)){
                    if (!eqToCplexEq.containsKey(eqKey))
                        newEqs.add(eqKey);
                    for (Object varKey : constraints.get(eqKey).keySet()) {
                        if (!varToCplexVar.containsKey(varKey))
                            newVars.add(varKey);
                    }
                }
            }
            for(Object eqKey : createdConstraints){
                if (!eqToCplexEq.containsKey(eqKey)) {
                    newEqs.add(eqKey);
                }
                for (Object varKey : constraints.get(eqKey).keySet()) {
                    if (!varToCplexVar.containsKey(varKey))
                        newVars.add(varKey);
                }
            }
            for(Object eqKey : updatedConstraints.keySet()){
                if (!eqToCplexEq.containsKey(eqKey)) {
                    newEqs.add(eqKey);
                    missingEqs.add(eqKey);
                }
                for (Object varKey : updatedConstraints.get(eqKey)) {
                    if (!varToCplexVar.containsKey(varKey))
                        newVars.add(varKey);
                }
            }
            for(Object var : createdUtility)
                if (!varToCplexVar.containsKey(var))
                    newVars.add(var);

            // create new vars and cons
            int j = cplexConstraints.length;
            IloRange[] newCplexConstraints = new IloRange[cplexConstraints.length + newEqs.size()];
            for(int i = 0; i < cplexConstraints.length; i++)
                newCplexConstraints[i] = cplexConstraints[i];
//            System.arraycopy(cplexConstraints, 0, newCplexConstraints, 0, cplexConstraints.length);
            cplexConstraints = newCplexConstraints;
            for (Object eqKey : newEqs){
                eqToCplexEq.put(eqKey, j); j++;
            }

            int i = cplexVariables.length;
            IloNumVar[] newCplexVariables = new IloNumVar[cplexVariables.length + newVars.size()];
//            System.arraycopy(cplexVariables, 0, newCplexVariables, 0, cplexVariables.length);
            for(int k = 0; k < cplexVariables.length; k++)
                newCplexVariables[k] = cplexVariables[k];
            cplexVariables = newCplexVariables;
            for (Object var : newVars){
//                i = variableIndices.get(var);
                variableIndices.put(var,i);
                varToCplexVar.put(var, i);
                cplexVariables[i] = cplex.numVar(getLB(var), getUB(var), var.toString());
                i++;
            }

            System.out.println("Identified new cons and vars.");

            for(GameState state: createdGadgets){
                for (Object eqKey: varsToDelete.get(state).keySet()) {
                    if (missingEqs.contains(eqKey))
                        cplexConstraints[eqToCplexEq.get(eqKey)] = createConstraint(eqKey);
                    else {
                        eqVars = new IloNumVar[varsToDelete.get(state).get(eqKey).size()];
                        eqCoefs = new double[varsToDelete.get(state).get(eqKey).size()];
                        int l = 0;
                        for (Object varKey : varsToDelete.get(state).get(eqKey)) {
                            eqVars[l] = cplexVariables[varToCplexVar.get(varKey)];
                            eqCoefs[l] = constraints.get(eqKey).get(varKey);
                            l++;
//                            cplex.setLinearCoef(cplexConstraints[eqToCplexEq.get(eqKey)], cplexVariables[varToCplexVar.get(varKey)], constraints.get(eqKey).get(varKey));
                        }
                        cplex.setLinearCoefs(cplexConstraints[eqToCplexEq.get(eqKey)], eqVars, eqCoefs);
                    }
                }
                for (Object var : utilityToDelete.get(state)) {
                    if (objective.containsKey(var)) {
                        objectiveVars.add(cplexVariables[varToCplexVar.get(var)]);
                        objectiveCoefs.add(objective.get(var));
//                        cplex.setLinearCoef(cplex.getObjective(), cplexVariables[varToCplexVar.get(var)], objective.get(var));
                    }
                }
                for (Object eqKey : eqsToDelete.get(state)){
                    cplexConstraints[eqToCplexEq.get(eqKey)] = createConstraint(eqKey);
                }
            }
            for(Object eqKey : createdConstraints){
                cplexConstraints[eqToCplexEq.get(eqKey)] = createConstraint(eqKey);
            }
            for(Object eqKey : updatedConstraints.keySet()){
                if(missingEqs.contains(eqKey))
                    cplexConstraints[eqToCplexEq.get(eqKey)] = createConstraint(eqKey);
                else{
                    int l = 0;
                    eqVars = new IloNumVar[updatedConstraints.get(eqKey).size()];
                    eqCoefs = new double[updatedConstraints.get(eqKey).size()];
                    for(Object varKey : updatedConstraints.get(eqKey)) {
                        eqVars[l] = cplexVariables[varToCplexVar.get(varKey)];
                        eqCoefs[l] = constraints.get(eqKey).get(varKey);
                        l++;
//                        cplex.setLinearCoef(cplexConstraints[eqToCplexEq.get(eqKey)], cplexVariables[varToCplexVar.get(varKey)], constraints.get(eqKey).get(varKey));
                    }
//                    System.out.println(eqKey + " / " + eqToCplexEq.get(eqKey) + " / " + cplexConstraints[eqToCplexEq.get(eqKey)] + " \n " + Arrays.toString(eqVars) + "\n"+Arrays.toString(eqCoefs));
                    cplex.setLinearCoefs(cplexConstraints[eqToCplexEq.get(eqKey)], eqVars, eqCoefs);
                }
            }
            for(Object var : createdUtility)
                if (objective.containsKey(var)) {
                    objectiveVars.add(cplexVariables[varToCplexVar.get(var)]);
                    objectiveCoefs.add(objective.get(var));
//                    cplex.setLinearCoef(cplex.getObjective(), cplexVariables[varToCplexVar.get(var)], objective.get(var));
                }
//            deleteOldGadgets();
            System.out.println("Added new cons and vars.");

            double[] objectiveCoefsArray = new double[objectiveCoefs.size()];
            for (i = 0; i < objectiveCoefs.size(); i++) objectiveCoefsArray[i] = objectiveCoefs.get(i);
            cplex.setLinearCoefs(cplex.getObjective(), objectiveVars.toArray(new IloNumVar[0]), objectiveCoefsArray);
            cplex.delete(cplexEqsToDelete.toArray(new IloRange[0]));
            System.out.println("Update done. Solving...");
            return new LPData(cplex, cplexVariables, cplexConstraints, getRelaxableConstraints(cplexConstraints), getWatchedPrimalVarsForResolve(cplexVariables), getWatchedDualVars(cplexConstraints));
        }
    }

    protected Map<Object, IloNumVar> getWatchedPrimalVarsForResolve(IloNumVar[] variables) {
        Map<Object, IloNumVar> watchedPrimalVars = new LinkedHashMap<Object, IloNumVar>();

        for (Map.Entry<Object, Integer> entry : primalWatch.entrySet()) {
            if (!varToCplexVar.containsKey(entry.getKey())) {
                System.out.println(entry.getKey()); continue;
            }
            watchedPrimalVars.put(entry.getKey(), variables[varToCplexVar.get(entry.getKey())]);
        }
        return watchedPrimalVars;
    }

    protected IloRange createConstraint(Object eqKey) throws IloException {
        IloLinearNumExpr rowExpr = createRowExpresion(cplex, cplexVariables, eqKey);
        Integer constraintType = constraintTypes.get(eqKey);

        switch (constraintType) {
            case 0:
                if (USE_CUSTOM_NAMES)
                    return cplex.addLe(rowExpr, getConstant(eqKey), eqKey.toString());
                else
                    return cplex.addLe(rowExpr, getConstant(eqKey));
            case 1:
                if (USE_CUSTOM_NAMES)
                    return cplex.addEq(rowExpr, getConstant(eqKey), eqKey.toString());
                else
                    return cplex.addEq(rowExpr, getConstant(eqKey));
            case 2:
                if (USE_CUSTOM_NAMES)
                    return cplex.addGe(rowExpr, getConstant(eqKey), eqKey.toString());
                else
                    return cplex.addGe(rowExpr, getConstant(eqKey));
            default:
                break;
        }
        return null;
    }

    protected IloLinearNumExpr createRowExpresion(IloCplex cplex, IloNumVar[] x, Object eqKey) throws IloException {
        IloLinearNumExpr rowExpr = cplex.linearNumExpr();

        for (Map.Entry<Object, Double> memberEntry : constraints.get(eqKey).entrySet()) {
            if (Math.abs(memberEntry.getValue()) > 0.01)
                rowExpr.addTerm(memberEntry.getValue(), x[varToCplexVar.get(memberEntry.getKey())]);
        }
        return rowExpr;
    }

    @Override
    protected IloRange[] addConstraintsViaMatrix(IloCplex cplex, IloNumVar[] x) throws IloException {
        System.out.println("# of vars: " + columnCount() + "; # of cons: " + constraints.keySet().size());
        eqToCplexEq.clear();
//        varToCplexVar.clear();

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
            eqToCplexEq.put(con, j);
//            iloRangeIdx.put(getEquationIndex(con),j);
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

//





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

    protected void updateEquationsIndices(){

        ArrayList list = new ArrayList(equationIndices.keySet());
        Collections.sort(list, new Comparator() {
            public int compare(Object o1, Object o2) {
                return equationIndices.get(o1).compareTo(equationIndices.get(o2));
            }
        });
        equationIndices.clear();
        for(int i = 0; i < list.size(); i++) {
            equationIndices.put(list.get(i), i);
//            if (primalWatch.containsKey(list.get(i)))
//                primalWatch.put(list.get(i), i);
        }
    }

    public IloNumVar[] getVars(){
        return cplexVariables;
    }

    public void markAsBinary(Object var){
        binaryVariables.add(var);
    }

    public Set<Object> getVarsInEq(Object eqKey){return constraints.containsKey(eqKey) ? constraints.get(eqKey).keySet() : new HashSet<Object>() {
    };}

}
