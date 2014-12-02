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
import cz.agents.gtlibrary.iinodes.ArrayListSequenceImpl;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.Pair;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Simplex implements Algorithm {

    public static void main(String[] args) {
        GameState root = new AoSGameState();
        GenSumSequenceFormConfig config = new GenSumSequenceFormConfig();
        Expander<SequenceInformationSet> expander = new AoSExpander<>(config);

        GeneralSumGameBuilder.buildWithUtilityShift(root, config, expander, 2);
        Simplex simplex = new Simplex(root.getAllPlayers(), config, new EpsilonPolynomialFactory(new BigIntRationalFactory()), new AoSGameInfo());

        simplex.compute();
    }

    public static final int M = 1000000;

    private Player[] players;
    private GenSumSequenceFormConfig config;
    private SimplexTable lpTable;
    private EpsilonPolynomialFactory factory;
    private GameInfo info;

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
        return solve();
    }

    private SolverResult solve() {
        ParamSimplexData data = lpTable.toFirstPhaseSimplex();
        EpsilonPolynomial[][] tableau = data.tableau;
        List<Integer> basis = data.basis;

        for (Integer member : data.basis) {
            for (int i = 0; i < tableau[0].length; i++) {
                tableau[0][i] = tableau[0][i].subtract(tableau[member][i]);
            }
        }
        while (hasNegativeEntryInObjective(tableau)) {
            int enteringVarIndex = chooseEnteringVarIndex(tableau);
            int leavingVarRow = chooseLeavingVarRow(tableau, enteringVarIndex);

            updateBasis(basis, enteringVarIndex, leavingVarRow);
            EpsilonPolynomial enteringVariableValue = tableau[leavingVarRow][enteringVarIndex];

            for (int i = 0; i < tableau[0].length; i++) {
                tableau[leavingVarRow][i] = tableau[leavingVarRow][i].divide(enteringVariableValue);
            }

            for (int i = 0; i < tableau.length; i++) {
                if (i == leavingVarRow)
                    continue;
                EpsilonPolynomial tempValue = tableau[i][enteringVarIndex];

                for (int j = 0; j < tableau[0].length; j++) {
                    tableau[i][j] = tableau[i][j].subtract(tempValue.multiply(tableau[leavingVarRow][j]));
                }
            }
        }
        return null;
    }

    private void updateBasis(List<Integer> basis, int enteringVarIndex, int leavingVarRow) {
        basis.set(leavingVarRow, enteringVarIndex);
    }

    private int chooseLeavingVarRow(EpsilonPolynomial[][] tableau, int enteringVarIndex) {
        EpsilonPolynomial min = null;
        int minIndex = -1;

        for (int i = 0; i < tableau.length; i++) {
            EpsilonPolynomial fraction =  tableau[i][tableau[0].length - 1].divide(tableau[i][enteringVarIndex]);

            if(min == null || min.compareTo(fraction) > 0) {
                min = fraction;
                minIndex = i;
            }
        }
        return minIndex;
    }

    private int chooseEnteringVarIndex(EpsilonPolynomial[][] tableau) {
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

    private boolean hasNegativeEntryInObjective(EpsilonPolynomial[][] tableau) {
        for (int i = 0; i < tableau[0].length; i++) {
            if (tableau[0][i].compareTo(factory.zero()) < 0)
                return true;
        }
        return false;
    }

    private void build() {
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

    private void createIntegerVariableLimit(Sequence sequence) {
        Object eqKey = new Pair<>("b_lim", sequence);

        lpTable.setConstraint(eqKey, new Pair<>("b", sequence), factory.one());
        lpTable.setConstraint(eqKey, new Pair<>("s_b_lim", sequence), factory.one());
        lpTable.setConstant(eqKey, factory.one());
    }

    protected void createSlackConstraint(Sequence sequence) {
        Object key = new Pair<>("s", sequence);

        lpTable.setConstraint(key, key, factory.one());
        lpTable.setConstraint(key, new Pair<>("b", sequence), factory.bigM());
        lpTable.setConstraint(key, new Pair<>("s_s", sequence), factory.one());
        lpTable.setInitBasis(key, new Pair<>("s_s", sequence));
        lpTable.setConstant(key, getEpsilonPolynomial(sequence, M));
    }

    protected void createIntegerConstraint(Sequence sequence) {
        Object key = new Pair<>("b", sequence);

        lpTable.setConstraint(key, sequence, factory.one());
        lpTable.setConstraint(key, key, factory.oneNeg());
        lpTable.setConstraint(key, new Pair<>("s_b", sequence), factory.one());
        lpTable.setInitBasis(key, new Pair<>("s_b", sequence));
        lpTable.markAsBinary(key);
    }

    protected void initRPConstraint(Sequence emptySequence) {
        lpTable.setConstraint(emptySequence.getPlayer(), emptySequence, factory.one());
        lpTable.setConstant(emptySequence.getPlayer(), getEpsilonPolynomial(emptySequence, 1));
        lpTable.setConstraintType(emptySequence.getPlayer(), 1);
    }

    protected void createConstraintFor(SequenceInformationSet informationSet) {
        if (!informationSet.getOutgoingSequences().isEmpty()) {
            lpTable.setConstraint(informationSet, informationSet.getPlayersHistory(), factory.one());
            for (Sequence outgoingSequence : informationSet.getOutgoingSequences()) {
                lpTable.setConstraint(informationSet, outgoingSequence, factory.oneNeg());
            }
            lpTable.setConstant(informationSet, getEpsilonPolynomial(informationSet));
        }
    }

    protected void createValueConstraintFor(Sequence sequence) {
        Object infSetVarKey = new Pair<>("v", (sequence.size() == 0 ? sequence.getPlayer().getId() : sequence.getLastInformationSet()));

        lpTable.setConstraint(sequence, infSetVarKey, factory.one());
        lpTable.setConstraint(sequence, new Pair<>("s", sequence), factory.oneNeg());
        lpTable.setConstraint(sequence, new Pair<>("s_v", sequence), factory.one());
        lpTable.setConstant(sequence, getEpsilonPolynomial(sequence));
        lpTable.setInitBasis(sequence, new Pair<>("s_v", sequence));
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

    private EpsilonPolynomial getEpsilonPolynomial(Sequence sequence) {
        return getEpsilonPolynomial(sequence, 0);
    }

    private EpsilonPolynomial getEpsilonPolynomial(Sequence sequence, int firstCoefficient) {
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

    private EpsilonPolynomial getEpsilonPolynomial(SequenceInformationSet informationSet) {
        Map<Integer, Integer> coefMap = new LinkedHashMap<>();
        int maxCoef = informationSet.getPlayersHistory().size() + 1;

        coefMap.put(informationSet.getPlayersHistory().size() + 1, -1);
        for (Sequence sequence : informationSet.getOutgoingSequences()) {
            int coefficient = sequence.size() + 1;
            Integer oldValue = coefMap.get(coefficient);

            coefMap.put(coefficient, oldValue == null ? 1 : oldValue + 1);
            if (coefficient > maxCoef)
                maxCoef = coefficient;
        }
        return factory.create(coefMap, maxCoef);
    }
}
