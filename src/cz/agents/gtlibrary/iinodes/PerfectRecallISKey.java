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

    @Override
    public int hashCode() {
        int hashCode;
        final int prime = 31;

        hashCode = 1;
        hashCode = prime * hashCode + getLeft();
        hashCode = prime * hashCode + ((getRight() == null) ? 0 : getRight().hashCode());
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof PerfectRecallISKey))
            return false;
        PerfectRecallISKey other = (PerfectRecallISKey) obj;
        if (getLeft() != other.getLeft())
            return false;
        if (getRight() == null) {
            if (other.getRight() != null)
                return false;
        } else if (!getRight().equals(other.getRight()))
            return false;
        return true;
    }

}
