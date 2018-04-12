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
public class MDPContractingBR extends MDPBestResponse {

    public static boolean PRUNING = false;
    public static int ITERATIONS = 10;

    protected long prunes = 0;

    protected Map<MDPState, Double> cachedLowerBounds = new HashMap<MDPState, Double>();
    protected Map<MDPState, Boolean> cachedIsChange = new HashMap<MDPState, Boolean>();
    protected Map<MDPState, Pair<Double, Double>> cachedValues = new HashMap<MDPState, Pair<Double, Double>>();

    protected Set<MDPState> statesToContract = new HashSet<MDPState>();
    protected Set<MDPState> statesToExpand = new HashSet<MDPState>();

    protected Map<MDPState, Pair<Map<MDPAction, Integer>, Integer>> behavioralStrategies = new HashMap<MDPState, Pair<Map<MDPAction, Integer>, Integer>>();
    protected Map<MDPState, Double> statesProbs = null;

    public MDPContractingBR(MDPConfig config, Player player) {
        super(config, player);
    }

    public double calculateBR(MDPContractingStrategy myStrategy, MDPStrategy opponentStrategy) {
        cachedValues.clear();
        cachedLowerBounds.clear();
        cachedIsChange.clear();
        bestResponseData.clear();
        prunes = 0;

        statesToContract.clear();
        statesToExpand.clear();

        Pair<Pair<Double, Double>, Boolean> result = calculateBRValue(myStrategy.getRootState(), myStrategy, opponentStrategy, getConfig().getBestUtilityValue(getConfig().getOtherPlayer(getPlayer())), 1d);
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
        double outgoingOriginalProbability = 0d;
        Double originalUtility = 0d;
        MDPAction bestAction = null;
        double bestValue = (getPlayer().getId() == 0) ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;

        boolean isThisStateContracted = myStrategy.isStateContracted(state);

        List<MDPAction> actions = myStrategy.getAllActions(state);
        Map<MDPAction, Double> actionValues = new HashMap<MDPAction, Double>();

        for (MDPAction action : actions) {
            MDPStateActionMarginal mdp = new MDPStateActionMarginal(state, action);
            double currentActionValue = myStrategy.getUtility(mdp, opponentStrategy);
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
                        prunes++;
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
//                    if (myStrategy.getExpandedStrategy(mdp) > MDPConfigImpl.getEpsilon())
//                        assert false;
//                    originalUtility = null;
//                }
//                if (originalUtility != null) {
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

        if (DoubleOracleCostPairedMDP.CONTRACTING) {
            if (isThisStateContracted) {
                for (MDPAction a : actions) {
                    if (myStrategy.isActionInFixedSupport(state, a)) {
                        if (Math.abs(actionValues.get(a) - bestValue) > 1e-6) {
                            statesToExpand.add(state); //action has to be a part of the BR -- the reward must be the best -- if not, the state is again expanded
                        }
                    } else {
                        if (Math.abs(actionValues.get(a) - bestValue) <= 1e-6) {
                            statesToExpand.add(state); //action cannot be a part of the BR -- the reward must be worse -- if not, the state is again expanded
                        }
                    }
                }
                if (!statesToExpand.contains(state)) {
                    //everything is OK -- this state is not going to be expanded again
                    //we replace the standard action with default
                    bestAction = myStrategy.getDefaultActionForContractedState(state);
                } else {
                    behavioralStrategies.remove(state);
                }
            }  else {
                Double probOfState = statesProbs.get(state);
                if (probOfState != null && probOfState > 1e-8) {
                    Map<MDPAction, Integer> currentBSMapping = myStrategy.createBehavioralStrategyMapping(state, probOfState);
                    int newIterationsValue = 1;
                    if (behavioralStrategies.containsKey(state)) {
                        Map<MDPAction, Integer> storedBSMapping = behavioralStrategies.get(state).getLeft();

                        if (currentBSMapping.equals(storedBSMapping)) {
                            if (behavioralStrategies.get(state).getRight() > ITERATIONS && currentBSMapping.size() > 1) {
                                statesToContract.add(state);
                            }
                            newIterationsValue += behavioralStrategies.get(state).getRight();
                        }
                    }
                    behavioralStrategies.put(state, new Pair<Map<MDPAction, Integer>, Integer>(currentBSMapping, newIterationsValue));
                }
            }
        }

        if (outgoingOriginalProbability < MDPConfigImpl.getEpsilon())
            originalUtility = null;

        if (bestAction != null) {
            if (!changed && !myStrategy.hasStateASuccessor(state) && bestAction.equals(actions.get(0))) changed = false;
            else changed = true;
            if (changed) {
                bestResponseData.put(state, bestAction);
            }
        } else {
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

    public long getPrunes() {
        return prunes;
    }

    public Set<MDPState> getStatesToContract() {
        return statesToContract;
    }

    public Set<MDPState> getStatesToExpand() {
        return statesToExpand;
    }

    public void setStatesProbs(Map<MDPState, Double> statesProbs) {
        this.statesProbs = statesProbs;
    }
}
