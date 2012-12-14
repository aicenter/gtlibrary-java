package cz.agents.gtlibrary.cfr;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cz.agents.gtlibrary.iinodes.InformationSetImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.History;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.utils.FixedSizeMap;
import cz.agents.gtlibrary.utils.Triplet;


public abstract class CFRInformationSet extends InformationSetImpl {

	protected Map<History, Set<Triplet<Long, History, Player>>> successorLinks;
	protected Map<Action, Float> strategy;
	protected Map<Action, Float> averageStrategy;
	protected Map<Action, Float> regret;
	protected Collection<Action> actions;
	
	protected float valueOfGame;

	public CFRInformationSet(GameState state, List<Action> actions) {
		super(state);
		this.actions = actions;
		successorLinks = new HashMap<History, Set<Triplet<Long, History, Player>>>();
		regret = new FixedSizeMap<Action, Float>(actions.size());
		initializeRegret(actions);
		initializeStrategies(state, actions);
		valueOfGame = 0;
	}

	private void initializeRegret(List<Action> actions) {
		for (Action action : actions) {
			regret.put(action, 0f);
		}
	}

	private void initializeStrategies(GameState state, List<Action> actions) {
		strategy = new FixedSizeMap<Action, Float>(actions.size());
		averageStrategy = new FixedSizeMap<Action, Float>(actions.size());

		if (state.isPlayerToMoveNature()) {
			fillStrategies(state.getDistributionOfNature(), actions);
		} else {
			fillStrategies(1. / actions.size(), actions);
		}
	}

	private void fillStrategies(double[] values, List<Action> actions) {
		int index = 0;

		for (Action action : actions) {
			strategy.put(action, (float) values[index]);
			averageStrategy.put(action, (float) values[index]);
			index++;
		}
	}

	private void fillStrategies(double value, List<Action> actions) {
		for (Action action : actions) {
			strategy.put(action, (float) value);
			averageStrategy.put(action, (float) value);
		}
	}

	public void addSuccessor(GameState parent, GameState child) {
		Set<Triplet<Long, History, Player>> successors = successorLinks.get(parent.getHistory());

		if (successors == null) {
			successors = new LinkedHashSet<Triplet<Long, History, Player>>();
			successors.add(createTriplet(child));
			successorLinks.put(parent.getHistory(), successors);
			return;
		}
		successors.add(createTriplet(child));
	}

	private Triplet<Long, History, Player> createTriplet(GameState child) {
		return new Triplet<Long, History, Player>(child.getISEquivalenceForPlayerToMove(), child.getHistory(), child.getPlayerToMove());
	}

	public Set<Triplet<Long, History, Player>> getSuccessorsFor(History history) {
		return successorLinks.get(history);
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
	
	public Collection<History> getHistories() {
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
	
	public Map<History, Set<Triplet<Long, History, Player>>> getSuccessors() {
		return successorLinks;
	}
}
