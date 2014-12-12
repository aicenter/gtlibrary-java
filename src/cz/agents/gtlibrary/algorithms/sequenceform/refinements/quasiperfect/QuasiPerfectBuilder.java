/*
Copyright 2014 Faculty of Electrical Engineering at CTU in Prague

This file is part of Game Theoretic Library.

Game Theoretic Library is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Game Theoretic Library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with Game Theoretic Library.  If not, see <http://www.gnu.org/licenses/>.*/


package cz.agents.gtlibrary.algorithms.sequenceform.refinements.quasiperfect;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.quasiperfect.lp.LPDictionary;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.quasiperfect.lp.SimplexData;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.quasiperfect.numbers.EpsilonReal;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.quasiperfect.numbers.Rational;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.Key;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPData;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.TreeVisitor;
import cz.agents.gtlibrary.domain.aceofspades.AoSExpander;
import cz.agents.gtlibrary.domain.aceofspades.AoSGameInfo;
import cz.agents.gtlibrary.domain.aceofspades.AoSGameState;
import cz.agents.gtlibrary.domain.bpg.BPGExpander;
import cz.agents.gtlibrary.domain.bpg.BPGGameInfo;
import cz.agents.gtlibrary.domain.bpg.BPGGameState;
import cz.agents.gtlibrary.domain.goofspiel.GSGameInfo;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielExpander;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielGameState;
import cz.agents.gtlibrary.domain.goofspiel.IIGoofSpielGameState;
import cz.agents.gtlibrary.domain.mpochm.MPoCHMExpander;
import cz.agents.gtlibrary.domain.mpochm.MPoCHMGameInfo;
import cz.agents.gtlibrary.domain.mpochm.MPoCHMGameState;
import cz.agents.gtlibrary.domain.poker.generic.GPGameInfo;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerExpander;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerGameState;
import cz.agents.gtlibrary.domain.poker.kuhn.KPGameInfo;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerExpander;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerGameState;
import cz.agents.gtlibrary.domain.randomgame.RandomGameExpander;
import cz.agents.gtlibrary.domain.randomgame.RandomGameInfo;
import cz.agents.gtlibrary.domain.randomgame.RandomGameState;
import cz.agents.gtlibrary.domain.upordown.UDExpander;
import cz.agents.gtlibrary.domain.upordown.UDGameInfo;
import cz.agents.gtlibrary.domain.upordown.UDGameState;
import cz.agents.gtlibrary.experimental.utils.UtilityCalculator;
import cz.agents.gtlibrary.iinodes.LinkedListSequenceImpl;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.strategy.Strategy;
import cz.agents.gtlibrary.strategy.UniformStrategyForMissingSequences;
import cz.agents.gtlibrary.utils.io.FileManager;
import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class QuasiPerfectBuilder extends TreeVisitor {

    protected String lpFileName;
    protected EpsilonLPTable lpTable;
    protected GameInfo gameInfo;

    public static void main(String[] args) {
        runAoS();
//		runGoofSpiel();
//        runIIGoofSpiel();
//		runKuhnPoker();
//        runMPoCHM();
//        runBPG();
//        runUD();
//		runGenericPoker();
//        runRandomGame();
    }

    public static void runKuhnPoker() {
        AlgorithmConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
        QuasiPerfectBuilder lpBuilder = new QuasiPerfectBuilder(new KuhnPokerExpander<SequenceInformationSet>(algConfig), new KuhnPokerGameState(), new KPGameInfo());

        lpBuilder.buildLP();
        lpBuilder.solve("KP");
    }

    public static void runRandomGame() {
        AlgorithmConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
        GameInfo info = new RandomGameInfo();
        QuasiPerfectBuilder lpBuilder = new QuasiPerfectBuilder(new RandomGameExpander<SequenceInformationSet>(algConfig), new RandomGameState(), info);

        lpBuilder.buildLP();
        lpBuilder.solve(getDomainType());
    }

    private static String getDomainType() {
        return "RGD:"+ RandomGameInfo.MAX_DEPTH+" BF:" + RandomGameInfo.MAX_BF+ " OBS:" + RandomGameInfo.MAX_OBSERVATION + " BU:"+ RandomGameInfo.BINARY_UTILITY + " UC:" + RandomGameInfo.UTILITY_CORRELATION + " MCM:" + RandomGameInfo.MAX_CENTER_MODIFICATION;
    }


    public static void runMPoCHM() {
        AlgorithmConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
        QuasiPerfectBuilder lpBuilder = new QuasiPerfectBuilder(new MPoCHMExpander<SequenceInformationSet>(algConfig), new MPoCHMGameState(), new MPoCHMGameInfo());

        lpBuilder.buildLP();
        lpBuilder.solve("MPoCHM");
    }

    public static void runUD() {
        AlgorithmConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
        QuasiPerfectBuilder lpBuilder = new QuasiPerfectBuilder(new UDExpander<SequenceInformationSet>(algConfig), new UDGameState(), new UDGameInfo());

        lpBuilder.buildLP();
        lpBuilder.solve("UD");
    }

    public static void runGenericPoker() {
        AlgorithmConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
        QuasiPerfectBuilder lpBuilder = new QuasiPerfectBuilder(new GenericPokerExpander<SequenceInformationSet>(algConfig), new GenericPokerGameState(), new GPGameInfo());

        lpBuilder.buildLP();
        lpBuilder.solve("GP" + GPGameInfo.MAX_CARD_TYPES + "" + GPGameInfo.MAX_CARD_OF_EACH_TYPE);
    }

    public static void runAoS() {
        AlgorithmConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
        QuasiPerfectBuilder lpBuilder = new QuasiPerfectBuilder(new AoSExpander<SequenceInformationSet>(algConfig), new AoSGameState(), new AoSGameInfo());

        lpBuilder.buildLP();
        lpBuilder.solve("AoS");
    }

    public static void runGoofSpiel() {
        AlgorithmConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
        QuasiPerfectBuilder lpBuilder = new QuasiPerfectBuilder(new GoofSpielExpander<SequenceInformationSet>(algConfig), new GoofSpielGameState(), new GSGameInfo());

        lpBuilder.buildLP();
        lpBuilder.solve("GS");
    }

    public static void runIIGoofSpiel() {
        AlgorithmConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
        QuasiPerfectBuilder lpBuilder = new QuasiPerfectBuilder(new GoofSpielExpander<SequenceInformationSet>(algConfig), new IIGoofSpielGameState(), new GSGameInfo());

        lpBuilder.buildLP();
        lpBuilder.solve("IIGS");
    }

    public static void runBPG() {
        AlgorithmConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
        QuasiPerfectBuilder lpBuilder = new QuasiPerfectBuilder(new BPGExpander<SequenceInformationSet>(algConfig), new BPGGameState(), new BPGGameInfo());

        lpBuilder.buildLP();
        lpBuilder.solve("BPG");
    }

    public QuasiPerfectBuilder(Expander<SequenceInformationSet> expander, GameState rootState, GameInfo gameInfo) {
        super(rootState, expander);
        this.expander = expander;
        lpFileName = "quasiPerfect.lp";
        this.gameInfo = gameInfo;
    }

    public void buildLP() {
        initTable();
        visitTree(rootState);
    }

    public void solve(String domainType) {
//        try {
            solveBySimplex(domainType);
//            solveByCplex();
//        } catch (IloException e) {
//            e.printStackTrace();
//        }

    }

    private void solveBySimplex(String domainType) {
        SimplexData simplexData = lpTable.toPeturbedSimplex();
        LPDictionary.Status stat = simplexData.getSimplex().twoPhaseSimplex();

        assert stat == LPDictionary.Status.OPTIMAL;
        EpsilonReal[] primalSolution = simplexData.getSimplex().getSolution();
        EpsilonReal[] dualSolution = simplexData.getSimplex().getDualSolution();

        System.out.println("Primal:" + Arrays.toString(primalSolution));
        System.out.println("Dual:" + Arrays.toString(dualSolution));
        System.out.println(simplexData.getWatchedPrimalVars());
        System.out.println(simplexData.getWatchedDualVars());

        Map<Sequence, Double> p1RealizationPlan = getP1RealizationPlan(toDouble(dualSolution), simplexData.getWatchedDualVars(), simplexData.getSimplex().n);
        Map<Sequence, Double> p2RealizationPlan = getP2RealizationPlan(toDouble(primalSolution), simplexData.getWatchedPrimalVars());

        for (Entry<Sequence, Double> entry : p1RealizationPlan.entrySet()) {
            System.out.println(entry);
        }

        for (Entry<Sequence, Double> entry : p2RealizationPlan.entrySet()) {
            System.out.println(entry);
        }

        FileManager<Map<Sequence, Double>> fileManager = new FileManager<Map<Sequence, Double>>();

        fileManager.saveObject(p1RealizationPlan, domainType + "quasiPerfectP1RealPlan");
        fileManager.saveObject(p2RealizationPlan, domainType + "quasiPerfectP2RealPlan");

        UtilityCalculator calculator = new UtilityCalculator(rootState, expander);
        Strategy p1Strategy = new UniformStrategyForMissingSequences();
        Strategy p2Strategy = new UniformStrategyForMissingSequences();

        p1Strategy.putAll(p1RealizationPlan);
        p2Strategy.putAll(p2RealizationPlan);
        System.out.println(calculator.computeUtility(p1Strategy, p2Strategy));
        p1Strategy.sanityCheck(rootState, expander);
        p2Strategy.sanityCheck(rootState, expander);
    }

    private double[] toDouble(EpsilonReal[] epsilonRealArray) {
        double[] doubleArray = new double[epsilonRealArray.length];

        for (int i = 0; i < epsilonRealArray.length; i++) {
            doubleArray[i] = epsilonRealArray[i].doubleValue();
        }
        return doubleArray;
    }

    private double[] toDoubleArray(EpsilonReal[] epsilonRealArray) {
        double[] doubleArray = new double[epsilonRealArray.length];
        int index = 0;

        for (double value : doubleArray) {
            doubleArray[index++] = epsilonRealArray[index].doubleValue();
        }
        return doubleArray;
    }


    private Map<Sequence, Double> getP1RealizationPlan(double[] dualSolution, Map<Object, Integer> watchedDualVars, int offset) {
        Map<Sequence, Double> p1RealPlan = new HashMap<Sequence, Double>();

        for (Entry<Object, Integer> watchedEntry : watchedDualVars.entrySet()) {
            p1RealPlan.put((Sequence) watchedEntry.getKey(), dualSolution[watchedEntry.getValue() + offset + 1]);
        }
        return p1RealPlan;
    }

    private Map<Sequence, Double> getP2RealizationPlan(double[] primalSolution, Map<Object, Integer> watchedPrimalVars) {
        Map<Sequence, Double> p2RealPlan = new HashMap<Sequence, Double>();

        for (Entry<Object, Integer> watchedEntry : watchedPrimalVars.entrySet()) {
            p2RealPlan.put((Sequence) watchedEntry.getKey(), primalSolution[watchedEntry.getValue() + 1]);
        }
        return p2RealPlan;
    }

    private void solveByCplex() throws IloException {
        LPData lpData = lpTable.toCplex();

        lpData.getSolver().exportModel(lpFileName);
        System.out.println(lpData.getSolver().solve());
        System.out.println(lpData.getSolver().getStatus());
        System.out.println(lpData.getSolver().getObjValue());

        Map<Sequence, Double> p1RealizationPlan = createFirstPlayerStrategy(lpData.getSolver(), lpData.getWatchedDualVariables());
        Map<Sequence, Double> p2RealizationPlan = createSecondPlayerStrategy(lpData.getSolver(), lpData.getWatchedPrimalVariables());
//			System.out.println(p1RealizationPlan);
//			System.out.println(p2RealizationPlan);

        for (Entry<Sequence, Double> entry : p1RealizationPlan.entrySet()) {
            if (entry.getValue() > 0)
                System.out.println(entry);
        }
        for (Entry<Sequence, Double> entry : p2RealizationPlan.entrySet()) {
            if (entry.getValue() > 0)
                System.out.println(entry);
        }

        UtilityCalculator calculator = new UtilityCalculator(rootState, expander);
        Strategy p1Strategy = new UniformStrategyForMissingSequences();
        Strategy p2Strategy = new UniformStrategyForMissingSequences();

        p1Strategy.putAll(p1RealizationPlan);
        p2Strategy.putAll(p2RealizationPlan);

//			System.out.println(p1Strategy.fancyToString(rootState, expander, new PlayerImpl(0)));
//			System.out.println("************************************");
//			System.out.println(p2Strategy.fancyToString(rootState, expander, new PlayerImpl(1)));
//			System.out.println("Solution: " + Arrays.toString(lpData.getSolver().getValues(lpData.getVariables())));
//			System.out.println("Dual solution: " + Arrays.toString(lpData.getSolver().getDuals(lpData.getConstraints())));
//			System.out.println(lpData.getWatchedPrimalVariables());
//			System.out.println(lpData.getWatchedDualVariables());
        System.out.println(calculator.computeUtility(p1Strategy, p2Strategy));
        p1Strategy.sanityCheck(rootState, expander);
        p2Strategy.sanityCheck(rootState, expander);
    }

    public Map<Sequence, Double> createFirstPlayerStrategy(IloCplex cplex, Map<Object, IloRange> watchedDualVars) throws IloException {
        Map<Sequence, Double> p1Strategy = new HashMap<Sequence, Double>();

        for (Entry<Object, IloRange> entry : watchedDualVars.entrySet()) {
            p1Strategy.put((Sequence) entry.getKey(), cplex.getDual(entry.getValue()));
        }
        return p1Strategy;
    }

    public Map<Sequence, Double> createSecondPlayerStrategy(IloCplex cplex, Map<Object, IloNumVar> watchedPrimalVars) throws IloException {
        Map<Sequence, Double> p2Strategy = new HashMap<Sequence, Double>();

        for (Entry<Object, IloNumVar> entry : watchedPrimalVars.entrySet()) {
            p2Strategy.put((Sequence) entry.getKey(), cplex.getValue(entry.getValue()));
        }
        return p2Strategy;
    }

    public void initTable() {
        Sequence p1EmptySequence = new LinkedListSequenceImpl(players[0]);
        Sequence p2EmptySequence = new LinkedListSequenceImpl(players[1]);

        lpTable = new EpsilonLPTable();

        initCost(p1EmptySequence);
        initE(p1EmptySequence);
        initF(p2EmptySequence);
        initf(p2EmptySequence);
    }

    public void initf(Sequence p2EmptySequence) {
        lpTable.setConstant(new Key("Q", p2EmptySequence), EpsilonReal.ONE.negate());//f for root
    }

    public void initF(Sequence p2EmptySequence) {
        lpTable.setConstraint(new Key("Q", p2EmptySequence), p2EmptySequence, EpsilonReal.ONE.negate());//F in root (only 1)
//		lpTable.setConstraintType(new Key("Q", p2EmptySequence), 1);
    }

    public void initE(Sequence p1EmptySequence) {
        lpTable.setConstraint(p1EmptySequence, new Key("P", p1EmptySequence), EpsilonReal.ONE.negate());//E in root (only 1)
//        lpTable.setConstraint(p1EmptySequence, new Key("U", p1EmptySequence), EpsilonReal.ONE);
//        lpTable.setLowerBound(new Key("P", p1EmptySequence), Double.NEGATIVE_INFINITY);
    }

    public void initCost(Sequence p1EmptySequence) {
        lpTable.setObjective(new Key("P", p1EmptySequence), EpsilonReal.ONE.negate());
    }

    @Override
    protected void visitLeaf(GameState state) {
        updateParentLinks(state);
        lpTable.addToConstraint(state.getSequenceFor(players[0]), state.getSequenceFor(players[1]), calculateUtility(state));
    }

    private EpsilonReal calculateUtility(GameState state) {
        return new EpsilonReal(state.getExactNatureProbability().multiply(state.getExactUtilities()[0].add(new Rational((int) gameInfo.getMaxUtility() + 1))));
    }

    @Override
    protected void visitNormalNode(GameState state) {
        if (state.getPlayerToMove().getId() == 0) {
            updateLPForFirstPlayer(state);
        } else {
            updateLPForSecondPlayer(state);
        }
        super.visitNormalNode(state);
    }

    public void updateLPForFirstPlayer(GameState state) {
        Key varKey = new Key("P", new Key(state.getISKeyForPlayerToMove()));

        updateParentLinks(state);
        lpTable.setConstraint(state.getSequenceFor(players[0]), varKey, EpsilonReal.ONE);//E
//        lpTable.setLowerBound(varKey, Double.NEGATIVE_INFINITY);
        lpTable.watchDualVariable(state.getSequenceFor(players[0]), state.getSequenceForPlayerToMove());
    }

    public void updateLPForSecondPlayer(GameState state) {
        Key eqKey = new Key("Q", new Key(state.getISKeyForPlayerToMove()));

        updateParentLinks(state);
        lpTable.setConstraint(eqKey, state.getSequenceFor(players[1]), EpsilonReal.ONE);//F
//		lpTable.setConstraintType(eqKey, 1);
        lpTable.watchPrimalVariable(state.getSequenceFor(players[1]), state.getSequenceFor(players[1]));
    }

    @Override
    protected void visitChanceNode(GameState state) {
        updateParentLinks(state);
        super.visitChanceNode(state);
    }

    public void updateParentLinks(GameState state) {
        updateP1Parent(state);
        updateP2Parent(state);
    }

    protected void updateP1Parent(GameState state) {
        Sequence p1Sequence = state.getSequenceFor(players[0]);

        if (p1Sequence.size() == 0)
            return;
        Object varKey = getLastISKey(p1Sequence);
        Key tmpKey = new Key("U", p1Sequence);

        lpTable.watchDualVariable(p1Sequence, p1Sequence);
        lpTable.setConstraint(p1Sequence, varKey, EpsilonReal.ONE.negate());//E child
//        lpTable.setLowerBound(varKey, Double.NEGATIVE_INFINITY);

        lpTable.setConstraint(p1Sequence, tmpKey, EpsilonReal.ONE);//u (eye)
//		lpTable.setObjective(tmpKey, new EpsilonPolynom(epsilon, p1Sequence.size()));//k(\epsilon)
        lpTable.setObjective(tmpKey, new EpsilonReal(1, p1Sequence.size()));
    }

    protected void updateP2Parent(GameState state) {
        Sequence p2Sequence = state.getSequenceFor(players[1]);

        if (p2Sequence.size() == 0)
            return;
        Object eqKey = getLastISKey(p2Sequence);
        Key tmpKey = new Key("V", p2Sequence);

        lpTable.setConstraint(eqKey, p2Sequence, EpsilonReal.ONE.negate());//F child
//        lpTable.setConstraintType(eqKey, 1);
        lpTable.watchPrimalVariable(p2Sequence, p2Sequence);
        lpTable.setConstraint(tmpKey, p2Sequence, EpsilonReal.ONE.negate());//indices y
//		lpTable.setConstant(tmpKey, new EpsilonPolynom(epsilon, p2Sequence.size()).negate());//l(\epsilon)
        lpTable.setConstant(tmpKey, new EpsilonReal(1, p2Sequence.size()).negate());
    }
}
