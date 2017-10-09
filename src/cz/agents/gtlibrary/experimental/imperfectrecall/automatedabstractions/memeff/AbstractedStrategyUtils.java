package cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.memeff;

import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.iinodes.PerfectRecallISKey;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.InformationSet;
import cz.agents.gtlibrary.interfaces.Sequence;

import java.util.List;
import java.util.Map;

public class AbstractedStrategyUtils {

    public static double getProbability(Sequence sequence, Map<ISKey, double[]> abstractedStrategy,
                                        InformationSetKeyMap currentAbstractionISKeys,
                                        Expander<? extends InformationSet> expander) {
        if (sequence.isEmpty())
            return 1;
        double probability = 1;

        for (Action action : sequence) {
            probability *= getProbabilityForAction(action, abstractedStrategy, currentAbstractionISKeys, expander);
        }
        return probability;
    }

    public static double getProbability(Sequence sequence, Map<ISKey, double[]> abstractedStrategy,
                                        InformationSetKeyMap currentAbstractionISKeys,
                                        Expander<? extends InformationSet> expander,
                                        Map<Action, Double> cache) {
        if (sequence.isEmpty())
            return 1;
        double probability = 1;

        for (Action action : sequence) {
            probability *= getProbabilityForAction(action, abstractedStrategy, currentAbstractionISKeys, expander, cache);
        }
        return probability;
    }

    public static double getProbabilityForAction(Action action, Map<ISKey, double[]> abstractedStrategy,
                                                 InformationSetKeyMap currentAbstractionISKeys,
                                                 Expander<? extends InformationSet> expander, Map<Action, Double> cache) {
        return cache.computeIfAbsent(action, a -> getProbabilityForAction(action, abstractedStrategy, currentAbstractionISKeys, expander));
    }

    public static double getProbabilityForAction(Action action, Map<ISKey, double[]> abstractedStrategy,
                                             InformationSetKeyMap currentAbstractionISKeys,
                                             Expander<? extends InformationSet> expander) {
        InformationSet informationSet = action.getInformationSet();
        List<Action> actions = expander.getActions(informationSet.getAllStates().stream().findAny().get());
        double[] realizations = abstractedStrategy.get(currentAbstractionISKeys.get((PerfectRecallISKey) informationSet.getISKey(), actions));

        for (int i = 0; i < actions.size(); i++) {
            if (actions.get(i).equals(action))
                return realizations[i];
        }
        throw new UnsupportedOperationException("Action not found");
    }
}
