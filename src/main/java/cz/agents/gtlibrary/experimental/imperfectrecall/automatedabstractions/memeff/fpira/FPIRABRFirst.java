package cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.memeff.fpira;

import cz.agents.gtlibrary.algorithms.cfr.ir.IRCFRInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.domain.goofspiel.GSGameInfo;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielExpander;
import cz.agents.gtlibrary.domain.goofspiel.IIGoofSpielGameState;
import cz.agents.gtlibrary.domain.poker.generic.GPGameInfo;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerExpander;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerGameState;
import cz.agents.gtlibrary.domain.poker.kuhn.KPGameInfo;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerExpander;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerGameState;
import cz.agents.gtlibrary.domain.pursuit.PursuitExpander;
import cz.agents.gtlibrary.domain.pursuit.PursuitGameInfo;
import cz.agents.gtlibrary.domain.pursuit.VisibilityPursuitGameState;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameExpander;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameInfo;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameState;
import cz.agents.gtlibrary.domain.wichardtne.PerfectInformationWichardtState;
import cz.agents.gtlibrary.domain.wichardtne.WichardtExpander;
import cz.agents.gtlibrary.domain.wichardtne.WichardtGameInfo;
import cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.CFRBRData;
import cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.memeff.AutomatedAbstractionData;
import cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.memeff.MemEffAbstractedInformationSet;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.iinodes.PerfectRecallISKey;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.io.GambitEFG;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FPIRABRFirst extends FPIRA {

    public static void main(String[] args) {
//        runGenericPoker();
//        runKuhnPoker();
        runRandomGame();
//        runWichardtCounterexample();
//        runIIGoofspiel();
    }

    public static void runIIGoofspiel() {
        GameState root = new IIGoofSpielGameState();
        Expander<MCTSInformationSet> expander = new GoofSpielExpander<>(new FPIRAConfig());
        FPIRA fpira = new FPIRABRFirst(root, expander, new GSGameInfo());

        fpira.runIterations(1000000);
    }

    public static void runIIGoofspiel(String backupFileName) {
        GameState root = new IIGoofSpielGameState();
        Expander<MCTSInformationSet> expander = new GoofSpielExpander<>(new FPIRAConfig());

        try {
            FileInputStream fin = new FileInputStream(backupFileName);
            ObjectInputStream oos = new ObjectInputStream(fin);
            AutomatedAbstractionData data = (AutomatedAbstractionData) oos.readObject();
            FPIRA fpira = new FPIRABRFirst(root, expander, new GSGameInfo(), data);

            fpira.runIterations(1000000);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void runVisibilityPursuit() {
        GameState root = new VisibilityPursuitGameState();
        Expander<MCTSInformationSet> expander = new PursuitExpander<>(new FPIRAConfig());
        FPIRA fpira = new FPIRABRFirst(root, expander, new PursuitGameInfo());

        fpira.runIterations(1000000);
    }

    public static void runGenericPoker() {
        GameState root = new GenericPokerGameState();
        Expander<MCTSInformationSet> expander = new GenericPokerExpander<>(new FPIRAConfig());
        FPIRA fpira = new FPIRABRFirst(root, expander, new GPGameInfo());

        fpira.runIterations(1000000);
    }

    public static void runGenericPoker(String backupFileName) {
        GameState root = new GenericPokerGameState();
        Expander<MCTSInformationSet> expander = new GenericPokerExpander<>(new FPIRAConfig());
        try {
            FileInputStream fin = new FileInputStream(backupFileName);
            ObjectInputStream oos = new ObjectInputStream(fin);
            AutomatedAbstractionData data = (AutomatedAbstractionData) oos.readObject();
            FPIRA fpira = new FPIRABRFirst(root, expander, new GPGameInfo(), data);

            fpira.runIterations(1000000);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void runKuhnPoker() {
        GameState root = new KuhnPokerGameState();
        Expander<MCTSInformationSet> expander = new KuhnPokerExpander<>(new FPIRAConfig());
        FPIRA fpira = new FPIRABRFirst(root, expander, new KPGameInfo());

        fpira.runIterations(1000000);
    }

    public static void runRandomGame() {
        GameState root = new RandomGameState();
        Expander<MCTSInformationSet> expander = new RandomGameExpander<>(new FPIRAConfig());
        FPIRA fpira = new FPIRABRFirst(root, expander, new RandomGameInfo());

        fpira.runIterations(1000000);
        GambitEFG gambit = new GambitEFG();

        gambit.write("FPIRAtest.gbt", root, expander);
    }

    public static void runWichardtCounterexample() {
        GameState root = new PerfectInformationWichardtState();
        Expander<MCTSInformationSet> expander = new WichardtExpander<>(new FPIRAConfig());
        FPIRA fpira = new FPIRABRFirst(root, expander, new WichardtGameInfo());

        fpira.runIterations(100000);
    }

    public FPIRABRFirst(GameState rootState, Expander<? extends InformationSet> expander, GameInfo info) {
        super(rootState, expander, info);
    }

    public FPIRABRFirst(GameState rootState, Expander<? extends InformationSet> expander, GameInfo info, AutomatedAbstractionData data) {
        super(rootState, expander, info, data);
    }

    protected void updateAbstractionInformationSets(GameState state, Map<Action, Double> bestResponse, Map<ISKey, double[]> opponentStrategy, Player opponent) {
        Map<InformationSet, Map<Integer, Map<PerfectRecallISKey, double[]>>> toSplit = new HashMap<>();
        Player currentPlayer = state.getAllPlayers()[1 - opponent.getId()];

        updateISStructure(state, bestResponse, opponentStrategy, opponent, toSplit, 1, 1);
        currentAbstractionInformationSets.values().stream().filter(i -> i.getPlayer().getId() != 2).forEach(i -> ((CFRBRData) i.getData()).updateMeanStrategy());
        splitISsAccordingToBR(toSplit, currentPlayer);
        assert toSplit.values().stream().allMatch(map -> map.size() == 1);
        if (aboveDelta(getStrategyDiffs(toSplit), getBehavioralStrategyFor(currentPlayer), currentPlayer)) {
            splitISsToPR(toSplit, currentPlayer);
        } else {
            updateRegrets(toSplit);
        }
    }

    private void updateRegrets(Map<InformationSet, Map<Integer, Map<PerfectRecallISKey, double[]>>> toSplit) {
        for (Map.Entry<InformationSet, Map<Integer, Map<PerfectRecallISKey, double[]>>> actionMapEntry : toSplit.entrySet()) {
            for (Map.Entry<Integer, Map<PerfectRecallISKey, double[]>> entry : actionMapEntry.getValue().entrySet()) {
                for (Map.Entry<PerfectRecallISKey, double[]> isKeyEntry : entry.getValue().entrySet()) {
                    CFRBRData data = (CFRBRData) ((IRCFRInformationSet)actionMapEntry.getKey()).getData();
                    double[] meanStrategy = data.getMp();

                    for (int i = 0; i < data.getActionCount(); i++) {
                        data.addToMeanStrategyUpdateNumerator(i, isKeyEntry.getValue()[0] * ((i == entry.getKey() ? 1 : 0) - meanStrategy[i]));
                    }
                    data.addToMeanStrategyUpdateDenominator(isKeyEntry.getValue()[0] + isKeyEntry.getValue()[1]);
                    data.updateMeanStrategy();
                }
            }
        }
    }

    protected void splitISsAccordingToBR(Map<InformationSet, Map<Integer, Map<PerfectRecallISKey, double[]>>> toSplit, Player player) {
        Map<InformationSet, Map<Integer, Map<PerfectRecallISKey, double[]>>> toSplitAdd = new HashMap<>();
        Map<InformationSet, Set<Integer>> toSplitRemove = new HashMap<>();

        for (Map.Entry<InformationSet, Map<Integer, Map<PerfectRecallISKey, double[]>>> informationSetMapEntry : toSplit.entrySet()) {
            if (informationSetMapEntry.getValue().size() > 1) {
                for (Map.Entry<Integer, Map<PerfectRecallISKey, double[]>> entry : informationSetMapEntry.getValue().entrySet()) {
                    Set<GameState> isStates = informationSetMapEntry.getKey().getAllStates();
                    Set<GameState> toRemove = new HashSet<>();

                    for (PerfectRecallISKey key : entry.getValue().keySet()) {
                        isStates.stream().filter(isState -> isState.getISKeyForPlayerToMove().equals(key)).forEach(toRemove::add);
                    }
                    if (toRemove.isEmpty())
                        continue;
                    IRCFRInformationSet newIS;
                    if (toRemove.size() == isStates.size())
                        newIS = (IRCFRInformationSet) informationSetMapEntry.getKey();
                    else {
                        isStates.removeAll(toRemove);
                        ((MemEffAbstractedInformationSet)informationSetMapEntry.getKey()).getAbstractedKeys().removeAll(entry.getValue().keySet());
                        newIS = createNewIS(toRemove, player, (CFRBRData) ((IRCFRInformationSet) informationSetMapEntry.getKey()).getData());
                    }
                    toSplitAdd.computeIfAbsent(newIS, key ->  new HashMap<>()).put(entry.getKey(), entry.getValue());
                    toSplitRemove.computeIfAbsent(informationSetMapEntry.getKey(), key -> new HashSet<>()).add(entry.getKey());
                }
            }
        }
        toSplit.putAll(toSplitAdd);
        toSplitRemove.forEach((k, v) -> {
            Map<Integer, Map<PerfectRecallISKey, double[]>> map = toSplit.get(k);

            v.forEach(actionIndex -> map.remove(actionIndex));
        });
        toSplit.entrySet().removeIf(e -> e.getValue().isEmpty());
    }

}
