/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.agents.gtlibrary.domain.antiMCTS;

import cz.agents.gtlibrary.domain.phantomTTT.*;
import cz.agents.gtlibrary.iinodes.PlayerImpl;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.Player;


/**
 * A single player game in which the optimal path is to always play right (utility 1),
 * but the player is deceived by payoffs to play left.
 * @author vilo
 */
public class AntiMCTSInfo implements GameInfo{
    public static final Player realPlayer = new PlayerImpl(0);
    public static final Player noopPlayer = new PlayerImpl(1);
    public static final int gameDepth=5;
    
    public static Player[] players = new Player[] { realPlayer, noopPlayer};
    
    @Override
    public Player getFirstPlayerToMove() {
        return realPlayer;
    }

    @Override
    public int getMaxDepth() {
        return Integer.MAX_VALUE;
    }

    @Override
    public String getInfo() {
        return "************ Anti-MCTS bad case synthetic game ************* \n ";
    }

    @Override
    public double getMaxUtility() {
        return 1.0;
    }

    @Override
    public Player getOpponent(Player player) {
        return player.equals(realPlayer) ? noopPlayer : realPlayer;
    }

    @Override
    public Player[] getAllPlayers() {
        return players;
    }
    
}
