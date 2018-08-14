package cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces;

import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.MCTSPublicState;
import cz.agents.gtlibrary.iinodes.PerfectRecallISKey;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.History;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;

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

    default double getReachPr() {
        return getPlayerReachPr() * getChanceReachPr();
    }

    double getPlayerReachPr();
    void setPlayerReachPr(double meanStrategyActionPr);
    double getChanceReachPr();

    default Player getPlayerToMove() {
        return getGameState().getPlayerToMove();
    }

    default Player getOpponentPlayer() {
        Player pl = getPlayerToMove();
        if(pl.getId() == 2) {
            throw new RuntimeException("Chance does not have opponent player!");
        }
        return getAllPlayers()[1-pl.getId()];
    }

    default boolean isPlayerMoving(Player player) {
        return player.equals(getPlayerToMove());
    }

    default boolean isGameEnd() {
        return false;
    }

    default PerfectRecallISKey getOpponentAugISKey() {
        History history = getGameState().getHistory();
        Player player = getPlayerToMove();
        Player opp = getOpponentPlayer();
        Sequence oppSeq = history.getSequencesOfPlayers().get(opp);
        int hashCode = getGameState().getISKeyForPlayerToMove().hashCode();
        return new PerfectRecallISKey(hashCode, oppSeq);
    }
}
