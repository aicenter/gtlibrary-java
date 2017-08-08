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


package cz.agents.gtlibrary.domain.simpleGeneralSum;

import cz.agents.gtlibrary.algorithms.sequenceform.refinements.quasiperfect.numbers.Rational;
import cz.agents.gtlibrary.iinodes.GameStateImpl;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.iinodes.PerfectRecallISKey;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.Pair;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Created with IntelliJ IDEA.
 * User: kail
 * Date: 12/5/13
 * Time: 3:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class SimpleGSState extends GameStateImpl {

    protected int depth = 0;
    protected int ID = 0;

    private int hashCode;
    private boolean hashCodeChange = true;
    private ISKey key = null;

    private Player playerToMove;


    public SimpleGSState(Player[] players) {
        super(players);
        playerToMove = players[0];
    }

    public SimpleGSState(SimpleGSState gameState) {
        super(gameState);
        this.playerToMove = gameState.playerToMove;
        this.depth = gameState.depth;
        this.ID = gameState.ID;
    }

    protected void executeAction(SimpleGSAction action) {
        if (action.player.getId() == 0) {
            playerToMove = players[1];
            ID = ID - (action.ID)*SimpleGSInfo.MAX_ACTIONS;
        } else {
            playerToMove = players[0];
            ID = ID - (action.ID);
            depth++;
        }

        if (isGameEnd()) ID = -ID;
        hashCodeChange = true;
        key = null;

    }

    @Override
    public Player getPlayerToMove() {
        return playerToMove;
    }

    @Override
    public GameState copy() {
        return new SimpleGSState(this);
    }

    @Override
    public double[] getUtilities() {
        if (ID < 0) return new double[] {0,0};
        else return SimpleGSInfo.utilityMatrix[ID];
    }

    @Override
    public double getProbabilityOfNatureFor(Action action) {
        return 1;
    }

    @Override
    public boolean isGameEnd() {
        return depth >= SimpleGSInfo.MAX_DEPTH;
    }

    @Override
    public boolean isPlayerToMoveNature() {
        return false;
    }

    @Override
    public ISKey getISKeyForPlayerToMove() {
        if (key != null)
            return key;
        key = new PerfectRecallISKey(new HashCodeBuilder().append(isGameEnd()).append(getHistory().getSequenceOf(playerToMove)).toHashCode(), history.getSequenceOf(playerToMove));
        return key;
    }

    @Override
    public int hashCode() {
        if (hashCodeChange) {
            final int prime = 31;

            hashCode = 1;
            hashCode = prime * hashCode + ((history == null) ? 0 : history.hashCode());
            hashCode = prime * hashCode + playerToMove.getId();
            hashCodeChange = false;
        }
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        if (hashCode() != obj.hashCode())
            return false;
        SimpleGSState other = (SimpleGSState)obj;

        if (!history.equals(other.history))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "SGS:"+ID+"PL:"+getPlayerToMove().getId()+":D:"+depth;
    }
}
