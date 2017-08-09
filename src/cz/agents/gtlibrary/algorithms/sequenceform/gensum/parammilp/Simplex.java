package cz.agents.gtlibrary.algorithms.sequenceform.gensum.parammilp;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.gensum.GenSumSequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.gensum.SolverResult;
import cz.agents.gtlibrary.algorithms.sequenceform.gensum.parammilp.numbers.EpsilonPolynomial;
import cz.agents.gtlibrary.algorithms.sequenceform.gensum.parammilp.numbers.factory.BigIntRationalFactory;
import cz.agents.gtlibrary.algorithms.sequenceform.gensum.parammilp.numbers.factory.EpsilonPolynomialFactory;
import cz.agents.gtlibrary.domain.aceofspades.AoSExpander;
import cz.agents.gtlibrary.domain.aceofspades.AoSGameInfo;
import cz.agents.gtlibrary.domain.aceofspades.AoSGameState;
import cz.agents.gtlibrary.domain.informeraos.InformerAoSExpander;
import cz.agents.gtlibrary.domain.informeraos.InformerAoSGameInfo;
import cz.agents.gtlibrary.domain.informeraos.InformerAoSGameState;
import cz.agents.gtlibrary.domain.poker.kuhn.KPGameInfo;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerExpander;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerGameState;
import cz.agents.gtlibrary.experimental.utils.UtilityCalculator;
import cz.agents.gtlibrary.iinodes.ArrayListSequenceImpl;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.strategy.NoMissingSeqStrategy;
import cz.agents.gtlibrary.utils.Pair;
import cz.agents.gtlibrary.utils.io.GambitEFG;

import java.util.*;

public class Simplex implements Algorithm {

    public static void main(String[] args) {
        runAoS();
//        runKuhnPoker();
//        runIAoS();
    }

    protected static void runIAoS() {
        GameState root = new InformerAoSGameState();
        GenSumSequenceFormConfig config = new GenSumSequenceFormConfig();
        Expander<SequenceInformationSet> expander = new InformerAoSExpander<>(config);

        GeneralSumGameBuilder.buildWithUtilityShift(root, config, expander, 10);
        Simplex simplex = new Simplex(root.getAllPlayers(), config, new EpsilonPolynomialFactory(new BigIntRationalFactory()), new InformerAoSGameInfo());

        GambitEFG writer = new GambitEFG();

        writer.write("IAoS.gbt", root, expander);
        SolverResult result = simplex.compute();
        UtilityCalculator calculator = new UtilityCalculator(root, expander);
//
        System.out.println(calculator.computeUtility(new NoMissingSeqStrategy(result.p1RealPlan), new NoMissingSeqStrategy(result.p2RealPlan)));

        System.out.println("------------------------");
        printRealPlan(result.p1RealPlan);
        printRealPlan(result.p2RealPlan);
    }

    protected static void runAoS() {
        GameState root = new AoSGameState();
        GenSumSequenceFormConfig config = new GenSumSequenceFormConfig();
        Expander<SequenceInformationSet> expander = new AoSExpander<>(config);

        GeneralSumGameBuilder.buildWithUtilityShift(root, config, expander, 2);
        Simplex simplex = new Simplex(root.getAllPlayers(), config, new EpsilonPolynomialFactory(new BigIntRationalFactory()), new AoSGameInfo());

        SolverResult result = simplex.compute();
        UtilityCalculator calculator = new UtilityCalculator(root, expander);

        System.out.println(calculator.computeUtility(new NoMissingSeqStrategy(result.p1RealPlan), new NoMissingSeqStrategy(result.p2RealPlan)));

        System.out.println("------------------------");
        printRealPlan(result.p1RealPlan);
        printRealPlan(result.p2RealPlan);
    }

