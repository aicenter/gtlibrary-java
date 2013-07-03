package cz.agents.gtlibrary.algorithms.sequenceform.refinements.librarycom;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import cz.agents.gtlibrary.iinodes.PlayerImpl;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.Pair;

public class Data {
	
	private final int UTILITY_COEF = 10000000;

	private Map<Object, Integer> E;
	private Map<Object, Integer> F;
	private Map<Object, Double> U;
	private Map<Object, String> M1;
	private Map<Object, String> M2;
	private Map<Object, Integer> rowIndicesE;
	private Map<Object, Integer> columnIndicesE;
	private Map<Object, Integer> rowIndicesF;
	private Map<Object, Integer> columnIndicesF;
//	private Map<Object, Integer> rowIndicesU;
//	private Map<Object, Integer> columnIndicesU;
	private Map<Player, Set<Sequence>> sequences;
	private Map<Player, Set<Pair<Integer, Sequence>>> isKeys;
	private Map<Object, Object> x1;
	private Map<Object, Object> x2;

	public Data() {
		E = new LinkedHashMap<Object, Integer>();
		F = new LinkedHashMap<Object, Integer>();
		U = new LinkedHashMap<Object, Double>();
		rowIndicesE = new LinkedHashMap<Object, Integer>();
		columnIndicesE = new LinkedHashMap<Object, Integer>();
		rowIndicesF = new LinkedHashMap<Object, Integer>();
		columnIndicesF = new LinkedHashMap<Object, Integer>();
//		rowIndicesU = new LinkedHashMap<Object, Integer>();
//		columnIndicesU = new LinkedHashMap<Object, Integer>();
		sequences = new LinkedHashMap<Player, Set<Sequence>>();
		sequences.put(new PlayerImpl(0), new HashSet<Sequence>());
		sequences.put(new PlayerImpl(1), new HashSet<Sequence>());
		isKeys = new LinkedHashMap<Player, Set<Pair<Integer, Sequence>>>();
		isKeys.put(new PlayerImpl(0), new HashSet<Pair<Integer, Sequence>>());
		isKeys.put(new PlayerImpl(1), new HashSet<Pair<Integer, Sequence>>());
		M1 = new LinkedHashMap<Object, String>();
		M2 = new LinkedHashMap<Object, String>();
		x1 = new LinkedHashMap<Object, Object>();
		x2 = new LinkedHashMap<Object, Object>();
	}

	protected int getRowIndexE(Object rowKey) {
		return getIndex(rowKey, rowIndicesE);
	}

	protected int getColumnIndexE(Object columnKey) {
		return getIndex(columnKey, columnIndicesE);
	}

	protected int getRowIndexF(Object rowKey) {
		return getIndex(rowKey, rowIndicesF);
	}

	protected int getColumnIndexF(Object columnKey) {
		return getIndex(columnKey, columnIndicesF);
	}

	protected int getRowIndexU(Object rowKey) {
		return getIndex(rowKey, columnIndicesE);
	}

	protected int getColumnIndexU(Object columnKey) {
		return getIndex(columnKey, columnIndicesF);
	}

	protected int getIndex(Object key, Map<Object, Integer> map) {
		Integer result = map.get(key);

		if (result == null) {
			result = map.size();
			map.put(key, result);
		}
		return result;
	}

	public Pair<Integer, Integer> getKeyE(Object rowKey, Object columnKey) {
		return new Pair<Integer, Integer>(getRowIndexE(rowKey), getColumnIndexE(columnKey));
	}

	public Pair<Integer, Integer> getKeyF(Object rowKey, Object columnKey) {
		return new Pair<Integer, Integer>(getRowIndexF(rowKey), getColumnIndexF(columnKey));
	}

	public Pair<Integer, Integer> getKeyU(Object rowKey, Object columnKey) {
		return new Pair<Integer, Integer>(getRowIndexU(rowKey), getColumnIndexU(columnKey));
	}

	public void setE(Object rowKey, Object columnKey, int value) {
		E.put(getKeyE(rowKey, columnKey), value);
	}

	public void setF(Object rowKey, Object columnKey, int value) {
		F.put(getKeyF(rowKey, columnKey), value);
	}

	public void addToU(Object rowKey, Object columnKey, double value) {
		Object key = getKeyU(rowKey, columnKey);
		Double oldValue = U.get(key);

		U.put(key, (oldValue == null ? value : oldValue + value));
	}

	public void addISKeyFor(Player player, Pair<Integer, Sequence> isKey) {
		isKeys.get(player).add(isKey);
	}

	public void addSequence(Sequence sequence) {
		sequences.get(sequence.getPlayer()).add(sequence);
	}

	public void addToX1(Object isKey, Object key) {
		x1.put(isKey, key);
	}

	public void addToX2(Object isKey, Object key) {
		x2.put(isKey, key);
	}
	
	public void addP1PerturbationsFor(Sequence sequence) {
		int index = getColumnIndexE(sequence);
		int subseqIndex = getColumnIndexE(getSubSequenceKey(sequence));
		
		M1.put(new Pair<Integer, Integer>(index, index), "1");
		M1.put(new Pair<Integer, Integer>(subseqIndex, subseqIndex), "1");
		M1.put(new Pair<Integer, Integer>(index, subseqIndex), "0,-1");
	}
	
