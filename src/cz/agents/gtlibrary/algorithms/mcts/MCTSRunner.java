package cz.agents.gtlibrary.algorithms.mcts;

import cz.agents.gtlibrary.algorithms.mcts.nodes.ChanceNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.Node;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.History;

public class MCTSRunner {

	private InnerNode rootNode;

	public MCTSRunner(MCTSConfig algConfig, GameState gameState, Expander<MCTSInformationSet> expander) {
		rootNode = createRootNode(gameState, expander, algConfig);
	}

	/**
	 * Runs given number of iterations of MCTS on tree held in this class
	 * @param iterations
	 * @return History of GameState associated with Node reached
	 */
	public History runMcts(int iterations) {
		Node selectedLeaf = rootNode;

		for (int i = 0; i < iterations; i++) {
			selectedLeaf = rootNode.selectRecursively();
			selectedLeaf.expand();
			selectedLeaf.backPropagate(selectedLeaf.simulate());
		}
		return selectedLeaf.getGameState().getHistory().copy();
	}

	@Deprecated()//nodes are locked during first call and never unlocked again
	public History runMctsWithIncreasingFixedDepth(int iterations) {
		Node selectedLeaf = rootNode;
		int depth = 0;
		int iterationsLeft = iterations;

		for (int i = 0; i < iterations; i++) {
			selectedLeaf = rootNode.selectRecursively(depth);

			if (i >= iterations - iterationsLeft / 3.) {
				iterationsLeft = iterations - i;
				if (selectedLeaf.getDepth() > depth + 1)
					depth++;
			}
			selectedLeaf.expand();
			selectedLeaf.backPropagate(selectedLeaf.simulate());
		}
		return selectedLeaf.getGameState().getHistory().copy();
	}

	protected InnerNode createRootNode(GameState gameState, Expander<MCTSInformationSet> expander, MCTSConfig algConfig) {
		if (gameState.isPlayerToMoveNature())
			return new ChanceNode(expander, algConfig, gameState);
		return new InnerNode(expander, algConfig, gameState);
	}
}
