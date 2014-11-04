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

import cz.agents.gtlibrary.algorithms.mcts.AlgorithmData;
import cz.agents.gtlibrary.algorithms.mcts.MCTSConfig;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.distribution.MeanStratDist;
import cz.agents.gtlibrary.algorithms.mcts.distribution.MeanStrategyProvider;
import cz.agents.gtlibrary.algorithms.mcts.distribution.StrategyCollector;
import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.oos.OOSAlgorithmData;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.BackPropFactory;
import cz.agents.gtlibrary.algorithms.sequenceform.FullSequenceEFG;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.domain.ir.leftright.LRExpander;
import cz.agents.gtlibrary.domain.ir.leftright.LRGameState;
import cz.agents.gtlibrary.domain.ir.memoryloss.MLExpander;
import cz.agents.gtlibrary.domain.ir.memoryloss.MLGameState;
import cz.agents.gtlibrary.domain.poker.generic.GPGameInfo;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerExpander;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerGameState;
import cz.agents.gtlibrary.domain.poker.kuhn.KPGameInfo;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerExpander;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerGameState;
import cz.agents.gtlibrary.experimental.utils.UtilityCalculator;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.strategy.Strategy;
import cz.agents.gtlibrary.utils.Pair;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.*;

/**
 * @author vilo
 */
public class CFRISAlgorithm implements GamePlayingAlgorithm {

    public static void main(String[] args) {
         runKuhnPoker();
//        runGenericPoker();
//        runML();
    }

    private static void runML() {
        GameState rootState = new MLGameState();
        Expander<MCTSInformationSet> cfrExpander1 = new MLExpander<>(new MCTSConfig());
        CFRISAlgorithm cfr = new CFRISAlgorithm(rootState.getAllPlayers()[0], rootState, cfrExpander1);

        cfr.runMiliseconds(10000);
    }

    private static void runKuhnPoker() {
        GameState rootState = new KuhnPokerGameState();
        Expander<MCTSInformationSet> cfrExpander1 = new KuhnPokerExpander<>(new MCTSConfig());
        Expander<MCTSInformationSet> cfrExpander2 = new KuhnPokerExpander<>(new MCTSConfig());
        Expander<SequenceInformationSet> brExpander = new KuhnPokerExpander<>(new SequenceFormConfig<>());
        FullSequenceEFG efg = new FullSequenceEFG(rootState, brExpander, new KPGameInfo(), (SequenceFormConfig<SequenceInformationSet>) brExpander.getAlgorithmConfig());

        efg.generateCompleteGame();
        CFRISAlgorithm cfr = new CFRISAlgorithm(rootState.getAllPlayers()[0], rootState, cfrExpander1);

        cfr.runMiliseconds(10000);
        Strategy p1Strategy = StrategyCollector.getStrategyFor(rootState, rootState.getAllPlayers()[0], new MeanStratDist(), cfr.informationSets, cfrExpander1);

        p1Strategy.sanityCheck(rootState, brExpander);
        cfr = new CFRISAlgorithm(rootState.getAllPlayers()[1], rootState, cfrExpander2);
        cfr.runMiliseconds(10000);
        Strategy p2Strategy = StrategyCollector.getStrategyFor(rootState, rootState.getAllPlayers()[1], new MeanStratDist(), cfr.informationSets, cfrExpander2);

        p2Strategy.sanityCheck(rootState, brExpander);
        UtilityCalculator calculator = new UtilityCalculator(rootState, brExpander);

        System.out.println(calculator.computeUtility(p1Strategy, p2Strategy));
        System.out.println(p1Strategy);
        System.out.println(p2Strategy);
    }

    private static void runGenericPoker() {
        GameState rootState = new GenericPokerGameState();
        Expander<MCTSInformationSet> cfrExpander1 = new GenericPokerExpander<>(new MCTSConfig());
        Expander<MCTSInformationSet> cfrExpander2 = new GenericPokerExpander<>(new MCTSConfig());
        Expander<SequenceInformationSet> brExpander = new GenericPokerExpander<>(new SequenceFormConfig<>());
        FullSequenceEFG efg = new FullSequenceEFG(rootState, brExpander, new GPGameInfo(), (SequenceFormConfig<SequenceInformationSet>) brExpander.getAlgorithmConfig());

        efg.generateCompleteGame();
        CFRISAlgorithm cfr = new CFRISAlgorithm(rootState.getAllPlayers()[0], rootState, cfrExpander1);

        cfr.runMiliseconds(10000);
        Strategy p1Strategy = StrategyCollector.getStrategyFor(rootState, rootState.getAllPlayers()[0], new MeanStratDist(), cfr.informationSets, cfrExpander1);

        p1Strategy.sanityCheck(rootState, brExpander);
        cfr = new CFRISAlgorithm(rootState.getAllPlayers()[1], rootState, cfrExpander2);
        cfr.runMiliseconds(10000);
        Strategy p2Strategy = StrategyCollector.getStrategyFor(rootState, rootState.getAllPlayers()[1], new MeanStratDist(), cfr.informationSets, cfrExpander2);

        p2Strategy.sanityCheck(rootState, brExpander);
        UtilityCalculator calculator = new UtilityCalculator(rootState, brExpander);

        System.out.println(calculator.computeUtility(p1Strategy, p2Strategy));
        System.out.println(p1Strategy);
        System.out.println(p2Strategy);
    }

