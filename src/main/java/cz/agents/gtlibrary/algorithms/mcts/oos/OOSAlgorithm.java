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
import cz.agents.gtlibrary.algorithms.cr.gadgettree.GadgetChanceNode;
import cz.agents.gtlibrary.algorithms.cr.gadgettree.GadgetInfoSet;
import cz.agents.gtlibrary.algorithms.cr.gadgettree.GadgetInnerNode;
import cz.agents.gtlibrary.algorithms.mcts.MCTSConfig;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.distribution.MeanStratDist;
import cz.agents.gtlibrary.algorithms.mcts.experiments.SMConvergenceExperiment;
import cz.agents.gtlibrary.algorithms.mcts.nodes.ChanceNodeImpl;
import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNodeImpl;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.ChanceNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.LeafNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.Node;
import cz.agents.gtlibrary.iinodes.PublicStateImpl;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.strategy.Strategy;
import cz.agents.gtlibrary.utils.HighQualityRandom;
import cz.agents.gtlibrary.utils.Pair;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.Random;

import static cz.agents.gtlibrary.algorithms.cr.CRAlgorithm.Budget.BUDGET_NUM_SAMPLES;
import static cz.agents.gtlibrary.algorithms.cr.CRAlgorithm.Budget.BUDGET_TIME;
import static cz.agents.gtlibrary.algorithms.cr.CRAlgorithm.isNiceGame;
import static cz.agents.gtlibrary.algorithms.cr.CRExperiments.buildCompleteTree;


/**
 * # variable naming:
 * X_Y_Z
 * <p>
 * ## X is strategy
 * rm_: regret matching strategy
 * avg_: average strategy
 * bs_: biased sampling strategy
 * us_: unbiased sampling strategy
 * s_: sampling strategy
 * <p>
 * ## Y is node
 * h - current history
 * z - leaf node
 * zh - from current history to the leaf, i.e. z|h
 * zha - from current history and playing action a with 100% prob to the leaf, i.e. z|h.a
 * ha - at the current history playing action a
 * <p>
 * ## Z is player
 * pl - current player
 * opp - opponent
 * cn - chance
 * all - all the players
 *
 * @author vilo
 * @author Michal Sustr
 */
public class OOSAlgorithm implements GamePlayingAlgorithm {
    final public static int AVG_STRATEGY_UNIFORM = 0;
    final public static int AVG_STRATEGY_LINEAR = 1;
    final public static int AVG_STRATEGY_SQUARE = 2;
    final public static int AVG_STRATEGY_XLOGX = 3;
    public static int seed = 49;
    public static double gadgetDelta = 0.;
    public static double gadgetEpsilon = 0.;
    public boolean saveEVTime = false;
    public boolean saveEVWeightedPl = false;
    public boolean saveEVWeightedAll = false;
    public CRAlgorithm.Budget budgetRoot = BUDGET_NUM_SAMPLES;
    public CRAlgorithm.Budget budgetGadget = BUDGET_NUM_SAMPLES;
    public int MAX_ACTIONS = 100; // maximum number of actions that can be taken in any infoset
    public boolean useRegretMatchingPlus = false;
    public int avgStrategyComputation = AVG_STRATEGY_UNIFORM;
    protected Player searchingPlayer;
    protected OOSSimulator simulator;
    protected InnerNode rootNode;
    protected ThreadMXBean threadBean;
    protected double u_z;
    private Random rnd = new HighQualityRandom(seed);
    // biasing of the algorithm
    // delta == 0 is de-facto MCCFR
    private double delta = 0.0;
    // exploration of the algorithm
    private double epsilon = 0.001;
    private boolean dropTree = false;
    private boolean useRMStrategy = false;
    private OOSTargeting targeting;
    private MCTSInformationSet curIS;
    private MCTSInformationSet trackingIS;
    private boolean giveUp = false;
    private Double normalizingUtils = 1.;
    private int numSamplesDuringRun;
    private int numSamplesInCurrentIS;
    private int numSamplesInCurrentPS;
    private int numNodesTouchedDuringRun;
    private MCTSConfig config;
    private double[] currentISprobDist;
    private double actionChosenWithProb = 1.;
    private boolean isBiasedIteration = false; // should current iteration make a biased sample? (with prob. delta)
    private boolean isBelowTargetIS = false;   // are we deeper in the tree, "below" target IS? If yes, we dont need to bias samples anymore, any sampling strategy is fine
    private double[] rmProbs = new double[MAX_ACTIONS];
    private double[] rmProbsCopy = new double[MAX_ACTIONS];
    private double[] tmpProbs = new double[MAX_ACTIONS]; // array of actual biased probabilities, but biasedProbs can be assigned RM probs
    private double[] biasedProbs = tmpProbs;
    // additional iteration return values
    private double rm_zh_all = -1;
    private double s_z_all = -1;
    private int numBiasApplicableActions = 0;


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
        if (s != null) useRMStrategy = Boolean.getBoolean(s);
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
        if (s != null) useRMStrategy = Boolean.getBoolean(s);
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
        if (s != null) useRMStrategy = Boolean.getBoolean(s);
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

