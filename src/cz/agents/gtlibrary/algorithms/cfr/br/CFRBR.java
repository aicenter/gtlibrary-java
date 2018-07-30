package cz.agents.gtlibrary.algorithms.cfr.br;

import cz.agents.gtlibrary.algorithms.cfr.CFRAlgorithm;
import cz.agents.gtlibrary.algorithms.mcts.MCTSConfig;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.distribution.MeanStratDist;
import cz.agents.gtlibrary.algorithms.mcts.distribution.StrategyCollector;
import cz.agents.gtlibrary.algorithms.mcts.nodes.ChanceNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.LeafNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.Node;
import cz.agents.gtlibrary.algorithms.mcts.oos.OOSAlgorithmData;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.gensum.experiments.StrategyStrengthLargeExperiments;
import cz.agents.gtlibrary.domain.flipit.*;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerExpander;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerGameState;
import cz.agents.gtlibrary.domain.randomgame.RandomGameExpander;
import cz.agents.gtlibrary.domain.randomgame.RandomGameInfo;
import cz.agents.gtlibrary.domain.randomgame.RandomGameState;
import cz.agents.gtlibrary.experimental.utils.UtilityCalculator;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.strategy.Strategy;
import cz.agents.gtlibrary.utils.io.GambitEFG;

import java.util.Arrays;
import java.util.Map;

public class CFRBR extends CFRAlgorithm {

    protected static int BRplayer = 1;
    protected static double eps = 1e-3;

    public CFRBR(Player searchingPlayer, GameState rootState, Expander expander, int BRplayer) {
        super(searchingPlayer, rootState, expander);
        this.BRplayer = BRplayer;
    }

    public static void main(String[] args) {
//        runMPoCHM();
//        runIAoS();
//        runAoS();
//        runKuhnPoker();
        runFlipIt();
//        runRandom();
    }

    private static void runRandom(){
        GameState rootState = new RandomGameState();
        GameInfo gameInfo = new RandomGameInfo();


//        SequenceFormConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
//        RandomGameExpander<SequenceInformationSet> expander = new RandomGameExpander<SequenceInformationSet>(algConfig);

        RandomGameExpander<MCTSInformationSet> expander = new RandomGameExpander<>(new MCTSConfig());
        Expander<SequenceInformationSet> brExpander = new RandomGameExpander<>(new SequenceFormConfig<>());

        CFRBR cfr = new CFRBR(rootState.getAllPlayers()[0], rootState, expander, BRplayer);
        StrategyStrengthLargeExperiments.buildCFRBRCompleteTree(cfr.getRootNode(), BRplayer);

        BestResponse response = new PureResponse(cfr.getRootNode().getGameState().getAllPlayers()[BRplayer], cfr.getRootNode());

        cfr.runIterations(10000000, response);

    }


    private static void runFlipIt() {

        FlipItGameInfo gameInfo = new FlipItGameInfo();
        gameInfo.ZERO_SUM_APPROX = true;
        gameInfo.gameVersion = FlipItGameInfo.FlipItInfo.REVEALED_ALL_POINTS;
        gameInfo.depth = 3;
        gameInfo.ENABLE_PASS = false;
        gameInfo.graphFile = "flipit_empty2.txt";

        System.out.println(gameInfo.getInfo());

        NodePointsFlipItGameState rootState = null;

        switch (FlipItGameInfo.gameVersion){
            case NO:                    rootState = new NoInfoFlipItGameState(); break;
            case FULL:                  rootState = new FullInfoFlipItGameState(); break;
            case REVEALED_ALL_POINTS:   rootState = new AllPointsFlipItGameState(); break;
            case REVEALED_NODE_POINTS:  rootState = new NodePointsFlipItGameState(); break;
        }

        FlipItExpander<MCTSInformationSet> expander = new FlipItExpander<>(new MCTSConfig());
        Expander<SequenceInformationSet> brExpander = new FlipItExpander<>(new SequenceFormConfig<>());

        CFRBR cfr = new CFRBR(rootState.getAllPlayers()[0], rootState, expander, BRplayer);
        StrategyStrengthLargeExperiments.buildCFRBRCompleteTree(cfr.getRootNode(), BRplayer);

//        BestResponse response = new PureResponse(cfr.getRootNode().getGameState().getAllPlayers()[BRplayer], cfr.getRootNode());
        BestResponse response = new QuantalResponse(cfr.getRootNode().getGameState().getAllPlayers()[BRplayer], cfr.getRootNode(), 0.000001);

        cfr.runIterations(10000000, response);

        Strategy p1rp = StrategyCollector.getStrategyFor(cfr.getRootNode(), rootState.getAllPlayers()[0], new MeanStratDist());
        Strategy p2rp = StrategyCollector.getStrategyFor(cfr.getRootNode(), rootState.getAllPlayers()[1], new MeanStratDist());

        UtilityCalculator calculator = new UtilityCalculator(rootState, brExpander);

        System.out.println(calculator.computeUtility(p1rp, p2rp));

        int p1strategySize = 0;
        for (Map.Entry<Sequence, Double> entry : p1rp.entrySet()) {
            if (entry.getValue() > eps) {
//                System.out.println(entry);
                p1strategySize++;
            }
        }
        System.out.println("P1 Strategy size = " + p1strategySize);
        System.out.println("-----------");
        int p2strategySize = 0;
        for (Map.Entry<Sequence, Double> entry : p2rp.entrySet()) {
            if (entry.getValue() > eps) {
//                System.out.println(entry);
                p2strategySize++;
            }
        }
        System.out.println("P2 Strategy size = " + p2strategySize);
    }

