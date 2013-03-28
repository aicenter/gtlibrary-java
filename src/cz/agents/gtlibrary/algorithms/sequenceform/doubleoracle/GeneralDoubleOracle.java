package cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.domain.bpg.BPGExpander;
import cz.agents.gtlibrary.domain.bpg.BPGGameInfo;
import cz.agents.gtlibrary.domain.bpg.BPGGameState;
import cz.agents.gtlibrary.domain.goofspiel.GSGameInfo;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielExpander;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielGameState;
import cz.agents.gtlibrary.domain.poker.generic.GPGameInfo;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerExpander;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerExpanderDomain;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerGameState;
import cz.agents.gtlibrary.domain.poker.kuhn.KPGameInfo;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerExpander;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerGameState;
import cz.agents.gtlibrary.domain.pursuit.PursuitExpander;
import cz.agents.gtlibrary.domain.pursuit.PursuitGameInfo;
import cz.agents.gtlibrary.domain.pursuit.PursuitGameState;
import cz.agents.gtlibrary.domain.randomgame.RandomGameExpander;
import cz.agents.gtlibrary.domain.randomgame.RandomGameInfo;
import cz.agents.gtlibrary.domain.randomgame.RandomGameState;
import cz.agents.gtlibrary.domain.simrandomgame.SimRandomExpander;
import cz.agents.gtlibrary.domain.simrandomgame.SimRandomGameInfo;
import cz.agents.gtlibrary.domain.simrandomgame.SimRandomGameState;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.FixedSizeMap;

public class GeneralDoubleOracle {
	private GameState rootState;
	private Expander<DoubleOracleInformationSet> expander;
	private GameInfo gameConfig;
	private DoubleOracleConfig<DoubleOracleInformationSet> algConfig;

	private PrintStream debugOutput = System.out;
	
	final private double EPS = 0.000001;
    final private static boolean DEBUG = false;
    final private static boolean MY_RP_BR_ORDERING = false;
    public static boolean IMPROVED_PLAYER_SELECTION = true;

	public static void main(String[] args) {
//        runBP();
//        runGenericPoker();
//        runKuhnPoker();
//        runGoofSpiel();
//        runRandomGame();
		runSimRandomGame();
//		runPursuit();
	}

	public static void runPursuit() {
        GameState rootState = new PursuitGameState();
        GameInfo gameInfo = new PursuitGameInfo();
		DoubleOracleConfig<DoubleOracleInformationSet> algConfig = new DoubleOracleConfig<DoubleOracleInformationSet>(rootState, gameInfo);
        Expander<DoubleOracleInformationSet> expander = new PursuitExpander<DoubleOracleInformationSet>(algConfig);
		GeneralDoubleOracle doefg = new GeneralDoubleOracle(rootState,  expander, gameInfo, algConfig);
        doefg.generate();
    }
	
    public static void runKuhnPoker() {
        GameState rootState = new KuhnPokerGameState();
        GameInfo gameInfo = new KPGameInfo();
		DoubleOracleConfig<DoubleOracleInformationSet> algConfig = new DoubleOracleConfig<DoubleOracleInformationSet>(rootState, gameInfo);
        Expander<DoubleOracleInformationSet> expander = new KuhnPokerExpander<DoubleOracleInformationSet>(algConfig);
		GeneralDoubleOracle doefg = new GeneralDoubleOracle(rootState,  expander, gameInfo, algConfig);
        doefg.generate();
    }

    public static void runRandomGame() {
        GameState rootState = new RandomGameState();
        GameInfo gameInfo = new RandomGameInfo();
        DoubleOracleConfig<DoubleOracleInformationSet> algConfig = new DoubleOracleConfig<DoubleOracleInformationSet>(rootState, gameInfo);
        Expander<DoubleOracleInformationSet> expander = new RandomGameExpander<DoubleOracleInformationSet>(algConfig);
        GeneralDoubleOracle doefg = new GeneralDoubleOracle(rootState,  expander, gameInfo, algConfig);
        doefg.generate();
    }
    
    public static void runSimRandomGame() {
        GameState rootState = new SimRandomGameState();
        GameInfo gameInfo = new SimRandomGameInfo();
        DoubleOracleConfig<DoubleOracleInformationSet> algConfig = new DoubleOracleConfig<DoubleOracleInformationSet>(rootState, gameInfo);
        Expander<DoubleOracleInformationSet> expander = new SimRandomExpander<DoubleOracleInformationSet>(algConfig);
        GeneralDoubleOracle doefg = new GeneralDoubleOracle(rootState,  expander, gameInfo, algConfig);
        doefg.generate();
    }

