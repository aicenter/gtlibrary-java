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

import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.distribution.MeanStrategyProvider;
import cz.agents.gtlibrary.algorithms.mcts.distribution.NbSamplesProvider;
import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNode;
import cz.agents.gtlibrary.interfaces.Action;
import java.util.Arrays;
import java.util.List;

/**
 * Regret matching in unknown game setting based on Hart and Mas-Colell 2001.
 * @author vilo
 */
public class RMSelector implements Selector, MeanStrategyProvider,  NbSamplesProvider {
    private RMBackPropFactory fact;
    private List<Action> actions;
    /** Current probability of playing this action. */
    double[] p;
    double[] mp;
    /** Cumulative regret estimate. */
    double[] r;
    /** Current time step. */
    int t=1;
    double d_t;
    public RMSelector(List<Action> actions, RMBackPropFactory fact){
        this(actions.size(), fact);
        this.actions = actions;
    }
    
    public RMSelector(int N, RMBackPropFactory fact){
        this.fact = fact;
        p = new double[N];
        mp = new double[N];
        r = new double[N];
        d_t = fact.gamma;
    }
    
    protected void updateProb() {
        final int K = r.length;
        double R = 0;
        for (double ri : r) R += Math.max(0,ri);
        
        if (R <= 0){
            Arrays.fill(p,1.0/K);
        } else {
            for (int i=0; i<p.length; i++) p[i] = Math.max(0,r[i])/R;
        }
        for (int i=0; i<p.length; i++) mp[i] += p[i];
    }
    
    @Override
    public int select(){
        updateProb();
        //d_t=fact.gamma/Math.sqrt(t);//does not work
        double rand = fact.random.nextDouble();
        for (int i=0; i<p.length; i++) {
            double pa = (1-d_t)*p[i] + d_t/p.length;
            
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
    public void update(int selection, double value) {
        final double v = fact.normalizeValue(value);
        final double pa = (1-d_t)*p[selection] + d_t/p.length;
        r[selection] += v/pa;
        for (int i=0; i<r.length; i++){
            r[i] -= v;
        }
        t++;
    }

    @Override
    public List<Action> getActions() {
        return actions;
    }

    @Override
    public int getNbSamples() {
        double sum = 0;
        for (double d : mp) sum += d;
        return (int) sum;
    }
    
    @Override
    public double[] getMp() {
        return mp;
    }
}
