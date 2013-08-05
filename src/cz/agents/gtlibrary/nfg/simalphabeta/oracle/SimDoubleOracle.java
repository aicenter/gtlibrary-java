package cz.agents.gtlibrary.nfg.simalphabeta.oracle;

import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.nfg.ActionPureStrategy;
import cz.agents.gtlibrary.nfg.MixedStrategy;
import cz.agents.gtlibrary.nfg.PlayerStrategySet;
import cz.agents.gtlibrary.nfg.Utility;
import cz.agents.gtlibrary.nfg.simalphabeta.cache.DOCache;
import cz.agents.gtlibrary.utils.Pair;

public class SimDoubleOracle extends DoubleOracle<ActionPureStrategy, ActionPureStrategy> {

	private DOCache cache;
	private SimABOracle firstPlayerOracle;
	private SimABOracle secondPlayerOracle;

	private double alpha;
	private double beta;
	private double hardAlpha;
	private double hardBeta;
	private double gameValue;
	private GameState state;
	private Data data;

	public SimDoubleOracle(SimABOracle firstPlayerOracle, SimABOracle secondPlayerOracle, Utility<ActionPureStrategy, ActionPureStrategy> utility, double alpha, double beta, DOCache cache, Data data, GameState state) {
		super(null, null, utility);
		this.firstPlayerOracle = firstPlayerOracle;
		this.secondPlayerOracle = secondPlayerOracle;
		this.alpha = alpha;
		this.beta = beta;
		this.hardAlpha = alpha;
		this.hardBeta = beta;
		this.cache = cache;
		this.state = state;
		this.data = data;
//		Info.incrementStatesVisited();

		//		for (ActionPureStrategy fpStrategy : firstPlayerOracle.getCurrentStrategies()) {
		//			for (ActionPureStrategy spStrategy : secondPlayerOracle.getCurrentStrategies()) {
		//				IIGameState tempState = state.performAction(fpStrategy.getAction());
		//
		//				tempState.performActionModifyingThisState(spStrategy.getAction());
		//
		//				double pesimisticUtility = -data.getAlphaBetaFor(tempState.getAllPlayers()[1]).getValue(tempState, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
		//				double optimisticUtility = data.getAlphaBetaFor(tempState.getAllPlayers()[0]).getValue(tempState, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
		//
		//				cache.setPesAndOptValueFor(fpStrategy, spStrategy, optimisticUtility, pesimisticUtility);
		//			}
		//		}

	}

	public double getGameValue() {
		return gameValue;
	}

	public MixedStrategy<ActionPureStrategy> getFirstPlayerStrategy() {
		return NEsolver.getPlayerOneStrategy();
	}

	public MixedStrategy<ActionPureStrategy> getSecondPlayerStrategy() {
		return NEsolver.getPlayerTwoStrategy();
	}

