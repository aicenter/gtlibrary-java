package cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.change;

import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.BilinearTable;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.SequenceFormIRInformationSet;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.change.bound.Bound;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.change.bound.LowerBound;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.change.bound.UpperBound;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.change.number.DigitArray;
import cz.agents.gtlibrary.interfaces.Action;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Changes extends ArrayList<Change> {

    private Bound lbs;
    private Bound ubs;
    private Map<Action, Integer> precision;
    private Boolean valid;

    public Changes() {
        super();
        lbs = new LowerBound();
        ubs = new UpperBound();
        precision = new HashMap<>();
        valid = null;
    }

    public Changes(Changes changes) {
        super(changes);
        lbs = deepCopy(changes.lbs, new LowerBound());
        ubs = deepCopy(changes.ubs, new UpperBound());
        precision = new HashMap<>(changes.precision);
        valid = changes.valid;
    }

    private Bound deepCopy(Bound source, Bound destination) {
        source.entrySet().stream().flatMap(e -> e.getValue().entrySet().stream()).forEach(e -> destination.put(e.getKey(), e.getValue()));
        return destination;
    }

    @Override
    public boolean add(Change change) {
        boolean returnVal = super.add(change);

        updateBounds(change);
        return returnVal;
    }

    @Override
    public boolean addAll(Collection<? extends Change> c) {
        c.forEach(change -> super.add(change));
        return true;
    }

    private void updateBounds(Change change) {
        valid = null;
        if (change instanceof LeftChange) {
            updateBoundsForLeft(change);
        } else if (change instanceof RightChange) {
            updateBoundsForRight(change);
        } else {
            updateBoundsForMiddle(change);
            updatePrecision(change);
        }
        while (inferBounds()) ;
    }

    private void updatePrecision(Change change) {
        int oldPrecision = precision.getOrDefault(change.getAction(), 1);

        if (change.getFixedDigitArrayValue().size() > oldPrecision)
            precision.put(change.getAction(), change.getFixedDigitArrayValue().size());
    }

    private void updateBoundsForMiddle(Change change) {
        DigitArray newUB = new DigitArray(change.getFixedDigitArrayValue());
        DigitArray oldUB = ubs.get(change.getAction());
        DigitArray oldLB = lbs.get(change.getAction());

        for (int i = newUB.size() - 1; i >= 0; i--) {
            if (newUB.get(i) == 9) {
                newUB.set(i, 0);
            } else {
                newUB.set(i, newUB.get(i) + 1);
                break;
            }
        }
        if (oldUB == null || oldUB.isGreaterThan(newUB))
            ubs.put(change.getAction(), new DigitArray(newUB));
        if (oldLB == null || change.getFixedDigitArrayValue().isGreaterThan(oldLB))
            lbs.put(change.getAction(), change.getFixedDigitArrayValue());
    }

    private void updateBoundsForRight(Change change) {
        DigitArray oldLB = lbs.get(change.getAction());

        if (oldLB == null || change.getFixedDigitArrayValue().isGreaterThan(oldLB))
            lbs.put(change.getAction(), change.getFixedDigitArrayValue());
    }

    private void updateBoundsForLeft(Change change) {
        DigitArray oldUB = ubs.get(change.getAction());

        if (oldUB == null || oldUB.isGreaterThan(change.getFixedDigitArrayValue()))
            ubs.put(change.getAction(), change.getFixedDigitArrayValue());
    }

    private boolean inferBounds() {
        boolean updated = updateUpperBounds();

        updated |= updateLowerBounds();
        return updated;
    }

    private boolean updateLowerBounds() {
        boolean updated = false;

        for (Map.Entry<SequenceFormIRInformationSet, Map<Action, DigitArray>> entry : ubs.entrySet()) {
            DigitArray ubSum = DigitArray.ZERO;

            for (Action action : entry.getKey().getActions()) {
                ubSum = ubSum.add(ubs.getOrDefault(action, DigitArray.ONE));
            }
            for (Action action : entry.getKey().getActions()) {
                DigitArray ubSumWithoutCurrent = ubSum.subtract(ubs.getOrDefault(action, DigitArray.ONE));
                DigitArray newLB = DigitArray.ONE.subtract(ubSumWithoutCurrent);

//                moveToProbabilityInterval(lbSumWithoutCurrent);
                if (newLB.isGreaterThan(lbs.getOrDefault(action, DigitArray.ZERO))) {
                    lbs.put(action, newLB);
                    updated = true;
                }
            }
        }
        return updated;
    }

    private boolean updateUpperBounds() {
        boolean updated = false;

        for (Map.Entry<SequenceFormIRInformationSet, Map<Action, DigitArray>> entry : lbs.entrySet()) {
            DigitArray lbSum = DigitArray.ZERO;

            for (Action action : entry.getKey().getActions()) {
                lbSum = lbSum.add(lbs.getOrDefault(action, DigitArray.ZERO));
            }
            for (Action action : entry.getKey().getActions()) {
                DigitArray currentLB = lbs.getOrDefault(action, DigitArray.ZERO);
                DigitArray lbSumWithoutCurrent = lbSum.subtract(currentLB);
                DigitArray newUB = DigitArray.ONE.subtract(lbSumWithoutCurrent);
//                moveToProbabilityInterval(lbSumWithoutCurrent);
                if (ubs.getOrDefault(action, DigitArray.ONE).isGreaterThan(newUB)) {
                    ubs.put(action, newUB);
                    updated = true;
                }
            }
        }
        return updated;
    }

    private void moveToProbabilityInterval(DigitArray digitArray) {
        if (!digitArray.isGreaterThan(DigitArray.ZERO)) {
            for (int i = 0; i < digitArray.size(); i++) {
                digitArray.set(i, 0);
            }
        } else if (digitArray.isGreaterThan(DigitArray.ONE)) {
            digitArray.set(0, 1);
            for (int i = 1; i < digitArray.size(); i++) {
                digitArray.set(i, 0);
            }
        }
        assert positive(digitArray);
    }

    private boolean positive(DigitArray currentLB) {
        return currentLB.stream().allMatch(i -> i >= 0);
    }

    public void updateTable(BilinearTable table) {
        forEach(c -> c.updateW(table));
    }

    public void removeChanges(BilinearTable table) {
        forEach(c -> c.removeWUpdate(table));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Changes)) return false;
        Changes changes = (Changes) o;

        if (!precision.equals(changes.precision)) return false;
        if (!lbs.equals(changes.lbs)) return false;
        return ubs.equals(changes.ubs);
    }

    @Override
    public int hashCode() {
        int result = 1;

        result = 31 * result + (precision != null ? precision.hashCode() : 0);
        result = 31 * result + (lbs != null ? lbs.hashCode() : 0);
        result = 31 * result + (ubs != null ? ubs.hashCode() : 0);
        return result;
    }

    public DigitArray getUbFor(Action action) {
        return ubs.getOrDefault(action, DigitArray.ONE);
    }

    public DigitArray getLbFor(Action action) {
        return lbs.getOrDefault(action, DigitArray.ZERO);
    }

    public boolean isValid() {
        if (valid == null)
            valid = checkValidity();
        return valid;
    }

    private boolean checkValidity() {
        Set<Action> relevantActions = Stream.concat(ubs.entrySet().stream().flatMap(e -> e.getValue().keySet().stream()),
                ubs.entrySet().stream().flatMap(e -> e.getValue().keySet().stream())).collect(Collectors.toSet());
        for (Action relevantAction : relevantActions) {
            DigitArray ub = ubs.getOrDefault(relevantAction, DigitArray.ONE);
            DigitArray lb = lbs.getOrDefault(relevantAction, DigitArray.ZERO);

            if (DigitArray.ZERO.isGreaterThan(ub) || lb.isGreaterThan(DigitArray.ONE))
                return false;
            if (lb.isGreaterThan(ub) || lb.equals(ub))
                return false;
        }
        return true;
    }
}
