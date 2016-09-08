/*
Copyright 2014 Faculty of Electrical Engineering at CTU in Prague

This file is part of Game Theoretic Library.

Game Theoretic Library is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Game Theoretic Library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with Game Theoretic Library.  If not, see <http://www.gnu.org/licenses/>.*/


package cz.agents.gtlibrary.domain.bpg;

import java.util.*;

import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.improvedBR.DoubleOracleWithBestMinmaxImprovement.PlayerSelection;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.quasiperfect.numbers.Rational;

import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.iinodes.PerfectRecallISKey;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import cz.agents.gtlibrary.domain.bpg.AttackerAction.AttackerMovementType;
import cz.agents.gtlibrary.domain.bpg.data.BorderPatrollingGraph;
import cz.agents.gtlibrary.domain.poker.PokerAction;
import cz.agents.gtlibrary.iinodes.ArrayListSequenceImpl;
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

	protected Set<Action> addedToFlagged;
	protected Map<Action, boolean[]> addedToObserved;

	protected boolean slowAttackerMovement;

	public final static int ATTACKER_WINS = 1;
	public final static int OPEN_POSITION = 0;

	protected int hashCode = -1;
	protected ISKey key;

	protected List<Node> patrollerMoves;
	protected List<Pair<Node, AttackerMovementType>> attackerMoves;

	public BPGGameState() {
		this(new BorderPatrollingGraph(BPGGameInfo.graphFile));
	}

    public BPGGameState(BorderPatrollingGraph graph) {
        super(BPGGameInfo.ALL_PLAYERS);
        slowAttackerMovement = false;
        playerToMove = BPGGameInfo.ATTACKER;
        this.graph = graph;
        flaggedNodesObservedByPatroller = new HashSet<>();
        flaggedNodes = new HashSet<>();
        addedToFlagged = new HashSet<>();
        addedToObserved = new HashMap<>();
        attackerPosition = graph.getOrigin();
        p1Position = graph.getP1Start();
        p2Position = graph.getP2Start();
		patrollerMoves = new ArrayList<>();
		attackerMoves = new ArrayList<>();
    }

	public BPGGameState(BPGGameState gameState) {
		super(gameState);
		this.graph = gameState.getGraph();
		this.slowAttackerMovement = gameState.slowAttackerMovement;
		this.playerToMove = gameState.playerToMove;
		this.flaggedNodes = new HashSet<>(gameState.flaggedNodes);
		this.flaggedNodesObservedByPatroller = new HashSet<>(gameState.flaggedNodesObservedByPatroller);
		this.addedToFlagged = new HashSet<>(gameState.addedToFlagged);
		this.addedToObserved = new HashMap<>(gameState.addedToObserved);
		this.attackerPosition = gameState.attackerPosition;
		this.p1Position = gameState.p1Position;
		this.p2Position = gameState.p2Position;
		this.attackerMoves = new ArrayList<>(gameState.attackerMoves);
		this.patrollerMoves = new ArrayList<>(gameState.patrollerMoves);
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
			if(flaggedNodes.add(action.getToNode())){
				addedToFlagged.add(action);
			}
			//System.out.println(action.getToNode());
		} else if (action.getType() == AttackerMovementType.SLOW) {
			slowAttackerMovement = true;
		}
		playerToMove = BPGGameInfo.DEFENDER;
		attackerMoves.add(new Pair<>(attackerPosition, action.getType()));
	}

	public void executePatrollerAction(PatrollerAction action) {
		clearCache();
		p1Position = action.getToNodeForP1();
		p2Position = action.getToNodeForP2();
		boolean[] added = new boolean[2];

		//mapu
		if (flaggedNodes.contains(action.getToNodeForP1())) {
			if (flaggedNodesObservedByPatroller.add(action.getToNodeForP1())){
				added[0] = true;
			}

		}
		if (flaggedNodes.contains(action.getToNodeForP2())) {
			if (flaggedNodesObservedByPatroller.add(action.getToNodeForP2()))
				added[1] = true;
		}
		if(added[0] || added[1])
			addedToObserved.put(action, added);
		playerToMove = BPGGameInfo.ATTACKER;
		patrollerMoves.add(p1Position);
		patrollerMoves.add(p2Position);
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
	public ISKey getISKeyForPlayerToMove() {
		if (key != null)
			return key;
		if (playerToMove.equals(BPGGameInfo.ATTACKER)) {
			key = new PerfectRecallISKey(new HashCodeBuilder().append(isGameEnd()).append(getHistory().getSequenceOf(playerToMove)).toHashCode(), new ArrayListSequenceImpl(history.getSequenceOf(playerToMove)));
		} else {
			key = new PerfectRecallISKey(new HashCodeBuilder().append(isGameEnd()).append(getHistory().getSequenceOf(playerToMove)).append(flaggedNodesObservedByPatroller).toHashCode(), new ArrayListSequenceImpl(history.getSequenceOf(playerToMove)));
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

		if(!playerToMove.equals(other.playerToMove))
			return false;
		if(!p1Position.equals(other.p1Position))
			return false;
		if(!p2Position.equals(p2Position))
			return false;
		if(!attackerPosition.equals(attackerPosition))
			return false;
		if (!attackerMoves.equals(other.attackerMoves))
			return false;
		if (!patrollerMoves.equals(other.patrollerMoves))
			return false;
		return true;
	}

	@Override
	public void reverseAction(){
		if (playerToMove.equals(BPGGameInfo.ATTACKER)){
			reversePatrollerAction();
		}
		else{
			reverseAttackerAction();
		}
		clearCache();
		super.reverseAction();
	}

	private void reverseAttackerAction() {
		playerToMove = BPGGameInfo.ATTACKER;
		AttackerAction action = (AttackerAction)history.getLastAction();
		attackerPosition = action.getFromNode();
		checkFlaggedByAttacker(action);
		if(action.getType() == AttackerMovementType.WAIT)
			slowAttackerMovement=true;
		else
			slowAttackerMovement=false;
	}

	private void reversePatrollerAction() {
		playerToMove = BPGGameInfo.DEFENDER;
		PatrollerAction action = (PatrollerAction)history.getLastAction();
		p1Position = action.getFromNodeForP1();
		p2Position = action.getFromNodeForP2();
		checkFlaggedByPatroller(action);
	}

	private void checkFlaggedByAttacker(AttackerAction action){
		if(addedToFlagged.contains(action)){
			addedToFlagged.remove(action);
			flaggedNodes.remove(action.getToNode());
		}
	}

	private void checkFlaggedByPatroller(PatrollerAction action){
		boolean[] added = addedToObserved.get(action);
		if(added != null){
			if(added[0])
				flaggedNodesObservedByPatroller.remove(action.getToNodeForP1());
			if(added[1])
				flaggedNodesObservedByPatroller.remove(action.getToNodeForP2());
			addedToObserved.remove(action);
		}
	}

}
