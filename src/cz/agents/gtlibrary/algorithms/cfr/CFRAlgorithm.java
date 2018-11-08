/*
Copyright 2014 Faculty of Electrical Engineering at CTU in Prague

This file is part of Game Theoretic Library.

Game Theoretic Library is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Game Theoretic Library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with Game Theoretic Library.  If not, see <http://www.gnu.org/licenses/>.*/


/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.agents.gtlibrary.algorithms.cfr;

import cz.agents.gtlibrary.algorithms.mcts.ConvergenceExperiment;
import cz.agents.gtlibrary.algorithms.mcts.MCTSConfig;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.distribution.MeanStratDist;
import cz.agents.gtlibrary.algorithms.mcts.distribution.StrategyCollector;
import cz.agents.gtlibrary.algorithms.mcts.nodes.ChanceNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.LeafNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.Node;
import cz.agents.gtlibrary.algorithms.mcts.oos.OOSAlgorithmData;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.BackPropFactory;
import cz.agents.gtlibrary.algorithms.sequenceform.FullSequenceEFG;
import cz.agents.gtlibrary.algorithms.sequenceform.SQFBestResponseAlgorithm;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.gensum.experiments.StrategyStrengthLargeExperiments;
import cz.agents.gtlibrary.domain.aceofspades.AoSExpander;
import cz.agents.gtlibrary.domain.aceofspades.AoSGameState;
import cz.agents.gtlibrary.domain.flipit.*;
import cz.agents.gtlibrary.domain.informeraos.InformerAoSExpander;
import cz.agents.gtlibrary.domain.informeraos.InformerAoSGameState;
import cz.agents.gtlibrary.domain.mpochm.MPoCHMExpander;
import cz.agents.gtlibrary.domain.mpochm.MPoCHMGameState;
import cz.agents.gtlibrary.domain.poker.generic.GPGameInfo;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerExpander;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerGameState;
import cz.agents.gtlibrary.domain.poker.kuhn.KPGameInfo;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerExpander;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerGameState;
import cz.agents.gtlibrary.experimental.utils.UtilityCalculator;
import cz.agents.gtlibrary.iinodes.ConfigImpl;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.strategy.Strategy;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Map;


/**
 * @author vilo
 */
public class CFRAlgorithm implements GamePlayingAlgorithm {

    public static void main(String[] args) {

        long start = System.currentTimeMillis();


//        runMPoCHM();
//        runIAoS();
//        runAoS();
//        runKuhnPoker();

        runGenericPoker();
//        runFlipIt();

        System.out.println((System.currentTimeMillis() - start) + " ms");
    }

    private static void runGenericPoker() {
        GameState root = new GenericPokerGameState();
        GPGameInfo gameInfo = new GPGameInfo();
        Expander<MCTSInformationSet> expander = new GenericPokerExpander<>(new MCTSConfig());
//        Expander<SequenceInformationSet> brExpander = new GenericPokerExpander<>(new SequenceFormConfig<>());
        CFRAlgorithm cfr = new CFRAlgorithm(root.getAllPlayers()[0], root, expander);
        ConvergenceExperiment.buildCompleteTree(cfr.getRootNode());

//        cfr.runIterations(1000);
//
//        Strategy p1rp = StrategyCollector.getStrategyFor(cfr.getRootNode(), root.getAllPlayers()[0], new MeanStratDist());
//        Strategy p2rp = StrategyCollector.getStrategyFor(cfr.getRootNode(), root.getAllPlayers()[1], new MeanStratDist());
//
//        UtilityCalculator calculator = new UtilityCalculator(root, expander);
//
//        SQFBestResponseAlgorithm brAlg0 = new SQFBestResponseAlgorithm(expander, 0, new Player[]{root.getAllPlayers()[0], root.getAllPlayers()[1]}, (ConfigImpl) expander.getAlgorithmConfig()/*sfAlgConfig*/, gameInfo);
//        System.out.println(brAlg0.calculateBR(root, p2rp));

        long start;
        SQFBestResponseAlgorithm brAlg0 = new SQFBestResponseAlgorithm(expander, 0, new Player[]{root.getAllPlayers()[0], root.getAllPlayers()[1]}, (ConfigImpl) expander.getAlgorithmConfig()/*sfAlgConfig*/, gameInfo);
        long total = 0;
        for (int i=0; i<100; i++) {
            start = cfr.threadBean.getCurrentThreadCpuTime();
            cfr.runIterations(50);
            total +=  (cfr.threadBean.getCurrentThreadCpuTime() - start) / 1000000l;
            Strategy p2rp = StrategyCollector.getStrategyFor(cfr.getRootNode(), root.getAllPlayers()[1], new MeanStratDist());
            System.out.println(total + ":" + brAlg0.calculateBR(root, p2rp));
        }

//        System.out.println(calculator.computeUtility(p1rp, p2rp));

//        for (Map.Entry<Sequence, Double> entry : p1rp.entrySet()) {
//            if (entry.getValue() > 0)
//                System.out.println(entry);
//        }
//        System.out.println("-----------");
//        for (Map.Entry<Sequence, Double> entry : p2rp.entrySet()) {
//            if (entry.getValue() > 0)
//                System.out.println(entry);
//        }
    }

