package cz.agents.gtlibrary.algorithms.sequenceform.refinements.nfp.reusing;

import java.util.Map;
import java.util.Set;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Sequence;

public class P2PBuilder extends InitialP2PBuilder {
	
	private Set<Sequence> lastItSeq;
	private Map<Sequence, Double> explSeqSum;
	private double initialValueOfGame;

	public P2PBuilder(Expander<SequenceInformationSet> expander, GameState rootState, IterationData data, double initialValueOfGame) {
		super(expander, rootState);
		this.lastItSeq = data.getLastItSeq();
		this.explSeqSum = data.getExplSeqSum();
		this.initialValueOfGame = initialValueOfGame;
		lpFileName = "recP2pLP.lp";
	}
	
	@Override
	public void initTable() {
		super.initTable();
		addPreviousItConstraints();
	}
	
	private void addPreviousItConstraints() {
		lpTable.setConstraint("prevIt", players[0], 1);
		lpTable.setConstant("prevIt", initialValueOfGame);
		lpTable.setConstraintType("prevIt", 1);
	}

	@Override
	public void initObjective(Sequence p2EmptySequence) {
		lpTable.setObjective("t", 1);
	}

	@Override
	public void updateLPForFirstPlayer(GameState state) {
		super.updateLPForFirstPlayer(state);
		Object eqKey = state.getSequenceForPlayerToMove();

		if (lastItSeq.contains(eqKey))
			lpTable.setConstraint(eqKey, "t", 1);
		Double value = explSeqSum.get(eqKey);

		if (value != null)
			lpTable.setConstant(eqKey, -value);
	}

	@Override
	protected void updateP1Parent(GameState state) {
		super.updateP1Parent(state);
		Sequence p1Sequence = state.getSequenceFor(players[0]);

		if (p1Sequence.size() == 0)
			return;
		if (lastItSeq.contains(p1Sequence))
			lpTable.setConstraint(p1Sequence, "t", 1);
		Double value = explSeqSum.get(p1Sequence);

		if (value != null)
			lpTable.setConstant(p1Sequence, -value);
	}

}
