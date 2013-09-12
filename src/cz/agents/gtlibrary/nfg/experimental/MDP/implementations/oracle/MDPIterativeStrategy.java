package cz.agents.gtlibrary.nfg.experimental.MDP.implementations.oracle;

import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.nfg.experimental.MDP.implementations.MDPStateActionMarginal;
import cz.agents.gtlibrary.nfg.experimental.MDP.implementations.MDPStrategy;
import cz.agents.gtlibrary.nfg.experimental.MDP.interfaces.MDPAction;
import cz.agents.gtlibrary.nfg.experimental.MDP.interfaces.MDPConfig;
import cz.agents.gtlibrary.nfg.experimental.MDP.interfaces.MDPExpander;
import cz.agents.gtlibrary.nfg.experimental.MDP.interfaces.MDPState;
import cz.agents.gtlibrary.utils.Pair;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: bosansky
 * Date: 8/19/13
 * Time: 9:11 AM
 * To change this template use File | Settings | File Templates.
 */
public class MDPIterativeStrategy extends MDPStrategy {
    final public static boolean USE_EXPST_CACHE = true;

    private DefaultStrategyType defaultStrategy = DefaultStrategyType.FirstAction;
//    private DefaultStrategyType defaultStrategy = DefaultStrategyType.Uniform;
    private Map<MDPState, Set<MDPAction>> actionMap = new HashMap<MDPState, Set<MDPAction>>();
    private Map<MDPStateActionMarginal, Map<MDPState, Double>> successorMap = new HashMap<MDPStateActionMarginal, Map<MDPState, Double>>();
    private Map<MDPState, Map<MDPStateActionMarginal, Double>> predecessorMap = new HashMap<MDPState, Map<MDPStateActionMarginal, Double>>();

    private Map<MDPStateActionMarginal, Double> expandedNonZeroStrategy = new HashMap<MDPStateActionMarginal, Double>();

    private Set<MDPStateActionMarginal> allStatesActions = new HashSet<MDPStateActionMarginal>();
    private MDPStrategy opponentsStrategy = null;

    public MDPIterativeStrategy(Player player, MDPConfig config, MDPExpander expander) {
        super(player, config, expander);
        generateAllStateActions();
    }

    public void initIterativeStrategy(MDPStrategy opponentStrategy) {
//        defaultUtilityCache.clear();
        this.opponentsStrategy = opponentStrategy;
//        calculateDefaultUtility(getRootState(), 1, true);

        MDPStateActionMarginal actionMarginal = new MDPStateActionMarginal(getRootState(), getAllActions(getRootState()).get(0));
        HashSet<MDPStateActionMarginal> tmp = new HashSet<MDPStateActionMarginal>();
        tmp.add(actionMarginal);
        HashMap<MDPState, Set<MDPStateActionMarginal>> tmp2 = new HashMap<MDPState, Set<MDPStateActionMarginal>>();
        tmp2.put(getRootState(), tmp);
        addBRStrategy(tmp2);
    }

