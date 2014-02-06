package cz.agents.gtlibrary.nfg.experimental.domain.transitgame;

import cz.agents.gtlibrary.nfg.experimental.MDP.implementations.MDPExpanderImpl;
import cz.agents.gtlibrary.nfg.experimental.MDP.implementations.MDPStateActionMarginal;
import cz.agents.gtlibrary.nfg.experimental.MDP.interfaces.MDPAction;
import cz.agents.gtlibrary.nfg.experimental.MDP.interfaces.MDPState;
import cz.agents.gtlibrary.nfg.experimental.domain.bpg.BPState;
import cz.agents.gtlibrary.utils.HighQualityRandom;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: bosansky
 * Date: 9/16/13
 * Time: 10:00 AM
 * To change this template use File | Settings | File Templates.
 */
public class TGExpander extends MDPExpanderImpl {

    public boolean sparseUncertainty = false;

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
                    for (int c=s.getCol()[u]-1; (c<=s.getCol()[u]+1) && (c<=TGConfig.LENGTH_OF_GRID); c++)
                        for (int r=s.getRow()[u]-1; (r<=s.getRow()[u]+1) && (r<=TGConfig.WIDTH_OF_GRID); r++) {
                            if (c < 0 || r < 0) continue;
                            TGAction a = new TGAction(s.getPlayer(), new int[]{c}, new int[]{r});
                            result.add(a);
                        }
                } else {
                    if (s.getPlayer().getId() == 0) {
                        for (int r=0; r<=TGConfig.WIDTH_OF_GRID; r++) {
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
        if (!TGConfig.useUncertainty || action.getState().isRoot() || ((TGState)action.getState()).getTimeStep() <= 0) {
            Map<MDPState, Double> result = new HashMap<MDPState, Double>();

            MDPState state = action.getState().copy().performAction(action.getAction());
            if (state != null) {
                result.put(state,1d);
            }
            return result;
        } else {
            Map<MDPState, Double> result = new HashMap<MDPState, Double>();

            MDPState state = action.getState().copy().performAction(action.getAction());
            if (state != null) {
                result.put(state,1d - TGConfig.MOVEMENT_UNCERTAINTY);
            }

            MDPState failState = action.getState().copy();
            ((TGState)failState).incTimeStep();
            if (!result.isEmpty()) {
                if (state.equals(failState)) {
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
                for (int r=s.getRow()[0]-1; (r<=s.getRow()[0]+1) && (r<=TGConfig.WIDTH_OF_GRID); r++) {
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
                for (int c=s.getCol()[0]-1; (c<=s.getCol()[0]+1) && (c<=TGConfig.LENGTH_OF_GRID); c++) {
                    if (s.getPlayer().getId() == 0 && c > (s.getTimeStep() - 1)) continue;
                    for (int r=s.getRow()[0]-1; (r<=s.getRow()[0]+1) && (r<=TGConfig.WIDTH_OF_GRID); r++) {
                        if (c < 0 || r < 0) continue;
                        TGState p = new TGState(s.getPlayer(), s.getTimeStep()-1, new int[]{c}, new int[]{r});
                        TGAction a = new TGAction(s.getPlayer(), new int[] {s.getCol()[0]}, new int[] {s.getRow()[0]});
                        result.put(new MDPStateActionMarginal(p, a),1d);
                    }
                }
            } else {
                for (int c=s.getCol()[0]-1; (c<=s.getCol()[0]+1) && (c<=TGConfig.LENGTH_OF_GRID); c++) {
                    if (s.getPlayer().getId() == 0 && c > (s.getTimeStep() - 1)) continue;
                    for (int r=s.getRow()[0]-1; (r<=s.getRow()[0]+1) && (r<=TGConfig.WIDTH_OF_GRID); r++) {
                        if (c < 0 || r < 0) continue;
                        TGState p = new TGState(s.getPlayer(), s.getTimeStep()-1, new int[]{c}, new int[]{r});
                        TGAction a = new TGAction(s.getPlayer(), new int[] {s.getCol()[0]}, new int[] {s.getRow()[0]});
                        result.put(new MDPStateActionMarginal(p, a),1d - TGConfig.MOVEMENT_UNCERTAINTY);
                    }
                }
                for (int c=s.getCol()[0]-1; (c<=s.getCol()[0]+1) && (c<=TGConfig.LENGTH_OF_GRID); c++) {
//                    if (s.getPlayer().getId() == 0 && c > (s.getTimeStep() - 1)) continue;
                    for (int r=s.getRow()[0]-1; (r<=s.getRow()[0]+1) && (r<=TGConfig.WIDTH_OF_GRID); r++) {
                        if (c < 0 || r < 0) continue;
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
        if (!sparseUncertainty) return TGConfig.useUncertainty;
        int allNodes = TGConfig.LENGTH_OF_GRID*TGConfig.WIDTH_OF_GRID;
        int currentID = state.getCol()[0]*TGConfig.WIDTH_OF_GRID + state.getRow()[0];
        if (currentID % (allNodes / 10) == 0) return true;
        else return false;
    }
}
