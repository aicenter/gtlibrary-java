package gametree.IINodes;

import gametree.interfaces.InformationSet;
import gametree.interfaces.Player;
import gametree.interfaces.Sequence;

public class InformationSetImpl implements InformationSet {
	
	private Sequence playerHistory;
	private Player player;
	
	public InformationSetImpl(Player player, Sequence playerHistory) {
		this.playerHistory = playerHistory;
		this.player = player;
	}

	@Override
	public Player getPlayer() {
		return player;
	}

	@Override
	public Sequence getPlayersHistory() {
		return playerHistory;
	}

}
