package cz.agents.gtlibrary.strategy;

import cz.agents.gtlibrary.iinodes.LinkedListSequenceImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Sequence;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class StrategyImpl extends Strategy {

	private Map<Sequence, Double> strategy;

	public StrategyImpl() {
		strategy = new HashMap<Sequence, Double>();
	}

	@Override
	public int size() {
		return strategy.size();
	}

	@Override
	public boolean isEmpty() {
		return strategy.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return strategy.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return strategy.containsValue(value);
	}

	@Override
	public Double get(Object key) {
		Double value = strategy.get(key);

		if (value == null) {
			return 0d;
		}
		return value;
	}

	@Override
	public Double put(Sequence key, Double value) {
		if(value == 0)
			return null;
		return strategy.put(key, value);
	}

	@Override
	public Double remove(Object key) {
		return strategy.remove(key);
	}

	@Override
	public void putAll(Map<? extends Sequence, ? extends Double> map) {
		for (Entry<? extends Sequence, ? extends Double> entry : map.entrySet()) {
			put(entry.getKey(), entry.getValue());
		}
	}

	@Override
	public void clear() {
		strategy.clear();
	}

	@Override
	public Set<Sequence> keySet() {
		return strategy.keySet();
	}

	@Override
	public Collection<Double> values() {
		return strategy.values();
	}

	@Override
	public Set<Entry<Sequence, Double>> entrySet() {
		return strategy.entrySet();
	}

	@Override
	public Map<Action, Double> getDistributionOfContinuationOf(Sequence sequence, Collection<Action> actions) {
		if (get(sequence) == 0)
			return getMissingSeqDistribution(actions);
		Map<Action, Double> distribution = new HashMap<Action, Double>();

		for (Action action : actions) {
			distribution.put(action, get(getContinuationSequence(sequence, action)));
		}
		distribution = normalize(distribution);
		return distribution;
	}

	public Sequence getContinuationSequence(Sequence sequence, Action action) {
		Sequence continuationSequence = new LinkedListSequenceImpl(sequence);

		continuationSequence.addLast(action);
		return continuationSequence;
	}

	private Map<Action, Double> normalize(Map<Action, Double> distribution) {
		double sum = getSum(distribution);
		
		if(sum == 0)
			return getMissingSeqDistribution(distribution.keySet());
		for (Entry<Action, Double> entry : distribution.entrySet()) {
			distribution.put(entry.getKey(), entry.getValue() / sum);
		}
		return distribution;
	}

	private double getSum(Map<Action, Double> distribution) {
		double sum = 0;

		for (Double value : distribution.values()) {
			sum += value;
		}
		return sum;
	}
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		
		result = prime * result + ((strategy == null) ? 0 : strategy.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StrategyImpl other = (StrategyImpl) obj;
		if (strategy == null) {
			if (other.strategy != null)
				return false;
		} else if (!strategy.equals(other.strategy))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return strategy.toString();
	}

	protected abstract Map<Action, Double> getMissingSeqDistribution(Collection<Action> actions);

}
