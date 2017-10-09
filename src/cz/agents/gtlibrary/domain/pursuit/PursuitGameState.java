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


package cz.agents.gtlibrary.domain.pursuit;

import cz.agents.gtlibrary.algorithms.sequenceform.refinements.quasiperfect.numbers.Rational;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.iinodes.PerfectRecallISKey;
import cz.agents.gtlibrary.iinodes.SimultaneousGameState;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.Pair;
import cz.agents.gtlibrary.utils.graph.Node;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.List;

public class PursuitGameState extends SimultaneousGameState {

    protected static final long serialVersionUID = -1158263262570332115L;

    protected List<Action> sequence;
    protected PursuitGraph graph;
    protected Node evaderPosition;
    protected Node p1Position;
    protected Node p2Position;
    protected int currentPlayerIndex;
    protected int round;
    protected int hashCode = -1;

    public PursuitGameState() {
        super(PursuitGameInfo.ALL_PLAYERS);
        graph = new PursuitGraph(PursuitGameInfo.graphFile);
        currentPlayerIndex = 0;
        round = 0;
        evaderPosition = graph.getEvaderStart();
        p1Position = graph.getP1Start();
        p2Position = graph.getP2Start();
        sequence = new ArrayList<>();
    }

    public PursuitGameState(PursuitGameState gameState) {
        super(gameState);
        this.evaderPosition = gameState.evaderPosition;
        this.p1Position = gameState.p1Position;
        this.p2Position = gameState.p2Position;
        this.currentPlayerIndex = gameState.currentPlayerIndex;
        this.round = gameState.round;
        this.graph = gameState.graph;
        this.sequence = new ArrayList<Action>(gameState.sequence.size() + 1);
        this.sequence.addAll(gameState.sequence);
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

    protected void switchPlayers() {
        currentPlayerIndex = 1 - currentPlayerIndex;
    }

    protected void endRound() {
        addActionsToSequence();
        if (isCaughtInNode() || isCaughtOnEdge()) {
            round = PursuitGameInfo.depth;
            return;
        }
        round++;
    }

    protected void addActionsToSequence() {
        sequence.add(getSequenceFor(players[0]).getLast());
        sequence.add(getSequenceFor(players[1]).getLast());
    }

    protected boolean isCaughtOnEdge() {
        EvaderPursuitAction lastEvaderAction = (EvaderPursuitAction) getSequenceFor(PursuitGameInfo.EVADER).getLast();
        PatrollerPursuitAction lastPatrollerAction = (PatrollerPursuitAction) getSequenceFor(PursuitGameInfo.PATROLLER).getLast();

        return caughtByP1(lastEvaderAction, lastPatrollerAction) || caughtByP2(lastEvaderAction, lastPatrollerAction);
    }

    protected boolean caughtByP2(EvaderPursuitAction lastEvaderAction, PatrollerPursuitAction lastPatrollerAction) {
        return lastEvaderAction.getOrigin().equals(lastPatrollerAction.getP2Destination()) && lastEvaderAction.getDestination().equals(lastPatrollerAction.getP2Origin());
    }

    protected boolean caughtByP1(EvaderPursuitAction lastEvaderAction, PatrollerPursuitAction lastPatrollerAction) {
        return lastEvaderAction.getOrigin().equals(lastPatrollerAction.getP1Destination()) && lastEvaderAction.getDestination().equals(lastPatrollerAction.getP1Origin());
    }

    protected boolean isCaughtInNode() {
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

//	@Override
//	public double[] getUtilities() {
//		if (!isGameEnd())
//			return new double[] { 0 };
//		if (isCaughtInNode() || isCaughtOnEdge())
//			return new double[] { -1, 1 };
//		return new double[] { 1, -1 };
//	}

    @Override
    public void setDepth(int depth) {
        this.depth = round + depth;
    }

    @Override
    protected double[] getEndGameUtilities() {
        if (onGoal())
            return new double[]{2, -2};
        if (isCaughtInNode() || isCaughtOnEdge())
            return new double[]{-1, 1};
        return new double[]{1, -1};
    }

    private boolean onGoal() {
        return evaderPosition.equals(graph.getEvaderGoal());
    }

    @Override
    public double[] evaluate() {
        double p1Distance = graph.getDistance(p1Position, evaderPosition);
        double p2Distance = graph.getDistance(p2Position, evaderPosition);
//        double weight = 2 * Math.sqrt(graph.getAllNodes().size());
//        double p1Value = FastTanh.tanh(Math.min(p1Distance, p2Distance));// - weight / 2) / (weight / 2);
        double p1Value = (Math.min(p1Distance, p2Distance) + 0.01*Math.max(p1Distance, p2Distance)) /(2.02 * Math.sqrt(graph.getAllNodes().size()));

        assert p1Value < 1;
        return new double[]{p1Value, -p1Value};
    }

    @Override
    public boolean isActualGameEnd() {
        return round == PursuitGameInfo.depth;
    }

    @Override
    public boolean isDepthLimit() {
        return round >= depth;
    }

    @Override
    public Rational[] getExactUtilities() {
        if (!isGameEnd())
            return new Rational[]{Rational.ZERO};
        if (isCaughtInNode() || isCaughtOnEdge())
            return new Rational[]{Rational.ONE.negate(), Rational.ONE};
        return new Rational[]{Rational.ONE, Rational.ONE.negate()};
    }

//	@Override
//	public boolean isGameEnd() {
//		return round == PursuitGameInfo.depth;
//	}

    public int getRound() {
        return round;
    }

    public PursuitGraph getGraph() {
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
    public Rational getExactProbabilityOfNatureFor(Action action) {
        return Rational.ONE;
    }

    @Override
    public ISKey getISKeyForPlayerToMove() {
        return new PerfectRecallISKey(sequence.hashCode(), getSequenceForPlayerToMove());
    }

    @Override
    public String toString() {
//        return history.toString();
        return "{"+evaderPosition.toString()+" | "+p1Position.toString()+","+p2Position.toString()+"}";
    }

}
