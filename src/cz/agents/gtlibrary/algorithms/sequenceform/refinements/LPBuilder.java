package cz.agents.gtlibrary.algorithms.sequenceform.refinements;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.UnknownObjectException;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.domain.aceofspades.AoSExpander;
import cz.agents.gtlibrary.domain.aceofspades.AoSGameState;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerExpander;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerGameState;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerExpander;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerGameState;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.strategy.Strategy;
import cz.agents.gtlibrary.strategy.UniformStrategyForMissingSequences;
import cz.agents.gtlibrary.utils.Pair;
import cz.agents.gtlibrary.utils.UtilityCalculator;

public class LPBuilder extends TreeVisitor {

	private double[][] lpTable;
	private String[][] varNames;
	private Map<Key, Integer> equations;
	private Map<Key, Integer> variables;
	private Map<Sequence, Integer> p1Indices;
	private Map<Sequence, Integer> p2Indices;
	private double utilityShift;
	private double epsilon;

	public static void main(String[] args) {
		AlgorithmConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
//		LPBuilder lpBuilder = new LPBuilder(new AoSExpander<SequenceInformationSet>(algConfig), new AoSGameState(), algConfig, 0.001);
//		LPBuilder lpBuilder = new LPBuilder(new KuhnPokerExpander<SequenceInformationSet>(algConfig), new KuhnPokerGameState(), algConfig, 0.001);
		LPBuilder lpBuilder = new LPBuilder(new GenericPokerExpander<SequenceInformationSet>(algConfig), new GenericPokerGameState(), algConfig, 0.001);

		lpBuilder.buildLP();
	}

	public LPBuilder(Expander<SequenceInformationSet> expander, GameState rootState, AlgorithmConfig<SequenceInformationSet> algConfig, double epsilon) {
		super(rootState, expander, algConfig);
		this.expander = expander;
		equations = new LinkedHashMap<Key, Integer>();
		variables = new LinkedHashMap<Key, Integer>();
		p1Indices = new HashMap<Sequence, Integer>();
		p2Indices = new HashMap<Sequence, Integer>();
		this.epsilon = epsilon;
	}

	public IloCplex buildLP() {
		initTable();
		visitTree(rootState);
//		for (double[] row : lpTable) {
//			System.out.println(Arrays.toString(row));
//		}
//		System.out.println();
//		for (String[] row : varNames) {
//			System.out.println(Arrays.toString(row));
//		}
//		System.out.println();
		IloCplex cplex = null;

		try {
			cplex = new IloCplex();
			populateCplex(cplex);
		} catch (IloException e) {
			e.printStackTrace();
		}

		return cplex;
	}

	private void populateCplex(IloCplex cplex) {
		double[] lb = new double[lpTable[0].length - 1];
		double[] ub = new double[lpTable[0].length - 1];
		String[] variableNames = new String[lpTable[0].length - 1];

		for (int i = 1; i < lpTable[0].length; i++) {
			ub[i - 1] = Double.POSITIVE_INFINITY;
		}
		for (Entry<Key, Integer> entry : variables.entrySet()) {
			if (entry.getValue() != 0)
				variableNames[entry.getValue() - 1] = entry.getKey().toString();
		}
		try {
			IloNumVar[] x = cplex.numVarArray(variableNames.length, lb, ub, variableNames);

			addObjective(cplex, x);

			IloRange[] constrains = addConstrains(cplex, x);
			cplex.exportModel("lp.lp");
			System.out.println(cplex.solve());
			System.out.println(cplex.getStatus());
			System.out.println(cplex.getObjValue());

			Map<Sequence, Double> p1RealizationPlan = createFirstPlayerStrategy(cplex, constrains);
			Map<Sequence, Double> p2RealizationPlan = createSecondPlayerStrategy(cplex, x);

			System.out.println(p1RealizationPlan);
			System.out.println(p2RealizationPlan);
			UtilityCalculator calculator = new UtilityCalculator(rootState, expander);
			Strategy p1Strategy = new UniformStrategyForMissingSequences();
			Strategy p2Strategy = new UniformStrategyForMissingSequences();
			
			p1Strategy.putAll(p1RealizationPlan);
			p2Strategy.putAll(p2RealizationPlan);
			
			System.out.println(calculator.computeUtility(p1Strategy, p2Strategy));
		} catch (IloException e) {
			e.printStackTrace();
		}

	}

	public Map<Sequence, Double> createFirstPlayerStrategy(IloCplex cplex, IloRange[] constrains) throws UnknownObjectException, IloException {
		Map<Sequence, Double> p1RealizationPlan = new HashMap<Sequence, Double>();
		for (Entry<Sequence, Integer> entry : p1Indices.entrySet()) {
			p1RealizationPlan.put(entry.getKey(), -cplex.getDual(constrains[entry.getValue()]));
		}
		return p1RealizationPlan;
	}

	public Map<Sequence, Double> createSecondPlayerStrategy(IloCplex cplex, IloNumVar[] x) throws UnknownObjectException, IloException {
		Map<Sequence, Double> p2RealizationPlan = new HashMap<Sequence, Double>();
		
		for (Entry<Sequence, Integer> entry : p2Indices.entrySet()) {
			p2RealizationPlan.put(entry.getKey(), cplex.getValue(x[entry.getValue()]));
		}
		return p2RealizationPlan;
	}

	public IloRange[] addConstrains(IloCplex cplex, IloNumVar[] x) throws IloException {
		IloRange[] constrains = new IloRange[lpTable.length - 1];

		for (int i = 1; i < lpTable.length; i++) {
			IloLinearNumExpr constrain = cplex.linearNumExpr();

			for (int j = 0; j < x.length; j++) {
				constrain.addTerm(x[j], lpTable[i][j + 1]);
			}
			constrains[i - 1] = cplex.addGe(constrain, -lpTable[i][0]);
		}
		return constrains;
	}

