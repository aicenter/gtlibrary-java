package cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.memeff;

import cz.agents.gtlibrary.algorithms.mcts.oos.OOSAlgorithmData;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Sequence;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class IRCFRData extends OOSAlgorithmData {

    protected Map<GameState, double[]> regretUpdates;
    protected Map<Sequence, Double> expPlayerProbs;
    protected Map<GameState, Double> opponentProbs;
    protected boolean updated;

    public IRCFRData(int actionCount) {
        super(actionCount);
        regretUpdates = new HashMap<>();
        expPlayerProbs = new HashMap<>();
        opponentProbs = new HashMap<>();
        updated = false;
    }

    public IRCFRData(List<Action> actions) {
        this(actions.size());
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
        double[] regretUpdate = new double[getActionCount()];

        for (int i = 0; i < getActionCount(); i++) {
            if (i == actionIndex)
                regretUpdate[i] += (c - x) * W;
            else
                regretUpdate[i] += -x * W;
        }
        regretUpdates.put(state, regretUpdate);
        expPlayerProbs.put(state.getSequenceForPlayerToMove(), expPlayerProb);
        updated = true;
    }

    public void updateAllRegrets(double[] Vs, double meanV, double opponentProb, GameState state, double expPlayerProb) {
        double[] regretUpdate = new double[getActionCount()];

        for (int i = 0; i < getActionCount(); i++) {
            regretUpdate[i] += opponentProb * (Vs[i] - meanV);
        }
        regretUpdates.put(state, regretUpdate);
        expPlayerProbs.put(state.getSequenceForPlayerToMove(), expPlayerProb);
        opponentProbs.put(state, opponentProb);
        updated = true;
    }

    public boolean applyUpdate() {
        if(!updated)
            return false;
        Map<Sequence, Double> normalizedExpPlayerProbs = normalize(expPlayerProbs);

        regretUpdates.forEach((state, regretUpdate) -> {
            double weight = normalizedExpPlayerProbs.get(state.getSequenceForPlayerToMove());

            for (int j = 0; j < regretUpdate.length; j++) {
                r[j] += regretUpdate[j] * weight;
            }
        });
        updated = false;
        updateMeanStrategy(getRMStrategy(), expPlayerProbs.values().stream().collect(Collectors.summingDouble(d -> d)));
        regretUpdates = new HashMap<>();
        expPlayerProbs = new HashMap<>();
        opponentProbs = new HashMap<>();
        return true;
    }

    private Map<Sequence, Double> normalize(Map<Sequence, Double> expPlayerProbs) {
        double sum = expPlayerProbs.values().stream().collect(Collectors.summingDouble(d -> d));

        if(sum == 0)
            return expPlayerProbs.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), e -> 1./expPlayerProbs.size()));
        return expPlayerProbs.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()/sum));
    }

    public double[] getNormalizedMeanStrategy() {
        double[] meanStrategy = getMp();
        double sum = Arrays.stream(meanStrategy).sum();

        return IntStream.range(0, meanStrategy.length).mapToDouble(i ->
                sum == 0 ? (1. / meanStrategy.length) : (meanStrategy[i] / sum)
        ).toArray();
    }

}
