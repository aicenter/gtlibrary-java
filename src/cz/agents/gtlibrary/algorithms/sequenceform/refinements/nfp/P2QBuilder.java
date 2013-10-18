package cz.agents.gtlibrary.algorithms.sequenceform.refinements.nfp;

import ilog.concert.IloException;
import ilog.cplex.IloCplex.UnknownObjectException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPData;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Sequence;

public class P2QBuilder extends InitialP2QBuilder {

	private Map<Sequence, Double> explSeqSum;

	public P2QBuilder(Expander<SequenceInformationSet> expander, GameState rootState, double initialValue, double gameValue, IterationData data) {
		super(expander, rootState, initialValue);
		this.explSeqSum = getSum(data.getLastItSeq(), data.getExplSeqSum(), gameValue);
		lpFileName = "P2qLP.lp";
	}

	@Override
	public void updateLPForFirstPlayer(GameState state) {
		super.updateLPForFirstPlayer(state);
		Object p1Sequence = state.getSequenceForPlayerToMove();
		Double value = explSeqSum.get(p1Sequence);

		if (value != null)
			lpTable.setConstraint(p1Sequence, "s", explSeqSum.get(p1Sequence));
	}

	@Override
	protected void updateP1Parent(GameState state) {
		super.updateP1Parent(state);
		Sequence p1Sequence = state.getSequenceFor(players[0]);

		if (p1Sequence.size() == 0)
			return;
		Double value = explSeqSum.get(p1Sequence);

		if (value != null)
			lpTable.setConstraint(p1Sequence, "s", explSeqSum.get(p1Sequence));
	}

	@Override
	protected Map<Sequence, Double> getSum(Set<Sequence> exploitableSequences, Map<Sequence, Double> explSeqSum, double valueOfGame) {
		Map<Sequence, Double> updatedSum = new HashMap<Sequence, Double>(explSeqSum);

		for (Sequence sequence : exploitableSequences) {
			Double oldValue = updatedSum.get(sequence);
			
			updatedSum.put(sequence, (oldValue == null?0:oldValue) + valueOfGame);
		}
		return updatedSum;
	}
	
	@Override
	protected IterationData createIterationData(LPData lpData) throws UnknownObjectException, IloException {
		Map<Sequence, Double> watchedSequenceValues = getWatchedUSequenceValues(lpData);
		Set<Sequence> exploitableSequences = getExploitableSequences(watchedSequenceValues);
		Map<Sequence, Double> realizationPlan = getRealizationPlan(lpData);
		
		return new IterationData(lpData.getSolver().getObjValue(), explSeqSum, exploitableSequences, realizationPlan);
	}
}
