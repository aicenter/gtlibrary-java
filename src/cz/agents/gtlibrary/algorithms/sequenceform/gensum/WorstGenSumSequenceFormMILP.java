package cz.agents.gtlibrary.algorithms.sequenceform.gensum;

import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;

import java.util.Map;

public class WorstGenSumSequenceFormMILP extends BRGenSumSequenceFormMILP {

    public WorstGenSumSequenceFormMILP(GenSumSequenceFormConfig config, Player[] players, GameInfo info, Player player, Map<Sequence, Double> opponentRealPlan) {
        super(config, players, info, player, opponentRealPlan);
    }

    protected void addObjective() {
        for (Map.Entry<Map<Player, Sequence>, Double[]> entry : config.getUtilityForSequenceCombinationGenSum().entrySet()) {
            Sequence playerSequence = entry.getKey().get(player);
            double utility = entry.getValue()[player.getId()];

            lpTable.addToObjective(playerSequence, -utility * opponentRealPlan.get(entry.getKey().get(info.getOpponent(player))));
        }
    }
}
