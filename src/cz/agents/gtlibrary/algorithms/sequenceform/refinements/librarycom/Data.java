/*
Copyright 2014 Faculty of Electrical Engineering at CTU in Prague

This file is part of Game Theoretic Library.

Game Theoretic Library is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Game Theoretic Library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with Game Theoretic Library.  If not, see <http://www.gnu.org/licenses/>.*/


package cz.agents.gtlibrary.algorithms.sequenceform.refinements.librarycom;

import cz.agents.gtlibrary.iinodes.PlayerImpl;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.Pair;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Data {

	private Map<Pair<Integer, Integer>, Integer> E;
	private Map<Pair<Integer, Integer>, Integer> F;
	private Map<Pair<Integer, Integer>, Double> U1;
    private Map<Pair<Integer, Integer>, Double> U2;
	private Map<Pair<Integer, Integer>, String> M1;
	private Map<Pair<Integer, Integer>, String> M2;
	private Map<Object, Integer> rowIndicesE;
	private Map<Object, Integer> columnIndicesE;
	private Map<Object, Integer> rowIndicesF;
	private Map<Object, Integer> columnIndicesF;
	private Map<Player, Set<Sequence>> sequences;
	private Map<Player, Set<Object>> isKeys;
	private Map<Player, Set<Sequence>> initStrategy;

	public Data() {
		E = new LinkedHashMap<>();
		F = new LinkedHashMap<>();
		U1 = new LinkedHashMap<>();
        U2 = new LinkedHashMap<>();
		rowIndicesE = new LinkedHashMap<>();
		columnIndicesE = new LinkedHashMap<>();
		rowIndicesF = new LinkedHashMap<>();
		columnIndicesF = new LinkedHashMap<>();
		sequences = new LinkedHashMap<>();
		sequences.put(new PlayerImpl(0), new HashSet<Sequence>());
		sequences.put(new PlayerImpl(1), new HashSet<Sequence>());
		isKeys = new LinkedHashMap<>();
		isKeys.put(new PlayerImpl(0), new HashSet<>());
		isKeys.put(new PlayerImpl(1), new HashSet<>());
		M1 = new LinkedHashMap<>();
		M2 = new LinkedHashMap<>();
		initStrategy = new HashMap<>();
		initStrategy.put(new PlayerImpl(0), new LinkedHashSet<Sequence>());
		initStrategy.put(new PlayerImpl(1), new LinkedHashSet<Sequence>());
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
		return new Pair<>(getRowIndexE(rowKey), getColumnIndexE(columnKey));
	}

	public Pair<Integer, Integer> getKeyF(Object rowKey, Object columnKey) {
		return new Pair<>(getRowIndexF(rowKey), getColumnIndexF(columnKey));
	}

	public Pair<Integer, Integer> getKeyU(Object rowKey, Object columnKey) {
		return new Pair<>(getRowIndexU(rowKey), getColumnIndexU(columnKey));
	}

	public void setE(Object rowKey, Object columnKey, int value) {
		E.put(getKeyE(rowKey, columnKey), value);
	}

	public void setF(Object rowKey, Object columnKey, int value) {
		F.put(getKeyF(rowKey, columnKey), value);
	}

	public void addToU1(Object rowKey, Object columnKey, double value) {
		Pair<Integer, Integer> key = getKeyU(rowKey, columnKey);
		Double oldValue = U1.get(key);

		U1.put(key, (oldValue == null ? value : oldValue + value));
	}

    public void addToU2(Object rowKey, Object columnKey, double value) {
        Pair<Integer, Integer> key = getKeyU(rowKey, columnKey);
        Double oldValue = U2.get(key);

        U2.put(key, (oldValue == null ? value : oldValue + value));
    }

	public void addISKeyFor(Player player, Object isKey) {
		if (isKey != null)
			isKeys.get(player).add(isKey);
	}

	public void addSequence(Sequence sequence) {
		sequences.get(sequence.getPlayer()).add(sequence);
	}

	public void addSequenceToInitialStrategy(Sequence sequence) {
		initStrategy.get(sequence.getPlayer()).add(sequence);
	}

	public void addP1PerturbationsFor(Sequence sequence) {
		int index = getColumnIndexE(sequence);
		int subseqIndex = getColumnIndexE(getSubSequenceKey(sequence));

		M1.put(new Pair<>(index, index), "1");
		M1.put(new Pair<>(subseqIndex, subseqIndex), "1");
		M1.put(new Pair<>(index, subseqIndex), "0,-1");
	}

	public void addP2PerturbationsFor(Sequence sequence) {
		int index = getColumnIndexF(sequence);
		int subseqIndex = getColumnIndexF(getSubSequenceKey(sequence));

		M2.put(new Pair<>(index, index), "1");
		M2.put(new Pair<>(subseqIndex, subseqIndex), "1");
		M2.put(new Pair<>(index, subseqIndex), "0,-1");
	}

	public Object getSubSequenceKey(Sequence sequence) {
		return sequence.getSubSequence(sequence.size() - 1);
	}

	public void exportLemkeData(String fileName) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(fileName)));

		printSizes(writer);
		writer.write("F1: " + E.size());
		printEntrySetWithBrackets(writer, E);
		writer.write("F2: " + F.size());
		printEntrySetWithBrackets(writer, F);
		writer.write("U1: " + U1.size());
		printUtilityEntrySetWithBrackets(writer, U1);
		writer.write("U2: " + U2.size());
		printUtilityEntrySetWithBrackets(writer, U2);
		writer.write("M1: " + M1.size());
		printEntrySetWithBrackets(writer, M1);
		writer.write("M2: " + M2.size());
		printEntrySetWithBrackets(writer, M2);
		writer.write("x1: " + initStrategy.get(new PlayerImpl(0)).size());
		printX1WithBrackets(writer);
		writer.write("x2: " + initStrategy.get(new PlayerImpl(1)).size());
		printX2WithBrackets(writer);
		writer.flush();
		writer.close();
	}

    public void exportSimplexData(String fileName) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(new File(fileName)));

        printSizes(writer);

        writer.write("U1: " + U1.size());
        printUtilityEntrySet(writer, U1);
        writer.write("U2: " + U2.size());
        printUtilityEntrySet(writer, U2);
        writer.write("F1: " + E.size());
        printEntrySet(writer, E);
        writer.write("F2: " + F.size());
        printEntrySet(writer, F);
