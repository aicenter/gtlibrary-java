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


package cz.agents.gtlibrary.domain.simpleGeneralSum;

import cz.agents.gtlibrary.iinodes.PlayerImpl;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.Player;

/**
 * Created with IntelliJ IDEA.
 * User: kail
 * Date: 12/5/13
 * Time: 3:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class SimpleGSInfo implements GameInfo {

    final protected static int MAX_UTILITY = 10;
    final protected static int MAX_DEPTH = 1;
    final protected static int MAX_ACTIONS = 2;

    public static Player PL0 = new PlayerImpl(0);
    public static Player PL1 = new PlayerImpl(1);

    public static double[][] utilityMatrix = {{3,1},{1,3},{2,1},{0,0}};

    @Override
    public double getMaxUtility() {
        return MAX_UTILITY;
    }

    @Override
    public Player getFirstPlayerToMove() {
        return PL0;
    }

    @Override
    public Player getOpponent(Player player) {
        if (player.equals(PL0)) return PL1;
        else return PL0;
    }

    @Override
    public String getInfo() {
        return "Simple General Sum";
    }

    @Override
    public int getMaxDepth() {
        return MAX_DEPTH;
    }

    @Override
    public Player[] getAllPlayers() {
        return new Player[] {PL0, PL1};
    }

    @Override
    public double getUtilityStabilizer() {
        return 1;
    }
}
