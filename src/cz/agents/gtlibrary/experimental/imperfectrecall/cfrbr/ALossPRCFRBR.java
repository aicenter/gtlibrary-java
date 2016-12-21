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
package cz.agents.gtlibrary.experimental.imperfectrecall.cfrbr;

import cz.agents.gtlibrary.algorithms.cfr.ir.FixedForIterationData;
import cz.agents.gtlibrary.algorithms.cfr.ir.IRCFR;
import cz.agents.gtlibrary.algorithms.cfr.ir.IRCFRConfig;
import cz.agents.gtlibrary.algorithms.cfr.ir.IRCFRInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.AlgorithmData;
import cz.agents.gtlibrary.algorithms.mcts.distribution.Distribution;
import cz.agents.gtlibrary.algorithms.mcts.distribution.MeanStratDist;
import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.oos.OOSAlgorithmData;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.BackPropFactory;
import cz.agents.gtlibrary.algorithms.sequenceform.FullSequenceEFG;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.domain.bpg.BPGExpander;
import cz.agents.gtlibrary.domain.bpg.BPGGameInfo;
import cz.agents.gtlibrary.domain.bpg.imperfectrecall.IRBPGGameState;
import cz.agents.gtlibrary.domain.poker.kuhn.KPGameInfo;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerExpander;
import cz.agents.gtlibrary.domain.poker.kuhn.ir.IRKuhnPokerGameState;
import cz.agents.gtlibrary.domain.randomabstraction.RandomAbstractionExpander;
import cz.agents.gtlibrary.domain.randomabstraction.RandomAlossAbstractionGameStateFactory;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameExpander;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameInfo;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameState;
import cz.agents.gtlibrary.domain.randomgameimproved.io.BasicGameBuilder;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.br.ALossBestResponseAlgorithm;
import cz.agents.gtlibrary.experimental.imperfectrecall.cfrbr.flexibleis.FlexibleISKeyExpander;
import cz.agents.gtlibrary.experimental.imperfectrecall.cfrbr.flexibleis.FlexibleISKeyGameState;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.interfaces.*;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author vilo
 */
public class ALossPRCFRBR implements GamePlayingAlgorithm {

    public static void main(String[] args) {
//        runIRKuhnPoker();
//        runRandomAbstractionGame();
        runIRBPG();
    }

    private static void runIRBPG() {
        GameState root = new IRBPGGameState();
        Expander<IRCFRInformationSet> expander = new BPGExpander<>(new IRCFRConfig());
        ALossPRCFRBR cfr = new ALossPRCFRBR(root.getAllPlayers()[1], root, expander, new BPGGameInfo());

        cfr.runIterations(10000);
    }


    private static void runIRKuhnPoker() {
        GameState root = new IRKuhnPokerGameState();
        Expander<IRCFRInformationSet> expander = new KuhnPokerExpander<>(new IRCFRConfig());
        BasicGameBuilder.build(root, expander.getAlgorithmConfig(), expander);
        ALossPRCFRBR cfr = new ALossPRCFRBR(root.getAllPlayers()[0], root, expander, new KPGameInfo());

        cfr.runIterations(10000);
    }

    private static void runRandomAbstractionGame() {
        GameState wrappedRoot = new RandomGameState();
        SequenceFormConfig<SequenceInformationSet> config = new SequenceFormConfig<>();
        Expander<SequenceInformationSet> wrappedExpander = new RandomGameExpander<>(config);

        FullSequenceEFG efg = new FullSequenceEFG(wrappedRoot, wrappedExpander, new RandomGameInfo(), config);
        efg.generateCompleteGame();

        GameState root = new RandomAlossAbstractionGameStateFactory().createRoot(wrappedRoot, wrappedExpander.getAlgorithmConfig());
        Expander<IRCFRInformationSet> expander = new RandomAbstractionExpander<>(wrappedExpander, new IRCFRConfig());

        BasicGameBuilder.build(root, expander.getAlgorithmConfig(), expander);
        ALossPRCFRBR cfr = new ALossPRCFRBR(root.getAllPlayers()[0], root, expander, new KPGameInfo());

        cfr.runIterations(100);
    }

    protected Player regretMatchingPlayer;
    protected BackPropFactory fact;
    protected FlexibleISKeyGameState rootState;
    protected ThreadMXBean threadBean;
    protected Expander<IRCFRInformationSet> expander;
    protected AlgorithmConfig<IRCFRInformationSet> config;
    protected ALossBestResponseAlgorithm br;

