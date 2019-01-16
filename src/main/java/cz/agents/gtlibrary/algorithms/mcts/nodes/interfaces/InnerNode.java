package cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces;

import cz.agents.gtlibrary.NotImplementedException;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.MCTSPublicState;
import cz.agents.gtlibrary.iinodes.PSKey;
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

    default Map<Action, Node> buildChildren() {
        if(getChildren().size() != getActions().size()) {
            getActions().forEach(this::getChildFor);
        }
        return getChildren();
    }

    void setChildren(Map<Action, Node> children);

    default double getReachPrPlayerChance() {
        return getPlayerReachPr() * getChanceReachPr();
    }

    default double getReachPr() {
        return getPlayerReachPr() * getOpponentReachPr() * getChanceReachPr();
    }

    default double getPlayerReachPr() {
        return getReachPrByPlayer(getPlayerToMove());
    }

    default double getOpponentReachPr() {
        return getReachPrByPlayer(getOpponentPlayerToMove());
    }

    default double getChanceReachPr() {
        return getReachPrByPlayer(getChancePlayer());
    }

    default double getReachPrByPlayer(Player player) {
        return getReachPrByPlayer(player.getId());
    }
    double getReachPrByPlayer(int playerId);
    default void setReachPrByPlayer(Player player, double meanStrategyPr) {
        setReachPrByPlayer(player.getId(), meanStrategyPr);
    }
    void setReachPrByPlayer(int playerId, double meanStrategyPr);

    default Player getPlayerToMove() {
        return getGameState().getPlayerToMove();
    }

    default Player getOpponentPlayerToMove() {
        return getGameState().getOpponentPlayerToMove();
    }

    default Player getChancePlayer() {
        return getGameState().getAllPlayers()[2];
    }

    default boolean isPlayerMoving(Player player) {
        return player.equals(getPlayerToMove());
    }

    default boolean isGameEnd() {
        return false;
    }

    default PerfectRecallISKey getOpponentAugISKey() {
        History history = getGameState().getHistory();
        Player opp = getOpponentPlayerToMove();
        Sequence oppSeq = history.getSequencesOfPlayers().get(opp);
        int hashCode = oppSeq.hashCode();
        return new PerfectRecallISKey(hashCode, oppSeq);
    }

    // always store values with the sign of player 0
    double getEVWeightedPl();
    void updateEVWeightedPl(double currentOffPolicyAproxSample);
    void setEVWeightedPl(double sumOffPolicyAproxSample);
    double getSumReachPl();
    void updateSumReachPl(double currentReachP);
    void setSumReachPl(double sumReachP);

    double getEVWeightedAll();
    void updateEVWeightedAll(double currentOffPolicyAproxSample);
    void setEVWeightedAll(double sumOffPolicyAproxSample);
    double getSumReachAll();
    void updateSumReachAll(double currentReachP);
    void setSumReachAll(double sumReachP);

    void updateEVTime(double v);
    double getEVTime(double iterationNum);
    void setEVTime(double sumOffPolicyAproxSample);

    void resetData();

    void destroy();

    void setPublicState(MCTSPublicState ps);
}
