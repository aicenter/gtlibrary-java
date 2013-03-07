package cz.agents.gtlibrary.iinodes;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;

public abstract class InformationSetImpl implements InformationSet {
	protected Sequence playerHistory;
	protected Player player;
	protected LinkedHashSet<GameState> statesInInformationSet = new LinkedHashSet<GameState>();
	private final int hashCode;

	public InformationSetImpl(GameState state) {
		this.playerHistory = state.getSequenceForPlayerToMove();
		this.player = state.getPlayerToMove();
		this.statesInInformationSet.add(state);
		this.hashCode = state.getISKeyForPlayerToMove().getLeft();
	}

	@Override
	public Player getPlayer() {
		return player;
	}

	@Override
	public Sequence getPlayersHistory() {
		return playerHistory;
	}

	@Override
	public int hashCode() {
		return this.hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (this.hashCode != obj.hashCode())
			return false;
                if (getClass() != obj.getClass()) {
                    return false;
                }
		InformationSet other = (InformationSet) obj;
		
		if (!this.player.equals(other.getPlayer()))
			return false;
		if (!this.playerHistory.equals(other.getPlayersHistory()))
			return false;
		return true;
	}

	public void addStateToIS(GameState state) {
		statesInInformationSet.add(state);
	}

    public void addAllStateToIS(Collection<GameState> states) {
        statesInInformationSet.addAll(states);
    }

    @Override
	public Set<GameState> getAllStates() {
		return statesInInformationSet;
	}

	@Override
	public String toString() {
		return "IS:(" + player + "):" + playerHistory;
	}
}
