package cz.agents.gtlibrary.nfg.experimental.MDP.implementations.oracle;

import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.nfg.experimental.MDP.core.MDPBestResponse;
import cz.agents.gtlibrary.nfg.experimental.MDP.implementations.MDPConfigImpl;
import cz.agents.gtlibrary.nfg.experimental.MDP.implementations.MDPStateActionMarginal;
import cz.agents.gtlibrary.nfg.experimental.MDP.implementations.MDPStrategy;
import cz.agents.gtlibrary.nfg.experimental.MDP.interfaces.MDPAction;
import cz.agents.gtlibrary.nfg.experimental.MDP.interfaces.MDPConfig;
import cz.agents.gtlibrary.nfg.experimental.MDP.interfaces.MDPState;
import cz.agents.gtlibrary.utils.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: bosansky
 * Date: 8/16/13
 * Time: 1:04 PM
 * To change this template use File | Settings | File Templates.
 */
public class MDPEpsilonFristBetterResponse extends MDPFristBetterResponse {

    private long improvedBR = 0;
    private double constant = 0.95;

    public MDPEpsilonFristBetterResponse(MDPConfig config, Player player) {
        super(config, player);
    }

    @Override
    public double calculateBR(MDPStrategy myStrategy, MDPStrategy opponentStrategy) {
        cachedValues.clear();
        cachedLowerBounds.clear();
        cachedIsChange.clear();
        bestResponseData.clear();
        stopSearch = false;
        improvedBR = 0;
        USE_FIRST_BT = USE_FIRST_BT & (Math.abs(MDPUpperBound) < Double.POSITIVE_INFINITY);
//        MDPLowerBound = getLowerBound(myStrategy.getRootState(), myStrategy, opponentStrategy);
        Pair<Pair<Double, Double>, Boolean> result = calculateBRValue(myStrategy.getRootState(), myStrategy, opponentStrategy, MDPLowerBound, 1d);
        if (stopSearch) {
            return MDPUpperBound;
        } else {
            return result.getLeft().getLeft();
        }
    }

