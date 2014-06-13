package cz.agents.gtlibrary.algorithms.cfr.vanilla;

import cz.agents.gtlibrary.algorithms.cfr.CFRConfig;
import cz.agents.gtlibrary.interfaces.GameState;

/**
 * This class will be removed, the implementation of CFR and OOS is obsolete.
 * Use cz.agents.gtlibrary.algorithms.cfr.CFRAlgorithm/CFRISAlgorithm instead.
 */
@Deprecated
public class VanillaCFRConfig extends CFRConfig<VanillaInformationSet>{

	public VanillaCFRConfig(GameState rootState) {
		super(rootState);
	}

	@Override
	public VanillaInformationSet createInformationSetFor(GameState gameState) {
		return new VanillaInformationSet(gameState);
	}

}
