package cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces;

import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.MCTSPublicState;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Player;

import java.util.List;
import java.util.Map;

public interface InnerNode extends Node {
    Node getChildOrNull(Action action);

    Node getChildFor(Action action);

    List<Action> getActions();

    void setActions(List<Action> actions);

    MCTSInformationSet getInformationSet();

    void setInformationSet(MCTSInformationSet informationSet);

    MCTSPublicState getPublicState();

    Map<Action, Node> getChildren();

    void setChildren(Map<Action, Node> children);

    double getReachPr();

    void setReachPr(double meanStrategyActionPr);

    default Player getPlayerToMove() {
        return getGameState().getPlayerToMove();
    }

    default boolean isPlayerMoving(Player player) {
        return player.equals(getPlayerToMove());
    }

    default boolean isGameEnd() {
        return false;
    }
}
