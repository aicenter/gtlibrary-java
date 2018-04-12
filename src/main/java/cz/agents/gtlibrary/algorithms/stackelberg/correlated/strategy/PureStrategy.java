package cz.agents.gtlibrary.algorithms.stackelberg.correlated.strategy;

import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;

import java.io.Serializable;
import java.util.ListIterator;

public interface PureStrategy extends Iterable<Action>, Serializable {

	public Action get(int index);
	
	public void set(int index, Action action);

	public Player getPlayer();

	public int size();

    public ListIterator<Action> listIterator();

    public ListIterator<Action> listIterator(int index);
    
    public boolean isCompatibleWith(Sequence sequence);
    
    public boolean isCompatibleWith(Action action);
    
    public PureStrategy copy();

	//public boolean isCompatibleWithPartial(Sequence sequence, PureStrategy strategy);

	public boolean isCompatibleWithPartial(Sequence sequence);

}
