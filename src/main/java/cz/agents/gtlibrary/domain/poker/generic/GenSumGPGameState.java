package cz.agents.gtlibrary.domain.poker.generic;

import cz.agents.gtlibrary.interfaces.GameState;

public class GenSumGPGameState extends GenericPokerGameState {
    private final double rake = .1;

    public GenSumGPGameState() {
    }

    public GenSumGPGameState(GenSumGPGameState gameState) {
        super(gameState);
    }

    @Override
    public double[] getUtilities() {
        if (utilities != null)
            return utilities;
        if (isGameEnd()) {
            int result = hasPlayerOneWon();

            if (result > 0)
                utilities = new double[]{(1 - rake) * gainForFirstPlayer + GPGameInfo.p1cardBounties.get(playerCards[0].getActionType()), -gainForFirstPlayer, 0};
            else if (result == 0)
                utilities = new double[]{0, 0, 0};
            else
                utilities = new double[]{gainForFirstPlayer - pot, (1 - rake) * (pot - gainForFirstPlayer) + GPGameInfo.p2cardBounties.get(playerCards[1].getActionType()), 0};
            return utilities;
        }
        return new double[]{0};
    }

    @Override
    public GameState copy() {
        return new GenSumGPGameState(this);
    }
}
