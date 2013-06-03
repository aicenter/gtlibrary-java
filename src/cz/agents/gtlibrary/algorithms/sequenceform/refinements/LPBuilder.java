package cz.agents.gtlibrary.algorithms.sequenceform.refinements;

import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.domain.aceofspades.AoSExpander;
import cz.agents.gtlibrary.domain.aceofspades.AoSGameState;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielExpander;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielGameState;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerExpander;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerGameState;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerExpander;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerGameState;
import cz.agents.gtlibrary.experimental.utils.UtilityCalculator;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.strategy.Strategy;
import cz.agents.gtlibrary.strategy.UniformStrategyForMissingSequences;
import cz.agents.gtlibrary.utils.Pair;

public class LPBuilder extends TreeVisitor {

	protected String lpFileName;
	protected LPTable lpTable;
	protected double utilityShift;
	protected Epsilon epsilon;

	public static void main(String[] args) {
//		runAoS();
//		runGoofSpiel();
//		runKuhnPoker();
		runGenericPoker();
	}

	public static void runKuhnPoker() {
		AlgorithmConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
		LPBuilder lpBuilder = new LPBuilder(new KuhnPokerExpander<SequenceInformationSet>(algConfig), new KuhnPokerGameState(), algConfig);

		lpBuilder.buildLP();
		lpBuilder.solve();
	}

	public static void runGenericPoker() {
		AlgorithmConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
		LPBuilder lpBuilder = new LPBuilder(new GenericPokerExpander<SequenceInformationSet>(algConfig), new GenericPokerGameState(), algConfig);

		lpBuilder.buildLP();
		lpBuilder.solve();
	}

	public static void runAoS() {
		AlgorithmConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
		LPBuilder lpBuilder = new LPBuilder(new AoSExpander<SequenceInformationSet>(algConfig), new AoSGameState(), algConfig);

		lpBuilder.buildLP();
		lpBuilder.solve();
	}

	public static void runGoofSpiel() {
		AlgorithmConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
		LPBuilder lpBuilder = new LPBuilder(new GoofSpielExpander<SequenceInformationSet>(algConfig), new GoofSpielGameState(), algConfig);

		lpBuilder.buildLP();
		lpBuilder.solve();
	}

	public LPBuilder(Expander<SequenceInformationSet> expander, GameState rootState, AlgorithmConfig<SequenceInformationSet> algConfig) {
		super(rootState, expander, algConfig);
		this.expander = expander;
		epsilon = new Epsilon();
		lpFileName = "lp.lp";
	}

	public void buildLP() {
		initTable();
		visitTree(rootState);
		computeEpsilon();
		System.out.println("epsilon: " + epsilon);
//		System.out.println(lpTable.toString());
	}

	public void solve() {
		try {
			LPData lpData = lpTable.toCplex();

//			lpData.getSolver().setParam(IloCplex.DoubleParam.EpMrk, 0.9999);
			lpData.getSolver().exportModel(lpFileName);
			System.out.println(lpData.getSolver().solve());
			System.out.println(lpData.getSolver().getStatus());
			System.out.println(lpData.getSolver().getObjValue());

			Map<Sequence, Double> p1RealizationPlan = createFirstPlayerStrategy(lpData.getSolver(), lpData.getWatchedDualVariables());
			Map<Sequence, Double> p2RealizationPlan = createSecondPlayerStrategy(lpData.getSolver(), lpData.getWatchedPrimalVariables());
			System.out.println(p1RealizationPlan);
			System.out.println(p2RealizationPlan);

			UtilityCalculator calculator = new UtilityCalculator(rootState, expander);
			Strategy p1Strategy = new UniformStrategyForMissingSequences();
			Strategy p2Strategy = new UniformStrategyForMissingSequences();

			p1Strategy.putAll(p1RealizationPlan);
			p2Strategy.putAll(p2RealizationPlan);
			
//			System.out.println(p1Strategy.fancyToString(rootState, expander, new PlayerImpl(0)));
//			System.out.println("************************************");
//			System.out.println(p2Strategy.fancyToString(rootState, expander, new PlayerImpl(1)));
			System.out.println("Solution: " + Arrays.toString(lpData.getSolver().getValues(lpData.getVariables())));
			System.out.println(calculator.computeUtility(p1Strategy, p2Strategy));
			p1Strategy.sanityCheck(rootState, expander);
			p2Strategy.sanityCheck(rootState, expander);
		} catch (IloException e) {
			e.printStackTrace();
		}

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
		Pair<Integer, Integer> sizes = getLPSize();

		lpTable = new LPTable(sizes.getLeft(), sizes.getRight());

		initCost();
		initE();
		initF();
		initf();
	}

	public void initf() {
		lpTable.setConstant(new Key("Q", lastKeys[1]), -1);//f for root
	}

