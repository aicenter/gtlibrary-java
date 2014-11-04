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

package cz.agents.gtlibrary.algorithms.mcts.oos;

import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNode;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.History;
import cz.agents.gtlibrary.interfaces.InformationSet;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.PublicAction;

/**
 *
 * @author vilo
 */
public class PSTargeting implements OOSTargeting {
    History sampleHist;
    
    @Override
    public boolean isAllowedAction(InnerNode node, Action action) {
        if (!(action instanceof PublicAction)) return true;
        Player pl = node.getGameState().getPlayerToMove();
        if (node.getGameState().getSequenceFor(pl).size() >= sampleHist.getSequenceOf(pl).size()) return true; //all actions are allowed here
        Action histAct = sampleHist.getSequenceOf(pl).get(node.getGameState().getSequenceFor(pl).size());//in other games, this might need to be more sophisticated
        return ((PublicAction)action).publicEquals(histAct);
    }

    @Override
    public void update(InformationSet curIS) {
        sampleHist = curIS.getAllStates().iterator().next().getHistory();
    }

    @Override
    public String toString() {
        return "PST(" + sampleHist + ')';
    }
    
}
