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


package cz.agents.gtlibrary.algorithms.sequenceform;

import cz.agents.gtlibrary.domain.aceofspades.AoSExpander;
import cz.agents.gtlibrary.domain.aceofspades.AoSGameInfo;
import cz.agents.gtlibrary.domain.aceofspades.AoSGameState;
import cz.agents.gtlibrary.domain.artificialchance.ACExpander;
import cz.agents.gtlibrary.domain.artificialchance.ACGameInfo;
import cz.agents.gtlibrary.domain.artificialchance.ACGameState;
import cz.agents.gtlibrary.domain.bpg.BPGExpander;
import cz.agents.gtlibrary.domain.bpg.BPGGameInfo;
import cz.agents.gtlibrary.domain.bpg.BPGGameState;
import cz.agents.gtlibrary.domain.exploitabilityGame.ExploitExpander;
import cz.agents.gtlibrary.domain.exploitabilityGame.ExploitGameInfo;
import cz.agents.gtlibrary.domain.exploitabilityGame.ExploitGameState;
import cz.agents.gtlibrary.domain.flipit.*;
import cz.agents.gtlibrary.domain.goofspiel.GSGameInfo;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielExpander;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielGameState;
import cz.agents.gtlibrary.domain.honeypotGame.HoneypotExpander;
import cz.agents.gtlibrary.domain.honeypotGame.HoneypotGameInfo;
import cz.agents.gtlibrary.domain.honeypotGame.HoneypotGameState;
import cz.agents.gtlibrary.domain.liarsdice.LDGameInfo;
import cz.agents.gtlibrary.domain.liarsdice.LiarsDiceExpander;
import cz.agents.gtlibrary.domain.liarsdice.LiarsDiceGameState;
import cz.agents.gtlibrary.domain.oshizumo.OZGameInfo;
import cz.agents.gtlibrary.domain.oshizumo.OshiZumoExpander;
import cz.agents.gtlibrary.domain.oshizumo.OshiZumoGameState;
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
import cz.agents.gtlibrary.domain.upordown.UDExpander;
import cz.agents.gtlibrary.domain.upordown.UDGameInfo;
import cz.agents.gtlibrary.domain.upordown.UDGameState;
import cz.agents.gtlibrary.iinodes.ActionImpl;
import cz.agents.gtlibrary.iinodes.SimultaneousGameState;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.nfg.simalphabeta.SimABConfig;
import cz.agents.gtlibrary.nfg.simalphabeta.SimABInformationSet;
import cz.agents.gtlibrary.strategy.Strategy;
import cz.agents.gtlibrary.strategy.UniformStrategyForMissingSequences;
import cz.agents.gtlibrary.utils.io.GambitEFG;

