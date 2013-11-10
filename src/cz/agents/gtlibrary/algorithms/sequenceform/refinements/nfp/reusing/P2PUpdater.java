package cz.agents.gtlibrary.algorithms.sequenceform.refinements.nfp.reusing;

import java.util.Map;
import java.util.Set;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Sequence;

public class P2PUpdater extends InitialP2PBuilder {
	
	private Set<Sequence> lastItSeq;
	private Map<Sequence, Double> explSeqSum;

	public P2PUpdater(Expander<SequenceInformationSet> expander, GameState rootState, RecyclingNFPTable table) {
		super(expander, rootState);
		lpTable = table;
		lpFileName = "recP2pLP.lp";
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
	public void updateLPForFirstPlayer(GameState state) {
		Object eqKey = state.getSequenceForPlayerToMove();

		if (lastItSeq.contains(eqKey))
			lpTable.setConstraint(eqKey, "t", 1);
		else
			lpTable.removeFromConstraint(eqKey, "t");
		Double value = explSeqSum.get(eqKey);

		if (value != null)
			lpTable.setConstant(eqKey, -value);
	}

	@Override
	protected void updateP1Parent(GameState state) {
		Sequence p1Sequence = state.getSequenceFor(players[0]);

		if (p1Sequence.size() == 0)
			return;
		if (lastItSeq.contains(p1Sequence))
			lpTable.setConstraintIfNotPresent(p1Sequence, "t", 1);
		else
			lpTable.removeFromConstraint(p1Sequence, "t");
		Double value = explSeqSum.get(p1Sequence);

		if (value != null)
			lpTable.setConstant(p1Sequence, -value);
	}
	
	@Override
	protected void updateP2Parent(GameState state) {
	}

	@Override
	public void updateLPForSecondPlayer(GameState state) {
	}
	
	@Override
	protected void visitLeaf(GameState state) {
		updateParentLinks(state);
	}
	
}
