package cz.agents.gtlibrary.algorithms.sequenceform.gensum.parammilp;

import cz.agents.gtlibrary.algorithms.sequenceform.gensum.parammilp.numbers.Arithmetic;
import cz.agents.gtlibrary.algorithms.sequenceform.gensum.parammilp.numbers.BigIntRational;
import cz.agents.gtlibrary.algorithms.sequenceform.gensum.parammilp.numbers.EpsilonPolynomial;
import cz.agents.gtlibrary.algorithms.sequenceform.gensum.parammilp.numbers.factory.EpsilonPolynomialFactory;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.quasiperfect.numbers.EpsilonReal;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.quasiperfect.numbers.Rational;
import ilog.concert.IloNumVar;
import ilog.concert.IloRange;

import java.util.*;

public class SimplexTable {

    protected EpsilonPolynomialFactory factory;

    private Set<Object> binaryVariables;
    private Map<Object, Object> binaryVarLimitFirstPhaseSlacks;
    private Map<Object, Object> binaryVarLimitConstraints;

    protected Map<Object, EpsilonPolynomial> objective;
    protected Map<Object, Map<Object, EpsilonPolynomial>> constraints;
    protected Map<Object, EpsilonPolynomial> constants;

    protected Map<Object, Integer> equationIndices;
    protected Map<Object, Integer> variableIndices;
    protected Map<Object, Integer> primalWatch;
    protected Map<Object, Integer> dualWatch;
    protected Set<Object> relaxableConstraints;

    protected Map<Object, Integer> constraintTypes;
    protected Map<Object, EpsilonPolynomial> lb;
    protected Map<Object, EpsilonPolynomial> ub;

    protected List<Integer> basis;
    protected Indices firstPhaseSlacks;
    protected Set<Object> firstPhaseSlackVariables;


    public SimplexTable(EpsilonPolynomialFactory factory) {
        this.factory = factory;

        constants = new LinkedHashMap<>();
        constraints = new LinkedHashMap<>();
        objective = new LinkedHashMap<>();

        equationIndices = new LinkedHashMap<>();
        variableIndices = new LinkedHashMap<>();
        primalWatch = new LinkedHashMap<>();
        dualWatch = new LinkedHashMap<>();
        relaxableConstraints = new HashSet<>();
        binaryVariables = new HashSet<>();
        binaryVarLimitFirstPhaseSlacks = new HashMap<>();
        binaryVarLimitConstraints = new HashMap<>();

        constraintTypes = new LinkedHashMap<>();
        lb = new LinkedHashMap<>();
        ub = new LinkedHashMap<>();
        basis = new ArrayList<>();
        firstPhaseSlacks = new Indices();
        firstPhaseSlackVariables = new HashSet<>();
    }

    public SimplexTable(int m, int n, EpsilonPolynomialFactory factory) {
        this.factory = factory;

        constants = new LinkedHashMap<>(m);
        constraints = new LinkedHashMap<>(m);
        objective = new LinkedHashMap<>(n);

        equationIndices = new LinkedHashMap<>(m);
        variableIndices = new LinkedHashMap<>(n);
        primalWatch = new LinkedHashMap<>();
        dualWatch = new LinkedHashMap<>();
        relaxableConstraints = new HashSet<>();
        binaryVariables = new HashSet<>();
        binaryVarLimitFirstPhaseSlacks = new HashMap<>();
        binaryVarLimitConstraints = new HashMap<>();

        constraintTypes = new LinkedHashMap<>(m);
        lb = new LinkedHashMap<>(n);
        ub = new LinkedHashMap<>(n);
        basis = new ArrayList<>(m);
        firstPhaseSlacks = new Indices();
        firstPhaseSlackVariables = new HashSet<>();
    }

    public EpsilonPolynomial get(Object eqKey, Object varKey) {
        EpsilonPolynomial value = constraints.get(eqKey).get(varKey);

        return value == null ? factory.zero() : value;
    }

    protected void updateEquationIndices(Object eqKey) {
        getEquationIndex(eqKey);
    }

    protected void updateVariableIndices(Object varKey) {
        getVariableIndex(varKey);
    }

    public void setObjective(Object varKey, EpsilonPolynomial value) {
        objective.put(varKey, value);
        updateVariableIndices(varKey);
    }

    public void setObjective(Object varKey, Arithmetic value) {
        setObjective(varKey, factory.create(value));
    }

