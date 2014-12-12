package cz.agents.gtlibrary.algorithms.sequenceform.gensum.parammilp;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.gensum.GenSumSequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.gensum.SolverResult;
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
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.strategy.NoMissingSeqStrategy;
import cz.agents.gtlibrary.utils.Pair;
import cz.agents.gtlibrary.utils.io.GambitEFG;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class GTFCaller extends Simplex {

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
        Simplex simplex = new GTFCaller(root.getAllPlayers(), config, new EpsilonPolynomialFactory(new BigIntRationalFactory()), new InformerAoSGameInfo());

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
        Simplex simplex = new GTFCaller(root.getAllPlayers(), config, new EpsilonPolynomialFactory(new BigIntRationalFactory()), new AoSGameInfo());

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
        Simplex simplex = new GTFCaller(root.getAllPlayers(), config, new EpsilonPolynomialFactory(new BigIntRationalFactory()), new KPGameInfo());
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

    public GTFCaller(Player[] players, GenSumSequenceFormConfig config, EpsilonPolynomialFactory factory, GameInfo info) {
        super(players, config, factory, info);
    }

    @Override
    public SolverResult compute() {
        build();
//        solve();
        solveUsingGTF();      //ten gtf má nějaký záporný věci, to by se asi dalo vykoukat ze stavění

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

    private void solveUsingGTF() {
        GTFSimplexData data = lpTable.toGtfSimplex();
        LPDictionary<EpsilonReal> lp = new LPDictionary<>(data.tableau);
        LPDictionary.Status stat = lp.twoPhaseSimplex();

        assert stat == LPDictionary.Status.OPTIMAL;
        EpsilonReal[] primalSolution = lp.getSolution();

        System.out.println("Primal:" + Arrays.toString(primalSolution));
        System.out.println(primalSolution.length);
        Map<Sequence, Double> p1Rp = new HashMap<>();

        for (Sequence sequence : config.getSequencesFor(players[0])) {
            p1Rp.put(sequence, primalSolution[lpTable.getVariableIndex(sequence)].doubleValue());
        }
        Map<Sequence, Double> p2Rp = new HashMap<>();

        for (Sequence sequence : config.getSequencesFor(players[1])) {
            p2Rp.put(sequence, primalSolution[lpTable.getVariableIndex(sequence)].doubleValue());
        }
//        System.out.println(p1Rp);
        System.out.println(p2Rp);
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

        lpTable.setConstraint(eqKey, sequence, factory.one());
        lpTable.setConstraint(eqKey, eqKey, factory.oneNeg());
        lpTable.markAsBinary(eqKey);
    }

    protected void initRPConstraint(Sequence emptySequence) {
        lpTable.setConstraint(emptySequence.getPlayer(), emptySequence, factory.one());
        lpTable.setConstant(emptySequence.getPlayer(), getEpsilonPolynomial(emptySequence.getPlayer(), 1));
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
        lpTable.setConstant(sequence, getEpsilonPolynomial(sequence));
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
