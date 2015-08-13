package cz.agents.gtlibrary.algorithms.stackelberg.iterativelp.br;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.stackelberg.StackelbergConfig;
import cz.agents.gtlibrary.interfaces.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FollowerBestResponse {

    private Map<GameState, List<GameState>> successors;
    private Map<GameState, double[]> cache;
    private Map<InformationSet, Map<Sequence, double[]>> expectedValues;
    private final Expander<SequenceInformationSet> expander;
    private final GameState root;
    private final StackelbergConfig config;
    private final Player leader;
    private final Player follower;
    private static final double[] NEG_INF = new double[]{Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY};

    public FollowerBestResponse(GameState root, Expander<SequenceInformationSet> expander, StackelbergConfig config, Player leader, Player follower) {
        successors = new HashMap<>();
        this.root = root;
        this.expander = expander;
        this.config = config;
        this.leader = leader;
        this.follower = follower;
    }

    public Map<Sequence, Double> computeBestResponseTo(Map<Sequence, Double> opponentStrategy) {
        expectedValues = new HashMap<>();
        cache = new HashMap<>();
        computeBestResponseTo(root, opponentStrategy);
        return createBestResponse();
    }

    private Map<Sequence, Double> createBestResponse() {
        Map<Sequence, Double> bestResponse = new HashMap<>(expectedValues.size());

        for (Map<Sequence, double[]> isExpectedValues : expectedValues.values()) {
            double[] currentBest = NEG_INF;
            Sequence sequence = null;

            for (Map.Entry<Sequence, double[]> entry : isExpectedValues.entrySet()) {
                if (isPrefferable(currentBest, entry.getValue())) {
                    currentBest = entry.getValue();
                    sequence = entry.getKey();
                }
            }
            bestResponse.put(sequence, 1d);
        }
        return bestResponse;
    }

    private double[] computeBestResponseTo(GameState state, Map<Sequence, Double> opponentStrategy) {
        double[] cachedUtility = cache.get(state);

        if (cachedUtility != null)
            return cachedUtility;
        if (state.isGameEnd())
            return getWeightedUtility(state, opponentStrategy);
        double[] utility = new double[2];

        if (state.getPlayerToMove().equals(follower)) {
            SequenceInformationSet informationSet = config.getInformationSetFor(state);
            Map<Sequence, double[]> isExpectedValues = expectedValues.get(informationSet);

            if (isExpectedValues == null) {
                isExpectedValues = new HashMap<>();
                expectedValues.put(informationSet, isExpectedValues);
                for (GameState gameState : informationSet.getAllStates()) {
                    for (GameState successor : getSuccessors(gameState)) {
                        double[] currentSequenceExpected = isExpectedValues.get(successor.getSequenceFor(follower));

                        if (currentSequenceExpected == null)
                            currentSequenceExpected = new double[2];
                        double[] successorUtility = computeBestResponseTo(successor, opponentStrategy);

                        for (int i = 0; i < currentSequenceExpected.length; i++) {
                            currentSequenceExpected[i] += successorUtility[i];
                        }
                        isExpectedValues.put(successor.getSequenceFor(follower), currentSequenceExpected);
                    }
                }
            }
            utility = max(isExpectedValues);
        } else {
            for (GameState successor : getSuccessors(state)) {
                if (opponentStrategy.get(state.getSequenceFor(leader)) > 1e-8) {
                    double[] successorUtility = computeBestResponseTo(successor, opponentStrategy);

                    for (int i = 0; i < utility.length; i++) {
                        utility[i] += successorUtility[i];
                    }
                }
            }
        }
        cache.put(state, utility);
        return utility;
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
        return currentMax[follower.getId()] > expectedValue[follower.getId()] ||
                (currentMax[follower.getId()] == expectedValue[follower.getId()] &&
                        currentMax[leader.getId()] > expectedValue[leader.getId()]);
    }

    private double[] getWeightedUtility(GameState state, Map<Sequence, Double> opponentStrategy) {
        double[] weightedUtility = new double[2];
        double[] utility = state.getUtilities();

        for (int i = 0; i < weightedUtility.length; i++) {
            weightedUtility[i] = utility[i] * state.getNatureProbability() * get(opponentStrategy, state.getSequenceFor(leader));
        }
        return weightedUtility;
    }

    private double get(Map<Sequence, Double> opponentStrategy, Sequence sequence) {
        Double value = opponentStrategy.get(sequence);

        return value == null ? 0 : value;
    }

    private List<GameState> getSuccessors(GameState state) {
        List<GameState> successors = this.successors.get(state);

        if (successors == null) {
            List<Action> actions = expander.getActions(state);
            successors = new ArrayList<>(actions.size());

            for (Action action : actions) {
                successors.add(state.performAction(action));
            }
        }
        return successors;
    }
}
