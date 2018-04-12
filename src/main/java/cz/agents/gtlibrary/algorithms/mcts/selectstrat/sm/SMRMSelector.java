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
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.agents.gtlibrary.algorithms.mcts.selectstrat.sm;

import cz.agents.gtlibrary.algorithms.mcts.AlgorithmData;
import cz.agents.gtlibrary.algorithms.mcts.distribution.MeanStrategyProvider;
import cz.agents.gtlibrary.algorithms.mcts.distribution.NbSamplesProvider;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.utils.Pair;

import java.util.Arrays;
import java.util.List;

/**
 * @author vilo
 */
public class SMRMSelector implements SMSelector, MeanStrategyProvider, NbSamplesProvider {
    SMRMBackPropFactory fact;
    List<Action> actions1;
    List<Action> actions2;
    /**
     * Current probability of playing this action.
     */
    double[][] p;
    double[][] mp;
    /**
     * Cumulative regret.
     */
    double[][] r;
    /**
     * Matrix estimation.
     */
    double[] v;
    int[] n;

    public SMRMSelector(SMRMBackPropFactory fact, List<Action> actions1, List<Action> actions2) {
        this.fact = fact;
        this.actions1 = actions1;
        this.actions2 = actions2;
        int[] len = new int[]{actions1.size(), actions2.size()};
        p = new double[2][];
        mp = new double[2][];
        r = new double[2][];
        for (int i = 0; i < 2; i++) {
            p[i] = new double[len[i]];
            mp[i] = new double[len[i]];
            r[i] = new double[len[i]];
        }
        v = new double[len[0] * len[1]];
        n = new int[v.length];
    }

    public void setP1Actions(List<Action> p1Actions) {
        actions1 = p1Actions;
    }

    protected void updateProb(int a) {
        final int K = r[a].length;
        double R = 0;
        for (double ri : r[a]) R += Math.max(0, ri);

        if (R <= 0) {
            Arrays.fill(p[a], 1.0 / K);
        } else {
            for (int i = 0; i < p[a].length; i++) p[a][i] = Math.max(0, r[a][i]) / R;
        }
        for (int i = 0; i < K; i++) mp[a][i] += p[a][i];
    }

    public int selectOne(int a) {
        double rand = fact.random.nextDouble();
        for (int i = 0; i < p[a].length; i++) {
            double pa = (1 - fact.gamma) * p[a][i] + fact.gamma / p[a].length;

            if (rand > pa) {
                rand -= pa;
            } else {
                return i;
            }
        }
        assert false;
        return -1;
    }

    @Override
    public Pair<Integer, Integer> select() {
        updateProb(0);
        updateProb(1);
        return new Pair<>(selectOne(0), selectOne(1));
    }

    @Override
    public void update(Pair<Integer, Integer> selection, double value) {
        int sgn = (actions1.get(0).getInformationSet().getPlayer().getId() == 0 ? 1 : -1);
        for (int i = 0; i < r[0].length; i++) {
            if (i != selection.getLeft()) {
                final int idx = getIdx(i, selection.getRight());
                r[0][i] += (n[idx] != 0 ? v[idx] / n[idx] : 0) - sgn * value;
            }
        }

        for (int i = 0; i < r[1].length; i++) {
            if (i != selection.getRight()) {
                final int idx = getIdx(selection.getLeft(), i);
                r[1][i] += -(n[idx] != 0 ? v[idx] / n[idx] : 0) + sgn * value;
            }
        }

        final int idx = getIdx(selection.getLeft(), selection.getRight());
        v[idx] += value;
        n[idx]++;
    }

    final int getIdx(int i, int j) {
        return j * r[0].length + i;
    }


    @Override
    public AlgorithmData getBottomData() {
        return new DummyBottom(this);
    }

    @Override
    public List<Action> getActions() {
        return actions1;
    }

    @Override
    public double[] getMp() {
        return mp[0];
    }

    @Override
    public int getNbSamples() {
        int sum = 0;
        for (int i : n) sum += i;
        return sum;
    }


    private class DummyBottom implements AlgorithmData, MeanStrategyProvider, NbSamplesProvider {
        SMRMSelector top;

        public DummyBottom(SMRMSelector top) {
            this.top = top;
        }

        @Override
        public List<Action> getActions() {
            return top.actions2;
        }

        @Override
        public double[] getMp() {
            return top.mp[1];
        }

        @Override
        public int getNbSamples() {
            return top.getNbSamples();
        }


    }
}
