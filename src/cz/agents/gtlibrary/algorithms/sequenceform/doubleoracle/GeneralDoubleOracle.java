package cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle;

import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import cz.agents.gtlibrary.algorithms.sequenceform.refinements.librarycom.DODataBuilder;
import cz.agents.gtlibrary.domain.aceofspades.AoSExpander;
import cz.agents.gtlibrary.domain.aceofspades.AoSGameInfo;
import cz.agents.gtlibrary.domain.aceofspades.AoSGameState;
import cz.agents.gtlibrary.domain.artificialchance.ACExpander;
import cz.agents.gtlibrary.domain.artificialchance.ACGameInfo;
import cz.agents.gtlibrary.domain.artificialchance.ACGameState;
import cz.agents.gtlibrary.domain.bpg.BPGExpander;
import cz.agents.gtlibrary.domain.bpg.BPGGameInfo;
import cz.agents.gtlibrary.domain.bpg.BPGGameState;
import cz.agents.gtlibrary.domain.goofspiel.GSGameInfo;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielExpander;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielGameState;
import cz.agents.gtlibrary.domain.phantomTTT.TTTExpander;
import cz.agents.gtlibrary.domain.phantomTTT.TTTInfo;
import cz.agents.gtlibrary.domain.phantomTTT.TTTState;
import cz.agents.gtlibrary.domain.poker.generic.GPGameInfo;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerExpander;
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
import cz.agents.gtlibrary.domain.randomgame.SimRandomGameState;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.FixedSizeMap;
import cz.agents.gtlibrary.utils.io.GambitEFG;

public class GeneralDoubleOracle {
	private GameState rootState;
	private Expander<DoubleOracleInformationSet> expander;
	private GameInfo gameInfo;
	private DoubleOracleConfig<DoubleOracleInformationSet> algConfig;

	private PrintStream debugOutput = System.out;
	
	final private double EPS = 0.00000001;
    final private static boolean DEBUG = false;
    final private static boolean MY_RP_BR_ORDERING = false;
    private ThreadMXBean threadBean ;

    public enum PlayerSelection {
        BOTH,SINGLE_ALTERNATING,SINGLE_IMPROVED
    }

    public static PlayerSelection playerSelection = PlayerSelection.SINGLE_IMPROVED;

	public static void main(String[] args) {
//		runAC();
        runBP();
//        runGenericPoker();
//        runKuhnPoker();
//        runGoofSpiel();
//        runRandomGame();
//		runSimRandomGame();
//		runPursuit();
//        runPhantomTTT();
//		runAoS();
	}
	
	 public static void runAC() {
	        GameState rootState = new ACGameState();
	        GameInfo gameInfo = new ACGameInfo();
			DoubleOracleConfig<DoubleOracleInformationSet> algConfig = new DoubleOracleConfig<DoubleOracleInformationSet>(rootState, gameInfo);
	        Expander<DoubleOracleInformationSet> expander = new ACExpander<DoubleOracleInformationSet>(algConfig);
			GeneralDoubleOracle doefg = new GeneralDoubleOracle(rootState,  expander, gameInfo, algConfig);
	        doefg.generate(null);
	    }

    public static void runPhantomTTT() {
        GameState rootState = new TTTState();
        GameInfo gameInfo = new TTTInfo();
        DoubleOracleConfig<DoubleOracleInformationSet> algConfig = new DoubleOracleConfig<DoubleOracleInformationSet>(rootState, gameInfo);
        Expander<DoubleOracleInformationSet> expander = new TTTExpander<DoubleOracleInformationSet>(algConfig);
        GeneralDoubleOracle doefg = new GeneralDoubleOracle(rootState, expander, gameInfo, algConfig);
        doefg.generate(null);
//        GeneralDoubleOracle.traverseCompleteGameTree(rootState, expander);
    }
    
    public static void runAoS() {
        GameState rootState = new AoSGameState();
        GameInfo gameInfo = new AoSGameInfo();
		DoubleOracleConfig<DoubleOracleInformationSet> algConfig = new DoubleOracleConfig<DoubleOracleInformationSet>(rootState, gameInfo);
        Expander<DoubleOracleInformationSet> expander = new AoSExpander<DoubleOracleInformationSet>(algConfig);
		GeneralDoubleOracle doefg = new GeneralDoubleOracle(rootState,  expander, gameInfo, algConfig);
        doefg.generate(null);
    }


