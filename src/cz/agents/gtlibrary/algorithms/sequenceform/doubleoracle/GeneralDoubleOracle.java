package cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.domain.bpg.BPGExpander;
import cz.agents.gtlibrary.domain.bpg.BPGGameInfo;
import cz.agents.gtlibrary.domain.bpg.BPGGameState;
import cz.agents.gtlibrary.domain.poker.generic.GPGameInfo;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerExpander;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerGameState;
import cz.agents.gtlibrary.domain.poker.kuhn.KPGameInfo;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerExpander;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerGameState;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.FixedSizeMap;

public class GeneralDoubleOracle {
	private GameState rootState;
	private Expander<DoubleOracleInformationSet> expander;
	private GameInfo gameConfig;
	private DoubleOracleConfig<DoubleOracleInformationSet> algConfig;

	private PrintStream debugOutput = System.out;
	
	final private double EPS = 0.000001;

	public static void main(String[] args) {
//		GameState rootState = new KuhnPokerGameState();
//        GameInfo gameInfo = new KPGameInfo();
//		DoubleOracleConfig<DoubleOracleInformationSet> algConfig = new DoubleOracleConfig<DoubleOracleInformationSet>(rootState, gameInfo);
//        Expander<DoubleOracleInformationSet> expander = new KuhnPokerExpander<DoubleOracleInformationSet>(algConfig);
//		GeneralDoubleOracle doefg = new GeneralDoubleOracle(rootState,  expander, gameInfo, algConfig);

		GameState rootState = new GenericPokerGameState();
        GameInfo gameInfo = new GPGameInfo();
		DoubleOracleConfig<DoubleOracleInformationSet> algConfig = new DoubleOracleConfig<DoubleOracleInformationSet>(rootState, gameInfo);
        Expander<DoubleOracleInformationSet> expander = new GenericPokerExpander<DoubleOracleInformationSet>(algConfig);
		GeneralDoubleOracle doefg = new GeneralDoubleOracle(rootState, expander, gameInfo, algConfig);

//		GameState rootState = new BPGGameState();
//		GameInfo gameInfo = new BPGGameInfo();
//		DoubleOracleConfig<DoubleOracleInformationSet> algConfig = new DoubleOracleConfig<DoubleOracleInformationSet>(rootState, gameInfo);
//		Expander<DoubleOracleInformationSet> expander = new BPGExpander<DoubleOracleInformationSet>(algConfig);
//		GeneralDoubleOracle doefg = new GeneralDoubleOracle(rootState, expander, gameInfo, algConfig);
		
		doefg.generate();
	}

	public GeneralDoubleOracle (GameState rootState, Expander<DoubleOracleInformationSet> expander, GameInfo config, DoubleOracleConfig<DoubleOracleInformationSet> algConfig) {
		this.rootState = rootState;
		this.expander = expander;
		this.gameConfig = config;
		this.algConfig = algConfig;
	}

