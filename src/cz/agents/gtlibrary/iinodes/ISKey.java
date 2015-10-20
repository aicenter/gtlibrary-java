package cz.agents.gtlibrary.iinodes;

import java.util.Arrays;

public abstract class ISKey {
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
        if(hashCode == -1)
            hashCode = objects != null ? Arrays.hashCode(objects) : 0;
        return hashCode;
    }

    @Override
    public String toString() {
        return Arrays.toString(objects);
    }
}