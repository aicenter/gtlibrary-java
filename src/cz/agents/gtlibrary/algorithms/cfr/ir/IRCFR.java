package cz.agents.gtlibrary.algorithms.cfr.ir;

import cz.agents.gtlibrary.algorithms.cfr.ir.testdomains.alosscounter.ALossCounterExpander;
import cz.agents.gtlibrary.algorithms.cfr.ir.testdomains.alosscounter.ALossCounterGameState;
import cz.agents.gtlibrary.algorithms.cfr.ir.testdomains.alossgame.ALossExpander;
import cz.agents.gtlibrary.algorithms.cfr.ir.testdomains.alossgame.ALossGameState;
import cz.agents.gtlibrary.algorithms.mcts.distribution.Distribution;
import cz.agents.gtlibrary.algorithms.mcts.distribution.MeanStratDist;
import cz.agents.gtlibrary.algorithms.mcts.oos.OOSAlgorithmData;
import cz.agents.gtlibrary.domain.imperfectrecall.brtest.BRTestExpander;
import cz.agents.gtlibrary.domain.imperfectrecall.brtest.BRTestGameState;
import cz.agents.gtlibrary.domain.ir.memoryloss.MLExpander;
import cz.agents.gtlibrary.domain.ir.memoryloss.MLGameState;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.io.GambitEFG;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.*;

public class IRCFR {
    public static void main(String[] args) {
//        runBRTest();
        runALoss();
//        runALossCounter();
    }

    private static void runML() {
        GameState rootState = new MLGameState();
        Expander<IRCFRInformationSet> cfrExpander1 = new MLExpander<>(new IRCFRConfig());
        IRCFR cfr = new IRCFR(rootState.getAllPlayers()[0], rootState, cfrExpander1);

        cfr.runMiliseconds(10000);
    }

    private static void runBRTest() {
        GameState rootState = new BRTestGameState();
        Expander<IRCFRInformationSet> cfrExpander1 = new BRTestExpander<>(new IRCFRConfig());
        IRCFR cfr = new IRCFR(rootState.getAllPlayers()[0], rootState, cfrExpander1);

        cfr.runMiliseconds(10000);
        new GambitEFG().write("BRTest.gbt", rootState, cfrExpander1);
    }

    private static void runALoss() {
        GameState rootState = new ALossGameState();
        Expander<IRCFRInformationSet> cfrExpander1 = new ALossExpander<>(new IRCFRConfig());
        IRCFR cfr = new IRCFR(rootState.getAllPlayers()[0], rootState, cfrExpander1);

        cfr.runMiliseconds(20000);

        Map<Action, Double> p1Strategy = getStrategyFor(rootState, rootState.getAllPlayers()[0], new MeanStratDist(), cfrExpander1.getAlgorithmConfig().getAllInformationSets(), cfrExpander1);
        Map<Action, Double> p2Strategy = getStrategyFor(rootState, rootState.getAllPlayers()[1], new MeanStratDist(), cfrExpander1.getAlgorithmConfig().getAllInformationSets(), cfrExpander1);
        p1Strategy.entrySet().forEach(System.out::println);
        p2Strategy.entrySet().forEach(System.out::println);
        new GambitEFG().write("ALoss.gbt", rootState, cfrExpander1);
    }

    private static void runALossCounter() {
        GameState rootState = new ALossCounterGameState();
        Expander<IRCFRInformationSet> cfrExpander1 = new ALossCounterExpander<>(new IRCFRConfig());
        IRCFR cfr = new IRCFR(rootState.getAllPlayers()[0], rootState, cfrExpander1);

        cfr.runMiliseconds(2000);

        Map<Action, Double> p1Strategy = getStrategyFor(rootState, rootState.getAllPlayers()[0], new MeanStratDist(), cfrExpander1.getAlgorithmConfig().getAllInformationSets(), cfrExpander1);
        Map<Action, Double> p2Strategy = getStrategyFor(rootState, rootState.getAllPlayers()[1], new MeanStratDist(), cfrExpander1.getAlgorithmConfig().getAllInformationSets(), cfrExpander1);
        p1Strategy.entrySet().forEach(System.out::println);
        p2Strategy.entrySet().forEach(System.out::println);
        new GambitEFG().write("ALossCounter.gbt", rootState, cfrExpander1);
    }

    public static Map<Action, Double> getStrategyFor(GameState rootState, Player player, Distribution distribution, Map<ISKey, IRCFRInformationSet> informationSets, Expander expander) {
        Map<Action, Double> strategy = new HashMap<>();
        HashSet<IRCFRInformationSet> processed = new HashSet();
        ArrayDeque<GameState> q = new ArrayDeque();

        q.add(rootState);
        while (!q.isEmpty()) {
            GameState curNode = q.removeFirst();
            IRCFRInformationSet curNodeIS = informationSets.get(curNode.getISKeyForPlayerToMove());

            if (curNodeIS == null) {
                assert (curNode.isPlayerToMoveNature());
            } else if (curNode.getPlayerToMove().equals(player) && !processed.contains(curNodeIS)) {
                Map<Action, Double> actionDistribution = distribution.getDistributionFor(curNodeIS.getData());

                strategy.putAll(actionDistribution);
                processed.add(curNodeIS);
            }
            List<Action> tmp = expander.getActions(curNode);

            for (Action a : tmp) {
                GameState newState = curNode.performAction(a);
                if (!newState.isGameEnd()) q.addLast(newState);
            }
        }
        return strategy;
    }

