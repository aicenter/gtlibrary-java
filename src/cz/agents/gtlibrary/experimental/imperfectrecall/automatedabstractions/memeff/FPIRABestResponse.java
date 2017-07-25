package cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.memeff;

import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.br.ALossBestResponseAlgorithm;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.iinodes.ImperfectRecallISKey;
import cz.agents.gtlibrary.iinodes.PerfectRecallISKey;
import cz.agents.gtlibrary.interfaces.*;
import sun.plugin.dom.exception.InvalidStateException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FPIRABestResponse extends ALossBestResponseAlgorithm {

    private Map<ISKey, double[]> opponentAbstractedStrategy;
    private InformationSetKeyMap currentAbstractionISKeys;

    public FPIRABestResponse(GameState root, Expander<? extends InformationSet> expander, int searchingPlayerIndex,
                             Player[] actingPlayers, AlgorithmConfig<? extends InformationSet> algConfig, GameInfo gameInfo,
                             boolean stateCacheUse, InformationSetKeyMap currentAbstractionISKeys) {
        super(root, expander, searchingPlayerIndex, actingPlayers, algConfig, gameInfo, stateCacheUse);
        this.currentAbstractionISKeys = currentAbstractionISKeys;
    }

    public Double calculateBRForAbstractedStrategy(GameState root, Map<ISKey, double[]> opponentAbstractedStrategy) {
        this.opponentAbstractedStrategy = opponentAbstractedStrategy;
        return calculateBR(root, new HashMap<>(), new HashMap<>());
    }

    protected Double calculateEvaluation(GameState gameState, double currentStateProbability) {
        double utRes = gameState.getUtilities()[0] * gameState.getNatureProbability();

        if (searchingPlayerIndex == 1)
            utRes *= -1; // a zero sum game
        if (currentStateProbability == 0)
            currentStateProbability = 1d;
        return utRes * currentStateProbability; // weighting with opponent's realization plan
    }

    @Override
    protected double getOpponentProbability(Sequence sequence) {
        if (sequence.isEmpty())
            return 1;
        double probability = 1;

        for (Action action : sequence) {
            probability *= getProbabilityForAction(action);
        }
        return probability;
    }

    protected double getProbabilityForAction(Action action) {
        InformationSet informationSet = action.getInformationSet();
        List<Action> actions = expander.getActions(informationSet.getAllStates().stream().findAny().get());
        double[] realizations = opponentAbstractedStrategy.get(currentAbstractionISKeys.get((PerfectRecallISKey) informationSet.getISKey(), actions));

        for (int i = 0; i < actions.size(); i++) {
            if (actions.get(i).equals(action))
                return realizations[i];
        }
        throw new InvalidStateException("Action not found");
    }

}
