package cz.agents.gtlibrary.iinodes;

import cz.agents.gtlibrary.interfaces.Sequence;

import java.io.Serializable;

public class PerfectRecallISKey extends ISKey implements Serializable {

    public PerfectRecallISKey(int hash, Sequence sequence) {
        super(hash, sequence);
    }

    public int getHash() {
        return ((Integer) objects[0]).intValue();
    }

    public Sequence getSequence() {
        return (Sequence) objects[1];
    }

}
