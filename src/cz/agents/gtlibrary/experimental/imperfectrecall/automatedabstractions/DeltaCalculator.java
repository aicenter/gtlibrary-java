package cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions;

import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.br.ALossBestResponseAlgorithm;
import cz.agents.gtlibrary.iinodes.ArrayListSequenceImpl;
import cz.agents.gtlibrary.interfaces.*;

import java.util.HashMap;
import java.util.Map;

public class DeltaCalculator extends ALossBestResponseAlgorithm {

    private StrategyDiffs strategyDiffs;
    private double prProbability;
    private double irProbability;

    public DeltaCalculator(GameState root, Expander<? extends InformationSet> expander, int searchingPlayerIndex, AlgorithmConfig<? extends InformationSet> algConfig, GameInfo gameInfo, boolean stateCacheUse) {
        super(root, expander, searchingPlayerIndex, new Player[]{root.getAllPlayers()[0], root.getAllPlayers()[1]}, algConfig, gameInfo, stateCacheUse);
    }

    public double calculateDelta(Map<Action, Double> strategy, StrategyDiffs strategyDiffs) {
        this.strategyDiffs = strategyDiffs;
        prProbability = 1;
        irProbability = 1;
        return super.calculateBR(gameTreeRoot, strategy);
    }

    @Override
    protected Double calculateEvaluation(GameState gameState, double currentStateProbability) {
        double utRes = gameState.getUtilities()[searchingPlayerIndex] * gameState.getNatureProbability();

        return utRes * prProbability - utRes * irProbability;
    }

    protected void handleState(BRActionSelection selection, Action action, GameState state) {
        Map<Action, GameState> successors = stateCache.computeIfAbsent(state, s -> USE_STATE_CACHE ? new HashMap<>(10000) : dummyInstance);
        GameState newState = successors.computeIfAbsent(action, a -> state.performAction(a));
        double natureProb = state.getNatureProbability();
//        double oppRP = getOpponentProbability(state.getSequenceFor(players[opponentPlayerIndex]));

        prProbability = getPRProbability(state, action);
        irProbability = getIRProbability(state, action);
//        if (newLowerBound <= MAX_UTILITY_VALUE) {
        double value = (Math.abs(prProbability) < 1e-8 && Math.abs(irProbability) < 1e-8) ? 0 : bestResponse(newState, Double.NEGATIVE_INFINITY, 1);

        selection.addValue(action, value, natureProb, 1);
//        }
    }

    protected double getOpponentProbability(Sequence sequence) {
        return 1;
    }

    private double getIRProbability(GameState state, Action action) {
        Sequence sequence = new ArrayListSequenceImpl(players[opponentPlayerIndex]);
        double probability = 1;

        for (Action oldAction : state.getSequenceFor(players[opponentPlayerIndex])) {
            probability *= getProbability(sequence, oldAction, strategyDiffs.irStrategyDiff);
            sequence.addLast(oldAction);
        }
        if (state.getPlayerToMove().equals(players[opponentPlayerIndex]))
            probability *= getProbability(state.getSequenceForPlayerToMove(), action, strategyDiffs.irStrategyDiff);
        return probability;
    }

    private double getPRProbability(GameState state, Action action) {
        Sequence sequence = new ArrayListSequenceImpl(players[opponentPlayerIndex]);
        double probability = 1;

        for (Action oldAction : state.getSequenceFor(players[opponentPlayerIndex])) {
            probability *= getProbability(sequence, oldAction, strategyDiffs.prStrategyDiff);
            sequence.addLast(oldAction);
        }
        if (state.getPlayerToMove().equals(players[opponentPlayerIndex]))
            probability *= getProbability(state.getSequenceForPlayerToMove(), action, strategyDiffs.prStrategyDiff);
        return probability;
    }

    private double getProbability(Sequence sequence, Action action, Map<Sequence, Map<Action, Double>> strategyDiff) {
        Map<Action, Double> diffForSequence = strategyDiff.get(sequence);
        double diffProbability = diffForSequence == null ? 0 : diffForSequence.getOrDefault(action, 0d);

        return opponentBehavioralStrategy.getOrDefault(action, 0d) + diffProbability;
    }
}
