package gametree.IINodes;

import gametree.interfaces.Player;

public class PlayerImpl implements Player {
	
	private int id;
	
	public PlayerImpl(int id) {
		this.id = id;
	}

	@Override
	public int getId() {
		return id;
	}	
	
	@Override
	public String toString() {
		return "Pl" + id;
	}
}
