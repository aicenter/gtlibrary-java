package cz.agents.gtlibrary.algorithms.mcts;

import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.Node;
import cz.agents.gtlibrary.iinodes.PublicStateImpl;
import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;

public class MCTSPublicState extends PublicStateImpl {

    private AlgorithmData algorithmData;

    public MCTSPublicState(MCTSConfig config, Expander expander, InnerNode node) {
        super(config, expander, node, null, null);
    }

    public MCTSPublicState(MCTSConfig mctsConfig, Expander expander, InnerNode node, MCTSPublicState parentPublicState, MCTSPublicState playerParentPublicState) {
        super(mctsConfig, expander, node, parentPublicState, playerParentPublicState);
    }

    public AlgorithmData getAlgorithmData() {
        return algorithmData;
    }

    public void setAlgorithmData(AlgorithmData algorithmData) {
        this.algorithmData = algorithmData;
    }

    @Override
    public void destroy() {
        super.destroy();
        this.algorithmData = null;
    }
}
