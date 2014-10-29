package cz.agents.gtlibrary.algorithms.cfr.generalsum;

import cz.agents.gtlibrary.algorithms.mcts.AlgorithmData;
import cz.agents.gtlibrary.algorithms.mcts.MCTSConfig;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.distribution.MeanStratDist;
import cz.agents.gtlibrary.algorithms.mcts.distribution.StrategyCollector;
import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.oos.OOSAlgorithmData;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.BackPropFactory;
import cz.agents.gtlibrary.algorithms.sequenceform.FullSequenceEFG;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.domain.ir.memoryloss.MLExpander;
import cz.agents.gtlibrary.domain.ir.memoryloss.MLGameState;
import cz.agents.gtlibrary.domain.poker.generic.GPGameInfo;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerExpander;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerGameState;
import cz.agents.gtlibrary.domain.poker.kuhn.KPGameInfo;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerExpander;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerGameState;
import cz.agents.gtlibrary.domain.pursuit.PursuitExpander;
import cz.agents.gtlibrary.domain.pursuit.PursuitGameState;
import cz.agents.gtlibrary.domain.stacktest.StackTestExpander;
import cz.agents.gtlibrary.domain.stacktest.StackTestGameState;
import cz.agents.gtlibrary.experimental.utils.UtilityCalculator;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.strategy.Strategy;
import cz.agents.gtlibrary.utils.Pair;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class CFRISGenSumAlg implements GamePlayingAlgorithm {

    public static void main(String[] args) {
//         runKuhnPoker();
//        runStackTest();
           runPursuit();
//        runGenericPoker();
//        runML();
    }

    private static void runML() {
        GameState rootState = new MLGameState();
        Expander<MCTSInformationSet> cfrExpander1 = new MLExpander<>(new MCTSConfig());
        CFRISGenSumAlg cfr = new CFRISGenSumAlg(rootState.getAllPlayers()[0], rootState, cfrExpander1);

        cfr.runMiliseconds(10000);
    }

    private static void runPursuit() {
        GameState rootState = new PursuitGameState();
        Expander<MCTSInformationSet> cfrExpander1 = new PursuitExpander<>(new MCTSConfig());
        CFRISGenSumAlg cfr = new CFRISGenSumAlg(rootState.getAllPlayers()[0], rootState, cfrExpander1);

        cfr.runMiliseconds(10000);
    }

    private static void runStackTest() {
        GameState rootState = new StackTestGameState();
        Expander<MCTSInformationSet> cfrExpander1 = new StackTestExpander<>(new MCTSConfig());
        CFRISGenSumAlg cfr = new CFRISGenSumAlg(rootState.getAllPlayers()[0], rootState, cfrExpander1);

        cfr.runMiliseconds(10000);
    }

    private static void runKuhnPoker() {
        GameState rootState = new KuhnPokerGameState();
        Expander<MCTSInformationSet> cfrExpander1 = new KuhnPokerExpander<>(new MCTSConfig());
        CFRISGenSumAlg cfr = new CFRISGenSumAlg(rootState.getAllPlayers()[0], rootState, cfrExpander1);

        cfr.runMiliseconds(10000);;
    }

    private static void runGenericPoker() {
        GameState rootState = new GenericPokerGameState();
        Expander<MCTSInformationSet> cfrExpander1 = new GenericPokerExpander<>(new MCTSConfig());
        CFRISGenSumAlg cfr = new CFRISGenSumAlg(rootState.getAllPlayers()[0], rootState, cfrExpander1);

        cfr.runMiliseconds(10000);
    }

    protected Player searchingPlayer;
    protected BackPropFactory fact;
    protected GameState rootState;
    protected ThreadMXBean threadBean;
    protected Expander expander;
    protected AlgorithmConfig<MCTSInformationSet> config;

    protected HashMap<Pair<Integer, Sequence>, MCTSInformationSet> informationSets = new HashMap<>();
    protected boolean firstIteration = true;

    public CFRISGenSumAlg(Player searchingPlayer, GameState rootState, Expander expander) {
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
        for (;(threadBean.getCurrentThreadCpuTime()-start)/1e6 < miliseconds;) {
            System.out.println(Arrays.toString(iteration(rootState, 1, 1, rootState.getAllPlayers()[0])));
            iters++;
            System.out.println(Arrays.toString(iteration(rootState, 1, 1, rootState.getAllPlayers()[1])));
            iters++;
        }
        firstIteration = false;
        System.out.println();
        System.out.println("Iters: " + iters);
        return null;
    }

    public Action runIterations(int iterations){
        int iters=0;

        for (int i = 0; i < iterations; i++) {
            iteration(rootState, 1, 1, rootState.getAllPlayers()[0]);
            iters++;
            iteration(rootState, 1, 1, rootState.getAllPlayers()[1]);
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
    protected double[] iteration(GameState node, double pi1, double pi2, Player expPlayer){
        if (pi1==0 && pi2==0)
            return new double[]{0, 0};
        if (node.isGameEnd())
            return node.getUtilities();
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
            double[] ev = new double[2];

            for (Action ai : actions){
                ai.setInformationSet(is);

                final double p = node.getProbabilityOfNatureFor(ai);
                double new_p1 = expPlayer.getId()==1 ? pi1 * p : pi1;
                double new_p2 = expPlayer.getId()==0 ? pi2 * p : pi2;
                GameState newState = node.performAction(ai);
                double[] tempValues = iteration(newState, new_p1, new_p2, expPlayer);

                ev[0] += p*tempValues[0];
                ev[1] += p*tempValues[1];
            }
            return ev;
        }

        double[] rmProbs = getStrategy(data, node);
        double[][] tmpV = new double[rmProbs.length][2];
        double[] ev = new double[2];

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
            ev[0] += rmProbs[i]*tmpV[i][0];
            ev[1] += rmProbs[i]*tmpV[i][1];
        }
        if (is.getPlayer().equals(expPlayer)) {
            update(node, pi1, pi2, expPlayer, data, rmProbs, tmpV, ev);
        }

        return ev;
    }

    protected AlgorithmData createAlgData(GameState node) {
        return new OOSAlgorithmData(expander.getActions(node));
    }

    protected void update(GameState state, double pi1, double pi2, Player expPlayer, OOSAlgorithmData data, double[] rmProbs, double[][] tmpV, double[] ev) {
        double[] expPlayerVals = getExpPlayerValues(expPlayer, tmpV);
        
        data.updateAllRegrets(expPlayerVals, ev[expPlayer.getId()], (expPlayer.getId() == 0 ? pi2 : pi1)/*pi1*pi2*/);
        data.updateMeanStrategy(rmProbs, (expPlayer.getId() == 0 ? pi1 : pi2)/*pi1*pi2*/);
    }

    private double[] getExpPlayerValues(Player expPlayer, double[][] tmpV) {
        double[] expPlayerVals = new double[tmpV.length];

        for (int i = 0; i < expPlayerVals.length; i++) {
            expPlayerVals[i] = tmpV[i][expPlayer.getId()];
        }
        return expPlayerVals;
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
