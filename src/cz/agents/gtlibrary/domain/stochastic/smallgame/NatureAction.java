package cz.agents.gtlibrary.domain.stochastic.smallgame;

import cz.agents.gtlibrary.interfaces.Player;

public class NatureAction extends SGAction {

	private static final long serialVersionUID = -1336366637039514482L;
	private SGGameState state;

	public NatureAction(int id, SGGameState state, Player player) {
		super(id, player);
		this.state = state;
	}

	public SGGameState getGameState() {
		return state;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((state == null) ? 0 : state.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		NatureAction other = (NatureAction) obj;
		if (state == null) {
			if (other.state != null)
				return false;
		} else if (!state.equals(other.state))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "NA" + id;
	}
}
