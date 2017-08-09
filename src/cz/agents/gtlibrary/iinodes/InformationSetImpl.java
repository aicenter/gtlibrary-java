/*
Copyright 2014 Faculty of Electrical Engineering at CTU in Prague

This file is part of Game Theoretic Library.

Game Theoretic Library is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Game Theoretic Library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with Game Theoretic Library.  If not, see <http://www.gnu.org/licenses/>.*/


package cz.agents.gtlibrary.iinodes;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import cz.agents.gtlibrary.interfaces.*;

public abstract class InformationSetImpl implements PerfectRecallInformationSet {
	
	private static final long serialVersionUID = 3656344734672077909L;
	
	protected Sequence playerHistory;
	protected Player player;
	protected LinkedHashSet<GameState> statesInInformationSet = new LinkedHashSet<>();
	private final int hashCode;

	public InformationSetImpl(GameState state) {
		this.playerHistory = state.getSequenceForPlayerToMove();
		this.player = state.getPlayerToMove();
		this.statesInInformationSet.add(state);
		this.hashCode = ((PerfectRecallISKey)state.getISKeyForPlayerToMove()).getHash();
	}
	
	public InformationSetImpl(GameState state, Sequence sequence) {
		this.playerHistory = new ArrayListSequenceImpl(sequence);
		this.player = state.getPlayerToMove();
		this.statesInInformationSet.add(state);
		this.hashCode = ((PerfectRecallISKey)state.getISKeyForPlayerToMove()).getHash();
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
		if (obj == null)
			return false;
		if (this.hashCode != obj.hashCode())
			return false;
        if (!(obj instanceof PerfectRecallInformationSet))
            return false;
		PerfectRecallInformationSet other = (PerfectRecallInformationSet) obj;
		
		if (!this.player.equals(other.getPlayer()))
			return false;
		if (!this.playerHistory.equals(other.getPlayersHistory()))
			return false;
		return true;
	}

	public void addStateToIS(GameState state) {
		assert state.getPlayerToMove().equals(player);
		statesInInformationSet.add(state);
	}

    public void addAllStateToIS(Collection<GameState> states) {
		assert states.stream().allMatch(state -> state.getPlayerToMove().equals(player));
        statesInInformationSet.addAll(states);
    }

    @Override
	public Set<GameState> getAllStates() {
		return statesInInformationSet;
	}

	@Override
	public ISKey getISKey() {
		return statesInInformationSet.iterator().next().getISKeyForPlayerToMove();
	}

	@Override
	public String toString() {
		return "IS:(" + player + "):" + playerHistory;
	}
}
