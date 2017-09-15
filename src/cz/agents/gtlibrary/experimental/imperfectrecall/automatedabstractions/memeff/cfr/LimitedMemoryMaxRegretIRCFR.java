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
import cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.memeff.MemEffAbstractedInformationSet;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.interfaces.*;

import java.util.*;
import java.util.stream.Collectors;

public class LimitedMemoryMaxRegretIRCFR extends MaxRegretIRCFR {

    public static void main(String[] args) {
        runGenericPoker();
//        runIIGoofspiel();
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

    public static int sizeLimit = 100;
    private Random random;
    private Set<ISKey> toUpdate;
    private boolean bellowLimit;

    public LimitedMemoryMaxRegretIRCFR(GameState rootState, Expander<? extends InformationSet> perfectRecallExpander, GameInfo info, MCTSConfig perfectRecallConfig) {
        super(rootState, perfectRecallExpander, info, perfectRecallConfig);
        random = new Random(1);
        toUpdate = new HashSet<>(sizeLimit);
        bellowLimit = false;
    }

    @Override
    protected void iteration(Player player) {
        findISsToUpdate(player);
        if (SIMULTANEOUS_PR_IR)
            perfectAndImperfectRecallIteration(rootState, 1, 1, player);
        else
            imperfectRecallIteration(rootState, 1, 1, player);
        updateImperfectRecallData();
        if (!SIMULTANEOUS_PR_IR)
            computeCurrentRegrets(rootState, 1, 1, player);
        if (REGRET_MATCHING_PLUS)
            removeNegativePRRegrets();
        updateAbstraction();
        if (DELETE_REGRETS)
            prRegrets.clear();
        toUpdate.clear();
        System.gc();
    }

    protected void findISsToUpdate(Player player) {
        if(bellowLimit)
            return;
        List<MemEffAbstractedInformationSet> imperfectRecallSetsForPlayer = currentAbstractionInformationSets.values().stream()
                .filter(i -> i.getPlayer().getId() == player.getId())
                .filter(i -> i.getAbstractedKeys().size() > 1)
                .collect(Collectors.toList());
        int abstractedISCount = getAbstractedISCount(imperfectRecallSetsForPlayer);

        if (abstractedISCount <= sizeLimit) {
            bellowLimit = true;
        } else {
            Collections.shuffle(imperfectRecallSetsForPlayer, random);
            for (MemEffAbstractedInformationSet informationSet : imperfectRecallSetsForPlayer) {
                List<ISKey> collect = new ArrayList<>(informationSet.getAbstractedKeys());

                if (collect.size() + toUpdate.size() > sizeLimit) {
                    Collections.shuffle(collect, random);
                    collect.subList(Math.min(sizeLimit - toUpdate.size(), collect.size()), collect.size()).clear();
                }
                toUpdate.addAll(collect);
            }
        }
    }

    private int getAbstractedISCount(List<MemEffAbstractedInformationSet> abstractedInformationSets) {
        return (int) abstractedInformationSets.stream()
                .flatMap(i -> i.getAbstractedKeys().stream())
                .count();
    }

    @Override
    protected void updateCurrentRegrets(GameState node, double pi1, double pi2, Player expPlayer, double[] expectedValuesForActions, double expectedValue) {
        if (bellowLimit || toUpdate.contains(node.getISKeyForPlayerToMove()))
            super.updateCurrentRegrets(node, pi1, pi2, expPlayer, expectedValuesForActions, expectedValue);
    }
}
