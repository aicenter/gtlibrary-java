package cz.agents.gtlibrary.nfg.simalphabeta.doubleoracle;

import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.nfg.ActionPureStrategy;
import cz.agents.gtlibrary.nfg.PlayerStrategySet;
import cz.agents.gtlibrary.nfg.core.ZeroSumGameNESolverImpl;
import cz.agents.gtlibrary.nfg.simalphabeta.Data;
import cz.agents.gtlibrary.nfg.simalphabeta.Killer;
import cz.agents.gtlibrary.nfg.simalphabeta.cache.DOCache;
import cz.agents.gtlibrary.nfg.simalphabeta.cache.NullDOCache;
import cz.agents.gtlibrary.nfg.simalphabeta.oracle.SimOracle;
import cz.agents.gtlibrary.nfg.simalphabeta.stats.Stats;
import cz.agents.gtlibrary.nfg.simalphabeta.utility.SimUtility;

public class FullLP extends DoubleOracle {

    private SimOracle p1Oracle;
    private SimOracle p2Oracle;
    private boolean isRoot;
    private DOCache cache;

    public FullLP(GameState rootState, Data data, SimUtility utility) {
        this(rootState, data, utility, false);
    }

    public FullLP(GameState rootState, Data data, SimUtility utility, boolean isRoot) {
        super(rootState, data);
        this.p1Oracle = data.getP1Oracle(rootState, utility, null);
        this.p2Oracle = data.getP2Oracle(rootState, utility, null);
        this.isRoot = isRoot;
        this.cache = data.getCache();
        coreSolver = new ZeroSumGameNESolverImpl<ActionPureStrategy, ActionPureStrategy>(utility);
    }

    @Override
    public void generate() {
        if(Killer.kill)
            return;
        PlayerStrategySet<ActionPureStrategy> p1StrategySet = new PlayerStrategySet<ActionPureStrategy>(p1Oracle.getActions());
        PlayerStrategySet<ActionPureStrategy> p2StrategySet = new PlayerStrategySet<ActionPureStrategy>(p2Oracle.getActions());

        Stats.getInstance().addToP1StrategyCount(p1StrategySet.size());
        Stats.getInstance().addToP2StrategyCount(p2StrategySet.size());
        coreSolver.addPlayerTwoStrategies(p2StrategySet);
        coreSolver.addPlayerOneStrategies(p1StrategySet);
        if(Killer.kill)
            return;
        coreSolver.computeNashEquilibrium();
        if(Killer.kill)
            return;
        int NEsize1 = Stats.getInstance().addToP1NESize(coreSolver.getPlayerOneStrategy());
        int NEsize2 =Stats.getInstance().addToP2NESize(coreSolver.getPlayerTwoStrategy());

//        if (!rootState.isPlayerToMoveNature()) {
//            int depth = Math.min(rootState.getHistory().getSequenceOf(rootState.getAllPlayers()[0]).size(), rootState.getHistory().getSequenceOf(rootState.getAllPlayers()[1]).size());
//            Stats.getInstance().leavingNode(depth, NEsize1, p1StrategySet.size());
//        }
    }

    @Override
    public double getGameValue() {
        return coreSolver.getGameValue();
    }

    @Override
    public DOCache getCache() {
        return cache;
    }
}
