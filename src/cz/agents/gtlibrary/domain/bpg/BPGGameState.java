package cz.agents.gtlibrary.domain.bpg;

import java.util.HashSet;
import java.util.Set;

import cz.agents.gtlibrary.algorithms.sequenceform.refinements.quasiperfect.numbers.Rational;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import cz.agents.gtlibrary.domain.bpg.AttackerAction.AttackerMovementType;
import cz.agents.gtlibrary.domain.bpg.data.BorderPatrollingGraph;
import cz.agents.gtlibrary.iinodes.GameStateImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.Pair;
import cz.agents.gtlibrary.utils.graph.Node;

public class BPGGameState extends GameStateImpl {

	protected static final long serialVersionUID = 7703160415991328955L;
	
	protected BorderPatrollingGraph graph;
	protected Player playerToMove;
	protected Node attackerPosition;
	protected Node p1Position;
	protected Node p2Position;

	protected Set<Node> flaggedNodesObservedByPatroller;
	protected Set<Node> flaggedNodes;

	protected boolean slowAttackerMovement;

	public final static int ATTACKER_WINS = 1;
	public final static int OPEN_POSITION = 0;

	protected int hashCode = -1;
	protected Pair<Integer, Sequence> key;

	public BPGGameState() {
		this(new BorderPatrollingGraph(BPGGameInfo.graphFile));
	}

    public BPGGameState(BorderPatrollingGraph graph) {
        super(BPGGameInfo.ALL_PLAYERS);
        slowAttackerMovement = false;
        playerToMove = BPGGameInfo.ATTACKER;
        this.graph = graph;
        flaggedNodesObservedByPatroller = new HashSet<Node>();
        flaggedNodes = new HashSet<Node>();
        attackerPosition = graph.getOrigin();
        p1Position = graph.getP1Start();
        p2Position = graph.getP2Start();
    }

	public BPGGameState(BPGGameState gameState) {
		super(gameState);
		this.graph = gameState.getGraph();
		this.slowAttackerMovement = gameState.slowAttackerMovement;
		this.playerToMove = gameState.playerToMove;
		this.flaggedNodes = new HashSet<Node>(gameState.flaggedNodes);
		this.flaggedNodesObservedByPatroller = new HashSet<Node>(gameState.flaggedNodesObservedByPatroller);
		this.attackerPosition = gameState.attackerPosition;
		this.p1Position = gameState.p1Position;
		this.p2Position = gameState.p2Position;
	}

	@Override
	public GameState copy() {
		return new BPGGameState(this);
	}

	public BorderPatrollingGraph getGraph() {
		return graph;
	}

	public boolean isSlowAttackerMovement() {
		return slowAttackerMovement;
	}

	public void executeAttackerAction(AttackerAction action) {
		clearCache();
		slowAttackerMovement = false;
		attackerPosition = action.getToNode();

		if ((action.getType() == AttackerMovementType.QUICK) && (action.getToNode().getIntID() >= 4 && action.getToNode().getIntID() <= 6)) {
			flaggedNodes.add(action.getToNode());
		} else if (action.getType() == AttackerMovementType.SLOW) {
			slowAttackerMovement = true;
		}
		playerToMove = BPGGameInfo.DEFENDER;
	}

	public void executePatrollerAction(PatrollerAction action) {
		clearCache();
		p1Position = action.getToNodeForP1();
		p2Position = action.getToNodeForP2();

		if (flaggedNodes.contains(action.getToNodeForP1())) {
			flaggedNodesObservedByPatroller.add(action.getToNodeForP1());
		}
		if (flaggedNodes.contains(action.getToNodeForP2())) {
			flaggedNodesObservedByPatroller.add(action.getToNodeForP2());
		}
		playerToMove = BPGGameInfo.ATTACKER;
	}

	protected void clearCache() {
		hashCode = -1;
		key = null;
	}

	@Override
	public double[] getUtilities() {
		if (getPlayerToMove().equals(BPGGameInfo.ATTACKER)) {
			PatrollerAction patrollerAction = (PatrollerAction) history.getSequenceOf(BPGGameInfo.DEFENDER).getLast();
			AttackerAction attackerAction = (AttackerAction) history.getSequenceOf(BPGGameInfo.ATTACKER).getLast();

			if (isCaught(patrollerAction, attackerAction))
				return new double[] { -ATTACKER_WINS, ATTACKER_WINS };
		}
		if (attackerPosition.equals(graph.getDestination()))
            return new double[]{ATTACKER_WINS, -ATTACKER_WINS};
		return new double[] { OPEN_POSITION, OPEN_POSITION };
	}

