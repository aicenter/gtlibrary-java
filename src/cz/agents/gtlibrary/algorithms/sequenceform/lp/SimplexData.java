package cz.agents.gtlibrary.algorithms.sequenceform.lp;

import cz.agents.gtlibrary.algorithms.sequenceform.numbers.EpsilonReal;

import java.util.Map;

public class SimplexData {
    private LPDictionary<EpsilonReal> simplex;
    private Map<Object, Integer> watchedPrimalVars;
    private Map<Object, Integer> watchedDualVars;

    public SimplexData(LPDictionary<EpsilonReal> simplex, Map<Object, Integer> watchedPrimalVars, Map<Object, Integer> watchedDualVars) {
        this.simplex = simplex;
        this.watchedPrimalVars = watchedPrimalVars;
        this.watchedDualVars = watchedDualVars;
    }

    public LPDictionary<EpsilonReal> getSimplex() {
        return simplex;
    }

    public Map<Object, Integer> getWatchedDualVars() {
        return watchedDualVars;
    }

    public Map<Object, Integer> getWatchedPrimalVars() {
        return watchedPrimalVars;
    }
}
