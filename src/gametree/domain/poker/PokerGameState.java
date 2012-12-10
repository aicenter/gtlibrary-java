package gametree.domain.poker;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import gametree.IINodes.HistoryImpl;
import gametree.IINodes.IIGameState;
import gametree.interfaces.Player;

public abstract class PokerGameState extends IIGameState {

	protected LinkedList<PokerAction> sequenceForAllPlayers;

	protected Player[] players;
	protected PokerAction[] playerCards;

	protected int round;
	protected int pot;
	protected int currentPlayerIndex;
	protected int gainForFirstPlayer;

	private double[] utilities;

	public PokerGameState(Player[] players, int ante) {
		super();
		this.players = players;
		this.playerCards = new PokerAction[2];
		this.sequenceForAllPlayers = new LinkedList<PokerAction>();
		this.history = new HistoryImpl(players);
		this.pot = 2 * ante;
		this.round = 0;
		this.gainForFirstPlayer = ante;
		this.currentPlayerIndex = 2;
	}

	@SuppressWarnings("unchecked")
	public PokerGameState(PokerGameState gameState) {
		super();
		this.history = gameState.getHistory().copy();
		this.sequenceForAllPlayers = (LinkedList<PokerAction>) gameState.sequenceForAllPlayers.clone();
		this.playerCards = gameState.playerCards.clone();
		this.players = gameState.players.clone();
		this.pot = gameState.pot;
		this.round = gameState.round;
		this.currentPlayerIndex = gameState.currentPlayerIndex;
		this.gainForFirstPlayer = gameState.gainForFirstPlayer;
	}

	@Override
	public Player[] getAllPlayers() {
		return players;
	}

	@Override
	public double[] getUtilities() {
		if (utilities != null) {
			return utilities;
		}
		if (isGameEnd()) {
			switch (hasPlayerOneWon()) {
			case 1:
				utilities = new double[] { gainForFirstPlayer, -gainForFirstPlayer, 0 };
				break;
			case 0:
				utilities = new double[] { 0, 0, 0 };
				break;
			case -1:
				utilities = new double[] { gainForFirstPlayer - pot, pot - gainForFirstPlayer, 0 };
				break;
			default:
				break;
			}
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
		return sequenceForAllPlayers.getLast().getAction().equals("b") || sequenceForAllPlayers.getLast().getAction().equals("r");
	}

	protected boolean isFirstPlayerAfterNature() {
		return sequenceForAllPlayers.getLast().getPlayer().equals(players[2]);
	}

	protected void addActionToSequence(PokerAction action) {
		utilities = null;
		sequenceForAllPlayers.add(action);
	}

	public boolean attendCard(PokerAction action) {
		if (round == 0) {
			dealCardToPlayer(action);
			return true;
		}
		return false;
	}

	public void check(PokerAction action) {
		if (isCheckValid()) {
			if (isLastMoveCheck()) {
				increaseRound();
			}
			addActionToSequence(action);
			switchPlayers();
		}
	}

	private boolean isLastMoveCheck() {
		return sequenceForAllPlayers.size() > 0 && sequenceForAllPlayers.getLast().getAction().equals("ch");
	}

	private boolean isCheckValid() {
		return isRoundForRegularPlayers() && (isFirstMoveInRound() || isLastMoveCheck());
	}

	public void bet(PokerAction action) {
		if (isBetOrCheckValid()) {
			addToPot(getValueOfBet(action));
			addActionToSequence(action);
			switchPlayers();
		}
	}

	protected abstract int getValueOfBet(PokerAction action);

	private boolean isBetOrCheckValid() {
		return (isFirstMoveInRound() || isLastMoveCheck()) && isRoundForRegularPlayers();
	}

	private boolean isRoundForRegularPlayers() {
		return round % 2 == 1;
	}

	private boolean isCallValid() {
		return isRoundForRegularPlayers() && isLastMoveAggressive();
	}

	private boolean isFoldValid() {
		return isRoundForRegularPlayers() && !sequenceForAllPlayers.isEmpty() && isLastMoveAggressive();
	}

	private void switchPlayers() {
		if (isFirstPlayerAfterNature()) {
			currentPlayerIndex = 0;
			return;
		}
		currentPlayerIndex = 1 - currentPlayerIndex;
	}

	public void call(PokerAction action) {
		if (isCallValid()) {
			addToPot(getValueOfCall(action));
			addActionToSequence(action);
			increaseRound();
			switchPlayers();
		}
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

	protected abstract int getValueOfCall(PokerAction action);

	public boolean fold(PokerAction action) {
		if (isFoldValid()) {
			round = getTerminalRound();
			addActionToSequence(action);
			switchPlayers();
			return true;
		}
		return false;
	}

	protected abstract int getTerminalRound();

	@Override
	public long getISEquivalenceFor(Player player) {
		HashCodeBuilder hcb = new HashCodeBuilder(17, 31);
		Iterator<PokerAction> iterator = sequenceForAllPlayers.iterator();
		int moveNum = 0;

		hcb.append(playerCards[player.getId()]);
		while (iterator.hasNext()) {
			hcb.append(iterator.next().observableISHash());
			hcb.append(moveNum++);
		}
		return hcb.toHashCode();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 31).append(history).toHashCode();
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
		if (!Arrays.equals(utilities, other.utilities))
			return false;
		return true;
	}

	/**
	 * 
	 * @return 1 if players[0] won, -1 if players[1] won, 0 if tie
	 */
	protected abstract int hasPlayerOneWon();

}
