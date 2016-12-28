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


package cz.agents.gtlibrary.domain.goofspiel;

import cz.agents.gtlibrary.iinodes.ActionImpl;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;
import cz.agents.gtlibrary.interfaces.Player;

public class GoofSpielAction extends ActionImpl implements Comparable<GoofSpielAction> {

	private static final long serialVersionUID = -3504137065821329745L;

	private final int value;

	private Player player;
	private int hashCode = -1;

	public GoofSpielAction(int value, Player player, InformationSet informationSet) {
		super(informationSet);
		this.value = value;
		this.player = player;
	}

	@Override
	public void perform(GameState gameState) {
		if (player.equals(GSGameInfo.FIRST_PLAYER)) {
			((GoofSpielGameState) gameState).performFirstPlayerAction(this);
		} else if (player.equals(GSGameInfo.SECOND_PLAYER)) {
			((GoofSpielGameState) gameState).performSecondPlayerAction(this);
		} else {
			((GoofSpielGameState) gameState).performNatureAction(this);
		}
	}

	public int getValue() {
		return value;
	}

	public Player getPlayer() {
		return player;
	}

	@Override
	public int compareTo(GoofSpielAction action) {
		return value - action.getValue();
	}


	@Override
	public int hashCode() {
		if (hashCode != -1)
			return hashCode;
		final int prime = 31;

		hashCode = 1;
		hashCode = prime * hashCode + ((player == null) ? 0 : player.hashCode());
		hashCode = prime * hashCode + ((informationSet == null) ? 0 : informationSet.hashCode());
		hashCode = prime * hashCode + value;
		return hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		GoofSpielAction other = (GoofSpielAction) obj;
		if (player == null) {
			if (other.player != null)
				return false;
		} else if (!player.equals(other.player))
			return false;
		if (value != other.value)
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
//                String descIS="";
//                try {
//                    GoofSpielGameState fistState = (GoofSpielGameState) informationSet.getAllStates().iterator().next();
//                    //descIS += fistState.getISKeyForPlayerToMove().getLeft() + ", ";
//                    descIS += fistState.getPlayerScore()[0] + "-" + fistState.getPlayerScore()[1];
//                } catch (NullPointerException ex){
//                    //intentionally empty
//                }

		builder.append("[" + value + /*", " + descIS + */", " + player);
		builder.append("]");
		return builder.toString();
	}

}
