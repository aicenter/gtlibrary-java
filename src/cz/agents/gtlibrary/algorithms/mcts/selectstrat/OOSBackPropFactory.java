/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.agents.gtlibrary.algorithms.mcts.selectstrat;

import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.Node;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.HighQualityRandom;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Random;

/**
 *
 * @author vilo
 */
public class OOSBackPropFactory implements BackPropFactory  {
    InnerNode root;
    double gamma = 0.2;
    Random random = new HighQualityRandom();
    /** Each player's contribution to the probability of being in the current IS. */
    public double pi[];
    public ArrayDeque<Double> pis[] = new ArrayDeque[]{new ArrayDeque(),  new ArrayDeque()};
    public double s=1.0;
    public ArrayDeque<Double> ss = new ArrayDeque();
    public double x,l;
    public double us=1.0;
    public double bs=1.0;


    public OOSBackPropFactory(double gamma) {
        this.gamma = gamma;
        pi = new double[getRealPlayersNum()];
        for (int i=0; i<getRealPlayersNum(); i++) pi[i]=1.0;
    }

    @Override
    public SelectionStrategy createForIS(MCTSInformationSet infSet) {
        if (root==null) {
            root = infSet.getAllNodes().iterator().next();
            setCurrentIS(infSet);
        }
        if (infSet.getPlayer().getId() > 1) return null;
        return new OOSSelector(this, infSet);
    }

    @Override
    public SelectionStrategy createForNode(Node node) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    private int getRealPlayersNum(){
        return 2;
    }
    
    public void putPIs(){
        pis[0].add(pi[0]);
        pis[1].add(pi[1]);
    }
      
    public void putSs(){
        ss.add(bs);
        ss.add(us);
        if (ss.size()>100) System.out.print("ERROR!!! Ss stack too full.");
    }
    
    public void popPIs(){
        pi[0] = pis[0].removeLast();
        pi[1] = pis[1].removeLast();
    }
    
    public void popSs(){
        us = ss.removeLast();
        bs = ss.removeLast();
    }
    
    private MCTSInformationSet currentIS = null;
    public HashSet<Action> opponentAllowedActions = new HashSet();
    public int opponentMaxSequenceLength = 0;
    public void setCurrentIS(MCTSInformationSet is){
        currentIS = is;
        opponentAllowedActions.clear();
        for (GameState gs : currentIS.getAllStates()){
            Sequence s = gs.getSequenceFor(gs.getAllPlayers()[1-gs.getPlayerToMove().getId()]);
            opponentAllowedActions.addAll(s.getAsList());
            opponentMaxSequenceLength = Math.max(opponentMaxSequenceLength, s.size());
        }
    }
    
    public MCTSInformationSet getCurrentIS(){
        return currentIS;
    }
    
    public boolean sampleOnBiasPath = false;
    public double delta = 0.5;
    public boolean underTargetIs = false;
    public boolean biassedSample = false;
    public boolean isBiasedIteration(){
       return biassedSample;
       //return false;
    }
}
