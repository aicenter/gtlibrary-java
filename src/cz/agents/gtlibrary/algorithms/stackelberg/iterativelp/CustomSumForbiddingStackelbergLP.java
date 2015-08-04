package cz.agents.gtlibrary.algorithms.stackelberg.iterativelp;

import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPData;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.InformationSet;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;

import java.util.HashMap;
import java.util.Map;

public class CustomSumForbiddingStackelbergLP extends SumForbiddingStackelbergLP {

    public CustomSumForbiddingStackelbergLP(Player leader, GameInfo info) {
        super(leader, info);
    }

    @Override
    protected Iterable<Sequence> getBrokenStrategyCauses(Map<InformationSet, Map<Sequence, Double>> strategy, LPData lpData) {
        Map<Sequence, Double> chosenBrokenStrategyCause = null;
        double maxDifference = Double.NEGATIVE_INFINITY;

        for (Map.Entry<InformationSet, Map<Sequence, Double>> isStrategy : strategy.entrySet()) {
            if (isStrategy.getValue().size() > 1) {
                if (chosenBrokenStrategyCause == null) {
                    chosenBrokenStrategyCause = new HashMap<>(isStrategy.getValue());
                    maxDifference = max(isStrategy) - min(isStrategy);
                } else {
                    double currentDifference = max(isStrategy) - min(isStrategy);

                    if (currentDifference > maxDifference)
                        chosenBrokenStrategyCause = new HashMap<>(isStrategy.getValue());
                }
            }
        }
        if (chosenBrokenStrategyCause == null)
            return null;
        return sort(chosenBrokenStrategyCause, chosenBrokenStrategyCause.keySet());
    }

    private double max(Map.Entry<InformationSet, Map<Sequence, Double>> isStrategy) {
        double max = Double.NEGATIVE_INFINITY;

        for (Double value : isStrategy.getValue().values()) {
            if (max < value)
                max = value;
        }
        return max;
    }

    private double min(Map.Entry<InformationSet, Map<Sequence, Double>> isStrategy) {
        double min = Double.POSITIVE_INFINITY;

        for (Double value : isStrategy.getValue().values()) {
            if (min > value)
                min = value;
        }
        return min;
    }
}
