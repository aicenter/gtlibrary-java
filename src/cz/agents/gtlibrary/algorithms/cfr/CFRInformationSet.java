package cz.agents.gtlibrary.algorithms.cfr;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import cz.agents.gtlibrary.iinodes.InformationSetImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.utils.FixedSizeMap;

public abstract class CFRInformationSet extends InformationSetImpl {

	protected Map<GameState, Set<GameState>> successorLinks;
	protected Map<Action, Float> strategy;
	protected Map<Action, Float> averageStrategy;
	protected Map<Action, Float> regret;
	protected Collection<Action> actions;

	protected float valueOfGame;

	public CFRInformationSet(GameState state) {
		super(state);
		successorLinks = new HashMap<GameState, Set<GameState>>();
		valueOfGame = 0;
	}

	public void initializeFor(Collection<Action> actions, GameState state) {
		if (this.actions == null) {
			regret = new FixedSizeMap<Action, Float>(actions.size());
			this.actions = actions;
			initializeRegret(actions);
			initializeStrategies(state, actions);
		}
	}

	private void initializeRegret(Collection<Action> actions) {
		for (Action action : actions) {
			regret.put(action, 0f);
		}
	}

	private void initializeStrategies(GameState state, Collection<Action> actions) {
		strategy = new FixedSizeMap<Action, Float>(actions.size());
		averageStrategy = new FixedSizeMap<Action, Float>(actions.size());

		if (state.isPlayerToMoveNature()) {
			fillStrategies(state, actions);
		} else {
			fillStrategies(1. / actions.size(), actions);
		}
	}

	private void fillStrategies(GameState state, Collection<Action> actions) {
		for (Action action : actions) {
			strategy.put(action, (float) state.getProbabilityOfNatureFor(action));
			averageStrategy.put(action, (float) state.getProbabilityOfNatureFor(action));
		}
	}

	private void fillStrategies(double value, Collection<Action> actions) {
		for (Action action : actions) {
			strategy.put(action, (float) value);
			averageStrategy.put(action, (float) value);
		}
	}

	public void addSuccessor(GameState parent, GameState child) {
		Set<GameState> successors = successorLinks.get(parent);

		if (successors == null) {
			successors = new LinkedHashSet<GameState>();
			successors.add(child);
			successorLinks.put(parent, successors);
			return;
		}
		successors.add(child);
	}

	public Set<GameState> getSuccessorsFor(GameState gameState) {
		return successorLinks.get(gameState);
	}

	public Map<Action, Float> getStrategy() {
		return strategy;
	}

	public Map<Action, Float> getAverageStrategy() {
		return averageStrategy;
	}

	public Map<Action, Float> getRegret() {
		return regret;
	}

	public float getStrategyFor(Action action) {
		return strategy.get(action);
	}

	public void setStrategyFor(Action action, float strategy) {
		this.strategy.put(action, strategy);
	}

	public float getRegretFor(Action action) {
		return regret.get(action);
	}

	public float getAverageStrategyFor(Action action) {
		return averageStrategy.get(action);
	}

	public Collection<Action> getActions() {
		return actions;
	}

	public boolean isSetForNature() {
		return getPlayer().getId() == 2;
	}

	public Collection<GameState> getStates() {
		return successorLinks.keySet();
	}

	public void setValueOfGame(float valueOfGame) {
		this.valueOfGame = valueOfGame;
	}

	public float getValueOfGame() {
		return valueOfGame;
	}

	public void addToRegretFor(Action action, float regret) {
		this.regret.put(action, this.regret.get(action) + regret);
	}

	public Map<GameState, Set<GameState>> getSuccessors() {
		return successorLinks;
	}
}
