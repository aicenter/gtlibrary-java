package cz.agents.gtlibrary.domain.poker.generic;

public class GenSumGPGameState extends GenericPokerGameState {
    private final double rake = .1;

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

}
