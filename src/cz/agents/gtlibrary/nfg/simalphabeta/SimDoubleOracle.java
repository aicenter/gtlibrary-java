package cz.agents.gtlibrary.nfg.simalphabeta;

import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.nfg.ActionPureStrategy;
import cz.agents.gtlibrary.nfg.MixedStrategy;
import cz.agents.gtlibrary.nfg.PlayerStrategySet;
import cz.agents.gtlibrary.nfg.core.ZeroSumGameNESolverImpl;
import cz.agents.gtlibrary.nfg.doubleoracle.NFGDoubleOracle;
import cz.agents.gtlibrary.nfg.simalphabeta.cache.DOCache;
import cz.agents.gtlibrary.nfg.simalphabeta.oracle.SimABOracle;
import cz.agents.gtlibrary.nfg.simalphabeta.utility.SimUtility;
import cz.agents.gtlibrary.utils.Pair;

public class SimDoubleOracle extends NFGDoubleOracle {
	
	private final boolean CHECK_STRATEGY_SET_CHANGES = true;

	private DOCache cache;
	private SimABOracle p1Oracle;
	private SimABOracle p2Oracle;

	private double alpha;
	private double beta;
	private double hardAlpha;
	private double hardBeta;
	private double gameValue;
	private GameState state;
	private Data data;
	private SimUtility p1Utility;

