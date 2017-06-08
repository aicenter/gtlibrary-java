package cz.agents.gtlibrary.domain.honeypotGame;

import cz.agents.gtlibrary.iinodes.PlayerImpl;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.Player;
import org.apache.commons.lang3.ArrayUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by Petr Tomasek on 29.4.2017.
 */
public class HoneypotGameInfo implements GameInfo {

    public static Player DEFENDER = new PlayerImpl(0, "Defender");
    public static Player ATTACKER = new PlayerImpl(1, "Attacker");
//    public static final Player NATURE = new PlayerImpl(2, "Nature");
    public static final Player[] ALL_PLAYERS = new Player[]{DEFENDER, ATTACKER};//, NATURE};
    public static final int NO_ACTION_ID = -1;

    public static int attacksAllowed = 8;
    public static HoneypotGameNode[] allNodes;
    public static final double[] NODE_VALUES = new double[]{10, 60, 50, 46, 70, 4};
    public static double initialAttackerBudget = 50;
    public static double initialDefenderBudget = 70;
    public static double minValue = Double.MAX_VALUE;
    public static double attackCost = initialAttackerBudget / attacksAllowed;;

    private static boolean readInputFile = false;
    private static String inputFile  = "honeypot_complex1.txt";

    public static final boolean ENABLE_ITERATIVE_SOLVING = false;

    public static long seed = 11;

    public HoneypotGameInfo() {
        if (readInputFile) readGraph();
        else initNodes();
    }

    public HoneypotGameInfo(String inputFile){
        this.inputFile = inputFile;
        this.readInputFile = true;
        readGraph();
    }

    private void initNodes() {
        allNodes = new HoneypotGameNode[NODE_VALUES.length];

        Arrays.sort(NODE_VALUES);
        ArrayUtils.reverse(NODE_VALUES);

        for (int i = 0; i < NODE_VALUES.length; i++) {
            allNodes[i] = new HoneypotGameNode(i + 1, NODE_VALUES[i]);
            if (NODE_VALUES[i] < minValue) {
                minValue = NODE_VALUES[i];
            }
        }
    }

    private void readGraph(){
        File file = new File(inputFile);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String[] line = reader.readLine().split("\\s+");
            initialDefenderBudget = Integer.parseInt(line[0]);
            initialAttackerBudget = Integer.parseInt(line[1]);
            attacksAllowed = Integer.parseInt(line[2]);
            attackCost = initialAttackerBudget / attacksAllowed;

            line = reader.readLine().split("\\s+");
            int nodesCount = Integer.parseInt(line[0]);
            allNodes = new HoneypotGameNode[nodesCount];
            ArrayList<Double> values = new ArrayList<>();

            line = reader.readLine().split("\\s+");
            for (int i = 0; i < nodesCount; i++) {
                double value = Double.parseDouble(line[i]);
                values.add(value);

                if (value < minValue) {
                    minValue = value;
                }
            }

            Collections.sort(values, Comparator.comparingDouble(o -> -o));


            for (int i = 0; i < nodesCount; i++) {
                allNodes[i] = new HoneypotGameNode(i + 1, values.get(i));
            }

        } catch (Exception e) {
            e.printStackTrace();
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
        String info = "Honeypot Game : defender budget = " + initialDefenderBudget + "; attacker budget = " + initialAttackerBudget +
                "; attacks allowed = " + attacksAllowed;
        if (readInputFile) info += "; input file = " + inputFile;
        else info += "; node values = "  + Arrays.toString(NODE_VALUES);
        return info;
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