	public void execute() {
		if ((hardBeta - hardAlpha) < 1e-14) {
			gameValue = hardAlpha;
			return;
		}

		double patrollerValue = 0;
		double evaderValue = 0;

		PlayerStrategySet<ActionPureStrategy> firstPlayerStrategySet = new PlayerStrategySet<ActionPureStrategy>();
		PlayerStrategySet<ActionPureStrategy> secondPlayerStrategySet = new PlayerStrategySet<ActionPureStrategy>();

		MixedStrategy<ActionPureStrategy> firstPlayerMixedStrategy = new MixedStrategy<ActionPureStrategy>();
		MixedStrategy<ActionPureStrategy> secondPlayerMixedStrategy = new MixedStrategy<ActionPureStrategy>();

		ActionPureStrategy firstPlayerOracleBestResponse = firstPlayerOracle.getForcedBestResponse(secondPlayerMixedStrategy, alpha, beta);
//		Info.incrementFPStrategyCount();
		firstPlayerStrategySet.add(firstPlayerOracleBestResponse);
		firstPlayerMixedStrategy.add(firstPlayerOracleBestResponse, 1.0);
		NEsolver.addPlayerOneStrategies(firstPlayerStrategySet);

		Pair<ActionPureStrategy, Double> secondPlayerOracleBestResponse = secondPlayerOracle.getBestResponse(firstPlayerMixedStrategy, alpha, beta, hardAlpha, hardBeta);
		assert !secondPlayerOracleBestResponse.getRight().isNaN();
		if (-secondPlayerOracleBestResponse.getRight() > alpha)
			alpha = -secondPlayerOracleBestResponse.getRight();
		if (secondPlayerOracleBestResponse.getLeft() == null) {
//			Info.incrementNaNCuts();
			gameValue = Double.NaN;
			return;
		}
//		Info.incrementSPStrategyCount();
		secondPlayerStrategySet.add(secondPlayerOracleBestResponse.getLeft());
		secondPlayerMixedStrategy.add(secondPlayerOracleBestResponse.getLeft(), 1.0);
		NEsolver.addPlayerTwoStrategies(secondPlayerStrategySet);

		NEsolver.computeNashEquilibrium();
		firstPlayerMixedStrategy = NEsolver.getPlayerOneStrategy();
		secondPlayerMixedStrategy = NEsolver.getPlayerTwoStrategy();
		gameValue = NEsolver.getGameValue();
		assert gameValue == gameValue;
		int it = 0;

		while (true) {
			assert alpha <= beta + 1e-8;
			// ********************* GET EVADER'S BEST RESPONSE ****************************
			Pair<ActionPureStrategy, Double> evadersBR = firstPlayerOracle.getBestResponse(secondPlayerMixedStrategy, alpha, beta, hardAlpha, hardBeta);
			assert !evadersBR.getRight().isNaN();
			if (evadersBR.getRight() < beta)
				beta = evadersBR.getRight();
			if (evadersBR.getLeft() == null) {
//				Info.incrementNaNCuts();
				gameValue = Double.NaN;
				return;
			}

			boolean evaderBRadded = firstPlayerStrategySet.add(evadersBR.getLeft());

			updateCacheValues(firstPlayerStrategySet, secondPlayerStrategySet);
			if (evaderBRadded) {
//				Info.incrementFPStrategyCount();
				NEsolver.addPlayerOneStrategies(firstPlayerStrategySet);
				NEsolver.computeNashEquilibrium();

				firstPlayerMixedStrategy = NEsolver.getPlayerOneStrategy();
				secondPlayerMixedStrategy = NEsolver.getPlayerTwoStrategy();
				gameValue = NEsolver.getGameValue();
				assert gameValue == gameValue;
			}

			// *************** GET PATROLLER'S BEST RESPONSE ********************
			Pair<ActionPureStrategy, Double> patrollerBR = secondPlayerOracle.getBestResponse(firstPlayerMixedStrategy, alpha, beta, hardAlpha, hardBeta);
			if (-patrollerBR.getRight() > alpha)
				alpha = -patrollerBR.getRight();
			if (patrollerBR.getLeft() == null) {
//				Info.incrementNaNCuts();
				gameValue = Double.NaN;
				return;
			}
			boolean patrollerBRadded = secondPlayerStrategySet.add(patrollerBR.getLeft());
			assert !patrollerBR.getRight().isNaN();
			updateCacheValues(firstPlayerStrategySet, secondPlayerStrategySet);
			if (patrollerBRadded) {
//				Info.incrementSPStrategyCount();
				NEsolver.addPlayerTwoStrategies(secondPlayerStrategySet);

				// ********************* COMPUTE NASH EQUILIBRIUM OF THE GAME *****************************
				NEsolver.computeNashEquilibrium();

				firstPlayerMixedStrategy = NEsolver.getPlayerOneStrategy();
				secondPlayerMixedStrategy = NEsolver.getPlayerTwoStrategy();
				gameValue = NEsolver.getGameValue();
				assert gameValue == gameValue;
			}

			// ************** TERMINATION CONDITIONS *************************
			if (CHECK_STRATEGY_SET_CHANGES) {//udìlat z toho získávání strategií, propojit s novejma classama refactor, udìlata by to bylo pøipravený když hraje nature první, commit, spoèítat kolik je tìch pure rp 
				if (!evaderBRadded && !patrollerBRadded) {
//					for (Entry<ActionPureStrategy, Double> entry : firstPlayerMixedStrategy) {
//						if(entry.getValue() > 1e-8)
//							Info.addTofpNESize(1);
//					}
//					for (Entry<ActionPureStrategy, Double> entry : secondPlayerMixedStrategy) {
//						if(entry.getValue() > 1e-8)
//							Info.addTospNESize(1);
//					}
					break;
				}
			} else if (Math.abs(patrollerValue + gameValue) < EPS && Math.abs(evaderValue - gameValue) < EPS) {
				long stopTime = System.currentTimeMillis();
				break;
			}
			it++;
		}
		//           NEsolver.releaseModel();
	}

	private void updateCacheValues(PlayerStrategySet<ActionPureStrategy> firstPlayerStrategySet, PlayerStrategySet<ActionPureStrategy> secondPlayerStrategySet) {
		for (ActionPureStrategy fpStrategy : firstPlayerStrategySet) {
			for (ActionPureStrategy spStrategy : secondPlayerStrategySet) {
				Pair<ActionPureStrategy, ActionPureStrategy> strategyPair = new Pair<ActionPureStrategy, ActionPureStrategy>(fpStrategy, spStrategy);
				if (cache.getOptimisticUtilityFor(strategyPair) == null || cache.getPesimisticUtilityFor(strategyPair) == null) {
					GameState tempState = state.performAction(fpStrategy.getAction());

					tempState.performActionModifyingThisState(spStrategy.getAction());

					long time = System.currentTimeMillis();
					double pesimisticUtility = -data.getAlphaBetaFor(tempState.getAllPlayers()[1]).getValue(tempState, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
					double optimisticUtility = data.getAlphaBetaFor(tempState.getAllPlayers()[0]).getValue(tempState, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
					
//					Info.addToABTime(System.currentTimeMillis() - time);
					cache.setPesAndOptValueFor(fpStrategy, spStrategy, optimisticUtility, pesimisticUtility);
				}

				double pesimisticUtility = cache.getPesimisticUtilityFor(strategyPair);
				double optimisticUtility = cache.getOptimisticUtilityFor(strategyPair);

				if (optimisticUtility - pesimisticUtility > 1e-14) {
					Double utility = ((IIUtility) utilityEvader).getUtility(fpStrategy, spStrategy, pesimisticUtility, optimisticUtility);

					if (!utility.isNaN())
						cache.setPesAndOptValueFor(fpStrategy, spStrategy, utility);
				}
			}
		}
	}
}
