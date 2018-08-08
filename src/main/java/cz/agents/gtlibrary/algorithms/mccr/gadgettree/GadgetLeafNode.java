package cz.agents.gtlibrary.algorithms.mccr.gadgettree;

import cz.agents.gtlibrary.NotImplementedException;
import cz.agents.gtlibrary.algorithms.mcts.AlgorithmData;
import cz.agents.gtlibrary.algorithms.mcts.MCTSConfig;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.LeafNode;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;

// this is the leaf node for terminate action
public class GadgetLeafNode implements LeafNode, GadgetNode {
    private final double[] utilities;
    private InnerNode parent;
    private Action lastAction;
    private GameState state;

    public GadgetLeafNode(GameState originalGameState, Double utility) {
        // todo: check signs
        this.utilities = new double[]{-utility, utility, 0};
        this.state = new GadgetLeafState(originalGameState, utilities);
    }

    @Override
    public double[] getUtilities() {
        return utilities;
    }

    @Override
    public int getDepth() {
        return 2;
    }


    @Override
    public InnerNode getParent() {
        return parent;
    }

    @Override
    public void setParent(InnerNode parent) {
        this.parent = parent;
    }

    @Override
    public Action getLastAction() {
        return lastAction;
    }

    @Override
    public void setLastAction(Action lastAction) {
        this.lastAction = lastAction;
    }

    @Override
    public Expander<MCTSInformationSet> getExpander() {
        throw new NotImplementedException();
    }

    @Override
    public GameState getGameState() {
        return state;
    }

    @Override
    public MCTSConfig getAlgConfig() {
        throw new NotImplementedException();
    }

    @Override
    public AlgorithmData getAlgorithmData() {
        throw new NotImplementedException();
    }

    @Override
    public void setAlgorithmData(AlgorithmData algorithmData) {
        throw new NotImplementedException();
    }
}
