/*
Copyright 2014 Faculty of Electrical Engineering at CTU in Prague

This file is part of Game Theoretic Library.

Game Theoretic Library is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Game Theoretic Library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with Game Theoretic Library.  If not, see <http://www.gnu.org/licenses/>.*/


package cz.agents.gtlibrary.algorithms.mcts.nodes;

import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.distribution.MeanStrategyProvider;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.Selector;
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
        if (actions == null)
            actions = getExpander().getActions(gameState);
        children = new FixedSizeMap<Action, Node>(actions.size());
    }

    public InnerNode(Expander<MCTSInformationSet> expander, GameState gameState) {
        super(expander, gameState);
        attendInformationSet();
        if (actions == null)
            actions = expander.getActions(gameState);
        children = new FixedSizeMap<Action, Node>(actions.size());
    }

    private void attendInformationSet() {
        if (gameState.isPlayerToMoveNature())
            return;
        informationSet = getAlgConfig().getInformationSetFor(gameState);

        //if the actions are already created reuse them
        if (informationSet.getAlgorithmData() != null)
            actions = ((MeanStrategyProvider) informationSet.getAlgorithmData()).getActions();

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
            return new ChanceNode(this, nextState, action, getAlgConfig().getRandom());
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
