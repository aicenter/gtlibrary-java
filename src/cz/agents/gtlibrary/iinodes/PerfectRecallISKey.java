package cz.agents.gtlibrary.iinodes;

import cz.agents.gtlibrary.interfaces.Sequence;

public class PerfectRecallISKey extends ISKey {

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
