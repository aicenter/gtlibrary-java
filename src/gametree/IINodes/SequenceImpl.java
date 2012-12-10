package gametree.IINodes;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import gametree.interfaces.Action;
import gametree.interfaces.Player;
import gametree.interfaces.Sequence;

public class SequenceImpl implements Sequence {

	private LinkedList<Action> actions;
	private Player player;

	public SequenceImpl(Player player) {
		this.player = player;
		actions = new LinkedList<Action>();
	}

	public SequenceImpl(Sequence sequence) {
		player = sequence.getPlayer();
		actions = new LinkedList<Action>();
		addAllAsLast(sequence);
	}

	@Override
	public Iterator<Action> iterator() {
		return actions.iterator();
	}

	@Override
	public void addFirst(Action action) {
		actions.addFirst(action);
	}

	@Override
	public void addLast(Action action) {
		actions.addLast(action);
	}

	@Override
	public Action getFirst() {
		return actions.getFirst();
	}

	@Override
	public Action getLast() {
		return actions.getLast();
	}

	@Override
	public Action removeFirst() {
		return actions.removeFirst();
	}

	@Override
	public Action removeLast() {
		return actions.removeLast();
	}

	@Override
	public void addAllAsFirst(Iterable<Action> actions) {
		for (Action action : actions) {
			addFirst(action);
		}
	}

	@Override
	public void addAllAsLast(Iterable<Action> actions) {
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
		Sequence tmpSeq = new SequenceImpl(player);

		result.add(new SequenceImpl(player));

		for (int i = 0; i < size(); i++) {
			tmpSeq.addLast(actions.get(i));
			result.add(new SequenceImpl(tmpSeq));
		}

		return result;
	}

	public Sequence[] getAllPrefixesArray() {
		Sequence[] result = new Sequence[size() + 1];
		Sequence tmpSeq = new SequenceImpl(player);

		result[0] = new SequenceImpl(tmpSeq);

		for (int i = 0; i < size(); i++) {
			tmpSeq.addLast(actions.get(i));
			result[i + 1] = new SequenceImpl(tmpSeq);
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
		Sequence result = new SequenceImpl(player);

		result.addAllAsLast(this.actions.subList(0, size));
		return result;
	}

	public Sequence getSubSequence(int from, int size) {
		assert (this.actions.size() - from >= size);
		Sequence result = new SequenceImpl(player);

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
		final int prime = 31;
		int result = 1;
		
		result = prime * result + ((actions == null) ? 0 : actions.hashCode());
		result = prime * result + ((player == null) ? 0 : player.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SequenceImpl other = (SequenceImpl) obj;
		if (actions == null) {
			if (other.actions != null)
				return false;
		} else if (!actions.equals(other.actions))
			return false;
		if (player == null) {
			if (other.player != null)
				return false;
		} else if (!player.equals(other.player))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return actions.toString();
	}

}
