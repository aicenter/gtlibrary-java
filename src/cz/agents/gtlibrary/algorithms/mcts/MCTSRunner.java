package cz.agents.gtlibrary.algorithms.mcts;

import java.util.Map;

import cz.agents.gtlibrary.algorithms.mcts.backprop.SampleWeightedBackPropStrategy;
import cz.agents.gtlibrary.algorithms.mcts.distribution.Distribution;
import cz.agents.gtlibrary.algorithms.mcts.distribution.FrequenceDistribution;
import cz.agents.gtlibrary.algorithms.mcts.nodes.ChanceNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.Node;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.UCTSelector;
import cz.agents.gtlibrary.domain.poker.kuhn.KPGameInfo;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerExpander;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerGameState;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;

public class MCTSRunner {
	
	private final int MCTS_ITERATIONS_PER_CALL = 1000;
	private final int SAME_STRATEGY_CHECK_COUNT = 50;

	private InnerNode rootNode;
	private MCTSConfig algConfig;
	private GameState gameState;
	private Expander<MCTSInformationSet> expander;
	
	public static void main(String[] args) {
		MCTSConfig firstMCTSConfig = new MCTSConfig(new Simulator(1), new SampleWeightedBackPropStrategy.Factory(), new UCTSelector(5));
		MCTSRunner runner = new MCTSRunner(firstMCTSConfig, new KuhnPokerGameState(), new KuhnPokerExpander<MCTSInformationSet>(firstMCTSConfig));
		Map<Sequence, Double> pureStrategy = runner.runMCTS(100000, KPGameInfo.FIRST_PLAYER, new FrequenceDistribution());
		
		System.out.println(pureStrategy);
	}

	public MCTSRunner(MCTSConfig algConfig, GameState gameState, Expander<MCTSInformationSet> expander) {
		this.algConfig = algConfig;
		this.gameState = gameState;
		this.expander = expander;
	}

	public Map<Sequence, Double> runMCTS(int iterations, Player player, Distribution distribution) {
		if (rootNode == null)
			rootNode = createRootNode(gameState, expander, algConfig);
		Node selectedLeaf = rootNode;

		for (int i = 0; i < iterations; i++) {
			selectedLeaf = rootNode.selectRecursively();
			selectedLeaf.expand();
			selectedLeaf.backPropagate(selectedLeaf.simulate());
		}
		Map<Sequence, Double> pureStrategy = rootNode.getStrategyFor(player, distribution);
		
		if(pureStrategy.containsKey(null))
			return null;
		return pureStrategy;
	}
	
	public Map<Sequence, Double> runMCTS(Player player, Distribution distribution) {
		Map<Sequence, Double> lastPureStrategy = null;
		Map<Sequence, Double> pureStrategy = null;
		int counter = 0;
		
		while (true) {
			pureStrategy = null;

			while (pureStrategy == null) {
				pureStrategy = runMCTS(MCTS_ITERATIONS_PER_CALL, player, distribution);
			}
			if (pureStrategy.equals(lastPureStrategy)) {
				counter++;
			} else {
				counter = 0;
			}
			if (counter == SAME_STRATEGY_CHECK_COUNT) {
				break;
			}
			lastPureStrategy = pureStrategy;
		}
		
		return pureStrategy;
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
