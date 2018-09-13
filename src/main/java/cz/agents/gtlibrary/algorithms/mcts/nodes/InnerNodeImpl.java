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

import cz.agents.gtlibrary.algorithms.cr.gadgettree.GadgetChanceNode;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.MCTSPublicState;
import cz.agents.gtlibrary.algorithms.mcts.distribution.MeanStrategyProvider;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.ChanceNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.Node;
import cz.agents.gtlibrary.iinodes.PSKey;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.FixedSizeMap;

import java.util.List;
import java.util.Map;


public class InnerNodeImpl extends NodeImpl implements InnerNode {

    protected Map<Action, Node> children;
    protected List<Action> actions;
    protected MCTSInformationSet informationSet;
    private MCTSPublicState publicState;
    private double[] playerReachPr = new double[] {1.,1.,1.};
    private double evSum = 0.;
    protected MCTSInformationSet oppAugInformationSet;

    /**
     * Non-root node constructor
     */
    public InnerNodeImpl(InnerNode parent, GameState gameState, Action lastAction) {
        super(parent, lastAction, gameState);
        playerReachPr[2] = parent.getChanceReachPr();
        if(parent instanceof ChanceNode && !(parent instanceof GadgetChanceNode)) {
            playerReachPr[2] *= parent.getProbabilityOfNatureFor(lastAction);
        }

        attendInformationSet();
        attendPublicState();
        if (actions == null)
            actions = getExpander().getActions(gameState);
        children = new FixedSizeMap<Action, Node>(actions.size());
    }

    /**
     * Root node constructor
     */
    public InnerNodeImpl(Expander<MCTSInformationSet> expander, GameState gameState) {
        super(expander, gameState);
        attendInformationSet();
        attendPublicState();
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
    private void attendPublicState() {
        if(gameState instanceof DomainWithPublicState) {
            publicState = getAlgConfig().getPublicStateFor(this);
            publicState.addNodeToPublicState(this);
            if(informationSet!=null) {
                informationSet.setPublicState(publicState);
            } else {
                assert gameState.isPlayerToMoveNature();
            }
        }
    }

    protected Node getNewChildAfter(Action action) {
        assert children.get(action) == null;
        GameState nextState = gameState.performAction(action);

        if (nextState.isGameEnd()) {
            return new LeafNodeImpl(this, nextState, action);
        }
        if (nextState.isPlayerToMoveNature()) {
            return new ChanceNodeImpl(this, nextState, action, getAlgConfig().getRandom());
        }
        return new InnerNodeImpl(this, nextState, action);
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

    public MCTSPublicState getPublicState() {
        return publicState;
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

    @Override
    public double getReachPrByPlayer(int player) {
        assert playerReachPr[player] <= 2 && playerReachPr[player] >= 0;
        return playerReachPr[player];
    }

    @Override
    public void setReachPrByPlayer(int player, double meanStrategyPr) {
        if(player == 2) throw new RuntimeException("Cannot overwrite reach probability for chance");
        playerReachPr[player] = meanStrategyPr;
        assert meanStrategyPr <= 1 && meanStrategyPr >= 0;
    }

    private Double reachPr = null;
    @Override
    public double getReachPrPlayerChance() {
        return getPlayerReachPr() * getChanceReachPr();
    }

    @Override
    public double getExpectedValue(int iterationNum) {
        return this.evSum / iterationNum;
    }

    @Override
    public void setExpectedValue(double sum) {
        this.evSum = sum;
    }

    @Override
    public void updateExpectedValue(double offPolicyAproxSample) {
        this.evSum += offPolicyAproxSample;
    }

    @Override
    public void resetData() {
        this.evSum = 0;
    }

    @Override
    public double getReachPr() {
        return playerReachPr[0]*playerReachPr[1]*playerReachPr[2];
    }
}
