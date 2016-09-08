package cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle;

import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DoubleOracleIRConfig extends SelfBuildingSequenceFormIRConfig {
    private Map<GameState, Double> pending;

    public DoubleOracleIRConfig() {
        super();
        pending = new HashMap<>();
    }

    @Override
    public void addInformationSetFor(GameState state) {
        super.addInformationSetFor(state);
        pending.remove(state);
    }

    public boolean isInPending(GameState state) {
        return pending.containsKey(state);
    }

    public void addPending(GameState state, double value) {
        pending.put(state, value);
    }

    public Map.Entry<GameState, Double> getBestPending() {
        Map.Entry<GameState, Double> currentBest = null;

        for (Map.Entry<GameState, Double> entry : pending.entrySet()) {
            if (currentBest == null || entry.getValue() > currentBest.getValue())
                currentBest = entry;
        }
        return currentBest;
    }

    public Map.Entry<GameState, Double> getBestPending(Set<Action> possibleBestResponses, Player minPlayer) {
        Map.Entry<GameState, Double> currentBest = null;

        for (Map.Entry<GameState, Double> entry : pending.entrySet()) {
            if (isPlayed(entry.getKey().getSequenceFor(minPlayer), possibleBestResponses) && (currentBest == null || entry.getValue() > currentBest.getValue()))
                currentBest = entry;
        }
        return currentBest;
    }

    private boolean isPlayed(Sequence sequence, Set<Action> possibleBestResponses) {
        return sequence.getAsList().stream().allMatch(a -> possibleBestResponses.contains(a));
    }

    public Map<InformationSet, Pair<GameState, Double>> getAndRemoveAllViablePending(Set<Action> possibleBestResponses, Player minPlayer, double ub) {
        Map<InformationSet, Pair<GameState, Double>> viablePending = new HashMap<>();

        pending.entrySet().stream().filter(e -> isPlayed(e.getKey().getSequenceFor(minPlayer), possibleBestResponses)).filter(e -> e.getValue() >= ub).forEach(entry -> {
            InformationSet currentIS = getInformationSetFor(entry.getKey());
            Pair<GameState, Double> currentPair = viablePending.get(currentIS);

            if (currentPair == null || entry.getValue() > currentPair.getRight())
                viablePending.put(currentIS, new Pair<>(entry.getKey(), entry.getValue()));
        });
        removeFromPending(viablePending);
        return viablePending;
    }

    private void removeFromPending(Map<InformationSet, Pair<GameState, Double>> viablePending) {
        viablePending.values().stream().map(p -> p.getRight()).forEach(state -> pending.remove(state));
    }

    public Map.Entry<GameState, Double> getAndRemoveBestPending() {
        Map.Entry<GameState, Double> bestPending = getBestPending();

        if (bestPending != null)
            pending.remove(bestPending.getKey());
        return bestPending;
    }

    public Map<GameState, Double> getPending() {
        return pending;
    }
}
