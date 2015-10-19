/*
Copyright 2014 Faculty of Electrical Engineering at CTU in Prague

This file is part of Game Theoretic Library.

Game Theoretic Library is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Game Theoretic Library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with Game Theoretic Library.  If not, see <http://www.gnu.org/licenses/>.*/


package cz.agents.gtlibrary.iinodes;

import cz.agents.gtlibrary.interfaces.Player;

public class PlayerImpl implements Player {
	
	private static final long serialVersionUID = -4463334076128714335L;
	
	private int id;
	private int hashCode;
    private String name;
	
	public PlayerImpl(int id) {
		this.id = id;
		hashCode = computeHashCode();
	}
        
        public PlayerImpl(int id, String name) {
                this(id);
		this.name = name;
	}

	@Override
	public int getId() {
		return id;
	}	
	
	public int computeHashCode() {
		final int prime = 31;
		int result = 351357;
		
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
		return name == null ? "Pl" + id : name;
	}

    @Override
    public String getName() {
        return name;
    }
}
