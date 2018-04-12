package cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.memeff.cfr;

import cz.agents.gtlibrary.algorithms.mcts.oos.OOSAlgorithmData;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Sequence;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class IRCFRData extends OOSAlgorithmData implements Serializable {

    double[] regretUpdate;
    protected boolean updated;
    protected boolean updatedInLastIteration;
    protected boolean visitedInLastIteration;
    protected boolean visitedByAvgStrategy;
    private Map<Sequence, Double> expPlayerProbs;

    public IRCFRData(int actionCount) {
        super(actionCount);
        if (!IRCFR.DIRECT_REGRET_UPDATE)
            regretUpdate = new double[actionCount];
        expPlayerProbs = new HashMap<>();
        updated = false;
        r[0] = 1;
    }

    public IRCFRData(List<Action> actions) {
        super(actions);
        if (!IRCFR.DIRECT_REGRET_UPDATE)
            regretUpdate = new double[actions.size()];
        expPlayerProbs = new HashMap<>();
        updated = false;
        r[0] = 1;
    }

    public IRCFRData(OOSAlgorithmData data) {
        this(data.getActionCount());
        setFrom(data);
    }

    @Override
    public void updateRegret(int ai, double W, double c, double x) {
        throw new UnsupportedOperationException("Wrong method used");
    }

    public void updateRegret(int actionIndex, double W, double c, double x, GameState state, double expPlayerProb) {
        if (IRCFR.DIRECT_REGRET_UPDATE)
            for (int i = 0; i < getActionCount(); i++) {
                if (i == actionIndex)
                    r[i] += (c - x) * W;
                else
                    r[i] += -x * W;
            }
        else
            for (int i = 0; i < getActionCount(); i++) {
                if (i == actionIndex)
                    regretUpdate[i] += (c - x) * W;
                else
                    regretUpdate[i] += -x * W;
            }
        expPlayerProbs.put(state.getSequenceForPlayerToMove(), expPlayerProb);
        updated = true;
    }

    public void updateAllRegrets(double[] Vs, double meanV, double opponentProb, GameState state, double expPlayerProb) {
        if (IRCFR.DIRECT_REGRET_UPDATE)
            for (int i = 0; i < getActionCount(); i++) {
                r[i] += opponentProb * (Vs[i] - meanV);
            }
        else
            for (int i = 0; i < getActionCount(); i++) {
                regretUpdate[i] += opponentProb * (Vs[i] - meanV);
            }
        expPlayerProbs.put(state.getSequenceForPlayerToMove(), expPlayerProb);
        updated = true;
    }

    public boolean applyUpdate(double avgStrategyWeight) {
        updatedInLastIteration = updated;
        if (!updated)
            return false;
        updateMeanStrategy(getRMStrategy(),
                avgStrategyWeight * expPlayerProbs.values().stream().collect(Collectors.summingDouble(d -> d)));

        if (!IRCFR.DIRECT_REGRET_UPDATE)
            for (int j = 0; j < regretUpdate.length; j++) {
                r[j] += regretUpdate[j];
            }
        if (IRCFR.REGRET_MATCHING_PLUS)
            IntStream.range(0, r.length).forEach(i -> r[i] = Math.max(r[i], 0));
        updated = false;
        expPlayerProbs = new HashMap<>();
        if (!IRCFR.DIRECT_REGRET_UPDATE)
            Arrays.fill(regretUpdate, 0);
        return true;
    }

    private Map<Sequence, Double> update(Map<Sequence, Double> expPlayerProbs) {
        double sum = expPlayerProbs.values().stream().collect(Collectors.summingDouble(d -> d));

        if (sum == 0)
            return expPlayerProbs.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), e -> 1. / expPlayerProbs.size()));
        return expPlayerProbs;
    }

    private Map<Sequence, Double> normalize(Map<Sequence, Double> expPlayerProbs) {
        double sum = expPlayerProbs.values().stream().collect(Collectors.summingDouble(d -> d));

        if (sum == 0)
            return expPlayerProbs.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), e -> 1. / expPlayerProbs.size()));
        return expPlayerProbs.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue() / sum));
    }

    public double[] getNormalizedMeanStrategy() {
        double[] meanStrategy = getMp();
        double sum = Arrays.stream(meanStrategy).sum();

        return IntStream.range(0, meanStrategy.length).mapToDouble(i ->
                sum == 0 ? (1. / meanStrategy.length) : (meanStrategy[i] / sum)
        ).toArray();
    }

    public boolean isUpdatedInLastIteration() {
        return updatedInLastIteration;
    }

    public boolean isVisitedInLastIteration() {
        return visitedInLastIteration;
    }

    public void setVisitedInLastIteration(boolean visitedInLastIteration) {
        this.visitedInLastIteration = visitedInLastIteration;
    }

    public void setVisitedByAvgStrategy(boolean visitedByAvgStrategy) {
        this.visitedByAvgStrategy = visitedByAvgStrategy;
    }

    public boolean getVisitedByAvgStrategy() {
        return visitedByAvgStrategy;
    }
}
