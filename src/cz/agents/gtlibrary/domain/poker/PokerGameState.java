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


package cz.agents.gtlibrary.domain.poker;

import cz.agents.gtlibrary.algorithms.sequenceform.refinements.quasiperfect.numbers.Rational;
import cz.agents.gtlibrary.iinodes.ArrayListSequenceImpl;
import cz.agents.gtlibrary.iinodes.GameStateImpl;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.iinodes.PerfectRecallISKey;
import cz.agents.gtlibrary.interfaces.Player;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

public abstract class PokerGameState extends GameStateImpl {

    private static final long serialVersionUID = 1768690890035774941L;

    protected LinkedList<PokerAction> sequenceForAllPlayers;

    protected PokerAction[] playerCards;
    protected ISKey cachedISKey = null;

    protected int round;
    protected int pot;
    protected int currentPlayerIndex;
    protected int gainForFirstPlayer;
    protected int hash = -1;

    protected double[] utilities;

    private final double coef = 1;

    public PokerGameState(Player[] players, int ante) {
        super(players);
        this.playerCards = new PokerAction[2];
        this.sequenceForAllPlayers = new LinkedList<PokerAction>();
        this.pot = 2 * ante;
        this.round = 0;
        this.gainForFirstPlayer = ante;
        this.currentPlayerIndex = 2;
    }

    @SuppressWarnings("unchecked")
    public PokerGameState(PokerGameState gameState) {
        super(gameState);
        this.sequenceForAllPlayers = (LinkedList<PokerAction>) gameState.sequenceForAllPlayers.clone();
        this.playerCards = gameState.playerCards.clone();
        this.pot = gameState.pot;
        this.round = gameState.round;
        this.currentPlayerIndex = gameState.currentPlayerIndex;
        this.gainForFirstPlayer = gameState.gainForFirstPlayer;
    }


    protected abstract int getTerminalRound();

    /**
     * @return 1 if players[0] won, -1 if players[1] won, 0 if tie
     */
    protected abstract int hasPlayerOneWon();

    protected abstract int getValueOfAggressive(PokerAction action);

    protected abstract int getValueOfCall();

    public abstract void attendCard(PokerAction action);

    @Override
    public double[] getUtilities() {
        if (utilities != null) {
            return utilities;
        }
        if (isGameEnd()) {
            int result = hasPlayerOneWon();

            if (result > 0)
                utilities = new double[]{coef * gainForFirstPlayer, -coef * gainForFirstPlayer, 0};
            else if (result == 0)
                utilities = new double[]{0, 0, 0};
            else
                utilities = new double[]{coef * (gainForFirstPlayer - pot), coef * (pot - gainForFirstPlayer), 0};
            return utilities;
        }
        return new double[]{0};
    }

    @Override
    public Rational[] getExactUtilities() {
        if (isGameEnd()) {
            Rational[] exactUtilities;
            int result = hasPlayerOneWon();

            if (result > 0)
                exactUtilities = new Rational[]{new Rational(gainForFirstPlayer), new Rational(-gainForFirstPlayer), Rational.ZERO};
            else if (result == 0)
                exactUtilities = new Rational[]{Rational.ZERO, Rational.ZERO, Rational.ZERO};
            else
                exactUtilities = new Rational[]{new Rational(gainForFirstPlayer - pot), new Rational(pot - gainForFirstPlayer), Rational.ZERO};
            double[] utilities = getUtilities();

            for (int i = 0; i < exactUtilities.length; i++) {
                assert Math.abs(exactUtilities[i].doubleValue() - utilities[i]) < 1e-8;
            }
            return exactUtilities;
        }
        return new Rational[]{Rational.ZERO};
    }


    @Override
    public Player getPlayerToMove() {
        return players[currentPlayerIndex];
    }

    @Override
    public boolean isPlayerToMoveNature() {
        return currentPlayerIndex == 2;
    }

