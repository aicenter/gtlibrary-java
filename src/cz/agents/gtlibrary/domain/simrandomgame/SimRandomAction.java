package cz.agents.gtlibrary.domain.simrandomgame;

import cz.agents.gtlibrary.iinodes.ActionImpl;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;
import cz.agents.gtlibrary.interfaces.Player;

public class SimRandomAction extends ActionImpl implements Comparable<SimRandomAction> {

	private final String value;

	private Player player;
	private int hashCode = -1;

	public SimRandomAction(String value, Player player, InformationSet informationSet) {
		super(informationSet);
		this.value = value;
		this.player = player;
	}

	@Override
	public void perform(GameState gameState) {
		((SimRandomGameState) gameState).evaluate(this);
	}

	public String getValue() {
		return value;
	}

	public Player getPlayer() {
		return player;
	}

	@Override
	public int compareTo(SimRandomAction action) {
		return value.compareTo(action.getValue());
	}

	@Override
	public int hashCode() {
		if (hashCode == -1) {
			final int prime = 31;

			hashCode = 1;
			hashCode = prime * hashCode + ((player == null) ? 0 : player.hashCode());
			hashCode = prime * hashCode + ((value == null) ? 0 : value.hashCode());
			hashCode = prime * hashCode + ((informationSet == null) ? 0 : informationSet.hashCode());
		}
		return hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		SimRandomAction other = (SimRandomAction) obj;
		if (player == null) {
			if (other.player != null)
				return false;
		} else if (!player.equals(other.player))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		if (informationSet == null) {
			if (other.informationSet != null)
				return false;
		} else if (!informationSet.equals(other.informationSet))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		builder.append("[" + value + ", " + player);
		builder.append("]");
		return builder.toString();
	}

}
