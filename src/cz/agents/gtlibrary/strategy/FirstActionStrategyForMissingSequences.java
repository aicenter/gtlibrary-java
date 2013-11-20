package cz.agents.gtlibrary.strategy;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import cz.agents.gtlibrary.interfaces.Action;

public class FirstActionStrategyForMissingSequences extends StrategyImpl {

	@Override
	protected Map<Action, Double> getMissingSeqDistribution(Collection<Action> actions) {
		Map<Action, Double> distribution = new HashMap<Action, Double>();
		
		for (Action action : actions) {
			distribution.put(action, 0d);
		}
		distribution.put(actions.iterator().next(), 1d);
		return distribution;
	}
	
	public static class Factory implements Strategy.Factory {

		@Override
		public Strategy create() {
			return new FirstActionStrategyForMissingSequences();
		}
		
	}

}
