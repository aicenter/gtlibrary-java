package cz.agents.gtlibrary.algorithms.sequenceform.gensum.parammilp;

import cz.agents.gtlibrary.algorithms.sequenceform.refinements.quasiperfect.numbers.Rational;
import ilog.concert.IloNumVar;
import ilog.concert.IloRange;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

;

public class SimplexTable {

    protected Map<Object, Rational> objective;
    protected Map<Object, Map<Object, Rational>> constraints;
    protected Map<Object, Rational> constants;

    protected Map<Object, Integer> equationIndices;
    protected Map<Object, Integer> variableIndices;
    protected Map<Object, Integer> primalWatch;
    protected Map<Object, Integer> dualWatch;
    protected Set<Object> relaxableConstraints;

    protected Map<Object, Integer> constraintTypes;
    protected Map<Object, Rational> lb;
    protected Map<Object, Rational> ub;

    public SimplexTable() {
        constants = new LinkedHashMap<>();
        constraints = new LinkedHashMap<>();
        objective = new LinkedHashMap<>();

        equationIndices = new LinkedHashMap<>();
        variableIndices = new LinkedHashMap<>();
        primalWatch = new LinkedHashMap<>();
        dualWatch = new LinkedHashMap<>();
        relaxableConstraints = new HashSet<>();

        constraintTypes = new LinkedHashMap<>();
        lb = new LinkedHashMap<>();
        ub = new LinkedHashMap<>();
    }

    public SimplexTable(int m, int n) {
        constants = new LinkedHashMap<>(m);
        constraints = new LinkedHashMap<>(m);
        objective = new LinkedHashMap<>(n);

        equationIndices = new LinkedHashMap<>(m);
        variableIndices = new LinkedHashMap<>(n);
        primalWatch = new LinkedHashMap<>();
        dualWatch = new LinkedHashMap<>();
        relaxableConstraints = new HashSet<>();

        constraintTypes = new LinkedHashMap<>(m);
        lb = new LinkedHashMap<>(n);
        ub = new LinkedHashMap<>();
    }

    public Rational get(Object eqKey, Object varKey) {
        Rational value = constraints.get(eqKey).get(varKey);

        return value == null ? Rational.ZERO : value;
    }

    protected void updateEquationIndices(Object eqKey) {
        getEquationIndex(eqKey);
    }

    protected void updateVariableIndices(Object varKey) {
        getVariableIndex(varKey);
    }

    public void setObjective(Object varKey, Rational value) {
        objective.put(varKey, value);
        updateVariableIndices(varKey);
    }

    public void addToObjective(Object varKey, Rational value) {
        Rational oldValue = objective.get(varKey);

        objective.put(varKey, oldValue == null ? value : oldValue.add(value));
        updateVariableIndices(varKey);
    }

    public Rational getObjective(Object varKey) {
        Rational value = objective.get(varKey);

        return value == null ? Rational.ZERO : value;
    }

    public void setConstant(Object eqKey, Rational value) {
        constants.put(eqKey, value);
        updateEquationIndices(eqKey);
    }

    public double getConstant(Object eqKey) {
        Rational value = constants.get(eqKey);

        return value == null ? 0 : value.doubleValue();
    }

    public void setConstraint(Object eqKey, Object varKey, Rational value) {
        Map<Object, Rational> row = constraints.get(eqKey);

        if (row == null) {
            row = new LinkedHashMap<>();
            constraints.put(eqKey, row);
        }
        row.put(varKey, value);
        updateEquationIndices(eqKey);
        updateVariableIndices(varKey);
    }

    public void addToConstraint(Object eqKey, Object varKey, Rational value) {
        setConstraint(eqKey, varKey, get(eqKey, varKey).add(value));
    }

    public void substractFromConstraint(Object eqKey, Object varKey, Rational value) {
        setConstraint(eqKey, varKey, get(eqKey, varKey).subtract(value));
    }

    public int rowCount() {
        return constraints.size();
    }

    public int columnCount() {
        return variableIndices.size();
    }

    public int getEquationIndex(Object eqKey) {
        return getIndex(eqKey, equationIndices);
    }

    public int getVariableIndex(Object varKey) {
        return getIndex(varKey, variableIndices);
    }

