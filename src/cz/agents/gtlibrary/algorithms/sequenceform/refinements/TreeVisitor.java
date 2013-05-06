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

	protected Key[] lastKeys;

	public TreeVisitor(GameState rootState, Expander<? extends InformationSet> expander, AlgorithmConfig<SequenceInformationSet> algConfig) {
		this.rootState = rootState;
		this.expander = expander;
		lastKeys = new Key[] { new Key(rootState.getAllPlayers()[0]), new Key(rootState.getAllPlayers()[1]) };
		this.algConfig = algConfig;
	}

	public void visitTree(GameState root) {
		if (algConfig.getInformationSetFor(root) == null)
			algConfig.addInformationSetFor(root, new SequenceInformationSet(root));
		algConfig.getInformationSetFor(root).addStateToIS(root);
		if (root.isPlayerToMoveNature()) {
			visitChanceNode(root);
		} else if (root.isGameEnd()) {
			visitLeaf(root);
		} else {
			visitNormalNode(root);
		}
	}

	protected void visitNormalNode(GameState state) {
		Key[] oldLastKeys = lastKeys.clone();

		for (Action action : expander.getActions(state)) {
			GameState child = state.performAction(action);

			lastKeys[state.getPlayerToMove().getId()] = new Key(getISKey(child, state.getPlayerToMove()));
			visitTree(child);
		}
		lastKeys = oldLastKeys;
	}

	private Object getISKey(GameState child, Player player) {
//		return player.getId() == 0 ? child.getISKeyForFirstPlayer() : child.getISKeyForSecondPlayer();
		return child.getSequenceFor(player);
	}

	protected abstract void visitLeaf(GameState state);

	protected void visitChanceNode(GameState state) {
		for (Action action : expander.getActions(state)) {
			GameState child = state.performAction(action);

			visitTree(child);
		}
	}

}
