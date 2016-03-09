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


package poker.kuhn;

import static org.junit.Assert.*;


import java.util.LinkedList;

import org.junit.Test;

import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.domain.poker.PokerAction;
import cz.agents.gtlibrary.domain.poker.kuhn.KPGameInfo;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerAction;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerGameState;
import cz.agents.gtlibrary.interfaces.Action;

public class KPGameStateTest {
	
	@Test
	public void testAfterDifferentCardsAndBet() {
		KuhnPokerGameState state = new KuhnPokerGameState();
		LinkedList<Action> sequence = new LinkedList<Action>();
		Action natAction1 = new KuhnPokerAction("0", null, KPGameInfo.NATURE);
		Action natAction2 = new KuhnPokerAction("1", null, KPGameInfo.NATURE);
		

		natAction1.perform(state);
		natAction2.perform(state);
		
		Action fpAction = new KuhnPokerAction("b", new MCTSInformationSet(state), KPGameInfo.FIRST_PLAYER);
		fpAction.perform(state);

		sequence.add(fpAction);
		
		assertEquals(false, state.isPlayerToMoveNature());
		assertEquals(false, state.isGameEnd());
		assertEquals(2 * KPGameInfo.ANTE + KPGameInfo.BET, state.getPot());
		assertEquals(1, state.getRound());
		assertEquals(KPGameInfo.ANTE, state.getGainForFirstPlayer());
		assertEquals(KPGameInfo.SECOND_PLAYER, state.getPlayerToMove());
		assertEquals(sequence, state.getSequenceForAllPlayers());
		assertArrayEquals(new PokerAction[] { (PokerAction) natAction1, (PokerAction) natAction2 }, state.getPlayerCards());
		assertArrayEquals(new double[] { 0 }, state.getUtilities(), 0.000001);
	}

	@Test
	public void testAfterDifferentCardsAndCheck() {
		KuhnPokerGameState state = new KuhnPokerGameState();
		LinkedList<Action> sequence = new LinkedList<Action>();
		Action natAction1 = new KuhnPokerAction("0", null, KPGameInfo.NATURE);
		Action natAction2 = new KuhnPokerAction("1", null, KPGameInfo.NATURE);
		
		natAction1.perform(state);
		natAction2.perform(state);
		
		Action fpAction = new KuhnPokerAction("ch", new MCTSInformationSet(state), KPGameInfo.FIRST_PLAYER);

		fpAction.perform(state);

		sequence.add(fpAction);
		
		assertEquals(false, state.isPlayerToMoveNature());
		assertEquals(false, state.isGameEnd());
		assertEquals(2 * KPGameInfo.ANTE, state.getPot());
		assertEquals(1, state.getRound());
		assertEquals(KPGameInfo.ANTE, state.getGainForFirstPlayer());
		assertEquals(KPGameInfo.SECOND_PLAYER, state.getPlayerToMove());
		assertEquals(sequence, state.getSequenceForAllPlayers());
		assertArrayEquals(new PokerAction[] { (PokerAction) natAction1, (PokerAction) natAction2 }, state.getPlayerCards());
		assertArrayEquals(new double[] { 0 }, state.getUtilities(), 0.000001);
	}
	
	@Test
	public void testAfterDifferentCardsAndCheckCheck() {
		KuhnPokerGameState state = new KuhnPokerGameState();
		LinkedList<Action> sequence = new LinkedList<Action>();
		Action natAction1 = new KuhnPokerAction("0", null, KPGameInfo.NATURE);
		Action natAction2 = new KuhnPokerAction("1", null, KPGameInfo.NATURE);

		natAction1.perform(state);
		natAction2.perform(state);
		
		Action fpAction = new KuhnPokerAction("ch", new MCTSInformationSet(state), KPGameInfo.FIRST_PLAYER);

		fpAction.perform(state);
		
		Action spAction = new KuhnPokerAction("ch", new MCTSInformationSet(state), KPGameInfo.SECOND_PLAYER);
		
		spAction.perform(state);

		sequence.add(fpAction);
		sequence.add(spAction);
		
		assertEquals(false, state.isPlayerToMoveNature());
		assertEquals(true, state.isGameEnd());
		assertEquals(2 * KPGameInfo.ANTE, state.getPot());
		assertEquals(2, state.getRound());
		assertEquals(KPGameInfo.ANTE, state.getGainForFirstPlayer());
		assertEquals(KPGameInfo.FIRST_PLAYER, state.getPlayerToMove());
		assertEquals(sequence, state.getSequenceForAllPlayers());
		assertArrayEquals(new PokerAction[] { (PokerAction) natAction1, (PokerAction) natAction2 }, state.getPlayerCards());
		assertArrayEquals(new double[] { -state.getGainForFirstPlayer(), state.getGainForFirstPlayer(), 0 }, state.getUtilities(), 0.000001);
	}
	
