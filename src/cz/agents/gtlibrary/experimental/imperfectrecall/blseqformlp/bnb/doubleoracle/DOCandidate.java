package cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.doubleoracle;

import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.Candidate;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.change.Changes;
import cz.agents.gtlibrary.interfaces.Action;

import java.util.Map;

public class DOCandidate extends Candidate {
    private Map<Action, Double> minPlayerBestResponse;

    public DOCandidate(double lb, double ub, Changes changes, Action action, int[] actionProbability, Map<Action, Double> minPlayerBestResponse) {
        super(lb, ub, changes, action, actionProbability);
        this.minPlayerBestResponse = minPlayerBestResponse;
    }

    public DOCandidate(double lb, double ub, Action action, int[] actionProbability, Map<Action, Double> minPlayerBestResponse) {
        super(lb, ub, action, actionProbability);
        this.minPlayerBestResponse = minPlayerBestResponse;
    }

    public Map<Action, Double> getMinPlayerBestResponse() {
        return minPlayerBestResponse;
    }
}
