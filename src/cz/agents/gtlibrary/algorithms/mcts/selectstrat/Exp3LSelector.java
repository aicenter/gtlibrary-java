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
import cz.agents.gtlibrary.algorithms.mcts.distribution.MeanStrategyProvider;
import cz.agents.gtlibrary.interfaces.Action;
import java.util.List;

/**
 * This is an Exp3 implementation based on (Bubeck 2010). It does not need an exploration,
 * but does not seem to always perform well in trees in practice, for example it converges much slower
 * than regular Exp3 with SM-MCTS on Goofspiel 6.
 * It would be good to understand it better.
 * better.
 * @author vilo
 */
public class Exp3LSelector implements Selector, AlgorithmData, MeanStrategyProvider {
    private Exp3BackPropFactory fact;
    private List<Action> actions;
    /** Current probability of playing this action. */
    public double[] p;
    /** Cumulative reward. */
    double[] l;
    /** Mean strategy. */
    double[] mp;
    /** Current iteration */
    int n=0;

    public Exp3LSelector(List<Action> actions, Exp3BackPropFactory fact) {
        this(actions.size(), fact);
        this.actions = actions;
    }
    
    final int K;
    final double lnK;
    public Exp3LSelector(int N, Exp3BackPropFactory fact) {
        this.fact = fact;
        p = new double[N];
        l = new double[N];
        mp = new double[N];
        K = l.length;
        lnK = Math.log(K);
        for (int i=0; i<K;i++) p[i]=1.0/K;
    }
    
    protected void updateProb() {
        for (int i=0; i < l.length; i++) {
            double denom = 1;
            for (int j=0 ; j < l.length; j++) {
                if (i != j) denom += Math.exp(-Math.sqrt(lnK / (n*K)) * (l[j] - l[i]));
            }
            final double cp = (1 / denom);
            p[i] = cp;
            mp[i]+=cp;
        }
    }
    
    
    @Override
    public int select(){
        if (n>0) updateProb();

        double rand = fact.random.nextDouble();
        
        for (int i=0; i<p.length; i++) {
            if (rand > p[i]) {
                rand -= p[i];
            } else {
                return i;
            }
        }

        assert false;
        return -1;
    }
    
    @Override
    public void update(int ai, double value) {
        l[ai] += (1 - fact.normalizeValue(value)) / p[ai];
        n++;
    }

    @Override
    public List<Action> getActions() {
        return actions;
    }
    
    @Override
    public double[] getMp() {
        return mp;
    }    
    
    
}
