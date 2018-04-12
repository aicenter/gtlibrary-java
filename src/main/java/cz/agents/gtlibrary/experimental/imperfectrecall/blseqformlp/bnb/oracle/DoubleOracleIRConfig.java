package cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle;

import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.SequenceFormIRInformationSet;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.br.LimitedActionsALossBRAlgorithm;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.FixedSizeMap;
import cz.agents.gtlibrary.utils.Pair;

import java.util.*;

public class DoubleOracleIRConfig extends SelfBuildingSequenceFormIRConfig {
    private Map<GameState, Double> pending;
    private Map<GameState, GameState> parents;
    private LimitedActionsALossBRAlgorithm br;


    public DoubleOracleIRConfig(GameInfo gameInfo) {
        super(gameInfo);
        pending = new HashMap<>();
        parents = new HashMap<>();
    }

    @Override
    public void addInformationSetFor(GameState state) {
        super.addInformationSetFor(state);
        pending.remove(state);
    }

    public boolean isInPending(GameState state) {
        return pending.containsKey(state);
    }

    public void addPending(GameState state, GameState parent, double value) {
//        if(state.isGameEnd() || terminalStates.contains(state))
//            return;
        pending.put(state, value);
        parents.put(state, parent);
    }

    public Pair<GameState, Double> getBestPending() {
        Pair<GameState, Double> currentBest = null;

        for (Map.Entry<GameState, Double> entry : pending.entrySet()) {
            if (currentBest == null || entry.getValue() / entry.getKey().getNatureProbability() > currentBest.getRight())
                currentBest = new Pair<>(entry.getKey(), entry.getValue() / entry.getKey().getNatureProbability());
        }
        return currentBest;
    }

//    public Pair<GameState, Double> getBestPending(Set<Action> possibleBestResponses, Player minPlayer) {
//        Pair<GameState, Double> currentBest = null;
//
//        for (Map.Entry<GameState, Double> entry : pending.entrySet()) {
//            if (isPlayed(entry.getKey().getSequenceFor(minPlayer), possibleBestResponses) && (currentBest == null || entry.getValue() / entry.getKey().getNatureProbability() > currentBest.getRight()))
//                currentBest = new Pair<>(entry.getKey(), entry.getValue() / entry.getKey().getNatureProbability());
//        }
//        return currentBest;
//    }

    public Pair<GameState, Double> getBestPending(List<Map<Action, Double>> possibleBestResponses, Player minPlayer) {
        Pair<GameState, Double> currentBest = null;

        for (Map.Entry<GameState, Double> entry : pending.entrySet()) {
            if (isPlayed(entry.getKey().getSequenceFor(minPlayer), possibleBestResponses) && (currentBest == null || entry.getValue() / entry.getKey().getNatureProbability() > currentBest.getRight()))
                currentBest = new Pair<>(entry.getKey(), entry.getValue() / entry.getKey().getNatureProbability());
        }
        return currentBest;
    }

    public Pair<GameState, Double> getBestPending(Map<Sequence, Set<Action>> possibleBestResponses, Player minPlayer) {
        Pair<GameState, Double> currentBest = null;

        for (Map.Entry<GameState, Double> entry : pending.entrySet()) {
            if (isPlayed(entry.getKey().getSequenceFor(minPlayer), possibleBestResponses) && (currentBest == null || entry.getValue() / entry.getKey().getNatureProbability() > currentBest.getRight()))
                currentBest = new Pair<>(entry.getKey(), entry.getValue() / entry.getKey().getNatureProbability());
        }
        return currentBest;
    }

    private boolean isPlayed(Sequence sequence, Map<Sequence, Set<Action>> possibleBestResponses) {
        if(sequence.isEmpty())
            return true;
        Sequence prefix = sequence.getSubSequence(0, sequence.size() - 1);
        Set<Action> actions = possibleBestResponses.get(prefix);

        if(actions == null)
            return false;
        return actions.contains(sequence.getLast());
    }


    private boolean isPlayed(Sequence sequence, List<Map<Action, Double>> possibleBestResponses) {
        return possibleBestResponses.stream().anyMatch(br -> sequence.getAsList().stream().allMatch(a -> br.containsKey(a)));
    }

    public Map<InformationSet, Pair<GameState, Double>> getAndRemoveAllViablePending(Expander<? extends InformationSet> expander, Map<Action, Double> maxPlayerStrategy, List<Map<Action, Double>> possibleBestResponses, Player minPlayer) {
        Map<InformationSet, Pair<GameState, Double>> viablePending = getViablePending(expander, maxPlayerStrategy, possibleBestResponses, minPlayer);

        removeFromPending(viablePending);
        return viablePending;
    }

