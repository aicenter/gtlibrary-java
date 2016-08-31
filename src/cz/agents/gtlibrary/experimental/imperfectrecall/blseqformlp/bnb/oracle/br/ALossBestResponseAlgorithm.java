/*
Copyright 2014 Faculty of Electrical Engineering at CTU in Prague

This file is part of Game Theoretic Library.

Game Theoretic Library is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Game Theoretic Library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with Game Theoretic Library.  If not, see <http://www.gnu.org/licenses/>.*/


package cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.br;

import cz.agents.gtlibrary.iinodes.ArrayListSequenceImpl;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.FixedSizeMap;
import cz.agents.gtlibrary.utils.Pair;
import cz.agents.gtlibrary.utils.ValueComparator;

import java.util.*;

/**
 * Best-response algorithm with pruning. It calculates best-response value for a
 * game described by the root state and the expander.
 */
public class ALossBestResponseAlgorithm {

    public long nodes = 0;
    protected Expander expander;
    protected Map<GameState, Double> cachedValuesForNodes = new HashMap<>();
    protected Map<Action, Double> opponentBehavioralStartegy = new HashMap<>();
    protected Map<Action, Double> myBehavioralStrategy = new HashMap<>();
    protected HashMap<Action, HashSet<Action>> BRresult = new HashMap<>();
    protected Map<Action, Double> bestResponse = new HashMap<>();
    final protected int searchingPlayerIndex;
    final protected int opponentPlayerIndex;
    final protected Player[] players;
    final protected AlgorithmConfig<? extends InformationSet> algConfig;
    final protected GameInfo gameInfo;
    protected double MAX_UTILITY_VALUE;
    final protected double EPS_CONSTANT = 0.000000001; // zero for numerical-stability reasons
    protected ORComparator comparator;
    protected GameState gameTreeRoot = null;
    private Set<Action> resultActions = new HashSet<>();
    private Set<Action> firstLevelActions = new HashSet<>();

    public ALossBestResponseAlgorithm(Expander expander, int searchingPlayerIndex, Player[] actingPlayers, AlgorithmConfig<? extends InformationSet> algConfig, GameInfo gameInfo) {
        this.searchingPlayerIndex = searchingPlayerIndex;
        this.opponentPlayerIndex = (1 + searchingPlayerIndex) % 2;
        this.players = actingPlayers;
        assert players.length == 2;
        this.expander = expander;
        this.algConfig = algConfig;
        this.gameInfo = gameInfo;
        this.MAX_UTILITY_VALUE = gameInfo.getMaxUtility();
    }

    public Double calculateBR(GameState root, Map<Action, Double> opponentBehavioralStrategy) {
        return calculateBR(root, opponentBehavioralStrategy, new HashMap<>());
    }

    public Double calculateBR(GameState root, Map<Action, Double> opponentBehavioralStrategy, Map<Action, Double> myBehavioralStrategy) {

        nodes = 0;

        this.opponentBehavioralStartegy = opponentBehavioralStrategy;
        this.myBehavioralStrategy = myBehavioralStrategy;
        this.BRresult.clear();
        this.bestResponse.clear();
        this.cachedValuesForNodes.clear();
        this.firstLevelActions.clear();
        this.gameTreeRoot = root;

        comparator = new ORComparator();

        return bestResponse(root, -MAX_UTILITY_VALUE, 1);
    }

    protected Double calculateEvaluation(GameState gameState, double currentStateProbability) {
        double utRes;
        Double utility = algConfig.getActualNonzeroUtilityValues(gameState);

        if (utility != null) {
            utRes = utility;
        } else {
            utRes = gameState.getUtilities()[0] * gameState.getNatureProbability();
            if (utRes != 0)
                algConfig.setUtility(gameState, utRes);
        }
        if (searchingPlayerIndex == 1)
            utRes *= -1; // a zero sum game
        if (currentStateProbability == 0)
            currentStateProbability = 1d;
        return utRes * currentStateProbability; // weighting with opponent's realization plan
    }

    public Double calculateBRNoClear(GameState root) {
        return bestResponse(root, -MAX_UTILITY_VALUE, 1);
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
            //maybe we could remove the cached value at this point? No in double-oracle -> we are using it in restricted game
            return tmpVal;
        }
        Player currentPlayer = gameState.getPlayerToMove();

