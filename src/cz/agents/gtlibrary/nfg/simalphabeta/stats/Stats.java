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


package cz.agents.gtlibrary.nfg.simalphabeta.stats;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.nfg.ActionPureStrategy;
import cz.agents.gtlibrary.nfg.MixedStrategy;
import cz.agents.gtlibrary.utils.io.Exportable;

public class Stats implements Exportable {
	
	private static Stats instance;

	private int abCutCountOverall = 0;
	private int cacheCutCountOverall = 0;
	private int statesVisitedOverall = 0;
	private int nanCutsOverall = 0;
	private int boundsTightenedOverall = 0;
	private int p1StrategiesAddedOverall = 0;
	private int p2StrategiesAddedOverall = 0;
	private int p1NESizeOverall = 0;
	private int p2NESizeOverall = 0;
	private long solveLPTimeOverall = 0;
	private long abTimeOverall = 0;
	private int p1ABStatesOverall = 0;
	private int p2ABStatesOverall = 0;
	private int LPinvocationsOverall = 0;;
	private int uniqueStatesOverall = 0;
	private long time;

	private int abCutCount = 0;
	private int cacheCutCount = 0;
	private int statesVisited = 0;
	private int nanCuts = 0;
	public int fullRunFromNESolver = 0;
	private int boundsTightened = 0;
	private int p1StrategiesAdded = 0;
	private int p2StrategiesAdded = 0;
	private int p1NESize = 0;
	private int p2NESize = 0;
	private long solveLPTime = 0;
	private long abTime = 0;
	private int p1ABStates = 0;
	private int p2ABStates = 0;
	private Set<GameState> stateSet = new HashSet<GameState>();
	private int LPinvocations = 0;
	private long overallTime;

    private Map<Integer, Map<Boolean, Integer>> pureOrMixed = new HashMap<>();
    private Map<Integer, Map<Integer, Integer>> supportSize = new HashMap<>();
    private int supSizeHistSteps = 20;

	
	private ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
	
	private Stats() {
	}
	
	public static Stats getInstance() {
		if(instance == null)
			instance = new Stats();
		return instance;
	}

	public void printInfo() {
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
		System.out.println("Total time: " + time);
	}

	public void printOverallInfo() {
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
		System.out.println("Total time: " + overallTime);
	}

