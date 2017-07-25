/*
Copyright 2014 Faculty of Electrical Engineering at CTU in Prague

This file is part of Game Theoretic Library.

Game Theoretic Library is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Game Theoretic Library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with Game Theoretic Library.  If not, see <http://www.gnu.org/licenses/>.*/


/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions;

import cz.agents.gtlibrary.algorithms.cfr.ir.FixedForIterationData;
import cz.agents.gtlibrary.algorithms.cfr.ir.IRCFR;
import cz.agents.gtlibrary.algorithms.cfr.ir.IRCFRConfig;
import cz.agents.gtlibrary.algorithms.cfr.ir.IRCFRInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.oos.OOSAlgorithmData;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.BackPropFactory;
import cz.agents.gtlibrary.algorithms.sequenceform.FullSequenceEFG;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.domain.bpg.BPGExpander;
import cz.agents.gtlibrary.domain.bpg.BPGGameInfo;
import cz.agents.gtlibrary.domain.bpg.imperfectrecall.IRBPGGameState;
import cz.agents.gtlibrary.domain.goofspiel.GSGameInfo;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielExpander;
import cz.agents.gtlibrary.domain.goofspiel.ir.IRGoofSpielGameState;
import cz.agents.gtlibrary.domain.poker.kuhn.KPGameInfo;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerExpander;
import cz.agents.gtlibrary.domain.poker.kuhn.ir.IRKuhnPokerGameState;
import cz.agents.gtlibrary.domain.randomabstraction.IDObservation;
import cz.agents.gtlibrary.domain.randomabstraction.P1RandomAbstractionGameStateFactory;
import cz.agents.gtlibrary.domain.randomabstraction.P1RandomAlossAbstractionGameStateFactory;
import cz.agents.gtlibrary.domain.randomabstraction.RandomAbstractionExpander;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameExpander;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameInfo;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameState;
import cz.agents.gtlibrary.domain.randomgameimproved.io.BasicGameBuilder;
import cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.cprr.CPRRExpander;
import cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.cprr.CPRRGameState;
import cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.flexibleisdomain.FlexibleISKeyExpander;
import cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.flexibleisdomain.FlexibleISKeyGameState;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.br.ALossBestResponseAlgorithm;
import cz.agents.gtlibrary.iinodes.GameStateImpl;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.iinodes.ImperfectRecallISKey;
import cz.agents.gtlibrary.iinodes.Observations;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.Pair;
import cz.agents.gtlibrary.utils.io.GambitEFG;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.*;
import java.util.stream.Collectors;

public class ALossPRCFRBR implements GamePlayingAlgorithm {

    public static final boolean UPDATE_IS_STRUCTURE = false;
    protected int maxBRInformationSets = -1;

    public static void main(String[] args) {
//        runIRKuhnPoker();
//        runAlossRandomAbstractionGame();
//        runRandomAbstractionGame();
//        runIRBPG();
//        runIRGoofspiel();
//        runAlossCPRRRandomAbstractionGame();
        runCPRRRandomAbstractionGame();
    }

    protected static void runIRBPG() {
        GameState root = new IRBPGGameState();
        Expander<IRCFRInformationSet> expander = new BPGExpander<>(new IRCFRConfig());
        ALossPRCFRBR cfr = new ALossPRCFRBR(root.getAllPlayers()[1], root, expander, new BPGGameInfo());

        cfr.runIterations(10000);
    }

    protected static void runIRGoofspiel() {
        GameState root = new IRGoofSpielGameState();
        Expander<IRCFRInformationSet> expander = new GoofSpielExpander<>(new IRCFRConfig());
        ALossPRCFRBR cfr = new ALossPRCFRBR(root.getAllPlayers()[1], root, expander, new GSGameInfo());

        cfr.runIterations(10000);
    }

    protected static void runIRKuhnPoker() {
        GameState root = new IRKuhnPokerGameState();
        Expander<IRCFRInformationSet> expander = new KuhnPokerExpander<>(new IRCFRConfig());
        BasicGameBuilder.build(root, expander.getAlgorithmConfig(), expander);
        ALossPRCFRBR cfr = new ALossPRCFRBR(root.getAllPlayers()[0], root, expander, new KPGameInfo());

        cfr.runIterations(100000);
    }