    protected int getIndex(Object key, Map<Object, Integer> map) {
        Integer result = map.get(key);

        if (result == null) {
            result = map.size();
            map.put(key, result);
        }
        return result;
    }

    public void watchPrimalVariable(Object varKey, Object watchKey) {
        primalWatch.put(watchKey, getVariableIndex(varKey));
    }

    public void watchDualVariable(Object eqKey, Object watchKey) {
        dualWatch.put(watchKey, getEquationIndex(eqKey));
    }

    protected String[] getVariableNames() {
        String[] variableNames = new String[columnCount()];

        for (Object variable : variableIndices.keySet()) {
            variableNames[getVariableIndex(variable)] = variable.toString();
        }
        return variableNames;
    }

    protected Map<Object, IloRange> getWatchedDualVars(IloRange[] constraints) {
        Map<Object, IloRange> watchedDualVars = new LinkedHashMap<Object, IloRange>();

        for (Map.Entry<Object, Integer> entry : dualWatch.entrySet()) {
            watchedDualVars.put(entry.getKey(), constraints[entry.getValue()]);
        }
        return watchedDualVars;
    }

    protected Map<Object, IloNumVar> getWatchedPrimalVars(IloNumVar[] variables) {
        Map<Object, IloNumVar> watchedPrimalVars = new LinkedHashMap<Object, IloNumVar>();

        for (Map.Entry<Object, Integer> entry : primalWatch.entrySet()) {
            watchedPrimalVars.put(entry.getKey(), variables[entry.getValue()]);
        }
        return watchedPrimalVars;
    }

    protected int getConstraintType(Map.Entry<Object, Map<Object, Rational>> rowEntry) {
        Integer constraintType = constraintTypes.get(rowEntry.getKey());

        return constraintType == null ? 0 : constraintType;
    }


    /**
     * Mark constraint, which might cause infeasibility due to numeric instability
     *
     * @param eqKey
     */
    public void markRelaxableConstraint(Object eqKey) {
        relaxableConstraints.add(eqKey);
    }

    /**
     * Remove constraint from relaxable constraints
     *
     * @param eqKey
     */
    public void unmarkRelaxableConstraint(Object eqKey) {
        relaxableConstraints.remove(eqKey);
    }

    /**
     * Set constraint for equation represented by eqObject, default constraint is ge
     *
     * @param eqKey
     * @param type  0 ... le, 1 .. eq, 2 ... ge
     */
    public void setConstraintType(Object eqKey, int type) {
        constraintTypes.put(eqKey, type);
    }

    /**
     * Set lower bound for variable represented by varObject, default value is 0
     *
     * @param varKey
     * @param value
     */
    public void setLowerBound(Object varKey, Rational value) {
        lb.put(varKey, value);
    }

    /**
     * Set upper bound for variable represented by varObject, default value is POSITIVE_INFINITY
     *
     * @param varKey
     * @param value
     */
    public void setUpperBound(Object varKey, Rational value) {
        ub.put(varKey, value);
    }

    public void removeFromConstraint(Object eqKey, Object varKey) {
        Map<Object, Rational> row = constraints.get(eqKey);

        if (row != null)
            row.remove(varKey);
    }

    public ParamSimplexData toSimplex() {
        Rational[][] tableau = new Rational[rowCount() + 1][columnCount() + 1];

        addObjective(tableau);
        addConstraints(tableau);
        return new ParamSimplexData(tableau);
    }

    private void addConstraints(Rational[][] tableau) {
        for (Map.Entry<Object, Map<Object, Rational>> constraintEntry : constraints.entrySet()) {
            for (Map.Entry<Object, Rational> memberEntry : constraintEntry.getValue().entrySet()) {
                tableau[getEquationIndex(constraintEntry.getKey()) + 1][getVariableIndex(memberEntry.getKey())] = memberEntry.getValue();
            }
        }
        for (Map.Entry<Object, Rational> constantEntry : constants.entrySet()) {
            tableau[getEquationIndex(constantEntry.getKey()) + 1][columnCount()] = constantEntry.getValue();
        }
    }

    private void addObjective(Rational[][] tableau) {
        for (Map.Entry<Object, Rational> objectiveEntry : objective.entrySet()) {
            tableau[0][getVariableIndex(objectiveEntry.getKey())] = objectiveEntry.getValue();
        }
    }
}
