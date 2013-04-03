package cz.agents.gtlibrary.algorithms.mcts;

import cz.agents.gtlibrary.algorithms.mcts.backprop.BPStrategy;
import cz.agents.gtlibrary.algorithms.mcts.backprop.BackPropFactory;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNode;
import cz.agents.gtlibrary.iinodes.InformationSetImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;

public class MCTSInformationSet extends InformationSetImpl {

	private Set<InnerNode> allNodes;
	private Map<Action, BPStrategy> actionStats;
	private BPStrategy[] informationSetStats;

	public MCTSInformationSet(GameState state) {
		super(state);
		allNodes = new HashSet<InnerNode>();
		informationSetStats = new BPStrategy[state.getAllPlayers().length];
		actionStats = new LinkedHashMap<Action, BPStrategy>();
	}

	public void addNode(InnerNode node) {
		allNodes.add(node);
	}

	public Set<InnerNode> getAllNodes() {
		return allNodes;
	}

	public void addValuesToStats(double[] values) {
		for (int i = 0; i < values.length; i++) {
			informationSetStats[i].onBackPropagate(values[i]);
		}
	}

	public void updateActionStatsFor(Action action, double[] values) {
		actionStats.get(action).onBackPropagate(values[player.getId()]);
	}

	public void initStats(List<Action> actions, BackPropFactory backPropagationStrategyFactory) {
		if (actionStats.isEmpty()) {
			for (Action action : actions) {
				actionStats.put(action, backPropagationStrategyFactory.createForISAction(this, action));
			}
			for (int i = 0; i < informationSetStats.length; i++) {
				informationSetStats[i] = backPropagationStrategyFactory.createForIS(this);
			}
		}
	}

	public BPStrategy getStatsFor(int playerIndex) {
		return informationSetStats[playerIndex];
	}

	public Map<Action, BPStrategy> getActionStats() {
		return actionStats;
	}
}
