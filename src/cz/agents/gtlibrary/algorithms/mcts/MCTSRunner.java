package cz.agents.gtlibrary.algorithms.mcts;

import cz.agents.gtlibrary.algorithms.mcts.distribution.Distribution;
import cz.agents.gtlibrary.algorithms.mcts.nodes.ChanceNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.Node;
import cz.agents.gtlibrary.iinodes.LinkedListSequenceImpl;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.strategy.Strategy;

public class MCTSRunner {

	private final int MCTS_ITERATIONS_PER_CALL = 1000;
	private final int SAME_STRATEGY_CHECK_COUNT = 20;

	private InnerNode rootNode;
	private MCTSConfig algConfig;
	private GameState gameState;
	private Expander<MCTSInformationSet> expander;

	public MCTSRunner(MCTSConfig algConfig, GameState gameState, Expander<MCTSInformationSet> expander) {
		this.algConfig = algConfig;
		this.gameState = gameState;
		this.expander = expander;
	}

	public Strategy runMCTS(int iterations, Player player, Distribution distribution) {
		if (rootNode == null)
			rootNode = createRootNode(gameState, expander, algConfig);
		Node selectedLeaf = rootNode;

		for (int i = 0; i < iterations; i++) {
			selectedLeaf = rootNode.selectRecursively();
			selectedLeaf.expand();
			selectedLeaf.backPropagate(selectedLeaf.simulate());
		}
		Strategy strategy = rootNode.getStrategyFor(player, distribution);
		
		strategy.put(new LinkedListSequenceImpl(player), 1d);
		return strategy;
	}
        
        public Strategy getCurrentStrategyFor(Player player, Distribution distribution){
                Strategy strategy = rootNode.getStrategyFor(player, distribution);
		
		strategy.put(new LinkedListSequenceImpl(player), 1d);
		return strategy;
        }

        public double[] getEV(){
            return rootNode.getEV();
        }

	public Strategy runMCTS(Player player, Distribution distribution) {
		Strategy lastPureStrategy = null;
		Strategy strategy = null;
		int counter = 0;
 
		while (true) {
			strategy = runMCTS(MCTS_ITERATIONS_PER_CALL, player, distribution);
			if (lastPureStrategy != null && strategy.maxDifferenceFrom(lastPureStrategy) < 0.001) {
//			if(strategy.equals(lastPureStrategy)) {
				counter++;
			} else {
				counter = 0;
			}
			if (counter == SAME_STRATEGY_CHECK_COUNT) {
				break;
			}
			lastPureStrategy = strategy;
		}
		return strategy;
	}

//
//	/**
//	 * Runs MCTS until given +- epsilon is reached
//	 * 
//	 * @param player
//	 * @param value
//	 * @param epsilon
//	 * @return
//	 */
//	public Map<Sequence, Double> runMcts(Player player, double value, double epsilon) {
//		if (rootNode == null)
//			rootNode = createRootNode(gameState, expander, algConfig);
//		Node selectedLeaf = rootNode;
//		int iterationCount = 0;
//
//		while (Math.abs(rootNode.getEV()[player.getId()] - value) > epsilon) {
//			selectedLeaf = rootNode.selectRecursively();
//			selectedLeaf.expand();
//			selectedLeaf.backPropagate(selectedLeaf.simulate());
//			iterationCount++;
//		}
//		System.out.println("Iterations of MCTS: " + iterationCount);
//		return rootNode.getPureStrategyFor(player);
//	}
//
//	public Map<Sequence, Double> runMctsWithIncreasingFixedDepth(int iterations, Player player) {
//		if (rootNode == null)
//			rootNode = createRootNode(gameState, expander, algConfig);
//		Node selectedLeaf = rootNode;
//		int depth = 0;
//		int iterationsLeft = iterations;
//
//		for (int i = 0; i < iterations; i++) {
//			selectedLeaf = rootNode.selectRecursively(depth);
//
//			if (i >= iterations - iterationsLeft / 3.) {
//				iterationsLeft = iterations - i;
//				if (selectedLeaf.getDepth() > depth + 1)
//					depth++;
//			}
//			selectedLeaf.expand();
//			selectedLeaf.backPropagate(selectedLeaf.simulate());
//		}
//		System.out.println("Expected value: " + Arrays.toString(rootNode.getEV()));
//		return rootNode.getPureStrategyFor(player);
//	}

	protected InnerNode createRootNode(GameState gameState, Expander<MCTSInformationSet> expander, MCTSConfig algConfig) {
		if (gameState.isPlayerToMoveNature())
			return new ChanceNode(expander, algConfig, gameState);
		return new InnerNode(expander, algConfig, gameState);
	}
}
