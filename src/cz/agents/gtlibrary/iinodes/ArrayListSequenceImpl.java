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


package cz.agents.gtlibrary.iinodes;

import java.util.*;

import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.InformationSet;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;

public class ArrayListSequenceImpl implements Sequence {

	private static final long serialVersionUID = -2037759537736542277L;

	private ArrayList<Action> actions;
	private Player player;
	private int hashCode = -1;

	public ArrayListSequenceImpl(Player player) {
		this.player = player;
		actions = new ArrayList<>();
	}

	public ArrayListSequenceImpl(Sequence sequence) {
		player = sequence.getPlayer();
		actions = new ArrayList<>(sequence.size()+1);
		addAllAsLast(sequence);
	}

	@Override
	public Iterator<Action> iterator() {
//		hashCode = -1;
		return actions.iterator();
	}

	@Override
	public void addFirst(Action action) {
		hashCode = -1;
		actions.add(0, action);
	}

	@Override
	public void addLast(Action action) {
		hashCode = -1;
		actions.add(action);
	}

	@Override
	public Action getFirst() {
		return actions.get(0);
	}

	@Override
	public Action getLast() {
		return actions.get(actions.size() - 1);
	}

	@Override
	public Action removeFirst() {
		hashCode = -1;
		return actions.remove(0);
	}

	@Override
	public Action removeLast() {
		hashCode = -1;
		return actions.remove(actions.size() - 1);
	}

	@Override
	public void addAllAsFirst(Iterable<Action> actions) {
		hashCode = -1;
		for (Action action : actions) {
			addFirst(action);
		}
	}

	@Override
	public void addAllAsLast(Iterable<Action> actions) {
		hashCode = -1;
		for (Action action : actions) {
			addLast(action);
		}
	}

	@Override
	public Player getPlayer() {
		return player;
	}

	public HashSet<Sequence> getAllPrefixes() {
		HashSet<Sequence> result = new HashSet<Sequence>();
		Sequence tmpSeq = new ArrayListSequenceImpl(player);

		result.add(new ArrayListSequenceImpl(player));

		for (int i = 0; i < size(); i++) {
			tmpSeq.addLast(actions.get(i));
			result.add(new ArrayListSequenceImpl(tmpSeq));
		}

		return result;
	}

	public Sequence[] getAllPrefixesArray() {
		Sequence[] result = new Sequence[size() + 1];
		Sequence tmpSeq = new ArrayListSequenceImpl(player);

		result[0] = new ArrayListSequenceImpl(tmpSeq);

		for (int i = 0; i < size(); i++) {
			tmpSeq.addLast(actions.get(i));
			result[i + 1] = new ArrayListSequenceImpl(tmpSeq);
		}

		return result;
	}

	public boolean isPrefixOf(Sequence longerSeq) {
		if (this.size() > longerSeq.size())
			return false;

		for (int i = 0; i < size(); i++) {
			if (!this.get(i).equals(longerSeq.get(i)))
				return false;
		}

		return true;
	}

	public Sequence getSubSequence(int size) {
		assert (this.actions.size() >= size);
		Sequence result = new ArrayListSequenceImpl(player);

		result.addAllAsLast(this.actions.subList(0, size));
		return result;
	}

	public Sequence getSubSequence(int from, int size) {
		assert (this.actions.size() - from >= size);
		Sequence result = new ArrayListSequenceImpl(player);

		result.addAllAsLast(this.actions.subList(from, from + size));
		return result;
	}

	@Override
	public int size() {
		return actions.size();
	}

	@Override
	public Action get(int index) {
		return actions.get(index);
	}

	@Override
	public int hashCode() {
//		if (hashCode != -1)
//			return hashCode;

		final int prime = 31;
		hashCode = 1;

		hashCode = prime * hashCode + ((actions == null) ? 0 : actions.hashCode());
		hashCode = prime * hashCode + ((player == null) ? 0 : player.hashCode());
		return hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Sequence)) return false;
		Sequence sequence = (Sequence) obj;

		if (!player.equals(sequence.getPlayer()))
			return false;
		if (!getAsList().equals(sequence.getAsList()))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return player + ": " + actions.toString();
	}

	@Override
	public InformationSet getLastInformationSet() {
		if (size() == 0)
			return null;
		return getLast().getInformationSet();
	}
	
	@Override
	public List<Action> getAsList() {
		return actions;
	}

    @Override
    public boolean isEmpty() {
        return actions.isEmpty();
    }

	public ListIterator<Action> listIterator() {
		 return actions.listIterator();
	}

	public ListIterator<Action> listIterator(int index) {
		return actions.listIterator(index);
	}
}
