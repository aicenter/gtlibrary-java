package cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.memeff.fpira;

import cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.memeff.InformationSetKeyMap;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.br.ALossBestResponseAlgorithm;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.iinodes.PerfectRecallISKey;
import cz.agents.gtlibrary.interfaces.*;

import java.util.*;

public class FPIRADeltaCalculator extends ALossBestResponseAlgorithm {

    private static byte UTILITY_MULTIPLIER = 1;
    protected FPIRAStrategyDiffs strategyDiffs;
    private Map<ISKey, double[]> opponentAbstractedStrategy;
    private InformationSetKeyMap currentAbstractionISKeys;
    protected double prProbability;
    protected double irProbability;
    private Map<Action, Double> prProbCache;
    private Map<Action, Double> irProbCache;
    public int maxProbCacheSize = 0;

    public FPIRADeltaCalculator(GameState root, Expander<? extends InformationSet> expander, int searchingPlayerIndex,
                                AlgorithmConfig<? extends InformationSet> algConfig, GameInfo gameInfo, boolean stateCacheUse, InformationSetKeyMap currentAbstractionISKeys) {
        super(root, expander, searchingPlayerIndex, new Player[]{root.getAllPlayers()[0], root.getAllPlayers()[1]}, algConfig, gameInfo, stateCacheUse);
        this.currentAbstractionISKeys = currentAbstractionISKeys;
        prProbCache = new HashMap<>();
        irProbCache = new HashMap<>();
    }

    @Override
    protected Double calculateEvaluation(GameState gameState, double currentStateProbability) {
        double utRes = gameState.getUtilities()[searchingPlayerIndex] * gameState.getNatureProbability();

        return UTILITY_MULTIPLIER * (utRes * irProbability - utRes * prProbability);
//        return utRes * irProbability - utRes * prProbability;
    }

    protected Double bestResponse(GameState gameState, double lowerBound, double currentStateProb) {
        nodes++;
        Double returnValue = null;

        if (gameState.isGameEnd())// we are in a leaf
            return calculateEvaluation(gameState, currentStateProb);

        Double tmpVal = cachedValuesForNodes.get(gameState);
        if (tmpVal != null) {
            return tmpVal;
        }
        Player currentPlayer = gameState.getPlayerToMove();

        if (currentPlayer.equals(players[searchingPlayerIndex])) { // searching player to move
            Set<GameState> alternativeNodes = new LinkedHashSet<>();

            boolean nonZeroOppRP = currentStateProb > 0;
            InformationSet currentIS = algConfig.getInformationSetFor(gameState);

            alternativeNodes.add(gameState);
            if (currentIS != null) {
                alternativeNodes.addAll(currentIS.getAllStates());
                if (!alternativeNodes.contains(gameState)) {
                    alternativeNodes.add(gameState);
                }
                if (alternativeNodes.size() == 1 && nonZeroOppRP) {
                    alternativeNodes.addAll(getAlternativeNodesOutsideRG(gameState));
                }
            }

            assert (alternativeNodes.contains(gameState));
            BRSrchSelection sel = new FPIRADeltaCalculatorBRSrchSelection(lowerBound, Double.POSITIVE_INFINITY, nonZeroOppRP);

            List<Action> actionsToExplore = expander.getActions(gameState);

            for (GameState currentNode : alternativeNodes) {
                sel.setCurrentNode(currentNode);
                selectAction(currentNode, sel, actionsToExplore, 0);
                sel.abandonCurrentNode();
            }

            Action resultAction = sel.getResult().getLeft(); //selected action for the searching player

            for (GameState currentNode : alternativeNodes) { // storing the results based on the action
                if (sel.actionRealValues.get(currentNode) == null || sel.actionRealValues.get(currentNode).isEmpty()) {
                    assert false;
                    if (currentNode.equals(gameState)) {
                        returnValue = Double.NEGATIVE_INFINITY;
                    }
                    continue;
                }
                double v;
                if (resultAction == null) {
                    assert false;
                    v = Double.NEGATIVE_INFINITY;
                } else {
                    v = sel.actionRealValues.get(currentNode).get(resultAction);
                }

                cachedValuesForNodes.put(currentNode, v);
                if (currentNode.equals(gameState)) {
                    returnValue = v;
                }
            }
            assert (returnValue != null);
        } else { // nature player or the opponent is to move
            double nodeProbability = gameState.getNatureProbability();
            boolean nonZeroORP = false;

            if (currentStateProb > 0) {
                nodeProbability *= currentStateProb;
                nonZeroORP = true;
            }
            BROppSelection sel = new BROppSelection(lowerBound, nodeProbability, nonZeroORP);
            List<Action> actionsToExplore = expander.getActions(gameState);
            actionsToExplore = sel.sortActions(gameState, actionsToExplore);
            selectAction(gameState, sel, actionsToExplore, currentStateProb);
            returnValue = sel.getResult().getRight();
        }

        assert (returnValue != null);
        assert (returnValue <= MAX_UTILITY_VALUE * (1.01));
        return returnValue;
    }

    protected void handleState(BRActionSelection selection, Action action, GameState state, double parentProb) {
        Map<Action, GameState> successors = stateCache.computeIfAbsent(state, s -> USE_STATE_CACHE ? new HashMap<>(10000) : dummyInstance);
        GameState newState = successors.computeIfAbsent(action, a -> state.performAction(a));
        double natureProb = state.getNatureProbability();

        prProbability = getPRProbability(state, action);
        irProbability = getIRProbability(state, action);
        double value = Math.abs(irProbability) < 1e-8 && Math.abs(prProbability) < 1e-8 ? 0 : bestResponse(newState, Double.NEGATIVE_INFINITY, 1);

        selection.addValue(action, value, natureProb, 1e-3 * state.getNatureProbability());
    }

