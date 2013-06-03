package cz.agents.gtlibrary.domain.goofspiel;

import java.util.LinkedList;
import java.util.List;

import cz.agents.gtlibrary.iinodes.ExpanderImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

public class GoofSpielExpander<I extends InformationSet> extends ExpanderImpl<I> {

	private static final long serialVersionUID = -2513008286051108758L;

	public GoofSpielExpander(AlgorithmConfig<I> algConfig) {
		super(algConfig);
	}

	@Override
	public List<Action> getActions(GameState gameState) {
		GoofSpielGameState gsState = (GoofSpielGameState) gameState;
		List<Action> actions = new LinkedList<Action>();

		if(gsState.isPlayerToMoveNature()) {
			if(GSGameInfo.useFixedNatureSequence) {
				actions.add(gsState.getNatureSequence().getFirst());
			} else {
				addCardsForPlayerToMove(gsState, actions);
			}
			return actions;
		}
		addCardsForPlayerToMove(gsState, actions);
//		Collections.shuffle(actions, new Random(GSGameInfo.seed));
		return actions;
	}

	public void addCardsForPlayerToMove(GoofSpielGameState gsState, List<Action> actions) {
		for (Integer actionValue : gsState.getCardsForPlayerToMove()) {
			GoofSpielAction action = new GoofSpielAction(actionValue, gsState.getPlayerToMove(), getAlgorithmConfig().getInformationSetFor(gsState));
			
			actions.add(action);
		}
	}
}