    protected static void runKuhnPoker() {
        GameState root = new KuhnPokerGameState();
        GenSumSequenceFormConfig config = new GenSumSequenceFormConfig();
        Expander<SequenceInformationSet> expander = new KuhnPokerExpander<>(config);

        GeneralSumGameBuilder.buildWithUtilityShift(root, config, expander, 20);
        Simplex simplex = new Simplex(root.getAllPlayers(), config, new EpsilonPolynomialFactory(new BigIntRationalFactory()), new KPGameInfo());
        SolverResult result = simplex.compute();
        UtilityCalculator calculator = new UtilityCalculator(root, expander);

        System.out.println(calculator.computeUtility(new NoMissingSeqStrategy(result.p1RealPlan), new NoMissingSeqStrategy(result.p2RealPlan)));

        System.out.println("------------------------");
        printRealPlan(result.p1RealPlan);
        printRealPlan(result.p2RealPlan);
    }

    public static final int M = 1000000;

    protected Player[] players;
    protected GenSumSequenceFormConfig config;
    protected SimplexTable lpTable;
    protected EpsilonPolynomialFactory factory;
    protected GameInfo info;

    public Simplex(Player[] players, GenSumSequenceFormConfig config, EpsilonPolynomialFactory factory, GameInfo info) {
        this.players = players;
        this.config = config;
        this.factory = factory;
        this.info = info;
        lpTable = new SimplexTable(factory);
    }

    @Override
    public SolverResult compute() {
        build();
        SimplexSolverResult result = solveFirstPhase();

//        result.tableau = lpTable.toFirstPhaseSimplex().tableau;
        if (!lpTable.getObjective().isEmpty()) {
            lpTable.addSecondPhaseObjective(result.tableau);
            removeFirstPhaseSlacks(result);
            result = solveSecondPhase(factory.bigM().negate(), toParamSimplexData(result));
        }
        return toSolverResult(result);
    }

    private void removeFirstPhaseSlacks(SimplexSolverResult result) {
        Set<Integer> blackList = new HashSet<>(result.firstPhaseSlacks.getColumns());
        EpsilonPolynomial[][] secondPhaseTableau = new EpsilonPolynomial[result.tableau.length][result.tableau[0].length - blackList.size()];
        int[] differenceArray = new int[result.tableau[0].length - 1];
        int offset = 0;

        for (int j = 0; j < result.tableau[0].length - 1; j++) {
            if (blackList.contains(j))
                offset++;
            else
                for (int i = 0; i < result.tableau.length; i++) {
                    secondPhaseTableau[i][j - offset] = result.tableau[i][j];
                }
            differenceArray[j] = offset;
        }
        for (int i = 0; i < result.tableau.length; i++) {
            secondPhaseTableau[i][secondPhaseTableau[0].length - 1] = result.tableau[i][result.tableau[0].length - 1];
        }
        result.tableau = secondPhaseTableau;

        removeFirstPhaseSlacksFromBasis(result, differenceArray);
        removeFirstPhaseSlacksFromVariableIndices(result, blackList, differenceArray);
    }

    private void removeFirstPhaseSlacksFromVariableIndices(SimplexSolverResult result, Set<Integer> blackList, int[] differenceArray) {
        for (Map.Entry<Object, Integer> entry : lpTable.getVariableIndices().entrySet()) {
            if (blackList.contains(entry.getValue()))
                result.variableIndices.remove(entry.getKey());
            else
                result.variableIndices.put(entry.getKey(), entry.getValue() - differenceArray[entry.getValue()]);
        }
    }

    private void removeFirstPhaseSlacksFromBasis(SimplexSolverResult result, int[] differenceArray) {
        for (int i = 0; i < result.basis.size(); i++) {
            int oldMember = result.basis.get(i);

            result.basis.set(i, oldMember - differenceArray[oldMember]);
        }
    }

    private ParamSimplexData toParamSimplexData(SimplexSolverResult result) {
        return new ParamSimplexData(result.tableau, result.basis, null, result.variableIndices);
    }

    protected SolverResult toSolverResult(SimplexSolverResult solve) {
        return new SolverResult(solve.p1Rp, solve.p2Rp, 0);
    }

