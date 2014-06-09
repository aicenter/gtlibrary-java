package cz.agents.gtlibrary.domain.nfptest;

import java.util.ArrayList;
import java.util.List;

import cz.agents.gtlibrary.iinodes.ExpanderImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

public class TestExpander<I extends InformationSet> extends ExpanderImpl<I> {

	private static final long serialVersionUID = 6455853080379013793L;

	public TestExpander(AlgorithmConfig<I> algConfig) {
		super(algConfig);
	}

	@Override
	public List<Action> getActions(GameState gameState) {
		if (gameState.getPlayerToMove().equals(gameState.getAllPlayers()[0]))
			return getActionsForP1(gameState);
		return getActionsForP2(gameState);
	}

	private List<Action> getActionsForP2(GameState gameState) {
		List<Action> actions = new ArrayList<Action>(2);
		
		actions.add(new P2TestAction(getAlgorithmConfig().getInformationSetFor(gameState), "u"));
		actions.add(new P2TestAction(getAlgorithmConfig().getInformationSetFor(gameState), "d"));
		return actions;
	}

	private List<Action> getActionsForP1(GameState gameState) {
		TestGameState tState = (TestGameState) gameState;

		if (tState.getLastActionOfP2() == null)
			return getFirstP1Actions(gameState);
		if (tState.getLastActionOfP2().getActionType().equals("u"))
			return getActionsAfterU(gameState);
		return getActionsAfterD(gameState);
	}

	private List<Action> getActionsAfterD(GameState gameState) {
		List<Action> actions = new ArrayList<Action>(2);

		actions.add(new P1TestAction(getAlgorithmConfig().getInformationSetFor(gameState), "L'"));
		actions.add(new P1TestAction(getAlgorithmConfig().getInformationSetFor(gameState), "R'"));
		return actions;
	}

	private List<Action> getActionsAfterU(GameState gameState) {
		List<Action> actions = new ArrayList<Action>(2);

		actions.add(new P1TestAction(getAlgorithmConfig().getInformationSetFor(gameState), "L"));
		actions.add(new P1TestAction(getAlgorithmConfig().getInformationSetFor(gameState), "R"));
		return actions;
	}

	private List<Action> getFirstP1Actions(GameState gameState) {
		List<Action> actions = new ArrayList<Action>(2);

		actions.add(new P1TestAction(getAlgorithmConfig().getInformationSetFor(gameState), "U"));
		actions.add(new P1TestAction(getAlgorithmConfig().getInformationSetFor(gameState), "D"));
		return actions;
	}

}
