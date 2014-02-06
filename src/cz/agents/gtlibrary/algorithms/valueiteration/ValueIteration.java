package cz.agents.gtlibrary.algorithms.valueiteration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import cz.agents.gtlibrary.algorithms.valueiteration.alphabeta.AlphaBeta;
import cz.agents.gtlibrary.algorithms.valueiteration.alphabeta.AlphaBetaImpl;
import cz.agents.gtlibrary.algorithms.valueiteration.task.AlphaBetaTask;
import cz.agents.gtlibrary.algorithms.valueiteration.task.BuildingTask;
import cz.agents.gtlibrary.domain.stochastic.StochasticExpander;
import cz.agents.gtlibrary.domain.stochastic.experiment.ExperimentExpander;
import cz.agents.gtlibrary.domain.stochastic.experiment.ExperimentGameState;
import cz.agents.gtlibrary.domain.stochastic.experiment.ExperimentInfo;
import cz.agents.gtlibrary.domain.stochastic.experiment.SanityCheck;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.nfg.ActionPureStrategy;
import cz.agents.gtlibrary.nfg.Utility;
import cz.agents.gtlibrary.nfg.core.ZeroSumGameNESolver;
import cz.agents.gtlibrary.nfg.core.ZeroSumGameNESolverImpl;
import cz.agents.gtlibrary.utils.threadpool.ThreadPool;

public class ValueIteration {

	private StochasticExpander expander;

	public static void main(String[] args) {
		ExperimentInfo.commitmentDepth = Integer.parseInt(args[0]);
		ExperimentInfo.epsilon = Double.parseDouble(args[1]);
		ExperimentInfo.graphFile = args[2];
		int threadCount = Integer.parseInt(args[3]);
//		ValueIteration valueIteration = new ValueIteration(new SGExpander());
//		Map<GameState, Double> values = valueIteration.computeValues(new SGGameState(new Random(1)));
		ValueIteration valueIteration = new ValueIteration(new ExperimentExpander());
		Map<GameState, Double> values = valueIteration.computeValues(new ExperimentGameState(), threadCount);
//		ValueIteration valueIteration = new ValueIteration(new NEExpander());
//		Map<GameState, Double> values = valueIteration.computeValues(NEGameInfo.gameStates.get("A"));
	}

	public ValueIteration(StochasticExpander expander) {
		super();
		this.expander = expander;
	}

	private Map<GameState, Double> computeValues(GameState root, int capacity) {
		ThreadPool pool = new ThreadPool(capacity);
		Map<GameState, Double> values = initValues(root, pool);
		Map<GameState, Double> lastValues = new HashMap<GameState, Double>(values);
		int consecutiveLikeliness = 0;

		while (true) {
			Map<GameState, Double> tempValues = Collections.synchronizedMap(new HashMap<GameState, Double>(values.size()));

			for (GameState state : values.keySet()) {
				if (state.isGameEnd() || state.equals(root)) {
					tempValues.put(state, values.get(state));
					continue;
				}
				pool.addTask(new AlphaBetaTask(expander, state, values, tempValues));
			}
			while (!pool.isFinnished()) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			values = tempValues;
			double maxDifference = getMaxDifference(values, lastValues);

			System.out.println("max difference: " + maxDifference);
			lastValues = new HashMap<GameState, Double>(values);
			if (maxDifference < 1e-6)
				consecutiveLikeliness++;
			else
				consecutiveLikeliness = 0;
			if (consecutiveLikeliness > 4) {
				AlphaBeta alphabeta = new AlphaBetaImpl(expander, values, root, -1, 0);

				values.put(root, alphabeta.getFirstLevelValue(root, -1, 0));
				System.out.println("final root value: " + values.get(root));
				pool.killAll();
				return values;
			}
		}

	}

	private double getMaxDifference(Map<GameState, Double> values, Map<GameState, Double> lastValues) {
		double maxDifference = Double.NEGATIVE_INFINITY;

		for (Entry<GameState, Double> entry : values.entrySet()) {
			double currentDifference = Math.abs(entry.getValue() - lastValues.get(entry.getKey()));

			if (currentDifference > maxDifference)
				maxDifference = currentDifference;
		}
		return maxDifference;
	}

	private Map<GameState, Double> initValues(GameState root, ThreadPool pool) {
		Map<GameState, Double> values = Collections.synchronizedMap(new HashMap<GameState, Double>(10000));

		values.put(root, root.getUtilities()[0]);
		List<Action> actions = expander.getActions(root);
		
		System.out.println(actions.size());
		for (Action patrollerAction : actions) {
			GameState attackerState = root.performAction(patrollerAction);

			pool.addTask(new BuildingTask(attackerState, expander, values));
		}
		System.out.println("Building tasks added...");
		while (!pool.isFinnished()) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Init build finnished, state count: " + values.size());
		return values;
	}

}
