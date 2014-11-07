package cz.agents.gtlibrary.algorithms.sequenceform.gensum;

import cz.agents.gtlibrary.algorithms.sequenceform.FullSequenceEFG;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.domain.aceofspades.AoSExpander;
import cz.agents.gtlibrary.domain.aceofspades.AoSGameInfo;
import cz.agents.gtlibrary.domain.aceofspades.AoSGameState;
import cz.agents.gtlibrary.interfaces.*;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UndomGenSumSequenceFormMILP extends GenSumSequenceFormMILP {

    public static void main(String[] args) {
        runAoS();
    }

    protected static void runAoS() {
        GameState root = new AoSGameState();
        GenSumSequenceFormConfig config = new GenSumSequenceFormConfig();
        Expander<SequenceInformationSet> expander = new AoSExpander<>(config);
        FullSequenceEFG builder = new FullSequenceEFG(root, expander, new AoSGameInfo(), config);

        builder.generateCompleteGame();
        GenSumSequenceFormMILP solver = new UndomGenSumSequenceFormMILP(config, root.getAllPlayers(), new AoSGameInfo(), root, expander, root.getAllPlayers()[1]);

        solver.compute();
    }

    protected GameState rootState;
    protected Expander<SequenceInformationSet> expander;
    protected Player player;

    public UndomGenSumSequenceFormMILP(GenSumSequenceFormConfig config, Player[] players, GameInfo info, GameState rootState, Expander<SequenceInformationSet> expander, Player player) {
        super(config, players, info);
        this.rootState = rootState;
        this.expander = expander;
        this.player = player;
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
        Map<Sequence, Double> opponentRealPlan = getUniformOpponentRealPlan();

        for (Map.Entry<Map<Player, Sequence>, Double[]> entry : config.getUtilityForSequenceCombinationGenSum().entrySet()) {
            Sequence playerSequence = entry.getKey().get(player);
            double utility = entry.getValue()[player.getId()];

            lpTable.addToObjective(playerSequence, utility * opponentRealPlan.get(entry.getKey().get(info.getOpponent(player))));
        }
    }

    public Map<Sequence, Double> getUniformOpponentRealPlan() {
        Map<Sequence, Double> opponentRealPlan = new HashMap<>();
        Player opponent = info.getOpponent(player);
        ArrayDeque<GameState> queue = new ArrayDeque<>();

        queue.add(rootState);
        opponentRealPlan.put(rootState.getSequenceFor(opponent), 1d);
        while (!queue.isEmpty()) {
            GameState state = queue.removeFirst();

            if (!state.isGameEnd()) {
                List<Action> actions = expander.getActions(state);
                double prefixProbability = opponentRealPlan.get(state.getSequenceFor(opponent));

                for (Action action : actions) {
                    GameState nextState = state.performAction(action);

                    if (state.getPlayerToMove().equals(opponent))
                        opponentRealPlan.put(nextState.getSequenceFor(opponent), prefixProbability/actions.size());
                    queue.addLast(nextState);
                }
            }
        }
        return opponentRealPlan;
    }
}
