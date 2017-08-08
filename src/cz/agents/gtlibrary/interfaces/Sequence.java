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


package cz.agents.gtlibrary.interfaces;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;

public interface Sequence extends Iterable<Action>, Serializable {
	
	public void addFirst(Action action);

	public void addLast(Action action);

	public Action get(int index);

	public Action getFirst();

	public Action getLast();

	public Action removeFirst();

	public Action removeLast();

	public void addAllAsFirst(Iterable<Action> actions);

	public void addAllAsLast(Iterable<Action> actions);

	public Player getPlayer();

	public boolean isPrefixOf(Sequence sequence);

	public Sequence getSubSequence(int size);

	public Sequence getSubSequence(int from, int size);

	public HashSet<Sequence> getAllPrefixes();

	public Sequence[] getAllPrefixesArray();

	public int size();

	public InformationSet getLastInformationSet();
	
	public List<Action> getAsList();

    public boolean isEmpty();

    public ListIterator<Action> listIterator();

    public ListIterator<Action> listIterator(int index);

}