    public static void main(String[] args) {
        seed = Integer.parseInt(args[0]);
//        runAoS();
//        runBPG();
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
            if (curIS != rootNode.getInformationSet()) isBiasedIteration = (rnd.nextDouble() <= delta);
            isBelowTargetIS = (curIS == null || curIS.equals(rootNode.getInformationSet()));
            iteration(rootNode, 1, 1, 1, 1 / targeting.getSampleProbMultiplayer(),
                    1 / targeting.getSampleProbMultiplayer(),
                    rootNode.getAllPlayers()[0]);//originally started by 1/10^d
            numSamplesDuringRun++;
            if (isBelowTargetIS) targISHits++;
            isBelowTargetIS = (curIS == null || curIS.equals(rootNode.getInformationSet()));
            iteration(rootNode, 1, 1, 1, 1 / targeting.getSampleProbMultiplayer(),
                    1 / targeting.getSampleProbMultiplayer(), rootNode.getAllPlayers()[1]);
            numSamplesDuringRun++;
            if (isBelowTargetIS) targISHits++;
        }
        if (curIS == null || !curIS.getPlayer().equals(searchingPlayer)) return null;
        if (curIS.getAlgorithmData() == null) return null;
        Map<Action, Double> distribution = (new MeanStratDist()).getDistributionFor(curIS.getAlgorithmData());

        Action a;
        if (useRMStrategy) {
            double[] strat = ((OOSAlgorithmData) curIS.getAlgorithmData()).getRMStrategy();
            int ai = randomChoice(strat, 1);
            a = curIS.getAllNodes().iterator().next().getActions().get(ai);
            actionChosenWithProb = strat[ai];
            currentISprobDist = strat;
        } else {
            a = Strategy.selectAction(distribution, rnd);
            actionChosenWithProb = distribution.get(a);
            currentISprobDist = distribution.values().stream().mapToDouble(i -> i).toArray();
        }

