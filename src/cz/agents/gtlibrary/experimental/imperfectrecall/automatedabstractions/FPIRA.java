package cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions;

import cz.agents.gtlibrary.algorithms.sequenceform.SQFBestResponseAlgorithm;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.interfaces.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FPIRA extends AutomatedAbstractionAlgorithm {

    private SQFBestResponseAlgorithm p0BR;
    private SQFBestResponseAlgorithm p1BR;

    public FPIRA(GameState rootState, Expander<SequenceInformationSet> expander, GameInfo info) {
        super(rootState, expander, info);
    }

    @Override
    protected void printStatistics() {

    }

    @Override
    protected void iteration(Player opponent) {
//        Map<Action, Double> strategy = getStrategyFor(opponent);
//        SQFBestResponseAlgorithm br = getBestResponseAlg(opponent);
//        double value = br.calculateBR(rootState, strategy);
//        Map<Sequence, Double> bestResponse = br.getBRStategy();
//
//        Set<ISKey> visitedISs = new HashSet<>();
//        countISsVisited(fullBestResponseResult, rootState, rootState.getAllPlayers()[1 - opponent.getId()], visitedISs);
//        maxBRInformationSets = Math.max(maxBRInformationSets, visitedISs.size());
//        updateISs(rootState, fullBestResponseResult, strategy, opponent);
////        updateData(rootState, bestResponse, strategy);
//        return value;
    }

    private SQFBestResponseAlgorithm getBestResponseAlg(Player opponent) {
        return opponent.getId() == 0 ? p1BR : p0BR;
    }

    protected Map<Action, Double> getStrategyFor(Player player) {
        Map<Action, Double> strategy = new HashMap<>(expander.getAlgorithmConfig().getAllInformationSets().size() / 2);

        currentAbstractionInformationSets.values().stream().filter(is -> is.getPlayer().equals(player)).forEach(is -> {
            double[] meanStrategy = is.getData().getMp();

            for (GameState state : is.getAllStates()) {
                int index = 0;

                for (Action action : expander.getActions(state)) {
                    strategy.put(action, meanStrategy[index++]);
                }
            }
        });
        return strategy;
    }
}