	public void addP2PerturbationsFor(Sequence sequence) {
		int index = getColumnIndexF(sequence);
		int subseqIndex = getColumnIndexF(getSubSequenceKey(sequence));
		
		M2.put(new Pair<Integer, Integer>(index, index), "1");
		M2.put(new Pair<Integer, Integer>(subseqIndex, subseqIndex), "1");
		M2.put(new Pair<Integer, Integer>(index, subseqIndex), "0,-1");
	}
	
	public Object getSubSequenceKey(Sequence sequence) {
		return sequence.getSubSequence(sequence.size() - 1);
	}

	public void export(String fileName) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(fileName)));

		printSizes(writer);
		writer.write("F1: " + E.size());
		printEntrySet(writer, E);
		writer.write("F2: " + F.size());
		printEntrySet(writer, F);
		writer.write("U1: " + U.size());
		printUtilityEntrySet(writer, U);
		writer.write("U2: " + U.size());
		printUtilityNegatedEntrySet(writer, U);
		writer.write("M1: " + M1.size());
		printEntrySet(writer, M1);
		writer.write("M2: " + M2.size());
		printEntrySet(writer, M2);
		writer.write("x1: " + x1.size());
		printX1(writer);
		writer.write("x2: " + x2.size());
		printX2(writer);
		writer.flush();
		writer.close();
	}

	public void printX1(BufferedWriter writer) throws IOException {
		writer.newLine();
		writer.newLine();
		for (Object object : x1.values()) {
			writer.write("(");
			writer.write(Integer.toString(getColumnIndexE(object)));
			writer.write(",(");
			writer.write("1))");
			writer.newLine();
		}
		writer.newLine();
	}

	public void printX2(BufferedWriter writer) throws IOException {
		writer.newLine();
		writer.newLine();
		for (Object object : x2.values()) {
			writer.write("(");
			writer.write(Integer.toString(getColumnIndexF(object)));
			writer.write(",(");
			writer.write("1))");
			writer.newLine();
		}
		writer.newLine();
	}

	public void printSizes(BufferedWriter writer) throws IOException {
		writer.write("S1: ");
		writer.write(Integer.toString(sequences.get(new PlayerImpl(0)).size()));
		writer.write(" S2: ");
		writer.write(Integer.toString(sequences.get(new PlayerImpl(1)).size()));
		writer.write(" H1: ");
		writer.write(Integer.toString(isKeys.get(new PlayerImpl(0)).size() + 1));
		writer.write(" H2: ");
		writer.write(Integer.toString(isKeys.get(new PlayerImpl(1)).size() + 1));
		writer.newLine();
		writer.newLine();
	}

	@SuppressWarnings("unchecked")
	public void printEntrySet(BufferedWriter writer, Map<Object, ? extends Object> map) throws IOException {
		writer.newLine();
		writer.newLine();
		for (Entry<Object, ? extends Object> entry : map.entrySet()) {
			writer.write("(");
			writer.write(Integer.toString(((Pair<Integer, Integer>) entry.getKey()).getLeft()));
			writer.write(",");
			writer.write(Integer.toString(((Pair<Integer, Integer>) entry.getKey()).getRight()));
			writer.write(",(");
			writer.write(entry.getValue().toString());
			writer.write("))");
			writer.newLine();
		}
		writer.newLine();
	}
	
	@SuppressWarnings("unchecked")
	public void printUtilityEntrySet(BufferedWriter writer, Map<Object, Double> map) throws IOException {
		writer.newLine();
		writer.newLine();
		for (Entry<Object, Double> entry : map.entrySet()) {
			writer.write("(");
			writer.write(Integer.toString(((Pair<Integer, Integer>) entry.getKey()).getLeft()));
			writer.write(",");
			writer.write(Integer.toString(((Pair<Integer, Integer>) entry.getKey()).getRight()));
			writer.write(",(");
			writer.write(new Integer((new Double(entry.getValue()*UTILITY_COEF).intValue())).toString());
			writer.write("))");
			writer.newLine();
		}
		writer.newLine();
	}
	
	@SuppressWarnings("unchecked")
	public void printUtilityNegatedEntrySet(BufferedWriter writer, Map<Object, ? extends Number> map) throws IOException {
		writer.newLine();
		writer.newLine();
		for (Entry<Object, ? extends Number> entry : map.entrySet()) {
			writer.write("(");
			writer.write(Integer.toString(((Pair<Integer, Integer>) entry.getKey()).getLeft()));
			writer.write(",");
			writer.write(Integer.toString(((Pair<Integer, Integer>) entry.getKey()).getRight()));
			writer.write(",(");
			writer.write(new Integer(new Double(-entry.getValue().doubleValue()*UTILITY_COEF).intValue()).toString());
			writer.write("))");
			writer.newLine();
		}
		writer.newLine();
	}

	@SuppressWarnings("unchecked")
	public void printNegatedEntrySet(BufferedWriter writer, Map<Object, ? extends Number> map) throws IOException {
		writer.newLine();
		writer.newLine();
		for (Entry<Object, ? extends Number> entry : map.entrySet()) {
			writer.write("(");
			writer.write(Integer.toString(((Pair<Integer, Integer>) entry.getKey()).getLeft()));
			writer.write(",");
			writer.write(Integer.toString(((Pair<Integer, Integer>) entry.getKey()).getRight()));
			writer.write(",(");
			writer.write(new Double(-entry.getValue().doubleValue()).toString());
			writer.write("))");
			writer.newLine();
		}
		writer.newLine();
	}

	public Map<Object, Integer> getColumnIndicesE() {
		return columnIndicesE;
	}

	public Map<Object, Integer> getColumnIndicesF() {
		return columnIndicesF;
	}

}