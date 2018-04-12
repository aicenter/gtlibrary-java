package cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.candidate;

import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.Candidate;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.change.Changes;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Sequence;

import java.util.Map;

public class OracleCandidate extends Candidate {
    private Map<Action, Double> minPlayerBestResponse;
    private Map<Sequence, Double> maxPlayerRealPlan;
    private Map<Action, Double> maxPlayerStrategy;
    private double precisionError;
    private int expansionCount;

    public OracleCandidate(double lb, double ub, Changes changes, Action action, int[] actionProbability, double precisionError, Map<Sequence, Double> maxPlayerRealPlan, Map<Action, Double> maxPlayerStrategy, Map<Action, Double> minPlayerBestResponse, int expansionCount) {
        super(lb, ub, changes, action, actionProbability);
        this.minPlayerBestResponse = minPlayerBestResponse;
        this.maxPlayerRealPlan = maxPlayerRealPlan;
        this.maxPlayerStrategy = maxPlayerStrategy;
        this.precisionError = precisionError;
        this.expansionCount = expansionCount;
    }

    public OracleCandidate(double lb, double ub, Action action, int[] actionProbability, double precisionError, Map<Sequence, Double> maxPlayerRealPlan, Map<Action, Double> maxPlayerStrategy, Map<Action, Double> minPlayerBestResponse, int expansionCount) {
        super(lb, ub, action, actionProbability);
        this.minPlayerBestResponse = minPlayerBestResponse;
        this.maxPlayerRealPlan = maxPlayerRealPlan;
        this.maxPlayerStrategy = maxPlayerStrategy;
        this.precisionError = precisionError;
        this.expansionCount = expansionCount;
    }

    public Map<Action, Double> getMinPlayerBestResponse() {
        return minPlayerBestResponse;
    }

    public double getPrecisionError() {
        return precisionError;
    }

    public Map<Sequence, Double> getMaxPlayerRealPlan() {
        return maxPlayerRealPlan;
    }

    public Map<Action, Double> getMaxPlayerStrategy() {
        return maxPlayerStrategy;
    }

    public int getExpansionCount() {
        return expansionCount;
    }

}