    public static void runPursuit() {
        GameState rootState = new PursuitGameState();
        GameInfo gameInfo = new PursuitGameInfo();
		DoubleOracleConfig<DoubleOracleInformationSet> algConfig = new DoubleOracleConfig<DoubleOracleInformationSet>(rootState, gameInfo);
        Expander<DoubleOracleInformationSet> expander = new PursuitExpander<DoubleOracleInformationSet>(algConfig);
		GeneralDoubleOracle doefg = new GeneralDoubleOracle(rootState,  expander, gameInfo, algConfig);
        doefg.generate(null);
    }
	
    public static void runKuhnPoker() {
        GameState rootState = new KuhnPokerGameState();
        GameInfo gameInfo = new KPGameInfo();
		DoubleOracleConfig<DoubleOracleInformationSet> algConfig = new DoubleOracleConfig<DoubleOracleInformationSet>(rootState, gameInfo);
        Expander<DoubleOracleInformationSet> expander = new KuhnPokerExpander<DoubleOracleInformationSet>(algConfig);
		GeneralDoubleOracle doefg = new GeneralDoubleOracle(rootState,  expander, gameInfo, algConfig);
		doefg.generate(null);
    }

    public static void runRandomGame() {
        GameState rootState = new RandomGameState();
        GameInfo gameInfo = new RandomGameInfo();
        DoubleOracleConfig<DoubleOracleInformationSet> algConfig = new DoubleOracleConfig<DoubleOracleInformationSet>(rootState, gameInfo);
        Expander<DoubleOracleInformationSet> expander = new RandomGameExpander<DoubleOracleInformationSet>(algConfig);
//        Expander<DoubleOracleInformationSet> expander = new RandomGameExpanderWithMoveOrdering<DoubleOracleInformationSet>(algConfig, new int[] {1, 2, 0});
        GeneralDoubleOracle doefg = new GeneralDoubleOracle(rootState,  expander, gameInfo, algConfig);
        doefg.generate(null);
//        GambitEFG.write("randomgame.gbt", rootState, (Expander) expander);
    }
    
    public static void runSimRandomGame() {
        GameState rootState = new SimRandomGameState();
        GameInfo gameInfo = new RandomGameInfo();
        DoubleOracleConfig<DoubleOracleInformationSet> algConfig = new DoubleOracleConfig<DoubleOracleInformationSet>(rootState, gameInfo);
        Expander<DoubleOracleInformationSet> expander = new RandomGameExpander<DoubleOracleInformationSet>(algConfig);
        GeneralDoubleOracle doefg = new GeneralDoubleOracle(rootState,  expander, gameInfo, algConfig);
        doefg.generate(null);
//        GambitEFG.write("randomgame.gbt", rootState, (Expander) expander);
    }

    public static void runGenericPoker() {
        GameState rootState = new GenericPokerGameState();
        GameInfo gameInfo = new GPGameInfo();
        DoubleOracleConfig<DoubleOracleInformationSet> algConfig = new DoubleOracleConfig<DoubleOracleInformationSet>(rootState, gameInfo);
        Expander<DoubleOracleInformationSet> expander = new GenericPokerExpander<DoubleOracleInformationSet>(algConfig);
//        Expander<DoubleOracleInformationSet> expander = new GenericPokerExpanderDomain<DoubleOracleInformationSet>(algConfig);
        GeneralDoubleOracle doefg = new GeneralDoubleOracle(rootState, expander, gameInfo, algConfig);
        doefg.generate(null);
    }

    public static void runBP() {
		GameState rootState = new BPGGameState();
		GameInfo gameInfo = new BPGGameInfo();
		DoubleOracleConfig<DoubleOracleInformationSet> algConfig = new DoubleOracleConfig<DoubleOracleInformationSet>(rootState, gameInfo);
		Expander<DoubleOracleInformationSet> expander = new BPGExpander<DoubleOracleInformationSet>(algConfig);
		GeneralDoubleOracle doefg = new GeneralDoubleOracle(rootState, expander, gameInfo, algConfig);
        doefg.generate(null);
    }

