package cz.agents.gtlibrary.strategy;

import java.util.Collection;
import java.util.Map;

import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Sequence;
import java.io.Serializable;

/**
 * Strategy holds mapping of sequences to their probability of occurrence,
 * only sequences with nonzero probability are stored.
 */
public abstract class Strategy implements Map<Sequence, Double>, Serializable  {

	public abstract Map<Action, Double> getDistributionOfContinuationOf(Sequence sequence, Collection<Action> actions);

	public static interface Factory {
		public Strategy create();
	}

	public double maxDifferenceFrom(Strategy other) {
		double max = -Double.MAX_VALUE;
		for (Map.Entry<Sequence, Double> en : entrySet()) {
			final Double otherVal = other.get(en.getKey());
			double diff = (otherVal == null ? en.getValue() : Math.abs(en.getValue() - otherVal));
			if (diff > max)
				max = diff;
		}
		for (Map.Entry<Sequence, Double> en : other.entrySet()) {
			if (get(en.getKey()) == null && en.getValue() > max) {
				max = en.getValue();
			}
		}
		return max;
	}

}
