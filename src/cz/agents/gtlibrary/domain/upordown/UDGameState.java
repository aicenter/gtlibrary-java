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


package cz.agents.gtlibrary.domain.upordown;

import cz.agents.gtlibrary.algorithms.sequenceform.refinements.quasiperfect.numbers.Rational;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.iinodes.PerfectRecallISKey;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import cz.agents.gtlibrary.iinodes.GameStateImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.Pair;

public class UDGameState extends GameStateImpl {

	private static final long serialVersionUID = -8808441099769774667L;
	private UDAction p1Action;
	private UDAction p2Action;

	public UDGameState() {
		super(UDGameInfo.ALL_PLAYERS);
	}

	public UDGameState(UDGameState udGameState) {
		super(udGameState);
		this.p1Action = udGameState.p1Action;
		this.p2Action = udGameState.p2Action;
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
		HashCodeBuilder builder = new HashCodeBuilder(17, 31);
		
		builder.append(p1Action);
		builder.append(p2Action);
		return new PerfectRecallISKey(builder.toHashCode(), getSequenceForPlayerToMove());
	}

	@Override
	public Player getPlayerToMove() {
		if (p1Action == null)
			return players[0];
		if (p2Action == null)
			return players[1];
		return players[0];
	}

	@Override
	public GameState copy() {
		return new UDGameState(this);
	}

	@Override
	public double[] getUtilities() {
		if (p1Action.getType().equals("U")) {
			if (p2Action.getType().equals("l"))
				return new double[] { 0, 0 };
			if (p2Action.getType().equals("r"))
				return new double[] { 1, -1 };
			throw new UnsupportedOperationException();
		} else if (p1Action.getType().equals("D")) {
			if (p2Action.getType().equals("l'"))
				return new double[] { 0, 0 };
			if (p2Action.getType().equals("r'"))
				return new double[] { 2, -2 };
			throw new UnsupportedOperationException();
		}
		throw new UnsupportedOperationException();
	}

    @Override
    public Rational[] getExactUtilities() {
        if (p1Action.getType().equals("U")) {
            if (p2Action.getType().equals("l"))
                return new Rational[] { Rational.ZERO, Rational.ZERO };
            if (p2Action.getType().equals("r"))
                return new Rational[] { Rational.ONE, Rational.ONE.negate() };
            throw new UnsupportedOperationException();
        } else if (p1Action.getType().equals("D")) {
            if (p2Action.getType().equals("l'"))
                return new Rational[] { Rational.ZERO, Rational.ZERO };
            if (p2Action.getType().equals("r'"))
                return new Rational[] { new Rational(2), new Rational(-2) };
            throw new UnsupportedOperationException();
        }
        throw new UnsupportedOperationException();
    }

	@Override
	public boolean isGameEnd() {
		return p1Action != null && p2Action != null;
	}

	@Override
	public boolean isPlayerToMoveNature() {
		return false;
	}

	public void setP1Action(UDAction p1Action) {
		this.p1Action = p1Action;
	}

	public void setP2Action(UDAction p2Action) {
		this.p2Action = p2Action;
	}
	
	public UDAction getP1Action() {
		return p1Action;
	}
	
	public UDAction getP2Action() {
		return p2Action;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((p1Action == null) ? 0 : p1Action.hashCode());
		result = prime * result + ((p2Action == null) ? 0 : p2Action.hashCode());
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
		UDGameState other = (UDGameState) obj;
		if (history == null) {
			if (other.history != null)
				return false;
		} else if (!history.equals(other.history))
			return false;
		if (p1Action == null) {
			if (other.p1Action != null)
				return false;
		} else if (!p1Action.equals(other.p1Action))
			return false;
		if (p2Action == null) {
			if (other.p2Action != null)
				return false;
		} else if (!p2Action.equals(other.p2Action))
			return false;
		return true;
	}

}
