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


package cz.agents.gtlibrary.nfg.MDP.domain.transitgame;

import cz.agents.gtlibrary.nfg.MDP.implementations.MDPExpanderImpl;
import cz.agents.gtlibrary.nfg.MDP.implementations.MDPStateActionMarginal;
import cz.agents.gtlibrary.nfg.MDP.interfaces.MDPAction;
import cz.agents.gtlibrary.nfg.MDP.interfaces.MDPState;
import cz.agents.gtlibrary.utils.HighQualityRandom;
import cz.agents.gtlibrary.utils.Pair;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: bosansky
 * Date: 9/16/13
 * Time: 10:00 AM
 * To change this template use File | Settings | File Templates.
 */
public class TGExpander extends MDPExpanderImpl {

    public static boolean sparseUncertainty = true;

    @Override
    public List<MDPAction> getActions(MDPState state) {
        List<MDPAction> result = new ArrayList<MDPAction>();

        if (state.isTerminal())
            return result;

        if (state.isRoot()) {
            if (state.getPlayer().getId() == 0) {
                TGAction a = new TGAction(state.getPlayer(),new int[]{-1}, new int[]{-1});
                result.add(a);
            } else {
                TGAction a = new TGAction(state.getPlayer(),new int[]{-1}, new int[]{-1});
                result.add(a);            }
        } else {
            TGState s = (TGState)state;
            if (s.getTimeStep() >= TGConfig.getMaxTimeStep()) {
                return result;
            }
            for (int u=0; u<s.getUNITS(); u++) {
                if (s.getCol()[u] >= 0 && s.getRow()[u] >= 0) {
                    for (int c=s.getCol()[u]-1; (c<=s.getCol()[u]+1) && (c<TGConfig.LENGTH_OF_GRID); c++)
                        for (int r=s.getRow()[u]-1; (r<=s.getRow()[u]+1) && (r<TGConfig.WIDTH_OF_GRID); r++) {
                            if (c < 0 || r < 0) continue;
                            TGAction a = new TGAction(s.getPlayer(), new int[]{c}, new int[]{r});
                            result.add(a);
                        }
                } else {
                    if (s.getPlayer().getId() == 0) {
                        for (int r=0; r<TGConfig.WIDTH_OF_GRID; r++) {
                            TGAction a = new TGAction(s.getPlayer(), new int[]{0}, new int[]{r});
                            result.add(a);
                        }
                    } else {
                        assert false; // shouldn't happen
                    }
                }
            }
        }

        if (TGConfig.SHUFFLE) {
            Collections.shuffle(result, new HighQualityRandom(TGConfig.SHUFFLE_ID));
        }
        return result;
    }

    @Override
    public Map<MDPState, Double> getSuccessors(MDPStateActionMarginal action) {
        MDPState currentState = action.getState();
        if (!TGConfig.useUncertainty || currentState.isRoot() || ((TGState)currentState).getTimeStep() <= 0 || !isUncertaintyInThisState((TGState)currentState)) {
            Map<MDPState, Double> result = new HashMap<MDPState, Double>();

            MDPState state = currentState.performAction(action.getAction());
            if (state != null) {
                result.put(state,1d);
            }
            return result;
        } else {
            Map<MDPState, Double> result = new HashMap<MDPState, Double>();

            MDPState newState = currentState.performAction(action.getAction());
            if (newState != null) {
                result.put(newState,1d - TGConfig.MOVEMENT_UNCERTAINTY);
            }

//            MDPState failState = action.getState();
//            ((TGState)failState).incTimeStep();
            MDPState failState = action.getState().performAction(new TGAction(currentState.getPlayer(), new int[] {((TGState)currentState).getCol()[0]}, new int[] {((TGState)currentState).getRow()[0]}));

            if (!result.isEmpty()) {
                if (newState.equals(failState)) {
                    result.put(failState,1d);
                } else {
                    result.put(failState,TGConfig.MOVEMENT_UNCERTAINTY);
                }
            }
            return result;
        }
    }

