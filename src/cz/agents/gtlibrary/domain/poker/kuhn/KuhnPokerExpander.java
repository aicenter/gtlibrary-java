package cz.agents.gtlibrary.domain.poker.kuhn;

import java.util.LinkedList;
import java.util.List;

import cz.agents.gtlibrary.domain.poker.PokerAction;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;


public class KuhnPokerExpander implements Expander {

	@Override
	public List<Action> getActions(GameState gameState) {
		KuhnPokerGameState kpState = (KuhnPokerGameState) gameState;
		List<Action> actions = new LinkedList<Action>();

		if (kpState.getRound() == 0) {			
			addActionsOfNature(kpState, actions);
			return actions;
		}
		addActionsOfRegularPlayer(kpState, actions);
		return actions;
	}

	@Override
	public List<Action> getActions(InformationSet informationSet) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private void addActionsOfRegularPlayer(KuhnPokerGameState kpState, List<Action> actions) {
		LinkedList<PokerAction> history = kpState.getSequenceForAllPlayers();

		if (!kpState.isGameEnd()) {
			if (history.isEmpty() || history.getLast().getAction().equals("ch")) {
				addActionsAfterPasiveAction(kpState, actions);
			} else if (history.getLast().getAction().equals("b")) {
				addActionsAfterAggressiveAction(kpState, actions);
			} else if (history.getLast().getAction().equals("c")) {
				addActionsAfterPasiveAction(kpState, actions);
			} else if (!history.getLast().getAction().equals("f")) {
				addActionsAfterPasiveAction(kpState, actions);
			}
		}
	}

	private void addActionsAfterAggressiveAction(KuhnPokerGameState kpState, List<Action> actions) {
		actions.add(createAction(kpState, "c"));
		actions.add(createAction(kpState, "f"));
	}

	private void addActionsAfterPasiveAction(KuhnPokerGameState kpState, List<Action> actions) {
		actions.add(createAction(kpState, "b"));
		actions.add(createAction(kpState, "ch"));
	}

	private void addActionsOfNature(KuhnPokerGameState kpState, List<Action> actions) {
		if (isCardAvailableInState("0", kpState))
			actions.add(new KuhnPokerAction("0", 0, kpState.getPlayerToMove()));
		if (isCardAvailableInState("1", kpState))
			actions.add(new KuhnPokerAction("1", 0, kpState.getPlayerToMove()));
		if (isCardAvailableInState("2", kpState))
			actions.add(new KuhnPokerAction("2", 0, kpState.getPlayerToMove()));
	}

	private PokerAction createAction(KuhnPokerGameState state, String action) {
		return new KuhnPokerAction(action, state.getISEquivalenceFor(state.getPlayerToMove()), state.getPlayerToMove());
	}

	private boolean isCardAvailableInState(String card, KuhnPokerGameState state) {
		return state.getPlayerCards()[0] == null || !card.equals(state.getPlayerCards()[0].getAction());
	}
}
