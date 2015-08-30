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


package cz.agents.gtlibrary.nfg.MDP.domain.randomgame;

import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.nfg.MDP.implementations.MDPActionImpl;
import cz.agents.gtlibrary.nfg.MDP.interfaces.MDPState;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Created with IntelliJ IDEA.
 * User: bosansky
 * Date: 9/12/13
 * Time: 2:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class RGMDPAction extends MDPActionImpl {

    private Player player;
    private int ID = 0; // {0, .., BF-1}

    private int hash;
    private boolean changed = true;

    public RGMDPAction(Player player, int ID) {
        this.player = player;
        this.ID = ID;
    }

    @Override
    public int hashCode() {
        if (changed) {
            HashCodeBuilder hb = new HashCodeBuilder(31, 17);
            hb.append(player);
            hb.append(ID);
            hash = hb.toHashCode();
            changed = false;
        }
        return hash;
    }

    public Player getPlayer() {
        return player;
    }

    public int getID() {
        return ID;
    }

    @Override
    public String toString() {
        return "RGA_"+getPlayer()+"_ID:"+ID;
    }
}
