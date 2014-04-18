package cz.agents.gtlibrary.experimental.stochastic.newexperiment;

import cz.agents.gtlibrary.experimental.stochastic.StochasticAction;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;

public class NEAction extends StochasticAction {

	protected static final long serialVersionUID = -9119301953413659685L;

	protected Player player;
	protected int id;

	public NEAction(int id, Player player) {
		this.id = id;
		this.player = player;
	}

	public Player getPlayer() {
		return player;
	}

	public int getId() {
		return id;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		result = prime * result + ((player == null) ? 0 : player.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NEAction other = (NEAction) obj;
		if (id != other.id)
			return false;
		if (player == null) {
			if (other.player != null)
				return false;
		} else if (!player.equals(other.player))
			return false;
		return true;
	}

	@Override
	public void perform(GameState gameState) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString() {
		return "A" + id;
	}

}
