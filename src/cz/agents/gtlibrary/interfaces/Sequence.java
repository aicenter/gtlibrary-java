package cz.agents.gtlibrary.interfaces;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;

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
}
