package cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions;

import cz.agents.gtlibrary.algorithms.cfr.ir.FixedForIterationData;
import cz.agents.gtlibrary.interfaces.Action;

import java.util.List;

public class CFRBRData extends FixedForIterationData {
    public CFRBRData(List<Action> actions) {
        super(actions);
    }

    public void setRegretAtIndex(int index, double regret) {
        for (int i = 0; i < r.length; i++) {
            r[i] = 0;
        }
        r[index] = regret;
    }

    public void updateMeanStrategy(int index, double w) {
        mp[index] += w;
        nbSamples++;
    }

}
