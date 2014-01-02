package cz.agents.gtlibrary.domain.goofspiel;

import cz.agents.gtlibrary.iinodes.ActionImpl;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;
import cz.agents.gtlibrary.interfaces.Player;

public class GoofSpielAction extends ActionImpl implements Comparable<GoofSpielAction> {

	private static final long serialVersionUID = -3504137065821329745L;

	private final int value;

	private Player player;
	private int hashCode = -1;

	public GoofSpielAction(int value, Player player, InformationSet informationSet) {
		super(informationSet);
		this.value = value;
		this.player = player;
	}

	@Override
	public void perform(GameState gameState) {
		if (player.equals(GSGameInfo.FIRST_PLAYER)) {
			((GoofSpielGameState) gameState).performFirstPlayerAction(this);
		} else if (player.equals(GSGameInfo.SECOND_PLAYER)) {
			((GoofSpielGameState) gameState).performSecondPlayerAction(this);
		} else {
			((GoofSpielGameState) gameState).performNatureAction(this);
		}
	}

	public int getValue() {
		return value;
	}

	public Player getPlayer() {
		return player;
	}

	@Override
	public int compareTo(GoofSpielAction action) {
		return value - action.getValue();
	}


	@Override
	public int hashCode() {
		if (hashCode != -1)
			return hashCode;
		final int prime = 31;

		hashCode = 1;
		hashCode = prime * hashCode + ((player == null) ? 0 : player.hashCode());
		hashCode = prime * hashCode + ((informationSet == null) ? 0 : informationSet.hashCode());
		hashCode = prime * hashCode + value;
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
		GoofSpielAction other = (GoofSpielAction) obj;
		if (player == null) {
			if (other.player != null)
				return false;
		} else if (!player.equals(other.player))
			return false;
		if (value != other.value)
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
