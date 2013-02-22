package cz.agents.gtlibrary.algorithms.sequenceform;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import cz.agents.gtlibrary.algorithms.mcts.BestResponseMCTSRunner;
import cz.agents.gtlibrary.algorithms.mcts.MCTSConfig;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.Simulator;
import cz.agents.gtlibrary.algorithms.mcts.backprop.SampleWeightedBackPropStrategy;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.UCTSelector;
import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.DoubleOracleBestResponse;
import cz.agents.gtlibrary.domain.bpg.BPGGameInfo;
import cz.agents.gtlibrary.domain.bpg.BPGExpander;
import cz.agents.gtlibrary.domain.bpg.BPGGameState;
import cz.agents.gtlibrary.domain.poker.generic.GPGameInfo;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerExpander;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerGameState;
import cz.agents.gtlibrary.domain.poker.kuhn.KPGameInfo;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerExpander;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerGameState;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;

public class GeneralFullSequenceEFG {

	private GameState rootState;
	private Expander<SequenceInformationSet> expander;
	private GameInfo gameConfig;
	private SequenceFormConfig<SequenceInformationSet> algConfig;

	private PrintStream debugOutput = System.out;

	public static void main(String[] args) {
//		GameState rootState = new KuhnPokerGameState();
//		SequenceFormConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
//		GeneralFullSequenceEFG efg = new GeneralFullSequenceEFG(rootState, new KuhnPokerExpander<SequenceInformationSet>(algConfig), new KPGameInfo(), algConfig);

//		GameState rootState = new GenericPokerGameState();
//		SequenceFormConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
//		GeneralFullSequenceEFG efg = new GeneralFullSequenceEFG(rootState, new GenericPokerExpander<SequenceInformationSet>(algConfig), new GPGameInfo(), algConfig);

		GameState rootState = new BPGGameState();
		SequenceFormConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
		GeneralFullSequenceEFG efg = new GeneralFullSequenceEFG(rootState, new BPGExpander<SequenceInformationSet>(algConfig), new BPGGameInfo(), algConfig);
		
		efg.generate();
	}

	public GeneralFullSequenceEFG(GameState rootState, Expander<SequenceInformationSet> expander, GameInfo config, SequenceFormConfig<SequenceInformationSet> algConfig) {
		this.rootState = rootState;
		this.expander = expander;
		this.gameConfig = config;
		this.algConfig = algConfig;
	}

	public void generate() {
		debugOutput.println("Full Sequence");
		debugOutput.println(gameConfig.getInfo());

		long start = System.currentTimeMillis();
		long overallSequenceGeneration = 0;
		long overallCPLEX = 0;
		Map<Player, Map<Sequence, Double>> realizationPlans = new HashMap<Player, Map<Sequence, Double>>();
		long startGeneration = System.currentTimeMillis();

		generateCompleteGame();
		System.out.println("Game tree built...");
		System.out.println("Information set count: " + algConfig.getAllInformationSets().size());
		overallSequenceGeneration = System.currentTimeMillis() - startGeneration;

		Player[] actingPlayers = new Player[] { rootState.getAllPlayers()[0], rootState.getAllPlayers()[1] };
		long startCPLEX = System.currentTimeMillis();
		GeneralSequenceFormLP sequenceFormLP = new GeneralSequenceFormLP(actingPlayers);

		sequenceFormLP.calculateBothPlStrategy(rootState, algConfig);

		long thisCPLEX = System.currentTimeMillis() - startCPLEX;

		overallCPLEX += thisCPLEX;

		for (Player player : rootState.getAllPlayers()) {
			realizationPlans.put(player, sequenceFormLP.resultStrategies.get(player));
		}

		System.out.println("done.");
		System.out.println("Time elapsed: " + (System.currentTimeMillis() - start));

		int[] support_size = new int[] { 0, 0 };
		for (Player player : actingPlayers) {
			for (Sequence sequence : realizationPlans.get(player).keySet()) {
				if (realizationPlans.get(player).get(sequence) > 0) {
					support_size[player.getId()]++;
					//	System.out.println(sequence + "\t:\t" + realizationPlans.get(playerID).get(sequence) /*+ ", " + tree.getProbabilityOfSequenceFromAverageStrategy(sequence)*/);
				}
			}
		}

		//		Runtime.getRuntime().gc();

		//		for (BasicPlayerID playerID : rootState.getAllPlayers()) {
		//			System.out.println("final result for " + playerID + ": " + GeneralSequenceFormLP.resultValues.get(playerID) /*+ ", " + tree.getIS(InformationSet.calculateISEquivalenceForPlayerToMove(rootState)).getValueOfGameForPlayer(playerID)*/);
		//		}
		
		System.out.println("final support_size: FirstPlayer: " + support_size[0] + " \t SecondPlayer: " + support_size[1]);
		System.out.println("final result:" + sequenceFormLP.resultValues.get(actingPlayers[0]));
		System.out.println("final memory:" + ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024));

		System.out.println("final CPLEX time: " + overallCPLEX);
		System.out.println("final StrategyGenerating time: " + overallSequenceGeneration);
		
		SQFBestResponseAlgorithm brAlg = new SQFBestResponseAlgorithm(expander, 0, actingPlayers, algConfig, gameConfig);
		System.out.println("BR: " + brAlg.calculateBR(rootState, realizationPlans.get(actingPlayers[1])));
		SQFBestResponseAlgorithm brAlg2 = new SQFBestResponseAlgorithm(expander, 1, actingPlayers, algConfig, gameConfig);
		System.out.println("BR: " + brAlg2.calculateBR(rootState, realizationPlans.get(actingPlayers[0])));
		
		MCTSConfig algConfig = new MCTSConfig(new Simulator(2), new SampleWeightedBackPropStrategy.Factory(), new UCTSelector(1));

		BestResponseMCTSRunner mctsRunner = new BestResponseMCTSRunner(algConfig, rootState, new BPGExpander<MCTSInformationSet>(algConfig), realizationPlans.get(actingPlayers[1]), actingPlayers[1]);
		System.out.println("*********************");
		for (int i = 0; i < 2000; i++) {
			mctsRunner.runMcts(5000);
		}
		System.out.println("*********************");
		algConfig = new MCTSConfig(new Simulator(2), new SampleWeightedBackPropStrategy.Factory(), new UCTSelector(1));
		mctsRunner = new BestResponseMCTSRunner(algConfig, rootState, new BPGExpander<MCTSInformationSet>(algConfig), realizationPlans.get(actingPlayers[0]), actingPlayers[0]);
		for (int i = 0; i < 2000; i++) {
			mctsRunner.runMcts(5000);
		}

	}

	public void generateCompleteGame() {
		LinkedList<GameState> queue = new LinkedList<GameState>();

		queue.add(rootState);

		while (queue.size() > 0) {
			GameState currentState = queue.removeFirst();

			algConfig.addStateToSequenceForm(currentState);
			if (currentState.isGameEnd()) {
				algConfig.setUtility(currentState);
				continue;
			}
			for (Action action : expander.getActions(currentState)) {
				GameState newState = currentState.performAction(action);

				queue.add(newState);
				currentState.performAction(action);
			}
		}
	}
}
