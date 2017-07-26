package cz.agents.gtlibrary.domain.flipit;

import cz.agents.gtlibrary.algorithms.flipit.iskeys.FlipItPerfectRecallISKey;
import cz.agents.gtlibrary.iinodes.ArrayListSequenceImpl;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.iinodes.ImperfectRecallISKey;
import cz.agents.gtlibrary.iinodes.PerfectRecallISKey;
import cz.agents.gtlibrary.interfaces.GameState;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.ArrayList;

/**
 * Created by Jakub Cerny on 29/06/2017.
 */
public class AllPointsFlipItGameState extends NoInfoFlipItGameState {


    public AllPointsFlipItGameState(){
        super();
    }

    public AllPointsFlipItGameState(AllPointsFlipItGameState state){
        super(state);
    }

    @Override
    public GameState copy() {
        return new AllPointsFlipItGameState(this);
    }

    @Override
    public boolean isDepthLimit() {
        return round >= depth;
    }

    @Override
    protected boolean attackerHasEnoughPointsToControl(){
        return true;
    }

    @Override
    public ISKey getISKeyForPlayerToMove() {
        if (FlipItGameInfo.USE_ISKEY_WITH_OBSERVATIONS) {
            ArrayList<Double> observations;
            if (history == null || history.getLength() == 0 || getSequenceForPlayerToMove().isEmpty())
                observations = new ArrayList<>();
            else {
                observations = (ArrayList) ((FlipItPerfectRecallISKey) getSequenceForPlayerToMove().getLastInformationSet().getISKey()).getObservation();
            }
            switch (currentPlayerIndex) {
                case 0:
                    long temp = Double.doubleToLongBits(defenderReward);
                    observations.add(defenderReward);
                    return new FlipItPerfectRecallISKey(observations, new HashCodeBuilder().append(temp).append(getSequenceForPlayerToMove()).toHashCode(), getSequenceForPlayerToMove());
                case 1:
                    temp = Double.doubleToLongBits(attackerReward[0]);
                    observations.add(attackerPoints);
                    return new FlipItPerfectRecallISKey(observations, new HashCodeBuilder().append(temp).append(getSequenceForPlayerToMove()).toHashCode(), getSequenceForPlayerToMove());
            }
            return new PerfectRecallISKey(0, new ArrayListSequenceImpl(history.getSequenceOf(getPlayerToMove())));
        }
        else{
            switch (currentPlayerIndex) {
                case 0:
                    long temp = Double.doubleToLongBits(defenderReward);
                    return new PerfectRecallISKey(new HashCodeBuilder().append(temp).append(getSequenceForPlayerToMove()).toHashCode(), getSequenceForPlayerToMove());
                case 1:
                    temp = Double.doubleToLongBits(attackerPoints);
                    return new PerfectRecallISKey(new HashCodeBuilder().append(temp).append(getSequenceForPlayerToMove()).toHashCode(), getSequenceForPlayerToMove());
            }
            return new PerfectRecallISKey(0, new ArrayListSequenceImpl(history.getSequenceOf(getPlayerToMove())));

        }
    }


}
