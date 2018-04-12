package cz.agents.gtlibrary.algorithms.sequenceform.refinements.librarycom.resultparser;

import cz.agents.gtlibrary.interfaces.Sequence;

import java.util.Map;

public interface ResultParser {
    public Map<Sequence, Double> getP1RealizationPlan();

    public Map<Sequence, Double> getP2RealizationPlan();

    public double getGameValue();

    public long getTime();
}
