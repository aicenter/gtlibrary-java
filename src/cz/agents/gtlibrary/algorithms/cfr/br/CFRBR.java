package cz.agents.gtlibrary.algorithms.cfr.br;

import cz.agents.gtlibrary.algorithms.cfr.CFRAlgorithm;
import cz.agents.gtlibrary.algorithms.cfr.br.responses.BestResponse;
import cz.agents.gtlibrary.algorithms.cfr.br.responses.PureResponse;
import cz.agents.gtlibrary.algorithms.cfr.br.responses.QuantalResponse;
import cz.agents.gtlibrary.algorithms.cfr.br.responses.VanillaIBLResponse;
import cz.agents.gtlibrary.algorithms.mcts.MCTSConfig;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.distribution.MeanStratDist;
import cz.agents.gtlibrary.algorithms.mcts.distribution.StrategyCollector;
import cz.agents.gtlibrary.algorithms.mcts.nodes.ChanceNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.LeafNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.Node;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.gensum.experiments.StrategyStrengthLargeExperiments;
import cz.agents.gtlibrary.domain.flipit.*;
import cz.agents.gtlibrary.domain.honeypotGame.HoneypotExpander;
import cz.agents.gtlibrary.domain.honeypotGame.HoneypotGameInfo;
import cz.agents.gtlibrary.domain.honeypotGame.HoneypotGameState;
import cz.agents.gtlibrary.domain.honeypotGame.HoneypotTurntakingGameState;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerExpander;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerGameState;
import cz.agents.gtlibrary.domain.randomgame.RandomGameExpander;
import cz.agents.gtlibrary.domain.randomgame.RandomGameInfo;
import cz.agents.gtlibrary.domain.randomgame.RandomGameState;
import cz.agents.gtlibrary.experimental.utils.UtilityCalculator;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.strategy.Strategy;
import cz.agents.gtlibrary.utils.io.GambitEFG;

import java.util.Map;

public class CFRBR extends CFRAlgorithm {

    protected static int BRplayerIndex = 1;
    protected static Player BRplayer;
    protected static double eps = 1e-3;

    protected static boolean MAKE_MS_UPDATE = false;
    protected static boolean IS_ZERO_SUM = true;

    public CFRBR(Player searchingPlayer, GameState rootState, Expander expander, int BRplayer) {
        super(searchingPlayer, rootState, expander);
        this.BRplayerIndex = BRplayer;
        this.BRplayer = rootState.getAllPlayers()[BRplayer];
    }

    public static void main(String[] args) {
//        runMPoCHM();
//        runIAoS();
//        runAoS();
//        runKuhnPoker();
        runFlipIt();
//        runRandom();
//        runHoneyPot();
    }

    private static void runHoneyPot(){
        HoneypotGameInfo gameInfo = new HoneypotGameInfo();
        HoneypotTurntakingGameState rootState = new HoneypotTurntakingGameState(gameInfo.allNodes);

        System.out.println(HoneypotGameInfo.attacksAllowed + " " + rootState.getRemainingAttacks());

//        GambitEFG gambit = new GambitEFG();
//        gambit.buildAndWrite("TurntakingHoneyPot.gbt", rootState, new HoneypotExpander<>(new SequenceFormConfig<>()));

        HoneypotExpander<MCTSInformationSet> expander = new HoneypotExpander<>(new MCTSConfig());
//        Expander<SequenceInformationSet> brExpander = new HoneypotExpander<>(new SequenceFormConfig<>());

        CFRBR cfr = new CFRBR(rootState.getAllPlayers()[0], rootState, expander, BRplayerIndex);
        StrategyStrengthLargeExperiments.buildCFRBRCompleteTree(cfr.getRootNode(), BRplayerIndex);

//        BestResponse response = new PureResponse(cfr.getRootNode().getGameState().getAllPlayers()[BRplayerIndex], cfr.getRootNode());
//        BestResponse response = new QuantalResponse(cfr.getRootNode().getGameState().getAllPlayers()[BRplayerIndex], cfr.getRootNode(), 0.05);
        BestResponse response = new VanillaIBLResponse(cfr.getRootNode().getGameState().getAllPlayers()[BRplayerIndex], cfr.getRootNode(), 0.5, 0.5, cfr.getRootNode().getGameState().getAllPlayers()[1 - BRplayerIndex]);

        cfr.runIterations(100000, response);
    }

    private static void runRandom(){
        GameState rootState = new RandomGameState();
        GameInfo gameInfo = new RandomGameInfo();


//        SequenceFormConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
//        RandomGameExpander<SequenceInformationSet> expander = new RandomGameExpander<SequenceInformationSet>(algConfig);

        RandomGameExpander<MCTSInformationSet> expander = new RandomGameExpander<>(new MCTSConfig());
        Expander<SequenceInformationSet> brExpander = new RandomGameExpander<>(new SequenceFormConfig<>());

        CFRBR cfr = new CFRBR(rootState.getAllPlayers()[0], rootState, expander, BRplayerIndex);
        StrategyStrengthLargeExperiments.buildCFRBRCompleteTree(cfr.getRootNode(), BRplayerIndex);

        BestResponse response = new PureResponse(cfr.getRootNode().getGameState().getAllPlayers()[BRplayerIndex], cfr.getRootNode());

        cfr.runIterations(10000000, response);

    }