    protected SimplexSolverResult solveFirstPhase() {
        SimplexSolverResult currentResult = runFirstPhaseSimplex();

        if (currentResult.value.compareTo(factory.zero()) > 0)
            return null;
        Object nonBinaryVarKey = getNonBinaryVariable(currentResult);

        if(nonBinaryVarKey == null)
            return currentResult;
        Object binaryVarLimitConstraintKey = lpTable.getLimitConstraintOf(nonBinaryVarKey);
        Object slackKey = lpTable.getBinaryLimitSlackOf(binaryVarLimitConstraintKey);

        lpTable.markFirstPhaseSlack(binaryVarLimitConstraintKey, slackKey);
        currentResult = solveFirstPhase();
        if (currentResult != null) {
            revertChanges(binaryVarLimitConstraintKey, slackKey, nonBinaryVarKey, currentResult);
            return currentResult;
        }
        lpTable.setConstant(binaryVarLimitConstraintKey, factory.zero());
        currentResult = solveFirstPhase();
        revertChanges(binaryVarLimitConstraintKey, slackKey, nonBinaryVarKey, currentResult);
        return currentResult;
    }

    private Object getNonBinaryVariable(SimplexSolverResult currentResult) {
        for (Object binaryVariable : lpTable.getBinaryVariables()) {
            EpsilonPolynomial value = currentResult.variableValues.get(binaryVariable);

            if(!value.isZero() && !value.isOne())
                return binaryVariable;
        }
        return null;
    }

    private SimplexSolverResult solveSecondPhase(EpsilonPolynomial currentBest, ParamSimplexData paramSimplexData) {
        SimplexSolverResult currentResult = runSecondPhaseSimplex(paramSimplexData.copy());
        Object nonBinaryVarKey = getNonBinaryVariable(currentResult);

        if (nonBinaryVarKey == null && currentResult.value.compareTo(currentBest) > 0)
            return currentResult;
        if (currentResult.value.compareTo(currentBest) <= 0)
            return null;
        Object binaryVarLimitConstraintKey = lpTable.getLimitConstraintOf(nonBinaryVarKey);
        Object slackKey = lpTable.getBinaryLimitSlackOf(binaryVarLimitConstraintKey);

        paramSimplexData.tableau[lpTable.getEquationIndex(binaryVarLimitConstraintKey) + 1][paramSimplexData.variableIndices.get(slackKey)] = factory.zero();
        SimplexSolverResult recursiveResult = solveSecondPhase(currentBest, paramSimplexData);

        if (currentResult != null)
            currentBest = currentResult.value;
        paramSimplexData.tableau[lpTable.getEquationIndex(binaryVarLimitConstraintKey) + 1][paramSimplexData.tableau[0].length] = factory.zero();
        SimplexSolverResult recursiveResult1 = solveSecondPhase(currentBest, paramSimplexData);

        revertChanges(paramSimplexData, binaryVarLimitConstraintKey, slackKey, nonBinaryVarKey);
        return chooseMax(recursiveResult, recursiveResult1);
    }

    private void revertChanges(ParamSimplexData paramSimplexData, Object binaryVarLimitConstraintKey, Object slackKey, Object nonBinaryVarKey) {
        paramSimplexData.tableau[lpTable.getEquationIndex(binaryVarLimitConstraintKey) + 1][paramSimplexData.variableIndices.get(slackKey)] = factory.one();
        paramSimplexData.tableau[lpTable.getEquationIndex(binaryVarLimitConstraintKey) + 1][paramSimplexData.tableau[0].length] = factory.one();
    }

    private void revertChanges(Object eqKey, Object varKey, Object nonBinaryVarKey, SimplexSolverResult currentResult) {
        lpTable.removeFirstPhaseSlack(eqKey, varKey);
        lpTable.markBinaryVariableLimitConstraint(eqKey, varKey, nonBinaryVarKey);
        lpTable.setConstant(eqKey, factory.one());
//        currentResult.firstPhaseSlacks.removeEntry(lpTable.getEquationIndex(eqKey) + 1, lpTable.getVariableIndex(varKey));
        currentResult.tableau[lpTable.getEquationIndex(eqKey) + 1][currentResult.tableau[0].length - 1] = factory.one();
    }

    private SimplexSolverResult chooseMax(SimplexSolverResult result, SimplexSolverResult result1) {
        return result.value.compareTo(result1.value) > 0 ? result : result1;
    }

    protected SimplexSolverResult choose(SimplexSolverResult currentResult, SimplexSolverResult result0, SimplexSolverResult result1) {
        SimplexSolverResult min = currentResult;

        if (min.value.compareTo(result0.value) > 0)
            min = result0;
        if (min.value.compareTo(result1.value) > 0)
            min = result1;
        return min;
    }

