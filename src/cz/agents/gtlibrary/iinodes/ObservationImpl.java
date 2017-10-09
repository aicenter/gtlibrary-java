package cz.agents.gtlibrary.iinodes;

import cz.agents.gtlibrary.interfaces.Observation;

import java.io.Serializable;

public class ObservationImpl implements Observation, Serializable {

    public static final ObservationImpl EMPTY_OBSERVATION = new ObservationImpl(-1);

    private int index;

    public ObservationImpl(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public boolean isEmpty() {
        return index < 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ObservationImpl that = (ObservationImpl) o;

        return index == that.index;

    }

    @Override
    public int hashCode() {
        return index*31 + 17;
    }

    @Override
    public String toString() {
        return "" + index;
    }
}
