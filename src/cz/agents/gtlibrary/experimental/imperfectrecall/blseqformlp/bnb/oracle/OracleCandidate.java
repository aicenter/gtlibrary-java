package cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle;

import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.Candidate;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.change.Changes;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Sequence;

import java.util.Map;

public class OracleCandidate extends Candidate {
    private Map<Action, Double> minPlayerBestResponse;
    private Map<Sequence, Double> maxPlayerRealPlan;

    public OracleCandidate(double lb, double ub, Changes changes, Action action, int[] actionProbability, Map<Sequence, Double> maxPlayerRealPlan, Map<Action, Double> minPlayerBestResponse) {
        super(lb, ub, changes, action, actionProbability);
        this.minPlayerBestResponse = minPlayerBestResponse;
        this.maxPlayerRealPlan = maxPlayerRealPlan;
    }

    public OracleCandidate(double lb, double ub, Action action, int[] actionProbability, Map<Sequence, Double> maxPlayerRealPlan, Map<Action, Double> minPlayerBestResponse) {
        super(lb, ub, action, actionProbability);
        this.minPlayerBestResponse = minPlayerBestResponse;
        this.maxPlayerRealPlan = maxPlayerRealPlan;
    }

    public Map<Action, Double> getMinPlayerBestResponse() {
        return minPlayerBestResponse;
    }

    public Map<Sequence, Double> getMaxPlayerRealPlan() {
        return maxPlayerRealPlan;
    }
}
