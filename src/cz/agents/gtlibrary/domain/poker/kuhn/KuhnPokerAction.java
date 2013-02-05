package cz.agents.gtlibrary.domain.poker.kuhn;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import cz.agents.gtlibrary.domain.poker.PokerAction;
import cz.agents.gtlibrary.interfaces.InformationSet;
import cz.agents.gtlibrary.interfaces.Player;

public class KuhnPokerAction extends PokerAction {

	public KuhnPokerAction(String action, InformationSet i, Player player) {
		super(action, i, player);
	}

	@Override
	public int computeHashCode() {
		return new HashCodeBuilder(17,37).append(action).append(player).append(getInformationSet()).toHashCode();
	}

	@Override
	public int computeHashCodeWithoutIS() {
		return new HashCodeBuilder(17,37).append(action).append(player).toHashCode();
	}

}
