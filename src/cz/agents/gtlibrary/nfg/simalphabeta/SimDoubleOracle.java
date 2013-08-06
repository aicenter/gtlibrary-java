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

		double patrollerValue = 0;
		double evaderValue = 0;

		PlayerStrategySet<ActionPureStrategy> firstPlayerStrategySet = new PlayerStrategySet<ActionPureStrategy>();
		PlayerStrategySet<ActionPureStrategy> secondPlayerStrategySet = new PlayerStrategySet<ActionPureStrategy>();

		MixedStrategy<ActionPureStrategy> firstPlayerMixedStrategy = new MixedStrategy<ActionPureStrategy>();
		MixedStrategy<ActionPureStrategy> secondPlayerMixedStrategy = new MixedStrategy<ActionPureStrategy>();

		ActionPureStrategy firstPlayerOracleBestResponse = p1Oracle.getForcedBestResponse(secondPlayerMixedStrategy, alpha, beta);
//		Info.incrementFPStrategyCount();
		firstPlayerStrategySet.add(firstPlayerOracleBestResponse);
		firstPlayerMixedStrategy.add(firstPlayerOracleBestResponse, 1.0);
		coreSolver.addPlayerOneStrategies(firstPlayerStrategySet);

		Pair<ActionPureStrategy, Double> secondPlayerOracleBestResponse = p2Oracle.getBestResponse(firstPlayerMixedStrategy, alpha, beta, hardAlpha, hardBeta);
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
		coreSolver.addPlayerTwoStrategies(secondPlayerStrategySet);

		coreSolver.computeNashEquilibrium();
		firstPlayerMixedStrategy = coreSolver.getPlayerOneStrategy();
		secondPlayerMixedStrategy = coreSolver.getPlayerTwoStrategy();
		gameValue = coreSolver.getGameValue();
		assert gameValue == gameValue;

		while (true) {
			assert alpha <= beta + 1e-8;
			// ********************* GET EVADER'S BEST RESPONSE ****************************
			Pair<ActionPureStrategy, Double> evadersBR = p1Oracle.getBestResponse(secondPlayerMixedStrategy, alpha, beta, hardAlpha, hardBeta);
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
				coreSolver.addPlayerOneStrategies(firstPlayerStrategySet);
				coreSolver.computeNashEquilibrium();

				firstPlayerMixedStrategy = coreSolver.getPlayerOneStrategy();
				secondPlayerMixedStrategy = coreSolver.getPlayerTwoStrategy();
				gameValue = coreSolver.getGameValue();
				assert gameValue == gameValue;
			}

			// *************** GET PATROLLER'S BEST RESPONSE ********************
			Pair<ActionPureStrategy, Double> patrollerBR = p2Oracle.getBestResponse(firstPlayerMixedStrategy, alpha, beta, hardAlpha, hardBeta);
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
				coreSolver.addPlayerTwoStrategies(secondPlayerStrategySet);

				// ********************* COMPUTE NASH EQUILIBRIUM OF THE GAME *****************************
				coreSolver.computeNashEquilibrium();

				firstPlayerMixedStrategy = coreSolver.getPlayerOneStrategy();
				secondPlayerMixedStrategy = coreSolver.getPlayerTwoStrategy();
				gameValue = coreSolver.getGameValue();
				assert gameValue == gameValue;
			}

			// ************** TERMINATION CONDITIONS *************************
			if (CHECK_STRATEGY_SET_CHANGES) {//udìlat z toho získávání strategií, propojit s novejma classama refactor, udìlata by to bylo pøipravený když hraje nature první, commit, spoèítat kolik je tìch pure rp 
				if (!evaderBRadded && !patrollerBRadded)
					break;
			} else if (Math.abs(patrollerValue + gameValue) < EPS && Math.abs(evaderValue - gameValue) < EPS) 
				break;
		}
		//           coreSolver.releaseModel();
	}

	private void updateCacheValues(PlayerStrategySet<ActionPureStrategy> firstPlayerStrategySet, PlayerStrategySet<ActionPureStrategy> secondPlayerStrategySet) {
		for (ActionPureStrategy fpStrategy : firstPlayerStrategySet) {
			for (ActionPureStrategy spStrategy : secondPlayerStrategySet) {
				Pair<ActionPureStrategy, ActionPureStrategy> strategyPair = new Pair<ActionPureStrategy, ActionPureStrategy>(fpStrategy, spStrategy);
				if (cache.getOptimisticUtilityFor(strategyPair) == null || cache.getPesimisticUtilityFor(strategyPair) == null) {
					GameState tempState = state.performAction(fpStrategy.getAction());

					tempState.performActionModifyingThisState(spStrategy.getAction());

//					long time = System.currentTimeMillis();
					double pesimisticUtility = -data.getAlphaBetaFor(tempState.getAllPlayers()[1]).getValue(tempState, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
					double optimisticUtility = data.getAlphaBetaFor(tempState.getAllPlayers()[0]).getValue(tempState, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
					
//					Info.addToABTime(System.currentTimeMillis() - time);
					cache.setPesAndOptValueFor(fpStrategy, spStrategy, optimisticUtility, pesimisticUtility);
				}

				double pesimisticUtility = cache.getPesimisticUtilityFor(strategyPair);
				double optimisticUtility = cache.getOptimisticUtilityFor(strategyPair);

				if (optimisticUtility - pesimisticUtility > 1e-14) {
					Double utility = p1Utility.getUtility(fpStrategy, spStrategy, pesimisticUtility, optimisticUtility);

					if (!utility.isNaN())
						cache.setPesAndOptValueFor(fpStrategy, spStrategy, utility);
				}
			}
		}
	}
}
