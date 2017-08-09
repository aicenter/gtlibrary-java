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


package cz.agents.gtlibrary.domain.poker.kuhn;

import cz.agents.gtlibrary.iinodes.PlayerImpl;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.utils.Pair;

import java.util.HashMap;
import java.util.Map;

public class KPGameInfo implements GameInfo {
	public static final Player FIRST_PLAYER = new PlayerImpl(0);
	public static final Player SECOND_PLAYER = new PlayerImpl(1);
	public static final Player NATURE = new PlayerImpl(2);

	public static final Player[] ALL_PLAYERS = new Player[] {FIRST_PLAYER, SECOND_PLAYER, NATURE};
	
	public static int ANTE = 1;
	public static int BET = 1;

	public static Map<String, Integer> p1cardBounties = new HashMap<>();
	public static Map<String, Integer> p2cardBounties = new HashMap<>();

	static {
		p1cardBounties.put("0", 0);
		p1cardBounties.put("1", 3);
		p1cardBounties.put("2", 1);
		p2cardBounties.put("2", 0);
		p2cardBounties.put("0", 4);
		p2cardBounties.put("1", 1);
	}

	@Override
	public double getMaxUtility() {
		return 6;
	}

	@Override
	public Player getFirstPlayerToMove() {
		return NATURE;
	}

	@Override
	public Player getOpponent(Player player) {
		if (player.equals(FIRST_PLAYER))
			return SECOND_PLAYER;
		return FIRST_PLAYER;
	}

	@Override
	public String getInfo() {
		return "Kuhn poker, ante: " + ANTE + ", bet: " + BET;
	}

	@Override
	public int getMaxDepth() {
		return 5;
	}
	
	@Override
	public Player[] getAllPlayers() {		
		return ALL_PLAYERS;
	}

    @Override
    public double getUtilityStabilizer() {
        return 60;
    }
}
