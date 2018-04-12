/*
Copyright 2014 Faculty of Electrical Engineering at CTU in Prague

This file is part of Game Theoretic Library.

Game Theoretic Library is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Game Theoretic Library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with Game Theoretic Library.  If not, see <http://www.gnu.org/licenses/>.*/


package cz.agents.gtlibrary.algorithms.reverse;

import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import cz.agents.gtlibrary.algorithms.reverse.DoubleOracleReverseConfig;
import cz.agents.gtlibrary.algorithms.reverse.DoubleOracleReverseInformationSet;
import cz.agents.gtlibrary.algorithms.reverse.SQFBestResponseReverseAlgorithm;
import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.DoubleOracleConfig;
//import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.DoubleOracleConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.DoubleOracleInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.DoubleOracleLPSolver;
import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.DoubleOracleSequenceFormLP;
import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.GeneralDoubleOracle;
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
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.FixedSizeMap;

public class ReverseDoubleOracle {
	private GameState rootState;
	private Expander<? extends DoubleOracleInformationSet> expander;
	private GameInfo gameInfo;
	private DoubleOracleReverseConfig<? extends DoubleOracleInformationSet> algConfig;

	private PrintStream debugOutput =  System.out;
    private long finishTime;
	
	final private double EPS = 0.00000001;
    final public static boolean DEBUG = false;
    final private static boolean MY_RP_BR_ORDERING = false;
    private static ThreadMXBean threadBean ;
	
	private static final int MAX_DEPTH = 1000;
	private static final int NUMBER_OF_TESTS = 10;

    public enum PlayerSelection {
        BOTH,SINGLE_ALTERNATING,SINGLE_IMPROVED
    }

    public static PlayerSelection playerSelection = PlayerSelection.BOTH;

	public static void main(String[] args) {
		
		threadBean = ManagementFactory.getThreadMXBean();
		long standardTime = 0;
		long reverseTime = 0;
		
		for(int i = 0; i<NUMBER_OF_TESTS; i++){
			//standardTime += runDiagnosticClassic();
			reverseTime += runDiagnosticReverse();
		}
		
		System.out.printf("Standard average time:\t %d \n", standardTime/NUMBER_OF_TESTS/1000000);
		System.out.printf("Reverse average time:\t %d \n", reverseTime/NUMBER_OF_TESTS/1000000);
	}
	
	
	
    private static long runDiagnosticReverse() {
    	long startTime = threadBean.getCurrentThreadCpuTime();
		runGoofSpiel();
//    	runBP();
    	//runPhantomTTT();
//    	runKuhnPoker();
//    	runGenericPoker();
		long finishTimeI = threadBean.getCurrentThreadCpuTime() - startTime;
		return finishTimeI;
	}



	private static long runDiagnosticClassic() {
    	long startTime = threadBean.getCurrentThreadCpuTime();
    	//GeneralDoubleOracle.runGoofSpiel();
    	GeneralDoubleOracle.runPhantomTTT();
		//GeneralDoubleOracle.runBP();
    	//GeneralDoubleOracle.runKuhnPoker();
    	//GeneralDoubleOracle.runGenericPoker();
		long finishTimeI = threadBean.getCurrentThreadCpuTime() - startTime;
		return finishTimeI;
	}
	
    public static void runPhantomTTT() {
        GameState rootState = new TTTState();
        GameInfo gameInfo = new TTTInfo();
        DoubleOracleReverseConfig<DoubleOracleReverseInformationSet> algConfig = new DoubleOracleReverseConfig<DoubleOracleReverseInformationSet>(rootState, gameInfo);
        Expander<DoubleOracleReverseInformationSet> expander = new TTTExpander<DoubleOracleReverseInformationSet>(algConfig);
        ReverseDoubleOracle  doefg = new ReverseDoubleOracle (rootState, expander, gameInfo, algConfig);
        doefg.generate(null);
    }



	public static void runKuhnPoker() {
        GameState rootState = new KuhnPokerGameState();
        GameInfo gameInfo = new KPGameInfo();
		DoubleOracleReverseConfig<DoubleOracleReverseInformationSet> algConfig = new DoubleOracleReverseConfig<DoubleOracleReverseInformationSet>(rootState, gameInfo);
        Expander<DoubleOracleReverseInformationSet> expander = new KuhnPokerExpander<DoubleOracleReverseInformationSet>(algConfig);
		ReverseDoubleOracle  doefg = new ReverseDoubleOracle (rootState,  expander, gameInfo, algConfig);
		doefg.generate(null);
    }

