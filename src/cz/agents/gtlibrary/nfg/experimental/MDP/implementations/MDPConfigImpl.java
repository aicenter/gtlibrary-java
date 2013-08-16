package cz.agents.gtlibrary.nfg.experimental.MDP.implementations;

import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.nfg.experimental.MDP.interfaces.MDPAction;
import cz.agents.gtlibrary.nfg.experimental.MDP.interfaces.MDPConfig;
import cz.agents.gtlibrary.nfg.experimental.MDP.interfaces.MDPState;

import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: bosansky
 * Date: 6/27/13
 * Time: 4:12 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class MDPConfigImpl implements MDPConfig {

    protected List<Player> allPlayers;
    public abstract double getUtility(MDPStateActionMarginal firstPlayerAction, MDPStateActionMarginal secondPlayerAction);

    public Player getOtherPlayer(Player player) {
        return allPlayers.get(0).equals(player) ? allPlayers.get(1) : allPlayers.get(0);
    }

    public static double getEpsilon() {
        return 0.0000001;
    }
}
