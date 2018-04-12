package cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.memeff.fpira;

import cz.agents.gtlibrary.algorithms.cfr.ir.IRCFRInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.oos.OOSAlgorithmData;
import cz.agents.gtlibrary.domain.goofspiel.GSGameInfo;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielExpander;
import cz.agents.gtlibrary.domain.goofspiel.IIGoofSpielGameState;
import cz.agents.gtlibrary.domain.poker.generic.GPGameInfo;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerExpander;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerGameState;
import cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.memeff.MemEffAbstractedInformationSet;
import cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.memeff.cfr.PureOOSData;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.iinodes.ImperfectRecallISKey;
import cz.agents.gtlibrary.iinodes.PerfectRecallISKey;
import cz.agents.gtlibrary.interfaces.*;

import java.io.Serializable;
import java.util.*;

public class FrequencyFPIRA extends FPIRA {

    public static void main(String[] args) {
//        runGenericPoker();
        runIIGoofspiel();
    }

    public static void runGenericPoker() {
        GameState root = new GenericPokerGameState();
        Expander<MCTSInformationSet> expander = new GenericPokerExpander<>(new FPIRAConfig());
        FPIRA fpira = new FrequencyFPIRA(root, expander, new GPGameInfo());

        fpira.runIterations(1000000);
    }

    public static void runIIGoofspiel() {
        GameState root = new IIGoofSpielGameState();
        Expander<MCTSInformationSet> expander = new GoofSpielExpander<>(new FPIRAConfig());
        FPIRA fpira = new FrequencyFPIRA(root, expander, new GSGameInfo());

        fpira.runIterations(1000000);
    }

    public FrequencyFPIRA(GameState rootState, Expander<? extends InformationSet> expander, GameInfo info) {
        super(rootState, expander, info);
    }

    @Override
    protected void updateAbstractionInformationSets(GameState state, Map<Action, Double> bestResponse, Map<ISKey, double[]> opponentStrategy, Player opponent) {
        Map<InformationSet, Map<Integer, Set<PerfectRecallISKey>>> toSplit = new HashMap<>();
        Player currentPlayer = state.getAllPlayers()[1 - opponent.getId()];

        updateISStructureFrequency(state, bestResponse, opponentStrategy, opponent, toSplit, 1, 1);
        splitISsAccordingToBRFrequency(toSplit, currentPlayer);
        assert toSplit.values().stream().allMatch(map -> map.size() == 1);
        if (aboveDelta(getStrategyDiffsFrequency(toSplit), getBehavioralStrategyFor(currentPlayer), currentPlayer)) {
            splitISsToPRFrequency(toSplit, currentPlayer);
        } else {
            updateRegrets(toSplit);
        }
    }

    private void updateRegrets(Map<InformationSet, Map<Integer, Set<PerfectRecallISKey>>> toSplit) {
        for (Map.Entry<InformationSet, Map<Integer, Set<PerfectRecallISKey>>> actionMapEntry : toSplit.entrySet()) {
            for (Map.Entry<Integer, Set<PerfectRecallISKey>> entry : actionMapEntry.getValue().entrySet()) {
                ((IRCFRInformationSet) actionMapEntry.getKey()).getData().addToRegretAtIndex(entry.getKey(), 1);
            }
        }
    }

    protected void splitISsAccordingToBRFrequency(Map<InformationSet, Map<Integer, Set<PerfectRecallISKey>>> toSplit, Player player) {
        Map<InformationSet, Map<Integer, Set<PerfectRecallISKey>>> toSplitAdd = new HashMap<>();
        Map<InformationSet, Set<Integer>> toSplitRemove = new HashMap<>();

        toSplit.entrySet().stream().filter(informationSetMapEntry -> informationSetMapEntry.getValue().size() > 1).forEach(informationSetMapEntry -> {
            for (Map.Entry<Integer, Set<PerfectRecallISKey>> entry : informationSetMapEntry.getValue().entrySet()) {
                Set<GameState> isStates = informationSetMapEntry.getKey().getAllStates();
                Set<GameState> toRemove = new HashSet<>();

                for (PerfectRecallISKey key : entry.getValue()) {
                    isStates.stream().filter(isState -> isState.getISKeyForPlayerToMove().equals(key)).forEach(toRemove::add);
                }
                if (toRemove.isEmpty())
                    continue;
                IRCFRInformationSet newIS;

                if (toRemove.size() == isStates.size())
                    newIS = (IRCFRInformationSet) informationSetMapEntry.getKey();
                else {
                    isStates.removeAll(toRemove);
                    ((MemEffAbstractedInformationSet) informationSetMapEntry.getKey()).getAbstractedKeys().removeAll(entry.getValue());
                    newIS = createNewIS(toRemove, player, ((IRCFRInformationSet) informationSetMapEntry.getKey()).getData());
                }
                toSplitAdd.computeIfAbsent(newIS, key ->  new HashMap<>()).put(entry.getKey(), entry.getValue());
                toSplitRemove.computeIfAbsent(informationSetMapEntry.getKey(), key -> new HashSet<>()).add(entry.getKey());
            }
        });
        toSplit.putAll(toSplitAdd);
        toSplitRemove.forEach((k, v) -> {
            Map<Integer, Set<PerfectRecallISKey>> map = toSplit.get(k);

            v.forEach(actionIndex -> map.remove(actionIndex));
        });
        toSplit.entrySet().removeIf(e -> e.getValue().isEmpty());
    }


