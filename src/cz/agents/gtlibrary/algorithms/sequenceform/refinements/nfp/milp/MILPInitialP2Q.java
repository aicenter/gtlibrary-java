package cz.agents.gtlibrary.algorithms.sequenceform.refinements.nfp.milp;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.nfp.InitialP2QBuilder;
import cz.agents.gtlibrary.iinodes.ArrayListSequenceImpl;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Sequence;

public class MILPInitialP2Q extends InitialP2QBuilder {

	public MILPInitialP2Q(Expander<SequenceInformationSet> expander, GameState rootState, double initialValue) {
		super(expander, rootState, initialValue);
	}

	@Override
	public void initTable() {
		Sequence p1EmptySequence = new ArrayListSequenceImpl(players[0]);
		Sequence p2EmptySequence = new ArrayListSequenceImpl(players[1]);

		lpTable = new MILPNFPTable();

		initE(p1EmptySequence);
		initF(p2EmptySequence);
		initf();
		addPreviousItConstraints(p2EmptySequence);
	}

}
