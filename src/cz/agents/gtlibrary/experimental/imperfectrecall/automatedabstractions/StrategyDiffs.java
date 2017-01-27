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
}
