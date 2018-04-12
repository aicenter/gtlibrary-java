package cz.agents.gtlibrary.algorithms.flipit.iskeys;

import cz.agents.gtlibrary.iinodes.PerfectRecallISKey;
import cz.agents.gtlibrary.interfaces.Sequence;

/**
 * Created by Jakub Cerny on 04/07/2017.
 */
public class FlipItPerfectRecallISKey extends PerfectRecallISKey {

    private Object observation;

    public FlipItPerfectRecallISKey(Object observation, int hash, Sequence sequence) {
        super(hash, sequence);
        this.observation = observation;
    }

    public Object getObservation(){
        return observation;
    }
}
