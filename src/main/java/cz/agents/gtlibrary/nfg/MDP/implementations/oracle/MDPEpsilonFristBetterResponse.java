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
import cz.agents.gtlibrary.nfg.MDP.DoubleOracleCostPairedMDP;
import cz.agents.gtlibrary.nfg.MDP.core.MDPBestResponse;
import cz.agents.gtlibrary.nfg.MDP.implementations.MDPConfigImpl;
import cz.agents.gtlibrary.nfg.MDP.implementations.MDPStateActionMarginal;
import cz.agents.gtlibrary.nfg.MDP.implementations.MDPStrategy;
import cz.agents.gtlibrary.nfg.MDP.interfaces.MDPAction;
import cz.agents.gtlibrary.nfg.MDP.interfaces.MDPConfig;
import cz.agents.gtlibrary.nfg.MDP.interfaces.MDPState;
import cz.agents.gtlibrary.utils.Pair;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: bosansky
 * Date: 8/16/13
 * Time: 1:04 PM
 * To change this template use File | Settings | File Templates.
 */
public class MDPEpsilonFristBetterResponse extends MDPBestResponse {

    public double epsilon = 0.1;
    public static boolean FILTER = false;

    protected long prunes = 0;

    protected Map<MDPState, Double> cachedLowerBounds = new HashMap<MDPState, Double>();
    protected Map<MDPState, Boolean> cachedIsChange = new HashMap<MDPState, Boolean>();
    protected Map<MDPState, Pair<Double, Double>> cachedValues = new HashMap<MDPState, Pair<Double, Double>>();

    protected double currentBest = 0;

    public MDPEpsilonFristBetterResponse(MDPConfig config, Player player) {
        super(config, player);
    }

    public MDPIterativeStrategy filterStrategy(MDPStrategy opponentStrategy) {
        MDPIterativeStrategy result = new MDPIterativeStrategy(opponentStrategy.getPlayer(), opponentStrategy.getConfig(), opponentStrategy.getExpander());

        for (Map.Entry<MDPStateActionMarginal, Double> e : ((MDPIterativeStrategy)opponentStrategy).getExpandedNonZeroStrategy().entrySet()) {
            if (e.getValue() > epsilon) {
                result.getExpandedNonZeroStrategy().put(e.getKey(), e.getValue());
            }
        }

        return result;
    }

    public double calculateBR(MDPContractingStrategy myStrategy, MDPStrategy opponentStrategy) {
        cachedValues.clear();
        cachedLowerBounds.clear();
        cachedIsChange.clear();
        bestResponseData.clear();
        prunes = 0;
        Pair<Pair<Double, Double>, Boolean> result = null;
        boolean recalculate = true;
        if (FILTER) {
            MDPIterativeStrategy filteredOpponent = filterStrategy(opponentStrategy);
            result = calculateBRValue(myStrategy.getRootState(), myStrategy, filteredOpponent, getConfig().getBestUtilityValue(getConfig().getOtherPlayer(getPlayer())), 1d);
            Map<MDPState, Set<MDPStateActionMarginal>> bestResponseActions = extractBestResponse(myStrategy);
//            for (Set<MDPStateActionMarginal> s : bestResponseActions.values()) {
                if (!myStrategy.getStates().containsAll(bestResponseActions.keySet())) {
                    recalculate = false;
//                    break;
                }
//            }
        }
        if (recalculate) {
            if (FILTER) {
                cachedValues.clear();
                cachedLowerBounds.clear();
                cachedIsChange.clear();
                bestResponseData.clear();
                epsilon = epsilon / 2.0;
                System.out.println("BR("+getPlayer()+"): recalculating reward ");
            }

            result = calculateBRValue(myStrategy.getRootState(), myStrategy, opponentStrategy, getConfig().getBestUtilityValue(getConfig().getOtherPlayer(getPlayer())), 1d);
        }
        return result.getLeft().getLeft();
    }

    // returns <<best reward, original reward (given current strategy; null if not applicable)>, false/true if there is a change from default strategy
    private Pair<Pair<Double, Double>, Boolean> calculateBRValue(MDPState state, MDPContractingStrategy myStrategy, MDPStrategy opponentStrategy, double alpha, double probability) {

        if (!myStrategy.hasAllStateASuccessor(state)) { // terminal state
            return new Pair<Pair<Double, Double>, Boolean>(new Pair<Double, Double>(0d, 0d), false);
        }

        if (cachedValues.containsKey(state)) {
            return new Pair<Pair<Double, Double>, Boolean>(cachedValues.get(state), cachedIsChange.get(state));
        }

        boolean changed = false;
        Double originalUtility = 0d;
        MDPAction bestAction = null;
        double bestValue = (getPlayer().getId() == 0) ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;

        List<MDPAction> actions = myStrategy.getAllActions(state);
        Map<MDPAction, Double> actionValues = new HashMap<MDPAction, Double>();

//        double thisCNRemProb = 0d;
//        for (MDPAction action : actions) {
//            MDPStateActionMarginal mdp = new MDPStateActionMarginal(state, action);
//            thisCNRemProb += myStrategy.getExpandedStrategy(mdp);
//        }

        for (MDPAction action : actions) {
            MDPStateActionMarginal mdp = new MDPStateActionMarginal(state, action);
            double currentActionValue = myStrategy.getUtility(mdp, opponentStrategy);
            Map<MDPState, Double> successors = myStrategy.getAllSuccessors(mdp);

            if (originalUtility != null) {
                originalUtility += currentActionValue * myStrategy.getExpandedStrategy(mdp);
            }

            // evaluating this action
            for (MDPState suc : successors.keySet()) {

                double currentLB = (getPlayer().getId() == 0) ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;

                Pair<Pair<Double, Double>, Boolean> recursive = calculateBRValue(suc, myStrategy, opponentStrategy, currentLB, probability * successors.get(suc));
                if (recursive.getLeft().getRight() != null) {
                    originalUtility += recursive.getLeft().getRight() * myStrategy.getExpandedStrategy(mdp) * successors.get(suc);
                }
                changed = changed | recursive.getRight();
                currentActionValue += recursive.getLeft().getLeft() * successors.get(suc);
            }

            actionValues.put(action, currentActionValue);

            // is this action better?
            if ((getPlayer().getId() == 0 && currentActionValue > bestValue) ||
                    (getPlayer().getId() == 1 && currentActionValue < bestValue)) {
                bestValue = currentActionValue;
                bestAction = action;
            }

        }

        if (bestAction != null) {
            if (!changed && !myStrategy.hasStateASuccessor(state) && bestAction.equals(actions.get(0))) changed = false;
            else changed = true;
            if (changed) {
                bestResponseData.put(state, bestAction);
            }
        } else {
            assert false;
            bestValue = getLowerBound(state, myStrategy, opponentStrategy);
        }
        cachedValues.put(state, new Pair<Double, Double>(bestValue, originalUtility));
        cachedIsChange.put(state, changed);
        return new Pair<Pair<Double, Double>, Boolean>(new Pair<Double, Double>(bestValue, originalUtility),changed);
    }

    protected double getLowerBound(MDPState state, MDPStrategy myStrategy, MDPStrategy opponentStrategy) {
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

    public void setCurrentBest(double currentBest) {
        this.currentBest = currentBest;
    }
}
