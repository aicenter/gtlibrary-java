package cz.agents.gtlibrary.algorithms.crswfabstraction.mergeddomain;

import cz.agents.gtlibrary.iinodes.ISKey;

import java.util.Arrays;
import java.util.List;

public class CompoundISKey extends ISKey {

    public void addKeys(List<ISKey> keys) {
        int offset = objects.length;
        objects = Arrays.copyOf(objects, offset + keys.size());
        for (int i = 0; i < keys.size(); i++) {
            objects[offset + i] = keys.get(i);
        }
    }
}
