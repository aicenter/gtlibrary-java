package cz.agents.gtlibrary.interfaces;

import java.util.Set;

public interface InformationSet {
	public Player getPlayer();
	public Sequence getPlayersHistory();	
	public Set<GameState> getAllStates();
}