	public void addObjective(IloCplex cplex, IloNumVar[] x) throws IloException {
		IloLinearNumExpr objective = cplex.linearNumExpr();

		for (int i = 0; i < x.length; i++) {
			objective.addTerm(x[i], lpTable[0][i + 1]);
		}
		cplex.addMaximize(objective);
	}

	public void initTable() {
		Pair<Integer, Integer> sizes = getLPSize();

		lpTable = new double[sizes.getLeft()][sizes.getRight()];
		varNames = new String[sizes.getLeft()][sizes.getRight()];

		initCost();
		initE();
		initF();
		initf();
	}

	public void initf() {
		int equationIndex = getIndex(new Key("Q", lastKeys[1]), equations);
		int variableIndex = getIndex(new Key("cons"), variables);

		lpTable[equationIndex][variableIndex] = -1;
		varNames[equationIndex][variableIndex] = "f_root";
	}

	public void initF() {
		int equationIndex = getIndex(new Key("Q", lastKeys[1]), equations);
		int variableIndex = getIndex(lastKeys[1], variables);

		lpTable[equationIndex][variableIndex] = 1;//hodnota v rootu
		varNames[equationIndex][variableIndex] = "F_root";
	}

	public void initE() {
		int equationIndex = getIndex(lastKeys[0], equations);
		int variableIndex = getIndex(new Key("P", lastKeys[0]), variables);

		lpTable[equationIndex][variableIndex] = 1;//hodnota v rootu
		varNames[equationIndex][variableIndex] = "E_root";
	}

	public void initCost() {
		int equationIndex = getIndex(new Key("COST"), equations);
		getIndex(new Key("cons"), variables);
		int variableIndex = getIndex(new Key("P", lastKeys[0]), variables);

		lpTable[equationIndex][variableIndex] = -1;
		varNames[equationIndex][variableIndex] = "COST";
	}

	private int getIndex(Key key, Map<Key, Integer> map) {
		Integer result = map.get(key);

		if (result == null) {
			result = map.size();
			map.put(key, result);
		}
		return result;
	}

	private Pair<Integer, Integer> getLPSize() {
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
		int p1realidx = getIndex(lastKeys[0], equations);
		int p2realidx = getIndex(lastKeys[1], variables);

		lpTable[p1realidx][p2realidx] -= state.getNatureProbability() * (state.getUtilities()[0] + utilityShift);//A
		varNames[p1realidx][p2realidx] = "A{" + lastKeys[0] + ", " + lastKeys[1] + "}";
	}

	@Override
	protected void visitNormalNode(GameState state) {
		int equationIndex, variableIndex;
		switch (state.getPlayerToMove().getId()) {
		case 0:
			equationIndex = getIndex(lastKeys[0], equations);
			variableIndex = getIndex(new Key("P", new Key(state.getISKeyForPlayerToMove())), variables);
			p1Indices.put(state.getSequenceForPlayerToMove(), equationIndex - 1);
			lpTable[equationIndex][variableIndex] = -1;//E
			varNames[equationIndex][variableIndex] = "E_" + lastKeys[0];
			for (Action action : expander.getActions(state)) {
				GameState child = state.performAction(action);

				equationIndex = getIndex(new Key(child.getSequenceFor(state.getPlayerToMove())), equations);
				p1Indices.put(child.getSequenceFor(state.getAllPlayers()[0]), equationIndex - 1);
				lpTable[equationIndex][variableIndex] = 1;//E
				varNames[equationIndex][variableIndex] = "E_" + child.getISKeyForFirstPlayer();
				int tmpidx = getIndex(new Key("U", new Key(child.getSequenceFor(state.getPlayerToMove()))), variables);
				lpTable[equationIndex][tmpidx] = -1;//u (eye)
				varNames[equationIndex][variableIndex] = "uIdx_" + child.getISKeyForFirstPlayer();
				lpTable[0][tmpidx] = Math.pow(epsilon, child.getSequenceFor(state.getAllPlayers()[0]).size());//k(\epsilon)
				varNames[0][tmpidx] = "k_" + child.getISKeyForFirstPlayer();
			}
			break;
		case 1:
			equationIndex = getIndex(new Key("Q", new Key(state.getISKeyForPlayerToMove())), equations);
			variableIndex = getIndex(lastKeys[1], variables);
			p2Indices.put(state.getSequenceForPlayerToMove(), variableIndex - 1);
			lpTable[equationIndex][variableIndex] = -1;//F
			varNames[equationIndex][variableIndex] = "F_" + lastKeys[1];//F
			for (Action action : expander.getActions(state)) {
				GameState child = state.performAction(action);

				variableIndex = getIndex(new Key(child.getSequenceFor(state.getPlayerToMove())), variables);
				p2Indices.put(child.getSequenceFor(state.getAllPlayers()[1]), variableIndex - 1);
				lpTable[equationIndex][variableIndex] = 1;//F
				varNames[equationIndex][variableIndex] = "F_" + child.getISKeyForSecondPlayer();
				int tmpidx = getIndex(new Key("V", new Key(child.getSequenceFor(state.getPlayerToMove()))), equations);
				lpTable[tmpidx][variableIndex] = 1;//indexy y
				varNames[tmpidx][variableIndex] = "yIdx_" + child.getISKeyForSecondPlayer();
				lpTable[tmpidx][0] = -Math.pow(epsilon, child.getSequenceFor(state.getAllPlayers()[1]).size());//l(\epsilon)
				varNames[tmpidx][0] = "l_" + child.getISKeyForSecondPlayer();
			}
			break;
		}
		super.visitNormalNode(state);
	}

}
