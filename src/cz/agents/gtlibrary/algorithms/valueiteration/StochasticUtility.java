package cz.agents.gtlibrary.algorithms.valueiteration;

import java.util.Map;

import cz.agents.gtlibrary.domain.stochastic.StochasticExpander;
import cz.agents.gtlibrary.domain.stochastic.experiment.AttackerAction;
import cz.agents.gtlibrary.domain.stochastic.experiment.ExperimentGameState;
import cz.agents.gtlibrary.domain.stochastic.experiment.NormalizationNatrueAction;
import cz.agents.gtlibrary.domain.stochastic.experiment.PatrollerAction;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.nfg.ActionPureStrategy;
import cz.agents.gtlibrary.nfg.Utility;

public class StochasticUtility extends Utility<ActionPureStrategy, ActionPureStrategy> {

	private GameState state;
	private StochasticExpander expander;
	private Map<GameState, Double> values;

	public StochasticUtility(GameState state, StochasticExpander expander, Map<GameState, Double> values) {
		super();
		this.state = state;
		this.expander = expander;
		this.values = values;
	}

	@Override
	public double getUtility(ActionPureStrategy s1, ActionPureStrategy s2) {
		GameState nextState = state.performAction(s1.getAction());
		double utility = 0;
		double sum1 = 0;
		double sum3 = 0;

		assert s1.getAction() instanceof PatrollerAction;
		assert s2.getAction() instanceof AttackerAction;
		nextState.performActionModifyingThisState(s2.getAction());
		for (Action action : expander.getActions(nextState)) {
			GameState tempState = nextState.performAction(action);

			sum1 += nextState.getProbabilityOfNatureFor(action);
			if (tempState.isGameEnd()) {
				utility += values.get(tempState) * nextState.getProbabilityOfNatureFor(action);
				sum3 += nextState.getProbabilityOfNatureFor(action);
			} else {
				double sum2 = 0;
				
				for (Action action1 : expander.getActions(tempState)) {
					assert action1 instanceof NormalizationNatrueAction;
					assert ((ExperimentGameState) tempState).isSecondMoveOfNature();
					assert tempState.getProbabilityOfNatureFor(action1) > 0;
					sum2 += tempState.getProbabilityOfNatureFor(action1);
					sum3 += tempState.getProbabilityOfNatureFor(action1) * nextState.getProbabilityOfNatureFor(action);
					utility += values.get(tempState.performAction(action1)) * tempState.getProbabilityOfNatureFor(action1) * nextState.getProbabilityOfNatureFor(action);
				}
				assert Math.abs(sum2 - 1) < 1e-8;
			}
		}
		assert  Math.abs(sum1 - 1) < 1e-8;
		assert  Math.abs(sum3 - 1) < 1e-8;
		return utility;
	}

}
