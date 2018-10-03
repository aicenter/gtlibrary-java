package cz.agents.gtlibrary.domain.observationGame;

import cz.agents.gtlibrary.iinodes.GameStateImpl;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.iinodes.PerfectRecallISKey;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bbosansky on 11/3/17.
 */
public class ObsGameState extends GameStateImpl{

    protected List<Action> history;
    protected int timeStep;
    protected int defenderRow;
    protected boolean attackerObserved = false;
    protected boolean attackSuccessful = false;
    protected int attackRow = -1;
    protected int observationRow;
    protected Player playerToMove;

    protected int hashCode = -1;
    protected boolean gameEnd = false;


    public ObsGameState() {
        super(ObsGameInfo.ALL_PLAYERS);
        history = new ArrayList<>();
        timeStep = 0;
        defenderRow = -1;
        observationRow = -1;
        playerToMove = ObsGameInfo.LEADER;
    }

    public ObsGameState(GameStateImpl gameState) {
        super(gameState);
        ObsGameState obs = (ObsGameState)gameState;
        history = new ArrayList<>(obs.history);
        timeStep = obs.timeStep;
        defenderRow = obs.defenderRow;
        attackerObserved = obs.attackerObserved;
        observationRow = obs.observationRow;
        playerToMove = obs.playerToMove;
    }

    protected void executeLeaderAction(ObsGameLeaderAction action) {
        if (!getPlayerToMove().equals(ObsGameInfo.LEADER))
            throw new IllegalStateException("Leader attempts to move out of his turn.");
        if (action.getFromRow() != defenderRow)
            throw new IllegalStateException("Leader's action has wrong origin. Action Origin: " + action.getFromRow() + " State origin: " + defenderRow);
        this.defenderRow = action.getToRow();
        if (timeStep == 0 || timeStep >= ObsGameInfo.attackAfterTimeStep) {
            switchPlayers();
        } else {
            endRound();
        }
        history.add(getSequenceFor(players[0]).getLast());
        clearCache();
    }

    protected void executeFollowerAction(ObsGameFollowerAction action) {
        if (!getPlayerToMove().equals(ObsGameInfo.FOLLOWER))
            throw new IllegalStateException("Follower attempts to move out of his turn.");
        if (action.getType().equals(ObsGameFollowerAction.FollowerActionType.OBSERVE)) {
            if (timeStep != 0)
                throw new IllegalStateException("Follower observes in an incorrect turn " + timeStep);
            if (action.getRow() == defenderRow) attackerObserved = true;
            observationRow = action.getRow();
        } else if (action.getType().equals(ObsGameFollowerAction.FollowerActionType.ATTACK)) {
            if (timeStep < ObsGameInfo.attackAfterTimeStep)
                throw new IllegalStateException("Follower attacks in an incorrect turn " + timeStep);
            if (action.getRow() != defenderRow) attackSuccessful = true;
            gameEnd = true;
            attackRow = action.getRow();
        } else if (action.getType().equals(ObsGameFollowerAction.FollowerActionType.WAIT)) {

        } else
            throw new IllegalStateException("Illegal type of an follower action " + action.getType());
//        history.add(getSequenceFor(players[1]).getLast());
        switchPlayers();
        endRound();
        clearCache();
    }

    public void clearCache() {
        hashCode = -1;
    }

    protected void switchPlayers() {
        int currentPlayerIndex = 1 - playerToMove.getId();
        playerToMove = ObsGameInfo.ALL_PLAYERS[currentPlayerIndex];
    }

    protected void endRound() {
        this.timeStep++;
        if (timeStep >= ObsGameInfo.maxTimeSteps)
            gameEnd = true;
    }

    @Override
    public Player getPlayerToMove() {
        return playerToMove;
    }

    @Override
    public GameState copy() {
        return new ObsGameState(this);
    }

    @Override
    public double[] getUtilities() {
        if (isGameEnd()) {
            if (attackSuccessful) {
                double val = ObsGameInfo.targetValues[attackRow];
                return new double[] {-val, val};
            } else
                return new double[] {1, -1};
        }
        return new double[]{0, 0};
    }

    @Override
    public double getProbabilityOfNatureFor(Action action) {
        return 0;
    }

    @Override
    public boolean isGameEnd() {
        return gameEnd;
    }

    @Override
    public boolean isPlayerToMoveNature() {
        return false;
    }

    @Override
    public ISKey getISKeyForPlayerToMove() {
        if (getPlayerToMove().equals(players[0])) {
            return new PerfectRecallISKey(new HashCodeBuilder(17, 31).append(history).append(attackRow).toHashCode(), getSequenceForPlayerToMove());
        } else if (getPlayerToMove().equals(players[1])) {
            return new PerfectRecallISKey(new HashCodeBuilder(17, 31).append(timeStep).append(observationRow).append(attackerObserved).append(attackRow).append(attackSuccessful).toHashCode(), getSequenceForPlayerToMove());
        } else throw new IllegalStateException("something is wrong");
    }

    @Override
    public int hashCode() {
        if (hashCode == -1) {
            hashCode = new HashCodeBuilder(17, 31).append(history).append(observationRow).append(attackerObserved).append(attackRow).append(attackSuccessful).toHashCode();
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
        ObsGameState other = (ObsGameState) obj;
        if (this.hashCode() != obj.hashCode())
            return false;
        if (this.timeStep != other.timeStep || (this.observationRow != other.observationRow) || (this.attackerObserved != other.attackerObserved) || this.attackRow != other.attackRow || this.attackSuccessful != other.attackSuccessful)
            return false;
        if (history == null) {
            if (other.history != null)
                return false;
        } else if (!history.equals(other.history))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "OS["+defenderRow+','+timeStep+','+attackerObserved+','+attackRow+']';
    }
}
