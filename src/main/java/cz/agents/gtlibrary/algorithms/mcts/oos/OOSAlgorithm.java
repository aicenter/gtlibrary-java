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
import cz.agents.gtlibrary.algorithms.mcts.MCTSConfig;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.distribution.MeanStratDist;
import cz.agents.gtlibrary.algorithms.mcts.experiments.SMConvergenceExperiment;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.ChanceNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.LeafNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.Node;
import cz.agents.gtlibrary.algorithms.mcts.nodes.*;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.BackPropFactory;
import cz.agents.gtlibrary.iinodes.PublicStateImpl;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.strategy.Strategy;
import cz.agents.gtlibrary.utils.HighQualityRandom;
import cz.agents.gtlibrary.utils.Pair;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.*;

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

    // biasing of the algorithm
    // delta == 0 is de-facto MCCFR
    private double delta = 0.0;
    public static double gadgetDelta = 0.;

    // exploration of the algorithm
    private double epsilon = 0.001;
    public static double gadgetEpsilon = 0.;


    private boolean dropTree = false;
    private boolean useCurrentStrategy = false;

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


    public boolean saveEVTime = false;
    public boolean saveEVWeighted = false;

    public static void main(String[] args) {
        seed = Integer.parseInt(args[0]);
//        runAoS();
//        runBPG();
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
            isBelowTargetIS = (curIS == null || curIS.equals(rootNode.getInformationSet()));
            iteration(rootNode, 1, 1, 1, 1 / targeting.getSampleProbMultiplayer(), 1 / targeting.getSampleProbMultiplayer(), rootNode.getAllPlayers()[0]);//originally started by 1/10^d
            numSamplesDuringRun++;
            if (isBelowTargetIS) targISHits++;
            isBelowTargetIS = (curIS == null || curIS.equals(rootNode.getInformationSet()));
            iteration(rootNode, 1, 1, 1, 1 / targeting.getSampleProbMultiplayer(), 1 / targeting.getSampleProbMultiplayer(), rootNode.getAllPlayers()[1]);
            numSamplesDuringRun++;
            if (isBelowTargetIS) targISHits++;
        }
        if (curIS == null || !curIS.getPlayer().equals(searchingPlayer)) return null;
        if (curIS.getAlgorithmData() == null) return null;
        Map<Action, Double> distribution = (new MeanStratDist()).getDistributionFor(curIS.getAlgorithmData());

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
            isBelowTargetIS = (curIS == null || curIS.equals(rootNode.getInformationSet()));
            p0Value = iteration(rootNode, 1, 1,1, 1, 1, rootNode.getAllPlayers()[0]);
            numSamplesDuringRun++;
            isBelowTargetIS = (curIS == null || curIS.equals(rootNode.getInformationSet()));
            iteration(rootNode, 1, 1,  1,1, 1,rootNode.getAllPlayers()[1]);
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
    private boolean isBelowTargetIS = false; // are we deeper in the tree, "below" target IS?
                                             // If yes, we dont need to bias samples anymore, original strategy is fine

    //additional iteration return values
    private double rp_zh = -1; // probability of going to currently sampled leaf "z" from current history "h",
                               // i.e. z|h (using the RM strategy)
    private double rp_z = -1;  // probability of sampling leaf (by using the epsilon-on-policy sampling strategy)

    private double[] rmProbs = new double[1000];
    private double[] tmpProbs = new double[1000];
    private double[] biasedProbs = tmpProbs;

    /**
     * The main function for OOS iteration.
     *
     * @param n         current node
     * @param pi        reach prob of the searching player                            to the current node using RM strategy
     * @param pi_opp_c  reach prob of the opponent of the searching player and chance to the current node using RM strategy
     * @param pi_c      reach prob of chance                                          to the current node using RM strategy
     * @param bs        reach prob of all players                                     to the current node using biased sampling strategy
     * @param us        reach prob of all players                                     to the current node using unbiased sampling strategy
     * @param expPlayer the exploring player for this iteration
     * @return iteration game reward is actually returned. Other return values are in global rp_zh and rp_z
     */
    protected double iteration(Node n, double pi, double pi_opp_c, double pi_c, double bs, double us, Player expPlayer) {
        numNodesTouchedDuringRun++;
        // Three possibilities for node:
        // 1) leaf node
        // 2) chance node
        // 3) inner node (that is not a chance node), i.e. where players play

        if (n instanceof LeafNode) {
            rp_zh = 1;
            rp_z = delta * bs + (1 - delta) * us;
            return ((LeafNode) n).getUtilities()[expPlayer.getId()] * normalizingUtils;
        }

        if (n instanceof ChanceNode) {
            ChanceNode cn = (ChanceNode) n;
            Pair<Action, Double> chanceOutcome = selectChanceAction(cn);
            Action a = chanceOutcome.getLeft();
            Double biased_prob = chanceOutcome.getRight();

            double p_chance = cn.getProbabilityOfNatureFor(a);
            double u = iteration(cn.getChildFor(a), pi, pi_opp_c * p_chance, pi_c * p_chance, biased_prob * bs, p_chance * us, expPlayer);
            rp_zh *= p_chance;
            return u;
        }

        InnerNode in = (InnerNode) n;
        MCTSInformationSet is = in.getInformationSet();
        OOSAlgorithmData data = (OOSAlgorithmData) is.getAlgorithmData();

        // outcomes of this iteration:
        double u = 0;    // sampled utility
        int ai = -1;     // action index
        double pai = -1; // probability of taking this action (according to RM)

        // some stats
        if(is.equals(trackingIS)) numSamplesInCurrentIS++;
        if(trackingIS != null && is.getPublicState() != null &&
            is.getPublicState().equals(trackingIS.getPublicState())) numSamplesInCurrentPS++;

        if (data == null) { // this is a new Information Set
            data = new OOSAlgorithmData(in.getActions(), config.useEpsilonRM);
            is.setAlgorithmData(data);

            ai = rnd.nextInt(in.getActions().size());
            pai = 1.0 / in.getActions().size();
            Action a = in.getActions().get(ai);
            Node child = in.getChildFor(a);

            u = simulator.simulate(child, expPlayer);
            rp_zh = simulator.playersProb; // *(1.0/in.getActions().size()) will be added at the bottom;
            rp_z = (delta * bs + (1 - delta) * us) * simulator.playOutProb * pai;

        } else if(in.getActions().size() == 1) {
            // avoid using random number generator and computing stats when player is deterministic
            return iteration(in.getChildFor(in.getActions().get(0)), pi, pi_opp_c, pi_c, bs, us, expPlayer);

        } else {
            data.getRMStrategy(rmProbs);

            double bsum = 0;
            double nextUs;
            inBiasActions = 0;
            biasedProbs = tmpProbs;

            if (delta > 0 && bs > 0 && !isBelowTargetIS) { // targeting may still make a difference
                if (curIS.equals(is)) isBelowTargetIS = true;
                else bsum = updateBiasing(in);
            }

            if (bsum == 0) { // if all actions were not present for the opponent or it was below the current IS
                biasedProbs = rmProbs;
                bsum = 1;
                inBiasActions = in.getActions().size();
            }

            if (is.getPlayer().equals(expPlayer)) {
                Pair<Integer, Double> playerOutcome = selectExploringPlayerAction(is, bsum);
                ai = playerOutcome.getLeft();
                nextUs = playerOutcome.getRight();
                pai = rmProbs[ai];
                Action a = in.getActions().get(ai);

                // the following is zero for banned actions and the correct probability for allowed
                double nextBs = 0.;
                if(biasedProbs[ai] > 0.0) {
                    nextBs = (1 - epsilon) * biasedProbs[ai] / bsum + (epsilon / inBiasActions);
                }

                u = iteration(in.getChildFor(a), pi * pai, pi_opp_c, pi_c, bs * nextBs, us * nextUs, expPlayer);
            } else {
                Pair<Integer, Double> playerOutcome = selectNonExploringPlayerAction(is, bsum);
                ai = playerOutcome.getLeft();
                nextUs = playerOutcome.getRight();

                pai = rmProbs[ai];
                Action a = in.getActions().get(ai);
                u = iteration(in.getChildFor(a), pi, pi_opp_c * pai, pi_c, bs * biasedProbs[ai] / bsum, us * nextUs, expPlayer);
            }
        }

        // regret/mean strategy update
        double s = delta * bs + (1 - delta) * us;
        double c = rp_zh;
        rp_zh *= pai;

        // history expected value
        if(!(is instanceof GadgetInfoSet)) {
            double reachp;
            double updateVal = (u * rp_zh) / (rp_z * normalizingUtils);
            double sign;

            if (!is.getPlayer().equals(expPlayer)) {
                reachp = pi_opp_c / pi_c;
                sign = 1;
            } else {
                reachp = pi;
                sign = -1;
            }

            if(saveEVTime || GadgetInnerNode.resolvingCFV == GadgetInnerNode.RESOLVE_TIME) in.updateEVTime(updateVal * sign);
            if(saveEVWeighted || GadgetInnerNode.resolvingCFV == GadgetInnerNode.RESOLVE_WEIGHTED) {
                in.updateEVWeighted(reachp * sign * updateVal);
                in.updateSumReachp(reachp / s);
            }
        }

        // update regrets
        if (is.getPlayer().equals(expPlayer)) {
            if(is instanceof GadgetInfoSet) { // update regret for both follow/terminate
                GadgetInnerNode one_gn = (GadgetInnerNode) is.getAllNodes().iterator().next();
                if(one_gn.getTerminateNode() != null) {
                    double u_t = one_gn.getTerminateNode().getUtilities()[expPlayer.getId()] * normalizingUtils;
                    double u_f = u * pi_c * c / rp_z;
                    data.updateRegret(pai, u_t, u_f);
                }
            } else { // regular regret update
                data.updateRegret(ai, u, pi_opp_c, rp_z, c, rp_zh);
            }
        } else {
            data.getRMStrategy(rmProbs);
            data.updateMeanStrategy(rmProbs, pi_opp_c / s);
        }

        return u;
    }

    private int inBiasActions = 0;

    private double updateBiasing(InnerNode in) {
        double bsum = 0.;

        inBiasActions = 0;
        int i = 0;
        for (Action a : in.getActions()) {
            if (targeting.isAllowedAction(in, a)) {
                biasedProbs[i] = rmProbs[i];
                bsum += biasedProbs[i];
                inBiasActions++;
            } else biasedProbs[i] = -0.0; //negative zeros denote the banned actions
            i++;
        }
        return bsum;
    }

    private double updateBiasing(ChanceNode cn) {
        double bsum = 0.;

        inBiasActions = 0;
        int i = 0;
        for (Action a : cn.getActions()) {
            if (targeting.isAllowedAction(cn, a)) {
                biasedProbs[i] = cn.getProbabilityOfNatureFor(a);
                bsum += biasedProbs[i];
                inBiasActions++;
            } else biasedProbs[i] = -0.0; //negative zeros denote the banned actions
            i++;
        }

        return bsum;
    }
    private Pair<Action, Double> selectChanceAction(ChanceNode cn) {
        // avoid using random number generator when chance is deterministic
        if(cn.getActions().size() == 1) {
            return new Pair<>(cn.getActions().get(0), 1.0);
        }

        // gadget biasing -- gadget chance node is always on the top of the tree, so no "underTargetIs" is applicable
        if (cn instanceof GadgetChanceNode && gadgetDelta > 0 && trackingIS != null) {
            GadgetChanceNode gcn = (GadgetChanceNode) cn;
            double bsum = gcn.getBiasedProbs(biasedProbs, trackingIS, gadgetEpsilon, gadgetDelta);
            int i = randomChoice(biasedProbs, bsum);
            return new Pair<>(cn.getActions().get(i), biasedProbs[i]);
        }

        // don't do any biasing, prevent unnecessary calculations
        if(delta == 0.) {
            Action a = cn.getRandomAction();
            return new Pair<>(a, cn.getProbabilityOfNatureFor(a));
        }

        // now finally general chance biasing
        Action a;
        double biasedProb;

        double bsum = 0;
        if (!isBelowTargetIS) bsum = updateBiasing(cn);

        int i;
        if (biasedIteration && bsum > 0) {
            i = randomChoice(biasedProbs, bsum);
            a = cn.getActions().get(i);
        } else {
            a = cn.getRandomAction();
            i = cn.getActions().indexOf(a);
        }

        if (bsum > 0) biasedProb = biasedProbs[i] / bsum;
        else biasedProb = cn.getProbabilityOfNatureFor(a);

        return new Pair<>(a, biasedProb);
    }
    private Pair<Integer, Double> selectExploringPlayerAction(MCTSInformationSet is, double bsum) {
        int ai;
        double nextUs;

        if (is instanceof GadgetInfoSet) {
            ai = 0; // always force follow!
            nextUs = 1.;
        } else {
            int nActions = is.getActions().size();

            if (!biasedIteration) {
                if (rnd.nextDouble() < epsilon) ai = rnd.nextInt(nActions); // epsilon exploration
                else ai = randomChoice(rmProbs, 1);                    // no exploration
            } else {
                if (rnd.nextDouble() < epsilon) { // epsilon exploration
                    if (inBiasActions == nActions) ai = rnd.nextInt(inBiasActions);
                    else {
                        int j = rnd.nextInt(inBiasActions);
                        ai = 0;// the following sets ai to the j-th allowed action
                        while (Double.compare(biasedProbs[ai], 0.0) == -1 || j-- > 0) ai++;
                    }
                } else ai = randomChoice(biasedProbs, bsum); // no exploration
            }

            double pai = rmProbs[ai];
            nextUs = ((1 - epsilon) * pai + (epsilon / nActions));
        }

        return new Pair<>(ai, nextUs);
    }

    private Pair<Integer, Double> selectNonExploringPlayerAction(MCTSInformationSet is, double bsum) {
        int ai;
        double nextUs;

        if (is instanceof GadgetInfoSet) {
            ai = 0; // always force follow!

            // do not include RM probs of follow to unbiased sampling -
            // we want to keep them consistent with trunk
            nextUs = 1.; // rmProbs[ai];
        } else {
            if (biasedIteration) ai = randomChoice(biasedProbs, bsum);
            else ai = randomChoice(rmProbs, 1);
            nextUs = rmProbs[ai];
        }

        return new Pair<>(ai, nextUs);
    }


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

//                System.out.println(">>>"+seed+";"+s.getPSKey().getId()+";"+numSamplesDuringRun+";"+numSamplesInCurrentIS+";"+numSamplesInCurrentPS+";"+numNodesTouchedDuringRun);
            } else {
//                System.err.println("Skipping "+s);
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
