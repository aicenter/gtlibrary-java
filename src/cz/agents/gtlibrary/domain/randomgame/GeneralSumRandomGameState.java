package cz.agents.gtlibrary.domain.randomgame;

import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.utils.HighQualityRandom;

import java.util.Random;

public class GeneralSumRandomGameState extends RandomGameState {

    public GeneralSumRandomGameState() {
        super();
    }

    public GeneralSumRandomGameState(GeneralSumRandomGameState gameState) {
        super(gameState);
    }

    /**
     * Currently only non-correlated utilities supported
     *
     * @return
     */
    @Override
    protected double[] getEndGameUtilities() {
        if (RandomGameInfo.UTILITY_CORRELATION)
            throw new UnsupportedOperationException("Correlated utilities not supported");
        if (RandomGameInfo.BINARY_UTILITY)
            return getRandomBinaryUtilities();
        return getRandomUtilities();
    }

    private double[] getRandomUtilities() {
        Random random = new HighQualityRandom(ID);
        double p1Value = random.nextDouble() * 2 * RandomGameInfo.MAX_UTILITY - RandomGameInfo.MAX_UTILITY;
        double lowerBound = Math.signum(RandomGameInfo.CORRELATION) * Math.max(-RandomGameInfo.MAX_UTILITY, p1Value - (-Math.abs(RandomGameInfo.CORRELATION) + 1) * RandomGameInfo.MAX_UTILITY);
        double upperBound = Math.signum(RandomGameInfo.CORRELATION) * Math.min(RandomGameInfo.MAX_UTILITY, p1Value + (-Math.abs(RandomGameInfo.CORRELATION) + 1) * RandomGameInfo.MAX_UTILITY);

        assert lowerBound <= upperBound;
        if (Math.abs(lowerBound - upperBound) < 1e-8)
            return new double[]{p1Value, lowerBound};
        return new double[]{p1Value, lowerBound + (upperBound - lowerBound) * random.nextDouble()};
    }

//    private double[] getRandomBinaryUtilities() {
//        Random random = new HighQualityRandom(ID);
//        int i = (random.nextInt(2 * RandomGameInfo.MAX_UTILITY + 1) - RandomGameInfo.MAX_UTILITY);
//
//        return new double[]{i,
//                Math.min(RandomGameInfo.MAX_UTILITY, Math.max(-RandomGameInfo.MAX_UTILITY, -((int) i * 0.5) + random.nextInt(2 * RandomGameInfo.MAX_UTILITY + 1) - RandomGameInfo.MAX_UTILITY))};
//    }

    private double[] getRandomBinaryUtilities() {
        Random random = new HighQualityRandom(ID);
        int p1Value = (random.nextInt(2 * RandomGameInfo.MAX_UTILITY + 1) - RandomGameInfo.MAX_UTILITY);
        int lowerBound = (int) Math.round(Math.signum(RandomGameInfo.CORRELATION) * Math.max(-RandomGameInfo.MAX_UTILITY, p1Value - (-Math.abs(RandomGameInfo.CORRELATION) + 1) * RandomGameInfo.MAX_UTILITY));
        int upperBound = (int) Math.round(Math.signum(RandomGameInfo.CORRELATION) * Math.min(RandomGameInfo.MAX_UTILITY, p1Value + (-Math.abs(RandomGameInfo.CORRELATION) + 1) * RandomGameInfo.MAX_UTILITY));

        assert lowerBound <= upperBound;
        if (lowerBound == upperBound)
            return new double[]{p1Value, lowerBound};
        return new double[]{p1Value, lowerBound + random.nextInt(upperBound - lowerBound + 1)};
    }

    @Override
    public GameState copy() {
        return new GeneralSumRandomGameState(this);
    }
}
