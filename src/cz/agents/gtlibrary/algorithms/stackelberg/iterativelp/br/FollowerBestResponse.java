package cz.agents.gtlibrary.algorithms.stackelberg.iterativelp.br;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.stackelberg.StackelbergConfig;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.Pair;

import java.util.*;

public class FollowerBestResponse {

    private Map<GameState, List<GameState>> successors;
    private Map<GameState, double[]> cache;
    private Map<InformationSet, Map<GameState, Map<Sequence, double[]>>> expectedValues;
    private Map<Sequence, Double> bestResponse;
    private final Expander<SequenceInformationSet> expander;
    private final GameState root;
    private final StackelbergConfig config;
    private final Player leader;
    private final Player follower;
    private static final double[] NEG_INF = new double[]{Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY};
    private final double eps = 1e-8;

    public FollowerBestResponse(GameState root, Expander<SequenceInformationSet> expander, StackelbergConfig config, Player leader, Player follower) {
        successors = new HashMap<>();
        this.root = root;
        this.expander = expander;
        this.config = config;
        this.leader = leader;
        this.follower = follower;
    }

    public Pair<Map<Sequence, Double>, Double > computeBestResponseTo(Map<Sequence, Double> leaderRealPlan) {
        expectedValues = new HashMap<>();
        cache = new HashMap<>();
        bestResponse = new HashMap<>();
        double[] expectedUtility = computeBestResponseTo(root, leaderRealPlan);

        return new Pair<>(bestResponse, expectedUtility[leader.getId()]);
    }

//    private Map<Sequence, Double> createBestResponse() {
//        Map<Sequence, Double> bestResponse = new HashMap<>(expectedValues.size());
//
//        for (Map<Sequence, double[]> isExpectedValues : expectedValues.values()) {
//            double[] currentBest = NEG_INF;
//            Sequence sequence = null;
//
//            for (Map.Entry<Sequence, double[]> entry : isExpectedValues.entrySet()) {
//                if (isPrefferable(currentBest, entry.getValue())) {
//                    currentBest = entry.getValue();
//                    sequence = entry.getKey();
//                }
//            }
//            bestResponse.put(sequence, 1d);
//        }
//        return bestResponse;
//    }

    private double[] computeBestResponseTo(GameState state, Map<Sequence, Double> leaderRealPlan) {
        double[] cachedUtility = cache.get(state);

        if (cachedUtility != null)
            return cachedUtility;
        if (state.isGameEnd())
            return getWeightedUtility(state, leaderRealPlan);
        double[] utility = new double[2];

        if (state.getPlayerToMove().equals(follower)) {
            SequenceInformationSet informationSet = config.getInformationSetFor(state);
            Map<GameState, Map<Sequence, double[]>> isExpectedValues = expectedValues.get(informationSet);

            if (isExpectedValues == null) {
                isExpectedValues = new HashMap<>();
                expectedValues.put(informationSet, isExpectedValues);
                for (GameState gameState : informationSet.getAllStates()) {
                    Map<Sequence, double[]> stateExpectedValues = isExpectedValues.get(gameState);

                    if (stateExpectedValues == null) {
                        stateExpectedValues = new HashMap<>();
                        isExpectedValues.put(gameState, stateExpectedValues);
                    }
                    for (GameState successor : getSuccessors(gameState)) {
                        double[] currentSequenceExpected = stateExpectedValues.get(successor.getSequenceFor(follower));

                        if (currentSequenceExpected == null)
                            currentSequenceExpected = new double[2];
                        if(get(leaderRealPlan, successor.getSequenceFor(leader)) > eps) {
                            double[] successorUtility = computeBestResponseTo(successor, leaderRealPlan);

                            for (int i = 0; i < currentSequenceExpected.length; i++) {
                                currentSequenceExpected[i] += successorUtility[i];
                            }
                        }
                        stateExpectedValues.put(successor.getSequenceFor(follower), currentSequenceExpected);
                    }
                }
            }
            utility = getExpectedValueFor(state, isExpectedValues);
        } else {
            for (GameState successor : getSuccessors(state)) {
                if (get(leaderRealPlan, successor.getSequenceFor(leader)) > 1e-8) {
                    double[] successorUtility = computeBestResponseTo(successor, leaderRealPlan);

                    for (int i = 0; i < utility.length; i++) {
                        utility[i] += successorUtility[i];
                    }
                }
            }
        }
        cache.put(state, utility);
        return utility;
    }

    private double[] getExpectedValueFor(GameState state, Map<GameState, Map<Sequence, double[]>> isExpectedValues) {
        Map<Sequence, double[]> expectedValues = getSequenceExpectedValues(isExpectedValues);
        double[] currentMax = NEG_INF;
        Sequence best = null;

        for (Map.Entry<Sequence, double[]> entry : expectedValues.entrySet()) {
            if(isPrefferable(currentMax, entry.getValue())) {
                currentMax = entry.getValue();
                best = entry.getKey();
            }
        }
        bestResponse.put(best, 1d);
        return isExpectedValues.get(state).get(best);
    }

    private Map<Sequence, double[]> getSequenceExpectedValues(Map<GameState, Map<Sequence, double[]>> isExpectedValues) {
        Map<Sequence, double[]> expectedValues = new HashMap<>();

        for (Map<Sequence, double[]> sequenceMap : isExpectedValues.values()) {
            for (Map.Entry<Sequence, double[]> entry : sequenceMap.entrySet()) {
                double[] oldValue = expectedValues.get(entry.getKey());

                if (oldValue == null) {
                    oldValue = new double[2];
                    expectedValues.put(entry.getKey(), oldValue);
                }
                for (int i = 0; i < oldValue.length; i++) {
                    oldValue[i] += entry.getValue()[i];
                }
            }
        }
        return expectedValues;
    }

    private double[] max(Map<Sequence, double[]> isExpectedValues) {
        double[] currentMax = NEG_INF;

        for (Map.Entry<Sequence, double[]> entry : isExpectedValues.entrySet()) {
            if (isPrefferable(currentMax, entry.getValue()))
                currentMax = entry.getValue();
        }
        return currentMax;
    }

    private boolean isPrefferable(double[] currentMax, double[] expectedValue) {
        return currentMax[follower.getId()] + eps < expectedValue[follower.getId()] ||
                (Math.abs(currentMax[follower.getId()] - expectedValue[follower.getId()]) < eps &&
                        currentMax[leader.getId()] < expectedValue[leader.getId()]);
    }

    private double[] getWeightedUtility(GameState state, Map<Sequence, Double> leaderRealPlan) {
        double[] weightedUtility = new double[2];
        double[] utility = state.getUtilities();

        for (int i = 0; i < weightedUtility.length; i++) {
            weightedUtility[i] = utility[i] * state.getNatureProbability() * get(leaderRealPlan, state.getSequenceFor(leader));
        }
        return weightedUtility;
    }

    private double get(Map<Sequence, Double> leaderRealPlan, Sequence sequence) {
        Double value = leaderRealPlan.get(sequence);

        return value == null ? 0 : value;
    }

    private List<GameState> getSuccessors(GameState state) {
        List<GameState> successors = this.successors.get(state);

        if (successors == null) {
            List<Action> actions = expander.getActions(state);
            successors = new ArrayList<>(actions.size());
            this.successors.put(state, successors);

            for (Action action : actions) {
                successors.add(state.performAction(action));
            }
        }
        return successors;
    }
}
