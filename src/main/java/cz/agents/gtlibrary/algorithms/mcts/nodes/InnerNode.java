package cz.agents.gtlibrary.algorithms.mcts.nodes;

import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.MCTSPublicState;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Expander;

import java.util.List;
import java.util.Map;

public interface InnerNode extends Node {
    public Node getChildOrNull(Action action);

    public Node getChildFor(Action action);

    public List<Action> getActions();

    public MCTSInformationSet getInformationSet();

    public MCTSPublicState getPublicState();

    public void setInformationSet(MCTSInformationSet informationSet);

    public Map<Action, Node> getChildren();

    public void setChildren(Map<Action, Node> children);

    public void setActions(List<Action> actions);
}
