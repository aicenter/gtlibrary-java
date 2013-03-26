package cz.agents.gtlibrary.domain.simrandomgame;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import cz.agents.gtlibrary.iinodes.GameStateImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.Pair;

public class SimRandomGameState extends GameStateImpl {

	private int currentPlayerIndex;
	private SimRandomAction lastUnseenAction = null;
	private List<SimRandomAction> actions = null;
	private int ID;
	private double center;
	private double oldCenter;
	private int hashCode = -1;
	private Pair<Integer, Sequence> key;

	public SimRandomGameState() {
		super(SimRandomGameInfo.ALL_PLAYERS);
		actions = new ArrayList<SimRandomAction>();
		ID = new Random(SimRandomGameInfo.seed).nextInt();
		center = new Random(ID).nextDouble();
		oldCenter = center;
	}

	public SimRandomGameState(SimRandomGameState gameState) {
		super(gameState);
		this.currentPlayerIndex = gameState.currentPlayerIndex;
		this.lastUnseenAction = gameState.lastUnseenAction;
		this.actions = new ArrayList<SimRandomAction>(gameState.actions);
		this.center = gameState.oldCenter;
		updateCenter();
	}

	@Override
	public Player getPlayerToMove() {
		return players[currentPlayerIndex];
	}

	public void evaluate(SimRandomAction action) {
		cleanCache();
		if (lastUnseenAction != null) {
			addToActions(action);
			updateCenter();
		} else {
			lastUnseenAction = action;
		}
		switchPlayersAfter(action);
	}

	private void cleanCache() {
		hashCode = -1;
		key = null;
	}

	public void updateCenter() {
		ID = actions.hashCode();
		oldCenter = center;
		center = new Random(ID).nextGaussian() * oldCenter;
	}

	public void addToActions(SimRandomAction action) {
		if (lastUnseenAction.getPlayer().getId() == 0) {
			actions.add(lastUnseenAction);
			actions.add(action);
		} else {
			actions.add(action);
			actions.add(lastUnseenAction);
		}
		lastUnseenAction = null;
	}

	public void switchPlayersAfter(SimRandomAction action) {
		if (action.getPlayer().getId() == 0) {
			currentPlayerIndex = 1;
		} else {
			currentPlayerIndex = 0;
		}
	}

	@Override
	public boolean isPlayerToMoveNature() {
		return false;
	}

	@Override
	public double[] getUtilities() {
		if (!isGameEnd())
			return new double[] { 0, 0 };

		double rndValue = new Random(ID).nextGaussian() * center * SimRandomGameInfo.MAX_UTILITY;

		return new double[] { rndValue, -rndValue };
	}

	@Override
	public boolean isGameEnd() {
		return Math.min(history.getSequenceOf(players[0]).size(), history.getSequenceOf(players[1]).size()) == SimRandomGameInfo.MAX_DEPTH;
	}

	@Override
	public GameState copy() {
		return new SimRandomGameState(this);
	}

	@Override
	public int hashCode() {
		if (hashCode == -1)
			hashCode = new HashCodeBuilder(17, 31).append(history).append(ID).toHashCode();
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
		SimRandomGameState other = (SimRandomGameState) obj;

		if (ID != other.ID)
			return false;
		if (currentPlayerIndex != other.currentPlayerIndex)
			return false;
		if (!actions.equals(other.actions))
			return false;
		return true;
	}

	public List<SimRandomAction> getActions() {
		return actions;
	}

	@Override
	public double getProbabilityOfNatureFor(Action action) {
		return 1;
	}

	@Override
	public Pair<Integer, Sequence> getISKeyForPlayerToMove() {
		if(key == null)
			key = new Pair<Integer, Sequence>(actions.hashCode(), getSequenceForPlayerToMove());
		return key;
	}

	@Override
	public String toString() {
		return "ID: " + ID + ", " + history.toString();
	}
}
