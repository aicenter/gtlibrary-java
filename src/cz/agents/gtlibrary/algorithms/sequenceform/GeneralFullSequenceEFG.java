package cz.agents.gtlibrary.algorithms.sequenceform;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Scanner;

import cz.agents.gtlibrary.algorithms.mcts.BestResponseMCTSRunner;
import cz.agents.gtlibrary.algorithms.mcts.MCTSConfig;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.Simulator;
import cz.agents.gtlibrary.algorithms.mcts.backprop.SampleWeightedBackPropStrategy;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.UCTSelector;
import cz.agents.gtlibrary.domain.bpg.BPGExpander;
import cz.agents.gtlibrary.domain.bpg.BPGGameInfo;
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
import cz.agents.gtlibrary.utils.UtilityCalculator;

public class GeneralFullSequenceEFG {

	private GameState rootState;
	private Expander<SequenceInformationSet> expander;
	private Expander<MCTSInformationSet> firtstMCTSExpander;
	private Expander<MCTSInformationSet> secondMCTSExpander;
	private GameInfo gameConfig;
	private SequenceFormConfig<SequenceInformationSet> algConfig;
	private MCTSConfig firstMCTSConfig;
	private MCTSConfig secondMCTSConfig;

	private PrintStream debugOutput = System.out;

	public static void main(String[] args) {
//		runKuhnPoker();
//		runGenericPoker();
		runBPG();
	}

	public static void runKuhnPoker() {
		GameState rootState = new KuhnPokerGameState();
		KPGameInfo gameInfo = new KPGameInfo();
		SequenceFormConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
		MCTSConfig firstMCTSConfig = new MCTSConfig(new Simulator(1), new SampleWeightedBackPropStrategy.Factory(), new UCTSelector(getC(gameInfo.getMaxUtility())));
		MCTSConfig secondMCTSConfig = new MCTSConfig(new Simulator(1), new SampleWeightedBackPropStrategy.Factory(), new UCTSelector(getC(gameInfo.getMaxUtility())));
		Expander<MCTSInformationSet> firstMCTSExpander = new KuhnPokerExpander<MCTSInformationSet>(firstMCTSConfig);
		Expander<MCTSInformationSet> secondMCTSExpander = new KuhnPokerExpander<MCTSInformationSet>(secondMCTSConfig);
		GeneralFullSequenceEFG efg = new GeneralFullSequenceEFG(rootState, new KuhnPokerExpander<SequenceInformationSet>(algConfig), firstMCTSExpander, secondMCTSExpander, firstMCTSConfig, secondMCTSConfig, gameInfo, algConfig);

		efg.generate();
	}

	public static void runGenericPoker() {
		GameState rootState = new GenericPokerGameState();
		GPGameInfo gameInfo = new GPGameInfo();
		SequenceFormConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
		MCTSConfig firstMCTSConfig = new MCTSConfig(new Simulator(1), new SampleWeightedBackPropStrategy.Factory(), new UCTSelector(getC(gameInfo.getMaxUtility())));
		MCTSConfig secondMCTSConfig = new MCTSConfig(new Simulator(1), new SampleWeightedBackPropStrategy.Factory(), new UCTSelector(getC(gameInfo.getMaxUtility())));
		Expander<MCTSInformationSet> firstMCTSExpander = new GenericPokerExpander<MCTSInformationSet>(firstMCTSConfig);
		Expander<MCTSInformationSet> secondMCTSExpander = new GenericPokerExpander<MCTSInformationSet>(secondMCTSConfig);
		GeneralFullSequenceEFG efg = new GeneralFullSequenceEFG(rootState, new GenericPokerExpander<SequenceInformationSet>(algConfig), firstMCTSExpander, secondMCTSExpander, firstMCTSConfig, secondMCTSConfig, gameInfo, algConfig);

		efg.generate();
	}

