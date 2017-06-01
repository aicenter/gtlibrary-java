package cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.change;

import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.BilinearTable;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.change.number.DigitArray;
import cz.agents.gtlibrary.interfaces.Action;

public class LeftChange extends Change {

    public LeftChange(Action action, int[] fixedDigitArrayValue) {
        super(action, fixedDigitArrayValue);
    }

    public LeftChange(Action action, DigitArray fixedDigitArrayValue) {
        super(action, fixedDigitArrayValue);
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
        if (!(o instanceof LeftChange))
            return false;
        return super.equals(o);
    }

    @Override
    public boolean updateW(BilinearTable table) {
        return table.updateWBoundsForLeft(this);
    }

    @Override
    public String toString() {
        return "[" + "L, " + super.toString() + "]";
    }
}
