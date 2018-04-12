package cz.agents.gtlibrary.iinodes;

import cz.agents.gtlibrary.interfaces.Observation;
import cz.agents.gtlibrary.interfaces.Player;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.*;

public class Observations implements List<Observation>, Serializable {

    private List<Observation> observationList;
    private Player observingPlayer;
    private Player observedPlayer;
    public Observations(Player observingPlayer, Player observedPlayer) {
        this.observingPlayer = observingPlayer;
        this.observedPlayer = observedPlayer;
        observationList = new LinkedList<>();
    }

    private int hashCode = -1;

    public Observations(List<Observation> observationList, Player observingPlayer, Player observedPlayer) {
        this.observingPlayer = observingPlayer;
        this.observedPlayer = observedPlayer;
        this.observationList = new LinkedList<>();
        addAll(observationList);
    }

    @Override
    public int size() {
        return observationList.size();
    }

    @Override
    public boolean isEmpty() {
        return observationList.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return observationList.contains(o);
    }

    @Override
    public Iterator<Observation> iterator() {
        hashCode = -1;
        return observationList.iterator();
    }

    @Override
    public Object[] toArray() {
        return observationList.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return observationList.toArray(a);
    }

    @Override
    public boolean add(Observation observation) {
        hashCode = -1;
        return observation.isEmpty() || observationList.add(observation);
    }

    @Override
    public boolean remove(Object o) {
        hashCode = -1;
        return observationList.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return observationList.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends Observation> c) {
        for (Observation observation : c) {
            add(observation);
        }
        return !c.isEmpty();
    }

    @Override
    public boolean addAll(int index, Collection<? extends Observation> c) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        hashCode = -1;
        return observationList.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        hashCode = -1;
        return observationList.retainAll(c);
    }

    @Override
    public void clear() {
        hashCode = -1;
        observationList.clear();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Observations that = (Observations) o;
        if (!observedPlayer.equals(that.observedPlayer)) return false;
        if (!observingPlayer.equals(that.observingPlayer)) return false;
        return observationList.equals(that.observationList);
    }

    @Override
    public int hashCode() {
        if (hashCode == -1)
            hashCode = new HashCodeBuilder(17, 31).append(observedPlayer).append(observingPlayer).append(observationList).toHashCode();
        return hashCode;
    }

    @Override
    public Observation get(int index) {
        return observationList.get(index);
    }

    @Override
    public Observation set(int index, Observation element) {
        hashCode = -1;
        return element.isEmpty() ? remove(index) : observationList.set(index, element);
    }

    @Override
    public void add(int index, Observation element) {
        hashCode = -1;
        if (!element.isEmpty())
            observationList.add(index, element);
    }

    @Override
    public Observation remove(int index) {
        hashCode = -1;
        return observationList.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return observationList.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return observationList.lastIndexOf(o);
    }

    @Override
    public ListIterator<Observation> listIterator() {
        hashCode = -1;
        return observationList.listIterator();
    }

    @Override
    public ListIterator<Observation> listIterator(int index) {
        hashCode = -1;
        return observationList.listIterator(index);
    }

    @Override
    public List<Observation> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException("Sublist not implemented");
    }

    @Override
    public String toString() {
        return observingPlayer + " obs " + observedPlayer + ":" + observationList.toString();
    }

    public Observations copy() {
        return new Observations(this, observingPlayer, observedPlayer);
    }

    public void performDepthChangingOperations(int seed) {
        //intentionally empty
    }

    public Player getObservingPlayer() {
        return observingPlayer;
    }

    public Player getObservedPlayer() {
        return observedPlayer;
    }
}
