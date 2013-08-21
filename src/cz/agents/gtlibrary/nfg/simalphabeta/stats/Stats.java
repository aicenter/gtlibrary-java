package cz.agents.gtlibrary.nfg.simalphabeta.stats;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.nfg.ActionPureStrategy;
import cz.agents.gtlibrary.nfg.MixedStrategy;

public class Stats {

	private static int abCutCountOverall = 0;
	private static int cacheCutCountOverall = 0;
	private static int statesVisitedOverall = 0;
	private static int nanCutsOverall = 0;
	private static int boundsTightenedOverall = 0;
	private static int p1StrategiesAddedOverall = 0;
	private static int p2StrategiesAddedOverall = 0;
	private static int p1NESizeOverall = 0;
	private static int p2NESizeOverall = 0;
	private static long solveLPTimeOverall = 0;
	private static long abTimeOverall = 0;
	private static int p1ABStatesOverall = 0;
	private static int p2ABStatesOverall = 0;
	private static int LPinvocationsOverall = 0;;
	private static int uniqueStatesOverall = 0;;;

	private static int abCutCount = 0;
	private static int cacheCutCount = 0;
	private static int statesVisited = 0;
	private static int nanCuts = 0;
	public static int fullRunFromNESolver = 0;
	private static int boundsTightened = 0;
	private static int p1StrategiesAdded = 0;
	private static int p2StrategiesAdded = 0;
	private static int p1NESize = 0;
	private static int p2NESize = 0;
	private static long solveLPTime = 0;
	private static long abTime = 0;
	private static int p1ABStates = 0;
	private static int p2ABStates = 0;
	private static Set<GameState> stateSet = new HashSet<GameState>();
	private static int LPinvocations = 0;

	public static void printInfo() {
		System.out.println("********** Current run statistics **********");
		System.out.println("AlphaBeta cuts: " + abCutCount);
		System.out.println("Cache cuts: " + cacheCutCount);
		System.out.println("States visited: " + statesVisited);
		System.out.println("nan cuts: " + nanCuts);
		System.out.println("Full LP runs from NESolver: " + fullRunFromNESolver);
		System.out.println("Bounds tightened: " + boundsTightened);
		System.out.println("First player strategies: " + p1StrategiesAdded);
		System.out.println("Second player strategies: " + p2StrategiesAdded);
		System.out.println("First player NE size: " + p1NESize);
		System.out.println("Second player NE size: " + p2NESize);
		System.out.println("LPSolve time: " + solveLPTime);
		System.out.println("Alpha-beta time: " + abTime);
		System.out.println("Alpha-beta states for player one: " + p1ABStates);
		System.out.println("Alpha-beta states for player two: " + p2ABStates);
		System.out.println("Unique LP states: " + stateSet.size());
		System.out.println("LP invocations: " + LPinvocations);
	}

	public static void printOverallInfo() {
		System.out.println("********** Overall statistics **********");
		System.out.println("AlphaBeta cuts: " + abCutCountOverall);
		System.out.println("Cache cuts: " + cacheCutCountOverall);
		System.out.println("States visited: " + statesVisitedOverall);
		System.out.println("nan cuts: " + nanCutsOverall);
		System.out.println("Bounds tightened: " + boundsTightenedOverall);
		System.out.println("First player strategies: " + p1StrategiesAddedOverall);
		System.out.println("Second player strategies: " + p2StrategiesAddedOverall);
		System.out.println("First player NE size: " + p1NESizeOverall);
		System.out.println("Second player NE size: " + p2NESizeOverall);
		System.out.println("LPSolve time: " + solveLPTimeOverall);
		System.out.println("Alpha-beta time: " + abTimeOverall);
		System.out.println("Alpha-beta states for player one: " + p1ABStatesOverall);
		System.out.println("Alpha-beta states for player two: " + p2ABStatesOverall);
		System.out.println("Unique LP states: " + uniqueStatesOverall);
		System.out.println("LP invocations: " + LPinvocationsOverall);
	}

