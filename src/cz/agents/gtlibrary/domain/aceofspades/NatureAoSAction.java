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


package cz.agents.gtlibrary.domain.aceofspades;

import cz.agents.gtlibrary.iinodes.ActionImpl;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

public class NatureAoSAction extends ActionImpl {

	private static final long serialVersionUID = 7481078580610165635L;

	private boolean isAceOfSpades;

	public NatureAoSAction(InformationSet informationSet, boolean isAceOfSpades) {
		super(informationSet);
		this.isAceOfSpades = isAceOfSpades;
	}

	@Override
	public void perform(GameState gameState) {
		((AoSGameState) gameState).performNatureAction(this);
	}

	public boolean isAceOfSpades() {
		return isAceOfSpades;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (isAceOfSpades ? 1231 : 1237);
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
		NatureAoSAction other = (NatureAoSAction) obj;
		if (isAceOfSpades != other.isAceOfSpades)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return isAceOfSpades ? "Ace of spades" : "Another card";
	}

}
