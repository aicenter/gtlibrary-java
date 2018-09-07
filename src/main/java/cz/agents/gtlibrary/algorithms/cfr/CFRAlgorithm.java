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

import cz.agents.gtlibrary.NotImplementedException;
import cz.agents.gtlibrary.algorithms.cr.CFRData;
import cz.agents.gtlibrary.algorithms.cr.gadgettree.GadgetChanceNode;
import cz.agents.gtlibrary.algorithms.mcts.MCTSConfig;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.distribution.MeanStratDist;
import cz.agents.gtlibrary.algorithms.mcts.distribution.StrategyCollector;
import cz.agents.gtlibrary.algorithms.mcts.nodes.ChanceNodeImpl;
import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNodeImpl;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.ChanceNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.LeafNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.Node;
import cz.agents.gtlibrary.algorithms.mcts.oos.OOSAlgorithmData;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.BackPropFactory;
import cz.agents.gtlibrary.algorithms.sequenceform.gensum.experiments.StrategyStrengthLargeExperiments;
import cz.agents.gtlibrary.domain.aceofspades.AoSExpander;
import cz.agents.gtlibrary.domain.aceofspades.AoSGameState;
import cz.agents.gtlibrary.domain.informeraos.InformerAoSExpander;
import cz.agents.gtlibrary.domain.informeraos.InformerAoSGameState;
import cz.agents.gtlibrary.domain.mpochm.MPoCHMExpander;
import cz.agents.gtlibrary.domain.mpochm.MPoCHMGameState;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.Pair;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.*;


/**
 * @author vilo
 */
public class CFRAlgorithm implements GamePlayingAlgorithm {

    private Double normalizingUtils = 1.0;

    public static void main(String[] args) {
        runMPoCHM();
//        runIAoS();
//        runAoS();
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
        if (rootState.isPlayerToMoveNature()) this.rootNode = new ChanceNodeImpl(expander, rootState);
        else this.rootNode = new InnerNodeImpl(expander, rootState);
        threadBean = ManagementFactory.getThreadMXBean();
    }

    public CFRAlgorithm(Player searchingPlayer, InnerNode rootNode) {
        this.searchingPlayer = searchingPlayer;
        this.rootNode = rootNode;
        threadBean = ManagementFactory.getThreadMXBean();
    }

    public CFRAlgorithm(InnerNode rootNode) {
        this.searchingPlayer = null;
        this.rootNode = rootNode;
        threadBean = ManagementFactory.getThreadMXBean();
    }