    protected SimplexSolverResult runFirstPhaseSimplex() {
        ParamSimplexData data = lpTable.toFirstPhaseSimplex();

        subtractFromFirstPhaseObjective(data);
        runSimplex(data);
        Map<Object, EpsilonPolynomial> variableValues = getVariableValues(data);
        Map<Sequence, Double> p1Rp = getRealizationPlanFor(players[0], variableValues);
        Map<Sequence, Double> p2Rp = getRealizationPlanFor(players[1], variableValues);
        EpsilonPolynomial value = data.tableau[0][data.tableau[0].length - 1].negate();

        if (allBinary(variableValues) && value.getPolynomial()[0].isZero()) {
            printVariableValues(variableValues);
            Indices slacksInBasis = getFirstPhaseSlacksInBasis(data.basis);

            if (!slacksInBasis.isEmpty())
                fixBasis(data, slacksInBasis);
            variableValues = getVariableValues(data);
            p1Rp = getRealizationPlanFor(players[0], variableValues);
            p2Rp = getRealizationPlanFor(players[1], variableValues);
            value = data.tableau[0][data.tableau[0].length - 1].negate();
            assert getFirstPhaseSlacksInBasis(data.basis).isEmpty();
            System.out.println("***************************");
            System.out.println("First phase results: ");
            System.out.println("reward: " + value);
            printRealPlan(p1Rp);
            printRealPlan(p2Rp);
        }
        return new SimplexSolverResult(p1Rp, p2Rp, value, data.tableau, data.basis, variableValues, data.firstPhaseSlacks, data.variableIndices);
    }

    private void subtractFromFirstPhaseObjective(ParamSimplexData data) {
        for (int i = 0; i < data.firstPhaseSlacks.getRows().size(); i++) {
            Integer member = data.firstPhaseSlacks.getRows().get(i);

            assert data.tableau[0][data.firstPhaseSlacks.getColumns().get(i)].equals(factory.one());
            assert oneAtZerosOtherwiseWithoutObj(data.tableau, data.firstPhaseSlacks.getRows().get(i), data.firstPhaseSlacks.getColumns().get(i));
            for (int j = 0; j < data.tableau[0].length; j++) {
                data.tableau[0][j] = data.tableau[0][j].subtract(data.tableau[member][j]);
            }
            assert oneAtZerosOtherwise(data.tableau, data.firstPhaseSlacks.getRows().get(i), data.firstPhaseSlacks.getColumns().get(i));
        }
    }

    protected SimplexSolverResult runSecondPhaseSimplex(ParamSimplexData data) {
        subtractFromSecondPhaseObjective(data);
        runSimplex(data);
        Map<Object, EpsilonPolynomial> variableValues = getVariableValues(data);
        Map<Sequence, Double> p1Rp = getRealizationPlanFor(players[0], variableValues);
        Map<Sequence, Double> p2Rp = getRealizationPlanFor(players[1], variableValues);
        EpsilonPolynomial value = data.tableau[0][data.tableau[0].length - 1].negate();

        if (allBinary(variableValues)) {
            printVariableValues(variableValues);
            System.out.println("***************************");
            System.out.println("Second phase results: ");
            System.out.println("reward: " + value);
            printRealPlan(p1Rp);
            printRealPlan(p2Rp);
        }
        return new SimplexSolverResult(p1Rp, p2Rp, value, data.tableau, data.basis, variableValues, data.firstPhaseSlacks, data.variableIndices);
    }

    private void subtractFromSecondPhaseObjective(ParamSimplexData data) {
        for (int row = 1; row <= data.basis.size(); row++) {
            int column = data.basis.get(row - 1);

            assert oneAtZerosOtherwiseWithoutObj(data.tableau, row, column);
            EpsilonPolynomial tempValue = data.tableau[0][column];

            for (int i = 0; i < data.tableau[0].length; i++) {
                data.tableau[0][i] = data.tableau[0][i].subtract(data.tableau[row][i].multiply(tempValue));
            }
            assert oneAtZerosOtherwise(data.tableau, row, column);
        }
    }

