package cz.agents.gtlibrary.domain.randomgame;

import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.nfg.simalphabeta.SimABInformationSet;
import cz.agents.gtlibrary.utils.HighQualityRandom;

import java.util.Random;

/**
 * Created by Jakub Cerny on 13/11/2017.
 */
public class GenSumSimRandomGameState extends SimRandomGameState {

    public GenSumSimRandomGameState() {
        super();
    }

    public GenSumSimRandomGameState(GenSumSimRandomGameState gameState) {
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
        double bound1;
        double bound2;

        if (RandomGameInfo.CORRELATION == 0) {
            bound1 = -RandomGameInfo.MAX_UTILITY;
            bound2 = RandomGameInfo.MAX_UTILITY;
        } else {
            bound1 = (p1Value + RandomGameInfo.MAX_UTILITY) * RandomGameInfo.CORRELATION - Math.signum(RandomGameInfo.CORRELATION) * RandomGameInfo.MAX_UTILITY;
            bound2 = (p1Value - RandomGameInfo.MAX_UTILITY) * RandomGameInfo.CORRELATION + Math.signum(RandomGameInfo.CORRELATION) * RandomGameInfo.MAX_UTILITY;
        }
        double upperBound = Math.max(bound1, bound2);
        double lowerBound = Math.min(bound1, bound2);

        assert lowerBound <= upperBound;
        if (Math.abs(lowerBound - upperBound) < 1e-8)
            return new double[]{p1Value, lowerBound};
        return new double[]{p1Value, lowerBound + (upperBound - lowerBound) * random.nextDouble()};
    }

    private double[] getRandomBinaryUtilities() {
        double[] utility = getRandomUtilities();

        utility[0] = Math.round(utility[0]);
        utility[1] = Math.round(utility[1]);
        return utility;
    }

    @Override
    public GameState copy() {
        return new GenSumSimRandomGameState(this);
    }


}
