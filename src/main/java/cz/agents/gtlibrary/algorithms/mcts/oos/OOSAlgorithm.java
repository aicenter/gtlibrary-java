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
package cz.agents.gtlibrary.algorithms.mcts.oos;

import cz.agents.gtlibrary.algorithms.mccr.gadgettree.GadgetInnerAction;
import cz.agents.gtlibrary.algorithms.mccr.gadgettree.GadgetInnerNode;
import cz.agents.gtlibrary.algorithms.mccr.gadgettree.GadgetLeafNode;
import cz.agents.gtlibrary.algorithms.mcts.ConvergenceExperiment;
import cz.agents.gtlibrary.algorithms.mcts.MCTSConfig;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.distribution.MeanStratDist;
import cz.agents.gtlibrary.algorithms.mcts.distribution.StrategyCollector;
import cz.agents.gtlibrary.algorithms.mcts.experiments.SMConvergenceExperiment;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.ChanceNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.LeafNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.Node;
import cz.agents.gtlibrary.algorithms.mcts.nodes.*;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.BackPropFactory;
import cz.agents.gtlibrary.algorithms.mccr.gadgettree.GadgetChanceNode;
import cz.agents.gtlibrary.domain.aceofspades.AoSExpander;
import cz.agents.gtlibrary.domain.aceofspades.AoSGameInfo;
import cz.agents.gtlibrary.domain.aceofspades.AoSGameState;
import cz.agents.gtlibrary.domain.bpg.BPGExpander;
import cz.agents.gtlibrary.domain.bpg.BPGGameState;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.strategy.Strategy;
import cz.agents.gtlibrary.utils.HighQualityRandom;
import cz.agents.gtlibrary.utils.io.GambitEFG;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.*;


/**
 * @author vilo
 */
public class OOSAlgorithm implements GamePlayingAlgorithm {
    protected Player searchingPlayer;
    protected OOSSimulator simulator;
    protected BackPropFactory fact;
    protected InnerNode rootNode;
    protected ThreadMXBean threadBean;
    private double delta = 0.0;
    private double epsilon = 0.001;
    public boolean dropTree = false;
    public boolean useCurrentStrategy = true;

    private MCTSInformationSet curIS;
    private static int seed = 49;
    private Double normalizingUtils = 1.;
    private Random rnd = new HighQualityRandom(seed);
    private OOSTargeting targeting;


    public static void main(String[] args) {
        seed = Integer.parseInt(args[0]);
//        runAoS();
//        runBPG();
    }

    private static void runAoS() {
        GameState root = new AoSGameState();
        MCTSConfig config = new MCTSConfig();
        Expander<MCTSInformationSet> expander = new AoSExpander<>(config);
        expander.getAlgorithmConfig().createInformationSetFor(root);


        OOSAlgorithm oos = new OOSAlgorithm(AoSGameInfo.FIRST_PLAYER, new OOSSimulator(expander), root, expander);
        ConvergenceExperiment.buildCompleteTree(oos.getRootNode());
//        SMJournalExperiments.buildCompleteTree(oos.getRootNode());
        Strategy strategy0 = StrategyCollector.getStrategyFor(oos.getRootNode(), root.getAllPlayers()[0], new MeanStratDist());

        System.out.println(strategy0);
        for (int i = 0; i < 100; i++) {
            oos.runIterations(1000);
            strategy0 = StrategyCollector.getStrategyFor(oos.getRootNode(), root.getAllPlayers()[0], new MeanStratDist());

            System.out.println(strategy0);
        }
        GambitEFG gambit = new GambitEFG();

        gambit.write("AoSDomain.gbt", root, expander);
    }

    private static void runBPG() {
        GameState root = new BPGGameState();
        MCTSConfig config = new MCTSConfig();
        Expander<MCTSInformationSet> expander = new BPGExpander<>(config);
        expander.getAlgorithmConfig().createInformationSetFor(root);


        OOSAlgorithm oos = new OOSAlgorithm(AoSGameInfo.FIRST_PLAYER, new OOSSimulator(expander), root, expander);
        ConvergenceExperiment.buildCompleteTree(oos.getRootNode());
        Strategy strategy0 = StrategyCollector.getStrategyFor(oos.getRootNode(), root.getAllPlayers()[0], new MeanStratDist());

        System.out.println(strategy0);
        for (int i = 0; i < 1000; i++) {
            oos.runIterations(2);
            strategy0 = StrategyCollector.getStrategyFor(oos.getRootNode(), root.getAllPlayers()[0], new MeanStratDist());

            System.out.println(strategy0);
        }
    }


