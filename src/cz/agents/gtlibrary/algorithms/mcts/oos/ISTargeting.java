/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.agents.gtlibrary.algorithms.mcts.oos;

import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.distribution.Distribution;
import cz.agents.gtlibrary.algorithms.mcts.distribution.MeanStratDist;
import cz.agents.gtlibrary.algorithms.mcts.nodes.ChanceNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNode;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 *
 * @author vilo
 */
public class ISTargeting implements OOSTargeting{
    InnerNode rootNode;
    double delta;
    final private HashMap<Player, HashSet<Action>> playerAllowedActions = new HashMap<>();
    final private HashSet<Sequence> chanceAllowedSequences = new HashSet<>();//only because chance action have IS set to null 
    
    final private HashMap<Player, Integer> maxSequenceLength = new HashMap<>();
    private int chanceMaxSequenceLength = 0;
    
    public ISTargeting(InnerNode rootNode, double delta) {
        this.rootNode = rootNode;
        this.delta = delta;
        for (Player p : rootNode.getGameState().getAllPlayers()){
            playerAllowedActions.put(p, new HashSet<Action>());
            maxSequenceLength.put(p, 0);
        }
    }

    protected void clear(){
        for (HashSet s : playerAllowedActions.values()) s.clear();
        for (Player p : maxSequenceLength.keySet()) maxSequenceLength.put(p, 0);
        chanceAllowedSequences.clear();
        chanceMaxSequenceLength = 0;
    }
    
    protected void addChanceSequenceWithPrefixes(Sequence seq){
        assert seq.getPlayer().getId()==2;
        if (seq.size() > chanceMaxSequenceLength) chanceMaxSequenceLength=seq.size();
        chanceAllowedSequences.addAll(seq.getAllPrefixes());
    }
    
    protected void addPlayersAction(Action a){
        final Player p = a.getInformationSet().getPlayer();
        playerAllowedActions.get(p).add(a);
        if (((MCTSInformationSet)a.getInformationSet()).getPlayersHistory().size() > maxSequenceLength.get(p))
            maxSequenceLength.put(p, ((MCTSInformationSet)a.getInformationSet()).getPlayersHistory().size());
    }
    
    
    @Override
    public boolean isAllowedAction(InnerNode node, Action action){
        if (node instanceof ChanceNode){
            final Player chancePlayer = node.getGameState().getPlayerToMove();
            final Sequence seq = ((ChanceNode)node).getChildFor(action).getGameState().getSequenceFor(chancePlayer);
            if (seq.size()>chanceMaxSequenceLength) return false;
            return chanceAllowedSequences.contains(((ChanceNode)node).getChildFor(action).getGameState().getSequenceFor(chancePlayer));
        } else {
            final Player pl = node.getInformationSet().getPlayer();
            if (node.getInformationSet().getPlayersHistory().size() > maxSequenceLength.get(pl)) return false;
            return playerAllowedActions.get(pl).contains(action);
        }
    }
    
    @Override
    public void update(InformationSet curIS) {
        clear();
        addIStoTargeting(curIS);
        
        bsSum = 0; usSum = 0;
        updateSampleProbsRec(curIS, rootNode, 1, 1);
        probMultiplayer = delta*bsSum/usSum + (1-delta);
    }
    
    MeanStratDist msd = new MeanStratDist();
    double bsSum,usSum;
    private void updateSampleProbsRec(InformationSet curIS, InnerNode n, double us, double bs){
        if (curIS.equals(n.getInformationSet())){
            bsSum += bs; usSum += us;
        } else {
            double biasedSum=0;
            final boolean nMove = n.getGameState().isPlayerToMoveNature();
            Map<Action,Double> strat = null;
            if (!nMove) strat = msd.getDistributionFor(n.getInformationSet().getAlgorithmData());
            for (Action a : n.getActions()){
                if (isAllowedAction(n, a)) biasedSum += nMove ? n.getGameState().getProbabilityOfNatureFor(a) : strat.get(a);
            }
            for (Action a : n.getActions()){
                if (isAllowedAction(n, a)) {
                    InnerNode next = (InnerNode) n.getChildOrNull(a);
                    final double pa = nMove ? n.getGameState().getProbabilityOfNatureFor(a) : strat.get(a);
                    if (next != null) updateSampleProbsRec(curIS, next, us*pa, bs*pa/biasedSum);
                }
            }
        }
    }
    
    final protected void addIStoTargeting(InformationSet is){
        for (GameState gs : is.getAllStates()){
            addStateToTargeting(gs);
        }
    }
    
    final protected void addStateToTargeting(GameState gs){
        for (Sequence seq : gs.getHistory().values()){
            if (seq.getPlayer().getId()==2){
                addChanceSequenceWithPrefixes(seq);
            } else {
                for (Action a : seq) addPlayersAction(a);
            }
        }
    }

     private double probMultiplayer = 1;
    @Override
    public double getSampleProbMultiplayer() {
        return probMultiplayer;
    }
    
}
    

