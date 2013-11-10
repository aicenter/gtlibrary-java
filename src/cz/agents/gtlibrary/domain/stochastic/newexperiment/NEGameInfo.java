package cz.agents.gtlibrary.domain.stochastic.newexperiment;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import cz.agents.gtlibrary.iinodes.PlayerImpl;
import cz.agents.gtlibrary.interfaces.Player;

public class NEGameInfo {

	public static Random random = new Random(1);
	public static Player p1 = new PlayerImpl(0);
	public static Player p2 = new PlayerImpl(1);
	public static Player nature = new PlayerImpl(2);
	public static Player[] ALL_PLAYERS = new Player[] { p1, p2, nature };
	public static int stateCount = 4;

	public static Map<String, NEGameState> gameStates;

	static {
		gameStates = new HashMap<String, NEGameState>();
		gameStates.put("A", new NEGameState(0, "A"));
		gameStates.put("B", new NEGameState(0, "B"));
		gameStates.put("C", new NEGameState(0, "C"));
		gameStates.put("D", new NEGameState(-1, "D"));
		gameStates.put("E", new NEGameState(-1, "E"));
		gameStates.put("F", new NEGameState(1, "F"));
		gameStates.put("G", new NEGameState(0, "G"));
		gameStates.put("H", new NEGameState(1, "H"));
	}

}
