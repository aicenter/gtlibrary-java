package cz.agents.gtlibrary.domain.randomgameimproved.ir;

import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameState;
import cz.agents.gtlibrary.iinodes.*;
import cz.agents.gtlibrary.interfaces.GameState;

public class PerfectIRRandomGameState extends RandomGameState{
    public PerfectIRRandomGameState() {
        super();
    }

    public PerfectIRRandomGameState(PerfectIRRandomGameState gameState) {
        super(gameState);
    }

    @Override
    public ISKey getISKeyForPlayerToMove() {
        Observations observations = new Observations(getPlayerToMove(), getPlayerToMove());

        observations.add(new PerfectRecallObservation((PerfectRecallISKey) super.getISKeyForPlayerToMove()));
        return new ImperfectRecallISKey(observations, null, null);
    }

    @Override
    public GameState copy() {
        return new PerfectIRRandomGameState(this);
    }
}