    private Pair<Pair<Double, Double>, Boolean> calculateBRValue(MDPState state, MDPStrategy myStrategy, MDPStrategy opponentStrategy, double alpha, double probability) {

        if (!myStrategy.hasAllStateASuccessor(state)) { // terminal state
            return new Pair<Pair<Double, Double>, Boolean>(new Pair<Double, Double>(0d, 0d), false);
        }

        if (cachedValues.containsKey(state)) {
            return new Pair<Pair<Double, Double>, Boolean>(cachedValues.get(state), cachedIsChange.get(state));
        }

        if (USE_FIRST_BT && stopSearch) return new Pair<Pair<Double, Double>, Boolean>(new Pair<Double, Double>(0d,0d), false);

        boolean changed = false;
        double outgoingOriginalProbability = 0d;
        Double originalUtility = 0d;
        MDPAction bestAction = null;
        double bestValue = (getPlayer().getId() == 0) ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
        double worstValue = (getPlayer().getId() == 0) ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
        double avgValue = (getPlayer().getId() == 0) ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;

        List<MDPAction> actions = myStrategy.getAllActions(state);
        for (MDPAction action : actions) {
            MDPStateActionMarginal mdp = new MDPStateActionMarginal(state, action);
            double currentActionValue = myStrategy.getUtility(mdp, opponentStrategy);
            double currentActionWorstValue = myStrategy.getWorstUtility(mdp, opponentStrategy);
            double currentActionAvgValue = myStrategy.getAverageUtility(mdp, opponentStrategy);
            Map<MDPState, Double> successors = myStrategy.getAllSuccessors(mdp);
            double thisCNRemProb = 1d;

            if (myStrategy.getExpandedStrategy(mdp) > MDPConfigImpl.getEpsilon()/10)
                outgoingOriginalProbability += myStrategy.getExpandedStrategy(mdp);

            if (originalUtility != null) {
                originalUtility += currentActionValue * myStrategy.getExpandedStrategy(mdp);
            }

            // evaluating this action
            for (MDPState suc : successors.keySet()) {
                if (PRUNING) {
                    if ((getPlayer().getId() == 0 && currentActionValue + thisCNRemProb * state.horizon()*getConfig().getBestUtilityValue(getPlayer()) < Math.max(alpha, bestValue)) ||
                        (getPlayer().getId() == 1 && currentActionValue + thisCNRemProb * state.horizon()*getConfig().getBestUtilityValue(getPlayer()) > Math.min(alpha, bestValue))) {
                        // this action cannot be better than best action in this state -- we can skip the remaining successors and proceed with another action
                        currentActionValue = (getPlayer().getId() == 0) ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
                        break;
                    }
                }
                thisCNRemProb = thisCNRemProb - successors.get(suc);
                double currentLB = (getPlayer().getId() == 0) ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
                if (PRUNING) {
                    if (getPlayer().getId() == 0) currentLB = Math.max(alpha, bestValue) - thisCNRemProb * state.horizon()*getConfig().getBestUtilityValue(getPlayer());
                    if (getPlayer().getId() == 1) currentLB = Math.min(alpha, bestValue) + thisCNRemProb * state.horizon()*getConfig().getBestUtilityValue(getPlayer());
                }
                Pair<Pair<Double, Double>, Boolean> recursive = calculateBRValue(suc, myStrategy, opponentStrategy, currentLB, probability * successors.get(suc));
                if (recursive.getLeft().getRight() != null) {
                    originalUtility += recursive.getLeft().getRight() * myStrategy.getExpandedStrategy(mdp) * successors.get(suc);
                }
                changed = changed | recursive.getRight();
                currentActionValue += recursive.getLeft().getLeft() * successors.get(suc);
            }

            if (stopSearch) {
                bestAction = action;
                bestValue = currentActionValue;
                break;
            }

            // is this action better?
//            if ((getPlayer().getId() == 0 && (currentActionValue > bestValue || (currentActionWorstValue > worstValue && currentActionValue >= constant*bestValue))) ||
//                (getPlayer().getId() == 1 && (currentActionValue < bestValue || (currentActionWorstValue < worstValue && currentActionValue <= constant*bestValue)))) {
            if ((getPlayer().getId() == 0 && (currentActionValue > bestValue || (currentActionAvgValue > avgValue && currentActionValue >= constant*bestValue))) ||
                (getPlayer().getId() == 1 && (currentActionValue < bestValue || (currentActionAvgValue < avgValue && currentActionValue <= constant*bestValue)))) {

//                if ((getPlayer().getId() == 0 && currentActionWorstValue > worstValue && currentActionValue >= constant*bestValue) ||
//                        (getPlayer().getId() == 1 && currentActionWorstValue < worstValue && currentActionValue <= constant*bestValue)) improvedBR++;
//                improvedBR++;
                bestValue = currentActionValue;
                bestAction = action;
                worstValue = currentActionWorstValue;
                avgValue = currentActionAvgValue;
            }
        }

        if (outgoingOriginalProbability < MDPConfigImpl.getEpsilon())
            originalUtility = null;

        if (bestAction != null) {
            if (!SAVE_DEF && !changed && !myStrategy.hasStateASuccessor(state) && bestAction.equals(actions.get(0))) changed = false;
            else changed = true;
            if (changed) {
                bestResponseData.put(state, bestAction);
            }
            if (USE_FIRST_BT && originalUtility != null) {
                if ((getPlayer().getId() == 0 && bestValue > originalUtility) ||
                    (getPlayer().getId() == 1 && bestValue < originalUtility)) {
                        stopSearch = true;
                        currentBest = currentBest - originalUtility + bestValue * outgoingOriginalProbability;
                }
            }
        } else {
            bestValue = getLowerBound(state, myStrategy, opponentStrategy);
        }
        cachedValues.put(state, new Pair<Double, Double>(bestValue, originalUtility));
        cachedIsChange.put(state, changed);
        return new Pair<Pair<Double, Double>, Boolean>(new Pair<Double, Double>(bestValue, originalUtility),changed);
    }

    public long getImprovedBR() {
        return improvedBR;
    }
}
