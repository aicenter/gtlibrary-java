package cz.agents.gtlibrary.nfg;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;

public class PlayerStrategySet<T extends PureStrategy> implements Iterable<T> {

    private LinkedHashSet<T> strategies;
    
    public PlayerStrategySet() {
    	strategies = new LinkedHashSet<T>();
	}
    
    public PlayerStrategySet(Iterable<T> strategies) {
    	this.strategies = new LinkedHashSet<T>();
		addAll(strategies);
	}

    public boolean add(T strategy) {
        return strategies.add(strategy);
    }

    public boolean containsStrategy(T strategy) {
        return strategies.contains(strategy);
    }

    /**
     * @return the strategies
     */
    public Collection<T> getStrategies() {
        return strategies;
    }


    @Override
    public String toString() {
        return strategies.toString();
    }

    public boolean addAll(Iterable<T> strategies) {
        boolean added = false;
        for (T strategy : strategies) {
            added |= add(strategy);
        }
        return added;
    }

    @Override
    public Iterator<T> iterator() {
        return strategies.iterator();
    }

    public int size() {
        return strategies.size();
    }
}
