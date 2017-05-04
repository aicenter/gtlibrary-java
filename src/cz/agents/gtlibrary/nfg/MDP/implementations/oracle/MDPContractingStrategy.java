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


package cz.agents.gtlibrary.nfg.MDP.implementations.oracle;

import cz.agents.gtlibrary.iinodes.ActionImpl;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.nfg.MDP.implementations.MDPActionImpl;
import cz.agents.gtlibrary.nfg.MDP.implementations.MDPConfigImpl;
import cz.agents.gtlibrary.nfg.MDP.implementations.MDPStateActionMarginal;
import cz.agents.gtlibrary.nfg.MDP.interfaces.MDPAction;
import cz.agents.gtlibrary.nfg.MDP.interfaces.MDPConfig;
import cz.agents.gtlibrary.nfg.MDP.interfaces.MDPExpander;
import cz.agents.gtlibrary.nfg.MDP.interfaces.MDPState;

import java.util.*;

/**
 *
 */
public class MDPContractingStrategy extends MDPIterativeStrategy {

    final public static double PRECISION = MDPConfigImpl.getEpsilon();

    private Map<MDPState, Map<MDPAction, Integer>> fixedBehavioralStrategies = new HashMap<MDPState, Map<MDPAction, Integer>>();
    private Map<MDPStateActionMarginal, Map<MDPState, Double>> successorMapChanges = new HashMap<MDPStateActionMarginal, Map<MDPState, Double>>();
    private Map<MDPState, Map<MDPStateActionMarginal, Double>> predecessorMapChanges = new HashMap<MDPState, Map<MDPStateActionMarginal, Double>>();
    private Map<MDPState, MDPStateActionMarginal> contractedDefaultActions = new HashMap<MDPState, MDPStateActionMarginal>();

    private Set<MDPStateActionMarginal> actionMarginalsToRemove = new HashSet<MDPStateActionMarginal>();


    public MDPContractingStrategy(Player player, MDPConfig config, MDPExpander expander) {
        super(player, config, expander);
    }


    public boolean isStateContracted(MDPState state) {
        return fixedBehavioralStrategies.containsKey(state);
    }

    public boolean isActionInFixedSupport(MDPState state, MDPAction action) {
        assert (fixedBehavioralStrategies.containsKey(state));

        Map<MDPAction, Integer> fixedBS = fixedBehavioralStrategies.get(state);
        if (!fixedBS.containsKey(action))
            return false;

        if (fixedBS.get(action) > 0)
            return true;
        else return false;
    }

    public Set<MDPStateActionMarginal> concractStates(Set<MDPState> states, Map<MDPState, Double> probsMap) {
//        predsSanity();
        Set<MDPStateActionMarginal> result = new HashSet<MDPStateActionMarginal>();

        for (MDPState s : states) {
            double contractedProb = 0d;
            MDPStateActionMarginal m = contractStateToActualStrategy(s, probsMap.get(s));
            for (Map.Entry<MDPAction, Integer> a : ((ContractedDefaultAction)m.getAction()).getContractedActions().entrySet()) {
                contractedProb += getExpandedStrategy(new MDPStateActionMarginal(s, a.getKey()));
            }
            strategy.put(m, contractedProb);
            result.add(m);
        }
//        getAllActionStates().addAll(result);
//        lastActionsSanity();
//        predsSanity();
//        removeMaringalsFromStrategy();
//        lastActionsSanity();
        return result;
    }

    public Set<MDPStateActionMarginal> expandStates(Set<MDPState> states) {
//        predsSanity();
        Set<MDPStateActionMarginal> result = new HashSet<MDPStateActionMarginal>();
        for (MDPState s : states) {
            MDPStateActionMarginal contrAction = contractedDefaultActions.get(s);
            double contractedProb = getExpandedStrategy(contrAction);

            Set<MDPAction> tmp = expandContractedState(s);
            for (MDPAction a : tmp) {
                MDPStateActionMarginal m = new MDPStateActionMarginal(s, a);
                result.add(m);
                strategy.put(m, ((ContractedDefaultAction)contrAction.getAction()).getContractedActions().get(a)*PRECISION*contractedProb);
                if (!isActionFullyExpandedInRG(m))
                    getLastActions().add(m);
            }
        }
//        getAllActionStates().addAll(result);
//        predsSanity();
//        removeMaringalsFromStrategy();
        return result;
    }

