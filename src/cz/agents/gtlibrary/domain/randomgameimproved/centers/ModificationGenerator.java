package cz.agents.gtlibrary.domain.randomgameimproved.centers;

import cz.agents.gtlibrary.utils.HighQualityRandom;

public interface ModificationGenerator {
    public double generateUtility(HighQualityRandom rnd);

    public ModificationGenerator copy();

    public double generateCorrelatedUtility(HighQualityRandom rnd, double p1Value);
}
