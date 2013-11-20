package cz.agents.gtlibrary.algorithms.cfr.vanilla;

import cz.agents.gtlibrary.algorithms.cfr.CFRConfig;
import cz.agents.gtlibrary.interfaces.GameState;

public class VanillaCFRConfig extends CFRConfig<VanillaInformationSet>{

	public VanillaCFRConfig(GameState rootState) {
		super(rootState);
	}

	@Override
	public VanillaInformationSet createInformationSetFor(GameState gameState) {
		return new VanillaInformationSet(gameState);
	}

}
