package cz.agents.gtlibrary.domain.pursuit;

import java.util.LinkedList;
import java.util.List;

import cz.agents.gtlibrary.domain.bpg.data.Edge;
import cz.agents.gtlibrary.iinodes.ExpanderImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

public class PursuitExpander<I extends InformationSet> extends ExpanderImpl<I> {

	private static final long serialVersionUID = 3736551160185044265L;

	public PursuitExpander(AlgorithmConfig<I> algConfig) {
		super(algConfig);
	}

	@Override
	public List<Action> getActions(GameState gameState) {
		if (gameState.getPlayerToMove().equals(PursuitGameInfo.EVADER))
			return getEvaderActions((PursuitGameState) gameState);
		return getPatrollerActions((PursuitGameState) gameState);
	}

	private List<Action> getEvaderActions(PursuitGameState state) {
		List<Action> actions = new LinkedList<Action>();

		for (Edge edge : state.getGraph().getEdgesOf(state.getEvaderPosition())) {
			if (edge.getSource().equals(state.getEvaderPosition()))
				actions.add(new EvaderPursuitAction(state.getEvaderPosition(), edge.getTarget(), getAlgorithmConfig().getInformationSetFor(state)));
		}
		if (!PursuitGameInfo.forceMoves) {
			actions.add(new EvaderPursuitAction(state.getEvaderPosition(), state.getEvaderPosition(), getAlgorithmConfig().getInformationSetFor(state)));
		}
		return actions;
	}

	private List<Action> getPatrollerActions(PursuitGameState state) {
		List<Action> actions = new LinkedList<Action>();

		for (Edge p1Edge : state.getGraph().getEdgesOf(state.getP1Position())) {
			for (Edge p2Edge : state.getGraph().getEdgesOf(state.getP2Position())) {
				if (p1Edge.getSource().equals(state.getP1Position()) && p2Edge.getSource().equals(state.getP2Position()))
					actions.add(new PatrollerPursuitAction(state.getP1Position(), p1Edge.getTarget(), state.getP2Position(), p2Edge.getTarget(), getAlgorithmConfig().getInformationSetFor(state)));
				if (!PursuitGameInfo.forceMoves) {
					actions.add(new PatrollerPursuitAction(state.getP1Position(), state.getP1Position(), state.getP2Position(), p2Edge.getTarget(), getAlgorithmConfig().getInformationSetFor(state)));
					actions.add(new PatrollerPursuitAction(state.getP1Position(), p1Edge.getTarget(), state.getP2Position(), state.getP2Position(), getAlgorithmConfig().getInformationSetFor(state)));
				}
			}
		}
		if (!PursuitGameInfo.forceMoves) {
			actions.add(new PatrollerPursuitAction(state.getP1Position(), state.getP1Position(), state.getP2Position(), state.getP2Position(), getAlgorithmConfig().getInformationSetFor(state)));
		}
		return actions;
	}

}