    public OOSAlgorithm(Player searchingPlayer, OOSSimulator simulator, GameState rootState, Expander expander) {
        this(searchingPlayer, simulator, rootState, expander, 0.9, 0.6);
    }

    public OOSAlgorithm(Player searchingPlayer, OOSSimulator simulator, GameState rootState, Expander expander, double delta, double epsilon) {
        this.rnd = ((MCTSConfig) expander.getAlgorithmConfig()).getRandom();
        this.searchingPlayer = searchingPlayer;
        this.simulator = simulator;
        this.delta = delta;
        this.epsilon = epsilon;
        if (rootState.isPlayerToMoveNature()) {
            this.rootNode = new ChanceNodeImpl(expander, rootState, rnd);
            //InnerNode next = (InnerNode) rootNode.getChildFor((Action) (expander.getActions(rootState).get(0)));
            curIS = null;
        } else {
            this.rootNode = new InnerNodeImpl(expander, rootState);
            curIS = rootNode.getInformationSet();
        }
        OOSAlgorithmData.useEpsilonRM = false;
        threadBean = ManagementFactory.getThreadMXBean();
        String s = System.getProperty("DROPTREE");
        if (s != null) dropTree = Boolean.getBoolean(s);
        s = System.getProperty("INCTREEBUILD");
        if (s != null && !Boolean.parseBoolean(s)) SMConvergenceExperiment.buildCompleteTree(rootNode);
        s = System.getProperty("CURSTRAT");
        if (s != null) useCurrentStrategy = Boolean.getBoolean(s);
        s = System.getProperty("TARGTYPE");
        if (s != null) {
            if (s.equals("IST")) targeting = new ISTargeting(rootNode, delta);
            else if (s.equals("PST")) targeting = new PSTargeting(rootNode, delta);
        } else targeting = new ISTargeting(rootNode, delta);
    }

    public OOSAlgorithm(Player searchingPlayer, InnerNode rootNode,  double epsilon) {
        this.searchingPlayer = searchingPlayer;
        this.simulator = new OOSSimulator(rootNode.getExpander());
        this.delta = 0.;
        this.epsilon = epsilon;
        this.rootNode = rootNode;

        if(rootNode.getGameState().isPlayerToMoveNature()) {
            curIS = null;
        } else {
            curIS = rootNode.getInformationSet();
        }

        threadBean = ManagementFactory.getThreadMXBean();
        String s = System.getProperty("DROPTREE");
        if (s != null) dropTree = Boolean.getBoolean(s);
        s = System.getProperty("INCTREEBUILD");
        if (s != null && !Boolean.parseBoolean(s)) SMConvergenceExperiment.buildCompleteTree(rootNode);
        s = System.getProperty("CURSTRAT");
        if (s != null) useCurrentStrategy = Boolean.getBoolean(s);
        s = System.getProperty("TARGTYPE");
        if (s != null) {
            if (s.equals("IST")) targeting = new ISTargeting(rootNode, delta);
            else if (s.equals("PST")) targeting = new PSTargeting(rootNode, delta);
        } else targeting = new ISTargeting(rootNode, delta);
    }

    public OOSAlgorithm(Player searchingPlayer, GadgetChanceNode rootNode,  double epsilon) {
        this(searchingPlayer, (InnerNode) rootNode, epsilon);
        this.normalizingUtils = rootNode.getRootReachPr();
    }

