package cz.agents.gtlibrary.algorithms.mccr.gadgettree;

import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.iinodes.PerfectRecallISKey;

import java.io.Serializable;

public class GadgetISKey extends ISKey implements Serializable{
    public GadgetISKey(PerfectRecallISKey isKey) {
        super(isKey.getHash(), isKey.getSequence(), "gadget");
    }

    public GadgetISKey(int hashCode) {
        super(hashCode, "gadget");
    }
}
