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


package cz.agents.gtlibrary.algorithms.sequenceform.refinements;

public class Key {

	private String string;
	private Object object;
	private int hashCode;
	private String represenation;

	public Key(String string, Object object) {
		this.string = string;
		this.object = object;
		hashCode = computeHashCode();
	}

	public Key(Object object) {
		this.string = "";
		this.object = object;
		hashCode = computeHashCode();
	}

	public Object getObject() {
		return object;
	}
	
	public int computeHashCode() {
		final int prime = 31;
		int result = 1;

		result = prime * result + ((object == null) ? 0 : object.hashCode());
		result = prime * result + ((string == null) ? 0 : string.hashCode());
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
		Key other = (Key) obj;
		if (object == null) {
			if (other.object != null)
				return false;
		} else if (!object.equals(other.object))
			return false;
		if (string == null) {
			if (other.string != null)
				return false;
		} else if (!string.equals(other.string))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public String toString() {
		if (represenation == null)
			represenation = "[" + object + (string.equals("") ? "" : (", " + string)) + "]";
		return represenation;
	}

}
