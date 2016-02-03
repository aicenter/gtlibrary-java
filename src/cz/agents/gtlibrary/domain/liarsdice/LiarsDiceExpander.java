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
package cz.agents.gtlibrary.domain.liarsdice;

import java.util.ArrayList;
import java.util.List;

import cz.agents.gtlibrary.iinodes.ExpanderImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

public class LiarsDiceExpander<I extends InformationSet> extends ExpanderImpl<I> {

    private static final long serialVersionUID = -5389882092681466870L;

    public LiarsDiceExpander(AlgorithmConfig<I> algConfig) {
        super(algConfig);
    }

    @Override
    public List<Action> getActions(GameState gameState) {
        LiarsDiceGameState ldState = (LiarsDiceGameState) gameState;
        List<Action> actions = new ArrayList<Action>();

        if (ldState.getRound() < (LDGameInfo.P1DICE+LDGameInfo.P2DICE)) {
            addActionsOfNature(ldState, actions, getAlgorithmConfig().getInformationSetFor(gameState));
            return actions;
        }
        addActionsOfRegularPlayer(ldState, actions, getAlgorithmConfig().getInformationSetFor(gameState));
        return actions;
    }

    private void addActionsOfNature(LiarsDiceGameState ldState, List<Action> actions, I informationSet) {
        for (int c = 1; c <= LDGameInfo.FACES; c++) {
            actions.add(new LiarsDiceAction(c, informationSet, ldState.getPlayerToMove()));
        }
    }

    private void addActionsOfRegularPlayer(LiarsDiceGameState ldState, List<Action> actions, I informationSet) {

        if (!ldState.isGameEnd()) {
            if (ldState.getCurBid() == 0) {
                // if first move of first turn, don't allow calling bluff
                for (int b = 1; b <= (LDGameInfo.CALLBID - 1); b++) {
                    actions.add(new LiarsDiceAction(b, informationSet, ldState.getPlayerToMove()));
                }
            } else {
                // otherwise start at next bid up to calling bluff
                for (int b = ldState.getCurBid() + 1; b <= (LDGameInfo.CALLBID); b++) {
                    actions.add(new LiarsDiceAction(b, informationSet, ldState.getPlayerToMove()));
                }
            }
        }
    }
}
