package poker.kuhnpoker;

import static org.junit.Assert.*;

import gametree.domain.poker.PokerAction;
import gametree.domain.poker.kuhn.KPGameInfo;
import gametree.domain.poker.kuhn.KuhnPokerAction;
import gametree.domain.poker.kuhn.KuhnPokerGameState;
import gametree.interfaces.Action;

import java.util.LinkedList;

import org.junit.Test;

public class GameStateTest {
	
	@Test
	public void testAfterDifferentCardsAndBet() {
		KuhnPokerGameState state = new KuhnPokerGameState();
		LinkedList<Action> sequence = new LinkedList<Action>();
		Action natAction1 = new KuhnPokerAction("0", 0, KPGameInfo.NATURE);
		Action natAction2 = new KuhnPokerAction("1", 0, KPGameInfo.NATURE);
		Action fpAction = new KuhnPokerAction("b", 0, KPGameInfo.FIRST_PLAYER);

		natAction1.perform(state);
		natAction2.perform(state);
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
		Action natAction1 = new KuhnPokerAction("0", 0, KPGameInfo.NATURE);
		Action natAction2 = new KuhnPokerAction("1", 0, KPGameInfo.NATURE);
		Action fpAction = new KuhnPokerAction("ch", 0, KPGameInfo.FIRST_PLAYER);

		natAction1.perform(state);
		natAction2.perform(state);
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
		Action natAction1 = new KuhnPokerAction("0", 0, KPGameInfo.NATURE);
		Action natAction2 = new KuhnPokerAction("1", 0, KPGameInfo.NATURE);
		Action fpAction = new KuhnPokerAction("ch", 0, KPGameInfo.FIRST_PLAYER);
		Action spAction = new KuhnPokerAction("ch", 0, KPGameInfo.SECOND_PLAYER);

		natAction1.perform(state);
		natAction2.perform(state);
		fpAction.perform(state);
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
		Action natAction1 = new KuhnPokerAction("1", 0, KPGameInfo.NATURE);
		Action natAction2 = new KuhnPokerAction("0", 0, KPGameInfo.NATURE);
		Action fpAction = new KuhnPokerAction("b", 0, KPGameInfo.FIRST_PLAYER);
		Action spAction = new KuhnPokerAction("c", 0, KPGameInfo.SECOND_PLAYER);

		natAction1.perform(state);
		natAction2.perform(state);
		fpAction.perform(state);
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
		Action natAction1 = new KuhnPokerAction("0", 0, KPGameInfo.NATURE);
		Action natAction2 = new KuhnPokerAction("0", 0, KPGameInfo.NATURE);
		Action fpAction = new KuhnPokerAction("ch", 0, KPGameInfo.FIRST_PLAYER);
		Action spAction = new KuhnPokerAction("ch", 0, KPGameInfo.SECOND_PLAYER);

		natAction1.perform(state);
		natAction2.perform(state);
		fpAction.perform(state);
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
		Action natAction1 = new KuhnPokerAction("0", 0, KPGameInfo.NATURE);
		Action natAction2 = new KuhnPokerAction("0", 0, KPGameInfo.NATURE);
		Action fpAction = new KuhnPokerAction("b", 0, KPGameInfo.FIRST_PLAYER);
		Action spAction = new KuhnPokerAction("c", 0, KPGameInfo.SECOND_PLAYER);

		natAction1.perform(state);
		natAction2.perform(state);
		fpAction.perform(state);
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
		Action natAction1 = new KuhnPokerAction("0", 0, KPGameInfo.NATURE);
		Action natAction2 = new KuhnPokerAction("0", 0, KPGameInfo.NATURE);
		Action fpAction = new KuhnPokerAction("b", 0, KPGameInfo.FIRST_PLAYER);
		Action spAction = new KuhnPokerAction("f", 0, KPGameInfo.SECOND_PLAYER);

		natAction1.perform(state);
		natAction2.perform(state);
		fpAction.perform(state);
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
		Action natAction1 = new KuhnPokerAction("0", 0, KPGameInfo.NATURE);
		Action natAction2 = new KuhnPokerAction("0", 0, KPGameInfo.NATURE);
		Action fpAction = new KuhnPokerAction("ch", 0, KPGameInfo.FIRST_PLAYER);
		Action spAction = new KuhnPokerAction("b", 0, KPGameInfo.SECOND_PLAYER);
		Action fp1Action = new KuhnPokerAction("f", 0, KPGameInfo.FIRST_PLAYER);

		natAction1.perform(state);
		natAction2.perform(state);
		fpAction.perform(state);
		spAction.perform(state);
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