    @Override
    public Rational[] getExactUtilities() {
        if (getPlayerToMove().equals(BPGGameInfo.ATTACKER)) {
            PatrollerAction patrollerAction = (PatrollerAction) history.getSequenceOf(BPGGameInfo.DEFENDER).getLast();
            AttackerAction attackerAction = (AttackerAction) history.getSequenceOf(BPGGameInfo.ATTACKER).getLast();

            if (isCaught(patrollerAction, attackerAction)) {
                return new Rational[] { new Rational(-ATTACKER_WINS), new Rational(ATTACKER_WINS) };
            }
        }
        if (attackerPosition.equals(graph.getDestination())) {
            return new Rational[] { new Rational(ATTACKER_WINS), new Rational(-ATTACKER_WINS) };
        }
        return new Rational[] { new Rational(OPEN_POSITION), new Rational(OPEN_POSITION) };
    }

	protected boolean caughtOnEdgeByP1(PatrollerAction patrollerAction, AttackerAction attackerAction) {
		return patrollerAction.getFromNodeForP1().equals(attackerAction.getToNode()) && patrollerAction.getToNodeForP1().equals(attackerAction.getFromNode());
	}

	protected boolean caughtOnEdgeByP2(PatrollerAction patrollerAction, AttackerAction attackerAction) {
		return patrollerAction.getFromNodeForP2().equals(attackerAction.getToNode()) && patrollerAction.getToNodeForP2().equals(attackerAction.getFromNode());
	}

	protected boolean caughtInNode(PatrollerAction patrollerAction, AttackerAction attackerAction) {
		return patrollerAction.getToNodeForP1().equals(attackerAction.getToNode()) || patrollerAction.getToNodeForP2().equals(attackerAction.getToNode());
	}

	@Override
	public boolean isGameEnd() {
		if (history.getSequenceOf(getPlayerToMove()).size() >= BPGGameInfo.DEPTH)
			return true;
		if (history.getSequenceOf(BPGGameInfo.ATTACKER).size() == 0 || history.getSequenceOf(BPGGameInfo.DEFENDER).size() == 0)
			return false;
		if (attackerPosition.equals(graph.getDestination()))
			return true;
		if (playerToMove.equals(BPGGameInfo.DEFENDER))
			return false;
		PatrollerAction patrollerAction = (PatrollerAction) history.getSequenceOf(BPGGameInfo.DEFENDER).getLast();
		AttackerAction attackerAction = (AttackerAction) history.getSequenceOf(BPGGameInfo.ATTACKER).getLast();

		return isCaught(patrollerAction, attackerAction);
	}

	protected boolean isCaught(PatrollerAction patrollerAction, AttackerAction attackerAction) {
		return caughtInNode(patrollerAction, attackerAction) || caughtOnEdgeByP1(patrollerAction, attackerAction) || caughtOnEdgeByP2(patrollerAction, attackerAction);
	}

	@Override
	public Player getPlayerToMove() {
		return playerToMove;
	}

	public Node getP1Position() {
		return p1Position;
	}

	public Node getP2Position() {
		return p2Position;
	}

	public Node getAttackerPosition() {
		return attackerPosition;
	}

	public Set<Node> getFlaggedNodes() {
		return flaggedNodes;
	}

	public Set<Node> getFlaggedNodesObservedByPatroller() {
		return flaggedNodesObservedByPatroller;
	}

	@Override
	public String toString() {
		return history.toString();
	}

	@Override
	public boolean isPlayerToMoveNature() {
		return false;
	}

	@Override
	public double getProbabilityOfNatureFor(Action action) {
		return 0;
	}

    @Override
    public Rational getExactProbabilityOfNatureFor(Action action) {
        return Rational.ZERO;
    }

	@Override
	public Pair<Integer, Sequence> getISKeyForPlayerToMove() {
		if (key != null)
			return key;
		if (playerToMove.equals(BPGGameInfo.ATTACKER)) {
			key = new Pair<Integer, Sequence>(new HashCodeBuilder().append(isGameEnd()).append(getHistory().getSequenceOf(playerToMove)).toHashCode(), history.getSequenceOf(playerToMove));
		} else {
			key = new Pair<Integer, Sequence>(new HashCodeBuilder().append(isGameEnd()).append(getHistory().getSequenceOf(playerToMove)).append(flaggedNodesObservedByPatroller).toHashCode(), history.getSequenceOf(playerToMove));
		}
		return key;
	}

	@Override
	public int hashCode() {
		if (hashCode == -1) {
			final int prime = 31;
			
			hashCode = 1;
			hashCode = prime * hashCode + ((history == null) ? 0 : history.hashCode());
		}
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
		BPGGameState other = (BPGGameState) obj;

		if (!history.equals(other.history))
			return false;
		return true;
	}

}