    protected void dealCardToPlayer(PokerAction action) {
        if (playerCards[0] == null) {
            playerCards[0] = action;
        } else {
            playerCards[1] = action;
            increaseRound();
            currentPlayerIndex = 0;
        }
    }

    protected void increaseRound() {
        round++;
    }

    public int getRound() {
        return round;
    }

    public PokerAction[] getPlayerCards() {
        return playerCards;
    }

    public int getPot() {
        return pot;
    }

    public int getGainForFirstPlayer() {
        return gainForFirstPlayer;
    }

    public LinkedList<PokerAction> getSequenceForAllPlayers() {
        return sequenceForAllPlayers;
    }

    protected boolean isFirstMoveInRound() {
        return sequenceForAllPlayers.isEmpty() || isFirstPlayerAfterNature();
    }

    protected boolean firstMoveOfPlayerToMove() {
        return sequenceForAllPlayers.size() <= 1 && !isPlayerToMoveNature();
    }

    protected boolean isLastMoveAggressive() {
        return sequenceForAllPlayers.getLast().getActionType().equals("b") || sequenceForAllPlayers.getLast().getActionType().equals("r");
    }

    protected boolean isFirstPlayerAfterNature() {
        return sequenceForAllPlayers.getLast().getPlayer().equals(players[2]);
    }

    protected void addActionToSequence(PokerAction action) {
        utilities = null;
        sequenceForAllPlayers.add(action);
    }

    protected void clearCachedValues() {
        hash = -1;
        cachedISKey = null;
    }

    public void check(PokerAction action) {
        if (isCheckValid()) {
            clearCachedValues();
            if (isLastMoveCheck()) {
                increaseRound();
            }
            addActionToSequence(action);
            switchPlayers();
        } else {
            throw new UnsupportedOperationException("invalid action");
        }
    }

    private boolean isLastMoveCheck() {
        return sequenceForAllPlayers.size() > 0 && sequenceForAllPlayers.getLast().getActionType().equals("ch");
    }

    private boolean isCheckValid() {
        return isRoundForRegularPlayers() && (isFirstMoveInRound() || isLastMoveCheck());
    }

    public void bet(PokerAction action) {
        if (isBetOrCheckValid()) {
            clearCachedValues();
            addToPot(getValueOfAggressive(action));
            addActionToSequence(action);
            switchPlayers();
        } else {
            throw new UnsupportedOperationException("invalid action");
        }
    }

    private boolean isBetOrCheckValid() {
        return (isFirstMoveInRound() || isLastMoveCheck()) && isRoundForRegularPlayers();
    }

    private boolean isRoundForRegularPlayers() {
        return round % 2 == 1;
    }

    protected boolean isCallValid() {
        return isRoundForRegularPlayers() && isLastMoveAggressive();
    }

    private boolean isFoldValid() {
        return isRoundForRegularPlayers() && !sequenceForAllPlayers.isEmpty() && isLastMoveAggressive();
    }

    protected boolean isRaiseValid() {
        //System.out.println(isRoundForRegularPlayers() && !sequenceForAllPlayers.isEmpty() && isLastMoveAggressive());
        return isRoundForRegularPlayers() && !sequenceForAllPlayers.isEmpty() && isLastMoveAggressive();
    }

    protected void switchPlayers() {
        if (round % 2 == 0 && round != getTerminalRound()) {
            currentPlayerIndex = 2;
            return;
        }
        if (isFirstPlayerAfterNature()) {
            currentPlayerIndex = 0;
            return;
        }
        currentPlayerIndex = 1 - currentPlayerIndex;
    }

    public void call(PokerAction action) {
        if (isCallValid()) {
            clearCachedValues();
            addToPot(getValueOfCall());
            addActionToSequence(action);
            increaseRound();
            switchPlayers();
        } else {
            throw new UnsupportedOperationException("invalid action");
        }
    }

    public void raise(PokerAction action) {
        if (isRaiseValid()) {
            clearCachedValues();
            addToPot(getValueOfCall() + getValueOfAggressive(action));
            addActionToSequence(action);
            switchPlayers();
        } else {
            throw new UnsupportedOperationException("invalid action");
        }
    }

