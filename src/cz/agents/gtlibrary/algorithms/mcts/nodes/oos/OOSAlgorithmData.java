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
package cz.agents.gtlibrary.algorithms.mcts.nodes.oos;

import cz.agents.gtlibrary.algorithms.mcts.AlgorithmData;
import cz.agents.gtlibrary.algorithms.mcts.distribution.MeanStrategyProvider;
import cz.agents.gtlibrary.algorithms.mcts.distribution.NbSamplesProvider;
import cz.agents.gtlibrary.interfaces.Action;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author vilo
 */
public class OOSAlgorithmData implements AlgorithmData, MeanStrategyProvider, NbSamplesProvider {
    public static boolean useEpsilonRM = false; 
    List<Action> actions;
    /** Mean strategy. */
    double[] mp;
    /** Cumulative regret. */
    double[] r;

    public OOSAlgorithmData(List<Action> actions) {
        this.actions = actions;
        mp = new double[actions.size()];
        r = new double[actions.size()];
    }
    
    public void getRMStrategy(double[] output) {
        final int K = actions.size();
        double R = 0;
        for (double ri : r) R += Math.max(0,ri);
        
        if (R <= 0){
            Arrays.fill(output,0,K,1.0/K);
        } else {
            for (int i=0; i<r.length; i++) output[i] = useEpsilonRM ? 0.99*Math.max(0,r[i])/R + 0.01/K : Math.max(0,r[i])/R;
        }
    }
    
    public double[] getRMStrategy(){
        double[] out = new double[r.length];
        getRMStrategy(out);
        return out;
    }
    
    public void updateRegret(int ai, double W, double c, double x){
        for (int i=0; i<r.length; i++){
            if (i==ai) r[i] += (c-x)*W;
            else r[i] += -x*W;
        }
    }
    
    public void updateAllRegrets(double[] Vs, double meanV, double w){
        for (int i=0; i<r.length; i++){
            r[i] += w*(Vs[i]-meanV);
        }
    }

    public void updateMeanStrategy(double[] p, double w){
        for (int i=0; i<r.length; i++){
            mp[i] += w*p[i];
        }
    }
    
        
    @Override
    public List<Action> getActions() {
        return actions;
    }

    @Override
    public double[] getMp() {
        return mp;
    }    

    @Override
    public int getNbSamples() {
        double sum = 0;
        for (double d : mp) sum += d;
        return (int) sum;
    }
}

