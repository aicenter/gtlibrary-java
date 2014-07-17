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


package iigamestate;

import static org.junit.Assert.*;

import org.junit.Test;

import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.domain.poker.kuhn.KPGameInfo;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerAction;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerGameState;
import cz.agents.gtlibrary.iinodes.HistoryImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.History;
import cz.agents.gtlibrary.interfaces.InformationSet;
import cz.agents.gtlibrary.interfaces.Player;

public class IIGameStateTest {

	@Test
	public void performActionModifyingThisStateAndHistoryTest() {
		GameState state = new KuhnPokerGameState();
		InformationSet set1 = new MCTSInformationSet(state);
		Action nAction1 = new KuhnPokerAction("0", set1, KPGameInfo.NATURE);
		
		state.performActionModifyingThisState(nAction1);
		
		InformationSet set2 = new MCTSInformationSet(state);
		Action nAction2 = new KuhnPokerAction("0", set2, KPGameInfo.NATURE);
		History expectedHistory = new HistoryImpl(new Player[] { KPGameInfo.FIRST_PLAYER, KPGameInfo.SECOND_PLAYER, KPGameInfo.NATURE });

		state.performActionModifyingThisState(nAction2);

		InformationSet set3 = new MCTSInformationSet(state);
		Action firstAction = new KuhnPokerAction("ch", set3, state.getPlayerToMove());

		state.performActionModifyingThisState(firstAction);

		InformationSet set4 = new MCTSInformationSet(state);
		Action secondAction = new KuhnPokerAction("ch", set4, state.getPlayerToMove());

		state.performActionModifyingThisState(secondAction);
		expectedHistory.addActionOf(nAction1, KPGameInfo.NATURE);
		expectedHistory.addActionOf(nAction2, KPGameInfo.NATURE);
		expectedHistory.addActionOf(firstAction, KPGameInfo.FIRST_PLAYER);
		expectedHistory.addActionOf(secondAction, KPGameInfo.SECOND_PLAYER);
		assertEquals(expectedHistory, state.getHistory());
	}

	@Test
	public void performActionAndHistoryTest() {
		GameState state = new KuhnPokerGameState();
		Action nAction1 = new KuhnPokerAction("0", null, KPGameInfo.NATURE);
		Action nAction2 = new KuhnPokerAction("0", null, KPGameInfo.NATURE);
		History expectedHistory = new HistoryImpl(new Player[] { KPGameInfo.FIRST_PLAYER, KPGameInfo.SECOND_PLAYER, KPGameInfo.NATURE });

		state = state.performAction(nAction1);
		state = state.performAction(nAction2);

		Action firstAction = new KuhnPokerAction("ch", new MCTSInformationSet(state), state.getPlayerToMove());

		state = state.performAction(firstAction);

		Action secondAction = new KuhnPokerAction("ch", new MCTSInformationSet(state), state.getPlayerToMove());

		state = state.performAction(secondAction);
		expectedHistory.addActionOf(nAction1, KPGameInfo.NATURE);
		expectedHistory.addActionOf(nAction2, KPGameInfo.NATURE);
		expectedHistory.addActionOf(firstAction, KPGameInfo.FIRST_PLAYER);
		expectedHistory.addActionOf(secondAction, KPGameInfo.SECOND_PLAYER);
		assertEquals(expectedHistory, state.getHistory());
	}

	@Test(expected = IllegalStateException.class)
	public void performActionAndHistoryTestWithWrongISHash() {
		GameState state = new KuhnPokerGameState();
		Action nAction1 = new KuhnPokerAction("0", null, KPGameInfo.NATURE);
		Action nAction2 = new KuhnPokerAction("0", null, KPGameInfo.NATURE);

		state = state.performAction(nAction1);
		state = state.performAction(nAction2);

		Action firstAction = new KuhnPokerAction("ch", new MCTSInformationSet(state), state.getPlayerToMove());

		state = state.performAction(firstAction);

		Action secondAction = new KuhnPokerAction("ch", new MCTSInformationSet(new KuhnPokerGameState()), state.getPlayerToMove());

		state = state.performAction(secondAction);
	}

	@Test(expected = IllegalStateException.class)
	public void performActionModifyingThisStateTestWithWrongISHash() {
		GameState state = new KuhnPokerGameState();
		Action nAction1 = new KuhnPokerAction("0", null, KPGameInfo.NATURE);
		Action nAction2 = new KuhnPokerAction("0", null, KPGameInfo.NATURE);

		state.performActionModifyingThisState(nAction1);
		state.performActionModifyingThisState(nAction2);

		Action firstAction = new KuhnPokerAction("ch", new MCTSInformationSet(state), state.getPlayerToMove());

		state.performActionModifyingThisState(firstAction);

		Action secondAction = new KuhnPokerAction("ch", new MCTSInformationSet(new KuhnPokerGameState()), state.getPlayerToMove());

		state.performActionModifyingThisState(secondAction);
	}

}
