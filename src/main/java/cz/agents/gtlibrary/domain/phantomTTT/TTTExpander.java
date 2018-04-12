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


package cz.agents.gtlibrary.domain.phantomTTT;

import cz.agents.gtlibrary.iinodes.ExpanderImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class TTTExpander<I extends cz.agents.gtlibrary.interfaces.InformationSet> extends ExpanderImpl<I> {

    private static final long serialVersionUID = 4771002747682438516L;

    public TTTExpander(AlgorithmConfig<I> algConfig) {
        super(algConfig);
    }

    /**
     * TODO: * add forced moves to heuristic!!!
     * * return only winning move in case of clear win
     * * return only saving move if it is known
     */
    @Override
    public List<Action> getActions(GameState gameState) {
        TTTState s = (TTTState) gameState;
        InformationSet curIS = getAlgorithmConfig().getInformationSetFor(gameState);
        ArrayList<Action> actions = new ArrayList<Action>();

        byte[] allowed;
        if (TTTState.forceFirstMoves) {
            if (s.moveNum == 0)
                allowed = new byte[]{4};//x
            else if (s.moveNum == 1)
                allowed = new byte[]{4};//o
//                else if (s.moveNum == 2) allowed = new byte[]{0};//o
//                else if (s.moveNum == 3) allowed = new byte[]{0};//x
//                else if (s.moveNum == 4) allowed = new byte[]{8};//x
//                else if (s.moveNum == 5) allowed = new byte[]{8};//o
//                else if (s.moveNum == 6) allowed = new byte[]{6,1};//o
//                else if (s.moveNum == 7) allowed = new byte[]{6,1};//x
//                else if (s.moveNum == 8) allowed = new byte[]{3};//x
//                else if (s.moveNum == 9) allowed = new byte[]{3};//o
//                else if (s.moveNum == 10) allowed = new byte[]{5,1};//o
            else
                allowed = new byte[]{4, 0, 8, 6, 2, 1, 3, 7, 5};
        } else {
            allowed = new byte[]{4, 0, 8, 6, 2, 1, 3, 7, 5};
        }
//                allowed = new byte[]{0,1,2,3,4,5,6,7,8};
        for (byte i : allowed) {
            if (!s.getTried(s.toMove, i)) {
                if (isWinningMove(i, s)) {
                    actions.clear();
                    actions.add(new TTTAction(i, curIS));
                    break;
//                            actions.add(actions.get(lastNotWinning));
//                            actions.set(lastNotWinning, new Action(s.moveNum,i));
//                            lastNotWinning++;
                } else {
                    actions.add(new TTTAction(i, curIS));
                }
            }
        }
        if (!TTTInfo.useDomainDependentExpander)
            Collections.shuffle(actions, new Random(1)); // uncomment if the domain-dependent should not be used
        return actions;
    }

    boolean isWinningMove(byte f, TTTState s) {
        char c = s.toMove;
        switch (f) {
            case 0:
                if (s.getSymbol(1) == c && s.getSymbol(2) == c
                        || s.getSymbol(3) == c && s.getSymbol(6) == c) return true;
                else return false;
            case 1:
                if (s.getSymbol(0) == c && s.getSymbol(2) == c
                        || s.getSymbol(4) == c && s.getSymbol(7) == c) return true;
                else return false;
            case 2:
                if (s.getSymbol(0) == c && s.getSymbol(1) == c
                        || s.getSymbol(5) == c && s.getSymbol(8) == c) return true;
                else return false;
            case 3:
                if (s.getSymbol(0) == c && s.getSymbol(6) == c
                        || s.getSymbol(4) == c && s.getSymbol(5) == c) return true;
                else return false;
            case 4:
                if (s.getSymbol(1) == c && s.getSymbol(7) == c
                        || s.getSymbol(3) == c && s.getSymbol(5) == c
                        || s.getSymbol(0) == c && s.getSymbol(8) == c
                        || s.getSymbol(2) == c && s.getSymbol(6) == c) return true;
                else return false;
            case 5:
                if (s.getSymbol(2) == c && s.getSymbol(8) == c
                        || s.getSymbol(3) == c && s.getSymbol(4) == c) return true;
                else return false;
            case 6:
                if (s.getSymbol(0) == c && s.getSymbol(3) == c
                        || s.getSymbol(7) == c && s.getSymbol(8) == c) return true;
                else return false;
            case 7:
                if (s.getSymbol(1) == c && s.getSymbol(4) == c
                        || s.getSymbol(6) == c && s.getSymbol(8) == c) return true;
                else return false;
            case 8:
                if (s.getSymbol(2) == c && s.getSymbol(5) == c
                        || s.getSymbol(6) == c && s.getSymbol(7) == c) return true;
                else return false;
        }
        assert false;
        return false;
    }
}
