package cz.agents.gtlibrary.algorithms.mcts;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cz.agents.gtlibrary.algorithms.mcts.backprop.BackPropagationStrategy;
import cz.agents.gtlibrary.algorithms.mcts.backprop.BackPropagationStrategy.Factory;
import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNode;
import cz.agents.gtlibrary.iinodes.InformationSetImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.utils.RunningStats;

public class MCTSInformationSet extends InformationSetImpl {
	
	private Set<InnerNode> allNodes;
	private Map<Action, BackPropagationStrategy> actionStats;
	private RunningStats[] informationSetStats;

	public MCTSInformationSet(GameState state) {
		super(state);
		allNodes = new HashSet<InnerNode>();
		informationSetStats = new RunningStats[state.getAllPlayers().length];
		for (int i = 0; i < state.getAllPlayers().length; i++) {
			informationSetStats[i] = new RunningStats();
		}
		actionStats = new LinkedHashMap<Action, BackPropagationStrategy>();
	}
	
	public void addNode(InnerNode node) {
		allNodes.add(node);
	}
	
	public Set<InnerNode> getAllNodes() {
		return allNodes;
	}

	public void addValuesToStats(double[] values) {
		for (int i = 0; i < values.length; i++) {
			informationSetStats[i].add(values[i]);
		}		
	}

	public void updateActionStatsFor(Action action, double[] values) {
		actionStats.get(action).onBackPropagate(values[player.getId()]);
	}

	public void initActionStats(List<Action> actions, Factory backPropagationStrategyFactory) {
		for (Action action : actions) {
			actionStats.put(action, backPropagationStrategyFactory.create());
		}
	}
	
	public RunningStats getStatsFor(int playerIndex) {
		return informationSetStats[playerIndex];
	}
	
	public Map<Action, BackPropagationStrategy> getActionStats() {
		return actionStats;
	}
}
