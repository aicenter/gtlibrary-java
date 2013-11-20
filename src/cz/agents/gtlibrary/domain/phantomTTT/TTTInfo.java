/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.agents.gtlibrary.domain.phantomTTT;

import cz.agents.gtlibrary.iinodes.PlayerImpl;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.Player;


/**
 *
 * @author vilo
 */
public class TTTInfo implements GameInfo{

    public static boolean useDomainDependentExpander = true;

    public static final Player XPlayer = new PlayerImpl(0,"x");
    public static final Player OPlayer = new PlayerImpl(1,"o");
    
    public static Player[] players = new Player[] { XPlayer, OPlayer};
    
    @Override
    public Player getFirstPlayerToMove() {
        return XPlayer;
    }

    @Override
    public int getMaxDepth() {
        return Integer.MAX_VALUE;
        //return 2;
    }

    @Override
    public String getInfo() {
        return "************ Phantom Tic Tac Toe ************* \n ";
    }

    @Override
    public double getMaxUtility() {
        return 1.0;
    }

    @Override
    public Player getOpponent(Player player) {
        return player.equals(XPlayer) ? OPlayer : XPlayer;
    }

    @Override
    public Player[] getAllPlayers() {
        return players;
    }
    
}
