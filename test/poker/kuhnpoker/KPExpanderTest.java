package poker.kuhnpoker;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;

import gametree.domain.poker.kuhn.KPGameInfo;
import gametree.domain.poker.kuhn.KuhnPokerAction;
import gametree.domain.poker.kuhn.KuhnPokerExpander;
import gametree.domain.poker.kuhn.KuhnPokerGameState;
import gametree.interfaces.Action;
import gametree.interfaces.Expander;
import gametree.interfaces.GameState;

import org.junit.Test;

public class KPExpanderTest {

	@Test
	public void begginingOfGameTest() {
		GameState state = new KuhnPokerGameState();
		Expander expander = new KuhnPokerExpander();
		List<Action> expectedActions = new LinkedList<Action>();
		
		expectedActions.add(new KuhnPokerAction("0", 0, KPGameInfo.NATURE));
		expectedActions.add(new KuhnPokerAction("1", 0, KPGameInfo.NATURE));
		expectedActions.add(new KuhnPokerAction("2", 0, KPGameInfo.NATURE));
		assertEquals(expectedActions, expander.getActions(state));
	}
	
	@Test
	public void afterFirstCard0Test() {
		GameState state = new KuhnPokerGameState();
		Expander expander = new KuhnPokerExpander();
		List<Action> expectedActions = new LinkedList<Action>();
		
		state.performActionModifyingThisState(new KuhnPokerAction("0", 0, KPGameInfo.NATURE));
		expectedActions.add(new KuhnPokerAction("1", 0, KPGameInfo.NATURE));
		expectedActions.add(new KuhnPokerAction("2", 0, KPGameInfo.NATURE));
		assertEquals(expectedActions, expander.getActions(state));
	}
	
	@Test
	public void afterFirstCard1Test() {
		GameState state = new KuhnPokerGameState();
		Expander expander = new KuhnPokerExpander();
		List<Action> expectedActions = new LinkedList<Action>();
		
		state.performActionModifyingThisState(new KuhnPokerAction("1", 0, KPGameInfo.NATURE));
		expectedActions.add(new KuhnPokerAction("0", 0, KPGameInfo.NATURE));
		expectedActions.add(new KuhnPokerAction("2", 0, KPGameInfo.NATURE));
		assertEquals(expectedActions, expander.getActions(state));
	}
	
	@Test
	public void afterFirstCard2Test() {
		GameState state = new KuhnPokerGameState();
		Expander expander = new KuhnPokerExpander();
		List<Action> expectedActions = new LinkedList<Action>();
		
		state.performActionModifyingThisState(new KuhnPokerAction("2", 0, KPGameInfo.NATURE));
		expectedActions.add(new KuhnPokerAction("0", 0, KPGameInfo.NATURE));
		expectedActions.add(new KuhnPokerAction("1", 0, KPGameInfo.NATURE));
		assertEquals(expectedActions, expander.getActions(state));
	}
	
	@Test
	public void afterCardsTest() {
		GameState state = new KuhnPokerGameState();
		Expander expander = new KuhnPokerExpander();
		List<Action> expectedActions = new LinkedList<Action>();
		
		state.performActionModifyingThisState(new KuhnPokerAction("2", 0, KPGameInfo.NATURE));
		state.performActionModifyingThisState(new KuhnPokerAction("0", 0, KPGameInfo.NATURE));
		expectedActions.add(new KuhnPokerAction("b", state.getISEquivalenceFor(KPGameInfo.FIRST_PLAYER), KPGameInfo.FIRST_PLAYER));
		expectedActions.add(new KuhnPokerAction("ch", state.getISEquivalenceFor(KPGameInfo.FIRST_PLAYER), KPGameInfo.FIRST_PLAYER));
		assertEquals(expectedActions, expander.getActions(state));
	}
	
	@Test
	public void afterBetTest() {
		GameState state = new KuhnPokerGameState();
		Expander expander = new KuhnPokerExpander();
		List<Action> expectedActions = new LinkedList<Action>();
		
		state.performActionModifyingThisState(new KuhnPokerAction("2", 0, KPGameInfo.NATURE));
		state.performActionModifyingThisState(new KuhnPokerAction("0", 0, KPGameInfo.NATURE));
		state.performActionModifyingThisState(new KuhnPokerAction("b", state.getISEquivalenceFor(KPGameInfo.FIRST_PLAYER), KPGameInfo.FIRST_PLAYER));
		expectedActions.add(new KuhnPokerAction("c", state.getISEquivalenceFor(KPGameInfo.SECOND_PLAYER), KPGameInfo.SECOND_PLAYER));
		expectedActions.add(new KuhnPokerAction("f", state.getISEquivalenceFor(KPGameInfo.SECOND_PLAYER), KPGameInfo.SECOND_PLAYER));
		assertEquals(expectedActions, expander.getActions(state));
	}

	@Test
	public void afterCheckTest() {
		GameState state = new KuhnPokerGameState();
		Expander expander = new KuhnPokerExpander();
		List<Action> expectedActions = new LinkedList<Action>();
		
		state.performActionModifyingThisState(new KuhnPokerAction("2", 0, KPGameInfo.NATURE));
		state.performActionModifyingThisState(new KuhnPokerAction("0", 0, KPGameInfo.NATURE));
		state.performActionModifyingThisState(new KuhnPokerAction("ch", state.getISEquivalenceFor(KPGameInfo.FIRST_PLAYER), KPGameInfo.FIRST_PLAYER));
		expectedActions.add(new KuhnPokerAction("b", state.getISEquivalenceFor(KPGameInfo.SECOND_PLAYER), KPGameInfo.SECOND_PLAYER));
		expectedActions.add(new KuhnPokerAction("ch", state.getISEquivalenceFor(KPGameInfo.SECOND_PLAYER), KPGameInfo.SECOND_PLAYER));
		assertEquals(expectedActions, expander.getActions(state));
	}
	
	@Test
	public void afterCheckBetTest() {
		GameState state = new KuhnPokerGameState();
		Expander expander = new KuhnPokerExpander();
		List<Action> expectedActions = new LinkedList<Action>();
		
		state.performActionModifyingThisState(new KuhnPokerAction("2", 0, KPGameInfo.NATURE));
		state.performActionModifyingThisState(new KuhnPokerAction("0", 0, KPGameInfo.NATURE));
		state.performActionModifyingThisState(new KuhnPokerAction("ch", state.getISEquivalenceFor(KPGameInfo.FIRST_PLAYER), KPGameInfo.FIRST_PLAYER));
		state.performActionModifyingThisState(new KuhnPokerAction("b", state.getISEquivalenceFor(KPGameInfo.SECOND_PLAYER), KPGameInfo.SECOND_PLAYER));
		expectedActions.add(new KuhnPokerAction("c", state.getISEquivalenceFor(KPGameInfo.FIRST_PLAYER), KPGameInfo.FIRST_PLAYER));
		expectedActions.add(new KuhnPokerAction("f", state.getISEquivalenceFor(KPGameInfo.FIRST_PLAYER), KPGameInfo.FIRST_PLAYER));
		assertEquals(expectedActions, expander.getActions(state));
	}
}
