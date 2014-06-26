package cz.agents.gtlibrary.algorithms.mcts.nodes;

import cz.agents.gtlibrary.algorithms.mcts.AlgorithmData;
import cz.agents.gtlibrary.algorithms.mcts.MCTSConfig;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;

public abstract class NodeImpl implements Node {

    protected InnerNode parent;
    protected GameState gameState;
    protected Action lastAction;
    protected Expander<MCTSInformationSet> expander;
    protected int depth;
    protected AlgorithmData algorithmData;

    public NodeImpl(InnerNode parent, Action lastAction, GameState gameState) {
        this.parent = parent;
        this.lastAction = lastAction;
        this.gameState = gameState;
        this.expander = parent.expander;
        depth = parent.depth + 1;
    }

    public NodeImpl(Expander<MCTSInformationSet> expander, GameState gameState) {
        this.expander = expander;
        this.gameState = gameState;
        depth = 0;
    }

    @Override
    public InnerNode getParent() {
        return parent;
    }

    @Override
    public int getDepth() {
        return depth;
    }

    @Override
    public void setParent(InnerNode parent) {
        this.parent = parent;
    }

    @Override
    public Action getLastAction() {
        return lastAction;
    }

    public void setLastAction(Action lastAction) {
        this.lastAction = lastAction;
    }

    @Override
    public GameState getGameState() {
        return gameState;
    }

    @Override
    public String toString() {
        return "Node: " + gameState;
    }

    public MCTSConfig getAlgConfig() {
        return (MCTSConfig) expander.getAlgorithmConfig();
    }

    public Expander<MCTSInformationSet> getExpander() {
        return expander;
    }

    public AlgorithmData getAlgorithmData() {
        return algorithmData;
    }

    public void setAlgorithmData(AlgorithmData algorithmData) {
        this.algorithmData = algorithmData;
    }
}
