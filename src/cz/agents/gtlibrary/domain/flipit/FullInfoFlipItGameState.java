package cz.agents.gtlibrary.domain.flipit;

import cz.agents.gtlibrary.iinodes.ArrayListSequenceImpl;
import cz.agents.gtlibrary.iinodes.HistoryImpl;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.iinodes.PerfectRecallISKey;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Sequence;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jakub Cerny on 29/06/2017.
 */
public class FullInfoFlipItGameState extends NoInfoFlipItGameState {

    protected List<Action> sequenceForBothPlayers;


    public FullInfoFlipItGameState(){
//        this.history = new HistoryImpl(players);
        super();
        sequenceForBothPlayers = new ArrayList<Action>();
    }

    public FullInfoFlipItGameState(FullInfoFlipItGameState state){
        super(state);
        sequenceForBothPlayers = new ArrayList<>(state.sequenceForBothPlayers);
    }

    @Override
    public void transformInto(GameState state){
        super.transformInto(state);
        sequenceForBothPlayers = new ArrayList<>(((FullInfoFlipItGameState)state).sequenceForBothPlayers);
    }

    @Override
    public double[] evaluate(){
        double[] utilities = new double[2+FlipItGameInfo.numTypes];
        utilities[0] += defenderReward;
        for (int i = 0;  i < FlipItGameInfo.numTypes; i++){
            utilities[i+1] += attackerReward[i];
        }
        utilities[utilities.length-1] = 0.0;
        if (FlipItGameInfo.ZERO_SUM_APPROX){
            double attackerCosts = 0.0;
            for (Action action : getSequenceFor(FlipItGameInfo.ATTACKER)){
                if (((FlipItAction)action).getControlNode()!= null){
                    attackerCosts += FlipItGameInfo.graph.getControlCost(((FlipItAction)action).getControlNode());
                }
            }
            utilities[0] += attackerCosts;
            for (int i = 0;  i < FlipItGameInfo.numTypes; i++){
                utilities[i+1] = -utilities[0];
            }
        }
        return utilities;
    }

    @Override
    public GameState copy() {
        return new FullInfoFlipItGameState(this);
    }

    @Override
    public ISKey getISKeyForPlayerToMove() {
        switch (currentPlayerIndex) {
            case 0:
                return new PerfectRecallISKey(new HashCodeBuilder().append(sequenceForBothPlayers)
                        .append(getSequenceForPlayerToMove()).toHashCode(), getSequenceForPlayerToMove());
            case 1:
                return new PerfectRecallISKey(new HashCodeBuilder().append(sequenceForBothPlayers)
                        .append(getSequenceForPlayerToMove()).toHashCode(), getSequenceForPlayerToMove());
        }
        return new PerfectRecallISKey(0, new ArrayListSequenceImpl(history.getSequenceOf(getPlayerToMove())));
    }

    @Override
    protected boolean attackerHasEnoughPointsToControl(){
        return true;
    }

    @Override
    protected void endRound() {
        super.endRound();
        sequenceForBothPlayers.add(getSequenceFor(players[0]).getLast());
        sequenceForBothPlayers.add(getSequenceFor(players[1]).getLast());
    }

    @Override
    public void setDepth(int depth) {
        this.depth = depth + round;
//        throw new UnsupportedOperationException("Depth cannot be set.");
    }

}
