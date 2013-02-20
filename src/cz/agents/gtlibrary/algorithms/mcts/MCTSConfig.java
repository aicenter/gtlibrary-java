package cz.agents.gtlibrary.algorithms.mcts;

import cz.agents.gtlibrary.algorithms.mcts.backprop.BackPropagationStrategy;
import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.SelectionStrategy;
import cz.agents.gtlibrary.iinodes.ConfigImpl;
import cz.agents.gtlibrary.interfaces.Player;

public class MCTSConfig extends ConfigImpl<MCTSInformationSet> {
	
	private Simulator simulator;
	private BackPropagationStrategy.Factory backPropagationStrategyFactory;
	private SelectionStrategy selectionStrategy;

	public MCTSConfig(Simulator simulator, BackPropagationStrategy.Factory backPropagationStrategyFactory, SelectionStrategy selectionStrategy) {
		this.simulator = simulator;
		this.backPropagationStrategyFactory = backPropagationStrategyFactory;
		this.selectionStrategy = selectionStrategy;
	}
	
	public Simulator getSimulator() {
		return simulator;
	}
	
	public BackPropagationStrategy getBackPropagationStrategyFor(InnerNode node, Player player) {
		return backPropagationStrategyFactory.createForNode(node, player);
	}
	
	public SelectionStrategy getSelectionStrategy() {
		return selectionStrategy;
	}
	
	public BackPropagationStrategy.Factory getBackPropagationStrategyFactory() {
		return backPropagationStrategyFactory;
	}
}