    public static void runGenericPoker() {
        GameState rootState = new GenericPokerGameState();
        GameInfo gameInfo = new GPGameInfo();
        DoubleOracleConfig<DoubleOracleInformationSet> algConfig = new DoubleOracleConfig<DoubleOracleInformationSet>(rootState, gameInfo);
        Expander<DoubleOracleInformationSet> expander = new GenericPokerExpander<DoubleOracleInformationSet>(algConfig);
//        Expander<DoubleOracleInformationSet> expander = new GenericPokerExpanderDomain<DoubleOracleInformationSet>(algConfig);
        GeneralDoubleOracle doefg = new GeneralDoubleOracle(rootState, expander, gameInfo, algConfig);
        doefg.generate();
    }

    public static void runBP() {
		GameState rootState = new BPGGameState();
		GameInfo gameInfo = new BPGGameInfo();
		DoubleOracleConfig<DoubleOracleInformationSet> algConfig = new DoubleOracleConfig<DoubleOracleInformationSet>(rootState, gameInfo);
		Expander<DoubleOracleInformationSet> expander = new BPGExpander<DoubleOracleInformationSet>(algConfig);
		GeneralDoubleOracle doefg = new GeneralDoubleOracle(rootState, expander, gameInfo, algConfig);
        doefg.generate();
    }

