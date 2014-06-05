package cz.agents.gtlibrary.algorithms.mcts;

import cz.agents.gtlibrary.iinodes.ConfigImpl;
import cz.agents.gtlibrary.interfaces.GameState;

public class MCTSConfig extends ConfigImpl<MCTSInformationSet> {
        
	@Override
	public MCTSInformationSet createInformationSetFor(GameState gameState) {
		return new MCTSInformationSet(gameState);
	}
        
      	@Override
	public MCTSInformationSet getInformationSetFor(GameState gameState) {		
		MCTSInformationSet infoSet = super.getInformationSetFor(gameState);
		if (infoSet == null) {
			infoSet = new MCTSInformationSet(gameState);
		}
		return infoSet;
        }       
}
