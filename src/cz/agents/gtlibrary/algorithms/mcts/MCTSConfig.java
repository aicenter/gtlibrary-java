package cz.agents.gtlibrary.algorithms.mcts;

import cz.agents.gtlibrary.algorithms.mcts.backprop.BackPropagationStrategy;
import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.SelectionStrategy;
import cz.agents.gtlibrary.iinodes.ConfigImpl;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.strategy.Strategy;

public class MCTSConfig extends ConfigImpl<MCTSInformationSet> {
	
	private Simulator simulator;
	private BackPropagationStrategy.Factory backPropagationStrategyFactory;
	private Strategy.Factory strategyFactory;
	private SelectionStrategy selectionStrategy;

	public MCTSConfig(Simulator simulator, BackPropagationStrategy.Factory backPropagationStrategyFactory,
			Strategy.Factory strategyFactory, SelectionStrategy selectionStrategy) {
		this.simulator = simulator;
		this.backPropagationStrategyFactory = backPropagationStrategyFactory;
		this.selectionStrategy = selectionStrategy;
		this.strategyFactory = strategyFactory;
	}
	
	public Simulator getSimulator() {
		return simulator;
	}
	
	public BackPropagationStrategy getBackPropagationStrategyFor(InnerNode node, Player player) {
		return backPropagationStrategyFactory.createForNode(node, player);
	}
	
	public Strategy getEmptyStrategy() {
		return strategyFactory.create();
	}
	
	public SelectionStrategy getSelectionStrategy() {
		return selectionStrategy;
	}
	
	public BackPropagationStrategy.Factory getBackPropagationStrategyFactory() {
		return backPropagationStrategyFactory;
	}

	@Override
	public MCTSInformationSet createInformationSetFor(GameState gameState) {
		return new MCTSInformationSet(gameState);
	}
}
