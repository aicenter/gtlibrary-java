package cz.agents.gtlibrary.nfg;

import cz.agents.gtlibrary.interfaces.Action;

/**
 * Created with IntelliJ IDEA.
 * User: bosansky
 * Date: 5/29/13
 * Time: 11:36 AM
 * To change this template use File | Settings | File Templates.
 */
public class ActionPureStrategy implements PureStrategy {

    private Action action;

    public ActionPureStrategy(Action action) {
        assert (action != null);
        this.action = action;
    }

    @Override
    public String toString() {
        return action.toString();
    }

    public Action getAction() {
        return action;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((action == null) ? 0 : action.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ActionPureStrategy other = (ActionPureStrategy) obj;
		if (action == null) {
			if (other.action != null)
				return false;
		} else if (!action.equals(other.action))
			return false;
		return true;
	}
    
    
}