    private Set<MDPAction> expandContractedState(MDPState state) {

        Map<MDPAction, Integer> expandedActions = fixedBehavioralStrategies.remove(state);
        MDPStateActionMarginal action = contractedDefaultActions.remove(state);
        actionMarginalsToRemove.add(action);
        successorMapChanges.remove(action);
        Set<MDPState> statesToRemoveFromPreds = new HashSet<MDPState>();
        for (Map.Entry<MDPState, Map<MDPStateActionMarginal, Double>> predsMappings : predecessorMapChanges.entrySet()) {
            if (predsMappings.getValue().containsKey(action)) {
                predsMappings.getValue().remove(action);
                boolean isThereSomeOtherPred = false;
                for (MDPStateActionMarginal remPredecessors : predsMappings.getValue().keySet()) {
                   if ((remPredecessors.getAction() instanceof ContractedDefaultAction)) {
                       isThereSomeOtherPred = true;
                       break;
                   }
                }
                if (!isThereSomeOtherPred) {
                    statesToRemoveFromPreds.add(predsMappings.getKey());
                }  else {
                    Map<MDPStateActionMarginal, Double> oldPredecessors = super.getPredecessors(predsMappings.getKey());
                    for (Map.Entry<MDPStateActionMarginal, Double> op : oldPredecessors.entrySet()) {
                        if (expandedActions.containsKey(op.getKey().getAction()) && !fixedBehavioralStrategies.containsKey(op.getKey().getState())) {
                            predsMappings.getValue().put(op.getKey(), op.getValue());
                        }
                    }
                }

            }
        }

        for (MDPState s : statesToRemoveFromPreds) {
            predecessorMapChanges.remove(s);
        }

        return expandedActions.keySet();
    }

    private MDPStateActionMarginal contractStateToActualStrategy(MDPState state, double probOfState) {
        boolean isLastAction = false;
        assert (!fixedBehavioralStrategies.containsKey(state));

        //create artificial default action -> should be identified by the fixed behavioral strategy
        Map<MDPAction, Integer> behavioralStrategyMapping = createBehavioralStrategyMapping(state, probOfState);

        MDPAction newDefaultAction = new ContractedDefaultAction(behavioralStrategyMapping);
        MDPStateActionMarginal newDefaultMarginalAction = new MDPStateActionMarginal(state, newDefaultAction);

        //create new successor and predecessor mapping
        Map<MDPState, Double> newSuccessors = new HashMap<MDPState, Double>();
        Set<MDPStateActionMarginal> contractedActions = new HashSet<MDPStateActionMarginal>();
//        double actionsNumber = (double)behavioralStrategyMapping.size();

        for (MDPAction a : behavioralStrategyMapping.keySet()) {
            assert (!(a instanceof ContractedDefaultAction));
            MDPStateActionMarginal m = new MDPStateActionMarginal(state, a);
            if (getLastActions().contains(m)) {
                isLastAction = true;
            } else {
//                if (!isActionFullyExpandedInRG(m))
//                    assert false;
            }
            contractedActions.add(m);
            for (Map.Entry<MDPState, Double> e : getSuccessors(m).entrySet()) {
                Double d = newSuccessors.get(e.getKey());
                if (d == null) d = 0d;
                d += behavioralStrategyMapping.get(a) * PRECISION * e.getValue();
                newSuccessors.put(e.getKey(), d);
            }
        }

        //store actual behavioral strategy

        contractedDefaultActions.put(state, newDefaultMarginalAction);
        successorMapChanges.put(newDefaultMarginalAction, newSuccessors);

        for (Map.Entry<MDPState, Double> e : newSuccessors.entrySet()) {
//            if (e.getValue() == 0) continue;
            Map<MDPStateActionMarginal, Double> curPredMapChange = predecessorMapChanges.get(e.getKey());
            if (curPredMapChange == null) {
                curPredMapChange = new HashMap<MDPStateActionMarginal, Double>();
                curPredMapChange.putAll(getPredecessors(e.getKey()));
            }
            for (MDPStateActionMarginal m : contractedActions) {
                if (curPredMapChange.containsKey(m))
                    curPredMapChange.remove(m);
            }
            curPredMapChange.put(newDefaultMarginalAction, e.getValue());
            predecessorMapChanges.put(e.getKey(), curPredMapChange);
        }

        fixedBehavioralStrategies.put(state, behavioralStrategyMapping);
        actionMarginalsToRemove.addAll(contractedActions);

        if (isLastAction) getLastActions().add(newDefaultMarginalAction);

        return newDefaultMarginalAction;
    }

    @Override
    protected Set<MDPStateActionMarginal> addStateAction(MDPState state, Set<MDPStateActionMarginal> actions) {
        if (fixedBehavioralStrategies.containsKey(state)) {
            assert actions.size() <= 1;
            assert actions.iterator().next().equals(contractedDefaultActions.get(state));
            return new HashSet<MDPStateActionMarginal>();
        } else {
            Set<MDPStateActionMarginal> result = super.addStateAction(state, actions);
            for (MDPStateActionMarginal newAction : result) {
                for (Map.Entry<MDPState, Double> suc : getSuccessors(newAction).entrySet()) {
                    Map<MDPStateActionMarginal, Double> curPred = predecessorMapChanges.get(suc.getKey());
                    if (curPred != null) {
                        assert (!curPred.containsKey(newAction));
                        curPred.put(newAction, suc.getValue());
                        predecessorMapChanges.put(suc.getKey(), curPred);
                    }
                }
            }
            return result;
        }
    }

