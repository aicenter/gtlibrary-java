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


package cz.agents.gtlibrary.nfg.MDP.domain.transitgame;

import cz.agents.gtlibrary.iinodes.PlayerImpl;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.nfg.MDP.implementations.MDPConfigImpl;
import cz.agents.gtlibrary.nfg.MDP.implementations.MDPStateActionMarginal;
import cz.agents.gtlibrary.nfg.MDP.interfaces.MDPState;
import cz.agents.gtlibrary.utils.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: bosansky
 * Date: 9/16/13
 * Time: 10:00 AM
 * To change this template use File | Settings | File Templates.
 */
public class TGConfig extends MDPConfigImpl{

//      public static int MAX_TIME_STEP = 10;
//      public static int LENGTH_OF_GRID = 6;
//      public static int WIDTH_OF_GRID = 4;

    public static boolean rememberHistory = false;

    public static double UTILITY_MULTIPLIER = 1;
//
//    public static int MAX_TIME_STEP = 7;
//    public static int LENGTH_OF_GRID = 5;
//    public static int WIDTH_OF_GRID = 3;
    public static int MAX_TIME_STEP = 14;
    public static int LENGTH_OF_GRID = 10;
    public static int WIDTH_OF_GRID = 5;
//    public static int MAX_TIME_STEP = 24;
//    public static int LENGTH_OF_GRID = 22;
//    public static int WIDTH_OF_GRID = 11;

    final protected static int PATROLLERS = 1;
    protected static int[] PATROLLER_BASES;
    public static boolean useUncertainty = true;
    public static double MOVEMENT_UNCERTAINTY = 0.1;
    public static boolean SHUFFLE = false;
    public static int SHUFFLE_ID = 0;
    public static double PATROLLER_NOT_RETURNED_PENALTY = 1000d;

    public TGConfig() {
        allPlayers = new ArrayList<Player>(2);
        allPlayers.add(new PlayerImpl(0));
        allPlayers.add(new PlayerImpl(1));
        PATROLLER_BASES = new int[] {Math.max(LENGTH_OF_GRID/2-1,0)};
        String shuffle = System.getProperty("ACTION_SHUFFLE");
        if (shuffle != null){
            SHUFFLE = true;
            SHUFFLE_ID = Integer.parseInt(shuffle);
        }
    }

