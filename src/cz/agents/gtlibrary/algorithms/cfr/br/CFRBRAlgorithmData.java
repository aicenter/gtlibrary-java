package cz.agents.gtlibrary.algorithms.cfr.br;

import cz.agents.gtlibrary.algorithms.mcts.AlgorithmData;
import cz.agents.gtlibrary.algorithms.mcts.distribution.MeanStrategyProvider;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Jakub Cerny on 16/04/2018.
 */
public class CFRBRAlgorithmData implements AlgorithmData, Serializable, MeanStrategyProvider {

    protected List<Action> actions;


    /** regrets (CFR) or strategies (BR) **/
    protected double[] data;

    /** Mean strategy. */
    protected double[] mp;

    protected boolean isBRData;

    protected int iteration;


    public CFRBRAlgorithmData(List<Action> actions, boolean BRData) {
        this.actions = actions;
        data = new double[actions.size()];
        mp = new double[actions.size()];
        isBRData = BRData;
        iteration = -1;

    }

    public HashMap<Action,Double> getStrategy(){
        HashMap<Action,Double> strategy = new HashMap<>();
        double sum = 1.0;
        if (true) {
            sum = 0.0;
            for (double data_i : data) {
                sum += Math.max(0, data_i);
            }
        }
        if (sum <= 0){
            if (isBRData){
                for (int i = 0; i < actions.size(); i++){
                    strategy.put(actions.get(i), i == 0 ? 1.0 : 0.0);
                }
            }
            else {
                for (Action action : actions) {
                    strategy.put(action, 1.0 / data.length);
                }
            }
        } else {
            int i = 0;
            for(Action action : actions){
                strategy.put(action, Math.max(0, data[i] / sum));
                i++;
            }
        }
        return strategy;
    }

    public double[] getStrategyAsList(){
        double[] strategy = new double[data.length];
        double sum = 1.0;
        if (true) {
            sum = 0.0;
            for (double data_i : data) {
                sum += Math.max(0, data_i);
            }
        }
        if (sum <= 0){
            if (isBRData){
                strategy[0] = 1.0;
            }
            else {
                Arrays.fill(strategy, 1.0 / data.length);
            }
        } else {
            for (int i = 0; i < data.length; i++){
                strategy[i] = Math.max(0, data[i] / sum);
            }
        }
        return strategy;
    }

    public double getProbabilityOfPlaying(Action action){
        double sum = 0;
        for (double data_i : data) {
            sum += Math.max(0, data_i);
        }
        if (sum <= 0) return 1.0 / data.length;
        int idx = actions.indexOf(action);
        return Math.max(0, data[idx]) / sum;
    }

    public void updateAllRegrets(double[] Vs, double meanV, double w){
        for (int i=0; i<data.length; i++){
            data[i] += w*(Vs[i]-meanV);
        }
    }

    public void updateMeanStrategy(double[] p, double w){
        for (int i=0; i<data.length; i++){
            mp[i] += w*p[i];
        }
    }


    public void resetData(double value){
        Arrays.fill(data, value);
    }

    public void setData(HashMap<Action, Double> values){
        int i = 0;
        for (Action a : actions){
            data[i] = values.get(a);
            i++;
        }
//        System.out.println("Data set : " + Arrays.toString(data));
    }

    public void setData(int idx, double value){
        data[idx] = value;
    }

    public void setData(Action action, double value){
        if (action == null) System.out.println("null action");
        data[actions.indexOf(action)] = value;
    }

    @Override
    public List<Action> getActions() {
        return actions;
    }

    @Override
    public double[] getMp() {
        if (isBRData) return data;
        return mp;//getStrategyAsList();
    }

    public int getNumActions(){
        return actions.size();
    }

    public void setIteration(int value){
        iteration = value;
    }

    public void increaseIteration(){
        iteration++;
    }

    public int getIteration(){
        return iteration;
    }


//    public void updateAllRegretsViaMultiplication(double[] Vs){
//        for (int i=0; i<r.length; i++){
//            r[i] *= Vs[i];
//        }
//        numStates++;
//    }
//
//    public void updateAllRegretsViaAddition(double[] Vs){
//        for (int i=0; i<r.length; i++){
//            r[i] += Vs[i];
//        }
//        numStates++;
//    }


}
