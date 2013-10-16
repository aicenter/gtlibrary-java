package cz.agents.gtlibrary.domain.upordown;

import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

public class P2UDAction extends UDAction {

	private static final long serialVersionUID = 5114969754889842700L;

	public P2UDAction(InformationSet informationSet, String type) {
		super(informationSet, type);
	}

	@Override
	public void perform(GameState gameState) {
		((UDGameState)gameState).setP2Action(this);
	}

}
