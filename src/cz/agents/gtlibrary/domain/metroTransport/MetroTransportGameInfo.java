package cz.agents.gtlibrary.domain.metroTransport;

import cz.agents.gtlibrary.domain.flipit.FlipItGraph;
import cz.agents.gtlibrary.iinodes.PlayerImpl;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.utils.HighQualityRandom;

/**
 * Created by Jakub Cerny on 05/10/2017.
 */
public class MetroTransportGameInfo implements GameInfo {

    // GRAPH FILE : topology, rewards and control costs
    public static String graphFile = "metro_simple3.txt";
    public static MetroGraph graph = new MetroGraph(graphFile);

    // PLAYERS
    public static final Player DEFENDER = new PlayerImpl(0, "Defender");
    public static final Player ATTACKER = new PlayerImpl(1, "Attacker");
    public static final Player NATURE = new PlayerImpl(2, "Nature");
    public static final Player[] ALL_PLAYERS = new Player[] {DEFENDER, ATTACKER};//, NATURE};

    public static final int NUMBER_OF_OFFICERS = 2;
    public static final int NUMBER_OF_STEPS = 6;
    public static final double TERMINATION_PROB = 0.1;

    public static final boolean ZERO_SUM = false;

    public static final HighQualityRandom random = new HighQualityRandom(0);

    @Override
    public double getMaxUtility() {
        return Math.max(graph.getRewardSum(), NUMBER_OF_STEPS);
    }

    @Override
    public Player getFirstPlayerToMove() {
        return DEFENDER;
    }

    @Override
    public Player getOpponent(Player player) {
        return (player.equals(DEFENDER) ? ATTACKER : DEFENDER);
    }

    @Override
    public String getInfo() {
        return "Metro transport game. # of officers: " + NUMBER_OF_OFFICERS + ", # of steps: " + NUMBER_OF_STEPS
                + ", termination probability: " + TERMINATION_PROB;
    }

    @Override
    public int getMaxDepth() {
        return 2 * NUMBER_OF_STEPS;
    }

    @Override
    public Player[] getAllPlayers() {
        return ALL_PLAYERS;
    }

    @Override
    public double getUtilityStabilizer() {
        return 1.0;
    }
}
