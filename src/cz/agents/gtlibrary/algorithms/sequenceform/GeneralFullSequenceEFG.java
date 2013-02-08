package cz.agents.gtlibrary.algorithms.sequenceform;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

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
	private SequenceFormConfig algConfig;

	private PrintStream debugOutput = System.out;

	public static void main(String[] args) {
		GameState rootState = new KuhnPokerGameState();
		SequenceFormConfig algConfig = new SequenceFormConfig();
		GeneralFullSequenceEFG efg = new GeneralFullSequenceEFG(rootState, new KuhnPokerExpander<SequenceInformationSet>(algConfig), new KPGameInfo(), algConfig);

		efg.generate();

	}

	public GeneralFullSequenceEFG(GameState rootState, Expander<SequenceInformationSet> expander, GameInfo config, SequenceFormConfig algConfig) {
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
		overallSequenceGeneration = System.currentTimeMillis() - startGeneration;

		//		System.out.println("compatible sequences size " + dataStorage.getCompatibleSequences().size());
		//		System.out.println("IS P1: " + dataStorage.getInformationSetsForPlayer(rootState.getAllPlayers()[0]).size());
		//		System.out.println("IS P2: " + dataStorage.getInformationSetsForPlayer(rootState.getAllPlayers()[1]).size());

		//		for (PlayerID player : rootState.getAllPlayers()) {
		//			System.out.println("Sequences done for " + player + " " + (dataStorage.getSequencesFor(player) != null ? dataStorage.getSequencesFor(player).size() : 0));
		//		}

		Player[] actingPlayers = new Player[] { rootState.getAllPlayers()[0], rootState.getAllPlayers()[1] };

		//		algConfig.setLastIterationSequences(actingPlayers[0], algConfig.getSequencesFor(actingPlayers[0]));
		//		algConfig.setLastIterationSequences(actingPlayers[1], algConfig.getSequencesFor(actingPlayers[1]));

		long startCPLEX = System.currentTimeMillis();

		GeneralSequenceFormLP.calculateBothPlStrategy(rootState, algConfig, actingPlayers);

		long thisCPLEX = System.currentTimeMillis() - startCPLEX;

		overallCPLEX += thisCPLEX;

		for (Player player : rootState.getAllPlayers()) {
			realizationPlans.put(player, GeneralSequenceFormLP.resultStrategies.get(player));
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

		try {
			Runtime.getRuntime().gc();
			Thread.currentThread().sleep(500l);
		} catch (InterruptedException e) {
		}

		//		algConfig.ISExpValues = GeneralSequenceFormLP.expValues;
		//		GTBestResponseAlgorithm oracle = new BNBBestResponseAlgorithm(expander, 0, actingPlayers, dataStorage, this.gameConfig);
		//		System.out.println(oracle.calculateBR(curPar, realizationPlans.get(actingPlayers[1]), rootState, 10000));

		//		for (PlayerID playerID : rootState.getAllPlayers()) {
		//			System.out.println("final size for " + playerID + ": " + dataStorage.getSequencesFor(playerID).size());
		//		}
		//
		//		for (BasicPlayerID playerID : rootState.getAllPlayers()) {
		//			System.out.println("final result for " + playerID + ": " + GeneralSequenceFormLP.resultValues.get(playerID) /*+ ", " + tree.getIS(InformationSet.calculateISEquivalenceForPlayerToMove(rootState)).getValueOfGameForPlayer(playerID)*/);
		//		}
		//		System.out.println("final size: FirstPlayer Sequences: " + algConfig.getSequencesFor(actingPlayers[0]).size() + " \t SecondPlayer Sequences : " + algConfig.getSequencesFor(actingPlayers[1]).size());
		//		System.out.println("final IS_size: FirstPlayer IS: " + algConfig.getInformationSetsForPlayer(actingPlayers[0]).size() + " \t SecondPlayer IS: " + algConfig.getInformationSetsForPlayer(actingPlayers[1]).size());
		System.out.println("final support_size: FirstPlayer: " + support_size[0] + " \t SecondPlayer: " + support_size[1]);
		System.out.println("final result:" + GeneralSequenceFormLP.resultValues.get(actingPlayers[0]));
		System.out.println("final memory:" + ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024));

		System.out.println("final CPLEX time: " + overallCPLEX);
		System.out.println("final StrategyGenerating time: " + overallSequenceGeneration);
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

				if(!currentState.isPlayerToMoveNature())
					algConfig.addOutgoingSequenceFor(newState, currentState.getPlayerToMove());
				queue.add(newState);
				currentState.performAction(action);
			}
		}
	}
}
