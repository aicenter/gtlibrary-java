/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.agents.gtlibrary.algorithms.mcts.oos;

import cz.agents.gtlibrary.algorithms.mcts.nodes.ChanceNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNode;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.InformationSet;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author vilo
 */
public abstract class OOSTargeting {
    
    final private HashMap<Player, HashSet<Action>> playerAllowedActions = new HashMap<>();
    final private HashSet<Sequence> chanceAllowedSequences = new HashSet<>();//only because chance action have IS set to null 
    
    final private HashMap<Player, Integer> maxSequenceLength = new HashMap<>();
    private int chanceMaxSequenceLength = 0;

    public OOSTargeting(Player[] allPlayers) {
        for (Player p : allPlayers){
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
        if (a.getInformationSet().getPlayersHistory().size() > maxSequenceLength.get(p))
            maxSequenceLength.put(p, a.getInformationSet().getPlayersHistory().size());
    }
    
    
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
    
    public abstract void update(InformationSet curIS);
}