    @Override
    public Action runMiliseconds(int miliseconds) {
        if (giveUp) return null;
        int iters = 0;
        int targISHits = 0;
        long start = threadBean.getCurrentThreadCpuTime();
        for (; (threadBean.getCurrentThreadCpuTime() - start) / 1e6 < miliseconds; ) {
            if (curIS != rootNode.getInformationSet()) biasedIteration = (rnd.nextDouble() <= delta);
            underTargetIS = (curIS == null || curIS == rootNode.getInformationSet());
            iteration(rootNode, 1, 1, 1, 1 / targeting.getSampleProbMultiplayer(), 1 / targeting.getSampleProbMultiplayer(), rootNode.getAllPlayers()[0]);//originally started by 1/10^d
            iters++;
            if (underTargetIS) targISHits++;
            underTargetIS = (curIS == null || curIS == rootNode.getInformationSet());
            iteration(rootNode, 1, 1, 1, 1 / targeting.getSampleProbMultiplayer(), 1 / targeting.getSampleProbMultiplayer(), rootNode.getAllPlayers()[1]);
            iters++;
            if (underTargetIS) targISHits++;
        }
//        System.out.println();
//        System.out.println("Multiplyer: " + targeting.getSampleProbMultiplayer());
//        System.out.println("OOS Iters: " + iters);
//        System.out.println("OOS Targeted IS Hits: " + targISHits);
//        System.out.println("Mean leaf depth: " + StrategyCollector.meanLeafDepth(rootNode));
//        System.out.println("CurIS size: " + (curIS==null ? "null" : curIS.getAllNodes().size()));
        if (curIS == null || !curIS.getPlayer().equals(searchingPlayer)) return null;
        if (curIS.getAlgorithmData() == null) return null;
        Map<Action, Double> distribution = (new MeanStratDist()).getDistributionFor(curIS.getAlgorithmData());
//        System.out.println("CurIS Mean Strategy: " + distribution.toString());
//        System.out.println("CurIS Cur Strategy: " + Arrays.toString(((OOSAlgorithmData)curIS.getAlgorithmData()).getRMStrategy()));
//        System.out.println("CurIS Actions: " + curIS.getAllNodes().iterator().next().getActions().toString());
        if (useCurrentStrategy)
            return curIS.getAllNodes().iterator().next().getActions().get(randomChoice(((OOSAlgorithmData) curIS.getAlgorithmData()).getRMStrategy(), 1));
        else return Strategy.selectAction(distribution, rnd);

    }

    public Action runIterations(int iterations) {
        double p0Value = 0;

        long starttime = System.currentTimeMillis();
        for (int i = 0; i < iterations / 2; i++) {
            if (curIS != rootNode.getInformationSet()) biasedIteration = (rnd.nextDouble() <= delta);
            underTargetIS = (curIS == null || curIS == rootNode.getInformationSet());
            p0Value = iteration(rootNode, 1, 1, 1, 1, 1, rootNode.getAllPlayers()[0]);
            underTargetIS = (curIS == null || curIS == rootNode.getInformationSet());
//            if (rootNode.getAllPlayers()[1].getId() == 1)
            iteration(rootNode, 1, 1, 1, 1, 1,rootNode.getAllPlayers()[1]);

//            if(i%1000000 == 0) System.err.println(i + "," + ((System.currentTimeMillis() - starttime) / 1000));
        }
        if (curIS == null || !curIS.getPlayer().equals(searchingPlayer)) return null;
        Map<Action, Double> distribution = (new MeanStratDist()).getDistributionFor(curIS.getAlgorithmData());

        return Strategy.selectAction(distribution, rnd);
    }


    private boolean biasedIteration = false;
    private boolean underTargetIS = false;
    //additional iteration return values
    private double x = -1;
    private double x_avg = -1;
    private double l = -1;

