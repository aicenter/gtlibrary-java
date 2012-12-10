package gametree.interfaces;

import java.util.HashSet;

public interface Sequence extends Iterable<Action> {
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
}
