package cz.agents.gtlibrary.domain.honeypotSMGame;

import cz.agents.gtlibrary.iinodes.PlayerImpl;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.Player;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;


public class HoneypotGameInfo implements GameInfo {

    public static Player DEFENDER = new PlayerImpl(0, "Defender");
    public static Player ATTACKER = new PlayerImpl(1, "Attacker");
    public static final Player[] ALL_PLAYERS = new Player[]{DEFENDER, ATTACKER};
    public static final int NO_ACTION_ID = -1;

    public static int attacksAllowed = 50;
    public static HoneypotGameNode[] allNodes;
    public static final double[] NODE_REWARDS = new double[]{15, 40, 35, 20, 35};//, 20};//, 30, 30};//, 4, 23, 12, 34, 45};
    public static final double[] NODE_ATTACKERCOSTS = new double[]{5, 20, 10, 5, 15};//, 55, 25, 5, 15, 5};
    public static final double[] NODE_DEFENDERCOSTS = new double[]{10, 20, 15, 15, 20};//, 35, 35, 35, 35, 35};
    public static double initialDefenderBudget = 40.0;

    public static int NUMBER_OF_PASSES_TO_END_GAME = 2;

    private static boolean readInputFile = false;
    private static String inputFile  = "honeypot_deployed1.txt";

    public static final boolean ENABLE_PASS = true;
    public static double maximumAttackUtility;

    public static HashSet<HashSet<HoneypotGameNode>> defenderActions;

    public HoneypotGameInfo() {
        if (readInputFile) readFile();
        else initNodes();
        initDefenderActions();
    }

    protected void initDefenderActions(){
        defenderActions = new HashSet<>();
        HashSet<HoneypotGameNode> nodes = new HashSet<>();
        int n = HoneypotGameInfo.allNodes.length;
        for (int i = 0; i < (1<<n); i++) {
            nodes.clear();
            for (int j = 0; j < n; j++) {
                if ((i & (1 << j)) > 0) {
                    nodes.add(HoneypotGameInfo.allNodes[j]);
                }
            }
            if(nodes.isEmpty()) continue;
            double sum = 0.0;
            for (HoneypotGameNode node : nodes){
                sum += node.defendCost;
                if(sum > HoneypotGameInfo.initialDefenderBudget)
                    break;
            }

            if(sum <= HoneypotGameInfo.initialDefenderBudget && isMaximalSet(nodes, sum)){
                defenderActions.add(new HashSet<HoneypotGameNode>(nodes));
            }
        }
    }

    protected boolean isMaximalSet(HashSet<HoneypotGameNode> nodes, double sum){
        for(HoneypotGameNode node : HoneypotGameInfo.allNodes){
            if(!nodes.contains(node) && sum + node.defendCost <= HoneypotGameInfo.initialDefenderBudget)
                return false;
        }
        return true;
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
            allNodes[i] = new HoneypotGameNode(i + 1, NODE_REWARDS[i], NODE_ATTACKERCOSTS[i], NODE_DEFENDERCOSTS[i]);
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
//            initialAttackerBudget = Integer.parseInt(line[1]);
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
                allNodes[i] = new HoneypotGameNode(i + 1, NODE_REWARDS[i], NODE_ATTACKERCOSTS[i], NODE_DEFENDERCOSTS[i]);
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
//            initialAttackerBudget = Integer.parseInt(line[1]);
            attacksAllowed = Integer.parseInt(line[2]);
//            uniformAttackCost = initialAttackerBudget / attacksAllowed;
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
            }

            Collections.sort(values, Comparator.comparingDouble(o -> -o));


            for (int i = 0; i < nodesCount; i++) {
//                allNodes[i] = new HoneypotGameNode(i + 1, values.get(i), uniformAttackCost, values.get(i));
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
        String info = "Honeypot Game : defender budget = " + initialDefenderBudget +
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
