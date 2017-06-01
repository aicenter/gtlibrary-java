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
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Sequence;

import java.util.Map;
import java.util.Set;

public class P2PBuilder extends InitialP2PBuilder {
	
	private Set<Sequence> lastItSeq;
	private Map<Sequence, Double> explSeqSum;
	private double initialValueOfGame;

	public P2PBuilder(Expander<SequenceInformationSet> expander, GameState rootState, IterationData data, GameInfo info, double initialValueOfGame) {
		super(expander, rootState, info);
		this.lastItSeq = data.getLastItSeq();
		this.explSeqSum = data.getExplSeqSum();
		this.initialValueOfGame = initialValueOfGame;
		lpFileName = "P2pLP.lp";
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
