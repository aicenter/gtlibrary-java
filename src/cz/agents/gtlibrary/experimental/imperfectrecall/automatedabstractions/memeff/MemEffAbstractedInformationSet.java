package cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.memeff;

import cz.agents.gtlibrary.algorithms.cfr.ir.IRCFRInformationSet;
import cz.agents.gtlibrary.iinodes.ImperfectRecallISKey;
import cz.agents.gtlibrary.iinodes.PerfectRecallISKey;
import cz.agents.gtlibrary.interfaces.GameState;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class MemEffAbstractedInformationSet extends IRCFRInformationSet implements Serializable {
    protected Set<PerfectRecallISKey> abstractedKeys;

    public MemEffAbstractedInformationSet(GameState state) {
        super(state);
        abstractedKeys = new HashSet<>();
        abstractedKeys.add((PerfectRecallISKey) state.getISKeyForPlayerToMove());
    }

    public MemEffAbstractedInformationSet(GameState state, ImperfectRecallISKey isKey) {
        super(state, isKey);
        abstractedKeys = new HashSet<>();
        abstractedKeys.add((PerfectRecallISKey) state.getISKeyForPlayerToMove());
    }

    @Override
    public void addAllStatesToIS(Collection<GameState> states) {
        states.forEach(this::addStateToIS);
    }

    @Override
    public void addStateToIS(GameState state) {
        PerfectRecallISKey key = (PerfectRecallISKey) state.getISKeyForPlayerToMove();

        if (abstractedKeys.contains(key))
            return;
        abstractedKeys.add(key);
        super.addStateToIS(state);
    }

    public Set<PerfectRecallISKey> getAbstractedKeys() {
        return abstractedKeys;
    }
}