    protected static void runAlossRandomAbstractionGame() {
        GameState wrappedRoot = new RandomGameState();
        SequenceFormConfig<SequenceInformationSet> config = new SequenceFormConfig<>();
        Expander<SequenceInformationSet> wrappedExpander = new RandomGameExpander<>(config);

        FullSequenceEFG efg = new FullSequenceEFG(wrappedRoot, wrappedExpander, new RandomGameInfo(), config);
        efg.generateCompleteGame();

        System.out.println("Unabstracted IS count: " + config.getAllInformationSets().size());
        GameState root = new P1RandomAlossAbstractionGameStateFactory().createRoot(wrappedRoot, wrappedExpander.getAlgorithmConfig());
        Expander<IRCFRInformationSet> expander = new RandomAbstractionExpander<>(wrappedExpander, new IRCFRConfig());

        BasicGameBuilder.build(root, expander.getAlgorithmConfig(), expander);
        System.out.println("Abstracted IS count: " + expander.getAlgorithmConfig().getAllInformationSets().size());
        ALossPRCFRBR cfr = new ALossPRCFRBR(root.getAllPlayers()[1], root, expander, new KPGameInfo());

        cfr.runIterations(1000);
        GambitEFG gambit = new GambitEFG();

        gambit.write("cfrbrtest.gbt", root, expander);
        System.out.println("Unabstracted IS count: " + config.getAllInformationSets().size());
        System.out.println("Abstracted IS count: " + expander.getAlgorithmConfig().getAllInformationSets().size());
    }

    protected static void runRandomAbstractionGame() {
        GameState wrappedRoot = new RandomGameState();
        SequenceFormConfig<SequenceInformationSet> config = new SequenceFormConfig<>();
        Expander<SequenceInformationSet> wrappedExpander = new RandomGameExpander<>(config);

        FullSequenceEFG efg = new FullSequenceEFG(wrappedRoot, wrappedExpander, new RandomGameInfo(), config);
        efg.generateCompleteGame();

        System.out.println("Unabstracted IS count: " + config.getAllInformationSets().size());
        GameState root = new P1RandomAbstractionGameStateFactory().createRoot(wrappedRoot, wrappedExpander.getAlgorithmConfig());
        Expander<IRCFRInformationSet> expander = new RandomAbstractionExpander<>(wrappedExpander, new IRCFRConfig());

        BasicGameBuilder.build(root, expander.getAlgorithmConfig(), expander);
        System.out.println("Abstracted IS count: " + expander.getAlgorithmConfig().getAllInformationSets().size());
        ALossPRCFRBR cfr = new ALossPRCFRBR(root.getAllPlayers()[1], root, expander, new KPGameInfo());

        cfr.runIterations(300);
        GambitEFG gambit = new GambitEFG();

        gambit.write("cfrbrtest.gbt", root, expander);
        System.out.println("Unabstracted IS count: " + config.getAllInformationSets().size());
        System.out.println("Abstracted IS count: " + expander.getAlgorithmConfig().getAllInformationSets().size());
    }

    protected static void runAlossCPRRRandomAbstractionGame() {
        GameState wrappedRoot = new RandomGameState();
        SequenceFormConfig<SequenceInformationSet> config = new SequenceFormConfig<>();
        Expander<SequenceInformationSet> wrappedExpander = new RandomGameExpander<>(config);

        FullSequenceEFG efg = new FullSequenceEFG(wrappedRoot, wrappedExpander, new RandomGameInfo(), config);
        efg.generateCompleteGame();

        System.out.println("Unabstracted IS count: " + config.getAllInformationSets().size());
        GameState root = new P1RandomAlossAbstractionGameStateFactory().createRoot(wrappedRoot, wrappedExpander.getAlgorithmConfig());
        Expander<IRCFRInformationSet> expander = new RandomAbstractionExpander<>(wrappedExpander, new IRCFRConfig());


        BasicGameBuilder.build(root, expander.getAlgorithmConfig(), expander);
        System.out.println("Abstracted IS count: " + expander.getAlgorithmConfig().getAllInformationSets().size());
        GameState cprrRoot = new CPRRGameState(root);
        Expander<IRCFRInformationSet> cprrExpander = new CPRRExpander<>(expander);
        ALossPRCFRBR cfr = new ALossPRCFRBR(cprrRoot.getAllPlayers()[1], cprrRoot, cprrExpander, new KPGameInfo());

        cfr.runIterations(1000);
        GambitEFG gambit = new GambitEFG();

        gambit.write("cfrbrtest.gbt", cprrRoot, cprrExpander);
        System.out.println("Unabstracted IS count: " + config.getAllInformationSets().size());
    }

