package cz.agents.gtlibrary.nfg;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

public class PlayerStrategySet<T extends PureStrategy> implements Iterable<T> {

    //private List<T> strategies = new ArrayList<Path>();
    private LinkedHashSet<T> strategies = new LinkedHashSet<T>();

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

    public boolean add(List<T> strategies) {
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
