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
    private DefaultStrategyType defaultStrategy = DefaultStrategyType.Uniform;
    private Map<MDPState, MDPAction> actionMap = new HashMap<MDPState, MDPAction>();
    private Map<MDPStateActionMarginal, Map<MDPState, Double>> successorMap = new HashMap<MDPStateActionMarginal, Map<MDPState, Double>>();
    private Map<MDPState, Map<MDPStateActionMarginal, Double>> predecessorMap = new HashMap<MDPState, Map<MDPStateActionMarginal, Double>>();

    private Map<MDPState, Map<MDPStateActionMarginal, Double>> defaultUtilityCache = new HashMap<MDPState, Map<MDPStateActionMarginal, Double>>();
    private Set<MDPStateActionMarginal> allStatesActions = new HashSet<MDPStateActionMarginal>();

    public MDPIterativeStrategy(Player player, MDPConfig config, MDPExpander expander) {
        super(player, config, expander);
        generateAllStateActions();
    }

    public void calculateDefaultUtility(MDPStrategy opponentStrategy) {
        defaultUtilityCache.clear();
        calculateDefaultUtility(getRootState(), opponentStrategy, 1);
    }

    private Map<MDPStateActionMarginal, Double> calculateDefaultUtility(MDPState state, MDPStrategy opponentStrategy, double prob) {
        Map<MDPStateActionMarginal, Double> result = new HashMap<MDPStateActionMarginal, Double>();
        for (MDPStateActionMarginal opAction : opponentStrategy.getAllActionStates()) {
            result.put(opAction, 0d);
        }
        if (!hasAllStateASuccessor(state)) { // terminal state
            return result;
        }
        if (defaultUtilityCache.containsKey(state)) {
            return defaultUtilityCache.get(state);
        }
        List<MDPAction> actions = getActions(state);
        if (defaultStrategy == DefaultStrategyType.Uniform) {
            for (MDPAction action : actions) {
                MDPStateActionMarginal mdp = new MDPStateActionMarginal(state, action);
                Map<MDPState, Double> successors = getAllSuccessors(mdp);
                for (MDPState suc : successors.keySet()) {
                    Map<MDPStateActionMarginal, Double> opSuc = calculateDefaultUtility(suc, opponentStrategy, successors.get(suc)*prob/actions.size());
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
                Map<MDPStateActionMarginal, Double> opSuc = calculateDefaultUtility(suc, opponentStrategy, successors.get(suc)*prob);
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
    public Map<MDPState, Double> getAllSuccessors(MDPStateActionMarginal action) {
        return super.getSuccessors(action);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public Map<MDPState, Double> getSuccessors(MDPStateActionMarginal action) {
        return successorMap.get(action);
    }

    @Override
    public Map<MDPStateActionMarginal, Double> getPredecessors(MDPState state) {
        return predecessorMap.get(state);
    }

    @Override
    public double getUtility(MDPStateActionMarginal firstPlayerAction, MDPStateActionMarginal secondPlayerAction) {
        return 0;
    }

    private void generateAllStateActions() {
        LinkedList<MDPState> queue = new LinkedList<MDPState>();
        queue.add(getRootState());
        while (!queue.isEmpty()) {
            MDPState state = queue.poll();
            List<MDPAction> actions = getActions(state);
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
}
