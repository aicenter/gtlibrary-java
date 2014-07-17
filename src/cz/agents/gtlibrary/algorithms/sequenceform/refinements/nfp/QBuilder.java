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

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPData;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Sequence;
import ilog.concert.IloException;
import ilog.cplex.IloCplex.UnknownObjectException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class QBuilder extends InitialQBuilder {

	private Map<Sequence, Double> explSeqSum;

	public QBuilder(Expander<SequenceInformationSet> expander, GameState rootState, GameInfo info, double initialValue, double gameValue, IterationData data) {
		super(expander, rootState, info, initialValue);
		this.explSeqSum = getSum(data.getLastItSeq(), data.getExplSeqSum(), gameValue);
		lpFileName = "qLP.lp";
	}

	@Override
	public void updateLPForSecondPlayer(GameState state) {
		super.updateLPForSecondPlayer(state);
		Object p2Sequence = state.getSequenceForPlayerToMove();
		Double value = explSeqSum.get(p2Sequence);

		if (value != null)
			lpTable.setConstraint(p2Sequence, "s", explSeqSum.get(p2Sequence));
	}

	@Override
	protected void updateP2Parent(GameState state) {
		super.updateP2Parent(state);
		Sequence p2Sequence = state.getSequenceFor(players[1]);

//		if (p2Sequence.size() == 0)
//			return;
		Double value = explSeqSum.get(p2Sequence);

		if (value != null)
			lpTable.setConstraint(p2Sequence, "s", explSeqSum.get(p2Sequence));
	}

	@Override
	protected Map<Sequence, Double> getSum(Set<Sequence> exploitableSequences, Map<Sequence, Double> explSeqSum, double valueOfGame) {
		Map<Sequence, Double> updatedSum = new HashMap<Sequence, Double>(explSeqSum);

		for (Sequence sequence : exploitableSequences) {
			Double value = updatedSum.get(sequence);
			updatedSum.put(sequence, (value == null ? 0 : value) + valueOfGame);
		}
		return updatedSum;
	}

	@Override
	protected IterationData createIterationData(LPData lpData) throws UnknownObjectException, IloException {
		Map<Sequence, Double> watchedSequenceValues = getWatchedUSequenceValues(lpData);
		Set<Sequence> exploitableSequences = getExploitableSequences(watchedSequenceValues);
		Map<Sequence, Double> realizationPlan = getRealizationPlan(lpData);

		return new IterationData(lpData.getSolver().getObjValue(), explSeqSum, exploitableSequences, realizationPlan, getUValues(watchedSequenceValues));
	}

}
