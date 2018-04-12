package cz.agents.gtlibrary.domain.randomgameimproved.centers;

import cz.agents.gtlibrary.utils.HighQualityRandom;

public class IncrementalChangeModificationGenerator implements ModificationGenerator {
    private int maxModification;
    private int increment;

    public IncrementalChangeModificationGenerator(int initialMaxModification, int increment) {
        this.maxModification = initialMaxModification;
        this.increment = increment;
    }

    public IncrementalChangeModificationGenerator(IncrementalChangeModificationGenerator generator) {
        this.maxModification = generator.maxModification;
        this.increment = generator.increment;
    }

    @Override
    public double generateUtility(HighQualityRandom rnd) {
        double modification = rnd.nextInt(2*maxModification + 1) - maxModification;
        maxModification += increment;
        return modification;
    }

    @Override
    public double generateCorrelatedUtility(HighQualityRandom rnd, double p1Value) {
        throw new UnsupportedOperationException("Correlated utility not supported by this type of utility generator.");
    }

    @Override
    public ModificationGenerator copy() {
        return new IncrementalChangeModificationGenerator(this);
    }
}
