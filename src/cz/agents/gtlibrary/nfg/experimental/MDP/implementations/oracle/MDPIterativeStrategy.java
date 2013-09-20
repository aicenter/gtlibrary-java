package cz.agents.gtlibrary.nfg.experimental.MDP.implementations.oracle;

import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.nfg.experimental.MDP.implementations.MDPConfigImpl;
import cz.agents.gtlibrary.nfg.experimental.MDP.implementations.MDPStateActionMarginal;
import cz.agents.gtlibrary.nfg.experimental.MDP.implementations.MDPStrategy;
import cz.agents.gtlibrary.nfg.experimental.MDP.interfaces.MDPAction;
import cz.agents.gtlibrary.nfg.experimental.MDP.interfaces.MDPConfig;
import cz.agents.gtlibrary.nfg.experimental.MDP.interfaces.MDPExpander;
import cz.agents.gtlibrary.nfg.experimental.MDP.interfaces.MDPState;
import cz.agents.gtlibrary.utils.Pair;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

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

//    private Set<MDPState> openStates = new HashSet<MDPState>();

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
        addBRStrategy(getRootState(), tmp2);
    }


    private Map<MDPStateActionMarginal, Double> calculateDefaultUtility(MDPState state, double prob, Set<MDPStateActionMarginal> opponentsMarginals, Map<Set<MDPStateActionMarginal>, Double> defUtilityCache) {
        Map<MDPStateActionMarginal, Double> result = new HashMap<MDPStateActionMarginal, Double>();

        if (!hasAllStateASuccessor(state) || getStates().contains(state)) { // terminal state
            for (MDPStateActionMarginal opAction : opponentsMarginals) {
                result.put(opAction, 0d);
            }
            return result;
        }

        List<MDPAction> actions = getAllActions(state);
        if (defaultStrategy == DefaultStrategyType.Uniform) {
            throw new NotImplementedException();
//            for (MDPAction action : actions) {
//                MDPStateActionMarginal mdp = new MDPStateActionMarginal(state, action);
//                Map<MDPState, Double> successors = getAllSuccessors(mdp);
//                for (MDPState suc : successors.keySet()) {
//                    Map<MDPStateActionMarginal, Double> opSuc = calculateDefaultUtility(suc, successors.get(suc)*prob/actions.size(), opponentsMarginals, defUtilityCache);
//                    for (MDPStateActionMarginal m : opSuc.keySet()) {
//                        double currentActionValue = result.get(m) + opSuc.get(m)*successors.get(suc) + getUtility(mdp, m, defUtilityCache)*prob/actions.size();
//                        result.put(m, currentActionValue);
//                    }
//                }
//            }
        } else if (defaultStrategy == DefaultStrategyType.FirstAction) {
            MDPAction action = actions.get(0);
            MDPStateActionMarginal mdp = new MDPStateActionMarginal(state, action);
            Map<MDPState, Double> successors = getAllSuccessors(mdp);
            for (MDPStateActionMarginal opAction : opponentsMarginals) {
                result.put(opAction, getUtility(mdp, opAction, defUtilityCache)*prob);
            }
            for (MDPState suc : successors.keySet()) {
                Map<MDPStateActionMarginal, Double> opSuc = calculateDefaultUtility(suc, successors.get(suc)*prob, opponentsMarginals, defUtilityCache);
                for (MDPStateActionMarginal m : opSuc.keySet()) {
                    double currentActionValue = result.get(m) + opSuc.get(m);
                    result.put(m, currentActionValue);
                }
            }

        }

        return result;
    }    //*/

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

    public Map<MDPStateActionMarginal, Double> getAllPredecessors(MDPState state) {
        return super.getPredecessors(state);
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

    private Map<MDPState, Set<MDPStateActionMarginal>> makeMarginalsConvex(MDPState state) {

        Map<MDPState, Set<MDPStateActionMarginal>> bestResponseCandidates = new HashMap<MDPState, Set<MDPStateActionMarginal>>();
        if (state.isTerminal()) return null;
        if (getStates().contains(state)) return bestResponseCandidates;

        boolean needToAddThisState = false;
        MDPAction a = getAllActions(state).get(0);
        MDPStateActionMarginal marginal = new MDPStateActionMarginal(state, a);
        for (Map.Entry<MDPState, Double> e : getAllSuccessors(marginal).entrySet()) {
            Map<MDPState, Set<MDPStateActionMarginal>> tmp = makeMarginalsConvex(e.getKey());
            if (tmp != null) {
                needToAddThisState = true;
                Set<MDPStateActionMarginal> set = bestResponseCandidates.get(state);
                if (set == null) set = new HashSet<MDPStateActionMarginal>();
                set.add(marginal);
                bestResponseCandidates.put(state, set);
                bestResponseCandidates.putAll(tmp);
            }
        }

        if (needToAddThisState)
            return bestResponseCandidates;
        else return null;
    }

    /**
     *
     * @param state
     * @param actions
     * @return actions that were not in the strategy before and were actually new
     */
    private Set<MDPStateActionMarginal> addStateAction(MDPState state, Set<MDPStateActionMarginal> actions, Map<MDPState, Set<MDPStateActionMarginal>> bestResponse) {

        Set<MDPAction> alreadyActions = actionMap.get(state);
        Set<MDPStateActionMarginal> newActions = new HashSet<MDPStateActionMarginal>();
        if (alreadyActions == null) alreadyActions = new LinkedHashSet<MDPAction>();

        if (getAllMarginalsInStrategy().containsAll(actions)) {
            return newActions;
        }

        if (!getStates().contains(state)) {
            addStrategyState(state);
        }

        for (MDPStateActionMarginal mdpam : actions) {
            if (getAllMarginalsInStrategy().contains(mdpam)) continue;
            newActions.add(mdpam);
            alreadyActions.add(mdpam.getAction());
            putStrategy(mdpam, 1d);
            Map<MDPState, Double> successors = getAllSuccessors(mdpam);
            successorMap.put(mdpam, successors);

            for (Map.Entry<MDPState, Double> followingStates : successors.entrySet()) {
                // adding a new successor to the restricted game
                Map<MDPStateActionMarginal, Double> p = predecessorMap.get(followingStates.getKey());
                if (p == null) p = new HashMap<MDPStateActionMarginal, Double>();
                p.put(mdpam, followingStates.getValue());
                predecessorMap.put(followingStates.getKey(), p);
            }
        }
        actionMap.put(state, alreadyActions);
        return newActions;
    }

    public Set<MDPStateActionMarginal> addBRStrategy(MDPState startingState, Map<MDPState, Set<MDPStateActionMarginal>> bestResponse) {
        LinkedList<MDPState> queue = new LinkedList<MDPState>();
        Set<MDPState> checkStates = new HashSet<MDPState>();
        Set<MDPStateActionMarginal> newActions = new HashSet<MDPStateActionMarginal>();
        queue.add(startingState);
        while (!queue.isEmpty()) {
            MDPState state = queue.poll();
            Set<MDPStateActionMarginal> actions = bestResponse.get(state);
            if (actions != null && !actions.isEmpty()) {
                newActions.addAll(addStateAction(state, actions, bestResponse));
                for (MDPStateActionMarginal m : actions) {
                    for (MDPState s : getAllSuccessors(m).keySet()) {
                        queue.addLast(s);
                    }
                }
            }
        }
        outerloop:
        for (MDPStateActionMarginal a : new HashSet<MDPStateActionMarginal>(getAllMarginalsInStrategy())) {
            for (MDPState state : getAllSuccessors(a).keySet()) {
                if (!hasStateASuccessor(state) && hasAllStateASuccessor(state)) {
                    Map<MDPState, Set<MDPStateActionMarginal>> tmp = makeMarginalsConvex(state);
                    if (tmp != null && tmp.size() > 0) {
                        Set<MDPStateActionMarginal> tmptmp = addBRStrategy(state, tmp);
                        if (tmptmp != null) {
                            newActions.addAll(tmptmp);
                            break outerloop;
                        }
                    }
                }
            }
        }

        if (startingState.isRoot()) {
            for (MDPStateActionMarginal a : getAllMarginalsInStrategy()) {
                updateUtilityValueForAction(a, opponentsStrategy);
            }
        }
        return newActions;
    }

    private void updateUtilityValueForAction(MDPStateActionMarginal myAction, MDPStrategy opponentsStrategy) {
        Map<MDPState, Double> successors = getAllSuccessors(myAction);
        Map<MDPStateActionMarginal, Double> actionUtility = new HashMap<MDPStateActionMarginal, Double>();

        for (MDPStateActionMarginal OPm : opponentsStrategy.getAllMarginalsInStrategy()) {
            actionUtility.put(OPm, getUtility(myAction, OPm));
        }

        for (Map.Entry<MDPState, Double> followingStates : successors.entrySet()) {
            // adding a new successor to the restricted game
            Map<MDPStateActionMarginal, Double> p = predecessorMap.get(followingStates.getKey());
            if (p == null) p = new HashMap<MDPStateActionMarginal, Double>();
            p.put(myAction, followingStates.getValue());
            predecessorMap.put(followingStates.getKey(), p);

            if (getStates().contains(followingStates.getKey())) continue;


            Map<MDPStateActionMarginal, Double> map = calculateDefaultUtility(followingStates.getKey(), 1, opponentsStrategy.getAllActionStates(), null);
            map = opponentsStrategy.adaptAccordingToDefaultPolicy(myAction, map);

            for (MDPStateActionMarginal OPm : map.keySet()) {
                actionUtility.put(OPm, actionUtility.get(OPm) + map.get(OPm)*followingStates.getValue());
            }
        }

        for (MDPStateActionMarginal OPm : actionUtility.keySet()) {
            if (actionUtility.get(OPm) != 0) {
                storeUtilityToCache(myAction, OPm, actionUtility.get(OPm));
            } else {
                removeUtilityFromCache(myAction, OPm);
            }
        }
    }

    private MDPState isThereDefaultStrategyPredecessor(MDPState state, boolean firstCall) {
        if (state.isRoot()) return null;
        if (!firstCall && getStates().contains(state)) return state;
        Map<MDPStateActionMarginal, Double> preds = getAllPredecessors(state);
        for (MDPStateActionMarginal a : preds.keySet()) {
            if (!getAllMarginalsInStrategy().contains(a)) { // this action cannot be in RG
                MDPState predState = a.getState();
                if (getAllActions(predState).get(0).equals(a)) {
                    MDPState p = isThereDefaultStrategyPredecessor(predState, false);
                    if (p != null) return p;
                }
            }
        }
        return null;
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

    @Override
    public double getUtility(MDPStateActionMarginal firstPlayerAction, MDPStrategy secondPlayerStrategy) {
        double result = 0;
        if (secondPlayerStrategy instanceof MDPIterativeStrategy) {
            for (MDPStateActionMarginal mdp : ((MDPIterativeStrategy)secondPlayerStrategy).getExpandedNonZeroStrategy().keySet()) {
                result += getUtility(firstPlayerAction, mdp) * secondPlayerStrategy.getExpandedStrategy(mdp);
            }
        } else {
            for (MDPStateActionMarginal mdp : secondPlayerStrategy.getAllMarginalsInStrategy()) {
                result += getUtility(firstPlayerAction, mdp) * secondPlayerStrategy.getExpandedStrategy(mdp);
            }
        }
        return result;
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
                    value += calculateDefaultUtility(s,successors.get(s),oppActions, tmpUtility).get(opponentsAction);
                } else {

                }
            }
            result.put(myActionInStrategy, value);
        }

        return result;
    }       //*/

    public void recalculateExpandedStrategy() {
        if (!USE_EXPST_CACHE) return;
        expandedNonZeroStrategy.clear();

        LinkedList<Pair<MDPState, Double>> queue = new LinkedList<Pair<MDPState, Double>>();

        for (MDPStateActionMarginal mdpsm : getAllMarginalsInStrategy()) {
            double p = getStrategyProbability(mdpsm);
            if (p > 0) {
                expandedNonZeroStrategy.put(mdpsm, p);
                Map<MDPState, Double> succ = getSuccessors(mdpsm);
                for (Map.Entry<MDPState, Double> e : succ.entrySet()) {
                    if (hasAllStateASuccessor(e.getKey()) && !hasStateASuccessor(e.getKey())) {
                        queue.add(new Pair<MDPState, Double>(e.getKey(),p * e.getValue()));
                    }
                }

            }
        }

        while (!queue.isEmpty()) {
            Pair<MDPState, Double> item = queue.poll();
            MDPState state = item.getLeft();
            double prob = item.getRight();
            List<MDPAction> actions = getAllActions(state);
            for (MDPAction a : actions) {
                double newProb = 0;
                MDPStateActionMarginal mdpsam = new MDPStateActionMarginal(state, a);

                if (getStrategyProbability(mdpsam) == null) {
                    double oldValue = 0d;
                    if ((defaultStrategy == DefaultStrategyType.FirstAction && !actions.get(0).equals(a)) ||
                            (getStates().contains(state))) {
                        continue;
                    } else {
                        if (expandedNonZeroStrategy.get(mdpsam) != null) {
                            oldValue = expandedNonZeroStrategy.get(mdpsam);
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
                        expandedNonZeroStrategy.put(mdpsam, newProb+oldValue);
                    }

                } else {
//                    newProb = getStrategyProbability(mdpsam);
//                    if (newProb != 0) {
//                        double oldValue = 0d;
//                        if (expandedNonZeroStrategy.containsKey(mdpsam)) oldValue = expandedNonZeroStrategy.get(mdpsam);
//                        expandedNonZeroStrategy.put(mdpsam, newProb + oldValue);
//                    }
                }
                if (newProb != 0)
                    for (Map.Entry<MDPState, Double> e : getAllSuccessors(mdpsam).entrySet()) {
                        queue.addLast(new Pair<MDPState, Double>(e.getKey(), newProb * e.getValue()));
                    }
            }
        }
    }

    public Map<MDPStateActionMarginal, Double> getExpandedNonZeroStrategy() {
        return expandedNonZeroStrategy;
    }

    public boolean hasStateASuccessor(MDPState state) {
        return actionMap.containsKey(state);
    }

    public void sanityCheck() {
        for (MDPStateActionMarginal marginal : getAllActionStates()) {
            if (getStrategy().containsKey(marginal)) {
                if (expandedNonZeroStrategy.containsKey(marginal))
                    assert (Math.abs(getStrategyProbability(marginal) - expandedNonZeroStrategy.get(marginal)) < MDPConfigImpl.getEpsilon());
                else
                    assert (getStrategyProbability(marginal) == 0);
                assert (getStates().contains(marginal.getState()));
            } else {

            }
            MDPState s = marginal.getState();
            if (s.isRoot()) continue;
            if (!hasAllStateASuccessor(s)) continue;
            double ls = 0;
            double rs = 0;

            Map<MDPStateActionMarginal, Double> m = getAllPredecessors(s);
            for (MDPStateActionMarginal pred : m.keySet()) {
                if (expandedNonZeroStrategy.containsKey(pred))
                    ls += expandedNonZeroStrategy.get(pred)*m.get(pred);

            }

            for (MDPAction a : getAllActions(s)) {
                MDPStateActionMarginal suc = new MDPStateActionMarginal(s, a);
                if (expandedNonZeroStrategy.containsKey(suc))
                    rs += expandedNonZeroStrategy.get(suc);
            }

            if (Math.abs(ls - rs) > MDPConfigImpl.getEpsilon()) {
                System.out.println(expandedNonZeroStrategy);
                assert false;
            }
        }
    }

    public void testUtility(MDPIterativeStrategy otherStrategy, double sol) {
        double utility = 0d;
        for (MDPStateActionMarginal a1 : getAllMarginalsInStrategy()) {
            double actionUtility = 0d;
            for (Map.Entry<MDPState, Double> e : this.getAllSuccessors(a1).entrySet()) {
                Map<MDPStateActionMarginal, Double> valueMap = null;
                if (!hasStateASuccessor(e.getKey()) && hasAllStateASuccessor(e.getKey())) {
                     valueMap = calculateDefaultUtility(e.getKey(), 1d, otherStrategy.expandedNonZeroStrategy.keySet(), null);
                }
                if (valueMap != null) {
                    for (MDPStateActionMarginal a2 : valueMap.keySet()) {
                        actionUtility += valueMap.get(a2)*otherStrategy.expandedNonZeroStrategy.get(a2)*e.getValue();
                    }
                }
            }
            for (MDPStateActionMarginal a2 : otherStrategy.getAllMarginalsInStrategy()) {
                if (otherStrategy.expandedNonZeroStrategy.containsKey(a2))
                    actionUtility += getUtility(a1, a2)*otherStrategy.expandedNonZeroStrategy.get(a2);
            }

            double actionUtility2 = 0d;
            for (MDPStateActionMarginal a2 : otherStrategy.getAllMarginalsInStrategy()) {
                actionUtility2 += getUtilityFromCache(a1, a2)*otherStrategy.getStrategyProbability(a2);
            }
            if (Math.abs(actionUtility - actionUtility2) > MDPConfigImpl.getEpsilon()) {
                assert false;
            }
        }
        for (MDPStateActionMarginal a1 : expandedNonZeroStrategy.keySet()) {
            double acUt = 0d;
            for (MDPStateActionMarginal a2 : otherStrategy.expandedNonZeroStrategy.keySet()) {
                acUt += getUtility(a1,a2)*otherStrategy.expandedNonZeroStrategy.get(a2);
            }
            utility += acUt*expandedNonZeroStrategy.get(a1);
        }
        if (Math.abs(utility - sol) > MDPConfigImpl.getEpsilon()) {
            System.out.println(expandedNonZeroStrategy);
            System.out.println(otherStrategy.expandedNonZeroStrategy);
            assert false;
        }
    }

//    public DefaultStrategyType getDefaultStrategy() {
//        return defaultStrategy;
//    }

//    public Map<MDPStateActionMarginal, Double> calculateDefaultUtilityFromState(MDPState state, MDPIterativeStrategy opponentsStrategy) {
//        if (defaultStrategy != DefaultStrategyType.FirstAction) throw new NotImplementedException();
//        Map<MDPStateActionMarginal, Double> result = new HashMap<MDPStateActionMarginal, Double>();
//        for (MDPStateActionMarginal opAction : opponentsStrategy.getAllMarginalsInStrategy()) {
//            result.put(opAction, 0d);
//        }
//        if (!hasAllStateASuccessor(state)) { // terminal state
//            return result;
//        }
//        MDPStateActionMarginal mdp = new MDPStateActionMarginal(state, getAllActions(state).get(0));
//        result = getUtilityForDefault(mdp, opponentsStrategy);
//        Map<MDPState, Double> successors = getAllSuccessors(mdp);
//        for (MDPState suc : successors.keySet()) {
//            Map<MDPStateActionMarginal, Double> map = calculateDefaultUtilityFromState(suc, opponentsStrategy);
//            for (MDPStateActionMarginal a : result.keySet()) {
//                result.put(a, result.get(a) + map.get(a)*successors.get(suc));
//            }
//        }
//        return result;
//    }
//
//    public double calculateDefaultUtilityFromMarginal(MDPStateActionMarginal myAction, MDPStateActionMarginal opponentsAction, MDPIterativeStrategy opponentsStrategy) {
//        double result = getUtility(myAction, opponentsAction) * opponentsStrategy.getExpandedStrategy(opponentsAction);
//
//        Map<MDPState, Double> successors = opponentsStrategy.getAllSuccessors(opponentsAction);
//        for (MDPState suc : successors.keySet()) {
//            List<MDPAction> actions = opponentsStrategy.getAllActions(suc);
//            for (MDPAction action : actions) {
//                MDPStateActionMarginal newAction = new MDPStateActionMarginal(suc, action);
//                if (opponentsStrategy.expandedNonZeroStrategy.containsKey(newAction))
//                    result += calculateDefaultUtilityFromMarginal(myAction, newAction, opponentsStrategy);
//            }
//        }
//        return result;
//    }

}
