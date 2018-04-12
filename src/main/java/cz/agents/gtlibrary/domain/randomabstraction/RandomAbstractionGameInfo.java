package cz.agents.gtlibrary.domain.randomabstraction;

import cz.agents.gtlibrary.iinodes.PlayerImpl;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.Player;

public class RandomAbstractionGameInfo implements GameInfo {

    public static final Player FIRST_PLAYER = new PlayerImpl(0);
    public static final Player SECOND_PLAYER = new PlayerImpl(1);
    public static final Player NATURE = new PlayerImpl(2);

    /**
     * Nature should always be last
     **/
    public static final Player[] ALL_PLAYERS = new Player[]{FIRST_PLAYER, SECOND_PLAYER, NATURE};
    public static double JOIN_PROB = 1;

    private GameInfo wrappedGameInfo;

    public RandomAbstractionGameInfo(GameInfo wrappedGameInfo) {
        this.wrappedGameInfo = wrappedGameInfo;
    }

    @Override
    public double getMaxUtility() {
        return wrappedGameInfo.getMaxUtility();
    }

    @Override
    public Player getFirstPlayerToMove() {
        return wrappedGameInfo.getFirstPlayerToMove();
    }

    @Override
    public Player getOpponent(Player player) {
        return wrappedGameInfo.getOpponent(player);
    }

    @Override
    public String getInfo() {
        return wrappedGameInfo.getInfo();
    }

    @Override
    public int getMaxDepth() {
        return wrappedGameInfo.getMaxDepth();
    }

    @Override
    public Player[] getAllPlayers() {
        return wrappedGameInfo.getAllPlayers();
    }

    @Override
    public double getUtilityStabilizer() {
        return wrappedGameInfo.getUtilityStabilizer();
    }
}
