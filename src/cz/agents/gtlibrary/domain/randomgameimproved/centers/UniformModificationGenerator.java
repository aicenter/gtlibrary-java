package cz.agents.gtlibrary.domain.randomgameimproved.centers;

import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameInfo;
import cz.agents.gtlibrary.utils.HighQualityRandom;

import java.io.Serializable;

public class UniformModificationGenerator implements ModificationGenerator, Serializable {
    private int maxModification;

    public UniformModificationGenerator(int maxModification) {
        this.maxModification = maxModification;
    }

    private UniformModificationGenerator(UniformModificationGenerator uniformModificationGenerator) {
        this.maxModification = uniformModificationGenerator.maxModification;
    }
    @Override
    public double generateUtility(HighQualityRandom rnd) {
        return rnd.nextInt(2*maxModification + 1) - maxModification;
    }

    @Override
    public double generateCorrelatedUtility(HighQualityRandom rnd, double p1Value) {
        double bound1;
        double bound2;

        if (RandomGameInfo.CORRELATION == 0) {
            bound1 = -maxModification;
            bound2 = maxModification;
        } else {
            bound1 = (p1Value + maxModification) * RandomGameInfo.CORRELATION - Math.signum(RandomGameInfo.CORRELATION) * maxModification;
            bound2 = (p1Value - maxModification) * RandomGameInfo.CORRELATION + Math.signum(RandomGameInfo.CORRELATION) * maxModification;
        }
        double upperBound = Math.max(bound1, bound2);
        double lowerBound = Math.min(bound1, bound2);
        return lowerBound + (upperBound - lowerBound) * rnd.nextDouble();
    }

    @Override
    public ModificationGenerator copy() {
        return new UniformModificationGenerator(this);
    }

}
