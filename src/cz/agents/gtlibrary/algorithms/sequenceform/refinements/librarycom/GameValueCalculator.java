package cz.agents.gtlibrary.algorithms.sequenceform.refinements.librarycom;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;

public class GameValueCalculator {

	private Map<Map<Player, Sequence>, Double> utilityForSeqComb;
	private Map<Sequence, Double> p1RealPlan;
	private Map<Sequence, Double> p2RealPlan;

	public GameValueCalculator(Map<Map<Player, Sequence>, Double> utilityForSeqComb, Map<Sequence, Double> p1RealPlan, Map<Sequence, Double> p2RealPlan) {
		super();
		this.utilityForSeqComb = utilityForSeqComb;
		this.p1RealPlan = p1RealPlan;
		this.p2RealPlan = p2RealPlan;
	}

	public double getGameValue() {
		double gameValue = 0;

		for (Entry<Sequence, Double> p1Entry : p1RealPlan.entrySet()) {
			for (Entry<Sequence, Double> p2Entry : p2RealPlan.entrySet()) {
				Map<Player, Sequence> sequenceComb = new HashMap<Player, Sequence>();

				sequenceComb.put(p1Entry.getKey().getPlayer(), p1Entry.getKey());
				sequenceComb.put(p2Entry.getKey().getPlayer(), p2Entry.getKey());

				Double valueForSeqComb = utilityForSeqComb.get(sequenceComb);

				if (valueForSeqComb != null)
					gameValue += p1Entry.getValue() * p2Entry.getValue() * valueForSeqComb;
			}
		}
		return gameValue;
	}

}
