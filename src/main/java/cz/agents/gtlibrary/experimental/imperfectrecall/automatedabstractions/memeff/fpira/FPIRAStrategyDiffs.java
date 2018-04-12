package cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.memeff.fpira;

import cz.agents.gtlibrary.iinodes.PerfectRecallISKey;

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
