package cz.agents.gtlibrary.algorithms.cfr.ir;

import cz.agents.gtlibrary.algorithms.mcts.oos.OOSAlgorithmData;
import cz.agents.gtlibrary.interfaces.Action;

import java.util.List;

public class FixedForIterationData extends OOSAlgorithmData {

    protected double[] regretUpdate;
    protected boolean updated;

    public FixedForIterationData(int actionCount) {
        super(actionCount);
        regretUpdate = new double[r.length];
        updated = false;
    }

    public FixedForIterationData(List<Action> actions) {
        super(actions);
        regretUpdate = new double[r.length];
        updated = false;
    }

    @Override
    public void updateRegret(int ai, double W, double c, double x) {
        for (int i = 0; i < regretUpdate.length; i++) {
            if (i == ai)
                regretUpdate[i] += (c - x) * W;
            else
                regretUpdate[i] += -x * W;
        }
        updated = true;
    }

    public void updateAllRegrets(double[] Vs, double meanV, double w) {
        for (int i = 0; i < regretUpdate.length; i++) {
            regretUpdate[i] += w * (Vs[i] - meanV);
        }
        updated = true;
    }

    public boolean applyUpdate() {
        if(!updated)
            return false;
        for (int i = 0; i < regretUpdate.length; i++) {
            r[i] += regretUpdate[i];
        }
        regretUpdate = new double[r.length];
        updated = false;
        return true;
    }
}
