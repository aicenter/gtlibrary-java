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


package cz.agents.gtlibrary.domain.nfptest;

import cz.agents.gtlibrary.algorithms.sequenceform.refinements.quasiperfect.numbers.Rational;
import cz.agents.gtlibrary.iinodes.GameStateImpl;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.iinodes.PerfectRecallISKey;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.Pair;

public class TestGameState extends GameStateImpl {

	private static final long serialVersionUID = 2837241225140778346L;

	private int round;

	public TestGameState() {
		super(TestGameInfo.ALL_PLAYERS);
		round = 0;
    }

	public TestGameState(TestGameState gameState) {
		super(gameState);
		this.round = gameState.round;
	}

	public void performP1Action(P1TestAction action) {
		if (action.getActionType().equals("D"))
			round = 3;
		else
			round++;
	}

	public void performP2Action(P2TestAction action) {
		round++;
	}

	public P2TestAction getLastActionOfP2() {
		if (getSequenceFor(players[1]).size() == 0)
			return null;
		return (P2TestAction) getSequenceFor(players[1]).getLast();
	}

	public P1TestAction getLastActionOfP1() {
		if (getSequenceFor(players[0]).size() == 0)
			return null;
		return (P1TestAction) getSequenceFor(players[0]).getLast();
	}

	@Override
	public double getProbabilityOfNatureFor(Action action) {
		return 0;
	}

    @Override
    public Rational getExactProbabilityOfNatureFor(Action action) {
        return Rational.ZERO;
    }

	@Override
	public ISKey getISKeyForPlayerToMove() {
		return new PerfectRecallISKey(history.hashCode(), getSequenceForPlayerToMove());
	}

	@Override
	public Player getPlayerToMove() {
		return players[round % 2];
	}

	@Override
	public GameState copy() {
		return new TestGameState(this);
	}

	@Override
	public double[] getUtilities() {
		if (!isGameEnd())
			return new double[] { 0 };
		if (getLastActionOfP1().getActionType().equals("L"))
			return new double[] { -2, 2 };
		if (getLastActionOfP1().getActionType().equals("R"))
			return new double[] { -1, 1 };
		if (getLastActionOfP1().getActionType().equals("L'"))
			return new double[] { 1, -1 };
		if (getLastActionOfP1().getActionType().equals("R'"))
			return new double[] { 2, -2 };
		return new double[] { -1, 1 };
	}

    @Override
    public Rational[] getExactUtilities() {
        if (!isGameEnd())
            return new Rational[] { Rational.ZERO };
        if (getLastActionOfP1().getActionType().equals("L"))
            return new Rational[] { new Rational(-2), new Rational(2) };
        if (getLastActionOfP1().getActionType().equals("R"))
            return new Rational[] { new Rational(-1), Rational.ONE };
        if (getLastActionOfP1().getActionType().equals("L'"))
            return new Rational[] { Rational.ONE, new Rational(-1) };
        if (getLastActionOfP1().getActionType().equals("R'"))
            return new Rational[] { new Rational(2), new Rational(-2) };
        return new Rational[] { new Rational(-1), Rational.ONE };
    }

	@Override
	public boolean isGameEnd() {
		return round == 3;
	}

	@Override
	public boolean isPlayerToMoveNature() {
		return false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;

		result = prime * result + round;
		result = prime * result + history.hashCode();
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
		TestGameState other = (TestGameState) obj;

		if (round != other.round)
			return false;
		if (!history.equals(other.history))
			return false;
		return true;
	}

}
