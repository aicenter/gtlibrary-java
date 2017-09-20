package cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.memeff;

import cz.agents.gtlibrary.domain.randomabstraction.IDObservation;
import cz.agents.gtlibrary.iinodes.ImperfectRecallISKey;
import cz.agents.gtlibrary.iinodes.Observations;
import cz.agents.gtlibrary.iinodes.PerfectRecallISKey;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

public class InformationSetKeyMap extends HashMap<PerfectRecallISKey, ImperfectRecallISKey> implements Serializable {

    @Override
    public ImperfectRecallISKey get(Object key) {
        System.err.println("wrong call of ISKey Map");
        return super.get(key);
    }

    public ImperfectRecallISKey get(GameState state, Expander<? extends InformationSet> expander) {
        return get((PerfectRecallISKey) state.getISKeyForPlayerToMove(), expander.getActions(state).size());
    }

    public ImperfectRecallISKey get(PerfectRecallISKey isKey, List<Action> actions) {
        return get(isKey, actions.size());
    }

    public ImperfectRecallISKey get(PerfectRecallISKey isKey, int actionCount) {
        ImperfectRecallISKey value = super.get(isKey);

        if (value != null)
            return value;
        Observations observations = new Observations(isKey.getSequence().getPlayer(), isKey.getSequence().getPlayer());

        observations.add(new IDObservation(isKey.getSequence().size()));
        observations.add(new IDObservation(actionCount));
        return new ImperfectRecallISKey(observations, null, null);
    }
}