    protected static void runCPRRRandomAbstractionGame() {
        GameState wrappedRoot = new RandomGameState();
        SequenceFormConfig<SequenceInformationSet> config = new SequenceFormConfig<>();
        Expander<SequenceInformationSet> wrappedExpander = new RandomGameExpander<>(config);

        FullSequenceEFG efg = new FullSequenceEFG(wrappedRoot, wrappedExpander, new RandomGameInfo(), config);
        efg.generateCompleteGame();

        System.out.println("Unabstracted IS count: " + config.getAllInformationSets().size());
        GameState root = new P1RandomAbstractionGameStateFactory().createRoot(wrappedRoot, wrappedExpander.getAlgorithmConfig());
        Expander<IRCFRInformationSet> expander = new RandomAbstractionExpander<>(wrappedExpander, new IRCFRConfig());


        BasicGameBuilder.build(root, expander.getAlgorithmConfig(), expander);
        System.out.println("Abstracted IS count: " + expander.getAlgorithmConfig().getAllInformationSets().size());
        GameState cprrRoot = new CPRRGameState(root);
        Expander<IRCFRInformationSet> cprrExpander = new CPRRExpander<>(expander);
        ALossPRCFRBR cfr = new ALossPRCFRBR(cprrRoot.getAllPlayers()[1], cprrRoot, cprrExpander, new KPGameInfo());

        cfr.runIterations(1000);
        GambitEFG gambit = new GambitEFG();

        gambit.write("cfrbrtest.gbt", cprrRoot, cprrExpander);
        System.out.println("Unabstracted IS count: " + config.getAllInformationSets().size());
    }

    protected final boolean SPLIT_ONLY_DEEPEST = true;
    protected final double EPS = 0;
    protected Player regretMatchingPlayer;
    protected Player brPlayer;
    protected BackPropFactory fact;
    protected FlexibleISKeyGameState rootState;
    protected ThreadMXBean threadBean;
    protected Expander<IRCFRInformationSet> expander;
    protected AlgorithmConfig<IRCFRInformationSet> config;
    protected ALossBestResponseAlgorithm br;
    protected Map<GameState, ISKey> isKeys;
    protected int isKeyCounter;
    protected int iteration = 0;

    protected HashMap<ISKey, IRCFRInformationSet> informationSets = new HashMap<>();
    protected boolean firstIteration = true;