    private void runSimplex(ParamSimplexData data) {
        while (hasNegativeEntryInObjective(data.tableau)) {
            int enteringVarIndex = chooseEnteringVarIndex(data.tableau);
            int leavingVarRow = chooseLeavingVarRowBland(data.tableau, data.basis, enteringVarIndex);

            assert oneAtZerosOtherwise(data.tableau, leavingVarRow, data.basis.get(leavingVarRow - 1));
            updateBasis(data.basis, enteringVarIndex, leavingVarRow);
            updateTableau(data, enteringVarIndex, leavingVarRow);
        }
    }

    private static void printRealPlan(Map<Sequence, Double> p1Rp) {
        for (Map.Entry<Sequence, Double> entry : p1Rp.entrySet()) {
            if (entry.getValue() > 0)
                System.out.println(entry);
        }
    }

    private void printVariableValues(Map<Object, EpsilonPolynomial> variableValues) {
        System.out.println("------------------------");
        for (Map.Entry<Object, EpsilonPolynomial> entry : variableValues.entrySet()) {
            if (!entry.getValue().isZero())
                System.out.println(entry);
        }
    }

    private void fixBasis(ParamSimplexData data, Indices slacksInBasis) {
        System.out.println("First phase slack in basis");
        for (int i = 0; i < slacksInBasis.size(); i++) {
            int leavingVarRow = slacksInBasis.getRows().get(i);
            int enteringVarIndex = findVarIndexWithNonzeroCoefInRow(data.tableau, leavingVarRow, slacksInBasis.getColumns().get(i));

            if (enteringVarIndex != -1) {
                updateBasis(data.basis, enteringVarIndex, leavingVarRow);
                updateTableau(data, enteringVarIndex, leavingVarRow);
            } else {
                System.err.println("No suitable replacement");
            }
        }
        assert getFirstPhaseSlacksInBasis(data.basis).isEmpty();
    }

    private int findVarIndexWithNonzeroCoefInRow(EpsilonPolynomial[][] tableau, int row, int forbiddenIndex) {
        for (int i = 0; i < tableau[0].length - 1; i++) {
            if (i != forbiddenIndex)
                if (!tableau[row][i].isZero())
                    return i;
        }
        return -1;
    }

    private void updateTableau(ParamSimplexData data, int enteringVarIndex, int leavingVarRow) {
        EpsilonPolynomial enteringVariableValue = data.tableau[leavingVarRow][enteringVarIndex];

//        assert enteringVariableValue.compareTo(factory.zero()) > 0;
        if (!enteringVariableValue.isOne())
            for (int i = 0; i < data.tableau[0].length; i++) {
                data.tableau[leavingVarRow][i] = data.tableau[leavingVarRow][i].divide(enteringVariableValue);
            }
        assert data.tableau[leavingVarRow][enteringVarIndex].isOne();
        for (int i = 0; i < data.tableau.length; i++) {
            if (i == leavingVarRow)
                continue;
            EpsilonPolynomial tempValue = data.tableau[i][enteringVarIndex];

            for (int j = 0; j < data.tableau[0].length; j++) {
                data.tableau[i][j] = data.tableau[i][j].subtract(data.tableau[leavingVarRow][j].multiply(tempValue));
            }
        }
        for (int i = 0; i < data.basis.size(); i++) {
            assert oneAtZerosOtherwise(data.tableau, i + 1, data.basis.get(i));
        }
    }

    private Indices getFirstPhaseSlacksInBasis(List<Integer> basis) {
        Indices slacksInBasis = new Indices();

        for (Integer slackVariableIndex : lpTable.getFirstPhaseSlacks().getColumns()) {
            int rowIndex = basis.lastIndexOf(slackVariableIndex);

            if (rowIndex != -1)
                slacksInBasis.addEntry(rowIndex + 1, slackVariableIndex);
        }
        return slacksInBasis;
    }

    protected boolean allBinary(Map<Object, EpsilonPolynomial> variableValues) {
        for (Object binaryVariable : lpTable.getBinaryVariables()) {
            EpsilonPolynomial value = variableValues.get(binaryVariable);

            if (!value.isOne() && !value.isZero())
                return false;
        }
        return true;
    }

