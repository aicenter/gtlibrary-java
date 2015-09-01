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


package cz.agents.gtlibrary.algorithms.sequenceform.refinements.nfp;

import cz.agents.gtlibrary.interfaces.Sequence;

import java.util.Map;
import java.util.Set;

public class IterationData {

	private double gameValue;
	private Map<Sequence, Double> explSeqSum;
	private Set<Sequence> lastItSeq;
	private Map<Sequence, Double> realizationPlan;
	private Map<Sequence, Double> uValues;

	public IterationData(double gameValue, Map<Sequence, Double> explSeqSum, Set<Sequence> lastItSeq, Map<Sequence, Double> realizationPlan, Map<Sequence, Double> uValues) {
		super();
		this.gameValue = gameValue;
		this.explSeqSum = explSeqSum;
		this.lastItSeq = lastItSeq;
		this.realizationPlan = realizationPlan;
		this.uValues = uValues;
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
	
	public Map<Sequence, Double> getUValues() {
		return uValues;
	}
}
