package cz.agents.gtlibrary.domain.randomgameimproved.centers;

import cz.agents.gtlibrary.utils.HighQualityRandom;

public class BoundedUniformModificationGenerator implements ModificationGenerator{
    private int maxModification;
    private int previousModification;
    private int maxModificationChange;

    public BoundedUniformModificationGenerator(int maxModification, int maxModificationChange) {
        this.maxModification = maxModification;
        this.maxModificationChange = maxModificationChange;
        this.previousModification = 0;
    }

    public BoundedUniformModificationGenerator(BoundedUniformModificationGenerator generator) {
        this.maxModification = generator.maxModification;
        this.previousModification = generator.previousModification;
        this.maxModificationChange = generator.maxModificationChange;
    }

    @Override
    public double generateUtility(HighQualityRandom rnd) {
        int rawModification = rnd.nextInt(2*maxModificationChange + 1) - maxModificationChange + previousModification;
        previousModification = Math.max(Math.min(rawModification, maxModification), -maxModification);
        return previousModification;
    }

    @Override
    public double generateCorrelatedUtility(HighQualityRandom rnd, double p1Value) {
        throw new UnsupportedOperationException("Correlated utility not supported by this type of utility generator.");
    }

    @Override
    public ModificationGenerator copy() {
        return new BoundedUniformModificationGenerator(this);
    }
}
