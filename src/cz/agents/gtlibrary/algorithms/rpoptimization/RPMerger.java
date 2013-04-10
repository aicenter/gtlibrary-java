package cz.agents.gtlibrary.algorithms.rpoptimization;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import cz.agents.gtlibrary.iinodes.LinkedListSequenceImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;

public class RPMerger {

	private GameState rootState;
	private Expander<? extends InformationSet> expander;

	public RPMerger(GameState rootState, Expander<? extends InformationSet> expander) {
		this.rootState = rootState;
		this.expander = expander;
	}

	public Map<Player, Map<Sequence, Double>> mergePlans(Map<Player, Map<Sequence, Double>> realPlan1, Map<Player, Map<Sequence, Double>> realPlan2) {
		Map<Player, Map<Sequence, Double>> realPlan = new HashMap<Player, Map<Sequence, Double>>();
		LinkedList<GameState> states = new LinkedList<GameState>();

		realPlan.put(rootState.getAllPlayers()[0], new HashMap<Sequence, Double>());
		realPlan.put(rootState.getAllPlayers()[1], new HashMap<Sequence, Double>());
		states.addLast(rootState);

		while (!states.isEmpty()) {
			GameState state = states.removeFirst();
			if(state.isGameEnd())
				continue;

			if (!state.isPlayerToMoveNature())
				addContinuation(state, realPlan, realPlan1, realPlan2);
			for (Action action : expander.getActionsForUnknownIS(state)) {
				states.add(state.performAction(action));
			}
		}
		return realPlan;
	}

	private void addContinuation(GameState state, Map<Player, Map<Sequence, Double>> realPlan, Map<Player, Map<Sequence, Double>> realPlan1, Map<Player, Map<Sequence, Double>> realPlan2) {
		boolean added = addContinuationFrom(state, realPlan, realPlan1);

		if (!added) {
			addContinuationFrom(state, realPlan, realPlan2);
		}
	}

	private boolean addContinuationFrom(GameState state, Map<Player, Map<Sequence, Double>> realPlan, Map<Player, Map<Sequence, Double>> realPlan1) {
		boolean added = false;

		Sequence sequence = state.getSequenceForPlayerToMove();
		for (Action action : expander.getActionsForUnknownIS(state)) {
			Sequence contSequence = new LinkedListSequenceImpl(sequence);

			contSequence.addLast(action);

			Double value = realPlan1.get(state.getPlayerToMove()).get(contSequence);

			if (value != null) {
				realPlan.get(state.getPlayerToMove()).put(contSequence, value);
				added = true;
			}
		}
		return added;
	}

}
