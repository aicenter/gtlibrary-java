package cz.agents.gtlibrary.interfaces;

import java.io.Serializable;
import java.util.Set;

public interface InformationSet extends Serializable {
	public Player getPlayer();
	public Sequence getPlayersHistory();	
	public Set<GameState> getAllStates();
}