	public SimDoubleOracle(SimABOracle firstPlayerOracle, SimABOracle secondPlayerOracle, SimUtility utility, double alpha, double beta, DOCache cache, Data data, GameState state) {
		super(state, data.expander, data.gameInfo, data.config);
		this.p1Oracle = firstPlayerOracle;
		this.p2Oracle = secondPlayerOracle;
		this.alpha = alpha;
		this.beta = beta;
		this.hardAlpha = alpha;
		this.hardBeta = beta;
		this.cache = cache;
		this.state = state;
		this.data = data;
		this.p1Utility = utility;
		this.coreSolver = new ZeroSumGameNESolverImpl<ActionPureStrategy, ActionPureStrategy>(utility);
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

	public void execute() {
		if ((hardBeta - hardAlpha) < 1e-14) {
			gameValue = hardAlpha;
			return;
		}

		double p2Value = 0;
		double p1Value = 0;
		PlayerStrategySet<ActionPureStrategy> p1StrategySet = new PlayerStrategySet<ActionPureStrategy>();
		PlayerStrategySet<ActionPureStrategy> p2StrategySet = new PlayerStrategySet<ActionPureStrategy>();
		ActionPureStrategy initialStrategy = p1Oracle.getFirstStrategy();

		p1StrategySet.add(initialStrategy);
		coreSolver.addPlayerOneStrategies(p1StrategySet);
		while (true) {
			Pair<ActionPureStrategy, Double> p2BestResponse = p2Oracle.getBestResponse(getP1MixedStrategy(initialStrategy), alpha, beta, hardAlpha, hardBeta);
			
			if (-p2BestResponse.getRight() > alpha)
				alpha = -p2BestResponse.getRight();
			if (p2BestResponse.getLeft() == null) {
				gameValue = Double.NaN;
				return;
			}
			
			boolean p2BestResponseadded = p2StrategySet.add(p2BestResponse.getLeft());
			
			assert !p2BestResponse.getRight().isNaN();
			updateCacheValues(p1StrategySet, p2StrategySet);
			if (p2BestResponseadded) {
				coreSolver.addPlayerTwoStrategies(p2StrategySet);
				coreSolver.computeNashEquilibrium();
				gameValue = coreSolver.getGameValue();
				assert gameValue == gameValue;
			}
			
			Pair<ActionPureStrategy, Double> p1BestResponse = p1Oracle.getBestResponse(getP2MixedStrategy(), alpha, beta, hardAlpha, hardBeta);

			assert alpha <= beta + 1e-8;
			assert !p1BestResponse.getRight().isNaN();
			if (p1BestResponse.getRight() < beta)
				beta = p1BestResponse.getRight();
			if (p1BestResponse.getLeft() == null) {
				gameValue = Double.NaN;
				return;
			}

			boolean p1BestResponseadded = p1StrategySet.add(p1BestResponse.getLeft());

			updateCacheValues(p1StrategySet, p2StrategySet);
			if (p1BestResponseadded) {
				coreSolver.addPlayerOneStrategies(p1StrategySet);
				coreSolver.computeNashEquilibrium();
				gameValue = coreSolver.getGameValue();
				assert gameValue == gameValue;
			}

			if (CHECK_STRATEGY_SET_CHANGES) {
				if (!p1BestResponseadded && !p2BestResponseadded)
					break;
			} else if (Math.abs(p2Value + gameValue) < EPS && Math.abs(p1Value - gameValue) < EPS) 
				break;
		}
	}

	private MixedStrategy<ActionPureStrategy> getP2MixedStrategy() {
		return coreSolver.getPlayerTwoStrategy();
	}

	private MixedStrategy<ActionPureStrategy> getP1MixedStrategy(ActionPureStrategy p1BestResponse) {
		return coreSolver.getPlayerOneStrategy() == null?getInitMixedStrategy(p1BestResponse):coreSolver.getPlayerOneStrategy();
	}

	private MixedStrategy<ActionPureStrategy> getInitMixedStrategy(ActionPureStrategy p1BestResponse) {
		MixedStrategy<ActionPureStrategy> p1MixedStrategy = new MixedStrategy<ActionPureStrategy>();
		
		p1MixedStrategy.add(p1BestResponse, 1.0);
		return p1MixedStrategy;
	}

	private void updateCacheValues(PlayerStrategySet<ActionPureStrategy> firstPlayerStrategySet, PlayerStrategySet<ActionPureStrategy> secondPlayerStrategySet) {
		for (ActionPureStrategy fpStrategy : firstPlayerStrategySet) {
			for (ActionPureStrategy spStrategy : secondPlayerStrategySet) {
				if (cache.getOptimisticUtilityFor(fpStrategy, spStrategy) == null || cache.getPesimisticUtilityFor(fpStrategy, spStrategy) == null)
					updateCacheFromAlphaBeta(fpStrategy, spStrategy);
				updateCacheFromRecursion(fpStrategy, spStrategy);
			}
		}
	}

	private void updateCacheFromRecursion(ActionPureStrategy fpStrategy, ActionPureStrategy spStrategy) {
		double pesimisticUtility = cache.getPesimisticUtilityFor(fpStrategy, spStrategy);
		double optimisticUtility = cache.getOptimisticUtilityFor(fpStrategy, spStrategy);

		if (optimisticUtility - pesimisticUtility > 1e-14) {
			Double utility = p1Utility.getUtility(fpStrategy, spStrategy, pesimisticUtility, optimisticUtility);

			if (!utility.isNaN())
				cache.setPesAndOptValueFor(fpStrategy, spStrategy, utility);
		}
	}

	private void updateCacheFromAlphaBeta(ActionPureStrategy fpStrategy, ActionPureStrategy spStrategy) {
		GameState tempState = getStateAfter(fpStrategy, spStrategy);
		double pesimisticUtility = -data.getAlphaBetaFor(tempState.getAllPlayers()[1]).getValue(tempState, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
		double optimisticUtility = data.getAlphaBetaFor(tempState.getAllPlayers()[0]).getValue(tempState, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

		cache.setPesAndOptValueFor(fpStrategy, spStrategy, optimisticUtility, pesimisticUtility);
	}

	private GameState getStateAfter(ActionPureStrategy fpStrategy, ActionPureStrategy spStrategy) {
		GameState tempState = state.performAction(fpStrategy.getAction());

		tempState.performActionModifyingThisState(spStrategy.getAction());
		return tempState;
	}
}
