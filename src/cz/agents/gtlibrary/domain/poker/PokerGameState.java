package cz.agents.gtlibrary.domain.poker;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import cz.agents.gtlibrary.iinodes.GameStateImpl;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.Pair;

public abstract class PokerGameState extends GameStateImpl {

	private static final long serialVersionUID = 1768690890035774941L;

	protected LinkedList<PokerAction> sequenceForAllPlayers;

	protected PokerAction[] playerCards;
	protected Pair<Integer, Sequence> cachedISKey = null;

	protected int round;
	protected int pot;
	protected int currentPlayerIndex;
	protected int gainForFirstPlayer;
	protected int hash = -1;

	private double[] utilities;

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
	 * 
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
				utilities = new double[] { gainForFirstPlayer, -gainForFirstPlayer, 0 };
			else if (result == 0)
				utilities = new double[] { 0, 0, 0 };
			else
				utilities = new double[] { gainForFirstPlayer - pot, pot - gainForFirstPlayer, 0 };
			return utilities;
		}
		return new double[] { 0 };
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
		return sequenceForAllPlayers.isEmpty() || lastMovePlayedByNature();
	}

	protected boolean lastMovePlayedByNature() {
		return sequenceForAllPlayers.getLast().getPlayer().equals(players[2]);
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
		}
	}

	public void raise(PokerAction action) {
		if (isRaiseValid()) {
			clearCachedValues();
			addToPot(getValueOfCall() + getValueOfAggressive(action));
			addActionToSequence(action);
			switchPlayers();
		}
	}

	public PokerAction getCardForActingPlayer() {
		if (isPlayerToMoveNature())
			throw new UnsupportedOperationException("Nature doesn't hold any cards...");
		return playerCards[currentPlayerIndex];
	}

	@Override
	public boolean isGameEnd() {
		return round == getTerminalRound();
	}

	protected void addToPot(int bet) {
		pot += bet;
		if (currentPlayerIndex == 1) {
			gainForFirstPlayer += bet;
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
	public Pair<Integer, Sequence> getISKeyForPlayerToMove() {
		if (cachedISKey != null)
			return cachedISKey;
		if (isPlayerToMoveNature()) {
			cachedISKey = new Pair<Integer, Sequence>(0, history.getSequenceOf(getPlayerToMove()));
			return cachedISKey;
		}

		HashCodeBuilder hcb = new HashCodeBuilder(17, 31);
		Iterator<PokerAction> iterator = sequenceForAllPlayers.iterator();
		int moveNum = 0;

		hcb.append(playerCards[getPlayerToMove().getId()]);
		while (iterator.hasNext()) {
			hcb.append(iterator.next().observableISHash());
			hcb.append(moveNum++);
		}
		cachedISKey = new Pair<Integer, Sequence>(hcb.toHashCode(), history.getSequenceOf(getPlayerToMove()));
		return cachedISKey;
	}


	@Override
	public int hashCode() {
		if (hash == -1)
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

}
