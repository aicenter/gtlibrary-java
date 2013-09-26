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
        return (action == null) ? "" : action.toString();
    }

    public Action getAction() {
        return action;
    }
}