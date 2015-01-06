package cz.agents.gtlibrary.algorithms.sequenceform.gensum.parammilp;

import java.util.ArrayList;
import java.util.List;

public class Indices {
    private List<Integer> rows;
    private List<Integer> columns;

    public Indices() {
        rows = new ArrayList<>();
        columns = new ArrayList<>();
    }

    public Indices(List<Integer> rows, List<Integer> columns) {
        this.rows = rows;
        this.columns = columns;
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
            if (rows.get(i) == row) {
                assert columns.get(i) == column;
                rows.remove(i);
                columns.remove(i);
                return;
            }
        }
    }

    public int size() {
        return columns.size();
    }

    public boolean isEmpty() {
        return columns.isEmpty();
    }

    public Indices copy() {
        return new Indices(new ArrayList<>(rows), new ArrayList<>(columns));
    }
}
