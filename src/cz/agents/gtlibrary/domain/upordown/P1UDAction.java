package cz.agents.gtlibrary.domain.upordown;

import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

public class P1UDAction extends UDAction {

	private static final long serialVersionUID = -338756462754901666L;

	public P1UDAction(InformationSet informationSet, String type) {
		super(informationSet, type);
	}

	@Override
	public void perform(GameState gameState) {
		((UDGameState)gameState).setP1Action(this);
	}

}
