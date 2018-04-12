package cz.agents.gtlibrary.domain.randomgameimproved.observationvariants;

import cz.agents.gtlibrary.iinodes.Observations;
import cz.agents.gtlibrary.interfaces.Observation;
import cz.agents.gtlibrary.interfaces.Player;

import java.util.List;

public enum ObservationsType {
    FORGETFUL {
        @Override
        public Observations getObservations(Player observingPlayer, Player observedPlayer) {
            return new ForgetfulObservations(observingPlayer, observedPlayer);
        }

        @Override
        public Observations getObservations(List<Observation> observationList, Player observingPlayer, Player observedPlayer) {
            return new ForgetfulObservations(observationList, observingPlayer, observedPlayer);
        }
    },
    HASHED_SUM {
        @Override
        public Observations getObservations(Player observingPlayer, Player observedPlayer) {
            return new HashedObservations(observingPlayer, observedPlayer);
        }

        @Override
        public Observations getObservations(List<Observation> observationList, Player observingPlayer, Player observedPlayer) {
            return new HashedObservations(observationList, observingPlayer, observedPlayer);
        }
    };

    public abstract Observations getObservations(Player observingPlayer, Player observedPlayer);

    public abstract Observations getObservations(List<Observation> observationList, Player observingPlayer, Player observedPlayer);
}

