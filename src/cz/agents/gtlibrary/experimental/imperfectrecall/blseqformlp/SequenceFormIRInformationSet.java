package cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp;

import cz.agents.gtlibrary.iinodes.IRInformationSetImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Sequence;

import java.util.*;
import java.util.stream.Collectors;

public class SequenceFormIRInformationSet extends IRInformationSetImpl {
    private Map<Sequence, Set<Sequence>> outgoingSequences;
    private Action first;

    public SequenceFormIRInformationSet(GameState state) {
        super(state);
        outgoingSequences = new HashMap<>();
        first = null;
    }

    public void addOutgoingSequenceFor(Sequence sequence, Sequence outgoingSequence) {
        Set<Sequence> currentOutgoing = this.outgoingSequences.get(sequence);

        if (currentOutgoing == null)
            currentOutgoing = new LinkedHashSet<>(outgoingSequences.size());
        currentOutgoing.add(outgoingSequence);
        this.outgoingSequences.put(sequence, currentOutgoing);
        if(first == null)
            first = outgoingSequence.getLast();
    }

    public void addOutgoingSequencesFor(Sequence sequence, Collection<Sequence> outgoingSequences) {
        Set<Sequence> currentOutgoing = this.outgoingSequences.get(sequence);

        if (currentOutgoing == null)
            currentOutgoing = new LinkedHashSet<>(outgoingSequences.size());
        currentOutgoing.addAll(outgoingSequences);
        this.outgoingSequences.put(sequence, currentOutgoing);
    }

    public Map<Sequence, Set<Sequence>> getOutgoingSequences() {
        return outgoingSequences;
    }

    public Set<Sequence> getOutgoingSequencesFor(Sequence sequence) {
        return outgoingSequences.get(sequence);
    }

    public Set<Action> getActions() {
        Set<Action> actions = new HashSet<>();

        for (Set<Sequence> outgoing : outgoingSequences.values()) {
            actions.addAll(outgoing.stream().map(Sequence::getLast).collect(Collectors.toList()));
        }
        return actions;
    }

    public boolean hasIR() {
        return outgoingSequences.size() > 1;
    }

    public Action getFirst() {
        return first;
    }
}
