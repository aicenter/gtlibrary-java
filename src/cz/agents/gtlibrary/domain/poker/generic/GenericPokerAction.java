package cz.agents.gtlibrary.domain.poker.generic;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import cz.agents.gtlibrary.domain.poker.PokerAction;
import cz.agents.gtlibrary.interfaces.InformationSet;
import cz.agents.gtlibrary.interfaces.Player;

public class GenericPokerAction extends PokerAction {

	private int value;

	public GenericPokerAction(String action, InformationSet i, Player player, int value) {
		super(action, i, player);
		this.value = value;
	}
	
	public int getValue() {
		return value;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		builder.append("[");
		builder.append(player);
		builder.append(", ");
		builder.append(action);
		builder.append(", ");
		builder.append("value: ");
		builder.append(value);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int computeHashCode() {
		return new HashCodeBuilder(17,31).append(action).append(player).append(value).append(informationSet).toHashCode();
	}

	@Override
	public int computeHashCodeWithoutIS() {
		return new HashCodeBuilder(17,31).append(action).append(player).append(value).toHashCode();
	}
}
