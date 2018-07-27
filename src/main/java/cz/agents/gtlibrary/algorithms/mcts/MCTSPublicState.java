package cz.agents.gtlibrary.algorithms.mcts;

import cz.agents.gtlibrary.iinodes.PublicStateImpl;
import cz.agents.gtlibrary.interfaces.GameState;

public class MCTSPublicState extends PublicStateImpl {

    private AlgorithmData algorithmData;

    public MCTSPublicState(GameState state) {
        super(state);
    }

    public AlgorithmData getAlgorithmData() {
        return algorithmData;
    }

    public void setAlgorithmData(AlgorithmData algorithmData) {
        this.algorithmData = algorithmData;
    }
}
