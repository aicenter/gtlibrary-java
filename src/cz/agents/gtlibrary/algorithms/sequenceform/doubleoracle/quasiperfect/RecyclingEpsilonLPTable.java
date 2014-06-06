package cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.quasiperfect;

import cz.agents.gtlibrary.algorithms.sequenceform.refinements.quasiperfect.numbers.EpsilonReal;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.quasiperfect.EpsilonLPTable;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPData;
import cz.agents.gtlibrary.interfaces.Sequence;
import ilog.concert.*;
import ilog.cplex.IloCplex;

import java.util.*;
import java.util.Map.Entry;

public class RecyclingEpsilonLPTable extends EpsilonLPTable {

    protected Map<Object, EpsilonReal> newObjective;
    protected Map<Object, Map<Object, EpsilonReal>> newConstraints;
    protected Map<Object, Map<Object, EpsilonReal>> updatedConstraints;
    protected Set<Object> removedConstraints;
    protected Map<Object, EpsilonReal> updatedConstants;

    protected IloObjective lpObj;
    protected IloRange[] lpConstraints;
    protected IloNumVar[] lpVariables;

    protected IloCplex cplex;

    public RecyclingEpsilonLPTable() {
        super();
        newConstraints = new LinkedHashMap<Object, Map<Object, EpsilonReal>>();
        newObjective = new LinkedHashMap<Object, EpsilonReal>();
        updatedConstraints = new LinkedHashMap<Object, Map<Object, EpsilonReal>>();
        removedConstraints = new HashSet<Object>();
        updatedConstants = new HashMap<Object, EpsilonReal>();
        try {
            cplex = new IloCplex();
        } catch (IloException e) {
            e.printStackTrace();
        }
    }

    public RecyclingEpsilonLPTable(int m, int n) {
        super(m, n);
        newConstraints = new LinkedHashMap<Object, Map<Object, EpsilonReal>>(m);
        newObjective = new LinkedHashMap<Object, EpsilonReal>(n);
        updatedConstraints = new LinkedHashMap<Object, Map<Object, EpsilonReal>>();
        removedConstraints = new HashSet<Object>();
        updatedConstants = new HashMap<Object, EpsilonReal>();
    }

    public void setObjective(Object varKey, EpsilonReal value) {
        if (objective.put(varKey, value) == null)
            newObjective.put(varKey, value);
        updateVariableIndices(varKey);
    }

//    public void setConstraint(Object eqKey, Object varKey, double value) {
//        if (Math.abs(value) < EpsilonReal.MIN_VALUE)
//            return;
//        Map<Object, EpsilonReal> row = constraints.get(eqKey);
//
//        if (row == null) {
//            row = new LinkedHashMap<Object, EpsilonReal>();
//            constraints.put(eqKey, row);
//            newConstraints.put(eqKey, row);
//            row.put(varKey, value);
//        } else {
//            if (newConstraints.containsKey(eqKey)) {
//                row.put(varKey, value);
//            } else {
//                Map<Object, EpsilonReal> rowDiff = new LinkedHashMap<Object, EpsilonReal>();
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

    public void setConstraint(Object eqKey, Object varKey, EpsilonReal value) {
        Map<Object, EpsilonReal> row = constraints.get(eqKey);

        if (row == null) {
            row = new LinkedHashMap<Object, EpsilonReal>();
            constraints.put(eqKey, row);
            newConstraints.put(eqKey, row);
            row.put(varKey, value);
        } else {
            if (newConstraints.containsKey(eqKey)) {
                row.put(varKey, value);
            } else {
                Map<Object, EpsilonReal> updatedRow = updatedConstraints.get(eqKey);

                if (updatedRow == null)
                    updatedRow = new HashMap<Object, EpsilonReal>();
                updatedRow.put(varKey, value);
                updatedConstraints.put(eqKey, updatedRow);
                row.put(varKey, value);
            }
        }
        updateEquationIndices(eqKey);
        updateVariableIndices(varKey);
    }

    public void setConstraintIfNotPresent(Object eqKey, Object varKey, EpsilonReal value) {
        if (constraints.get(eqKey).containsKey(varKey))
            return;
        setConstraint(eqKey, varKey, value);

    }

    @Override
    public void setConstant(Object eqKey, EpsilonReal value) {
        updatedConstants.put(eqKey, value);
    }

