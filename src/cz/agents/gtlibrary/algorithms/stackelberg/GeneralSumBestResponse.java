package cz.agents.gtlibrary.algorithms.stackelberg;

import cz.agents.gtlibrary.algorithms.sequenceform.SQFBestResponseAlgorithm;
import cz.agents.gtlibrary.iinodes.ConfigImpl;
import cz.agents.gtlibrary.interfaces.*;

import java.util.Map;

public class GeneralSumBestResponse extends SQFBestResponseAlgorithm {

    public GeneralSumBestResponse(Expander expander, int searchingPlayerIndex, Player[] actingPlayers, ConfigImpl algConfig, GameInfo gameInfo) {
        super(expander, searchingPlayerIndex, actingPlayers, algConfig, gameInfo);
    }

    @Override
    protected Double calculateEvaluation(Map<Player, Sequence> currentHistory, GameState gameState) {
        Double weight = getOpponentRealizationPlan().get(currentHistory.get(players[opponentPlayerIndex]));

        return weight * gameState.getUtilities()[searchingPlayerIndex];
    }
}
