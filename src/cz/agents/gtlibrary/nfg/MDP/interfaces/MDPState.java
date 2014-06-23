package cz.agents.gtlibrary.nfg.MDP.interfaces;

import cz.agents.gtlibrary.interfaces.Player;

/**
 * Created with IntelliJ IDEA.
 * User: bosansky
 * Date: 6/27/13
 * Time: 1:22 PM
 * To change this template use File | Settings | File Templates.
 */
public interface MDPState {
    public Player getPlayer();
    public MDPState performAction(MDPAction action);
    public MDPState copy();
    public boolean isRoot();
    public boolean isTerminal();
    public int horizon();
}