    private static void runFlipIt() {

        FlipItGameInfo gameInfo = new FlipItGameInfo();
        gameInfo.ZERO_SUM_APPROX = true;
        gameInfo.gameVersion = FlipItGameInfo.FlipItInfo.REVEALED_ALL_POINTS;
        gameInfo.depth = 1;
        gameInfo.ENABLE_PASS = false;
        gameInfo.graphFile = "flipit_empty2.txt";
        gameInfo.graph = new FlipItGraph(gameInfo.graphFile);

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

//        GambitEFG gambit = new GambitEFG();
//        gambit.buildAndWrite("SunnyFlip.gbt", rootState, new FlipItExpander<>(new SequenceFormConfig<>()));

        CFRBR cfr = new CFRBR(rootState.getAllPlayers()[0], rootState, expander, BRplayerIndex);
        StrategyStrengthLargeExperiments.buildCFRBRCompleteTree(cfr.getRootNode(), BRplayerIndex);

//        BestResponse response = new PureResponse(cfr.getRootNode().getGameState().getAllPlayers()[BRplayerIndex], cfr.getRootNode());
        BestResponse response = new QuantalResponse(cfr.getRootNode().getGameState().getAllPlayers()[BRplayerIndex], cfr.getRootNode(), 0.5);

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
        StrategyStrengthLargeExperiments.buildCFRBRCompleteTree(cfr.getRootNode(), BRplayerIndex);

        BestResponse response = new QuantalResponse(cfr.getRootNode().getGameState().getAllPlayers()[BRplayerIndex], cfr.getRootNode(), -10);
//        BestResponse response = new PureResponse(cfr.getRootNode().getGameState().getAllPlayers()[BRplayerIndex], cfr.getRootNode());

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
            iteration(rootNode, 1, 1, rootNode.getGameState().getAllPlayers()[0], BRplayerIndex);
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
            if (i % 10 == 0)
                System.out.println("Iteration " + i + ", value = " + value);
            for(int j = 0; j < 10; j++) {
                value = iteration(rootNode, 1, 1, rootNode.getGameState().getAllPlayers()[1 - BRplayerIndex], BRplayerIndex);
            }
//            iteration(rootNode, 1, 1, rootNode.getGameState().getAllPlayers()[BRplayerIndex]);
            response.computeBR(rootNode);//(rootNode, 1, 1, rootNode.getGameState().getAllPlayers()[BRplayerIndex],0.001);
        }
        return null;
    }

    public double runIteration(BestResponse response) {
        double value = iteration(rootNode, 1, 1, rootNode.getGameState().getAllPlayers()[1 - BRplayerIndex], BRplayerIndex);
        response.computeBR(rootNode);
        return value;
    }

    public double runCFRIteration() {
        return iteration(rootNode, 1, 1, searchingPlayer, 1 - searchingPlayer.getId());
    }

    public double runCFRIterationAgainst(int playerIdx, double playerProbability) {
        if (searchingPlayer.getId() == 0)
            return iteration(rootNode, 1.0, playerProbability, searchingPlayer, playerIdx);
        else
            return iteration(rootNode, playerProbability, 1.0, searchingPlayer, playerIdx);
    }

    public double runBRIteration(BestResponse response) {
        return response.computeBR(rootNode);
    }

    protected double iteration(Node node, double pi1, double pi2, Player expPlayer, int brPlayerIdx) {
        if (pi1 == 0 && pi2 == 0) return 0;
        if (node instanceof LeafNode) {
            if (!IS_ZERO_SUM){
                return -1.0 * ((LeafNode) node).getUtilities()[brPlayerIdx];
            }
            else {
                return ((LeafNode) node).getUtilities()[expPlayer.getId()];
            }
        }
        if (node instanceof ChanceNode) {
            ChanceNode cn = (ChanceNode) node;
            double ev = 0;
            for (Action ai : cn.getActions()) {
                final double p = cn.getGameState().getProbabilityOfNatureFor(ai);
                double new_p1 = expPlayer.getId() == 1 ? pi1 * p : pi1;
                double new_p2 = expPlayer.getId() == 0 ? pi2 * p : pi2;
                ev += p * iteration(cn.getChildFor(ai), new_p1, new_p2, expPlayer, brPlayerIdx);
            }
            return ev;
        }
        InnerNode in = (InnerNode) node;
        MCTSInformationSet is = in.getInformationSet();
        CFRBRAlgorithmData data = (CFRBRAlgorithmData) is.getAlgorithmData();

        double[] strategy = is.getPlayer().getId() != expPlayer.getId() ?
                data.getStrategyOfPlayerAsList(brPlayerIdx) : data.getStrategyOfPlayerAsList(expPlayer.getId());
        double[] expectedValues = new double[strategy.length];
        double ev = 0;

        int i = -1;
        for (Action ai : in.getActions()) {
            i++;
            if (is.getPlayer().getId() == 0) {
                expectedValues[i] = iteration(in.getChildFor(ai), pi1 * strategy[i], pi2, expPlayer, brPlayerIdx);
            } else {
                expectedValues[i] = iteration(in.getChildFor(ai), pi1, strategy[i] * pi2, expPlayer, brPlayerIdx);
            }
            ev += strategy[i] * expectedValues[i];
        }
        if (is.getPlayer().equals(expPlayer)) {
            data.updateAllRegrets(expectedValues, ev, (expPlayer.getId() == 0 ? pi2 : pi1));
            if (MAKE_MS_UPDATE) data.updateMeanStrategy(strategy, (expPlayer.getId() == 0 ? pi1 : pi2));
        }
        else{
//            System.out.println(Arrays.toString(strategy));
        }

        return ev;
    }

    public void setIsZeroSum(boolean isZeroSum){
        IS_ZERO_SUM = isZeroSum;
    }

}
