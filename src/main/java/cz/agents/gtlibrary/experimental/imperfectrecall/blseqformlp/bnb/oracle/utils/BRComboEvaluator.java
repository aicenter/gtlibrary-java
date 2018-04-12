package cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.utils;

import cz.agents.gtlibrary.interfaces.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

public class BRComboEvaluator {
//    private static Map<Action, Double> minPlayerBestResponse;
//    private static Function<DoubleStream, Optional<Double>> minPlayerValuesAggregator;
//    private static Player maxPlayer;
//    private static Expander<? extends InformationSet> expander;
//    private static Map<Sequence, Map<Action, Double>> expectedValues;
//
//    public static double computeEvaluation(GameState state, Expander<? extends InformationSet> expander,
//                                           Map<Action, Double> minPlayerBestResponse, Function<DoubleStream, Optional<Double>> minPlayerValuesAggregator, Player maxPlayer) {
//        BRComboEvaluator.minPlayerBestResponse = minPlayerBestResponse;
//        BRComboEvaluator.minPlayerValuesAggregator = minPlayerValuesAggregator;
//        BRComboEvaluator.maxPlayer = maxPlayer;
//        BRComboEvaluator.expander = expander;
//        expectedValues = new HashMap<>();
//        computeEvaluation(state);
//        return getActualValue(state);
//    }
//
//    public static double computeEvaluation(GameState state) {
//        if (state.isGameEnd())
//            return state.getUtilities()[maxPlayer.getId()] * state.getNatureProbability();
//        if (state.getPlayerToMove().equals(maxPlayer)) {
//            Map<Action, Double> currentValues = expander.getActions(state).stream().collect(Collectors.toMap(a -> a, a -> computeEvaluation(state.performAction(a))));
//            Map<Action, Double> currentStoredValues = expectedValues.getOrDefault(state.getSequenceForPlayerToMove(), new HashMap<>(currentValues.size()));
//
//            currentValues.forEach((key, val) -> currentStoredValues.compute(key, (actionKey, reward) -> Math.max(currentValues.get(key), reward)));
//            expectedValues.put(state.getSequenceForPlayerToMove(), currentStoredValues);
//            return currentValues.values().stream().max(Double::compare).get();
//        }
//        if (state.isPlayerToMoveNature())
//            return expander.getActions(state).stream()
//                    .mapToDouble(a -> computeEvaluation(state.performAction(a))).sum();
//        List<Action> actions = expander.getActions(state);
//        Map<Action, Double> currentValues = actions.stream().filter(a -> minPlayerBestResponse.getOrDefault(a, 0d) > 1e-8)
//                .collect(Collectors.toMap(a -> a, a -> computeEvaluation(state.performAction(a))));
//
//        Map<Action, Double> currentStoredValues = expectedValues.getOrDefault(state.getSequenceForPlayerToMove(), new HashMap<>(currentValues.size()));
//
//        currentValues.forEach((key, val) -> currentStoredValues.compute(key, (actionKey, reward) -> Math.max(currentValues.get(key), reward)));
//        expectedValues.put(state.getSequenceForPlayerToMove(), currentStoredValues);
//        return minPlayerValuesAggregator.apply(currentValues.values().stream().mapToDouble(v -> v)).orElse(computeEvaluation(state.performAction(actions.get(0))));
//    }
}
