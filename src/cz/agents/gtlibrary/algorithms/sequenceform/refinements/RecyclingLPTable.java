package cz.agents.gtlibrary.algorithms.sequenceform.refinements;

import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPData;
import cz.agents.gtlibrary.interfaces.Sequence;
import ilog.concert.*;
import ilog.cplex.IloCplex;

import java.util.*;
import java.util.Map.Entry;

public class RecyclingLPTable extends LPTable {

    protected Map<Object, Double> newObjective;
    protected Map<Object, Map<Object, Double>> newConstraints;
    protected Map<Object, Map<Object, Double>> updatedConstraints;
    protected Set<Object> removedConstraints;
    protected Map<Object, Double> updatedConstants;

    protected IloObjective lpObj;
    protected IloRange[] lpConstraints;
    protected IloNumVar[] lpVariables;

    public RecyclingLPTable() {
        super();
        newConstraints = new LinkedHashMap<Object, Map<Object, Double>>();
        newObjective = new LinkedHashMap<Object, Double>();
        updatedConstraints = new LinkedHashMap<Object, Map<Object, Double>>();
        removedConstraints = new HashSet<Object>();
        updatedConstants = new HashMap<Object, Double>();
    }

    public RecyclingLPTable(int m, int n) {
        super(m, n);
        newConstraints = new LinkedHashMap<Object, Map<Object, Double>>(m);
        newObjective = new LinkedHashMap<Object, Double>(n);
        updatedConstraints = new LinkedHashMap<Object, Map<Object, Double>>();
        removedConstraints = new HashSet<Object>();
        updatedConstants = new HashMap<Object, Double>();
    }

    public void setObjective(Object varKey, double value) {
        if (Math.abs(value) < Double.MIN_VALUE)
            return;
        if (objective.put(varKey, value) == null)
            newObjective.put(varKey, value);
        updateVariableIndices(varKey);
    }

//    public void setConstraint(Object eqKey, Object varKey, double value) {
//        if (Math.abs(value) < Double.MIN_VALUE)
//            return;
//        Map<Object, Double> row = constraints.get(eqKey);
//
//        if (row == null) {
//            row = new LinkedHashMap<Object, Double>();
//            constraints.put(eqKey, row);
//            newConstraints.put(eqKey, row);
//            row.put(varKey, value);
//        } else {
//            if (newConstraints.containsKey(eqKey)) {
//                row.put(varKey, value);
//            } else {
//                Map<Object, Double> rowDiff = new LinkedHashMap<Object, Double>();
//
//                if (row.containsKey(varKey)) {
//                    if (Math.abs(value - row.get(varKey)) < 1e-10)
//                        return;
//                    rowDiff.put(varKey, value - row.get(varKey));
//                } else {
//                    rowDiff.put(varKey, value);
//                }
//                updatedConstraints.put(eqKey, rowDiff);
//                row.put(varKey, value);
//            }
//        }
//
//        updateEquationIndices(eqKey);
//        updateVariableIndices(varKey);
//    }

    public void setConstraint(Object eqKey, Object varKey, double value) {
//        if (Math.abs(value) < Double.MIN_VALUE)
//            return;
        Map<Object, Double> row = constraints.get(eqKey);

        if (row == null) {
            row = new LinkedHashMap<Object, Double>();
            constraints.put(eqKey, row);
            newConstraints.put(eqKey, row);
            row.put(varKey, value);
        } else {
            if (newConstraints.containsKey(eqKey)) {
                row.put(varKey, value);
            } else {
                if (row.containsKey(varKey)) {
                    if (Math.abs(value - row.get(varKey)) < 1e-10)
                        return;
                }
                Map<Object, Double> updatedRow = updatedConstraints.get(eqKey);

                if (updatedRow == null)
                    updatedRow = new HashMap<Object, Double>();
                updatedRow.put(varKey, value);
                updatedConstraints.put(eqKey, updatedRow);
                row.put(varKey, value);
            }
        }

        updateEquationIndices(eqKey);
        updateVariableIndices(varKey);
    }

