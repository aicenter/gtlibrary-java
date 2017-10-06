package cz.agents.gtlibrary.domain.wichardtne;

import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.iinodes.PerfectRecallISKey;
import cz.agents.gtlibrary.interfaces.GameState;

public class PerfectInformationWichardtState extends WichardtGameState {

    public PerfectInformationWichardtState() {
        super();
    }

    public PerfectInformationWichardtState(PerfectInformationWichardtState gameState) {
        super(gameState);
    }

    @Override
    public ISKey getISKeyForPlayerToMove() {
        if (getPlayerToMove().equals(WichardtGameInfo.FIRST_PLAYER))
            return new PerfectRecallISKey(hashCode(), getSequenceForPlayerToMove());
        return new PerfectRecallISKey(0, getSequenceForPlayerToMove());
    }

    @Override
    public GameState copy() {
        return new PerfectInformationWichardtState(this);
    }
}