    private static void runFlipIt() {
        GameState root = null;
        FlipItGameInfo gameInfo = new FlipItGameInfo();

        if (FlipItGameInfo.CALCULATE_UTILITY_BOUNDS) gameInfo.calculateMinMaxBounds();

        switch (FlipItGameInfo.gameVersion){
            case NO:                    root = new NoInfoFlipItGameState(); break;
            case FULL:                  root = new FullInfoFlipItGameState(); break;
            case REVEALED_ALL_POINTS:   root = new AllPointsFlipItGameState(); break;
            case REVEALED_NODE_POINTS:  root = new NodePointsFlipItGameState(); break;

        }

        Expander<MCTSInformationSet> expander = new FlipItExpander<>(new MCTSConfig());
//        Expander<SequenceInformationSet> brExpander = new GenericPokerExpander<>(new SequenceFormConfig<>());
        CFRAlgorithm cfr = new CFRAlgorithm(root.getAllPlayers()[0], root, expander);
        long start = cfr.threadBean.getCurrentThreadCpuTime();
        ConvergenceExperiment.buildCompleteTree(cfr.getRootNode());
        System.out.println("Building took " + ((cfr.threadBean.getCurrentThreadCpuTime() - start)/1000000l) + " ms");

        Strategy p1rp = StrategyCollector.getStrategyFor(cfr.getRootNode(), root.getAllPlayers()[0], new MeanStratDist());

        UtilityCalculator calculator = new UtilityCalculator(root, expander);
        SQFBestResponseAlgorithm brAlg0 = new SQFBestResponseAlgorithm(expander, 0, new Player[]{root.getAllPlayers()[0], root.getAllPlayers()[1]}, (ConfigImpl) expander.getAlgorithmConfig()/*sfAlgConfig*/, gameInfo);
        long total = 0;
        for (int i=0; i<100; i++) {
            start = cfr.threadBean.getCurrentThreadCpuTime();
            cfr.runIterations(10);
            total +=  (cfr.threadBean.getCurrentThreadCpuTime() - start) / 1000000l;
            Strategy p2rp = StrategyCollector.getStrategyFor(cfr.getRootNode(), root.getAllPlayers()[1], new MeanStratDist());
            System.out.println(total + ":" + brAlg0.calculateBR(root, p2rp));
        }



    }

