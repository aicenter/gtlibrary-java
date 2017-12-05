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


package cz.agents.gtlibrary.domain.oshizumo;

import java.util.Arrays;

import cz.agents.gtlibrary.iinodes.PlayerImpl;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.Player;

public class OZGameInfo implements GameInfo {

    public static final Player FIRST_PLAYER = new PlayerImpl(0);
    public static final Player SECOND_PLAYER = new PlayerImpl(1);

    public static final Player[] ALL_PLAYERS = {FIRST_PLAYER, SECOND_PLAYER};
    public static long seed = 10;

    // parameters taken from http://skatgame.net/mburo/ps/sumo.pdf
    public static int startingCoins = 7;        // N from paper
    public static int locK = 3;                 // locations (2K+1)
    public static int minBid = 2;               // B from paper

    public static boolean BINARY_UTILITIES = true;
    public static boolean GENERAL_SUM = true;

    public OZGameInfo() {
    }

    @Override
    public double getMaxUtility() {
        if (BINARY_UTILITIES) {
            return 1;
        } else {
            return startingCoins + locK;
        }
    }

    @Override
    public Player getFirstPlayerToMove() {
        return FIRST_PLAYER;
    }

    @Override
    public Player getOpponent(Player player) {
        return player.equals(FIRST_PLAYER) ? SECOND_PLAYER : FIRST_PLAYER;
    }

    @Override
    public String getInfo() {
        return "OshiZumo, starting coins: " + startingCoins + ", locK: " + locK + ", minBid: " + minBid;
    }

    @Override
    public int getMaxDepth() {
        if (minBid == 0)
            return startingCoins * 2;
        else
            return startingCoins;
    }

    @Override
    public Player[] getAllPlayers() {
        return ALL_PLAYERS;
    }

    @Override
    public double getUtilityStabilizer() {
        return 1;
    }
}
