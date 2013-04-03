package cz.agents.gtlibrary.algorithms.sequenceform;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import cz.agents.gtlibrary.domain.bpg.BPGExpander;
import cz.agents.gtlibrary.domain.bpg.BPGGameInfo;
import cz.agents.gtlibrary.domain.bpg.BPGGameState;
import cz.agents.gtlibrary.domain.goofspiel.GSGameInfo;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielExpander;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielGameState;
import cz.agents.gtlibrary.domain.poker.generic.GPGameInfo;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerExpander;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerGameState;
import cz.agents.gtlibrary.domain.poker.kuhn.KPGameInfo;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerExpander;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerGameState;
import cz.agents.gtlibrary.domain.randomgame.RandomGameExpander;
import cz.agents.gtlibrary.domain.randomgame.RandomGameInfo;
import cz.agents.gtlibrary.domain.randomgame.RandomGameState;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;

public class FullSequenceEFG {

	private GameState rootState;
	private Expander<SequenceInformationSet> expander;
	private GameInfo gameConfig;
	private SequenceFormConfig<SequenceInformationSet> algConfig;

	private PrintStream debugOutput = System.out;

	public static void main(String[] args) {
//		runKuhnPoker();
		runGenericPoker();
//		runBPG();
//		runGoofSpiel();
//      runRandomGame();
	}

	public static void runKuhnPoker() {
		GameState rootState = new KuhnPokerGameState();
		KPGameInfo gameInfo = new KPGameInfo();
		SequenceFormConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
		FullSequenceEFG efg = new FullSequenceEFG(rootState, new KuhnPokerExpander<SequenceInformationSet>(algConfig), gameInfo, algConfig);

		efg.generate();
	}

    public static void runRandomGame() {
        GameState rootState = new RandomGameState();
        GameInfo gameInfo = new RandomGameInfo();
        SequenceFormConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
        FullSequenceEFG efg = new FullSequenceEFG(rootState, new RandomGameExpander<SequenceInformationSet>(algConfig), gameInfo, algConfig);

        efg.generate();
    }


    public static void runGoofSpiel() {
		GameState rootState = new GoofSpielGameState();
		GSGameInfo gameInfo = new GSGameInfo();
		SequenceFormConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
		FullSequenceEFG efg = new FullSequenceEFG(rootState, new GoofSpielExpander<SequenceInformationSet>(algConfig), gameInfo, algConfig);

		efg.generate();
	}

	public static void runGenericPoker() {
		GameState rootState = new GenericPokerGameState();
		GPGameInfo gameInfo = new GPGameInfo();
		SequenceFormConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
		FullSequenceEFG efg = new FullSequenceEFG(rootState, new GenericPokerExpander<SequenceInformationSet>(algConfig), gameInfo, algConfig);

		efg.generate();
	}

	public static void runBPG() {
		GameState rootState = new BPGGameState();
		BPGGameInfo gameInfo = new BPGGameInfo();
		SequenceFormConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
		FullSequenceEFG efg = new FullSequenceEFG(rootState, new BPGExpander<SequenceInformationSet>(algConfig), gameInfo, algConfig);

		efg.generate();
	}

	public FullSequenceEFG(GameState rootState, Expander<SequenceInformationSet> expander, GameInfo config, SequenceFormConfig<SequenceInformationSet> algConfig) {
		this.rootState = rootState;
		this.expander = expander;
		this.gameConfig = config;
		this.algConfig = algConfig;
	}

	public Map<Player, Map<Sequence, Double>> generate() {
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
		SequenceFormLP sequenceFormLP = new SequenceFormLP(actingPlayers);

		sequenceFormLP.calculateBothPlStrategy(rootState, algConfig);

		long thisCPLEX = System.currentTimeMillis() - startCPLEX;

		overallCPLEX += thisCPLEX;

		for (Player player : rootState.getAllPlayers()) {
			realizationPlans.put(player, sequenceFormLP.getResultStrategiesForPlayer(player));
		}

		System.out.println("done.");
        long finishTime = System.currentTimeMillis() - start;

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
            Thread.sleep(500l);
        } catch (InterruptedException e) {
        }

		//		for (BasicPlayerID playerID : rootState.getAllPlayers()) {
		//			System.out.println("final result for " + playerID + ": " + SequenceFormLP.resultValues.get(playerID) /*+ ", " + tree.getIS(InformationSet.calculateISEquivalenceForPlayerToMove(rootState)).getValueOfGameForPlayer(playerID)*/);
		//		}

        System.out.println("final size: FirstPlayer Sequences: " + algConfig.getSequencesFor(actingPlayers[0]).size() + " \t SecondPlayer Sequences : " + algConfig.getSequencesFor(actingPlayers[1]).size());
        System.out.println("final support_size: FirstPlayer: " + support_size[0] + " \t SecondPlayer: " + support_size[1]);
        System.out.println("final result:" + sequenceFormLP.getResultForPlayer(actingPlayers[0]));
		System.out.println("final memory:" + ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024));
        System.out.println("final time: " + finishTime);
        System.out.println("final CPLEX time: " + overallCPLEX);
        System.out.println("final BR time: " + 0);
        System.out.println("final RGB time: " + 0);
        System.out.println("final StrategyGenerating time: " + overallSequenceGeneration);

		// sanity check -> calculation of Full BR on the solution of SQF LP
//		SQFBestResponseAlgorithm brAlg = new SQFBestResponseAlgorithm(expander, 0, actingPlayers, algConfig, gameConfig);
//		System.out.println("BR: " + brAlg.calculateBR(rootState, realizationPlans.get(actingPlayers[1])));
//
//		SQFBestResponseAlgorithm brAlg2 = new SQFBestResponseAlgorithm(expander, 1, actingPlayers, algConfig, gameConfig);
//		System.out.println("BR: " + brAlg2.calculateBR(rootState, realizationPlans.get(actingPlayers[0])));
//
//        algConfig.validateGameStructure(rootState, expander);

		return realizationPlans;
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
