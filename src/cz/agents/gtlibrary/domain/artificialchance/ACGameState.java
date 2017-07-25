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


package cz.agents.gtlibrary.domain.artificialchance;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import cz.agents.gtlibrary.algorithms.sequenceform.refinements.quasiperfect.numbers.Rational;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.iinodes.PerfectRecallISKey;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import cz.agents.gtlibrary.domain.artificialchance.ACAction.ActionType;
import cz.agents.gtlibrary.iinodes.GameStateImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.Pair;

public class ACGameState extends GameStateImpl {

	private static final long serialVersionUID = 704818370381061596L;

	private ActionType[] playerCards;
	private ActionType table;

	private int currentPlayerIndex;
	private int round;
	private int pot;
	private int gainForFirstPlayer;
	enum StageOfGame {
		p1Deal, p2Deal, preTableP1, preTableP2, tableDeal, postTableP1, postTableP2, end
	}

	private boolean folded;

	private ISKey cachedISKey;
	private List<ACAction> sequenceForAllPlayers;

	public ACGameState() {
		super(ACGameInfo.ALL_PLAYERS);
		this.round = 0;
		this.playerCards = new ActionType[2];
		this.currentPlayerIndex = 2;
		this.pot = 2 * ACGameInfo.ANTE;
		this.gainForFirstPlayer = ACGameInfo.ANTE;
		this.folded = false;
		this.sequenceForAllPlayers = new LinkedList<ACAction>();
	}

	public ACGameState(ACGameState gameState) {
		super(gameState);
		this.round = gameState.round;
		this.playerCards = gameState.playerCards;
		this.currentPlayerIndex = gameState.currentPlayerIndex;
		this.pot = gameState.pot;
		this.gainForFirstPlayer = gameState.gainForFirstPlayer;
		this.folded = gameState.folded;
		this.sequenceForAllPlayers = new LinkedList<ACAction>(gameState.sequenceForAllPlayers);
	}

	@Override
	public double getProbabilityOfNatureFor(Action action) {
		assert currentPlayerIndex == 2;
		return 1. / 3;
	}

    @Override
    public Rational getExactProbabilityOfNatureFor(Action action) {
        assert currentPlayerIndex == 2;
        return new Rational(1, 3);
    }

	@Override
	public ISKey getISKeyForPlayerToMove() {
		if (cachedISKey != null)
			return cachedISKey;
		if (isPlayerToMoveNature()) {
			cachedISKey = new PerfectRecallISKey(0, history.getSequenceOf(getPlayerToMove()));
			return cachedISKey;
		}

		HashCodeBuilder hcb = new HashCodeBuilder(17, 31);
		Iterator<ACAction> iterator = sequenceForAllPlayers.iterator();
		int moveNum = 0;

		hcb.append(playerCards[getPlayerToMove().getId()]);
		while (iterator.hasNext()) {
			hcb.append(iterator.next());
			hcb.append(moveNum++);
		}
		cachedISKey = new PerfectRecallISKey(hcb.toHashCode(), history.getSequenceOf(getPlayerToMove()));
		return cachedISKey;
	}

	@Override
	public Player getPlayerToMove() {
		return players[currentPlayerIndex];
	}

	@Override
	public GameState copy() {
		return new ACGameState(this);
	}

	@Override
	public double[] getUtilities() {
		if (!isGameEnd())
			return new double[] { 0 };
		if (folded)
			return new double[] { gainForFirstPlayer, -gainForFirstPlayer, 0 };
		if (playerCards[0].equals(table))
			return new double[] { gainForFirstPlayer, -gainForFirstPlayer, 0 };
		if (playerCards[1].equals(table))
			return new double[] { gainForFirstPlayer - pot, pot - gainForFirstPlayer, 0 };
		return new double[] { 0, 0, 0 };
	}

