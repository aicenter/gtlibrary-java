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

    public static int attacksAllowed = 1;
    public static HoneypotGameNode[] allNodes;
    public static final double[] NODE_REWARDS = new double[]{25, 35, 20, 30, 30};//, 4, 23, 12, 34, 45};
    public static final double[] NODE_ATTACKERCOSTS = new double[]{10, 15, 10, 10, 15};//, 55, 25, 5, 15, 5};
    public static final double[] NODE_DEFENDERCOSTS = new double[]{5, 5, 5, 5, 5};//, 35, 35, 35, 35, 35};
    public static double initialAttackerBudget =  0.0;
    public static double initialDefenderBudget = 10.0;
    public static double minValue = Double.MAX_VALUE;
    public static double uniformAttackCost = initialAttackerBudget / attacksAllowed;

    public static final boolean USE_UNIFORM_COSTS = false;
    public static final boolean CAN_ATTACK_WITH_NEGATIVE_POINTS = true;
    public static int NUMBER_OF_PASSES_TO_END_GAME = 1;

    private static boolean readInputFile = false;
    private static String inputFile  = "honeypot_deployed1.txt";

    public static final boolean ENABLE_ITERATIVE_SOLVING = true;

    public static long seed = 11;

    public HoneypotGameInfo() {
        if (readInputFile) readFile();
        else initNodes();
    }

    public HoneypotGameInfo(String inputFile){
        this.inputFile = inputFile;
        this.readInputFile = true;
//        readGraph();
        readFile();
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

    private void readFile(){
        File file = new File(inputFile);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String[] line = reader.readLine().split("\\s+");
            initialDefenderBudget = Integer.parseInt(line[0]);
            initialAttackerBudget = Integer.parseInt(line[1]);
            attacksAllowed = Integer.parseInt(line[2]);
//            uniformAttackCost = initialAttackerBudget / attacksAllowed;
            if (line.length > 3)
                NUMBER_OF_PASSES_TO_END_GAME = Integer.parseInt(line[3]);

            line = reader.readLine().split("\\s+");
            int nodesCount = Integer.parseInt(line[0]);

            line = reader.readLine().split("\\s+");
            double[] NODE_REWARDS = new double[nodesCount];
            for (int i = 0; i < nodesCount; i++) {
                NODE_REWARDS[i] = Double.parseDouble(line[i]);
            }

            line = reader.readLine().split("\\s+");
            double[] NODE_ATTACKERCOSTS = new double[nodesCount];
            for (int i = 0; i < nodesCount; i++) {
                NODE_ATTACKERCOSTS[i] = Double.parseDouble(line[i]);
            }

            line = reader.readLine().split("\\s+");
            double[] NODE_DEFENDERCOSTS = new double[nodesCount];
            for (int i = 0; i < nodesCount; i++) {
                NODE_DEFENDERCOSTS[i] = Double.parseDouble(line[i]);
            }

            allNodes = new HoneypotGameNode[NODE_REWARDS.length];

            for (int i = 0; i < NODE_REWARDS.length; i++) {
                if (USE_UNIFORM_COSTS) allNodes[i] = new HoneypotGameNode(i + 1, NODE_REWARDS[i], uniformAttackCost, uniformAttackCost);
                else allNodes[i] = new HoneypotGameNode(i + 1, NODE_REWARDS[i], NODE_ATTACKERCOSTS[i], NODE_DEFENDERCOSTS[i]);
                if (NODE_REWARDS[i] < minValue) {
                    minValue = NODE_REWARDS[i];
                }
            }
            Arrays.sort(allNodes, (a,b) -> -1 * Double.compare(a.reward, b.reward));

        } catch (Exception e) {
            e.printStackTrace();
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
            uniformAttackCost = initialAttackerBudget / attacksAllowed;
            if (line.length > 3)
                NUMBER_OF_PASSES_TO_END_GAME = Integer.parseInt(line[3]);

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
//        else info += "; node rewards = "  + Arrays.toString(NODE_REWARDS);
        info += "\nNodes = " + Arrays.toString(allNodes);
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
