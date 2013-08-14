package cz.agents.gtlibrary.nfg.experimental.domain.bpg;

import cz.agents.gtlibrary.iinodes.PlayerImpl;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.nfg.experimental.MDP.implementations.MDPConfigImpl;
import cz.agents.gtlibrary.nfg.experimental.MDP.implementations.MDPStateActionMarginal;
import cz.agents.gtlibrary.nfg.experimental.MDP.interfaces.MDPState;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: bosansky
 * Date: 7/25/13
 * Time: 3:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class BPConfig extends MDPConfigImpl {

    final private static int MAX_TIME_STEP = 4;
    final private static double FLAG_PROB = 0.1;

    public BPConfig() {
        allPlayers = new ArrayList<Player>(2);
        allPlayers.add(new BPAttacker());
        allPlayers.add(new BPDefender());
    }

    @Override
    public List<Player> getAllPlayers() {
        return allPlayers;
    }

    @Override
    public double getUtility(MDPStateActionMarginal firstPlayerAction, MDPStateActionMarginal secondPlayerAction) {
        assert (firstPlayerAction.getPlayer().getId() != secondPlayerAction.getPlayer().getId());
        if (firstPlayerAction.getState().isRoot() || secondPlayerAction.getState().isRoot())
            return 0;
        BPAction attAction;
        BPAction defAction;
        BPState attState;
        BPState defState;

        if (firstPlayerAction.getPlayer().getId() == 0) {
            attAction = (BPAction)firstPlayerAction.getAction();
            attState = (BPState)firstPlayerAction.getState();
            defAction = (BPAction)secondPlayerAction.getAction();
            defState = (BPState)secondPlayerAction.getState();
        } else {
            defAction = (BPAction)firstPlayerAction.getAction();
            defState = (BPState)firstPlayerAction.getState();
            attAction = (BPAction)secondPlayerAction.getAction();
            attState = (BPState)secondPlayerAction.getState();
        }

        if (attAction.getMoves()[0].getToNode() == 16)
            return 1;
        if ((defAction.getMoves()[0].getToNode() == attAction.getMoves()[0].getToNode() && defState.getTimeStep() == attState.getTimeStep()) ||
            (defAction.getMoves()[1].getToNode() == attAction.getMoves()[0].getToNode() && defState.getTimeStep() == attState.getTimeStep()))
            return -1;
        return 0;
    }

    public class BPAttacker extends PlayerImpl {
        public BPAttacker() {
            super(0);
        }
    }

    public class BPDefender extends PlayerImpl {
        public BPDefender() {
            super(1);
        }
    }

    public static int getMaxTimeStep() {
        return MAX_TIME_STEP;
    }

    @Override
    public MDPState getDomainRootState(Player player) {
        return new BPState(player);
    }

    public static double getFLAG_PROB() {
        return FLAG_PROB;
    }
}
