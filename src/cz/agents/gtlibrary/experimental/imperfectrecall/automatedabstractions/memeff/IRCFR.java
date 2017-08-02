package cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.memeff;

import cz.agents.gtlibrary.algorithms.cfr.ir.IRCFRInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.AlgorithmData;
import cz.agents.gtlibrary.algorithms.mcts.MCTSConfig;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.oos.OOSAlgorithmData;
import cz.agents.gtlibrary.domain.poker.kuhn.KPGameInfo;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerExpander;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerGameState;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.iinodes.ImperfectRecallISKey;
import cz.agents.gtlibrary.iinodes.PerfectRecallISKey;
import cz.agents.gtlibrary.interfaces.*;

import java.util.*;
import java.util.stream.Collectors;

public class IRCFR extends AutomatedAbstractionAlgorithm {
    private MCTSConfig perfectRecallConfig;

    public static void main(String[] args) {
        GameState root = new KuhnPokerGameState();
        MCTSConfig config = new MCTSConfig();
        Expander<MCTSInformationSet> expander = new KuhnPokerExpander<>(config);
        IRCFR alg = new IRCFR(root, expander, new KPGameInfo(), config);

        alg.runIterations(1000);
    }

    public IRCFR(GameState rootState, Expander<? extends InformationSet> perfectRecallExpander, GameInfo info, MCTSConfig perfectRecallConfig) {
        super(rootState, perfectRecallExpander, info);
        this.perfectRecallConfig = perfectRecallConfig;
    }

    @Override
    protected boolean isConverged(double v) {
        return false;
    }

    @Override
    protected void iteration(Player player) {
        perfectRecallIteration(rootState, 1, 1, player);
        imperfectRecallIteration(rootState, 1, 1, player);
        updateImperfectRecallData();
        Map<ImperfectRecallISKey, Map<PerfectRecallISKey, OOSAlgorithmData>> regretDifferences = getRegretDifferences();

        System.out.println(regretDifferences.size());
    }

    private Map<ImperfectRecallISKey, Map<PerfectRecallISKey, OOSAlgorithmData>> getRegretDifferences() {
        Map<ImperfectRecallISKey, Map<PerfectRecallISKey, OOSAlgorithmData>> toSplit = new HashMap<>();

        currentAbstractionInformationSets.values().forEach(i -> {
            double[] abstractionRegrets = i.getData().getRegrets();
            Set<ISKey> isKeys = i.getAllStates().stream().filter(s -> !s.isPlayerToMoveNature()).map(s -> s.getISKeyForPlayerToMove()).collect(Collectors.toSet());

            isKeys.forEach(k -> {
                OOSAlgorithmData algorithmData = (OOSAlgorithmData) perfectRecallConfig.getAllInformationSets().get(k).getAlgorithmData();

                if (Arrays.equals(abstractionRegrets, algorithmData.getRegrets())) {
                    Map<PerfectRecallISKey, OOSAlgorithmData> dataMap = toSplit.computeIfAbsent((ImperfectRecallISKey) i.getISKey(), key -> new HashMap<>());

                    dataMap.put((PerfectRecallISKey) k, algorithmData);
                }
            });
        });
        return toSplit;
    }

    private void updateImperfectRecallData() {
        currentAbstractionInformationSets.values().forEach(i -> ((IRCFRData) i.getData()).applyUpdate());
    }

    protected double perfectRecallIteration(GameState node, double pi1, double pi2, Player expPlayer) {
        if (pi1 == 0 && pi2 == 0)
            return 0;
        if (node.isGameEnd())
            return node.getUtilities()[expPlayer.getId()];
        MCTSInformationSet informationSet = perfectRecallConfig.getAllInformationSets().get(node.getSequenceForPlayerToMove());

        if (informationSet == null) {
            informationSet = perfectRecallConfig.createInformationSetFor(node);
            perfectRecallConfig.addInformationSetFor(node, informationSet);
            informationSet.setAlgorithmData(createPerfectRecallAlgData(node));
        }
        if (!informationSet.getAllStates().contains(node)) {
            perfectRecallConfig.addInformationSetFor(node, informationSet);
        }

        OOSAlgorithmData data = (OOSAlgorithmData) informationSet.getAlgorithmData();
        List<Action> actions = data.getActions();

        data.setFrom(getAbstractedInformationSet(node).getData());
        if (node.isPlayerToMoveNature()) {
            double expectedValue = 0;

            for (Action ai : actions) {
                ai.setInformationSet(informationSet);

                final double p = node.getProbabilityOfNatureFor(ai);
                double new_p1 = expPlayer.getId() == 1 ? pi1 * p : pi1;
                double new_p2 = expPlayer.getId() == 0 ? pi2 * p : pi2;
                GameState newState = node.performAction(ai);

                expectedValue += p * perfectRecallIteration(newState, new_p1, new_p2, expPlayer);
            }
            return expectedValue;
        }

        double[] currentStrategy = getStrategy(data);
        double[] expectedValuesForActions = new double[currentStrategy.length];
        double expectedValue = 0;

        int i = -1;
        for (Action ai : actions) {
            i++;
            ai.setInformationSet(informationSet);
            GameState newState = node.performAction(ai);

            if (informationSet.getPlayer().getId() == 0) {
                expectedValuesForActions[i] = perfectRecallIteration(newState, pi1 * currentStrategy[i], pi2, expPlayer);
            } else {
                expectedValuesForActions[i] = perfectRecallIteration(newState, pi1, currentStrategy[i] * pi2, expPlayer);
            }
            expectedValue += currentStrategy[i] * expectedValuesForActions[i];
        }
        if (informationSet.getPlayer().equals(expPlayer))
            updateForPerfectRecall(pi1, pi2, expPlayer, data, currentStrategy, expectedValuesForActions, expectedValue);
        return expectedValue;
    }

