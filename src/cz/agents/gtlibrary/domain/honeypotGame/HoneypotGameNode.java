package cz.agents.gtlibrary.domain.honeypotGame;

/**
 * Created by Petr Tomasek on 29.4.2017.
 */
public class HoneypotGameNode {

    protected int id;
    protected double value;

    public HoneypotGameNode(int id, double value){
        this.id = id;
        this.value = value;
    }

    @Override
    public int hashCode() {
        return (id + 7) * 17;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        HoneypotGameNode other = (HoneypotGameNode) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        if (id != HoneypotGameInfo.NO_ACTION_ID) {
            return "ID: " + id + " | VALUE: " + value;
        } else {
            return "DONE";
        }
    }
}