    public PokerAction getCardForActingPlayer() {
        if (isPlayerToMoveNature())
            throw new UnsupportedOperationException("Nature doesn't hold any cards...");
        return playerCards[currentPlayerIndex];
    }

    public PokerAction getCardFor(Player player) {
        return playerCards[player.getId()];
    }

    @Override
    public boolean isGameEnd() {
        return round == getTerminalRound();
    }

    protected void addToPot(int bet) {
        //System.out.println("adding to pot : "+bet);
        pot += bet;
        if (currentPlayerIndex == 1) {
            gainForFirstPlayer += bet;
        }
    }

    protected void removeFromPot(int bet) {
        //System.out.println("removing from pot: "+bet);
        pot -= bet;
        if (history.getLastPlayer().getId() == 1) {
            gainForFirstPlayer -= bet;
        }
    }

    public void fold(PokerAction action) {
        if (isFoldValid()) {
            clearCachedValues();
            round = getTerminalRound();
            addActionToSequence(action);
            switchPlayers();
        }
    }

    @Override
    public ISKey getISKeyForPlayerToMove() {
        if (cachedISKey != null)
            return cachedISKey;
        if (isPlayerToMoveNature()) {
            cachedISKey = new PerfectRecallISKey(0, new ArrayListSequenceImpl(history.getSequenceOf(getPlayerToMove())));
            return cachedISKey;
        }

        HashCodeBuilder hcb = new HashCodeBuilder(17, 31);
        Iterator<PokerAction> iterator = sequenceForAllPlayers.iterator();
        int moveNum = 0;

        hcb.append(playerCards[getPlayerToMove().getId()].getActionType());
        while (iterator.hasNext()) {
            hcb.append(iterator.next().observableISHash());
            hcb.append(moveNum++);
        }
        cachedISKey = new PerfectRecallISKey(hcb.toHashCode(), history.getSequenceOf(getPlayerToMove()));
        return cachedISKey;
    }


    @Override
    public int hashCode() {
//        if (hash == -1)
            hash = new HashCodeBuilder(17, 31).append(history).toHashCode();
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PokerGameState other = (PokerGameState) obj;
        if (currentPlayerIndex != other.currentPlayerIndex)
            return false;
        if (gainForFirstPlayer != other.gainForFirstPlayer)
            return false;
        if (!Arrays.equals(playerCards, other.playerCards))
            return false;
        if (!Arrays.equals(players, other.players))
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
        return true;
    }

    @Override
    public String toString() {
        return history.toString();
    }

    @Override
    public void reverseAction() {

        PokerAction lastAction = (PokerAction) history.getLastAction();
        if (!sequenceForAllPlayers.isEmpty() && lastAction.equals(sequenceForAllPlayers.getLast())) {

            sequenceForAllPlayers.removeLast();

            if (lastAction.getActionType().equals("b")) {
                reverseBet(lastAction);
            } else if (lastAction.getActionType().equals("c")) {
                reverseCall();
            } else if (lastAction.getActionType().equals("ch")) {
                reverseCheck();
            } else if (lastAction.getActionType().equals("r")) {
                reverseRaise(lastAction);
            }
        }
        clearCachedValues();
        currentPlayerIndex = history.getLastPlayer().getId();
        super.reverseAction();
    }

    private void reverseRaise(PokerAction lastAction) {
        removeFromPot(getValueOfCall() + getValueOfAggressive(lastAction));
    }

    private void reverseCheck() {
        if (sequenceForAllPlayers.size() > 0 && sequenceForAllPlayers.get(sequenceForAllPlayers.size() - 1).getActionType().equals("ch"))
            round--;
    }

    private void reverseCall() {
        removeFromPot(getValueOfCall());
        round--;
    }

    private void reverseBet(PokerAction lastAction) {
        removeFromPot(getValueOfAggressive(lastAction));
    }


}
