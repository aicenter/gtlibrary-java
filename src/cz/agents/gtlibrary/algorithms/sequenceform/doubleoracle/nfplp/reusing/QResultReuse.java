package cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.nfplp.reusing;

import cz.agents.gtlibrary.interfaces.Sequence;

import java.util.Map;
import java.util.Set;

public class QResultReuse {

    private double gameValue;
    private Set<Sequence> lastItSeq;
    private Map<Sequence, Double> explSeqSum;
    private Map<Sequence, Double> realizationPlan;

    public QResultReuse(double gameValue, Map<Sequence, Double> explSeqSum, Set<Sequence> lastItSeq, Map<Sequence, Double> realizationPlan) {
        this.gameValue = gameValue;
        this.explSeqSum = explSeqSum;
        this.lastItSeq = lastItSeq;
        this.realizationPlan = realizationPlan;
    }

    public double getGameValue() {
        return gameValue;
    }

    public Map<Sequence, Double> getExplSeqSum() {
        return explSeqSum;
    }

    public Set<Sequence> getLastItSeq() {
        return lastItSeq;
    }

    public Map<Sequence, Double> getRealizationPlan() {
        return realizationPlan;
    }
}
