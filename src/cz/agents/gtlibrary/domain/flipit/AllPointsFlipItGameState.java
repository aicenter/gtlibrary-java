package cz.agents.gtlibrary.domain.flipit;

import cz.agents.gtlibrary.algorithms.flipit.iskeys.FlipItPerfectRecallISKey;
import cz.agents.gtlibrary.iinodes.*;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Observation;
import cz.agents.gtlibrary.interfaces.Player;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.ArrayList;

/**
 * Created by Jakub Cerny on 29/06/2017.
 */
public class AllPointsFlipItGameState extends NoInfoFlipItGameState {

    int hash = 0;

    public AllPointsFlipItGameState(){
        super();
    }

    public AllPointsFlipItGameState(AllPointsFlipItGameState state){
        super(state);
        this.hash = state.hash;
    }

    public void setHash(int hs){ hash = hs;}

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
        if (FlipItGameInfo.PERFECT_RECALL) {
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
            } else {
                switch (currentPlayerIndex) {
                    case 0:
                        long temp = Double.doubleToLongBits(defenderReward);
                        return new PerfectRecallISKey(new HashCodeBuilder().append(hash).append(temp).append(getSequenceForPlayerToMove()).toHashCode(), getSequenceForPlayerToMove());
                    case 1:
                        temp = Double.doubleToLongBits(attackerPoints);
                        return new PerfectRecallISKey(new HashCodeBuilder().append(temp).append(getSequenceForPlayerToMove()).toHashCode(), getSequenceForPlayerToMove());
                }
                return new PerfectRecallISKey(0, new ArrayListSequenceImpl(history.getSequenceOf(getPlayerToMove())));

            }
        } else {
            Player player = getPlayerToMove();
            Observations observations = new Observations(player, player);
            switch (currentPlayerIndex) {
                case 0:
                    long temp = Double.doubleToLongBits(defenderReward);
                    observations.add(new ObservationImpl((int)temp));
                    for (Action a : getSequenceForPlayerToMove())
                        observations.add(new ObservationImpl(((FlipItAction)a).isNoop() ? -1 : ((FlipItAction)a).getControlNode().getIntID()));
                    return new ImperfectRecallISKey(observations, null, null);
                case 1:
                    temp = Double.doubleToLongBits(attackerPoints);
                    observations.add(new ObservationImpl((int)temp));
                    for (Action a : getSequenceForPlayerToMove())
                        observations.add(new ObservationImpl(((FlipItAction)a).isNoop() ? -1 : ((FlipItAction)a).getControlNode().getIntID()));
                    return new ImperfectRecallISKey(observations, null, null);
            }
            return new PerfectRecallISKey(0, new ArrayListSequenceImpl(history.getSequenceOf(getPlayerToMove())));

        }
    }


}
