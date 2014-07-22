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

    public void clear() {
        strategies.clear();
    }
}