    protected Map<Object, EpsilonPolynomial> getVariableValues(ParamSimplexData data) {
        Map<Integer, EpsilonPolynomial> result = new HashMap<>(data.basis.size());

        for (int i = 0; i < data.basis.size(); i++) {
            result.put(data.basis.get(i), data.tableau[i + 1][data.tableau[0].length - 1]);
        }
        Map<Object, EpsilonPolynomial> variableValues = new HashMap<>(result.size());

        for (Map.Entry<Object, Integer> entry : data.variableIndices.entrySet()) {
            EpsilonPolynomial value = result.get(entry.getValue());

            if (value != null)
                variableValues.put(entry.getKey(), value);
            else
                variableValues.put(entry.getKey(), factory.zero());
        }
        return variableValues;
    }

    protected boolean oneAtZerosOtherwiseWithoutObj(EpsilonPolynomial[][] tableau, Integer row, Integer column) {
        for (int i = 1; i < tableau.length; i++) {
            if (i != row) {
                if (!tableau[i][column].isZero())
                    return false;
            } else {
                if (!tableau[i][column].isOne())
                    return false;
            }
        }
        return true;
    }

    protected boolean oneAtZerosOtherwise(EpsilonPolynomial[][] tableau, Integer row, Integer column) {
        for (int i = 0; i < tableau.length; i++) {
            if (i != row) {
                if (!tableau[i][column].isZero())
                    return false;
            } else {
                if (!tableau[i][column].isOne())
                    return false;
            }
        }
        return true;
    }

    protected void printColumn(EpsilonPolynomial[][] tableau, int enteringVarIndex, String label) {
        System.out.print(label + ": [");
        for (int i = 0; i < tableau.length; i++) {
            System.out.print(tableau[i][enteringVarIndex] + ", ");
        }
        System.out.print("]");
        System.out.println();
    }

    protected Map<InformationSet, Double> getInformationSetValues(Map<Integer, EpsilonPolynomial> result) {
        Map<InformationSet, Double> values = new HashMap<>();

        for (InformationSet informationSet : config.getAllInformationSets().values()) {
            EpsilonPolynomial value = result.get(lpTable.getVariableIndex(informationSet));

            if (value != null)
                values.put(informationSet, value.getPolynomial()[0].doubleValue());
        }
        return values;
    }

    protected Map<Sequence, Double> getRealizationPlanFor(Player player, Map<Object, EpsilonPolynomial> result) {
        Map<Sequence, Double> rp = new HashMap<>();

        for (Sequence sequence : config.getSequencesFor(player)) {
            rp.put(sequence, result.get(sequence).getPolynomial()[0].doubleValue());
        }
        return rp;
    }

    protected int chooseLeavingVarRow(EpsilonPolynomial[][] tableau, int enteringVarIndex) {
        EpsilonPolynomial min = null;
        int minIndex = -1;

        for (int i = 1; i < tableau.length; i++) {
            if (tableau[i][enteringVarIndex].compareTo(factory.zero()) > 0) {
                EpsilonPolynomial fraction = tableau[i][tableau[0].length - 1].divide(tableau[i][enteringVarIndex]);

                if (min == null || min.compareTo(fraction) > 0) {
                    min = fraction;
                    minIndex = i;
                }
            }
        }
        return minIndex;
    }

    public int chooseLeavingVarRowBland(EpsilonPolynomial[][] tableau, List<Integer> basis, int enteringVarIndex) {
        EpsilonPolynomial min = null;
        int basisVariableIndex = -1;
        int minIndex = -1;

        for (int i = 1; i < tableau.length; i++) {
            if (tableau[i][enteringVarIndex].compareTo(factory.zero()) > 0) {
                EpsilonPolynomial fraction = tableau[i][tableau[0].length - 1].divide(tableau[i][enteringVarIndex]);

                if (min == null || min.compareTo(fraction) > 0 || (min.compareTo(fraction) == 0 && basisVariableIndex > basis.get(i - 1))) {
                    min = fraction;
                    minIndex = i;
                    basisVariableIndex = basis.get(i - 1);
                }
            }
        }
        return minIndex;
    }

    protected void updateBasis(List<Integer> basis, int enteringVarIndex, int leavingVarRow) {
        basis.set(leavingVarRow - 1, enteringVarIndex);
    }

