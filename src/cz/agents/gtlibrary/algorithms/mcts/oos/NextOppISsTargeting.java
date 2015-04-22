/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.agents.gtlibrary.algorithms.mcts.oos;

import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNode;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;
import cz.agents.gtlibrary.interfaces.Player;
import java.util.ArrayDeque;
import java.util.List;

/**
 * This is non-generic public subtree targeting implementation.
 * Instead of targeting the public subtree, it targets all information sets of the opponent 
 * that can follow after the current information set. Hopefully, this is the same portion 
 * of the tree for Liar's Dice and Generic Poker.
 * @author vilo
 */
public class NextOppISsTargeting extends ISTargeting{
    Expander expander;

    public NextOppISsTargeting(InnerNode rootNode, double delta, Expander expander) {
        super(rootNode,delta);
        this.expander = expander;
    }
    
    @Override
    public void update(InformationSet curIS) {
        clear();
        Player pl = curIS.getPlayer();
        
        ArrayDeque<GameState> q = new ArrayDeque<>();
        q.addAll(curIS.getAllStates());
        
        while (!q.isEmpty()){
            GameState gs = q.removeFirst();
            if (gs.isPlayerToMoveNature() || gs.getPlayerToMove().equals(pl)){
                for (Action a : (List<Action>) expander.getActions(gs)){
                    q.add(gs.performAction(a));
                }
            } else {
                addIStoTargeting(expander.getAlgorithmConfig().getInformationSetFor(gs));
            }
        }
    }
}
    

