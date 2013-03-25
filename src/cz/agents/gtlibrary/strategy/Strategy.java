package cz.agents.gtlibrary.strategy;

import java.util.Collection;
import java.util.Map;

import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Sequence;


/**
 * Strategy holds mapping of sequences to their probability of occurrence, 
 * only sequences with nonzero probability are stored.
 */
public interface Strategy extends Map<Sequence, Double> {

	public Map<Action, Double> getDistributionOfContinuationOf(Sequence sequence, Collection<Action> actions);

	public static interface Factory {
		public Strategy create();
	}

}
