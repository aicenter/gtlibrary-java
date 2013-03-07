package cz.agents.gtlibrary.algorithms.mcts;

import java.util.Map;

import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.br.BRChanceNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.br.BRInnerNode;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;

public class BestResponseMCTSRunner extends MCTSRunner {
	
	private Map<Sequence, Double> opponentRealizationPlan;
	private Player opponent;
	
	public BestResponseMCTSRunner(MCTSConfig algConfig, GameState gameState, Expander<MCTSInformationSet> expander,
			Map<Sequence, Double> opponentRealizationPlan, Player opponent) {
		super(algConfig, gameState, expander);
		this.opponentRealizationPlan = opponentRealizationPlan;
		this.opponent = opponent;
	}
	
	@Override
	protected InnerNode createRootNode(GameState gameState, Expander<MCTSInformationSet> expander, MCTSConfig algConfig) {
		if (gameState.isPlayerToMoveNature())
			return new BRChanceNode(gameState, expander, algConfig, opponentRealizationPlan, opponent, 1);
		return new BRInnerNode(gameState, expander, algConfig, opponentRealizationPlan, opponent, 1);
	}
}

