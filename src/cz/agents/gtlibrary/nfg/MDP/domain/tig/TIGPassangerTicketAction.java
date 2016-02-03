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


package cz.agents.gtlibrary.nfg.MDP.domain.tig;

import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.nfg.MDP.implementations.MDPActionImpl;

/**
 *
 * @author viliam
 */
public class TIGPassangerTicketAction extends MDPActionImpl {

    Player player;
    boolean buy;

    public TIGPassangerTicketAction(Player player, boolean stay) {
        this.player = player;
        this.buy = stay;
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj))
            return false;
        TIGPassangerTicketAction other = (TIGPassangerTicketAction)obj;
        if (!this.player.equals(other.getPlayer())
                || this.buy != other.buy)
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        return buy ? 13 : 17;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public String toString() {
        return  buy ? "Buy" : "NoBuy";
    }
}
