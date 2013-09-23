package cz.agents.gtlibrary.nfg.experimental.MDP.implementations.oracle;

import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.nfg.experimental.MDP.core.MDPBestResponse;
import cz.agents.gtlibrary.nfg.experimental.MDP.implementations.MDPStateActionMarginal;
import cz.agents.gtlibrary.nfg.experimental.MDP.implementations.MDPStrategy;
import cz.agents.gtlibrary.nfg.experimental.MDP.interfaces.MDPAction;
import cz.agents.gtlibrary.nfg.experimental.MDP.interfaces.MDPConfig;
import cz.agents.gtlibrary.nfg.experimental.MDP.interfaces.MDPState;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: bosansky
 * Date: 8/16/13
 * Time: 1:04 PM
 * To change this template use File | Settings | File Templates.
 */
public class MDPFristBetterResponse extends MDPBestResponse {

    public static boolean USE_FIRST_BT = true;
    public static boolean SAVE_DEF = false;
    public static boolean PRUNING = true;

    private double MDPUpperBound = 0;
    private double MDPLowerBound = 0;
    private double currentBest = 0;
    private boolean stopSearch = false;

    private Map<MDPState, Double> cachedLowerBounds = new HashMap<MDPState, Double>();

    public MDPFristBetterResponse(MDPConfig config, Player player) {
        super(config, player);
    }

    public double calculateBR(MDPStrategy myStrategy, MDPStrategy opponentStrategy) {
        cachedValues.clear();
        cachedLowerBounds.clear();
        bestResponseData.clear();
        stopSearch = false;
//        MDPLowerBound = getLowerBound(myStrategy.getRootState(), myStrategy, opponentStrategy);
        return calculateBRValue(myStrategy.getRootState(), myStrategy, opponentStrategy, MDPLowerBound, 1d);
    }

    public void setMDPUpperBound(double MDPUpperBound) {
        this.MDPUpperBound = MDPUpperBound;
    }

    public void setMDPLowerBound(double MDPLowerBound) {
        this.MDPLowerBound = MDPLowerBound;
    }

    public void setCurrentBest(double currentBest) {
        this.currentBest = currentBest;
    }

    private double calculateBRValue(MDPState state, MDPStrategy myStrategy, MDPStrategy opponentStrategy, double alpha, double probability) {

        if (!myStrategy.hasAllStateASuccessor(state)) { // terminal state
            return 0;
        }

        if (cachedValues.containsKey(state)) {
            return cachedValues.get(state);
        }

//        if (USE_FIRST_BT && stopSearch) return getLowerBound(state, myStrategy, opponentStrategy);



        MDPAction bestAction = null;
        double bestValue = (getPlayer().getId() == 0) ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;

        for (MDPAction action : myStrategy.getAllActions(state)) {
            MDPStateActionMarginal mdp = new MDPStateActionMarginal(state, action);
            double currentActionValue = myStrategy.getUtility(mdp, opponentStrategy);
            Map<MDPState, Double> successors = myStrategy.getAllSuccessors(mdp);
            double thisCNRemProb = 1d;

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
                currentActionValue += calculateBRValue(suc, myStrategy, opponentStrategy, currentLB, probability * successors.get(suc)) * successors.get(suc);
            }

            // is this action better?
            if ((getPlayer().getId() == 0 && currentActionValue > bestValue) ||
                (getPlayer().getId() == 1 && currentActionValue < bestValue)) {
                bestValue = currentActionValue;
                bestAction = action;
            }
        }

        if (bestAction != null) {
            bestResponseData.put(state, bestAction);
        } else {
            bestValue = getLowerBound(state, myStrategy, opponentStrategy);
        }
        cachedValues.put(state, bestValue);
        return bestValue;
    }

    private double getLowerBound(MDPState state, MDPStrategy myStrategy, MDPStrategy opponentStrategy) {
        Double result = cachedLowerBounds.get(state);
        if (result != null) return result;

        if (!myStrategy.hasAllStateASuccessor(state)) { // terminal state
            return 0;
        }

        MDPStateActionMarginal mdp = new MDPStateActionMarginal(state, myStrategy.getAllActions(state).get(0));
        result = myStrategy.getUtility(mdp, opponentStrategy);
        Map<MDPState, Double> successors = myStrategy.getAllSuccessors(mdp);
        for (MDPState suc : successors.keySet()) {
            result += getLowerBound(suc, myStrategy, opponentStrategy) * successors.get(suc);
        }

        cachedLowerBounds.put(state, result);

        return result;
    }
}
