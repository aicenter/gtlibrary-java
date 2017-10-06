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


package cz.agents.gtlibrary.strategy;

import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.iinodes.LinkedListSequenceImpl;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.Pair;

import java.util.*;

public class StrategyImpl extends Strategy {

    private Map<Sequence, Double> strategy;

    public StrategyImpl() {
        strategy = new HashMap<Sequence, Double>();
    }

    public StrategyImpl(Map<Sequence, Double> realizationPlan) {
        strategy = new HashMap<Sequence, Double>(realizationPlan);
    }

    @Override
    public int size() {
        return strategy.size();
    }

    @Override
    public boolean isEmpty() {
        return strategy.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return strategy.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return strategy.containsValue(value);
    }

    @Override
    public Double get(Object key) {
        Double value = strategy.get(key);

        if (value == null) {
            return 0d;
        }
        return value;
    }

    @Override
    public Double put(Sequence key, Double value) {
        if (value == 0)
            return null;
        return strategy.put(key, value);
    }

    @Override
    public Double remove(Object key) {
        return strategy.remove(key);
    }

    @Override
    public void putAll(Map<? extends Sequence, ? extends Double> map) {
        for (Entry<? extends Sequence, ? extends Double> entry : map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        strategy.clear();
    }

    @Override
    public Set<Sequence> keySet() {
        return strategy.keySet();
    }

    @Override
    public Collection<Double> values() {
        return strategy.values();
    }

    @Override
    public Set<Entry<Sequence, Double>> entrySet() {
        return strategy.entrySet();
    }

    @Override
    public Map<Action, Double> getDistributionOfContinuationOf(Sequence sequence, Collection<Action> actions) {
        if (get(sequence) == 0)
            return getMissingSeqDistribution(actions);
        Map<Action, Double> distribution = new HashMap<Action, Double>();
        double sum = 0;

        for (Action action : actions) {
            Double probability = get(getContinuationSequence(sequence, action));

            sum += probability;
            if (probability > 0)
                distribution.put(action, get(getContinuationSequence(sequence, action)));
        }
        if(sum == 0)
            return getMissingSeqDistribution(actions);
        distribution = normalize(distribution);
        return distribution;
    }

    public Sequence getContinuationSequence(Sequence sequence, Action action) {
        Sequence continuationSequence = new LinkedListSequenceImpl(sequence);

        continuationSequence.addLast(action);
        return continuationSequence;
    }

    private Map<Action, Double> normalize(Map<Action, Double> distribution) {
        if (distribution.isEmpty())
            return distribution;
        double sum = getSum(distribution);

        if (sum == 0)
            return getMissingSeqDistribution(distribution.keySet());
        for (Entry<Action, Double> entry : distribution.entrySet()) {
            distribution.put(entry.getKey(), entry.getValue() / sum);
        }
        return distribution;
    }

    private double getSum(Map<Action, Double> distribution) {
        double sum = 0;

        for (Double value : distribution.values()) {
            sum += value;
        }
        return sum;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        StrategyImpl other = (StrategyImpl) obj;
        if (strategy == null) {
            if (other.strategy != null)
                return false;
        } else if (!strategy.equals(other.strategy))
            return false;
        return true;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;

        result = prime * result + ((strategy == null) ? 0 : strategy.hashCode());
        return result;
    }

        

    @Override
    public String toString() {
        return strategy.toString();
    }

    private static boolean briefFancyTostring=true;
    private HashSet<ISKey> printed = new HashSet<ISKey>();
    @Override
    public String fancyToString(GameState root, Expander<? extends InformationSet> expander, Player player) {
            if (briefFancyTostring) printed.clear();
            LinkedList<GameState> queue = new LinkedList<GameState>();
            StringBuilder builder = new StringBuilder();

            queue.add(root);

            while(!queue.isEmpty()) {
                    GameState currentState = queue.removeFirst();

                    for (Action action : expander.getActions(currentState)) {
                            GameState child = currentState.performAction(action);

                            if(currentState.getPlayerToMove().equals(player) 
                                    && (!briefFancyTostring || get(child.getSequenceFor(player)) > 0 && !printed.contains(currentState.getISKeyForPlayerToMove()))) {
                                    builder.append(child.getSequenceFor(player));
                                    builder.append(": ");
                                    builder.append(get(child.getSequenceFor(player)));
                                    builder.append("\n");
                            }
                            if(!child.isGameEnd())
                                    queue.addLast(child);
                    }
                    if (briefFancyTostring) printed.add(currentState.getISKeyForPlayerToMove());
            }
            return builder.toString();
    }

    protected Map<Action, Double> getMissingSeqDistribution(Collection<Action> actions){
        return null;
    };

}
