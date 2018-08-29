package cz.agents.gtlibrary.algorithms.mcts;

import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.Node;
import cz.agents.gtlibrary.iinodes.PublicStateImpl;
import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;

public class MCTSPublicState extends PublicStateImpl {

    private AlgorithmData algorithmData;

    public MCTSPublicState(MCTSConfig config, InnerNode node) {
        super(config, node, null);
    }

    public MCTSPublicState(MCTSConfig mctsConfig, InnerNode node, MCTSPublicState parentPublicState) {
        super(mctsConfig, node, parentPublicState);
    }

    public AlgorithmData getAlgorithmData() {
        return algorithmData;
    }

    public void setAlgorithmData(AlgorithmData algorithmData) {
        this.algorithmData = algorithmData;
    }
}
