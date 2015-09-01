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


/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.agents.gtlibrary.algorithms.mcts.selectstrat;

import cz.agents.gtlibrary.algorithms.mcts.AlgorithmData;
import cz.agents.gtlibrary.algorithms.mcts.distribution.ActionFrequencyProvider;
import cz.agents.gtlibrary.algorithms.mcts.distribution.MeanStrategyProvider;
import cz.agents.gtlibrary.algorithms.mcts.distribution.NbSamplesProvider;
import cz.agents.gtlibrary.interfaces.Action;

import java.util.Arrays;
import java.util.List;

/**
 * @author vilo
 */
public class UCTSelector implements Selector, AlgorithmData, NbSamplesProvider, ActionFrequencyProvider, MeanStrategyProvider {
    public static boolean useDeterministicUCT = false;

    private UCTBackPropFactory fact;
    private List<Action> actions;
    double[] v;
    int[] ni;
    int n = 0;

    public UCTSelector(List<Action> actions, UCTBackPropFactory fact) {
        this(actions.size(), fact);
        this.actions = actions;
    }


    public UCTSelector(int N, UCTBackPropFactory fact) {
        this.fact = fact;
        v = new double[N];
        ni = new int[N];
    }

    @Override
    public int select() {
        //random unused action
        if (n < v.length)
            return getRandomUnusedActionIdx();
        if(useDeterministicUCT)
            return getDetBestActionIdx();
        return getUndetBestActionIdx();
    }

    private int getRandomUnusedActionIdx() {
        int j = fact.random.nextInt(v.length - n);
        int i = 0;

        while (j > 0) {
            i++;
            if (ni[i] == 0)
                j--;
        }
        return i;
    }

    private int getDetBestActionIdx() {
        double bestVal = -Double.MAX_VALUE;
        int bestIdx = -1;

        for (int i = 0; i < v.length; i++) {
            double curVal = v[i] + fact.C * Math.sqrt(Math.log(n) / ni[i]);

            if (curVal > bestVal) {
                bestVal = curVal;
                bestIdx = i;
            }
        }
        return bestIdx;
    }

    private int getUndetBestActionIdx() {
        // epsilon is used as the delta for the tie-breaker
        double epsilon = 0.01;
        double bestVal = getMaxValue();
        int epsilonBestCount = getEpsilonBestCount(epsilon, bestVal);

        return getRandomEpsilonBest(epsilon, bestVal, epsilonBestCount);
    }

    private int getRandomEpsilonBest(double epsilon, double bestVal, int epsilonBestCount) {
        int randomIndex = fact.random.nextInt(epsilonBestCount);
        int currentCount = 0;

        for (int i = 0; i < v.length; i++) {
            double curVal = v[i] + fact.C * Math.sqrt(Math.log(n) / ni[i]);

            if (curVal >= bestVal - epsilon) {
                if (currentCount == randomIndex)
                    return i;
                currentCount++;
            }
        }
        return -1;
    }

    private int getEpsilonBestCount(double epsilon, double bestVal) {
        int epsilonBestCount = 0;

        for (int i = 0; i < v.length; i++) {
            double curVal = v[i] + fact.C * Math.sqrt(Math.log(n) / ni[i]);

            if (curVal >= bestVal - epsilon)
                epsilonBestCount++;
        }
        return epsilonBestCount;
    }

    private double getMaxValue() {
        double bestVal = -Double.MAX_VALUE;

        for (int i = 0; i < v.length; i++) {
            double curVal = v[i] + fact.C * Math.sqrt(Math.log(n) / ni[i]);

            if (curVal > bestVal) {
                bestVal = curVal;
            }
        }
        return bestVal;
    }

    @Override
    public void update(int ai, double value) {
        n++;
        ni[ai]++;
        if (ni[ai] == 1) {
            v[ai] = value;
        } else {
            v[ai] += (value - v[ai]) / ni[ai];
        }
    }

    @Override
    public int getNbSamples() {
        return n;
    }

    @Override
    public double[] getActionFreq() {
        double[] out = new double[ni.length];

        if (n == 0)
            Arrays.fill(out, 1.0 / out.length);
        else
            for (int i = 0; i < ni.length; i++)
                out[i] = ((double) ni[i]) / n;
        return out;
    }

    @Override
    public List<Action> getActions() {
        return actions;
    }

    @Override
    public double[] getMp() {
        double[] out = new double[ni.length];
        for (int i = 0; i < ni.length; i++) out[i] = (double) ni[i];
        return out;
    }


}