	public void reset() {
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

	public void resetOverall() {
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

	public void incrementStatesVisited() {
		statesVisited++;
		statesVisitedOverall++;
	}

	public void incrementP1StrategyCount() {
		p1StrategiesAdded++;
		p1StrategiesAddedOverall++;
	}

	public void incrementP2StrategyCount() {
		p2StrategiesAdded++;
		p2StrategiesAddedOverall++;
	}

	public void incrementBoundsTightened() {
		boundsTightened++;
		boundsTightenedOverall++;
	}

	public void incrementNaNCuts() {
		nanCuts++;
		nanCutsOverall++;
	}

	public void incrementCacheCuts() {
		cacheCutCount++;
		cacheCutCountOverall++;
	}

	public void incrementABCuts() {
		abCutCount++;
		abCutCountOverall++;
	}

	public void addToP1NESize(int amount) {
		p1NESize += amount;
		p1NESizeOverall += amount;
	}

	public void addToP2NESize(int amount) {
		p2NESize += amount;
		p2NESizeOverall += amount;
	}

	public void addToLPSolveTime(long time) {
		solveLPTime += time;
		solveLPTimeOverall += time;
		LPinvocations++;
		LPinvocationsOverall++;
	}

	public void addToABTime(long time) {
		abTime += time;
		abTimeOverall += time;
	}

	public void increaseP1ABStates() {
		p1ABStates++;
		p1ABStatesOverall++;
	}

	public void increaseP2ABStates() {
		p2ABStates++;
		p2ABStatesOverall++;
	}

	public void increaseABStatesFor(Player player) {
		if (player.getId() == 0)
			increaseP1ABStates();
		else
			increaseP2ABStates();
	}

	public int addToP1NESize(MixedStrategy<ActionPureStrategy> p1MixedStrategy) {
        int result = 0;
		for (Entry<ActionPureStrategy, Double> entry : p1MixedStrategy) {
            if (entry.getValue() > 1e-8) {
                addToP1NESize(1);
                result++;
            }
		}
        return result;
	}

	public int addToP2NESize(MixedStrategy<ActionPureStrategy> p2MixedStrategy) {
        int result = 0;
		for (Entry<ActionPureStrategy, Double> entry : p2MixedStrategy) {
			if (entry.getValue() > 1e-8) {
                addToP2NESize(1);
                result++;
            }
		}
        return result;
	}

	public void addToP2StrategyCount(int count) {
		p2StrategiesAdded += count;
		p2StrategiesAddedOverall += count;
	}

	public void addToP1StrategyCount(int count) {
		p1StrategiesAdded += count;
		p1StrategiesAddedOverall += count;
	}

	public void addState(GameState state) {
		if (stateSet.add(state))
			uniqueStatesOverall++;
	}

	@Override
	public String getColumnLabels() {
		return ";Alpha-beta;;;;LP;;;;Strategies;;;;Oracle;;;Total\n " + 
			";Time;Cuts;P1 states;P2 states;Time;Invocations;States;Unique states;P1 strategies;P2 Strategies;P1 NE;P2 NE;Cache cuts;Bounds tightened;NaN cuts;Time\n ";
	}

	@Override
	public String getColumnValues() {
		return ";" + abTimeOverall + ";" + abCutCountOverall + ";" + p1ABStatesOverall + ";" + p2ABStatesOverall + ";" + solveLPTimeOverall + ";" + LPinvocationsOverall + 
			";" + statesVisitedOverall + ";" + uniqueStatesOverall + ";" + p1StrategiesAddedOverall + ";" + p2StrategiesAddedOverall + 
			";" + p1NESizeOverall + ";" + p2NESizeOverall + ";" + cacheCutCountOverall + ";" + boundsTightenedOverall + ";" + nanCutsOverall + ";" + overallTime + "\n";
	}
	
	public void startTime() {
		time = threadMXBean.getCurrentThreadCpuTime()/1000000l;
	}
	
	public void stopTime() {
		time = threadMXBean.getCurrentThreadCpuTime()/1000000l - time;
		overallTime += time;
	}

    public void leavingNode(int depth, int supsize, int BF) {
        Map<Boolean, Integer> mapForDepth1 = pureOrMixed.get(depth);
        if (mapForDepth1 == null) {
            mapForDepth1 = new HashMap<>();
            mapForDepth1.put(true, 0);
            mapForDepth1.put(false, 0);
        }
        if (supsize == 1) {
            mapForDepth1.put(true, mapForDepth1.get(true) + 1);
        }  else {
            mapForDepth1.put(false, mapForDepth1.get(false) + 1);
        }
        pureOrMixed.put(depth, mapForDepth1);

        Map<Integer, Integer> mapForDepth2 = supportSize.get(depth);
        if (mapForDepth2 == null) {
            mapForDepth2 = new HashMap<>();
            for (int i=0; i<supSizeHistSteps+1; i++)
                mapForDepth2.put(i, 0);
        }

        int discSS = supsize*supSizeHistSteps/BF;
        mapForDepth2.put(discSS, mapForDepth2.get(discSS) + 1);
        supportSize.put(depth, mapForDepth2);
    }

    public void showSupportCounts() {
        System.out.println("Pure or Mixed:\n------------------");
        for (Integer d : pureOrMixed.keySet()) {
            System.out.println("Depth " + d + ": Pure:" + pureOrMixed.get(d).get(true) + " Mixed:" + pureOrMixed.get(d).get(false));
        }

        System.out.println("Support Sizes:\n------------------");
        for (Integer d : supportSize.keySet()) {
            System.out.println("Depth " + d + ":");
            for (int i=0; i<supSizeHistSteps+1; i++) {
                System.out.println("SS " + ((double)i/supSizeHistSteps) + ":" + supportSize.get(d).get(i));
            }
            System.out.println("--------");
        }

    }
}
