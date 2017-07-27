package cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.memeff;

import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.br.ALossBestResponseAlgorithm;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.interfaces.*;

import java.util.HashMap;
import java.util.Map;

public class FPIRABestResponse extends ALossBestResponseAlgorithm {

    private Map<ISKey, double[]> opponentAbstractedStrategy;
    private InformationSetKeyMap currentAbstractionISKeys;
    private Map<Action, Double> probabilityCache;

    public FPIRABestResponse(GameState root, Expander<? extends InformationSet> expander, int searchingPlayerIndex,
                             Player[] actingPlayers, AlgorithmConfig<? extends InformationSet> algConfig, GameInfo gameInfo,
                             boolean stateCacheUse, InformationSetKeyMap currentAbstractionISKeys) {
        super(root, expander, searchingPlayerIndex, actingPlayers, algConfig, gameInfo, stateCacheUse);
        this.currentAbstractionISKeys = currentAbstractionISKeys;
        probabilityCache = new HashMap<>();
    }

    public Double calculateBRForAbstractedStrategy(GameState root, Map<ISKey, double[]> opponentAbstractedStrategy) {
        this.opponentAbstractedStrategy = opponentAbstractedStrategy;
        this.probabilityCache.clear();
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
        return AbstractedStrategyUtils.getProbability(sequence, opponentAbstractedStrategy, currentAbstractionISKeys, expander, probabilityCache);
    }

    protected double getProbabilityForAction(Action action) {
        return AbstractedStrategyUtils.getProbabilityForAction(action, opponentAbstractedStrategy, currentAbstractionISKeys, expander, probabilityCache);
    }

}