	public static void runBPG() {
		GameState rootState = new BPGGameState();
		BPGGameInfo gameInfo = new BPGGameInfo();
		SequenceFormConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
		MCTSConfig firstMCTSConfig = new MCTSConfig(new Simulator(1), new SampleWeightedBackPropStrategy.Factory(), new UCTSelector(getC(gameInfo.getMaxUtility())));
		MCTSConfig secondMCTSConfig = new MCTSConfig(new Simulator(1), new SampleWeightedBackPropStrategy.Factory(), new UCTSelector(getC(gameInfo.getMaxUtility())));
		Expander<MCTSInformationSet> firstMCTSExpander = new BPGExpander<MCTSInformationSet>(firstMCTSConfig);
		Expander<MCTSInformationSet> secondMCTSExpander = new BPGExpander<MCTSInformationSet>(secondMCTSConfig);
		GeneralFullSequenceEFG efg = new GeneralFullSequenceEFG(rootState, new BPGExpander<SequenceInformationSet>(algConfig), firstMCTSExpander, secondMCTSExpander, firstMCTSConfig, secondMCTSConfig, gameInfo, algConfig);

		efg.generate();
	}

	private static double getC(double maxUtility) {
		return Math.sqrt(2) * maxUtility;
	}

	public GeneralFullSequenceEFG(GameState rootState, Expander<SequenceInformationSet> expander, Expander<MCTSInformationSet> firstMCTSExpander, Expander<MCTSInformationSet> secondMCTSExpander, MCTSConfig firstMCTSConfig, MCTSConfig secondMCTSConfig, GameInfo config, SequenceFormConfig<SequenceInformationSet> algConfig) {
		this.rootState = rootState;
		this.expander = expander;
		this.gameConfig = config;
		this.algConfig = algConfig;
		this.firtstMCTSExpander = firstMCTSExpander;
		this.secondMCTSExpander = secondMCTSExpander;
		this.firstMCTSConfig = firstMCTSConfig;
		this.secondMCTSConfig = secondMCTSConfig;
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
		GeneralSequenceFormLP sequenceFormLP = new GeneralSequenceFormLP(actingPlayers);

		sequenceFormLP.calculateBothPlStrategy(rootState, algConfig);

		long thisCPLEX = System.currentTimeMillis() - startCPLEX;

		overallCPLEX += thisCPLEX;

		for (Player player : rootState.getAllPlayers()) {
			realizationPlans.put(player, sequenceFormLP.getResultStrategiesForPlayer(player));
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
		System.out.println("final result:" + sequenceFormLP.getResultForPlayer(actingPlayers[0]));
		System.out.println("final memory:" + ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024));

		System.out.println("final CPLEX time: " + overallCPLEX);
		System.out.println("final StrategyGenerating time: " + overallSequenceGeneration);

		// sanity check -> calculation of Full BR on the solution of SQF LP
		SQFBestResponseAlgorithm brAlg = new SQFBestResponseAlgorithm(expander, 0, actingPlayers, algConfig, gameConfig);
		System.out.println("BR: " + brAlg.calculateBR(rootState, realizationPlans.get(actingPlayers[1])));

		SQFBestResponseAlgorithm brAlg2 = new SQFBestResponseAlgorithm(expander, 1, actingPlayers, algConfig, gameConfig);
		System.out.println("BR: " + brAlg2.calculateBR(rootState, realizationPlans.get(actingPlayers[0])));

		algConfig.validateGameStructure(rootState, expander);
		System.out.println("MCTS ready");
		new Scanner(System.in).next();
		BestResponseMCTSRunner mctsRunner = new BestResponseMCTSRunner(firstMCTSConfig, rootState, firtstMCTSExpander, realizationPlans.get(actingPlayers[1]), actingPlayers[1]);
		UtilityCalculator utility = new UtilityCalculator(rootState, firtstMCTSExpander);

		System.out.println("MCTS response: " + utility.computeUtility(mctsRunner.runMCTS(actingPlayers[0]), realizationPlans.get(actingPlayers[1])));

		utility = new UtilityCalculator(rootState, secondMCTSExpander);
		mctsRunner = new BestResponseMCTSRunner(secondMCTSConfig, rootState, secondMCTSExpander, realizationPlans.get(actingPlayers[0]), actingPlayers[0]);

		System.out.println("MCTS response: " + -utility.computeUtility(realizationPlans.get(actingPlayers[0]), mctsRunner.runMCTS(actingPlayers[1])));

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
