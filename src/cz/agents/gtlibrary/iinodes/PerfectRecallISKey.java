package cz.agents.gtlibrary.iinodes;

import cz.agents.gtlibrary.interfaces.Sequence;

public class PerfectRecallISKey extends ISKey {

    public PerfectRecallISKey(int hash, Sequence sequence) {
        super(hash, sequence);
    }

    public int getLeft() {
        return ((Integer) objects[0]).intValue();
    }

    public Sequence getRight() {
        return (Sequence) objects[1];
    }

}
