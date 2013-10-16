package cz.agents.gtlibrary.algorithms.sequenceform.refinements.nfp;

import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.Key;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPData;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.TreeVisitor;
import cz.agents.gtlibrary.domain.upordown.UDExpander;
import cz.agents.gtlibrary.domain.upordown.UDGameState;
import cz.agents.gtlibrary.iinodes.ArrayListSequenceImpl;
import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.Pair;

public class SecondNFPBuilder extends TreeVisitor {

	protected String lpFileName;
	protected NFPTable lpTable;
	protected double valueOfGame;

	public static void main(String[] args) {
//		runAoS();
//		runGoofSpiel();
//		runKuhnPoker();
//		runGenericPoker();
		runUpOrDown();
	}

	private static void runUpOrDown() {
		AlgorithmConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
		SecondNFPBuilder lpBuilder = new SecondNFPBuilder(new UDExpander<SequenceInformationSet>(algConfig), new UDGameState(), algConfig, 0);

		lpBuilder.buildLP();
		lpBuilder.solve();
	}

	public SecondNFPBuilder(Expander<SequenceInformationSet> expander, GameState rootState, AlgorithmConfig<SequenceInformationSet> algConfig, double valueOfGame) {
		super(rootState, expander, algConfig);
		this.expander = expander;
		this.valueOfGame = valueOfGame;
		lpFileName = "secondNFP.lp";
	}

	public void buildLP() {
		initTable();
		visitTree(rootState);
	}

	public void solve() {
		try {
			LPData lpData = lpTable.toCplex();

			lpData.getSolver().exportModel(lpFileName);
			System.out.println(lpData.getSolver().solve());
			System.out.println(lpData.getSolver().getStatus());
			System.out.println(lpData.getSolver().getObjValue());
//			System.out.println(Arrays.toString(lpData.getSolver().getValues(lpData.getVariables())));
			for (int i = 0; i < lpData.getVariables().length; i++) {
				System.out.println(lpData.getVariables()[i] + ": " + lpData.getSolver().getValue(lpData.getVariables()[i]));
			}
//			Map<Sequence, Double> p1RealizationPlan = createFirstPlayerStrategy(lpData.getSolver(), lpData.getWatchedPrimalVariables());
//
//			for (Entry<Sequence, Double> entry : p1RealizationPlan.entrySet()) {
//				if (entry.getValue() > 0)
//					System.out.println(entry);
//			}
		} catch (IloException e) {
			e.printStackTrace();
		}

	}

	public Map<Sequence, Double> createFirstPlayerStrategy(IloCplex cplex, Map<Object, IloNumVar> watchedPrimalVars) throws IloException {
		Map<Sequence, Double> p1Strategy = new HashMap<Sequence, Double>();

		for (Entry<Object, IloNumVar> entry : watchedPrimalVars.entrySet()) {
			p1Strategy.put((Sequence) entry.getKey(), cplex.getValue(entry.getValue()));
		}
		return p1Strategy;
	}

//	public Map<Sequence, Double> createSecondPlayerStrategy(IloCplex cplex, Map<Object, IloNumVar> watchedPrimalVars) throws IloException {
//		Map<Sequence, Double> p2Strategy = new HashMap<Sequence, Double>();
//
//		for (Entry<Object, IloNumVar> entry : watchedPrimalVars.entrySet()) {
//			p2Strategy.put((Sequence) entry.getKey(), cplex.getValue(entry.getValue()));
//		}
//		return p2Strategy;
//	}

	public void initTable() {
		Sequence p1EmptySequence = new ArrayListSequenceImpl(players[0]);
		Sequence p2EmptySequence = new ArrayListSequenceImpl(players[1]);

		lpTable = new NFPTable();

//		initCost(p2EmptySequence);
		initE(p1EmptySequence);
		initF(p2EmptySequence);
		inite();
		addPreviousItConstraints(p2EmptySequence);
	}

	private void addPreviousItConstraints(Sequence p2EmptySequence) {
		lpTable.setConstraint("prevIt", players[1], 1);
		lpTable.setConstraint("prevIt", "s", -valueOfGame);
		lpTable.setConstraintType("prevIt", 1);
		lpTable.setLowerBound("s", 1);
	}

	public void inite() {
		lpTable.setConstraint(players[0], "s", -1);//e for root
	}

	public void initF(Sequence p2EmptySequence) {
		lpTable.setConstraint(p2EmptySequence, players[1], 1);//F in root (only 1)
		lpTable.setConstraintType(p2EmptySequence, 0);
		lpTable.setLowerBound(players[1], Double.NEGATIVE_INFINITY);
	}

	public void initE(Sequence p1EmptySequence) {
		lpTable.setConstraint(players[0], p1EmptySequence, 1);//E in root (only 1)
		lpTable.setConstraintType(players[0], 1);
	}

	public void initCost(Sequence p2EmptySequence) {
		lpTable.setObjective(players[1], 1);
	}

	@Override
	protected void visitLeaf(GameState state) {
		updateParentLinks(state);
		lpTable.substractFromConstraint(state.getSequenceFor(players[1]), state.getSequenceFor(players[0]), state.getNatureProbability() * state.getUtilities()[0]);
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
		Object varKey = state.getSequenceForPlayerToMove();

		updateParentLinks(state);
		lpTable.setConstraint(state.getISKeyForPlayerToMove(), varKey, -1);//E
		lpTable.setConstraintType(state.getISKeyForPlayerToMove(), 1);
		lpTable.setLowerBound(varKey, 0);
		lpTable.watchPrimalVariable(state.getSequenceFor(players[0]), state.getSequenceForPlayerToMove());
	}

	public void updateLPForSecondPlayer(GameState state) {
		Object eqKey = state.getSequenceForPlayerToMove();

		updateParentLinks(state);
		lpTable.setConstraint(eqKey, state.getISKeyForPlayerToMove(), -1);//F
		lpTable.setConstraintType(eqKey, 0);
		lpTable.setLowerBound(state.getISKeyForPlayerToMove(), Double.NEGATIVE_INFINITY);
		addU(eqKey);
	}

	private void addU(Object eqKey) {
		Key uKey = new Key("u", eqKey);
		
		lpTable.setConstraint(eqKey, uKey, 1);
		lpTable.setLowerBound(uKey, 0);
		lpTable.setUpperBound(uKey, 1);
		lpTable.setObjective(uKey, 1);
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
		Object eqKey = getLastISKey(p1Sequence);

		lpTable.watchPrimalVariable(p1Sequence, p1Sequence);
		lpTable.setConstraint(eqKey, p1Sequence, 1);//E child
		lpTable.setLowerBound(p1Sequence, 0);
	}

	protected void updateP2Parent(GameState state) {
		Sequence p2Sequence = state.getSequenceFor(players[1]);

		if (p2Sequence.size() == 0)
			return;
		Object varKey = getLastISKey(p2Sequence);

		lpTable.setConstraint(p2Sequence, varKey, 1);//F child
		lpTable.setConstraintType(p2Sequence, 0);
		lpTable.setLowerBound(varKey, Double.NEGATIVE_INFINITY);
		addU(p2Sequence);
	}

	protected Object getLastISKey(Sequence sequence) {
		InformationSet informationSet = sequence.getLastInformationSet();

		return new Pair<Integer, Sequence>(informationSet.hashCode(), informationSet.getPlayersHistory());
	}

}
