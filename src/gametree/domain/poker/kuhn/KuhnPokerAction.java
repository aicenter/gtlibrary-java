package gametree.domain.poker.kuhn;

import gametree.domain.poker.PokerAction;
import gametree.interfaces.Player;

public class KuhnPokerAction extends PokerAction {

	public KuhnPokerAction(String action, long isHash, Player player) {
		super(action, isHash, player);
	}

}
