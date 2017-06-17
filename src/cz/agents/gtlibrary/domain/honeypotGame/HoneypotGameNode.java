package cz.agents.gtlibrary.domain.honeypotGame;

/**
 * Created by Petr Tomasek on 29.4.2017.
 */
public class HoneypotGameNode {

    protected int id;
    protected double reward;
    protected double attackCost;
    protected double defendCost;

    public HoneypotGameNode(int id, double reward, double attackCost, double defendCost){
        this.id = id;
        this.reward = reward;
        this.attackCost = attackCost;
        this.defendCost = defendCost;
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
            return "ID: " + id + " | REWARD: " + reward + " | ACOST: " + attackCost + " | DCOST: " + defendCost;
        } else {
            return "DONE";
        }
    }
}
