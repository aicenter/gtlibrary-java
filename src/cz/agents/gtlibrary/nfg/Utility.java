package cz.agents.gtlibrary.nfg;

import java.util.Map;

/**
 * Abstract class representing Utility formulation given two strategies.
 * Author: Ondrej Vanek
 * Date: 11/16/11
 * Time: 1:13 PM
 */
public abstract class Utility<T extends PureStrategy, U extends PureStrategy> {

    public abstract double getUtility(T s1, U s2);

    public double getUtility(T strategy, MixedStrategy<U> mixed) {
        double util = 0;
        for (Map.Entry<U, Double> entry : mixed) {
            util += getUtility(strategy, entry.getKey()) * entry.getValue();
        }
        return util;
    }

    public double getUtility(MixedStrategy<T> mixed, U strategy) {
        double util = 0;
        for (Map.Entry<T, Double> entry : mixed) {
            util += getUtility(entry.getKey(), strategy) * entry.getValue();
        }
        return util;

    }

    public double getUtility(MixedStrategy<T> mixed1, MixedStrategy<U> mixed2) {
        double util = 0;
        for (Map.Entry<T, Double> entry1 : mixed1) {
            for (Map.Entry<U, Double> entry2 : mixed2) {
                util += getUtility(entry1.getKey(), entry2.getKey()) * entry1.getValue() * entry2.getValue();
            }
        }
        return util;
    }
}
