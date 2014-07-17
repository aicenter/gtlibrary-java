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


package cz.agents.gtlibrary.domain.pursuit;

import cz.agents.gtlibrary.iinodes.PlayerImpl;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.Player;

public class PursuitGameInfo implements GameInfo {

	public static String graphFile = "pursuit_simple4x4.txt";
	public static Player EVADER = new PlayerImpl(0);
	public static Player PATROLLER = new PlayerImpl(1);
	public static Player[] ALL_PLAYERS = { EVADER, PATROLLER };
	public static long seed = 11;
	public static int evaderStart = 0;
	public static int p1Start = 11;
	public static int p2Start = 15;
	public static int depth = 5;
	public static boolean forceMoves = true;
    public static boolean randomizeStartPositions = true;

    @Override
	public double getMaxUtility() {
		return 1;
	}

	@Override
	public Player getFirstPlayerToMove() {
		return EVADER;
	}

	@Override
	public Player getOpponent(Player player) {
		return player.equals(EVADER) ? PATROLLER : EVADER;
	}

	@Override
	public String getInfo() {
		return "Pursuit, Evader start: " + evaderStart + ", P1 start: " + p1Start + ", P2 start: " + p2Start + ", depth: " + depth + ", graph: " + graphFile;
	}

	@Override
	public int getMaxDepth() {
		return depth;
	}

	@Override
	public Player[] getAllPlayers() {
		return ALL_PLAYERS;
	}

    @Override
    public double getUtilityStabilizer() {
        return 1;
    }
}
