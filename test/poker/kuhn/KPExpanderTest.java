package poker.kuhn;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;


import org.junit.Test;

import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.domain.poker.kuhn.KPGameInfo;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerAction;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerExpander;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerGameState;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;

public class KPExpanderTest {

	@Test
	public void begginingOfGameTest() {
		GameState state = new KuhnPokerGameState();
		Expander<MCTSInformationSet> expander = new KuhnPokerExpander<MCTSInformationSet>(null);
		List<Action> expectedActions = new LinkedList<Action>();
		
		expectedActions.add(new KuhnPokerAction("0", new MCTSInformationSet(state), KPGameInfo.NATURE));
		expectedActions.add(new KuhnPokerAction("1", new MCTSInformationSet(state), KPGameInfo.NATURE));
		expectedActions.add(new KuhnPokerAction("2", new MCTSInformationSet(state), KPGameInfo.NATURE));
		assertEquals(expectedActions, expander.getActions(new MCTSInformationSet(state)));
	}
	
	@Test
	public void afterFirstCard0Test() {
		GameState state = new KuhnPokerGameState();
		Expander<MCTSInformationSet> expander = new KuhnPokerExpander<MCTSInformationSet>(null);
		List<Action> expectedActions = new LinkedList<Action>();
		
		state.performActionModifyingThisState(new KuhnPokerAction("0", new MCTSInformationSet(state), KPGameInfo.NATURE));
		expectedActions.add(new KuhnPokerAction("1", new MCTSInformationSet(state), KPGameInfo.NATURE));
		expectedActions.add(new KuhnPokerAction("2", new MCTSInformationSet(state), KPGameInfo.NATURE));
		assertEquals(expectedActions, expander.getActions(new MCTSInformationSet(state)));
	}
	
	@Test
	public void afterFirstCard1Test() {
		GameState state = new KuhnPokerGameState();
		Expander<MCTSInformationSet> expander = new KuhnPokerExpander<MCTSInformationSet>(null);
		List<Action> expectedActions = new LinkedList<Action>();
		
		state.performActionModifyingThisState(new KuhnPokerAction("1", new MCTSInformationSet(state), KPGameInfo.NATURE));
		expectedActions.add(new KuhnPokerAction("0", new MCTSInformationSet(state), KPGameInfo.NATURE));
		expectedActions.add(new KuhnPokerAction("2", new MCTSInformationSet(state), KPGameInfo.NATURE));
		assertEquals(expectedActions, expander.getActions(new MCTSInformationSet(state)));
	}
	
	@Test
	public void afterFirstCard2Test() {
		GameState state = new KuhnPokerGameState();
		Expander<MCTSInformationSet> expander = new KuhnPokerExpander<MCTSInformationSet>(null);
		List<Action> expectedActions = new LinkedList<Action>();
		
		state.performActionModifyingThisState(new KuhnPokerAction("2", null, KPGameInfo.NATURE));
		expectedActions.add(new KuhnPokerAction("0", new MCTSInformationSet(state), KPGameInfo.NATURE));
		expectedActions.add(new KuhnPokerAction("1", new MCTSInformationSet(state), KPGameInfo.NATURE));
		assertEquals(expectedActions, expander.getActions(new MCTSInformationSet(state)));
	}
	
	@Test
	public void afterCardsTest() {
		GameState state = new KuhnPokerGameState();
		Expander<MCTSInformationSet> expander = new KuhnPokerExpander<MCTSInformationSet>(null);
		List<Action> expectedActions = new LinkedList<Action>();
		
		state.performActionModifyingThisState(new KuhnPokerAction("2", null, KPGameInfo.NATURE));
		state.performActionModifyingThisState(new KuhnPokerAction("0", null, KPGameInfo.NATURE));
		expectedActions.add(new KuhnPokerAction("b", new MCTSInformationSet(state), KPGameInfo.FIRST_PLAYER));
		expectedActions.add(new KuhnPokerAction("ch", new MCTSInformationSet(state), KPGameInfo.FIRST_PLAYER));
		assertEquals(expectedActions, expander.getActions(new MCTSInformationSet(state)));
	}
	
	@Test
	public void afterBetTest() {
		GameState state = new KuhnPokerGameState();
		Expander<MCTSInformationSet> expander = new KuhnPokerExpander<MCTSInformationSet>(null);
		List<Action> expectedActions = new LinkedList<Action>();
		
		state.performActionModifyingThisState(new KuhnPokerAction("2", null, KPGameInfo.NATURE));
		state.performActionModifyingThisState(new KuhnPokerAction("0", null, KPGameInfo.NATURE));
		state.performActionModifyingThisState(new KuhnPokerAction("b", new MCTSInformationSet(state), KPGameInfo.FIRST_PLAYER));
		expectedActions.add(new KuhnPokerAction("c", new MCTSInformationSet(state), KPGameInfo.SECOND_PLAYER));
		expectedActions.add(new KuhnPokerAction("f", new MCTSInformationSet(state), KPGameInfo.SECOND_PLAYER));
		assertEquals(expectedActions, expander.getActions(new MCTSInformationSet(state)));
	}

	@Test
	public void afterCheckTest() {
		GameState state = new KuhnPokerGameState();
		Expander<MCTSInformationSet> expander = new KuhnPokerExpander<MCTSInformationSet>(null);
		List<Action> expectedActions = new LinkedList<Action>();
		
		state.performActionModifyingThisState(new KuhnPokerAction("2", null, KPGameInfo.NATURE));
		state.performActionModifyingThisState(new KuhnPokerAction("0", null, KPGameInfo.NATURE));
		state.performActionModifyingThisState(new KuhnPokerAction("ch", new MCTSInformationSet(state), KPGameInfo.FIRST_PLAYER));
		expectedActions.add(new KuhnPokerAction("b", new MCTSInformationSet(state), KPGameInfo.SECOND_PLAYER));
		expectedActions.add(new KuhnPokerAction("ch", new MCTSInformationSet(state), KPGameInfo.SECOND_PLAYER));
		assertEquals(expectedActions, expander.getActions(new MCTSInformationSet(state)));
	}
	
	@Test
	public void afterCheckBetTest() {
		GameState state = new KuhnPokerGameState();
		Expander<MCTSInformationSet> expander = new KuhnPokerExpander<MCTSInformationSet>(null);
		List<Action> expectedActions = new LinkedList<Action>();
		
		state.performActionModifyingThisState(new KuhnPokerAction("2", null, KPGameInfo.NATURE));
		state.performActionModifyingThisState(new KuhnPokerAction("0", null, KPGameInfo.NATURE));
		state.performActionModifyingThisState(new KuhnPokerAction("ch", new MCTSInformationSet(state), KPGameInfo.FIRST_PLAYER));
		state.performActionModifyingThisState(new KuhnPokerAction("b", new MCTSInformationSet(state), KPGameInfo.SECOND_PLAYER));
		expectedActions.add(new KuhnPokerAction("c", new MCTSInformationSet(state), KPGameInfo.FIRST_PLAYER));
		expectedActions.add(new KuhnPokerAction("f", new MCTSInformationSet(state), KPGameInfo.FIRST_PLAYER));
		assertEquals(expectedActions, expander.getActions(new MCTSInformationSet(state)));
	}
}
