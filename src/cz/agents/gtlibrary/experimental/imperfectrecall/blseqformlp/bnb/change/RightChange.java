package cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.change;

import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.BilinearTable;
import cz.agents.gtlibrary.interfaces.Action;
import ilog.concert.IloException;

public class RightChange extends Change {

    public RightChange(Action action, int[] fixedDigitArray, double fixedDigitNumber) {
        super(action, fixedDigitArray, fixedDigitNumber);
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
        try {
            return table.updateWBoundsForRight(this);
        } catch (IloException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public String toString() {
        return "[" + "R, " + super.toString() + "]";
    }
}
