package cz.agents.gtlibrary.strategy;

import cz.agents.gtlibrary.iinodes.ArrayListSequenceImpl;
import cz.agents.gtlibrary.interfaces.*;

import java.util.List;

public class NoiseMaker {

    public static <S extends Strategy> S addStaticNoise(Player player, GameState root, Expander<? extends InformationSet> expander, S strategy, double epsilon) {
        if (!root.isGameEnd()) {
            List<Action> actions = expander.getActions(root);

            if (root.getPlayerToMove().equals(player)) {
                makeCurrentDistributionConsistentWithParent(root, strategy, actions);
                addNoise(root, strategy, epsilon, actions);
            }
            for (Action action : actions) {
                addStaticNoise(player, root.performAction(action), expander, strategy, epsilon);
            }
        }
        return strategy;
    }

    private static <S extends Strategy> void makeCurrentDistributionConsistentWithParent(GameState root, S strategy, List<Action> actions) {
        double parentProbability = strategy.get(root.getSequenceForPlayerToMove());
        double sum = getSum(actions, root.getSequenceForPlayerToMove(), strategy);

        if (Math.abs(sum - parentProbability) > 1e-8)
            if (Math.abs(sum) < 1e-8) {
                setUniformDistribution(root, strategy, actions);
            } else {
                for (Action action : actions) {
                    Sequence extension = getExtension(root, action);
                    double probability = strategy.get(extension);

                    if (probability > 0)
                        strategy.put(extension, probability + (parentProbability - sum) * probability / sum);
                }
            }
        assert Math.abs(getSum(actions, root.getSequenceForPlayerToMove(), strategy) - parentProbability) < 1e-8;
    }

    private static Sequence getExtension(GameState root, Action action) {
        Sequence extension = new ArrayListSequenceImpl(root.getSequenceForPlayerToMove());

        extension.addLast(action);
        return extension;
    }

    private static <S extends Strategy> void addNoise(GameState root, S strategy, double epsilon, List<Action> actions) {
        double sum = getSum(actions, root.getSequenceForPlayerToMove(), strategy);
        double offset = sum * epsilon;
        double toRemove = addOffsetToZeroProbSequences(root, strategy, actions, offset);

        removeRedundantValue(root, strategy, actions, sum, offset, toRemove);
    }

    private static void setUniformDistribution(GameState root, Strategy strategy, List<Action> actions) {
        double probabilityToDistribute = strategy.get(root.getSequenceForPlayerToMove());

        for (Action action : actions) {
            Sequence extension = getExtension(root, action);
            strategy.put(extension, probabilityToDistribute / actions.size());
        }
    }

    private static void removeRedundantValue(GameState root, Strategy strategy, List<Action> actions, double sum, double offset, double toRemove) {
        for (Action action : actions) {
            Sequence extension = getExtension(root, action);
            double probability = strategy.get(extension);

            if (probability > offset) {
                double value = toRemove * probability / sum;

                strategy.put(extension, strategy.get(extension) - value);
            }
        }
        assert Math.abs(getSum(actions, root.getSequenceForPlayerToMove(), strategy) - strategy.get(root.getSequenceForPlayerToMove())) < 1e-2;
    }

    private static double addOffsetToZeroProbSequences(GameState root, Strategy strategy, List<Action> actions, double offset) {
        double toRemove = 0;

        for (Action action : actions) {
            Sequence extension = getExtension(root, action);
            double probability = strategy.get(extension);

            if (probability == 0) {
                toRemove += offset;
                strategy.put(extension, offset);
            }
        }
        return toRemove;
    }

    private static double getSum(List<Action> actions, Sequence sequence, Strategy strategy) {
        double sum = 0;

        for (Action action : actions) {
            Sequence extension = new ArrayListSequenceImpl(sequence);

            extension.addLast(action);
            sum += strategy.get(extension);
        }
        return sum;
    }
}
