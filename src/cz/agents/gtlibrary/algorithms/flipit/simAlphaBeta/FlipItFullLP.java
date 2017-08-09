package cz.agents.gtlibrary.algorithms.flipit.simAlphaBeta;

import cz.agents.gtlibrary.domain.flipit.FlipItGameInfo;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.nfg.ActionPureStrategy;
import cz.agents.gtlibrary.nfg.MixedStrategy;
import cz.agents.gtlibrary.nfg.PlayerStrategySet;
import cz.agents.gtlibrary.nfg.simalphabeta.Data;
import cz.agents.gtlibrary.nfg.simalphabeta.Killer;
import cz.agents.gtlibrary.nfg.simalphabeta.doubleoracle.FullLP;
import cz.agents.gtlibrary.nfg.simalphabeta.stats.Stats;
import cz.agents.gtlibrary.nfg.simalphabeta.utility.SimUtility;

/**
 * Created by Jakub Cerny on 14/07/2017.
 */
public class FlipItFullLP extends FullLP {


    public FlipItFullLP(GameState rootState, Data data, SimUtility utility) {
        super(rootState, data, utility);
    }

    public FlipItFullLP(GameState rootState, Data data, SimUtility utility, boolean isRoot) {
        super(rootState, data, utility,isRoot);
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

        MixedStrategy<ActionPureStrategy> playerOneStrategy =  coreSolver.getPlayerOneStrategy();
        if (FlipItGameInfo.OUTPUT_STRATEGY) {
            System.out.println(rootState.getSequenceFor(FlipItGameInfo.DEFENDER));
            System.out.println(rootState.getSequenceFor(FlipItGameInfo.ATTACKER));
            for (ActionPureStrategy strategy : playerOneStrategy.getPureStrategies()) {
                if (!strategy.getAction().getInformationSet().getPlayer().equals(FlipItGameInfo.DEFENDER))
                    System.err.println("Inconsistent IS");
                if (playerOneStrategy.getProbability(strategy) > EPS)
                    System.out.println(strategy + " : " + playerOneStrategy.getProbability(strategy));
            }
//            System.out.println();
        }

        int NEsize1 = Stats.getInstance().addToP1NESize(playerOneStrategy);
        int NEsize2 = Stats.getInstance().addToP2NESize(coreSolver.getPlayerTwoStrategy());

//        if (!rootState.isPlayerToMoveNature()) {
//            int depth = Math.min(rootState.getHistory().getSequenceOf(rootState.getAllPlayers()[0]).size(), rootState.getHistory().getSequenceOf(rootState.getAllPlayers()[1]).size());
//            Stats.getInstance().leavingNode(depth, NEsize1, p1StrategySet.size());
//        }
    }

}
