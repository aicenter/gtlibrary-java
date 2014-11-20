package cz.agents.gtlibrary.algorithms.sequenceform.gensum;

import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;

import java.util.Map;

public class BRGenSumSequenceFormMILP extends GenSumSequenceFormMILP {

    protected Map<Sequence, Double> opponentRealPlan;
    protected Player player;

    public BRGenSumSequenceFormMILP(GenSumSequenceFormConfig config, Player[] players, GameInfo info, Player player, Map<Sequence, Double> opponentRealPlan) {
        super(config, players, info);
        this.player = player;
        this.opponentRealPlan = opponentRealPlan;
    }

    @Override
    public SolverResult compute() {
        generateSequenceConstraints();
        generateISConstraints();
        addObjective();
        return solve();
    }

    protected void addObjective() {
        for (Map.Entry<Map<Player, Sequence>, Double[]> entry : config.getUtilityForSequenceCombinationGenSum().entrySet()) {
            Sequence playerSequence = entry.getKey().get(player);
            double utility = entry.getValue()[1 - player.getId()];
            Double opponentProbability = opponentRealPlan.get(entry.getKey().get(info.getOpponent(player)));

            if (opponentProbability != null)
                lpTable.addToObjective(playerSequence, -utility * opponentProbability);
        }
    }
}
