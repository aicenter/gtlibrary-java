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


package cz.agents.gtlibrary.domain.bpg;

import cz.agents.gtlibrary.iinodes.PlayerImpl;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.Player;

public class BPGGameInfo implements GameInfo {

    public static Player ATTACKER = new PlayerImpl(0);
	public static Player DEFENDER = new PlayerImpl(1);

	public static Player[] ALL_PLAYERS = { ATTACKER, DEFENDER };

	public static int DEPTH = 3;
	public static boolean SLOW_MOVES = true;
	public static String graphFile = /*"GridW3-fully-connected.txt";*/"GridW3-almost-connected.txt";//*/"GridW3.txt";
    public static double EVADER_MOVE_COST = 0.4/(2*DEPTH);
    public static double DEFENDER_MOVE_COST = 0.5/(2*DEPTH);

	public static final boolean SCALE_UTILITIES = false;
	public static final double SCALING_FACTOR = 100;
	public static final int ROUNDING = 2;

	public static final Boolean dummyAttackerObservation = false;
	public static final Boolean dummyAttackerSituation = true;
	public static final Integer SAME  = 0;
	public static final Integer UP    = 1;
	public static final Integer DOWN  = 2;
	public static final Integer LEFT  = 3;
	public static final Integer RIGHT = 4;
	public static final Integer LL    = 5;

	public static final Integer UP_S    = 6;
	public static final Integer DOWN_S  = 7;
	public static final Integer LEFT_S  = 8;
	public static final Integer RIGHT_S = 9;
	public static final Integer LL_S    = 10;

	@Override
	public double getMaxUtility() {
		return 2;
	}

	@Override
	public Player getFirstPlayerToMove() {
		return ATTACKER;
	}

	@Override
	public Player getOpponent(Player player) {
		return player.equals(ATTACKER) ? DEFENDER : ATTACKER;
	}

	@Override
	public String getInfo() {
		return "Border patrolling game \nSlow moves:" + SLOW_MOVES + "\tDepth:" + DEPTH + "\tGraph:" + graphFile;
	}

	@Override
	public int getMaxDepth() {
		return DEPTH;
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
