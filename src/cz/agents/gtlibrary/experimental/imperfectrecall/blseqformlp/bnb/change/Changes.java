package cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.change;

import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.BilinearTable;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class Changes extends LinkedHashSet<Change> {

    public Changes() {
        super();
    }

    public Changes(Changes changes) {
        super(changes);
    }

    public void updateTable(BilinearTable table) {
        this.stream().forEach(c -> c.updateW(table));
    }

    public void removeChanges(BilinearTable table) {
        this.stream().forEach(c -> c.removeWUpdate(table));
    }
}
