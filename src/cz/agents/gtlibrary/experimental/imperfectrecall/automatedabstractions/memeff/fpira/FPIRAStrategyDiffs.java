package cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.memeff.fpira;

import cz.agents.gtlibrary.iinodes.ArrayListSequenceImpl;
import cz.agents.gtlibrary.iinodes.PerfectRecallISKey;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Sequence;

import java.util.HashMap;
import java.util.Map;

public class FPIRAStrategyDiffs {
    public Map<PerfectRecallISKey, double[]> prStrategyDiff;
    public Map<PerfectRecallISKey, double[]> irStrategyDiff;

    public FPIRAStrategyDiffs() {
        this.prStrategyDiff = new HashMap<>();
        this.irStrategyDiff = new HashMap<>();
    }

}
