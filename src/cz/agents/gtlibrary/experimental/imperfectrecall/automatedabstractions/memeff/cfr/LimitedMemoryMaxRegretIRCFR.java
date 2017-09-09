package cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.memeff.cfr;

import cz.agents.gtlibrary.algorithms.cfr.ir.IRCFRInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.MCTSConfig;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.domain.goofspiel.GSGameInfo;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielExpander;
import cz.agents.gtlibrary.domain.goofspiel.IIGoofSpielGameState;
import cz.agents.gtlibrary.domain.poker.generic.GPGameInfo;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerExpander;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerGameState;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.interfaces.*;

import java.util.*;
import java.util.stream.Collectors;

public class LimitedMemoryMaxRegretIRCFR extends MaxRegretIRCFR {

    public static void main(String[] args) {
        runGenericPoker();
    }

    public static void runGenericPoker() {
        GameState root = new GenericPokerGameState();
        MCTSConfig config = new MCTSConfig();
        Expander<MCTSInformationSet> expander = new GenericPokerExpander<>(config);
        GameInfo info = new GPGameInfo();
        MaxRegretIRCFR alg = new LimitedMemoryMaxRegretIRCFR(root, expander, info, config);

        alg.runIterations(10000000);
    }

    public static void runIIGoofspiel() {
        GameState root = new IIGoofSpielGameState();
        MCTSConfig config = new MCTSConfig();
        Expander<MCTSInformationSet> expander = new GoofSpielExpander<>(config);
        GameInfo info = new GSGameInfo();
        MaxRegretIRCFR alg = new LimitedMemoryMaxRegretIRCFR(root, expander, info, config);

        alg.runIterations(10000000);
    }


    private Random random;
    public static int sizeLimit = 100;
    private Set<ISKey> toUpdate;

    public LimitedMemoryMaxRegretIRCFR(GameState rootState, Expander<? extends InformationSet> perfectRecallExpander, GameInfo info, MCTSConfig perfectRecallConfig) {
        super(rootState, perfectRecallExpander, info, perfectRecallConfig);
        random = new Random(1);
        toUpdate = new HashSet<>(sizeLimit);
    }

    @Override
    protected void iteration(Player player) {
        findISsToUpdate(player);
        super.iteration(player);
    }

    protected void findISsToUpdate(Player player) {
        toUpdate.clear();
        int abstractedISCount = getAbstractedISCount(player);
        int maxAllowed = Math.min(abstractedISCount, sizeLimit);

        if (abstractedISCount <= sizeLimit) {
            toUpdate = currentAbstractionInformationSets.values().stream().filter(i -> i.getPlayer().getId() == player.getId())
                    .filter(i -> i.getAllStates().stream().map(s -> s.getISKeyForPlayerToMove()).distinct().count() > 1)
                    .flatMap(i -> i.getAllStates().stream().map(s -> s.getISKeyForPlayerToMove()).distinct()).collect(Collectors.toSet());
        } else {
            while (toUpdate.size() < maxAllowed) {
                InformationSet randomAbstractedIS = getRandomAbstractedIS(player);
                List<ISKey> collect = randomAbstractedIS.getAllStates().stream().map(s -> s.getISKeyForPlayerToMove()).distinct().collect(Collectors.toList());

                if (collect.size() + toUpdate.size() > maxAllowed) {
                    Collections.shuffle(collect, random);
                    collect.subList(Math.min(maxAllowed - toUpdate.size(), collect.size()), collect.size()).clear();
                }
                toUpdate.addAll(collect);
            }
        }
    }

    private int getAbstractedISCount(Player player) {
        return (int) currentAbstractionInformationSets.values().stream().filter(i -> i.getPlayer().getId() == player.getId())
                .filter(i -> i.getAllStates().stream().map(s -> s.getISKeyForPlayerToMove()).distinct().count() > 1)
                .flatMap(i -> i.getAllStates().stream().map(s -> s.getISKeyForPlayerToMove()).distinct())
                .count();
    }

    private InformationSet getRandomAbstractedIS(Player player) {
        double randomVal = random.nextDouble();
        int playerISCount = (int) currentAbstractionInformationSets.values().stream().filter(i -> i.getPlayer().getId() == player.getId()).filter(i -> i.getAllStates().stream().map(s -> s.getISKeyForPlayerToMove()).distinct().count() > 1).count();

        for (IRCFRInformationSet informationSet : currentAbstractionInformationSets.values()) {
            if (informationSet.getPlayer().getId() != player.getId() || informationSet.getAllStates().stream().map(s -> s.getISKeyForPlayerToMove()).distinct().count() == 1)
                continue;
            randomVal -= 1. / playerISCount;
            if (randomVal <= 0)
                return informationSet;
        }
        return null;
    }


    @Override
    protected void updateCurrentRegrets(GameState node, double pi1, double pi2, Player expPlayer, double[] expectedValuesForActions, double expectedValue) {
        if (toUpdate.contains(node.getISKeyForPlayerToMove()))
            super.updateCurrentRegrets(node, pi1, pi2, expPlayer, expectedValuesForActions, expectedValue);
    }
}
