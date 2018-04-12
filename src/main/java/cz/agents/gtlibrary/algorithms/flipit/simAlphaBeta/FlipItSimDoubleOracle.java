package cz.agents.gtlibrary.algorithms.flipit.simAlphaBeta;

import cz.agents.gtlibrary.domain.flipit.FlipItGameInfo;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.nfg.ActionPureStrategy;
import cz.agents.gtlibrary.nfg.MixedStrategy;
import cz.agents.gtlibrary.nfg.PlayerStrategySet;
import cz.agents.gtlibrary.nfg.simalphabeta.Data;
import cz.agents.gtlibrary.nfg.simalphabeta.Killer;
import cz.agents.gtlibrary.nfg.simalphabeta.cache.DOCache;
import cz.agents.gtlibrary.nfg.simalphabeta.doubleoracle.SimDoubleOracle;
import cz.agents.gtlibrary.nfg.simalphabeta.stats.Stats;
import cz.agents.gtlibrary.nfg.simalphabeta.utility.SimUtility;
import cz.agents.gtlibrary.utils.Pair;

/**
 * Created by Jakub Cerny on 14/07/2017.
 */
public class FlipItSimDoubleOracle extends SimDoubleOracle {

    public FlipItSimDoubleOracle(SimUtility utility, double alpha, double beta, Data data, GameState state, DOCache cache, boolean isRoot) {
        super(utility, alpha, beta, data, state, cache, isRoot);
    }

    @Override
    public void generate() {
//        System.out.println("T");
        if ((beta - alpha) < 1e-5) {
            gameValue = alpha;
            return;
        }

        PlayerStrategySet<ActionPureStrategy> p1StrategySet = new PlayerStrategySet<ActionPureStrategy>();
        PlayerStrategySet<ActionPureStrategy> p2StrategySet = new PlayerStrategySet<ActionPureStrategy>();
        ActionPureStrategy initialStrategy = p1Oracle.getFirstStrategy();

        p1StrategySet.add(initialStrategy);
        Stats.getInstance().incrementP1StrategyCount();
        coreSolver.addPlayerOneStrategies(p1StrategySet);
        int iters = -1;

        Pair<ActionPureStrategy, Double> p1BestResponse;
        while (true) {
            if(Killer.kill)
                return;
            iters++;
            Pair<ActionPureStrategy, Double> p2BestResponse = p2Oracle.getBestResponse(getP1MixedStrategy(initialStrategy), alpha, beta);
            if(Killer.kill)
                return;
            if (-p2BestResponse.getRight() > alpha)
                alpha = -p2BestResponse.getRight();
            if (p2BestResponse.getLeft() == null) {
                Stats.getInstance().incrementNaNCuts();
                gameValue = Double.NaN;
                return;
            }

            boolean p2BestResponseadded = p2StrategySet.add(p2BestResponse.getLeft());

            assert !p2BestResponse.getRight().isNaN();
            updateCacheValues(p1StrategySet, p2StrategySet);
            if (p2BestResponseadded) {
                updateForP2Response(p2StrategySet);
                assert gameValue == gameValue;
            }
            if(Killer.kill)
                return;
            p1BestResponse = p1Oracle.getBestResponse(getP2MixedStrategy(), alpha, beta);
            if(Killer.kill)
                return;
            assert alpha <= beta + 1e-8;
            assert !p1BestResponse.getRight().isNaN();
            if (p1BestResponse.getRight() < beta)
                beta = p1BestResponse.getRight();
            if (p1BestResponse.getLeft() == null) {
                Stats.getInstance().incrementNaNCuts();
                gameValue = Double.NaN;
                return;
            }

            boolean p1BestResponseadded = p1StrategySet.add(p1BestResponse.getLeft());

            updateCacheValues(p1StrategySet, p2StrategySet);
            if (p1BestResponseadded) {
                updateForP1Response(p1StrategySet);
                assert gameValue == gameValue;
            }

            if (CHECK_STRATEGY_SET_CHANGES) {
                if (!p1BestResponseadded && !p2BestResponseadded) {
                    Stats.getInstance().addToP1NESize(getP1MixedStrategy(null));
                    Stats.getInstance().addToP2NESize(getP2MixedStrategy());
                    break;
                }
            } else if (Math.abs(p2BestResponse.getRight() + gameValue) < EPS && Math.abs(p1BestResponse.getRight() - gameValue) < EPS)
                break;
        }

//        System.out.println("TU");
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

    }
}
