package cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.change;

import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.BilinearTable;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.change.number.DigitArray;
import cz.agents.gtlibrary.interfaces.Action;
import ilog.concert.IloException;

import java.util.Arrays;

public abstract class Change {
    public static Change EMPTY = new EmptyChange();
    protected Action action;
    protected DigitArray fixedDigitArrayValue;
    protected double fixedDigitNumericValue;

    public Change(Action action, int[] fixedDigitArrayValue) {
        this.action = action;
        this.fixedDigitArrayValue = new DigitArray(fixedDigitArrayValue, true);
    }

    public Change(Action action, DigitArray fixedDigitArrayValue) {
        this.action = action;
        this.fixedDigitArrayValue = fixedDigitArrayValue;
    }

    public Action getAction() {
        return action;
    }

    public DigitArray getFixedDigitArrayValue() {
        return fixedDigitArrayValue;
    }

    public double getFixedDigitNumericValue() {
        return fixedDigitNumericValue;
    }

    public int getFixedDigitCount() {
        return fixedDigitArrayValue.size() - 1;
    }

    public abstract boolean isChangeCompliant(int[] digitArray);

    public abstract boolean isChangeCompliant(double number);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Change)) return false;

        Change change = (Change) o;

        if (Double.compare(change.fixedDigitNumericValue, fixedDigitNumericValue) != 0) return false;
        if (!action.equals(change.action)) return false;
        return fixedDigitArrayValue.equals(change.fixedDigitArrayValue);
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = action.hashCode();
        result = 31 * result + fixedDigitArrayValue.hashCode();
        temp = Double.doubleToLongBits(fixedDigitNumericValue);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    public abstract boolean updateW(BilinearTable table);

    public void removeWUpdate(BilinearTable table) {
        try {
            table.removeWUpdate(this);
        } catch (IloException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return action.toString() + ", " + fixedDigitArrayValue.toString();
    }

}
