package cz.agents.gtlibrary.algorithms.sequenceform.gensum.parammilp;

import cz.agents.gtlibrary.interfaces.InformationSet;

import java.util.ArrayList;
import java.util.List;

public class Indices {
    private List<Integer> rows;
    private List<Integer> columns;

    public Indices() {
        rows = new ArrayList<>();
        columns = new ArrayList<>();
    }

    public void addEntry(int row, int column) {
        rows.add(row);
        columns.add(column);
    }

    public List<Integer> getColumns() {
        return columns;
    }

    public List<Integer> getRows() {
        return rows;
    }

    public void removeEntry(int row, int column) {
        for (int i = rows.size() - 1; i >= 0; i--) {
             if(rows.get(i) == row) {
                 assert columns.get(i) == column;
                 rows.remove(i);
                 columns.remove(i);
                 return;
             }
        }
    }
}
