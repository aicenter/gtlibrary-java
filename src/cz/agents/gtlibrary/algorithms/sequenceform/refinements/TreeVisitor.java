package cz.agents.gtlibrary.algorithms.sequenceform.refinements;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;
import cz.agents.gtlibrary.interfaces.Player;

public abstract class TreeVisitor {

	protected GameState rootState;
	protected Expander<? extends InformationSet> expander;
	protected AlgorithmConfig<SequenceInformationSet> algConfig;
	protected Player[] players;

	public TreeVisitor(GameState rootState, Expander<? extends InformationSet> expander, AlgorithmConfig<SequenceInformationSet> algConfig) {
		this.rootState = rootState;
		this.expander = expander;
		this.algConfig = algConfig;
		this.players = rootState.getAllPlayers();
	}

	public void visitTree(GameState root, Player lastPlayer, Key lastKey) {
		if (algConfig.getInformationSetFor(root) == null)
			algConfig.addInformationSetFor(root, new SequenceInformationSet(root));
		algConfig.getInformationSetFor(root).addStateToIS(root);
		if (root.isPlayerToMoveNature()) {
			visitChanceNode(root, lastPlayer, lastKey);
		} else if (root.isGameEnd()) {
			visitLeaf(root, lastPlayer, lastKey);
		} else {
			visitNormalNode(root, lastPlayer, lastKey);
		}
	}

	protected void visitNormalNode(GameState state, Player lastPlayer, Key lastKey) {
		Key key = getKey(state);

		for (Action action : expander.getActions(state)) {
			GameState child = state.performAction(action);

			visitTree(child, state.getPlayerToMove(), key);
		}
	}

	protected Key getKey(GameState state) {
		return new Key(state.getPlayerToMove().getId() == 0 ? "P" : "Q", new Key(state.getISKeyForPlayerToMove()));
	}

	protected abstract void visitLeaf(GameState state, Player lastPlayer, Key lastKey);

	protected void visitChanceNode(GameState state, Player lastPlayer, Key lastKey) {
		for (Action action : expander.getActions(state)) {
			GameState child = state.performAction(action);

			visitTree(child, null, null);
		}
	}

}
