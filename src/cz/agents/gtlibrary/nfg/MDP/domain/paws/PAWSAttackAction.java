/*
Copyright 2016 Department of Computing Science, University of Alberta, Edmonton

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


package cz.agents.gtlibrary.nfg.MDP.domain.paws;

import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.nfg.MDP.implementations.MDPActionImpl;
import cz.agents.gtlibrary.nfg.MDP.interfaces.MDPState;

/**
 * 
 * @author viliam
 */
public class PAWSAttackAction extends MDPActionImpl {

    private Player player;
    int row;
    int col;

    public PAWSAttackAction(Player player, int row, int col) {
        this.player = player;
        this.row = row;
        this.col = col;
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj))
            return false;
        PAWSAttackAction other = (PAWSAttackAction)obj;
        if (!this.player.equals(other.getPlayer()))
            return false;
        if (this.row != other.row || this.col != other.col)
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        return (row << 16) | col;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public String toString() {
        return "Att(" + row + "," + col + ")";
    }
}
