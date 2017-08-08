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


package cz.agents.gtlibrary.algorithms.sequenceform;

import cz.agents.gtlibrary.iinodes.ArrayListSequenceImpl;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.strategy.FirstActionStrategyForMissingSequences;
import cz.agents.gtlibrary.strategy.Strategy;
import cz.agents.gtlibrary.utils.FixedSizeMap;
import cz.agents.gtlibrary.utils.Pair;
import cz.agents.gtlibrary.utils.ValueComparator;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Best-response algorithm with pruning. It calculates the best-response value for a
 * game described by the root state and the expander.
 */
public class SQFBestResponseAlgorithm {

    private static final boolean USE_FIX = false;
    public long nodes = 0;
    protected Expander<? extends InformationSet> expander;
    protected Map<GameState, Double> cachedValuesForNodes = new HashMap<GameState, Double>();
    protected Map<Sequence, Double> opponentRealizationPlan = new HashMap<Sequence, Double>();
    protected Map<Sequence, Double> myRealizationPlan = new HashMap<Sequence, Double>();
    protected HashMap<Sequence, HashSet<Sequence>> BRresult = new HashMap<Sequence, HashSet<Sequence>>();
    protected HashSet<Sequence> bestResponseSequences = new HashSet<Sequence>();
    final protected int searchingPlayerIndex;
    final protected int opponentPlayerIndex;
    final protected Player[] players;
    final protected AlgorithmConfig<? extends InformationSet> algConfig;
    final protected GameInfo gameInfo;
    protected double MAX_UTILITY_VALUE;
    final protected double EPS_CONSTANT = 0.000000001; // zero for numerical-stability reasons 
    protected ORComparator comparator;
    protected GameState gameTreeRoot = null;

    public static boolean useOriginalBRFormulation = false;

    public int maxBRResultSize = 0;
    public int maxStateCacheSize = 0;

    public SQFBestResponseAlgorithm(Expander expander, int searchingPlayerIndex, Player[] actingPlayers, AlgorithmConfig<? extends InformationSet> algConfig, GameInfo gameInfo) {
        this.searchingPlayerIndex = searchingPlayerIndex;
        this.opponentPlayerIndex = (1 + searchingPlayerIndex) % 2;
        this.players = actingPlayers;
        assert players.length == 2;
        this.expander = expander;
        this.algConfig = algConfig;
        this.gameInfo = gameInfo;
        this.MAX_UTILITY_VALUE = gameInfo.getMaxUtility();
    }

    public Double calculateBR(GameState root, Map<Sequence, Double> opponentRealizationPlan) {
        return calculateBR(root, opponentRealizationPlan, new HashMap<Sequence, Double>());
    }

    public Double calculateBR(GameState root, Map<Sequence, Double> opponentRealizationPlan, Map<Sequence, Double> myRP) {

        nodes = 0;

        this.opponentRealizationPlan = opponentRealizationPlan;
        this.myRealizationPlan = myRP;
        this.BRresult.clear();
        this.bestResponseSequences.clear();
        this.cachedValuesForNodes.clear();
        this.gameTreeRoot = root;

        comparator = new ORComparator(opponentRealizationPlan);

        double value = bestResponse(root, -MAX_UTILITY_VALUE);

        maxBRResultSize = (int) Math.max(maxBRResultSize, BRresult.values().stream().flatMap(s -> s.stream()).count());
        maxStateCacheSize = Math.max(maxStateCacheSize, cachedValuesForNodes.size());
        return value;
    }

    protected Double calculateEvaluation(Map<Player, Sequence> currentHistory, GameState gameState) {
        double utRes = 0;
        if (algConfig.getActualNonzeroUtilityValues(gameState) != null) {
            utRes = algConfig.getActualNonzeroUtilityValues(gameState);
        } else {
            utRes = gameState.getUtilities()[0] * gameState.getNatureProbability();
            if (utRes != 0) {
                algConfig.setUtility(gameState, utRes);
            }
        }
        if (searchingPlayerIndex == 1) {
            utRes *= -1; // a zero sum game
        }
        Double weight = getOpponentRealizationPlan().get(currentHistory.get(players[opponentPlayerIndex]));
        if (weight == null || weight == 0) {
            weight = 1d;
        }
        return utRes * weight; // weighting with opponent's realization plan
    }

