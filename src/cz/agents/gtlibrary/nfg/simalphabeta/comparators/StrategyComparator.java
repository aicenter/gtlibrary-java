package cz.agents.gtlibrary.nfg.simalphabeta.comparators;

import java.util.Comparator;

import cz.agents.gtlibrary.nfg.ActionPureStrategy;

public abstract class StrategyComparator implements Comparator<ActionPureStrategy> {
	
	@Override
	public int compare(ActionPureStrategy s1, ActionPureStrategy s2) {
		int result = Double.compare(getValue(s2), getValue(s1));
        if (result == 0)
            result = Integer.compare(s1.hashCode(), s2.hashCode());
        return result;
	}

	protected abstract double getValue(ActionPureStrategy strategy);

}
