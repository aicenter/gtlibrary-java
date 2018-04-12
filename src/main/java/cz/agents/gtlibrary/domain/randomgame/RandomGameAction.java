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


package cz.agents.gtlibrary.domain.randomgame;

import cz.agents.gtlibrary.iinodes.ActionImpl;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class RandomGameAction extends ActionImpl{

	private static final long serialVersionUID = -3743050233407643384L;
	
	private final String value;
    private final int order;
    private int hashCode = -1;

    public RandomGameAction(InformationSet informationSet, String value, int order) {
        super(informationSet);
        this.value = value;
        this.order = order;
    }

    @Override
    public void perform(GameState gameState) {
        if (informationSet != null && !informationSet.getPlayer().equals(gameState.getPlayerToMove())) {
            throw new IllegalArgumentException("Incorrect player.");
        }

        ((RandomGameState)gameState).evaluateAction(this);
    }

    @Override
    public int hashCode() {
        if(hashCode == -1)
            hashCode = new HashCodeBuilder(17,31).append(value).append(order).append(informationSet).toHashCode();
        return hashCode;
    }

//    @Override
//    public boolean equals(Object obj) {
//        if (!super.equals(obj))
//            return false;
//
//        return true;
//    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RandomGameAction)) return false;
        if (!super.equals(o)) return false;

        RandomGameAction that = (RandomGameAction) o;

        if (order != that.order) return false;
        if (!value.equals(that.value)) return false;

        return true;
    }
    public String getValue() {
        return value;
    }

    public int getOrder() {
        return order;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[" + informationSet.getPlayer() + ", " + informationSet.hashCode() + ", ");
        builder.append(value.substring(value.indexOf("_")+1));
        builder.append("]");
        return builder.toString();
    }

}
