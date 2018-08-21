package cz.agents.gtlibrary.algorithms.mccr.gadgettree;

import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.iinodes.PerfectRecallISKey;
import cz.agents.gtlibrary.interfaces.Sequence;

import java.io.Serializable;

public class GadgetISKey extends ISKey implements Serializable{
    private final Sequence sequence;
    private final int hash;

    public GadgetISKey(PerfectRecallISKey isKey) {
        super(isKey.getSequence().hashCode(), isKey.getSequence(), "gadget");
        this.sequence = isKey.getSequence();
        this.hash = this.sequence.hashCode();
    }

    public GadgetISKey(int hashCode, Sequence sequence) {
        super(hashCode, "gadget");
        this.sequence = sequence;
        this.hash = hashCode;
    }

    public Sequence getSequence() {
        return sequence;
    }

    public int getHash() {
        return hash;
    }
}
