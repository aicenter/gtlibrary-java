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

import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.nfg.MDP.implementations.MDPConfigImpl;
import cz.agents.gtlibrary.nfg.MDP.implementations.MDPStateActionMarginal;
import cz.agents.gtlibrary.nfg.MDP.implementations.MDPStrategy;
import cz.agents.gtlibrary.nfg.MDP.interfaces.MDPAction;
import cz.agents.gtlibrary.nfg.MDP.interfaces.MDPConfig;
import cz.agents.gtlibrary.nfg.MDP.interfaces.MDPExpander;
import cz.agents.gtlibrary.nfg.MDP.interfaces.MDPState;
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
    final public static boolean REGULARIZE_STRATEGIES = false;

    private DefaultStrategyType defaultStrategy = DefaultStrategyType.FirstAction;
//    private DefaultStrategyType defaultStrategy = DefaultStrategyType.Uniform;
    private Map<MDPState, Set<MDPAction>> actionMap = new HashMap<MDPState, Set<MDPAction>>();
    private Map<MDPStateActionMarginal, Map<MDPState, Double>> successorMap = new HashMap<MDPStateActionMarginal, Map<MDPState, Double>>();
    private Map<MDPState, Map<MDPStateActionMarginal, Double>> predecessorMap = new HashMap<MDPState, Map<MDPStateActionMarginal, Double>>();

    private Map<MDPStateActionMarginal, Double> expandedNonZeroStrategy = new HashMap<MDPStateActionMarginal, Double>();
    private Set<MDPStateActionMarginal> expandedStrategy = new HashSet<MDPStateActionMarginal>();

