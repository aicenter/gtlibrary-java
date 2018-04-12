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


package cz.agents.gtlibrary.nfg.MDP.core;

import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.nfg.MDP.implementations.MDPStateActionMarginal;
import cz.agents.gtlibrary.nfg.MDP.implementations.MDPStrategy;
import cz.agents.gtlibrary.nfg.MDP.interfaces.MDPAction;
import cz.agents.gtlibrary.nfg.MDP.interfaces.MDPConfig;
import cz.agents.gtlibrary.nfg.MDP.interfaces.MDPState;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: bosansky
 * Date: 8/16/13
 * Time: 1:04 PM
 * To change this template use File | Settings | File Templates.
 */
public class MDPBestResponse {


    private MDPConfig config;
    private Player player;
    protected Map<MDPState, Double> cachedValues = new HashMap<MDPState, Double>();
    protected Map<MDPState, MDPAction> bestResponseData = new HashMap<MDPState, MDPAction>();


    public MDPBestResponse(MDPConfig config, Player player) {
        this.config = config;
        this.player = player;
    }

    public double calculateBR(MDPStrategy myStrategy, MDPStrategy opponentStrategy) {
        cachedValues.clear();
        bestResponseData.clear();
        return calculateBRValue(myStrategy.getRootState(), myStrategy, opponentStrategy);
    }


    private double calculateBRValue(MDPState state, MDPStrategy myStrategy, MDPStrategy opponentStrategy) {

        if (!myStrategy.hasAllStateASuccessor(state)) { // terminal state
            return 0;
        }

        if (cachedValues.containsKey(state)) {
            return cachedValues.get(state);
        }


        MDPAction bestAction = null;
        double bestValue = (player.getId() == 0) ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;

        for (MDPAction action : myStrategy.getAllActions(state)) {
            MDPStateActionMarginal mdp = new MDPStateActionMarginal(state, action);
            double currentActionValue = myStrategy.getUtility(mdp, opponentStrategy);
            Map<MDPState, Double> successors = myStrategy.getAllSuccessors(mdp);
            for (MDPState suc : successors.keySet()) {
                currentActionValue += calculateBRValue(suc, myStrategy, opponentStrategy) * successors.get(suc);
            }
            if ((player.getId() == 0 && currentActionValue > bestValue) ||
                (player.getId() == 1 && currentActionValue < bestValue)) {
                bestValue = currentActionValue;
                bestAction = action;
            }
        }

        bestResponseData.put(state, bestAction);
        cachedValues.put(state, bestValue);

        return bestValue;
    }

    public Map<MDPState, Set<MDPStateActionMarginal>> extractBestResponse(MDPStrategy myStrategy) {
        Map<MDPState, Set<MDPStateActionMarginal>> result = new HashMap<MDPState, Set<MDPStateActionMarginal>>();
        LinkedList<MDPState> queue = new LinkedList<MDPState>();
        queue.addLast(myStrategy.getRootState());

        while (!queue.isEmpty()) {
            MDPState s = queue.poll();
            if (bestResponseData.containsKey(s)) {
                Set<MDPStateActionMarginal> set = result.get(s);
                if (set == null) set = new HashSet<MDPStateActionMarginal>();
                MDPStateActionMarginal mdp = new MDPStateActionMarginal(s, bestResponseData.get(s));
                set.add(mdp);
                result.put(s, set);
                for (MDPState ss : myStrategy.getAllSuccessors(mdp).keySet()) {
                    queue.addLast(ss);
                }
            }
        }

        return result;
    }

    public Map<MDPState, Double> getCachedValues() {
        return cachedValues;
    }

    public Map<MDPState, MDPAction> getBestResponseData() {
        return bestResponseData;
    }

    public MDPConfig getConfig() {
        return config;
    }

    public Player getPlayer() {
        return player;
    }
}
