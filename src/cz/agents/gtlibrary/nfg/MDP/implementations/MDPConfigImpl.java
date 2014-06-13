package cz.agents.gtlibrary.nfg.MDP.implementations;

import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.nfg.MDP.interfaces.MDPAction;
import cz.agents.gtlibrary.nfg.MDP.interfaces.MDPConfig;
import cz.agents.gtlibrary.nfg.MDP.interfaces.MDPState;

import java.util.*;

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

    public List<Player> getAllPlayers() {
        return allPlayers;
    }

    public static double getEpsilon() {
        return 0.000001;
    }

    public abstract double getBestUtilityValue(Player player);
}
