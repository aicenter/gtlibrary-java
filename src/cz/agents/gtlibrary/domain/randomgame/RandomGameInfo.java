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


package cz.agents.gtlibrary.domain.randomgame;

import cz.agents.gtlibrary.iinodes.PlayerImpl;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.utils.HighQualityRandom;

import java.util.Random;


public class RandomGameInfo implements GameInfo {
    public static final Player FIRST_PLAYER = new PlayerImpl(0);
    public static final Player SECOND_PLAYER = new PlayerImpl(1);

    public static final Player[] ALL_PLAYERS = new Player[] {FIRST_PLAYER, SECOND_PLAYER};

    public static double CORRELATION = -1.0;//-0.9;// -1 for zero sum, 1 for identical utilities
    public static int MAX_DEPTH = 3;
    public static int MAX_BF = 2;
    public static int MAX_OBSERVATION = 2;
    public static int MAX_UTILITY = 100;
    public static boolean BINARY_UTILITY = false;
    public static boolean UTILITY_CORRELATION = false;
    public static int MAX_CENTER_MODIFICATION = 1;
    public static boolean FIXED_SIZE_BF = false;
    //    public static double KEEP_OBS_PROB = 0.9;
    public static int[] ACTIONS;

    public static long seed = 9;//13;

    public static Random rnd = new HighQualityRandom(seed);

    public RandomGameInfo() {
        rnd = new HighQualityRandom(seed);
        if (UTILITY_CORRELATION) {
            if (BINARY_UTILITY)
                MAX_UTILITY = 1;
            else
                MAX_UTILITY = 2*MAX_CENTER_MODIFICATION*MAX_DEPTH;
        }
        ACTIONS = new int[MAX_BF-1];
        for (int i=0; i<MAX_BF-1; i++) {
            ACTIONS[i]=i;
        }
    }

    public RandomGameInfo(int seed, int depth, int bf) {
        this.seed = seed;
        this.MAX_BF = bf;
        this.MAX_DEPTH = depth;
        rnd = new HighQualityRandom(seed);
        if (UTILITY_CORRELATION) {
            if (BINARY_UTILITY)
                MAX_UTILITY = 1;
            else
                MAX_UTILITY = 2*MAX_CENTER_MODIFICATION*MAX_DEPTH;
        }
        ACTIONS = new int[MAX_BF-1];
        for (int i=0; i<MAX_BF-1; i++) {
            ACTIONS[i]=i;
        }
    }

    @Override
    public double getMaxUtility() {
        return MAX_UTILITY;
    }

    @Override
    public Player getFirstPlayerToMove() {
        return FIRST_PLAYER;
    }

    @Override
    public Player getOpponent(Player player) {
        if (player.equals(FIRST_PLAYER))
            return SECOND_PLAYER;
        return FIRST_PLAYER;
    }

    @Override
    public String getInfo() {
        return "Random game:\nMAX_UTILITY:" + MAX_UTILITY + ", MAX_BF:" + MAX_BF + ", MAX_OBSERVATIONS:" + MAX_OBSERVATION + ", MAX_DEPTH:" + MAX_DEPTH + ", BIN_UTIL:" + BINARY_UTILITY + ", UTIL_CORR:" + UTILITY_CORRELATION + ", CORR:" + CORRELATION + ", SEED:" + seed;
    }

    @Override
    public int getMaxDepth() {
        return MAX_DEPTH;
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