    public Map<InformationSet, Pair<GameState, Double>> getAndRemoveAllViablePending(Expander<? extends InformationSet> expander, Map<Action, Double> maxPlayerStrategy, Map<Sequence, Set<Action>> possibleBestResponses, Player minPlayer) {
        Map<InformationSet, Pair<GameState, Double>> viablePending = getViablePending(expander, maxPlayerStrategy, possibleBestResponses, minPlayer);

        removeFromPending(viablePending);
        return viablePending;
    }

//    public Map<InformationSet, Pair<GameState, Double>> getViablePending(Set<Action> possibleBestResponses, Player minPlayer, double ub) {
//        Map<InformationSet, Pair<GameState, Double>> viablePending = new HashMap<>();
//
//        pending.entrySet().stream().filter(e -> isPlayed(e.getKey().getSequenceFor(minPlayer), possibleBestResponses)).filter(e -> e.getValue() / e.getKey().getNatureProbability() >= ub).forEach(entry -> {
//            InformationSet currentIS = getParentIS(entry.getKey(), gameInfo.getOpponent(minPlayer));
//            Pair<GameState, Double> currentPair = viablePending.get(currentIS);
//
//            if (currentPair == null || entry.getValue() > currentPair.getRight())
//                viablePending.put(currentIS, new Pair<>(entry.getKey(), entry.getValue() / entry.getKey().getNatureProbability()));
//        });
//        return viablePending;
//    }

    public Map<InformationSet, Pair<GameState, Double>> getViablePending(Expander<? extends InformationSet> expander, Map<Action, Double> maxPlayerStrategy, List<Map<Action, Double>> possibleBestResponses, Player minPlayer) {
        Map<InformationSet, Pair<GameState, Double>> viablePending = new HashMap<>();

        pending.entrySet().stream().filter(e -> isPlayed(e.getKey().getSequenceFor(minPlayer), possibleBestResponses))
                .filter(e -> e.getValue() / e.getKey().getNatureProbability() > getParentValue(e.getKey(), expander, maxPlayerStrategy, possibleBestResponses, gameInfo.getOpponent(minPlayer))).forEach(entry -> {
            InformationSet currentIS = getParentIS(entry.getKey(), gameInfo.getOpponent(minPlayer));
            Pair<GameState, Double> currentPair = viablePending.get(currentIS);

            if (currentPair == null || entry.getValue() > currentPair.getRight())
                viablePending.put(currentIS, new Pair<>(entry.getKey(), entry.getValue() / entry.getKey().getNatureProbability()));
        });
        return viablePending;
    }

    public Map<InformationSet, Pair<GameState, Double>> getViablePending(Expander<? extends InformationSet> expander, Map<Action, Double> maxPlayerStrategy, Map<Sequence, Set<Action>> possibleBestResponses, Player minPlayer) {
        Map<InformationSet, Pair<GameState, Double>> viablePending = new HashMap<>();

        pending.entrySet().stream().filter(e -> isPlayed(e.getKey().getSequenceFor(minPlayer), possibleBestResponses))
                .filter(e -> e.getValue() / e.getKey().getNatureProbability() > getParentValue(e.getKey(), expander, maxPlayerStrategy, possibleBestResponses, gameInfo.getOpponent(minPlayer))).forEach(entry -> {
            InformationSet currentIS = getParentIS(entry.getKey(), gameInfo.getOpponent(minPlayer));
            Pair<GameState, Double> currentPair = viablePending.get(currentIS);

            if (currentPair == null || entry.getValue() > currentPair.getRight())
                viablePending.put(currentIS, new Pair<>(entry.getKey(), entry.getValue() / entry.getKey().getNatureProbability()));
        });
        return viablePending;
    }

    private SequenceFormIRInformationSet getParentIS(GameState pending, Player maxPlayer) {
       return (SequenceFormIRInformationSet) pending.getSequenceFor(maxPlayer).getLastInformationSet();
    }

    public double getParentValue(GameState state, Expander<? extends InformationSet> expander, Map<Action, Double> maxPlayerStrategy, List<Map<Action, Double>> minPlayerStrategies, Player maxPlayer) {
        return getValue(parents.get(state), expander, maxPlayerStrategy, minPlayerStrategies, maxPlayer);
    }

    public double getParentValue(GameState state, Expander<? extends InformationSet> expander, Map<Action, Double> maxPlayerStrategy, Map<Sequence, Set<Action>> minPlayerStrategies, Player maxPlayer) {
        return getValue(parents.get(state), expander, maxPlayerStrategy, minPlayerStrategies, maxPlayer);
    }

    public double getValue(GameState state, Expander<? extends InformationSet> expander, Map<Action, Double> maxPlayerStrategy, List<Map<Action, Double>> minPlayerStrategies, Player maxPlayer) {
        return minPlayerStrategies.stream().mapToDouble(strategy -> getExpectedValue(state, expander, maxPlayerStrategy, strategy, maxPlayer)).min().getAsDouble();
    }

