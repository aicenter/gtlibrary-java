package cz.agents.gtlibrary.domain.stochastic;

import java.util.List;

import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;

public interface StochasticExpander {
	public List<Action> getActions(GameState state);
}
