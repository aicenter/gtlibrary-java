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
import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNode;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Player;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author vilo
 */
public class Exp3SelectionStrategy implements SelectionStrategy {
    Exp3BackPropFactory fact;
    public List<Action> actions;
    MCTSInformationSet infSet;
    /** Current probability of playing this action. */
    public double[] p;
    /** Cumulative reward. */
    double[] r;
    public Exp3SelectionStrategy(Exp3BackPropFactory fact, MCTSInformationSet infSet){
        this.fact = fact;
        this.infSet = infSet;
        actions = infSet.getAllNodes().iterator().next().getActions();
        p = new double[actions.size()];
        r = new double[actions.size()];
    }

    @Override
    public double onBackPropagate(InnerNode node, Action action, double value) {
        double effValue;
        if (fact.useCFRValues){
            effValue = value / fact.pis.removeLast();
            if (fact.pis.isEmpty()) Arrays.fill(fact.pi,1);
        } else {
            effValue = value;
        }
        
        int i = actions.indexOf(action);
        r[i] += fact.normalizeValue(effValue) / p[i];
        return value;
    }
    
    protected void updateProb() {
        final int K = actions.size();
        final double gamma = fact.gamma;

        for (int i=0; i < r.length; i++) {
            double denom = 1;
            for (int j=0 ; j < r.length; j++) {
                if (i != j) {
                    //denom += Math.exp((gamma / K) * (bpj.r - bpi.r));
                    denom += Math.exp((gamma / K) * (r[j] - r[i]));
                }
            }
            p[i] = (1 - gamma) * (1 / denom) + gamma / K;
        }
    }

    @Override
    public Action select(){
        updateProb();

        double rand = fact.random.nextDouble();
        
        for (int i=0; i<p.length; i++) {
            if (rand > p[i]) {
                rand -= p[i];
            } else {
                if (fact.useCFRValues){
                  fact.pis.add(fact.pi[infSet.getPlayer().getId()]);
                  fact.pi[infSet.getPlayer().getId()] *= p[i];
                }
                return actions.get(i);
            }
        }

        assert false;
        return null;
    }
}