    public CFRAlgorithm(GadgetChanceNode rootNode) {
        this((InnerNode) rootNode);
        this.normalizingUtils = rootNode.getRootReachPr();
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
            return ((LeafNode) node).getUtilities()[expPlayer.getId()] * normalizingUtils;
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

    public void printStrategies(InnerNode root) {
        ArrayDeque<Node> q = new ArrayDeque<>();
        q.add(root);

        while(!q.isEmpty()) {
            Node n = q.removeFirst();

            if(n.getParent() != null) {
                InnerNode par = n.getParent();
                double p = 1;
                if(par instanceof ChanceNode) {
                   p = par.getProbabilityOfNatureFor(n.getLastAction());
                } else {
                    p = ((OOSAlgorithmData) par.getInformationSet().getAlgorithmData())
                            .getMeanStrategy()[par.getActions().indexOf(n.getLastAction())];
                }
                System.out.println(par + " -> " + n + " : P"+p);
            }

            if(n instanceof InnerNode) {
                q.addAll(((InnerNode) n).getChildren().values());
            }
        }
    }

    public double computeCFVofIS(MCTSInformationSet is) {
        double cfv = 0.0;
        Player isPlayer = is.getPlayer();
        Player opPlayer = is.getOpponent();

        for(InnerNode n : is.getAllNodes()) {
            double rp = calcRpPlayerChanceOfNode(n, isPlayer);
            double eu = computeExpUtilityOfState(n, opPlayer);
            cfv += rp * eu;
        }

        return cfv;
    }

    public double calcRpPlayerChanceOfNode(InnerNode n, Player isPlayer) {
        double rp = 1.;
        Node curNode = n;
        while(curNode.getParent() != null) {
            Action a = curNode.getLastAction();
            Node parent = curNode.getParent();

            if(parent instanceof ChanceNode) {
                rp *= parent.getProbabilityOfNatureFor(a);
            } else if(parent instanceof InnerNode &&
                    ((InnerNode) parent).getPlayerToMove().equals(isPlayer)) {
                OOSAlgorithmData data = (OOSAlgorithmData) ((InnerNode) parent).getInformationSet().getAlgorithmData();
                rp *= data.getMeanStrategy()[data.getActions().indexOf(a)];
            }

            curNode = parent;
        }
        return rp;
    }

    public static double calcRpPlayerOfNode(InnerNode n, Player isPlayer) {
        double rp = 1.;
        Node curNode = n;
        while(curNode.getParent() != null) {
            Action a = curNode.getLastAction();
            Node parent = curNode.getParent();

            if(parent instanceof InnerNode &&
                    ((InnerNode) parent).getPlayerToMove().equals(isPlayer)) {
                OOSAlgorithmData data = (OOSAlgorithmData) ((InnerNode) parent).getInformationSet().getAlgorithmData();
                rp *= data.getMeanStrategy()[data.getActions().indexOf(a)];
            }

            curNode = parent;
        }
        return rp;
    }

    public double[] computeCFVAofIS(MCTSInformationSet is) {
        double[] cfva = new double[is.getActions().size()];
//        Player isPlayer = is.getPlayer();
        Player isPlayer = is.getAllNodes().iterator().next().getAllPlayers()[1-is.getPlayer().getId()]; // opp

        for(InnerNode n : is.getAllNodes()) {
            // calc reach probability (chance moves / opponent moves)
            double rp = 1.;
            Node curNode = n;
            while(curNode.getParent() != null) {
                Action a = curNode.getLastAction();
                InnerNode parent = curNode.getParent();

                if(parent instanceof ChanceNode) {
                    rp *= parent.getProbabilityOfNatureFor(a);
                } else if(parent instanceof InnerNode &&
                        !parent.getPlayerToMove().equals(isPlayer)) { // opp player
                    OOSAlgorithmData data = (OOSAlgorithmData) parent.getInformationSet().getAlgorithmData();
                    rp *= data.getMeanStrategy()[data.getActions().indexOf(a)];
                }

                curNode = parent;
            }

            for (int a = 0; a < cfva.length; a++) {
                double eu = computeExpUtilityOfState(n.getChildFor(is.getActions().get(a)), isPlayer);
                cfva[a] += rp * eu;
            }


        }
        return cfva;
    }

    public static double computeExpUtilityOfState(Node node, Player player) {
        if (node instanceof LeafNode) {
            return ((LeafNode) node).getUtilities()[player.getId()];
        }

        double eu = 0;

        if (node instanceof ChanceNode) {
            ChanceNode cn = (ChanceNode) node;
            for (Action a : cn.getActions()) {
                final double p = cn.getGameState().getProbabilityOfNatureFor(a);
                eu += p * computeExpUtilityOfState(cn.getChildFor(a), player);
            }
            return eu;
        }

        assert node instanceof InnerNode;
        InnerNode in = (InnerNode) node;
        OOSAlgorithmData data = (OOSAlgorithmData) in.getInformationSet().getAlgorithmData();
        double[] ms = data.getMeanStrategy();

        for (Action a : in.getActions()) {
             eu += ms[data.getActions().indexOf(a)] * computeExpUtilityOfState(in.getChildFor(a), player);
        }

        return eu;
    }


    public static CFRData collectCFRResolvingData(Set<PublicState> startAtPs) {
        ArrayDeque<PublicState> q = new ArrayDeque<>();
        q.addAll(startAtPs);
        Map<InnerNode, Double> reachProbs = new HashMap<>();
        Map<InnerNode, Double> historyExpValues = new HashMap<>();
        while (!q.isEmpty()) {
            PublicState ps = q.removeFirst();
            for (MCTSInformationSet is : ps.getAllInformationSets()) {
                for (InnerNode in : is.getAllNodes()) {
                    reachProbs.put(in, calcRpPlayerOfNode(in, is.getPlayer()));
                    historyExpValues.put(in, computeExpUtilityOfState(in, is.getOpponent()));
                }
            }
            q.addAll(ps.getNextPlayerPublicStates());
        }
        return new CFRData(reachProbs, historyExpValues);
    }

    public static CFRData collectCFRResolvingData(PublicState startAtPs) {
        Set<PublicState> hs = new HashSet<PublicState>();
        hs.add(startAtPs);
        return collectCFRResolvingData(hs);
    }

    public static void updateCFRResolvingData(
            PublicState publicState,
            Map<InnerNode, Double> reachProbs,
            Map<InnerNode, Double> historyExpValues) {
        for (MCTSInformationSet is : publicState.getAllInformationSets()) {
            ((OOSAlgorithmData) is.getAlgorithmData()).resetData();
            for (InnerNode in : is.getAllNodes()) {
                in.setReachPrByPlayer(is.getPlayer(), reachProbs.get(in));
                in.setExpectedValue(historyExpValues.get(in));
            }
        }
    }
}
