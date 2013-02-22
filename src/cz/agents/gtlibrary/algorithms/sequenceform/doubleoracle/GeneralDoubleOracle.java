package cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import cz.agents.gtlibrary.algorithms.sequenceform.GeneralFullSequenceEFG;
import cz.agents.gtlibrary.algorithms.sequenceform.GeneralSequenceFormLP;
import cz.agents.gtlibrary.algorithms.sequenceform.SQFBestResponseAlgorithm;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.domain.bpg.BPGExpander;
import cz.agents.gtlibrary.domain.bpg.BPGGameInfo;
import cz.agents.gtlibrary.domain.bpg.BPGGameState;
import cz.agents.gtlibrary.iinodes.LinkedListSequenceImpl;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.FixedSizeMap;

public class GeneralDoubleOracle {
	private GameState rootState;
	private Expander<DoubleOracleInformationSet> expander;
	private GameInfo gameConfig;
	private DoubleOracleConfig<DoubleOracleInformationSet> algConfig;

	private PrintStream debugOutput = System.out;
	
	final private double EPS = 0.000000001; 

	public static void main(String[] args) {
//		GameState rootState = new KuhnPokerGameState();
//		SequenceFormConfig algConfig = new SequenceFormConfig();
//		GeneralFullSequenceEFG efg = new GeneralFullSequenceEFG(rootState, new KuhnPokerExpander<SequenceInformationSet>(algConfig), new KPGameInfo(), algConfig);

//		GameState rootState = new GenericPokerGameState();
//		SequenceFormConfig algConfig = new SequenceFormConfig();
//		GeneralFullSequenceEFG efg = new GeneralFullSequenceEFG(rootState, new GenericPokerExpander<SequenceInformationSet>(algConfig), new GPGameInfo(), algConfig);

		GameState rootState = new BPGGameState();
		GameInfo gameInfo = new BPGGameInfo();
		DoubleOracleConfig<DoubleOracleInformationSet> algConfig = new DoubleOracleConfig<DoubleOracleInformationSet>(rootState, gameInfo);
		Expander<DoubleOracleInformationSet> expander = new BPGExpander<DoubleOracleInformationSet>(algConfig);
		GeneralDoubleOracle doefg = new GeneralDoubleOracle(rootState, expander, gameInfo, algConfig);
		
		doefg.generate();
	}

	public GeneralDoubleOracle (GameState rootState, Expander<DoubleOracleInformationSet> expander, GameInfo config, DoubleOracleConfig<DoubleOracleInformationSet> algConfig) {
		this.rootState = rootState;
		this.expander = expander;
		this.gameConfig = config;
		this.algConfig = algConfig;
	}

	public void generate() {
		debugOutput.println("Double Oracle");
		debugOutput.println(gameConfig.getInfo());
		
		long start = System.currentTimeMillis();
		long overallSequenceGeneration = 0;
		long overallBRCalculation = 0;
		long overallCompatibilityAlgorithm = 0;
		long overallCPLEX = 0;
		int iterations = 0;

		int firstPlayerSequencesBRAdded = 0;
		int secondPlayerSequencesBRAdded = 0;

		Player[] actingPlayers = new Player[] { rootState.getAllPlayers()[0], rootState.getAllPlayers()[1] };
		
		algConfig.addStateToSequenceForm(rootState);
		
		DoubleOracleBestResponse[] brAlgorithms = new DoubleOracleBestResponse[] { 
					new DoubleOracleBestResponse(expander, 0, actingPlayers, algConfig, gameConfig),
					new DoubleOracleBestResponse(expander, 1, actingPlayers, algConfig, gameConfig)};

		// init realization plans -> for each player, an empty sequence has probability equal to 1
		Map<Player, Map<Sequence, Double>> realizationPlans = new FixedSizeMap<Player, Map<Sequence, Double>>(2);
		realizationPlans.put(actingPlayers[0], new HashMap<Sequence, Double>());
		realizationPlans.put(actingPlayers[1], new HashMap<Sequence, Double>());
		realizationPlans.get(actingPlayers[0]).put(rootState.getSequenceFor(actingPlayers[0]), 1d);
		realizationPlans.get(actingPlayers[1]).put(rootState.getSequenceFor(actingPlayers[1]), 1d);
		
		int currentPlayerIndex = 0;
		GeneralSequenceFormLP sequenceFormLP = new GeneralSequenceFormLP(actingPlayers);
		
		double ubUtility = gameConfig.getMaxUtility();
		double lbUtility = -gameConfig.getMaxUtility();
		
		while ((Math.abs(ubUtility) - Math.abs(lbUtility)) < EPS) {
			
			iterations++;
			
			int opponentPlayerIndex = ( currentPlayerIndex + 1 ) % 2;
			
			long startFullBR = System.currentTimeMillis();
			long thisBR = 0;
			double currentBRVal = brAlgorithms[currentPlayerIndex].calculateBR(rootState, realizationPlans.get(actingPlayers[opponentPlayerIndex]));

			thisBR = System.currentTimeMillis() - startFullBR;
			System.out.println("Iteration " + iterations + " : full BR time : " + thisBR);
			overallBRCalculation += thisBR;

			HashSet<Sequence> currentFullBRSequences = brAlgorithms[currentPlayerIndex].getFullBRSequences();
			algConfig.createValidRestrictedGame(actingPlayers[currentPlayerIndex], currentFullBRSequences, brAlgorithms[currentPlayerIndex], expander);
			if (currentPlayerIndex == 0) {
				ubUtility = Math.min(ubUtility, currentBRVal);
			} else {
				lbUtility = Math.max(lbUtility, currentBRVal);
			}
			
//			long startCPLEX = System.currentTimeMillis();
//			sequenceFormLP.calculateBothPlStrategy(rootState, algConfig);
//			long thisCPLEX = System.currentTimeMillis() - startCPLEX;
//
//			System.out.println("Iteration " + iterations + " : CPLEX time : " + thisCPLEX);
//			overallCPLEX += thisCPLEX;
//			System.out.println(sequenceFormLP.resultValues);
//
//			for (Player player : rootState.getAllPlayers()) {
//				realizationPlans.put(player, sequenceFormLP.resultStrategies.get(player));
//			}

			break;
			
			
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
	}
}