        return a;
    }

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
            if (curIS != rootNode.getInformationSet()) isBiasedIteration = (rnd.nextDouble() <= delta);
            isBelowTargetIS = (curIS == null || curIS.equals(rootNode.getInformationSet()));
            p0Value = iteration(rootNode, 1, 1, 1, 1, 1, rootNode.getAllPlayers()[0]);
            numSamplesDuringRun++;
            isBelowTargetIS = (curIS == null || curIS.equals(rootNode.getInformationSet()));
            iteration(rootNode, 1, 1, 1, 1, 1, rootNode.getAllPlayers()[1]);
            numSamplesDuringRun++;
        }
        if (curIS == null || !curIS.getPlayer().equals(searchingPlayer)) return null;
        Map<Action, Double> distribution = (new MeanStratDist()).getDistributionFor(curIS.getAlgorithmData());

        Action a;
        if (useRMStrategy) {
            double[] strat = ((OOSAlgorithmData) curIS.getAlgorithmData()).getRMStrategy();
            int ai = randomChoice(strat, 1);
            a = curIS.getAllNodes().iterator().next().getActions().get(ai);
            actionChosenWithProb = strat[ai];
            currentISprobDist = strat;
        } else {
            if (distribution == null) a = null;
            else {
                a = Strategy.selectAction(distribution, rnd);
                actionChosenWithProb = distribution.get(a);
                currentISprobDist = distribution.values().stream().mapToDouble(i -> i).toArray();
            }
        }

        return a;
    }

    /**
     * The main function for OOS iteration.
     * <p>
     * Utilities are always for the current exploring player.
     *
     * @param n         current node
     * @param rm_h_pl   reach prob of the searching player                  to the current node using RM strategy
     * @param rm_h_opp  reach prob of the opponent of the searching player  to the current node using RM strategy
     * @param rm_h_cn   reach prob of chance                                to the current node using RM strategy
     * @param bs_h_all  reach prob of all players to the current node using biased sampling strategy
     * @param us_h_all  reach prob of all players to the current node using unbiased sampling strategy
     * @param expPlayer the exploring player for this iteration
     * @return expected baseline-augmented utility of current node for the exploring player
     */
    protected double iteration(Node n,
                               double rm_h_pl, double rm_h_opp, double rm_h_cn,
                               double bs_h_all, double us_h_all,
                               Player expPlayer) {
        // Useful for debugging changes of the algorithm
//         System.err.println(numNodesTouchedDuringRun + ";" + n + ";" + rm_h_pl + ";" + rm_h_opp + ";" + rm_h_cn + ";" + bs_h_all + ";" + us_h_all + ";" + expPlayer + ";" + rnd.nextDouble());

        numNodesTouchedDuringRun++;
        // Three possibilities for node:
        // 1) leaf node
        // 2) chance node
        // 3) inner node (that is not a chance node), i.e. where players play

        if (n instanceof LeafNode) {
            rm_zh_all = 1;
            s_z_all = delta * bs_h_all + (1 - delta) * us_h_all;
            u_z = ((LeafNode) n).getUtilities()[expPlayer.getId()] * normalizingUtils;
            return u_z; // == u_h
        }

        InnerNode in = (InnerNode) n;
        if (in.getActions().size() == 1) {
            // avoid using random number generator and computing stats when player is deterministic
            // this means that all stats related to this node (like exp. value) are delegated to the child node!
            return iteration(in.getChildFor(in.getActions().get(0)), rm_h_pl, rm_h_opp, rm_h_cn, bs_h_all, us_h_all,
                    expPlayer);
        }

        double s_h_all = delta * bs_h_all + (1 - delta) * us_h_all;
        double bs_ha_all, us_ha_all;

        if (n instanceof ChanceNode) {
            ChanceNode cn = (ChanceNode) n;
            Pair<Action, Double> chanceOutcome = selectChanceAction(cn);
            Action a = chanceOutcome.getLeft();
            bs_ha_all = chanceOutcome.getRight();

            double p_chance = cn.getProbabilityOfNatureFor(a);
            us_ha_all = p_chance;

            double u_ha = iteration(cn.getChildFor(a),
                    rm_h_pl, rm_h_opp, rm_h_cn * p_chance,
                    bs_h_all * bs_ha_all,
                    us_h_all * us_ha_all,
                    expPlayer);

            rm_zh_all *= p_chance;

            // compute baseline-augmented utilities
            double s_ha_all = delta * bs_ha_all + (1 - delta) * us_ha_all;
            double u_h = ((u_ha - cn.getBaselineFor(a, expPlayer)) * p_chance) / s_ha_all;
            for (Action i : cn.getActions()) u_h += cn.getProbabilityOfNatureFor(i) * cn.getBaselineFor(i, expPlayer);

            if (!(n instanceof GadgetChanceNode)) updateHistoryExpectedValue(expPlayer, cn,
                    u_h, // todo: undo effect of normalizing utils!!!
                    rm_h_pl, rm_h_opp, rm_h_cn, s_h_all);

            return u_h;
        }

        MCTSInformationSet is = in.getInformationSet();
        OOSAlgorithmData data = (OOSAlgorithmData) is.getAlgorithmData();

        // outcomes of this case which will be used later:
        double u_h;       // baseline-augmented estimate of expected utility for current history
        double u_ha;      // baseline-augmented estimate of expected utility for next history
        double u_x;       // baseline-augmented estimate of expected utility for next history, if we go there with 100% probability from current history
        int ai;           // action index
        double rm_ha_all; // probability of taking this action (according to RM)
        double s_ha_all;  // probability of sampling this action

        // some stats
        if (is.equals(trackingIS)) numSamplesInCurrentIS++;
        if (trackingIS != null && is.getPublicState() != null &&
                is.getPublicState().equals(trackingIS.getPublicState())) numSamplesInCurrentPS++;

        if (data == null) { // this is a new Information Set
            data = new OOSAlgorithmData(in.getActions(), config.useEpsilonRM);
            is.setAlgorithmData(data);

            ai = rnd.nextInt(in.getActions().size());
            rm_ha_all = 1.0 / in.getActions().size();
            Action a = in.getActions().get(ai);
            Node child = in.getChildFor(a);

            u_z = simulator.simulate(child, expPlayer);
            rm_zh_all = simulator.playersProb; // "* pai" will be added at the bottom
            s_z_all = (delta * bs_h_all + (1 - delta) * us_h_all) * simulator.playOutProb * rm_ha_all;
            s_ha_all = rm_ha_all;

            // compute replacement for baseline-augmented utilities
            // todo: check!
            u_ha = u_z / normalizingUtils;
            u_x = u_ha;
            u_h = u_x;
        } else {
            data.getRMStrategy(rmProbs);

            double bsum = 0;
            Action a;
            numBiasApplicableActions = 0;
            biasedProbs = tmpProbs;

            if (delta > 0 && bs_h_all > 0 && !isBelowTargetIS) { // targeting may still make a difference
                if (curIS.equals(is)) isBelowTargetIS = true;
                else bsum = updateBiasing(in);
            }

            if (bsum == 0) { // if all actions were not present for the opponent or it was below the current IS
                biasedProbs = rmProbs;
                bsum = 1;
                numBiasApplicableActions = in.getActions().size();
            }

            u_h = 0.;

            if (is.getPlayer().equals(expPlayer)) { // exploring move
                Pair<Integer, Double> playerOutcome = selectExploringPlayerAction(is, bsum);
                ai = playerOutcome.getLeft();
                us_ha_all = playerOutcome.getRight();
                rm_ha_all = rmProbs[ai];
                a = in.getActions().get(ai);

                // the following is zero for banned actions and the correct probability for allowed
                bs_ha_all = 0.;
                if (biasedProbs[ai] > 0.0) {
                    bs_ha_all = (1 - epsilon) * biasedProbs[ai] / bsum + (epsilon / numBiasApplicableActions);
                }

                // precompute baseline components now, because after child iteration RM probs will change
                for (Action i : in.getActions()) {
                    if (i.equals(a)) continue;
                    u_h += rmProbs[in.getActions().indexOf(i)] * in.getBaselineFor(i, expPlayer);
                }

                u_ha = iteration(in.getChildFor(a), rm_h_pl * rm_ha_all, rm_h_opp, rm_h_cn, bs_h_all * bs_ha_all,
                        us_h_all * us_ha_all, expPlayer);
            } else {
                Pair<Integer, Double> playerOutcome = selectNonExploringPlayerAction(is, bsum);
                ai = playerOutcome.getLeft();
                us_ha_all = playerOutcome.getRight();
                bs_ha_all = biasedProbs[ai] / bsum;
                rm_ha_all = rmProbs[ai];
                a = in.getActions().get(ai);

                // precompute baseline components now, because after child iteration RM probs will change
                for (Action i : in.getActions()) {
                    if (i.equals(a)) continue;
                    u_h += rmProbs[in.getActions().indexOf(i)] * in.getBaselineFor(i, expPlayer);
                }

                u_ha = iteration(in.getChildFor(a), rm_h_pl, rm_h_opp * rm_ha_all, rm_h_cn,
                        bs_h_all * bs_ha_all,
                        us_h_all * us_ha_all,
                        expPlayer);
            }

            // finish computing baseline-augmented utilities
            s_ha_all = delta * bs_ha_all + (1 - delta) * us_ha_all;
            u_x = ((u_ha - in.getBaselineFor(a, expPlayer))) / s_ha_all + in.getBaselineFor(a, expPlayer);
            u_h += u_x * rm_ha_all;
        }

        // regret/mean strategy update
        double rm_zha_all = rm_zh_all;
        rm_zh_all *= rm_ha_all;

        // history expected value
        if (!(is instanceof GadgetInfoSet)) updateHistoryExpectedValue(expPlayer, in,
                u_h, // todo: undo effect of normalizing utils!!!
                rm_h_pl, rm_h_opp, rm_h_cn, s_h_all);

        updateInfosetRegrets(in, expPlayer, data, ai, rm_ha_all, u_z, u_x, u_h, rm_h_cn, rm_h_opp, rm_zha_all, s_h_all);

        return u_h;
    }

    protected void updateInfosetRegrets(InnerNode in, Player expPlayer, OOSAlgorithmData data,
                                        int ai, double pai,
                                        double u_z, double u_x, double u_h,
                                        double rm_h_cn, double rm_h_opp, double rm_zha_all, double s_h_all) {
        if (in.getPlayerToMove().equals(expPlayer)) {
            // todo: baseline-aug. utilities for gadget!
            if (in instanceof GadgetInnerNode) { // gadget update
                // check if we even should do updates (it's only action may be follow)
                GadgetInnerNode one_gn = (GadgetInnerNode) in;
                if (one_gn.getTerminateNode() == null) return;

                double u_terminate = one_gn.getTerminateNode().getUtilities()[expPlayer.getId()] * normalizingUtils;
                double u_follow = u_z * rm_h_cn * rm_zha_all / s_z_all;
                data.updateRegret(pai, u_terminate, u_follow);
            } else { // regular regret update
                if (useRegretMatchingPlus) data.updateRegretPlus(ai, u_x, u_h, rm_h_opp * rm_h_cn / s_h_all, in);
                else data.updateRegret(ai, u_x, u_h, rm_h_opp * rm_h_cn / s_h_all, in);
            }
        } else {
            // we use stochastically weighted averaging
            data.getRMStrategy(rmProbs);

            double w;
            switch (avgStrategyComputation) {
                case AVG_STRATEGY_UNIFORM:
                    w = 1;
                    break;
                case AVG_STRATEGY_LINEAR:
                    w = numSamplesDuringRun + 1;
                    break;
                case AVG_STRATEGY_SQUARE:
                    w = (numSamplesDuringRun + 1) * (numSamplesDuringRun + 1);
                    break;
                case AVG_STRATEGY_XLOGX:
                    w = (numSamplesDuringRun + 1) * Math.log10(numSamplesDuringRun + 1);
                    break;
                default:
                    throw new RuntimeException("unrecognized avg strategy computation");
            }
            data.updateMeanStrategy(rmProbs, w * rm_h_opp * rm_h_cn / s_h_all);
        }
    }

    protected void updateHistoryExpectedValue(Player expPlayer, InnerNode updateNode, double u_h,
                                              double rm_h_pl, double rm_h_opp, double rm_h_cn, double s_h_all) {

        // let's make sure that the utility is always for player 0
        // updateVal we get is for the exploring player
        u_h *= expPlayer.getId() == 0 ? 1 : -1;

        if (saveEVTime || GadgetInnerNode.resolvingCFV == GadgetInnerNode.RESOLVE_TIME) {
            updateNode.updateEVTime(u_h);
        }
        if (saveEVWeightedPl || GadgetInnerNode.resolvingCFV == GadgetInnerNode.RESOLVE_WEIGHTED_PL) {
            boolean infosetIsExploringPlayer = updateNode.getPlayerToMove().equals(expPlayer);
            double rm_h_owner = infosetIsExploringPlayer ? rm_h_pl : rm_h_opp;
            updateNode.updateEVWeightedPl(rm_h_owner * u_h);
            updateNode.updateSumReachPl(rm_h_owner / s_h_all);
        }
        if (saveEVWeightedAll || GadgetInnerNode.resolvingCFV == GadgetInnerNode.RESOLVE_WEIGHTED_ALL) {
            double rp = rm_h_pl * rm_h_opp * rm_h_cn;
            updateNode.updateEVWeightedAll(rp * u_h);
            updateNode.updateSumReachAll(rp / s_h_all);
        }
    }

    private double updateBiasing(InnerNode in) {
        double bsum = 0.;

        numBiasApplicableActions = 0;
        int i = 0;
        for (Action a : in.getActions()) {
            if (targeting.isAllowedAction(in, a)) {
                biasedProbs[i] = rmProbs[i];
                bsum += biasedProbs[i];
                numBiasApplicableActions++;
            } else biasedProbs[i] = -0.0; //negative zeros denote the banned actions
            i++;
        }
        return bsum;
    }

    private double updateBiasing(ChanceNode cn) {
        double bsum = 0.;

        numBiasApplicableActions = 0;
        int i = 0;
        for (Action a : cn.getActions()) {
            if (targeting.isAllowedAction(cn, a)) {
                biasedProbs[i] = cn.getProbabilityOfNatureFor(a);
                bsum += biasedProbs[i];
                numBiasApplicableActions++;
            } else biasedProbs[i] = -0.0; //negative zeros denote the banned actions
            i++;
        }

        return bsum;
    }

    private Pair<Action, Double> selectChanceAction(ChanceNode cn) {
        // avoid using random number generator when chance is deterministic
        if (cn.getActions().size() == 1) {
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
        if (delta == 0.) {
            Action a = cn.getRandomAction();
            return new Pair<>(a, cn.getProbabilityOfNatureFor(a));
        }

        // now finally general chance biasing
        Action a;
        double biasedProb;

        double bsum = 0;
        if (!isBelowTargetIS) bsum = updateBiasing(cn);

        int i;
        if (isBiasedIteration && bsum > 0) {
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
        double us_ha_all;

        if (is instanceof GadgetInfoSet) {
            ai = 0; // always force follow!
            us_ha_all = 1.;
        } else {
            int nActions = is.getActions().size();

            if (!isBiasedIteration) {
                if (rnd.nextDouble() < epsilon) ai = rnd.nextInt(nActions); // epsilon exploration
                else ai = randomChoice(rmProbs, 1);                    // no exploration
            } else {
                if (rnd.nextDouble() < epsilon) { // epsilon exploration
                    if (numBiasApplicableActions == nActions) ai = rnd.nextInt(numBiasApplicableActions);
                    else {
                        int j = rnd.nextInt(numBiasApplicableActions);
                        ai = 0;// the following sets ai to the j-th allowed action
                        while (Double.compare(biasedProbs[ai], 0.0) == -1 || j-- > 0) ai++;
                    }
                } else ai = randomChoice(biasedProbs, bsum); // no exploration
            }

            double pai = rmProbs[ai];
            us_ha_all = ((1 - epsilon) * pai + (epsilon / nActions));
        }

        return new Pair<>(ai, us_ha_all);
    }

    private Pair<Integer, Double> selectNonExploringPlayerAction(MCTSInformationSet is, double bsum) {
        int ai;
        double us_ha_all;

        if (is instanceof GadgetInfoSet) {
            ai = 0; // always force follow!

            // do not include RM probs of follow to unbiased sampling -
            // we want to keep them consistent with trunk
            us_ha_all = 1.; // rmProbs[ai];
        } else {
            if (isBiasedIteration) ai = randomChoice(biasedProbs, bsum);
            else ai = randomChoice(rmProbs, 1);
            us_ha_all = rmProbs[ai];
        }

        return new Pair<>(ai, us_ha_all);
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

    public InnerNode solveEntireGame(Game targetG,
                                     Player resolvingPlayer,
                                     int iterationsInRoot,
                                     int iterationsPerGadgetGame) {
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
        Game[] gameAtDepth = new Game[maxDepth + 1];
        config.getAllPublicStates().stream()
                .filter(ps -> ps.getPlayer().equals(resolvingPlayer))
                .forEach(ps -> {
                    int d = ps.getDepth();
                    if (gameAtDepth[d] != null) return;
                    System.err.println("Building at depth " + d);
                    gameAtDepth[d] = targetG.clone();
                    gameAtDepth[d].config.useEpsilonRM = targetG.config.useEpsilonRM;
                    buildCompleteTree(gameAtDepth[d].getRootNode());
                });
        System.err.println("Resolving in " + config.getAllPublicStates().stream()
                .filter(ps -> ps.getPlayer().equals(resolvingPlayer))
                .filter(ps -> ps.getAllInformationSets().iterator().next().getActions().size() > 1)
                .count() + " public states");


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
        if (maybePlayerRootPs.getPlayer().getId() == resolvingPlayer.getId()) {
            q.add(maybePlayerRootPs); // it really is player's root ps
        } else {
            q.addAll(maybePlayerRootPs.getNextPlayerPublicStates(resolvingPlayer));
        }

        int numPsVisited = 0;
        while (!q.isEmpty()) {
            numPsVisited++;
            PublicState s = q.removeLast();
            System.err.println("Resolving " + s);
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
                if (maxNumActionsAtPs == 1) {
                    skipResolving = true;
                }
            }
            if (!skipResolving) {
                if (budgetGadget == BUDGET_TIME) {
                    runMiliseconds(iterationsPerGadgetGame);
                } else {
                    assert budgetGadget == BUDGET_NUM_SAMPLES;
                    runIterations(iterationsPerGadgetGame);
                }
            }

            // update original g strategy after resolving this public state
            s.getAllInformationSets().forEach(is -> {
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
                .stream().filter(ps -> ps.getPlayer().equals(resolvingPlayer))
                .count();
        return targetG.getRootNode();
    }

    protected void copyGame(Game target, Game source) {
        // assumes fully built game trees!
        source.config.getAllInformationSets().forEach((isKey, x) -> {
            MCTSInformationSet sIS = source.config.getAllInformationSets().get(isKey);
            MCTSInformationSet tIS = target.config.getAllInformationSets().get(isKey);

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