    protected double imperfectRecallIteration(GameState node, double pi1, double pi2, Player expPlayer) {
        if (pi1 == 0 && pi2 == 0)
            return 0;
        if (node.isGameEnd())
            return node.getUtilities()[expPlayer.getId()];
        IRCFRInformationSet informationSet = getAbstractedInformationSet(node);
        OOSAlgorithmData data = informationSet.getData();
        List<Action> actions = perfectRecallExpander.getActions(node);

        if (node.isPlayerToMoveNature()) {
            double expectedValue = 0;

            for (Action ai : actions) {
                final double p = node.getProbabilityOfNatureFor(ai);
                double new_p1 = expPlayer.getId() == 1 ? pi1 * p : pi1;
                double new_p2 = expPlayer.getId() == 0 ? pi2 * p : pi2;
                GameState newState = node.performAction(ai);

                expectedValue += p * imperfectRecallIteration(newState, new_p1, new_p2, expPlayer);
            }
            return expectedValue;
        }

        double[] currentStrategy = getStrategy(data);
        double[] expectedValuesForActions = new double[currentStrategy.length];
        double expectedValue = 0;

        int i = -1;
        for (Action ai : actions) {
            i++;
            GameState newState = node.performAction(ai);

            if (informationSet.getPlayer().getId() == 0) {
                expectedValuesForActions[i] = imperfectRecallIteration(newState, pi1 * currentStrategy[i], pi2, expPlayer);
            } else {
                expectedValuesForActions[i] = imperfectRecallIteration(newState, pi1, currentStrategy[i] * pi2, expPlayer);
            }
            expectedValue += currentStrategy[i] * expectedValuesForActions[i];
        }
        if (informationSet.getPlayer().equals(expPlayer))
            updateForImperfectRecall(pi1, pi2, expPlayer, data, expectedValuesForActions, expectedValue);
        return expectedValue;
    }

    private IRCFRInformationSet getAbstractedInformationSet(GameState node) {
        return currentAbstractionInformationSets.get(currentAbstractionISKeys.get((PerfectRecallISKey) node.getISKeyForPlayerToMove(), perfectRecallExpander.getActions(node)));
    }

    protected void updateForPerfectRecall(double pi1, double pi2, Player expPlayer, OOSAlgorithmData data,
                                          double[] currentStrategy, double[] expectedValuesForActions, double expectedValue) {
        double[] expPlayerVals = new double[expectedValuesForActions.length];

        for (int i = 0; i < expectedValuesForActions.length; i++) {
            expPlayerVals[i] = expectedValuesForActions[i];
        }
        data.updateAllRegrets(expectedValuesForActions, expectedValue, (expPlayer.getId() == 0 ? pi2 : pi1)/*pi1*pi2*/);
        data.updateMeanStrategy(currentStrategy, (expPlayer.getId() == 0 ? pi1 : pi2)/*pi1*pi2*/);
    }

    protected void updateForImperfectRecall(double pi1, double pi2, Player expPlayer, OOSAlgorithmData data,
                                            double[] expectedValuesForActions, double expectedValue) {
        double[] expPlayerVals = new double[expectedValuesForActions.length];

        for (int i = 0; i < expectedValuesForActions.length; i++) {
            expPlayerVals[i] = expectedValuesForActions[i];
        }
        ((IRCFRData) data).updateAllRegrets(expectedValuesForActions, expectedValue, (expPlayer.getId() == 0 ? pi2 : pi1), (expPlayer.getId() == 0 ? pi1 : pi2));
    }

    protected AlgorithmData createPerfectRecallAlgData(GameState node) {
        return new OOSAlgorithmData(perfectRecallExpander.getActions(node));
    }

    protected void addData(Collection<IRCFRInformationSet> informationSets) {
        informationSets.forEach(i -> i.setData(new IRCFRData(this.perfectRecallExpander.getActions(i.getAllStates().stream().findAny().get()).size())));
    }

    protected double[] getStrategy(OOSAlgorithmData data) {
        return data.getRMStrategy();
    }
}
