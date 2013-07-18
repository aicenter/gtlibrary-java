package cz.agents.gtlibrary.algorithms.sequenceform.refinements;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.Pair;

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
		for (Action action : expander.getActions(state)) {
			visitTree(state.performAction(action));
		}
	}

	protected Key getKey(GameState state) {
		return new Key(state.getPlayerToMove().getId() == 0 ? "P" : "Q", new Key(state.getISKeyForPlayerToMove()));
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
