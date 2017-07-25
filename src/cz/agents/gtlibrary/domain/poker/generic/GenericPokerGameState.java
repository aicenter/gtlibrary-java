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


package cz.agents.gtlibrary.domain.poker.generic;

import cz.agents.gtlibrary.algorithms.sequenceform.refinements.quasiperfect.numbers.Rational;
import cz.agents.gtlibrary.domain.poker.PokerAction;
import cz.agents.gtlibrary.domain.poker.PokerGameState;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;

public class GenericPokerGameState extends PokerGameState {

	private static final long serialVersionUID = -6924001547794664556L;
	
	private PokerAction table;
	private int continuousRaiseCount;

	public GenericPokerGameState() {
		super(GPGameInfo.ALL_PLAYERS, GPGameInfo.ANTE);
		continuousRaiseCount = 0;
	}

	public GenericPokerGameState(GenericPokerGameState gameState) {
		super(gameState);
		this.table = gameState.table;
		this.continuousRaiseCount = gameState.continuousRaiseCount;
	}

	@Override
	public GameState copy() {
		return new GenericPokerGameState(this);
	}

	@Override
	protected int hasPlayerOneWon() {
		if (sequenceForAllPlayers.getLast().getActionType().equals("f")) {
			return currentPlayerIndex == 1 ? -1 : 1;
		}
		if (playerCards[0].getActionType().equals(playerCards[1].getActionType())) {
			return 0;
		}
		if (playerCards[0].getActionType().equals(table.getActionType())) {
			return 1;
		}
		if (playerCards[1].getActionType().equals(table.getActionType())) {
			return -1;
		}
		if (compareCards(playerCards[0], playerCards[1]) > 0) {
			return 1;
		}
		return -1;
	}

	private int compareCards(PokerAction card1, PokerAction card2) {
		return Integer.parseInt(card1.getActionType()) - Integer.parseInt(card2.getActionType());
	}

	public PokerAction getTable() {
		return table;
	}

	public int getContinuousRaiseCount() {
		return continuousRaiseCount;
	}

	@Override
	public double getProbabilityOfNatureFor(Action action) {
		return (double) (getInitialCountOf(action) - getOccurrenceCountOf(action)) / (GPGameInfo.DECK.length - getDealtCardCount());
	}

    @Override
    public Rational getExactProbabilityOfNatureFor(Action action) {
        Rational exactProbability = new Rational(getInitialCountOf(action) - getOccurrenceCountOf(action), GPGameInfo.DECK.length - getDealtCardCount());

        assert Math.abs(exactProbability.doubleValue() - getProbabilityOfNatureFor(action)) < 1e-8;
        return exactProbability;
    }

	private int getInitialCountOf(Action action) {
		int count = 0;
		int actionValue = Integer.parseInt(((PokerAction)action).getActionType());
		
		for (Integer cardValue : GPGameInfo.DECK) {
			if(cardValue == actionValue)
				count++;
		}
		return count;
	}

	private int getDealtCardCount() {
		int cardCount = 0;

		if (playerCards[0] != null)
			cardCount++;
		if (playerCards[1] != null)
			cardCount++;
		if (table != null)
			cardCount++;
		return cardCount;
	}

	private int getOccurrenceCountOf(Action action) {
		int occurrenceCount = 0;

		if (areSame(playerCards[0], (PokerAction) action))
			occurrenceCount++;
		if (areSame(playerCards[1], (PokerAction) action))
			occurrenceCount++;
		if (areSame(table, (PokerAction) action))
			occurrenceCount++;
		return occurrenceCount;
	}

	private boolean areSame(PokerAction card1, PokerAction card2) {
		return card1 != null && card2 != null && card1.getActionType().equals(card2.getActionType());
	}

	@Override
	protected int getValueOfAggressive(PokerAction action) {
		return ((GenericPokerAction) action).getValue();
	}

	@Override
	protected int getValueOfCall() {
		return ((GenericPokerAction) sequenceForAllPlayers.getLast()).getValue();
	}

	@Override
	protected boolean isRaiseValid() {
		//System.out.println(super.isRaiseValid() && continuousRaiseCount < GPGameInfo.MAX_RAISES_IN_ROW);
		return super.isRaiseValid() && continuousRaiseCount < GPGameInfo.MAX_RAISES_IN_ROW;
	}

	@Override
	protected int getTerminalRound() {
		return 4;
	}

	@Override
	public void raise(PokerAction action) {
		if (isRaiseValid()) {
			clearCachedValues();
			continuousRaiseCount++;
			addToPot(getValueOfCall() + getValueOfAggressive(action));
			addActionToSequence(action);
			switchPlayers();
		}
	}

	public void call(PokerAction action) {
		if (isCallValid()) {
			clearCachedValues();
			continuousRaiseCount = 0;
			addToPot(getValueOfCall());
			addActionToSequence(action);
			increaseRound();
			switchPlayers();
		}
	}

	public void attendCard(PokerAction action) {
		if (round == 0) {
			clearCachedValues();
			dealCardToPlayer(action);
		} else if (round == 2) {
			clearCachedValues();
			dealTableCard(action);
		}
	}
	
//	@Override
//	protected void dealCardToPlayer(PokerAction action) {
////		if(playerCards[0] == null)
////			denominator *= GPGameInfo.MAX_CARD_OF_EACH_TYPE*GPGameInfo.MAX_CARD_TYPES;
////		else
////			denominator *= GPGameInfo.MAX_CARD_OF_EACH_TYPE*GPGameInfo.MAX_CARD_TYPES-1;
//		super.dealCardToPlayer(action);
//	}

	private void dealTableCard(PokerAction action) {
//		denominator *= GPGameInfo.MAX_CARD_TYPES*GPGameInfo.MAX_CARD_OF_EACH_TYPE - 2;
		table = action;
		addActionToSequence(action);
		increaseRound();
		switchPlayers();
	}
	
	private int getCardCount() {
		return GPGameInfo.MAX_CARD_TYPES*GPGameInfo.MAX_CARD_OF_EACH_TYPE;
	}
	
	private boolean isSpecificAction(PokerAction action){
		String actionType = action.getActionType();
		return actionType.equals("b") ||
				actionType.equals("ch");
		}
	
	public void reverseAction(){
		
		PokerAction lastAction = (PokerAction)history.getLastAction();
		if (history.getLastPlayer().getId()==2 || (!sequenceForAllPlayers.isEmpty() && lastAction.equals(sequenceForAllPlayers.getLast()))){
			if (lastAction.getActionType().equals("c")) {
				reverseCall();
			} else if (lastAction.getActionType().equals("f")) {
				reverseFold();
			} else if (lastAction.getActionType().equals("r")) {
				reverseRaise();
			} else if (! isSpecificAction(lastAction)){
				reverseAttend();
			}
		}
		super.reverseAction();
	}

	private void reverseCall() {
		continuousRaiseCount = countContinousRaiseCount();
	}

	private void reverseFold() {
		if(table==null)
			round=1;
		else
			round=3;
	}

	private void reverseRaise() {
		continuousRaiseCount--;
	}

	private void reverseAttend() {
		if (round == 3){
			table=null;
			round--;
		}
		else if (round == 1){
			playerCards[1]=null;
			round--;
		}
		else{
			playerCards[0]=null;
		}
	}
	
	private int countContinousRaiseCount(){
		int raiseCount = 0;
		for(int i = sequenceForAllPlayers.size()-2; i>=0; i--){
			if(sequenceForAllPlayers.get(i).getActionType()=="r")
				raiseCount++;
			else
				break;
		}
		return raiseCount;
	}

}
