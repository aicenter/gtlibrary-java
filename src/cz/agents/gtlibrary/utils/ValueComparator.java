package cz.agents.gtlibrary.utils;

import java.util.Comparator;
import java.util.Map;

public class ValueComparator<E extends Object> implements Comparator<E> {

    Map<E, Double> base;
    public ValueComparator(Map<E, Double> base) {
        this.base = base;
    }

    // Note: this comparator imposes orderings that are inconsistent with equals.    
    public int compare(E a, E b) {
        if (base.get(a) >= base.get(b)) {
            return -1;
        } else {
            return 1;
        } // returning 0 would merge keys
    }
}