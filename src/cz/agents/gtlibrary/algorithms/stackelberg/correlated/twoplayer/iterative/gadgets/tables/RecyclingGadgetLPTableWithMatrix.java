package cz.agents.gtlibrary.algorithms.stackelberg.correlated.twoplayer.iterative.gadgets.tables;

import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPData;
import cz.agents.gtlibrary.interfaces.GameState;
import ilog.concert.IloException;
import ilog.concert.IloLPMatrix;
import ilog.concert.IloNumVar;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.*;

/**
 * Created by Jakub Cerny on 30/01/2018.
 */
public class RecyclingGadgetLPTableWithMatrix extends GadgetLPTable {

    protected IloLPMatrix matrix;
    protected ThreadMXBean threadBean;
    protected long constraintsRemovingTime;
    protected long cplexConstraintsRemovingTime;
    protected long constraintsCreatingTime;
    protected long newConsAndVarsFindingTime;
    protected long constraintsUpdatingTime;
    protected long objectiveUpdatingTime;

    protected int constructFullLPCount;
    protected int updateLPCount;

    public RecyclingGadgetLPTableWithMatrix(){
        super();
    }

    public RecyclingGadgetLPTableWithMatrix(HashSet<GameState> deletedGadgets, HashSet<GameState> createdGadgets,
                         HashMap<Object, Set<Object>> deletedConstraints, HashSet<Object> createdConstraints,
                         Integer gadgetsCreated, Integer gadgetsDismissed,
                         HashMap<GameState,HashMap<Object, HashSet<Object>>> varsToDelete,
                         HashMap<GameState, HashSet<Object>> eqsToDelete,
                         HashMap<GameState, HashSet<Object>> utilityToDelete,
                         HashSet<Object> createdUtility,
                         HashMap<Object, HashSet<Object>> updatedConstraints){
        super(deletedGadgets,createdGadgets,deletedConstraints,createdConstraints,gadgetsCreated,gadgetsDismissed,
                varsToDelete,eqsToDelete,utilityToDelete,createdUtility,updatedConstraints);
        threadBean = ManagementFactory.getThreadMXBean();
        constraintsCreatingTime = newConsAndVarsFindingTime = 0;
        constraintsUpdatingTime = objectiveUpdatingTime = 0;
        cplexConstraintsRemovingTime = 0;
        updateLPCount = 0;
        constructFullLPCount = 0;
    }

    @Override
    public LPData toCplex() throws IloException {
        System.out.println(Math.abs(lastLPSize - getLPSize()) + " / " + UPDATE_LP_TABLE_DIFFERENCE * getLPSize());
        if (!RESOLVE || Math.abs(lastLPSize - getLPSize()) > UPDATE_LP_TABLE_DIFFERENCE * getLPSize()){//createdGadgets.size() > UPDATE_LP_TABLE_DIFFERENCE * (gadgetsCreated - gadgetsDismissed)) {
            System.out.println("Creating new cplex model.");
            constructFullLPCount ++ ;
//            deleteOldGadgets();
            lastLPSize = getLPSize();
            constructClearModel();
            return new LPData(cplex, cplexVariables, cplexConstraints, getRelaxableConstraints(cplexConstraints), getWatchedPrimalVars(cplexVariables), getWatchedDualVars(cplexConstraints));
        }
        else{
            System.out.println("Updating existing cplex model.");
            updateLPCount ++;
            lastLPSize = getLPSize();

            // removed
            // updated
            // added

            // find removed -> remove from matrix and hashmap & reindex hashmap
            // find new vars & cons -> add vars into matrix
            // find new eqs -> add into matrix
            // find updated -> add into matrix

            long startTime = threadBean.getCurrentThreadCpuTime();
            removeOldConstraints();
            constraintsRemovingTime += threadBean.getCurrentThreadCpuTime() - startTime;

            startTime = threadBean.getCurrentThreadCpuTime();
            HashSet<Object> newEqs = new HashSet<>();
            HashSet<Object> missingEqs = new HashSet<>();
            findNewConsAndVars(newEqs, missingEqs);
            newConsAndVarsFindingTime += threadBean.getCurrentThreadCpuTime() - startTime;

//            System.out.println(newEqs.size() + " : " + missingEqs.size());

            startTime = threadBean.getCurrentThreadCpuTime();
            contructNewConstraints(newEqs, missingEqs);
            constraintsCreatingTime += threadBean.getCurrentThreadCpuTime() - startTime;

            // udelat updaty
            startTime = threadBean.getCurrentThreadCpuTime();
            updateOldConstraints(newEqs, missingEqs);
            constraintsUpdatingTime += threadBean.getCurrentThreadCpuTime() - startTime;

            // UTILITY update
            startTime = threadBean.getCurrentThreadCpuTime();
            updateUtility();
            objectiveUpdatingTime += threadBean.getCurrentThreadCpuTime() - startTime;

            System.out.println("Update done. Solving...");
            return new LPData(cplex, cplexVariables, cplexConstraints, getRelaxableConstraints(cplexConstraints), getWatchedPrimalVarsForResolve(cplexVariables), getWatchedDualVars(cplexConstraints));
        }
    }

