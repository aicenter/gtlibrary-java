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


package cz.agents.gtlibrary.domain.artificialchance;

import cz.agents.gtlibrary.domain.artificialchance.ACAction.ActionType;
import cz.agents.gtlibrary.iinodes.PlayerImpl;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.Player;

public class ACGameInfo implements GameInfo {

	public static final Player FIRST_PLAYER = new PlayerImpl(0);
	public static final Player SECOND_PLAYER = new PlayerImpl(1);
	public static final Player NATURE = new PlayerImpl(2);

	public static final Player[] ALL_PLAYERS = { FIRST_PLAYER, SECOND_PLAYER, NATURE };

	public static final ActionType[] P1_NATURE = new ActionType[] { ActionType.J, ActionType.Q };
	public static final ActionType[] P2_NATURE = new ActionType[] { ActionType.Q, ActionType.K };
	public static final ActionType[] TABLE_NATURE = new ActionType[] { ActionType.J, ActionType.Q, ActionType.K };
	public static final ActionType[] P1_ACTIONS1 = new ActionType[] { ActionType.bet, ActionType.check };
	public static final ActionType[] P1_ACTIONS2 = new ActionType[] { ActionType.bet, ActionType.check };
	public static final ActionType[] P2_ACTIONS_AGGR1 = new ActionType[] { ActionType.call, ActionType.fold };
	public static final ActionType[] P2_ACTIONS_AGGR2 = new ActionType[] { ActionType.call, ActionType.fold };
	public static final ActionType[] P2_ACTIONS_PASSIVE1 = new ActionType[] { ActionType.check };
	public static final ActionType[] P2_ACTIONS_PASSIVE2 = new ActionType[] { ActionType.check };

	public static final int ANTE = 1;
	public static final int BET = 1;

	@Override
	public double getMaxUtility() {
		return 2;
	}

	@Override
	public Player getFirstPlayerToMove() {
		return FIRST_PLAYER;
	}

	@Override
	public Player getOpponent(Player player) {
		return player.equals(FIRST_PLAYER) ? SECOND_PLAYER : FIRST_PLAYER;
	}

	@Override
	public String getInfo() {
		return "AC characteristics";
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
        return 1;
    }
}
