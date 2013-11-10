package cz.agents.gtlibrary.domain.stochastic.smallgame;

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
