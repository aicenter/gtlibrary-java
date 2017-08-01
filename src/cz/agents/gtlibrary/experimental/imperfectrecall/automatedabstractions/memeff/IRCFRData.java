package cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.memeff;

import cz.agents.gtlibrary.algorithms.mcts.oos.OOSAlgorithmData;
import cz.agents.gtlibrary.interfaces.Action;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class IRCFRData extends OOSAlgorithmData {

    protected List<double[]> regretUpdates;
    protected List<Double> expPlayerProbs;
    protected boolean updated;

    public IRCFRData(int actionCount) {
        super(actionCount);
        regretUpdates = new ArrayList<>();
        expPlayerProbs = new ArrayList<>();
        updated = false;
    }

    public IRCFRData(List<Action> actions) {
        this(actions.size());
    }

    @Override
    public void updateRegret(int ai, double W, double c, double x) {
        throw new UnsupportedOperationException("Wrong method used");
    }

    public void updateRegret(int actionIndex, double W, double c, double x, double expPlayerProb) {
        double[] regretUpdate = new double[getActionCount()];

        for (int i = 0; i < getActionCount(); i++) {
            if (i == actionIndex)
                regretUpdate[i] += (c - x) * W;
            else
                regretUpdate[i] += -x * W;
        }
        regretUpdates.add(regretUpdate);
        expPlayerProbs.add(expPlayerProb);
        updated = true;
    }

    public void updateAllRegrets(double[] Vs, double meanV, double w, double expPlayerProb) {
        double[] regretUpdate = new double[getActionCount()];

        for (int i = 0; i < getActionCount(); i++) {
            regretUpdate[i] += w * (Vs[i] - meanV);
        }
        regretUpdates.add(regretUpdate);
        expPlayerProbs.add(expPlayerProb);
        updated = true;
    }

    public boolean applyUpdate() {
        boolean oldUpdated = updated;

        normalize(expPlayerProbs);
        for (int i = 0; i < regretUpdates.size(); i++) {
            double[] regretUpdate = regretUpdates.get(i);
            double weight = expPlayerProbs.get(i);

            for (int j = 0; j < regretUpdate.length; j++) {
                r[j] += regretUpdate[j]*weight;
            }
        }
        regretUpdates = new ArrayList<>();
        updated = false;
        return oldUpdated;
    }

    private List<Double> normalize(List<Double> expPlayerProbs) {
        double sum = expPlayerProbs.stream().collect(Collectors.summingDouble(d -> d));

        return expPlayerProbs.stream().mapToDouble(d -> d/sum).boxed().collect(Collectors.toList());
    }

    @Override
    public void updateMeanStrategy(double[] p, double w) {
    }
}
