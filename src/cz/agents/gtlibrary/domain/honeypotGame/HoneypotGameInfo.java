package cz.agents.gtlibrary.domain.honeypotGame;

import cz.agents.gtlibrary.iinodes.PlayerImpl;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.Player;

import java.io.*;
import java.util.HashSet;

/**
 * Created by Petr Tomasek on 29.4.2017.
 */
public class HoneypotGameInfo implements GameInfo {

    public static Player DEFENDER = new PlayerImpl(0, "Defender");
    public static Player ATTACKER = new PlayerImpl(1, "Attacker");
    public static final Player NATURE = new PlayerImpl(2, "Nature");
    public static final Player[] ALL_PLAYERS = new Player[]{DEFENDER, ATTACKER, NATURE};
    public static final int NO_ACTION_ID = -1;

    public static long seed = 11;

    public int attacksAllowed;
    public HoneypotGameNode[] allNodes;
    public double initialAttackerBudget;
    public double initialDefenderBudget;
    public double minValue = Double.MAX_VALUE;
    public double attackCost;
    private String inputFile;

    public HoneypotGameInfo() {

    }

    public HoneypotGameInfo(String inputFile) throws IOException {
        this.inputFile = inputFile;
        File file = new File(inputFile);
        BufferedReader reader = new BufferedReader(new FileReader(file));

        String[] line = reader.readLine().split("\\s+");
        initialDefenderBudget = Integer.parseInt(line[0]);
        initialAttackerBudget = Integer.parseInt(line[1]);
        attacksAllowed = Integer.parseInt(line[2]);
        attackCost = initialAttackerBudget / attacksAllowed;

        line = reader.readLine().split("\\s+");
        int nodesCount = Integer.parseInt(line[0]);
        allNodes = new HoneypotGameNode[nodesCount];

        line = reader.readLine().split("\\s+");
        for (int i = 0; i < nodesCount; i++) {
            double value = Double.parseDouble(line[i]);
            allNodes[i] = new HoneypotGameNode(i + 1, value);

            if (value < minValue) {
                minValue = value;
            }
        }
    }

    @Override
    public double getMaxUtility() {
        double maxUtility = 0;

        for (HoneypotGameNode node : allNodes) {
            if (node.value > maxUtility)
                maxUtility = node.value;
        }

        return maxUtility + (attacksAllowed - 1) * (maxUtility / 2);
    }

    @Override
    public Player getFirstPlayerToMove() {
        return DEFENDER;
    }

    @Override
    public Player getOpponent(Player player) {
        if (player.equals(ATTACKER)) return DEFENDER;
        return ATTACKER;
    }

    @Override
    public String getInfo() {
        return "Honeypot Game : defender budget = " + initialDefenderBudget + "; attacker budget = " + initialAttackerBudget +
                "; attacks allowed = " + attacksAllowed + "; input file = " + inputFile;
    }

    @Override
    public int getMaxDepth() {
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
