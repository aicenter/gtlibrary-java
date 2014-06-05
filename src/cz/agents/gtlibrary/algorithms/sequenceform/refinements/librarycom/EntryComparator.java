package cz.agents.gtlibrary.algorithms.sequenceform.refinements.librarycom;

import java.util.Comparator;
import java.util.Map.Entry;

import cz.agents.gtlibrary.utils.Pair;

public class EntryComparator implements Comparator<Entry<Pair<Integer, Integer>, ? extends Object>> {

	@Override
	public int compare(Entry<Pair<Integer, Integer>, ? extends Object> o1, Entry<Pair<Integer, Integer>, ? extends Object> o2) {
		if(o1.getKey().getLeft() > o2.getKey().getLeft())
			return 1;
		if(o1.getKey().getLeft() < o2.getKey().getLeft())
			return -1;
		if(o1.getKey().getRight() > o2.getKey().getRight())
			return 1;
		if(o1.getKey().getRight() < o2.getKey().getRight())
			return -1;
		return 0;
	}

}
