package cz.agents.gtlibrary.nfg;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Representation of a mixed strategy.
 * Author: Ondrej Vanek
 * Date: 11/16/11
 * Time: 10:37 AM
 */
public class MixedStrategy<T extends PureStrategy> implements Iterable<Map.Entry<T, Double>> {

    private Map<T, Double> mixedStrategy = new HashMap<T, Double>();

    public void put(T strategy, double probability) {
        mixedStrategy.put(strategy, probability);
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
}
