/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.agents.gtlibrary.algorithms.mcts.oos;

import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;

/**
 *
 * @author vilo
 */
public class ISTargeting extends OOSTargeting{

    public ISTargeting(Player[] allPlayers) {
        super(allPlayers);
    }

    @Override
    public void update(InformationSet curIS) {
        clear();
        addIStoTargeting(curIS);
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
    
}
    

