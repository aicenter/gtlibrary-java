package cz.agents.gtlibrary.algorithms.sequenceform.refinements.nfp.reusing;

import java.util.Map;
import java.util.Set;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Sequence;

public class PUpdater extends InitialPBuilder {

	private Set<Sequence> lastItSeq;
	private Map<Sequence, Double> explSeqSum;

	public PUpdater(Expander<SequenceInformationSet> expander, GameState rootState, RecyclingNFPTable table) {
		super(expander, rootState);
		lpFileName = "recpLP.lp";
		lpTable = table;
	}
	
	public void buildLP(IterationData data) {
		this.lastItSeq = data.getLastItSeq();
		this.explSeqSum = data.getExplSeqSum();
		super.buildLP();
	}
	
	@Override
	public void initTable() {
	}
	
	@Override
	public void initObjective(Sequence p2EmptySequence) {
		lpTable.setObjective("t", 1);
	}

	@Override
	public void updateLPForSecondPlayer(GameState state) {
		Object eqKey = state.getSequenceForPlayerToMove();

		if (lastItSeq.contains(eqKey))
			lpTable.setConstraintIfNotPresent(eqKey, "t", 1);
		else 
			lpTable.removeFromConstraint(eqKey, "t");
		Double value = explSeqSum.get(eqKey);

		if (value != null)
			lpTable.setConstant(eqKey, -value);
	}

	@Override
	protected void updateP2Parent(GameState state) {
		Sequence p2Sequence = state.getSequenceFor(players[1]);

		if (p2Sequence.size() == 0)
			return;
		if (lastItSeq.contains(p2Sequence))
			lpTable.setConstraintIfNotPresent(p2Sequence, "t", 1);
		else
			lpTable.removeFromConstraint(p2Sequence, "t");
		Double value = explSeqSum.get(p2Sequence);

		if (value != null)
			lpTable.setConstant(p2Sequence, -value);
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
}