	@Test
	public void testAfterDifferentCardsAndBetCall() {
		KuhnPokerGameState state = new KuhnPokerGameState();
		LinkedList<Action> sequence = new LinkedList<Action>();
		Action natAction1 = new KuhnPokerAction("1", null, KPGameInfo.NATURE);
		Action natAction2 = new KuhnPokerAction("0", null, KPGameInfo.NATURE);

		natAction1.perform(state);
		natAction2.perform(state);
		
		Action fpAction = new KuhnPokerAction("b", new MCTSInformationSet(state), KPGameInfo.FIRST_PLAYER);
		
		fpAction.perform(state);
		
		Action spAction = new KuhnPokerAction("c", new MCTSInformationSet(state), KPGameInfo.SECOND_PLAYER);
		
		spAction.perform(state);

		sequence.add(fpAction);
		sequence.add(spAction);
		
		assertEquals(false, state.isPlayerToMoveNature());
		assertEquals(true, state.isGameEnd());
		assertEquals(2 * KPGameInfo.ANTE + 2 * KPGameInfo.BET, state.getPot());
		assertEquals(2, state.getRound());
		assertEquals(KPGameInfo.ANTE + KPGameInfo.BET, state.getGainForFirstPlayer());
		assertEquals(KPGameInfo.FIRST_PLAYER, state.getPlayerToMove());
		assertEquals(sequence, state.getSequenceForAllPlayers());
		assertArrayEquals(new PokerAction[] { (PokerAction) natAction1, (PokerAction) natAction2 }, state.getPlayerCards());
		assertArrayEquals(new double[] { state.getGainForFirstPlayer(), -state.getGainForFirstPlayer(), 0 }, state.getUtilities(), 0.000001);
	}
	
	@Test
	public void testAfterSameCardsAndCheckCheck() {
		KuhnPokerGameState state = new KuhnPokerGameState();
		LinkedList<Action> sequence = new LinkedList<Action>();
		Action natAction1 = new KuhnPokerAction("0", null, KPGameInfo.NATURE);
		Action natAction2 = new KuhnPokerAction("0", null, KPGameInfo.NATURE);

		natAction1.perform(state);
		natAction2.perform(state);
		
		Action fpAction = new KuhnPokerAction("ch", new MCTSInformationSet(state), KPGameInfo.FIRST_PLAYER);
		
		fpAction.perform(state);
		
		Action spAction = new KuhnPokerAction("ch", new MCTSInformationSet(state), KPGameInfo.SECOND_PLAYER);
		
		spAction.perform(state);

		sequence.add(fpAction);
		sequence.add(spAction);
		
		assertEquals(false, state.isPlayerToMoveNature());
		assertEquals(true, state.isGameEnd());
		assertEquals(2 * KPGameInfo.ANTE, state.getPot());
		assertEquals(2, state.getRound());
		assertEquals(KPGameInfo.ANTE, state.getGainForFirstPlayer());
		assertEquals(KPGameInfo.FIRST_PLAYER, state.getPlayerToMove());
		assertEquals(sequence, state.getSequenceForAllPlayers());
		assertArrayEquals(new PokerAction[] { (PokerAction) natAction1, (PokerAction) natAction2 }, state.getPlayerCards());
		assertArrayEquals(new double[] { 0, 0, 0 }, state.getUtilities(), 0.000001);
	}
	
