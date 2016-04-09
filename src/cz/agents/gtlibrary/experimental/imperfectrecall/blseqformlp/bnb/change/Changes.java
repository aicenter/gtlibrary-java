package cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.change;

import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.BilinearTable;

import java.util.LinkedHashSet;

public class Changes extends LinkedHashSet<Change> {

    public Changes() {
        super();
    }

    public Changes(Changes changes) {
        super(changes);
    }

    public void updateTable(BilinearTable table) {
        forEach(c -> c.updateW(table));
    }

    public void removeChanges(BilinearTable table) {
        forEach(c -> c.removeWUpdate(table));
    }
}
