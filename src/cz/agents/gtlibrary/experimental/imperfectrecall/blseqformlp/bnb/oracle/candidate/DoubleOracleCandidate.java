package cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.candidate;

import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.change.Changes;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.Pair;

import java.util.Map;
import java.util.Set;

public class DoubleOracleCandidate extends OracleCandidate {
    private Set<Action> possibleBestResponses;
    private double lpUB;

    public DoubleOracleCandidate(double lb, double ub, Changes changes, Action action, int[] actionProbability, double precisionError, Map<Sequence, Double> maxPlayerRealPlan, Map<Action, Double> maxPlayerStrategy, Map<Action, Double> minPlayerBestResponse, int expansionCount, Set<Action> possibleBestResponses, double lpUB) {
        super(lb, ub, changes, action, actionProbability, precisionError, maxPlayerRealPlan, maxPlayerStrategy, minPlayerBestResponse, expansionCount);
        this.possibleBestResponses = possibleBestResponses;
        this.lpUB = lpUB;
    }

    public DoubleOracleCandidate(double lb, double ub, Action action, int[] actionProbability, double precisionError, Map<Sequence, Double> maxPlayerRealPlan, Map<Action, Double> maxPlayerStrategy, Map<Action, Double> minPlayerBestResponse, int expansionCount, Set<Action> possibleBestResponses, double lpUB) {
        super(lb, ub, action, actionProbability, precisionError, maxPlayerRealPlan, maxPlayerStrategy, minPlayerBestResponse, expansionCount);
        this.possibleBestResponses = possibleBestResponses;
        this.lpUB = lpUB;
    }

    public Set<Action> getPossibleBestResponses() {
        return possibleBestResponses;
    }

}
