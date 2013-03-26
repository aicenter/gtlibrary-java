package cz.agents.gtlibrary.algorithms.mcts;

import cz.agents.gtlibrary.algorithms.mcts.backprop.BackPropFactory;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.SelectionStrategy;
import cz.agents.gtlibrary.iinodes.ConfigImpl;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.strategy.Strategy;

public class MCTSConfig extends ConfigImpl<MCTSInformationSet> {
	
	private Simulator simulator;
	private BackPropFactory backPropagationStrategyFactory;
	private Strategy.Factory strategyFactory;
	private SelectionStrategy selectionStrategy;

	public MCTSConfig(Simulator simulator, BackPropFactory backPropagationStrategyFactory,
			Strategy.Factory strategyFactory, SelectionStrategy selectionStrategy) {
		this.simulator = simulator;
		this.backPropagationStrategyFactory = backPropagationStrategyFactory;
		this.selectionStrategy = selectionStrategy;
		this.strategyFactory = strategyFactory;
	}
	
	public Simulator getSimulator() {
		return simulator;
	}
	
	public Strategy getEmptyStrategy() {
		return strategyFactory.create();
	}
	
	public SelectionStrategy getSelectionStrategy() {
		return selectionStrategy;
	}
	
	public BackPropFactory getBackPropagationStrategyFactory() {
		return backPropagationStrategyFactory;
	}

	@Override
	public MCTSInformationSet createInformationSetFor(GameState gameState) {
		return new MCTSInformationSet(gameState);
	}
}
