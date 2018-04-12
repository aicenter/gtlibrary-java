package cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions;

import cz.agents.gtlibrary.algorithms.cfr.ir.FixedForIterationData;
import cz.agents.gtlibrary.interfaces.Action;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class CFRBRData extends FixedForIterationData {
    private double[] meanStrategyUpdateNumerator;
    private double meanStrategyUpdateDenominator;

    public CFRBRData(int actionCount) {
        super(actionCount);
        meanStrategyUpdateNumerator = new double[actionCount];
//        Arrays.fill(mp, 1./mp.length);
        if (mp.length > 0)
            mp[0] = 1;
    }

    public CFRBRData(List<Action> actions) {
        super(actions);
        meanStrategyUpdateNumerator = new double[actions.size()];
//        Arrays.fill(mp, 1./mp.length);
        if (mp.length > 0)
            mp[0] = 1;
    }

    public CFRBRData(CFRBRData data) {
        this(data.getActionCount());
        System.arraycopy(data.getMp(), 0, mp, 0, mp.length);
        nbSamples = data.nbSamples;
    }

    public void updateMeanStrategy(int index, double w) {
        mp[index] += w;
        nbSamples++;
    }

    public void addToMeanStrategyUpdateNumerator(int actionIndex, double v) {
        meanStrategyUpdateNumerator[actionIndex] += v;
    }

    public void updateMeanStrategy() {
        if (meanStrategyUpdateDenominator < 1e-8)
            return;
        IntStream.range(0, mp.length).forEach(i -> mp[i] += meanStrategyUpdateNumerator[i] / meanStrategyUpdateDenominator);
        assert Math.abs(Arrays.stream(mp).sum() - 1) < 1e-8 && Arrays.stream(mp).allMatch(v -> v >= 0);
        meanStrategyUpdateDenominator = 0;
        Arrays.fill(meanStrategyUpdateNumerator, 0);
    }

    public void addToMeanStrategyUpdateDenominator(double v) {
        meanStrategyUpdateDenominator += v;
    }
}
