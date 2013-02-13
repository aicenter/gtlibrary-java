package cz.agents.gtlibrary.domain.poker.generic;

import cz.agents.gtlibrary.domain.poker.PokerAction;
import cz.agents.gtlibrary.domain.poker.PokerGameState;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;

public class GenericPokerGameState extends PokerGameState {

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
		return (double)(GPGameInfo.MAX_CARD_OF_EACH_TYPE - getOccurrenceCountOf(action))/(GPGameInfo.DECK.length - getDealtCardCount());
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

		if (action.equals(playerCards[0]))
			occurrenceCount++;
		if (action.equals(playerCards[1]))
			occurrenceCount++;
		if (action.equals(table))
			occurrenceCount++;
		return occurrenceCount;
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
		} else if(round == 2) {
			clearCachedValues();
			dealTableCard(action);
		}
	}

	private void dealTableCard(PokerAction action) {
		table = action;
		addActionToSequence(action);
		increaseRound();
		switchPlayers();
	}

}
