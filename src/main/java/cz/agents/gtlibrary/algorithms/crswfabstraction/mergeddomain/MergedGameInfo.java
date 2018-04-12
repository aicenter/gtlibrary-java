package cz.agents.gtlibrary.algorithms.crswfabstraction.mergeddomain;

import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.Player;

import java.util.Map;

public class MergedGameInfo implements GameInfo {

    private Map<ISKey, CompoundISKey> mergedKeys;
    private GameInfo info;

    public MergedGameInfo(GameInfo info, Map<ISKey, CompoundISKey> mergedKeys) {
        this.info = info;
        this.mergedKeys = mergedKeys;
    }

    @Override
    public double getMaxUtility() {
        return info.getMaxUtility();
    }

    @Override
    public Player getFirstPlayerToMove() {
        return info.getFirstPlayerToMove();
    }

    @Override
    public Player getOpponent(Player player) {
        return info.getOpponent(player);
    }

    @Override
    public String getInfo() {
        return "Merged game info of: " + info.getInfo();
    }

    @Override
    public int getMaxDepth() {
        return info.getMaxDepth();
    }

    @Override
    public Player[] getAllPlayers() {
        return info.getAllPlayers();
    }

    @Override
    public double getUtilityStabilizer() {
        return info.getUtilityStabilizer();
    }

    public ISKey getMergedKey(ISKey key) {
        if (!mergedKeys.containsKey(key)) return key;
        return mergedKeys.get(key);
    }
}