    public static void runGoofSpiel() {
        GameState rootState = new GoofSpielGameState();
        GSGameInfo gameInfo = new GSGameInfo();
        DoubleOracleConfig<DoubleOracleInformationSet> algConfig = new DoubleOracleConfig<DoubleOracleInformationSet>(rootState, gameInfo);
        Expander<DoubleOracleInformationSet> expander = new GoofSpielExpander<DoubleOracleInformationSet>(algConfig);
        GeneralDoubleOracle doefg = new GeneralDoubleOracle(rootState, expander, gameInfo, algConfig);
        doefg.generate(null);
    }

    public GeneralDoubleOracle (GameState rootState, Expander<DoubleOracleInformationSet> expander, GameInfo config, DoubleOracleConfig<DoubleOracleInformationSet> algConfig) {
		this.rootState = rootState;
		this.expander = expander;
		this.gameInfo = config;
		this.algConfig = algConfig;
	}

	public Map<Player, Map<Sequence, Double>> generate(Map<Player, Map<Sequence, Double>> initializationRG) {
		debugOutput.println("Double Oracle");
		debugOutput.println(gameInfo.getInfo());
        threadBean = ManagementFactory.getThreadMXBean();
		
		long start = threadBean.getCurrentThreadCpuTime();
		long overallSequenceGeneration = 0;
		long overallBRCalculation = 0;
		long overallCPLEX = 0;
        long overallRGBuilding = 0;
		int iterations = 0;

        Player[] actingPlayers = new Player[] { rootState.getAllPlayers()[0], rootState.getAllPlayers()[1] };
        DoubleOracleBestResponse[] brAlgorithms = new DoubleOracleBestResponse[] {
                new DoubleOracleBestResponse(expander, 0, actingPlayers, algConfig, gameInfo),
                new DoubleOracleBestResponse(expander, 1, actingPlayers, algConfig, gameInfo)};
        Map<Player, Map<Sequence, Double>> realizationPlans = new FixedSizeMap<Player, Map<Sequence, Double>>(2);

//        calculateSequences();

        if (initializationRG == null || initializationRG.isEmpty()) {
            GameState firstState = findFirstNonNatureState(rootState, expander);

            algConfig.addStateToSequenceForm(firstState);

            // init realization plans -> for each player, an empty sequence has probability equal to 1
            realizationPlans.put(actingPlayers[0], new HashMap<Sequence, Double>());
            realizationPlans.put(actingPlayers[1], new HashMap<Sequence, Double>());
            realizationPlans.get(actingPlayers[0]).put(firstState.getSequenceFor(actingPlayers[0]), 1d);
            realizationPlans.get(actingPlayers[1]).put(firstState.getSequenceFor(actingPlayers[1]), 1d);

            algConfig.addFullBRSequences(actingPlayers[0], realizationPlans.get(actingPlayers[0]).keySet());
            algConfig.addFullBRSequences(actingPlayers[1], realizationPlans.get(actingPlayers[1]).keySet());
        } else {
            realizationPlans = initializationRG;
            Map<Player, Set<Sequence>> tmpMap = new HashMap<Player, Set<Sequence>>();
            tmpMap.put(actingPlayers[0], initializationRG.get(actingPlayers[0]).keySet());
            tmpMap.put(actingPlayers[1], initializationRG.get(actingPlayers[1]).keySet());
            algConfig.initializeRG(tmpMap, brAlgorithms, expander);
            for (Player p : actingPlayers) {
                Set<Sequence> shorter = new HashSet<Sequence>();
                for (Sequence s : initializationRG.get(p).keySet()) {
                    if (s.size() == 0) continue;
                    Sequence ss = s.getSubSequence(s.size()-1);
                    shorter.add(ss);
                }
                for (Sequence s : initializationRG.get(p).keySet()) {
                    if (!shorter.contains(s))
                        algConfig.addFullBRSequence(p, s);
                }
            }
        }
		int currentPlayerIndex = 0;
		DoubleOracleSequenceFormLP doRestrictedGameSolver = new DoubleOracleSequenceFormLP(actingPlayers);
//		DOLPBuilder doRestrictedGameSolver = new DOLPBuilder(actingPlayers);
//		DOLPBuilder doRestrictedGameSolver = new RecyclingDOLPBuilder(actingPlayers);
//		ReducedDOLPBuilder doRestrictedGameSolver = new ReducedDOLPBuilder(actingPlayers, gameInfo, rootState, expander);
//		DODataBuilder doRestrictedGameSolver = new DODataBuilder(actingPlayers, rootState, expander);
        doRestrictedGameSolver.setDebugOutput(debugOutput);
		
		double p1BoundUtility = gameInfo.getMaxUtility();
		double p2BoundUtility = gameInfo.getMaxUtility();
		
		int[] oldSize = new int[] {-1,-1};
        int[] diffSize = new int[] {-1, -1};
        double[] lastBRValue = new double[] {-1.0, -1.0};

        boolean[] newSeqs = new boolean[] {true, true};
		
		while ((Math.abs(p1BoundUtility + p2BoundUtility) > EPS) ||
                Math.abs(doRestrictedGameSolver.getResultForPlayer(actingPlayers[0]) + doRestrictedGameSolver.getResultForPlayer(actingPlayers[1])) > EPS){

			iterations++;
            debugOutput.println("Iteration " + iterations + ": Cumulative Time from Beginning:" + ((threadBean.getCurrentThreadCpuTime() - start)/1000000l));

//            diffSize[currentPlayerIndex] = algConfig.getSizeForPlayer(actingPlayers[currentPlayerIndex]) - oldSize[currentPlayerIndex];

			debugOutput.println("Last difference: " + (algConfig.getSizeForPlayer(actingPlayers[currentPlayerIndex]) - oldSize[currentPlayerIndex]));
            debugOutput.println("Current Size: " + algConfig.getSizeForPlayer(actingPlayers[currentPlayerIndex]));
			oldSize[currentPlayerIndex] = algConfig.getSizeForPlayer(actingPlayers[currentPlayerIndex]);

//            if (diffSize[0] == 0 && diffSize[1] == 0) {
//                System.out.println("ERROR : NOT CONVERGED");
//                break;
//            }


			int opponentPlayerIndex = ( currentPlayerIndex + 1 ) % 2;
			
			long startFullBR = threadBean.getCurrentThreadCpuTime();
            double currentBRVal;
            if (MY_RP_BR_ORDERING)
			    currentBRVal = brAlgorithms[currentPlayerIndex].calculateBR(rootState, realizationPlans.get(actingPlayers[opponentPlayerIndex]), realizationPlans.get(actingPlayers[currentPlayerIndex]));
            else
                currentBRVal = brAlgorithms[currentPlayerIndex].calculateBR(rootState, realizationPlans.get(actingPlayers[opponentPlayerIndex]));
            long thisBR = (threadBean.getCurrentThreadCpuTime() - startFullBR)/1000000l;

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
            long startRGB = threadBean.getCurrentThreadCpuTime();
            if (newFullBRSequences.size() > 0) {
                if (DEBUG) debugOutput.println("New Full BR Sequences: " + newFullBRSequences);
                algConfig.createValidRestrictedGame(actingPlayers[currentPlayerIndex], newFullBRSequences, brAlgorithms, expander);
                algConfig.addFullBRSequences(actingPlayers[currentPlayerIndex], newFullBRSequences);
                newSeqs[0] = true;
                newSeqs[1] = true;
            } else {
                newSeqs[currentPlayerIndex] = false;
            }
            long thisRGB = (threadBean.getCurrentThreadCpuTime() - startRGB)/1000000l;
            overallRGBuilding += thisRGB;
			
			if (currentPlayerIndex == 0) {
				p1BoundUtility = Math.min(p1BoundUtility, currentBRVal);
			} else {
				p2BoundUtility = Math.min(p2BoundUtility, currentBRVal);
			}

            debugOutput.println("Iteration " + iterations + ": Bounds Interval Size :" + (p1BoundUtility + p2BoundUtility));

            if (DEBUG) debugOutput.println(algConfig.getNewSequences());

            switch (playerSelection) {
                case BOTH:
                    if (currentPlayerIndex != 0) {

                        long startCPLEX = threadBean.getCurrentThreadCpuTime();
                        doRestrictedGameSolver.calculateStrategyForPlayer(currentPlayerIndex, rootState, algConfig, (p1BoundUtility + p2BoundUtility));
                        doRestrictedGameSolver.calculateStrategyForPlayer(opponentPlayerIndex, rootState, algConfig, (p1BoundUtility + p2BoundUtility));
                        long thisCPLEX = (threadBean.getCurrentThreadCpuTime() - startCPLEX)/1000000l;

                        debugOutput.println("Iteration " + iterations + " : CPLEX time : " + thisCPLEX);
                        overallCPLEX += thisCPLEX;
                        debugOutput.println("LP Value " + actingPlayers[currentPlayerIndex] + " : " + doRestrictedGameSolver.getResultForPlayer(actingPlayers[currentPlayerIndex]));
                        debugOutput.println("LP Value " + actingPlayers[opponentPlayerIndex] + " : " + doRestrictedGameSolver.getResultForPlayer(actingPlayers[opponentPlayerIndex]));
                        algConfig.clearNewSequences();
                    }
                    currentPlayerIndex = opponentPlayerIndex;
                    break;
                case SINGLE_ALTERNATING:
                    long startCPLEX = threadBean.getCurrentThreadCpuTime();
                    doRestrictedGameSolver.calculateStrategyForPlayer(currentPlayerIndex, rootState, algConfig, (p1BoundUtility + p2BoundUtility));
                    long thisCPLEX = (threadBean.getCurrentThreadCpuTime() - startCPLEX)/1000000l;

                    debugOutput.println("Iteration " + iterations + " : CPLEX time : " + thisCPLEX);
                    overallCPLEX += thisCPLEX;
                    debugOutput.println("LP Value " + actingPlayers[currentPlayerIndex] + " : " + doRestrictedGameSolver.getResultForPlayer(actingPlayers[currentPlayerIndex]));
                    debugOutput.println("LP Value " + actingPlayers[opponentPlayerIndex] + " : " + doRestrictedGameSolver.getResultForPlayer(actingPlayers[opponentPlayerIndex]));

                    currentPlayerIndex = opponentPlayerIndex;
                    algConfig.clearNewSequences();
                    break;

                case SINGLE_IMPROVED:
                    if (doRestrictedGameSolver.getResultForPlayer(actingPlayers[currentPlayerIndex]) == null ||
                            doRestrictedGameSolver.getResultForPlayer(actingPlayers[opponentPlayerIndex]) == null) { // we have not calculated the value for the current player in RG yet
                            currentPlayerIndex = opponentPlayerIndex;
                    } else {
                        double oldLPResult = doRestrictedGameSolver.getResultForPlayer(actingPlayers[currentPlayerIndex]);
//                    double oldLPResult1 = doRestrictedGameSolver.getResultForPlayer(actingPlayers[opponentPlayerIndex]);
//                    double oldLPResult2 = doRestrictedGameSolver.getResultForPlayer(actingPlayers[currentPlayerIndex]);
                        if (currentPlayerIndex == 0) {
                            if (newFullBRSequences.size() > 0 && Math.abs(p1BoundUtility - (oldLPResult)) > Math.abs(p2BoundUtility - (-oldLPResult))) {
//                        if (Math.abs(currentBRVal - (oldLPResult)) > EPS) {
//                        if (Math.abs(lastBRValue[0] - (oldLPResult2)) - EPS > Math.abs(lastBRValue[1] - (oldLPResult1))) {
                                currentPlayerIndex = 0;
                            } else {
                                currentPlayerIndex = 1;
                            }
                        } else {
                            if (newFullBRSequences.size() == 0 || Math.abs(p1BoundUtility - (-oldLPResult)) >= Math.abs(p2BoundUtility - (oldLPResult))) {
//                        if (Math.abs(currentBRVal - (oldLPResult)) > EPS) {
//                        if (Math.abs(lastBRValue[0] - (-oldLPResult2)) >= Math.abs(lastBRValue[1] - (-oldLPResult1)) - EPS ) {
                                currentPlayerIndex = 0;
                            } else {
                                currentPlayerIndex = 1;
                            }
                        }

                    }

                    opponentPlayerIndex = (1+currentPlayerIndex)%2;

                    startCPLEX = threadBean.getCurrentThreadCpuTime();
                    doRestrictedGameSolver.calculateStrategyForPlayer(opponentPlayerIndex, rootState, algConfig, (p1BoundUtility + p2BoundUtility));
                    thisCPLEX = (threadBean.getCurrentThreadCpuTime() - startCPLEX)/1000000l;

                    debugOutput.println("Iteration " + iterations + " : CPLEX time : " + thisCPLEX);
                    overallCPLEX += thisCPLEX;
                    debugOutput.println("LP Value " + actingPlayers[currentPlayerIndex] + " : " + doRestrictedGameSolver.getResultForPlayer(actingPlayers[currentPlayerIndex]));
                    debugOutput.println("LP Value " + actingPlayers[opponentPlayerIndex] + " : " + doRestrictedGameSolver.getResultForPlayer(actingPlayers[opponentPlayerIndex]));

                    algConfig.clearNewSequences();

                    break;
                default:
                    assert false;
                    break;
            }

            realizationPlans.put(actingPlayers[currentPlayerIndex], doRestrictedGameSolver.getResultStrategiesForPlayer(actingPlayers[currentPlayerIndex]));
            realizationPlans.put(actingPlayers[opponentPlayerIndex], doRestrictedGameSolver.getResultStrategiesForPlayer(actingPlayers[opponentPlayerIndex]));

            if (DEBUG)
                for (Player player : actingPlayers) {
                    for (Sequence sequence : realizationPlans.get(player).keySet()) {
                        if (realizationPlans.get(player).get(sequence) > 0) {
                            if (DEBUG) debugOutput.println(sequence + "\t:\t" + realizationPlans.get(player).get(sequence));
                        }
                    }
                }

            if (DEBUG)
                algConfig.validateRestrictedGameStructure(expander, brAlgorithms);

            if (!playerSelection.equals(PlayerSelection.BOTH) && !newSeqs[0] && !newSeqs[1]) {
                System.out.println("ERROR : NOT CONVERGED");
                break;
            }
		}



        debugOutput.println("done.");
        long finishTime = (threadBean.getCurrentThreadCpuTime() - start)/1000000l;

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

        debugOutput.println("final size: FirstPlayer Sequences: " + algConfig.getSequencesFor(actingPlayers[0]).size() + " \t SecondPlayer Sequences : " + algConfig.getSequencesFor(actingPlayers[1]).size());
        debugOutput.println("final support_size: FirstPlayer: " + support_size[0] + " \t SecondPlayer: " + support_size[1]);
        debugOutput.println("final result:" + doRestrictedGameSolver.getResultForPlayer(actingPlayers[0]));
        debugOutput.println("final memory:" + ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024));
        debugOutput.println("final time: " + finishTime);
        debugOutput.println("final CPLEX time: " + overallCPLEX);
        debugOutput.println("final BR time: " + overallBRCalculation);
        debugOutput.println("final RGB time: " + overallRGBuilding);
        debugOutput.println("final StrategyGenerating time: " + overallSequenceGeneration);
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