    public static void runGenericPoker() {
        GameState rootState = new GenericPokerGameState();
        GameInfo gameInfo = new GPGameInfo();
        DoubleOracleReverseConfig<DoubleOracleReverseInformationSet> algConfig = new DoubleOracleReverseConfig<DoubleOracleReverseInformationSet>(rootState, gameInfo);
        Expander<DoubleOracleReverseInformationSet> expander = new GenericPokerExpander<DoubleOracleReverseInformationSet>(algConfig);
        ReverseDoubleOracle  doefg = new ReverseDoubleOracle (rootState, expander, gameInfo, algConfig);
        doefg.generate(null);
    }

    public static void runBP() {
		GameState rootState = new BPGGameState();
		GameInfo gameInfo = new BPGGameInfo();
		DoubleOracleReverseConfig<DoubleOracleReverseInformationSet> algConfig = new DoubleOracleReverseConfig<DoubleOracleReverseInformationSet>(rootState, gameInfo);
        Expander<DoubleOracleReverseInformationSet> expander = new BPGExpander<DoubleOracleReverseInformationSet>(algConfig);
		ReverseDoubleOracle  doefg = new ReverseDoubleOracle (rootState, expander, gameInfo, algConfig);
        doefg.generate(null);
    }

    public static void runGoofSpiel() {
        GSGameInfo.useFixedNatureSequence = true;
        GameState rootState = new GoofSpielGameState();
        GSGameInfo gameInfo = new GSGameInfo();
        DoubleOracleReverseConfig<DoubleOracleReverseInformationSet> algConfig = new DoubleOracleReverseConfig<DoubleOracleReverseInformationSet>(rootState, gameInfo);
        Expander<DoubleOracleReverseInformationSet> expander = new GoofSpielExpander<DoubleOracleReverseInformationSet>(algConfig);
        ReverseDoubleOracle  doefg = new ReverseDoubleOracle (rootState, expander, gameInfo, algConfig);
        doefg.generate(null);
    }

