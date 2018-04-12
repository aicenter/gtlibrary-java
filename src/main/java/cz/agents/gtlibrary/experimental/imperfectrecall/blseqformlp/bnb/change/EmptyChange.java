package cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.change;

import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.BilinearTable;

public class EmptyChange extends Change {

    public EmptyChange() {
        super(null, new int[0]);
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
