package cz.agents.gtlibrary.iinodes;

import cz.agents.gtlibrary.interfaces.Player;

public class PlayerImpl implements Player {
	
	private int id;
	private int hashCode;
	
	public PlayerImpl(int id) {
		this.id = id;
		hashCode = computeHashCode();
	}

	@Override
	public int getId() {
		return id;
	}	
	
	public int computeHashCode() {
		final int prime = 31;
		int result = 1;
		
		result = prime * result + id;
		return result;
	}
	
	@Override
	public int hashCode() {
		return hashCode;
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
