package cz.agents.gtlibrary.domain.poker.kuhn;

import cz.agents.gtlibrary.interfaces.GameState;

public class GenSumKuhnPokerGameState extends KuhnPokerGameState {
    private final double rake = .1;

    public GenSumKuhnPokerGameState() {
    }

    public GenSumKuhnPokerGameState(GenSumKuhnPokerGameState gameState) {
        super(gameState);
    }

    @Override
    public double[] getUtilities() {
        if (utilities != null) {
            return utilities;
        }
        if (isGameEnd()) {
            int result = hasPlayerOneWon();

            if (result > 0)
                utilities = new double[]{(1 - rake) * gainForFirstPlayer, -gainForFirstPlayer, 0};
            else if (result == 0)
                utilities = new double[]{0, 0, 0};
            else
                utilities = new double[]{gainForFirstPlayer - pot, (1 - rake) * (pot - gainForFirstPlayer), 0};
            return utilities;
        }
        return new double[]{0};
    }

    @Override
    public GameState copy() {
        return new GenSumKuhnPokerGameState(this);
    }
}