    protected int chooseEnteringVarIndex(EpsilonPolynomial[][] tableau) {
        EpsilonPolynomial min = null;
        int minIndex = -1;

        for (int i = 0; i < tableau[0].length - 1; i++) {
            if (min == null || min.compareTo(tableau[0][i]) > 0) {
                min = tableau[0][i];
                minIndex = i;
            }
        }
        return minIndex;
    }

    protected boolean hasNegativeEntryInObjective(EpsilonPolynomial[][] tableau) {
        for (int i = 0; i < tableau[0].length - 1; i++) {
            if (tableau[0][i].compareTo(factory.zero()) < 0)
                return true;
        }
        return false;
    }

    protected void build() {
        generateSequenceConstraints();
        generateISConstraints();
        addObjective();
    }

    private void addObjective() {
        lpTable.addToObjective(new Pair<>("v", 0), factory.oneNeg());
    }

    protected void generateISConstraints() {
        for (SequenceInformationSet informationSet : config.getAllInformationSets().values()) {
            createConstraintFor(informationSet);
        }
    }

    protected void generateSequenceConstraints() {
        initRPConstraint(new ArrayListSequenceImpl(players[0]));
        initRPConstraint(new ArrayListSequenceImpl(players[1]));
        for (Sequence sequence : config.getAllSequences()) {
            createValueConstraintFor(sequence);
            createIntegerConstraint(sequence);
            createSlackConstraint(sequence);
            createIntegerVariableLimit(sequence);
        }
    }

    protected void createIntegerVariableLimit(Sequence sequence) {
        Object eqKey = new Pair<>("b_lim", sequence);
        Pair<String, Sequence> slackKey = new Pair<>("s_b_lim", sequence);

        lpTable.setConstraint(eqKey, new Pair<>("b", sequence), factory.one());
        lpTable.setConstraint(eqKey, slackKey, factory.one());
        lpTable.setInitBasis(eqKey, slackKey);
        lpTable.markAsBinary(new Pair<>("b", sequence));
        lpTable.markBinaryVariableLimitConstraint(eqKey, slackKey, new Pair<>("b", sequence));
        lpTable.setConstant(eqKey, factory.one());
    }

    protected void createSlackConstraint(Sequence sequence) {
        Object eqKey = new Pair<>("s", sequence);
        Pair<String, Sequence> slackKey = new Pair<>("s_s", sequence);

        lpTable.setConstraint(eqKey, eqKey, factory.one());
        lpTable.setConstraint(eqKey, new Pair<>("b", sequence), factory.bigM());
        lpTable.setConstraint(eqKey, slackKey, factory.one());
        lpTable.setInitBasis(eqKey, slackKey);
        lpTable.setConstant(eqKey, getEpsilonPolynomial(sequence, M));
    }

    protected void createIntegerConstraint(Sequence sequence) {
        Object eqKey = new Pair<>("b", sequence);
        Object slackKey = new Pair<>("s_b", sequence);

        lpTable.setConstraint(eqKey, sequence, factory.one());
        lpTable.setConstraint(eqKey, eqKey, factory.oneNeg());
        lpTable.setConstraint(eqKey, slackKey, factory.one());
        lpTable.setInitBasis(eqKey, slackKey);
        lpTable.markAsBinary(eqKey);
    }

    protected void initRPConstraint(Sequence emptySequence) {
        Pair<String, Player> slackKey = new Pair<>("s_r", emptySequence.getPlayer());

        lpTable.setConstraint(emptySequence.getPlayer(), emptySequence, factory.one());
        lpTable.setConstraint(emptySequence.getPlayer(), slackKey, factory.one());
        lpTable.setInitBasis(emptySequence.getPlayer(), slackKey);
        lpTable.markFirstPhaseSlack(emptySequence.getPlayer(), slackKey);
        lpTable.setConstant(emptySequence.getPlayer(), getEpsilonPolynomial(emptySequence.getPlayer(), 1));
    }

