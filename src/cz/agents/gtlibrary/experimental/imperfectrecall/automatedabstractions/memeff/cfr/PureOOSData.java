package cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.memeff.cfr;

import cz.agents.gtlibrary.algorithms.mcts.oos.OOSAlgorithmData;
import cz.agents.gtlibrary.interfaces.Action;

import java.io.Serializable;
import java.util.List;

public class PureOOSData extends OOSAlgorithmData implements Serializable {
    public PureOOSData(int actionCount) {
        super(actionCount);
        r[0] = 1;
    }

    public PureOOSData(List<Action> actions) {
        super(actions);
        r[0] = 1;
    }

    public PureOOSData(OOSAlgorithmData data) {
        super(data);
    }
}
