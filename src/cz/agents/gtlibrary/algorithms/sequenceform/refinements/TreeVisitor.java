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


package cz.agents.gtlibrary.algorithms.sequenceform.refinements;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.iinodes.PerfectRecallISKey;
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
		String string = sequence.getPlayer().equals(players[0]) ? "P" : "Q";

		return new Key(string, new Key(sequence.getLastInformationSet().getISKey()));
	}

}
