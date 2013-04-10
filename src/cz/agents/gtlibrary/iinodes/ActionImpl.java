package cz.agents.gtlibrary.iinodes;

import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

public abstract class ActionImpl implements Action {
	
	private static final long serialVersionUID = -7380941202647059723L;
	
	protected InformationSet informationSet;

	public ActionImpl(InformationSet informationSet) {
		//		assert (informationSet != null);		
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

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		if (this.hashCode() != obj.hashCode())
			return false;
		if (informationSet == null) {
			if (((ActionImpl) obj).informationSet != null)
				return false;
		} else if (!informationSet.equals(((ActionImpl) obj).informationSet))
			return false;
		return true;
	}

    @Override
    abstract public int hashCode();
}
