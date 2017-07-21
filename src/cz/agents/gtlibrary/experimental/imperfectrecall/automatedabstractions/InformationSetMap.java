package cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions;

import cz.agents.gtlibrary.domain.randomabstraction.IDObservation;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.iinodes.ImperfectRecallISKey;
import cz.agents.gtlibrary.iinodes.Observations;
import cz.agents.gtlibrary.iinodes.PerfectRecallISKey;

import java.util.HashMap;

public class InformationSetMap extends HashMap<PerfectRecallISKey, ImperfectRecallISKey> {

    @Override
    public ImperfectRecallISKey get(Object key) {
        ImperfectRecallISKey value = super.get(key);

        if(value != null)
            return value;
        PerfectRecallISKey prKey = (PerfectRecallISKey) key;
        Observations observations = new Observations(prKey.getSequence().getPlayer(), prKey.getSequence().getPlayer());

        observations.add(new IDObservation(prKey.getSequence().size()));
        return new ImperfectRecallISKey(observations, null, null);
    }
}