    public ALossPRCFRBR(Player regretMatchingPlayer, GameState rootState, Expander<IRCFRInformationSet> expander, GameInfo info) {
        isKeys = new HashMap<>();
        this.regretMatchingPlayer = regretMatchingPlayer;
        this.brPlayer = info.getOpponent(regretMatchingPlayer);
        this.rootState = new FlexibleISKeyGameState((GameStateImpl) rootState, isKeys);
        this.expander = new FlexibleISKeyExpander<>(expander, new IRCFRConfig(), informationSets);
        this.config = this.expander.getAlgorithmConfig();
        BasicGameBuilder.build(rootState, expander.getAlgorithmConfig(), expander);
//        BasicGameBuilder.build(this.rootState, this.expander.getAlgorithmConfig(), this.expander);
        br = new ALossBestResponseAlgorithm(this.rootState, this.expander, 1 - regretMatchingPlayer.getId(), new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]}, config, info, false);
        threadBean = ManagementFactory.getThreadMXBean();
        isKeyCounter = Integer.MIN_VALUE;
    }

    @Override
    public Action runMiliseconds(int miliseconds) {
        int iters = 0;
        long start = threadBean.getCurrentThreadCpuTime();
        while ((threadBean.getCurrentThreadCpuTime() - start) / 1e6 < miliseconds) {
            System.out.println(regretMatchingIteration(rootState, 1, 1, rootState.getAllPlayers()[0]));
            iters++;
            System.out.println(regretMatchingIteration(rootState, 1, 1, rootState.getAllPlayers()[1]));
            iters++;
        }
        firstIteration = false;
        System.out.println();
        System.out.println("Iters: " + iters);
        return null;
    }

    public Action runIterations(int iterations) {
        for (int i = 0; i < iterations; i++) {
            this.iteration++;
            if (regretMatchingPlayer.equals(rootState.getAllPlayers()[0])) {
                System.out.println("rm: " + regretMatchingIteration(rootState, 1, 1, rootState.getAllPlayers()[0]));
                update();
                System.out.println("br: " + bestResponseIteration(regretMatchingPlayer, br));
            } else {
                regretMatchingIteration(rootState, 1, 1, rootState.getAllPlayers()[1]);
                update();
                bestResponseIteration(regretMatchingPlayer, br);
            }
            if (i % 20 == 0) {
                Map<Action, Double> strategy = getStrategyFor(regretMatchingPlayer);

//                System.out.println(strategy);
//                System.out.println(IRCFR.getStrategyFor(rootState, rootState.getAllPlayers()[1 - regretMatchingPlayer.getId()], new MeanStratDist(), config.getAllInformationSets(), expander));
                System.out.println("exp val against br: " + -br.calculateBR(rootState, strategy));

//                Map<Action, Double> bestResponse = br.getBestResponse();
                Map<Action, Double> averageBestResponse = getStrategyFor(brPlayer);
                System.out.println("exp val avg vs avg: " + computeExpectedValue(rootState, strategy, averageBestResponse));
                System.out.println("Current IS count: " + config.getAllInformationSets().size());
            }
        }
        firstIteration = false;
        System.out.println("Orig IS count: " + ((FlexibleISKeyExpander) expander).getWrappedExpander().getAlgorithmConfig().getAllInformationSets().size());
        System.out.println("New IS count: " + expander.getAlgorithmConfig().getAllInformationSets().size());
        return null;
    }

    protected double computeExpectedValue(GameState state, Map<Action, Double> p0Strategy, Map<Action, Double> p1Strategy) {
        if (state.isGameEnd())
            return state.getUtilities()[regretMatchingPlayer.getId()];
        if (state.isPlayerToMoveNature())
            return expander.getActions(state).stream()
                    .mapToDouble(a -> state.getProbabilityOfNatureFor(a) * computeExpectedValue(state.performAction(a), p0Strategy, p1Strategy)).sum();
        if (state.getPlayerToMove().equals(state.getAllPlayers()[0]))
            return expander.getActions(state).stream().filter(a -> p0Strategy.getOrDefault(a, 0d) > 1e-8)
                    .mapToDouble(a -> p0Strategy.get(a) * computeExpectedValue(state.performAction(a), p0Strategy, p1Strategy)).sum();
        return expander.getActions(state).stream().filter(a -> p1Strategy.getOrDefault(a, 0d) > 1e-8)
                .mapToDouble(a -> p1Strategy.get(a) * computeExpectedValue(state.performAction(a), p0Strategy, p1Strategy)).sum();
    }

    protected void update() {
        informationSets.values().stream().filter(is -> is.getPlayer().equals(regretMatchingPlayer)).forEach(informationSet -> informationSet.getData().applyUpdate());
    }

    protected double bestResponseIteration(Player opponent, ALossBestResponseAlgorithm br) {
        Map<Action, Double> strategy = getStrategyFor(opponent);
        double value = br.calculateBR(rootState, strategy);
        Map<Sequence, Map<ISKey, Action>> fullBestResponseResult = br.getFullBestResponseResult();

        Set<ISKey> visitedISs = new HashSet<>();
        countISsVisited(fullBestResponseResult, rootState, rootState.getAllPlayers()[1 - opponent.getId()], visitedISs);
        maxBRInformationSets =  Math.max(maxBRInformationSets, visitedISs.size());
        updateISs(rootState, fullBestResponseResult, strategy, opponent);
//        updateData(rootState, bestResponse, strategy);
        return value;
    }

    private void countISsVisited(Map<Sequence, Map<ISKey, Action>> fullBestResponseResult, GameState state, Player player, Set<ISKey> visitesISs) {
        if(state.isGameEnd())
            return;
        visitesISs.add(state.getISKeyForPlayerToMove());
        if(state.getPlayerToMove().equals(player)) {
            visitesISs.add(state.getISKeyForPlayerToMove());
            expander.getActions(state).stream().filter(a -> a.equals(fullBestResponseResult.getOrDefault(state.getSequenceForPlayerToMove(), new HashMap<>()).get(state.getISKeyForPlayerToMove())))
                    .forEach(a -> countISsVisited(fullBestResponseResult, state.performAction(a), player, visitesISs));
            return;
        }
        expander.getActions(state).stream()
                .forEach(a -> countISsVisited(fullBestResponseResult, state.performAction(a), player, visitesISs));

    }

    protected Map<Action, Double> getOpponentStrategyForBR(Player opponent, FlexibleISKeyGameState rootState, Expander<IRCFRInformationSet> expander) {
        return IRCFR.getStrategyFor(rootState, opponent, data -> {
            CFRBRData cfrbrData = (CFRBRData) data;
            Map<Action, Double> distribution = new HashMap<>(cfrbrData.getActions().size());

            for (int i = 0; i < cfrbrData.getActions().size(); i++) {
                distribution.put(cfrbrData.getActions().get(i), cfrbrData.getRMStrategy()[i]);
            }
            return distribution;
        }, config.getAllInformationSets(), expander);
    }

    protected void updateISs(FlexibleISKeyGameState state, Map<Sequence, Map<ISKey, Action>> bestResponse, Map<Action, Double> opponentStrategy, Player opponent) {
        Map<Action, Double> avgStrategy = getStrategyFor(rootState.getAllPlayers()[1 - opponent.getId()]);
        HashMap<ISKey, ExpectedValues> valueMap = new HashMap<>();
        ExpectedValues expectedValues = getExpectedValues(state, bestResponse, avgStrategy, opponentStrategy, 1, 1, 1, valueMap, opponent);

        valueMap.forEach((k, v) -> {
            FixedForIterationData data = informationSets.get(k).getData();

            v.updateExpectedExpectedValue(1. / (data.getNbSamples() + 1));
        });
        Map<InformationSet, List<Pair<Sequence, Integer>>> toSplit = new HashMap<>();
        updateISStructure(state, bestResponse, opponentStrategy, opponent, valueMap, toSplit);
        if (UPDATE_IS_STRUCTURE)
            splitISs(toSplit, state.getAllPlayers()[1 - opponent.getId()]);
        informationSets.forEach((key, is) -> is.getData().setActions(expander.getActions(is.getAllStates().stream().findAny().get())));
    }

    protected void splitISs(Map<InformationSet, List<Pair<Sequence, Integer>>> toSplit, Player player) {
        for (Map.Entry<InformationSet, List<Pair<Sequence, Integer>>> entry : toSplit.entrySet()) {
            for (Pair<Sequence, Integer> entryPair : entry.getValue()) {
                Set<GameState> isStates = entry.getKey().getAllStates();
                Set<GameState> toRemove = new HashSet<>();

                isStates.stream().filter(isState -> isState.getSequenceForPlayerToMove().equals(entryPair.getLeft())).forEach(toRemove::add);
                if (toRemove.isEmpty())
                    continue;
                isStates.removeAll(toRemove);
                IRCFRInformationSet newIS = createNewIS(toRemove, player);

                ((CFRBRData) newIS.getData()).setRegretAtIndex(entryPair.getRight(), 1);
                ((CFRBRData) newIS.getData()).updateMeanStrategy(entryPair.getRight(), 1);
                System.err.println("creating IS in it " + iteration + "\n old IS: " + entry.getKey().getISKey() + "\n " + entryPair.getLeft() + "\n new IS: " + newIS.getISKey());
            }
            GambitEFG gambit = new GambitEFG();

            gambit.write("cfrbriteration" + iteration + ".gbt", rootState, expander);
        }
    }

    protected int updateISStructure(GameState state, Map<Sequence, Map<ISKey, Action>> bestResponse, Map<Action, Double> opponentStrategy, Player opponent, HashMap<ISKey, ExpectedValues> valueMap, Map<InformationSet, List<Pair<Sequence, Integer>>> toSplit) {
        if (state.isGameEnd())
            return 0;
        if (state.isPlayerToMoveNature()) {
            return expander.getActions(state).stream().map(state::performAction).mapToInt(s -> updateISStructure(s, bestResponse, opponentStrategy, opponent, valueMap, toSplit)).sum();
        }
        if (state.getPlayerToMove().equals(opponent)) {
            return expander.getActions(state).stream().filter(a -> opponentStrategy.getOrDefault(a, 0d) > 1e-8)
                    .map(state::performAction).mapToInt(s -> updateISStructure(s, bestResponse, opponentStrategy, opponent, valueMap, toSplit)).sum();
        }
        IRCFRInformationSet is = informationSets.get(state.getISKeyForPlayerToMove());
        ExpectedValues expectedValues = valueMap.get(state.getISKeyForPlayerToMove());
        List<Action> actions = expander.getActions(state);
        Set<Action> bestResponseActionsForIS = getBestResponseActionsForIS(is, bestResponse);
        Action currentStateBestResponseAction = bestResponse.get(state.getSequenceForPlayerToMove()).get(state.getISKeyForPlayerToMove());
//        assert actions.stream().filter(a -> bestResponse.getOrDefault(a, 0d) > 1 - 1e-8).count() == 1;
        int actionIndex = getIndex(actions, currentStateBestResponseAction);
        int splitCount = updateISStructure(state.performAction(currentStateBestResponseAction), bestResponse, opponentStrategy, opponent, valueMap, toSplit);
//        int splitCount = actions.stream().filter(a -> bestResponse.getOrDefault(a, 0d) > 1 - 1e-8).map(state::performAction)
//                .mapToInt(s -> updateISStructure(s, bestResponse, opponentStrategy, opponent, valueMap, toSplit)).sum();

        if (UPDATE_IS_STRUCTURE)
            if (!SPLIT_ONLY_DEEPEST || splitCount == 0) {
                if (expectedValues != null && expectedValues.getRealvsExpectedDistance() < -EPS || bestResponseActionsForIS.size() > 1) {
                    Set<GameState> isStates = is.getAllStates();
                    long toRemove = isStates.stream().filter(isState -> isState.getSequenceForPlayerToMove().equals(state.getSequenceForPlayerToMove())).count();

                    if (toRemove != isStates.size()) {
                        List<Pair<Sequence, Integer>> currentSplitList = toSplit.compute(is, (k, v) -> v == null ? new ArrayList<>() : v);

                        currentSplitList.add(new Pair<>(state.getSequenceForPlayerToMove(), actionIndex));
                        System.err.println(iteration + " adding IS: " + is.getISKey() + " " + state.getSequenceForPlayerToMove() + " " + actionIndex);
                        return 1;
                    }
                }
            } else if (SPLIT_ONLY_DEEPEST) {
                if (toSplit.remove(is) != null)
                    System.err.println("removing " + is.getISKey());
            }
        ((CFRBRData) is.getData()).setRegretAtIndex(actionIndex, 1);
        ((CFRBRData) is.getData()).updateMeanStrategy(actionIndex, 1);
        return splitCount;
    }

    private Set<Action> getBestResponseActionsForIS(IRCFRInformationSet is, Map<Sequence, Map<ISKey, Action>> bestResponse) {
        return is.getAllStates().stream().filter(s -> bestResponse.containsKey(s.getSequenceForPlayerToMove()))
                .map(s -> bestResponse.get(s.getSequenceForPlayerToMove()).get(s.getISKeyForPlayerToMove())).collect(Collectors.toSet());
    }

    protected void updateBR(Map<Action, Double> bestResponse, List<Action> actions, int actionIndex, GameState state) {
        Action toAdd = expander.getActions(state).get(actionIndex);
        Action toRemove = actions.get(actionIndex);

        System.out.println("removing " + toRemove + " adding " + toAdd);
        bestResponse.remove(toRemove);
        bestResponse.put(toAdd, 1d);
    }

    protected IRCFRInformationSet createNewIS(Set<GameState> states, Player player) {
        ImperfectRecallISKey newISKey = createNewISKey(player);
        GameState state = states.stream().findAny().get();

        states.forEach(s -> isKeys.put(s, newISKey));
        IRCFRInformationSet is = config.createInformationSetFor(state);

        config.addInformationSetFor(state, is);
        is.addAllStatesToIS(states);
        informationSets.put(newISKey, is);
        is.setData(createAlgData(state));
        return is;
    }

    protected ImperfectRecallISKey createNewISKey(Player player) {
        Observations observations = new Observations(player, player);

        observations.add(new IDObservation(isKeyCounter++));
        return new ImperfectRecallISKey(observations, null, null);
    }


    protected ExpectedValues getExpectedValues(GameState state, Map<Sequence, Map<ISKey, Action>> bestResponse,
                                               Map<Action, Double> avgStrategy, Map<Action, Double> opponentStrategy,
                                               double brProb, double currentProb, double newProb, Map<ISKey, ExpectedValues> valueMap, Player opponent) {
        if (brProb < 1e-8 && currentProb < 1e-8 && newProb < 1e-8)
            return ExpectedValues.ZEROS;
        if (state.isGameEnd())
            return new ExpectedValues(state.getUtilities()[1 - opponent.getId()] * brProb,
                    state.getUtilities()[1 - opponent.getId()] * currentProb,
                    state.getUtilities()[1 - opponent.getId()] * newProb);
        if (state.isPlayerToMoveNature()) {
            ExpectedValues expectedValues = new ExpectedValues();

            for (Action action : expander.getActions(state)) {
                double actionNatureProb = state.getProbabilityOfNatureFor(action);
                ExpectedValues expectedValuesForAction = getExpectedValues(state.performAction(action), bestResponse, avgStrategy, opponentStrategy,
                        brProb * actionNatureProb, currentProb * actionNatureProb, newProb * actionNatureProb, valueMap, opponent);

                expectedValues.add(expectedValuesForAction);
            }
            return expectedValues;
        }
        if (state.getPlayerToMove().equals(opponent)) {
            ExpectedValues expectedValues = new ExpectedValues();

            for (Action action : expander.getActions(state)) {
                double actionProb = opponentStrategy.getOrDefault(action, 0d);
                ExpectedValues expectedValuesForAction = getExpectedValues(state.performAction(action),
                        bestResponse, avgStrategy, opponentStrategy, brProb * actionProb, currentProb * actionProb, newProb * actionProb, valueMap, opponent);

                expectedValues.add(expectedValuesForAction);
            }
            return expectedValues;
        }
        List<Action> actions = expander.getActions(state);
        ExpectedValues expectedValuesForIS = valueMap.compute(state.getISKeyForPlayerToMove(), (isKey, value) -> value == null ? new ExpectedValues() : value);
        ExpectedValues expectedValues = new ExpectedValues();
        double[] meanStrat = new double[actions.size()];
        int actionIndex = 0;
        FixedForIterationData data = informationSets.get(state.getISKeyForPlayerToMove()).getData();
        Map<ISKey, Action> isKeyActionMap = bestResponse.get(state.getSequenceForPlayerToMove());
        Action bestResponseAction = isKeyActionMap == null ? null : isKeyActionMap.get(state.getISKeyForPlayerToMove());

        System.arraycopy(data.getMp(), 0, meanStrat, 0, meanStrat.length);
        meanStrat = updateAndNormalizeMeanStrat(meanStrat, bestResponseAction, actions);
        assert Math.abs(Arrays.stream(meanStrat).sum() - 1) < 1e-8;
        for (Action action : actions) {
            double actionProb = avgStrategy.getOrDefault(action, 0d);
            double actionBRProb = bestResponseAction.equals(action) ? 1 : 0;
            ExpectedValues expectedValuesForAction = getExpectedValues(state.performAction(action),
                    bestResponse, avgStrategy, opponentStrategy, brProb * actionBRProb, currentProb * actionProb, newProb * meanStrat[actionIndex++], valueMap, opponent);

            expectedValues.add(expectedValuesForAction);
        }
        expectedValuesForIS.add(expectedValues);
        return expectedValues;
    }


    protected double[] updateAndNormalizeMeanStrat(double[] meanStrat, Action bestResponseAction, List<Action> actions) {
        double sum = 0;
        int index = 0;

        for (int i = 0; i < meanStrat.length; i++) {
            if (bestResponseAction.equals(actions.get(i)))
                meanStrat[index]++;
            sum += meanStrat[index++];
        }
        if (sum < 1 - 1e-3)
            for (int i = 0; i < meanStrat.length; i++) {
                meanStrat[i] = 1. / meanStrat.length;
            }
        else
            for (int i = 0; i < meanStrat.length; i++) {
                meanStrat[i] /= sum;
            }
        return meanStrat;
    }

    protected int getIndex(List<Action> actions, Action bestResponseAction) {
        int index = -1;

        for (Action action : actions) {
            index++;
            if (bestResponseAction.equals(action))
                return index;
        }
        return -1;
    }

    /**
     * The main function for CFR iteration. Implementation based on Algorithm 1 in M. Lanctot PhD thesis.
     *
     * @param node      current node
     * @param pi1       probability with which the opponent of the searching player and chance want to reach the current node
     * @param expPlayer the exploring player for this iteration
     * @return iteration game reward is actually returned. Other return values are in global x and l
     */
    protected double regretMatchingIteration(GameState node, double pi1, double pi2, Player expPlayer) {
        if (pi1 == 0 && pi2 == 0) return 0;
        if (node.isGameEnd()) {
            return node.getUtilities()[expPlayer.getId()];
        }

        IRCFRInformationSet is = informationSets.get(node.getISKeyForPlayerToMove());

        if (is == null) {
            is = config.createInformationSetFor(node);
            config.addInformationSetFor(node, is);
            is.setData(createAlgData(node));
            informationSets.put(node.getISKeyForPlayerToMove(), is);
        }
        if (!is.getAllStates().contains(node)) {
            config.addInformationSetFor(node, is);
        }

        OOSAlgorithmData data = is.getData();
        List<Action> actions = expander.getActions(node);

        if (node.isPlayerToMoveNature()) {
            double ev = 0;
            for (Action ai : actions) {
                ai.setInformationSet(is);
                final double p = node.getProbabilityOfNatureFor(ai);
                double new_p1 = expPlayer.getId() == 1 ? pi1 * p : pi1;
                double new_p2 = expPlayer.getId() == 0 ? pi2 * p : pi2;
                GameState newState = node.performAction(ai);
                ev += p * regretMatchingIteration(newState, new_p1, new_p2, expPlayer);
            }
            return ev;
        }

        double[] rmProbs = getStrategy(data, node);
        double[] tmpV = new double[rmProbs.length];
        double ev = 0;

        int i = -1;
        for (Action ai : actions) {
            i++;
            ai.setInformationSet(is);
            GameState newState = node.performAction(ai);
            if (is.getPlayer().getId() == 0) {
                tmpV[i] = regretMatchingIteration(newState, pi1 * rmProbs[i], pi2, expPlayer);
            } else {
                tmpV[i] = regretMatchingIteration(newState, pi1, rmProbs[i] * pi2, expPlayer);
            }
            ev += rmProbs[i] * tmpV[i];
        }
        assert !is.getPlayer().equals(expPlayer) || isPureStrat(rmProbs) || isUniform(rmProbs);
        if (is.getPlayer().equals(expPlayer)) {
            update(node, pi1, pi2, expPlayer, data, rmProbs, tmpV, ev);
        }

        return ev;
    }

    protected boolean isUniform(double[] rmProbs) {
        return Arrays.stream(rmProbs).allMatch(v -> Math.abs(v - rmProbs[0]) < 1e-3) || Math.abs(Arrays.stream(rmProbs).sum() - 1) < 1e-3;
    }

    protected boolean isPureStrat(double[] rmProbs) {
        return Arrays.stream(rmProbs).filter(v -> v > 1e-3).allMatch(v -> Math.abs(v - 1) < 1e-3) &&
                Arrays.stream(rmProbs).filter(v -> v > 1e-3).count() == 1;
    }

    protected FixedForIterationData createAlgData(GameState node) {
        return new CFRBRData(expander.getActions(node));
    }

    protected void update(GameState state, double pi1, double pi2, Player expPlayer, OOSAlgorithmData data, double[] rmProbs, double[] tmpV, double ev) {
        double[] expPlayerVals = new double[tmpV.length];

        for (int i = 0; i < tmpV.length; i++) {
            expPlayerVals[i] = tmpV[i];
        }
        data.updateAllRegrets(tmpV, ev, (expPlayer.getId() == 0 ? pi2 : pi1)/*pi1*pi2*/);
        data.updateMeanStrategy(rmProbs, (expPlayer.getId() == 0 ? pi1 : pi2)/*pi1*pi2*/);
    }

    protected Map<Action, Double> getStrategyFor(Player player) {
        Map<Action, Double> strategy = new HashMap<>(informationSets.size() / 2);

        informationSets.values().stream().filter(is -> is.getPlayer().equals(player)).forEach(is -> {
            double[] meanStrategy = is.getData().getMp();
            int index = 0;

            for (Action action : is.getData().getActions()) {
                strategy.put(action, meanStrategy[index++]);
            }
        });
        return strategy;
    }

    protected double[] getStrategy(OOSAlgorithmData data, GameState state) {
        return data.getRMStrategy();
    }

    @Override
    public void setCurrentIS(InformationSet is) {
        throw new NotImplementedException();
    }

    public HashMap<ISKey, IRCFRInformationSet> getInformationSets() {
        return informationSets;
    }

    @Override
    public InnerNode getRootNode() {
        return null;
    }

    @Override
    public Action runMiliseconds(int miliseconds, GameState gameState) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}

