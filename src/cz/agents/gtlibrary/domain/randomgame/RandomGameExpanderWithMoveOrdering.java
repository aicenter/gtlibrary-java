package cz.agents.gtlibrary.domain.randomgame;

import cz.agents.gtlibrary.experimental.rpoptimization.ActionComparator;
import cz.agents.gtlibrary.interfaces.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class RandomGameExpanderWithMoveOrdering<I extends InformationSet> extends RandomGameExpander<I> {

	private static final long serialVersionUID = 3616797799524952539L;

    private int[] order;

	public RandomGameExpanderWithMoveOrdering(AlgorithmConfig<I> algConfig, int[] ordering) {
		super(algConfig);
        this.order = ordering;
	}

	@Override
	public List<Action> getActions(GameState gameState) {
		List<Action> actions = super.getActions(gameState);
		Action[] reorderedActions = new Action[actions.size()];

        assert (order.length == actions.size());

        for (int i=0; i<actions.size(); i++) {
            reorderedActions[order[i]] = actions.get(i);
        }

        return Arrays.asList(reorderedActions);
	}

}
