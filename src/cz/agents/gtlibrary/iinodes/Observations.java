package cz.agents.gtlibrary.iinodes;

import cz.agents.gtlibrary.interfaces.Observation;

import java.util.*;

public class Observations implements List<Observation> {

    private ArrayList<Observation> observationList;

    public Observations() {
        observationList = new ArrayList<>();
    }

    public Observations(List<Observation> observationList) {
        this.observationList = new ArrayList<>(observationList.size());
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
        return observation.isEmpty() || observationList.add(observation);
    }

    @Override
    public boolean remove(Object o) {
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
        return observationList.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return observationList.retainAll(c);
    }

    @Override
    public void clear() {
        observationList.clear();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Observations that = (Observations) o;
        return observationList.equals(that.observationList);
    }

    @Override
    public int hashCode() {
        return observationList.hashCode();
    }

    @Override
    public Observation get(int index) {
        return observationList.get(index);
    }

    @Override
    public Observation set(int index, Observation element) {
        return element.isEmpty() ? remove(index) : observationList.set(index, element);
    }

    @Override
    public void add(int index, Observation element) {
        if (!element.isEmpty())
            observationList.add(index, element);
    }

    @Override
    public Observation remove(int index) {
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
        return observationList.listIterator();
    }

    @Override
    public ListIterator<Observation> listIterator(int index) {
        return observationList.listIterator(index);
    }

    @Override
    public List<Observation> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException("Sublist not implemented");
    }

    @Override
    public String toString() {
        return observationList.toString();
    }
}
