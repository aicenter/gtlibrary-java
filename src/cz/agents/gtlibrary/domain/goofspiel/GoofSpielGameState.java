package cz.agents.gtlibrary.domain.goofspiel;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import cz.agents.gtlibrary.utils.FixedSizeMap;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import cz.agents.gtlibrary.iinodes.GameStateImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.Pair;

public class GoofSpielGameState extends GameStateImpl {

	private Map<Player, HashSet<Integer>> playerCards;
	private LinkedList<Action> sequenceForAllPlayers;
	private GoofSpielAction faceUpCard;

	private int[] playerScore;

	private int round;
	private int currentPlayerIndex;

	private Pair<Integer, Sequence> key;
	private int hashCode = -1;

	public GoofSpielGameState() {
		super(GSGameInfo.ALL_PLAYERS);
		sequenceForAllPlayers = new LinkedList<Action>();
		playerCards = new FixedSizeMap<Player, HashSet<Integer>>(3);
		playerScore = new int[2];
		round = 0;
		currentPlayerIndex = 2;

		createPlayerCards();
	}

	public GoofSpielGameState(GoofSpielGameState gameState) {
		super(gameState);
		this.round = gameState.round;
		this.currentPlayerIndex = gameState.currentPlayerIndex;
		this.playerScore = gameState.playerScore.clone();
		this.playerCards = getDeepCopyOfPlayerCards(gameState.playerCards);
		if (faceUpCard != null)
			this.faceUpCard = gameState.faceUpCard;
		this.sequenceForAllPlayers = new LinkedList<Action>(gameState.sequenceForAllPlayers);
	}

	private Map<Player, HashSet<Integer>> getDeepCopyOfPlayerCards(Map<Player, HashSet<Integer>> playerCards) {
		Map<Player, HashSet<Integer>> playerCardsCopy = new FixedSizeMap<Player, HashSet<Integer>>(3);

		for (Entry<Player, HashSet<Integer>> entry : playerCards.entrySet()) {
			playerCardsCopy.put(entry.getKey(), new HashSet<Integer>(entry.getValue()));
		}
		return playerCardsCopy;
	}

	private void createPlayerCards() {
		for (Player player : players) {
			HashSet<Integer> cardsForPlayer = new LinkedHashSet<Integer>();

			for (int cardValue : GSGameInfo.CARDS_FOR_PLAYER) {
				cardsForPlayer.add(cardValue);
			}
			playerCards.put(player, cardsForPlayer);
		}
	}

//
//	@SuppressWarnings("unchecked")
//	private void computeISKnowledge() {
//
//		if (knowledgeForPlayers == null) {
//			knowledgeForPlayers = new HashMap<Player, InformationSetKnowledge>();
//		} else {
//			knowledgeForPlayers.clear();
//		}
//
//		LinkedList<Action> sequenceForAllPlayersCopy = (LinkedList<Action>) sequenceForAllPlayers.clone();
//		HashCodeBuilder hcb = new HashCodeBuilder(17, 31);
//
//		hcb.append(sequenceForAllPlayers);
//		int hashCode = hcb.toHashCode();
//
//		knowledgeForPlayers.put(players[0], new GoofSpielInformationSetKnowledge(sequenceForAllPlayersCopy, hashCode));
//		knowledgeForPlayers.put(players[1], new GoofSpielInformationSetKnowledge(sequenceForAllPlayersCopy, hashCode));
//		knowledgeForPlayers.put(players[2], new NatureInformationSetKnowledge());
//	}

	@Override
	public Player getPlayerToMove() {
		return players[currentPlayerIndex];
	}

	private void addActionToSequenceForAllPlayers(GoofSpielAction action) {
		sequenceForAllPlayers.add(action);
	}

	public void performFirstPlayerAction(GoofSpielAction action) {
		cleanCache();
		evaluate(action, getLastActionOf(GSGameInfo.SECOND_PLAYER));
		playerCards.get(GSGameInfo.FIRST_PLAYER).remove(action.getValue());
	}

	public void performSecondPlayerAction(GoofSpielAction action) {
		cleanCache();
		evaluate(getLastActionOf(GSGameInfo.FIRST_PLAYER), action);
		playerCards.get(GSGameInfo.SECOND_PLAYER).remove(action.getValue());
	}

	public void performNatureAction(GoofSpielAction action) {
		cleanCache();
		playerCards.get(GSGameInfo.NATURE).remove(action.getValue());
		addActionToSequenceForAllPlayers(action);
		faceUpCard = action;
		currentPlayerIndex = 0;
	}

