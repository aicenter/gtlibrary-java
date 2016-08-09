package cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.change;

import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.BilinearTable;
import cz.agents.gtlibrary.interfaces.Action;
import ilog.concert.IloException;

import java.util.Arrays;

public abstract class Change {
    protected Action action;
    protected int[] fixedDigitArrayValue;
    protected double fixedDigitNumericValue;

    public Change(Action action, int[] fixedDigitArrayValue) {
        this.action = action;
        this.fixedDigitArrayValue = fixedDigitArrayValue;
    }

    public Action getAction() {
        return action;
    }

    public int[] getFixedDigitArrayValue() {
        return fixedDigitArrayValue;
    }

    public double getFixedDigitNumericValue() {
        return fixedDigitNumericValue;
    }

    public int getFixedDigitCount() {
        return fixedDigitArrayValue.length - 1;
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
        return Arrays.equals(fixedDigitArrayValue, change.fixedDigitArrayValue);
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = action.hashCode();
        result = 31 * result + Arrays.hashCode(fixedDigitArrayValue);
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
        return action.toString() + ", " + Arrays.toString(fixedDigitArrayValue);
    }

}
