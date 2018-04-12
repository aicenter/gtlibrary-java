package cz.agents.gtlibrary.iinodes;

import cz.agents.gtlibrary.interfaces.Observation;

import java.io.Serializable;

public class PerfectRecallObservation implements Observation, Serializable {
    private PerfectRecallISKey prKey;

    public PerfectRecallObservation(PerfectRecallISKey prKey) {
        this.prKey = prKey;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PerfectRecallObservation)) return false;

        PerfectRecallObservation that = (PerfectRecallObservation) o;

        return prKey != null ? prKey.equals(that.prKey) : that.prKey == null;

    }

    @Override
    public int hashCode() {
        return prKey != null ? prKey.hashCode() : 0;
    }
}
