package cz.agents.gtlibrary.domain.pursuit;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import cz.agents.gtlibrary.domain.bpg.data.Node;
import cz.agents.gtlibrary.iinodes.GameStateImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.Pair;

public class PursuitGameState extends GameStateImpl {

	private static final long serialVersionUID = -1158263262570332115L;
	
	private List<Action> sequence;
	private Graph graph;
	private Node evaderPosition;
	private Node p1Position;
	private Node p2Position;
	private int currentPlayerIndex;
	private int round;
	private int hashCode = -1;

	public PursuitGameState() {
		super(PursuitGameInfo.ALL_PLAYERS);
		graph = new Graph(PursuitGameInfo.graphFile);
		currentPlayerIndex = 0;
		round = 0;
		evaderPosition = graph.getEvaderStart();
		p1Position = graph.getP1Start();
		p2Position = graph.getP2Start();
		sequence = new LinkedList<Action>();
	}

	public PursuitGameState(PursuitGameState gameState) {
		super(gameState);
		this.evaderPosition = gameState.evaderPosition;
		this.p1Position = gameState.p1Position;
		this.p2Position = gameState.p2Position;
		this.currentPlayerIndex = gameState.currentPlayerIndex;
		this.round = gameState.round;
		this.graph = gameState.graph;
		this.sequence = new LinkedList<Action>(gameState.sequence);
	}

	public void executePatrollerAction(PatrollerPursuitAction action) {
		if (!getPlayerToMove().equals(PursuitGameInfo.PATROLLER))
			throw new IllegalStateException("Evader attempts to move out of his turn.");
		if (!action.getP1Origin().equals(p1Position))
			throw new IllegalStateException("Patroller1's action has wrong origin. Expected: " + p1Position + " Actual: " + action.getP1Origin());
		if (!action.getP2Origin().equals(p2Position))
			throw new IllegalStateException("Patroller2's action has wrong origin. Expected: " + p2Position + " Actual: " + action.getP2Origin());
		p1Position = action.getP1Destination();
		p2Position = action.getP2Destination();
		switchPlayers();
		endRound();
		clearCache();
	}

	public void executeEvaderAction(EvaderPursuitAction action) {
		if (!getPlayerToMove().equals(PursuitGameInfo.EVADER))
			throw new IllegalStateException("Patroller attempts to move out of his turn.");
		if (!action.getOrigin().equals(evaderPosition))
			throw new IllegalStateException("Evader's action has wrong origin. Expected: " + evaderPosition + " Actual: " + action.getOrigin());
		evaderPosition = action.getDestination();
		switchPlayers();
		clearCache();
	}

	public void clearCache() {
		hashCode = -1;
	}

	private void switchPlayers() {
		currentPlayerIndex = 1 - currentPlayerIndex;
	}

	private void endRound() {
		addActionsToSequence();
		if (isCaughtInNode() || isCaughtOnEdge()) {
			round = PursuitGameInfo.depth;
			return;
		}
		round++;
	}

	private void addActionsToSequence() {
		sequence.add(getSequenceFor(players[0]).getLast());
		sequence.add(getSequenceFor(players[1]).getLast());
	}

	private boolean isCaughtOnEdge() {
		EvaderPursuitAction lastEvaderAction = (EvaderPursuitAction) getSequenceFor(PursuitGameInfo.EVADER).getLast();
		PatrollerPursuitAction lastPatrollerAction = (PatrollerPursuitAction) getSequenceFor(PursuitGameInfo.PATROLLER).getLast();

		return caughtByP1(lastEvaderAction, lastPatrollerAction) || caughtByP2(lastEvaderAction, lastPatrollerAction);
	}

	private boolean caughtByP2(EvaderPursuitAction lastEvaderAction, PatrollerPursuitAction lastPatrollerAction) {
		return lastEvaderAction.getOrigin().equals(lastPatrollerAction.getP2Destination()) && lastEvaderAction.getDestination().equals(lastPatrollerAction.getP2Origin());
	}

	private boolean caughtByP1(EvaderPursuitAction lastEvaderAction, PatrollerPursuitAction lastPatrollerAction) {
		return lastEvaderAction.getOrigin().equals(lastPatrollerAction.getP1Destination()) && lastEvaderAction.getDestination().equals(lastPatrollerAction.getP1Origin());
	}

	private boolean isCaughtInNode() {
		return evaderPosition.equals(p1Position) || evaderPosition.equals(p2Position);
	}

	@Override
	public Player getPlayerToMove() {
		return players[currentPlayerIndex];
	}

	@Override
	public boolean isPlayerToMoveNature() {
		return false;
	}

	@Override
	public double[] getUtilities() {
		if (!isGameEnd())
			return new double[] { 0 };
		if (isCaughtInNode() || isCaughtOnEdge())
			return new double[] { -1, 1 };
		return new double[] { 1, -1 };
	}

	@Override
	public boolean isGameEnd() {
		return round == PursuitGameInfo.depth;
	}

	public int getRound() {
		return round;
	}

	public Graph getGraph() {
		return graph;
	}

	public Node getP1Position() {
		return p1Position;
	}

	public Node getP2Position() {
		return p2Position;
	}

	public Node getEvaderPosition() {
		return evaderPosition;
	}

	@Override
	public GameState copy() {
		return new PursuitGameState(this);
	}

	@Override
	public int hashCode() {
		if (hashCode == -1)
			hashCode = new HashCodeBuilder(17, 31).append(history).toHashCode();
		return hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PursuitGameState other = (PursuitGameState) obj;
		if (this.hashCode() != obj.hashCode())
			return false;
		if (currentPlayerIndex != other.currentPlayerIndex)
			return false;
		if (evaderPosition == null) {
			if (other.evaderPosition != null)
				return false;
		} else if (!evaderPosition.equals(other.evaderPosition))
			return false;
		if (graph == null) {
			if (other.graph != null)
				return false;
		} else if (!graph.equals(other.graph))
			return false;
		if (p1Position == null) {
			if (other.p1Position != null)
				return false;
		} else if (!p1Position.equals(other.p1Position))
			return false;
		if (p2Position == null) {
			if (other.p2Position != null)
				return false;
		} else if (!p2Position.equals(other.p2Position))
			return false;
		if (history == null) {
			if (other.history != null)
				return false;
		} else if (!history.equals(other.history))
			return false;
		if (round != other.round)
			return false;
		return true;
	}

	@Override
	public double getProbabilityOfNatureFor(Action action) {
		return 1;
	}

	@Override
	public Pair<Integer, Sequence> getISKeyForPlayerToMove() {
		return new Pair<Integer, Sequence>(sequence.hashCode(), getSequenceForPlayerToMove());
	}
	
	@Override
	public String toString() {
		return history.toString();
	}

}