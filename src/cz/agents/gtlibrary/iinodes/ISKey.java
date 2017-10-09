package cz.agents.gtlibrary.iinodes;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.Arrays;

public abstract class ISKey implements Serializable {
    protected Object[] objects;
    private int hashCode = -1;

    public ISKey(Object... objects) {
        this.objects = objects;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ISKey)) return false;

        ISKey isKey = (ISKey) o;

        return Arrays.equals(objects, isKey.objects);
    }

    @Override
    public int hashCode() {
        if(hashCode == -1) {
            HashCodeBuilder hcb = new HashCodeBuilder(17,31);
            if (objects != null) {
                for (Object o : objects)
                    hcb.append(o);
                hashCode = hcb.toHashCode();
            } else hashCode = 0;
        }
        return hashCode;
    }

    @Override
    public String toString() {
        return Arrays.toString(objects);
    }
}