    private static void runKuhnPoker() {
        GameState root = new KuhnPokerGameState();
        Expander<MCTSInformationSet> expander = new KuhnPokerExpander<>(new MCTSConfig());
        Expander<SequenceInformationSet> brExpander = new KuhnPokerExpander<>(new SequenceFormConfig<>());
        CFRAlgorithm cfr = new CFRAlgorithm(root.getAllPlayers()[0], root, expander);
        StrategyStrengthLargeExperiments.buildCFRCompleteTree(cfr.getRootNode());

        cfr.runIterations(5000);

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

    private static void runMPoCHM() {
        GameState root = new MPoCHMGameState();
        Expander<MCTSInformationSet> expander = new MPoCHMExpander<>(new MCTSConfig());
        CFRAlgorithm cfr = new CFRAlgorithm(root.getAllPlayers()[0], root, expander);
        StrategyStrengthLargeExperiments.buildCFRCompleteTree(cfr.getRootNode());
        cfr.runIterations(1000000);

        Map<Sequence, Double> p1rp = StrategyCollector.getStrategyFor(cfr.getRootNode(), root.getAllPlayers()[0], new MeanStratDist());
        Map<Sequence, Double> p2rp = StrategyCollector.getStrategyFor(cfr.getRootNode(), root.getAllPlayers()[1], new MeanStratDist());

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

    private static void runAoS() {
        GameState root = new AoSGameState();
        Expander<MCTSInformationSet> expander = new AoSExpander<>(new MCTSConfig());
        CFRAlgorithm cfr = new CFRAlgorithm(root.getAllPlayers()[0], root, expander);
        StrategyStrengthLargeExperiments.buildCFRCompleteTree(cfr.getRootNode());
        cfr.runIterations(1000000);

        Map<Sequence, Double> p1rp = StrategyCollector.getStrategyFor(cfr.getRootNode(), root.getAllPlayers()[0], new MeanStratDist());
        Map<Sequence, Double> p2rp = StrategyCollector.getStrategyFor(cfr.getRootNode(), root.getAllPlayers()[1], new MeanStratDist());

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

    private static void runIAoS() {
        OOSAlgorithmData.useEpsilonRM = true;
        GameState root = new InformerAoSGameState();
        Expander<MCTSInformationSet> expander = new InformerAoSExpander<>(new MCTSConfig());
        CFRAlgorithm cfr = new CFRAlgorithm(root.getAllPlayers()[0], root, expander);
        StrategyStrengthLargeExperiments.buildCFRCompleteTree(cfr.getRootNode());
        cfr.runIterations(1000000);

        Map<Sequence, Double> p1rp = StrategyCollector.getStrategyFor(cfr.getRootNode(), root.getAllPlayers()[0], new MeanStratDist());
        Map<Sequence, Double> p2rp = StrategyCollector.getStrategyFor(cfr.getRootNode(), root.getAllPlayers()[1], new MeanStratDist());

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


    protected Player searchingPlayer;
    protected BackPropFactory fact;
    protected InnerNode rootNode;
    protected ThreadMXBean threadBean;


    public CFRAlgorithm(Player searchingPlayer, GameState rootState, Expander expander) {
        this.searchingPlayer = searchingPlayer;
        if (rootState.isPlayerToMoveNature()) this.rootNode = new ChanceNode(expander, rootState);
        else this.rootNode = new InnerNode(expander, rootState);
        threadBean = ManagementFactory.getThreadMXBean();
    }

    @Override
    public Action runMiliseconds(int miliseconds) {
        int iters = 0;
        long start = threadBean.getCurrentThreadCpuTime();
        for (; (threadBean.getCurrentThreadCpuTime() - start) / 1e6 < miliseconds; ) {
            iteration(rootNode, 1, 1, rootNode.getGameState().getAllPlayers()[0]);
            iters++;
            iteration(rootNode, 1, 1, rootNode.getGameState().getAllPlayers()[1]);
            iters++;
        }
        System.out.println();
        System.out.println("Iters: " + iters);
        return null;
    }

    public Action runIterations(int iterations) {
        for (int i = 0; i < iterations; i++) {
            iteration(rootNode, 1, 1, rootNode.getGameState().getAllPlayers()[0]);
            iteration(rootNode, 1, 1, rootNode.getGameState().getAllPlayers()[1]);
        }
        return null;
    }

    /**
     * The main function for CFR iteration. Implementation based on Algorithm 1 in M. Lanctot PhD thesis.
     *
     * @param node      current node
     * @param pi1       probability with which the opponent of the searching player and chance want to reach the current node
     * @param expPlayer the exploring player for this iteration
     * @return iteration game reward is actually returned. Other return values are in global x and l
     */
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


    @Override
    public void setCurrentIS(InformationSet is) {
        throw new NotImplementedException();
    }

    public InnerNode getRootNode() {
        return rootNode;
    }

    @Override
    public Action runMiliseconds(int miliseconds, GameState gameState) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
