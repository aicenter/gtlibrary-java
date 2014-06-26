package cz.agents.gtlibrary.domain.nfptest;

import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

public class P2TestAction extends TestAction {
	
	private static final long serialVersionUID = -7887887266063667543L;

	public P2TestAction(InformationSet informationSet, String actionType) {
		super(informationSet, actionType);
	}

	@Override
	public void perform(GameState gameState) {
		TestGameState tGameSTate = (TestGameState) gameState;
		
		tGameSTate.performP2Action(this);
	}
	
	@Override
	public String toString() {
		return "P2: " + actionType;
	}

}