    protected Player searchingPlayer;
    protected BackPropFactory fact;
    protected GameState rootState;
    protected ThreadMXBean threadBean;
    protected Expander expander;
    protected AlgorithmConfig<MCTSInformationSet> config;

    protected HashMap<Pair<Integer, Sequence>, MCTSInformationSet> informationSets = new HashMap<>();
    protected boolean firstIteration = true;

    public CFRISAlgorithm(Player searchingPlayer, GameState rootState, Expander expander) {
        this.searchingPlayer = searchingPlayer;
        this.rootState = rootState;
        this.expander = expander;
        this.config = expander.getAlgorithmConfig();
        threadBean = ManagementFactory.getThreadMXBean();
    }

    @Override
    public Action runMiliseconds(int miliseconds){
        int iters=0;
        long start = threadBean.getCurrentThreadCpuTime();
        while ((threadBean.getCurrentThreadCpuTime()-start)/1e6 < miliseconds) {
            System.out.println(iteration(rootState, 1, 1, rootState.getAllPlayers()[0]));
            iters++;
            System.out.println(iteration(rootState, 1, 1, rootState.getAllPlayers()[1]));
            iters++;
        }
        firstIteration = false;
        System.out.println();
        System.out.println("Iters: " + iters);
        return null;
    }

    /**
     * The main function for CFR iteration. Implementation based on Algorithm 1 in M. Lanctot PhD thesis.
     * @param node current node
     * @param pi1 probability with which the opponent of the searching player and chance want to reach the current node
     * @param expPlayer the exploring player for this iteration
     * @return iteration game value is actually returned. Other return values are in global x and l
     */
    protected double iteration(GameState node, double pi1, double pi2, Player expPlayer){
        if (pi1==0 && pi2==0) return 0;
        if (node.isGameEnd()) {
            return node.getUtilities()[expPlayer.getId()];
        }

        MCTSInformationSet is = informationSets.get(node.getISKeyForPlayerToMove());
        if (is == null) {
            is = config.createInformationSetFor(node);
            config.addInformationSetFor(node, is);
            is.setAlgorithmData(createAlgData(node));
            informationSets.put(node.getISKeyForPlayerToMove(), is);
        }
        if (firstIteration && !is.getAllStates().contains(node)) {
            config.addInformationSetFor(node, is);
        }

        OOSAlgorithmData data = (OOSAlgorithmData) is.getAlgorithmData();
        List<Action> actions = data.getActions();

        if (node.isPlayerToMoveNature()) {
            double ev=0;
            for (Action ai : actions){
                ai.setInformationSet(is);
                final double p = node.getProbabilityOfNatureFor(ai);
                double new_p1 = expPlayer.getId()==1 ? pi1 * p : pi1;
                double new_p2 = expPlayer.getId()==0 ? pi2 * p : pi2;
                GameState newState = node.performAction(ai);
                ev += p*iteration(newState, new_p1, new_p2, expPlayer);
            }
            return ev;
        }

        double[] rmProbs = getStrategy(data, node);
        double[] tmpV = new double[rmProbs.length];
        double ev=0;

        int i=-1;
        for (Action ai : actions){
            i++;
            ai.setInformationSet(is);
            GameState newState = node.performAction(ai);
            if (is.getPlayer().getId()==0){
                tmpV[i]=iteration(newState, pi1 * rmProbs[i], pi2, expPlayer);
            }  else {
                tmpV[i]=iteration(newState, pi1, rmProbs[i] * pi2, expPlayer);
            }
            ev += rmProbs[i]*tmpV[i];
        }
        if (is.getPlayer().equals(expPlayer)) {
            update(node, pi1, pi2, expPlayer, data, rmProbs, tmpV, ev);
        }

        return ev;
    }

    protected AlgorithmData createAlgData(GameState node) {
        return new OOSAlgorithmData(expander.getActions(node));
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

    public HashMap<Pair<Integer, Sequence>, MCTSInformationSet> getInformationSets() {
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