    protected void splitISsToPRFrequency(Map<InformationSet, Map<Integer, Set<PerfectRecallISKey>>> toSplit, Player player) {
        for (Map.Entry<InformationSet, Map<Integer, Set<PerfectRecallISKey>>> actionMapEntry : toSplit.entrySet()) {
            for (Map.Entry<Integer, Set<PerfectRecallISKey>> entry : actionMapEntry.getValue().entrySet()) {
                for (PerfectRecallISKey isKey : entry.getValue()) {
                    Set<GameState> isStates = actionMapEntry.getKey().getAllStates();
                    Set<GameState> toRemove = new HashSet<>();

                    isStates.stream().filter(isState -> isState.getISKeyForPlayerToMove().equals(isKey)).forEach(toRemove::add);
                    if (toRemove.isEmpty())
                        continue;
                    int actionIndex = entry.getKey();

                    assert actionIndex > -1;
                    if (toRemove.size() == isStates.size()) {
                        ((IRCFRInformationSet) actionMapEntry.getKey()).getData().addToRegretAtIndex(actionIndex, 1);
                    } else {
                        isStates.removeAll(toRemove);
                        ((MemEffAbstractedInformationSet) actionMapEntry.getKey()).getAbstractedKeys().remove(isKey);
                        IRCFRInformationSet newIS = createNewIS(toRemove, player, ((IRCFRInformationSet) actionMapEntry.getKey()).getData());

                        newIS.getData().addToRegretAtIndex(actionIndex, 1);
                    }
                }
            }
        }
    }

    protected int updateISStructureFrequency(GameState state, Map<Action, Double> bestResponse, Map<ISKey, double[]> opponentStrategy,
                                             Player opponent, Map<InformationSet, Map<Integer, Set<PerfectRecallISKey>>> toSplit,
                                             double pBR, double pAvg) {
        if (state.isGameEnd())
            return 0;
        if (state.isPlayerToMoveNature())
            return perfectRecallExpander.getActions(state).stream().map(state::performAction).mapToInt(s -> updateISStructureFrequency(s, bestResponse, opponentStrategy, opponent, toSplit, pBR, pAvg)).sum();
        if (state.getPlayerToMove().equals(opponent)) {
            List<Action> actions = perfectRecallExpander.getActions(state);

            return actions.stream().filter(a -> getProbability(opponentStrategy, a, actions) > 1e-8)
                    .map(state::performAction).mapToInt(s -> updateISStructureFrequency(s, bestResponse, opponentStrategy, opponent, toSplit, pBR, pAvg)).sum();
        }
        IRCFRInformationSet is = currentAbstractionInformationSets.get(getAbstractionISKey(state));
        List<Action> actions = perfectRecallExpander.getActions(state);
        double[] meanStrategy = is.getData().getRMStrategy();

        assert actions.stream().filter(a -> bestResponse.getOrDefault(a, 0d) > 1 - 1e-8).count() <= 1;
        assert Math.abs(Arrays.stream(meanStrategy).sum() - 1) < 1e-8 || (Math.abs(Arrays.stream(meanStrategy).sum()) < 1e-8 && iteration == 0);
        int splitCount = perfectRecallExpander.getActions(state).stream().filter(a -> getProbability(meanStrategy, a, actions) > 1e-8 || bestResponse.getOrDefault(a, 0d) > 1 - 1e-8)
                .mapToInt(a -> updateISStructureFrequency(state.performAction(a), bestResponse, opponentStrategy, opponent, toSplit, bestResponse.getOrDefault(a, 0d), pAvg * getProbability(meanStrategy, a, actions))).sum();
        Set<GameState> isStates = is.getAllStates();

        if (pBR > 1 - 1e-8 && isStates.stream().filter(isState -> isState.getISKeyForPlayerToMove().equals(state.getISKeyForPlayerToMove())).count() != isStates.size()) {
            Action currentStateBestResponseAction = actions.stream().filter(a -> bestResponse.getOrDefault(a, 0d) > 1 - 1e-8).findAny().get();
            int actionIndex = getIndex(actions, currentStateBestResponseAction);
            Map<Integer, Set<PerfectRecallISKey>> actionMap = toSplit.compute(is, (k, v) -> v == null ? new HashMap<>() : v);
            Set<PerfectRecallISKey> updateISs = actionMap.compute(actionIndex, (k, v) -> v == null ? new HashSet<>() : v);

            updateISs.add((PerfectRecallISKey) state.getISKeyForPlayerToMove());
            return splitCount + 1;
        } else {
            if (pBR > 1 - 1e-8) {
                Action currentStateBestResponseAction = actions.stream().filter(a -> bestResponse.getOrDefault(a, 0d) > 1 - 1e-8).findAny().get();
                int actionIndex = getIndex(actions, currentStateBestResponseAction);

                is.getData().addToRegretAtIndex(actionIndex, 1); //this might increment differently based on strategies?
            }
        }
        return splitCount;
    }