    @Override
    public Map<MDPState, Double> getSuccessors(MDPStateActionMarginal action) {
        if (fixedBehavioralStrategies.containsKey(action.getState()) || successorMapChanges.containsKey(action)) {
            if (!(fixedBehavioralStrategies.containsKey(action.getState()) && successorMapChanges.containsKey(action)))
                assert false;
            return successorMapChanges.get(action);
        }
        else return super.getSuccessors(action);
    }

    public Map<MDPState, Double> getAllSuccessors(MDPStateActionMarginal action) {
        if (fixedBehavioralStrategies.containsKey(action.getState()) && successorMapChanges.containsKey(action)) {
            Map<MDPState, Double> result = new HashMap<MDPState, Double>();
            for (Map.Entry<MDPAction, Integer> a : fixedBehavioralStrategies.get(action.getState()).entrySet()) {
                for (Map.Entry<MDPState, Double> e : super.getAllSuccessors(new MDPStateActionMarginal(action.getState(), a.getKey())).entrySet()) {
                    Double curProb = result.get(e.getKey());
                    if (curProb == null) curProb = 0d;
                    curProb += e.getValue() * a.getValue() * PRECISION;
                    result.put(e.getKey(), curProb);
                }
            }
//            return successorMapChanges.get(action);
            return result;
        } else if (action.getAction() instanceof ContractedDefaultAction) {
            assert false;
            return null;
        }
        else return super.getAllSuccessors(action);
    }

    @Override
    public Map<MDPStateActionMarginal, Double> getPredecessors(MDPState state) {
        if (predecessorMapChanges.containsKey(state)) {
            return predecessorMapChanges.get(state);
        }
        else return super.getPredecessors(state);
    }

    @Override
    public Map<MDPStateActionMarginal, Double> getAllPredecessors(MDPState state) {
        if (predecessorMapChanges.containsKey(state)) {
            Map<MDPStateActionMarginal, Double> result = new HashMap<MDPStateActionMarginal, Double>();
            result.putAll(super.getAllPredecessors(state));
            result.putAll(predecessorMapChanges.get(state));
            return result;

        }
        else return super.getAllPredecessors(state);
    }

    @Override
    public List<MDPAction> getActions(MDPState state) {
        // check whether there is an artificial default-strategy action
        if (contractedDefaultActions.containsKey(state)) {
            List<MDPAction> result = new ArrayList<MDPAction>(1);
            result.add(contractedDefaultActions.get(state).getAction());
            return result;
        }
        // if not, return super
        else return super.getActions(state);
    }

    @Override
    public boolean isFullyExpandedInRG(MDPState state) {
        if (!fixedBehavioralStrategies.containsKey(state)) {
            return super.isFullyExpandedInRG(state);
        } else {
            if (state.isTerminal()) return true;
            Set<MDPAction> fixedActions = fixedBehavioralStrategies.get(state).keySet();
            int allActionsCount = getAllActions(state).size();
            if (fixedActions.size() < allActionsCount) {

                return false;
            } else if (fixedActions.size() == allActionsCount) {
                return true;
            } else {
                assert false;
                return false;
            }

        }
    }

    public MDPAction getDefaultActionForContractedState(MDPState state) {
        assert (contractedDefaultActions.containsKey(state));
        return contractedDefaultActions.get(state).getAction();
    }

    public Map<MDPAction, Integer> createBehavioralStrategyMapping(MDPState state, Double probOfState) {
        Map<MDPAction, Integer> result = new HashMap<MDPAction, Integer>();
//        double probOfState = 0d;
//        if (state.isRoot()) {
//            probOfState = 1d;
//        } else {
//            Map<MDPStateActionMarginal, Double> preds = getPredecessors(state);
//            for (MDPStateActionMarginal m : preds.keySet()) {
//                probOfState += getExpandedStrategy(m) * preds.get(m);
//            }
//        }
//        if (probOfState < 1e-10) {
//            for (MDPAction a : getActions(state)) {
//                result.put(a, 0);
//            }
//            result.put(getActions(state).get(0), new Double(1 / PRECISION).intValue());
//            return result;
//        }
        double checkSum = 0d;
        for (MDPAction a : getActions(state)) {
            double v = getExpandedStrategy(new MDPStateActionMarginal(state, a))/probOfState;
            checkSum += v;
            int vv = new Double(v*(1/PRECISION)).intValue();
            result.put(a, vv);
        }
//        if (checkSum > 0 && Math.abs(checkSum - 1) > 0.001)
//            assert false;
        return result;
    }

