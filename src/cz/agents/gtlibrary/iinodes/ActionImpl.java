package cz.agents.gtlibrary.iinodes;

import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

public abstract class ActionImpl implements Action {
	protected InformationSet informationSet;

	public ActionImpl(InformationSet informationSet) {
		this.informationSet = informationSet;
	}

	@Override
	public abstract void perform(GameState gameState);

	@Override
	public InformationSet getInformationSet() {
		return informationSet;
	}
	
	@Override
	public void setInformationSet(InformationSet informationSet) {
		this.informationSet = informationSet;
	}
	
}
