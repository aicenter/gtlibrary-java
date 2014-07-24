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


package cz.agents.gtlibrary.nfg.MDP.implementations.pureStrategyOracle;

import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.nfg.MixedStrategy;
import cz.agents.gtlibrary.nfg.MDP.implementations.MDPStateActionMarginal;
import cz.agents.gtlibrary.nfg.MDP.implementations.MDPStrategy;
import cz.agents.gtlibrary.nfg.MDP.implementations.oracle.DefaultStrategyType;
import cz.agents.gtlibrary.nfg.MDP.interfaces.MDPAction;
import cz.agents.gtlibrary.nfg.MDP.interfaces.MDPConfig;
import cz.agents.gtlibrary.nfg.MDP.interfaces.MDPExpander;
import cz.agents.gtlibrary.nfg.MDP.interfaces.MDPState;
import cz.agents.gtlibrary.utils.Pair;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: bosansky
 * Date: 9/9/13
 * Time: 1:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class McMahanMDPStrategy extends MDPStrategy {

    private int hashCode = 0;
    private boolean changed = true;

    private DefaultStrategyType defaultStrategy = DefaultStrategyType.FirstAction;
//    private DefaultStrategyType defaultStrategy = DefaultStrategyType.Uniform;

    public McMahanMDPStrategy(Player player, MDPConfig config, MDPExpander expander) {
        super(player, config, expander);
        strategy = new HashMap<MDPStateActionMarginal, Double>();
    }

    @Override
    public void generateCompleteStrategy() {
        LinkedList<Pair<MDPState, Double>> queue = new LinkedList<Pair<MDPState, Double>>();
        queue.add(new Pair<MDPState, Double>(getRootState(), 1d));
        while (!queue.isEmpty()) {
            Pair<MDPState, Double> item = queue.poll();
            MDPState state = item.getLeft();
            double prob = item.getRight();
            addStrategyState(state);
            List<MDPAction> actions = getActions(state);
            for (MDPAction a : actions) {
                MDPStateActionMarginal mdpsam = new MDPStateActionMarginal(state, a);
                double newValue = 0d;
                double oldValue = (strategy.containsKey(mdpsam)) ? strategy.get(mdpsam) : 0d;
                if (defaultStrategy == DefaultStrategyType.Uniform) {
                    newValue += prob/(double)actions.size();
                } else if (defaultStrategy == DefaultStrategyType.FirstAction) {
                    if (actions.get(0).equals(a)) {
                        newValue += prob;
                    } else {

                    }
                } else {
                    assert false;
                }
                putStrategy(mdpsam,newValue + oldValue);
                if (newValue + oldValue > 0) {
                    Map<MDPState, Double> successors = getSuccessors(mdpsam);
                    for (MDPState s : successors.keySet()) {
                        queue.addLast(new Pair<MDPState, Double>(s, successors.get(s) * newValue));
                    }
                }
            }
        }
    }

    public McMahanMDPStrategy(Player player, MDPConfig config, MDPExpander expander, Map<McMahanMDPStrategy, Double> weightedStrategy, double weightSum) {
        this(player, config, expander);

        int strategies = weightedStrategy.size();

        LinkedList<MDPState> queue = new LinkedList<MDPState>();
        queue.add(getRootState());
        while (!queue.isEmpty()) {
            MDPState state = queue.poll();
            if (!state.isRoot() && getStates().contains(state)) continue;
            addStrategyState(state);
            List<MDPAction> actions = getActions(state);
            for (MDPAction a : actions) {
                MDPStateActionMarginal mdpsam = new MDPStateActionMarginal(state, a);

                double value = 0d;

                for (Map.Entry<McMahanMDPStrategy, Double> item : weightedStrategy.entrySet()) {
                    value += item.getKey().getStrategyProbability(mdpsam) * item.getValue()/weightSum;
                }
                if (value > 0) {
                    putStrategy(mdpsam,value);
                    for (Map.Entry<MDPState, Double> e : getSuccessors(mdpsam).entrySet()) {
                        queue.addLast(e.getKey());
                    }
                }
            }
        }
    }

    public McMahanMDPStrategy(Player player, MDPConfig config, MDPExpander expander, MixedStrategy<McMahanMDPStrategy> mixedStrategy) {
        this(player, config, expander);

        int strategies = mixedStrategy.size();

        LinkedList<MDPState> queue = new LinkedList<MDPState>();
        queue.add(getRootState());
        while (!queue.isEmpty()) {
            MDPState state = queue.poll();
            if (!state.isRoot() && getStates().contains(state)) continue;
            addStrategyState(state);
            List<MDPAction> actions = getActions(state);
            for (MDPAction a : actions) {
                MDPStateActionMarginal mdpsam = new MDPStateActionMarginal(state, a);

                double value = 0d;

                Iterator<Map.Entry<McMahanMDPStrategy, Double>> i = mixedStrategy.iterator();
                while (i.hasNext()) {
                    Map.Entry<McMahanMDPStrategy, Double> item = i.next();
                    value += item.getKey().getStrategyProbability(mdpsam) * item.getValue();
                }
                if (value > 0) {
                    putStrategy(mdpsam,value);
                    for (Map.Entry<MDPState, Double> e : getSuccessors(mdpsam).entrySet()) {
                        queue.addLast(e.getKey());
                    }
                }
            }
        }
    }

    public Double getStrategyProbability(MDPStateActionMarginal mdpStateActionMarginal) {
        if (!strategy.containsKey(mdpStateActionMarginal))
            return 0d;
        else return strategy.get(mdpStateActionMarginal);
    }

    @Override
    public double getExpandedStrategy(MDPStateActionMarginal mdpStateActionMarginal) {
        return getStrategyProbability(mdpStateActionMarginal);
    }

    public McMahanMDPStrategy(Player player, MDPConfig config, MDPExpander expander, Map<MDPState, Set<MDPStateActionMarginal>> bestResponse) {
        this(player, config, expander);

        LinkedList<Pair<MDPState, Double>> queue = new LinkedList<Pair<MDPState, Double>>();
        queue.add(new Pair<MDPState, Double>(getRootState(), 1d));
        while (!queue.isEmpty()) {
            Pair<MDPState, Double> item = queue.poll();
            MDPState state = item.getLeft();
            double prob = item.getRight();
            addStrategyState(state);
            List<MDPAction> actions = getActions(state);
            for (MDPAction a : actions) {
                MDPStateActionMarginal mdpsam = new MDPStateActionMarginal(state, a);

                double newValue = 0;
                double oldValue = (strategy.containsKey(mdpsam)) ? strategy.get(mdpsam) : 0d;

                if (bestResponse.get(state) != null && bestResponse.get(state).contains(mdpsam)) {
                    newValue = prob;
                }
                putStrategy(mdpsam,newValue+oldValue);

                if (newValue+oldValue > 0) {
                    Map<MDPState, Double> successors = getSuccessors(mdpsam);
                    for (MDPState e : successors.keySet()) {
                        queue.addLast(new Pair<MDPState, Double>(e, successors.get(e) * newValue));
                    }
                }
            }
        }

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        McMahanMDPStrategy that = (McMahanMDPStrategy) o;

        if (defaultStrategy != that.defaultStrategy) return false;
        if (!getStrategy().equals(that.getStrategy())) return false;
        return true;
    }

    @Override
    public int hashCode() {
        if (changed) {
            hashCode = getStrategy().hashCode();
            changed = false;
        }
        return hashCode;
    }

    @Override
    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append("MDP Strategy: {");
        for (MDPStateActionMarginal m : getAllMarginalsInStrategy()) {
            if (getStrategyProbability(m) > 0) {
                result.append(m);
                result.append("=");
                result.append(getStrategyProbability(m));
                result.append(",");
            }
        }
        result.append("}");
        return result.toString();
    }
}
