package cz.agents.gtlibrary.algorithms.sequenceform.refinements.nfp.reusing;

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

public class QUpdater extends InitialQBuilder {

	private Map<Sequence, Double> explSeqSum;

	public QUpdater(Expander<SequenceInformationSet> expander, GameState rootState, double initialValue, RecyclingNFPTable lpTable) {
		super(expander, rootState, initialValue);
		lpFileName = "qLP.lp";
		this.lpTable = lpTable;
	}

	@Override
	public void updateLPForSecondPlayer(GameState state) {
		Object p2Sequence = state.getSequenceForPlayerToMove();
		Double value = explSeqSum.get(p2Sequence);

		if (value != null)
			lpTable.setConstraint(p2Sequence, "s", explSeqSum.get(p2Sequence));
	}
	
	@Override
	public void initTable() {
	}
	
	public void buildLP(IterationData data, double gameValue) {
		this.explSeqSum = getSum(data.getLastItSeq(), data.getExplSeqSum(), gameValue);
		super.buildLP();
	}

	@Override
	protected void updateP2Parent(GameState state) {
		Sequence p2Sequence = state.getSequenceFor(players[1]);

//		if (p2Sequence.size() == 0)
//			return;
		Double value = explSeqSum.get(p2Sequence);

		if (value != null)
			lpTable.setConstraint(p2Sequence, "s", explSeqSum.get(p2Sequence));
	}
	
	@Override
	public void updateLPForFirstPlayer(GameState state) {
	}
	
	@Override
	protected void updateP1Parent(GameState state) {
	}
	
	@Override
	protected void visitLeaf(GameState state) {
		updateParentLinks(state);
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
