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

import static cz.agents.gtlibrary.nfg.MDP.domain.tig.TIGExpander.MAINTAIN_PREDECESSORS;
import cz.agents.gtlibrary.nfg.MDP.domain.tig.TIGPassangerTypeAction;
import cz.agents.gtlibrary.nfg.MDP.domain.tig.TIGPatrolTicketsAction;
import cz.agents.gtlibrary.nfg.MDP.implementations.MDPExpanderImpl;
import cz.agents.gtlibrary.nfg.MDP.implementations.MDPStateActionMarginal;
import cz.agents.gtlibrary.nfg.MDP.implementations.MDPStrategy;
import cz.agents.gtlibrary.nfg.MDP.interfaces.MDPAction;
import cz.agents.gtlibrary.nfg.MDP.interfaces.MDPState;

import java.util.*;
import cz.agents.gtlibrary.NotImplementedException;

/**
 *
 * @author viliam
 */
public class PAWSExpander extends MDPExpanderImpl {
   
    @Override
    public List<MDPAction> getActions(MDPState state) {

        if (state.isTerminal())
            return new ArrayList<MDPAction>();
        
        if (state.isRoot()){ //LP workaround
            if (state.getPlayer().getId() == 0) {
                ArrayList<MDPAction> res = new ArrayList<>();
                PAWSPatrolState baseState = new PAWSPatrolState(state.getPlayer());
                baseState.distance = 0;
                res.add(new PAWSPatrolAction(state.getPlayer(), baseState, -1, -1, 0));
                return res;
            } else {
                return PAWSConfig.attackActions;
            }
        }
        
        if (state.getPlayer().getId() == 0) {
            List<MDPAction> actions = PAWSConfig.patrolMDP.get(state);
            if (actions == null){
                assert ((PAWSPatrolState)state).isBase();
                return new ArrayList<MDPAction>();
            }
            return actions;
        } else {
            return PAWSConfig.attackActions;
        }
        
       
    }
 
    PAWSAttackState attackTerminal = null;
    @Override
    public Map<MDPState, Double> getSuccessors(MDPStateActionMarginal sapair) {
        MDPState oldRoot = null;
        if (sapair.getState() instanceof MDPStrategy.MDPRootState){//workaroung for DO
            oldRoot = sapair.getState();
            if (sapair.getPlayer().getId() == 0){
                sapair = new MDPStateActionMarginal(new PAWSPatrolState(sapair.getPlayer()), sapair.getAction());
            } else {
                sapair = new MDPStateActionMarginal(new PAWSAttackState(sapair.getPlayer()), sapair.getAction());
            }
        }
        
        Map<MDPState, Double> result = new HashMap<MDPState, Double>();
        if (sapair.getState() instanceof PAWSPatrolState){
            PAWSPatrolAction a = (PAWSPatrolAction) sapair.getAction();
            result.put(a.to, 1.0);
        } else if (sapair.getState() instanceof PAWSAttackState) {
            if (attackTerminal == null){
                attackTerminal = (PAWSAttackState) sapair.getState().copy();
                attackTerminal.terminal = true;
            }
            result.put(attackTerminal, 1.0);
        }
        
        if (MAINTAIN_PREDECESSORS){
            if (oldRoot != null) sapair = new MDPStateActionMarginal(oldRoot, sapair.getAction());
            for (Map.Entry<MDPState, Double> en : result.entrySet()){
                Map<MDPStateActionMarginal, Double> preds = predecessors.get(en.getKey());
                if (preds==null) {
                    preds = new HashMap<>();
                    predecessors.put(en.getKey(), preds);
                }
                preds.put(sapair, en.getValue());
            }
        }
        
        return result;
    }
    
    public static boolean MAINTAIN_PREDECESSORS = false;
    private Map<MDPState,Map<MDPStateActionMarginal, Double>> predecessors = new HashMap<>();
    
    @Override
    public Map<MDPStateActionMarginal, Double> getPredecessors(MDPState state) {
        assert MAINTAIN_PREDECESSORS;
        return predecessors.get(state);
    }
}
