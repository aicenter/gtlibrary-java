package cz.agents.gtlibrary.algorithms.cfr;

import cz.agents.gtlibrary.algorithms.mcts.MCTSConfig;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.distribution.MeanStratDist;
import cz.agents.gtlibrary.algorithms.mcts.distribution.StrategyCollector;
import cz.agents.gtlibrary.algorithms.mcts.nodes.ChanceNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.LeafNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.Node;
import cz.agents.gtlibrary.algorithms.mcts.oos.OOSAlgorithmData;
import cz.agents.gtlibrary.algorithms.qre.QuantalResponse;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.gensum.experiments.StrategyStrengthLargeExperiments;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerExpander;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerGameState;
import cz.agents.gtlibrary.experimental.utils.UtilityCalculator;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.strategy.Strategy;

import java.util.Map;

public class CFRBR extends CFRAlgorithm{

    protected int BRplayer = 1;

    public CFRBR(Player searchingPlayer, GameState rootState, Expander expander, int BRplayer) {
        super(searchingPlayer, rootState, expander);
        this.BRplayer = BRplayer;
    }

    public static void main(String[] args) {
//        runMPoCHM();
//        runIAoS();
//        runAoS();
        runKuhnPoker();
    }

    private static void runKuhnPoker() {
        GameState root = new KuhnPokerGameState();
        Expander<MCTSInformationSet> expander = new KuhnPokerExpander<>(new MCTSConfig());
        Expander<SequenceInformationSet> brExpander = new KuhnPokerExpander<>(new SequenceFormConfig<>());
        CFRAlgorithm cfr = new CFRBR(root.getAllPlayers()[0], root, expander,1);
        StrategyStrengthLargeExperiments.buildCFRCompleteTree(cfr.getRootNode());

        cfr.runIterations(10000);

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
    }

    @Override
    public Action runMiliseconds(int miliseconds) {
        int iters = 0;
        long start = threadBean.getCurrentThreadCpuTime();
        for (; (threadBean.getCurrentThreadCpuTime() - start) / 1e6 < miliseconds; ) {
            iteration(rootNode, 1, 1, rootNode.getGameState().getAllPlayers()[0]);
            iters++;
            QuantalResponse.computeQR(rootNode, 1, 1, rootNode.getGameState().getAllPlayers()[1],100);
            iters++;
        }
        System.out.println();
        System.out.println("Iters: " + iters);
        return null;
    }

    public Action runIterations(int iterations) {
        for (int i = 0; i < iterations; i++) {
            iteration(rootNode, 1, 1, rootNode.getGameState().getAllPlayers()[0]);
            QuantalResponse.computeQR(rootNode, 1, 1, rootNode.getGameState().getAllPlayers()[1],100);
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
        OOSAlgorithmData data = (OOSAlgorithmData) is.getAlgorithmData();

        double[] rmProbs = data.getRMStrategy();
        double[] tmpV = new double[rmProbs.length];
        double ev = 0;

        int i = -1;
        for (Action ai : in.getActions()) {
            i++;
            if (is.getPlayer().getId() == 0) {
                tmpV[i] = iteration(in.getChildFor(ai), pi1 * rmProbs[i], pi2, expPlayer);
            } else {
                tmpV[i] = iteration(in.getChildFor(ai), pi1, rmProbs[i] * pi2, expPlayer);
            }
            ev += rmProbs[i] * tmpV[i];
        }
        if (is.getPlayer().equals(expPlayer)) {
            data.updateAllRegrets(tmpV, ev, (expPlayer.getId() == 0 ? pi2 : pi1));
            data.updateMeanStrategy(rmProbs, (expPlayer.getId() == 0 ? pi1 : pi2));
        }

        return ev;
    }

}
