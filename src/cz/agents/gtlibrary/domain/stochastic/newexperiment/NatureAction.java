package cz.agents.gtlibrary.domain.stochastic.newexperiment;

import cz.agents.gtlibrary.domain.stochastic.StochasticAction;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;

public class NatureAction extends StochasticAction {

	private static final long serialVersionUID = -1336366637039514482L;
	private NEGameState state;
	private Player player;

	public NatureAction(NEGameState state, Player player) {
		this.state = state;
		this.player = player;
	}

	public NEGameState getGameState() {
		return state;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((player == null) ? 0 : player.hashCode());
		result = prime * result + ((state == null) ? 0 : state.hashCode());
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
		NatureAction other = (NatureAction) obj;
		if (player == null) {
			if (other.player != null)
				return false;
		} else if (!player.equals(other.player))
			return false;
		if (state == null) {
			if (other.state != null)
				return false;
		} else if (!state.equals(other.state))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "NA" + state;
	}

	@Override
	public void perform(GameState gameState) {
		throw new UnsupportedOperationException();
	}
}