//    private Set<MDPStateActionMarginal> allStatesActions = new HashSet<MDPStateActionMarginal>();
    private MDPStrategy opponentsStrategy = null;

    private static Map<Set<MDPStateActionMarginal>, Double> defaultUtilityValues = null;
    private static Set<MDPStateActionMarginal> lastActions = new HashSet<MDPStateActionMarginal>();
    private static Map<Player, Set<MDPStateActionMarginal>> removedLastActions = new HashMap<Player, Set<MDPStateActionMarginal>>();

    public MDPIterativeStrategy(Player player, MDPConfig config, MDPExpander expander) {
        super(player, config, expander);
        removedLastActions.put(player, new HashSet<MDPStateActionMarginal>());
    }

    public void initIterativeStrategy(MDPStrategy opponentStrategy) {
        if (defaultUtilityValues == null) {
            defaultUtilityValues = new HashMap<Set<MDPStateActionMarginal>, Double>();
        }

        this.opponentsStrategy = opponentStrategy;

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

//    public void generateAllStateActions() {
//        ArrayList<MDPState> queue = new ArrayList<MDPState>();
//        queue.add(getRootState());
//        while (!queue.isEmpty()) {
//            MDPState state = queue.remove(queue.size()-1);
////            queue.remove(state);
//            List<MDPAction> actions = getAllActions(state);
//            for (MDPAction a : actions) {
//                MDPStateActionMarginal mdpsam = new MDPStateActionMarginal(state, a);
//                if (!state.isRoot() && getAllActionStates().contains(mdpsam)) break;
//                allStatesActions.add(mdpsam);
//                for (Map.Entry<MDPState, Double> e : getAllSuccessors(mdpsam).entrySet()) {
//                    queue.add(e.getKey());
//                }
//            }
//        }
//    }

//    @Override
//    public Set<MDPStateActionMarginal> getAllActionStates() {
//        return allStatesActions;
//    }

    private Map<MDPState, Set<MDPStateActionMarginal>> findMissingSequences(MDPState state) {

        Map<MDPState, Set<MDPStateActionMarginal>> bestResponseCandidates = new HashMap<MDPState, Set<MDPStateActionMarginal>>();
        if (state.isTerminal()) return null;
        if (getStates().contains(state)) return bestResponseCandidates;

        boolean needToAddThisState = false;
        MDPAction a = getAllActions(state).get(0);
        MDPStateActionMarginal marginal = new MDPStateActionMarginal(state, a);
        for (Map.Entry<MDPState, Double> e : getAllSuccessors(marginal).entrySet()) {
            Map<MDPState, Set<MDPStateActionMarginal>> tmp = findMissingSequences(e.getKey());
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
    protected Set<MDPStateActionMarginal> addStateAction(MDPState state, Set<MDPStateActionMarginal> actions) {

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
        Set<MDPStateActionMarginal> newActions = new HashSet<MDPStateActionMarginal>();
        queue.add(startingState);
        while (!queue.isEmpty()) {
            MDPState state = queue.poll();
            Set<MDPStateActionMarginal> actions = bestResponse.get(state);
            if (actions != null && !actions.isEmpty()) {
                newActions.addAll(addStateAction(state, actions));
                for (MDPStateActionMarginal m : actions) {
                    boolean willContinue = true;
                    for (MDPState s : getAllSuccessors(m).keySet()) {
                        queue.addLast(s);
                        willContinue &= bestResponse.containsKey(s);
                    }
                    if (!isActionFullyExpandedInRG(m)) {
                        if (!willContinue) {
                            lastActions.add(m);
                        }
                    }
                }
            }
        }

//        for (MDPState state : getStates()) {
//            if (!isFullyExpandedInRG(state)) {
//                Map<MDPState, Set<MDPStateActionMarginal>> tmp = findMissingSequences(state);
//                if (tmp != null && tmp.size() > 0) {
//                    Set<MDPStateActionMarginal> tmptmp = addBRStrategy(state, tmp);
//                    if (tmptmp != null) {
//                        newActions.addAll(tmptmp);
//                    }
//                }
//            }
//        }
        Map<MDPState, Set<MDPStateActionMarginal>> missingActions = new HashMap<MDPState, Set<MDPStateActionMarginal>>();
        for (MDPStateActionMarginal marginal : lastActions) {
            if (!marginal.getPlayer().equals(getPlayer())) continue;
            if (!isActionFullyExpandedInRG(marginal)) {
                for (MDPState state : getAllSuccessors(marginal).keySet()) {
                    Map<MDPState, Set<MDPStateActionMarginal>> missingSequencesFromThisState = findMissingSequences(state);
                    if (missingSequencesFromThisState != null) {
                        for (MDPState s : missingSequencesFromThisState.keySet()) {
                            Set<MDPStateActionMarginal> tmp = missingActions.get(s);
                            if (tmp == null) tmp = new HashSet<MDPStateActionMarginal>();
                            tmp.addAll(missingSequencesFromThisState.get(s));
                            missingActions.put(s,tmp);
                        }
                    }
                }
            }
        }
        for (MDPState ss : missingActions.keySet()) {
            queue.add(ss);
            while (!queue.isEmpty()) {
                MDPState state = queue.poll();
                Set<MDPStateActionMarginal> actions = missingActions.get(state);
                if (actions != null && !actions.isEmpty()) {
                    newActions.addAll(addStateAction(state, actions));
                    for (MDPStateActionMarginal m : actions) {
                        boolean willContinue = true;
                        for (MDPState s : getAllSuccessors(m).keySet()) {
                            queue.addLast(s);
                            willContinue &= missingActions.containsKey(s);
                        }
                        if (!isActionFullyExpandedInRG(m)) {
                            if (!willContinue) {
                                lastActions.add(m);
                            }
                        }
                    }
                }
            }
        }

//        if (startingState.isRoot()) {
            HashSet<MDPStateActionMarginal> maybeToRemove = new HashSet<MDPStateActionMarginal>();
            for (MDPStateActionMarginal a : newActions) {
                if (!a.getState().isRoot()) maybeToRemove.addAll(getPredecessors(a.getState()).keySet());
            }

            for (MDPStateActionMarginal a : maybeToRemove) {
                if (lastActions.contains(a) && isActionFullyExpandedInRG(a)) {
                    removedLastActions.get(getPlayer()).add(a);
                    lastActions.remove(a);
                }
            }
//        }
        return newActions;
    }

    private void updateDefaultUtilityValueForAction(MDPStateActionMarginal myAction, MDPStrategy opponentsStrategy) {
        Map<MDPState, Double> successors = getAllSuccessors(myAction);
        Map<MDPStateActionMarginal, Double> actionUtility = new HashMap<MDPStateActionMarginal, Double>();

        for (MDPStateActionMarginal OPm : opponentsStrategy.getES()) {
            double v = getNonCachedUtility(myAction, OPm);
            if (v != 0) actionUtility.put(OPm, v);
        }
        actionUtility = opponentsStrategy.adaptAccordingToDefaultPolicy(myAction, actionUtility);

        for (Map.Entry<MDPState, Double> followingStates : successors.entrySet()) {
            // adding a new successor to the restricted game
//            Map<MDPStateActionMarginal, Double> p = predecessorMap.get(followingStates.getKey());
//            if (p == null) p = new HashMap<MDPStateActionMarginal, Double>();
//            p.put(myAction, followingStates.getValue());
//            predecessorMap.put(followingStates.getKey(), p);

            if (getStates().contains(followingStates.getKey())) continue;


            Map<MDPStateActionMarginal, Double> map = calculateDefaultUtility(followingStates.getKey(), 1, opponentsStrategy.getES(), null);

            map = opponentsStrategy.adaptAccordingToDefaultPolicy(myAction, map);
            for (MDPStateActionMarginal OPm : map.keySet()) {
                actionUtility.put(OPm, actionUtility.get(OPm) + map.get(OPm)*followingStates.getValue());
            }
        }

        for (MDPStateActionMarginal OPm : actionUtility.keySet()) {
            if (!lastActions.contains(OPm) && !lastActions.contains(myAction)) {
                removeDefaultUtilityFromCache(myAction, OPm);
                continue;
            }
//            storeDefaultUtilityToCache(myAction, OPm, actionUtility.get(OPm));
            if (actionUtility.get(OPm) != 0) {
                storeDefaultUtilityToCache(myAction, OPm, actionUtility.get(OPm));
            } else {
                removeDefaultUtilityFromCache(myAction, OPm);
            }
        }
    }

//    private MDPState isThereDefaultStrategyPredecessor(MDPState state, boolean firstCall) {
//        if (state.isRoot()) return null;
//        if (!firstCall && getStates().contains(state)) return state;
//        Map<MDPStateActionMarginal, Double> preds = getAllPredecessors(state);
//        for (MDPStateActionMarginal a : preds.keySet()) {
//            if (!getAllMarginalsInStrategy().contains(a)) { // this action cannot be in RG
//                MDPState predState = a.getState();
//                if (getAllActions(predState).get(0).equals(a)) {
//                    MDPState p = isThereDefaultStrategyPredecessor(predState, false);
//                    if (p != null) return p;
//                }
//            }
//        }
//        return null;
//    }

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
                result += getNonCachedUtility(firstPlayerAction, mdp) * secondPlayerStrategy.getExpandedStrategy(mdp);
            }
        } else {
            for (MDPStateActionMarginal mdp : secondPlayerStrategy.getAllMarginalsInStrategy()) {
                result += getNonCachedUtility(firstPlayerAction, mdp) * secondPlayerStrategy.getExpandedStrategy(mdp);
            }
        }
        return result;
    }

    public double getUtility(MDPStateActionMarginal firstPlayerAction, MDPStateActionMarginal secondPlayerAction) {
        HashSet<MDPStateActionMarginal> tmp = new HashSet<MDPStateActionMarginal>();
        tmp.add(firstPlayerAction);
        tmp.add(secondPlayerAction);
        if (lastActions.contains(firstPlayerAction) || lastActions.contains(secondPlayerAction)) {
            if (defaultUtilityValues.containsKey(tmp))
                return defaultUtilityValues.get(tmp);
            else return 0;
        } else {
            return getNonCachedUtility(firstPlayerAction, secondPlayerAction);
        }
    }

    private double getUtility(MDPStateActionMarginal firstPlayerAction, MDPStateActionMarginal secondPlayerAction, Map<Set<MDPStateActionMarginal>, Double> defUtilityCache) {
        if (defUtilityCache == null) {
            return getNonCachedUtility(firstPlayerAction, secondPlayerAction);
        } else {
            HashSet<MDPStateActionMarginal> tmp = new HashSet<MDPStateActionMarginal>();
            tmp.add(firstPlayerAction);
            tmp.add(secondPlayerAction);
            if (!defUtilityCache.containsKey(tmp))
                return 0d;
            else
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
            Double value = valuesForOpponentsAction.get(myActionInStrategy);
            if (value == null) value = 0d;
            Map<MDPState, Double> successors = getAllSuccessors(myActionInStrategy);
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
            if (p > MDPConfigImpl.getEpsilon()/100.0) {
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

    public void lastActionsTest() {
        for (Set<MDPStateActionMarginal> p : defaultUtilityValues.keySet()) {
            Iterator<MDPStateActionMarginal> i = p.iterator();
            MDPStateActionMarginal a1 = i.next();
            MDPStateActionMarginal a2 = i.next();
            if (!lastActions.contains(a1) && !lastActions.contains(a2))
                assert false;
        }
    }

    public void sanityCheck() {
//        for (Map.Entry<MDPState, Set<MDPAction>> acs : actionMap.entrySet()) {
//            for (MDPAction a : acs.getValue()) {
//                if (!strategy.keySet().contains(new MDPStateActionMarginal(acs.getKey(), a)))
//                    assert false;
//            }
//        }


        for (MDPStateActionMarginal marginal : getAllMarginalsInStrategy()) {
//            if (getStrategy().containsKey(marginal)) {
                if (lastActions.contains(marginal) && isActionFullyExpandedInRG(marginal))
                    assert false;
                if (!isActionFullyExpandedInRG(marginal) && !lastActions.contains(marginal))
                    assert false;
//            }
            if (getStrategy().containsKey(marginal)) {
                if (expandedNonZeroStrategy.containsKey(marginal))
                    assert (Math.abs(getStrategyProbability(marginal) - expandedNonZeroStrategy.get(marginal)) < MDPConfigImpl.getEpsilon());
                else
                    if (getStrategyProbability(marginal) > MDPConfigImpl.getEpsilon())
                        assert false;
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

            if (getStates().contains(s)) {
                for (MDPAction a : getActions(s)) {
                    MDPStateActionMarginal suc = new MDPStateActionMarginal(s, a);
                    if (expandedNonZeroStrategy.containsKey(suc))
                        rs += expandedNonZeroStrategy.get(suc);
                }
            } else {
                rs += getExpandedStrategy(marginal);
            }

            if (Math.abs(ls - rs) > MDPConfigImpl.getEpsilon()) {
                System.out.println(expandedNonZeroStrategy);
                assert false;
            }
        }
    }

    public void testUtility(MDPIterativeStrategy otherStrategy, double sol) {
//        if (getUtilityCache().size() > getAllMarginalsInStrategy().size() * otherStrategy.getAllMarginalsInStrategy().size())
//            assert false;
//        for (MDPStateActionMarginal a1 : getAllMarginalsInStrategy()) {
//            for (MDPStateActionMarginal a2 : otherStrategy.getAllMarginalsInStrategy()) {
//                double utility = getUtility(a1, a2);
//                if (getAllSuccessors(a1).size() == getSuccessors(a1).size() && otherStrategy.getAllSuccessors(a2).size() == otherStrategy.getSuccessors(a2).size())
//                    assert (utility == getUtilityFromCache(a1, a2));
//            }
//        }


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
            for (MDPStateActionMarginal a2 : otherStrategy.expandedNonZeroStrategy.keySet()) {
                if (otherStrategy.expandedNonZeroStrategy.containsKey(a2))
                    actionUtility += getNonCachedUtility(a1, a2)*otherStrategy.expandedNonZeroStrategy.get(a2);
            }

            double actionUtility2 = 0d;
            for (MDPStateActionMarginal a2 : otherStrategy.getAllMarginalsInStrategy()) {
                actionUtility2 += getUtility(a1, a2)*otherStrategy.getStrategyProbability(a2);
            }
            if (Math.abs(actionUtility - actionUtility2) > 10*MDPConfigImpl.getEpsilon()) {
                System.out.println(expandedNonZeroStrategy);
                System.out.println(getStrategy());
                System.out.println(otherStrategy.expandedNonZeroStrategy);
                System.out.println(otherStrategy.getStrategy());
                System.out.println(defaultUtilityValues);
                System.out.println(lastActions);

                double tmp = 0d;
                for (Map.Entry<MDPState, Double> e : this.getAllSuccessors(a1).entrySet()) {
                    Map<MDPStateActionMarginal, Double> valueMap = null;
                    if (!hasStateASuccessor(e.getKey()) && hasAllStateASuccessor(e.getKey())) {
                        valueMap = calculateDefaultUtility(e.getKey(), 1d, otherStrategy.expandedNonZeroStrategy.keySet(), null);
                    }
                    if (valueMap != null) {
                        for (MDPStateActionMarginal a2 : valueMap.keySet()) {
                            tmp += valueMap.get(a2)*otherStrategy.expandedNonZeroStrategy.get(a2)*e.getValue();
                        }
                    }
                }

                double tmp2 = 0d;
                for (MDPStateActionMarginal a2 : otherStrategy.getAllMarginalsInStrategy()) {
                    tmp2 += getUtility(a1, a2)*otherStrategy.getStrategyProbability(a2);
                }

                assert false;
            }
        }
        for (MDPStateActionMarginal a1 : expandedNonZeroStrategy.keySet()) {
            double acUt = 0d;
            for (MDPStateActionMarginal a2 : otherStrategy.expandedNonZeroStrategy.keySet()) {
                acUt += getNonCachedUtility(a1, a2)*otherStrategy.expandedNonZeroStrategy.get(a2);
            }
            utility += acUt*expandedNonZeroStrategy.get(a1);
        }
        if (Math.abs(utility - sol) > 10*MDPConfigImpl.getEpsilon()) {
            System.out.println(expandedNonZeroStrategy);
            System.out.println(getStrategy());
            System.out.println(otherStrategy.expandedNonZeroStrategy);
            System.out.println(otherStrategy.getStrategy());
            System.out.println(defaultUtilityValues);
            System.out.println(lastActions);

            assert false;
        }
    }

    public boolean isFullyExpandedInRG(MDPState state) {
        if (state.isTerminal()) return true;
        if (!actionMap.containsKey(state)) {
            if (hasAllStateASuccessor(state)) return false;
            else return true;
        } else {
            if (actionMap.get(state).size() < getAllActions(state).size()) return true;
            HashSet<MDPState> succs = new HashSet<MDPState>();

            for (MDPAction a : getActions(state))
                succs.addAll(getSuccessors(new MDPStateActionMarginal(state, a)).keySet());

            for (MDPState s : succs) {
                if (!getStates().contains(s))
                    return false;
            }
            return true;
        }
    }

    public boolean isActionFullyExpandedInRG(MDPStateActionMarginal marginal) {
        boolean result = true;

//        if (getSuccessors(marginal) == null) {
//            if (getAllSuccessors(marginal) == null) result = true;
//            else result = false;
//        } else {
//            if (getSuccessors(marginal).keySet().size() < getAllSuccessors(marginal).size()) result = false;
//            else if (getSuccessors(marginal).keySet().size() > getAllSuccessors(marginal).size())
//                assert false;
//            else {

                for (MDPState suc : getSuccessors(marginal).keySet()) {
                    if (!suc.isTerminal() && !getStates().contains(suc)) {
                        result = false;
                    }
                }
//            }
//        }

        return result;
    }


//    public double getDefaultUtilityFromCache(MDPStateActionMarginal firstPlayerAction, MDPStateActionMarginal secondPlayerAction) {
//        Set<MDPStateActionMarginal> mdps = new HashSet<MDPStateActionMarginal>();
//        mdps.add(firstPlayerAction);
//        mdps.add(secondPlayerAction);
//        Double v = defaultUtilityValues.get(mdps);
//        if (v == null) v = 0d;
//        return v;
//    }

    public void storeDefaultUtilityToCache(MDPStateActionMarginal firstPlayerAction, MDPStateActionMarginal secondPlayerAction, Double value) {
        Set<MDPStateActionMarginal> mdps = new HashSet<MDPStateActionMarginal>();
        mdps.add(firstPlayerAction);
        mdps.add(secondPlayerAction);
        defaultUtilityValues.put(mdps, value);
    }

    public void removeDefaultUtilityFromCache(MDPStateActionMarginal firstPlayerAction, MDPStateActionMarginal secondPlayerAction) {
        Set<MDPStateActionMarginal> mdps = new HashSet<MDPStateActionMarginal>();
        mdps.add(firstPlayerAction);
        mdps.add(secondPlayerAction);
        defaultUtilityValues.remove(mdps);
    }

    public static void updateDefaultUtilityValues(Set<MDPStateActionMarginal> newActions, MDPIterativeStrategy s1, MDPIterativeStrategy s2) {
        HashSet<MDPStateActionMarginal> toRecalculate = new HashSet<MDPStateActionMarginal>();
        toRecalculate.addAll(removedLastActions.get(s1.getPlayer()));
        toRecalculate.addAll(removedLastActions.get(s2.getPlayer()));

        for (MDPStateActionMarginal marginal : newActions) {
            toRecalculate.add(marginal);
            MDPIterativeStrategy s = null;
            if (marginal.getPlayer().getId() == 0) {
                s = s1;
            } else {
                s = s2;
            }
            if (!marginal.getState().isRoot())
                for (MDPStateActionMarginal m2 : s.getPredecessors(marginal.getState()).keySet()) {
                    if (lastActions.contains(m2)) toRecalculate.add(marginal);
                }
        }

        for (MDPStateActionMarginal marginal : toRecalculate) {
            if (marginal.getPlayer().getId() == 0) {
                s1.updateDefaultUtilityValueForAction(marginal, s2);
            } else {
                s2.updateDefaultUtilityValueForAction(marginal, s1);
            }
        }
    }

    public static void clearRemovedLastActions() {
        for (Player p : removedLastActions.keySet()) {
            removedLastActions.get(p).clear();
        }
    }

    public static Set<MDPStateActionMarginal> getRemovedLastActions(Player player) {
        return removedLastActions.get(player);
    }

    public static Set<MDPStateActionMarginal> getLastActions() {
        return lastActions;
    }

    protected double getNonCachedUtility(MDPStateActionMarginal p1Action, MDPStateActionMarginal p2Action) {
        return super.getUtility(p1Action, p2Action);
    }

    protected void addAction(MDPState state, MDPAction action) {
        Set<MDPAction> alreadyActions = actionMap.get(state);
        if (alreadyActions == null) alreadyActions = new LinkedHashSet<MDPAction>();
        alreadyActions.add(action);
        actionMap.put(state, alreadyActions);
    }

    public MDPStrategy getOpponentsStrategy() {
        return opponentsStrategy;
    }

    public void completeStrategy() {
        expandedStrategy.clear();
        expandedStrategy.addAll(strategy.keySet());
        for (MDPStateActionMarginal a : lastActions) {
            if (!a.getPlayer().equals(getPlayer())) continue;
            ArrayList<MDPState> queue = new ArrayList<MDPState>();
            queue.addAll(getAllSuccessors(a).keySet());
            while (!queue.isEmpty()) {
                MDPState state = queue.remove(queue.size()-1);
                if (state.isTerminal()) continue;
                MDPAction action = getAllActions(state).get(0);
                MDPStateActionMarginal ma = new MDPStateActionMarginal(state, action);
                expandedStrategy.add(ma);
                queue.addAll(getAllSuccessors(ma).keySet());
            }
        }
    }

    public Set<MDPStateActionMarginal> getES() {
        return expandedStrategy;
    }
}
