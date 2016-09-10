package cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.cache;

import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.change.Change;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.change.Changes;

import java.util.HashMap;
import java.util.Map;

public class ChangeValueCache {
    private Map<Changes, Double> cache;

    public ChangeValueCache() {
        cache = new HashMap<>();
    }

    public void add(Changes changes, double ub) {
        cache.put(changes, ub);
    }

}
