/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.agents.gtlibrary.algorithms.mcts;

import cz.agents.gtlibrary.algorithms.mcts.distribution.MeanStratDist;
import cz.agents.gtlibrary.algorithms.mcts.nodes.ChanceNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.LeafNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.Node;
import cz.agents.gtlibrary.algorithms.mcts.nodes.oos.OOSAlgorithmData;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.BackPropFactory;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.strategy.Strategy;
import cz.agents.gtlibrary.utils.HighQualityRandom;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Map;
import java.util.Random;

    
/**
 *
 * @author vilo
 */
public class SMOOSAlgorithm implements GamePlayingAlgorithm {
    protected Player searchingPlayer;
    protected OOSSimulator simulator;
    protected BackPropFactory fact;
    protected InnerNode rootNode;
    protected ThreadMXBean threadBean;
    private double epsilon = 0.6;
    
    private Random rnd = new HighQualityRandom();

    public SMOOSAlgorithm(Player searchingPlayer, OOSSimulator simulator, GameState rootState, Expander expander) {
        this(searchingPlayer, simulator, rootState, expander, 0.6);
    }
    
    public SMOOSAlgorithm(Player searchingPlayer, OOSSimulator simulator, GameState rootState, Expander expander, double epsilon) {
        this.searchingPlayer = searchingPlayer;
        this.simulator = simulator;
        this.epsilon = epsilon;
        if (rootState.isPlayerToMoveNature()) this.rootNode = new ChanceNode(expander, rootState);
        else this.rootNode = new InnerNode(expander, rootState);
        threadBean = ManagementFactory.getThreadMXBean();
    }
    
    @Override
    public Action runMiliseconds(int miliseconds){
        int iters=0;
        long start = threadBean.getCurrentThreadCpuTime();
        for (;(threadBean.getCurrentThreadCpuTime()-start)/1e6 < miliseconds;) {
            iteration(rootNode,1,1,1,rootNode.getGameState().getAllPlayers()[0]);
            iters++;
            iteration(rootNode,1,1,1,rootNode.getGameState().getAllPlayers()[1]);
            iters++;
        }
        System.out.println();
        System.out.println("OOS Iters: " + iters);
        if (!rootNode.getInformationSet().getPlayer().equals(searchingPlayer)) return null;
        Map<Action, Double> distribution = (new MeanStratDist()).getDistributionFor(rootNode.getInformationSet().getAlgorithmData());
        return Strategy.selectAction(distribution, rnd);
    }
    
    public Action runIterations(int iterations){
        for (int i=0;i<iterations/2;i++) {
            iteration(rootNode,1,1,1,rootNode.getGameState().getAllPlayers()[0]);
            iteration(rootNode,1,1,1,rootNode.getGameState().getAllPlayers()[1]);
        }
        if (!rootNode.getInformationSet().getPlayer().equals(searchingPlayer)) return null;
        Map<Action, Double> distribution = (new MeanStratDist()).getDistributionFor(rootNode.getInformationSet().getAlgorithmData());
        return Strategy.selectAction(distribution, rnd);
    }
    
    
    //additional iteration return values
    private double x=-1;
    private double l=-1;
    
