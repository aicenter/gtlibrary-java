package cz.agents.gtlibrary.iinodes;

import java.io.Serializable;

public class ImperfectRecallISKey extends ISKey implements Serializable {
    /**
     * Requires the list of observations of the current player to move and the list of observations of the rest of the players.
     * In order to make sure that the resulting game wont have absent mindedness all the length of the observations of the current
     * player must be the same as the number of actions made by this player (need not be this way for the rest of observations)
     */
    public ImperfectRecallISKey(Observations ownObservations, Observations opponentObservations, Observations natureObservations) {
        super(ownObservations, opponentObservations, natureObservations);
    }

    public Observations getOwnObservations() {
        return (Observations) objects[0];
    }

    public Observations getOpponentObservations() {
        return (Observations) objects[1];
    }

    public Observations getNatureObservations() {
        return (Observations) objects[2];
    }
}
