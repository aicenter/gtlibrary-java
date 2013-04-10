package cz.agents.gtlibrary.domain.randomgame;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import cz.agents.gtlibrary.algorithms.rpoptimization.ActionComparator;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;

public class RPOptimizedRandomExpander<I extends InformationSet> extends RandomGameExpander<I> {
	
	private static final long serialVersionUID = 3616797799524952539L;
	
	private Map<Player, Map<Sequence, Double>> plans;

	public RPOptimizedRandomExpander(AlgorithmConfig<I> algConfig, Map<Player, Map<Sequence, Double>> plans) {
		super(algConfig);
		this.plans = plans;
	}

	@Override
	public List<Action> getActions(GameState gameState) {
		List<Action> actions = super.getActions(gameState);
		
		if (!gameState.isPlayerToMoveNature())
			Collections.sort(actions, new ActionComparator(plans.get(gameState.getPlayerToMove()), gameState));
		return actions;
	}

}
