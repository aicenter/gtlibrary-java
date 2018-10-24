package cz.agents.gtlibrary.iinodes;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Public state key
 */
public class PSKey extends ISKey implements Serializable {

    private final Object[] extraObjects;

    public PSKey(int id) {
        super(id);
        extraObjects = null;
    }

    public PSKey(int id, Object ... objects) {
        super(id);
        this.extraObjects = objects;
    }

    public int getId() {
        return (Integer) objects[0];
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof PSKey)) {
            return false;
        }
        if(!super.equals(o)) {
            return false;
        };

        PSKey psKey = (PSKey) o;
        return Arrays.equals(extraObjects, psKey.extraObjects);
    }
}