    /** 
     * The main function for OOS iteration.
     * @param node current node
     * @param pi probability with which the searching player wants to reach the current node
     * @param pi_ probability with which the opponent of the searching player and chance want to reach the current node
     * @param us probability that the unbiased sample reaches this node
     * @param expPlayer the exploring player for this iteration
     * @return iteration game value is actually returned. Other return values are in global x and l
     */
    protected double iteration(Node node, double pi, double pi_, double us, Player expPlayer){
        if (node instanceof LeafNode) {
            x=1; l=us;
            return ((LeafNode)node).getUtilities()[expPlayer.getId()];
        } 
        if (node instanceof ChanceNode) {
            ChanceNode cn = (ChanceNode)node;
            Action a = cn.getRandomAction();
            double p = cn.getGameState().getProbabilityOfNatureFor(a);
            double u=iteration(cn.getChildFor(a), pi, p*pi_, p*us, expPlayer);
            x *= p;
            return u;
        }
        InnerNode in = (InnerNode) node;
        MCTSInformationSet is = in.getInformationSet();
        OOSAlgorithmData data;
        Action selectedA; double u=0; int ai=-1; double pai=-1;
        if (is.getAlgorithmData() == null){//this is a new Information Set
            data = new OOSAlgorithmData(in.getActions());
            is.setAlgorithmData(data);
            ai = rnd.nextInt(in.getActions().size());
            pai=1.0/in.getActions().size();
            selectedA = in.getActions().get(ai);
            Node child = in.getChildFor(selectedA);
            u = simulator.simulate(child.getGameState())[expPlayer.getId()];
            x = simulator.playersProb*(1.0/in.getActions().size());
            l = us*simulator.playOutProb*(1.0/in.getActions().size());
        } else {
            data = (OOSAlgorithmData) is.getAlgorithmData();
            data.getRMStrategy(rmProbs);
            
            if (is.getPlayer().equals(expPlayer)){
                if (rnd.nextDouble() < epsilon) ai = rnd.nextInt(in.getActions().size());
                else ai = randomChoice(rmProbs, 1);                
                pai=rmProbs[ai];
                u = iteration(in.getChildFor(in.getActions().get(ai)),
                        pi*pai, pi_, us*((1-epsilon)*pai + (epsilon/in.getActions().size())), expPlayer);
            } else {
                ai = randomChoice(rmProbs, 1);
                pai=rmProbs[ai];
                u = iteration(in.getChildFor(in.getActions().get(ai)),
                        pi, pi_*pai, us*pai, expPlayer);
            }
        }
        
        //regret/mean strategy update
        double s = us;
        double c = x;
        x *= pai;
        
        if (is.getPlayer().equals(expPlayer)){
            data.updateRegret(ai, u*pi_/l, c, x);
        } else {
            data.getRMStrategy(rmProbs);
            data.updateMeanStrategy(rmProbs, pi_/s);
        }
        return u;
    }   
            

    
    private double[] rmProbs = new double[1000];
    private int randomChoice(double[] dArray, double sum){
        double r = rnd.nextDouble() * sum;
        for (int i=0;i< dArray.length;i++){
            if (r <= dArray[i]) return i;
            r -= dArray[i];
        }
        return -1;
    }
    
    
    @Override
    public void setCurrentIS(InformationSet is){
        throw new UnsupportedOperationException();
    }
    
    private Player getOpponent(){
        return rootNode.getGameState().getAllPlayers()[1-searchingPlayer.getId()];
    }
    
    private Player getNaturePlayer(){
        return rootNode.getGameState().getAllPlayers()[2];
    }
    
    
    public InnerNode getRootNode() {
        return rootNode;
    }

    @Override
    public Action runMiliseconds(int miliseconds, GameState gameState) {
        MCTSInformationSet is = rootNode.getAlgConfig().getInformationSetFor(gameState);
        if (is.getAllNodes().isEmpty()){
            InnerNode in = rootNode;
            in = (InnerNode) in.getChildFor(gameState.getSequenceFor(gameState.getAllPlayers()[0]).getLast());
            in = (InnerNode) in.getChildFor(gameState.getSequenceFor(gameState.getAllPlayers()[1]).getLast());
            if (in.getGameState().isPlayerToMoveNature()){
                in = (InnerNode) in.getChildFor(gameState.getSequenceFor(gameState.getAllPlayers()[2]).getLast());
            }
            is = rootNode.getAlgConfig().getInformationSetFor(gameState);
            is.setAlgorithmData(fact.createSelector(in.getActions()));
        }
        assert is.getAllNodes().size()==1;
        rootNode = is.getAllNodes().iterator().next();
        rootNode.setParent(null);
        Action a = runMiliseconds(miliseconds);
        if (gameState.getPlayerToMove().equals(searchingPlayer)){
            return a;
        } else {
            InnerNode child = (InnerNode) rootNode.getChildFor(rootNode.getActions().get(0));
            is = child.getInformationSet();
            Map<Action, Double> distribution = (new MeanStratDist()).getDistributionFor(is.getAlgorithmData());
            return Strategy.selectAction(distribution, rnd);
        }
    }
}
