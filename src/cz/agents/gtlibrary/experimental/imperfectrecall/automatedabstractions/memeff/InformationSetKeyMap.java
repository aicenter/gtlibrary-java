package cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.memeff;

import cz.agents.gtlibrary.domain.randomabstraction.IDObservation;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.iinodes.ImperfectRecallISKey;
import cz.agents.gtlibrary.iinodes.Observations;
import cz.agents.gtlibrary.iinodes.PerfectRecallISKey;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

import java.util.HashMap;
import java.util.List;

public class InformationSetKeyMap extends HashMap<PerfectRecallISKey, ImperfectRecallISKey> {

//    @Override
//    public ImperfectRecallISKey get(Object key) {
//        ImperfectRecallISKey value = super.get(key);
//
//        if(value != null)
//            return value;
//        PerfectRecallISKey prKey = (PerfectRecallISKey) key;
//        Observations observations = new Observations(prKey.getSequence().getPlayer(), prKey.getSequence().getPlayer());
//
//        observations.add(new IDObservation(prKey.getSequence().size()));
//        return new ImperfectRecallISKey(observations, null, null);
//    }


    @Override
    public ImperfectRecallISKey get(Object key) {
        System.err.println("wrong call of ISKey Map");
        return super.get(key);
    }

    public ImperfectRecallISKey get(GameState state, Expander<? extends InformationSet> expander) {
        ISKey isKey= state.getISKeyForPlayerToMove();
        ImperfectRecallISKey value = super.get(isKey);

        if(value != null)
            return value;
        PerfectRecallISKey prKey = (PerfectRecallISKey) isKey;
        Observations observations = new Observations(prKey.getSequence().getPlayer(), prKey.getSequence().getPlayer());

        observations.add(new IDObservation(prKey.getSequence().size()));
        observations.add(new IDObservation(expander.getActions(state).size()));
        return new ImperfectRecallISKey(observations, null, null);
    }

    public ImperfectRecallISKey get(PerfectRecallISKey isKey, List<Action> actions) {
        ImperfectRecallISKey value = super.get(isKey);

        if(value != null)
            return value;
        Observations observations = new Observations(isKey.getSequence().getPlayer(), isKey.getSequence().getPlayer());

        observations.add(new IDObservation(isKey.getSequence().size()));
        observations.add(new IDObservation(actions.size()));
        return new ImperfectRecallISKey(observations, null, null);
    }
}
