package cz.agents.gtlibrary.domain.randomgameimproved.observationvariants;

import cz.agents.gtlibrary.iinodes.Observations;
import cz.agents.gtlibrary.interfaces.Observation;
import cz.agents.gtlibrary.interfaces.Player;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class HashedObservations extends Observations {

    private int hash;

    public HashedObservations(Player observingPlayer, Player observedPlayer) {
        super(observingPlayer, observedPlayer);
        hash = -1;
    }

    public HashedObservations(List<Observation> observationList, Player observingPlayer, Player observedPlayer) {
        super(observationList, observingPlayer, observedPlayer);
        hash = -1;
    }

    @Override
    public boolean add(Observation observation) {
        hash = -1;
        return super.add(observation);
    }

    @Override
    public boolean remove(Object o) {
        hash = -1;
        return super.remove(o);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        hash = -1;
        return super.removeAll(c);
    }

    @Override
    public void add(int index, Observation element) {
        super.add(index, element);
        System.out.println("Obs:" + element.isEmpty());
        hash = -1;
    }

    @Override
    public int hashCode() {
        if (hash < 0) computeHash();
        int prime = 17;
        return prime*getObservedPlayer().hashCode() + 13 + prime*getObservingPlayer().hashCode() + 13 + hash;
    }

    private void computeHash() {
        hash = 0;
        Iterator<Observation> iter = iterator();
        while (iter.hasNext()) {
            hash += iter.next().hashCode();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HashedObservations that = (HashedObservations) o;

        if (!getObservedPlayer().equals(that.getObservedPlayer())) return false;
        if (!getObservingPlayer().equals(that.getObservingPlayer())) return false;
        if (hash < 0) computeHash();
        return hash == that.hash;

    }

    @Override
    public Observations copy() {
        return new HashedObservations(this, getObservingPlayer(), getObservedPlayer());
    }
}