    protected void constructClearModel() throws IloException {
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
    }

    protected void updateUtility() throws IloException {
        ArrayList<IloNumVar> objectiveVars = new ArrayList<>();
        ArrayList<Double> objectiveCoefs = new ArrayList<>();
        for(GameState state: deletedGadgets) {
            for (Object var : utilityToDelete.get(state)) {
                objectiveVars.add(cplexVariables[varToCplexVar.get(var)]);
                objectiveCoefs.add(0.0);
            }
        }
        for(GameState state: createdGadgets) {
            for (Object var : utilityToDelete.get(state)) {
                if (objective.containsKey(var)) {
                    objectiveVars.add(cplexVariables[varToCplexVar.get(var)]);
                    objectiveCoefs.add(objective.get(var));
                }
            }
        }
        for(Object var : createdUtility) {
            if (objective.containsKey(var)) {
                objectiveVars.add(cplexVariables[varToCplexVar.get(var)]);
                objectiveCoefs.add(objective.get(var));
            }
        }
        double[] objectiveCoefsArray = new double[objectiveCoefs.size()];
        for (int i = 0; i < objectiveCoefs.size(); i++) objectiveCoefsArray[i] = objectiveCoefs.get(i);
        cplex.setLinearCoefs(cplex.getObjective(), objectiveVars.toArray(new IloNumVar[0]), objectiveCoefsArray);
    }

