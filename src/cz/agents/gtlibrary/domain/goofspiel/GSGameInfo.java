package cz.agents.gtlibrary.domain.goofspiel;

import java.util.Arrays;

import cz.agents.gtlibrary.iinodes.PlayerImpl;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.Player;

public class GSGameInfo implements GameInfo {

	public static final Player FIRST_PLAYER = new PlayerImpl(0);
	public static final Player SECOND_PLAYER = new PlayerImpl(1);
	public static final Player NATURE = new PlayerImpl(2);

	public static final Player[] ALL_PLAYERS = { FIRST_PLAYER, SECOND_PLAYER, NATURE };
	public static final int[] CARDS_FOR_PLAYER = new int[] { 1, 2, 3, 4,/* 5, /*6,/* 7, 8, 9, 10, 11, 12, 13*/};
	public static final long seed = 1;
	public static boolean useFixedNatureSequence = true;
	public static final int depth = CARDS_FOR_PLAYER.length;

	@Override
	public double getMaxUtility() {
//		double value = 0;
//		
//		for (int cardValue : CARDS_FOR_PLAYER) {
//			value += cardValue;
//		}
//		return value - 1 - value/2.;
		return 1;
	}

	@Override
	public Player getFirstPlayerToMove() {
		return NATURE;
	}

	@Override
	public Player getOpponent(Player player) {
		return player.equals(FIRST_PLAYER) ? SECOND_PLAYER : FIRST_PLAYER;
	}

	@Override
	public String getInfo() {
		return "Goofspiel, cards: " + Arrays.toString(CARDS_FOR_PLAYER) + ", fixed nature sequence: " + useFixedNatureSequence;
	}

	@Override
	public int getMaxDepth() {
		return 3 * CARDS_FOR_PLAYER.length;
	}

	@Override
	public Player[] getAllPlayers() {
		return ALL_PLAYERS;
	}

}
