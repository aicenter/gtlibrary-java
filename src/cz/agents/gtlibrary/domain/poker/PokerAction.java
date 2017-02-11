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


package cz.agents.gtlibrary.domain.poker;

import cz.agents.gtlibrary.iinodes.ActionImpl;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;
import cz.agents.gtlibrary.interfaces.Player;

public abstract class PokerAction extends ActionImpl {

	private static final long serialVersionUID = 6347157865176927070L;

	protected String card;
	protected final String action;
	protected final Player player;

	protected int cachedHash = 0;
	protected int cachedHashWithoutIS;

	public PokerAction(String action, InformationSet i, Player player) {
		super(i);
		this.action = action;
		this.player = player;
		cachedHash = computeHashCode();
		cachedHashWithoutIS = computeHashCodeWithoutIS();
	}

	public PokerAction(String action, InformationSet i, Player player, String card) {
		super(i);
		this.action = action;
		this.player = player;
		this.card = card;
		cachedHash = computeHashCode();
		cachedHashWithoutIS = computeHashCodeWithoutIS();
	}

	public abstract int computeHashCode();

	public abstract int computeHashCodeWithoutIS();

	@Override
	public void perform(GameState gameState) {
		PokerGameState state = (PokerGameState) gameState;

		if (!getPlayer().equals(state.getPlayerToMove())) {
			throw new IllegalStateException("Wrong player attempts to make move.");
		}

		if (action.equals("b")) {
			state.bet(this);
		} else if (action.equals("c")) {
			state.call(this);
		} else if (action.equals("ch")) {
			state.check(this);
		} else if (action.equals("f")) {
			state.fold(this);
		} else if (action.equals("r")) {
			state.raise(this);
		} else {
			state.attendCard(this);
		}
	}

	public String getActionType() {
		return action;
	}

	public Player getPlayer() {
		return player;
	}

	@Override
	public String toString() {
		return "[" + action + ", " + player + ", " + card + "]";
	}

	@Override
	public int hashCode() {
		return computeHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!super.equals(obj))
			return false;
		PokerAction other = (PokerAction) obj;
		if (action == null) {
			if (other.action != null)
				return false;
		} else if (!action.equals(other.action))
			return false;
		if (player == null) {
			if (other.player != null)
				return false;
		} else if (!player.equals(other.player))
			return false;
		return true;
	}

	public boolean observableEquals(PokerAction other) {
		if (this == other)
			return true;
		if (other == null)
			return false;
		if (getClass() != other.getClass())
			return false;
		if (action == null) {
			if (other.action != null)
				return false;
		} else if (!action.equals(other.action))
			return false;
		if (player == null) {
			if (other.player != null)
				return false;
		} else if (!player.equals(other.player))
			return false;
		return true;
	}

	public int observableISHash() {
		return cachedHashWithoutIS;
	}

    public String getPlayersCard() {
        if (card != null) {
            return ((PokerGameState)informationSet.getAllStates().iterator().next()).getCardFor(player).getActionType();
        } else
            return "none";
    }

}
