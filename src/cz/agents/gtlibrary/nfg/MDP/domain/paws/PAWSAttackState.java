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

/**
 *
 * @author viliam
 */
package cz.agents.gtlibrary.nfg.MDP.domain.paws;

import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.nfg.MDP.implementations.MDPStateImpl;
import cz.agents.gtlibrary.nfg.MDP.interfaces.MDPAction;
import cz.agents.gtlibrary.nfg.MDP.interfaces.MDPState;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 *
 * @author viliam
 */
public class PAWSAttackState extends MDPStateImpl {
    boolean terminal = false;
    
    public PAWSAttackState(Player player) {
        super(player);
        assert (player.getId() == 1); //assert it is the attacker
    }


    public PAWSAttackState(PAWSAttackState state) {
        super(state.getPlayer());
    }

    @Override
    public MDPState performAction(MDPAction action) {
        throw new NotImplementedException();
    }

    @Override
    public MDPState copy() {
        MDPState result = new PAWSAttackState(this);
        return result;
    }

    @Override
    public boolean isTerminal() {
        return terminal;
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o))
            return false;
        PAWSAttackState other = (PAWSAttackState)o;
        if (!this.getPlayer().equals(other.getPlayer()))
             return false;
        if (this.terminal != other.terminal)
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        return terminal ? 0 : 1;
    }
    
    @Override
    public boolean isRoot() {
        return !terminal;
    }

    @Override
    public String toString() {StringBuilder sb = new StringBuilder();
        return terminal ? "AttLeaf" : "AttRoot";
    }

    @Override
    public int horizon() {//upper bound on the number of actions that will still be performed
        return 1;
    }
}