    public void setConstraintIfNotPresent(Object eqKey, Object varKey, double value) {
        if (constraints.get(eqKey).containsKey(varKey))
            return;
        setConstraint(eqKey, varKey, value);

    }

    @Override
    public void setConstant(Object eqKey, double value) {
        updatedConstants.put(eqKey, value);
    }

    public LPData toCplex() throws IloException {
        double[] ub = getUpperBounds();
        double[] lb = getLowerBounds();
        String[] variableNames = getVariableNames();

        cplex.setParam(IloCplex.IntParam.RootAlg, CPLEXALG);
        cplex.setParam(IloCplex.IntParam.Threads, CPLEXTHREADS);
//        cplex.setParam(IloCplex.BooleanParam.Parallel, IloCplex.ParallelMode.Opportunistic);
//        cplex.setParam(IloCplex.DoubleParam.EpMrk, 0.99999);
        //		cplex.setParam(IloCplex.DoubleParam.BarEpComp, 1e-4);
        //		System.out.println("BarEpComp: " + cplex.getParam(IloCplex.DoubleParam.BarEpComp));
//        cplex.setParam(IloCplex.BooleanParam.NumericalEmphasis, true);
        cplex.setOut(null);
        lpVariables = updateVariables(variableNames, lb, ub);
        lpConstraints = addConstraints(lpVariables);

        addObjective(lpVariables);
        return new LPData(cplex, lpVariables, lpConstraints, getRelaxableConstraints(lpConstraints), getWatchedPrimalVars(lpVariables), getWatchedDualVars(lpConstraints));
    }

    protected IloNumVar[] updateVariables(String[] variableNames, double[] lb, double[] ub) throws IloException {
        if (lpVariables == null)
            return cplex.numVarArray(variableNames.length, lb, ub, variableNames);

        IloNumVar[] newVariables = new IloNumVar[variableNames.length];

        for (int i = 0; i < lpVariables.length; i++) {
            newVariables[i] = lpVariables[i];
        }
        for (int i = lpVariables.length; i < newVariables.length; i++) {
            newVariables[i] = cplex.numVar(lb[i], ub[i], variableNames[i]);
        }
        return newVariables;
    }

    protected IloRange[] addConstraints(IloNumVar[] x) throws IloException {
        IloRange[] cplexConstraints = createConstraintsFromLastIteration();

        for (Entry<Object, Map<Object, Double>> rowEntry : updatedConstraints.entrySet()) {
            assert rowEntry.getValue().get("t") == null || Math.abs(rowEntry.getValue().get("t")) == 1 || Math.abs(rowEntry.getValue().get("t")) == 0;
            modifyExistingConstraint(x, cplexConstraints, rowEntry, getEquationIndex(rowEntry.getKey()) - 1);
        }
        for (Object eqKey : removedConstraints) {
            int equationIndex = getEquationIndex(eqKey) - 1;

            cplex.remove(cplexConstraints[equationIndex]);
            cplexConstraints[equationIndex] = null;
        }
        updatedConstraints.clear();
        removedConstraints.clear();

        for (Entry<Object, Map<Object, Double>> rowEntry : newConstraints.entrySet()) {
            int equationIndex = getEquationIndex(rowEntry.getKey()) - 1;

            if (cplexConstraints[equationIndex] == null) {
                assert rowEntry.getValue().get("t") == null || rowEntry.getValue().get("t") == 1;
                createNewConstraint(x, cplexConstraints, rowEntry.getKey(), rowEntry.getValue(), equationIndex);
            } else {
                //				modifyExistingConstraint(x, cplexConstraints, rowEntry, equationIndex);
                assert false;
            }
        }
        updateConstants(cplexConstraints);
        updatedConstants.clear();
        newConstraints.clear();

        return cplexConstraints;
    }

    private void updateConstants(IloRange[] cplexConstraints) throws IloException {
        for (Entry<Object, Double> entry : updatedConstants.entrySet()) {
            updateConstant(cplexConstraints, entry.getKey(), entry.getValue());
        }
    }