    @Override
    public Map<MDPStateActionMarginal, Double> getPredecessors(MDPState state) {
        TGState s = (TGState)state;
        Map<MDPStateActionMarginal, Double> result = new HashMap<MDPStateActionMarginal, Double>();

        if (TGConfig.rememberHistory) {
            double prob = 1;

            if (s.getCol()[0] == -1 && s.getRow()[0] == -1)
                return null;

            if (s.getTimeStep() == 0) {
                if (s.getPlayer().getId() == 0) {
                    Pair<int[], int[]> lastCoord = s.getHistory().get(s.getHistory().size()-1);
                    List<Pair<int[], int[]>> lastHistory = s.getHistory().subList(0,s.getHistory().size()-1);
                    TGState previousState = new TGState(s.getPlayer(), s.getTimeStep() - 1, lastCoord.getLeft(), lastCoord.getRight(), lastHistory);
                    TGAction a = new TGAction(s.getPlayer(), new int[] {s.getCol()[0]}, new int[] {s.getRow()[0]});
                    result.put(new MDPStateActionMarginal(previousState, a),1d);
                    return result;
                } else {
                    return null;
                }
            }
            Pair<int[], int[]> lastCoord = s.getHistory().get(s.getHistory().size()-1);
            List<Pair<int[], int[]>> lastHistory = s.getHistory().subList(0,s.getHistory().size()-1);
            TGState previousState = new TGState(s.getPlayer(), s.getTimeStep() - 1, lastCoord.getLeft(), lastCoord.getRight(), lastHistory);
            if (s.getTimeStep() > 1 && TGConfig.useUncertainty && isUncertaintyInThisState(previousState)) {
                if (previousState.getCol()[0] == s.getCol()[0] && previousState.getRow()[0] == s.getRow()[0]) {
                    TGAction a = new TGAction(s.getPlayer(), new int[] {s.getCol()[0]}, new int[] {s.getRow()[0]});
                    result.put(new MDPStateActionMarginal(previousState, a), 1d);

                    for (int c=s.getCol()[0]-1; (c<=s.getCol()[0]+1) && (c<TGConfig.LENGTH_OF_GRID); c++) {
//                    if (s.getPlayer().getId() == 0 && c > (s.getTimeStep() - 1)) continue;
                        for (int r=s.getRow()[0]-1; (r<=s.getRow()[0]+1) && (r<TGConfig.WIDTH_OF_GRID); r++) {
                            if (c < 0 || r < 0) continue;
                            if (!isUncertaintyInThisState(s.getCol()[0],s.getRow()[0])) continue;
                            TGAction a2 = new TGAction(s.getPlayer(), new int[]{c}, new int[]{r});
                            MDPStateActionMarginal marginal = new MDPStateActionMarginal(previousState, a2);
                            if (result.containsKey(marginal)) {
                                result.put(marginal,1d);
                            } else {
                                result.put(marginal,TGConfig.MOVEMENT_UNCERTAINTY);
                            }
                        }
                    }
                } else {
                    TGAction a = new TGAction(s.getPlayer(), new int[] {s.getCol()[0]}, new int[] {s.getRow()[0]});
                    result.put(new MDPStateActionMarginal(previousState, a), 1d - TGConfig.MOVEMENT_UNCERTAINTY);
                }
            } else {
                TGAction a = new TGAction(s.getPlayer(), new int[] {s.getCol()[0]}, new int[] {s.getRow()[0]});
                result.put(new MDPStateActionMarginal(previousState, a), prob);
            }
            return result;
        }

        if (s.getCol()[0] == -1 && s.getRow()[0] == -1)
            return null;
        if (s.getTimeStep() == 0) {
            if (s.getPlayer().getId() == 0) {
                TGState p = new TGState(s.getPlayer(), -1, new int[]{-1}, new int[]{-1});
                TGAction a = new TGAction(s.getPlayer(), new int[] {s.getCol()[0]}, new int[] {s.getRow()[0]});
                result.put(new MDPStateActionMarginal(p, a),1d);
            } else {
                return null;
            }
        } else if (s.getTimeStep() == 1) {
            if (s.getPlayer().getId() == 0)
                for (int r=s.getRow()[0]-1; (r<=s.getRow()[0]+1) && (r<TGConfig.WIDTH_OF_GRID); r++) {
                    if (r < 0) continue;
                    TGState p = new TGState(s.getPlayer(), 0, new int[]{0}, new int[]{r});
                    TGAction a = new TGAction(s.getPlayer(), new int[] {s.getCol()[0]}, new int[] {s.getRow()[0]});
                    result.put(new MDPStateActionMarginal(p, a),1d);
                }
            else {
                TGState p = new TGState(s.getPlayer(), 0, new int[]{TGConfig.PATROLLER_BASES[0]}, new int[]{0});
                TGAction a = new TGAction(s.getPlayer(), new int[] {s.getCol()[0]}, new int[] {s.getRow()[0]});
                result.put(new MDPStateActionMarginal(p, a),1d);
            }
        } else {
            if (!TGConfig.useUncertainty) { // || ((s.getPlayer().getId() == 0 && s.getCol()[0] >= s.getTimeStep()) || (s.getPlayer().getId() == 1 && s.getRow()[0] >= s.getTimeStep()))
                for (int c=s.getCol()[0]-1; (c<=s.getCol()[0]+1) && (c<TGConfig.LENGTH_OF_GRID); c++) {
                    if (s.getPlayer().getId() == 0 && c > (s.getTimeStep() - 1)) continue;
                    for (int r=s.getRow()[0]-1; (r<=s.getRow()[0]+1) && (r<TGConfig.WIDTH_OF_GRID); r++) {
                        if (c < 0 || r < 0) continue;
                        TGState p = new TGState(s.getPlayer(), s.getTimeStep()-1, new int[]{c}, new int[]{r});
                        TGAction a = new TGAction(s.getPlayer(), new int[] {s.getCol()[0]}, new int[] {s.getRow()[0]});
                        result.put(new MDPStateActionMarginal(p, a),1d);
                    }
                }
            } else {
                for (int c=s.getCol()[0]-1; (c<=s.getCol()[0]+1) && (c<TGConfig.LENGTH_OF_GRID); c++) {
                    if (s.getPlayer().getId() == 0 && c > (s.getTimeStep() - 1)) continue;
                    for (int r=s.getRow()[0]-1; (r<=s.getRow()[0]+1) && (r<TGConfig.WIDTH_OF_GRID); r++) {
                        if (c < 0 || r < 0) continue;
                        TGState p = new TGState(s.getPlayer(), s.getTimeStep()-1, new int[]{c}, new int[]{r});
                        TGAction a = new TGAction(s.getPlayer(), new int[] {s.getCol()[0]}, new int[] {s.getRow()[0]});
                        if (isUncertaintyInThisState(p)){
                            result.put(new MDPStateActionMarginal(p, a),1d - TGConfig.MOVEMENT_UNCERTAINTY);
                        } else {
                            result.put(new MDPStateActionMarginal(p, a),1d);
                        }
                    }
                }
                for (int c=s.getCol()[0]-1; (c<=s.getCol()[0]+1) && (c<TGConfig.LENGTH_OF_GRID); c++) {
//                    if (s.getPlayer().getId() == 0 && c > (s.getTimeStep() - 1)) continue;
                    for (int r=s.getRow()[0]-1; (r<=s.getRow()[0]+1) && (r<TGConfig.WIDTH_OF_GRID); r++) {
                        if (c < 0 || r < 0) continue;
                        if (!isUncertaintyInThisState(s.getCol()[0],s.getRow()[0])) continue;
                        TGState p = new TGState(s.getPlayer(), s.getTimeStep()-1, new int[] {s.getCol()[0]}, new int[] {s.getRow()[0]});
                        TGAction a = new TGAction(s.getPlayer(), new int[]{c}, new int[]{r});
                        MDPStateActionMarginal marginal = new MDPStateActionMarginal(p, a);
                        if (result.containsKey(marginal)) {
                            result.put(marginal,1d);
                        } else {
                            result.put(marginal,TGConfig.MOVEMENT_UNCERTAINTY);
                        }
                    }
                }
            }
        }
        return result;
    }

    private boolean isUncertaintyInThisState(TGState state) {
        return isUncertaintyInThisState(state.getCol()[0],state.getRow()[0]);
    }

    private static boolean isUncertaintyInThisState(int col, int row) {
        if (!sparseUncertainty) return TGConfig.useUncertainty;
        int allNodes = TGConfig.LENGTH_OF_GRID*TGConfig.WIDTH_OF_GRID;
        int currentID = col*TGConfig.WIDTH_OF_GRID + row;
        if (currentID % ((allNodes / 5) + 1) == 0) return true;
        else return false;
    }

    public static void main(String[] args) {
        System.out.println(isUncertaintyInThisState(0,0));
        System.out.println(isUncertaintyInThisState(0,1));
        System.out.println(isUncertaintyInThisState(1,0));
        System.out.println(isUncertaintyInThisState(1,1));
        System.out.println(isUncertaintyInThisState(2,0));
    }
}
