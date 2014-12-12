package cz.agents.gtlibrary.algorithms.sequenceform.gensum.parammilp;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.gensum.GenSumSequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.gensum.SolverResult;
import cz.agents.gtlibrary.algorithms.sequenceform.gensum.parammilp.numbers.EpsilonPolynomial;
import cz.agents.gtlibrary.algorithms.sequenceform.gensum.parammilp.numbers.factory.BigIntRationalFactory;
import cz.agents.gtlibrary.algorithms.sequenceform.gensum.parammilp.numbers.factory.EpsilonPolynomialFactory;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.quasiperfect.lp.LPDictionary;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.quasiperfect.numbers.EpsilonReal;
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
        for (Map.Entry<Sequence, Double> entry : result.p1RealPlan.entrySet()) {
            if (entry.getValue() > 0)
                System.out.println(entry);
        }
        for (Map.Entry<Sequence, Double> entry : result.p2RealPlan.entrySet()) {
            if (entry.getValue() > 0)
                System.out.println(entry);
        }
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
        for (Map.Entry<Sequence, Double> entry : result.p1RealPlan.entrySet()) {
            if (entry.getValue() > 0)
                System.out.println(entry);
        }
        for (Map.Entry<Sequence, Double> entry : result.p2RealPlan.entrySet()) {
            if (entry.getValue() > 0)
                System.out.println(entry);
        }
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
        for (Map.Entry<Sequence, Double> entry : result.p1RealPlan.entrySet()) {
            if (entry.getValue() > 0)
                System.out.println(entry);
        }
        for (Map.Entry<Sequence, Double> entry : result.p2RealPlan.entrySet()) {
            if (entry.getValue() > 0)
                System.out.println(entry);
        }
    }

    public static final int M = 1000000;

    protected Player[] players;
    protected GenSumSequenceFormConfig config;
    protected SimplexTable lpTable;
    protected EpsilonPolynomialFactory factory;
    protected GameInfo info;

    protected FirstPhaseParametricSolverResult min;
    protected FirstPhaseParametricSolverResult epsilonMax;

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
        solve();

        System.out.println("min: " + min.value);
        for (Map.Entry<Sequence, Double> entry : min.p1Rp.entrySet()) {
            if (entry.getValue() > 0)
                System.out.println(entry);
        }
        for (Map.Entry<Sequence, Double> entry : min.p2Rp.entrySet()) {
            if (entry.getValue() > 0)
                System.out.println(entry);
        }
        System.out.println("epsilonMax: " + epsilonMax.value);
        for (Map.Entry<Sequence, Double> entry : epsilonMax.p1Rp.entrySet()) {
            if (entry.getValue() > 0)
                System.out.println(entry);
        }
        for (Map.Entry<Sequence, Double> entry : epsilonMax.p2Rp.entrySet()) {
            if (entry.getValue() > 0)
                System.out.println(entry);
        }
        return wrap(min);
    }

    protected SolverResult wrap(FirstPhaseParametricSolverResult solve) {
        return new SolverResult(solve.p1Rp, solve.p2Rp, 0);
    }

    protected void solve() {
        FirstPhaseParametricSolverResult currentResult = runSimplex();

        if (currentResult.value.getPolynomial()[0].compareTo(factory.getArithmeticFactory().zero()) > 0)
            return;
        if (lpTable.getBinaryVariableLimitConstraints().isEmpty())
            return;
        Iterator<Map.Entry<Object, Object>> iterator = lpTable.getBinaryVariableLimitConstraints().entrySet().iterator();
        Map.Entry<Object, Object> entry = iterator.next();

        iterator.remove();
        lpTable.markFirstPhaseSlack(entry.getKey(), entry.getValue());
        solve();
        lpTable.setConstant(entry.getKey(), factory.zero());
        solve();
        lpTable.removeFirstPhaseSlack(entry.getKey(), entry.getValue());
        lpTable.markBinaryVariableLimitConstraint(entry.getKey(), entry.getValue());
        lpTable.setConstant(entry.getKey(), factory.one());
    }

    protected FirstPhaseParametricSolverResult choose(FirstPhaseParametricSolverResult currentResult, FirstPhaseParametricSolverResult result0, FirstPhaseParametricSolverResult result1) {
        FirstPhaseParametricSolverResult min = currentResult;

        if (min.value.compareTo(result0.value) > 0)
            min = result0;
        if (min.value.compareTo(result1.value) > 0)
            min = result1;
        return min;
    }

    protected FirstPhaseParametricSolverResult runSimplex() {
        ParamSimplexData data = lpTable.toFirstPhaseSimplex();
        EpsilonPolynomial[][] tableau = data.tableau;
        List<Integer> basis = data.basis;

//        System.out.println("init c " + Arrays.toString(tableau[0]));
//        System.out.println(data.firstPhaseSlacks);
        for (int i = 0; i < data.firstPhaseSlacks.getRows().size(); i++) {
            Integer member = data.firstPhaseSlacks.getRows().get(i);

            assert tableau[0][data.firstPhaseSlacks.getColumns().get(i)].equals(factory.one());
            assert oneAtZerosOtherwiseWithoutObj(tableau, data.firstPhaseSlacks.getRows().get(i), data.firstPhaseSlacks.getColumns().get(i));
            for (int j = 0; j < tableau[0].length; j++) {
                tableau[0][j] = tableau[0][j].subtract(tableau[member][j]);
            }
            assert oneAtZerosOtherwise(tableau, data.firstPhaseSlacks.getRows().get(i), data.firstPhaseSlacks.getColumns().get(i));
        }

//        System.out.println("c after " + Arrays.toString(tableau[0]));
        while (hasNegativeEntryInObjective(tableau)) {
//            System.out.println("c: " + Arrays.toString(tableau[0]));
            int enteringVarIndex = chooseEnteringVarIndex(tableau);
            int leavingVarRow = chooseLeavingVarRowBland(tableau, basis, enteringVarIndex);

            assert oneAtZerosOtherwise(tableau, leavingVarRow, basis.get(leavingVarRow - 1));
//            System.out.println("entering var index: " + enteringVarIndex);
//            System.out.println("leaving var row: " + leavingVarRow);
//            printColumn(tableau, enteringVarIndex, "entering var column: ");
//            printColumn(tableau, tableau[0].length - 1, "b");
            updateBasis(basis, enteringVarIndex, leavingVarRow);
            EpsilonPolynomial enteringVariableValue = tableau[leavingVarRow][enteringVarIndex];

            assert enteringVariableValue.compareTo(factory.zero()) > 0;
            if (!enteringVariableValue.isOne())
                for (int i = 0; i < tableau[0].length; i++) {
                    tableau[leavingVarRow][i] = tableau[leavingVarRow][i].divide(enteringVariableValue);
                }
            assert tableau[leavingVarRow][enteringVarIndex].isOne();
            for (int i = 0; i < tableau.length; i++) {
                if (i == leavingVarRow)
                    continue;
                EpsilonPolynomial tempValue = tableau[i][enteringVarIndex];

                for (int j = 0; j < tableau[0].length; j++) {
                    tableau[i][j] = tableau[i][j].subtract(tableau[leavingVarRow][j].multiply(tempValue));
                }
            }
            for (int i = 0; i < basis.size(); i++) {
                assert oneAtZerosOtherwise(tableau, i + 1, basis.get(i));
            }
        }
        Map<Object, EpsilonPolynomial> variableValues = getVariableValues(data);
        Map<Sequence, Double> p1Rp = getRealizationPlanFor(players[0], variableValues);
        Map<Sequence, Double> p2Rp = getRealizationPlanFor(players[1], variableValues);
        EpsilonPolynomial value = tableau[0][tableau[0].length - 1].negate();

        if (allBinary(variableValues) && value.getPolynomial()[0].isZero()) {
            System.out.println("value: " + value);
            for (Map.Entry<Sequence, Double> entry : p1Rp.entrySet()) {
                if (entry.getValue() > 0)
                    System.out.println(entry);
            }
            for (Map.Entry<Sequence, Double> entry : p2Rp.entrySet()) {
                if (entry.getValue() > 0)
                    System.out.println(entry);
            }
            if (min == null || min.value.compareTo(value) > 0)
                min = new FirstPhaseParametricSolverResult(p1Rp, p2Rp, value);
            if (epsilonMax == null || epsilonMax.value.compareTo(value) < 0)
                epsilonMax = new FirstPhaseParametricSolverResult(p1Rp, p2Rp, value);
        }
        return new FirstPhaseParametricSolverResult(p1Rp, p2Rp, value);
    }

    protected boolean allBinary(Map<Object, EpsilonPolynomial> variableValues) {
        for (Object binaryVariable : lpTable.getBinaryVariables()) {
            EpsilonPolynomial value = variableValues.get(binaryVariable);

            if (!value.getPolynomial()[0].isOne() && !value.getPolynomial()[0].isZero())
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

        for (Map.Entry<Object, Integer> entry : lpTable.getVariableIndices().entrySet()) {
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
        lpTable.markBinaryVariableLimitConstraint(eqKey, slackKey);
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

        lpTable.setConstraint(sequence, infSetVarKey, factory.one());
        lpTable.setConstraint(sequence, new Pair<>("s", sequence), factory.oneNeg());
        lpTable.setConstraint(sequence, slackKey, factory.one());
        lpTable.setConstant(sequence, getEpsilonPolynomial(sequence));
        lpTable.setInitBasis(sequence, slackKey);
        lpTable.markFirstPhaseSlack(sequence, slackKey);
        for (Sequence compatibleSequence : config.getCompatibleSequencesFor(sequence)) {
            Double utility = config.getUtilityFor(sequence, compatibleSequence, sequence.getPlayer());

            if (utility != null)
                lpTable.setConstraint(sequence, compatibleSequence, factory.create(-(info.getUtilityStabilizer() * utility)));
        }
        for (SequenceInformationSet reachableIS : config.getReachableSets(sequence)) {
            if (!reachableIS.getOutgoingSequences().isEmpty())
                lpTable.setConstraint(sequence, new Pair<>("v", reachableIS), factory.oneNeg());
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
