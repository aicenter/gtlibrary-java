package cz.agents.gtlibrary.domain.randomgameimproved.observationvariants;

import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameInfo;
import cz.agents.gtlibrary.iinodes.Observations;
import cz.agents.gtlibrary.interfaces.Observation;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.utils.HighQualityRandom;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class ForgetfulObservations extends Observations {

    public ForgetfulObservations(Player observingPlayer, Player observedPlayer) {
        super(observingPlayer, observedPlayer);
    }

    public ForgetfulObservations(List<Observation> observationList, Player observingPlayer, Player observedPlayer) {
        super(observationList, observingPlayer, observedPlayer);
    }

    @Override
    public void performDepthChangingOperations(int seed) {
        if (getObservedPlayer().equals(getObservingPlayer())) return;
        ListIterator<Observation> iterator = listIterator();
        HighQualityRandom rnd = new HighQualityRandom(seed);
        while (iterator.hasNext()) {
            iterator.next();
            if (rnd.nextDouble() < RandomGameInfo.FORGET_OBSERVATION_PROBABILITY) {
                iterator.remove();
            }
        }
    }

    @Override
    public Observations copy() {
        return new ForgetfulObservations(this, getObservingPlayer(), getObservedPlayer());
    }
}