    public LPData toCplex() throws IloException {
        double[] ub = getUpperBounds();
        double[] lb = getLowerBounds();
        String[] variableNames = getVariableNames();

//        cplex.setParam(IloCplex.IntParam.RootAlg, CPLEXALG);
//        cplex.setParam(IloCplex.IntParam.Threads, CPLEXTHREADS);
//        cplex.setParam(IloCplex.EpsilonRealParam.EpMrk, 0.99999);
        //		cplex.setParam(IloCplex.EpsilonRealParam.BarEpComp, 1e-4);
        //		System.out.println("BarEpComp: " + cplex.getParam(IloCplex.EpsilonRealParam.BarEpComp));
        cplex.setParam(IloCplex.BooleanParam.NumericalEmphasis, true);
        cplex.setOut(null);
        lpVariables = updateVariables(variableNames, lb, ub);
        lpConstraints = addConstraints(lpVariables);

        addObjective(lpVariables);
        return new LPData(cplex, lpVariables, lpConstraints, null, getWatchedPrimalVars(lpVariables), getWatchedDualVars(lpConstraints));
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

        for (Entry<Object, Map<Object, EpsilonReal>> rowEntry : updatedConstraints.entrySet()) {
            modifyExistingConstraint(x, cplexConstraints, rowEntry, getEqIndex(rowEntry.getKey()) - 1);
        }
        for (Object eqKey : removedConstraints) {
            int equationIndex = getEqIndex(eqKey) - 1;

            cplex.remove(cplexConstraints[equationIndex]);
            cplexConstraints[equationIndex] = null;
        }
        updatedConstraints.clear();
        removedConstraints.clear();

        for (Entry<Object, Map<Object, EpsilonReal>> rowEntry : newConstraints.entrySet()) {
            int equationIndex = getEqIndex(rowEntry.getKey()) - 1;

            if (cplexConstraints[equationIndex] == null) {
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
        for (Entry<Object, EpsilonReal> entry : updatedConstants.entrySet()) {
            updateConstant(cplexConstraints, entry.getKey(), entry.getValue());
        }
    }

    private void updateConstant(IloRange[] cplexConstraints, Object eqKey, EpsilonReal constant) throws IloException {
        if (constraintTypes.get(eqKey) == 0)
            cplexConstraints[getEqIndex(eqKey) - 1].setUB(constant.doubleValue());
        else if (constraintTypes.get(eqKey) == 2)
            cplexConstraints[getEqIndex(eqKey) - 1].setLB(constant.doubleValue());
        else
            cplexConstraints[getEqIndex(eqKey) - 1].setBounds(constant.doubleValue(), constant.doubleValue());
    }

    private void modifyExistingConstraint(IloNumVar[] x, IloRange[] cplexConstraints, Entry<Object, Map<Object, EpsilonReal>> rowEntry, int equationIndex) throws IloException {
        for (Entry<Object, EpsilonReal> update : rowEntry.getValue().entrySet()) {
            cplex.setLinearCoef(cplexConstraints[equationIndex], x[getVarIndex(update.getKey()) - 1], update.getValue().doubleValue());
        }
    }

    protected void createNewConstraint(IloNumVar[] x, IloRange[] cplexConstraints, Object key, Map<Object, EpsilonReal> row, int equationIndex) throws IloException {
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

    protected IloLinearNumExpr createRowExpresion(IloNumVar[] x, Map<Object, EpsilonReal> row) throws IloException {
        IloLinearNumExpr rowExpr = cplex.linearNumExpr();

        for (Entry<Object, EpsilonReal> memberEntry : row.entrySet()) {
            rowExpr.addTerm(memberEntry.getValue().doubleValue(), x[getVarIndex(memberEntry.getKey()) - 1]);
        }
        return rowExpr;
    }

    protected void addObjective(IloNumVar[] x) throws IloException {
        IloLinearNumExpr objExpr = cplex.linearNumExpr();

        for (Entry<Object, EpsilonReal> entry : newObjective.entrySet()) {
            objExpr.addTerm(entry.getValue().doubleValue(), x[getVarIndex(entry.getKey()) - 1]);
        }
        if (lpObj == null)
            lpObj = cplex.addMaximize(objExpr);
        else
            cplex.addToExpr(lpObj, objExpr);
        newObjective.clear();
    }

    public void removeFromConstraint(Object eqKey, Object varKey) {
        Map<Object, EpsilonReal> row = constraints.get(eqKey);

        if (row != null) {
            EpsilonReal removedValue = row.remove(varKey);

            if (removedValue == null)
                return;
            if (row.isEmpty()) {
                constraints.remove(eqKey);
                removedConstraints.add(eqKey);
                updatedConstraints.remove(eqKey);
            } else {
                Map<Object, EpsilonReal> updatedRow = updatedConstraints.get(eqKey);

                if (updatedRow == null)
                    updatedRow = new LinkedHashMap<Object, EpsilonReal>();
                updatedRow.put(varKey, EpsilonReal.ZERO);
                updatedConstraints.put(eqKey, updatedRow);
            }
        }

    }

    public void removeConstant(Sequence eqKey) {
        constants.put(eqKey, EpsilonReal.ZERO);
        updatedConstants.put(eqKey, EpsilonReal.ZERO);
    }
}
