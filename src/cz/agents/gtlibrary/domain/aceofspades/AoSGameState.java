package cz.agents.gtlibrary.domain.aceofspades;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import cz.agents.gtlibrary.iinodes.GameStateImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.Pair;

public class AoSGameState extends GameStateImpl {

	private static final long serialVersionUID = -6364948901237584991L;

	private NatureAoSAction card;
	private int currentPlayer;
	private boolean isGameEnd;

	private Pair<Integer, Sequence>[] playerISKeys;

	@SuppressWarnings("unchecked")
	public AoSGameState() {
		super(AoSGameInfo.ALL_PLAYERS);
		currentPlayer = 2;
		isGameEnd = false;
		playerISKeys = new Pair[2];
		for (int i = 0; i < 4; i++) {
			calculateISKeyFor(players[i % 2]);
		}
	}

	public AoSGameState(AoSGameState gameState) {
		super(gameState);
		this.currentPlayer = gameState.currentPlayer;
		this.card = gameState.card;
		this.isGameEnd = gameState.isGameEnd;
		this.playerISKeys = gameState.playerISKeys.clone();
	}

	@Override
	public double getProbabilityOfNatureFor(Action action) {
		if (!isPlayerToMoveNature())
			throw new UnsupportedOperationException("Nature is not on the move...");
		if (((NatureAoSAction) action).isAceOfSpades())
			return 1. / 52;
		return 51. / 52;

	}

	public void calculateISKeyFor(Player player) {
		playerISKeys[player.getId()] = new Pair<Integer, Sequence>(0, getSequenceFor(player));
	}

	@Override
	public Pair<Integer, Sequence> getISKeyForPlayerToMove() {
		if (isPlayerToMoveNature())
			return new Pair<Integer, Sequence>(0, getSequenceFor(players[2]));
		return playerISKeys[getPlayerToMove().getId()];
	}

	public int getHashOfRegPlayerSequences() {
		return new HashCodeBuilder().append(history.getSequenceOf(players[0])).append(history.getSequenceOf(players[1])).toHashCode();
	}

	@Override
	public Player getPlayerToMove() {
		return players[currentPlayer];
	}

	@Override
	public GameState copy() {
		return new AoSGameState(this);
	}

	@Override
	public double[] getUtilities() {
		if (!((FirstPlayerAoSAction) history.getSequenceOf(players[0]).getLast()).wantsToContinue())
			return new double[] { 0, 0, 0 };
		if (guessedCorrectly())
			return new double[] { -1, 1, 0 };
		return new double[] { 0, 0, 0 };
	}

	private boolean guessedCorrectly() {
		return card.isAceOfSpades() == ((SecondPlayerAoSAction) history.getSequenceOf(players[1]).getLast()).guessedAceOfSpades();
	}

	@Override
	public boolean isGameEnd() {
		return isGameEnd;
	}

	@Override
	public boolean isPlayerToMoveNature() {
		return currentPlayer == 2;
	}

	public void performFirstPlayerAction(FirstPlayerAoSAction action) {
		if (action.wantsToContinue())
			currentPlayer = 1;
		else
			isGameEnd = true;
		calculateISKeyFor(players[0]);
		calculateISKeyFor(players[1]);
	}

	public void performSecondPlayerAction(SecondPlayerAoSAction action) {
		isGameEnd = true;
		calculateISKeyFor(players[0]);
		calculateISKeyFor(players[1]);
	}

	public void performNatureAction(NatureAoSAction action) {
		card = action;
		currentPlayer = 0;
		calculateISKeyFor(players[0]);
		calculateISKeyFor(players[1]);
	}

	@Override
	public int hashCode() {
		return history.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AoSGameState other = (AoSGameState) obj;
		if (card == null) {
			if (other.card != null)
				return false;
		} else if (!card.equals(other.card))
			return false;
		if (history == null) {
			if (other.history != null)
				return false;
		} else if (!history.equals(other.history))
			return false;
		if (currentPlayer != other.currentPlayer)
			return false;
		if (isGameEnd != other.isGameEnd)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return history.toString();
	}

}