    protected HashMap<ISKey, IRCFRInformationSet> informationSets = new HashMap<>();
    protected boolean firstIteration = true;

    public ALossPRCFRBR(Player regretMatchingPlayer, GameState rootState, Expander<IRCFRInformationSet> expander, GameInfo info) {
        this.regretMatchingPlayer = regretMatchingPlayer;
        this.rootState = new FlexibleISKeyGameState(rootState);
        this.expander = new FlexibleISKeyExpander<>(expander);
        this.config = expander.getAlgorithmConfig();
        br = new ALossBestResponseAlgorithm(this.rootState, this.expander, 1 - regretMatchingPlayer.getId(), new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]}, config, info, false);
        threadBean = ManagementFactory.getThreadMXBean();
    }

    @Override
    public Action runMiliseconds(int miliseconds) {
        int iters = 0;
        long start = threadBean.getCurrentThreadCpuTime();
        while ((threadBean.getCurrentThreadCpuTime() - start) / 1e6 < miliseconds) {
            System.out.println(regretMatchingIteration(rootState, 1, 1, rootState.getAllPlayers()[0]));
            iters++;
            System.out.println(regretMatchingIteration(rootState, 1, 1, rootState.getAllPlayers()[1]));
            iters++;
        }
        firstIteration = false;
        System.out.println();
        System.out.println("Iters: " + iters);
        return null;
    }

    public Action runIterations(int iterations) {
        for (int i = 0; i < iterations; i++) {
            if (regretMatchingPlayer.equals(rootState.getAllPlayers()[0])) {
                regretMatchingIteration(rootState, 1, 1, rootState.getAllPlayers()[0]);
                update();
                bestResponseIteration();
                update();
            } else {
                regretMatchingIteration(rootState, 1, 1, rootState.getAllPlayers()[1]);
                update();
                bestResponseIteration();
                update();
            }
            if (i % 100 == 0) {
                Map<Action, Double> strategy = IRCFR.getStrategyFor(rootState, regretMatchingPlayer, new MeanStratDist(), config.getAllInformationSets(), expander);

//                System.out.println(strategy);
//                System.out.println(IRCFR.getStrategyFor(rootState, rootState.getAllPlayers()[1 - regretMatchingPlayer.getId()], new MeanStratDist(), config.getAllInformationSets(), expander));
                System.out.println(br.calculateBR(rootState, strategy));
            }
        }
        firstIteration = false;
        return null;
    }

    private void update() {
        informationSets.values().forEach(informationSet -> informationSet.getData().applyUpdate());
    }

    private double bestResponseIteration() {
        Map<Action, Double> strategy = IRCFR.getStrategyFor(rootState, regretMatchingPlayer, data -> {
            CFRBRData cfrbrData = (CFRBRData) data;
            Map<Action, Double> distribution = new HashMap<>(cfrbrData.getActions().size());

            for (int i = 0; i < cfrbrData.getActions().size(); i++) {
                distribution.put(cfrbrData.getActions().get(i), cfrbrData.getRMStrategy()[i]);
            }
            return distribution;
        }, config.getAllInformationSets(), expander);
        double value = br.calculateBR(rootState, strategy);
        updateData(rootState, br.getBestResponse(), strategy);

        return value;
    }

    private void updateData(GameState state, Map<Action, Double> bestResponse, Map<Action, Double> strategy) {
        if (state.isGameEnd())
            return;
        if (state.isPlayerToMoveNature()) {
            expander.getActions(state).stream().map(a -> state.performAction(a)).forEach(s -> updateData(s, bestResponse, strategy));
            return;
        }
        if (state.getPlayerToMove().equals(regretMatchingPlayer)) {
            expander.getActions(state).stream().filter(a -> strategy.getOrDefault(a, 0d) > 1e-8).map(a -> state.performAction(a)).forEach(s -> updateData(s, bestResponse, strategy));
            return;
        }
        IRCFRInformationSet is = informationSets.get(state.getISKeyForPlayerToMove());
        List<Action> actions = expander.getActions(state);
        assert actions.stream().filter(a -> bestResponse.getOrDefault(a, 0d) > 1 - 1e-8).count() == 1;
        int actionIndex = getIndex(actions, bestResponse);

        ((CFRBRData) is.getData()).setRegretAtIndex(actionIndex, 1);
        ((CFRBRData) is.getData()).updateMeanStrategy(actionIndex, 1);
        actions.stream().filter(a -> bestResponse.getOrDefault(a, 0d) > 1 - 1e-8).map(a -> state.performAction(a)).forEach(s -> updateData(s, bestResponse, strategy));
    }

    private int getIndex(List<Action> actions, Map<Action, Double> bestResponse) {
        int index = -1;

        for (Action action : actions) {
            index++;
            if (bestResponse.getOrDefault(action, 0d) > 1 - 1e-8)
                return index;
        }
        return -1;
    }

    /**
     * The main function for CFR iteration. Implementation based on Algorithm 1 in M. Lanctot PhD thesis.
     *
     * @param node      current node
     * @param pi1       probability with which the opponent of the searching player and chance want to reach the current node
     * @param expPlayer the exploring player for this iteration
     * @return iteration game value is actually returned. Other return values are in global x and l
     */
    protected double regretMatchingIteration(GameState node, double pi1, double pi2, Player expPlayer) {
        if (pi1 == 0 && pi2 == 0) return 0;
        if (node.isGameEnd()) {
            return node.getUtilities()[expPlayer.getId()];
        }

        IRCFRInformationSet is = informationSets.get(node.getISKeyForPlayerToMove());
        if (is == null) {
            is = config.createInformationSetFor(node);
            config.addInformationSetFor(node, is);
            is.setData(createAlgData(node));
            informationSets.put(node.getISKeyForPlayerToMove(), is);
        }
        if (firstIteration && !is.getAllStates().contains(node)) {
            config.addInformationSetFor(node, is);
        }

        OOSAlgorithmData data = is.getData();
        List<Action> actions = data.getActions();

        if (node.isPlayerToMoveNature()) {
            double ev = 0;
            for (Action ai : actions) {
                ai.setInformationSet(is);
                final double p = node.getProbabilityOfNatureFor(ai);
                double new_p1 = expPlayer.getId() == 1 ? pi1 * p : pi1;
                double new_p2 = expPlayer.getId() == 0 ? pi2 * p : pi2;
                GameState newState = node.performAction(ai);
                ev += p * regretMatchingIteration(newState, new_p1, new_p2, expPlayer);
            }
            return ev;
        }

        double[] rmProbs = getStrategy(data, node);
        double[] tmpV = new double[rmProbs.length];
        double ev = 0;

        int i = -1;
        for (Action ai : actions) {
            i++;
            ai.setInformationSet(is);
            GameState newState = node.performAction(ai);
            if (is.getPlayer().getId() == 0) {
                tmpV[i] = regretMatchingIteration(newState, pi1 * rmProbs[i], pi2, expPlayer);
            } else {
                tmpV[i] = regretMatchingIteration(newState, pi1, rmProbs[i] * pi2, expPlayer);
            }
            ev += rmProbs[i] * tmpV[i];
        }
        if (is.getPlayer().equals(expPlayer)) {
            update(node, pi1, pi2, expPlayer, data, rmProbs, tmpV, ev);
        }

        return ev;
    }

    protected FixedForIterationData createAlgData(GameState node) {
        return new CFRBRData(expander.getActions(node));
    }

    protected void update(GameState state, double pi1, double pi2, Player expPlayer, OOSAlgorithmData data, double[] rmProbs, double[] tmpV, double ev) {
        double[] expPlayerVals = new double[tmpV.length];

        for (int i = 0; i < tmpV.length; i++) {
            expPlayerVals[i] = tmpV[i];
        }
        data.updateAllRegrets(tmpV, ev, (expPlayer.getId() == 0 ? pi2 : pi1)/*pi1*pi2*/);
        data.updateMeanStrategy(rmProbs, (expPlayer.getId() == 0 ? pi1 : pi2)/*pi1*pi2*/);
    }

    protected double[] getStrategy(OOSAlgorithmData data, GameState state) {
        return data.getRMStrategy();
    }

    @Override
    public void setCurrentIS(InformationSet is) {
        throw new NotImplementedException();
    }

    public HashMap<ISKey, IRCFRInformationSet> getInformationSets() {
        return informationSets;
    }

    @Override
    public InnerNode getRootNode() {
        return null;
    }

    @Override
    public Action runMiliseconds(int miliseconds, GameState gameState) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}

