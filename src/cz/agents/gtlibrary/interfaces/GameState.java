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


package cz.agents.gtlibrary.interfaces;

import java.io.Serializable;

import cz.agents.gtlibrary.algorithms.sequenceform.refinements.quasiperfect.numbers.Rational;
import cz.agents.gtlibrary.iinodes.GameStateImpl;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.utils.Pair;

public interface GameState extends Serializable {
	public Player[] getAllPlayers();

	public void transformInto(GameState gameState);

	public Player getPlayerToMove();

	public GameState performAction(Action action);

	public History getHistory();

	public void setHistory(History history);

	public Sequence getSequenceFor(Player player);

	public Sequence getSequenceForPlayerToMove();

	public GameState copy();

	public double[] getUtilities();

    public Rational[] getExactUtilities();

	public double getProbabilityOfNatureFor(Action action);

    public Rational getExactProbabilityOfNatureFor(Action action);

	public boolean isGameEnd();

	public boolean isPlayerToMoveNature();

	public double getNatureProbability();

    public Rational getExactNatureProbability();

	public void performActionModifyingThisState(Action action);

	public void reverseAction();

	public ISKey getISKeyForPlayerToMove();

	public boolean checkConsistency(Action action);

    public double[] evaluate();
}
