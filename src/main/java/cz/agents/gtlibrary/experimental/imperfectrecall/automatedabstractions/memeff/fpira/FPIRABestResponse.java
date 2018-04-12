package cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.memeff.fpira;

import cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.memeff.AbstractedStrategyUtils;
import cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.memeff.InformationSetKeyMap;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.br.ALossBestResponseAlgorithm;
import cz.agents.gtlibrary.iinodes.ArrayListSequenceImpl;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.interfaces.*;

import java.util.*;

public class FPIRABestResponse extends ALossBestResponseAlgorithm {

    private Map<ISKey, double[]> opponentAbstractedStrategy;
    private InformationSetKeyMap currentAbstractionISKeys;
    private Map<Action, Double> probabilityCache;
    public int maxProbCacheSize = 0;

    public FPIRABestResponse(GameState root, Expander<? extends InformationSet> expander, int searchingPlayerIndex,
                             Player[] actingPlayers, AlgorithmConfig<? extends InformationSet> algConfig, GameInfo gameInfo,
                             boolean stateCacheUse, InformationSetKeyMap currentAbstractionISKeys) {
        super(root, expander, searchingPlayerIndex, actingPlayers, algConfig, gameInfo, stateCacheUse);
        this.currentAbstractionISKeys = currentAbstractionISKeys;
        probabilityCache = new HashMap<>();
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
            List<GameState> alternativeNodes = new ArrayList<>();

            boolean nonZeroOppRP = currentStateProb > 0;
            boolean nonZeroOppRPAlt = false;
            InformationSet currentIS = algConfig.getInformationSetFor(gameState);

            if (currentIS != null) {
                alternativeNodes.addAll(currentIS.getAllStates());
                if (!alternativeNodes.contains(gameState))
                    alternativeNodes.add(gameState);
                if (alternativeNodes.size() == 1 && nonZeroOppRP)
                    alternativeNodes.addAll(getAlternativeNodesOutsideRG(gameState));
            } else {
                alternativeNodes.add(gameState);
                alternativeNodes.addAll(getAlternativeNodesOutsideRG(gameState));
            }

            assert (alternativeNodes.contains(gameState));
            HashMap<GameState, Double> alternativeNodesProbs = new HashMap<>();
            double ISProbability = 0;

            for (GameState currentNode : alternativeNodes) {
                double currentNodeProb = currentNode.getNatureProbability();

                if (nonZeroOppRP) {
                    double altProb = currentNode.equals(gameState) ? currentStateProb : getOpponentProbability(currentNode.getSequenceFor(players[opponentPlayerIndex]));

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
                Iterator<GameState> iterator = alternativeNodes.iterator();

                while (iterator.hasNext()) {
                    GameState state = iterator.next();
                    if (!state.getHistory().getSequenceOf(players[opponentPlayerIndex]).equals(gameState.getHistory().getSequenceOf(players[opponentPlayerIndex]))) {
                        iterator.remove();
                        alternativeNodesProbs.remove(state);
                    }
                }
            }
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
                if (currentNode.equals(gameState)) {
                    if (Collections.max(sel.actionRealValues.get(currentNode).values()) < lowerBound)
                        break;
                }
            }

            Action resultAction = sel.getResult().getLeft(); //selected action for the searching player

            for (GameState currentNode : alternativeNodes) { // storing the results based on the action
                if (sel.actionRealValues.get(currentNode) == null || sel.actionRealValues.get(currentNode).isEmpty()) {
                    if (currentNode.equals(gameState)) {
                        returnValue = Double.NEGATIVE_INFINITY;
                    }
                    continue;
                }
                double v;
                if (resultAction == null) {
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
            storeResult(gameState, resultAction);
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
            if (nonZeroORP && !sel.nonZeroContinuation) {
                returnValue *= currentStateProb;
            }
        }
        assert (returnValue != null);
        assert (returnValue <= MAX_UTILITY_VALUE * (1.01));
        return returnValue;
    }

    private void storeResult(GameState gameState, Action resultAction) {
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
    }

    public Map<Action, Double> getBestResponse() {
        if (BRresult == null) {
            return null;
        }
        Map<Action, Double> result = new HashMap<>();
        Queue<Sequence> queue = new ArrayDeque<>();

        queue.addAll(firstLevelActions.values());
        firstLevelActions.values().forEach(s -> result.put(s.getLast(), 1d));
        while (queue.size() > 0) {
            Sequence sequence = queue.poll();
            Map<ISKey, Sequence> res = BRresult.get(sequence);

            if (res != null) {
                res.values().stream().forEach(s -> result.put(s.getLast(), 1d));
                queue.addAll(res.values());
            }
        }
        firstLevelActions.clear();
        probabilityCache.clear();
        cachedValuesForNodes.clear();
        BRresult.clear();
        opponentAbstractedStrategy = null;
        opponentBehavioralStrategy = null;
        return result;
    }

    public void clearData() {
        firstLevelActions.clear();
        probabilityCache.clear();
        cachedValuesForNodes.clear();
        BRresult.clear();
        opponentAbstractedStrategy = null;
        opponentBehavioralStrategy = null;
    }

    public Double calculateBRForAbstractedStrategy(GameState root, Map<ISKey, double[]> opponentAbstractedStrategy) {
        this.opponentAbstractedStrategy = opponentAbstractedStrategy;
        Double value = calculateBR(root, new HashMap<>(), new HashMap<>());

        maxProbCacheSize = Math.max(maxProbCacheSize, probabilityCache.size());
        this.probabilityCache.clear();
        return value;
    }

    protected Double calculateEvaluation(GameState gameState, double currentStateProbability) {
        double utRes = gameState.getUtilities()[0] * gameState.getNatureProbability();

        if (searchingPlayerIndex == 1)
            utRes *= -1; // a zero sum game
        if (currentStateProbability == 0)
            currentStateProbability = 1d;
        return utRes * currentStateProbability; // weighting with opponent's realization plan
    }

    @Override
    protected double getOpponentProbability(Sequence sequence) {
        return AbstractedStrategyUtils.getProbability(sequence, opponentAbstractedStrategy, currentAbstractionISKeys, expander, probabilityCache);
    }

    protected double getProbabilityForAction(Action action) {
        return AbstractedStrategyUtils.getProbabilityForAction(action, opponentAbstractedStrategy, currentAbstractionISKeys, expander, probabilityCache);
    }

}
