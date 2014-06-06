package cz.agents.gtlibrary.algorithms.sequenceform.refinements;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.Pair;

public abstract class TreeVisitor {

	protected GameState rootState;
	protected Expander<? extends InformationSet> expander;
	protected AlgorithmConfig<SequenceInformationSet> algConfig;
	protected Player[] players;

	public TreeVisitor(GameState rootState, Expander<SequenceInformationSet> expander) {
		this.rootState = rootState;
		this.expander = expander;
		this.algConfig = expander.getAlgorithmConfig();
		this.players = rootState.getAllPlayers();
	}

	public void visitTree(GameState root) {
		if (algConfig.getInformationSetFor(root) == null)
			algConfig.addInformationSetFor(root, new SequenceInformationSet(root));

        if(!algConfig.getInformationSetFor(root).getAllStates().contains(root))
            algConfig.getInformationSetFor(root).addStateToIS(root);
		if (root.isGameEnd()) {
			visitLeaf(root);
		} else if (root.isPlayerToMoveNature()) {
			visitChanceNode(root);
		} else {
			visitNormalNode(root);
		}
	}

	protected void visitNormalNode(GameState state) {
		for (Action action : expander.getActions(state)) {
			visitTree(state.performAction(action));
		}
	}

	protected abstract void visitLeaf(GameState state);

	protected void visitChanceNode(GameState state) {
		for (Action action : expander.getActions(state)) {
			visitTree(state.performAction(action));
		}
	}

	protected Object getLastISKey(Sequence sequence) {
		InformationSet informationSet = sequence.getLastInformationSet();
		String string = sequence.getPlayer().equals(players[0]) ? "P" : "Q";

		return new Key(string, new Key(new Pair<Integer, Sequence>(informationSet.hashCode(), informationSet.getPlayersHistory())));
	}

}
