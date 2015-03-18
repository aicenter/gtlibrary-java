package cz.agents.gtlibrary.algorithms.stackelberg.milp;

import cz.agents.gtlibrary.interfaces.GameState;

public class BoundLeafInfo {
    public GameState leaf;
    public double leaderUpperBound;
    public double followerUpperBound;

    public BoundLeafInfo(GameState leaf, double leaderUpperBound, double followerUpperBound) {
        this.leaf = leaf;
        this.leaderUpperBound = leaderUpperBound;
        this.followerUpperBound = followerUpperBound;
    }
}
