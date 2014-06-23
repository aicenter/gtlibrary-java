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
import cz.agents.gtlibrary.iinodes.ArrayListSequenceImpl;
import cz.agents.gtlibrary.iinodes.LinkedListSequenceImpl;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.strategy.Strategy;
import cz.agents.gtlibrary.utils.HighQualityRandom;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;

    
/**
 *
 * @author vilo
 */
public class OOSAlgorithm implements GamePlayingAlgorithm {
    protected Player searchingPlayer;
    protected OOSSimulator simulator;
    protected BackPropFactory fact;
    protected InnerNode rootNode;
    protected ThreadMXBean threadBean;
    private double delta = 0.9;
    private double epsilon = 0.6;
    
    private MCTSInformationSet curIS;
    private Random rnd = new HighQualityRandom();

    public OOSAlgorithm(Player searchingPlayer, OOSSimulator simulator, GameState rootState, Expander expander) {
        this(searchingPlayer, simulator, rootState, expander, 0.9, 0.6);
    }
    
    public OOSAlgorithm(Player searchingPlayer, OOSSimulator simulator, GameState rootState, Expander expander, double delta, double epsilon) {
        this.searchingPlayer = searchingPlayer;
        this.simulator = simulator;
        this.delta = delta;
        this.epsilon = epsilon;
        if (rootState.isPlayerToMoveNature()) this.rootNode = new ChanceNode(expander, rootState);
        else this.rootNode = new InnerNode(expander, rootState);
        threadBean = ManagementFactory.getThreadMXBean();
        curIS = rootNode.getInformationSet();
    }
    
    @Override
    public Action runMiliseconds(int miliseconds){
        int iters=0;
        long start = threadBean.getCurrentThreadCpuTime();
        for (;(threadBean.getCurrentThreadCpuTime()-start)/1e6 < miliseconds;) {
            if (curIS!=rootNode.getInformationSet()) biasedIteration = (rnd.nextDouble()<=delta);
            underTargetIS = false;
            iteration(rootNode,1,1,1,1,rootNode.getGameState().getAllPlayers()[0]);
            iters++;
            underTargetIS = false;
            iteration(rootNode,1,1,1,1,rootNode.getGameState().getAllPlayers()[1]);
            iters++;
        }
        System.out.println();
        System.out.println("OOS Iters: " + iters);
        if (!curIS.getPlayer().equals(searchingPlayer)) return null;
        Map<Action, Double> distribution = (new MeanStratDist()).getDistributionFor(curIS.getAlgorithmData());
        return Strategy.selectAction(distribution, rnd);
    }
    
    public Action runIterations(int iterations){
        for (int i=0;i<iterations/2;i++) {
            if (curIS!=rootNode.getInformationSet()) biasedIteration = (rnd.nextDouble()<=delta);
            underTargetIS = false;
            iteration(rootNode,1,1,1,1,rootNode.getGameState().getAllPlayers()[0]);
            underTargetIS = false;
            iteration(rootNode,1,1,1,1,rootNode.getGameState().getAllPlayers()[1]);
        }
        if (!curIS.getPlayer().equals(searchingPlayer)) return null;
        Map<Action, Double> distribution = (new MeanStratDist()).getDistributionFor(curIS.getAlgorithmData());
        return Strategy.selectAction(distribution, rnd);
    }
    
    
    private boolean biasedIteration = false;
    private boolean underTargetIS = false;
    //additional iteration return values
    private double x=-1;
    private double l=-1;
    
