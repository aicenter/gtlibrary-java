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


package cz.agents.gtlibrary.domain.nfptest;

import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

public class P2TestAction extends TestAction {
	
	private static final long serialVersionUID = -7887887266063667543L;

	public P2TestAction(InformationSet informationSet, String actionType) {
		super(informationSet, actionType);
	}

	@Override
	public void perform(GameState gameState) {
		TestGameState tGameSTate = (TestGameState) gameState;
		
		tGameSTate.performP2Action(this);
	}
	
	@Override
	public String toString() {
		return "P2: " + actionType;
	}

}
