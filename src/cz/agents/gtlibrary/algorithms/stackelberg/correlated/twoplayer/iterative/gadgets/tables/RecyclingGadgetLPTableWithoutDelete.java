package cz.agents.gtlibrary.algorithms.stackelberg.correlated.twoplayer.iterative.gadgets.tables;

import cz.agents.gtlibrary.algorithms.sequenceform.refinements.RecyclingLPTable;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.utils.Pair;
import ilog.concert.IloException;
import ilog.cplex.IloCplex;

import java.util.*;

/**
 * Created by Jakub Cerny on 31/01/2018.
 */
public class RecyclingGadgetLPTableWithoutDelete extends RecyclingGadgetLPTableWithMatrix {

    public RecyclingGadgetLPTableWithoutDelete(HashSet<GameState> deletedGadgets, HashSet<GameState> createdGadgets, HashMap<Object, Set<Object>> deletedConstraints, HashSet<Object> createdConstraints, Integer gadgetsCreated, Integer gadgetsDismissed, HashMap<GameState, HashMap<Object, HashSet<Object>>> varsToDelete, HashMap<GameState, HashSet<Object>> eqsToDelete, HashMap<GameState, HashSet<Object>> utilityToDelete, HashSet<Object> createdUtility, HashMap<Object, HashSet<Object>> updatedConstraints) {
        super(deletedGadgets,createdGadgets,deletedConstraints,createdConstraints,gadgetsCreated,gadgetsDismissed,
                varsToDelete,eqsToDelete,utilityToDelete,createdUtility,updatedConstraints);
    }

    @Override
    protected void constructClearModel() throws IloException {

        for (GameState state : deletedGadgets){
            if (!eqsToDelete.containsKey(state)) continue;
            for (Object eqKey : eqsToDelete.get(state)) {
                deleteConstraintWithoutVars(eqKey);
            }
        }

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

    @Override
    protected void removeOldConstraints() throws IloException {
        ArrayList<Integer> cplexEqsToDelete = new ArrayList<>();
        ArrayList<Integer> cplexVarsToDelete = new ArrayList<>();
        HashSet<Pair<Integer, Integer>> toBeRemoved = new HashSet<>();
        for(GameState state: deletedGadgets){
            if (!eqsToDelete.containsKey(state)) continue;
            for (Object eqKey : eqsToDelete.get(state)){
                if (eqKey == null || eqToCplexEq.get(eqKey) == null) continue;
                int idx = eqToCplexEq.get(eqKey);
                if (!constraints.containsKey(eqKey)){
//                    System.out.println(eqKey);
                    continue;
                }
                for (Object var : constraints.get(eqKey).keySet()) {
                    if (varToCplexVar.get(var) == null){
//                        System.out.println("Is null! : " + var);
                        continue;
                    }
//                    toBeRemoved.add(new int[]{idx, varToCplexVar.get(var).intValue()});
                    toBeRemoved.add(new Pair<>(idx, varToCplexVar.get(var).intValue()));
//                    cplexEqsToDelete.add(idx);
//                    cplexVarsToDelete.add(varToCplexVar.get(var));
                }
//                constraints.remove(eqKey);
            }
        }
//        System.out.println(deletedConstraints);
//        System.out.println(createdConstraints);
        for(Object eqKey : deletedConstraints.keySet()){
            if (createdConstraints.contains(eqKey)) continue;
//            if (!createdConstraints.contains(eqKey)) {
                int idx = eqToCplexEq.get(eqKey);
                for (Object var : deletedConstraints.get(eqKey)) {
//                    toBeRemoved.add(new int[]{idx, varToCplexVar.get(var).intValue()});
                    if (varToCplexVar.get(var) == null){
//                        System.out.println(var);
                        continue;
                    }
                    toBeRemoved.add(new Pair<>(idx, varToCplexVar.get(var).intValue()));
//                    toBeRemoved.add(new Pair(idx, varToCplexVar.get(var)));
//                    cplexEqsToDelete.add(idx);
//                    cplexVarsToDelete.add(varToCplexVar.get(var));
                }
//                constraints.remove(eqKey);
//            }
//            else{
//                int idx = eqToCplexEq.get(eqKey);
//                for (int i = 0; i < matrix.getNcols(); i ++ ) {
////                    toBeRemoved.add(new int[]{idx, varToCplexVar.get(var).intValue()});
//                    toBeRemoved.add(new Pair<>(idx, i));
////                    toBeRemoved.add(new Pair(idx, varToCplexVar.get(var)));
////                    cplexEqsToDelete.add(idx);
////                    cplexVarsToDelete.add(varToCplexVar.get(var));
//                }
//            }
//            constraints.remove(eqKey);
        }
//        int[] rowsToRemove = new int[cplexEqsToDelete.size()];
//        int[] colsToRemove = new int[cplexVarsToDelete.size()];
//        for(int i = 0; i < rowsToRemove.length; i++){
//            rowsToRemove[i] = cplexEqsToDelete.get(i);
//            colsToRemove[i] = cplexVarsToDelete.get(i);
//        }
        int[] rowsToRemove = new int[toBeRemoved.size()];
        int[] colsToRemove = new int[toBeRemoved.size()];
        int i = 0;
        for(Pair<Integer, Integer> p : toBeRemoved){
            rowsToRemove[i] = p.getLeft();
            colsToRemove[i] = p.getRight();
            i++;
        }
//        System.out.println(Arrays.toString(rowsToRemove));
//        System.out.println(Arrays.toString(colsToRemove));
        long startTime = threadBean.getCurrentThreadCpuTime();
        if (rowsToRemove.length > 0)
            matrix.setNZs(rowsToRemove, colsToRemove, new double[rowsToRemove.length]);
        cplexConstraintsRemovingTime += threadBean.getCurrentThreadCpuTime() - startTime;
    }
}
