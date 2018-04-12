package cz.agents.gtlibrary.domain.nonlocality;

import cz.agents.gtlibrary.algorithms.sequenceform.refinements.quasiperfect.numbers.Rational;
import cz.agents.gtlibrary.iinodes.GameStateImpl;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.iinodes.PerfectRecallISKey;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.Pair;

/**
 * The example of nonlocality from AAAI-14 CPW paper.
 * @author vilo
 */
public class NonLocState extends GameStateImpl {

    public NonLocState() {
        super(NonLocInfo.ALL_PLAYERS);
    }

    public NonLocState(NonLocState gameState) {
        super(gameState);
    }

    @Override
    public Player getPlayerToMove() {
        if (getSequenceFor(NonLocInfo.NATURE).size()==0) return NonLocInfo.NATURE;
        else if (getSequenceFor(NonLocInfo.FIRST_PLAYER).size()==0) return NonLocInfo.FIRST_PLAYER;
        else return NonLocInfo.SECOND_PLAYER;
    }

    @Override
    public GameState copy() {
        return new NonLocState(this);
    }

    @Override
    public double[] getUtilities() {
        if (!isGameEnd()) return null;
        if (((NonLocAction) getSequenceFor(players[2]).getLast()).getType().startsWith("L")){
            if (((NonLocAction) getSequenceFor(players[0]).getLast()).getType().startsWith("L")) return new double[]{1, -1};
            else return new double[]{0, 0};
        } else {
            if (((NonLocAction) getSequenceFor(players[0]).getLast()).getType().startsWith("L")){
                if (((NonLocAction) getSequenceFor(players[1]).getLast()).getType().startsWith("L")) return new double[]{3, -3};
                else return new double[]{0, 0};
            } else {
                if (((NonLocAction) getSequenceFor(players[1]).getLast()).getType().startsWith("L")) return new double[]{0, 0};
                else return new double[]{3, -3};
            }
        }
    }

    @Override
    public double getProbabilityOfNatureFor(Action action) {
        return 0.5;
    }

    @Override
    public Rational getExactProbabilityOfNatureFor(Action action) {
        return new Rational(1,2);
    }

    @Override
    public boolean isGameEnd() {
        if (getSequenceFor(players[2]).size()==0) return false;
        return  ((NonLocAction) getSequenceFor(players[2]).getLast()).getType().startsWith("L") && getSequenceFor(players[0]).size()==1
                || ((NonLocAction) getSequenceFor(players[2]).getLast()).getType().startsWith("R") && getSequenceFor(players[1]).size()==1;
    }

    @Override
    public boolean isPlayerToMoveNature() {
        return getSequenceFor(players[2]).size()==0;
    }

    @Override
    public ISKey getISKeyForPlayerToMove() {
        return new PerfectRecallISKey(0, getSequenceForPlayerToMove());
    }

    @Override
    public int hashCode() {
        return history.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NonLocState)) return false;
        NonLocState that = (NonLocState) o;

        if (!history.equals(that.history))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return history.toString();
    }
}
