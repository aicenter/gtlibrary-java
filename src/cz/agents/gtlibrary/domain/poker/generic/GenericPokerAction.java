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

import org.apache.commons.lang3.builder.HashCodeBuilder;

import cz.agents.gtlibrary.domain.poker.PokerAction;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.InformationSet;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.PublicAction;

public class GenericPokerAction extends PokerAction implements PublicAction {

	private static final long serialVersionUID = -1491826905055714815L;
	
	final private int value;
        final private int round;

	public GenericPokerAction(String action, InformationSet i, Player player, int value, int round) {
		super(action, i, player);
		this.value = value;
                this.round = round;
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
		builder.append("value: ");
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
    public boolean publicEquals(Action act) {
        GenericPokerAction other = (GenericPokerAction)act;
        if (getInformationSet() == null){
            if (other.getInformationSet() != null) return false;//both are nature actions
            if (round < 2) return round == other.round;
            else return value == other.value;
        }
        assert getInformationSet().getPlayer().getId() != 2 : "Chance action are assumed to have null information set";
        return round == other.round && action.equals(other.action) && value == other.value;
    }

    
}
