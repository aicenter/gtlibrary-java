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

import cz.agents.gtlibrary.algorithms.cr.CRAlgorithm;
import cz.agents.gtlibrary.algorithms.cr.Game;
import cz.agents.gtlibrary.algorithms.cr.gadgettree.*;
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
import cz.agents.gtlibrary.domain.aceofspades.AoSExpander;
import cz.agents.gtlibrary.domain.aceofspades.AoSGameInfo;
import cz.agents.gtlibrary.domain.aceofspades.AoSGameState;
import cz.agents.gtlibrary.domain.bpg.BPGExpander;
import cz.agents.gtlibrary.domain.bpg.BPGGameState;
import cz.agents.gtlibrary.iinodes.PublicStateImpl;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.strategy.Strategy;
import cz.agents.gtlibrary.utils.HighQualityRandom;
import cz.agents.gtlibrary.utils.io.GambitEFG;
import org.apache.commons.lang3.SerializationUtils;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.*;
import java.util.stream.Collectors;

import static cz.agents.gtlibrary.algorithms.cr.CRAlgorithm.Budget.BUDGET_NUM_SAMPLES;
import static cz.agents.gtlibrary.algorithms.cr.CRAlgorithm.Budget.BUDGET_TIME;
import static cz.agents.gtlibrary.algorithms.cr.CRAlgorithm.isNiceGame;
import static cz.agents.gtlibrary.algorithms.cr.CRExperiments.buildCompleteTree;


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
    public boolean useCurrentStrategy = false;

    private MCTSInformationSet curIS;
    public static int seed = 49;
    private Double normalizingUtils = 1.;
    private Random rnd = new HighQualityRandom(seed);
    private OOSTargeting targeting;

    private int numSamplesDuringRun;
    private int numSamplesInCurrentIS;
    private int numSamplesInCurrentPS;
    private int numNodesTouchedDuringRun;
    private MCTSConfig config;
    private double[] currentISprobDist;
    private MCTSInformationSet trackingIS;
    public static double gadgetDelta = 0.;
    public static double gadgetEpsilon = 0.;
    public boolean resolveTime = false;
    public boolean resolveWeighted = false;

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

    public OOSAlgorithm(Player searchingPlayer,
                        OOSSimulator simulator,
                        GameState rootState,
                        Expander expander,
                        double delta,
                        double epsilon) {
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
        this.config = (MCTSConfig) expander.getAlgorithmConfig();
//        config.useEpsilonRM = false;
        threadBean = ManagementFactory.getThreadMXBean();
        String s = System.getenv("DROPTREE");
        if (s != null) dropTree = Boolean.getBoolean(s);
        s = System.getenv("INCTREEBUILD");
        if (s != null && !Boolean.parseBoolean(s)) SMConvergenceExperiment.buildCompleteTree(rootNode);
        s = System.getenv("CURSTRAT");
        if (s != null) useCurrentStrategy = Boolean.getBoolean(s);
        s = System.getenv("TARGTYPE");
        if (s != null) {
            if (s.equals("IST")) targeting = new ISTargeting(rootNode, delta);
            else if (s.equals("PST")) targeting = new PSTargeting(rootNode, delta);
        } else targeting = new ISTargeting(rootNode, delta);
    }

    public OOSAlgorithm(Player searchingPlayer, InnerNode rootNode, double epsilon) {
        this.searchingPlayer = searchingPlayer;
        this.simulator = new OOSSimulator(rootNode.getExpander());
        this.delta = 0.;
        this.epsilon = epsilon;
        this.rootNode = rootNode;

        if (rootNode.getGameState().isPlayerToMoveNature()) {
            curIS = null;
        } else {
            curIS = rootNode.getInformationSet();
        }

        this.config = (MCTSConfig) rootNode.getExpander().getAlgorithmConfig();
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

    public OOSAlgorithm(Player searchingPlayer, InnerNode rootNode, double epsilon, double delta) {
        this.searchingPlayer = searchingPlayer;
        this.simulator = new OOSSimulator(rootNode.getExpander());
        this.delta = delta;
        this.epsilon = epsilon;
        this.rootNode = rootNode;

        if (rootNode.getGameState().isPlayerToMoveNature()) {
            curIS = null;
        } else {
            curIS = rootNode.getInformationSet();
        }

        this.config = (MCTSConfig) rootNode.getExpander().getAlgorithmConfig();
        threadBean = ManagementFactory.getThreadMXBean();
        String s = System.getenv("DROPTREE");
        if (s != null) dropTree = Boolean.getBoolean(s);
        s = System.getenv("INCTREEBUILD");
        if (s != null && !Boolean.parseBoolean(s)) SMConvergenceExperiment.buildCompleteTree(rootNode);
        s = System.getenv("CURSTRAT");
        if (s != null) useCurrentStrategy = Boolean.getBoolean(s);
        s = System.getenv("TARGTYPE");
        if (s != null) {
            if (s.equals("IST")) targeting = new ISTargeting(rootNode, delta);
            else if (s.equals("PST")) targeting = new PSTargeting(rootNode, delta);
        } else targeting = new ISTargeting(rootNode, delta);
    }

    public OOSAlgorithm(Player searchingPlayer, GadgetChanceNode rootNode, double epsilon) {
        this(searchingPlayer, (InnerNode) rootNode, epsilon);
        this.normalizingUtils = rootNode.getRootReachPr();
    }

    @Override
    public Action runMiliseconds(int miliseconds) {
        if (giveUp) return null;
        numSamplesDuringRun = 0;
        numSamplesInCurrentIS = 0;
        numSamplesInCurrentPS = 0;
        numNodesTouchedDuringRun = 0;
        int targISHits = 0;
        long start = threadBean.getCurrentThreadCpuTime();
        for (; (threadBean.getCurrentThreadCpuTime() - start) / 1e6 < miliseconds; ) {
            if (curIS != rootNode.getInformationSet()) biasedIteration = (rnd.nextDouble() <= delta);
            underTargetIS = (curIS == null || curIS.equals(rootNode.getInformationSet()));
            iteration(rootNode, 1, 1, 1, 1, 1 / targeting.getSampleProbMultiplayer(), 1 / targeting.getSampleProbMultiplayer(), rootNode.getAllPlayers()[0]);//originally started by 1/10^d
            numSamplesDuringRun++;
            if (underTargetIS) targISHits++;
            underTargetIS = (curIS == null || curIS.equals(rootNode.getInformationSet()));
            iteration(rootNode, 1, 1, 1, 1,1 / targeting.getSampleProbMultiplayer(), 1 / targeting.getSampleProbMultiplayer(), rootNode.getAllPlayers()[1]);
            numSamplesDuringRun++;
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

        Action a;
        if (useCurrentStrategy) {
            double[] strat = ((OOSAlgorithmData) curIS.getAlgorithmData()).getRMStrategy();
            int ai = randomChoice(strat, 1);
            a = curIS.getAllNodes().iterator().next().getActions().get(ai);
            actionChosenWithProb = strat[ai];
            currentISprobDist = strat;
        } else {
            a = Strategy.selectAction(distribution, rnd);
            actionChosenWithProb = distribution.get(a);
            currentISprobDist = distribution.values().stream().mapToDouble(i->i).toArray();
        }

        return a;
    }

    private double actionChosenWithProb = 1.;
    @Override
    public Double actionChosenWithProb() {
        return actionChosenWithProb;
    }

    @Override
    public Action runIterations(int iterations) {
        if (giveUp) return null;
        double p0Value = 0;

        long starttime = System.currentTimeMillis();
        numSamplesDuringRun = 0;
        numSamplesInCurrentIS = 0;
        numSamplesInCurrentPS = 0;
        numNodesTouchedDuringRun = 0;

        for (int i = 0; i < iterations / 2; i++) {
            if (curIS != rootNode.getInformationSet()) biasedIteration = (rnd.nextDouble() <= delta);
            underTargetIS = (curIS == null || curIS.equals(rootNode.getInformationSet()));
            p0Value = iteration(rootNode, 1, 1, 1,1, 1, 1, rootNode.getAllPlayers()[0]);
            numSamplesDuringRun++;
            underTargetIS = (curIS == null || curIS.equals(rootNode.getInformationSet()));
            iteration(rootNode, 1, 1, 1, 1,1, 1,rootNode.getAllPlayers()[1]);
            numSamplesDuringRun++;
        }
        if (curIS == null || !curIS.getPlayer().equals(searchingPlayer)) return null;
        Map<Action, Double> distribution = (new MeanStratDist()).getDistributionFor(curIS.getAlgorithmData());

        Action a;
        if (useCurrentStrategy) {
            double[] strat = ((OOSAlgorithmData) curIS.getAlgorithmData()).getRMStrategy();
            int ai = randomChoice(strat, 1);
            a = curIS.getAllNodes().iterator().next().getActions().get(ai);
            actionChosenWithProb = strat[ai];
            currentISprobDist = strat;
        } else {
            if(distribution == null) a = null;
            else {
                a = Strategy.selectAction(distribution, rnd);
                actionChosenWithProb = distribution.get(a);
                currentISprobDist = distribution.values().stream().mapToDouble(i -> i).toArray();
            }
        }

        return a;
    }


    private boolean biasedIteration = false;
    private boolean underTargetIS = false;
    //additional iteration return values
    private double x = -1;
    private double l = -1;

    /**
     * The main function for OOS iteration.
     *
     * @param node      current node
     * @param pi        probability with which the searching player wants to reach the current node
     * @param pi_       probability with which the opponent of the searching player and chance want to reach the current node
     * @param pi_c      probability with which chance wants to reach the current node
     * @param rp        reach probability of all players with which they want to reach the current node using RM strategy
     * @param bs        probability that the current (possibly biased) sample reaches this node
     * @param us        probability that the unbiased sample reaches this node
     * @param expPlayer the exploring player for this iteration
     * @return iteration game reward is actually returned. Other return values are in global x and l
     */
    protected double iteration(Node node, double pi, double pi_, double pi_c, double rp, double bs, double us, Player expPlayer) {
        //useful for debugging
//        ((NodeImpl)node).testSumS += 1/(delta*bs+(1-delta)*us);
//        ((NodeImpl)node).visits += 1;
        numNodesTouchedDuringRun++;

        if (node instanceof LeafNode) {
            x = 1;
            l = delta * bs + (1 - delta) * us;
            double u = ((LeafNode) node).getUtilities()[expPlayer.getId()] * normalizingUtils;
            return u;
        }
        if (node instanceof ChanceNode) {
            ChanceNode cn = (ChanceNode) node;
            Action a;
            double bp = 1.;

            if(cn.getActions().size() > 1) {
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

                // gadget biasing
                if (node instanceof GadgetChanceNode && gadgetDelta > 0 && trackingIS != null) {
                    GadgetChanceNode gcn = (GadgetChanceNode) node;
                    bsum = gcn.getBiasedProbs(biasedProbs, trackingIS, gadgetEpsilon, gadgetDelta);
                    i = randomChoice(biasedProbs, bsum);
                    a = cn.getActions().get(i);
                    bp = biasedProbs[i];
                }
            } else {
                a = cn.getActions().get(0);
            }

            //if (rootNode.equals(cn.getGameState())) underTargetIS = true;
            final double realP = cn.getProbabilityOfNatureFor(a);
            double u = iteration(cn.getChildFor(a), pi, pi_ * realP, pi_c * realP, rp*realP, bp * bs, realP * us, expPlayer);
            x *= realP;
            return u;
        }

        InnerNode in = (InnerNode) node;
        MCTSInformationSet is = in.getInformationSet();
        OOSAlgorithmData data;
        Action selectedA;
        double u = 0;
        int ai = -1;
        double pai = -1;
        if(is.equals(trackingIS)) numSamplesInCurrentIS++;
        if(trackingIS != null && is.getPublicState() != null &&
            is.getPublicState().equals(trackingIS.getPublicState())) numSamplesInCurrentPS++;

        if (is.getAlgorithmData() == null) {//this is a new Information Set
            data = new OOSAlgorithmData(in.getActions(), config.useEpsilonRM);
            is.setAlgorithmData(data);
            ai = rnd.nextInt(in.getActions().size());
            pai = 1.0 / in.getActions().size();
            selectedA = in.getActions().get(ai);
            Node child = in.getChildFor(selectedA);
            u = simulator.simulate(child, expPlayer);
            x = simulator.playersProb; //*(1.0/in.getActions().size()) will be added at the bottom;
            l = (delta * bs + (1 - delta) * us) * simulator.playOutProb * (1.0 / in.getActions().size());
        } else {
            data = (OOSAlgorithmData) is.getAlgorithmData();
            data.getRMStrategy(rmProbs);
            double biasedSum = 0;
            int inBiasActions = 0;
            double nextUs;
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
            }
            if (biasedSum == 0) {//if all actions were not present for the opponnet or it was under the current IS
                biasedProbs = rmProbs;
                biasedSum = 1;
                inBiasActions = in.getActions().size();
            }

            if (is.getPlayer().equals(expPlayer)) {
                if(in.getActions().size() > 1) {
                    if (is instanceof GadgetInfoSet) {
                        ai = 0; // always force follow!
                        pai = rmProbs[ai];
                        nextUs = us;
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
                        nextUs = us * ((1 - epsilon) * pai + (epsilon / in.getActions().size()));
                    }
                } else {
                    ai = 0;
                    pai = 1;
                    nextUs = us;
                    inBiasActions = 1;
                    biasedSum = 1;
                    biasedProbs[0] = 1.;
                }

                u = iteration(in.getChildFor(in.getActions().get(ai)),
                        pi * pai,
                        pi_, pi_c,
                        //the following is zero for banned actions and the correct probability for allowed
                        rp * pai,
                        bs * ((Double.compare(biasedProbs[ai], 0.0) == -1 ? 0 : (1 - epsilon) * biasedProbs[ai] / biasedSum + (epsilon / inBiasActions))),
                        nextUs, expPlayer);
            } else {
                if(in.getActions().size() > 1) {
                    if (is instanceof GadgetInfoSet) {
                        ai = 0; // always force follow!
                        pai = rmProbs[ai];
                        nextUs = us; // * rmProbs[ai];
                    } else {
                        if (biasedIteration) ai = randomChoice(biasedProbs, biasedSum);
                        else ai = randomChoice(rmProbs, 1);
                        pai = rmProbs[ai];
                        nextUs = us * pai;
                    }
                } else {
                    ai = 0;
                    pai = 1;
                    nextUs = us;
                    biasedSum = 1;
                    biasedProbs[0] = 1.;
                }

                u = iteration(in.getChildFor(in.getActions().get(ai)),
                        pi, pi_ * pai, pi_c, rp*pai, bs * biasedProbs[ai] / biasedSum, nextUs, expPlayer);
            }
        }

        //regret/mean strategy update
        double s = delta * bs + (1 - delta) * us;
        double c = x;
        x *= pai;

        // history expected value
        double updateVal = (u * x) / (l * normalizingUtils);
        double reachp;
        if(!(is instanceof GadgetInfoSet)) {
            if (!is.getPlayer().equals(expPlayer)) {
                reachp = pi_ / pi_c;
                if(resolveTime || GadgetInnerNode.resolvingCFV == GadgetInnerNode.RESOLVE_TIME) ((InnerNode) node).updateExpectedValue2(updateVal);
                if(resolveWeighted || GadgetInnerNode.resolvingCFV == GadgetInnerNode.RESOLVE_WEIGHTED) {
                    ((InnerNode) node).updateExpectedValue(reachp * updateVal);
                    ((InnerNode) node).updateSumReachp(reachp / s);
                }
            } else {
                reachp = pi;
                if(resolveTime || GadgetInnerNode.resolvingCFV == GadgetInnerNode.RESOLVE_TIME) ((InnerNode) node).updateExpectedValue2(updateVal * -1);
                if(resolveWeighted || GadgetInnerNode.resolvingCFV == GadgetInnerNode.RESOLVE_WEIGHTED) {
                    ((InnerNode) node).updateExpectedValue(-updateVal * reachp);
                    ((InnerNode) node).updateSumReachp(reachp / s);
                }
            }
        }

        if (is.getPlayer().equals(expPlayer)) {
            if(is instanceof GadgetInfoSet) { // update regret for both follow/terminate
                GadgetInnerNode one_gn = (GadgetInnerNode) is.getAllNodes().iterator().next();
                if(one_gn.getTerminateNode() != null) {
                    double u_t = one_gn.getTerminateNode().getUtilities()[expPlayer.getId()] * normalizingUtils;
                    double u_f = u * pi_c * c / l;
                    data.updateRegret(pai, u_t, u_f);
                }
            } else { // regular regret update
                data.updateRegret(ai, u, pi_, l, c, x);
            }
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
        trackingIS = (MCTSInformationSet) is;
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


    public CRAlgorithm.Budget budgetRoot = BUDGET_NUM_SAMPLES;
    public CRAlgorithm.Budget budgetGadget = BUDGET_NUM_SAMPLES;

    public InnerNode solveEntireGame(Game targetG, Player resolvingPlayer, int iterationsInRoot, int iterationsPerGadgetGame) {
        System.err.println("Using " +
                "iterationsInRoot=" + iterationsInRoot + " " +
                "iterationsPerGadgetGame=" + iterationsPerGadgetGame + " " +
                "epsilonExploration=" + epsilon + " " +
                "deltaTargeting=" + delta + " " +
                "targeting=" + targeting.toString() + " " +
                "player=" + resolvingPlayer.getId() + " ");

        // to able to calc best response, we need to have the whole tree built
        System.err.println("Building result tree");
        buildCompleteTree(targetG.getRootNode());

        // game for storing preplay iterations
        Game rootGame = targetG.clone();
        rootGame.config.useEpsilonRM = targetG.config.useEpsilonRM;
        config = rootGame.config;
        rootNode = rootGame.getRootNode();
        buildCompleteTree(rootGame.getRootNode());

        // build games for each depth of player's public states
        int maxDepth = config.getAllPublicStates().stream().map(PublicStateImpl::getDepth).max(Integer::compare).get();
        System.err.println("Building iterations trees");
        Game[] gameAtDepth = new Game[maxDepth+1];
        config.getAllPublicStates().stream()
                .filter(ps -> ps.getPlayer().equals(resolvingPlayer))
                .forEach(ps -> {
                    int d = ps.getDepth();
                    if(gameAtDepth[d] != null) return;
                    System.err.println("Building at depth "+d);
                    gameAtDepth[d] = targetG.clone();
                    gameAtDepth[d].config.useEpsilonRM = targetG.config.useEpsilonRM;
                    buildCompleteTree(gameAtDepth[d].getRootNode());
                });
        System.err.println("Resolving in "+config.getAllPublicStates().stream()
                .filter(ps -> ps.getPlayer().equals(resolvingPlayer))
                .filter(ps -> ps.getAllInformationSets().iterator().next().getActions().size() > 1)
                .count()+" public states");


        // build temp game where we will store the current resolving
        Game tempGame = targetG.clone();
        tempGame.config.useEpsilonRM = targetG.config.useEpsilonRM;
        buildCompleteTree(tempGame.getRootNode());

        if (iterationsInRoot < 2) { // no root init
            throw new RuntimeException("Cannot skip root initialization!");
        }

        System.err.println("Root iterations");
        if (budgetRoot == BUDGET_TIME) {
            runMiliseconds(iterationsInRoot);
        } else {
            assert budgetRoot == BUDGET_NUM_SAMPLES;
            runIterations(iterationsInRoot);
        }

        ArrayDeque<PublicState> q = new ArrayDeque<>();

        PublicState maybePlayerRootPs = rootNode.getPublicState();
        if(maybePlayerRootPs.getPlayer().getId() == resolvingPlayer.getId()) {
            q.add(maybePlayerRootPs); // it really is player's root ps
        } else {
            q.addAll(maybePlayerRootPs.getNextPlayerPublicStates(resolvingPlayer));
        }

        int numPsVisited= 0;
        while (!q.isEmpty()) {
            numPsVisited++;
            PublicState s = q.removeLast();
            System.err.println("Resolving "+s);
            Game sourceG;
            if (s.getPlayerParentPublicState() != null) {
                int parentDepth = s.getPlayerParentPublicState().getDepth();
                sourceG = gameAtDepth[parentDepth];
            } else {
                sourceG = rootGame;
            }
            copyGame(tempGame, sourceG);
            config = tempGame.config;
            rootNode = tempGame.getRootNode();
            rnd = tempGame.rnd;

            if (!s.isReachable(resolvingPlayer)) {
                // If public state is not reachable by our player, we can leave whatever strategy was there.
                System.err.println("Skipping resolving public state " + s + " - not reachable.");
                continue;
            }

            q.addAll(s.getNextPlayerPublicStates(resolvingPlayer));
//             don't resolve in chance public states
//            if (s.getAllNodes().iterator().next() instanceof ChanceNode) continue;

            setCurrentIS(s.getAllInformationSets().iterator().next()); // pick one IS

            boolean skipResolving = false;
            if (isNiceGame(curIS.getAllStates().iterator().next())) {
                int maxNumActionsAtPs = s.getAllInformationSets().stream()
                        .map(is -> is.getActions().size())
                        .max(Integer::compareTo).get();
                if(maxNumActionsAtPs == 1) {
                    skipResolving = true;
                }
            }
            if(!skipResolving) {
                if (budgetGadget == BUDGET_TIME) {
                    runMiliseconds(iterationsPerGadgetGame);
                } else {
                    assert budgetGadget == BUDGET_NUM_SAMPLES;
                    runIterations(iterationsPerGadgetGame);
                }

                System.out.println(">>>"+seed+";"+s.getPSKey().getId()+";"+numSamplesDuringRun+";"+numSamplesInCurrentIS+";"+numSamplesInCurrentPS+";"+numNodesTouchedDuringRun);
            } else {
                System.err.println("Skipping "+s);
            }

            // update original g strategy after resolving this public state
            s.getAllInformationSets().forEach(is-> {
                MCTSInformationSet sIS = tempGame.config.getAllInformationSets().get(is.getISKey());
                MCTSInformationSet tIS = targetG.config.getAllInformationSets().get(is.getISKey());

                OOSAlgorithmData sData = (OOSAlgorithmData) sIS.getAlgorithmData();
                OOSAlgorithmData tData = (OOSAlgorithmData) tIS.getAlgorithmData();
                tData.setFrom(sData);
            });

            // update strategy at this depth with resolved temp game
            copyGame(gameAtDepth[s.getDepth()], tempGame);
        }
        assert numPsVisited == targetG.config.getAllPublicStates()
                .stream().filter(ps->ps.getPlayer().equals(resolvingPlayer))
                .count();
        return targetG.getRootNode();
    }

    protected void copyGame(Game t, Game s) {
        // target, source
        // assumes fully built game trees!
        s.config.getAllInformationSets().forEach((isKey, x) -> {
            MCTSInformationSet sIS = s.config.getAllInformationSets().get(isKey);
            MCTSInformationSet tIS = t.config.getAllInformationSets().get(isKey);

            OOSAlgorithmData sData = (OOSAlgorithmData) sIS.getAlgorithmData();
            OOSAlgorithmData tData = (OOSAlgorithmData) tIS.getAlgorithmData();
            tData.setFrom(sData);
        });
    }

    public void setTargeting(String kind) {
        if (kind.equals("IST")) targeting = new ISTargeting(rootNode, delta);
        else if (kind.equals("PST")) targeting = new PSTargeting(rootNode, delta);
    }

    public int numSamplesDuringRun() {
        return numSamplesDuringRun;
    }

    public int numSamplesInCurrentIS() {
        return numSamplesInCurrentIS;
    }
    public int numSamplesInCurrentPS() {
        return numSamplesInCurrentPS;
    }
    public int numNodesTouchedDuringRun() {
        return numNodesTouchedDuringRun;
    }

    @Override
    public double[] currentISprobDist() {
        return currentISprobDist;
    }

    public void setTrackingIS(MCTSInformationSet trackingIS) {
        this.trackingIS = trackingIS;
    }
}
