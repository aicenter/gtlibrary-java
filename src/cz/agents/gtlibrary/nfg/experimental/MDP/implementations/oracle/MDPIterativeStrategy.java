package cz.agents.gtlibrary.nfg.experimental.MDP.implementations.oracle;

import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.nfg.experimental.MDP.implementations.MDPStateActionMarginal;
import cz.agents.gtlibrary.nfg.experimental.MDP.implementations.MDPStrategy;
import cz.agents.gtlibrary.nfg.experimental.MDP.interfaces.MDPAction;
import cz.agents.gtlibrary.nfg.experimental.MDP.interfaces.MDPConfig;
import cz.agents.gtlibrary.nfg.experimental.MDP.interfaces.MDPExpander;
import cz.agents.gtlibrary.nfg.experimental.MDP.interfaces.MDPState;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: bosansky
 * Date: 8/19/13
 * Time: 9:11 AM
 * To change this template use File | Settings | File Templates.
 */
public class MDPIterativeStrategy extends MDPStrategy {
    private DefaultStrategyType defaultStrategy = DefaultStrategyType.FirstAction;
//    private DefaultStrategyType defaultStrategy = DefaultStrategyType.Uniform;
    private Map<MDPState, Set<MDPAction>> actionMap = new HashMap<MDPState, Set<MDPAction>>();
    private Map<MDPStateActionMarginal, Map<MDPState, Double>> successorMap = new HashMap<MDPStateActionMarginal, Map<MDPState, Double>>();
    private Map<MDPState, Map<MDPStateActionMarginal, Double>> predecessorMap = new HashMap<MDPState, Map<MDPStateActionMarginal, Double>>();

    private Map<MDPState, Map<MDPStateActionMarginal, Double>> defaultUtilityCache = new HashMap<MDPState, Map<MDPStateActionMarginal, Double>>();

    private Set<MDPStateActionMarginal> allStatesActions = new HashSet<MDPStateActionMarginal>();
    private Set<MDPStateActionMarginal> opponentStatesActions = new HashSet<MDPStateActionMarginal>();

    public MDPIterativeStrategy(Player player, MDPConfig config, MDPExpander expander) {
        super(player, config, expander);
        generateAllStateActions();
    }

    public void initIterativeStrategy(MDPStrategy opponentStrategy) {
        defaultUtilityCache.clear();
        opponentStatesActions = opponentStrategy.getAllActionStates();
        calculateDefaultUtility(getRootState(), opponentStatesActions, 1);

        MDPStateActionMarginal actionMarginal = new MDPStateActionMarginal(getRootState(), getAllActions(getRootState()).get(0));
        HashSet<MDPStateActionMarginal> tmp = new HashSet<MDPStateActionMarginal>();
        tmp.add(actionMarginal);
        addStateAction(getRootState(), tmp);
    }

    private Map<MDPStateActionMarginal, Double> calculateDefaultUtility(MDPState state, Set<MDPStateActionMarginal> opponentStatesActions, double prob) {
        Map<MDPStateActionMarginal, Double> result = new HashMap<MDPStateActionMarginal, Double>();
        for (MDPStateActionMarginal opAction : opponentStatesActions) {
            result.put(opAction, 0d);
        }
        if (!hasAllStateASuccessor(state)) { // terminal state
            defaultUtilityCache.put(state, result);
            return result;
        }
        if (defaultUtilityCache.containsKey(state)) {
            return defaultUtilityCache.get(state);
        }
        List<MDPAction> actions = getAllActions(state);
        if (defaultStrategy == DefaultStrategyType.Uniform) {
            for (MDPAction action : actions) {
                MDPStateActionMarginal mdp = new MDPStateActionMarginal(state, action);
                Map<MDPState, Double> successors = getAllSuccessors(mdp);
                for (MDPState suc : successors.keySet()) {
                    Map<MDPStateActionMarginal, Double> opSuc = calculateDefaultUtility(suc, opponentStatesActions, successors.get(suc)*prob/actions.size());
                    for (MDPStateActionMarginal m : opSuc.keySet()) {
                        double currentActionValue = result.get(m) + opSuc.get(m) + super.getUtility(mdp, m)*successors.get(suc)*prob/actions.size();
                        result.put(m, currentActionValue);
                    }
                }
            }
        } else if (defaultStrategy == DefaultStrategyType.FirstAction) {
            MDPAction action = actions.get(0);
            MDPStateActionMarginal mdp = new MDPStateActionMarginal(state, action);
            Map<MDPState, Double> successors = getAllSuccessors(mdp);
            for (MDPState suc : successors.keySet()) {
                Map<MDPStateActionMarginal, Double> opSuc = calculateDefaultUtility(suc, opponentStatesActions, successors.get(suc)*prob);
                for (MDPStateActionMarginal m : opSuc.keySet()) {
                    double currentActionValue = result.get(m) + opSuc.get(m) + super.getUtility(mdp, m)*successors.get(suc)*prob;
                    result.put(m, currentActionValue);
                }
            }

        }
        defaultUtilityCache.put(state, result);
        return result;
    }

    @Override
    public List<MDPAction> getActions(MDPState state) {
        return new ArrayList<MDPAction>(actionMap.get(state));
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

    public Map<MDPState, Map<MDPStateActionMarginal, Double>> getDefaultUtilityCache() {
        return defaultUtilityCache;
    }

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

        if (getStrategy().keySet().containsAll(actions)) {
            return newActions;
        }

        for (MDPStateActionMarginal mdpam : actions) {
            if (getStrategy().containsKey(mdpam)) continue;
            newActions.add(mdpam);
            alreadyActions.add(mdpam.getAction());
            putStrategy(mdpam, 1d);
            Map<MDPState, Double> successors = getAllSuccessors(mdpam);
            successorMap.put(mdpam, successors);

            Map<MDPStateActionMarginal, Double> actionUtility = new HashMap<MDPStateActionMarginal, Double>();

            for (Map.Entry<MDPState, Double> followingStates : successors.entrySet()) {
                Map<MDPStateActionMarginal, Double> p = predecessorMap.get(followingStates.getKey());
                if (p == null) p = new HashMap<MDPStateActionMarginal, Double>();
                p.put(mdpam, followingStates.getValue());
                predecessorMap.put(followingStates.getKey(), p);

                for (MDPStateActionMarginal OPm : opponentStatesActions) {
                    if (!actionUtility.containsKey(OPm)) {
                        actionUtility.put(OPm, getUtility(mdpam, OPm));
                    }
//                    double value =  actionUtility.get(OPm) + (defaultUtilityCache.containsKey(followingStates.getKey()) ? defaultUtilityCache.get(followingStates.getKey()).get(OPm) : 0);
                    if (!defaultUtilityCache.containsKey(followingStates.getKey())) {
                        calculateDefaultUtility(followingStates.getKey(), opponentStatesActions, 1);
                    }
                    double value =  actionUtility.get(OPm) + defaultUtilityCache.get(followingStates.getKey()).get(OPm);
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
            for (MDPStateActionMarginal OPm : opponentStatesActions) {
                double value = getUtility(precedingStates.getKey(), OPm);
                if (value != 0) {
                    storeUtilityToCache(precedingStates.getKey(), OPm, value);
                } else {
                    removeUtilityFromCache(precedingStates.getKey(), OPm);
                }
            }
        }
    }
}
