package cz.agents.gtlibrary.algorithms.sequenceform.refinements.nfp;

import java.util.Map;
import java.util.Set;

import cz.agents.gtlibrary.interfaces.Sequence;

public class IterationData {

	private double gameValue;
	private Map<Sequence, Double> explSeqSum;
	private Set<Sequence> lastItSeq;
	private Map<Sequence, Double> realizationPlan;

	public IterationData(double gameValue, Map<Sequence, Double> explSeqSum, Set<Sequence> lastItSeq, Map<Sequence, Double> realizationPlan) {
		super();
		this.gameValue = gameValue;
		this.explSeqSum = explSeqSum;
		this.lastItSeq = lastItSeq;
		this.realizationPlan = realizationPlan;
	}

	public Map<Sequence, Double> getExplSeqSum() {
		return explSeqSum;
	}

	public Set<Sequence> getLastItSeq() {
		return lastItSeq;
	}

	public double getGameValue() {
		return gameValue;
	}
	
	public Map<Sequence, Double> getRealizationPlan() {
		return realizationPlan;
	}
}
