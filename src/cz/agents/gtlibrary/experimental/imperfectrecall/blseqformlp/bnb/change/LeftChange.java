package cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.change;

import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.BilinearTable;
import cz.agents.gtlibrary.interfaces.Action;
import ilog.concert.IloException;

public class LeftChange extends Change {

    public LeftChange(Action action, int[] fixedDigitArrayValue, double fixedDigitNumber) {
        super(action, fixedDigitArrayValue, fixedDigitNumber);
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
        try {
            return table.updateWBoundsForLeft(this);
        } catch (IloException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public String toString() {
        return "[" + "L, " + super.toString() + "]";
    }
}
