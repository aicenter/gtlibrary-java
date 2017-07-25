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


package cz.agents.gtlibrary.experimental.stochastic.experiment;

import cz.agents.gtlibrary.iinodes.PlayerImpl;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.Player;

public class ExperimentInfo implements GameInfo {

	public static double UTILITY = 1;
	public static Player PATROLLER = new PlayerImpl(0);
	public static Player ATTACKER = new PlayerImpl(1);
	public static Player NATURE = new PlayerImpl(2);
	public static Player[] ALL_PLAYERS = new Player[] { PATROLLER, ATTACKER, NATURE };
	public static String patrollerStartId = "0";
	public static String graphFile = "exp_small.txt";
	public static double epsilon = 0.05;
	public static int commitmentDepth = 2;

	@Override
	public double getMaxUtility() {
		return UTILITY;
	}

	@Override
	public Player getFirstPlayerToMove() {
		return PATROLLER;
	}

	@Override
	public Player getOpponent(Player player) {
		return player.equals(ATTACKER) ? PATROLLER : ATTACKER;
	}

	@Override
	public String getInfo() {
		// TODO Auto-generated method stub
		return null;
	}  

	@Override
	public int getMaxDepth() {
		// TODO Auto-generated method stub
		return 0;
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
