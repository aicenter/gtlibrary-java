package cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.br;

import cz.agents.gtlibrary.iinodes.ArrayListSequenceImpl;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.interfaces.*;

import java.util.*;

public class LimitedActionsALossBRAlgorithm extends ALossBestResponseAlgorithm {
    protected Map<Sequence, Set<Action>> possibleContinuations;

    public LimitedActionsALossBRAlgorithm(GameState root, Expander expander, int searchingPlayerIndex, Player[] actingPlayers,
                                          AlgorithmConfig<? extends InformationSet> algConfig, GameInfo gameInfo, Map<GameState, Map<Action, GameState>> stateCache) {
        super(root, expander, searchingPlayerIndex, actingPlayers, algConfig, gameInfo, false);
        this.stateCache = stateCache;
    }

    public Double calculateLimitedBR(GameState root, Map<Action, Double> opponentBehavioralStrategy, Map<Sequence, Set<Action>> possibleContinuations) {
        return calculateLimitedBR(root, opponentBehavioralStrategy, new HashMap<>(), possibleContinuations);
    }

    public Double calculateLimitedBR(GameState root, Map<Action, Double> opponentBehavioralStrategy,
                                     Map<Action, Double> myBehavioralStrategy, Map<Sequence, Set<Action>> possibleContinuations) {
        this.opponentBehavioralStrategy = opponentBehavioralStrategy;
        this.myBehavioralStrategy = myBehavioralStrategy;
        this.BRresult.clear();
        this.bestResponse.clear();
        this.cachedValuesForNodes.clear();
        this.firstLevelActions.clear();
        this.gameTreeRoot = root;
        this.possibleContinuations = possibleContinuations;

        comparator = new ORComparator();

        return bestResponse(root, -MAX_UTILITY_VALUE, getOpponentProbability(root.getSequenceFor(players[opponentPlayerIndex])));
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
            List<GameState> alternativeNodes = new ArrayList<>();

            boolean nonZeroOppRP = currentStateProb > 0;
            boolean nonZeroOppRPAlt = false;
            InformationSet currentIS = algConfig.getInformationSetFor(gameState);

            if (currentIS != null) {
                alternativeNodes.addAll(currentIS.getAllStates());
                if (!alternativeNodes.contains(gameState)) {
                    alternativeNodes.add(gameState);
                }
                if (alternativeNodes.size() == 1 && nonZeroOppRP) {
                    alternativeNodes.addAll(getAlternativeNodesOutsideRG(gameState));
                }
            } // if we do not have alternative nodes stored in the currentIS, there is no RP leading to these nodes --> we do not need to consider them
            else {
//                alternativeNodes.add(gameState);
                alternativeNodes.addAll(getAlternativeNodesOutsideRG(gameState));
            }

            assert (alternativeNodes.contains(gameState));
            HashMap<GameState, Double> alternativeNodesProbs = new HashMap<GameState, Double>();
            double ISProbability = 0;

            for (GameState currentNode : alternativeNodes) {
                double currentNodeProb = currentNode.getNatureProbability();

                if (nonZeroOppRP) {
                    double altProb = getOpponentProbability(currentNode.getSequenceFor(players[opponentPlayerIndex]));

                    currentNodeProb *= altProb;
                    if (altProb > 0)
                        nonZeroOppRPAlt = true;
                }
                ISProbability += currentNodeProb;
                alternativeNodesProbs.put(currentNode, currentNodeProb);
            }

            if (!nonZeroOppRP && !nonZeroOppRPAlt && ISProbability > gameState.getNatureProbability()) {
                // if there is zero OppRP prob we keep only those nodes in IS that are caused by the moves of nature
                // i.e., -> we keep all the nodes that share the same history of the opponent
                for (GameState state : new ArrayList<GameState>(alternativeNodes)) {
                    if (!state.getHistory().getSequenceOf(players[opponentPlayerIndex]).equals(gameState.getHistory().getSequenceOf(players[opponentPlayerIndex]))) {
                        alternativeNodes.remove(state);
                        alternativeNodesProbs.remove(state);
                    }
                }
            }
            new ArrayList<>(alternativeNodes).stream().filter(state -> !state.getSequenceForPlayerToMove().equals(gameState.getSequenceForPlayerToMove())).forEach(state -> {
                alternativeNodes.remove(state);
                alternativeNodesProbs.remove(state);
            });

            BRSrchSelection sel = new BRSrchSelection(lowerBound, ISProbability, alternativeNodesProbs, nonZeroOppRP);
            Collections.sort(alternativeNodes, comparator);

            Set<Action> possibleActions = possibleContinuations.get(gameState.getSequenceForPlayerToMove());
            List<Action> actions = expander.getActions(gameState);
            List<Action> actionsToExplore = new ArrayList<>(actions);
            Iterator<Action> actionIterator  = actionsToExplore.iterator();

            while(actionIterator.hasNext()) {
                Action action = actionIterator.next();

                if(possibleActions == null || !possibleActions.contains(action))
                    actionIterator.remove();
            }
            if (actionsToExplore.isEmpty())
                actionsToExplore.add(actions.get(0));
            actionsToExplore = sel.sortActions(gameState, actionsToExplore);

            for (GameState currentNode : alternativeNodes) {
                sel.setCurrentNode(currentNode);
                selectAction(currentNode, sel, actionsToExplore, alternativeNodesProbs.get(currentNode) / currentNode.getNatureProbability());
                sel.abandonCurrentNode();
                if (sel.allNodesProbability < EPS_CONSTANT) {
                    break;
                }
//                if ((sel.getResult().getRight() + sel.allNodesProbability * MAX_UTILITY_VALUE) < lowerBound) { //
//                    break;
//                }
                if (currentNode.equals(gameState)) {
                    if (Collections.max(sel.actionRealValues.get(currentNode).values()) < lowerBound)
                        break;
                }
            }

            Action resultAction = sel.getResult().getLeft(); //selected action for the searching player

            for (GameState currentNode : alternativeNodes) { // storing the results based on the action
                if (sel.actionRealValues.get(currentNode) == null || sel.actionRealValues.get(currentNode).isEmpty()) {
                    if (currentNode.equals(gameState)) {
//                        returnValue = -MAX_UTILITY_VALUE*alternativeNodesProbs.get(currentNode);
                        returnValue = Double.NEGATIVE_INFINITY;
                    }
                    continue;
                }
                double v;
                if (resultAction == null) {
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

//            if (returnValue == null) {
//                System.out.println();
//            }
            assert (returnValue != null);
            Sequence sequence = gameState.getSequenceFor(players[searchingPlayerIndex]);
            Sequence sequenceCopy = new ArrayListSequenceImpl(sequence);

            sequenceCopy.addLast(resultAction);
            if (sequence.isEmpty() || gameState.equals(gameTreeRoot)) {
                if (!firstLevelActions.containsKey(gameState.getISKeyForPlayerToMove()))
                    firstLevelActions.put(gameState.getISKeyForPlayerToMove(), sequenceCopy);
            } else {

//            Sequence resultSequence = new ArrayListSequenceImpl(currentHistory.get(players[searchingPlayerIndex]));
//            resultSequence.addLast(resultAction);

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
//            if (algConfig.getActualNonzeroUtilityValues(gameState) != null) {
//                returnValue = algConfig.getActualNonzeroUtilityValues(gameState);
//                if (nonZeroORP) {
//                    returnValue *= currentStateProb;
//                }
//                if (searchingPlayerIndex != 0) {
//                    returnValue *= -1;
//                }
//            } else {
            BROppSelection sel = new BROppSelection(lowerBound, nodeProbability, nonZeroORP);
            List<Action> actionsToExplore = expander.getActions(gameState);
            actionsToExplore = sel.sortActions(gameState, actionsToExplore);
            selectAction(gameState, sel, actionsToExplore, currentStateProb);
            returnValue = sel.getResult().getRight();
            if (nonZeroORP && !sel.nonZeroContinuation) {
                returnValue *= currentStateProb;
            }
//            }
        }

        assert (returnValue != null);
        assert (returnValue <= MAX_UTILITY_VALUE * (1.01));
        return returnValue;
    }

}
