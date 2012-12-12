package cz.agents.gtlibrary.domain.poker.kuhn;

import cz.agents.gtlibrary.domain.poker.PokerAction;
import cz.agents.gtlibrary.interfaces.Player;

public class KuhnPokerAction extends PokerAction {

	public KuhnPokerAction(String action, long isHash, Player player) {
		super(action, isHash, player);
	}

}
