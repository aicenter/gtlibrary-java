package cz.agents.gtlibrary.iinodes;

import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.InformationSet;

public abstract class ExpanderImpl<I extends InformationSet> implements Expander<I> {
	final private AlgorithmConfig<I> algConfig;
	
	public ExpanderImpl(AlgorithmConfig<I> algConfig) {
		this.algConfig = algConfig;
	}

	@Override
	public AlgorithmConfig<I> getAlgorithmConfig() {
		return algConfig;
	}
}
