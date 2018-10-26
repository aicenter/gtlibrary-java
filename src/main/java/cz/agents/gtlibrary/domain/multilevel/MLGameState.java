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


package cz.agents.gtlibrary.domain.multilevel;

import cz.agents.gtlibrary.NotImplementedException;
import cz.agents.gtlibrary.iinodes.*;
import cz.agents.gtlibrary.interfaces.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MLGameState extends GameStateImpl implements DomainWithPublicState {
    private static final long serialVersionUID = -188542236725674L;

    protected int round;
    protected ISKey isKey;
    protected PSKey psKey;
    protected int index;

    public MLGameState() {
        super(MLGameInfo.ALL_PLAYERS);
        round = 0;
        index = 0;
    }

    public MLGameState(MLGameState gameState) {
        super(gameState);
        this.round = gameState.round;
        this.index = gameState.index;
    }

    @Override
    public Player getPlayerToMove() {
        if(index == 0) return players[0];
        else return players[1];
    }

    public void performAction(MLAction action) {
        cleanCache();
        int v = action.getValue();
        index |= v << round*2;
        round++;
    }

    private void cleanCache() {
        isKey = null;
        psKey = null;
    }

    @Override
    public boolean isPlayerToMoveNature() {
        return false;
    }

    @Override
    public double[] getUtilities() {
        double v;
        switch (index) {
            case 9:  v =  1; break;
            case 21: v =  1; break;
            case 37: v = -1; break;
            case 10: v = -2; break;
            case 22: v =  0; break;
            case 38: v =  2; break;
            default:
                throw new RuntimeException(index + " is not a leaf!");
        }

        return new double[]{v, -v, 0};
    }

    @Override
    public boolean isGameEnd() {
        return index == 9  ||
               index == 21 ||
               index == 37 ||
               index == 10 ||
               index == 22 ||
               index == 38;
    }

    @Override
    public GameState copy() {
        return new MLGameState(this);
    }

    @Override
    public double[] evaluate() {
        throw new NotImplementedException();
    }

    @Override
    public double getProbabilityOfNatureFor(Action action) {
        throw new NotImplementedException();
    }

    @Override
    public int hashCode() {
        return index;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MLGameState other = (MLGameState) obj;

        return index == other.index;
    }

    @Override
    public ISKey getISKeyForPlayerToMove() {
        if (isKey == null) {
            isKey = new PerfectRecallISKey(index == 0 ? 0 : 1, history.getSequenceOf(getPlayerToMove()));
        }
        return isKey;
    }

    @Override
    public PSKey getPSKeyForPlayerToMove() {
        PSKey maybeHasForcedKey = super.getPSKeyForPlayerToMove();
        if (maybeHasForcedKey != null) return maybeHasForcedKey;

        if (psKey == null) {
            psKey = new PSKey(index == 0 ? 0 : 1);
        }
        return psKey;
    }

    @Override
    public String toString() {
        return history.toString();
    }
}