//        writer.write("M1: " + M1.size());
//        printEntrySet(writer, M1);
//        writer.write("M2: " + M2.size());
//        printEntrySet(writer, M2);
        writer.write("x1: " + initStrategy.get(new PlayerImpl(0)).size());
        printX1(writer);
        writer.write("x2: " + initStrategy.get(new PlayerImpl(1)).size());
        printX2(writer);
        writer.flush();
        writer.close();
    }

	public void printX1WithBrackets(BufferedWriter writer) throws IOException {
		writer.newLine();
		writer.newLine();
		for (Object object : initStrategy.get(new PlayerImpl(0))) {
			writer.write("(");
			writer.write(Integer.toString(getColumnIndexE(object)));
			writer.write(",(");
			writer.write("1))");
			writer.newLine();
		}
		writer.newLine();
	}

	public void printX2WithBrackets(BufferedWriter writer) throws IOException {
		writer.newLine();
		writer.newLine();
		for (Object object : initStrategy.get(new PlayerImpl(1))) {
			writer.write("(");
			writer.write(Integer.toString(getColumnIndexF(object)));
			writer.write(",(");
			writer.write("1))");
			writer.newLine();
		}
		writer.newLine();
	}

    public void printX1(BufferedWriter writer) throws IOException {
        writer.newLine();
        writer.newLine();
        for (Object object : initStrategy.get(new PlayerImpl(0))) {
            writer.write("(");
            writer.write(Integer.toString(getColumnIndexE(object)));
            writer.write(",");
            writer.write("1)");
            writer.newLine();
        }
        writer.newLine();
    }

    public void printX2(BufferedWriter writer) throws IOException {
        writer.newLine();
        writer.newLine();
        for (Object object : initStrategy.get(new PlayerImpl(1))) {
            writer.write("(");
            writer.write(Integer.toString(getColumnIndexF(object)));
            writer.write(",");
            writer.write("1)");
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

	public void printEntrySetWithBrackets(BufferedWriter writer, Map<Pair<Integer, Integer>, ? extends Object> map) throws IOException {
		writer.newLine();
		writer.newLine();
		List<Map.Entry<Pair<Integer, Integer>, ? extends Object>> entryList = new LinkedList<>();

		entryList.addAll(map.entrySet());
		Collections.sort(entryList, new EntryComparator());
		for (Map.Entry<Pair<Integer, Integer>, ? extends Object> entry : entryList) {
			writer.write("(");
			writer.write(Integer.toString(entry.getKey().getLeft()));
			writer.write(",");
			writer.write(Integer.toString(entry.getKey().getRight()));
			writer.write(",(");
			writer.write(entry.getValue().toString());
			writer.write("))");
			writer.newLine();
		}
		writer.newLine();
	}

    public void printEntrySet(BufferedWriter writer, Map<Pair<Integer, Integer>, ? extends Object> map) throws IOException {
        writer.newLine();
        writer.newLine();
        List<Map.Entry<Pair<Integer, Integer>, ? extends Object>> entryList = new LinkedList<>();

        entryList.addAll(map.entrySet());
        Collections.sort(entryList, new EntryComparator());
        for (Map.Entry<Pair<Integer, Integer>, ? extends Object> entry : entryList) {
            writer.write("(");
            writer.write(Integer.toString(entry.getKey().getLeft()));
            writer.write(",");
            writer.write(Integer.toString(entry.getKey().getRight()));
            writer.write(",");
            writer.write(entry.getValue().toString());
            writer.write(")");
            writer.newLine();
        }
        writer.newLine();
    }

	public void printUtilityEntrySetWithBrackets(BufferedWriter writer, Map<Pair<Integer, Integer>, Double> map) throws IOException {
		writer.newLine();
		writer.newLine();
		List<Map.Entry<Pair<Integer, Integer>, Double>> entryList = new LinkedList<>();

		entryList.addAll(map.entrySet());
		Collections.sort(entryList, new EntryComparator());
		for (Map.Entry<Pair<Integer, Integer>, Double> entry : entryList) {
			assert entry.getValue() == entry.getValue().intValue();
			writer.write("(");
			writer.write(Integer.toString(entry.getKey().getLeft()));
			writer.write(",");
			writer.write(Integer.toString(entry.getKey().getRight()));
			writer.write(",(");
			writer.write(new Integer((new Double(entry.getValue()).intValue())).toString());
			writer.write("))");
			writer.newLine();
		}
		writer.newLine();
	}

    public void printUtilityEntrySet(BufferedWriter writer, Map<Pair<Integer, Integer>, Double> map) throws IOException {
        writer.newLine();
        writer.newLine();
        List<Map.Entry<Pair<Integer, Integer>, Double>> entryList = new LinkedList<>();

        entryList.addAll(map.entrySet());
        Collections.sort(entryList, new EntryComparator());
        for (Map.Entry<Pair<Integer, Integer>, Double> entry : entryList) {
			assert entry.getValue() == entry.getValue().intValue();
            writer.write("(");
            writer.write(Integer.toString(entry.getKey().getLeft()));
            writer.write(",");
            writer.write(Integer.toString(entry.getKey().getRight()));
            writer.write(",");
            writer.write(new Integer((new Double(entry.getValue()).intValue())).toString());
            writer.write(")");
            writer.newLine();
        }
        writer.newLine();
    }

	@SuppressWarnings("unchecked")
	public void printUtilityNegatedEntrySet(BufferedWriter writer, Map<Pair<Integer, Integer>, ? extends Number> map) throws IOException {
		writer.newLine();
		writer.newLine();
		List<Map.Entry<Pair<Integer, Integer>, ? extends Number>> entryList = new LinkedList<>();

		entryList.addAll(map.entrySet());
		Collections.sort(entryList, new EntryComparator());
		for (Map.Entry<Pair<Integer, Integer>, ? extends Number> entry : entryList) {
			writer.write("(");
			writer.write(Integer.toString(entry.getKey().getLeft()));
			writer.write(",");
			writer.write(Integer.toString(entry.getKey().getRight()));
			writer.write(",(");
			writer.write(new Integer(new Double(-entry.getValue().doubleValue()).intValue()).toString());
			writer.write("))");
			writer.newLine();
		}
		writer.newLine();
	}

	@SuppressWarnings("unchecked")
	public void printNegatedEntrySet(BufferedWriter writer, Map<Pair<Integer, Integer>, ? extends Number> map) throws IOException {
		writer.newLine();
		writer.newLine();

		List<Map.Entry<Pair<Integer, Integer>, ? extends Number>> entryList = new LinkedList<>();

		entryList.addAll(map.entrySet());
		Collections.sort(entryList, new EntryComparator());
		for (Map.Entry<Pair<Integer, Integer>, ? extends Number> entry : entryList) {
			writer.write("(");
			writer.write(Integer.toString(entry.getKey().getLeft()));
			writer.write(",");
			writer.write(Integer.toString(entry.getKey().getRight()));
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