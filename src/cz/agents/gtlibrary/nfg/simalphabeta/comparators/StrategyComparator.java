package cz.agents.gtlibrary.nfg.simalphabeta.comparators;

import java.util.Comparator;

import cz.agents.gtlibrary.nfg.ActionPureStrategy;

public abstract class StrategyComparator implements Comparator<ActionPureStrategy> {
	
	@Override
	public int compare(ActionPureStrategy s1, ActionPureStrategy s2) {
		return Double.compare(getValue(s2), getValue(s1));
	}

	protected abstract double getValue(ActionPureStrategy strategy);

}
