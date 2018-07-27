package cz.agents.gtlibrary.iinodes;

import java.io.Serializable;

/**
 * Public state key
 */
public class PSKey extends ISKey implements Serializable {

    public PSKey(int hash) {
        super(hash);
    }

    public int getHash() {
        return (Integer) objects[0];
    }
}
