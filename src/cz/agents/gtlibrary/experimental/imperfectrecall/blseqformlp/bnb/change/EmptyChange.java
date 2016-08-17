package cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.change;

import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.BilinearTable;
import cz.agents.gtlibrary.interfaces.Action;

public class EmptyChange extends Change {
    public EmptyChange() {
        super(null, null);
    }

    @Override
    public boolean isChangeCompliant(int[] digitArray) {
        return true;
    }

    @Override
    public boolean isChangeCompliant(double number) {
        return true;
    }

    @Override
    public boolean updateW(BilinearTable table) {
        return true;
    }
}
