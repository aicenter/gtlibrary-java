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


package cz.agents.gtlibrary.experimental.stochastic.newexperiment;

import cz.agents.gtlibrary.experimental.stochastic.StochasticGameState;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Player;

public class NEGameState extends StochasticGameState {

	private static final long serialVersionUID = 134417344843916155L;

	private double utility;
	private Action p1Action;
	private Action p2Action;
	private Player currentPlayer;
	private String id;

	public NEGameState(double utility, String id) {
		super(NEGameInfo.ALL_PLAYERS);
		this.utility = utility;
		currentPlayer = NEGameInfo.p1;
		this.id = id;
	}

	public NEGameState(NEGameState state) {
		super(state);
		this.id = state.id;
		this.utility = state.utility;
		this.p1Action = state.p1Action;
		this.p2Action = state.p2Action;
		this.currentPlayer = state.currentPlayer;
	}

	public double getProbabilityOfNatureFor(Action action) {
		return 1;
	}

	public void performActionModifyingThisState(Action action) {
		if (((NEAction) action).getPlayer().equals(NEGameInfo.p1)) {
			p1Action = action;
			currentPlayer = NEGameInfo.p2;
		} else {
			p2Action = action;
			currentPlayer = NEGameInfo.nature;
		}
	}
	
	public String getId() {
		return id;
	}

	public NEGameState performAction(Action action) {
		if (action instanceof NatureAction)
			return ((NatureAction) action).getGameState();
		NEGameState state = copy();

		state.performActionModifyingThisState(action);
		return state;
	}

	public NEGameState copy() {
		return new NEGameState(this);
	}

	public Player getPlayerToMove() {
		return currentPlayer;
	}
	
	public NEAction getP1Action() {
		return (NEAction) p1Action;
	}
	
	public NEAction getP2Action() {
		return (NEAction) p2Action;
	}

	@Override
	public double[] getUtilities() {
		return new double[] { utility, -utility, 0 };
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((currentPlayer == null) ? 0 : currentPlayer.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((p1Action == null) ? 0 : p1Action.hashCode());
		result = prime * result + ((p2Action == null) ? 0 : p2Action.hashCode());
		long temp;
		temp = Double.doubleToLongBits(utility);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		NEGameState other = (NEGameState) obj;
		if (currentPlayer == null) {
			if (other.currentPlayer != null)
				return false;
		} else if (!currentPlayer.equals(other.currentPlayer))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (p1Action == null) {
			if (other.p1Action != null)
				return false;
		} else if (!p1Action.equals(other.p1Action))
			return false;
		if (p2Action == null) {
			if (other.p2Action != null)
				return false;
		} else if (!p2Action.equals(other.p2Action))
			return false;
		if (Double.doubleToLongBits(utility) != Double.doubleToLongBits(other.utility))
			return false;
		return true;
	}

	@Override
	public boolean isGameEnd() {
		return id.equals("D") || id.equals("E") || id.equals("F") || id.equals("G") || id.equals("H");
	}

	@Override
	public boolean isPlayerToMoveNature() {
		return currentPlayer.equals(NEGameInfo.nature);
	}
	
	@Override
	public String toString() {
		return id;
	}

}