    public Double calculateBRNoClear(GameState root) {
        double value = bestResponse(root, -MAX_UTILITY_VALUE);

        maxBRResultSize = (int) Math.max(maxBRResultSize, BRresult.values().stream().flatMap(s -> s.stream()).count());
        maxStateCacheSize = Math.max(maxStateCacheSize, cachedValuesForNodes.size());
        return value;
    }

    protected Double bestResponse(GameState gameState, double lowerBound) {

        Map<Player, Sequence> currentHistory = new HashMap<Player, Sequence>();
        currentHistory.put(players[searchingPlayerIndex], gameState.getSequenceFor(players[searchingPlayerIndex]));
        currentHistory.put(players[opponentPlayerIndex], gameState.getSequenceFor(players[opponentPlayerIndex]));

        nodes++;
        Double returnValue = null;

        if (gameState.isGameEnd()) { // we are in a leaf
            return calculateEvaluation(currentHistory, gameState);
        }

        Double tmpVal = cachedValuesForNodes.get(gameState);
        if (tmpVal != null) { // we have already solved this node as a part of an evaluated information set
            //maybe we could remove the cached value at this point? No in double-oracle -> we are using it in restricted game
            return tmpVal;
        }

        Player currentPlayer = gameState.getPlayerToMove();


        if (currentPlayer.equals(players[searchingPlayerIndex])) { // searching player to move
            List<GameState> alternativeNodes = new ArrayList<GameState>();

            boolean nonZeroOppRP = (opponentRealizationPlan.get(gameState.getSequenceFor(players[opponentPlayerIndex])) != null && opponentRealizationPlan.get(gameState.getSequenceFor(players[opponentPlayerIndex])) > 0);
            boolean nonZeroOppRPAlt = false;

            InformationSet currentIS = algConfig.getInformationSetFor(gameState);
            if (currentIS != null) {
                alternativeNodes.addAll(currentIS.getAllStates());

                if (!alternativeNodes.contains(gameState)) {
                    alternativeNodes.add(gameState);
                }

                if (alternativeNodes.size() == 1 && ((!useOriginalBRFormulation || !nonZeroOppRP) && (useOriginalBRFormulation || nonZeroOppRP))) {
                    alternativeNodes.addAll(getAlternativeNodesOutsideRG(gameState));
                }
            } // if we do not have alternative nodes stored in the currentIS, there is no RP leading to these nodes --> we do not need to consider them
            else {
                alternativeNodes.addAll(getAlternativeNodesOutsideRG(gameState));
            }


            assert (alternativeNodes.contains(gameState));
            HashMap<GameState, Double> alternativeNodesProbs = new HashMap<GameState, Double>();

            double ISProbability = 0;

            for (GameState currentNode : alternativeNodes) {
                double currentNodeProb = currentNode.getNatureProbability();
                if (nonZeroOppRP) {
                    if (getOpponentRealizationPlan().containsKey(currentNode.getHistory().getSequenceOf(players[opponentPlayerIndex]))) {
                        double altProb = getOpponentRealizationPlan().get(currentNode.getHistory().getSequenceOf(players[opponentPlayerIndex]));
                        currentNodeProb *= altProb;
                        if (altProb > 0) {
                            nonZeroOppRPAlt = true;
                        }
                    } else {
                        currentNodeProb = 0;
                    }
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
            new ArrayList<GameState>(alternativeNodes).stream().filter(state -> !state.getSequenceForPlayerToMove().equals(gameState.getSequenceForPlayerToMove())).forEach(state -> {
                alternativeNodes.remove(state);
                alternativeNodesProbs.remove(state);
            });

            BRSrchSelection sel = new BRSrchSelection(lowerBound, ISProbability, alternativeNodesProbs, nonZeroOppRP);
            Collections.sort(alternativeNodes, comparator);

            List<Action> actionsToExplore = expander.getActions(gameState);
            actionsToExplore = sel.sortActions(gameState, actionsToExplore);

            for (GameState currentNode : alternativeNodes) {
                sel.setCurrentNode(currentNode);
                selectAction(currentNode, sel, actionsToExplore);
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

            Sequence resultSequence = new ArrayListSequenceImpl(currentHistory.get(players[searchingPlayerIndex]));
            resultSequence.addLast(resultAction);


            HashSet<Sequence> tmpBRSet = BRresult.get(currentHistory.get(players[searchingPlayerIndex]));
            if (tmpBRSet == null) {
                tmpBRSet = new HashSet<Sequence>();
            }

            tmpBRSet.add(resultSequence);
            BRresult.put(currentHistory.get(players[searchingPlayerIndex]), tmpBRSet);

        } else { // nature player or the opponent is to move
            double nodeProbability = gameState.getNatureProbability();
            boolean nonZeroORP = false;
            Double currentOppRealizationPlan = getOpponentRealizationPlan().get(currentHistory.get(players[opponentPlayerIndex]));
            if (currentOppRealizationPlan != null && currentOppRealizationPlan > 0) {
                nodeProbability *= currentOppRealizationPlan;
                nonZeroORP = true;
            }
            if (algConfig.getActualNonzeroUtilityValues(gameState) != null) {
                returnValue = algConfig.getActualNonzeroUtilityValues(gameState);
                if (nonZeroORP) {
                    returnValue *= currentOppRealizationPlan;
                }
                if (searchingPlayerIndex != 0) {
                    returnValue *= -1;
                }
            } else {
                BROppSelection sel = new BROppSelection(lowerBound, nodeProbability, nonZeroORP);
                List<Action> actionsToExplore = expander.getActions(gameState);
                actionsToExplore = sel.sortActions(gameState, actionsToExplore);
                selectAction(gameState, sel, actionsToExplore);
                returnValue = sel.getResult().getRight();
                if (nonZeroORP && !sel.nonZeroContinuation) {
                    returnValue *= currentOppRealizationPlan;
                }
            }
        }

        assert (returnValue != null);
        assert (returnValue <= MAX_UTILITY_VALUE * (1.01));
        return returnValue;
    }

    public void selectAction(GameState state, BRActionSelection selection, List<Action> actionsToExplore) {
//        List<Action> actionsToExplore = expander.getActions(state);
//        actionsToExplore = selection.sortActions(state, actionsToExplore);

        Double rP = getOpponentRealizationPlan().get(state.getHistory().getSequenceOf(players[opponentPlayerIndex]));
        if (rP == null) {
            rP = 0d;
        }

        double stateProb = rP*state.getNatureProbability();

        for (Action act : actionsToExplore) {
            Action action = act;

            GameState newState = state.performAction(action);

            double natureProb = newState.getNatureProbability(); // TODO extract these probabilities from selection Map
            Double oppRP = getOpponentRealizationPlan().get(newState.getHistory().getSequenceOf(players[opponentPlayerIndex]));
            if (oppRP == null) {
                oppRP = 0d;
            }

            if (state.getPlayerToMove().getId() != searchingPlayerIndex) stateProb = natureProb*oppRP;

            double newLowerBound = selection.calculateNewBoundForAction(action, natureProb, oppRP, state);

            // nasobeni stateProb tady nebyvalo
            if (newLowerBound <= stateProb * MAX_UTILITY_VALUE) {
                double value = bestResponse(newState, newLowerBound) ;
                selection.addValue(action, value, natureProb, oppRP);
            }
        }
    }

    public Map<Sequence, Double> getOpponentRealizationPlan() {
        return opponentRealizationPlan;
    }

    public abstract class BRActionSelection {

        protected double lowerBound;

        public abstract void addValue(Action action, double value, double natureProb, double orpProb);

        public abstract Pair<Action, Double> getResult();

        public BRActionSelection(double lowerBound) {
            this.lowerBound = lowerBound;
        }

        public abstract List<Action> sortActions(GameState state, List<Action> actions);

        public abstract double calculateNewBoundForAction(Action action, double natureProb, double orpProb, GameState state);
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
        public double calculateNewBoundForAction(Action action, double natureProb, double orpProb, GameState state) {
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
                    Double prob = getOpponentRealizationPlan().get(newSeq);
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
        public double calculateNewBoundForAction(Action action, double natureProb, double orpProb, GameState state) {
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
            if (myRealizationPlan.size() == 0) {
                return actions;
            }

            List<Action> result = new ArrayList<Action>();
            // sort according to my old realization plan
            Sequence currentSequence = state.getSequenceFor(players[searchingPlayerIndex]);
            Map<Action, Double> sequenceMap = new FixedSizeMap<Action, Double>(actions.size());
            boolean hasPositiveProb = false;
            for (Action a : actions) {
                Sequence newSeq = new ArrayListSequenceImpl(currentSequence);
                newSeq.addLast(a);
                Double prob = myRealizationPlan.get(newSeq);
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

    public HashSet<Sequence> getBRSequences() {
        if (BRresult == null) {
            return null;
        }
        if (bestResponseSequences.size() != 0) {
            return bestResponseSequences;
        }
        HashSet<Sequence> result = new HashSet<Sequence>();
        LinkedList<Sequence> queue = new LinkedList<Sequence>();
        queue.add(new ArrayListSequenceImpl(this.players[searchingPlayerIndex]));
        result.addAll(queue);

        while (queue.size() > 0) {
            Sequence s = queue.removeFirst();
            Set<Sequence> res = BRresult.get(s);
            if (res != null) {
                result.addAll(res);
                queue.addAll(res);
            }
        }

        bestResponseSequences = result;
        return result;
    }

    public HashSet<Sequence> getFullBRSequences() {
        HashSet<Sequence> result = new HashSet<Sequence>();
        Set<Sequence> keysInBR = BRresult.keySet();

        for (Sequence c : getBRSequences()) {
            if (!keysInBR.contains(c)) {
                result.add(c);
            }
        }

        return result;
    }

    public int getSearchingPlayerIndex() {
        return searchingPlayerIndex;
    }

    public int getOpponentPlayerIndex() {
        return opponentPlayerIndex;
    }

    public class ORComparator implements Comparator<GameState> {

        final protected Map<Sequence, Double> probability;

        public ORComparator(Map<Sequence, Double> prob) {
            this.probability = prob;
        }

        @Override
        public int compare(GameState arg0, GameState arg1) {
            Double or0 = (probability.get(arg0.getHistory().getSequenceOf(players[opponentPlayerIndex])) == null) ? Double.NEGATIVE_INFINITY : probability.get(arg0.getHistory().getSequenceOf(players[opponentPlayerIndex])) * arg0.getNatureProbability();
            Double or1 = (probability.get(arg1.getHistory().getSequenceOf(players[opponentPlayerIndex])) == null) ? Double.NEGATIVE_INFINITY : probability.get(arg1.getHistory().getSequenceOf(players[opponentPlayerIndex])) * arg1.getNatureProbability();
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

    public Strategy getBRStategy() {
        Strategy out = new FirstActionStrategyForMissingSequences();
        out.put(new ArrayListSequenceImpl(players[searchingPlayerIndex]), 1.0);
        for (HashSet<Sequence> col : BRresult.values()) {
            for (Sequence seq : col) {
                out.put(seq, 1.0);
            }
        }
        return out;
    }

    public List<GameState> getAlternativeNodesOutsideRGFix(GameState state) {
        List<GameState> alternativeNodes = new ArrayList<>();

        getAlternativeNodesOutsideRGFix(gameTreeRoot, state, alternativeNodes, 1);
        return alternativeNodes;
    }

    private void getAlternativeNodesOutsideRGFix(GameState currentState, GameState stateForAlternatives, List<GameState> alternativeNodes, double opponentProbability) {
        if (currentState.isGameEnd())
            return;
        if (currentState.getPlayerToMove().getId() == searchingPlayerIndex) {
            int actionIndex = currentState.getSequenceForPlayerToMove().size();
            int desiredSeqLength = stateForAlternatives.getSequenceFor(currentState.getPlayerToMove()).size();

            if (actionIndex == desiredSeqLength && stateForAlternatives.getISKeyForPlayerToMove().equals(currentState.getISKeyForPlayerToMove())) {
                alternativeNodes.add(currentState);
                return;
            }
            if (actionIndex >= desiredSeqLength)
                return;
            Action actionToPlay = stateForAlternatives.getSequenceFor(currentState.getPlayerToMove()).get(actionIndex);

            if (currentState.checkConsistency(actionToPlay))
                getAlternativeNodesOutsideRGFix(currentState.performAction(actionToPlay), stateForAlternatives, alternativeNodes, opponentProbability);
            return;
        }
        if (currentState.getPlayerToMove().getId() == opponentPlayerIndex) {
            List<Action> actions = expander.getActions(currentState);
            List<GameState> nonZeroChildren = actions.stream().filter(a -> {
                Sequence seqCopy = new ArrayListSequenceImpl(currentState.getSequenceForPlayerToMove());

                seqCopy.addLast(a);
                return opponentRealizationPlan.getOrDefault(seqCopy, 0d) > 1e-8;
            }).map(a -> currentState.performAction(a)).collect(Collectors.toList());

            assert opponentProbability > 1e-8;
            if (nonZeroChildren.isEmpty()) { //default strategy case
                getAlternativeNodesOutsideRGDefaultFix(currentState.performAction(actions.get(0)), stateForAlternatives,
                        alternativeNodes, 1);
            } else {
                nonZeroChildren.forEach(s ->
                        getAlternativeNodesOutsideRGFix(s, stateForAlternatives,
                                alternativeNodes, opponentRealizationPlan.get(s.getSequenceFor(currentState.getPlayerToMove())))
                );
            }
            return;
        }
        expander.getActions(currentState).forEach(a ->
                getAlternativeNodesOutsideRGFix(currentState.performAction(a), stateForAlternatives, alternativeNodes, opponentProbability)
        );
    }

    private void getAlternativeNodesOutsideRGDefaultFix(GameState currentState, GameState stateForAlternatives, List<GameState> alternativeNodes, double opponentProbability) {
        if (currentState.isGameEnd())
            return;
        if (currentState.getPlayerToMove().getId() == searchingPlayerIndex) {
            int actionIndex = currentState.getSequenceForPlayerToMove().size();
            int desiredSeqLength = stateForAlternatives.getSequenceFor(currentState.getPlayerToMove()).size();

            if (actionIndex == desiredSeqLength && stateForAlternatives.getISKeyForPlayerToMove().equals(currentState.getISKeyForPlayerToMove())) {
                alternativeNodes.add(currentState);
                return;
            }
            if (actionIndex >= desiredSeqLength)
                return;
            Action actionToPlay = stateForAlternatives.getSequenceFor(currentState.getPlayerToMove()).get(actionIndex);

            if (currentState.checkConsistency(actionToPlay))
                getAlternativeNodesOutsideRGDefaultFix(currentState.performAction(actionToPlay), stateForAlternatives, alternativeNodes, opponentProbability);
            return;
        }
        if (currentState.getPlayerToMove().getId() == opponentPlayerIndex) {
            getAlternativeNodesOutsideRGDefaultFix(currentState.performAction(expander.getActions(currentState).get(0)), stateForAlternatives,
                    alternativeNodes, 1);
            return;
        }
        expander.getActions(currentState).forEach(a ->
                getAlternativeNodesOutsideRGDefaultFix(currentState.performAction(a), stateForAlternatives, alternativeNodes, opponentProbability)
        );
    }


    public List<GameState> getAlternativeNodesOutsideRG(GameState state) {
        if (USE_FIX)
            return getAlternativeNodesOutsideRGFix(state);
        List<GameState> alternativeNodes = new ArrayList<GameState>();
        Stack<GameState> queue = new Stack<GameState>();
        queue.add(gameTreeRoot);

        Player mainPlayer = state.getPlayerToMove();
        int length = state.getHistory().getLength();
        boolean neverCheckOppAgain = false;

        while (!queue.isEmpty()) {
            GameState currentState = queue.pop();
            if (currentState.getHistory().getLength() == length) {
                boolean oppRPContains = opponentRealizationPlan.containsKey(currentState.getSequenceFor(gameInfo.getOpponent(mainPlayer)));
                if (currentState.getISKeyForPlayerToMove().equals(state.getISKeyForPlayerToMove()) && !currentState.equals(state) && ((!useOriginalBRFormulation || !oppRPContains) && (useOriginalBRFormulation || oppRPContains))) {
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
                if (!neverCheckOppAgain && opponentRealizationPlan.containsKey(currentState.getSequenceForPlayerToMove())) {
                    Sequence s = new ArrayListSequenceImpl(currentState.getSequenceForPlayerToMove());
                    for (Action a : tmp) {
                        s.addLast(a);
                        Double d = opponentRealizationPlan.get(s);
                        if (d != null && d > 0) {
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

}
