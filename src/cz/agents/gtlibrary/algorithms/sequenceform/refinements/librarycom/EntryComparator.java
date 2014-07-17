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

import cz.agents.gtlibrary.utils.Pair;

import java.util.Comparator;
import java.util.Map.Entry;

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