    private Map<MDPStateActionMarginal, Double> calculateDefaultUtility(MDPState state, double prob, Set<MDPStateActionMarginal> opponentsMarginals, Map<Set<MDPStateActionMarginal>, Double> defUtilityCache) {
        Map<MDPStateActionMarginal, Double> result = new HashMap<MDPStateActionMarginal, Double>();
        for (MDPStateActionMarginal opAction : opponentsMarginals) {
            result.put(opAction, 0d);
        }

        if (!hasAllStateASuccessor(state)) { // terminal state
//            defaultUtilityCache.put(state, result);
            return result;
        }
//        if (defaultUtilityCache.containsKey(state)) {
//            return defaultUtilityCache.get(state);
//        }
        List<MDPAction> actions = getAllActions(state);
        if (defaultStrategy == DefaultStrategyType.Uniform) {
            for (MDPAction action : actions) {
                MDPStateActionMarginal mdp = new MDPStateActionMarginal(state, action);
                Map<MDPState, Double> successors = getAllSuccessors(mdp);
                for (MDPState suc : successors.keySet()) {
                    Map<MDPStateActionMarginal, Double> opSuc = calculateDefaultUtility(suc, successors.get(suc)*prob/actions.size(), opponentsMarginals, defUtilityCache);
                    for (MDPStateActionMarginal m : opSuc.keySet()) {
                        double currentActionValue = result.get(m) + opSuc.get(m) + getUtility(mdp, m, defUtilityCache)*successors.get(suc)*prob/actions.size();
                        result.put(m, currentActionValue);
                    }
                }
            }
        } else if (defaultStrategy == DefaultStrategyType.FirstAction) {
            MDPAction action = actions.get(0);
            MDPStateActionMarginal mdp = new MDPStateActionMarginal(state, action);
            Map<MDPState, Double> successors = getAllSuccessors(mdp);
            for (MDPState suc : successors.keySet()) {
                Map<MDPStateActionMarginal, Double> opSuc = calculateDefaultUtility(suc, successors.get(suc)*prob, opponentsMarginals, defUtilityCache);
                for (MDPStateActionMarginal m : opSuc.keySet()) {
                    double currentActionValue = result.get(m) + opSuc.get(m) + getUtility(mdp, m, defUtilityCache)*successors.get(suc)*prob;
                    result.put(m, currentActionValue);
                }
            }

        }
//        if (save) {
//            Map<MDPStateActionMarginal, Double> store = new HashMap<MDPStateActionMarginal, Double>();
//            for (MDPStateActionMarginal m : new HashSet<MDPStateActionMarginal>(result.keySet())) {
//                store.put(m, result.get(m)/prob);
//            }
//            defaultUtilityCache.put(state, store);
//        }
        return result;
    }

    @Override
    public List<MDPAction> getActions(MDPState state) {
        Set<MDPAction> actions = actionMap.get(state);
        if (actions == null) return null;
        return new ArrayList<MDPAction>(actions);
    }

    @Override
    public Map<MDPState, Double> getSuccessors(MDPStateActionMarginal action) {
        return successorMap.get(action);
    }

    @Override
    public Map<MDPStateActionMarginal, Double> getPredecessors(MDPState state) {
        return predecessorMap.get(state);
    }

    private void generateAllStateActions() {
        LinkedList<MDPState> queue = new LinkedList<MDPState>();
        queue.add(getRootState());
        while (!queue.isEmpty()) {
            MDPState state = queue.poll();
            List<MDPAction> actions = getAllActions(state);
            for (MDPAction a : actions) {
                MDPStateActionMarginal mdpsam = new MDPStateActionMarginal(state, a);
                if (!state.isRoot() && getAllActionStates().contains(mdpsam)) break;
                allStatesActions.add(mdpsam);
                for (Map.Entry<MDPState, Double> e : getAllSuccessors(mdpsam).entrySet()) {
                    queue.addLast(e.getKey());
                }
            }
        }
    }

    @Override
    public Set<MDPStateActionMarginal> getAllActionStates() {
        return allStatesActions;
    }

//    public Map<MDPState, Map<MDPStateActionMarginal, Double>> getDefaultUtilityCache() {
//        return defaultUtilityCache;
//    }

    public Set<MDPStateActionMarginal> addBRStrategy(Map<MDPState, Set<MDPStateActionMarginal>> bestResponse) {
        LinkedList<MDPState> queue = new LinkedList<MDPState>();
        Set<MDPStateActionMarginal> newActions = new HashSet<MDPStateActionMarginal>();
        queue.add(getRootState());
        while (!queue.isEmpty()) {
            MDPState state = queue.poll();
            Set<MDPStateActionMarginal> actions = bestResponse.get(state);
            if (actions != null && !actions.isEmpty()) {
                newActions.addAll(addStateAction(state, actions));
                if (!state.isRoot()) removeDefaultUtilityValues(state);
                for (MDPStateActionMarginal m : actions) {
                    for (MDPState s : getAllSuccessors(m).keySet()) {
                        queue.addLast(s);
                    }
                }
            }
        }
        return newActions;
    }

