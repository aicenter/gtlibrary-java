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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Iterator;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.quasiperfect.numbers.Rational;
import cz.agents.gtlibrary.iinodes.GameStateImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.Pair;

public class LiarsDiceGameState extends GameStateImpl {
    protected Pair<Integer, Sequence> cachedISKey = null;

    protected int currentBid = 0;
    protected int previousBid = 0;
    protected int round = 0;
    protected int currentPlayerIndex;
    protected int hash = -1;
    protected int[] rolls;

    public static final int CALLBID = (LDGameInfo.P1DICE + LDGameInfo.P2DICE) * 6 + 1;

    public LiarsDiceGameState() {
        super(new Player[]{LDGameInfo.FIRST_PLAYER, LDGameInfo.SECOND_PLAYER, LDGameInfo.NATURE});
        this.round = 0;
        this.currentPlayerIndex = 2;
        this.rolls = new int[]{0, 0};
        this.currentBid = 0;
        this.previousBid = 0;
    }

    public LiarsDiceGameState(LiarsDiceGameState gameState) {
        super(gameState);
        this.round = gameState.round;
        this.currentPlayerIndex = gameState.currentPlayerIndex;
        this.rolls = new int[2];
        this.rolls[0] = gameState.rolls[0];
        this.rolls[1] = gameState.rolls[1];
        this.currentBid = gameState.currentBid;
        this.previousBid = gameState.previousBid;
    }

    @Override
    public GameState copy() {
        return new LiarsDiceGameState(this);
    }

    @Override
    public double getProbabilityOfNatureFor(Action action) {
        return 1.0 / 6.0;
    }

    @Override
    public Rational getExactProbabilityOfNatureFor(Action action) {
        if (!isPlayerToMoveNature()) {
            return Rational.ZERO;
        }

        return new Rational(1, 6);
    }

    @Override
    public double[] getUtilities() {
        if (isGameEnd()) {
            int winner = determineWinner();
            if (winner == 0) {
                return new double[]{1, -1, 0};
            } else {
                return new double[]{-1, 1, 0};
            }
        }
        return new double[]{0};
    }

    @Override
    public Rational[] getExactUtilities() {
        if (isGameEnd()) {
            Rational[] exactUtilities;
            int winner = determineWinner();
            if (winner == 0) {
                exactUtilities = new Rational[]{new Rational(1), new Rational(-1), Rational.ZERO};
            } else {
                exactUtilities = new Rational[]{new Rational(-1), new Rational(1), Rational.ZERO};
            }
            return exactUtilities;
        }
        return new Rational[]{Rational.ZERO};
    }

    public void bid(LiarsDiceAction action) {
        clearCachedValues();

        if (currentPlayerIndex == 2 && rolls[0] == 0) {
            rolls[0] = action.getValue();
        } else if (currentPlayerIndex == 2 && rolls[1] == 0) {
            rolls[1] = action.getValue();
        } else {
            previousBid = currentBid;
            currentBid = action.getValue();
        }

        increaseRound();
        switchPlayers();
    }

    public int determineWinner() {
        int callingPlayer = (1 - currentPlayerIndex);
        int quantity = LiarsDiceAction.getQuantity(previousBid);
        int face = LiarsDiceAction.getFace(previousBid);

        int matches = 0;

        // 6's are wild
        if (rolls[0] == face || rolls[0] == 6) {
            matches++;
        }

        if (rolls[1] == face || rolls[1] == 6) {
            matches++;
        }

        // if bid is false, calling player wins
        if (matches < quantity) {
            return callingPlayer;
        } else {
            return (1 - callingPlayer);
        }
    }

    public int getCurBid() {
        return currentBid;
    }

    public int getRound() {
        return round;
    }

    @Override
    public Player getPlayerToMove() {
        return players[currentPlayerIndex];
    }

    @Override
    public boolean isPlayerToMoveNature() {
        return currentPlayerIndex == 2;
    }

    @Override
    public boolean isGameEnd() {
        return currentBid == CALLBID;
    }

    @Override
    public Pair<Integer, Sequence> getISKeyForPlayerToMove() {
        if (cachedISKey != null) {
            return cachedISKey;
        }
        if (isPlayerToMoveNature()) {
            cachedISKey = new Pair<Integer, Sequence>(0, history.getSequenceOf(getPlayerToMove()));
            return cachedISKey;
        }

        int hc = rolls[getPlayerToMove().getId()];
        for (Action a : getSequenceFor(getAllPlayers()[1-getPlayerToMove().getId()])){
            hc *= CALLBID;
            hc += ((LiarsDiceAction)a).getValue();
        }

        cachedISKey = new Pair<Integer, Sequence>(hc, history.getSequenceOf(getPlayerToMove()));
        return cachedISKey;
    }

    @Override
    public int hashCode() {
        if (hash == -1) {
            hash = new HashCodeBuilder(17, 31).append(history).toHashCode();
        }
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        LiarsDiceGameState other = (LiarsDiceGameState) obj;
        if (currentPlayerIndex != other.currentPlayerIndex) {
            return false;
        }
        if (!Arrays.equals(rolls, other.rolls)) {
            return false;
        }
        if (currentBid != other.currentBid) {
            return false;
        }
        if (previousBid != other.previousBid) {
            return false;
        }
        if (round != other.round) {
            return false;
        }
        if (!getHistory().equals(other.getHistory())) {
            return false;
        } 
        return true;
    }

    @Override
    public String toString() {
        return history.toString();
    }

    protected void clearCachedValues() {
        hash = -1;
        cachedISKey = null;
    }

    protected void switchPlayers() {
        if (currentPlayerIndex == 2 && rolls[1] == 0) {
            currentPlayerIndex = 2;
        } else if (currentPlayerIndex == 2 && rolls[1] != 0) {
            currentPlayerIndex = 0;
        } else {
            currentPlayerIndex = 1 - currentPlayerIndex;
        }
    }

    protected void increaseRound() {
        round++;
    }

}
