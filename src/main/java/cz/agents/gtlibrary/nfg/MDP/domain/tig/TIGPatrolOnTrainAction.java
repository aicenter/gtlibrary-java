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

/**
 *
 * @author viliam
 */
package cz.agents.gtlibrary.nfg.MDP.domain.tig;

import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.nfg.MDP.implementations.MDPActionImpl;

public class TIGPatrolOnTrainAction extends MDPActionImpl {

    Player player;
    boolean stay;

    public TIGPatrolOnTrainAction(Player player, boolean stay) {
        this.player = player;
        this.stay = stay;
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj))
            return false;
        TIGPatrolOnTrainAction other = (TIGPatrolOnTrainAction)obj;
        if (!this.player.equals(other.getPlayer())
                || this.stay != other.stay)
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        return stay ? 13 : 17;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public String toString() {
        return  stay ? "Stay" : "Leave";
    }
}