    public void addToObjective(Object varKey, EpsilonPolynomial value) {
        EpsilonPolynomial oldValue = objective.get(varKey);

        objective.put(varKey, oldValue == null ? value : oldValue.add(value));
        updateVariableIndices(varKey);
    }

    public void addToObjective(Object varKey, Arithmetic arithmetic) {
        addToObjective(varKey, factory.create(arithmetic));
    }

    public EpsilonPolynomial getObjective(Object varKey) {
        EpsilonPolynomial value = objective.get(varKey);

        return value == null ? factory.zero() : value;
    }

    public void setConstant(Object eqKey, EpsilonPolynomial value) {
        constants.put(eqKey, value);
        updateEquationIndices(eqKey);
    }

    public void setConstant(Object eqKey, Arithmetic value) {
        setConstant(eqKey, factory.create(value));
    }

    public void setConstraint(Object eqKey, Object varKey, EpsilonPolynomial value) {
        Map<Object, EpsilonPolynomial> row = constraints.get(eqKey);

        if (row == null) {
            row = new LinkedHashMap<>();
            constraints.put(eqKey, row);
        }
        row.put(varKey, value);
        updateEquationIndices(eqKey);
        updateVariableIndices(varKey);
    }

    public void setConstraint(Object eqKey, Object varKey, Arithmetic value) {
        setConstraint(eqKey, varKey, factory.create(value));
    }

    public void addToConstraint(Object eqKey, Object varKey, EpsilonPolynomial value) {
        setConstraint(eqKey, varKey, get(eqKey, varKey).add(value));
    }

