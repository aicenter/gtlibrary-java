package cz.agents.gtlibrary.cfr;

import java.util.LinkedList;
import java.util.List;

import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;

public abstract class CFR<I extends CFRInformationSet> {
	protected CFRConfig<I> config;

	public CFR(CFRConfig<I> config) {
		this.config = config;
	}

	public void buildGameTree(GameState rootState, Expander expander) {
		LinkedList<GameState> queue = new LinkedList<GameState>();

		queue.add(rootState);

		while (queue.size() > 0) {
			GameState state = queue.removeFirst();

			if (state.isGameEnd()) {
				state.getUtilities();
				config.setUtilityFor(state.getHistory(), state.getUtilities());
				continue;
			}
			
			I set = config.getInformationSetFor(state);
			List<Action> actions = expander.getActions(state);

			if (set == null)
				set = createAndAddSet(state, actions);
			
			for (Action action : expander.getActions(state)) {
				GameState newState = state.performAction(action);

				set.addSuccessor(state, newState);
				queue.add(newState);
			}
		}
	}

	private I createAndAddSet(GameState state, List<Action> actions) {
		I set = createInformationSet(state, actions);
		
		config.addInformationSetFor(state, set);
		return set;
	}

	public void updateTree(int iterations) {
		for (int i = 0; i < iterations; i++) {
			if(i%10000 == 0)
				System.out.println(config.getInformationSetFor(config.getRootState()).getValueOfGame());
			updateTree();
		}
	}

	public abstract void updateTree();

	public abstract I createInformationSet(GameState state, List<Action> actions);

}