    protected double getIRProbability(GameState state, Action action) {
        double probability = 1;

        for (Action oldAction : state.getSequenceFor(players[opponentPlayerIndex])) {
            probability *= getProbability(oldAction.getInformationSet().getISKey(), oldAction, strategyDiffs.irStrategyDiff, irProbCache);
        }
        if (state.getPlayerToMove().equals(players[opponentPlayerIndex]))
            probability *= getProbability(state.getISKeyForPlayerToMove(), action, strategyDiffs.irStrategyDiff, irProbCache);
        return probability;
    }

    protected double getPRProbability(GameState state, Action action) {
        double probability = 1;

        for (Action oldAction : state.getSequenceFor(players[opponentPlayerIndex])) {
            probability *= getProbability(oldAction.getInformationSet().getISKey(), oldAction, strategyDiffs.prStrategyDiff, prProbCache);
        }
        if (state.getPlayerToMove().equals(players[opponentPlayerIndex]))
            probability *= getProbability(state.getISKeyForPlayerToMove(), action, strategyDiffs.prStrategyDiff, prProbCache);
        return probability;
    }

    protected double getProbability(ISKey key, Action action, Map<PerfectRecallISKey, double[]> strategyDiff) {
        List<Action> actions = expander.getActions(action.getInformationSet().getAllStates().stream().findAny().get());
        double[] diffForKey = strategyDiff.get(key);
        int actionIndex = getIndex(action, actions);
        double diffProbability = diffForKey == null ? 0 : diffForKey[actionIndex];

        return getProbabilityForActionIndex((PerfectRecallISKey) key, actionIndex, actions) + diffProbability;
    }

    protected double getProbability(ISKey key, Action action, Map<PerfectRecallISKey, double[]> strategyDiff, Map<Action, Double> cache) {
        return cache.computeIfAbsent(action, a -> getProbability(key, a, strategyDiff));
    }

    public double calculateDeltaForAbstractedStrategy(Map<ISKey, double[]> opponentAbstractedStrategy, FPIRAStrategyDiffs strategyDiffs) {
        this.opponentAbstractedStrategy = opponentAbstractedStrategy;
        this.strategyDiffs = strategyDiffs;
        prProbability = 1;
        irProbability = 1;
        UTILITY_MULTIPLIER = 1;
        Double value = calculateBR(gameTreeRoot, new HashMap<>());

        this.strategyDiffs = null;
        maxProbCacheSize = Math.max(maxProbCacheSize, Math.max(prProbCache.size(), irProbCache.size()));
        return value;
    }

    public double calculateNegativeDeltaForAbstractedStrategy(Map<ISKey, double[]> opponentAbstractedStrategy, FPIRAStrategyDiffs strategyDiffs) {
        this.opponentAbstractedStrategy = opponentAbstractedStrategy;
        this.strategyDiffs = strategyDiffs;
        prProbability = 1;
        irProbability = 1;
        UTILITY_MULTIPLIER = -1;
        return calculateBR(gameTreeRoot, new HashMap<>());
    }

    @Override
    protected double getOpponentProbability(Sequence sequence) {
        if (sequence.isEmpty())
            return 1;
        double probability = 1;

        for (Action action : sequence) {
            probability *= getProbabilityForAction(action);
        }
        return probability;
    }

    protected double getProbabilityForAction(Action action) {
        InformationSet informationSet = action.getInformationSet();
        List<Action> actions = expander.getActions(informationSet.getAllStates().stream().findAny().get());
        double[] realizations = opponentAbstractedStrategy.get(currentAbstractionISKeys.get((PerfectRecallISKey) informationSet.getISKey(), actions));


        for (int i = 0; i < actions.size(); i++) {
            if (actions.get(i).equals(action)) {
                return realizations[i];
            }
        }
        throw new UnsupportedOperationException("Action not found");
    }

    private double getProbabilityForActionIndex(PerfectRecallISKey isKey, int actionIndex, List<Action> actions) {
        double[] realizations = opponentAbstractedStrategy.get(currentAbstractionISKeys.get(isKey, actions));

        return realizations[actionIndex];
    }

    private int getIndex(Action action, List<Action> actions) {
        for (int i = 0; i < actions.size(); i++) {
            if (actions.get(i).equals(action)) {
                return i;
            }
        }
        throw new UnsupportedOperationException("Action not found");
    }

    public void clearProbabilityCache() {
        prProbCache.clear();
        irProbCache.clear();
    }

    private class FPIRADeltaCalculatorBRSrchSelection extends BRSrchSelection {
        public FPIRADeltaCalculatorBRSrchSelection(double lowerBound, double allNodesProbability, boolean nonZeroORP) {
            super(lowerBound, allNodesProbability, null, nonZeroORP);
        }

        @Override
        public void abandonCurrentNode() {
            this.currentNode = null;
            previousMaxValue = actionExpectedValues.get(maxAction);
        }

        @Override
        public double calculateNewBoundForAction(Action action, double natureProb, double orpProb) {
            throw new UnsupportedOperationException("Bound should not be used here");
        }
    }
}
