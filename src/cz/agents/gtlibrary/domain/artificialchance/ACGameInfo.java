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
		return "AC experimental";
	}

	@Override
	public int getMaxDepth() {
		return 5;
	}

	@Override
	public Player[] getAllPlayers() {
		return ALL_PLAYERS;
	}

}
