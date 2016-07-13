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
 * Ticket inspection game patrol state.
 * @author viliam
 */
public class PAWSPatrolState extends MDPStateImpl {    
    private int nodeID;
    int distance;
    

    private int hash;
    private boolean changed = true;

    public PAWSPatrolState(Player player) {
        super(player);
        nodeID = PAWSConfig.BASE_ID;
        distance = -1;
        assert (player.getId() == 0); //assert it is the patrol
    }


    public PAWSPatrolState(PAWSPatrolState state) {
        super(state.getPlayer());
        this.nodeID = state.nodeID;
        this.distance = state.distance;
        this.hash = state.hash;
        this.changed = false;
    }

    @Override
    public MDPState performAction(MDPAction action) {
        throw new NotImplementedException();
    }

    @Override
    public MDPState copy() {
        MDPState result = new PAWSPatrolState(this);
        return result;
    }

    @Override
    public boolean isTerminal() {
        assert (PAWSConfig.MAX_DISTANCE == 16000);
        if (nodeID == PAWSConfig.BASE_ID && distance > 15150) return true; //necessary for DO, not for LP
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o))
            return false;
        PAWSPatrolState other = (PAWSPatrolState)o;
        if (!this.getPlayer().equals(other.getPlayer()))
             return false;
        if (this.nodeID != other.nodeID || this.distance != other.distance)
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        if (changed) {
            hash = (distance << 16) | nodeID;
            changed = false;
        }
        return hash;
    }
    
    @Override
    public boolean isRoot() {
        return distance == -1;
    }

    @Override
    public String toString() {StringBuilder sb = new StringBuilder();
        if (distance==0) return "Root";
        return "Pat(" + nodeID + "," + distance + ")";
    }
    
    public void set(int nodeID, int distance){
        this.nodeID = nodeID;
        this.distance = distance;
        this.changed = true;
    }
    
    public boolean isBase(){
        return nodeID == PAWSConfig.BASE_ID;
    }

    @Override
    public int horizon() {//upper bound on the number of actions that will still be performed
        return (PAWSConfig.MAX_DISTANCE - distance)/100;
    }
}
