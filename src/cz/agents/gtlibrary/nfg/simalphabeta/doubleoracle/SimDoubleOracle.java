package cz.agents.gtlibrary.nfg.simalphabeta.doubleoracle;

import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.nfg.ActionPureStrategy;
import cz.agents.gtlibrary.nfg.MixedStrategy;
import cz.agents.gtlibrary.nfg.PlayerStrategySet;
import cz.agents.gtlibrary.nfg.core.ZeroSumGameNESolverImpl;
import cz.agents.gtlibrary.nfg.simalphabeta.Data;
import cz.agents.gtlibrary.nfg.simalphabeta.cache.DOCache;
import cz.agents.gtlibrary.nfg.simalphabeta.oracle.SimOracle;
import cz.agents.gtlibrary.nfg.simalphabeta.stats.Stats;
import cz.agents.gtlibrary.nfg.simalphabeta.utility.SimUtility;
import cz.agents.gtlibrary.utils.Pair;

public class SimDoubleOracle extends DoubleOracle {

    protected final boolean CHECK_STRATEGY_SET_CHANGES = true;

    protected SimOracle p1Oracle;
    protected SimOracle p2Oracle;

    protected double alpha;
    protected double beta;
    protected double gameValue;
    //	protected GameState state;
    protected Data data;
    protected SimUtility p1Utility;
    protected DOCache cache = null;

    final protected boolean isRoot;

    public SimDoubleOracle(SimUtility utility, double alpha, double beta, Data data, GameState state, DOCache cache) {
        super(state, data);
        this.p1Oracle = data.getP1Oracle(state, utility, cache);
        this.p2Oracle = data.getP2Oracle(state, utility, cache);
        this.alpha = alpha;
        this.beta = beta;
//		this.state = state;
        this.data = data;
        this.p1Utility = utility;
        this.coreSolver = new ZeroSumGameNESolverImpl<ActionPureStrategy, ActionPureStrategy>(utility);
        this.cache = cache;
        this.isRoot = (state.getHistory().getSequenceOf(state.getAllPlayers()[0]).size() == 0) && (state.getHistory().getSequenceOf(state.getAllPlayers()[1]).size() == 0);
    }

    public double getGameValue() {
        return gameValue;
    }

    public MixedStrategy<ActionPureStrategy> getFirstPlayerStrategy() {
        return coreSolver.getPlayerOneStrategy();
    }

    public MixedStrategy<ActionPureStrategy> getSecondPlayerStrategy() {
        return coreSolver.getPlayerTwoStrategy();
    }

    public void generate() {
        if ((beta - alpha) < 1e-14) {
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

        while (true) {
            iters++;
            if (isRoot) {
//                System.out.print("Iterations in root: " + iters);
//                System.out.println(" interval size: " + (beta - alpha));
            }


            Pair<ActionPureStrategy, Double> p2BestResponse = p2Oracle.getBestResponse(getP1MixedStrategy(initialStrategy), alpha, beta);

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

            Pair<ActionPureStrategy, Double> p1BestResponse = p1Oracle.getBestResponse(getP2MixedStrategy(), alpha, beta);

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
    }

    protected void updateForP2Response(PlayerStrategySet<ActionPureStrategy> p2StrategySet) {
        Stats.getInstance().incrementP2StrategyCount();
        coreSolver.addPlayerTwoStrategies(p2StrategySet);
        coreSolver.computeNashEquilibrium();
        gameValue = coreSolver.getGameValue();
    }

    protected void updateForP1Response(PlayerStrategySet<ActionPureStrategy> p1StrategySet) {
        Stats.getInstance().incrementP1StrategyCount();
        coreSolver.addPlayerOneStrategies(p1StrategySet);
        coreSolver.computeNashEquilibrium();
        gameValue = coreSolver.getGameValue();
    }

    protected MixedStrategy<ActionPureStrategy> getP2MixedStrategy() {
        return coreSolver.getPlayerTwoStrategy();
    }

    protected MixedStrategy<ActionPureStrategy> getP1MixedStrategy(ActionPureStrategy p1BestResponse) {
        return coreSolver.getPlayerOneStrategy() == null ? getInitMixedStrategy(p1BestResponse) : coreSolver.getPlayerOneStrategy();
    }

    protected MixedStrategy<ActionPureStrategy> getInitMixedStrategy(ActionPureStrategy p1BestResponse) {
        MixedStrategy<ActionPureStrategy> p1MixedStrategy = new MixedStrategy<ActionPureStrategy>();

        p1MixedStrategy.put(p1BestResponse, 1.0);
        return p1MixedStrategy;
    }

    protected void updateCacheValues(PlayerStrategySet<ActionPureStrategy> p1StrategySet, PlayerStrategySet<ActionPureStrategy> p2StrategySet) {
        for (ActionPureStrategy p1Strategy : p1StrategySet) {
            for (ActionPureStrategy p2Strategy : p2StrategySet) {
                Pair<ActionPureStrategy, ActionPureStrategy> strategyPair = new Pair<ActionPureStrategy, ActionPureStrategy>(p1Strategy, p2Strategy);

                if (cache.getOptimisticUtilityFor(strategyPair) == null || cache.getPesimisticUtilityFor(strategyPair) == null)
                    updateCacheFromAlphaBeta(strategyPair);
                updateCacheFromRecursion(strategyPair);
            }
        }
    }

    protected void updateCacheFromRecursion(Pair<ActionPureStrategy, ActionPureStrategy> strategyPair) {
        double pesimisticUtility = cache.getPesimisticUtilityFor(strategyPair);
        double optimisticUtility = cache.getOptimisticUtilityFor(strategyPair);

        if (optimisticUtility - pesimisticUtility > 1e-14) {
            Double utility = p1Utility.getUtility(strategyPair.getLeft(), strategyPair.getRight(), pesimisticUtility, optimisticUtility);

            if (!utility.isNaN())
                cache.setPesAndOptValueFor(strategyPair, utility);
        }
    }

    protected void updateCacheFromAlphaBeta(Pair<ActionPureStrategy, ActionPureStrategy> strategyPair) {
        GameState tempState = getStateAfter(strategyPair);
        long time = System.currentTimeMillis();
        double pesimisticUtility = -data.getAlphaBetaFor(tempState.getAllPlayers()[1]).getUnboundedValue(tempState);
        double optimisticUtility = data.getAlphaBetaFor(tempState.getAllPlayers()[0]).getUnboundedValue(tempState);

        Stats.getInstance().addToABTime(System.currentTimeMillis() - time);
        cache.setPesAndOptValueFor(strategyPair, optimisticUtility, pesimisticUtility);
    }

    protected GameState getStateAfter(Pair<ActionPureStrategy, ActionPureStrategy> strategyPair) {
        GameState tempState = rootState.performAction(strategyPair.getLeft().getAction());

        tempState.performActionModifyingThisState(strategyPair.getRight().getAction());
        return tempState;
    }
}
