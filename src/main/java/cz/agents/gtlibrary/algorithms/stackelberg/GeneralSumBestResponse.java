package cz.agents.gtlibrary.algorithms.stackelberg;

import cz.agents.gtlibrary.algorithms.sequenceform.SQFBestResponseAlgorithm;
import cz.agents.gtlibrary.iinodes.ConfigImpl;
import cz.agents.gtlibrary.interfaces.*;

import java.util.Map;

public class GeneralSumBestResponse extends SQFBestResponseAlgorithm {

    public GeneralSumBestResponse(Expander expander, int searchingPlayerIndex, Player[] actingPlayers, AlgorithmConfig<? extends InformationSet> algConfig, GameInfo gameInfo) {
        super(expander, searchingPlayerIndex, actingPlayers, algConfig, gameInfo);
    }

    @Override
    protected Double calculateEvaluation(Map<Player, Sequence> currentHistory, GameState gameState) {
        double utRes = gameState.getUtilities()[searchingPlayerIndex] * gameState.getNatureProbability();
        Double weight = getOpponentRealizationPlan().get(currentHistory.get(players[opponentPlayerIndex]));

        if (weight == null || weight == 0) {
            weight = 1d;
        }
        return utRes * weight;
    }
}
