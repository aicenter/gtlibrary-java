package strategy;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.Test;

import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerAction;
import cz.agents.gtlibrary.iinodes.LinkedListSequenceImpl;
import cz.agents.gtlibrary.iinodes.PlayerImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.strategy.Strategy;
import cz.agents.gtlibrary.strategy.UniformStrategyForMissingSequences;

public class UniformStrategyForMissingSequencesTest {

	@Test
	public void getOnNonZeroProbabilitySequenceTest() {
		Strategy strategy = new UniformStrategyForMissingSequences();
		Sequence sequence = new LinkedListSequenceImpl(new PlayerImpl(0));
		
		sequence.addLast(new KuhnPokerAction("b", null, new PlayerImpl(0)));
		strategy.put(sequence, 0.5);
		assertEquals(0.5, strategy.get(sequence), 1e-8);
		assertEquals(true, strategy.containsKey(sequence));
	}
	
	@Test
	public void getOnMissingSequenceTest() {
		Strategy strategy = new UniformStrategyForMissingSequences();
		Sequence sequence = new LinkedListSequenceImpl(new PlayerImpl(0));
		
		sequence.addLast(new KuhnPokerAction("b", null, new PlayerImpl(0)));
		assertEquals(0, strategy.get(sequence), 1e-8);
		assertEquals(false, strategy.containsKey(sequence));
	}
	
	@Test
	public void getOnZeroProbabilitySequenceTest() {
		Strategy strategy = new UniformStrategyForMissingSequences();
		Sequence sequence = new LinkedListSequenceImpl(new PlayerImpl(0));
		
		sequence.addLast(new KuhnPokerAction("b", null, new PlayerImpl(0)));
		strategy.put(sequence, 0d);
		assertEquals(0d, strategy.get(sequence), 1e-8);
		assertEquals(false, strategy.containsKey(sequence));
	}
	
	@Test
	public void entrySetOnNonZeroProbabilitySequenceTest() {
		Strategy strategy = new UniformStrategyForMissingSequences();
		Sequence sequence = new LinkedListSequenceImpl(new PlayerImpl(0));
		
		sequence.addLast(new KuhnPokerAction("b", null, new PlayerImpl(0)));
		strategy.put(sequence, 0.5);

		Set<Entry<Sequence, Double>> entrySet = strategy.entrySet();
		
		assertEquals(false, entrySet.isEmpty());
	}
	
	@Test
	public void entrySetOnZeroProbabilitySequenceTest() {
		Strategy strategy = new UniformStrategyForMissingSequences();
		Sequence sequence = new LinkedListSequenceImpl(new PlayerImpl(0));
		
		sequence.addLast(new KuhnPokerAction("b", null, new PlayerImpl(0)));
		strategy.put(sequence, 0d);

		Set<Entry<Sequence, Double>> entrySet = strategy.entrySet();
		
		assertEquals(true, entrySet.isEmpty());
	}
	
	@Test
	public void keySetOnNonZeroProbabilitySequenceTest() {
		Strategy strategy = new UniformStrategyForMissingSequences();
		Sequence sequence = new LinkedListSequenceImpl(new PlayerImpl(0));
		
		sequence.addLast(new KuhnPokerAction("b", null, new PlayerImpl(0)));
		strategy.put(sequence, 0.5);

		Set<Sequence> keySet = strategy.keySet();
		
		assertEquals(false, keySet.isEmpty());
	}
	
	@Test
	public void keySetOnZeroProbabilitySequenceTest() {
		Strategy strategy = new UniformStrategyForMissingSequences();
		Sequence sequence = new LinkedListSequenceImpl(new PlayerImpl(0));
		
		sequence.addLast(new KuhnPokerAction("b", null, new PlayerImpl(0)));
		strategy.put(sequence, 0d);

		Set<Sequence> keySet = strategy.keySet();
		
		assertEquals(true, keySet.isEmpty());
	}
	
	@Test
	public void valuesOnNonZeroProbabilitySequenceTest() {
		Strategy strategy = new UniformStrategyForMissingSequences();
		Sequence sequence = new LinkedListSequenceImpl(new PlayerImpl(0));
		
		sequence.addLast(new KuhnPokerAction("b", null, new PlayerImpl(0)));
		strategy.put(sequence, 0.5);

		Collection<Double> values = strategy.values();
		
		assertEquals(false, values.isEmpty());
	}
	
	@Test
	public void valuesOnZeroProbabilitySequenceTest() {
		Strategy strategy = new UniformStrategyForMissingSequences();
		Sequence sequence = new LinkedListSequenceImpl(new PlayerImpl(0));
		
		sequence.addLast(new KuhnPokerAction("b", null, new PlayerImpl(0)));
		strategy.put(sequence, 0d);

		Collection<Double> values = strategy.values();
		
		assertEquals(true, values.isEmpty());
	}

	@Test
	public void distributionOfContinuationtTest() {
		Strategy strategy = new UniformStrategyForMissingSequences();
		Sequence sequence = new LinkedListSequenceImpl(new PlayerImpl(0));
		
		sequence.addLast(new KuhnPokerAction("b", null, new PlayerImpl(0)));
		List<Action> actions = new LinkedList<Action>();
		Action action1 = new KuhnPokerAction("c", null, new PlayerImpl(1));
		Action action2 = new KuhnPokerAction("f", null, new PlayerImpl(2));
		
		actions.add(action1);
		actions.add(action2);

		Map<Action, Double> distribution = strategy.getDistributionOfContinuationOf(sequence, actions);
		
		assertEquals(2, distribution.size());
		assertEquals(0.5, distribution.get(action1), 1e-8);
		assertEquals(0.5, distribution.get(action2), 1e-8);
	}
	
}
