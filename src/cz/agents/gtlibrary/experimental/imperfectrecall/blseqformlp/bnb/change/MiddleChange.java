package cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.change;

import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.BilinearTable;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.change.number.DigitArray;
import cz.agents.gtlibrary.interfaces.Action;

public class MiddleChange extends Change {

    public MiddleChange(Action action, int[] fixedDigitArray) {
        super(action, fixedDigitArray);
    }

    public MiddleChange(Action action, DigitArray fixedDigitArray) {
        super(action, fixedDigitArray);
    }

    @Override
    public boolean isChangeCompliant(int[] digitArray) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isChangeCompliant(double number) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MiddleChange))
            return false;
        MiddleChange other = (MiddleChange) o;

        if(fixedDigitArrayValue.size() != other.fixedDigitArrayValue.size())
            return false;
        return super.equals(o);
    }

    @Override
    public boolean updateW(BilinearTable table) {
        table.refinePrecisionOfRelevantBilinearVars(action);
        return table.updateWBoundsForMiddle(this);
    }

    @Override
    public void removeWUpdate(BilinearTable table) {
        table.resetPrecision(action);
        super.removeWUpdate(table);
    }

    @Override
    public String toString() {
        return "[" + "M, " + super.toString() + "]";
    }
}
