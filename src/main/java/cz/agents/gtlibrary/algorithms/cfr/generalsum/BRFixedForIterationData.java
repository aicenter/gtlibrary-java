package cz.agents.gtlibrary.algorithms.cfr.generalsum;

import cz.agents.gtlibrary.algorithms.cfr.ir.FixedForIterationData;
import cz.agents.gtlibrary.interfaces.Action;

import java.util.List;

public class BRFixedForIterationData extends FixedForIterationData {

    public BRFixedForIterationData(List<Action> actions) {
        super(actions);
    }

    @Override
    public boolean applyUpdate() {
        boolean oldUpdated = updated;

        if(updated) {
            r = regretUpdate;
            regretUpdate = new double[r.length];
            updated = false;
        }
        return oldUpdated;
    }

    @Override
    public double[] getRMStrategy() {
        int maxRegretIndex = getMaxRegretIndex();
        double[] rmStrategy = new double[r.length];

        rmStrategy[maxRegretIndex] = 1;
        return rmStrategy;
    }

    public int getMaxRegretIndex() {
        double max = Double.NEGATIVE_INFINITY;
        int maxIndex = -1;

        for (int i = 0; i < r.length; i++) {
            if (r[i] > max) {
                maxIndex = i;
                max = r[i];
            }
        }
        return maxIndex;
    }
}
