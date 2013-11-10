package cz.agents.gtlibrary.domain.stochastic;

import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

public abstract class StochasticAction implements Action {

	private static final long serialVersionUID = -7722238745635290247L;

	@Override
	public abstract void perform(GameState gameState);

	@Override
	public InformationSet getInformationSet() {
		return null;
	}

	@Override
	public void setInformationSet(InformationSet informationSet) {
	}

	public abstract int hashCode();

	public abstract boolean equals(Object object);
	
}
