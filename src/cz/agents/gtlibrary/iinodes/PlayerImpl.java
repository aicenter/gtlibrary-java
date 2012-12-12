package cz.agents.gtlibrary.iinodes;

import cz.agents.gtlibrary.interfaces.Player;

public class PlayerImpl implements Player {
	
	private int id;
	
	public PlayerImpl(int id) {
		this.id = id;
	}

	@Override
	public int getId() {
		return id;
	}	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
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
		PlayerImpl other = (PlayerImpl) obj;
		if (id != other.id)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Pl" + id;
	}
}
