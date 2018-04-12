package cz.agents.gtlibrary.domain.randomabstraction;

import cz.agents.gtlibrary.interfaces.Observation;

import java.io.Serializable;

public class IDObservation implements Observation, Serializable {
    private int id;

    public IDObservation(int id) {
        this.id = id;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IDObservation)) return false;

        IDObservation that = (IDObservation) o;

        return id == that.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return id + "";
    }
}
