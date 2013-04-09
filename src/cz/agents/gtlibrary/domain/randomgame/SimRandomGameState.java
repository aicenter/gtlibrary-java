/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.agents.gtlibrary.domain.randomgame;

import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;

/**
 *
 * @author vilo
 */
public class SimRandomGameState extends RandomGameState {

    public SimRandomGameState(SimRandomGameState gameState) {
        super(gameState);
    }

    public SimRandomGameState() {
        super();
    }
    
    @Override
    protected void generateObservations(int newID, RandomGameAction action) {
        Player[] allPlayers = getAllPlayers();
        if (getPlayerToMove().equals(allPlayers[1])){
            observations.get(allPlayers[0]).add(action.getOrder());
            observations.get(allPlayers[1]).add(((RandomGameAction)getHistory().getSequenceOf(allPlayers[0]).getLast()).getOrder());
        }
        switchPlayers();
    }

    @Override
    public GameState copy() {
        return new SimRandomGameState(this);
    }

    @Override
    public String toString() {
        return "GS" + getHistory();
    }
}