    /**
     * The main function for OOS iteration.
     *
     * @param node      current node
     * @param pi_avg    probability with which the searching player wants to reach the current node using his avg strategy
     * @param pi_       probability with which the opponent of the searching player and chance want to reach the current node
     * @param pi_c      probability with which chance wants to reach the current node
     * @param bs        probability that the current (possibly biased) sample reaches this node
     * @param us        probability that the unbiased sample reaches this node
     * @param expPlayer the exploring player for this iteration
     * @return iteration game reward is actually returned. Other return values are in global x and l
     */
    protected double iteration(Node node, double pi_avg, double pi_, double pi_c, double bs, double us, Player expPlayer) {
        //useful for debugging
//        ((NodeImpl)node).testSumS += 1/(delta*bs+(1-delta)*us);
//        ((NodeImpl)node).visits += 1;

        if (node instanceof LeafNode) {
            x = 1;
            x_avg = 1;
            l = delta * bs + (1 - delta) * us;
            return ((LeafNode) node).getUtilities()[expPlayer.getId()] * normalizingUtils;
        }
        if (node instanceof ChanceNode) {
            ChanceNode cn = (ChanceNode) node;
            Action a;
            double bp;
            double bsum = 0;
            if (!underTargetIS) {
                int i = 0;
                for (Action ai : cn.getActions()) {
                    if (targeting.isAllowedAction(cn, ai)) {
                        biasedProbs[i] = cn.getProbabilityOfNatureFor(ai);
                        bsum += biasedProbs[i];
                    } else {
                        biasedProbs[i] = 0;
                    }
                    i++;
                }
                //assert bsum>0;
            }

            int i;
            if (biasedIteration && bsum > 0) {
                i = randomChoice(biasedProbs, bsum);
                a = cn.getActions().get(i);
            } else {
                a = cn.getRandomAction();
                i = cn.getActions().indexOf(a);
            }
            if (bsum > 0) bp = biasedProbs[i] / bsum;
            else bp = cn.getProbabilityOfNatureFor(a);

            //if (rootNode.equals(cn.getGameState())) underTargetIS = true;
            final double realP = cn.getProbabilityOfNatureFor(a);
            double u = iteration(cn.getChildFor(a), pi_avg, pi_ * realP, pi_c * realP, bp * bs, realP * us, expPlayer);
            x *= realP;
            x_avg *= realP;
            return u;
        }
        InnerNode in = (InnerNode) node;

        MCTSInformationSet is = in.getInformationSet();
        OOSAlgorithmData data;
        Action selectedA;
        double u = 0;
        int ai = -1;
        double pai = -1;
        double p_avg_ai = -1;
//        boolean probe = in instanceof GadgetInnerNode;
        boolean probe = false;
        if (is.getAlgorithmData() == null) {//this is a new Information Set
            data = new OOSAlgorithmData(in.getActions());
            is.setAlgorithmData(data);
            ai = rnd.nextInt(in.getActions().size());
            pai = 1.0 / in.getActions().size();
            p_avg_ai = pai;
            selectedA = in.getActions().get(ai);
            Node child = in.getChildFor(selectedA);
            u = simulator.simulate(child.getGameState())[expPlayer.getId()];
            x = simulator.playersProb; //*(1.0/in.getActions().size()) will be added at the bottom;
            x_avg = simulator.playersProb; //*(1.0/in.getActions().size()) will be added at the bottom;
            l = (delta * bs + (1 - delta) * us) * simulator.playOutProb * (1.0 / in.getActions().size());
            assert probe == false;
        } else {
            data = (OOSAlgorithmData) is.getAlgorithmData();
            data.getRMStrategy(rmProbs);
            double biasedSum = 0;
            int inBiasActions = 0;
            biasedProbs = tmpProbs;
            if (bs > 0 && !underTargetIS) { //targeting may still make a difference
                if (curIS.equals(is)) {
                    underTargetIS = true;
                } else {
                    int i = 0;
                    for (Action a : in.getActions()) {
                        if (targeting.isAllowedAction(in, a)) {
                            biasedSum += rmProbs[i];
                            biasedProbs[i] = rmProbs[i];
                            inBiasActions++;
                        } else biasedProbs[i] = -0.0;//negative zeros denote the banned actions
                        i++;
                    }
                }
                assert probe == false;
            }
            if (biasedSum == 0) {//if all actions were not present for the opponnet or it was under the current IS
                biasedProbs = rmProbs;
                biasedSum = 1;
                inBiasActions = in.getActions().size();
            }

            if (is.getPlayer().equals(expPlayer)) {
                int nActions = in.getActions().size();
                if(probe) {
                    ai = 0; // follow action
                    pai = 1.0;
                    p_avg_ai = 1.0;
                    nActions = 1;
                    assert in.getActions().get(ai) instanceof GadgetInnerAction;
                    assert ((GadgetInnerAction) in.getActions().get(ai)).isFollow();
                } else {
                    if (!biasedIteration) {
                        if (rnd.nextDouble() < epsilon) ai = rnd.nextInt(in.getActions().size());
                        else ai = randomChoice(rmProbs, 1);
                    } else {
                        if (rnd.nextDouble() < epsilon) {
                            if (inBiasActions == in.getActions().size()) ai = rnd.nextInt(inBiasActions);
                            else {
                                int j = rnd.nextInt(inBiasActions);
                                ai = 0;//the following sets ai to the j-th allowed action
                                while (Double.compare(biasedProbs[ai], 0.0) == -1 || j-- > 0) ai++;
                            }
                        } else ai = randomChoice(biasedProbs, biasedSum);
                    }

                    pai = rmProbs[ai];
                    p_avg_ai = data.getMeanStrategy()[ai];
                }

//                double uspai = rmProbs[ai];


                u = iteration(in.getChildFor(in.getActions().get(ai)),
                        pi_avg * p_avg_ai,
                        pi_, pi_c,
                        //the following is zero for banned actions and the correct probability for allowed
                        bs * ((Double.compare(biasedProbs[ai], 0.0) == -1 ? 0 : (1 - epsilon) * biasedProbs[ai] / biasedSum + (epsilon / inBiasActions))),
                        us * ((1 - epsilon) * pai + (epsilon / nActions)), expPlayer);
            } else {
                if(probe) {
                    ai = 0; // follow action
                    pai = 1.0;
                    p_avg_ai = 1.0;
                    assert in.getActions().get(ai) instanceof GadgetInnerAction;
                    assert ((GadgetInnerAction) in.getActions().get(ai)).isFollow();
                } else {
                    if (biasedIteration) ai = randomChoice(biasedProbs, biasedSum);
                    else ai = randomChoice(rmProbs, 1);
                    pai = rmProbs[ai];
                    p_avg_ai = data.getMeanStrategy()[ai];
                }

//                double uspai = rmProbs[ai];

                u = iteration(in.getChildFor(in.getActions().get(ai)),
                        pi_avg, pi_ * pai, pi_c, bs * biasedProbs[ai] / biasedSum, us * pai, expPlayer);
            }
        }

        //regret/mean strategy update
        double s = delta * bs + (1 - delta) * us;
        double c = x;
        x *= pai;
        double c_avg = x_avg;
        x_avg *= p_avg_ai;

        if (is.getPlayer().equals(expPlayer)) {
            if(probe) {
                assert ai == 0;
                pai = rmProbs[ai];
                p_avg_ai = data.getMeanStrategy()[ai];
                GadgetLeafNode leaf = ((GadgetLeafNode) in.getChildFor(in.getActions().get(1)));
                double u_t = leaf.getUtilities()[in.getPlayerToMove().getId()];

                data.updateRegret(u, pi_, l, c, u_t, pai);
            } else {
                data.updateRegret(ai, u, pi_avg, pi_, pi_c, l, c, x, c_avg, x_avg);
            }
            // RP? pre aktualne h to je (pi * pi_c)
        } else {
            data.getRMStrategy(rmProbs);
            data.updateMeanStrategy(rmProbs, pi_ / s);
        }
        return u;
    }


