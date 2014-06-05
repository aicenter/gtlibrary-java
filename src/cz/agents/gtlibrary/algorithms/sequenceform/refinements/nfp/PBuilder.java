package cz.agents.gtlibrary.algorithms.sequenceform.refinements.nfp;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Sequence;

import java.util.Map;
import java.util.Set;

public class PBuilder extends InitialPBuilder {

	private Set<Sequence> lastItSeq;
	private Map<Sequence, Double> explSeqSum;
	private double initialValueOfGame;

	public PBuilder(Expander<SequenceInformationSet> expander, GameState rootState, IterationData data, double initialValueOfGame) {
		super(expander, rootState);
		this.lastItSeq = data.getLastItSeq();
		this.explSeqSum = data.getExplSeqSum();
		this.initialValueOfGame = initialValueOfGame;
		lpFileName = "pLP.lp";
	}
	
	@Override
	public void initTable() {
		super.initTable();
		addPreviousItConstraints();
	}
	
	private void addPreviousItConstraints() {
		lpTable.setConstraint("prevIt", players[1], 1);
		lpTable.setConstant("prevIt", initialValueOfGame);
		lpTable.setConstraintType("prevIt", 1);
	}

	@Override
	public void initObjective(Sequence p2EmptySequence) {
		lpTable.setObjective("t", 1);
	}

	@Override
	public void updateLPForSecondPlayer(GameState state) {
		super.updateLPForSecondPlayer(state);
		Object eqKey = state.getSequenceForPlayerToMove();

		if (lastItSeq.contains(eqKey))
			lpTable.setConstraint(eqKey, "t", 1);
		Double value = explSeqSum.get(eqKey);

		if (value != null)
			lpTable.setConstant(eqKey, -value);
	}

	@Override
	protected void updateP2Parent(GameState state) {
		super.updateP2Parent(state);
		Sequence p2Sequence = state.getSequenceFor(players[1]);

		if (p2Sequence.size() == 0)
			return;
		if (lastItSeq.contains(p2Sequence))
			lpTable.setConstraint(p2Sequence, "t", 1);
		Double value = explSeqSum.get(p2Sequence);

		if (value != null)
			lpTable.setConstant(p2Sequence, -value);
	}
}
