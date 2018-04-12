package cz.agents.gtlibrary.algorithms.sequenceform.gensum;

import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.utils.Pair;

public class PlayerExpValMaxGenSumSequenceFormMILP extends GenSumSequenceFormMILP {
    private Player[] maxPlayers;

    public PlayerExpValMaxGenSumSequenceFormMILP(GenSumSequenceFormConfig config, Player[] players, GameInfo info, Player... maxPlayers) {
        super(config, players, info);
        this.maxPlayers = maxPlayers;
    }

    @Override
    public SolverResult compute() {
        generateSequenceConstraints();
        generateISConstraints();
        addObjective();
//        addMaxValueConstraints();
        return solve();
    }

    protected void addObjective() {
        for (Player maxPlayer : maxPlayers) {
            lpTable.setObjective(new Pair<>("v", maxPlayer.getId()), 1);
        }
    }

}
