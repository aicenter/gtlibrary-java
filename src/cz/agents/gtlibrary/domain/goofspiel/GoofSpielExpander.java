package cz.agents.gtlibrary.domain.goofspiel;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import cz.agents.gtlibrary.iinodes.ExpanderImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

public class GoofSpielExpander<I extends InformationSet> extends ExpanderImpl<I> {

	public GoofSpielExpander(AlgorithmConfig<I> algConfig) {
		super(algConfig);
	}

	@Override
	public List<Action> getActions(GameState gameState) {
		GoofSpielGameState gsState = (GoofSpielGameState) gameState;
		List<Action> actions = new LinkedList<Action>();

		if(gsState.isPlayerToMoveNature()) {
			actions.add(gsState.getNatureSequence().getFirst());
			return actions;
		}
		for (Integer actionValue : gsState.getCardsForPlayerToMove()) {
			GoofSpielAction action = new GoofSpielAction(actionValue, gsState.getPlayerToMove(), getAlgorithmConfig().getInformationSetFor(gameState));
			
			actions.add(action);
		}
		Collections.shuffle(actions, new Random(GSGameInfo.seed));
		return actions;
	}
}
