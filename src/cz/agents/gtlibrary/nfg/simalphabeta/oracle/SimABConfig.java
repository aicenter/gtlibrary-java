package cz.agents.gtlibrary.nfg.simalphabeta.oracle;

import cz.agents.gtlibrary.iinodes.ConfigImpl;
import cz.agents.gtlibrary.interfaces.GameState;

public class SimABConfig extends ConfigImpl<SimABInformationSet>{

	@Override
	public SimABInformationSet createInformationSetFor(GameState gameState) {
		SimABInformationSet informationSet = getInformationSetFor(gameState);
		
		if(informationSet == null) {
			informationSet = new SimABInformationSet(gameState);
			addInformationSetFor(gameState, informationSet);
		}
		return informationSet;
	}

}