    public static void traverseCompleteGameTree(GameState rootState, Expander<DoubleOracleInformationSet> expander) {
        System.out.println("Claculating the size of the game.");
        LinkedList<GameState> queue = new LinkedList<GameState>();
        long nodes = 0;
        queue.add(rootState);

        while (queue.size() > 0) {
            nodes++;
            GameState currentState = queue.removeFirst();

            if (currentState.isGameEnd()) {
                continue;
            }
            for (Action action : expander.getActions(currentState)) {
                GameState newState = currentState.performAction(action);

                queue.addFirst(newState);
                currentState.performAction(action);
            }
        }

        System.out.println("Nodes: " + nodes);
    }

    public void calculateSequences() {
        LinkedList<GameState> queue = new LinkedList<GameState>();
        HashMap<Player, HashSet<Sequence>> sequences = new HashMap<Player, HashSet<Sequence>>();

        for (Player p : rootState.getAllPlayers())
            sequences.put(p, new HashSet<Sequence>());

        queue.add(rootState);

        while (queue.size() > 0) {
            GameState currentState = queue.removeLast();

//            for (Player p : rootState.getAllPlayers())
            Player p = rootState.getAllPlayers()[1];
            if (currentState.isGameEnd()) // || !currentState.getPlayerToMove().equals(p))
                sequences.get(p).add(currentState.getSequenceFor(p));

            for (Action action : expander.getActions(currentState)) {
                queue.add(currentState.performAction(action));
            }
        }
        System.out.println("final size: FirstPlayer Sequences: " + sequences.get(rootState.getAllPlayers()[0]).size() + " \t SecondPlayer Sequences : " + sequences.get(rootState.getAllPlayers()[1]).size());
        System.exit(0);
    }

    public void setDebugOutput(PrintStream debugOutput) {
        this.debugOutput = debugOutput;
    }
}
