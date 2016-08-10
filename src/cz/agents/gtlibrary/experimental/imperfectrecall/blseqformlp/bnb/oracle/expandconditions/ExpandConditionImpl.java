package cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.expandconditions;

import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.SequenceFormIRConfig;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.SequenceFormIRInformationSet;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.OracleCandidate;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Sequence;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ExpandConditionImpl implements ExpandCondition {

    @Override
    public boolean validForExpansion(SequenceFormIRConfig config, OracleCandidate candidate) {
        double boundGap = candidate.getUb() - candidate.getLb();
        double precisionError = getPrecisionError(config, candidate);

        return boundGap > precisionError;
    }

    private double getPrecisionError(SequenceFormIRConfig config, OracleCandidate candidate) {
        return config.getAllInformationSets().values().stream().filter(i -> i.hasIR()).
                mapToDouble(i -> getMaxPrecisionErrorFor(config, candidate, i)).max().orElse(0);
    }

    private double getMaxPrecisionErrorFor(SequenceFormIRConfig config, OracleCandidate candidate, SequenceFormIRInformationSet i) {
        Map<Action, Double> minBehavProb = new HashMap<>();
        Map<Action, Double> maxBehavProb = new HashMap<>();
        Map<Action, Sequence> minSequence = new HashMap<>();
        Map<Action, Sequence> maxSequence = new HashMap<>();

        for (Map.Entry<Sequence, Set<Sequence>> entry : i.getOutgoingSequences().entrySet()) {
            double incomingSeqProb = candidate.getMaxPlayerRealPlan().getOrDefault(entry.getKey(), 0d);

            if (incomingSeqProb > 1e-4) {
                for (Sequence outgoingSequence : entry.getValue()) {
                    double actionBehavProb = candidate.getMaxPlayerRealPlan().getOrDefault(outgoingSequence, 0d) / incomingSeqProb;
                    Double minBehav = minBehavProb.get(outgoingSequence.getLast());
                    Double maxBehav = maxBehavProb.get(outgoingSequence.getLast());

                    if (minBehav == null || minBehav > actionBehavProb) {
                        minBehavProb.put(outgoingSequence.getLast(), actionBehavProb);
                        minSequence.put(outgoingSequence.getLast(), outgoingSequence);
                    }
                    if (maxBehav == null || maxBehav < actionBehavProb) {
                        maxBehavProb.put(outgoingSequence.getLast(), actionBehavProb);
                        maxSequence.put(outgoingSequence.getLast(), outgoingSequence);
                    }
                }
            }
        }
        double maxPositiveDiff = 0;
        double maxNegativeDiff = 0;

        for (Action action : i.getActions()) {
            double probabilityGap = maxBehavProb.getOrDefault(action, 0d) - minBehavProb.getOrDefault(action, 0d);

            if(probabilityGap > 1e-8) {
                maxPositiveDiff += probabilityGap * (config.getHighestReachableUtilityFor(maxSequence.get(action)) - config.getLowestReachableUtilityFor(minSequence.get(action)));
                maxNegativeDiff += probabilityGap * (config.getLowestReachableUtilityFor(maxSequence.get(action)) - config.getHighestReachableUtilityFor(minSequence.get(action)));
            }
        }
        return Math.max(Math.abs(maxNegativeDiff), Math.abs(maxPositiveDiff));
    }
}