    protected void updateOldConstraints(HashSet<Object> newEqs, HashSet<Object> missingEqs) throws IloException {
        ArrayList<Integer> updatedRows = new ArrayList<>();
        ArrayList<Integer> updatedCols = new ArrayList<>();
        ArrayList<Double> updatedValues = new ArrayList<>();
        for(GameState state: deletedGadgets) {
            for (Object eqKey : varsToDelete.get(state).keySet()) {
                Integer idx = eqToCplexEq.get(eqKey);
                if (idx == null) continue;
                for (Object varKey : varsToDelete.get(state).get(eqKey)) {
                    updatedRows.add(idx);
                    updatedCols.add(varToCplexVar.get(varKey));
                    updatedValues.add(0.0);
                }
            }
        }
        for(GameState state: createdGadgets) {
            for (Object eqKey : varsToDelete.get(state).keySet()) {
                if (!missingEqs.contains(eqKey)) {
                    int idx = eqToCplexEq.get(eqKey);
                    for (Object varKey : varsToDelete.get(state).get(eqKey)) {
                        updatedRows.add(idx);
                        updatedCols.add(varToCplexVar.get(varKey));
                        updatedValues.add(constraints.get(eqKey).get(varKey));
                    }
                }
            }
        }
        for(Object eqKey : createdConstraints){
            if(!newEqs.contains(eqKey)){
                int idx = eqToCplexEq.get(eqKey);
                for (Object varKey : constraints.get(eqKey).keySet()) {
                    updatedRows.add(idx);
                    updatedCols.add(varToCplexVar.get(varKey));
                    updatedValues.add(constraints.get(eqKey).get(varKey));
                }
            }
        }
        for(Object eqKey : updatedConstraints.keySet()){
            if(!missingEqs.contains(eqKey)){
                int idx = eqToCplexEq.get(eqKey);
                for (Object varKey : updatedConstraints.get(eqKey)) {
                    updatedRows.add(idx);
                    updatedCols.add(varToCplexVar.get(varKey));
                    updatedValues.add(constraints.get(eqKey).get(varKey));
                }
            }
        }
        int[] rows = new int[updatedRows.size()];
        int[] cols = new int[updatedCols.size()];
        double[] values = new double[updatedValues.size()];
        for (int i = 0; i < rows.length; i++){
            rows[i] = updatedRows.get(i);
            cols[i] = updatedCols.get(i);
            values[i] = updatedValues.get(i);
        }
//            System.out.println(Arrays.toString(rows));
//            System.out.println(Arrays.toString(cols));
        if (rows.length > 0)
            matrix.setNZs(rows, cols, values);
    }

    protected void findNewConsAndVars(HashSet<Object> newEqs, HashSet<Object> missingEqs) throws IloException {
        HashSet<Object> newVars = new HashSet<>();
        getNewVarsAndCons(newVars, newEqs, missingEqs);
        int i = 0;//varToCplexVar.size();
        IloNumVar[] newIloVars = new IloNumVar[newVars.size()];
        for (Object var : newVars){
            varToCplexVar.put(var, varToCplexVar.size());
            newIloVars[i] = cplex.numVar(getLB(var), getUB(var), var.toString());
            i++;
        }
        matrix.addCols(newIloVars);
        cplexVariables = matrix.getNumVars();
    }

    protected void removeOldConstraints() throws IloException {
        HashSet<Object> cplexEqsToDelete = new HashSet<>();
        for(GameState state: deletedGadgets){
            for (Object eqKey : eqsToDelete.get(state)){
                cplexEqsToDelete.add(eqKey);
            }
        }
        for(Object eqKey : deletedConstraints.keySet()){
            cplexEqsToDelete.add(eqKey);
        }
        int[] rowsToRemove = new int[cplexEqsToDelete.size()];
        int i = 0;
        for(Object key : cplexEqsToDelete){ rowsToRemove[i] = eqToCplexEq.get(key); eqToCplexEq.remove(key); i++; }
        long startTime = threadBean.getCurrentThreadCpuTime();
        matrix.removeRows(rowsToRemove);
        cplexConstraintsRemovingTime += threadBean.getCurrentThreadCpuTime() - startTime;
        ArrayList list = new ArrayList(eqToCplexEq.keySet());
        Collections.sort(list, new Comparator() {
            public int compare(Object o1, Object o2) {
                return eqToCplexEq.get(o1).compareTo(eqToCplexEq.get(o2));
            }
        });
        eqToCplexEq.clear();
        i = 0;
        for (Object key : list){ eqToCplexEq.put(key, i); i++;}
    }

