/*
Copyright 2014 Faculty of Electrical Engineering at CTU in Prague

This file is part of Game Theoretic Library.

Game Theoretic Library is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General  License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Game Theoretic Library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General  License for more details.

You should have received a copy of the GNU Lesser General  License
along with Game Theoretic Library.  If not, see <http://www.gnu.org/licenses/>.*/


package cz.agents.gtlibrary.interfaces;

import cz.agents.gtlibrary.algorithms.sequenceform.refinements.quasiperfect.numbers.Rational;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.iinodes.PSKey;

import java.io.Serializable;

public interface GameState extends Serializable {
    Player[] getAllPlayers();

    Player getPlayerToMove();

    GameState performAction(Action action);

    History getHistory();

    Sequence getSequenceFor(Player player);

    Sequence getSequenceForPlayerToMove();

    GameState copy();

    double[] getUtilities();

    Rational[] getExactUtilities();

    double getProbabilityOfNatureFor(Action action);

    Rational getExactProbabilityOfNatureFor(Action action);

    boolean isGameEnd();

    boolean isPlayerToMoveNature();

    double getNatureProbability();

    Rational getExactNatureProbability();

    void performActionModifyingThisState(Action action);

    void reverseAction();

    ISKey getISKeyForPlayerToMove();

    boolean checkConsistency(Action action);

    double[] evaluate();

    default Player getOpponentPlayerToMove() {
        Player pl = getPlayerToMove();
        if (pl.getId() == 2) {
            throw new RuntimeException("Chance does not have opponent player!");
        }
        return getAllPlayers()[1 - pl.getId()];
    }

    // Note that there exists "DomainWithState" which has these as well
    // DomainWithState means the domain can generate it's  state without building the whole
    // game tree and using cz.agents.gtlibrary.algorithms.cr.TreeGenerator
    PSKey getPSKeyForPlayerToMove();

    void setPSKeyForPlayerToMove(PSKey psKey);
}
