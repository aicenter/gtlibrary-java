package cz.agents.gtlibrary.experimental.imperfectrecall.dag.rg;

import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameState;
import cz.agents.gtlibrary.experimental.imperfectrecall.dag.DAGConfig;
import cz.agents.gtlibrary.experimental.imperfectrecall.dag.DAGGameState;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;

public class DAGRandomGameState extends RandomGameState implements DAGGameState {

    protected DAGConfig config;

    public DAGRandomGameState(DAGConfig config) {
        super();
        this.config = config;
    }

    public DAGRandomGameState(DAGRandomGameState gameState) {
        super(gameState);
        this.config = gameState.config;
    }

    @Override
    public Object getDAGKey() {
        return getISKeyForPlayerToMove();
    }

    @Override
    public GameState performAction(Action action) {
        GameState successor = config.getSuccessor(this, action);

        if (successor == null) {
            successor = super.performAction(action);
            config.setSuccessor(this, action, successor);
        }
        return successor;
    }

    @Override
    public GameState copy() {
        return new DAGRandomGameState(this);
    }
}