    public ReverseDoubleOracle  (GameState rootState, Expander<DoubleOracleReverseInformationSet> expander, GameInfo config, DoubleOracleReverseConfig<DoubleOracleReverseInformationSet> algConfig) {
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
        SQFBestResponseReverseAlgorithm[] brAlgorithms = new SQFBestResponseReverseAlgorithm[] {
                new SQFBestResponseReverseAlgorithm(expander, 0, actingPlayers, algConfig, gameInfo),
                new SQFBestResponseReverseAlgorithm(expander, 1, actingPlayers, algConfig, gameInfo)};

        Map<Player, Map<Sequence, Double>> realizationPlans = new FixedSizeMap<Player, Map<Sequence, Double>>(2);
        
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
		DoubleOracleLPSolver doRestrictedGameSolver = new DoubleOracleSequenceFormLP(actingPlayers);

        doRestrictedGameSolver.setDebugOutput(debugOutput);
		
		double p1BoundUtility = gameInfo.getMaxUtility();
		double p2BoundUtility = gameInfo.getMaxUtility();
		
		int[] oldSize = new int[] {-1,-1};
        double[] lastBRValue = new double[] {-1.0, -1.0};

        mainloop:
        	
		while ((Math.abs(p1BoundUtility + p2BoundUtility) > EPS) ||
                Math.abs(doRestrictedGameSolver.getResultForPlayer(actingPlayers[0]) + doRestrictedGameSolver.getResultForPlayer(actingPlayers[1])) > EPS){

	        if(iterations==MAX_DEPTH)
	        	System.exit(0);
	        
			iterations++;
            debugOutput.println("Iteration " + iterations + ": Cumulative Time from Beginning:" + ((threadBean.getCurrentThreadCpuTime() - start)/1000000l));

			debugOutput.println("Last difference: " + (algConfig.getSizeForPlayer(actingPlayers[currentPlayerIndex]) - oldSize[currentPlayerIndex]));
            debugOutput.println("Current Size: " + algConfig.getSizeForPlayer(actingPlayers[currentPlayerIndex]));
			oldSize[currentPlayerIndex] = algConfig.getSizeForPlayer(actingPlayers[currentPlayerIndex]);


			int opponentPlayerIndex = ( currentPlayerIndex + 1 ) % 2;
			
			long startFullBR = threadBean.getCurrentThreadCpuTime();
            double currentBRVal;
            if (MY_RP_BR_ORDERING){
			    currentBRVal = brAlgorithms[currentPlayerIndex].calculateBR(rootState, realizationPlans.get(actingPlayers[opponentPlayerIndex]), realizationPlans.get(actingPlayers[currentPlayerIndex]));
	   
            }
			else{
                currentBRVal = brAlgorithms[currentPlayerIndex].calculateBR(rootState, realizationPlans.get(actingPlayers[opponentPlayerIndex]));
			}
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

            if (algConfig.getNewSequences().isEmpty()
                    && (Math.abs(p1BoundUtility + p2BoundUtility) > EPS)
                    && doRestrictedGameSolver.getNewSequencesSinceLastLPCalc(actingPlayers[0]).isEmpty()
                    && doRestrictedGameSolver.getNewSequencesSinceLastLPCalc(actingPlayers[1]).isEmpty()) {
                debugOutput.println("ERROR : NOT CONVERGED");
                break;
            }

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
                            doRestrictedGameSolver.getResultForPlayer(actingPlayers[opponentPlayerIndex]) == null) { // we have not calculated the reward for the current player in RG yet
                            currentPlayerIndex = opponentPlayerIndex;
                    } else {
                        double oldLPResult = doRestrictedGameSolver.getResultForPlayer(actingPlayers[currentPlayerIndex]);
                        if (currentPlayerIndex == 0) {
                            if (newFullBRSequences.size() > 0 && Math.abs(p1BoundUtility - (oldLPResult)) > Math.abs(p2BoundUtility - (-oldLPResult))) {
                                currentPlayerIndex = 0;
                            } else {
                                currentPlayerIndex = 1;
                            }
                        } else {
                            if (newFullBRSequences.size() == 0 || Math.abs(p1BoundUtility - (-oldLPResult)) >= Math.abs(p2BoundUtility - (oldLPResult))) {
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
            opponentPlayerIndex = (1+currentPlayerIndex)%2;

            Map<Sequence, Double> tmp = doRestrictedGameSolver.getResultStrategiesForPlayer(actingPlayers[currentPlayerIndex]);
            realizationPlans.put(actingPlayers[currentPlayerIndex], tmp);
            Map<Sequence, Double> tmp2 = doRestrictedGameSolver.getResultStrategiesForPlayer(actingPlayers[opponentPlayerIndex]);
            realizationPlans.put(actingPlayers[opponentPlayerIndex], tmp2);

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
		}



        debugOutput.println("done.");
        finishTime = (threadBean.getCurrentThreadCpuTime() - start)/1000000l;

        doRestrictedGameSolver.calculateStrategyForPlayer(1, rootState, algConfig, (p1BoundUtility + p2BoundUtility));

		int[] support_size = new int[] { 0, 0 };
		for (Player player : actingPlayers) {
			for (Sequence sequence : realizationPlans.get(player).keySet()) {
				if (realizationPlans.get(player).get(sequence) > 0) {
					support_size[player.getId()]++;
                    if (DEBUG)
                        debugOutput.println(sequence + "\t:\t" + realizationPlans.get(player).get(sequence));
				}
			}
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
        debugOutput.println("LP GenerationTime:" + doRestrictedGameSolver.getOverallGenerationTime());
        debugOutput.println("LP Constraint GenerationTime:" + doRestrictedGameSolver.getOverallConstraintGenerationTime());
        debugOutput.println("LP ComputationTime:" + doRestrictedGameSolver.getOverallConstraintLPSolvingTime());
        
        return realizationPlans;
	}

    public GameState findFirstNonNatureState(GameState rootState, Expander<? extends DoubleOracleInformationSet> expander) {
        GameState tmpState = rootState.copy();

        while (tmpState.isPlayerToMoveNature()) {
            Action action = expander.getActions(tmpState).get(0);
            tmpState = tmpState.performAction(action);
        }

        return tmpState;
    }

    public static void traverseCompleteGameTree(GameState rootState, Expander<DoubleOracleInformationSet> expander) {
        System.out.println("Calculating the size of the game.");
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

    public void setDebugOutput(PrintStream debugOutput) {
        this.debugOutput = debugOutput;
    }

    public long getFinishTime() {
        return finishTime;
    }
}
