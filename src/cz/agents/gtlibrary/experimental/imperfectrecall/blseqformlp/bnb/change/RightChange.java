package cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.change;

import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.BilinearTable;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.change.number.DigitArray;
import cz.agents.gtlibrary.interfaces.Action;

public class RightChange extends Change {

    public RightChange(Action action, int[] fixedDigitArray) {
        super(action, fixedDigitArray);
    }

    public RightChange(Action action, DigitArray fixedDigitArray) {
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
        if (!(o instanceof RightChange))
            return false;
        return super.equals(o);
    }

    @Override
    public boolean updateW(BilinearTable table) {
        return table.updateWBoundsForRight(this);
    }

    @Override
    public String toString() {
        return "[" + "R, " + super.toString() + "]";
    }
}
