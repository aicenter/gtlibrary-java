package cz.agents.gtlibrary.domain.simpleGeneralSum;

import cz.agents.gtlibrary.algorithms.sequenceform.refinements.quasiperfect.numbers.Rational;
import cz.agents.gtlibrary.iinodes.GameStateImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.Pair;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Created with IntelliJ IDEA.
 * User: kail
 * Date: 12/5/13
 * Time: 3:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class SimpleGSState extends GameStateImpl {

    protected int depth = 0;
    protected int ID = 0;

    private int hashCode;
    private boolean hashCodeChange = true;
    private Pair<Integer, Sequence> key = null;

    private Player playerToMove;


    public SimpleGSState(Player[] players) {
        super(players);
        playerToMove = players[0];
    }

    public SimpleGSState(SimpleGSState gameState) {
        super(gameState);
        this.playerToMove = gameState.playerToMove;
        this.depth = gameState.depth;
        this.ID = gameState.ID;
    }

    protected void executeAction(SimpleGSAction action) {
        if (action.player.getId() == 0) {
            playerToMove = players[1];
            ID = ID - (action.ID)*SimpleGSInfo.MAX_ACTIONS;
        } else {
            playerToMove = players[0];
            ID = ID - (action.ID);
            depth++;
        }

        if (isGameEnd()) ID = -ID;
        hashCodeChange = true;
        key = null;

    }

    @Override
    public Player getPlayerToMove() {
        return playerToMove;
    }

    @Override
    public GameState copy() {
        return new SimpleGSState(this);
    }

    @Override
    public double[] getUtilities() {
        if (ID < 0) return new double[] {0,0};
        else return SimpleGSInfo.utilityMatrix[ID];
    }

    @Override
    public Rational[] getExactUtilities() {
        throw new UnsupportedOperationException("Not supported...");
    }

    @Override
    public double getProbabilityOfNatureFor(Action action) {
        return 1;
    }


    @Override
    public Rational getExactProbabilityOfNatureFor(Action action) {
        throw new UnsupportedOperationException("Not supported...");
    }

    @Override
    public boolean isGameEnd() {
        return depth >= SimpleGSInfo.MAX_DEPTH;
    }

    @Override
    public boolean isPlayerToMoveNature() {
        return false;
    }

    @Override
    public Pair<Integer, Sequence> getISKeyForPlayerToMove() {
        if (key != null)
            return key;
        key = new Pair<Integer, Sequence>(new HashCodeBuilder().append(isGameEnd()).append(getHistory().getSequenceOf(playerToMove)).toHashCode(), history.getSequenceOf(playerToMove));
        return key;
    }

    @Override
    public int hashCode() {
        if (hashCodeChange) {
            final int prime = 31;

            hashCode = 1;
            hashCode = prime * hashCode + ((history == null) ? 0 : history.hashCode());
            hashCodeChange = false;
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
        if (hashCode() != obj.hashCode())
            return false;
        SimpleGSState other = (SimpleGSState)obj;

        if (!history.equals(other.history))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "SGS:"+ID+":D:"+depth;
    }
}
