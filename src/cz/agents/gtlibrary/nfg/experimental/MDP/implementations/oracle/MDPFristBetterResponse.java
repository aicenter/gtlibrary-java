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

    private double bound = 0;
    private double currentBest = 0;
    private boolean stopSearch = false;

    public MDPFristBetterResponse(MDPConfig config, Player player) {
        super(config, player);
    }

    public double calculateBR(MDPStrategy myStrategy, MDPStrategy opponentStrategy) {
        cachedValues.clear();
        bestResponseData.clear();
        stopSearch = false;
        return calculateBRValue(myStrategy.getRootState(), myStrategy, opponentStrategy,0d,1d);
    }

    public void setBound(double bound) {
        this.bound = bound;
    }

    public void setCurrentBest(double currentBest) {
        this.currentBest = currentBest;
    }

    private double calculateBRValue(MDPState state, MDPStrategy myStrategy, MDPStrategy opponentStrategy, double currentVal, double probability) {

        boolean useDefaultStrategy = false;

        if (!myStrategy.hasAllStateASuccessor(state)) { // terminal state
            return 0;
        }

        if (cachedValues.containsKey(state)) {
            return cachedValues.get(state);
        }

        if ((getPlayer().getId() == 0 && (currentVal + probability * state.horizon()*getConfig().getBestUtilityValue(getPlayer())) < currentBest) ||
            (getPlayer().getId() == 1 && (currentVal + probability * state.horizon()*getConfig().getBestUtilityValue(getPlayer())) > currentBest)) {
//            return getConfig().getBestUtilityValue(getConfig().getOtherPlayer(getPlayer()));
//            return 0d;
            useDefaultStrategy = true;
        }

        MDPAction bestAction = null;
        double bestValue = (getPlayer().getId() == 0) ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;

        for (MDPAction action : myStrategy.getAllActions(state)) {
            MDPStateActionMarginal mdp = new MDPStateActionMarginal(state, action);
            double currentActionValue = myStrategy.getUtility(mdp, opponentStrategy);
            Map<MDPState, Double> successors = myStrategy.getAllSuccessors(mdp);
            for (MDPState suc : successors.keySet()) {
                currentActionValue += calculateBRValue(suc, myStrategy, opponentStrategy, currentActionValue + currentVal, probability * successors.get(suc)) * successors.get(suc);
            }
            if ((getPlayer().getId() == 0 && currentActionValue > bestValue) ||
                (getPlayer().getId() == 1 && currentActionValue < bestValue)) {
                bestValue = currentActionValue;
                bestAction = action;
                if (currentVal + bestValue > currentBest) {
                    currentBest = currentVal + bestValue;
                }
            }
            if (stopSearch) break;
            if (useDefaultStrategy) break;
        }

        if (!useDefaultStrategy) {
            bestResponseData.put(state, bestAction);
            cachedValues.put(state, bestValue);
        }


        if ((getPlayer().getId() == 0 && (bestValue + currentVal) > bound) ||
            (getPlayer().getId() == 1 && (bestValue + currentVal) < bound))
            stopSearch = true;

        return bestValue;
    }

}
