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


package cz.agents.gtlibrary.experimental.stochastic.smallgame;

import java.util.Random;

import cz.agents.gtlibrary.iinodes.PlayerImpl;
import cz.agents.gtlibrary.interfaces.Player;

public class SGGameInfo {

	public static Random random = new Random(1);
	public static Player p1 = new PlayerImpl(0);
	public static Player p2 = new PlayerImpl(1);
	public static Player nature = new PlayerImpl(2);
	public static Player[] ALL_PLAYERS = new Player[] { p1, p2, nature };
	public static int stateCount = 4;

	public static SGGameState[] gameStates;

	static {
		gameStates = new SGGameState[stateCount];
		for (int i = 0; i < stateCount; i++) {
			gameStates[i] = new SGGameState(new Random(i));
		}
	}

}
