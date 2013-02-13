package cz.agents.gtlibrary.domain.poker.kuhn;

import java.util.LinkedList;
import java.util.List;

import cz.agents.gtlibrary.domain.poker.PokerAction;
import cz.agents.gtlibrary.iinodes.ExpanderImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;


public class KuhnPokerExpander<I extends InformationSet> extends ExpanderImpl<I>{

	public KuhnPokerExpander(AlgorithmConfig<I> algConfig) {
		super(algConfig);
	}

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
		throw new UnsupportedOperationException("Not implemented yet.");
	}
	
	private void addActionsOfRegularPlayer(KuhnPokerGameState kpState, List<Action> actions) {
		LinkedList<PokerAction> history = kpState.getSequenceForAllPlayers();

		if (!kpState.isGameEnd()) {
			if (history.isEmpty() || history.getLast().getActionType().equals("ch")) {
				addActionsAfterPasiveAction(kpState, actions);
			} else if (history.getLast().getActionType().equals("b")) {
				addActionsAfterAggressiveAction(kpState, actions);
			} else if (history.getLast().getActionType().equals("c")) {
				addActionsAfterPasiveAction(kpState, actions);
			} else if (!history.getLast().getActionType().equals("f")) {
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
		I infoSet = getAlgorithmConfig().getInformationSetFor(kpState);
		if (isCardAvailableInState("0", kpState))
			actions.add(new KuhnPokerAction("0", infoSet, kpState.getPlayerToMove()));
		if (isCardAvailableInState("1", kpState))
			actions.add(new KuhnPokerAction("1", infoSet, kpState.getPlayerToMove()));
		if (isCardAvailableInState("2", kpState))
			actions.add(new KuhnPokerAction("2", infoSet, kpState.getPlayerToMove()));
	}

	private PokerAction createAction(KuhnPokerGameState state, String action) {
		return new KuhnPokerAction(action, getAlgorithmConfig().getInformationSetFor(state), state.getPlayerToMove());
	}

	private boolean isCardAvailableInState(String card, KuhnPokerGameState state) {
		return state.getPlayerCards()[0] == null || !card.equals(state.getPlayerCards()[0].getActionType());
	}
}
