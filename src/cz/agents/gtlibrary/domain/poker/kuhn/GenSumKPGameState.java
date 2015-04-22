package cz.agents.gtlibrary.domain.poker.kuhn;

import cz.agents.gtlibrary.interfaces.GameState;

public class GenSumKPGameState extends KuhnPokerGameState {
    protected double rake;

    public GenSumKPGameState(double rake) {
        this.rake = rake;
    }

    public GenSumKPGameState(GenSumKPGameState gameState) {
        super(gameState);
    }

    @Override
    public double[] getUtilities() {
        if (utilities != null) {
            return copy(utilities);
        }
        if (isGameEnd()) {
            int result = hasPlayerOneWon();

            if (result > 0)
                utilities = new double[]{(1 - rake) * gainForFirstPlayer, -gainForFirstPlayer, 0};
            else if (result == 0)
                utilities = new double[]{0, 0, 0};
            else
                utilities = new double[]{gainForFirstPlayer - pot, (1 - rake) * (pot - gainForFirstPlayer), 0};
            return copy(utilities);
        }
        return new double[]{0};
    }

    protected double[] copy(double[] utilities) {
        double[] copy = new double[utilities.length];

        for (int i = 0; i < utilities.length; i++) {
            copy[i] = utilities[i];
        }
        return copy;
    }


    @Override
    public GameState copy() {
        return new GenSumKPGameState(this);
    }
}
