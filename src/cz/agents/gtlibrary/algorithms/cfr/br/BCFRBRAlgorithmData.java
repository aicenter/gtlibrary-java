package cz.agents.gtlibrary.algorithms.cfr.br;

import cz.agents.gtlibrary.interfaces.Action;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Jakub Cerny on 05/05/2018.
 */
public class BCFRBRAlgorithmData extends CFRBRAlgorithmData {

    protected HashMap<Integer, double[]> strategies;
    protected HashMap<Integer, Integer> iterations;

    public BCFRBRAlgorithmData(List<Action> actions, boolean BRData, int[] typeIdxs) {
        super(BRData);
        this.actions = actions;

        if (isBRData){
            strategies = new HashMap<>();
            iterations = new HashMap<>();
            for(int i : typeIdxs){
                double[] data = new double[actions.size()];
                data[0] = 1.0;
                strategies.put(i, data);
                iterations.put(i, -1);
            }
        }
        else{
            data = new double[actions.size()];
            mp = new double[actions.size()];
        }

    }

    @Override
    public HashMap<Action,Double> getStrategyOfPlayerIdx(int playerIdx){
        HashMap<Action,Double> strategy = new HashMap<>();

        if (!isBRData || !strategies.containsKey(playerIdx)) return super.getStrategyOfPlayerIdx(playerIdx);


        double sum = 1.0;
        if (true) {
            sum = 0.0;
            for (double data_i : strategies.get(playerIdx)) {
                sum += Math.max(0, data_i);
            }
        }
        if (sum <= 0){
            for (int i = 0; i < actions.size(); i++){
                strategy.put(actions.get(i), i == 0 ? 1.0 : 0.0);
            }
        } else {
            int i = 0;
            double[] data = strategies.get(playerIdx);
            for(Action action : actions){
                strategy.put(action, Math.max(0, data[i] / sum));
                i++;
            }
        }
        return strategy;
    }

    @Override
    public double[] getStrategyOfPlayerAsList(int playerIdx){

        if (!isBRData || !strategies.containsKey(playerIdx)) return super.getStrategyOfPlayerAsList(playerIdx);

        double[] strategy = new double[actions.size()];
        double sum = 1.0;
        if (true) {
            sum = 0.0;
            for (double data_i : strategies.get(playerIdx)) {
                sum += Math.max(0, data_i);
            }
        }
        if (sum <= 0){
            strategy[0] = 1.0;
            System.out.println("setting default strategy");
        } else {
            double[] data = strategies.get(playerIdx);
            for (int i = 0; i < data.length; i++){
                strategy[i] = Math.max(0, data[i] / sum);
            }
        }
        return strategy;
    }


    @Override
    public void resetDataOfPlayer(int playerIdx, double value){
        if (!isBRData || !strategies.containsKey(playerIdx)) {
            Arrays.fill(data, value);
        }
        else{
            double[] data = strategies.get(playerIdx);
            Arrays.fill(data, value);
            strategies.put(playerIdx, data);
        }
    }

    @Override
    public void setDataOfPlayerIdx(int playerIdx, HashMap<Action, Double> values){
        if (!isBRData || !strategies.containsKey(playerIdx)) {
            super.setDataOfPlayerIdx(playerIdx, values);
        }
        else {
            int i = 0;
            double[] data = strategies.get(playerIdx);
            for (Action a : actions) {
                data[i] = values.get(a);
                i++;
            }
            strategies.put(playerIdx, data);
        }
//        System.out.println("Data set : " + Arrays.toString(data));
    }

    @Override
    public void setDataOfPlayerIdx(int playerIdx, int idx, double value){
        if (!isBRData || !strategies.containsKey(playerIdx)) {
            data[idx] = value;
        }
        else{
            strategies.get(playerIdx)[idx] = value;
        }
    }

    @Override
    public void setDataOfPlayerIdx(int playerIdx, Action action, double value){
        if (action == null) System.out.println("null action");
        int actionIdx = actions.indexOf(action);

        setDataOfPlayerIdx(playerIdx, actionIdx, value);
    }

    @Override
    public void setIterationForPlayerIdx(int playerIdx, int value){
        iterations.put(playerIdx, value);
    }

    @Override
    public int getIterationForPlayerIdx(int playerIdx){
        return iterations.get(playerIdx);
    }


}
