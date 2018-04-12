package cz.agents.gtlibrary.domain.pursuit;

import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.iinodes.PerfectRecallISKey;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.utils.graph.Node;

import java.util.ArrayList;
import java.util.List;

public class VisibilityPursuitGameState extends PursuitGameState {

    public VisibilityPursuitGameState() {
    }

    public VisibilityPursuitGameState(PursuitGameState gameState) {
        super(gameState);
    }

    @Override
    public ISKey getISKeyForPlayerToMove() {
        List<Node> observedNodes = new ArrayList<>(2);
        Node evaderLastPosition = sequence.size() < 2 ? graph.getEvaderStart() : ((EvaderPursuitAction) sequence.get(sequence.size() - 2)).getDestination();

        if (getPlayerToMove().equals(PursuitGameInfo.EVADER)) {
            if (graph.getDistance(evaderPosition, p1Position) <= 2)
                observedNodes.add(p1Position);
            if (graph.getDistance(evaderPosition, p2Position) <= 2)
                observedNodes.add(p2Position);
        } else {
            if (graph.getDistance(evaderLastPosition, p1Position) <= 2 || graph.getDistance(evaderLastPosition, p2Position) <= 2)
                observedNodes.add(evaderLastPosition);
        }
        return new PerfectRecallISKey(observedNodes.hashCode(), getSequenceForPlayerToMove());
    }

    @Override
    public GameState copy() {
        return new VisibilityPursuitGameState(this);
    }
}
