package cz.agents.gtlibrary.algorithms.mcts.nodes;

import cz.agents.gtlibrary.algorithms.mcts.MCTSConfig;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.BasicStats;
import cz.agents.gtlibrary.algorithms.mcts.distribution.Distribution;
import cz.agents.gtlibrary.iinodes.LinkedListSequenceImpl;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.strategy.Strategy;
import cz.agents.gtlibrary.utils.FixedSizeMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class InnerNode extends NodeImpl {

	protected Map<Action, Node> children;
	protected List<Action> actions;
	protected Player currentPlayer;
	protected Expander<MCTSInformationSet> expander;
	protected MCTSInformationSet informationSet;
        protected BasicStats[] nodeStats;

	protected boolean isLocked;

	public InnerNode(InnerNode parent, GameState gameState, Action lastAction) {
		super(parent, lastAction, gameState);
		currentPlayer = gameState.getPlayerToMove();
		isLocked = false;
		this.expander = parent.expander;
		attendInformationSet();
		actions = expander.getActions(gameState);
	}

	public InnerNode(Expander<MCTSInformationSet> expander, MCTSConfig config, GameState gameState) {
		super(config, gameState);
		currentPlayer = gameState.getPlayerToMove();
		isLocked = false;
		this.expander = expander;
		attendInformationSet();
		actions = expander.getActions(gameState);
	}

	private void attendInformationSet() {
		informationSet = algConfig.getInformationSetFor(gameState);

                //adding a new information set to the config
		if (informationSet.getAllNodes().isEmpty()) {
			algConfig.addInformationSetFor(gameState, informationSet);
		}
		informationSet.addNode(this);
		informationSet.addStateToIS(gameState);
	}

	@Override
	public double[] simulate() {
		return algConfig.getSimulator().simulate(gameState, expander);
	}

	public Node selectChild() {
		return getChildFor(getActionFromDecisionStrategy(currentPlayer.getId()));
	}

	public Node getNewChildAfter(Action action) {
		GameState nextState = gameState.performAction(action);

		if (nextState.isGameEnd()) {
			return new LeafNode(this, nextState, action);
		}
		if (nextState.isPlayerToMoveNature()) {
			return new ChanceNode(this, nextState, action);
		}
		return new InnerNode(this, nextState, action);
	}

	@Override
	public void backPropagate(Action action, double[] values) {
                //happens only in the current leaf node
                if (action != null && currentPlayer.getId() < 2) {
                    values[currentPlayer.getId()] =  informationSet.backPropagate(this, action, values[currentPlayer.getId()]);
                    values[1-currentPlayer.getId()] = -values[currentPlayer.getId()];
                }
                for (int i=0; i < nodeStats.length; i++) nodeStats[i].onBackPropagate(values[i]);
		if (parent != null && !parent.isLocked()) {
			parent.backPropagate(lastAction, values);
		}
	}
        
        public Node getChildOrNull(Action action){
            return children.get(action);
        }

	protected Node getChildFor(Action action) {
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

	protected Action getActionFromDecisionStrategy(int playerIndex) {
		return informationSet.selectionStrategy.select();
	}

	@Override
	public Node selectRecursively() {
		if (children == null)
			return this;
		return selectChild().selectRecursively();
	}

	public Node selectRecursively(int fixedDepth) {
		if (fixedDepth > 0)
			isLocked = true;
		if (children == null)
			return this;
		Node child = selectChild();

		return child instanceof LeafNode ? child : ((InnerNode) child).selectRecursively(fixedDepth - 1);
	}

	public boolean isLocked() {
		return isLocked;
	}

	@Override
	public int hashCode() {
		return gameState.getHistory().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof InnerNode))
			return false;
		return this.hashCode() == obj.hashCode();
	}

        public List<Action> getActions() {
            return actions;
        }

        public MCTSInformationSet getInformationSet() {
            return informationSet;
        }

	@Override
	public void expand() {
		if (children != null) {
			return;
		}
                nodeStats =  new BasicStats[gameState.getAllPlayers().length];
                for (int i = 0; i < nodeStats.length; i++) {
                        nodeStats[i] = new BasicStats();
                }
		children = new FixedSizeMap<Action, Node>(actions.size());
		informationSet.initStats(actions, algConfig.getBackPropagationStrategyFactory());
	}

	@Override
	public double[] getEV() {
		double[] ev = new double[gameState.getAllPlayers().length];

		for (int i = 0; i < ev.length; i++)
			ev[i] = nodeStats[i].getEV();
		return ev;
	}

	@Override
	public int getNbSamples() {
		return nodeStats[0].getNbSamples();
	}

        protected Strategy getStrategyFor(Node node, Player player, Distribution distribution){
            return getStrategyFor(node, player, distribution, Integer.MAX_VALUE);
        }

	protected Strategy getStrategyFor(Node node, Player player, Distribution distribution, int cutOffDepth) {
		if (node == null || cutOffDepth == 0) {
			return algConfig.getEmptyStrategy();
		}
		return node.getStrategyFor(player, distribution, cutOffDepth);
	}

	protected Sequence createSequenceForStrategy() {
		return new LinkedListSequenceImpl(gameState.getSequenceForPlayerToMove());
	}

        
        @Override
	public Strategy getStrategyFor(Player player, Distribution distribution, int cutOffDepth) {
            if (children == null)
                    return algConfig.getEmptyStrategy();
            Strategy strategy = algConfig.getEmptyStrategy();
            Map<Action, Double> actionDistribution = distribution.getDistributionFor(informationSet);

            for (Entry<Action, Double> actionEn : actionDistribution.entrySet()) {
                if (player.equals(currentPlayer)) {
                    if (actionEn.getValue() > 0) {
                        Sequence sequence = new LinkedListSequenceImpl(currentPlayer);
                        sequence.addLast(actionEn.getKey());
                        strategy.put(sequence, actionEn.getValue());
                        for (Map.Entry<Sequence, Double> seqEn : getStrategyFor(children.get(actionEn.getKey()), player, distribution, cutOffDepth-1).entrySet()) {
                            sequence = new LinkedListSequenceImpl(seqEn.getKey());
                            sequence.addFirst(actionEn.getKey());
                            strategy.put(sequence, actionEn.getValue() * seqEn.getValue());
                        }
                    }
                } else {
                    for (Map.Entry<Sequence, Double> seqEn : getStrategyFor(children.get(actionEn.getKey()), player, distribution, cutOffDepth-1).entrySet()) {
//                        Double prob = strategy.get(seqEn.getKey());
//                        if (prob != null) {
//                            assert seqEn.getValue() == prob;
//                        } else {
                            strategy.put(seqEn.getKey(),seqEn.getValue());
//                        }
                        
                    }
                }
            }
            return strategy;
	}

    public Map<Action, Node> getChildren() {
        return children;
    }

        
}
