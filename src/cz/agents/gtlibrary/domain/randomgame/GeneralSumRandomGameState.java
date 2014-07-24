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

        return new double[]{random.nextDouble() * 2 * RandomGameInfo.MAX_UTILITY - RandomGameInfo.MAX_UTILITY,
                -random.nextDouble() * 2 * RandomGameInfo.MAX_UTILITY + RandomGameInfo.MAX_UTILITY};
    }

    private double[] getRandomBinaryUtilities() {
        Random random = new HighQualityRandom(ID);
        int i = (random.nextInt(2 * RandomGameInfo.MAX_UTILITY + 1) - RandomGameInfo.MAX_UTILITY);

        return new double[]{i,
                Math.min(RandomGameInfo.MAX_UTILITY, Math.max(-RandomGameInfo.MAX_UTILITY, -((int)i*0.5)+random.nextInt(2 * RandomGameInfo.MAX_UTILITY + 1) - RandomGameInfo.MAX_UTILITY))};
    }

    @Override
    public GameState copy() {
        return new GeneralSumRandomGameState(this);
    }
}