    private double[] rmProbs = new double[1000];
    private double[] tmpProbs = new double[1000];
    private double[] biasedProbs = tmpProbs;

    private int randomChoice(double[] dArray, double sum) {
        double r = rnd.nextDouble() * sum;
        for (int i = 0; i < dArray.length; i++) {
            if (r <= dArray[i]) return i;
            r -= dArray[i];
        }
        return -1;
    }


    private void clearTreeISs() {
        ArrayDeque<InnerNode> q = new ArrayDeque();
        q.add(rootNode);
        while (!q.isEmpty()) {
            InnerNode curNode = q.removeFirst();
            //curNode.setAlgorithmData(null);
            OOSAlgorithmData data = (OOSAlgorithmData) curNode.getAlgorithmData();
            if (data != null) data.clear();
            for (Node n : curNode.getChildren().values()) {
                if ((n instanceof InnerNode)) q.addLast((InnerNode) n);
            }
        }
    }

    private boolean giveUp = false;

    @Override
    public void setCurrentIS(InformationSet is) {
        curIS = (MCTSInformationSet) is;
        if (curIS.getAllNodes().isEmpty()) {
            giveUp = true;
            clearTreeISs();
            return;
        }
        targeting.update(is);
        if (dropTree) clearTreeISs();
    }

    private Player getOpponent() {
        return rootNode.getAllPlayers()[1 - searchingPlayer.getId()];
    }

    private Player getNaturePlayer() {
        return rootNode.getAllPlayers()[2];
    }


    public InnerNode getRootNode() {
        return rootNode;
    }

    @Override
    public Action runMiliseconds(int miliseconds, GameState gameState) {
        assert false;
        return null;
    }

    public void setRnd(Random rnd) {
        this.rnd = rnd;
    }
}