    protected FPIRAStrategyDiffs getStrategyDiffsFrequency(Map<InformationSet, Map<Integer, Set<PerfectRecallISKey>>> toSplit) {
        FPIRAStrategyDiffs strategyDiffs = new FPIRAStrategyDiffs();

        for (Map.Entry<InformationSet, Map<Integer, Set<PerfectRecallISKey>>> actionMapEntry : toSplit.entrySet()) {
            for (Map.Entry<Integer, Set<PerfectRecallISKey>> entry : actionMapEntry.getValue().entrySet()) {
                int actionCount = ((IRCFRInformationSet) actionMapEntry.getKey()).getData().getActionCount();
                int actionIndex = entry.getKey();
                double[] meanStratDiff = new double[actionCount];
                double[] regrets = ((IRCFRInformationSet) actionMapEntry.getKey()).getData().getRegrets();
                double regretSum = Arrays.stream(((IRCFRInformationSet) actionMapEntry.getKey()).getData().getRegrets()).sum();

                meanStratDiff[actionIndex] = (regrets[actionIndex] + 1) / (regretSum + 1) - regrets[actionIndex] / regretSum;
                for (int i = 0; i < actionCount; i++) {
                    if (actionIndex == i)
                        continue;
                    meanStratDiff[i] = (regrets[i]) / (regretSum + 1) - regrets[i] / regretSum;
                }
                entry.getValue().forEach(key -> strategyDiffs.prStrategyDiff.put(key, meanStratDiff));
                ((MemEffAbstractedInformationSet) actionMapEntry.getKey()).getAbstractedKeys().stream().filter(key -> entry.getValue().contains(key)).forEach(key ->
                        strategyDiffs.irStrategyDiff.put(key, meanStratDiff)
                );
            }
        }
        return strategyDiffs;
    }

    @Override
    protected void addData(Collection<MemEffAbstractedInformationSet> informationSets) {
        informationSets.stream()
                .filter(i -> i.getPlayer().getId() != 2)
                .forEach(i -> i.setData(new PureOOSData(this.perfectRecallExpander.getActions(i.getAllStates().stream().findAny().get()).size())));
    }

    protected Map<ISKey, double[]> getBehavioralStrategyFor(Player player) {
        Map<ISKey, double[]> strategy = new HashMap<>(currentAbstractionInformationSets.size() / 2);

        currentAbstractionInformationSets.values().stream().filter(is -> is.getPlayer().equals(player)).forEach(is ->
                strategy.put(is.getISKey(), is.getData().getRMStrategy())
        );
        return strategy;
    }

    protected IRCFRInformationSet createNewIS(Set<GameState> states, Player player, OOSAlgorithmData data) {
        ImperfectRecallISKey newISKey = createCounterISKey(player);
        GameState state = states.stream().findAny().get();
        MemEffAbstractedInformationSet is = createInformationSet(state, newISKey);

        is.addAllStatesToIS(states);
        is.setData(new PureOOSData(data));
        currentAbstractionInformationSets.put(newISKey, is);
        states.forEach(s -> currentAbstractionISKeys.put((PerfectRecallISKey) s.getISKeyForPlayerToMove(), newISKey));
        return is;
    }
}
