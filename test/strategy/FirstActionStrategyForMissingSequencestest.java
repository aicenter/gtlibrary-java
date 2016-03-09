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


package strategy;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.junit.Test;

import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerAction;
import cz.agents.gtlibrary.iinodes.LinkedListSequenceImpl;
import cz.agents.gtlibrary.iinodes.PlayerImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.strategy.Strategy;
import cz.agents.gtlibrary.strategy.FirstActionStrategyForMissingSequences;

public class FirstActionStrategyForMissingSequencestest {
	
	@Test
	public void getOnNonZeroProbabilitySequenceTest() {
		Strategy strategy = new FirstActionStrategyForMissingSequences();
		Sequence sequence = new LinkedListSequenceImpl(new PlayerImpl(0));
		
		sequence.addLast(new KuhnPokerAction("b", null, new PlayerImpl(0)));
		strategy.put(sequence, 0.5);
		assertEquals(0.5, strategy.get(sequence), 1e-8);
		assertEquals(true, strategy.containsKey(sequence));
	}
	
	@Test
	public void getOnMissingSequenceTest() {
		Strategy strategy = new FirstActionStrategyForMissingSequences();
		Sequence sequence = new LinkedListSequenceImpl(new PlayerImpl(0));
		
		sequence.addLast(new KuhnPokerAction("b", null, new PlayerImpl(0)));
		assertEquals(0, strategy.get(sequence), 1e-8);
		assertEquals(false, strategy.containsKey(sequence));
	}
	
	@Test
	public void getOnZeroProbabilitySequenceTest() {
		Strategy strategy = new FirstActionStrategyForMissingSequences();
		Sequence sequence = new LinkedListSequenceImpl(new PlayerImpl(0));
		
		sequence.addLast(new KuhnPokerAction("b", null, new PlayerImpl(0)));
		strategy.put(sequence, 0d);
		assertEquals(0d, strategy.get(sequence), 1e-8);
		assertEquals(false, strategy.containsKey(sequence));
	}
	
	@Test
	public void entrySetOnNonZeroProbabilitySequenceTest() {
		Strategy strategy = new FirstActionStrategyForMissingSequences();
		Sequence sequence = new LinkedListSequenceImpl(new PlayerImpl(0));
		
		sequence.addLast(new KuhnPokerAction("b", null, new PlayerImpl(0)));
		strategy.put(sequence, 0.5);

		Set<Entry<Sequence, Double>> entrySet = strategy.entrySet();
		
		assertEquals(false, entrySet.isEmpty());
	}
	
	@Test
	public void entrySetOnZeroProbabilitySequenceTest() {
		Strategy strategy = new FirstActionStrategyForMissingSequences();
		Sequence sequence = new LinkedListSequenceImpl(new PlayerImpl(0));
		
		sequence.addLast(new KuhnPokerAction("b", null, new PlayerImpl(0)));
		strategy.put(sequence, 0d);

		Set<Entry<Sequence, Double>> entrySet = strategy.entrySet();
		
		assertEquals(true, entrySet.isEmpty());
	}
	
	@Test
	public void keySetOnNonZeroProbabilitySequenceTest() {
		Strategy strategy = new FirstActionStrategyForMissingSequences();
		Sequence sequence = new LinkedListSequenceImpl(new PlayerImpl(0));
		
		sequence.addLast(new KuhnPokerAction("b", null, new PlayerImpl(0)));
		strategy.put(sequence, 0.5);

		Set<Sequence> keySet = strategy.keySet();
		
		assertEquals(false, keySet.isEmpty());
	}
	
	@Test
	public void keySetOnZeroProbabilitySequenceTest() {
		Strategy strategy = new FirstActionStrategyForMissingSequences();
		Sequence sequence = new LinkedListSequenceImpl(new PlayerImpl(0));
		
		sequence.addLast(new KuhnPokerAction("b", null, new PlayerImpl(0)));
		strategy.put(sequence, 0d);

		Set<Sequence> keySet = strategy.keySet();
		
		assertEquals(true, keySet.isEmpty());
	}
	
	@Test
	public void valuesOnNonZeroProbabilitySequenceTest() {
		Strategy strategy = new FirstActionStrategyForMissingSequences();
		Sequence sequence = new LinkedListSequenceImpl(new PlayerImpl(0));
		
		sequence.addLast(new KuhnPokerAction("b", null, new PlayerImpl(0)));
		strategy.put(sequence, 0.5);

		Collection<Double> values = strategy.values();
		
		assertEquals(false, values.isEmpty());
	}
	
	@Test
	public void valuesOnZeroProbabilitySequenceTest() {
		Strategy strategy = new FirstActionStrategyForMissingSequences();
		Sequence sequence = new LinkedListSequenceImpl(new PlayerImpl(0));
		
		sequence.addLast(new KuhnPokerAction("b", null, new PlayerImpl(0)));
		strategy.put(sequence, 0d);

		Collection<Double> values = strategy.values();
		
		assertEquals(true, values.isEmpty());
	}
	
	@Test
	public void distributionOfContinuationtTest() {
		Strategy strategy = new FirstActionStrategyForMissingSequences();
		Sequence sequence = new LinkedListSequenceImpl(new PlayerImpl(0));
		
		sequence.addLast(new KuhnPokerAction("b", null, new PlayerImpl(0)));
		List<Action> actions = new LinkedList<Action>();
		Action action1 = new KuhnPokerAction("c", null, new PlayerImpl(1));
		Action action2 = new KuhnPokerAction("f", null, new PlayerImpl(2));
		
		actions.add(action1);
		actions.add(action2);

		Map<Action, Double> distribution = strategy.getDistributionOfContinuationOf(sequence, actions);
		
		assertEquals(2, distribution.size());
		assertEquals(1, distribution.get(action1), 1e-8);
		assertEquals(0, distribution.get(action2), 1e-8);
	}
}
