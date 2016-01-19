package cz.agents.gtlibrary.experimental.imperfectRecall.blseqformlp;

import cz.agents.gtlibrary.iinodes.IRInformationSetImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Sequence;

import java.util.*;

public class SequenceFormIRInformationSet extends IRInformationSetImpl {
    private Map<Sequence, Set<Sequence>> outgoingSequences;
    private boolean hasIR = false;

    public SequenceFormIRInformationSet(GameState state) {
        super(state);
        outgoingSequences = new HashMap<>();
    }

    public void addOutgoingSequenceFor(Sequence sequence, Sequence outgoingSequence) {
        Set<Sequence> currentOutgoing = this.outgoingSequences.get(sequence);

        if (currentOutgoing == null)
            currentOutgoing = new HashSet<>(outgoingSequences.size());
        currentOutgoing.add(outgoingSequence);
        this.outgoingSequences.put(sequence, currentOutgoing);
    }

    public void addOutgoingSequencesFor(Sequence sequence, Collection<Sequence> outgoingSequences) {
        Set<Sequence> currentOutgoing = this.outgoingSequences.get(sequence);

        if (currentOutgoing == null)
            currentOutgoing = new HashSet<>(outgoingSequences.size());
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
            for (Sequence sequence : outgoing) {
                actions.add(sequence.getLast());
            }
        }
        return actions;
    }

    public boolean isHasIR() {
        return hasIR;
    }

    public void setHasIR(boolean hasIR) {
        this.hasIR = hasIR;
    }
}
