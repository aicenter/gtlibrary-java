package cz.agents.gtlibrary.algorithms.sequenceform.refinements;

public class LPTable {
	
	private Number[][] table;
	
	public LPTable(int m, int n) {
		table = new Number[m][n];
	}
	
	public double get(int i, int j) {
		return table[i][j] == null?0:table[i][j].doubleValue();
	}
	
	public void set(int i, int j, double value) {
		table[i][j] = value;
	}
	
	public void add(int i, int j, double value) {
		table[i][j] = get(i, j) + value;
	}
	
	public void substract(int i, int j, double value) {
		table[i][j] = get(i, j) - value;
	}
	
	public int rowCount() {
		return table.length;
	}
	
	public int columnCount() {
		return table[0].length;
	}

}
