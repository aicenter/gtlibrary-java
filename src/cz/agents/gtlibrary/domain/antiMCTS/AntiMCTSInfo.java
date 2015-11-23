/*
Copyright 2014 Faculty of Electrical Engineering at CTU in Prague

This file is part of Game Theoretic Library.

Game Theoretic Library is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Game Theoretic Library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with Game Theoretic Library.  If not, see <http://www.gnu.org/licenses/>.*/


package cz.agents.gtlibrary.domain.antiMCTS;

import cz.agents.gtlibrary.domain.phantomTTT.*;
import cz.agents.gtlibrary.iinodes.PlayerImpl;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.Player;


/**
 * A single player game in which the optimal path is to always play right (utility 1),
 * but the player is deceived by payoffs to play left. The linear payoffs version is very
 * similar to Coquelin, Munos:Bandit algorithms for tree search. UAI 2007.
 * @author vilo
 */
public class AntiMCTSInfo implements GameInfo{
    public static final Player realPlayer = new PlayerImpl(0);
    public static final Player noopPlayer = new PlayerImpl(1);
    public static int gameDepth=5;
    public static boolean exponentialRewards=true;//otherwice linear
    
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

    @Override
    public double getUtilityStabilizer() {
        return 1;
    }
}