    private static void runKuhnPoker() {
        GameState root = new KuhnPokerGameState();
        Expander<MCTSInformationSet> expander = new KuhnPokerExpander<>(new MCTSConfig());
        Expander<SequenceInformationSet> brExpander = new KuhnPokerExpander<>(new SequenceFormConfig<>());
        CFRBR cfr = new CFRBR(root.getAllPlayers()[0], root, expander,1);
        StrategyStrengthLargeExperiments.buildCFRBRCompleteTree(cfr.getRootNode(), BRplayer);

        BestResponse response = new QuantalResponse(cfr.getRootNode().getGameState().getAllPlayers()[BRplayer], cfr.getRootNode(), -10);
//        BestResponse response = new PureResponse(cfr.getRootNode().getGameState().getAllPlayers()[BRplayer], cfr.getRootNode());

        cfr.runIterations(1000000, response);

        Strategy p1rp = StrategyCollector.getStrategyFor(cfr.getRootNode(), root.getAllPlayers()[0], new MeanStratDist());
        Strategy p2rp = StrategyCollector.getStrategyFor(cfr.getRootNode(), root.getAllPlayers()[1], new MeanStratDist());

        UtilityCalculator calculator = new UtilityCalculator(root, brExpander);

        System.out.println(calculator.computeUtility(p1rp, p2rp));


        for (Map.Entry<Sequence, Double> entry : p1rp.entrySet()) {
            if (entry.getValue() > 0)
                System.out.println(entry);
        }
        System.out.println("-----------");
        for (Map.Entry<Sequence, Double> entry : p2rp.entrySet()) {
            if (entry.getValue() > 0)
                System.out.println(entry);
        }

//        GambitEFG gambitEFG = new GambitEFG();
//        gambitEFG.write("GP.gbt", root, expander);
    }

//    @Override
    public Action runMiliseconds(int miliseconds, BestResponse response) {
        int iters = 0;
        long start = threadBean.getCurrentThreadCpuTime();
        for (; (threadBean.getCurrentThreadCpuTime() - start) / 1e6 < miliseconds; ) {
            iteration(rootNode, 1, 1, rootNode.getGameState().getAllPlayers()[0]);
            iters++;
            response.computeBR(rootNode);//(rootNode, 1, 1, rootNode.getGameState().getAllPlayers()[1],100);
            iters++;
        }
        System.out.println();
        System.out.println("Iters: " + iters);
        return null;
    }

    public Action runIterations(int iterations, BestResponse response) {
        System.out.println("Running");
        double value = 0.0;
        for (int i = 0; i < iterations; i++) {
            if (i % 1000 == 0)
                System.out.println("Iteration " + i + ", value = " + value);
            for(int j = 0; j < 10; j++) {
                value = iteration(rootNode, 1, 1, rootNode.getGameState().getAllPlayers()[1 - BRplayer]);
            }
//            iteration(rootNode, 1, 1, rootNode.getGameState().getAllPlayers()[BRplayer]);
            response.computeBR(rootNode);//(rootNode, 1, 1, rootNode.getGameState().getAllPlayers()[BRplayer],0.001);
        }
        return null;
    }

    protected double iteration(Node node, double pi1, double pi2, Player expPlayer) {
        if (pi1 == 0 && pi2 == 0) return 0;
        if (node instanceof LeafNode) {
            return ((LeafNode) node).getUtilities()[expPlayer.getId()];
        }
        if (node instanceof ChanceNode) {
            ChanceNode cn = (ChanceNode) node;
            double ev = 0;
            for (Action ai : cn.getActions()) {
                final double p = cn.getGameState().getProbabilityOfNatureFor(ai);
                double new_p1 = expPlayer.getId() == 1 ? pi1 * p : pi1;
                double new_p2 = expPlayer.getId() == 0 ? pi2 * p : pi2;
                ev += p * iteration(cn.getChildFor(ai), new_p1, new_p2, expPlayer);
            }
            return ev;
        }
        InnerNode in = (InnerNode) node;
        MCTSInformationSet is = in.getInformationSet();
        CFRBRAlgorithmData data = (CFRBRAlgorithmData) is.getAlgorithmData();

        double[] strategy = data.getStrategyAsList();
        double[] expectedValues = new double[strategy.length];
        double ev = 0;

        int i = -1;
        for (Action ai : in.getActions()) {
            i++;
            if (is.getPlayer().getId() == 0) {
                expectedValues[i] = iteration(in.getChildFor(ai), pi1 * strategy[i], pi2, expPlayer);
            } else {
                expectedValues[i] = iteration(in.getChildFor(ai), pi1, strategy[i] * pi2, expPlayer);
            }
            ev += strategy[i] * expectedValues[i];
        }
        if (is.getPlayer().equals(expPlayer)) {
            data.updateAllRegrets(expectedValues, ev, (expPlayer.getId() == 0 ? pi2 : pi1));
            data.updateMeanStrategy(strategy, (expPlayer.getId() == 0 ? pi1 : pi2));
        }
        else{
//            System.out.println(Arrays.toString(strategy));
        }

        return ev;
    }

}
