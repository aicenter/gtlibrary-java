package cz.agents.gtlibrary.iinodes;

import cz.agents.gtlibrary.interfaces.Observation;

import java.util.*;

public class Observations implements List<Observation> {

    private ArrayList<Observation> observationList;

    public Observations(List<Observation> observationList) {
        this.observationList = new ArrayList<>(observationList);
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
        return observationList.add(observation);
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
        return observationList.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends Observation> c) {
        return observationList.addAll(index, c);
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
        return false;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public Observation get(int index) {
        return observationList.get(index);
    }

    @Override
    public Observation set(int index, Observation element) {
        return observationList.set(index, element);
    }

    @Override
    public void add(int index, Observation element) {
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
}
