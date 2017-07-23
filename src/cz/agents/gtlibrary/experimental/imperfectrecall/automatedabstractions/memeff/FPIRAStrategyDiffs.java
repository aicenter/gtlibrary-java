package cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.memeff;

import cz.agents.gtlibrary.iinodes.ArrayListSequenceImpl;
import cz.agents.gtlibrary.iinodes.PerfectRecallISKey;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Sequence;

import java.util.HashMap;
import java.util.Map;

public class FPIRAStrategyDiffs {
    public Map<PerfectRecallISKey, Map<Action, Double>> prStrategyDiff;
    public Map<PerfectRecallISKey, Map<Action, Double>> irStrategyDiff;

    public FPIRAStrategyDiffs() {
        this.prStrategyDiff = new HashMap<>();
        this.irStrategyDiff = new HashMap<>();
    }

    public void remap() {
        prStrategyDiff = remap(prStrategyDiff);
        irStrategyDiff = remap(irStrategyDiff);
    }

    public Map<PerfectRecallISKey, Map<Action, Double>> remap(Map<PerfectRecallISKey, Map<Action, Double>> map) {
        Map<PerfectRecallISKey, Map<Action, Double>> updatedMap = new HashMap<>(map.size());

        for (Map.Entry<PerfectRecallISKey, Map<Action, Double>> isKeyMapEntry : map.entrySet()) {
            Map<Action, Double> updatedActionMap = new HashMap<>(isKeyMapEntry.getValue().size());

            for (Map.Entry<Action, Double> actionDoubleEntry : isKeyMapEntry.getValue().entrySet()) {
                updatedActionMap.put(actionDoubleEntry.getKey(), actionDoubleEntry.getValue());
            }
            updatedMap.put(isKeyMapEntry.getKey(), updatedActionMap);
        }
        return updatedMap;
    }

}
