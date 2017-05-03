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


package cz.agents.gtlibrary.domain.poker.kuhn;

import cz.agents.gtlibrary.algorithms.sequenceform.refinements.quasiperfect.numbers.Rational;
import cz.agents.gtlibrary.domain.poker.PokerAction;
import cz.agents.gtlibrary.domain.poker.PokerGameState;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;

public class KuhnPokerGameState extends PokerGameState {

    public KuhnPokerGameState() {
        super(new Player[]{KPGameInfo.FIRST_PLAYER, KPGameInfo.SECOND_PLAYER, KPGameInfo.NATURE}, KPGameInfo.ANTE);
    }

    public KuhnPokerGameState(KuhnPokerGameState gameState) {
        super(gameState);
    }

    @Override
    protected int hasPlayerOneWon() {
        if (sequenceForAllPlayers.getLast().getActionType().equals("f")) {
            return currentPlayerIndex == 1 ? -1 : 1;
        }
        return compareCards(playerCards[0], playerCards[1]);
    }

    private int compareCards(PokerAction fpCard, PokerAction spCard) {
        return Integer.parseInt(fpCard.getActionType()) - Integer.parseInt(spCard.getActionType());
    }

    @Override
    public GameState copy() {
        return new KuhnPokerGameState(this);
    }

    @Override
    protected int getValueOfAggressive(PokerAction action) {
        return KPGameInfo.BET;
    }

    @Override
    protected int getValueOfCall() {
        return KPGameInfo.BET;
    }

    @Override
    protected int getTerminalRound() {
        return 2;
    }

    @Override
    public double getProbabilityOfNatureFor(Action action) {
        if (!isPlayerToMoveNature()) {
            return 0;
        }
        if (playerCards[0] != null) {
            return 0.5;
        }
        return 1. / 3;
    }

    @Override
    public Rational getExactProbabilityOfNatureFor(Action action) {
        if (!isPlayerToMoveNature()) {
            return Rational.ZERO;
        }
        if (playerCards[0] != null) {
            return new Rational(1, 2);
        }
        return new Rational(1, 3);
    }

    @Override
    public void raise(PokerAction action) {
        throw new UnsupportedOperationException("Raise is not defined in Kuhn-Poker");
    }

    @Override
    public void attendCard(PokerAction action) {
        if (round == 0) {
            clearCachedValues();
            dealCardToPlayer(action);
        } else {
            throw new UnsupportedOperationException("invalid action");
        }
    }

    private boolean isSpecificKuhnPokerAction(PokerAction action) {
        String actionType = action.getActionType();
        return !(actionType.equals("b") || actionType.equals("c") ||
                actionType.equals("ch") || actionType.equals("r"));
    }

    @Override
    public void reverseAction() {
        PokerAction lastAction = (PokerAction) history.getLastAction();
        if (history.getLastPlayer().getId() == 2 || (!sequenceForAllPlayers.isEmpty() && lastAction.equals(sequenceForAllPlayers.getLast()))) {
            if (lastAction.getActionType().equals("f")) {
                reverseFold();
            } else if (isSpecificKuhnPokerAction(lastAction)) {
                if (round == 1) {
                    playerCards[1] = null;
                    round--;
                } else
                    playerCards[0] = null;
            }
        }

        super.reverseAction();
    }

    private void reverseFold() {
        round = 1;
    }

}
