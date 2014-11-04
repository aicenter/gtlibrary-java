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


package cz.agents.gtlibrary.algorithms.sequenceform.refinements.librarycom;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.DoubleOracleConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.DoubleOracleInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.Key;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.librarycom.resultparser.LemkeResultParser;
import cz.agents.gtlibrary.iinodes.LinkedListSequenceImpl;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.strategy.Strategy;
import cz.agents.gtlibrary.strategy.UniformStrategyForMissingSequences;

import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class DODataBuilder {

	protected String fileName;
	protected Data data;
	protected Player[] players;
	protected PrintStream output;
	protected Double p1Value;
	protected Map<Sequence, Double> p1RealizationPlan;
	protected Map<Sequence, Double> p2RealizationPlan;
	protected long generationTime;
	protected long constraintGenerationTime;
	protected long lpSolvingTime;
	protected GameState rootState;
	protected Expander<? extends InformationSet> expander;
	protected Map<GameState, Double> tempUtilities;
	protected Map<Map<Player, Sequence>, Double> utilForSeqComb;

	public DODataBuilder(Player[] players, GameState rootState, Expander<? extends InformationSet> expander) {
		this.players = players;
		fileName = "DO_INPUT";
		p1Value = Double.NaN;

		this.rootState = rootState;
		this.expander = expander;
		initTable();
	}

	public void initTable() {
		Sequence p1EmptySequence = new LinkedListSequenceImpl(players[0]);
		Sequence p2EmptySequence = new LinkedListSequenceImpl(players[1]);

		data = new Data();

		initE(p1EmptySequence);
		initF(p2EmptySequence);
	}
	
	public void initF(Sequence p2EmptySequence) {
		data.setF(new Key("Q", p2EmptySequence), p2EmptySequence, 1);//F in root (only 1)
	}

	public void initE(Sequence p1EmptySequence) {
		data.setE(new Key("P", p1EmptySequence), p1EmptySequence, 1);//E in root (only 1)
	}

	public void solve() {
		try {
			data.exportLemkeData(fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			Runtime.getRuntime().exec("lemkeQP " + fileName).waitFor();
		} catch (IOException e) {
			System.err.println("Error during library invocation...");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		LemkeResultParser parser = new LemkeResultParser(fileName + "l1qp", getP1IndicesOfSequences(), getP2IndicesOfSequences());
		
		p1RealizationPlan = parser.getP1RealizationPlan();
		p2RealizationPlan = parser.getP2RealizationPlan();
		
		for (Entry<Sequence, Double> entry : p1RealizationPlan.entrySet()) {
			if(entry.getValue() > 0)
				System.out.println(entry);
		}
		for (Entry<Sequence, Double> entry : p2RealizationPlan.entrySet()) {
			if(entry.getValue() > 0)
				System.out.println(entry);
		}
		
		Strategy p1Strategy = new UniformStrategyForMissingSequences();
		Strategy p2Strategy = new UniformStrategyForMissingSequences();
		
		p1Strategy.putAll(p1RealizationPlan);
		p2Strategy.putAll(p2RealizationPlan);
		
//		UtilityCalculator calculator = new UtilityCalculatorForRestrictedGame(rootState, expander, tempUtilities);
//		p1Value = calculator.computeUtility(p1Strategy, p2Strategy);
//		System.out.println(calculator.computeUtility(p1Strategy, p2Strategy));
		
//		p1Value = -parser.getGameValue();
		
		GameValueCalculator calculator = new GameValueCalculator(utilForSeqComb, p1RealizationPlan, p2RealizationPlan);
		
		p1Value = calculator.getGameValue();

	}


	public void calculateStrategyForPlayer(int playerIndex, GameState root, DoubleOracleConfig<DoubleOracleInformationSet> config, double currentBoundSize) {
		long startTime = System.currentTimeMillis();

		tempUtilities = config.getActualNonZeroUtilityValuesInLeafs();
		utilForSeqComb = config.getUtilityForSequenceCombination();
		p1Value = Double.NaN;
		initTable();
		for (Sequence sequence : config.getSequencesFor(players[0])) {
			data.addSequence(sequence);
			data.addISKeyFor(players[0], sequence.getLastInformationSet());
			updateForP1(sequence);
		}
		for (Sequence sequence : config.getSequencesFor(players[1])) {
			data.addSequence(sequence);
			data.addISKeyFor(players[1], sequence.getLastInformationSet());
			updateForP2(sequence);
		}
		addUtilities(config, config.getSequencesFor(players[0]), config.getSequencesFor(players[1]));
		constraintGenerationTime += System.currentTimeMillis() - startTime;
		generationTime += System.currentTimeMillis() - startTime;
	}

	public void updateForP2(Sequence sequence) {
		if (sequence.size() == 0)
			return;
		Object eqKey = getKeyForIS("Q", sequence);
		Object varKey = getSubsequenceKey(sequence);

		data.setF(eqKey, varKey, -1);//F
		addLinksToPrevISForP2(sequence, eqKey);
	}

	public void updateForP1(Sequence sequence) {
		if (sequence.size() == 0)
			return;
		Object eqKey = getKeyForIS("P", sequence);
		Object varKey = getSubsequenceKey(sequence);

		data.setE(eqKey, varKey, -1);//E
		addLinksToPrevISForP1(sequence, eqKey);
	}

	protected void addLinksToPrevISForP2(Sequence sequence, Object eqKey) {
		SequenceInformationSet set = (SequenceInformationSet) sequence.getLastInformationSet();

		for (Sequence outgoingSequence : set.getOutgoingSequences()) {
			data.setF(eqKey, outgoingSequence, 1);//F child
			data.addP2PerturbationsFor(outgoingSequence);
		}
	}

	public void addLinksToPrevISForP1(Sequence sequence, Object eqKey) {
		SequenceInformationSet set = (SequenceInformationSet) sequence.getLastInformationSet();

		for (Sequence outgoingSequence : set.getOutgoingSequences()) {
			data.setE(eqKey, outgoingSequence, 1);//E child
			data.addP1PerturbationsFor(outgoingSequence);
		}
	}
	
	protected Object getSubsequenceKey(Sequence sequence) {
		return sequence.getSubSequence(sequence.size() - 1);
	}

	protected Object getKeyForIS(String string, Sequence sequence) {
		return new Key(string, sequence.getLastInformationSet());
	}

	protected void addUtilities(DoubleOracleConfig<DoubleOracleInformationSet> config, Iterable<Sequence> p1Sequences, Iterable<Sequence> p2Sequences) {
		for (Sequence p1Sequence : p1Sequences) {
			for (Sequence p2Sequence : p2Sequences) {
				Double utility = config.getUtilityFromAllFor(p1Sequence, p2Sequence);

				if (utility != null)
					data.addToU1(p1Sequence, p2Sequence, utility);
			}
		}
	}

	public Double getResultForPlayer(Player player) {
		if (Double.isNaN(p1Value))
			solve();
		return (player.equals(players[0]) ? 1 : -1) * p1Value;
	}

	public void setDebugOutput(PrintStream debugOutput) {
		output = debugOutput;
	}

	public Map<Sequence, Double> getResultStrategiesForPlayer(Player player) {
		if (Double.isNaN(p1Value))
			solve();
		return player.equals(players[0]) ? p1RealizationPlan : p2RealizationPlan;
	}
	
	public Map<Integer, Sequence> getP1IndicesOfSequences() {
		return getRevertedMapping(data.getColumnIndicesE(), players[0]);
	}

	public Map<Integer, Sequence> getP2IndicesOfSequences() {
		return getRevertedMapping(data.getColumnIndicesF(), players[1]);
	}

	public Map<Integer, Sequence> getRevertedMapping(Map<Object, Integer> map, Player player) {
		Map<Integer, Sequence> p1Indices = new HashMap<Integer, Sequence>();

		for (Entry<Object, Integer> entry : map.entrySet()) {
			if (entry.getKey() instanceof Sequence)
				p1Indices.put(entry.getValue(), (Sequence) entry.getKey());
		}
		return p1Indices;
	}
	
	public long getOverallGenerationTime() {
		return generationTime;
	}

	public long getOverallConstraintGenerationTime() {
		return constraintGenerationTime;
	}

	public long getOverallConstraintLPSolvingTime() {
		return lpSolvingTime;
	}

}
