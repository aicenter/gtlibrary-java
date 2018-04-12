package cz.agents.gtlibrary.algorithms.cfr.generalsum;

import cz.agents.gtlibrary.algorithms.cfr.ir.FixedForIterationData;
import cz.agents.gtlibrary.algorithms.mcts.AlgorithmData;
import cz.agents.gtlibrary.algorithms.mcts.MCTSConfig;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.distribution.Distribution;
import cz.agents.gtlibrary.algorithms.mcts.distribution.MeanStratDist;
import cz.agents.gtlibrary.algorithms.mcts.distribution.StrategyCollector;
import cz.agents.gtlibrary.algorithms.mcts.oos.OOSAlgorithmData;
import cz.agents.gtlibrary.domain.ir.memoryloss.MLExpander;
import cz.agents.gtlibrary.domain.ir.memoryloss.MLGameState;
import cz.agents.gtlibrary.domain.poker.generic.GPGameInfo;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerExpander;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerGameState;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerExpander;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerGameState;
import cz.agents.gtlibrary.domain.pursuit.PursuitExpander;
import cz.agents.gtlibrary.domain.pursuit.PursuitGameState;
import cz.agents.gtlibrary.domain.randomgame.GeneralSumRandomGameState;
import cz.agents.gtlibrary.domain.randomgame.RandomGameExpander;
import cz.agents.gtlibrary.domain.stacktest.StackTestExpander;
import cz.agents.gtlibrary.domain.stacktest.StackTestGameState;
import cz.agents.gtlibrary.experimental.utils.UtilityCalculator;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.strategy.Strategy;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CFRISGenSumBRAlg extends CFRISGenSumAlg {

    public static void main(String[] args) {
//         runKuhnPoker();
//        runStackTest();
//        runPursuit();
//        runGenericPoker();
//        runML();
        runGenSumRandom();
    }

    private static void runGenSumRandom() {
        GameState rootState = new GeneralSumRandomGameState();
        Expander<MCTSInformationSet> cfrExpander1 = new RandomGameExpander<>(new MCTSConfig());
        CFRISGenSumBRAlg cfr = new CFRISGenSumBRAlg(rootState.getAllPlayers()[0], rootState, cfrExpander1);

        cfr.runMiliseconds(10000);
    }

    private static void runML() {
        GameState rootState = new MLGameState();
        Expander<MCTSInformationSet> cfrExpander1 = new MLExpander<>(new MCTSConfig());
        CFRISGenSumBRAlg cfr = new CFRISGenSumBRAlg(rootState.getAllPlayers()[0], rootState, cfrExpander1);

        cfr.runMiliseconds(10000);
    }

    private static void runPursuit() {
        GameState rootState = new PursuitGameState();
        Expander<MCTSInformationSet> cfrExpander1 = new PursuitExpander<>(new MCTSConfig());
        CFRISGenSumBRAlg cfr = new CFRISGenSumBRAlg(rootState.getAllPlayers()[0], rootState, cfrExpander1);

        cfr.runMiliseconds(10000);
    }

    private static void runStackTest() {
        GameState rootState = new StackTestGameState();
        Expander<MCTSInformationSet> cfrExpander1 = new StackTestExpander<>(new MCTSConfig());
        CFRISGenSumBRAlg cfr = new CFRISGenSumBRAlg(rootState.getAllPlayers()[0], rootState, cfrExpander1);

        cfr.runMiliseconds(10000);
    }

    private static void runKuhnPoker() {
        GameState rootState = new KuhnPokerGameState();
        Expander<MCTSInformationSet> cfrExpander1 = new KuhnPokerExpander<>(new MCTSConfig());
        CFRISGenSumBRAlg cfr = new CFRISGenSumBRAlg(rootState.getAllPlayers()[0], rootState, cfrExpander1);

        cfr.runMiliseconds(10000);
    }

    private static void runGenericPoker() {
        new GPGameInfo();
        GameState rootState = new GenericPokerGameState();
        Expander<MCTSInformationSet> cfrExpander1 = new GenericPokerExpander<>(new MCTSConfig());
        CFRISGenSumBRAlg cfr = new CFRISGenSumBRAlg(rootState.getAllPlayers()[0], rootState, cfrExpander1);

        cfr.runMiliseconds(160000);
    }


    public CFRISGenSumBRAlg(Player searchingPlayer, GameState rootState, Expander expander) {
        super(searchingPlayer, rootState, expander);
    }

    @Override
    protected AlgorithmData createAlgData(GameState node) {
        if (node.getPlayerToMove().equals(searchingPlayer))
            return new FixedForIterationData(expander.getActions(node));
        return new BRFixedForIterationData(expander.getActions(node));
    }

    @Override
    public Action runMiliseconds(int miliseconds) {
        int iters = 0;
        long start = threadBean.getCurrentThreadCpuTime();

        while ((threadBean.getCurrentThreadCpuTime() - start) / 1e6 < miliseconds) {
            System.out.println("-----------------------");
            System.out.println(Arrays.toString(iteration(rootState, 1, 1, rootState.getAllPlayers()[0])));
            update();
            System.out.println("Current strategy: ");
            System.out.println(StrategyCollector.getStrategyFor(rootState, rootState.getAllPlayers()[0], new CurrentStrategyDistribution(), informationSets, expander));
            System.out.println("Mean strategy: ");
            System.out.println(StrategyCollector.getStrategyFor(rootState, rootState.getAllPlayers()[0], new MeanStratDist(), informationSets, expander));
            iters++;
            System.out.println(Arrays.toString(iteration(rootState, 1, 1, rootState.getAllPlayers()[1])));
            update();
            System.out.println("Current strategy: ");
            System.out.println(StrategyCollector.getStrategyFor(rootState, rootState.getAllPlayers()[1], new CurrentStrategyDistribution(), informationSets, expander));
            System.out.println("Mean strategy: ");
            System.out.println(StrategyCollector.getStrategyFor(rootState, rootState.getAllPlayers()[1], new MeanStratDist(), informationSets, expander));
            iters++;
        }
        firstIteration = false;
        System.out.println();
        System.out.println("Iters: " + iters);
        Strategy p1Strategy = StrategyCollector.getStrategyFor(rootState, rootState.getAllPlayers()[0], new MeanStratDist(), informationSets, expander);
        Strategy p2Strategy = StrategyCollector.getStrategyFor(rootState, rootState.getAllPlayers()[1], new MeanStratDist(), informationSets, expander);
        UtilityCalculator calculator = new UtilityCalculator(rootState, expander);

        System.out.println("Final utility: " + calculator.computeUtility(p1Strategy, p2Strategy));
        return null;
    }

    private void update() {
        for (MCTSInformationSet informationSet : informationSets.values()) {
            if (informationSet.getAlgorithmData() instanceof FixedForIterationData)
                ((FixedForIterationData) informationSet.getAlgorithmData()).applyUpdate();
        }
    }

    private class CurrentStrategyDistribution implements Distribution {
        @Override
        public Map<Action, Double> getDistributionFor(AlgorithmData data) {
            List<Action> actions = ((OOSAlgorithmData) data).getActions();
            Map<Action, Double> distribution = new HashMap<>(actions.size());
            double[] strategy = ((OOSAlgorithmData) data).getRMStrategy();
            int index = 0;

            for (Action action : actions) {
                distribution.put(action, strategy[index++]);
            }
            return distribution;
        }
    }
}
