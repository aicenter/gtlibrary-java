package cz.agents.gtlibrary.iinodes;

import java.util.HashMap;
import java.util.Map;

import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;
import cz.agents.gtlibrary.interfaces.Sequence;


public abstract class IIConfig<I extends InformationSet> implements AlgorithmConfig<I> {

	private Map<Long, Map<Sequence, I>> allInformationSets;

	public IIConfig() {
		allInformationSets = new HashMap<Long, Map<Sequence, I>>();
	}

	@Override
	public void addInformationSetFor(GameState gameState, I informationSet) {
		addInformationSetFor(gameState.getISEquivalenceForPlayerToMove(), gameState.getSequenceForPlayerToMove(), informationSet);
	}
	
	public void addInformationSetFor(long isHash, Sequence sequence, I informationSet) {
		Map<Sequence, I> sequenceMap = allInformationSets.get(isHash);

		if (sequenceMap == null) {
			sequenceMap = new HashMap<Sequence, I>();
			sequenceMap.put(sequence, informationSet);
			return;
		}
		addISToSequenceMap(sequence, informationSet, sequenceMap);
	}

	private void addISToSequenceMap(Sequence sequence, I informationSet, Map<Sequence, I> sequenceMap) {
		InformationSet isFromMap = sequenceMap.get(sequence);

		if (isFromMap == null) {
			sequenceMap.put(new SequenceImpl(sequence), informationSet);
		}
	}

	public I getInformationSetFor(GameState gameState) {
		return getInformationSetFor(gameState.getISEquivalenceForPlayerToMove(), gameState.getSequenceForPlayerToMove());
	}
	
	public I getInformationSetFor(long isHash, Sequence sequence) {
		Map<Sequence, I> sequenceMap = allInformationSets.get(isHash);
		
		if (sequenceMap == null)
			return null;
		return sequenceMap.get(sequence);
	}
	
	public Map<Long, Map<Sequence, I>> getAllInformationSets() {
		return allInformationSets;
	}
}
