package cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb;

import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.change.Change;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.change.Changes;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.change.MiddleChange;
import cz.agents.gtlibrary.interfaces.Action;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Candidate implements Comparable<Candidate> {

    protected Changes changes;
    protected Action action;
    protected int[] actionProbability;
    protected int fixedDigitsForCurrentAction;

    protected double lb = Double.NEGATIVE_INFINITY;
    protected double ub = Double.POSITIVE_INFINITY;

    public Candidate(double lb, double ub, Changes changes,
                     Action action, int[] actionProbability) {
        this.lb = lb;
        this.ub = ub;
        this.changes = changes;
        this.action = action;
        this.actionProbability = actionProbability;
    }

    public Candidate(double lb, double ub, Action action, int[] actionProbability) {
        this.lb = lb;
        this.ub = ub;
        this.changes = new Changes();
        this.action = action;
        this.actionProbability = actionProbability;
    }

    public double getUb() {
        return ub;
    }

    public void setUb(double ub) {
        this.ub = ub;
    }

    public double getLb() {
        return lb;
    }

    public Action getAction() {
        return action;
    }

    public int[] getActionProbability() {
        return actionProbability;
    }

    public Changes getChanges() {
        return changes;
    }

    public int getFixedDigitsForCurrentAction() {
        if (fixedDigitsForCurrentAction != 0)
            return fixedDigitsForCurrentAction;
        changes.stream()
                .filter(change -> change instanceof MiddleChange)
                .filter(change -> change.getAction().equals(action))
                .forEach(change -> fixedDigitsForCurrentAction = Math.max(fixedDigitsForCurrentAction,
                change.getFixedDigitCount()));
        return fixedDigitsForCurrentAction;
    }

    @Override
    public int compareTo(Candidate o) {
        if (this.ub > o.ub)
            return -1;
        if (this.ub < o.ub)
            return 1;
        if (this.lb > o.lb)
            return -1;
        if (this.lb < o.lb)
            return 1;
        if (this.ub - this.lb > o.ub - o.lb)
            return -1;
        if (this.ub - this.lb < o.ub - o.lb)
            return 1;
        if (this.changes.size() > o.changes.size())
            return -1;
        if (this.changes.size() < o.changes.size())
            return 1;
        return Integer.compare(this.changes.hashCode(), o.changes.hashCode());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Candidate)) return false;

        Candidate candidate = (Candidate) o;

        if (!Arrays.equals(candidate.actionProbability, actionProbability)) return false;
        if (!changes.equals(candidate.changes)) return false;
        return !(action != null ? !action.equals(candidate.action) : candidate.action != null);

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = changes.hashCode();
        result = 31 * result + (action != null ? action.hashCode() : 0);
        temp = Arrays.hashCode(actionProbability);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "Candidate{" +
                "lb=" + lb +
                ", ub=" + ub +
                '}';
    }
}