    @Override
    public double getUtility(MDPStateActionMarginal firstPlayerAction, MDPStateActionMarginal secondPlayerAction) {
        assert (firstPlayerAction.getPlayer().getId() != secondPlayerAction.getPlayer().getId());
        if (firstPlayerAction.getState().isRoot() || secondPlayerAction.getState().isRoot())
            return 0;

        Double result = 0d;

        TGAction attAction;
        TGAction defAction;
        TGState attState;
        TGState defState;

        if (firstPlayerAction.getPlayer().getId() == 0) {
            attAction = (TGAction)firstPlayerAction.getAction();
            attState = (TGState)firstPlayerAction.getState();
            defAction = (TGAction)secondPlayerAction.getAction();
            defState = (TGState)secondPlayerAction.getState();
        } else {
            defAction = (TGAction)firstPlayerAction.getAction();
            defState = (TGState)firstPlayerAction.getState();
            attAction = (TGAction)secondPlayerAction.getAction();
            attState = (TGState)secondPlayerAction.getState();
        }

        if (rememberHistory) {
            if (defState.getTimeStep() != attState.getTimeStep() || attState.getTimeStep()+1 < MAX_TIME_STEP)
                result = 0d;
            else {
                if ((attState.getCol()[0] == defState.getCol()[0] && attState.getRow()[0] == defState.getRow()[0]) ||
                        (attAction.getTargetCol()[0] == defState.getCol()[0] && attAction.getTargetRow()[0] == defState.getRow()[0] && attState.getCol()[0] == defAction.getTargetCol()[0] && attState.getRow()[0] == defAction.getTargetRow()[0]) ||
                        (attAction.getTargetCol()[0] == defAction.getTargetCol()[0] && attAction.getTargetRow()[0] == defAction.getTargetRow()[0])
                        ) {
                    result = -1d;
                } else {
                    Pair<int[], int[]> defPosTp1 = new Pair<int[], int[]>(defState.getCol(), defState.getRow());
                    Pair<int[], int[]> attPosTp1 = new Pair<int[], int[]>(attState.getCol(), attState.getRow());
                    for (int t=defState.getTimeStep()-1; t>=0; t--) {
                        Pair<int[], int[]> defPosT = defState.getHistory().get(t);
                        Pair<int[], int[]> attPosT = attState.getHistory().get(t+1);
                        if (Arrays.equals(defPosT.getLeft(), attPosT.getLeft()) && Arrays.equals(defPosT.getRight(), attPosT.getRight())) {
                            result = -1d;
                            break;
                        } else if (Arrays.equals(defPosT.getLeft(), attPosTp1.getLeft()) && Arrays.equals(defPosT.getRight(), attPosTp1.getRight()) &&
                                   Arrays.equals(defPosTp1.getLeft(), attPosT.getLeft()) && Arrays.equals(defPosTp1.getRight(), attPosT.getRight())) {
                            result = -1d;
                            break;
                        }
                        defPosTp1 = defPosT;
                        attPosTp1 = attPosT;
                    }
                }
                if (result > -1d && attAction.getTargetCol()[0] == TGConfig.LENGTH_OF_GRID - 1) {
                    result = 1d;
                    result = result - attState.getTimeStep() * 0.01;
                }
            }
            return result;
        }

        if (defState.getTimeStep() != attState.getTimeStep())
            result = 0d;
        else if ((attState.getCol()[0] == defState.getCol()[0] && attState.getRow()[0] == defState.getRow()[0]) ||
                 (attAction.getTargetCol()[0] == defState.getCol()[0] && attAction.getTargetRow()[0] == defState.getRow()[0] && attState.getCol()[0] == defAction.getTargetCol()[0] && attState.getRow()[0] == defAction.getTargetRow()[0]) ||
                 (attAction.getTargetCol()[0] == defAction.getTargetCol()[0] && attAction.getTargetRow()[0] == defAction.getTargetRow()[0])
                )
            result = -1d*UTILITY_MULTIPLIER;
        else if (attAction.getTargetCol()[0] == TGConfig.LENGTH_OF_GRID - 1) {
//            result = 20d;
//            result = result - attState.getTimeStep() * 2.0;
            result = 1d*UTILITY_MULTIPLIER;
            result = result - attState.getTimeStep() * 0.02;
//            result = UTILITY_MULTIPLIER+1d;
//            result = result - attState.getTimeStep() * (0.01 + UTILITY_MULTIPLIER/100d);
        } else if (defState.getTimeStep()+1 == getMaxTimeStep() && !((defState.getRow()[0] == 0 && defState.getCol()[0] != TGConfig.PATROLLER_BASES[0]) || (defAction.getTargetCol()[0] == TGConfig.PATROLLER_BASES[0] && defAction.getTargetRow()[0] == 0))) {
            result = PATROLLER_NOT_RETURNED_PENALTY;
        }
        return result;
    }

    @Override
    public double getBestUtilityValue(Player player) {
        if (player.getId() == 0) {
            return 1d*UTILITY_MULTIPLIER;
        } else {
            return -1d*UTILITY_MULTIPLIER;
        }
    }

    @Override
    public MDPState getDomainRootState(Player player) {
        return new TGState(player);
    }

    public static int getMaxTimeStep() {
        return MAX_TIME_STEP;
    }

    @Override
    public String toString() {
        return "TG" + WIDTH_OF_GRID + "x" + LENGTH_OF_GRID + "|" + MAX_TIME_STEP + "; Penalty=" +
          PATROLLER_NOT_RETURNED_PENALTY + "; Uncertainty=" + (useUncertainty ? MOVEMENT_UNCERTAINTY : "NA") + "; "
                + (rememberHistory ? "Full Game" : "");
    }
    
    
}