	public void initF() {
		lpTable.setConstraint(new Key("Q", lastKeys[1]), lastKeys[1], 1);//F in root (only 1)
		lpTable.setConstraintType(new Key("Q", lastKeys[1]), 1);
	}

	public void initE() {
		lpTable.setConstraint(lastKeys[0], new Key("P", lastKeys[0]), 1);//E in root (only 1)
		lpTable.setLowerBound(new Key("P", lastKeys[0]), Double.NEGATIVE_INFINITY);
	}

	public void initCost() {
		lpTable.setObjective(new Key("P", lastKeys[0]), -1);
	}

	protected Pair<Integer, Integer> getLPSize() {
		SizeVisitor visitor = new SizeVisitor(rootState, expander, algConfig);

		visitor.visitTree(rootState);
		utilityShift = -visitor.getMinUtilityForPlayerOne() + 1;

		System.out.println("P1 IS count: " + visitor.getISCountFor(rootState.getAllPlayers()[0]));
		System.out.println("P2 IS count: " + visitor.getISCountFor(rootState.getAllPlayers()[1]));
		System.out.println("P1 cont count: " + visitor.getContinuationCountFor(rootState.getAllPlayers()[0]));
		System.out.println("P2 cont count: " + visitor.getContinuationCountFor(rootState.getAllPlayers()[1]));

		int combinedStateCount = visitor.getContinuationCountFor(rootState.getAllPlayers()[0]) + visitor.getContinuationCountFor(rootState.getAllPlayers()[1]);

		return new Pair<Integer, Integer>(combinedStateCount + visitor.getISCountFor(rootState.getAllPlayers()[0]), combinedStateCount + visitor.getISCountFor(rootState.getAllPlayers()[1]));
	}

	@Override
	protected void visitLeaf(GameState state) {
		lpTable.substractFromConstraint(lastKeys[0], lastKeys[1], state.getNatureProbability() * (state.getUtilities()[0]/* + utilityShift*/));
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

		lpTable.setConstraint(lastKeys[0], varKey, -1);//E
		lpTable.setLowerBound(varKey, Double.NEGATIVE_INFINITY);
		lpTable.watchDualVariable(lastKeys[0], state.getSequenceForPlayerToMove());
		for (Action action : expander.getActions(state)) {
			updateLPForFirstPlayerChild(state.performAction(action), state.getPlayerToMove(), varKey);
		}
	}

	public void updateLPForFirstPlayerChild(GameState child, Player lastPlayer, Key varKey) {
		Key eqKey = new Key(child.getSequenceFor(lastPlayer));
		Key tmpKey = new Key("U", new Key(child.getSequenceFor(lastPlayer)));

		lpTable.watchDualVariable(eqKey, child.getSequenceFor(lastPlayer));
		lpTable.setConstraint(eqKey, varKey, 1);//E child
		lpTable.setLowerBound(varKey, Double.NEGATIVE_INFINITY);

		lpTable.setConstraint(eqKey, tmpKey, -1);//u (eye)
		lpTable.setObjective(tmpKey, new EpsilonPolynom(epsilon, child.getSequenceFor(lastPlayer).size()));//k(\epsilon)
	}

	public void updateLPForSecondPlayer(GameState state) {
		Key eqKey = new Key("Q", new Key(state.getISKeyForPlayerToMove()));

		lpTable.setConstraint(eqKey, lastKeys[1], -1);//F
		lpTable.setConstraintType(eqKey, 1);
		lpTable.watchPrimalVariable(lastKeys[1], state.getSequenceForPlayerToMove());
		for (Action action : expander.getActions(state)) {
			updateLPForSecondPlayerChild(state.performAction(action), state.getPlayerToMove(), eqKey);
		}
	}

	public void updateLPForSecondPlayerChild(GameState child, Player lastPlayer, Key eqKey) {
		Key varKey = new Key(child.getSequenceFor(lastPlayer));
		Key tmpKey = new Key("V", new Key(child.getSequenceFor(lastPlayer)));

		lpTable.setConstraint(eqKey, varKey, 1);//F child
		lpTable.setConstraintType(eqKey, 1);
		lpTable.watchPrimalVariable(varKey, child.getSequenceFor(lastPlayer));
		lpTable.setConstraint(tmpKey, varKey, 1);//indices y
		lpTable.setConstant(tmpKey, new EpsilonPolynom(epsilon, child.getSequenceFor(lastPlayer).size()).negate());//l(\epsilon)
	}

	protected void computeEpsilon() {
		double equationCount = lpTable.rowCount() - 1;
		double maxCoefficient = lpTable.getMaxCoefficient();

		epsilon.setValue(0.5 * Math.pow(equationCount, -equationCount - 1) * Math.pow(maxCoefficient, -2 * equationCount - 1));
	}

}