	public Map<Player, Map<Sequence, Double>> generate() {
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

        GameState firstState = findFirstNonNatureState(rootState, expander);

		Player[] actingPlayers = new Player[] { rootState.getAllPlayers()[0], rootState.getAllPlayers()[1] };
		
		algConfig.addStateToSequenceForm(firstState);
		
		DoubleOracleBestResponse[] brAlgorithms = new DoubleOracleBestResponse[] { 
					new DoubleOracleBestResponse(expander, 0, actingPlayers, algConfig, gameConfig),
					new DoubleOracleBestResponse(expander, 1, actingPlayers, algConfig, gameConfig)};

		// init realization plans -> for each player, an empty sequence has probability equal to 1
		Map<Player, Map<Sequence, Double>> realizationPlans = new FixedSizeMap<Player, Map<Sequence, Double>>(2);
		realizationPlans.put(actingPlayers[0], new HashMap<Sequence, Double>());
		realizationPlans.put(actingPlayers[1], new HashMap<Sequence, Double>());
		realizationPlans.get(actingPlayers[0]).put(firstState.getSequenceFor(actingPlayers[0]), 1d);
		realizationPlans.get(actingPlayers[1]).put(firstState.getSequenceFor(actingPlayers[1]), 1d);
		
		algConfig.addFullBRSequences(actingPlayers[0], realizationPlans.get(actingPlayers[0]).keySet());
		algConfig.addFullBRSequences(actingPlayers[1], realizationPlans.get(actingPlayers[1]).keySet());
		
		int currentPlayerIndex = 0;
		DoubleOracleSequenceFormLP doRestrictedGameSolver = new DoubleOracleSequenceFormLP(actingPlayers);
		
		double p1BoundUtility = gameConfig.getMaxUtility();
		double p2BoundUtility = gameConfig.getMaxUtility();
		
		int[] oldSize = new int[] {-1,-1};
		
		while ((p1BoundUtility + p2BoundUtility) > EPS) {
			
			iterations++;
//			if (algConfig.getNodesInRestrictedGame() - oldRG == 0) {
//				break;
//			} else {
				System.out.println("Last difference: " + (algConfig.getSizeForPlayer(actingPlayers[currentPlayerIndex]) - oldSize[currentPlayerIndex]));
				System.out.println("Current Size: " + algConfig.getSizeForPlayer(actingPlayers[currentPlayerIndex]));
//			}
				oldSize[currentPlayerIndex] = algConfig.getSizeForPlayer(actingPlayers[currentPlayerIndex]);
			
			int opponentPlayerIndex = ( currentPlayerIndex + 1 ) % 2;
			
			long startFullBR = System.currentTimeMillis();
			long thisBR = 0;
			double currentBRVal = brAlgorithms[currentPlayerIndex].calculateBR(rootState, realizationPlans.get(actingPlayers[opponentPlayerIndex]));
			thisBR = System.currentTimeMillis() - startFullBR;
			
			System.out.println("BR Value " + actingPlayers[currentPlayerIndex] + " : " + currentBRVal); 
			System.out.println("Iteration " + iterations + " : full BR time : " + thisBR);
			overallBRCalculation += thisBR;

			
			HashSet<Sequence> currentFullBRSequences = brAlgorithms[currentPlayerIndex].getFullBRSequences();
			HashSet<Sequence> newFullBRSequences = new HashSet<Sequence>();
			for (Sequence s : currentFullBRSequences) {
				if (!algConfig.getSequencesFor(actingPlayers[currentPlayerIndex]).contains(s)) {
					newFullBRSequences.add(s);
				}
			}
//            System.out.println("All BR Sequences: " + currentFullBRSequences);
            if (newFullBRSequences.size() > 0) {
                System.out.println("New Full BR Sequences: " + newFullBRSequences);
                algConfig.createValidRestrictedGame(actingPlayers[currentPlayerIndex], newFullBRSequences, brAlgorithms, expander);
                algConfig.addFullBRSequences(actingPlayers[currentPlayerIndex], newFullBRSequences);
            }
			
			if (currentPlayerIndex == 0) {
				p1BoundUtility = Math.min(p1BoundUtility, currentBRVal);
			} else {
				p2BoundUtility = Math.min(p2BoundUtility, currentBRVal);
			}
			
			long startCPLEX = System.currentTimeMillis();
			doRestrictedGameSolver.calculateStrategyForPlayer(currentPlayerIndex, rootState, algConfig);
			long thisCPLEX = System.currentTimeMillis() - startCPLEX;

			System.out.println("Iteration " + iterations + " : CPLEX time : " + thisCPLEX);
			overallCPLEX += thisCPLEX;
			
			System.out.println("LP Value " + actingPlayers[opponentPlayerIndex] + " : " + doRestrictedGameSolver.getResultForPlayer(actingPlayers[opponentPlayerIndex]));

			realizationPlans.put(actingPlayers[currentPlayerIndex], doRestrictedGameSolver.getResultStrategiesForPlayer(actingPlayers[currentPlayerIndex]));
			
			currentPlayerIndex = opponentPlayerIndex;
			
			for (Player player : actingPlayers) {
				for (Sequence sequence : realizationPlans.get(player).keySet()) {
					if (realizationPlans.get(player).get(sequence) > 0) {
//						System.out.println(sequence + "\t:\t" + realizationPlans.get(player).get(sequence));
					}
				}
			}

//			assert ((1 - 1 + currentPlayerIndex) == currentPlayerIndex);
//            algConfig.validateRestrictedGameStructure(expander, brAlgorithms);
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
		System.out.println("final result:" + doRestrictedGameSolver.getResultForPlayer(actingPlayers[0]));
		System.out.println("final memory:" + ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024));

		System.out.println("final CPLEX time: " + overallCPLEX);
		System.out.println("final StrategyGenerating time: " + overallSequenceGeneration);
                return realizationPlans;
	}

    public GameState findFirstNonNatureState(GameState rootState, Expander<DoubleOracleInformationSet> expander) {
        GameState tmpState = rootState.copy();

        while (tmpState.isPlayerToMoveNature()) {
            Action action = expander.getActions(tmpState).get(0);
            tmpState = tmpState.performAction(action);
        }

        return tmpState;
    }
}
