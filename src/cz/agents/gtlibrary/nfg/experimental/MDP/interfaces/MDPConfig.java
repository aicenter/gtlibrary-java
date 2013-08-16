package cz.agents.gtlibrary.nfg.experimental.MDP.interfaces;

import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.nfg.experimental.MDP.implementations.MDPStateActionMarginal;
import cz.agents.gtlibrary.nfg.experimental.MDP.implementations.MDPStrategy;

import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: bosansky
 * Date: 7/24/13
 * Time: 1:17 PM
 * To change this template use File | Settings | File Templates.
 */
public interface MDPConfig {
    public List<Player> getAllPlayers();
    public double getUtility(MDPStateActionMarginal firstPlayerAction, MDPStateActionMarginal secondPlayerAction);
    public double getUtility(MDPStateActionMarginal firstPlayerAction, MDPStrategy secondPlayerStrategy);
    public Player getOtherPlayer(Player player);
    public MDPState getDomainRootState(Player player);
}
