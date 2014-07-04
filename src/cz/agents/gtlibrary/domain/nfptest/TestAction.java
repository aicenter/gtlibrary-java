package cz.agents.gtlibrary.domain.nfptest;

import cz.agents.gtlibrary.iinodes.ActionImpl;
import cz.agents.gtlibrary.interfaces.InformationSet;

public abstract class TestAction extends ActionImpl {

	private static final long serialVersionUID = 1837357664132787551L;

	protected String actionType;

	public TestAction(InformationSet informationSet, String actionType) {
		super(informationSet);
		this.actionType = actionType;
	}

	public String getActionType() {
		return actionType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((actionType == null) ? 0 : actionType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		TestAction other = (TestAction) obj;
		if (actionType == null) {
			if (other.actionType != null)
				return false;
		} else if (!actionType.equals(other.actionType))
			return false;
		return true;
	}
}