    protected Player searchingPlayer;
    protected GameState rootState;
    protected ThreadMXBean threadBean;
    protected Expander<IRCFRInformationSet> expander;
    protected AlgorithmConfig<IRCFRInformationSet> config;

    protected boolean firstIteration = true;

    public IRCFR(Player searchingPlayer, GameState rootState, Expander<IRCFRInformationSet> expander) {
        this.searchingPlayer = searchingPlayer;
        this.rootState = rootState;
        this.expander = expander;
        this.config = expander.getAlgorithmConfig();
        threadBean = ManagementFactory.getThreadMXBean();
    }

    public Action runMiliseconds(int miliseconds) {
        int iters = 0;
        long start = threadBean.getCurrentThreadCpuTime();
        while ((threadBean.getCurrentThreadCpuTime() - start) / 1e6 < miliseconds) {
            System.out.println(iteration(rootState, 1, 1, rootState.getAllPlayers()[0]));
            update();
            iters++;
            System.out.println(iteration(rootState, 1, 1, rootState.getAllPlayers()[1]));
            update();
            iters++;
        }
        firstIteration = false;
        System.out.println();
        System.out.println("Iters: " + iters);
        return null;
    }

    public Action runIterations(int iterations) {
        for (int i = 0; i < iterations; i++) {
            iteration(rootState, 1, 1, rootState.getAllPlayers()[0]);
            iteration(rootState, 1, 1, rootState.getAllPlayers()[1]);
        }
        firstIteration = false;
        System.out.println();
        return null;
    }

    private void update() {
        config.getAllInformationSets().values().forEach(informationSet -> ((FixedForIterationData)informationSet.getData()).applyUpdate());
    }

    /**
     * The main function for CFR iteration. Implementation based on Algorithm 1 in M. Lanctot PhD thesis.
     *
     * @param state     current state
     * @param pi1       probability with which the opponent of the searching player and chance want to reach the current state
     * @param expPlayer the exploring player for this iteration
     * @return iteration game reward is actually returned. Other return values are in global x and l
     */
    protected double iteration(GameState state, double pi1, double pi2, Player expPlayer) {
        if (pi1 == 0 && pi2 == 0)
            return 0;
        if (state.isGameEnd())
            return state.getUtilities()[expPlayer.getId()];
        IRCFRInformationSet informationSet = config.getInformationSetFor(state);

        if (informationSet == null) {
            informationSet = config.createInformationSetFor(state);
            config.addInformationSetFor(state, informationSet);
            informationSet.setData(createAlgData(state));
        }
        if (firstIteration && !informationSet.getAllStates().contains(state))
            config.addInformationSetFor(state, informationSet);
        OOSAlgorithmData data = informationSet.getData();
        List<Action> actions = data.getActions();

        if (state.isPlayerToMoveNature())
            return getValueForNature(state, pi1, pi2, expPlayer, informationSet, actions);
        double[] rmProbs = getStrategy(data);
        double[] tmpV = new double[rmProbs.length];
        double ev = 0;

        int i = -1;
        for (Action ai : actions) {
            i++;
            ai.setInformationSet(informationSet);
            GameState newState = state.performAction(ai);

            if (informationSet.getPlayer().getId() == 0) {
                tmpV[i] = iteration(newState, pi1 * rmProbs[i], pi2, expPlayer);
            } else {
                tmpV[i] = iteration(newState, pi1, rmProbs[i] * pi2, expPlayer);
            }
            ev += rmProbs[i] * tmpV[i];
        }
        if (informationSet.getPlayer().equals(expPlayer))
            update(pi1, pi2, expPlayer, data, rmProbs, tmpV, ev);
        return ev;
    }

    private double getValueForNature(GameState node, double pi1, double pi2, Player expPlayer, IRCFRInformationSet is, List<Action> actions) {
        double ev = 0;

        for (Action action : actions) {
            final double p = node.getProbabilityOfNatureFor(action);
            double new_p1 = expPlayer.getId() == 1 ? pi1 * p : pi1;
            double new_p2 = expPlayer.getId() == 0 ? pi2 * p : pi2;
            GameState newState = node.performAction(action);

            action.setInformationSet(is);
            ev += p * iteration(newState, new_p1, new_p2, expPlayer);
        }
        return ev;
    }

    protected FixedForIterationData createAlgData(GameState node) {
        return new FixedForIterationData(expander.getActions(node));
    }

    protected void update(double pi1, double pi2, Player expPlayer, OOSAlgorithmData data, double[] rmProbs, double[] tmpV, double ev) {
        double[] expPlayerVals = new double[tmpV.length];

        for (int i = 0; i < tmpV.length; i++) {
            expPlayerVals[i] = tmpV[i];
        }
        data.updateAllRegrets(tmpV, ev, /*(expPlayer.getId() == 0 ? pi2 : pi1)*/pi1 * pi2);
        data.updateMeanStrategy(rmProbs, /*(expPlayer.getId() == 0 ? pi1 : pi2)*/pi1 * pi2);
    }

    protected double[] getStrategy(OOSAlgorithmData data) {
        return data.getRMStrategy();
    }

}
