package cz.agents.gtlibrary.algorithms.sequenceform.refinements;

import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;

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
import cz.agents.gtlibrary.iinodes.LinkedListSequenceImpl;
import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.strategy.Strategy;
import cz.agents.gtlibrary.strategy.UniformStrategyForMissingSequences;

public class LPBuilder extends TreeVisitor {

	protected String lpFileName;
	protected LPTable lpTable;
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
		visitTree(rootState, null, null);
		computeEpsilon();
		System.out.println("epsilon: " + epsilon);
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
//			System.out.println(p1RealizationPlan);
//			System.out.println(p2RealizationPlan);
			
			for (Entry<Sequence, Double> entry : p1RealizationPlan.entrySet()) {
				if(entry.getValue() > 0)
					System.out.println(entry);
			}
			for (Entry<Sequence, Double> entry : p2RealizationPlan.entrySet()) {
				if(entry.getValue() > 0)
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
		Sequence p1EmptySequence = new LinkedListSequenceImpl(players[0]);
		Sequence p2EmptySequence = new LinkedListSequenceImpl(players[1]);
		
		lpTable = new LPTable();

		initCost(p1EmptySequence);
		initE(p1EmptySequence);
		initF(p2EmptySequence);
		initf(p2EmptySequence);
	}

	public void initf(Sequence p2EmptySequence) {
		lpTable.setConstant(new Key("Q", p2EmptySequence), -1);//f for root
	}

	public void initF(Sequence p2EmptySequence) {
		lpTable.setConstraint(new Key("Q", p2EmptySequence), p2EmptySequence, 1);//F in root (only 1)
		lpTable.setConstraintType(new Key("Q", p2EmptySequence), 1);
	}

	public void initE(Sequence p1EmptySequence) {
		lpTable.setConstraint(p1EmptySequence, new Key("P", p1EmptySequence), 1);//E in root (only 1)
		lpTable.setLowerBound(new Key("P", p1EmptySequence), Double.NEGATIVE_INFINITY);
	}

	public void initCost(Sequence p1EmptySequence) {
		lpTable.setObjective(new Key("P", p1EmptySequence), -1);
	}

	@Override
	protected void visitLeaf(GameState state, Player lastPlayer, Key lastKey) {
		updateParentLinks(state, lastPlayer, lastKey);
		lpTable.substractFromConstraint(state.getSequenceFor(players[0]), state.getSequenceFor(players[1]), state.getNatureProbability() * state.getUtilities()[0]);
	}

	@Override
	protected void visitNormalNode(GameState state, Player lastPlayer, Key lastKey) {
		if (state.getPlayerToMove().getId() == 0) {
			updateLPForFirstPlayer(state, lastPlayer, lastKey);
		} else {
			updateLPForSecondPlayer(state, lastPlayer, lastKey);
		}
		super.visitNormalNode(state, lastPlayer, lastKey);
	}

	public void updateLPForFirstPlayer(GameState state, Player lastPlayer, Key lastKey) {
		Key varKey = new Key("P", new Key(state.getISKeyForPlayerToMove()));

		updateParentLinks(state, lastPlayer, lastKey);
		lpTable.setConstraint(state.getSequenceFor(players[0]), varKey, -1);//E
		lpTable.setLowerBound(varKey, Double.NEGATIVE_INFINITY);
		lpTable.watchDualVariable(state.getSequenceFor(players[0]), state.getSequenceForPlayerToMove());
	}

	public void updateForFirstPlayerParent(GameState child, Player lastPlayer, Key varKey) {
		Object eqKey = child.getSequenceFor(lastPlayer);
		Key tmpKey = new Key("U", child.getSequenceFor(lastPlayer));

		lpTable.watchDualVariable(eqKey, child.getSequenceFor(lastPlayer));
		lpTable.setConstraint(eqKey, varKey, 1);//E child
		lpTable.setLowerBound(varKey, Double.NEGATIVE_INFINITY);

		lpTable.setConstraint(eqKey, tmpKey, -1);//u (eye)
		lpTable.setObjective(tmpKey, new EpsilonPolynom(epsilon, child.getSequenceFor(lastPlayer).size()));//k(\epsilon)
	}

	public void updateLPForSecondPlayer(GameState state, Player lastPlayer, Key lastKey) {
		Key eqKey = new Key("Q", new Key(state.getISKeyForPlayerToMove()));

		updateParentLinks(state, lastPlayer, lastKey);
		lpTable.setConstraint(eqKey, state.getSequenceFor(players[1]), -1);//F
		lpTable.setConstraintType(eqKey, 1);
		lpTable.watchPrimalVariable(state.getSequenceFor(players[1]), state.getSequenceForPlayerToMove());
	}

	public void updateForSecondPlayerParent(GameState child, Player lastPlayer, Key eqKey) {
		Object varKey = child.getSequenceFor(lastPlayer);
		Key tmpKey = new Key("V", child.getSequenceFor(lastPlayer));

		lpTable.setConstraint(eqKey, varKey, 1);//F child
		lpTable.setConstraintType(eqKey, 1);
		lpTable.watchPrimalVariable(varKey, child.getSequenceFor(lastPlayer));
		lpTable.setConstraint(tmpKey, varKey, 1);//indices y
		lpTable.setConstant(tmpKey, new EpsilonPolynom(epsilon, child.getSequenceFor(lastPlayer).size()).negate());//l(\epsilon)
	}

	@Override
	protected void visitChanceNode(GameState state, Player lastPlayer, Key lastKey) {
		updateParentLinks(state, lastPlayer, lastKey);
		super.visitChanceNode(state, lastPlayer, lastKey);
	}

	public void updateParentLinks(GameState state, Player lastPlayer, Key lastKey) {
		if (lastPlayer != null)
			if (lastPlayer.getId() == 0) {
				updateForFirstPlayerParent(state, lastPlayer, lastKey);
			} else {
				updateForSecondPlayerParent(state, lastPlayer, lastKey);
			}
	}

	protected void computeEpsilon() {
		double equationCount = lpTable.rowCount();
		double maxCoefficient = lpTable.getMaxCoefficient();

		epsilon.setValue(0.5 * Math.pow(equationCount, -equationCount - 1) * Math.pow(maxCoefficient, -2 * equationCount - 1));
	}

}