    public void substractFromConstraint(Object eqKey, Object varKey, EpsilonPolynomial value) {
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

    protected int getConstraintType(Map.Entry<Object, Map<Object, EpsilonPolynomial>> rowEntry) {
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
     * Set upper bound for variable represented by varObject, default reward is POSITIVE_INFINITY
     *
     * @param varKey
     * @param value
     */
    public void setUpperBound(Object varKey, EpsilonPolynomial value) {
        ub.put(varKey, value);
    }

    public void markAsBinary(Object varKey) {
        binaryVariables.add(varKey);
    }

    public void removeFromConstraint(Object eqKey, Object varKey) {
        Map<Object, EpsilonPolynomial> row = constraints.get(eqKey);

        if (row != null)
            row.remove(varKey);
    }

    public ParamSimplexData toSimplex() {
        EpsilonPolynomial[][] tableau = new EpsilonPolynomial[rowCount() + 1][columnCount() + 1];

        for (int i = 0; i < tableau.length; i++) {
            for (int j = 0; j < tableau[0].length; j++) {
                tableau[i][j] = factory.zero();
            }
        }
        addObjective(tableau);
        addConstraints(tableau);
        return new ParamSimplexData(tableau, basis, firstPhaseSlacks, variableIndices);
    }

    public ParamSimplexData toFirstPhaseSimplex() {
        EpsilonPolynomial[][] tableau = new EpsilonPolynomial[rowCount() + 1][columnCount() + 1];

        for (int i = 0; i < tableau.length; i++) {
            for (int j = 0; j < tableau[0].length; j++) {
                tableau[i][j] = factory.zero();
            }
        }
        addFirstPhaseObjective(tableau);
        addConstraints(tableau);
        return new ParamSimplexData(tableau, new ArrayList<>(basis), firstPhaseSlacks, new HashMap<>(variableIndices));
    }

    public GTFSimplexData toGtfSimplex() {
        EpsilonReal[][] tableau = new EpsilonReal[rowCount() + 1][columnCount() + 1];

        for (int i = 0; i < tableau.length; i++) {
            for (int j = 0; j < tableau[0].length; j++) {
                tableau[i][j] = EpsilonReal.ZERO;
            }
        }
        addFirstPhaseObjective(tableau);
        addConstraints(tableau);
        return new GTFSimplexData(tableau, new ArrayList<>(basis), firstPhaseSlacks);
    }

    private void addConstraints(EpsilonReal[][] tableau) {
        for (Map.Entry<Object, Map<Object, EpsilonPolynomial>> constraintEntry : constraints.entrySet()) {
            for (Map.Entry<Object, EpsilonPolynomial> memberEntry : constraintEntry.getValue().entrySet()) {
                tableau[getEquationIndex(constraintEntry.getKey()) + 1][getVariableIndex(memberEntry.getKey()) + 1] = getEpsilonReal(memberEntry.getValue());
            }
        }
        for (Map.Entry<Object, EpsilonPolynomial> constantEntry : constants.entrySet()) {
            tableau[getEquationIndex(constantEntry.getKey()) + 1][0] = getEpsilonReal(constantEntry.getValue());
        }
    }

    private EpsilonReal getEpsilonReal(EpsilonPolynomial value) {
        Rational[] polynomial = new Rational[value.getPolynomial().length];

        for (int i = 0; i < polynomial.length; i++) {
            BigIntRational bigIntValue = (BigIntRational) value.getPolynomial()[i];

            polynomial[i] = new Rational(bigIntValue.getNumerator(), bigIntValue.getDenominator());
        }
        return new EpsilonReal(polynomial);
    }

    private void addFirstPhaseObjective(EpsilonReal[][] tableau) {
        for (Integer member : firstPhaseSlacks.getColumns()) {
            tableau[0][member + 1] = EpsilonReal.ONE.negate();
        }
    }

    private void addFirstPhaseObjective(EpsilonPolynomial[][] tableau) {
        for (Integer member : firstPhaseSlacks.getColumns()) {
            tableau[0][member] = factory.one();
        }
    }

    public void addSecondPhaseObjective(EpsilonPolynomial[][] tableau) {
        for (int i = 0; i < tableau[0].length; i++) {
            tableau[0][i] = factory.zero();
        }
        for (Map.Entry<Object, EpsilonPolynomial> entry : objective.entrySet()) {
            tableau[0][getVariableIndex(entry.getKey())] = entry.getValue().negate();
        }
    }

    private void addConstraints(EpsilonPolynomial[][] tableau) {
        for (Map.Entry<Object, Map<Object, EpsilonPolynomial>> constraintEntry : constraints.entrySet()) {
            for (Map.Entry<Object, EpsilonPolynomial> memberEntry : constraintEntry.getValue().entrySet()) {
                tableau[getEquationIndex(constraintEntry.getKey()) + 1][getVariableIndex(memberEntry.getKey())] = memberEntry.getValue();
            }
        }
        for (Map.Entry<Object, EpsilonPolynomial> constantEntry : constants.entrySet()) {
            tableau[getEquationIndex(constantEntry.getKey()) + 1][columnCount()] = constantEntry.getValue();
        }
    }

    private void addObjective(EpsilonPolynomial[][] tableau) {
        for (Map.Entry<Object, EpsilonPolynomial> objectiveEntry : objective.entrySet()) {
            tableau[0][getVariableIndex(objectiveEntry.getKey())] = objectiveEntry.getValue();
        }
    }

    public void setInitBasis(Object eqKey, Object varKey) {
        assert getEquationIndex(eqKey) == basis.size();
        basis.add(getVariableIndex(varKey));
    }

    public void markFirstPhaseSlack(Object eqKey, Object varKey) {
        firstPhaseSlacks.addEntry(getEquationIndex(eqKey) + 1, getVariableIndex(varKey));
        firstPhaseSlackVariables.add(varKey);
    }

    public Map<Object, Integer> getVariableIndices() {
        return variableIndices;
    }

    public Set<Object> getBinaryVariables() {
        return binaryVariables;
    }

    public Map<Object, Object> getBinaryVarLimitFirstPhaseSlacks() {
        return binaryVarLimitFirstPhaseSlacks;
    }

    public void markBinaryVariableLimitConstraint(Object eqKey, Object firstPhaseSlackVarKey, Object binaryVarKey) {
        binaryVarLimitFirstPhaseSlacks.put(eqKey, firstPhaseSlackVarKey);
        binaryVarLimitConstraints.put(binaryVarKey, eqKey);
    }

    public void removeFirstPhaseSlack(Object eqKey, Object varKey) {
        firstPhaseSlackVariables.remove(varKey);
        firstPhaseSlacks.removeEntry(getEquationIndex(eqKey) + 1, getVariableIndex(varKey));
    }

    public Indices getFirstPhaseSlacks() {
        return firstPhaseSlacks;
    }

    public Map<Object, EpsilonPolynomial> getObjective() {
        return objective;
    }

    public Object getBinaryLimitSlackOf(Object eqKey) {
        return binaryVarLimitFirstPhaseSlacks.get(eqKey);
    }

    public Object getLimitConstraintOf(Object varKey) {
        return binaryVarLimitConstraints.get(varKey);
    }

}