    protected void contructNewConstraints(HashSet<Object> newEqs, HashSet<Object> missingEqs) throws IloException {
        int[][] idxs = new int[newEqs.size()][];
        double[][] vals = new double[idxs.length][];
        double[] lbs = new double[idxs.length];
        double[] ubs = new double[idxs.length];
        int j = 0;
        for(GameState state: createdGadgets) {
            for (Object con : varsToDelete.get(state).keySet()) {
                if (missingEqs.contains(con)) {
                    j = constructNZ(idxs, vals, lbs, ubs, j, con);
                }
            }
            for (Object con : eqsToDelete.get(state)) {
                j = constructNZ(idxs, vals, lbs, ubs, j, con);
            }
        }
        for(Object con : createdConstraints){
            if (newEqs.contains(con))
                j = constructNZ(idxs, vals, lbs, ubs, j, con);
        }
        for(Object con : updatedConstraints.keySet()) {
            if (missingEqs.contains(con)) {
                j = constructNZ(idxs, vals, lbs, ubs, j, con);
            }
        }
        matrix.addRows(lbs, ubs, idxs, vals);
        cplexConstraints = matrix.getRanges();
    }

    protected int constructNZ(int[][] idxs, double[][] vals, double[] lbs, double[] ubs, int j, Object con) {
        if (eqToCplexEq.containsKey(con)) return j;
        eqToCplexEq.put(con, eqToCplexEq.size());
        int[] idx = new int[constraints.get(con).keySet().size()];
        double[] val = new double[idx.length];
        int k = 0;
        for(Object var : constraints.get(con).keySet()){
            idx[k] = varToCplexVar.get(var);
            val[k] = constraints.get(con).get(var);
            k++;
        }
        idxs[j] = idx;
        vals[j] = val;
        switch (constraintTypes.get(con)){
            case 0 : lbs[j] = Double.NEGATIVE_INFINITY; ubs[j] = getConstant(con); break;
            case 1 : lbs[j] = getConstant(con); ubs[j] =  getConstant(con); break;
            case 2 : lbs[j] = getConstant(con); ubs[j] =  Double.POSITIVE_INFINITY; break;
        }
        j++;
        return j;
    }

    protected void getNewVarsAndCons(HashSet<Object> newVars, HashSet<Object> newEqs, HashSet<Object> missingEqs) {
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
//            System.out.println(eqKey);
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
    }

    @Override
    protected IloRange[] addConstraintsViaMatrix(IloCplex cplex, IloNumVar[] x) throws IloException {
        System.out.println("# of vars: " + columnCount() + "; # of cons: " + constraints.keySet().size());
        eqToCplexEq.clear();

        matrix = cplex.addLPMatrix();
        matrix.addCols(x);
        System.out.println("Adding constraints.");
        int[][] idxs = new int[constraints.keySet().size()][];
        double[][] vals = new double[idxs.length][];
        double[] lbs = new double[idxs.length];
        double[] ubs = new double[idxs.length];
        int j = 0;
        for(Object con : constraints.keySet()){
            eqToCplexEq.put(con, j);
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
                case 0 : lbs[j] = Double.NEGATIVE_INFINITY; ubs[j] = getConstant(con); break;
                case 1 : lbs[j] = getConstant(con); ubs[j] =  getConstant(con); break;
                case 2 : lbs[j] = getConstant(con); ubs[j] =  Double.POSITIVE_INFINITY; break;
            }
            j++;
//            matrix.addRow(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, idx, vals);
        }
        matrix.addRows(lbs, ubs, idxs, vals);

        return matrix.getRanges();
    }

    public int getConstructFullLPCount(){return constructFullLPCount;}
    public int getUpdateLPCount(){return updateLPCount;}

    public String getTimeStats(){
        return "constraints creating time = " + constraintsCreatingTime / 1000000l + "\n" +
                "constraints removing time = " + constraintsRemovingTime / 1000000l + "\n" +
                "cplex constraints removing time = " + cplexConstraintsRemovingTime / 1000000l + "\n" +
                "constraints updating time = " + constraintsUpdatingTime / 1000000l + "\n" +
                "finding new cons and vars time = " + newConsAndVarsFindingTime / 1000000l + "\n" +
                "objective updating time = " + objectiveUpdatingTime / 1000000l + "\n" +
                "lp update count = " + updateLPCount + "\n" +
                "lp construct count = " + constructFullLPCount;
    }

}
