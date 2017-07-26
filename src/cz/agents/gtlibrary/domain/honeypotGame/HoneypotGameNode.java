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

    public double getRewardAfterNumberOfAttacks(int numberOfAttacks){
        if (numberOfAttacks == 0) return reward;
        else return 0.0;// Math.pow(reward, 1/numberOfAttacks); // reward / Math.pow(2,numberOfAttacks);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HoneypotGameNode that = (HoneypotGameNode) o;

        return id == that.id;

    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        if (id != HoneypotGameInfo.NO_ACTION_ID) {
            return "ID: " + id + " | REWARD: " + reward + " | ACOST: " + attackCost + " | DCOST: " + defendCost;
        } else {
            return "DONE";
        }
    }

    public int getID(){
        return  id;
    }
}