    // placeholders for newly created default actions
    private class ContractedDefaultAction extends MDPActionImpl {

        private final int hash;
        private Map<MDPAction, Integer> contractedActions;

        public ContractedDefaultAction(Map<MDPAction, Integer> contractedActions) {
            this.contractedActions = contractedActions;
            int tmp = 0;
            for (Map.Entry<MDPAction, Integer> e : contractedActions.entrySet()) {
                tmp = (tmp * 31 + e.getKey().hashCode())*31 + e.getValue();
            }
            hash = tmp;
        }

        @Override
        public boolean equals(Object obj) {
            return super.equals(obj);
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public String toString() {
            return "ContractedDefaultAction{" +
                    "hash=" + hash +
                    '}';
        }

        public Map<MDPAction, Integer> getContractedActions() {
            return contractedActions;
        }
    }

    public Set<MDPStateActionMarginal> getActionMarginalsToRemove() {
        return actionMarginalsToRemove;
    }

    public void clearActionMarginalsToRemove() {
        actionMarginalsToRemove.clear();
    }

    public void removeMaringalsFromStrategy() {
        for (MDPStateActionMarginal m : actionMarginalsToRemove) {
            getLastActions().remove(m);
//                for (MDPStateActionMarginal opM : getOpponentsStrategy().getAllMarginalsInStrategy()) {
//                    removeDefaultUtilityFromCache(m, opM);
//                }
//                for (MDPStateActionMarginal opM : ((MDPContractingStrategy)getOpponentsStrategy()).getActionMarginalsToRemove()) {
//                    removeDefaultUtilityFromCache(m, opM);
//                }
//            } else {
//                for (MDPStateActionMarginal m2 : getLastActions()) {
//                    if (!getPlayer().equals(m2.getPlayer()))
//                        removeDefaultUtilityFromCache(m, m2);
//                }

            if (m.getAction() instanceof ContractedDefaultAction) {
                for (MDPStateActionMarginal opM : getOpponentsStrategy().getAllMarginalsInStrategy()) {
                    removeDefaultUtilityFromCache(m, opM);
                }
            } else {
                getRemovedLastActions(m.getPlayer()).add(m);
            }
            strategy.remove(m);
//            getAllActionStates().remove(m);
        }
//        clearActionMarginalsToRemove();
    }

    @Override
    public double getNonCachedUtility(MDPStateActionMarginal firstPlayerAction, MDPStateActionMarginal secondPlayerAction) {
        double result = 0;
        if (firstPlayerAction.getAction() instanceof ContractedDefaultAction) {
            for (Map.Entry<MDPAction, Integer> e : ((ContractedDefaultAction) firstPlayerAction.getAction()).getContractedActions().entrySet()) {
                MDPStateActionMarginal tmp = new MDPStateActionMarginal(firstPlayerAction.getState(), e.getKey());
                result += e.getValue()*PRECISION*getUtility(tmp, secondPlayerAction);
            }
            return result;
        } else if (secondPlayerAction.getAction() instanceof ContractedDefaultAction) {
            for (Map.Entry<MDPAction, Integer> e : ((ContractedDefaultAction) secondPlayerAction.getAction()).getContractedActions().entrySet()) {
                MDPStateActionMarginal tmp = new MDPStateActionMarginal(secondPlayerAction.getState(), e.getKey());
                result += e.getValue()*PRECISION*getUtility(firstPlayerAction, tmp);
            }
            return result;
        }
        else return super.getNonCachedUtility(firstPlayerAction, secondPlayerAction);
    }
      
    public void lastActionsSanity() {
        for (MDPStateActionMarginal m : contractedDefaultActions.values()) {
            if (!isActionFullyExpandedInRG(m) && !getLastActions().contains(m)) {
                assert false;
            }
        }
    }

    @Override
    public boolean isActionFullyExpandedInRG(MDPStateActionMarginal marginal) {
        if (marginal.getAction() instanceof ContractedDefaultAction && !successorMapChanges.containsKey(marginal))
            return true;
        else return super.isActionFullyExpandedInRG(marginal);
    }

    public void predsSanity() {
        for (Map<MDPStateActionMarginal, Double> map : predecessorMapChanges.values()) {
            for (MDPStateActionMarginal a : map.keySet()) {
                if (!(a.getAction() instanceof ContractedDefaultAction)) {
                    if (fixedBehavioralStrategies.containsKey(a.getState()))
                        assert false;
                }
            }
        }
    }

    public int getFixedBehavioralStrategiesSize() {
        return fixedBehavioralStrategies.size();
    }
}
