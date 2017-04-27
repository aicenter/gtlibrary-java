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

import cz.agents.gtlibrary.nfg.MDP.implementations.MDPExpanderImpl;
import cz.agents.gtlibrary.nfg.MDP.implementations.MDPStateActionMarginal;
import cz.agents.gtlibrary.nfg.MDP.implementations.MDPStrategy;
import cz.agents.gtlibrary.nfg.MDP.interfaces.MDPAction;
import cz.agents.gtlibrary.nfg.MDP.interfaces.MDPState;
import cz.agents.gtlibrary.utils.HighQualityRandom;

import java.util.*;

/**
 *
 * @author viliam
 */
public class TIGExpander extends MDPExpanderImpl {
   
    @Override
    public List<MDPAction> getActions(MDPState state) {
        List<MDPAction> result = new ArrayList<MDPAction>();

        if (state.isTerminal())
            return result;

        if (state.isRoot()){
            if (state.getPlayer().getId() == 0) {
                result.add(new TIGPatrolTicketsAction());
            } else {
                result.add(new TIGPassangerTypeAction());
            }
        } else if (state instanceof TIGPatrolState) {
            TIGPatrolState s = (TIGPatrolState)state;
            if (s.onTrain){//on train
                if (s.stop>0 && s.stop<TIGConfig.NUM_STOPS-1) result.add(new TIGPatrolOnTrainAction(s.getPlayer(), true));
                result.add(new TIGPatrolOnTrainAction(s.getPlayer(), false));
            } else {
                result.add(new TIGPatrolStopAction(s.getPlayer(), s.time, Math.min(s.time+TIGConfig.PAT_IN_STOP_TIME, TIGConfig.MAX_TIME), s.stop));
                if (s.stop<TIGConfig.NUM_STOPS-1) {
                    int trainNum = TIGConfig.nextTrain(s.stop, 0, s.time);
                    if (trainNum >= 0) result.add(new TIGPatrolGetonAction(s.getPlayer(), 0, trainNum));
                }
                if (s.stop>0){
                    int trainNum = TIGConfig.nextTrain(s.stop, 1, s.time);
                    if (trainNum >= 0) result.add(new TIGPatrolGetonAction(s.getPlayer(), 1, TIGConfig.nextTrain(s.stop, 1, s.time)));
                }
            }
        } else if (state instanceof TIGPassangerState){
            TIGPassangerState s = (TIGPassangerState) state;
            if (s.amount>0){
                result.add(new TIGPassangerTicketAction(s.getPlayer(), true));
                result.add(new TIGPassangerTicketAction(s.getPlayer(), false));
            }
        }
       
        if (TIGConfig.SHUFFLE) {
            Collections.shuffle(result, new HighQualityRandom(TIGConfig.SHUFFLE_ID));
        }
        return result;
    }

