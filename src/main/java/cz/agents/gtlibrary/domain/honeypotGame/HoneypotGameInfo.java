package cz.agents.gtlibrary.domain.honeypotGame;

import cz.agents.gtlibrary.iinodes.PlayerImpl;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.Player;
import org.apache.commons.lang3.ArrayUtils;

import java.io.*;
import java.util.*;

/**
 * Created by Petr Tomasek on 29.4.2017.
 */
public class HoneypotGameInfo implements GameInfo {

    public static Player DEFENDER = new PlayerImpl(0, "Defender");
    public static Player ATTACKER = new PlayerImpl(1, "Attacker");
//    public static final Player NATURE = new PlayerImpl(2, "Nature");
    public static final Player[] ALL_PLAYERS = new Player[]{DEFENDER, ATTACKER};//, NATURE};
    public static final int NO_ACTION_ID = -1;

    public static int attacksAllowed = 5;
    public static HoneypotGameNode[] allNodes;
    public static final double[] NODE_REWARDS = new double[]{34, 16, 50, 55, 40};//, 4, 23, 12, 34, 45};
    public static final double[] NODE_ATTACKERCOSTS = new double[]{10, 5, 15, 35, 15};//, 55, 25, 5, 15, 5};
    public static final double[] NODE_DEFENDERCOSTS = new double[]{20, 20, 20, 30, 20};//, 35, 35, 35, 35, 35};
    public static double initialAttackerBudget =  0.0;
    public static double initialDefenderBudget = 40.0;
    public static double minValue = Double.MAX_VALUE;
    public static double uniformAttackCost = initialAttackerBudget / attacksAllowed;

    public static final boolean USE_UNIFORM_COSTS = false;
    public static final boolean CAN_ATTACK_WITH_NEGATIVE_POINTS = true;
    public static final int NUMBER_OF_PASSES_TO_END_GAME = 2;

    private static boolean readInputFile = false;
    private static String inputFile  = "honeypot_complex1.txt";

    public static final boolean ENABLE_ITERATIVE_SOLVING = true;

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
        allNodes = new HoneypotGameNode[NODE_REWARDS.length];

        for (int i = 0; i < NODE_REWARDS.length; i++) {
            if (USE_UNIFORM_COSTS) allNodes[i] = new HoneypotGameNode(i + 1, NODE_REWARDS[i], uniformAttackCost, uniformAttackCost);
            else allNodes[i] = new HoneypotGameNode(i + 1, NODE_REWARDS[i], NODE_ATTACKERCOSTS[i], NODE_DEFENDERCOSTS[i]);
            if (NODE_REWARDS[i] < minValue) {
                minValue = NODE_REWARDS[i];
            }
        }

        Arrays.sort(allNodes, (a,b) -> -1 * Double.compare(a.reward, b.reward));

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
            uniformAttackCost = initialAttackerBudget / attacksAllowed;

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
                allNodes[i] = new HoneypotGameNode(i + 1, values.get(i), uniformAttackCost, values.get(i));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public double getMaxUtility() {
        double maxUtility = 0;

        for (HoneypotGameNode node : allNodes) {
            if (node.reward > maxUtility)
                maxUtility = node.reward;
        }

        return attacksAllowed * maxUtility;
//        return maxUtility + (attacksAllowed - 1) * (maxUtility / 2);
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
        else info += "; node rewards = "  + Arrays.toString(NODE_REWARDS);
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
