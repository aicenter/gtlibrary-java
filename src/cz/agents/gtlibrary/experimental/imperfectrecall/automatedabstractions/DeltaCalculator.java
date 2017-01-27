package cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions;

import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.br.ALossBestResponseAlgorithm;
import cz.agents.gtlibrary.interfaces.*;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DeltaCalculator extends ALossBestResponseAlgorithm {


    private StrategyDiffs strategyDiffs;

    public DeltaCalculator(GameState root, Expander<? extends InformationSet> expander, int searchingPlayerIndex, AlgorithmConfig<? extends InformationSet> algConfig, GameInfo gameInfo, boolean stateCacheUse) {
        super(root, expander, searchingPlayerIndex, new Player[]{root.getAllPlayers()[0], root.getAllPlayers()[1]}, algConfig, gameInfo, stateCacheUse);
    }

    public double calculateDelta(Map<Action, Double> strategy, StrategyDiffs strategyDiffs) {
        this.strategyDiffs = strategyDiffs;
        return super.calculateBR(gameTreeRoot, strip(strategy, strategyDiffs));
    }

    private Map<Action, Double> strip(Map<Action, Double> strategy, StrategyDiffs strategyDiffs) {
        strip(gameTreeRoot, strategy, strategyDiffs, new HashSet<>());
        return null;
    }

    private int strip(GameState state, Map<Action, Double> strategy, StrategyDiffs strategyDiffs, Set<Action> actionsToDelete) {
//        if (state.isPlayerToMoveNature() || state.getPlayerToMove().getId() == searchingPlayerIndex)
//            return expander.getActions(state).stream().mapToInt(a -> strip(state.performAction(a), strategy, strategyDiffs, actionsToDelete)).sum();
        return 0;

    }
}
