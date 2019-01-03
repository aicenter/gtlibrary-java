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


package cz.agents.gtlibrary.domain.poker.generic;

import cz.agents.gtlibrary.domain.liarsdice.LiarsDiceAction;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.PublicAction;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import cz.agents.gtlibrary.domain.poker.PokerAction;
import cz.agents.gtlibrary.interfaces.InformationSet;
import cz.agents.gtlibrary.interfaces.Player;

public class GenericPokerAction extends PokerAction implements PublicAction {

	private static final long serialVersionUID = -1491826905055714815L;
	
	final private int value;

	public GenericPokerAction(String action, InformationSet i, Player player, int value) {
		super(action, i, player);
		this.value = value;
        cachedHash = computeHashCode();
        cachedHashWithoutIS = computeHashCodeWithoutIS();
    }
	
	public int getValue() {
		return value;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		builder.append("[");
		builder.append(player);
		builder.append(", ");
        builder.append(getPlayersCard());
        builder.append(", ");
		builder.append(action);
		builder.append(", ");
		builder.append("reward: ");
		builder.append(value);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int computeHashCode() {
		return new HashCodeBuilder(17,31).append(action).append(player).append(value).append(informationSet).toHashCode();
	}

	@Override
	public int computeHashCodeWithoutIS() {
		return new HashCodeBuilder(17,31).append(action).append(player).append(value).toHashCode();
	}

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj))
            return false;
        GenericPokerAction other = (GenericPokerAction)obj;
        if (this.value != other.value)
            return false;
        return true;
    }

    @Override
    public boolean observableEquals(PokerAction obj) {
		GenericPokerAction other = (GenericPokerAction) obj;
		if (action == null) {
			if (other.action != null) return false;
		} else if (!action.equals(other.action)) return false;

		if (player == null) {
			if (other.player != null) return false;
		} else if (!player.equals(other.player)) return false;

		if (this.value != other.value) return false;
		return true;
    }

	@Override
	public boolean publicEquals(Action act) {
		if (act.getInformationSet() == null && getInformationSet() == null) return true;
		return observableEquals((GenericPokerAction) act);
	}
}
