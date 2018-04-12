package cz.agents.gtlibrary.algorithms.crswfabstraction;

import cz.agents.gtlibrary.interfaces.GameState;

public class LeafNode {

    private GameState state;
    private GameState parentState;

    public LeafNode(GameState state, GameState parentState) {
        this.state = state;
        this.parentState = parentState;
    }

    public GameState getState() {
        return state;
    }

    public GameState getParentState() {
        return parentState;
    }

    public double getUtility() {
        return state.getUtilities()[parentState.getPlayerToMove().getId()];
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LeafNode leafNode = (LeafNode) o;

        if (!state.equals(leafNode.state)) return false;
        return parentState.equals(leafNode.parentState);

    }

    @Override
    public int hashCode() {
        int result = state.hashCode();
        result = 31 * result + parentState.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return state.toString();
    }
}
