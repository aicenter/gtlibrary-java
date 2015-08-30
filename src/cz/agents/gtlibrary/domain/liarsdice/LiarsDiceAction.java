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
package cz.agents.gtlibrary.domain.liarsdice;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import cz.agents.gtlibrary.iinodes.ActionImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.PublicAction;

public class LiarsDiceAction extends ActionImpl implements PublicAction {

    protected final int action;
    protected final Player player;

    protected int cachedHash = 0;
    protected int cachedHashWithoutIS;

    public LiarsDiceAction(int action, InformationSet i, Player player) {
        super(i);
        this.action = action;
        this.player = player;

        cachedHash = computeHashCode();
        cachedHashWithoutIS = computeHashCodeWithoutIS();
    }

    private int computeHashCode() {
        return new HashCodeBuilder(17, 37).append(action).append(player).append(getInformationSet()).toHashCode();
    }

    private int computeHashCodeWithoutIS() {
        return new HashCodeBuilder(17, 37).append(action).append(player).toHashCode();
    }

    @Override
    public void perform(GameState gameState) {
        LiarsDiceGameState state = (LiarsDiceGameState) gameState;

        if (!getPlayer().equals(state.getPlayerToMove())) {
            throw new IllegalStateException("Wrong player attempts to make move.");
        }

        state.bid(this);
    }

    public int getValue() {
        return action;
    }

    public Player getPlayer() {
        return player;
    }

  // 1-1, 1-2, ..., 1-6, 2-1, ... , 2-6, 3-1,...3-6,...,call bluff
    public static int getQuantity(int action) {
        return 1 + (action - 1) / LDGameInfo.FACES;
    }

    public static int getFace(int action) {
        return ((action - 1) % LDGameInfo.FACES) + 1;
    }

    @Override
    public String toString() {
        return "[" + action + " (" + getQuantity(action) + "-" + getFace(action) + "), " + player + "]";
    }

    @Override
    public int hashCode() {
        return cachedHash;
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        LiarsDiceAction other = (LiarsDiceAction) obj;
        if (action != other.action) {
            return false;
        } else if (!player.equals(other.player)) {
            return false;
        }
        return true;
    }

    public boolean observableEquals(LiarsDiceAction other) {
        if (this == other) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (getClass() != other.getClass()) {
            return false;
        }
        if (action != other.action) {
            return false;
        }
        if (player == null) {
            if (other.player != null) {
                return false;
            }
        } else if (!player.equals(other.player)) {
            return false;
        }
        return true;
    }

    public int observableISHash() {
        return cachedHashWithoutIS;
    }

    @Override
    public boolean publicEquals(Action act) {
        if (act.getInformationSet() == null && getInformationSet() == null) return true;
        return observableEquals((LiarsDiceAction) act);
    }

}