    protected void createConstraintFor(SequenceInformationSet informationSet) {
        if (!informationSet.getOutgoingSequences().isEmpty()) {
            Object slackKey = new Pair<>(informationSet, "s_r");

            lpTable.setConstraint(informationSet, informationSet.getPlayersHistory(), factory.one());
            lpTable.setConstraint(informationSet, slackKey, factory.one());
            lpTable.setInitBasis(informationSet, slackKey);
            lpTable.markFirstPhaseSlack(informationSet, slackKey);
            for (Sequence outgoingSequence : informationSet.getOutgoingSequences()) {
                lpTable.setConstraint(informationSet, outgoingSequence, factory.oneNeg());
            }
            lpTable.setConstant(informationSet, getEpsilonPolynomial(informationSet));
        }
    }

    protected void createValueConstraintFor(Sequence sequence) {
        Object infSetVarKey = new Pair<>("v", (sequence.size() == 0 ? sequence.getPlayer().getId() : sequence.getLastInformationSet()));
        Pair<String, Sequence> slackKey = new Pair<>("s_v", sequence);

        lpTable.setConstraint(sequence, infSetVarKey, factory.oneNeg());
        lpTable.setConstraint(sequence, new Pair<>("s", sequence), factory.one());
//        lpTable.setInitBasis(sequence, new Pair<>("s", sequence));
        lpTable.setConstraint(sequence, slackKey, factory.one());
        lpTable.setConstant(sequence, getEpsilonPolynomial(sequence));
        lpTable.setInitBasis(sequence, slackKey);
        lpTable.markFirstPhaseSlack(sequence, slackKey);
        for (Sequence compatibleSequence : config.getCompatibleSequencesFor(sequence)) {
            Double utility = config.getUtilityFor(sequence, compatibleSequence, sequence.getPlayer());

            if (utility != null)
                lpTable.setConstraint(sequence, compatibleSequence, factory.create(info.getUtilityStabilizer() * utility));
        }
        for (SequenceInformationSet reachableIS : config.getReachableSets(sequence)) {
            if (!reachableIS.getOutgoingSequences().isEmpty())
                lpTable.setConstraint(sequence, new Pair<>("v", reachableIS), factory.one());
        }
    }

    protected EpsilonPolynomial getEpsilonPolynomial(Sequence sequence) {
        return getEpsilonPolynomial(sequence, 0);
    }

    protected EpsilonPolynomial getEpsilonPolynomial(Sequence sequence, int firstCoefficient) {
        Map<Integer, Integer> coefMap = new LinkedHashMap<>();
        int maxCoef = 0;

        coefMap.put(0, firstCoefficient);
        for (Sequence compatibleSequence : config.getCompatibleSequencesFor(sequence)) {
            Double utility = config.getUtilityFor(sequence, compatibleSequence, sequence.getPlayer());
            int coefficient = compatibleSequence.size() + 1;

            if (utility != null) {
                Integer oldValue = coefMap.get(coefficient);

                utility = utility * info.getUtilityStabilizer();
                assert Math.abs(utility - Math.round(utility)) < 1e-8;

                coefMap.put(coefficient, (int) (oldValue == null ? Math.round(utility) : oldValue + Math.round(utility)));
                if (maxCoef < coefficient)
                    maxCoef = coefficient;
            }
        }
        return factory.create(coefMap, maxCoef);
    }

    protected EpsilonPolynomial getEpsilonPolynomial(SequenceInformationSet informationSet) {
        Map<Integer, Integer> coefMap = new LinkedHashMap<>();
        int maxCoef = informationSet.getPlayersHistory().size() + 1;

        coefMap.put(informationSet.getPlayersHistory().size() + 1, 1);
        for (Sequence sequence : informationSet.getOutgoingSequences()) {
            int coefficient = sequence.size() + 1;
            Integer oldValue = coefMap.get(coefficient);

            coefMap.put(coefficient, oldValue == null ? -1 : oldValue - 1);
            if (coefficient > maxCoef)
                maxCoef = coefficient;
        }
        return factory.create(coefMap, maxCoef);
    }

    protected EpsilonPolynomial getEpsilonPolynomial(Player player, int firstCoefficient) {
        Map<Integer, Integer> coefMap = new LinkedHashMap<>();
        int maxCoef = 1;

        coefMap.put(0, firstCoefficient);
        coefMap.put(1, -1);
        return factory.create(coefMap, maxCoef);
    }
}
