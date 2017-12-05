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

import cz.agents.gtlibrary.algorithms.sequenceform.refinements.quasiperfect.numbers.Rational;
import cz.agents.gtlibrary.interfaces.*;

public abstract class GameStateImpl implements GameState {

    private static final long serialVersionUID = 7693972683606265891L;

    protected Player[] players;
    protected History history;
    protected double natureProbability;
    protected Rational exactNatureProbability;

    public GameStateImpl(Player[] players) {
        this.history = new HistoryImpl(players);
        this.natureProbability = 1;
        this.exactNatureProbability = Rational.ONE;
        this.players = players;
    }

    public GameStateImpl(GameStateImpl gameState) {
        this.history = gameState.getHistory().copy();
        this.natureProbability = gameState.getNatureProbability();
        this.exactNatureProbability = gameState.exactNatureProbability;
        this.players = gameState.getAllPlayers();
    }

    @Override
    public void transformInto(GameState gameState) {
        this.history = gameState.getHistory().copy();
        this.natureProbability = gameState.getNatureProbability();
        this.exactNatureProbability = (gameState instanceof GameStateImpl) ?
            new Rational(1,1) : ((GameStateImpl)gameState).exactNatureProbability;
        this.players = gameState.getAllPlayers();
    }

    @Override
    public abstract Player getPlayerToMove();

    @Override
    public GameState performAction(Action action) {
        GameState state = this.copy();

        state.performActionModifyingThisState(action);
        return state;
    }

    @Override
    public void performActionModifyingThisState(Action action) {
        if (isPlayerToMoveNature() || checkConsistency(action)) {
            updateNatureProbabilityFor(action);
            addActionToHistory(action, getPlayerToMove());
            action.perform(this);
        } else {
            System.out.println(this.toString());
            System.out.println(this.getISKeyForPlayerToMove());
            System.out.println(action.getInformationSet());
            System.out.println(action.getInformationSet().getISKey());
            throw new IllegalStateException("Inconsistent move.");
        }
    }

    private void updateNatureProbabilityFor(Action action) {
        if (isPlayerToMoveNature()) {
            natureProbability *= getProbabilityOfNatureFor(action);
            exactNatureProbability = exactNatureProbability.multiply(getExactProbabilityOfNatureFor(action));
        }
    }

    private void addActionToHistory(Action action, Player playerToMove) {
        history.addActionOf(action, playerToMove);
    }

    @Override
    public boolean checkConsistency(Action action) {
        if (action == null || action.getInformationSet() == null)
            return false;
        return this.getISKeyForPlayerToMove().equals(action.getInformationSet().getISKey());
//		return action.getInformationSet().getAllStates().contains(this);
    }

    @Override
    public double[] evaluate() {
        throw new UnsupportedOperationException("Evaluation function not implemented for this domain...");
    }

    @Override
    public Rational[] getExactUtilities() {
        throw new UnsupportedOperationException("Exact utility not implemented for this domain...");
    }

    @Override
    public Rational getExactProbabilityOfNatureFor(Action action) {
        throw new UnsupportedOperationException("Exact probability for action not implemented for this domain...");
    }

    @Override
    public History getHistory() {
        return history;
    }

    @Override
    public void setHistory(History history){ this.history = history; }

    @Override
    public Player[] getAllPlayers() {
        return players;
    }

    @Override
    public Sequence getSequenceFor(Player player) {
        return history.getSequenceOf(player);
    }

    @Override
    public Sequence getSequenceForPlayerToMove() {
        return history.getSequenceOf(getPlayerToMove());
    }

    @Override
    public double getNatureProbability() {
        return natureProbability;
    }

    @Override
    public Rational getExactNatureProbability() {
        return exactNatureProbability;
    }

    @Override
    public abstract GameState copy();

    @Override
    public abstract double[] getUtilities();

    @Override
    public abstract boolean isGameEnd();

    @Override
    public abstract boolean isPlayerToMoveNature();

    @Override
    public void reverseAction() {
        Action lastAction;
        if (history.getLastPlayer().getId() == 2) {
            lastAction = history.getLastAction();
            natureProbability /= getProbabilityOfNatureFor(lastAction);
            exactNatureProbability = exactNatureProbability.divide(getExactProbabilityOfNatureFor(lastAction));
        }
        history.reverse();
    }

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object object);
}
