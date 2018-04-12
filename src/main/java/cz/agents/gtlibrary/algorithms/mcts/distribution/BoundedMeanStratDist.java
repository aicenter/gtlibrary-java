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


package cz.agents.gtlibrary.algorithms.mcts.distribution;

import cz.agents.gtlibrary.algorithms.mcts.AlgorithmData;
import cz.agents.gtlibrary.interfaces.Action;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class BoundedMeanStratDist implements Distribution {

    protected int numberOfNonZeroActions;
    protected double EPS;

    public BoundedMeanStratDist(int numberOfNonZeroActions){
        this.numberOfNonZeroActions = numberOfNonZeroActions;
    }

    public BoundedMeanStratDist(double EPS){
        this.numberOfNonZeroActions = 0;
        this.EPS = EPS;
    }

    @Override
    public Map<Action, Double> getDistributionFor(AlgorithmData data) {
        MeanStrategyProvider stat = (MeanStrategyProvider) data;
        if (stat == null) return null;
        final double[] mp = stat.getMp();
        double sum = 0;
        for (double d : mp) sum += d;

        Map<Action, Double> distribution = new HashMap<>(stat.getActions().size());
//        System.out.println(multiplayer.length);

        int i = 0;
        for (Action a : stat.getActions())
            distribution.put(a, sum == 0 ? 1.0 / mp.length : mp[i++] / sum);

        Map<Action, Double> sorted = sortByValue(distribution);
//        System.out.println("///");

        if (numberOfNonZeroActions > 0)
            choiceByConstantSize(distribution, sorted);
        else
            choiceByEpsilon(distribution, sorted);

        return distribution;
    }

    private void choiceByConstantSize(Map<Action, Double> distribution, Map<Action, Double> sorted) {
        int i;
        double sum;
        i = 0;
        sum = 0;
        for(Map.Entry<Action, Double> entry : sorted.entrySet()) {
            if (i < numberOfNonZeroActions) sum += entry.getValue();
            else
                distribution.put(entry.getKey(),0.0);
//            System.out.println(entry.getValue());
            i++;
        }

        i = 0;
        for(Map.Entry<Action, Double> entry : sorted.entrySet()) {
            if (i < numberOfNonZeroActions){
                distribution.put(entry.getKey(), sum == 0 ? 1.0 / numberOfNonZeroActions : entry.getValue() / sum);
            }
            else{
                break;
                //distribution.put(entry.getKey(),0.0);
            }
            i++;
//            System.out.println(distribution.get(entry.getKey()));
        }
    }

    private void choiceByEpsilon(Map<Action, Double> distribution, Map<Action, Double> sorted) {
//        System.out.println("By EPS");
        double sum;
        sum = 0;
        while (sum == 0) {
            for (Map.Entry<Action, Double> entry : sorted.entrySet()) {
                if (entry.getValue() > EPS) sum += entry.getValue();
                else
                    distribution.put(entry.getKey(), 0.0);
//            System.out.println(entry.getValue());
            }
            if (sum == 0) EPS -= (EPS/10);
        }
        for(Map.Entry<Action, Double> entry : sorted.entrySet()) {
            if (entry.getValue() > EPS){
                distribution.put(entry.getKey(), sum == 0 ? 1.0 / numberOfNonZeroActions : entry.getValue() / sum);
            }
            else{
                break;
                //distribution.put(entry.getKey(),0.0);
            }
//            System.out.println(distribution.get(entry.getKey()));
        }
    }

    private <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        return map.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

}
