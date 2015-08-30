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


package cz.agents.gtlibrary.algorithms.sequenceform.refinements.nfp;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.Key;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerExpander;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerGameState;
import cz.agents.gtlibrary.interfaces.*;

import java.util.HashSet;
import java.util.Set;

public class SanityCheck {

	private GameState root;
	private Expander<? extends InformationSet> expander;

	private Set<Key> keys;
	private Set<Sequence> sequences;

	public static void main(String[] args) {
		SanityCheck check = new SanityCheck(new GenericPokerGameState(), new GenericPokerExpander<SequenceInformationSet>(new SequenceFormConfig<SequenceInformationSet>()));

		check.check();
	}

	public SanityCheck(GameState root, Expander<? extends InformationSet> expander) {
		this.root = root;
		this.expander = expander;
		sequences = new HashSet<Sequence>();
		keys = new HashSet<Key>();
	}

	public void check() {
		check(root);
	}

	private void check(GameState state) {
		((SequenceFormConfig<SequenceInformationSet>)expander.getAlgorithmConfig()).addStateToSequenceForm(state);//co zkusit přenásobit utility velkym číslem
		if (state.isPlayerToMoveNature()) {
//			assert !sequences.contains(state.getSequenceFor(state.getAllPlayers()[0])) || !sequences.contains(state.getSequenceFor(state.getAllPlayers()[1]));
			assert state.getSequenceFor(state.getAllPlayers()[0]).size() == 0 || sequences.contains(state.getSequenceFor(state.getAllPlayers()[0]).getSubSequence(0, state.getSequenceFor(state.getAllPlayers()[0]).size() - 1));
			assert state.getSequenceFor(state.getAllPlayers()[1]).size() == 0 || sequences.contains(state.getSequenceFor(state.getAllPlayers()[1]).getSubSequence(0, state.getSequenceFor(state.getAllPlayers()[1]).size() - 1));
			assert state.getSequenceFor(state.getAllPlayers()[0]).size() == 0 || keys.contains(new Key("u", state.getSequenceFor(state.getAllPlayers()[0]).getSubSequence(0, state.getSequenceFor(state.getAllPlayers()[0]).size() - 1)));
			assert state.getSequenceFor(state.getAllPlayers()[1]).size() == 0 || keys.contains(new Key("u", state.getSequenceFor(state.getAllPlayers()[1]).getSubSequence(0, state.getSequenceFor(state.getAllPlayers()[1]).size() - 1)));
			sequences.add(state.getSequenceFor(state.getAllPlayers()[0]));
			sequences.add(state.getSequenceFor(state.getAllPlayers()[1]));
			keys.add(new Key("u", state.getSequenceFor(state.getAllPlayers()[0])));
			keys.add(new Key("u", state.getSequenceFor(state.getAllPlayers()[1])));

			for (Action action : expander.getActions(state)) {
				check(state.performAction(action));
			}
		} else {
//			assert !sequences.contains(state.getSequenceForPlayerToMove());
			assert state.getSequenceForPlayerToMove().size() == 0 || sequences.contains(state.getSequenceForPlayerToMove().getSubSequence(0, state.getSequenceForPlayerToMove().size() - 1));
			assert state.getSequenceForPlayerToMove().size() == 0 || keys.contains(new Key("u", state.getSequenceForPlayerToMove().getSubSequence(0, state.getSequenceForPlayerToMove().size() - 1)));
			sequences.add(state.getSequenceForPlayerToMove());
			keys.add(new Key("u", state.getSequenceForPlayerToMove()));
			if (!state.isGameEnd())
				for (Action action : expander.getActions(state)) {
					check(state.performAction(action));
				}
		}
	}

//	private void checkAfterNature(GameState state) {
//		
//		if (state.isPlayerToMoveNature()) {
//			for (Action action : expander.getActions(state)) {
//				checkAfterNature(state.performAction(action));
//			}
//		} else {
//			((SequenceFormConfig<SequenceInformationSet>)expander.getAlgorithmConfig()).addStateToSequenceForm(state);
//			for (Action action : expander.getActions(state)) {
//				check(state.performAction(action));
//			}
//		}
//	}

}
