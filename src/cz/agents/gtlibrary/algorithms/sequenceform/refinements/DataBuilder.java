package cz.agents.gtlibrary.algorithms.sequenceform.refinements;

import java.io.IOException;
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

public class DataBuilder extends TreeVisitor {
	
	protected String fileName;
	protected Data data;
	protected Player[] players;

	public static void main(String[] args) {
		runAoS();
//		runGoofSpiel();
//		runKuhnPoker();
//		runGenericPoker();
	}

	public static void runKuhnPoker() {
		AlgorithmConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
		DataBuilder lpBuilder = new DataBuilder(new KuhnPokerExpander<SequenceInformationSet>(algConfig), new KuhnPokerGameState(), algConfig, "kuhnPokerRepr");

		lpBuilder.buildLP();
		try {
			Runtime.getRuntime().exec("lemkeQP kuhnPokerRepr").waitFor();;
		} catch (IOException e) {
			System.err.println("Error during library invocation...");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		ResultParser parser = new ResultParser("kuhnPokerReprl1qp", lpBuilder.getP1IndicesOfSequences(), lpBuilder.getP2IndicesOfSequences());

		System.out.println(parser.getP1RealizationPlan());
		System.out.println(parser.getP2RealizationPlan());
		
		Strategy p1Strategy = new UniformStrategyForMissingSequences();
		Strategy p2Strategy = new UniformStrategyForMissingSequences();
		
		p1Strategy.putAll(parser.getP1RealizationPlan());
		p2Strategy.putAll(parser.getP2RealizationPlan());
		
		UtilityCalculator calculator = new UtilityCalculator(new KuhnPokerGameState(), new KuhnPokerExpander<SequenceInformationSet>(algConfig));
		
		System.out.println(calculator.computeUtility(p1Strategy, p2Strategy));
	}

	public static void runGenericPoker() {
		AlgorithmConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
		DataBuilder lpBuilder = new DataBuilder(new GenericPokerExpander<SequenceInformationSet>(algConfig), new GenericPokerGameState(), algConfig, "GenericPokerRepr");

		lpBuilder.buildLP();
		
		try {
			Runtime.getRuntime().exec("lemkeQP GenericPokerRepr").waitFor();;
		} catch (IOException e) {
			System.err.println("Error during library invocation...");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		ResultParser parser = new ResultParser("GenericPokerl1qp", lpBuilder.getP1IndicesOfSequences(), lpBuilder.getP2IndicesOfSequences());

		System.out.println(parser.getP1RealizationPlan());
		System.out.println(parser.getP2RealizationPlan());
		
		Strategy p1Strategy = new UniformStrategyForMissingSequences();
		Strategy p2Strategy = new UniformStrategyForMissingSequences();
		
		p1Strategy.putAll(parser.getP1RealizationPlan());
		p2Strategy.putAll(parser.getP2RealizationPlan());
		
		UtilityCalculator calculator = new UtilityCalculator(new GenericPokerGameState(), new GenericPokerExpander<SequenceInformationSet>(algConfig));
		
		System.out.println(calculator.computeUtility(p1Strategy, p2Strategy));
	}

	public static void runAoS() {
		AlgorithmConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
		DataBuilder lpBuilder = new DataBuilder(new AoSExpander<SequenceInformationSet>(algConfig), new AoSGameState(), algConfig, "AoSRepr");

		lpBuilder.buildLP();
		
		try {
			Runtime.getRuntime().exec("lemkeQP AoSRepr").waitFor();;
		} catch (IOException e) {
			System.err.println("Error during library invocation...");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		ResultParser parser = new ResultParser("AoSReprl1qp", lpBuilder.getP1IndicesOfSequences(), lpBuilder.getP2IndicesOfSequences());

		System.out.println(parser.getP1RealizationPlan());
		System.out.println(parser.getP2RealizationPlan());
		
		Strategy p1Strategy = new UniformStrategyForMissingSequences();
		Strategy p2Strategy = new UniformStrategyForMissingSequences();
		
		p1Strategy.putAll(parser.getP1RealizationPlan());
		p2Strategy.putAll(parser.getP2RealizationPlan());
		
		UtilityCalculator calculator = new UtilityCalculator(new AoSGameState(), new AoSExpander<SequenceInformationSet>(algConfig));
		
		System.out.println(calculator.computeUtility(p1Strategy, p2Strategy));

	}

	public static void runGoofSpiel() {
		AlgorithmConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
		DataBuilder lpBuilder = new DataBuilder(new GoofSpielExpander<SequenceInformationSet>(algConfig), new GoofSpielGameState(), algConfig, "GoofspielRepr");

		lpBuilder.buildLP();
		
		try {
			Runtime.getRuntime().exec("lemkeQP GoofspielRepr").waitFor();;
		} catch (IOException e) {
			System.err.println("Error during library invocation...");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		ResultParser parser = new ResultParser("GoofspielReprl1qp", lpBuilder.getP1IndicesOfSequences(), lpBuilder.getP2IndicesOfSequences());

		System.out.println(parser.getP1RealizationPlan());
		System.out.println(parser.getP2RealizationPlan());
		
		Strategy p1Strategy = new UniformStrategyForMissingSequences();
		Strategy p2Strategy = new UniformStrategyForMissingSequences();
		
		p1Strategy.putAll(parser.getP1RealizationPlan());
		p2Strategy.putAll(parser.getP2RealizationPlan());
		
		UtilityCalculator calculator = new UtilityCalculator(new GoofSpielGameState(), new GoofSpielExpander<SequenceInformationSet>(algConfig));
		
		System.out.println(calculator.computeUtility(p1Strategy, p2Strategy));
	}

	public DataBuilder(Expander<SequenceInformationSet> expander, GameState rootState, AlgorithmConfig<SequenceInformationSet> algConfig, String fileName) {
		super(rootState, expander, algConfig);
		this.expander = expander;
		this.fileName = fileName;
		this.players = rootState.getAllPlayers();
	}

	public void buildLP() {
		initData();
		visitTree(rootState, null, null);
		try {
			data.export(fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void initData() {
		data = new Data();

		initE();
		initF();
	}

	public void initF() {
		data.setF(new Key("Q", new LinkedListSequenceImpl(players[1])), new LinkedListSequenceImpl(players[1]), 1);//F in root (only 1)
		data.addToX2(new Key("Q", new LinkedListSequenceImpl(players[1])), new LinkedListSequenceImpl(players[1]));//empty sequence representation for initial strategy profile
	}

	public void initE() {
		data.setE(new Key("P", new LinkedListSequenceImpl(players[0])), new LinkedListSequenceImpl(players[0]), 1);//E in root (only 1)
		data.addToX1(new Key("P", new LinkedListSequenceImpl(players[0])), new LinkedListSequenceImpl(players[0]));//empty sequence representation for initial strategy profile
	}

	@Override
	protected void visitLeaf(GameState state, Player lastPlayer, Key lastKey) {
		updateSequences(state);
		updateParentLinks(state, lastPlayer, lastKey);
		data.addToU(state.getSequenceFor(players[0]), state.getSequenceFor(players[1]), state.getNatureProbability() * (state.getUtilities()[0]));
	}

	@Override
	protected void visitNormalNode(GameState state, Player lastPlayer, Key lastKey) {
		data.addISKeyFor(state.getPlayerToMove(), state.getISKeyForPlayerToMove());
		updateSequences(state);
		if (state.getPlayerToMove().getId() == 0) {
			updateLPForFirstPlayer(state, lastPlayer, lastKey);
		} else {
			updateLPForSecondPlayer(state, lastPlayer, lastKey);
		}
		super.visitNormalNode(state, lastPlayer, lastKey);
	}

	public void updateLPForFirstPlayer(GameState state, Player lastPlayer, Key lastKey) {
		Key eqKey = new Key("P", new Key(state.getISKeyForPlayerToMove()));

		updateParentLinks(state, lastPlayer, lastKey);
		data.setE(eqKey, state.getSequenceFor(players[0]), -1);//E
	}

	public void updateForFirstPlayerParent(GameState child, Player lastPlayer, Key eqKey) {
		data.setE(eqKey, child.getSequenceFor(lastPlayer), 1);//E child
		data.addToX1(eqKey, child.getSequenceFor(lastPlayer));
		data.addP1PerturbationsFor(child.getSequenceFor(lastPlayer));
	}

	public void updateLPForSecondPlayer(GameState state, Player lastPlayer, Key lastKey) {
		Key eqKey = new Key("Q", new Key(state.getISKeyForPlayerToMove()));

		updateParentLinks(state, lastPlayer, lastKey);
		data.setF(eqKey, state.getSequenceFor(players[1]), -1);//F
	}

	public void updateForSecondPlayerParent(GameState child, Player lastPlayer, Key eqKey) {
		data.setF(eqKey, child.getSequenceFor(lastPlayer), 1);//F child
		data.addToX2(eqKey, child.getSequenceFor(lastPlayer));
		data.addP2PerturbationsFor(child.getSequenceFor(lastPlayer));
	}

	@Override
	protected void visitChanceNode(GameState state, Player lastPlayer, Key lastKey) {
		updateSequences(state);
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

	public void updateSequences(GameState state) {
		data.addSequence(state.getSequenceFor(state.getAllPlayers()[0]));
		data.addSequence(state.getSequenceFor(state.getAllPlayers()[1]));
	}

	public Map<Integer, Sequence> getP1IndicesOfSequences() {
		return getRevertedMapping(data.getColumnIndicesE(), players[0]);
	}

	public Map<Integer, Sequence> getP2IndicesOfSequences() {
		return getRevertedMapping(data.getColumnIndicesF(), players[1]);
	}

	public Map<Integer, Sequence> getRevertedMapping(Map<Object, Integer> map, Player player) {
		Map<Integer, Sequence> p1Indices = new HashMap<Integer, Sequence>();

		for (Entry<Object, Integer> entry : map.entrySet()) {
			if (entry.getKey() instanceof Sequence)
				p1Indices.put(entry.getValue(), (Sequence) entry.getKey());
			else
				p1Indices.put(entry.getValue(), new LinkedListSequenceImpl(player));
		}
		return p1Indices;
	}
}
