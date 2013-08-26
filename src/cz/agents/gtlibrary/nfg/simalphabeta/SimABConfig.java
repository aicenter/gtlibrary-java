package cz.agents.gtlibrary.nfg.simalphabeta;

import cz.agents.gtlibrary.iinodes.ConfigImpl;
import cz.agents.gtlibrary.interfaces.GameState;

public class SimABConfig extends ConfigImpl<SimABInformationSet>{

	@Override
	public SimABInformationSet getInformationSetFor(GameState gameState) {
		return new SimABInformationSet(gameState);
	}

	@Override
	public SimABInformationSet createInformationSetFor(GameState gameState) {
		return null;
	}
}