        if (currentPlayer.equals(players[searchingPlayerIndex])) { // searching player to move
            List<GameState> alternativeNodes = new ArrayList<GameState>();

            boolean nonZeroOppRP = currentStateProb > 0;
            boolean nonZeroOppRPAlt = false;
            InformationSet currentIS = algConfig.getInformationSetFor(gameState);

            if (currentIS != null) {
                alternativeNodes.addAll(currentIS.getAllStates());
                if (!alternativeNodes.contains(gameState)) {
                    alternativeNodes.add(gameState);
                }
                if (alternativeNodes.size() == 1 && !nonZeroOppRP) {
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

            List<Action> actionsToExplore = expander.getActions(gameState);
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
                if (sel.actionRealValues.get(currentNode) == null) {
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

            resultActions.add(resultAction);
            Sequence sequence = gameState.getSequenceFor(players[searchingPlayerIndex]);
            if(sequence.isEmpty()) {
                 firstLevelActions.add(resultAction);
            } else {
                Action previousAction = sequence.getLast();

//            Sequence resultSequence = new ArrayListSequenceImpl(currentHistory.get(players[searchingPlayerIndex]));
//            resultSequence.addLast(resultAction);

                HashSet<Action> tmpActionSet = BRresult.get(previousAction);
                if (tmpActionSet == null) {
                    tmpActionSet = new HashSet<>();
                }

                tmpActionSet.add(resultAction);
                BRresult.put(previousAction, tmpActionSet);
            }
        } else { // nature player or the opponent is to move
            double nodeProbability = gameState.getNatureProbability();
            boolean nonZeroORP = false;

            if (currentStateProb > 0) {
                nodeProbability *= currentStateProb;
                nonZeroORP = true;
            }
            if (algConfig.getActualNonzeroUtilityValues(gameState) != null) {
                returnValue = algConfig.getActualNonzeroUtilityValues(gameState);
                if (nonZeroORP) {
                    returnValue *= currentStateProb;
                }
                if (searchingPlayerIndex != 0) {
                    returnValue *= -1;
                }
            } else {
                BROppSelection sel = new BROppSelection(lowerBound, nodeProbability, nonZeroORP);
                List<Action> actionsToExplore = expander.getActions(gameState);
                actionsToExplore = sel.sortActions(gameState, actionsToExplore);
                selectAction(gameState, sel, actionsToExplore, currentStateProb);
                returnValue = sel.getResult().getRight();
                if (nonZeroORP && !sel.nonZeroContinuation) {
                    returnValue *= currentStateProb;
                }
            }
        }

        assert (returnValue != null);
        assert (returnValue <= MAX_UTILITY_VALUE * (1.01));
        return returnValue;
    }

    private double getOpponentProbability(Sequence sequence) {
        double probability = 1;

        for (Action action : sequence) {
            double currentProbability = opponentBehavioralStartegy.getOrDefault(action, 0d);

            if (currentProbability == 0)
                return 0;
            probability *= currentProbability;
        }
        return probability;
    }

    private double getMyProbability(Sequence sequence) {
        double probability = 1;

        for (Action action : sequence) {
            double currentProbability = myBehavioralStrategy.getOrDefault(action, 0d);

            if (currentProbability == 0)
                return 0;
            probability *= currentProbability;
        }
        return probability;
    }

    public void selectAction(GameState state, BRActionSelection selection, List<Action> actionsToExplore, double currentStateProb) {
//        List<Action> actionsToExplore = expander.getActions(state);
//        actionsToExplore = selection.sortActions(state, actionsToExplore);
        for (Action action : actionsToExplore) {
            GameState newState = state.performAction(action);
            double natureProb = newState.getNatureProbability(); // TODO extract these probabilities from selection Map
            double oppRP = getOpponentProbability(newState.getSequenceFor(players[opponentPlayerIndex]));
            double newLowerBound = selection.calculateNewBoundForAction(action, natureProb, oppRP);

            if (newLowerBound <= MAX_UTILITY_VALUE) {
                double value = bestResponse(newState, newLowerBound, oppRP);
                selection.addValue(action, value, natureProb, oppRP);
            }
        }
    }

    public Map<Action, Double> getOpponentBehavioralStartegy() {
        return opponentBehavioralStartegy;
    }

    public abstract class BRActionSelection {

        protected double lowerBound;

        public abstract void addValue(Action action, double value, double natureProb, double orpProb);

        public abstract Pair<Action, Double> getResult();

        public BRActionSelection(double lowerBound) {
            this.lowerBound = lowerBound;
        }

        public abstract List<Action> sortActions(GameState state, List<Action> actions);

        public abstract double calculateNewBoundForAction(Action action, double natureProb, double orpProb);
    }

    public class BROppSelection extends BRActionSelection {

        protected double nodeProbability;
        protected double value = 0;
        protected boolean nonZeroORP;
        public boolean nonZeroContinuation = false;
        protected Double tempValue = null;

        public BROppSelection(double lowerBound, double nodeProbability, boolean nonZeroORP) {
            super(lowerBound);
            this.nodeProbability = nodeProbability;
            this.nonZeroORP = nonZeroORP;
        }

        @Override
        public void addValue(Action action, double value, double natureProb, double orpProb) {
            double probability = natureProb;
            if (tempValue == null) {
                tempValue = value;
            }
            if (nonZeroORP) {
                if (orpProb == 0) {
                    return;
                }
//				nonZeroContinuation = true;
                probability *= orpProb;
            }
            if (this.nodeProbability > 0) {
                this.nodeProbability -= probability;
                this.value += value;
            }
        }

        @Override
        public Pair<Action, Double> getResult() {
            if (nonZeroORP && !nonZeroContinuation) {
                return new Pair<Action, Double>(null, tempValue);
            } else {
                return new Pair<Action, Double>(null, value);
            }
        }

        @Override
        public double calculateNewBoundForAction(Action action, double natureProb, double orpProb) {
            double probability = natureProb;
            if (nonZeroORP) {
                probability *= orpProb;
                if (orpProb == 0) {
                    if (tempValue == null) {
                        return -MAX_UTILITY_VALUE;
                    } else {
                        return Double.POSITIVE_INFINITY;
                    }
                }
            }
            if (nodeProbability < EPS_CONSTANT) {
                if (tempValue == null) {
                    return -MAX_UTILITY_VALUE;
                } else {
                    return Double.POSITIVE_INFINITY;
                }
            }
            return (lowerBound - (value + (nodeProbability - probability) * MAX_UTILITY_VALUE));
        }

        @Override
        public List<Action> sortActions(GameState state, List<Action> actions) {
            List<Action> result = new ArrayList<Action>();
            if (state.isPlayerToMoveNature()) {
                nonZeroContinuation = nonZeroORP;
                // sort according to the nature probability
                Map<Action, Double> actionMap = new FixedSizeMap<Action, Double>(actions.size());
                for (Action a : actions) {
                    actionMap.put(a, state.getProbabilityOfNatureFor(a)); // the standard way is to sort ascending; hence, we store negative probability
                }
                ValueComparator<Action> comp = new ValueComparator<Action>(actionMap);
                TreeMap<Action, Double> sortedMap = new TreeMap<Action, Double>(comp);
                sortedMap.putAll(actionMap);
                result.addAll(sortedMap.keySet());
            } else {
                // sort according to the opponent realizaiton plan
                Sequence currentSequence = state.getSequenceFor(players[opponentPlayerIndex]);
                Map<Action, Double> sequenceMap = new FixedSizeMap<Action, Double>(actions.size());
                for (Action a : actions) {
                    Sequence newSeq = new ArrayListSequenceImpl(currentSequence);
                    newSeq.addLast(a);
                    Double prob = getOpponentProbability(newSeq);
                    if (prob == null) {
                        prob = 0d;
                    }
                    if (prob > 0) {
                        nonZeroContinuation = true;
                    }
                    sequenceMap.put(a, prob); // the standard way is to sort ascending; hence, we store negative probability
                }
                if (!nonZeroContinuation) {
                    return actions;
                }
                ValueComparator<Action> comp = new ValueComparator<Action>(sequenceMap);
                TreeMap<Action, Double> sortedMap = new TreeMap<Action, Double>(comp);
                sortedMap.putAll(sequenceMap);
                result.addAll(sortedMap.keySet());
            }
            return result;
        }
    }

    public class BRSrchSelection extends BRActionSelection {

        public double allNodesProbability;
        protected HashMap<Action, Double> actionExpectedValues = new HashMap<Action, Double>();
        public HashMap<GameState, HashMap<Action, Double>> actionRealValues = new HashMap<GameState, HashMap<Action, Double>>();
        protected double maxValue = Double.NEGATIVE_INFINITY;
        protected double previousMaxValue = Double.NEGATIVE_INFINITY;
        protected Action maxAction = null;
        protected GameState currentNode = null;
        protected HashMap<GameState, Double> alternativeNodesProbs = null;
        protected boolean nonZeroORP;

        public BRSrchSelection(double lowerBound, double allNodesProbability, HashMap<GameState, Double> alternativeNodesProbs, boolean nonZeroORP) {
            super(lowerBound);
            this.allNodesProbability = allNodesProbability;
            this.alternativeNodesProbs = alternativeNodesProbs;
            this.nonZeroORP = nonZeroORP;
        }

        public void setCurrentNode(GameState currentNode) {
//			allNodesProbability -= nodeProbability;
            this.currentNode = currentNode;
            actionRealValues.put(currentNode, new HashMap<Action, Double>());
            maxValue = Double.NEGATIVE_INFINITY;
        }

        public void abandonCurrentNode() {
            allNodesProbability -= alternativeNodesProbs.get(currentNode);
            this.currentNode = null;
            previousMaxValue = actionExpectedValues.get(maxAction);
        }

        @Override
        public void addValue(Action action, double value, double natureProb, double orpProb) {
            assert (currentNode != null);

            HashMap<Action, Double> currentNodeActionValues = actionRealValues.get(currentNode);
            assert (currentNodeActionValues != null);
            assert (!currentNodeActionValues.containsKey(action));
            currentNodeActionValues.put(action, value);

            if (orpProb > 0 || !nonZeroORP) {
                Double currValue = actionExpectedValues.get(action);
                if (currValue == null) {
                    currValue = 0d;
                }
                currValue += value;
                actionExpectedValues.put(action, currValue);

                if (currValue > maxValue) {
                    maxValue = currValue;
                    maxAction = action;
                }
            }
        }

        @Override
        public Pair<Action, Double> getResult() {
            return new Pair<Action, Double>(maxAction, actionExpectedValues.get(maxAction));
        }

        @Override
        public double calculateNewBoundForAction(Action action, double natureProb, double orpProb) {
            if (nonZeroORP && orpProb <= 0) {
                return Double.POSITIVE_INFINITY;
            }
            if (previousMaxValue == Double.NEGATIVE_INFINITY) {
                return Double.NEGATIVE_INFINITY;
            } else {
                if (this.allNodesProbability < EPS_CONSTANT) {
                    return Double.POSITIVE_INFINITY;
                } else {
//					double probability = natureProb;
//					if (nonZeroORP) probability *= orpProb;
                    return ((previousMaxValue + this.allNodesProbability * (-MAX_UTILITY_VALUE))
                            - (actionExpectedValues.get(action) + (this.allNodesProbability - alternativeNodesProbs.get(currentNode)) * MAX_UTILITY_VALUE));
                }
            }
        }

        @Override
        public List<Action> sortActions(GameState state, List<Action> actions) {
            if (myBehavioralStrategy.size() == 0) {
                return actions;
            }

            List<Action> result = new ArrayList<Action>();
            // sort according to my old realizaiton plan
            Sequence currentSequence = state.getSequenceFor(players[searchingPlayerIndex]);
            Map<Action, Double> sequenceMap = new FixedSizeMap<Action, Double>(actions.size());
            boolean hasPositiveProb = false;
            for (Action a : actions) {
                Sequence newSeq = new ArrayListSequenceImpl(currentSequence);
                newSeq.addLast(a);
                Double prob = getMyProbability(newSeq);
                if (prob == null) {
                    prob = 0d;
                }
                if (prob > 0) {
                    hasPositiveProb = true;
                }
                sequenceMap.put(a, prob); // the standard way is to sort ascending; hence, we store negative probability
            }
            if (!hasPositiveProb) {
                return actions; // if
            }
            ValueComparator<Action> comp = new ValueComparator<Action>(sequenceMap);
            TreeMap<Action, Double> sortedMap = new TreeMap<Action, Double>(comp);
            sortedMap.putAll(sequenceMap);
            result.addAll(sortedMap.keySet());
            return result;
        }
    }

    public Map<Action, Double> getBestResponse() {
        if (BRresult == null) {
            return null;
        }
        if (bestResponse.size() != 0) {
            return bestResponse;
        }
        Map<Action, Double> result = new HashMap<>();
        Queue<Action> queue = new ArrayDeque<>();

        queue.addAll(firstLevelActions);
        firstLevelActions.forEach(a -> result.put(a, 1d));
        while (queue.size() > 0) {
            Action action = queue.poll();
            Set<Action> res = BRresult.get(action);
            if (res != null) {
                res.stream().forEach(a -> result.put(a, 1d));
                queue.addAll(res);
            }
        }

        bestResponse = result;
        return result;
    }

    public int getSearchingPlayerIndex() {
        return searchingPlayerIndex;
    }

    public int getOpponentPlayerIndex() {
        return opponentPlayerIndex;
    }

    public class ORComparator implements Comparator<GameState> {

        public ORComparator() {
        }

        @Override
        public int compare(GameState arg0, GameState arg1) {
            Double or0 = getOpponentProbability(arg0.getSequenceFor(players[opponentPlayerIndex])) * arg0.getNatureProbability();
            Double or1 = getOpponentProbability(arg1.getSequenceFor(players[opponentPlayerIndex])) * arg1.getNatureProbability();
            if (or0 < or1) {
                return 1;
            }
            if (or0 > or1) {
                return -1;
            }
            return 0;
        }
    }

    public Double getCachedValueForState(GameState state) {
        return cachedValuesForNodes.get(state);
    }

//    public Strategy getBRStategy() {
//        Strategy out = new FirstActionStrategyForMissingSequences();
//        out.put(new ArrayListSequenceImpl(players[searchingPlayerIndex]), 1.0);
//        for (HashSet<Sequence> col : BRresult.values()) {
//            for (Sequence seq : col) {
//                out.put(seq, 1.0);
//            }
//        }
//        return out;
//    }

    public List<GameState> getAlternativeNodesOutsideRG(GameState state) {
        List<GameState> alternativeNodes = new ArrayList<GameState>();
        Stack<GameState> queue = new Stack<GameState>();
        queue.add(gameTreeRoot);

        Player mainPlayer = state.getPlayerToMove();
        int length = state.getHistory().getLength();
        boolean neverCheckOppAgain = false;

        while (!queue.isEmpty()) {
            GameState currentState = queue.pop();
            if (currentState.getHistory().getLength() == length) {
                if (currentState.getISKeyForPlayerToMove().equals(state.getISKeyForPlayerToMove()) && !currentState.equals(state) && notVisitedDueToOpponent(currentState)) {
                    alternativeNodes.add(currentState);
                }
                continue;
            }

            if (currentState.isGameEnd())
                continue;

            if (currentState.isPlayerToMoveNature()) {
                List<Action> tmp = expander.getActions(currentState);
                for (Action a : tmp) {
                    GameState newState = currentState.performAction(a);
                    if (newState != null) {
                        queue.push(newState);
                    }
                }
            } else if (!currentState.getPlayerToMove().equals(mainPlayer)) {
                List<Action> tmp = expander.getActions(currentState);
                boolean noUpdate = true;
                if (!neverCheckOppAgain && !notVisitedDueToOpponent(currentState)) {
                    Sequence s = new ArrayListSequenceImpl(currentState.getSequenceForPlayerToMove());
                    for (Action a : tmp) {
                        s.addLast(a);
                        double d = getOpponentProbability(s);
                        if (d > 0) {
                            GameState newState = currentState.performAction(a);
                            if (newState != null) {
                                queue.push(newState);
                                noUpdate = false;
                            }
                        }
                        s.removeLast();
                    }
                    if (noUpdate && !tmp.isEmpty()) {
                        GameState newState = currentState.performAction(tmp.get(0));
                        if (newState != null) {
                            queue.push(newState);
                        }
                    }
                } else {
//                    neverCheckOppAgain = true;
                    GameState newState = currentState.performAction(tmp.get(0));
                    if (newState != null) {
                        queue.push(newState);
                    }
                }
            } else {
                Player toMove = currentState.getPlayerToMove();
                int whichAction = currentState.getSequenceFor(toMove).size();
                if (whichAction < state.getSequenceFor(toMove).size()) {
                    Action actionToExecute = state.getSequenceFor(toMove).get(whichAction);
                    if (currentState.checkConsistency(actionToExecute)) {
                        GameState newState = currentState.performAction(actionToExecute);
                        queue.push(newState);
                    }
                }
            }

        }
        return alternativeNodes;
    }

    private boolean notVisitedDueToOpponent(GameState currentState) {
        return Math.abs(getOpponentProbability(currentState.getSequenceFor(players[opponentPlayerIndex]))) < 1e-8;
    }

}