    Map<MDPState, Double> typesCache = null; 
    @Override
    public Map<MDPState, Double> getSuccessors(MDPStateActionMarginal action) {
        MDPState oldRoot = null;
        if (action.getState() instanceof MDPStrategy.MDPRootState){//workaroung for DO
            oldRoot = action.getState();
            if (action.getPlayer().getId() == 0){
                action = new MDPStateActionMarginal(new TIGPatrolState(action.getPlayer()), action.getAction());
            } else {
                action = new MDPStateActionMarginal(new TIGPassangerState(action.getPlayer()), action.getAction());
            }
        }
        
        Map<MDPState, Double> result = new HashMap<MDPState, Double>();
        if (action.getState() instanceof TIGPatrolState){
            TIGPatrolState currentState = (TIGPatrolState) action.getState();
            if (action.getAction() instanceof TIGPatrolOnTrainAction){
                assert currentState.onTrain;
                assert currentState.trainDir >= 0;
               
                int arrivalTime = TIGConfig.getTrainTime(currentState.trainDir, currentState.trainNum, currentState.stop);
                
                TIGPatrolState stayState = (TIGPatrolState) currentState.copy();
                stayState.stop += 1 * (stayState.trainDir == 0 ? 1 : -1);
                stayState.time = arrivalTime + 1;
                
                TIGPatrolOnTrainAction a = (TIGPatrolOnTrainAction) action.getAction();
                if (a.stay){
                    result.put(stayState, 1.0);
                } else {
                    TIGPatrolState leaveState = (TIGPatrolState) currentState.copy();
                    leaveState.time = arrivalTime + 1;
                    leaveState.trainDir = -1;
                    leaveState.trainNum = -1;
                    leaveState.onTrain = false;
                    if (currentState.stop==0 || currentState.stop==TIGConfig.NUM_STOPS-1){
                        result.put(leaveState,1.0);
                    } else {
                        result.put(stayState,TIGConfig.MOVEMENT_UNCERTAINTY);
                        result.put(leaveState,1-TIGConfig.MOVEMENT_UNCERTAINTY);
                    }
                }
            } else if (action.getAction() instanceof TIGPatrolGetonAction){
                TIGPatrolGetonAction a = (TIGPatrolGetonAction) action.getAction();
                TIGPatrolState leaveState = (TIGPatrolState) currentState.copy();
                assert !leaveState.onTrain;
                leaveState.onTrain = true;
                leaveState.time=TIGConfig.getTrainTime(a.trainDir, a.trainNum, leaveState.stop)+1;
                leaveState.trainDir = a.trainDir;
                leaveState.trainNum = a.trainNum;
                TIGPatrolState stayState = (TIGPatrolState) currentState.copy();
                stayState.time = leaveState.time;
                result.put(stayState,TIGConfig.MOVEMENT_UNCERTAINTY);
                result.put(leaveState,1-TIGConfig.MOVEMENT_UNCERTAINTY);   
            } else if (action.getAction() instanceof TIGPatrolStopAction){
                TIGPatrolStopAction a = (TIGPatrolStopAction) action.getAction();
                TIGPatrolState stayState = (TIGPatrolState) currentState.copy();
                assert stayState.time == a.fromTime;
                stayState.time = a.toTime;
                result.put(stayState,1.0);
            } else if (action.getAction() instanceof TIGPatrolTicketsAction){
                TIGPatrolState stayState = (TIGPatrolState) currentState.copy();
                assert stayState.time == -2;
                stayState.time = 0;
                result.put(stayState,1.0);
            } else {
                assert false;
            }
        } else if (action.getState() instanceof TIGPassangerState) {
            TIGPassangerState currentState = (TIGPassangerState) action.getState();
            if (action.getAction() instanceof TIGPassangerTicketAction){
                result.put(TIGConfig.pasEndState, 1.0);
            } else if (action.getAction() instanceof TIGPassangerTypeAction){
                if (typesCache != null) return typesCache;
                ArrayList<TIGPassangerState> tmp = new ArrayList<>();
                for (int start=0; start<TIGConfig.NUM_STOPS-1;start++){
                    for (int end=start+1; end<TIGConfig.NUM_STOPS;end++){
                        for (int num=0; num < TIGConfig.trainStarts[0].length;num++){
                            tmp.add(new TIGPassangerState(currentState.getPlayer(),start, end, 0, num));
                        }
                        for (int num=0; num < TIGConfig.trainStarts[1].length;num++){
                            tmp.add(new TIGPassangerState(currentState.getPlayer(),TIGConfig.NUM_STOPS-start-1, TIGConfig.NUM_STOPS-end-1, 1, num));
                        }
                    }
                }
                int allPass = 0;
                for (TIGPassangerState s : tmp) allPass += s.amount;
                for (TIGPassangerState s : tmp) if (s.amount>0) result.put(s,((double)s.amount)/allPass);
                typesCache = result;
            }
        }
        
        if (MAINTAIN_PREDECESSORS){
            if (oldRoot != null) action = new MDPStateActionMarginal(oldRoot, action.getAction());
            for (Map.Entry<MDPState, Double> en : result.entrySet()){
                Map<MDPStateActionMarginal, Double> preds = predecessors.get(en.getKey());
                if (preds==null) {
                    preds = new HashMap<>();
                    predecessors.put(en.getKey(), preds);
                }
                preds.put(action, en.getValue());
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