    /** 
     * The main function for OOS iteration.
     * @param node current node
     * @param pi probability with which the searching player wants to reach the current node
     * @param pi_ probability with which the opponent of the searching player and chance want to reach the current node
     * @param bs probability that the current (possibly biased) sample reaches this node
     * @param us probability that the unbiased sample reaches this node
     * @param expPlayer the exploring player for this iteration
     * @return iteration game value is actually returned. Other return values are in global x and l
     */
    protected double iteration(Node node, double pi, double pi_, double bs, double us, Player expPlayer){
        if (node instanceof LeafNode) {
            x=1; l=delta*bs+(1-delta)*us;
            return ((LeafNode)node).getUtilities()[expPlayer.getId()];
        } 
        if (node instanceof ChanceNode) {
            ChanceNode cn = (ChanceNode)node;
            Action a; double p;
            if (biasedIteration && cn.getGameState().getSequenceFor(getNaturePlayer()).size() <= chanceMaxSequenceLength){
                double sum=0; int i=0;
                for (Action ai : cn.getActions()){
                    biasedProbs[i] = cn.getGameState().getProbabilityOfNatureFor(ai);
                    sum += biasedProbs[i];
                    i++;
                }
                assert sum>0;
                i = randomChoice(biasedProbs, sum);
                a = cn.getActions().get(i);
                p = biasedProbs[i]/sum;
            } else {
                a = cn.getRandomAction();
                p = cn.getGameState().getProbabilityOfNatureFor(a);
            }
            if (rootNode.getGameState().equals(cn.getGameState())) underTargetIS = true;
            double u=iteration(cn.getChildFor(a), pi, p*pi_, p*bs, cn.getGameState().getProbabilityOfNatureFor(a)*us, expPlayer);
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
            l = (delta*bs+(1-delta)*us)*simulator.playOutProb*(1.0/in.getActions().size());
        } else {
            data = (OOSAlgorithmData) is.getAlgorithmData();
            data.getRMStrategy(rmProbs);
            double biasedSum=0;
            int inBiasActions=0;
            biasedProbs = tmpProbs;
            if (bs > 0 && !underTargetIS) {
                if (curIS.equals(is)){
                    underTargetIS = true;
                    biasedProbs = rmProbs;
                    biasedSum = 1; inBiasActions=in.getActions().size();
                } else if (searchingPlayer.equals(is.getPlayer())){
                    //this IS is above the current IS, returning the action from the history
                    if (curIS.getPlayersHistory().size() > is.getPlayersHistory().size()){
                        selectedA = curIS.getPlayersHistory().get(is.getPlayersHistory().size());
                        ai = in.getActions().indexOf(selectedA);
                        Arrays.fill(biasedProbs,0,in.getActions().size(),-0.0);
                        biasedProbs[ai]=1;
                        biasedSum = 1; inBiasActions=1;
                    } else {
                        assert biasedIteration==false;
                    }
                } else {
                    if (is.getPlayersHistory().size() < opponentMaxSequenceLength){
                        int i=0;
                        for (Action a : in.getActions()){
                            if (opponentAllowedActions.contains(a)){
                                biasedSum += rmProbs[i];
                                biasedProbs[i] = rmProbs[i];
                                inBiasActions++;
                            } else biasedProbs[i] = -0.0;//negative zeros denote the banned actions
                            i++;
                        }
                    }
                }
                if (biasedSum==0) {//if all actions were not present for the opponnet or it was under the current IS
                    biasedProbs = rmProbs;
                    biasedSum=1; inBiasActions=in.getActions().size();
                }
            } else {
                biasedProbs = rmProbs;
                biasedSum = 1; inBiasActions=in.getActions().size();
            }
            
            if (is.getPlayer().equals(expPlayer)){
                if (!biasedIteration){
                    if (rnd.nextDouble() < epsilon) ai = rnd.nextInt(in.getActions().size());
                    else ai = randomChoice(rmProbs, 1);
                } else {
                    if (rnd.nextDouble() < epsilon){
                        if (inBiasActions==in.getActions().size()) ai = rnd.nextInt(inBiasActions);
                        else {
                            int j=rnd.nextInt(inBiasActions);
                            ai=0;//the following sets ai to the j-th allowed action
                            while(Double.compare(biasedProbs[ai],0.0) == -1 || j-->0) ai++;
                        }
                    } else ai = randomChoice(biasedProbs, biasedSum);
                }
                
                pai=rmProbs[ai];
                u = iteration(in.getChildFor(in.getActions().get(ai)),
                        pi*pai, pi_, 
                        //the following is zero for banned actions and the correct probability for allowed
                        bs*((Double.compare(biasedProbs[ai],0.0) == -1 ? 0 : (1-epsilon)*biasedProbs[ai]/biasedSum + (epsilon/inBiasActions))),
                        us*((1-epsilon)*pai + (epsilon/in.getActions().size())), expPlayer);
            } else {
                if (biasedIteration) ai = randomChoice(biasedProbs, biasedSum);
                else ai = randomChoice(rmProbs, 1);
                pai=rmProbs[ai];
                u = iteration(in.getChildFor(in.getActions().get(ai)),
                        pi, pi_*pai, bs*biasedProbs[ai]/biasedSum, us*pai, expPlayer);
            }
        }
        
        //regret/mean strategy update
        double s = delta*bs + (1-delta)*us;
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
    private double[] tmpProbs = new double[1000];
    private double[] biasedProbs = tmpProbs;
    private int randomChoice(double[] dArray, double sum){
        double r = rnd.nextDouble() * sum;
        for (int i=0;i< dArray.length;i++){
            if (r <= dArray[i]) return i;
            r -= dArray[i];
        }
        return -1;
    }
    
    
    public HashSet<Action> opponentAllowedActions = new HashSet();
    public HashSet<Action> chanceAllowedActions = new HashSet();
    public int opponentMaxSequenceLength = 0;
    public int chanceMaxSequenceLength = 0;
    @Override
    public void setCurrentIS(InformationSet is){
        curIS = (MCTSInformationSet) is;
        opponentAllowedActions.clear();
        chanceAllowedActions.clear();
        for (GameState gs : curIS.getAllStates()){
            Sequence s = gs.getSequenceFor(getOpponent());
            opponentAllowedActions.addAll(s.getAsList());
            opponentMaxSequenceLength = Math.max(opponentMaxSequenceLength, s.size());
            if (gs.getAllPlayers().length>2){
                s = gs.getSequenceFor(getNaturePlayer());
                chanceAllowedActions.addAll(s.getAsList());
                chanceMaxSequenceLength = Math.max(opponentMaxSequenceLength, s.size());
            }
        }
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
        //TODO: finish debuging this
        MCTSInformationSet is = rootNode.getAlgConfig().getInformationSetFor(gameState);
        if (is.getAllNodes().isEmpty()){
            Sequence[] seqs = new Sequence[gameState.getAllPlayers().length];
            for (Player pl : gameState.getAllPlayers()){
                seqs[pl.getId()] = new LinkedListSequenceImpl(gameState.getSequenceFor(pl));
            }
            InnerNode in = rootNode;
            while (!in.getGameState().equals(gameState)){
                in = (InnerNode) in.getChildFor(seqs[in.getGameState().getPlayerToMove().getId()].removeFirst());
                if (in.getInformationSet().getAlgorithmData() == null){
                    in.getInformationSet().setAlgorithmData(fact.createSelector(in.getActions()));
                }
            }
            is = rootNode.getAlgConfig().getInformationSetFor(gameState);
        }
        if (!gameState.getPlayerToMove().equals(searchingPlayer)){
            InnerNode in = is.getAllNodes().iterator().next();
            in = (InnerNode) in.getChildFor(in.getActions().get(0));
            setCurrentIS(in.getInformationSet());
        } else {
            setCurrentIS(rootNode.getAlgConfig().getInformationSetFor(gameState));
        }
        rootNode.setParent(null);
        return runMiliseconds(miliseconds);
    }
}
