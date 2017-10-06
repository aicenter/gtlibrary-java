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

import java.util.*;

/**
 * Representation of a mixed strategy.
 * Author: Ondrej Vanek
 * Date: 11/16/11
 * Time: 10:37 AM
 */
public class MixedStrategy<T extends PureStrategy> implements Iterable<Map.Entry<T, Double>> {

    private Map<T, Double> mixedStrategy = new HashMap<T, Double>();
    private boolean reSort = true;
    private List<T> sortedStrategies = new ArrayList<T>();


    public void put(T strategy, double probability) {
        mixedStrategy.put(strategy, probability);
        reSort = true;
    }

    /**
     * @param strategy
     * @return Rather than throw an exception, return 0 is better, this class can then contain only non-zero strategies.
     */
    public double getProbability(T strategy) {
//        if(!mixedStrategy.containsKey(strategy)){
//            throw new IllegalArgumentException("Strategy "+ strategy+" is not in the mixed strategy set.");
//        }
        if (!mixedStrategy.containsKey(strategy)) {
            return 0;
        }
        return mixedStrategy.get(strategy);
    }

    public void clear() {
        mixedStrategy.clear();
    }

    @Override
    public Iterator<Map.Entry<T, Double>> iterator() {
        return mixedStrategy.entrySet().iterator();
    }

    public List<T> sortStrategies() {
        if (reSort) {
            sortedStrategies.clear();
            sortedStrategies.addAll(mixedStrategy.keySet());
            Collections.sort(sortedStrategies, new StrategyProbabilityComparator());
            reSort = false;
        }
        return sortedStrategies;
    }

    public int size() {
        return mixedStrategy.size();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("mixed=[\n");
        for (Map.Entry<T, Double> entry : mixedStrategy.entrySet()) {
            if (entry.getValue() > 0.00001) {
                sb.append(entry.getKey() + ":" + entry.getValue() + "\n");
            }
        }

        sb.append("];");
        return sb.toString();
    }

    public void sanityCheck() {
        double EPS = 0.0000001;
        double sum = 0;
        for (Map.Entry<T, Double> e : this.mixedStrategy.entrySet()) {
            sum += e.getValue();
        }
        if (Math.abs(sum - 1.0) > EPS) {
            throw new RuntimeException("Mixed strategy " + this + " is ill-formed!");
        }
    }

    public int getSupportSize() {
        int result = 0;

        for (Map.Entry<T, Double> entry : mixedStrategy.entrySet()) {
            if (entry.getValue() > 0.00001) {
                result++;
            }
        }

        return result;
    }

    public class StrategyProbabilityComparator implements Comparator<T> {

        @Override
        public int compare(T arg0, T arg1) {
            Double or0 = (mixedStrategy.get(arg0) == null) ? Double.NEGATIVE_INFINITY : mixedStrategy.get(arg0);
            Double or1 = (mixedStrategy.get(arg1) == null) ? Double.NEGATIVE_INFINITY : mixedStrategy.get(arg1);
            if (or0 < or1) {
                return 1;
            }
            if (or0 > or1) {
                return -1;
            }
            return 0;
        }
    }

    public Collection<T> getPureStrategies() {
        return mixedStrategy.keySet();
    }
}
