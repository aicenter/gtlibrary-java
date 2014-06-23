package cz.agents.gtlibrary.algorithms.mcts.nodes;

import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.utils.FixedSizeMap;

import java.util.List;
import java.util.Map;

public class InnerNode extends NodeImpl {

    protected Map<Action, Node> children;
    protected List<Action> actions;
    protected MCTSInformationSet informationSet;

    public InnerNode(InnerNode parent, GameState gameState, Action lastAction) {
        super(parent, lastAction, gameState);
        attendInformationSet();
        actions = getExpander().getActions(gameState);
        children = new FixedSizeMap<Action, Node>(actions.size());
    }

    public InnerNode(Expander<MCTSInformationSet> expander, GameState gameState) {
        super(expander, gameState);
        attendInformationSet();
        actions = getExpander().getActions(gameState);
        children = new FixedSizeMap<Action, Node>(actions.size());
    }

    private void attendInformationSet() {
        informationSet = getAlgConfig().getInformationSetFor(gameState);

        //adding a new information set to the config
        if (informationSet.getAllNodes().isEmpty()) {
            getAlgConfig().addInformationSetFor(gameState, informationSet);
        }
        informationSet.addNode(this);
        informationSet.addStateToIS(gameState);
    }

    protected Node getNewChildAfter(Action action) {
        assert children.get(action) == null;
        GameState nextState = gameState.performAction(action);

        if (nextState.isGameEnd()) {
            return new LeafNode(this, nextState, action);
        }
        if (nextState.isPlayerToMoveNature()) {
            return new ChanceNode(this, nextState, action);
        }
        return new InnerNode(this, nextState, action);
    }

    public Node getChildOrNull(Action action) {
        return children.get(action);
    }

    public Node getChildFor(Action action) {
        Node selected = children.get(action);

        if (selected == null) {
            selected = createChild(action);
        }
        return selected;
    }

    protected Node createChild(Action action) {
        Node child = getNewChildAfter(action);

        children.put(action, child);
        return child;
    }

    @Override
    public int hashCode() {
        return gameState.getHistory().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof InnerNode))
            return false;
        return gameState.getHistory().equals(((InnerNode) obj).getGameState().getHistory());
    }

    public List<Action> getActions() {
        return actions;
    }

    public MCTSInformationSet getInformationSet() {
        return informationSet;
    }

    public void setInformationSet(MCTSInformationSet informationSet) {
        this.informationSet = informationSet;
    }

    public Map<Action, Node> getChildren() {
        return children;
    }

    public void setChildren(Map<Action, Node> children) {
        this.children = children;
    }

    public void setActions(List<Action> actions) {
        this.actions = actions;
    }
}