    /**
     *
     * @param state
     * @param actions
     * @return actions that were not in the strategy before and were actually new
     */
    private Set<MDPStateActionMarginal> addStateAction(MDPState state, Set<MDPStateActionMarginal> actions) {

        Set<MDPAction> alreadyActions = actionMap.get(state);
        Set<MDPStateActionMarginal> newActions = new HashSet<MDPStateActionMarginal>();
        if (alreadyActions == null) alreadyActions = new LinkedHashSet<MDPAction>();

        if (getAllMarginalsInStrategy().containsAll(actions)) {
            return newActions;
        }

        for (MDPStateActionMarginal mdpam : actions) {
            if (getAllMarginalsInStrategy().contains(mdpam)) continue;
            newActions.add(mdpam);
            alreadyActions.add(mdpam.getAction());
            putStrategy(mdpam, 1d);
            Map<MDPState, Double> successors = getAllSuccessors(mdpam);
            successorMap.put(mdpam, successors);

            if (!getStates().contains(mdpam.getState())) {
                addStrategyState(mdpam.getState());
            }

            Map<MDPStateActionMarginal, Double> actionUtility = new HashMap<MDPStateActionMarginal, Double>();

            for (Map.Entry<MDPState, Double> followingStates : successors.entrySet()) {
                // adding a new successor to the restricted game
                Map<MDPStateActionMarginal, Double> p = predecessorMap.get(followingStates.getKey());
                if (p == null) p = new HashMap<MDPStateActionMarginal, Double>();
                p.put(mdpam, followingStates.getValue());
                predecessorMap.put(followingStates.getKey(), p);

                Map<MDPStateActionMarginal, Double> map = calculateDefaultUtility(followingStates.getKey(), 1, opponentsStrategy.getAllActionStates(), null);
                map = opponentsStrategy.adaptAccordingToDefaultPolicy(mdpam, map);

                for (MDPStateActionMarginal OPm : map.keySet()) {
                    if (!actionUtility.containsKey(OPm)) {
                        actionUtility.put(OPm, getUtility(mdpam, OPm));
                    }
                    double value =  actionUtility.get(OPm) + map.get(OPm);
                    if (value != 0) {
                        storeUtilityToCache(mdpam, OPm, value);
                    }
                }
            }

        }

        actionMap.put(state, alreadyActions);
        return newActions;
    }

    private void removeDefaultUtilityValues(MDPState state) {
        for (Map.Entry<MDPStateActionMarginal, Double> precedingStates : predecessorMap.get(state).entrySet()) {
            for (MDPStateActionMarginal OPm : opponentsStrategy.getAllMarginalsInStrategy()) {
                double value = super.getUtility(precedingStates.getKey(), OPm);
                if (value != 0) {
                    storeUtilityToCache(precedingStates.getKey(), OPm, value);
                } else {
                    removeUtilityFromCache(precedingStates.getKey(), OPm);
                }
            }
        }
    }

    @Override
    public double getExpandedStrategy(MDPStateActionMarginal mdpStateActionMarginal) {
        Double result = getStrategyProbability(mdpStateActionMarginal);
        if (result != null)
            return result;
        if (USE_EXPST_CACHE) {
            result = expandedNonZeroStrategy.get(mdpStateActionMarginal);
            if (result == null) result = 0d;
            return result;
        } else {
            if (defaultStrategy == DefaultStrategyType.FirstAction) {
                if (!getAllActions(mdpStateActionMarginal.getState()).get(0).equals(mdpStateActionMarginal.getAction()))
                    return 0; // we have the first-action default strategy, but this action is not the first one in this state according to the expander
            }

            if (getStates().contains(mdpStateActionMarginal.getState())) // there exists this state, but this action is not in the strategy
                return 0;

            Map<MDPStateActionMarginal, Double> predecessors = super.getPredecessors(mdpStateActionMarginal.getState());
            result = 0d;
            for (MDPStateActionMarginal p : predecessors.keySet()) {
                result += getExpandedStrategy(p) * predecessors.get(p);
            }
            if (defaultStrategy == DefaultStrategyType.FirstAction)
                return result;
            else if (defaultStrategy == DefaultStrategyType.Uniform)
                return result/(double)(getActions(mdpStateActionMarginal.getState()).size());
            else assert false;
            return Double.NaN;
        }
    }


