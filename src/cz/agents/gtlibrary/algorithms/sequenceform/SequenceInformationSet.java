package cz.agents.gtlibrary.algorithms.sequenceform;

import java.util.LinkedHashSet;

import cz.agents.gtlibrary.iinodes.InformationSetImpl;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Sequence;

public class SequenceInformationSet extends InformationSetImpl {

	private LinkedHashSet<Sequence> outgoingSequences = new LinkedHashSet<Sequence>();

	public SequenceInformationSet(GameState state) {
		super(state);
	}

	public void addOutgoingSequences(Sequence outgoingSeq) {
		outgoingSequences.add(outgoingSeq);
	}
	
	public LinkedHashSet<Sequence> getOutgoingSequences() {
		return outgoingSequences;
	}
}
