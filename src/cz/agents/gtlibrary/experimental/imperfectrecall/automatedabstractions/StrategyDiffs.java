package cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions;

import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Sequence;

import java.util.HashMap;
import java.util.Map;

public class StrategyDiffs {
    public Map<Sequence, Map<Action, Double>> prStrategyDiff;
    public Map<Sequence, Map<Action, Double>> irStrategyDiff;

    public StrategyDiffs() {
        this.prStrategyDiff = new HashMap<>();
        this.irStrategyDiff = new HashMap<>();
    }

    public void remap() {
        prStrategyDiff = remap(prStrategyDiff);
        irStrategyDiff = remap(irStrategyDiff);
    }

    private Map<Sequence, Map<Action, Double>> remap(Map<Sequence, Map<Action, Double>> map) {
        Map<Sequence, Map<Action, Double>> updatedMap = new HashMap<>(map.size());

        for (Map.Entry<Sequence, Map<Action, Double>> sequenceMapEntry : map.entrySet()) {
            Map<Action, Double> updatedActionMap = new HashMap<>(sequenceMapEntry.getValue().size());

            for (Map.Entry<Action, Double> actionDoubleEntry : sequenceMapEntry.getValue().entrySet()) {
                updatedActionMap.put(actionDoubleEntry.getKey(), actionDoubleEntry.getValue());
            }
            updatedMap.put(sequenceMapEntry.getKey(), updatedActionMap);
        }
        return updatedMap;
    }
}
