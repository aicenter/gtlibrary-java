package cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions;

import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.br.ALossBestResponseAlgorithm;
import cz.agents.gtlibrary.iinodes.ArrayListSequenceImpl;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.interfaces.*;

import java.util.*;

public class DeltaCalculator extends ALossBestResponseAlgorithm {

    protected StrategyDiffs strategyDiffs;
    protected double prProbability;
    protected double irProbability;

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

        return Math.abs(utRes * irProbability - utRes * prProbability);
//        return utRes * irProbability - utRes * prProbability;
    }

    protected Double bestResponse(GameState gameState, double lowerBound, double currentStateProb) {

//        Map<Player, Sequence> currentHistory = new HashMap<Player, Sequence>();
//        currentHistory.put(players[searchingPlayerIndex], gameState.getSequenceFor(players[searchingPlayerIndex]));
//        currentHistory.put(players[opponentPlayerIndex], gameState.getSequenceFor(players[opponentPlayerIndex]));
        nodes++;
        Double returnValue = null;

        if (gameState.isGameEnd())// we are in a leaf
            return calculateEvaluation(gameState, currentStateProb);

        Double tmpVal = cachedValuesForNodes.get(gameState);
        if (tmpVal != null) { // we have already solved this node as a part of an evaluated information set
            //maybe we could remove the cached reward at this point? No in double-oracle -> we are using it in restricted game
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
                if (alternativeNodes.size() == 1 && !nonZeroOppRP) {
                    alternativeNodes.addAll(getAlternativeNodesOutsideRG(gameState));
                }
            } // if we do not have alternative nodes stored in the currentIS, there is no RP leading to these nodes --> we do not need to consider them

            assert (alternativeNodes.contains(gameState));
            HashMap<GameState, Double> alternativeNodesProbs = new HashMap<GameState, Double>();

            for (GameState currentNode : alternativeNodes) {
                double currentNodeProb = currentNode.getNatureProbability();

                if (nonZeroOppRP) {
                    double altProb = getOpponentProbability(currentNode.getSequenceFor(players[opponentPlayerIndex]));

                    currentNodeProb *= altProb;
                }
                alternativeNodesProbs.put(currentNode, currentNodeProb);
            }

//            if (!nonZeroOppRP && !nonZeroOppRPAlt && ISProbability > gameState.getNatureProbability()) {
//                // if there is zero OppRP prob we keep only those nodes in IS that are caused by the moves of nature
//                // i.e., -> we keep all the nodes that share the same history of the opponent
//                for (GameState state : new ArrayList<GameState>(alternativeNodes)) {
//                    if (!state.getHistory().getSequenceOf(players[opponentPlayerIndex]).equals(gameState.getHistory().getSequenceOf(players[opponentPlayerIndex]))) {
//                        alternativeNodes.remove(state);
//                        alternativeNodesProbs.remove(state);
//                    }
//                }
//            }
            new ArrayList<>(alternativeNodes).stream().filter(state -> !state.getSequenceForPlayerToMove().equals(gameState.getSequenceForPlayerToMove())).forEach(state -> {
                alternativeNodes.remove(state);
                alternativeNodesProbs.remove(state);
            });

            BRSrchSelection sel = new BRSrchSelection(lowerBound, Double.POSITIVE_INFINITY, alternativeNodesProbs, nonZeroOppRP);

            List<Action> actionsToExplore = expander.getActions(gameState);

            for (GameState currentNode : alternativeNodes) {
                sel.setCurrentNode(currentNode);
                selectAction(currentNode, sel, actionsToExplore, alternativeNodesProbs.get(currentNode) / currentNode.getNatureProbability());
                sel.abandonCurrentNode();
            }

            Action resultAction = sel.getResult().getLeft(); //selected action for the searching player

            for (GameState currentNode : alternativeNodes) { // storing the results based on the action
                if (sel.actionRealValues.get(currentNode) == null || sel.actionRealValues.get(currentNode).isEmpty()) {
                    assert false;
                    if (currentNode.equals(gameState)) {
//                        returnValue = -MAX_UTILITY_VALUE*alternativeNodesProbs.get(currentNode);
                        returnValue = Double.NEGATIVE_INFINITY;
                    }
                    continue;
                }
                double v;
                if (resultAction == null) {
                    assert false;
//                    v = -MAX_UTILITY_VALUE*alternativeNodesProbs.get(currentNode);
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
            Sequence sequence = gameState.getSequenceFor(players[searchingPlayerIndex]);
            Sequence sequenceCopy = new ArrayListSequenceImpl(sequence);

            sequenceCopy.addLast(resultAction);
            if (sequence.isEmpty() || gameState.equals(gameTreeRoot)) {
                if (!firstLevelActions.containsKey(gameState.getISKeyForPlayerToMove()))
                    firstLevelActions.put(gameState.getISKeyForPlayerToMove(), sequenceCopy);
            } else {
                Map<ISKey, Sequence> tmpActionMap = BRresult.getOrDefault(sequence, new HashMap<>());

                tmpActionMap.putIfAbsent(gameState.getISKeyForPlayerToMove(), sequenceCopy);
                BRresult.put(sequence, tmpActionMap);
            }
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
//            }
        }

        assert (returnValue != null);
        assert (returnValue <= MAX_UTILITY_VALUE * (1.01));
        return returnValue;
    }

    protected void handleState(BRActionSelection selection, Action action, GameState state) {
        Map<Action, GameState> successors = stateCache.computeIfAbsent(state, s -> USE_STATE_CACHE ? new HashMap<>(10000) : dummyInstance);
        GameState newState = successors.computeIfAbsent(action, a -> state.performAction(a));
        double natureProb = state.getNatureProbability();
//        double oppRP = getOpponentProbability(state.getSequenceFor(players[opponentPlayerIndex]));

        prProbability = getPRProbability(state, action);
        irProbability = getIRProbability(state, action);
//        if (newLowerBound <= MAX_UTILITY_VALUE) {
        double value = Math.abs(irProbability) < 1e-8 && Math.abs(prProbability) < 1e-8 ? 0 : bestResponse(newState, Double.NEGATIVE_INFINITY, 1);

        selection.addValue(action, value, natureProb, 1e-3 * state.getNatureProbability());
//        }
    }

    protected double getOpponentProbability(Sequence sequence) {
        return 1;
    }

    protected double getIRProbability(GameState state, Action action) {
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

    protected double getPRProbability(GameState state, Action action) {
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

    protected double getProbability(Sequence sequence, Action action, Map<Sequence, Map<Action, Double>> strategyDiff) {
        Map<Action, Double> diffForSequence = strategyDiff.get(sequence);
        double diffProbability = diffForSequence == null ? 0 : diffForSequence.getOrDefault(action, 0d);

        return opponentBehavioralStrategy.getOrDefault(action, 0d) + diffProbability;
    }
}