	@Test
	public void testAfterSameCardsAndBetCall() {
		KuhnPokerGameState state = new KuhnPokerGameState();
		LinkedList<Action> sequence = new LinkedList<Action>();
		Action natAction1 = new KuhnPokerAction("0", null, KPGameInfo.NATURE);
		Action natAction2 = new KuhnPokerAction("0", null, KPGameInfo.NATURE);

		natAction1.perform(state);
		natAction2.perform(state);
		
		Action fpAction = new KuhnPokerAction("b", new MCTSInformationSet(state), KPGameInfo.FIRST_PLAYER);
		
		fpAction.perform(state);
		
		Action spAction = new KuhnPokerAction("c", new MCTSInformationSet(state), KPGameInfo.SECOND_PLAYER);
		
		spAction.perform(state);

		sequence.add(fpAction);
		sequence.add(spAction);
		
		assertEquals(false, state.isPlayerToMoveNature());
		assertEquals(true, state.isGameEnd());
		assertEquals(2 * KPGameInfo.ANTE + 2 * KPGameInfo.BET, state.getPot());
		assertEquals(2, state.getRound());
		assertEquals(KPGameInfo.ANTE + KPGameInfo.BET, state.getGainForFirstPlayer());
		assertEquals(KPGameInfo.FIRST_PLAYER, state.getPlayerToMove());
		assertEquals(sequence, state.getSequenceForAllPlayers());
		assertArrayEquals(new PokerAction[] { (PokerAction) natAction1, (PokerAction) natAction2 }, state.getPlayerCards());
		assertArrayEquals(new double[] { 0, 0, 0 }, state.getUtilities(), 0.000001);
	}
	
	@Test
	public void testAfterSameCardsAndBetFold() {
		KuhnPokerGameState state = new KuhnPokerGameState();
		LinkedList<Action> sequence = new LinkedList<Action>();
		Action natAction1 = new KuhnPokerAction("0", null, KPGameInfo.NATURE);
		Action natAction2 = new KuhnPokerAction("0", null, KPGameInfo.NATURE);

		natAction1.perform(state);
		natAction2.perform(state);
		
		Action fpAction = new KuhnPokerAction("b", new MCTSInformationSet(state), KPGameInfo.FIRST_PLAYER);
		
		fpAction.perform(state);
		
		Action spAction = new KuhnPokerAction("f", new MCTSInformationSet(state), KPGameInfo.SECOND_PLAYER);
		
		spAction.perform(state);

		sequence.add(fpAction);
		sequence.add(spAction);
		
		assertEquals(false, state.isPlayerToMoveNature());
		assertEquals(true, state.isGameEnd());
		assertEquals(2 * KPGameInfo.ANTE + KPGameInfo.BET, state.getPot());
		assertEquals(2, state.getRound());
		assertEquals(KPGameInfo.ANTE, state.getGainForFirstPlayer());
		assertEquals(KPGameInfo.FIRST_PLAYER, state.getPlayerToMove());
		assertEquals(sequence, state.getSequenceForAllPlayers());
		assertArrayEquals(new PokerAction[] { (PokerAction) natAction1, (PokerAction) natAction2 }, state.getPlayerCards());
		assertArrayEquals(new double[] { KPGameInfo.ANTE, -KPGameInfo.ANTE, 0 }, state.getUtilities(), 0.000001);
	}
	
	@Test
	public void testAfterSameCardsAndCheckBetFold() {
		KuhnPokerGameState state = new KuhnPokerGameState();
		LinkedList<Action> sequence = new LinkedList<Action>();
		Action natAction1 = new KuhnPokerAction("0", null, KPGameInfo.NATURE);
		Action natAction2 = new KuhnPokerAction("0", null, KPGameInfo.NATURE);

		natAction1.perform(state);
		natAction2.perform(state);
		
		Action fpAction = new KuhnPokerAction("ch", new MCTSInformationSet(state), KPGameInfo.FIRST_PLAYER);
		
		fpAction.perform(state);
		
		Action spAction = new KuhnPokerAction("b", new MCTSInformationSet(state), KPGameInfo.SECOND_PLAYER);
		
		spAction.perform(state);
		
		Action fp1Action = new KuhnPokerAction("f", new MCTSInformationSet(state), KPGameInfo.FIRST_PLAYER);
		
		fp1Action.perform(state);

		sequence.add(fpAction);
		sequence.add(spAction);
		sequence.add(fp1Action);
		
		assertEquals(false, state.isPlayerToMoveNature());
		assertEquals(true, state.isGameEnd());
		assertEquals(2 * KPGameInfo.ANTE + KPGameInfo.BET, state.getPot());
		assertEquals(2, state.getRound());
		assertEquals(KPGameInfo.ANTE + KPGameInfo.BET, state.getGainForFirstPlayer());
		assertEquals(KPGameInfo.SECOND_PLAYER, state.getPlayerToMove());
		assertEquals(sequence, state.getSequenceForAllPlayers());
		assertArrayEquals(new PokerAction[] { (PokerAction) natAction1, (PokerAction) natAction2 }, state.getPlayerCards());
		assertArrayEquals(new double[] { -KPGameInfo.ANTE, KPGameInfo.ANTE, 0 }, state.getUtilities(), 0.000001);
	}
}
