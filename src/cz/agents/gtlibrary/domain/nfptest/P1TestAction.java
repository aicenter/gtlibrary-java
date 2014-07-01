package cz.agents.gtlibrary.domain.nfptest;

import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

public class P1TestAction extends TestAction {

	private static final long serialVersionUID = 9025417019384385478L;

	public P1TestAction(InformationSet informationSet, String actionType) {
		super(informationSet, actionType);
	}

	@Override
	public void perform(GameState gameState) {
		TestGameState tGameSTate = (TestGameState) gameState;
		
		tGameSTate.performP1Action(this);
	}
	
	@Override
	public String toString() {
		return "P1: " + actionType;
	}
	
}