	private void cleanCache() {
		key = null;
		hashCode = -1;
	}

	private void evaluateRound(GoofSpielAction firstPlayerCard, GoofSpielAction secondPlayerCard) {
		if (firstPlayerCard.compareTo(secondPlayerCard) > 0) {
			playerScore[0] += getLastActionOf(GSGameInfo.NATURE).getValue();
		} else if (firstPlayerCard.compareTo(secondPlayerCard) < 0) {
			playerScore[1] += getLastActionOf(GSGameInfo.NATURE).getValue();
		}
		round++;
		if (isGameEnd())
			currentPlayerIndex = 0;
	}

	private GoofSpielAction getLastActionOf(Player player) {
		if (history.getSequenceOf(player).size() == 0) {
			return null;
		}
		return (GoofSpielAction) history.getSequenceOf(player).getLast();
	}

	private void evaluate(GoofSpielAction fpAction, GoofSpielAction spAction) {
		if (history.getSequenceOf(GSGameInfo.FIRST_PLAYER).size() == history.getSequenceOf(GSGameInfo.SECOND_PLAYER).size()) {
			endRound(fpAction, spAction);
		} else {
			switchPlayers();
		}
	}

	public void endRound(GoofSpielAction fpAction, GoofSpielAction spAction) {
		addActionToSequenceForAllPlayers(fpAction);
		addActionToSequenceForAllPlayers(spAction);
		currentPlayerIndex = 2;
		evaluateRound(fpAction, spAction);
	}

	public void switchPlayers() {
		if (history.getSequenceOf(GSGameInfo.FIRST_PLAYER).size() > history.getSequenceOf(GSGameInfo.SECOND_PLAYER).size())
			currentPlayerIndex = 1;
		else
			currentPlayerIndex = 0;
	}

	public GoofSpielAction getFaceUpCard() {
		return faceUpCard;
	}

	@Override
	public boolean isPlayerToMoveNature() {
		return currentPlayerIndex == 2;
	}

	@Override
	public double[] getUtilities() {
//		double drawValue = (playerScore[0] + playerScore[1]) / 2.;
//
//		return new double[] { playerScore[0] - drawValue, playerScore[1] - drawValue, 0 };
		if (playerScore[0] > playerScore[1])
			return new double[] { 1, -1, 0 };
		if (playerScore[0] < playerScore[1])
			return new double[] { -1, 1, 0 };
		return new double[] { 0, 0, 0 };
	}

	@Override
	public boolean isGameEnd() {
		return round == GSGameInfo.CARDS_FOR_PLAYER.length;
	}

	@Override
	public GameState copy() {
		return new GoofSpielGameState(this);
	}

	public HashSet<Integer> getCardsFor(Player actor) {
		return playerCards.get(actor);
	}

	public int getRound() {
		return round;
	}

	public LinkedList<Action> getSequenceForAllPlayers() {
		return sequenceForAllPlayers;
	}

	public int[] getPlayerScore() {
		return playerScore;
	}

	@Override
	public double getProbabilityOfNatureFor(Action action) {
		Set<Integer> natureActions = playerCards.get(GSGameInfo.NATURE);

		if (natureActions.contains(((GoofSpielAction) action).getValue()))
			return 1d / natureActions.size();
		return 0;
	}

	@Override
	public int hashCode() {
		if (hashCode == -1)
			hashCode = new HashCodeBuilder(17, 31).append(history).toHashCode();
		return hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GoofSpielGameState other = (GoofSpielGameState) obj;
		if (currentPlayerIndex != other.currentPlayerIndex)
			return false;
		if (faceUpCard == null) {
			if (other.faceUpCard != null)
				return false;
		} else if (!faceUpCard.equals(other.faceUpCard))
			return false;
		if (playerCards == null) {
			if (other.playerCards != null)
				return false;
		} else if (!playerCards.equals(other.playerCards))
			return false;
		if (!Arrays.equals(playerScore, other.playerScore))
			return false;
		if (!Arrays.equals(players, other.players))
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
	public Pair<Integer, Sequence> getISKeyForPlayerToMove() {
		if (key == null) {
			if (isPlayerToMoveNature())
				key = new Pair<Integer, Sequence>(0, history.getSequenceOf(getPlayerToMove()));
			else
				key = new Pair<Integer, Sequence>(sequenceForAllPlayers.hashCode(), getSequenceForPlayerToMove());
		}
		return key;
	}

	public Collection<Integer> getCardsForPlayerToMove() {
		return playerCards.get(getPlayerToMove());
	}

}