    public static void runGoofSpiel() {
        GameState rootState = new GoofSpielGameState();
        GSGameInfo gameInfo = new GSGameInfo();
        DoubleOracleConfig<DoubleOracleInformationSet> algConfig = new DoubleOracleConfig<DoubleOracleInformationSet>(rootState, gameInfo);
        Expander<DoubleOracleInformationSet> expander = new GoofSpielExpander<DoubleOracleInformationSet>(algConfig);
        GeneralDoubleOracle doefg = new GeneralDoubleOracle(rootState, expander, gameInfo, algConfig);
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
		long overallCPLEX = 0;
        long overallRGBuilding = 0;
		int iterations = 0;

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
        int[] diffSize = new int[] {-1, -1};
        double[] lastBRValue = new double[] {-1.0, -1.0};
		
		while ((Math.abs(p1BoundUtility + p2BoundUtility) > EPS) ||
                Math.abs(doRestrictedGameSolver.getResultForPlayer(actingPlayers[0]) + doRestrictedGameSolver.getResultForPlayer(actingPlayers[1])) > EPS){
//		while (true) {

			iterations++;
//            algConfig.setCurrentIteration(iterations);

            diffSize[currentPlayerIndex] = algConfig.getSizeForPlayer(actingPlayers[currentPlayerIndex]) - oldSize[currentPlayerIndex];
            algConfig.clearNewSequences();
			debugOutput.println("Last difference: " + (algConfig.getSizeForPlayer(actingPlayers[currentPlayerIndex]) - oldSize[currentPlayerIndex]));
            debugOutput.println("Current Size: " + algConfig.getSizeForPlayer(actingPlayers[currentPlayerIndex]));
			oldSize[currentPlayerIndex] = algConfig.getSizeForPlayer(actingPlayers[currentPlayerIndex]);

            if (diffSize[0] == 0 && diffSize[1] == 0) {
                System.out.println("ERROR : NOT CONVERGED");
                break;
            }

			int opponentPlayerIndex = ( currentPlayerIndex + 1 ) % 2;
			
			long startFullBR = System.currentTimeMillis();
            double currentBRVal;
            if (MY_RP_BR_ORDERING)
			    currentBRVal = brAlgorithms[currentPlayerIndex].calculateBR(rootState, realizationPlans.get(actingPlayers[opponentPlayerIndex]), realizationPlans.get(actingPlayers[currentPlayerIndex]));
            else
                currentBRVal = brAlgorithms[currentPlayerIndex].calculateBR(rootState, realizationPlans.get(actingPlayers[opponentPlayerIndex]));
            long thisBR = System.currentTimeMillis() - startFullBR;

            debugOutput.println("BR Value " + actingPlayers[currentPlayerIndex] + " : " + currentBRVal);
            debugOutput.println("Iteration " + iterations + " : full BR time : " + thisBR);
			overallBRCalculation += thisBR;

            lastBRValue[currentPlayerIndex] = currentBRVal;
			
			HashSet<Sequence> currentFullBRSequences = brAlgorithms[currentPlayerIndex].getFullBRSequences();
			HashSet<Sequence> newFullBRSequences = new HashSet<Sequence>();
			for (Sequence s : currentFullBRSequences) {
				if (!algConfig.getSequencesFor(actingPlayers[currentPlayerIndex]).contains(s)) {
					newFullBRSequences.add(s);
				}
			}
            if (DEBUG) debugOutput.println("All BR Sequences: " + currentFullBRSequences);
            long startRGB = System.currentTimeMillis();
            if (newFullBRSequences.size() > 0) {
                if (DEBUG) debugOutput.println("New Full BR Sequences: " + newFullBRSequences);
                algConfig.createValidRestrictedGame(actingPlayers[currentPlayerIndex], newFullBRSequences, brAlgorithms, expander);
                algConfig.addFullBRSequences(actingPlayers[currentPlayerIndex], newFullBRSequences);
            }
            long thisRGB = System.currentTimeMillis() - startRGB;
            overallRGBuilding += thisRGB;
			
			if (currentPlayerIndex == 0) {
				p1BoundUtility = Math.min(p1BoundUtility, currentBRVal);
			} else {
				p2BoundUtility = Math.min(p2BoundUtility, currentBRVal);
			}

            debugOutput.println("Iteration " + iterations + ": Bounds Interval Size :" + (p1BoundUtility + p2BoundUtility));

            if (DEBUG) debugOutput.println(algConfig.getNewSequences());



            if (!IMPROVED_PLAYER_SELECTION) {
                long startCPLEX = System.currentTimeMillis();
                doRestrictedGameSolver.calculateStrategyForPlayer(currentPlayerIndex, rootState, algConfig, (p1BoundUtility + p2BoundUtility));
                long thisCPLEX = System.currentTimeMillis() - startCPLEX;

                debugOutput.println("Iteration " + iterations + " : CPLEX time : " + thisCPLEX);
                overallCPLEX += thisCPLEX;
                debugOutput.println("LP Value " + actingPlayers[opponentPlayerIndex] + " : " + doRestrictedGameSolver.getResultForPlayer(actingPlayers[opponentPlayerIndex]));

                currentPlayerIndex = opponentPlayerIndex;
            } else {
                if (doRestrictedGameSolver.getResultForPlayer(actingPlayers[currentPlayerIndex]) == null ||
                    doRestrictedGameSolver.getResultForPlayer(actingPlayers[opponentPlayerIndex]) == null) { // we have not calculated the value for the current player in RG yet
                    long startCPLEX = System.currentTimeMillis();
                    doRestrictedGameSolver.calculateStrategyForPlayer(currentPlayerIndex, rootState, algConfig, (p1BoundUtility + p2BoundUtility));
                    long thisCPLEX = System.currentTimeMillis() - startCPLEX;

                    debugOutput.println("Iteration " + iterations + " : CPLEX time : " + thisCPLEX);
                    overallCPLEX += thisCPLEX;
                    debugOutput.println("LP Value " + actingPlayers[opponentPlayerIndex] + " : " + doRestrictedGameSolver.getResultForPlayer(actingPlayers[opponentPlayerIndex]));

                    currentPlayerIndex = opponentPlayerIndex;
                } else {
                    double oldLPResult = doRestrictedGameSolver.getResultForPlayer(actingPlayers[currentPlayerIndex]);
//                    double oldLPResult1 = doRestrictedGameSolver.getResultForPlayer(actingPlayers[opponentPlayerIndex]);
//                    double oldLPResult2 = doRestrictedGameSolver.getResultForPlayer(actingPlayers[currentPlayerIndex]);
                    if (currentPlayerIndex == 0) {
                        if (Math.abs(p1BoundUtility - (oldLPResult)) > Math.abs(p2BoundUtility - (-oldLPResult))) {
//                        if (Math.abs(currentBRVal - (oldLPResult)) > EPS) {
//                        if (Math.abs(lastBRValue[0] - (oldLPResult2)) - EPS > Math.abs(lastBRValue[1] - (oldLPResult1))) {
                            currentPlayerIndex = 0;
                        } else {
                            currentPlayerIndex = 1;
                        }
                    } else {
                        if (Math.abs(p1BoundUtility - (-oldLPResult)) >= Math.abs(p2BoundUtility - (oldLPResult))) {
//                        if (Math.abs(currentBRVal - (oldLPResult)) > EPS) {
//                        if (Math.abs(lastBRValue[0] - (-oldLPResult2)) >= Math.abs(lastBRValue[1] - (-oldLPResult1)) - EPS ) {
                            currentPlayerIndex = 0;
                        } else {
                            currentPlayerIndex = 1;
                        }
                    }

                    opponentPlayerIndex = (1+currentPlayerIndex)%2;

                    long startCPLEX = System.currentTimeMillis();
                    doRestrictedGameSolver.calculateStrategyForPlayer(opponentPlayerIndex, rootState, algConfig, (p1BoundUtility + p2BoundUtility));
                    long thisCPLEX = System.currentTimeMillis() - startCPLEX;

                    debugOutput.println("Iteration " + iterations + " : CPLEX time : " + thisCPLEX);
                    overallCPLEX += thisCPLEX;
                    debugOutput.println("LP Value " + actingPlayers[currentPlayerIndex] + " : " + doRestrictedGameSolver.getResultForPlayer(actingPlayers[currentPlayerIndex]));
                    debugOutput.println("LP Value " + actingPlayers[opponentPlayerIndex] + " : " + doRestrictedGameSolver.getResultForPlayer(actingPlayers[opponentPlayerIndex]));
                }
            }

            realizationPlans.put(actingPlayers[currentPlayerIndex], doRestrictedGameSolver.getResultStrategiesForPlayer(actingPlayers[currentPlayerIndex]));
            if (IMPROVED_PLAYER_SELECTION) realizationPlans.put(actingPlayers[opponentPlayerIndex], doRestrictedGameSolver.getResultStrategiesForPlayer(actingPlayers[opponentPlayerIndex]));

            for (Player player : actingPlayers) {
				for (Sequence sequence : realizationPlans.get(player).keySet()) {
					if (realizationPlans.get(player).get(sequence) > 0) {
                        if (DEBUG) debugOutput.println(sequence + "\t:\t" + realizationPlans.get(player).get(sequence));
					}
				}
			}

            if (DEBUG)
                algConfig.validateRestrictedGameStructure(expander, brAlgorithms);

		}

        debugOutput.println("done.");
        long finishTime = System.currentTimeMillis() - start;

        doRestrictedGameSolver.calculateStrategyForPlayer(1, rootState, algConfig, (p1BoundUtility + p2BoundUtility));

		int[] support_size = new int[] { 0, 0 };
//        int[] maxIt = new int[] { 0, 0 };
		for (Player player : actingPlayers) {
			for (Sequence sequence : realizationPlans.get(player).keySet()) {
				if (realizationPlans.get(player).get(sequence) > 0) {
					support_size[player.getId()]++;
//                    maxIt[player.getId()] = Math.max(maxIt[player.getId()], algConfig.getIterationForSequence(sequence));
                    if (DEBUG)
                        debugOutput.println(sequence + "\t:\t" + realizationPlans.get(player).get(sequence));
				}
			}
		}

        try {
            Runtime.getRuntime().gc();
            Thread.currentThread().sleep(500l);
        } catch (InterruptedException e) {
        }

        System.out.println("final size: FirstPlayer Sequences: " + algConfig.getSequencesFor(actingPlayers[0]).size() + " \t SecondPlayer Sequences : " + algConfig.getSequencesFor(actingPlayers[1]).size());
		System.out.println("final support_size: FirstPlayer: " + support_size[0] + " \t SecondPlayer: " + support_size[1]);
		System.out.println("final result:" + doRestrictedGameSolver.getResultForPlayer(actingPlayers[0]));
		System.out.println("final memory:" + ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024));
        System.out.println("final time: " + finishTime);
		System.out.println("final CPLEX time: " + overallCPLEX);
        System.out.println("final BR time: " + overallBRCalculation);
        System.out.println("final RGB time: " + overallRGBuilding);
        System.out.println("final StrategyGenerating time: " + overallSequenceGeneration);
//        debugOutput.println("last support sequence iteration: PL1: " + maxIt[0] + " \t PL2: " + maxIt[1]);
        debugOutput.println("LP GenerationTime:" + doRestrictedGameSolver.getOverallGenerationTime());
        debugOutput.println("LP Constraint GenerationTime:" + doRestrictedGameSolver.getOverallConstraintGenerationTime());
        debugOutput.println("LP ComputationTime:" + doRestrictedGameSolver.getOverallConstraintLPSolvingTime());
        
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
