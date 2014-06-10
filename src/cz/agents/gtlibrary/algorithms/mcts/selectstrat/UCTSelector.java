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
 *
 * @author vilo
 */
public class UCTSelector implements Selector, AlgorithmData, NbSamplesProvider, ActionFrequencyProvider, MeanStrategyProvider {
    private UCTBackPropFactory fact;
    private List<Action> actions;
    double[] v;
    int[] ni;
    int n=0;

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
	if (n<v.length){
            int j = fact.rnd.nextInt(v.length-n);
            int i=0;
            while (j>0){
                i++;
                if (ni[i]==0) j--;
            }
            return i;
        }
        
        double bestVal=-Double.MAX_VALUE;
        int bestIdx=-1;
        for (int i=0; i<v.length;i++){
            double curVal = v[i] + fact.C*Math.sqrt(Math.log(n) / ni[i]);
            if (curVal > bestVal){
                bestVal = curVal;
                bestIdx = i;
            }
        }
        return bestIdx;
    }
    
    @Override
    public void update(int ai, double value) {
        n++;
        ni[ai]++;
        if (ni[ai]==1){
            v[ai] = value;
        } else {
            v[ai] += (value - v[ai])/ni[ai];
        }
    }

    @Override
    public int getNbSamples() {
        return n;
    }

    @Override
    public double[] getActionFreq() {
        double[] out = new double[ni.length];
        if (n==0) Arrays.fill(out, 1.0/out.length);
        else for (int i=0; i< ni.length; i++) out[i]=((double)ni[i])/n;
        return out;
    }

    @Override
    public List<Action> getActions() {
        return actions;
    }

    @Override
    public double[] getMp() {
        return getActionFreq();
    }
    
    
}
