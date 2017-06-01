/*
Copyright 2014 Faculty of Electrical Engineering at CTU in Prague

This file is part of Game Theoretic Library.

Game Theoretic Library is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Game Theoretic Library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with Game Theoretic Library.  If not, see <http://www.gnu.org/licenses/>.*/


package cz.agents.gtlibrary.algorithms.sequenceform.refinements.librarycom;

import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

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