    public double getValue(GameState state, Expander<? extends InformationSet> expander, Map<Action, Double> maxPlayerStrategy, Map<Sequence, Set<Action>> minPlayerStrategies, Player maxPlayer) {
        return -br.calculateLimitedBR(state, maxPlayerStrategy, minPlayerStrategies);
    }

   private double getExpectedValue(GameState state, Expander<? extends InformationSet> expander, Map<Action, Double> maxPlayerStrategy, Map<Action, Double> minPlayerStrategy, Player maxPlayer) {
        if(state.isGameEnd())
            return state.getUtilities()[maxPlayer.getId()];
        if(terminalStates.contains(state))
            return (maxPlayer.getId() == 0 ? 1 : -1) * getActualNonzeroUtilityValues(state);
        if (state.isPlayerToMoveNature())
            return expander.getActions(state).stream().mapToDouble(a -> state.getProbabilityOfNatureFor(a)*getExpectedValue(state.performAction(a), expander, maxPlayerStrategy, minPlayerStrategy, maxPlayer)).sum();
        if(state.getPlayerToMove().equals(maxPlayer))
            return expander.getActions(state).stream()
                    .filter(a -> maxPlayerStrategy.getOrDefault(a, 0d) > 1e-8)
                    .map(a -> new Pair<>(a, state.performAction(a)))
                    .filter(p -> getSequencesFor(maxPlayer).contains(p.getRight().getSequenceFor(maxPlayer)))
                    .mapToDouble(p -> maxPlayerStrategy.getOrDefault(p.getLeft(), 0d)*getExpectedValue(p.getRight(), expander, maxPlayerStrategy, minPlayerStrategy, maxPlayer)).sum();
        assert expander.getActions(state).stream().filter(a -> minPlayerStrategy.getOrDefault(a, 0d) > 1e-8).count() <= 1;
        if (expander.getActions(state).stream().filter(a -> minPlayerStrategy.getOrDefault(a, 0d) > 1e-8).count() == 0)
            return getExpectedValue(state.performAction(expander.getActions(state).get(0)), expander, maxPlayerStrategy, minPlayerStrategy, maxPlayer);
        return expander.getActions(state).stream().filter(a -> minPlayerStrategy.getOrDefault(a, 0d) > 1e-8)
                .mapToDouble(a -> getExpectedValue(state.performAction(a), expander, maxPlayerStrategy, minPlayerStrategy, maxPlayer)).sum();
    }

    private void removeFromPending(Map<InformationSet, Pair<GameState, Double>> viablePending) {
        viablePending.values().stream().map(p -> p.getRight()).forEach(state -> pending.remove(state));
    }

    public Pair<GameState, Double> getAndRemoveBestPending() {
        Pair<GameState, Double> bestPending = getBestPending();

        if (bestPending != null)
            pending.remove(bestPending.getLeft());
        return bestPending;
    }

    public Pair<GameState, Double> getAndRemoveBestPending(List<Map<Action, Double>> possibleBestResponses, Player minPlayer) {
        Pair<GameState, Double> bestPending = getBestPending(possibleBestResponses, minPlayer);

        if (bestPending != null)
            pending.remove(bestPending.getLeft());
        return bestPending;
    }

    public Map<GameState, Double> getPending() {
        return pending;
    }

    public boolean pendingAvailable(Expander<SequenceFormIRInformationSet> expander, Map<Action, Double> maxPlayerStrategy, List<Map<Action, Double>> possibleBestResponses, Player minPlayer) {
        return pending.entrySet().stream().anyMatch(e -> isPlayed(e.getKey().getSequenceFor(minPlayer), possibleBestResponses) &&
                (e.getValue() / e.getKey().getNatureProbability() > getParentValue(e.getKey(), expander, maxPlayerStrategy, possibleBestResponses, gameInfo.getOpponent(minPlayer))));
    }

    public void setBr(LimitedActionsALossBRAlgorithm br) {
        this.br = br;
    }

    public void setUtility(GameState leaf, double utility) {
        Double storedUtility = actualUtilityValuesInLeafs.get(leaf);
        if (storedUtility != null) {
            if (Math.abs(storedUtility - utility) < 1e-8)
                return;
            utilityForSequenceCombination.compute(createActivePlayerMap(leaf), (k, v) -> v - storedUtility);
        }
        FixedSizeMap<Player, Sequence> activePlayerMap = createActivePlayerMap(leaf);
        double existingUtility = utility;

        if (utilityForSequenceCombination.containsKey(activePlayerMap))
            existingUtility += utilityForSequenceCombination.get(activePlayerMap);

        actualUtilityValuesInLeafs.put(leaf, utility);
        utilityForSequenceCombination.put(activePlayerMap, existingUtility);
    }
}
