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
package cz.agents.gtlibrary.algorithms.mcts.oos;

import cz.agents.gtlibrary.algorithms.mcts.AlgorithmData;
import cz.agents.gtlibrary.algorithms.mcts.distribution.MeanStrategyProvider;
import cz.agents.gtlibrary.algorithms.mcts.distribution.NbSamplesProvider;
import cz.agents.gtlibrary.algorithms.mcts.nodes.LeafNodeImpl;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.LeafNode;
import cz.agents.gtlibrary.interfaces.Action;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;


/**
 *
 * @author vilo
 */
public class OOSAlgorithmData implements AlgorithmData, MeanStrategyProvider, NbSamplesProvider, Serializable{
    public static boolean useEpsilonRM = false;
    public static double epsilon = 0.001;
    protected List<Action> actions;
    /** Mean strategy. */
    protected double[] mp;
    /** Cumulative regret. */
    protected double[] r;
    /** Number of strategy update samples. */
    protected int nbSamples;
    private int actionCount;

    /** enable this flag to gather statistics about the CFV for each action */
    public static boolean gatherActionCFV = true;
    private double[] actionCFV;
//    private double isCFV;
    public boolean track = false;


    public OOSAlgorithmData(int actionCount) {
        mp = new double[actionCount];
        r = new double[actionCount];
        this.actionCount = actionCount;

        if (gatherActionCFV) {
            actionCFV = new double[actionCount];
        }
    }

    public OOSAlgorithmData(List<Action> actions) {
        this.actions = actions;
        this.actionCount = actions.size();
        mp = new double[actions.size()];
        r = new double[actions.size()];

        if (gatherActionCFV) {
            actionCFV = new double[actionCount];
        }
    }

    public OOSAlgorithmData(OOSAlgorithmData data) {
        this.actions = data.actions;
        this.actionCount = data.getActionCount();
        mp = new double[actionCount];
        System.arraycopy(data.mp, 0, mp, 0, data.mp.length);
        r = new double[actionCount];
        System.arraycopy(data.r, 0, r, 0, data.r.length);

        if(gatherActionCFV) {
            actionCFV = new double[actionCount];
            System.arraycopy(data.actionCFV, 0, actionCFV, 0, data.actionCFV.length);
        }
    }

    public void getRMStrategy(double[] output) {
        final int K = actionCount;

        double R = 0;
        for (double ri : r) R += Math.max(0,ri);
        
        if (R <= 0){
            Arrays.fill(output,0,K,1.0/K);
        } else {
            for (int i=0; i<r.length; i++) output[i] = useEpsilonRM ? (1-epsilon)*Math.max(0,r[i])/R + epsilon/K : Math.max(0,r[i])/R;
        }
    }
    
    public double[] getRMStrategy(){
        double[] out = new double[r.length];
        getRMStrategy(out);
        return out;
    }
    
    public void updateRegret(int ai, double u, double pi_, double l, double c, double x){
        double W = u * pi_ / l;

        for (int i=0; i<r.length; i++){
            if (i==ai) r[i] += (c-x)*W;
            else r[i] += -x*W;
        }
    }

    
    public void updateRegretSM(int ai, double W, double pa, double sa){
        for (int i=0; i<r.length; i++){
            if (i==ai) r[i] += W*(1-pa)/sa;
            else r[i] += -W*pa/sa;
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
        nbSamples++;
    }

    public void setRegretAtIndex(int index, double regret) {
        for (int i = 0; i < r.length; i++) {
            r[i] = 0;
        }
        r[index] = regret;
    }

    public void addToRegretAtIndex(int index, double regret) {
        r[index] += regret;
    }


    public void setRegret(double[] r) {
       this.r = r;
   }
        
    @Override
    public List<Action> getActions() {
        return actions;
    }

    public void setActions(List<Action> actions) {
        this.actions = actions;
    }

    @Override
    public double[] getMp() {
        return mp;
    }    

    @Override
    public int getNbSamples() {
        return nbSamples;
    }

    public double[] getRegrets() {
        return r;
    }

    public void clear() {
        Arrays.fill(r, 0);
        Arrays.fill(mp, 0);
        nbSamples=0;
    }

    public int getActionCount() {
        return actionCount;
    }

    public double[] getActionCFV() {
        return actionCFV;
    }

//    public double getIsCFV() {
//        return isCFV;
//    }

//    public void setIsCFV(double isCFV) {
//        this.isCFV = isCFV;
//    }


    public void setFrom(OOSAlgorithmData other) {
//        assert nbSamples == 0 || IntStream.range(0, r.length).allMatch(i -> Math.abs(r[i] - other.r[i]) < 1e-6);
//        assert AutomatedAbstractionAlgorithm.USE_ABSTRACTION || IntStream.range(0, mp.length).allMatch(i -> Math.abs(mp[i] - other.mp[i]) < 1e-6);
        System.arraycopy(other.mp, 0, mp, 0, other.mp.length);
        System.arraycopy(other.r, 0, r, 0, other.r.length);
        nbSamples = other.getNbSamples();
    }

    public void resetData() {
        for (int i = 0; i < actionCount; i++) {
            mp[i] = 0.0;
            r[i] = 0.0;
            if (gatherActionCFV) {
                actionCFV[i] = 0.0;
            }
        }
        nbSamples = 0;
    }

    public double[] getMeanStrategy() {
        double[] meanStrat = new double[mp.length];

        double mpSum = 0;
        for (double d : mp) mpSum += d;

        for (int j = 0; j < mp.length; j++) {
            meanStrat[j] = (mpSum == 0 ? 1.0 / mp.length : mp[j] / mpSum);
        }
        return meanStrat;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OOSAlgorithmData that = (OOSAlgorithmData) o;

        if (nbSamples != that.nbSamples) return false;
        if (actionCount != that.actionCount) return false;
//        if (Double.compare(that.isCFV, isCFV) != 0) return false;
        if (actions != null ? !actions.equals(that.actions) : that.actions != null) return false;
        if (!Arrays.equals(mp, that.mp)) return false;
        if (!Arrays.equals(r, that.r)) return false;
        return Arrays.equals(actionCFV, that.actionCFV);
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = actions != null ? actions.hashCode() : 0;
        result = 31 * result + Arrays.hashCode(mp);
        result = 31 * result + Arrays.hashCode(r);
        result = 31 * result + nbSamples;
        result = 31 * result + actionCount;
        result = 31 * result + Arrays.hashCode(actionCFV);
        return result;
    }
}

