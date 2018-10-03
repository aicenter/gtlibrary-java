package cz.agents.gtlibrary.domain.pursuit;

import cz.agents.gtlibrary.algorithms.cfr.br.responses.AbstractObservationProvider;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.iinodes.PerfectRecallISKey;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.utils.graph.Node;

import java.util.ArrayList;
import java.util.List;

public class VisibilityPursuitGameState extends PursuitGameState implements AbstractObservationProvider {

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
            if (graph.getDistance(evaderPosition, p1Position) <= PursuitGameInfo.visibility)
                observedNodes.add(p1Position);
            if (graph.getDistance(evaderPosition, p2Position) <= PursuitGameInfo.visibility)
                observedNodes.add(p2Position);
        } else {
            if (graph.getDistance(evaderLastPosition, p1Position) <= PursuitGameInfo.visibility || graph.getDistance(evaderLastPosition, p2Position) <= PursuitGameInfo.visibility)
                observedNodes.add(evaderLastPosition);
        }
        return new PerfectRecallISKey(observedNodes.hashCode(), getSequenceForPlayerToMove());
    }

    @Override
    public GameState copy() {
        return new VisibilityPursuitGameState(this);
    }

    // assumes grid, immediate neighbors visibility
    @Override
    public Object getAbstractObservation() {
        boolean isL = false;
        boolean isR = false;
        boolean isU = false;
        boolean isD = false;
        if (graph.getDistance(evaderPosition, p1Position) <= PursuitGameInfo.visibility) {
            boolean observed = false;
            if(p1Position.getIntID() + 1 == evaderPosition.getIntID()) {
                isL = true;
                observed = true;
            }
            if(p1Position.getIntID() - 1 == evaderPosition.getIntID()) {
                isR = true;
                observed = true;
            }
            if(!observed) {
                if (p1Position.getIntID() > evaderPosition.getIntID())
                    isD = true;
                if (p1Position.getIntID() < evaderPosition.getIntID())
                    isU = true;
            }
        }
        if (graph.getDistance(evaderPosition, p2Position) <= PursuitGameInfo.visibility) {
            boolean observed = false;
            if(p2Position.getIntID() + 1 == evaderPosition.getIntID()) {
                isL = true;
                observed = true;
            }
            if(p2Position.getIntID() - 1 == evaderPosition.getIntID()) {
                isR = true;
                observed = true;
            }
            if(!observed) {
                if (p2Position.getIntID() > evaderPosition.getIntID())
                    isD = true;
                if (p2Position.getIntID() < evaderPosition.getIntID())
                    isU = true;
            }
        }

        if(isL && isR)
            return PursuitGameInfo.LR;
        if(isL && isU)
            return PursuitGameInfo.LU;
        if(isL && isD)
            return PursuitGameInfo.LD;
        if(isR && isU)
            return PursuitGameInfo.RU;
        if(isR && isD)
            return PursuitGameInfo.RD;
        if(isU && isD)
            return PursuitGameInfo.UD;
        if(isD)
            return PursuitGameInfo.DOWN;
        if(isU)
            return PursuitGameInfo.UP;
        if(isR)
            return PursuitGameInfo.RIGHT;
        if(isL)
            return PursuitGameInfo.LEFT;

        return PursuitGameInfo.SAME;
    }
}