    private void updateConstant(IloRange[] cplexConstraints, Object eqKey, Double constant) throws IloException {
        if (constraintTypes.get(eqKey) == 0)
            cplexConstraints[getEquationIndex(eqKey) - 1].setUB(constant);
        else if (constraintTypes.get(eqKey) == 2)
            cplexConstraints[getEquationIndex(eqKey) - 1].setLB(constant);
        else
            cplexConstraints[getEquationIndex(eqKey) - 1].setBounds(constant, constant);
    }

    private void modifyExistingConstraint(IloNumVar[] x, IloRange[] cplexConstraints, Entry<Object, Map<Object, Double>> rowEntry, int equationIndex) throws IloException {
        for (Entry<Object, Double> update : rowEntry.getValue().entrySet()) {
            cplex.setLinearCoef(cplexConstraints[equationIndex], x[getVariableIndex(update.getKey()) - 1], update.getValue());
        }
    }

    protected void createNewConstraint(IloNumVar[] x, IloRange[] cplexConstraints, Object key, Map<Object, Double> row, int equationIndex) throws IloException {
        IloLinearNumExpr rowExpr = createRowExpresion(x, row);
        Integer constraintType = getConstraintType(key);

        switch (constraintType) {
            case 0:
                cplexConstraints[equationIndex] = cplex.addLe(rowExpr, 0);
                break;
            case 1:
                cplexConstraints[equationIndex] = cplex.addEq(rowExpr, 0);
                break;
            case 2:
                cplexConstraints[equationIndex] = cplex.addGe(rowExpr, 0);
                break;
            default:
                break;
        }
    }

    protected IloRange[] createConstraintsFromLastIteration() {
        IloRange[] cplexConstraints = new IloRange[rowCount()];

        if (lpConstraints != null)
            for (int i = 0; i < lpConstraints.length; i++) {
                cplexConstraints[i] = lpConstraints[i];
            }
        return cplexConstraints;
    }

    protected int getConstraintType(Object eqKey) {
        Integer constraintType = constraintTypes.get(eqKey);

        return constraintType == null ? 0 : constraintType;
    }

    protected IloLinearNumExpr createRowExpresion(IloNumVar[] x, Map<Object, Double> row) throws IloException {
        IloLinearNumExpr rowExpr = cplex.linearNumExpr();

        for (Entry<Object, Double> memberEntry : row.entrySet()) {
            rowExpr.addTerm(memberEntry.getValue().doubleValue(), x[getVariableIndex(memberEntry.getKey()) - 1]);
        }
        return rowExpr;
    }

    protected void addObjective(IloNumVar[] x) throws IloException {
        IloLinearNumExpr objExpr = cplex.linearNumExpr();

        for (Entry<Object, Double> entry : newObjective.entrySet()) {
            objExpr.addTerm(entry.getValue(), x[getVariableIndex(entry.getKey()) - 1]);
        }
        if (lpObj == null)
            lpObj = cplex.addMaximize(objExpr);
        else
            cplex.addToExpr(lpObj, objExpr);
        newObjective.clear();
    }

    public void removeFromConstraint(Object eqKey, Object varKey) {
        Map<Object, Double> row = constraints.get(eqKey);

        if (row != null) {
            Double removedValue = row.remove(varKey);

            if (removedValue == null)
                return;
            if (row.isEmpty()) {
                constraints.remove(eqKey);
                removedConstraints.add(eqKey);
                updatedConstraints.remove(eqKey);
            } else {
                Map<Object, Double> updatedRow = updatedConstraints.get(eqKey);

                if (updatedRow == null)
                    updatedRow = new LinkedHashMap<Object, Double>();
                updatedRow.put(varKey, 0d);
                updatedConstraints.put(eqKey, updatedRow);
            }
        }

    }

    public void removeConstant(Sequence eqKey) {
        constants.put(eqKey, 0d);
        updatedConstants.put(eqKey, 0d);
    }
}
