package cz.agents.gtlibrary.nfg;

import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;

/**
 * Created with IntelliJ IDEA.
 * User: bosansky
 * Date: 5/29/13
 * Time: 11:38 AM
 * To change this template use File | Settings | File Templates.
 */
public class NFGActionUtilityComputer<T extends ActionPureStrategy, U extends ActionPureStrategy> extends Utility<T, U> {

	private GameState root;

	public NFGActionUtilityComputer(GameState root) {
		this.root = root;
	}

	@Override
	public double getUtility(T s1, U s2) {
		if (s1 == null || s2 == null || s1.getAction().getInformationSet() == null || s2.getAction().getInformationSet() == null)
			throw new IllegalArgumentException();

		GameState newState = performProperAction(root, s1.getAction(), s2.getAction());

		newState = performProperAction(newState, s1.getAction(), s2.getAction());
		assert (newState.isGameEnd());
		return newState.getUtilities()[0];
	}

	private GameState performProperAction(GameState state, Action action1, Action action2) {
		if (action1.getInformationSet().getPlayer().equals(state.getPlayerToMove()))
			return state.performAction(action1);
		return state.performAction(action2);
	}
}