import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class FullSequenceEFG {

	private GameState rootState;
	private Expander<SequenceInformationSet> expander;
	private GameInfo gameConfig;
	private SequenceFormConfig<SequenceInformationSet> algConfig;

    private long finishTime;

	private PrintStream debugOutput = System.out;
	final private static boolean DEBUG = true;
	private ThreadMXBean threadBean;

	private double gameValue = Double.NaN;

	private static double eps = 1e-6;

	public static void main(String[] args) {
//		runAC();
//		runAoS();
//		runKuhnPoker();
//		runGenericPoker();
//		runBPG();
//		runGoofSpiel();
//      runRandomGame();
//      runSimRandomGame();
//        runLiarsDice();
//		runPursuit();
//      runPhantomTTT();
//		runUpOrDown();
//        runOshiZumo();
//        testExploitGame();
//		runFlipIt();
		runHoneyPot();
	}

	private static void runHoneyPot(){
		HoneypotGameInfo gameInfo = new HoneypotGameInfo();
		HoneypotGameState rootState = new HoneypotGameState(gameInfo.allNodes);
		SequenceFormConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<>();
		FullSequenceEFG efg = new FullSequenceEFG(rootState, new HoneypotExpander<>(algConfig), gameInfo, algConfig);
		efg.generate();

		GambitEFG gambit = new GambitEFG();
		gambit.buildAndWrite("HPG_test.gbt", rootState, new HoneypotExpander<>(algConfig));
	}

	private static void runFlipIt(){
		boolean PRINT_STRATEGY = false;
		boolean shiftedRootstate = false;
		String[] defenderActions = new String[]{"ID1","ID4", "ID1", "ID4"};
		String[] attackerActions = new String[]{"ID1", "ID1", "ID4", "ID1"};
		FlipItGameInfo gameInfo = new FlipItGameInfo();
		gameInfo.ZERO_SUM_APPROX = true;
		NodePointsFlipItGameState rootState = null;

		switch (FlipItGameInfo.gameVersion){
			case NO:                    rootState = new NoInfoFlipItGameState(); break;
			case FULL:                  rootState = new FullInfoFlipItGameState(); break;
			case REVEALED_ALL_POINTS:   rootState = new AllPointsFlipItGameState(); break;
			case REVEALED_NODE_POINTS:  rootState = new NodePointsFlipItGameState(); break;
		}

		SequenceFormConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
		FlipItExpander<SequenceInformationSet> expander = new FlipItExpander<>(algConfig);

		if (shiftedRootstate){
			Action performingAction = null;
			for (int i = 0; i < defenderActions.length; i++){
				for (Action a : expander.getActions(rootState)){
					if (!((FlipItAction)a).isNoop() && ((FlipItAction)a).getControlNode().getId().equals(defenderActions[i])){
						performingAction = a;
//						System.out.println("found");
						break;
					}
				}
//				algConfig.addStateToSequenceForm(rootState);
				performingAction.perform(rootState);
//				rootState = rootState.performAction(performingAction);
//				rootState.performActionModifyingThisState(performingAction);
				for (Action a : expander.getActions(rootState)){
					if (!((FlipItAction)a).isNoop() && ((FlipItAction)a).getControlNode().getId().equals(attackerActions[i])){
						performingAction = a;
//						System.out.println("found");
						break;
					}
				}
//				algConfig.addStateToSequenceForm(rootState);
//				rootState.performActionModifyingThisState(performingAction);
//				rootState = rootState.performAction(performingAction);
				performingAction.perform(rootState);
			}
			System.out.println("New rootstate : ");
			System.out.println(rootState.getSequenceFor(gameInfo.getAllPlayers()[0]));
			System.out.println(rootState.getSequenceFor(gameInfo.getAllPlayers()[1]));
			System.out.println("Utility = " + Arrays.toString(rootState.evaluate()));
			System.out.println("Ownership = " + Arrays.toString(rootState.getDefenderControlledNodes()));
		}


		FullSequenceEFG efg = new FullSequenceEFG(rootState, new FlipItExpander<>(algConfig), gameInfo, algConfig);
		Map<Player, Map<Sequence, Double>> rps = efg.generate();

		if (PRINT_STRATEGY) {
			for (Entry<Sequence, Double> entry : rps.get(rootState.getAllPlayers()[0]).entrySet()) {
				if (entry.getValue() > eps)
					System.out.println(entry);
			}
			System.out.println("**********");
			for (Entry<Sequence, Double> entry : rps.get(rootState.getAllPlayers()[1]).entrySet()) {
				if (entry.getValue() > eps)
					System.out.println(entry);
			}
		}

		Double maxUtility = Double.MIN_VALUE;
		for (Double utility : algConfig.getActualNonZeroUtilityValuesInLeafs().values()){
			if (Math.abs(utility) > maxUtility)
				maxUtility = Math.abs(utility);
		}
		System.out.println("GI maxUtility : "+gameInfo.getMaxUtility() + "; GT maxUtility : " + maxUtility);

		GambitEFG gambit = new GambitEFG();
//		gambit.buildAndWrite("flipit.gbt", rootState, new FlipItExpander<>(algConfig));

		int larger = 0;
		for (InformationSet set : algConfig.getAllInformationSets().values()) {
			if (set.getPlayer().equals(FlipItGameInfo.DEFENDER) && set.getAllStates().size()>1)
				larger++;
			if (set.getPlayer().equals(FlipItGameInfo.ATTACKER) && set.getAllStates().size()>(FlipItGameInfo.graph.getAllNodes().size()+1))
				larger++;
		}

		System.out.println("Number of non-simultaneous ISs: "+larger);

	}

    private static void testExploitGame() {
        SimultaneousGameState s = new OshiZumoGameState();
        SimABConfig algConfig = new SimABConfig();
        Expander<SimABInformationSet> e = new OshiZumoExpander<>(algConfig);
        GameInfo gi = new OZGameInfo();

        ExploitGameInfo ngi = new ExploitGameInfo(s,e,gi);
        ExploitGameState ns = new ExploitGameState(ngi);
        ExploitExpander ne = new ExploitExpander(new SequenceFormConfig(),ngi);

        FullSequenceEFG efg = new FullSequenceEFG(ns,ne,ngi,(SequenceFormConfig)ne.getAlgorithmConfig());
        efg.generateCompleteGame();

        GambitEFG gambitEFG = new GambitEFG();
        gambitEFG.write("EXPL.gbt", ns, ne);

    }
	
	private static void runUpOrDown() {
		GameState rootState = new UDGameState();
		GameInfo gameInfo = new UDGameInfo();
		SequenceFormConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
		FullSequenceEFG efg = new FullSequenceEFG(rootState, new UDExpander<SequenceInformationSet>(algConfig), gameInfo, algConfig);

		System.out.println(efg.generate());
	}

	public static void runAC() {
		GameState rootState = new ACGameState();
		GameInfo gameInfo = new ACGameInfo();
		SequenceFormConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
		FullSequenceEFG efg = new FullSequenceEFG(rootState, new ACExpander<SequenceInformationSet>(algConfig), gameInfo, algConfig);

		Map<Player, Map<Sequence, Double>> rps = efg.generate();
		
		for (Entry<Sequence, Double> entry : rps.get(rootState.getAllPlayers()[0]).entrySet()) {
			if(entry.getValue() > 0)
				System.out.println(entry);
		}
		System.out.println("**********");
		for (Entry<Sequence, Double> entry : rps.get(rootState.getAllPlayers()[1]).entrySet()) {
			if(entry.getValue() > 0)
				System.out.println(entry);
		}
	}

    public static void runOshiZumo() {
        GameState rootState = new OshiZumoGameState();
        GameInfo gameInfo = new OZGameInfo();
        SequenceFormConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
        Expander expander = new OshiZumoExpander<>(algConfig);
        FullSequenceEFG efg = new FullSequenceEFG(rootState, expander, gameInfo, algConfig);

        efg.generate();
//        efg.generateCompleteGame();
//        GambitEFG.write("OZ.gbt", rootState, expander);
    }


	public static void runPursuit() {
		GameState rootState = new PursuitGameState();
		GameInfo gameInfo = new PursuitGameInfo();
		SequenceFormConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
		FullSequenceEFG efg = new FullSequenceEFG(rootState, new PursuitExpander<SequenceInformationSet>(algConfig), gameInfo, algConfig);

		efg.generate();
	}

	public static void runAoS() {
		GameState rootState = new AoSGameState();
		GameInfo gameInfo = new AoSGameInfo();
		SequenceFormConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
		FullSequenceEFG efg = new FullSequenceEFG(rootState, new AoSExpander<SequenceInformationSet>(algConfig), gameInfo, algConfig);

		System.out.println(efg.generate());
	}

	public static void runSimRandomGame() {
        //bb used for debugging random games -- can be removed after while
//        double sum=0;
//        double min=Double.POSITIVE_INFINITY;
//        double max=Double.NEGATIVE_INFINITY;
//        for (int seed=0; seed < 100; seed++) {
//            RandomGameInfo.seed = seed+300;
//            GameState rootState = new SimRandomGameState();
//    		GameInfo gameInfo = new RandomGameInfo();
//	    	SequenceFormConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
//            Expander expander = new RandomGameExpander<SequenceInformationSet>(algConfig);
//    		FullSequenceEFG efg = new FullSequenceEFG(rootState, expander, gameInfo, algConfig);
//            efg.debugOutput = DummyPrintStream.getDummyPS();
//	    	efg.generate();
//            sum += efg.getGameValue();
//            min = Math.min(min,efg.getGameValue());
//            max = Math.max(max,efg.getGameValue());
//        }
//        System.out.println(sum);
//        System.out.println(min);
//        System.out.println(max);
//        return;
		GameState rootState = new SimRandomGameState();
		GameInfo gameInfo = new RandomGameInfo();
		SequenceFormConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
                Expander expander = new RandomGameExpander<SequenceInformationSet>(algConfig);
		FullSequenceEFG efg = new FullSequenceEFG(rootState, expander, gameInfo, algConfig);

		efg.generate();
//
//        GambitEFG gambitEFG = new GambitEFG();
//        gambitEFG.write("RG.gbt", rootState, expander);
	}

	public static void runKuhnPoker() {
		GameState rootState = new KuhnPokerGameState();
		KPGameInfo gameInfo = new KPGameInfo();
		SequenceFormConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
                Expander expander = new KuhnPokerExpander<SequenceInformationSet>(algConfig);
		FullSequenceEFG efg = new FullSequenceEFG(rootState, expander, gameInfo, algConfig);

		Map<Player, Map<Sequence, Double>> rps = efg.generate();
//        GambitEFG.write("GP.gbt", rootState, expander);

//		for (Entry<Sequence, Double> entry : rps.get(rootState.getAllPlayers()[0]).entrySet()) {
//			if(entry.getValue() > 0)
//				System.out.println(entry);
//		}
//		System.out.println("**********");
//		for (Entry<Sequence, Double> entry : rps.get(rootState.getAllPlayers()[1]).entrySet()) {
//			if(entry.getValue() > 0)
//				System.out.println(entry);
//		}
	}

	
        public static void runLiarsDice() {
                GameState rootState = new LiarsDiceGameState();
                LDGameInfo gameInfo = new LDGameInfo();
                SequenceFormConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
                Expander expander = new LiarsDiceExpander<SequenceInformationSet>(algConfig);
                FullSequenceEFG efg = new FullSequenceEFG(rootState, expander, gameInfo, algConfig);

                Map<Player, Map<Sequence, Double>> rps = efg.generate();
                Strategy s = new UniformStrategyForMissingSequences();
                s.putAll(rps.get(rootState.getAllPlayers()[0]));
                System.out.println(s.fancyToString(rootState, expander, rootState.getAllPlayers()[0]));
        }

	public static void runRandomGame() {
		GameState rootState = new RandomGameState();
		GameInfo gameInfo = new RandomGameInfo();
		SequenceFormConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
        RandomGameExpander<SequenceInformationSet> expander = new RandomGameExpander<SequenceInformationSet>(algConfig);
        FullSequenceEFG efg = new FullSequenceEFG(rootState, expander, gameInfo, algConfig);

		efg.generate();

//        GambitEFG.write("randomgame.gbt", rootState, (Expander)expander);
	}

	public static void runGoofSpiel() {
        GSGameInfo.useFixedNatureSequence = true;
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
        Expander expander = new GenericPokerExpander<SequenceInformationSet>(algConfig);
		FullSequenceEFG efg = new FullSequenceEFG(rootState, expander, gameInfo, algConfig);
		Map<Player, Map<Sequence, Double>> rps = efg.generate();

//        GambitEFG.write("GP.gbt", rootState, expander);
//		for (Entry<Sequence, Double> entry : rps.get(rootState.getAllPlayers()[1]).entrySet()) {
//
//			if (entry.getValue() > 0)
//				System.out.println(entry);
//		}
//		UtilityCalculator calculator = new UtilityCalculator(rootState, new GenericPokerExpander<SequenceInformationSet>(algConfig));
//		Strategy p1Strategy = new UniformStrategyForMissingSequences();
//		Strategy p2Strategy = new UniformStrategyForMissingSequences();
//
//		p1Strategy.putAll(rps.get(new PlayerImpl(0)));
//		p2Strategy.putAll(rps.get(new PlayerImpl(1)));
//		System.out.println(calculator.computeUtility(p1Strategy, p2Strategy));
	}

	public static void runBPG() {
		GameState rootState = new BPGGameState();
		BPGGameInfo gameInfo = new BPGGameInfo();
		SequenceFormConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
		FullSequenceEFG efg = new FullSequenceEFG(rootState, new BPGExpander<SequenceInformationSet>(algConfig), gameInfo, algConfig);
        efg.generate();
	}

    public static void runPhantomTTT() {
        GameState rootState = new TTTState();
        GameInfo gameInfo = new TTTInfo();
        SequenceFormConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
        Expander expander = new TTTExpander<SequenceInformationSet>(algConfig);
        FullSequenceEFG efg = new FullSequenceEFG(rootState, expander, gameInfo, algConfig);
        efg.generate();
//        GeneralDoubleOracle.traverseCompleteGameTree(rootState, expander);
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
		threadBean = ManagementFactory.getThreadMXBean();

		long start = threadBean.getCurrentThreadCpuTime();
		long overallSequenceGeneration = 0;
		long overallCPLEX = 0;
		Map<Player, Map<Sequence, Double>> realizationPlans = new HashMap<Player, Map<Sequence, Double>>();
		long startGeneration = threadBean.getCurrentThreadCpuTime();

		generateCompleteGame();
        debugOutput.println("Game tree built...");
        debugOutput.println("Information set count: " + algConfig.getAllInformationSets().size());
		debugOutput.println("Sequences: " + algConfig.getSequencesFor(gameConfig.getAllPlayers()[0]).size() + ", " + algConfig.getSequencesFor(gameConfig.getAllPlayers()[1]).size());
		overallSequenceGeneration = (threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l;

		Player[] actingPlayers = new Player[] { rootState.getAllPlayers()[0], rootState.getAllPlayers()[1] };
		long startCPLEX = threadBean.getCurrentThreadCpuTime();
		FullSequenceFormLP sequenceFormLP = new SequenceFormLP(actingPlayers);
//        FullSequenceFormLP sequenceFormLP = new FullUndominatedSolver(actingPlayers);
//        FullSequenceFormLP sequenceFormLP = new FullNFPSolver(actingPlayers, gameConfig);



        sequenceFormLP.setDebugOutput(debugOutput);
		sequenceFormLP.calculateBothPlStrategy(rootState, algConfig);

		long thisCPLEX = (threadBean.getCurrentThreadCpuTime() - startCPLEX) / 1000000l;

		overallCPLEX += thisCPLEX;

		for (Player player : rootState.getAllPlayers()) {
			realizationPlans.put(player, sequenceFormLP.getResultStrategiesForPlayer(player));
		}

        debugOutput.println("done.");
		finishTime = (threadBean.getCurrentThreadCpuTime() - start) / 1000000l;

		int[] support_size = new int[] { 0, 0 };
		for (Player player : actingPlayers) {
			for (Sequence sequence : realizationPlans.get(player).keySet()) {
				if (realizationPlans.get(player).get(sequence) > 0) {
					support_size[player.getId()]++;
					if (DEBUG)
						System.out.println(sequence + "\t:\t" + realizationPlans.get(player).get(sequence));
				}
			}
		}

//		try {
//			Runtime.getRuntime().gc();
//			Thread.sleep(500l);
//		} catch (InterruptedException e) {
//		}

		gameValue = sequenceFormLP.getResultForPlayer(actingPlayers[0]);
        debugOutput.println("final size: FirstPlayer Sequences: " + algConfig.getSequencesFor(actingPlayers[0]).size() + " \t SecondPlayer Sequences : " + algConfig.getSequencesFor(actingPlayers[1]).size());
        debugOutput.println("final support_size: FirstPlayer: " + support_size[0] + " \t SecondPlayer: " + support_size[1]);
        debugOutput.println("final result:" + gameValue);
        debugOutput.println("final memory:" + ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024));
        debugOutput.println("final time: " + finishTime);
        debugOutput.println("final CPLEX time: " + overallCPLEX);
        debugOutput.println("final BR time: " + 0);
        debugOutput.println("final RGB time: " + 0);
        debugOutput.println("final StrategyGenerating time: " + overallSequenceGeneration);

		if (DEBUG) {
			// sanity check -> calculation of Full BR on the solution of SQF LP
			SQFBestResponseAlgorithm brAlg = new SQFBestResponseAlgorithm(expander, 0, actingPlayers, algConfig, gameConfig);
            debugOutput.println("BR: " + brAlg.calculateBR(rootState, realizationPlans.get(actingPlayers[1])));

			SQFBestResponseAlgorithm brAlg2 = new SQFBestResponseAlgorithm(expander, 1, actingPlayers, algConfig, gameConfig);
            debugOutput.println("BR: " + brAlg2.calculateBR(rootState, realizationPlans.get(actingPlayers[0])));

			algConfig.validateGameStructure(rootState, expander);
		}
		return realizationPlans;
	}

	public void generateCompleteGame() {
		ArrayDeque<GameState> queue = new ArrayDeque<GameState>();

		queue.add(rootState);

		while (queue.size() > 0) {
			GameState currentState = queue.removeLast();

			algConfig.addStateToSequenceForm(currentState);
			if (currentState.isGameEnd()) {
				algConfig.setUtility(currentState);
				continue;
			}
			for (Action action : expander.getActions(currentState)) {
				queue.add(currentState.performAction(action));
			}
		}
	}

	public double getGameValue() {
		return gameValue;
	}

    public long getFinishTime() {
        return finishTime;
    }

    public void setDebugOutput(PrintStream debugOutput) {
        this.debugOutput = debugOutput;
    }

}
