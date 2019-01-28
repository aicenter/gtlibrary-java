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

import cz.agents.gtlibrary.algorithms.cfr.CFRAlgorithm;
import cz.agents.gtlibrary.algorithms.cr.gadgettree.GadgetChanceNode;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.MCTSPublicState;
import cz.agents.gtlibrary.algorithms.mcts.distribution.MeanStrategyProvider;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.ChanceNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.LeafNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.Node;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.FixedSizeMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class InnerNodeImpl extends NodeImpl implements InnerNode {

    protected Map<Action, Node> children;
    protected List<Action> actions;
    protected MCTSInformationSet informationSet;
    private MCTSPublicState publicState;
    private double[] playerReachPr = new double[] {1.,1.,1.};
    private double evSumTime = 0.;
    private double evSumWeightedPl = 0.;
    private double evSumWeightedAll = 0.;
    private double sumReachPl = 0.;
    private double sumReachAll = 0.;
    protected MCTSInformationSet oppAugInformationSet;
    public static boolean saveChildren = true;
    public static boolean attendIS = true;
    public static boolean attendPS = true;
    public boolean destroyed = false;

    public static final int BASELINE_NONE = 0;
    public static final int BASELINE_UTILITY_WEIGHTED_PL = 1;
    public static final int BASELINE_UTILITY_WEIGHTED_ALL = 2;
    public static final int BASELINE_UTILITY_TIME = 3;
    public static final int BASELINE_UTILITY_CONST = 4;
    public static final int BASELINE_ORACLE = 5;
    public static int baselineMethod = BASELINE_NONE;
    private Map<Action, Double> cachedOracleValues;
    public static boolean shouldCacheOracleValue = false;

    /**
     * Non-root node constructor
     */
    public InnerNodeImpl(InnerNode parent, GameState gameState, Action lastAction) {
        super(parent, lastAction, gameState);
        playerReachPr[2] = parent.getChanceReachPr();
        if(parent instanceof ChanceNode && !(parent instanceof GadgetChanceNode)) {
            playerReachPr[2] *= parent.getProbabilityOfNatureFor(lastAction);
        }

        if(attendIS) attendInformationSet();
        if(attendPS) attendPublicState();

        if (actions == null)
            actions = getExpander().getActions(gameState);
        children = new FixedSizeMap<Action, Node>(actions.size());

        if(shouldCacheOracleValue) cachedOracleValues = new HashMap<>(actions.size());
    }

    /**
     * Root node constructor
     */
    public InnerNodeImpl(Expander<MCTSInformationSet> expander, GameState gameState) {
        super(expander, gameState);
        if(attendIS) attendInformationSet();
        if(attendPS) attendPublicState();

        if (actions == null)
            actions = expander.getActions(gameState);
        children = new FixedSizeMap<Action, Node>(actions.size());

        if(shouldCacheOracleValue) cachedOracleValues = new HashMap<>(actions.size());
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

        if(saveChildren) children.put(action, child);
        return child;
    }

    @Override
    public int hashCode() {
        if(gameState == null) return 0;
        if(gameState.getHistory() == null) return 0;
        return gameState.getHistory().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof InnerNode))
            return false;
        if(this.gameState == null) return false;
        if(this.gameState.getHistory() == null) return false;

        InnerNode objNode = ((InnerNode) obj);
        if(objNode.getGameState() == null) return false;
        if(objNode.getGameState().getHistory() == null) return false;
        return gameState.getHistory().equals(objNode.getGameState().getHistory());
    }

    public List<Action> getActions() {
        return actions;
    }

    public MCTSInformationSet getInformationSet() {
        return informationSet;
    }

    @Override
    public MCTSPublicState getPublicState() {
        return publicState;
    }

    @Override
    public void setPublicState(MCTSPublicState publicState) {
        this.publicState = publicState;
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

    @Override
    public double getReachPrPlayerChance() {
        return getPlayerReachPr() * getChanceReachPr();
    }

    @Override
    public double getEVWeightedPl() {
        return evSumWeightedPl == 0 ? 0 : evSumWeightedPl / (getSumReachPl());
    }

    @Override
    public void setEVWeightedPl(double sum) {
        this.evSumWeightedPl = sum;
    }

    @Override
    public void updateEVWeightedPl(double offPolicyAproxSample) {
        this.evSumWeightedPl += offPolicyAproxSample;
    }

    @Override
    public double getEVTime(double iterationNum) {
        return evSumTime == 0 ? 0 : evSumTime / iterationNum;
    }

    @Override
    public void setEVTime(double sum) {
        this.evSumTime = sum;
    }

    @Override
    public void updateEVTime(double offPolicyAproxSample) {
        this.evSumTime += offPolicyAproxSample;
    }

    @Override
    public double getSumReachPl() {
        return sumReachPl;
    }

    @Override
    public void updateSumReachPl(double currentReachP) {
        sumReachPl += currentReachP;
    }

    @Override
    public void setSumReachPl(double sumReachP) {
        this.sumReachPl = sumReachP;
    }

    @Override
    public double getEVWeightedAll() {
        return evSumWeightedAll == 0 ? 0 : evSumWeightedAll / (getSumReachAll());
    }

    @Override
    public void updateEVWeightedAll(double currentOffPolicyAproxSample) {
        evSumWeightedAll += currentOffPolicyAproxSample;
    }

    @Override
    public void setEVWeightedAll(double sumOffPolicyAproxSample) {
        evSumWeightedAll = sumOffPolicyAproxSample;
    }

    @Override
    public double getSumReachAll() {
        return sumReachAll;
    }

    @Override
    public void updateSumReachAll(double currentReachP) {
        sumReachAll += currentReachP;
    }

    @Override
    public void setSumReachAll(double sumReachP) {
        sumReachAll += sumReachP;
    }

    @Override
    public double getReachPr() {
        return playerReachPr[0]*playerReachPr[1]*playerReachPr[2];
    }

    @Override
    public double getBaselineFor(Action a, Player pl) {
        assert pl.getId() == 0 || pl.getId() == 1;
        if(baselineMethod == BASELINE_NONE) return 0;
        if(baselineMethod == BASELINE_UTILITY_CONST) { // useful for debugging
            return (pl.getId() == 0 ? 1 : -1) * 0.7839506172839504;
        }

        // we cannot retrieve a baseline
        Node child = getChildOrNull(a);
        if(child == null) return 0;

        // we do not store utilities at nodes that have only one action
        if(getActions().size() == 1 || child instanceof LeafNode) return child.getBaselineFor(a, pl);

        InnerNode in = (InnerNode) child;
        int sign = pl.getId() == 0 ? 1 : -1;
        switch(baselineMethod) {
            // maybe let's do this later, it's annoying to pass over the number of iters
            // case BASELINE_UTILITY_TIME:
            //     return sign * in.getEVTime();
            case BASELINE_UTILITY_WEIGHTED_ALL:
                return sign * in.getEVWeightedAll();
            case BASELINE_UTILITY_WEIGHTED_PL:
                return sign * in.getEVWeightedPl();
            case BASELINE_ORACLE:
                // return value for given player
                if(shouldCacheOracleValue && cachedOracleValues.get(a) != null) return sign * cachedOracleValues.get(a);

                double val = CFRAlgorithm.computeExpUtilityOfState(in, pl, true);
                if(shouldCacheOracleValue) cachedOracleValues.put(a, sign * val); // store value for pl0

                return val;
            default:
                throw new RuntimeException("invalid choice of baseline method");
        }
    }

    @Override
    public void resetData() {
        this.evSumTime = 0.;
        this.evSumWeightedPl = 0.;
        this.evSumWeightedAll = 0.;
        this.sumReachPl = 0.;
        this.sumReachAll = 0.;
    }

    @Override
    public void destroy() {
        super.destroy();
        destroyed = true;
        children = null;
        actions = null;
        informationSet = null;
        publicState = null;
        oppAugInformationSet = null;
    }
}
