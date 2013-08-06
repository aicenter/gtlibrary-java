package cz.agents.gtlibrary.nfg.simalphabeta;

import cz.agents.gtlibrary.iinodes.ConfigImpl;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.nfg.simalphabeta.oracle.SimABInformationSet;

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
