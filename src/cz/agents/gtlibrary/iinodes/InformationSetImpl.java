package cz.agents.gtlibrary.iinodes;

import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;

public abstract class InformationSetImpl implements InformationSet {
	
	private Sequence playerHistory;
	private Player player;
	
	public InformationSetImpl(GameState state) {
		this.playerHistory = state.getSequenceForPlayerToMove();
		this.player = state.getPlayerToMove();
	}
	
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
