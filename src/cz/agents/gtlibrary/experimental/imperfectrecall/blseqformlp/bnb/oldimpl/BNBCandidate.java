package cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oldimpl;

import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.utils.Pair;
import cz.agents.gtlibrary.utils.Triplet;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by bosansky on 2/10/16.
 */
@Deprecated
public class BNBCandidate implements Comparable<BNBCandidate>{
    private double lb = Double.NEGATIVE_INFINITY;
    private double ub = Double.POSITIVE_INFINITY;

    public static enum ChangeType {MIDDLE, LEFT, RIGHT};

    private Set<Pair<ChangeType, Triplet<Integer, Action, Double>>> changes;

    private Action actionToFocusOn;
    private double currentProbOfAction;
    private int numberOfMiddle = 0;

    public BNBCandidate(double lb, double ub, Set<Pair<BNBCandidate.ChangeType, Triplet<Integer, Action, Double>>> changes) {
        this.lb = lb;
        this.ub = ub;
        this.changes = changes;
        for (Pair<BNBCandidate.ChangeType, Triplet<Integer, Action, Double>> c : changes)
            if (c.getLeft().equals(ChangeType.MIDDLE)) numberOfMiddle++;
    }

    public BNBCandidate(double lb, double ub) {
        this.lb = lb;
        this.ub = ub;
        this.changes = new HashSet<>();
    }


    @Override
    public int compareTo(BNBCandidate o) {
////        if (this.numberOfMiddle > o.numberOfMiddle) return -1;
////        else if (this.numberOfMiddle < o.numberOfMiddle) return 1;
////        else {
//            if (this.lb > o.lb) return -1;
//            else if (this.lb < o.lb) return 1;
//            else {
//                if (this.ub > o.ub) return -1;
//                else if (this.ub < o.ub) return 1;
//                else {
//                    if (this.changes.size() < o.changes.size()) return -1;
//                    else if (this.changes.size() > o.changes.size()) return 1;
//                    else return Integer.compare(this.changes.hashCode(), o.changes.hashCode());
//                }
//            }
////        }

//        if (this.ub - this.lb > o.ub - o.lb) return -1;
//        else if (this.ub - this.lb < o.ub - o.lb) return 1;
//        else {
//            if (this.changes.size() < o.changes.size()) return -1;
//            else if (this.changes.size() > o.changes.size()) return 1;
//            else return Integer.compare(this.changes.hashCode(), o.changes.hashCode());
//            }

        if (this.ub > o.ub) return -1;
            else if (this.ub < o.ub) return 1;
            else {
                if (this.lb > o.lb) return -1;
                else if (this.lb < o.lb) return 1;
                else {
                    if (this.ub - this.lb > o.ub - o.lb) return -1;
                    else if (this.ub - this.lb < o.ub - o.lb) return 1;
                    else {
                        if (this.changes.size() > o.changes.size()) return -1;
                        else if (this.changes.size() < o.changes.size()) return 1;
                        else return Integer.compare(this.changes.hashCode(), o.changes.hashCode());
                    }
                }
            }
//        }
    }

    public void setActionToFocusOn(Action actionToFocusOn, double currentProbOfAction) {
        this.actionToFocusOn = actionToFocusOn;
        this.currentProbOfAction = currentProbOfAction;
    }

    public double getLb() {
        return lb;
    }

    public double getUb() {
        return ub;
    }

    public Set<Pair<BNBCandidate.ChangeType, Triplet<Integer, Action, Double>>> getChanges() {
        return changes;
    }

    public Action getActionToFocusOn() {
        return actionToFocusOn;
    }

    public double getCurrentProbOfAction() {
        return currentProbOfAction;
    }

    @Override
    public int hashCode() {
        HashCodeBuilder hb = new HashCodeBuilder(17,31);
        hb.append(lb);
        hb.append(ub);
        hb.append(actionToFocusOn);
        hb.append(currentProbOfAction);
        hb.append(changes);
        return hb.toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof BNBCandidate)) return false;
        return (this.compareTo((BNBCandidate)obj) == 0);
    }

    @Override
    public String toString() {
        return "BNBCandidate{" +
                "ub=" + ub +
                ", lb=" + lb +
                '}';
    }
}
