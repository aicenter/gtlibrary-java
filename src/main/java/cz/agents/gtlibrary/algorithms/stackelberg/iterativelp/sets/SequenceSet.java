package cz.agents.gtlibrary.algorithms.stackelberg.iterativelp.sets;

import cz.agents.gtlibrary.interfaces.Sequence;

import java.util.HashSet;
import java.util.Set;

public class SequenceSet {

    public static final SequenceSet empty = new SequenceSet(null, Double.POSITIVE_INFINITY);

    Set<SequenceSet> directSuperSets;
    Sequence sequence;
    double value;

    public SequenceSet(Sequence sequence, double value) {
        this.sequence = sequence;
        this.value = value;
        directSuperSets = new HashSet<>();
    }

    public void addSuperSet(SequenceSet superSet) {
        directSuperSets.add(superSet);
    }

    public SequenceSet createSuperSet(Sequence sequence, double value) {
        SequenceSet superSet = new SequenceSet(sequence, value);

        directSuperSets.add(superSet);
        return superSet;
    }

    public SequenceSet createSuperSet(Sequence sequence) {
        SequenceSet superSet = new SequenceSet(sequence, Double.POSITIVE_INFINITY);

        directSuperSets.add(superSet);
        return superSet;
    }

    public void setValue(double value) {
        if (value < this.value)
            this.value = value;
    }

    public Set<SequenceSet> getDirectSuperSets() {
        return directSuperSets;
    }

    public double getValue() {
        return value;
    }

    public Sequence getSequence() {
        return sequence;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SequenceSet)) return false;

        SequenceSet that = (SequenceSet) o;

        return !(sequence != null ? !sequence.equals(that.sequence) : that.sequence != null);

    }

    @Override
    public int hashCode() {
        return sequence != null ? sequence.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "{" + value +
                ", " + sequence +
                '}';
    }
}
