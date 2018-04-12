package cz.agents.gtlibrary.algorithms.cfr.ir;

import cz.agents.gtlibrary.algorithms.cfr.CFRISAlgorithm;
import cz.agents.gtlibrary.algorithms.mcts.AlgorithmData;
import cz.agents.gtlibrary.algorithms.mcts.MCTSConfig;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.distribution.MeanStrategyProvider;
import cz.agents.gtlibrary.algorithms.mcts.oos.OOSAlgorithmData;
import cz.agents.gtlibrary.domain.ir.leftright.LRExpander;
import cz.agents.gtlibrary.domain.ir.leftright.LRGameState;
import cz.agents.gtlibrary.domain.ir.memoryloss.MLExpander;
import cz.agents.gtlibrary.domain.ir.memoryloss.MLGameState;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerExpander;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerGameState;
import cz.agents.gtlibrary.interfaces.*;

import java.util.*;

@Deprecated
public class CFRISIRAlgorithm extends CFRISAlgorithm {

    public static void main(String[] args) {
        runML();
//        runKuhnPoker();
//        runLR();
    }

    private static void runKuhnPoker() {
        GameState rootState = new KuhnPokerGameState();
        Expander<MCTSInformationSet> cfrExpander1 = new KuhnPokerExpander<>(new MCTSConfig());
        CFRISAlgorithm cfr = new CFRISIRAlgorithm(rootState.getAllPlayers()[0], rootState, cfrExpander1);

        cfr.runMiliseconds(10000);
    }

    private static void runLR() {
        GameState rootState = new LRGameState();
        Expander<MCTSInformationSet> cfrExpander1 = new LRExpander<>(new MCTSConfig());
        CFRISAlgorithm cfr = new CFRISIRAlgorithm(rootState.getAllPlayers()[0], rootState, cfrExpander1);

        cfr.runMiliseconds(100000);
    }

    private static void runML() {
        GameState rootState = new MLGameState();
        Expander<MCTSInformationSet> cfrExpander1 = new MLExpander<>(new MCTSConfig());
        CFRISAlgorithm cfr = new CFRISIRAlgorithm(rootState.getAllPlayers()[0], rootState, cfrExpander1);

        cfr.runMiliseconds(100000);
    }

    public CFRISIRAlgorithm(Player searchingPlayer, GameState rootState, Expander expander) {
        super(searchingPlayer, rootState, expander);
    }

//    @Override
//    protected AlgorithmData createAlgData(GameState node) {
//        return new CFRIRData(expander.getActions(node));
//    }

    @Override
    public Action runMiliseconds(int miliseconds) { //  jiná init strategie, update s mejma pravděpodobnostma
        int iters = 0;
        long start = threadBean.getCurrentThreadCpuTime();

        for (; (threadBean.getCurrentThreadCpuTime() - start) / 1e6 < miliseconds; ) {
            System.out.println("P0 reward: " + iteration(rootState, 1, 1, rootState.getAllPlayers()[0]));
            update(rootState, 1, 1, rootState.getAllPlayers()[0]);
            printMeanStrategy(rootState.getAllPlayers()[0]);
            printStrategy(rootState.getAllPlayers()[0]);
            iters++;

            System.out.println("P1 reward: " + iteration(rootState, 1, 1, rootState.getAllPlayers()[1]));
            update(rootState, 1, 1, rootState.getAllPlayers()[1]);
            printMeanStrategy(rootState.getAllPlayers()[1]);
            printStrategy(rootState.getAllPlayers()[1]);
            iters++;
            System.out.println("-----------------------");
        }
        firstIteration = false;
        System.out.println();
        System.out.println("Iters: " + iters);
        return null;
    }

    private void update(GameState node, double pi1, double pi2, Player expPlayer) {
//        if (node.isGameEnd() || (pi1 == 0 && pi2 == 0))
//            return;
//
//        MCTSInformationSet is = informationSets.get(node.getISKeyForPlayerToMove());
//
//        assert is != null;
//        CFRIRData data = (CFRIRData) is.getAlgorithmData();
//        List<Action> actions = data.getActions();
//
//        if (node.isPlayerToMoveNature()) {
//            for (Action ai : actions) {
//                final double p = node.getProbabilityOfNatureFor(ai);
//                double new_p1 = expPlayer.getId() == 1 ? pi1 * p : pi1;
//                double new_p2 = expPlayer.getId() == 0 ? pi2 * p : pi2;
//
//                update(node.performAction(ai), new_p1, new_p2, expPlayer);
//            }
//            return;
//        }
//
//        double[] rmProbs = getStrategy(data, node);
//
//        int i = -1;
//        for (Action ai : actions) {
//            i++;
//            if (is.getPlayer().getId() == 0)
//                update(node.performAction(ai), pi1 * rmProbs[i], pi2, expPlayer);
//            else
//                update(node.performAction(ai), pi1, rmProbs[i] * pi2, expPlayer);
//        }
//        if (is.getPlayer().equals(expPlayer))
//            if (data.applyUpdate())
//                data.updateMeanStrategy(rmProbs, (expPlayer.getId() == 0 ? pi1 : pi2));
    }

    private void printMeanStrategy(Player player) {
        Map<InformationSet, double[]> strategy = new HashMap<>();
        Queue<GameState> queue = new ArrayDeque<>();

        queue.add(rootState);
        while (!queue.isEmpty()) {
            GameState current = queue.poll();

            if (current.isGameEnd())
                continue;
            MCTSInformationSet currentSet = informationSets.get(current.getISKeyForPlayerToMove());
            OOSAlgorithmData data = (OOSAlgorithmData) currentSet.getAlgorithmData();

            if (current.getPlayerToMove().equals(player))
                strategy.put(currentSet, getMeanDist(data));
            for (Action action : data.getActions())
                queue.add(current.performAction(action));
        }
        System.out.println(player + " mean: ");
        print(strategy);
    }
//
//    @Override
//    protected void update(GameState state, double pi1, double pi2, Player expPlayer, OOSAlgorithmData data, double[] rmProbs, double[] tmpV, double ev) {
//        data.updateAllRegrets(tmpV, ev, (expPlayer.getId() == 0 ? pi2 : pi1));
//    }

    private void print(Map<InformationSet, double[]> strategy) {
        for (Map.Entry<InformationSet, double[]> entry : strategy.entrySet()) {
            System.out.println(entry.getKey() + ": " + Arrays.toString(entry.getValue()));
        }
    }

    private void printStrategy(Player player) {
        Map<InformationSet, double[]> strategy = new HashMap<>();
        Queue<GameState> queue = new ArrayDeque<>();

        queue.add(rootState);
        while (!queue.isEmpty()) {
            GameState current = queue.poll();

            if (current.isGameEnd())
                continue;
            MCTSInformationSet currentSet = informationSets.get(current.getISKeyForPlayerToMove());
            OOSAlgorithmData data = (OOSAlgorithmData) currentSet.getAlgorithmData();

            if (current.getPlayerToMove().equals(player))
                strategy.put(currentSet, data.getRMStrategy());
            for (Action action : data.getActions()) {
                queue.add(current.performAction(action));
            }
        }
        System.out.println(player + " current: ");
        print(strategy);
    }

    private double[] getMeanDist(AlgorithmData data) {
        MeanStrategyProvider stat = (MeanStrategyProvider) data;

        if (stat == null)
            return null;
        final double[] mp = stat.getMp();
        double sum = 0;

        for (double d : mp)
            sum += d;

        double[] distribution = new double[mp.length];

        for (int i = 0; i < mp.length; i++)
            distribution[i] = mp[i] / sum;
        return distribution;
    }
}
