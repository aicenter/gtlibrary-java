package cz.agents.gtlibrary.algorithms.stackelberg.correlated.strategy;

import cz.agents.gtlibrary.iinodes.ActionImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;

import java.util.Arrays;
import java.util.Iterator;
import java.util.ListIterator;

public class PureStrategyImpl implements PureStrategy {
	
	/**
	 * 
	 */
	protected Action[] strategy;
	protected Player player;
	
	public PureStrategyImpl(Action[] strategy, Player player) {
		this.strategy = strategy;
		this.player = player;
	}

	@Override
	public Iterator<Action> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Action get(int index) {
		return strategy[index];
	}

	@Override
	public Player getPlayer() {
		return player;
	}

	@Override
	public int size() {
		return strategy.length;
	}

	@Override
	public ListIterator<Action> listIterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ListIterator<Action> listIterator(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isCompatibleWith(Sequence sequence) {
    	boolean isCompatible;
    	for(Action a : sequence){
    		isCompatible = false;
    		for(Action b : strategy){
    			if(a.equals(b)){
    				isCompatible = true;
    				break;
    			}
    		}
    		if(!isCompatible)
    			return false;
    	}
    	return true;
	}

	@Override
	public boolean isCompatibleWith(Action action) {
    	for(Action b : strategy){
			if(action.equals(b))
				return true;
    	}
    	return false;
	}
	
	@Override
    public boolean isCompatibleWithPartial(Sequence sequence){
    	for(Action a : sequence){
    		for(Action b : strategy){
    			if(b!=null && !a.equals(b) && a.getInformationSet().equals(b.getInformationSet())){
    				return false;
    			}
    		}
    	}
    	return true;
    }
	
	

	@Override
	public String toString() {
		String description = "Pure strategy of "+player.getId()+" [ ";
    	for(Action a : strategy)
    		description+=a+" ";
    	description+=("]\n");
    	return description;
	}

	@Override
	public int hashCode() {
		final int prime = 47;
		final int prime2 = 53;
		int result = 1;
		result = prime * result + ((player == null) ? 0 : player.hashCode());
		for (Action a : strategy){
			result = prime * result + ((a==null) ? prime2 : a.hashCode());
		}
//			result = prime * result + Arrays.hashCode(strategy);
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
		PureStrategyImpl other = (PureStrategyImpl) obj;
		if (player == null) {
			if (other.player != null)
				return false;
		} else if (!player.equals(other.player))
			return false;
		if (!Arrays.equals(strategy, other.strategy))
			return false;
		return true;
	}

	@Override
	public PureStrategyImpl copy() {
    	Action[] strategyCopy = new Action[strategy.length];
    	for(int i = 0; i < strategy.length; i++){
    		strategyCopy[i] = strategy[i];
    	}
    	return new PureStrategyImpl(strategyCopy, player);
	}

	@Override
	public void set(int index, Action action) {
		strategy[index] = action;
	}
	

}