	public static void reset() {
		abCutCount = 0;
		cacheCutCount = 0;
		statesVisited = 0;
		nanCuts = 0;
		fullRunFromNESolver = 0;
		boundsTightened = 0;
		p1StrategiesAdded = 0;
		p2StrategiesAdded = 0;
		p1NESize = 0;
		p2NESize = 0;
		solveLPTime = 0;
		abTime = 0;
		p1ABStates = 0;
		p2ABStates = 0;
		stateSet.clear();
		LPinvocations = 0;
	}

	public static void resetOverall() {
		abCutCountOverall = 0;
		cacheCutCountOverall = 0;
		statesVisitedOverall = 0;
		nanCutsOverall = 0;
		boundsTightenedOverall = 0;
		p1StrategiesAddedOverall = 0;
		p2StrategiesAddedOverall = 0;
		p1NESizeOverall = 0;
		p2NESizeOverall = 0;
		solveLPTimeOverall = 0;
		abTimeOverall = 0;
		p1ABStatesOverall = 0;
		p2ABStatesOverall = 0;
		LPinvocationsOverall = 0;
		uniqueStatesOverall = 0;
	}

	public static void incrementStatesVisited() {
		statesVisited++;
		statesVisitedOverall++;
	}

	public static void incrementP1StrategyCount() {
		p1StrategiesAdded++;
		p1StrategiesAddedOverall++;
	}

	public static void incrementP2StrategyCount() {
		p2StrategiesAdded++;
		p2StrategiesAddedOverall++;
	}

	public static void incrementBoundsTightened() {
		boundsTightened++;
		boundsTightenedOverall++;
	}

	public static void incrementNaNCuts() {
		nanCuts++;
		nanCutsOverall++;
	}

	public static void incrementCacheCuts() {
		cacheCutCount++;
		cacheCutCountOverall++;
	}

	public static void incrementABCuts() {
		abCutCount++;
		abCutCountOverall++;
	}

	public static void addToP1NESize(int amount) {
		p1NESize += amount;
		p1NESizeOverall += amount;
	}

	public static void addToP2NESize(int amount) {
		p2NESize += amount;
		p2NESizeOverall += amount;
	}

	public static void addToLPSolveTime(long time) {
		solveLPTime += time;
		solveLPTimeOverall += time;
		LPinvocations++;
		LPinvocationsOverall++;
	}

	public static void addToABTime(long time) {
		abTime += time;
		abTimeOverall += time;
	}

	public static void increaseP1ABStates() {
		p1ABStates++;
		p1ABStatesOverall++;
	}

	public static void increaseP2ABStates() {
		p2ABStates++;
		p2ABStatesOverall++;
	}

	public static void increaseABStatesFor(Player player) {
		if (player.getId() == 0)
			increaseP1ABStates();
		else
			increaseP2ABStates();
	}

	public static void addToP1NESize(MixedStrategy<ActionPureStrategy> p1MixedStrategy) {
		for (Entry<ActionPureStrategy, Double> entry : p1MixedStrategy) {
			if (entry.getValue() > 1e-8)
				addToP1NESize(1);
		}
	}

	public static void addToP2NESize(MixedStrategy<ActionPureStrategy> p2MixedStrategy) {
		for (Entry<ActionPureStrategy, Double> entry : p2MixedStrategy) {
			if (entry.getValue() > 1e-8)
				addToP2NESize(1);
		}
	}

	public static void addToP2StrategyCount(int count) {
		p2StrategiesAdded += count;
		p2StrategiesAddedOverall += count;
	}

	public static void addToP1StrategyCount(int count) {
		p1StrategiesAdded += count;
		p1StrategiesAddedOverall += count;
	}

	public static void addState(GameState state) {
		if(stateSet.add(state))
			uniqueStatesOverall++;
	}
}
