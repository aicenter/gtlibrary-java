package cz.agents.gtlibrary.nfg.experimental.domain.transitgame;

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
 * Date: 9/16/13
 * Time: 10:00 AM
 * To change this template use File | Settings | File Templates.
 */
public class TGConfig extends MDPConfigImpl{

//      public static int MAX_TIME_STEP = 10;
//      public static int LENGTH_OF_GRID = 6;
//      public static int WIDTH_OF_GRID = 4;
//
//    public static int MAX_TIME_STEP = 7;
//    public static int LENGTH_OF_GRID = 3;
//    public static int WIDTH_OF_GRID = 3;
//    public static int MAX_TIME_STEP = 18;
//    public static int LENGTH_OF_GRID = 16;
//    public static int WIDTH_OF_GRID = 8;
    public static int MAX_TIME_STEP = 16;
    public static int LENGTH_OF_GRID = 8;
    public static int WIDTH_OF_GRID = 6;

    final protected static int PATROLLERS = 1;
    protected static int[] PATROLLER_BASES;
    public static boolean useUncertainty = true;
    public static double MOVEMENT_UNCERTAINTY = 0.1;
    public static boolean SHUFFLE = false;
    public static int SHUFFLE_ID = 0;

    public TGConfig() {
        allPlayers = new ArrayList<Player>(2);
        allPlayers.add(new PlayerImpl(0));
        allPlayers.add(new PlayerImpl(1));
        PATROLLER_BASES = new int[] {Math.max(LENGTH_OF_GRID/2-1,0)};
    }

    @Override
    public double getUtility(MDPStateActionMarginal firstPlayerAction, MDPStateActionMarginal secondPlayerAction) {
        assert (firstPlayerAction.getPlayer().getId() != secondPlayerAction.getPlayer().getId());
        if (firstPlayerAction.getState().isRoot() || secondPlayerAction.getState().isRoot())
            return 0;

        Double result = 0d;

        TGAction attAction;
        TGAction defAction;
        TGState attState;
        TGState defState;

        if (firstPlayerAction.getPlayer().getId() == 0) {
            attAction = (TGAction)firstPlayerAction.getAction();
            attState = (TGState)firstPlayerAction.getState();
            defAction = (TGAction)secondPlayerAction.getAction();
            defState = (TGState)secondPlayerAction.getState();
        } else {
            defAction = (TGAction)firstPlayerAction.getAction();
            defState = (TGState)firstPlayerAction.getState();
            attAction = (TGAction)secondPlayerAction.getAction();
            attState = (TGState)secondPlayerAction.getState();
        }

        if (defState.getTimeStep() != attState.getTimeStep())
            result = 0d;
        else if ((attState.getCol()[0] == defState.getCol()[0] && attState.getRow()[0] == defState.getRow()[0]) ||
                 (attAction.getTargetCol()[0] == defState.getCol()[0] && attAction.getTargetRow()[0] == defState.getRow()[0] && attState.getCol()[0] == defAction.getTargetCol()[0] && attState.getRow()[0] == defAction.getTargetRow()[0]) ||
                 (attAction.getTargetCol()[0] == defAction.getTargetCol()[0] && attAction.getTargetRow()[0] == defAction.getTargetRow()[0])
                )
            result = -1d;
        else if (attAction.getTargetCol()[0] == TGConfig.LENGTH_OF_GRID - 1) {
            result = 1d;
            result = result - attState.getTimeStep() * 0.01;
        }
        else if (defState.getTimeStep()+1 == getMaxTimeStep() && !((defState.getRow()[0] == 0 && defState.getCol()[0] != TGConfig.PATROLLER_BASES[0]) || (defAction.getTargetCol()[0] == TGConfig.PATROLLER_BASES[0] && defAction.getTargetRow()[0] == 0))) {
            result = 1000d;
        }
        return result;
    }

    @Override
    public double getBestUtilityValue(Player player) {
        if (player.getId() == 0) {
            return 1d;
        } else {
            return -1d;
        }
    }

    @Override
    public MDPState getDomainRootState(Player player) {
        return new TGState(player);
    }

    public static int getMaxTimeStep() {
        return MAX_TIME_STEP;
    }
}
