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
import org.apache.commons.lang3.builder.HashCodeBuilder;


public class TIGPatrolStopAction extends MDPActionImpl {

    Player player;
    int fromTime;
    int toTime;
    int stop;
    
    private int hash;
    private boolean changed = true;

    public TIGPatrolStopAction(Player player, int fromTime, int toTime, int stop) {
        this.player = player;
        this.fromTime = fromTime;
        this.toTime = toTime;
        this.stop = stop;
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj))
            return false;
        TIGPatrolStopAction other = (TIGPatrolStopAction)obj;
        if (!this.player.equals(other.getPlayer())
                || this.toTime != other.toTime
                || this.fromTime != other.fromTime 
                || this.stop != other.stop)
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        if (changed) {
            HashCodeBuilder hb = new HashCodeBuilder(31, 17);
            hb.append(player);
            hb.append(fromTime);
            hb.append(toTime);
            hb.append(stop);
            hash = hb.toHashCode();
            changed = false;
        }
        return hash;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public String toString() {
        return "Stop" + stop + ":" + fromTime + "-" + toTime;
    }
}
