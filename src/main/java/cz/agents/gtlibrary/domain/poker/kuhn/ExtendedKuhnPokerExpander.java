package cz.agents.gtlibrary.domain.poker.kuhn;

import cz.agents.gtlibrary.domain.poker.PokerAction;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.InformationSet;

import java.util.LinkedList;
import java.util.List;

public class ExtendedKuhnPokerExpander<I extends InformationSet> extends KuhnPokerExpander<I> {

    public ExtendedKuhnPokerExpander(AlgorithmConfig<I> algConfig) {
        super(algConfig);
    }

    protected void addActionsOfRegularPlayer(KuhnPokerGameState kpState, List<Action> actions, I informationSet) {
        LinkedList<PokerAction> history = kpState.getSequenceForAllPlayers();

        if (!kpState.isGameEnd()) {
            if (history.isEmpty() || history.getLast().getActionType().equals("ch")) {
                addActionsAfterPasiveAction(kpState, actions, informationSet);
            } else if (history.getLast().getActionType().equals("b")) {
                addActionsAfterAggressiveAction(kpState, actions, informationSet);
            }else if (history.getLast().getActionType().equals("r")) {
                addActionsAfterRaise(kpState, actions, informationSet);
            } else if (history.getLast().getActionType().equals("c")) {
                addActionsAfterPasiveAction(kpState, actions, informationSet);
            } else if (!history.getLast().getActionType().equals("f")) {
                addActionsAfterPasiveAction(kpState, actions, informationSet);
            }
        }
    }

    private void addActionsAfterRaise(KuhnPokerGameState kpState, List<Action> actions, I informationSet) {
        actions.add(createAction(kpState, "c", informationSet));
        actions.add(createAction(kpState, "f", informationSet));
    }

    protected void addActionsAfterAggressiveAction(KuhnPokerGameState kpState, List<Action> actions, I informationSet) {
        actions.add(createAction(kpState, "c", informationSet));
        actions.add(createAction(kpState, "f", informationSet));
        actions.add(createAction(kpState, "r", informationSet));
    }
}
