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

import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;

public abstract class SimultaneousGameState extends GameStateImpl {

    protected int depth;

    public SimultaneousGameState(Player[] players) {
        super(players);
        depth = Integer.MAX_VALUE;
    }

    public SimultaneousGameState(SimultaneousGameState gameState) {
        super(gameState);
        this.depth = gameState.depth;
    }

    @Override
    public void transformInto(GameState gameState) {
        super.transformInto(gameState);
        this.depth = ((SimultaneousGameState)gameState).depth;
    }

    public int getDepth() {
        return depth;
    }

    @Override
    public boolean isGameEnd() {
        return isDepthLimit() || isActualGameEnd();
    }

    public double[] getUtilities() {
        if (isActualGameEnd())
            return getEndGameUtilities();
        return evaluate();
    }

    public abstract void setDepth(int depth);

    protected abstract double[] getEndGameUtilities();

    public abstract boolean isActualGameEnd();

    public abstract boolean isDepthLimit();
}