    private double getUtility(MDPStateActionMarginal firstPlayerAction, MDPStateActionMarginal secondPlayerAction, Map<Set<MDPStateActionMarginal>, Double> defUtilityCache) {
        if (defUtilityCache == null) {
            return super.getUtility(firstPlayerAction, secondPlayerAction);
        } else {
            HashSet<MDPStateActionMarginal> tmp = new HashSet<MDPStateActionMarginal>();
            tmp.add(firstPlayerAction);
            tmp.add(secondPlayerAction);
            return defUtilityCache.get(tmp);
        }
    }

    public Map<MDPStateActionMarginal, Double> adaptAccordingToDefaultPolicy(MDPStateActionMarginal opponentsAction, Map<MDPStateActionMarginal, Double> valuesForOpponentsAction) {
        Map<MDPStateActionMarginal, Double> result = new HashMap<MDPStateActionMarginal, Double>();
        Map<Set<MDPStateActionMarginal>, Double> tmpUtility = new HashMap<Set<MDPStateActionMarginal>, Double>();

        for (MDPStateActionMarginal myAction : valuesForOpponentsAction.keySet()) {
            HashSet<MDPStateActionMarginal> tmp = new HashSet<MDPStateActionMarginal>();
            tmp.add(myAction);
            tmp.add(opponentsAction);
            tmpUtility.put(tmp, valuesForOpponentsAction.get(myAction));
        }

        HashSet<MDPStateActionMarginal> oppActions = new HashSet<MDPStateActionMarginal>();
        oppActions.add(opponentsAction);

        for (MDPStateActionMarginal myActionInStrategy : getAllMarginalsInStrategy()) {
            double value = valuesForOpponentsAction.get(myActionInStrategy);
            Map<MDPState, Double> successors = getSuccessors(myActionInStrategy);
            for (MDPState s : successors.keySet()) {
                if (!hasStateASuccessor(s) && hasAllStateASuccessor(s)) {
                    value += successors.get(s) * calculateDefaultUtility(s,1,oppActions, tmpUtility).get(opponentsAction);
                } else {

                }
            }
            result.put(myActionInStrategy, value);
        }

        return result;
    }

    public void recalculateExpandedStrategy() {
        if (!USE_EXPST_CACHE) return;
        expandedNonZeroStrategy.clear();
        LinkedList<Pair<MDPState, Double>> queue = new LinkedList<Pair<MDPState, Double>>();
        queue.add(new Pair<MDPState, Double>(getRootState(),1d));
        while (!queue.isEmpty()) {
            Pair<MDPState, Double> item = queue.poll();
            MDPState state = item.getLeft();
            double prob = item.getRight();
            List<MDPAction> actions = getAllActions(state);
            for (MDPAction a : actions) {
                double newProb = 0;
                MDPStateActionMarginal mdpsam = new MDPStateActionMarginal(state, a);
                if (getStrategyProbability(mdpsam) == null) { // outside RG
                    if ((defaultStrategy == DefaultStrategyType.FirstAction && !actions.get(0).equals(a)) ||
                            (getStates().contains(state))) {
                        continue;
                    } else {
                        if (expandedNonZeroStrategy.get(mdpsam) != null) {
                            newProb = expandedNonZeroStrategy.get(mdpsam);
                        }
                        if (defaultStrategy == DefaultStrategyType.FirstAction) {
                            // if we are here, we must be in the first action
                            newProb += prob;
                        } else if (defaultStrategy == DefaultStrategyType.Uniform) {
                            newProb += prob/(double)actions.size();
                        } else {
                            assert false;
                        }
                    }
                    if (newProb != 0) {
                        expandedNonZeroStrategy.put(mdpsam, newProb);
                    } else {
                        if (expandedNonZeroStrategy.containsKey(mdpsam)) { // if it is a zero value and it was stored previously
                            expandedNonZeroStrategy.remove(mdpsam);
                        }
                        continue;
                    }

                } else {
                    newProb = getStrategyProbability(mdpsam);
                }
                for (Map.Entry<MDPState, Double> e : getAllSuccessors(mdpsam).entrySet()) {
                    queue.addLast(new Pair<MDPState, Double>(e.getKey(), newProb * e.getValue()));
                }
            }
        }
    }

    public Map<MDPStateActionMarginal, Double> getExpandedNonZeroStrategy() {
        return expandedNonZeroStrategy;
    }
}
