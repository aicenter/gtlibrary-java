package cz.agents.gtlibrary.domain.bpg;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import cz.agents.gtlibrary.domain.bpg.AttackerAction.AttackerMovementType;
import cz.agents.gtlibrary.domain.bpg.data.BorderPatrollingGraph;
import cz.agents.gtlibrary.domain.bpg.data.Edge;
import cz.agents.gtlibrary.domain.bpg.data.Node;
import cz.agents.gtlibrary.iinodes.ExpanderImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

public class BPGExpander<I extends InformationSet> extends ExpanderImpl<I> {

	public BPGExpander(AlgorithmConfig<I> algConfig) {
		super(algConfig);
	}

	public List<Action> getActions(GameState state) {
		BPGGameState gameState = (BPGGameState) state;

		if (gameState.getPlayerToMove().equals(BPGGameInfo.ATTACKER)) {
			return getActionsOfAttacker(gameState);
		}
		return getActionsForPatroller(gameState);
	}

	private List<Action> getActionsOfAttacker(BPGGameState gameState) {
		List<Action> result = new ArrayList<Action>();
		Node attackerPosition = gameState.getAttackerPosition();

		if (gameState.isSlowAttackerMovement()) {
			result.add(new AttackerAction(attackerPosition, attackerPosition, getAlgorithmConfig().getInformationSetFor(gameState), AttackerMovementType.WAIT));
		} else {
			for (Edge edge : gameState.getGraph().getGraph().outgoingEdgesOf(attackerPosition)) {
				if (edge.getSource().equals(edge.getTarget()) && !edge.getSource().equals(gameState.getGraph().getOrigin()))
					continue;
				int idOfTarget = edge.getTarget().getIntID();
				AttackerAction quickAttackerAction = new AttackerAction(attackerPosition, edge.getTarget(), getAlgorithmConfig().getInformationSetFor(gameState), AttackerMovementType.QUICK);

				result.add(quickAttackerAction);
				if (BPGGameInfo.SLOW_MOVES) {
					if (idOfTarget >= 2 && idOfTarget <= 6) {
						AttackerAction slowAttackerAction = new AttackerAction(attackerPosition, edge.getTarget(), getAlgorithmConfig().getInformationSetFor(gameState), AttackerMovementType.SLOW);
						result.add(slowAttackerAction);
					}
				}

			}
		}
		return result;
	}

	private List<Action> getActionsForPatroller(BPGGameState gameState) {
		List<Action> result = new LinkedList<Action>();
		Node p1Position = gameState.getP1Position();
		Node p2Position = gameState.getP2Position();

		for (Node p1Target : getTargetsForP1(p1Position, gameState.getGraph())) {
			for (Node p2Target : getTargetsForP2(p2Position, gameState.getGraph())) {
				result.add(new PatrollerAction(p1Position, p2Position, p1Target, p2Target,
						getAlgorithmConfig().getInformationSetFor(gameState), gameState.getFlaggedNodes()));
			}
		}
		return result;
	}

	private Iterable<Node> getTargetsForP1(Node position, BorderPatrollingGraph graph) {
		List<Node> nodes = new LinkedList<Node>();

		for (Edge edge : graph.getGraph().outgoingEdgesOf(position)) {
			if (edge.getTarget().equals(graph.getDestination()))
				continue;
			if (edge.getTarget().getIntID() >= 1 && edge.getTarget().getIntID() <= 6)
				nodes.add(edge.getTarget());
		}
		return nodes;
	}

	private Iterable<Node> getTargetsForP2(Node position, BorderPatrollingGraph graph) {
		List<Node> nodes = new LinkedList<Node>();

		for (Edge edge : graph.getGraph().outgoingEdgesOf(position)) {
			if (edge.getTarget().equals(graph.getDestination()))
				continue;
			if (edge.getTarget().getIntID() > 10)
				nodes.add(edge.getTarget());
		}
		return nodes;
	}

	@Override
	public List<Action> getActions(InformationSet informationSet) {
		// TODO Auto-generated method stub
		return null;
	}

}
