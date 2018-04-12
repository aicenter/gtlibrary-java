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
import cz.agents.gtlibrary.domain.mpochm.MPoCHMExpander;
import cz.agents.gtlibrary.domain.mpochm.MPoCHMGameInfo;
import cz.agents.gtlibrary.domain.mpochm.MPoCHMGameState;
import cz.agents.gtlibrary.domain.poker.kuhn.KPGameInfo;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerExpander;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerGameState;
import cz.agents.gtlibrary.domain.stacktest.StackTestExpander;
import cz.agents.gtlibrary.domain.stacktest.StackTestGameInfo;
import cz.agents.gtlibrary.domain.stacktest.StackTestGameState;
import cz.agents.gtlibrary.experimental.utils.UtilityCalculator;
import cz.agents.gtlibrary.iinodes.ArrayListSequenceImpl;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.strategy.NoMissingSeqStrategy;
import cz.agents.gtlibrary.utils.Pair;
import cz.agents.gtlibrary.utils.io.GambitEFG;

import java.util.HashMap;
import java.util.Map;

public class MiltersenSimplex extends Simplex {

    public static void main(String[] args) {
        runAoS();
//        runKuhnPoker();
//        runIAoS();
//        runMPoCHM();
//        runStackTest();
    }

    protected static void runIAoS() {
        GameState root = new InformerAoSGameState();
        GenSumSequenceFormConfig config = new GenSumSequenceFormConfig();
        Expander<SequenceInformationSet> expander = new InformerAoSExpander<>(config);

        GeneralSumGameBuilder.buildWithUtilityShift(root, config, expander, 10);
        Simplex simplex = new MiltersenSimplex(root.getAllPlayers(), config, new EpsilonPolynomialFactory(new BigIntRationalFactory()), new InformerAoSGameInfo());

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
        Simplex simplex = new MiltersenSimplex(root.getAllPlayers(), config, new EpsilonPolynomialFactory(new BigIntRationalFactory()), new AoSGameInfo());

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

    protected static void runMPoCHM() {
        GameState root = new MPoCHMGameState();
        GenSumSequenceFormConfig config = new GenSumSequenceFormConfig();
        Expander<SequenceInformationSet> expander = new MPoCHMExpander<>(config);

        GeneralSumGameBuilder.buildWithUtilityShift(root, config, expander, 10);
        Simplex simplex = new MiltersenSimplex(root.getAllPlayers(), config, new EpsilonPolynomialFactory(new BigIntRationalFactory()), new MPoCHMGameInfo());

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

    protected static void runStackTest() {
        GameState root = new StackTestGameState();
        GenSumSequenceFormConfig config = new GenSumSequenceFormConfig();
        Expander<SequenceInformationSet> expander = new StackTestExpander<>(config);

        GeneralSumGameBuilder.buildWithUtilityShift(root, config, expander, 10);
        Simplex simplex = new MiltersenSimplex(root.getAllPlayers(), config, new EpsilonPolynomialFactory(new BigIntRationalFactory()), new StackTestGameInfo());

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

        GambitEFG writer = new GambitEFG();

        writer.write("StackTest.gbt", root, expander);
    }

    protected static void runKuhnPoker() {
        GameState root = new KuhnPokerGameState();
        GenSumSequenceFormConfig config = new GenSumSequenceFormConfig();
        Expander<SequenceInformationSet> expander = new KuhnPokerExpander<>(config);

        GeneralSumGameBuilder.buildWithUtilityShift(root, config, expander, 20);
        Simplex simplex = new MiltersenSimplex(root.getAllPlayers(), config, new EpsilonPolynomialFactory(new BigIntRationalFactory()), new KPGameInfo());
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

    public MiltersenSimplex(Player[] players, GenSumSequenceFormConfig config, EpsilonPolynomialFactory factory, GameInfo info) {
        super(players, config, factory, info);
    }

    protected void generateSequenceConstraints() {
        initRPConstraint(new ArrayListSequenceImpl(players[0]));
        initRPConstraint(new ArrayListSequenceImpl(players[1]));
        for (Sequence sequence : config.getAllSequences()) {
            createValueConstraintFor(sequence);
            createIntegerConstraint(sequence);
            createSlackConstraint(sequence);
            createIntegerVariableLimit(sequence);
            createRealPlanLimit(sequence);
        }
    }

    private void createRealPlanLimit(Sequence sequence) {
        Object eqKey = new Pair<>("r_lim", sequence);
        Object slackKey = new Pair<>("s_r_lim_s", sequence);

        lpTable.setConstraint(eqKey, sequence, factory.one());
        lpTable.setConstant(eqKey, getLengthPolynomial(sequence));
        lpTable.setConstraint(eqKey, new Pair<>("s_r_lim", sequence), factory.oneNeg());
        lpTable.setConstraint(eqKey, slackKey, factory.one());
        lpTable.markFirstPhaseSlack(eqKey, slackKey);
        lpTable.setInitBasis(eqKey, slackKey);
    }

    private EpsilonPolynomial getLengthPolynomial(Sequence sequence) {
        Map<Integer, Integer> coefMap = new HashMap<>(1);

        coefMap.put(sequence.size() + 1, 1);
        return factory.create(coefMap, sequence.size() + 1);
    }

    protected void createIntegerVariableLimit(Sequence sequence) {
        Object eqKey = new Pair<>("b_lim", sequence);
        Pair<String, Sequence> slackKey = new Pair<>("s_b_lim", sequence);

        lpTable.setConstraint(eqKey, new Pair<>("b", sequence), factory.one());
        lpTable.setConstraint(eqKey, slackKey, factory.one());
        lpTable.setInitBasis(eqKey, slackKey);
        lpTable.markAsBinary(new Pair<>("b", sequence));

        lpTable.setConstant(eqKey, factory.one());

        if(sequence.size() == 0)
            lpTable.markFirstPhaseSlack(eqKey, slackKey);
        else
            lpTable.markBinaryVariableLimitConstraint(eqKey, slackKey, new Pair<>("b", sequence));
    }

    protected void createSlackConstraint(Sequence sequence) {
        Object eqKey = new Pair<>("s", sequence);
        Pair<String, Sequence> slackKey = new Pair<>("s_s", sequence);

        lpTable.setConstraint(eqKey, eqKey, factory.one());
        lpTable.setConstraint(eqKey, new Pair<>("b", sequence), factory.bigM());
        lpTable.setConstraint(eqKey, slackKey, factory.one());
        lpTable.setInitBasis(eqKey, slackKey);
        lpTable.setConstant(eqKey, factory.bigM());
    }

    protected void createIntegerConstraint(Sequence sequence) {
        Object eqKey = new Pair<>("b", sequence);
        Object slackKey = new Pair<>("s_b", sequence);

        lpTable.setConstraint(eqKey, sequence, factory.one());
        lpTable.setConstraint(eqKey, eqKey, factory.oneNeg());
        lpTable.setConstraint(eqKey, slackKey, factory.one());
        lpTable.setInitBasis(eqKey, slackKey);
        lpTable.markAsBinary(eqKey);
        lpTable.setConstant(eqKey, getLengthPolynomial(sequence));
    }

    protected void initRPConstraint(Sequence emptySequence) {
        Pair<String, Player> slackKey = new Pair<>("s_r", emptySequence.getPlayer());

        lpTable.setConstraint(emptySequence.getPlayer(), emptySequence, factory.one());
        lpTable.setConstraint(emptySequence.getPlayer(), slackKey, factory.one());
        lpTable.setInitBasis(emptySequence.getPlayer(), slackKey);
        lpTable.markFirstPhaseSlack(emptySequence.getPlayer(), slackKey);
        lpTable.setConstant(emptySequence.getPlayer(), factory.one());
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
        }
    }

    protected void createValueConstraintFor(Sequence sequence) {
        Object infSetVarKey = new Pair<>("v", (sequence.size() == 0 ? sequence.getPlayer().getId() : sequence.getLastInformationSet()));
        Pair<String, Sequence> slackKey = new Pair<>("s_v", sequence);

        lpTable.setConstraint(sequence, infSetVarKey, factory.one());
        lpTable.setConstraint(sequence, new Pair<>("s", sequence), factory.oneNeg());
        lpTable.setConstraint(sequence, slackKey, factory.one());
//        lpTable.setConstant(sequence, getEpsilonPolynomial(sequence));
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
}
