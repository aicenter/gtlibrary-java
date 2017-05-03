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


package cz.agents.gtlibrary.domain.poker.kuhn;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import cz.agents.gtlibrary.domain.poker.PokerAction;
import cz.agents.gtlibrary.iinodes.ExpanderImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;


public class KuhnPokerExpander<I extends InformationSet> extends ExpanderImpl<I>{

	protected static final long serialVersionUID = -5389882092681466870L;

	public KuhnPokerExpander(AlgorithmConfig<I> algConfig) {
		super(algConfig);
	}

	@Override
	public List<Action> getActions(GameState gameState) {
		KuhnPokerGameState kpState = (KuhnPokerGameState) gameState;
		List<Action> actions = new ArrayList<Action>();

		if (kpState.getRound() == 0) {
			addActionsOfNature(kpState, actions, getAlgorithmConfig().getInformationSetFor(gameState));
			return actions;
		}
		addActionsOfRegularPlayer(kpState, actions, getAlgorithmConfig().getInformationSetFor(gameState));
		return actions;
	}

	protected void addActionsOfRegularPlayer(KuhnPokerGameState kpState, List<Action> actions, I informationSet) {
		LinkedList<PokerAction> history = kpState.getSequenceForAllPlayers();

		if (!kpState.isGameEnd()) {
			if (history.isEmpty() || history.getLast().getActionType().equals("ch")) {
				addActionsAfterPasiveAction(kpState, actions, informationSet);
			} else if (history.getLast().getActionType().equals("b")) {
				addActionsAfterAggressiveAction(kpState, actions, informationSet);
			} else if (history.getLast().getActionType().equals("c")) {
				addActionsAfterPasiveAction(kpState, actions, informationSet);
			} else if (!history.getLast().getActionType().equals("f")) {
				addActionsAfterPasiveAction(kpState, actions, informationSet);
			}
		}
	}

	protected void addActionsAfterAggressiveAction(KuhnPokerGameState kpState, List<Action> actions, I informationSets) {
		actions.add(createAction(kpState, "c", informationSets, kpState.getCardForActingPlayer().getActionType()));
		actions.add(createAction(kpState, "f", informationSets, kpState.getCardForActingPlayer().getActionType()));
	}

	protected void addActionsAfterPasiveAction(KuhnPokerGameState kpState, List<Action> actions, I informationSet) {
		actions.add(createAction(kpState, "b", informationSet, kpState.getCardForActingPlayer().getActionType()));
		actions.add(createAction(kpState, "ch", informationSet, kpState.getCardForActingPlayer().getActionType()));
	}

	protected void addActionsOfNature(KuhnPokerGameState kpState, List<Action> actions, I informationSet) {
		if (isCardAvailableInState("0", kpState))
			actions.add(new KuhnPokerAction("0", informationSet, kpState.getPlayerToMove()));
		if (isCardAvailableInState("1", kpState))
			actions.add(new KuhnPokerAction("1", informationSet, kpState.getPlayerToMove()));
		if (isCardAvailableInState("2", kpState))
			actions.add(new KuhnPokerAction("2", informationSet, kpState.getPlayerToMove()));
	}

	protected PokerAction createAction(KuhnPokerGameState state, String action, I informationSet) {
		return new KuhnPokerAction(action, informationSet, state.getPlayerToMove());
	}

	protected PokerAction createAction(KuhnPokerGameState state, String action, I informationSet, String card) {
		return new KuhnPokerAction(action, informationSet, state.getPlayerToMove(), card);
	}

	protected boolean isCardAvailableInState(String card, KuhnPokerGameState state) {
		return state.getPlayerCards()[0] == null || !card.equals(state.getPlayerCards()[0].getActionType());
	}
}