    @Override
    public Rational[] getExactUtilities() {
        if (!isGameEnd())
            return new Rational[] { Rational.ZERO };
        if (folded)
            return new Rational[] { new Rational(gainForFirstPlayer), new Rational(-gainForFirstPlayer), Rational.ZERO };
        if (playerCards[0].equals(table))
            return new Rational[] { new Rational(gainForFirstPlayer), new Rational(-gainForFirstPlayer), Rational.ZERO };
        if (playerCards[1].equals(table))
            return new Rational[] { new Rational(gainForFirstPlayer - pot), new Rational(pot - gainForFirstPlayer), Rational.ZERO };
        return new Rational[] { Rational.ZERO, Rational.ZERO, Rational.ZERO };
    }

	@Override
	public boolean isGameEnd() {
		return getStageOfGame().equals(StageOfGame.end);
	}

	@Override
	public boolean isPlayerToMoveNature() {
		return currentPlayerIndex == 2;
	}

	public void bet() {
		assert currentPlayerIndex == 0;
		pot += ACGameInfo.BET;
		currentPlayerIndex = 1 - currentPlayerIndex;
		round++;
		clearCache();
	}

	public void check() {
		assert currentPlayerIndex != 2;
		round++;
		if (getStageOfGame().equals(StageOfGame.end))
			currentPlayerIndex = 0;
		else
			currentPlayerIndex++;
		clearCache();
	}

	public void call() {
		assert currentPlayerIndex == 1;
		gainForFirstPlayer += ACGameInfo.BET;
		pot += ACGameInfo.BET;
		round++;
		if (getStageOfGame().equals(StageOfGame.end))
			currentPlayerIndex = 0;
		else
			currentPlayerIndex = 2;
		clearCache();
	}

	public void fold() {
		round = StageOfGame.end.ordinal();
		folded = true;
		currentPlayerIndex = 0;
		clearCache();
	}

	public void dealCard(ActionType card) {
		assert currentPlayerIndex == 2;
		clearCache();
		round++;
		if(playerCards[0] == null) {
			playerCards[0] = card;
			currentPlayerIndex = 2;
			return;
		}
		if(playerCards[1] == null) {
			playerCards[1] = card;
			currentPlayerIndex = 0;
			return;
		}
		table = card;
		currentPlayerIndex = 0;
	}

	private void clearCache() {
		cachedISKey = null;
	}

	public void addActionToSequence(ACAction action) {
		sequenceForAllPlayers.add(action);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + history.hashCode();
		result = prime * result + currentPlayerIndex;
		result = prime * result + (folded ? 1231 : 1237);
		result = prime * result + gainForFirstPlayer;
		result = prime * result + Arrays.hashCode(playerCards);
		result = prime * result + pot;
		result = prime * result + round;
		result = prime * result + ((sequenceForAllPlayers == null) ? 0 : sequenceForAllPlayers.hashCode());
		result = prime * result + ((table == null) ? 0 : table.hashCode());
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
		ACGameState other = (ACGameState) obj;
		if (!history.equals(other.history))
			return false;
		if (currentPlayerIndex != other.currentPlayerIndex)
			return false;
		if (folded != other.folded)
			return false;
		if (gainForFirstPlayer != other.gainForFirstPlayer)
			return false;
		if (!Arrays.equals(playerCards, other.playerCards))
			return false;
		if (pot != other.pot)
			return false;
		if (round != other.round)
			return false;
		if (sequenceForAllPlayers == null) {
			if (other.sequenceForAllPlayers != null)
				return false;
		} else if (!sequenceForAllPlayers.equals(other.sequenceForAllPlayers))
			return false;
		if (table != other.table)
			return false;
		return true;
	}

	@Override
	public String toString() {//nevad�j te� ty pln� hashCody?
		return history.toString();
	}

	public int getPot() {
		return pot;
	}

	public int getGainForFirstPlayer() {
		return gainForFirstPlayer;
	}

	public List<ACAction> getSequenceForAllPlayers() {
		return sequenceForAllPlayers;
	}

	public ActionType getTable() {
		return table;
	}

	public int getRound() {
		return round;
	}
	
	public StageOfGame getStageOfGame() {
		return StageOfGame.values()[round];
	}
}
