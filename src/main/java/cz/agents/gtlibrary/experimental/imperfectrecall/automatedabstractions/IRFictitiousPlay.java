package cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions;

import cz.agents.gtlibrary.algorithms.cfr.ir.IRCFRConfig;
import cz.agents.gtlibrary.algorithms.cfr.ir.IRCFRInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.FullSequenceEFG;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.domain.flipit.*;
import cz.agents.gtlibrary.domain.poker.generic.GPGameInfo;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerExpander;
import cz.agents.gtlibrary.domain.poker.generic.ir.CPRRConstIRGenericPokerGameState;
import cz.agents.gtlibrary.domain.poker.generic.ir.IRGenericPokerGameState;
import cz.agents.gtlibrary.domain.randomabstraction.*;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameExpander;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameInfo;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameState;
import cz.agents.gtlibrary.domain.randomgameimproved.io.BasicGameBuilder;
import cz.agents.gtlibrary.domain.randomgameimproved.ir.PerfectIRRandomGameState;
import cz.agents.gtlibrary.domain.wichardtne.WichardtExpander;
import cz.agents.gtlibrary.domain.wichardtne.WichardtGameInfo;
import cz.agents.gtlibrary.domain.wichardtne.WichardtGameState;
import cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.cprr.CPRRExpander;
import cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.cprr.CPRRGameState;
import cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.flexibleisdomain.FlexibleISKeyExpander;
import cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.flexibleisdomain.FlexibleISKeyGameState;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.br.ALossBestResponseAlgorithm;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.iinodes.ImperfectRecallISKey;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.io.GambitEFG;

import java.util.*;
import java.util.stream.Collectors;

public class IRFictitiousPlay extends ALossPRCFRBR {

    public static void main(String[] args) {
//        runGenericPoker();
//        runIRGenericPoker();
        runFlipIt();
//        runWichardtCounterExample();
//        runBothIRRandomAbstractionGame();
//        runCPRRBothIRRandomAbstractionGame();
//        runCPRRConstantBothIRRandomAbstractionGame();
//        runSimpleCPRRBothIRRandomAbstractionGame();
//        runRandomAbstractionGame();
//        runCPRRRandomAbstractionGame();
    }

    private static void runFlipIt() {

        FlipItGameInfo gameInfo = new FlipItGameInfo();
        gameInfo.ZERO_SUM_APPROX = true;

        GameState wrappedRoot = null;

        switch (FlipItGameInfo.gameVersion){
            case NO:                    wrappedRoot = new NoInfoFlipItGameState(); break;
            case FULL:                  wrappedRoot = new FullInfoFlipItGameState(); break;
            case REVEALED_ALL_POINTS:   wrappedRoot = new AllPointsFlipItGameState(); break;
            case REVEALED_NODE_POINTS:  wrappedRoot = new NodePointsFlipItGameState(); break;

        }

        SequenceFormConfig<SequenceInformationSet> config = new SequenceFormConfig<>();
        Expander<SequenceInformationSet> wrappedExpander = new FlipItExpander<>(config);

        FullSequenceEFG efg = new FullSequenceEFG(wrappedRoot, wrappedExpander, gameInfo, config);
        efg.generateCompleteGame();

        GameState root = new RandomAbstractionGameStateFactory().createRoot(wrappedRoot, wrappedExpander.getAlgorithmConfig());
        Expander<IRCFRInformationSet> expander = new RandomAbstractionExpander<>(wrappedExpander, new IRCFRConfig());

        BasicGameBuilder.build(root, expander.getAlgorithmConfig(), expander);
        System.out.println("Abstracted IS count: " + expander.getAlgorithmConfig().getAllInformationSets().size());
        ALossPRCFRBR cfr = new IRFictitiousPlay(root, expander, new GPGameInfo());

        cfr.runIterations(1000);
    }

    protected static void runGenericPoker() {
        GameState root = new IRGenericPokerGameState();
        Expander<IRCFRInformationSet> expander = new GenericPokerExpander<>(new IRCFRConfig());

        System.out.println("Abstracted IS count: " + expander.getAlgorithmConfig().getAllInformationSets().size());
        ALossPRCFRBR cfr = new IRFictitiousPlay(root, expander, new GPGameInfo());

        cfr.runIterations(1000000);
        GambitEFG gambit = new GambitEFG();

        gambit.write("cfrbrtest.gbt", root, expander);
    }

    protected static void runIRGenericPoker() {
        GameState root = new CPRRConstIRGenericPokerGameState();
        Expander<IRCFRInformationSet> expander = new GenericPokerExpander<>(new IRCFRConfig());

        System.out.println("Abstracted IS count: " + expander.getAlgorithmConfig().getAllInformationSets().size());
        ALossPRCFRBR cfr = new IRFictitiousPlay(root, expander, new GPGameInfo());

        cfr.runIterations(1000000);
        GambitEFG gambit = new GambitEFG();

        gambit.write("cfrbrtest.gbt", root, expander);
    }

    protected static void runWichardtCounterExample() {
        GameState root = new WichardtGameState();
        Expander<IRCFRInformationSet> expander = new WichardtExpander<>(new IRCFRConfig());

        System.out.println("Abstracted IS count: " + expander.getAlgorithmConfig().getAllInformationSets().size());
        ALossPRCFRBR cfr = new IRFictitiousPlay(root, expander, new WichardtGameInfo());

        cfr.runIterations(1000000);
        GambitEFG gambit = new GambitEFG();

        gambit.write("cfrbrtest.gbt", root, expander);
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
        ALossPRCFRBR cfr = new IRFictitiousPlay(root, expander, new RandomAbstractionGameInfo(new RandomGameInfo()));

        cfr.runIterations(1000000);
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
        ALossPRCFRBR cfr = new IRFictitiousPlay(root, expander, new RandomAbstractionGameInfo(new RandomGameInfo()));

        cfr.runIterations(1000000);
        GambitEFG gambit = new GambitEFG();

        gambit.write("cfrbrtest.gbt", root, expander);
        System.out.println("Unabstracted IS count: " + config.getAllInformationSets().size());
        System.out.println("Abstracted IS count: " + expander.getAlgorithmConfig().getAllInformationSets().size());
    }

    protected static void runBothIRRandomAbstractionGame() {
        GameState wrappedRoot = new RandomGameState();
        SequenceFormConfig<SequenceInformationSet> config = new SequenceFormConfig<>();
        Expander<SequenceInformationSet> wrappedExpander = new RandomGameExpander<>(config);

        FullSequenceEFG efg = new FullSequenceEFG(wrappedRoot, wrappedExpander, new RandomGameInfo(), config);
        efg.generateCompleteGame();

        System.out.println("Unabstracted IS count: " + config.getAllInformationSets().size());
        GameState root = new RandomAbstractionGameStateFactory().createRoot(wrappedRoot, wrappedExpander.getAlgorithmConfig());
        Expander<IRCFRInformationSet> expander = new RandomAbstractionExpander<>(wrappedExpander, new IRCFRConfig());

        BasicGameBuilder.build(root, expander.getAlgorithmConfig(), expander);
        System.out.println("Abstracted IS count: " + expander.getAlgorithmConfig().getAllInformationSets().size());
        ALossPRCFRBR cfr = new IRFictitiousPlay(root, expander, new RandomAbstractionGameInfo(new RandomGameInfo()));

        cfr.runIterations(1000000);
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
        ALossPRCFRBR cfr = new IRFictitiousPlay(cprrRoot, cprrExpander, new RandomAbstractionGameInfo(new RandomGameInfo()));

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
        ALossPRCFRBR cfr = new IRFictitiousPlay(cprrRoot, cprrExpander, new RandomAbstractionGameInfo(new RandomGameInfo()));

        cfr.runIterations(1000);
        GambitEFG gambit = new GambitEFG();

        gambit.write("cfrbrtest.gbt", cprrRoot, cprrExpander);
        System.out.println("Unabstracted IS count: " + config.getAllInformationSets().size());
    }

    protected static void runCPRRConstantBothIRRandomAbstractionGame() {
        GameState wrappedRoot = new RandomGameState();
        SequenceFormConfig<SequenceInformationSet> config = new SequenceFormConfig<>();
        Expander<SequenceInformationSet> wrappedExpander = new RandomGameExpander<>(config);

        FullSequenceEFG efg = new FullSequenceEFG(wrappedRoot, wrappedExpander, new RandomGameInfo(), config);
        efg.generateCompleteGame();

        System.out.println("Unabstracted IS count: " + config.getAllInformationSets().size());
        GameState root = new CPRRConstantRandomAbstractionGameStateFactory().createRoot(wrappedRoot, wrappedExpander.getAlgorithmConfig());
        Expander<IRCFRInformationSet> expander = new RandomAbstractionExpander<>(wrappedExpander, new IRCFRConfig());

        BasicGameBuilder.build(root, expander.getAlgorithmConfig(), expander);
        System.out.println("Abstracted IS count: " + expander.getAlgorithmConfig().getAllInformationSets().size());
        ALossPRCFRBR cfr = new IRFictitiousPlay(root, expander, new RandomAbstractionGameInfo(new RandomGameInfo()));

        cfr.runIterations(100000);
        GambitEFG gambit = new GambitEFG();

        gambit.write("cfrbrtest.gbt", root, expander);
        System.out.println("Unabstracted IS count: " + config.getAllInformationSets().size());
        System.out.println("Abstracted IS count: " + expander.getAlgorithmConfig().getAllInformationSets().size());
    }

    protected static void runSimpleCPRRBothIRRandomAbstractionGame() {
        GameState root = new PerfectIRRandomGameState();
        Expander<IRCFRInformationSet> expander = new RandomGameExpander<>(new IRCFRConfig());

        ALossPRCFRBR cfr = new IRFictitiousPlay(root, expander, new RandomGameInfo());

        cfr.runIterations(100000);
        GambitEFG gambit = new GambitEFG();

        System.out.println("Unabstracted IS count: " + expander.getAlgorithmConfig().getAllInformationSets().size());
    }

    protected static void runCPRRBothIRRandomAbstractionGame() {
        GameState wrappedRoot = new RandomGameState();
        SequenceFormConfig<SequenceInformationSet> config = new SequenceFormConfig<>();
        Expander<SequenceInformationSet> wrappedExpander = new RandomGameExpander<>(config);

        FullSequenceEFG efg = new FullSequenceEFG(wrappedRoot, wrappedExpander, new RandomGameInfo(), config);
        efg.generateCompleteGame();

        System.out.println("Unabstracted IS count: " + config.getAllInformationSets().size());
        GameState root = new RandomAbstractionGameStateFactory().createRoot(wrappedRoot, wrappedExpander.getAlgorithmConfig());
        Expander<IRCFRInformationSet> expander = new RandomAbstractionExpander<>(wrappedExpander, new IRCFRConfig());


        BasicGameBuilder.build(root, expander.getAlgorithmConfig(), expander);
        System.out.println("Abstracted IS count: " + expander.getAlgorithmConfig().getAllInformationSets().size());
        GameState cprrRoot = new CPRRGameState(root);
        Expander<IRCFRInformationSet> cprrExpander = new CPRRExpander<>(expander);
        ALossPRCFRBR cfr = new IRFictitiousPlay(cprrRoot, cprrExpander, new RandomAbstractionGameInfo(new RandomGameInfo()));

        cfr.runIterations(100000);
        GambitEFG gambit = new GambitEFG();

        gambit.write("cfrbrtest.gbt", cprrRoot, cprrExpander);
        System.out.println("Unabstracted IS count: " + config.getAllInformationSets().size());
    }

    public static double CONVERGENCE_POWER = 1;
    public static double EPS = 0;

    protected final ALossBestResponseAlgorithm p0BR;
    protected final ALossBestResponseAlgorithm p1BR;
    protected final DeltaCalculator p0Delta;
    protected final DeltaCalculator p1Delta;

    public IRFictitiousPlay(GameState rootState, Expander<IRCFRInformationSet> expander, GameInfo info) {
        super(rootState.getAllPlayers()[0], rootState, expander, info);
        BasicGameBuilder.build(this.rootState, this.expander.getAlgorithmConfig(), this.expander);
        informationSets.putAll(this.expander.getAlgorithmConfig().getAllInformationSets());
        addData(informationSets.values());
        p0BR = new ALossBestResponseAlgorithm(this.rootState, this.expander, 0, new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]}, config, info, false);
        p1BR = new ALossBestResponseAlgorithm(this.rootState, this.expander, 1, new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]}, config, info, false);
        p0Delta = new DeltaCalculator(this.rootState, this.expander, 0, config, info, false);
        p1Delta = new DeltaCalculator(this.rootState, this.expander, 1, config, info, false);
    }

    private void addData(Collection<IRCFRInformationSet> informationSets) {
        informationSets.forEach(i -> i.setData(new CFRBRData(this.expander.getActions(i))));
    }

    public Action runIterations(int iterations) {
        for (int i = 0; i < iterations; i++) {
            this.iteration++;
            bestResponseIteration(rootState.getAllPlayers()[1], p0BR);
            bestResponseIteration(rootState.getAllPlayers()[0], p1BR);
            if (i % 20 == 0 || iteration == 1) {
                Map<Action, Double> p1Strategy = getStrategyFor(rootState.getAllPlayers()[1]);

//                System.out.println(p1Strategy);

//                System.out.println("br: " + p0BR.getBestResponse());

                Map<Action, Double> p0Strategy = getStrategyFor(rootState.getAllPlayers()[0]);
//                System.out.println(p0Strategy);
                System.out.println("Iteration: " + iteration);
                System.out.println("p0BR: " + p0BR.calculateBR(rootState, p1Strategy));
                System.out.println("p1BR: " + -p1BR.calculateBR(rootState, p0Strategy));
//                System.out.println("br: " + p1BR.getBestResponse());
                System.out.println("exp val avg vs avg: " + computeExpectedValue(rootState, p0Strategy, p1Strategy));
                System.out.println("Current IS count: " + config.getAllInformationSets().size());
                System.out.println("Max BR ISs: " + maxBRInformationSets);
            }
        }
        firstIteration = false;
        System.out.println("Orig IS count: " + ((FlexibleISKeyExpander) expander).getWrappedExpander().getAlgorithmConfig().getAllInformationSets().size());
        System.out.println("New IS count: " + expander.getAlgorithmConfig().getAllInformationSets().size());
        System.out.println("Max BR ISs: " + maxBRInformationSets);
        GambitEFG gambit = new GambitEFG();

        gambit.write("cfrbrresult.gbt", rootState, expander);
        return null;
    }

    protected void updateISs(FlexibleISKeyGameState state, Map<Sequence, Map<ISKey, Action>> bestResponse, Map<Action, Double> opponentStrategy, Player opponent) {
        Map<InformationSet, Map<Integer, Map<Sequence, double[]>>> toSplit = new HashMap<>();
        Player currentPlayer = state.getAllPlayers()[1 - opponent.getId()];

        updateISStructure(state, bestResponse, opponentStrategy, opponent, toSplit, 1, 1);
        informationSets.values().forEach(i -> ((CFRBRData) i.getData()).updateMeanStrategy());
        if (aboveDelta(getStrategyDiffs(toSplit), getStrategyFor(currentPlayer), currentPlayer)) {
            splitISsToPR(toSplit, currentPlayer);
        } else {
            splitISsAccordingToBR(toSplit, currentPlayer);
        }
//        removeEmptyISs();
        addActionsToISs();
    }

    private Map<InformationSet, Map<Integer, Map<Sequence, double[]>>> updateMap(Map<InformationSet, Map<Integer, Map<Sequence, double[]>>> toSplit) {
        Map<InformationSet, Map<Integer, Map<Sequence, double[]>>> updated = new HashMap<>(toSplit.size());

        for (Map.Entry<InformationSet, Map<Integer, Map<Sequence, double[]>>> informationSetMapEntry : toSplit.entrySet()) {
            Map<Integer, Map<Sequence, double[]>> updatedIntegerMap = new HashMap<>(informationSetMapEntry.getValue().size());

            for (Map.Entry<Integer, Map<Sequence, double[]>> integerMapEntry : informationSetMapEntry.getValue().entrySet()) {
                Map<Sequence, double[]> updatedSequenceMap = new HashMap<>(integerMapEntry.getValue().size());

                for (Map.Entry<Sequence, double[]> sequenceEntry : integerMapEntry.getValue().entrySet()) {
                    updatedSequenceMap.put(sequenceEntry.getKey(), sequenceEntry.getValue());
                }
                updatedIntegerMap.put(integerMapEntry.getKey(), updatedSequenceMap);
            }
            updated.put(informationSetMapEntry.getKey(), updatedIntegerMap);
        }
        return updated;
    }

    private void removeEmptyISs() {
        List<ISKey> collect = informationSets.values().stream().filter(is -> !is.getAllStates().isEmpty()).map(is -> is.getISKey()).collect(Collectors.toList());

        collect.forEach(key -> informationSets.remove(key));
    }

    private void addActionsToISs() {
        informationSets.forEach((key, is) -> is.getData().setActions(expander.getActions(is.getAllStates().stream().findAny().get())));
    }

    private boolean aboveDelta(StrategyDiffs strategyDiffs, Map<Action, Double> strategy, Player player) {
//        return false;
        double delta;

        assert valid(strategyDiffs, strategy);
//        Map<Sequence, Double> irDiffProbs = getIRDiffProbs(strategyDiffs.irStrategyDiff, strategy);
        if (player.getId() == 0)
            delta = p1Delta.calculateDelta(strategy, strategyDiffs);
        else
            delta = p0Delta.calculateDelta(strategy, strategyDiffs);
        if (Math.abs(delta) > 0)
            System.err.println(delta + " vs " + (1. / Math.pow(iteration, CONVERGENCE_POWER) * EPS + 1e-3));
        return delta > 0;
    }

    private Map<Sequence, Double> getIRDiffProbs(Map<Sequence, Map<Action, Double>> irStrategyDiff, Map<Action, Double> strategy) {
        Map<Sequence, Double> diffProbs = new HashMap<>();
        for (Sequence sequence : irStrategyDiff.keySet()) {
            diffProbs.put(sequence, getProbability(sequence, strategy));
        }
        return diffProbs;
    }

    private Double getProbability(Sequence sequence, Map<Action, Double> strategy) {
        double prob = 1;
        for (Action action : sequence) {
            prob *= strategy.getOrDefault(action, 0d);
        }
        return prob;
    }

    private boolean valid(StrategyDiffs strategyDiffs, Map<Action, Double> strategy) {
        for (Map<Action, Double> actionDoubleMap : strategyDiffs.prStrategyDiff.values()) {
            for (Map.Entry<Action, Double> actionDoubleEntry : actionDoubleMap.entrySet()) {
                if (strategy.getOrDefault(actionDoubleEntry.getKey(), 0d) + actionDoubleEntry.getValue() < 0)
                    return false;
            }
        }
        for (Map<Action, Double> actionDoubleMap : strategyDiffs.irStrategyDiff.values()) {
            for (Map.Entry<Action, Double> actionDoubleEntry : actionDoubleMap.entrySet()) {
                if (strategy.getOrDefault(actionDoubleEntry.getKey(), 0d) + actionDoubleEntry.getValue() < 0)
                    return false;
            }
        }
        return true;
    }

    protected int updateISStructure(GameState state, Map<Sequence, Map<ISKey, Action>> bestResponse, Map<Action, Double> opponentStrategy, Player opponent, Map<InformationSet, Map<Integer, Map<Sequence, double[]>>> toSplit, double pBR, double pAvg) {
        if (state.isGameEnd())
            return 0;
        if (state.isPlayerToMoveNature())
            return expander.getActions(state).stream().map(state::performAction).mapToInt(s -> updateISStructure(s, bestResponse, opponentStrategy, opponent, toSplit, pBR, pAvg)).sum();
        if (state.getPlayerToMove().equals(opponent))
            return expander.getActions(state).stream().filter(a -> opponentStrategy.getOrDefault(a, 0d) > 1e-8)
                    .map(state::performAction).mapToInt(s -> updateISStructure(s, bestResponse, opponentStrategy, opponent, toSplit, pBR, pAvg)).sum();
        IRCFRInformationSet is = informationSets.get(state.getISKeyForPlayerToMove());
        List<Action> actions = expander.getActions(state);
        Map<ISKey, Action> isKeyActionMap = bestResponse.get(state.getSequenceForPlayerToMove());
        Action currentStateBestResponseAction = isKeyActionMap == null ? null : isKeyActionMap.get(state.getISKeyForPlayerToMove());

        double[] meanStrategy = is.getData().getMp();

        assert Math.abs(Arrays.stream(meanStrategy).sum() - 1) < 1e-8 || (Math.abs(Arrays.stream(meanStrategy).sum() - 0) < 1e-8 && iteration == 0);
        int splitCount = expander.getActions(state).stream().filter(a -> meanStrategy[getIndex(actions, a)] > 1e-8 || a.equals(currentStateBestResponseAction))
                .mapToInt(a -> updateISStructure(state.performAction(a), bestResponse, opponentStrategy, opponent, toSplit, a.equals(currentStateBestResponseAction) ? 1 : 0, pAvg * meanStrategy[getIndex(actions, a)])).sum();
        Set<GameState> isStates = is.getAllStates();

        if (pBR > 1 - 1e-8 && isStates.stream().filter(isState -> isState.getSequenceForPlayerToMove().equals(state.getSequenceForPlayerToMove())).count() != isStates.size()) {
            int actionIndex = getIndex(actions, currentStateBestResponseAction);
            Map<Integer, Map<Sequence, double[]>> actionMap = toSplit.compute(is, (k, v) -> v == null ? new HashMap<>() : v);
            Map<Sequence, double[]> currentSplitSequences = actionMap.compute(actionIndex, (k, v) -> v == null ? new HashMap<>() : v);
            double[] currentValuePair = currentSplitSequences.compute(state.getSequenceForPlayerToMove(), (k, v) -> v == null ? new double[2] : v);

            currentValuePair[0] += 1. / (iteration + 1) * pBR;
            currentValuePair[1] += ((double) iteration) / (iteration + 1) * pAvg;
            return splitCount + 1;
        } else {
            double brIncrement = 1. / (iteration + 1) * pBR;
            double avgIncrement = ((double) iteration) / (iteration + 1) * pAvg;

            if (pBR > 1 - 1e-8) {
                int actionIndex = getIndex(actions, currentStateBestResponseAction);

                for (int i = 0; i < actions.size(); i++) {
                    ((CFRBRData) is.getData()).addToMeanStrategyUpdateNumerator(i, brIncrement * ((i == actionIndex ? 1 : 0) - meanStrategy[i]));
                }
            }
            ((CFRBRData) is.getData()).addToMeanStrategyUpdateDenominator(brIncrement + avgIncrement);
        }
        return splitCount;
    }

//    protected void splitISs(Map<Action, Map<Sequence, double[]>> toSplit, Player player) {
//        for (Map.Entry<InformationSet, List<Pair<Sequence, Integer>>> entry : toSplit.entrySet()) {
//            for (Pair<Sequence, Integer> entryPair : entry.getValue()) {
//                Set<GameState> isStates = entry.getKey().getAllStates();
//                Set<GameState> toRemove = new HashSet<>();
//
//                isStates.stream().filter(isState -> isState.getSequenceForPlayerToMove().equals(entryPair.getLeft())).forEach(toRemove::add);
//                if (toRemove.isEmpty())
//                    continue;
//                isStates.removeAll(toRemove);
//                IRCFRInformationSet newIS = createNewIS(toRemove, player);
//
//                ((CFRBRData) newIS.getData()).setRegretAtIndex(entryPair.getRight(), 1);
//                ((CFRBRData) newIS.getData()).updateMeanStrategy(entryPair.getRight(), 1);
//                System.err.println("creating IS in it " + iteration + "\n old IS: " + entry.getKey().getISKey() + "\n " + entryPair.getLeft() + "\n new IS: " + newIS.getISKey());
//            }
//            GambitEFG gambit = new GambitEFG();
//
//            gambit.write("cfrbriteration" + iteration + ".gbt", rootState, expander);
//        }
//    }

    protected void splitISsAccordingToBR(Map<InformationSet, Map<Integer, Map<Sequence, double[]>>> toSplit, Player player) {
        for (Map.Entry<InformationSet, Map<Integer, Map<Sequence, double[]>>> informationSetMapEntry : toSplit.entrySet()) {
            if (informationSetMapEntry.getValue().size() > 1) {
                for (Map.Entry<Integer, Map<Sequence, double[]>> entry : informationSetMapEntry.getValue().entrySet()) {
                    Set<GameState> isStates = informationSetMapEntry.getKey().getAllStates();
                    Set<GameState> toRemove = new HashSet<>();
                    int actionIndex = entry.getKey();

                    for (Sequence sequence : entry.getValue().keySet()) {
                        isStates.stream().filter(isState -> isState.getSequenceForPlayerToMove().equals(sequence)).forEach(toRemove::add);
                    }
                    if (toRemove.isEmpty())
                        continue;
                    IRCFRInformationSet newIS;
                    if (toRemove.size() == isStates.size())
                        newIS = (IRCFRInformationSet) informationSetMapEntry.getKey();
                    else {
                        isStates.removeAll(toRemove);
                        newIS = createNewIS(toRemove, player, (CFRBRData) ((IRCFRInformationSet) informationSetMapEntry.getKey()).getData());
                    }
                    double[] meanStrategy = newIS.getData().getMp();

                    for (Map.Entry<Sequence, double[]> sequenceEntry : entry.getValue().entrySet()) {
                        for (int i = 0; i < ((IRCFRInformationSet) informationSetMapEntry.getKey()).getData().getActions().size(); i++) {
                            ((CFRBRData) newIS.getData()).addToMeanStrategyUpdateNumerator(i, sequenceEntry.getValue()[0] * ((i == actionIndex ? 1 : 0) - meanStrategy[i]));
                        }
                        ((CFRBRData) newIS.getData()).addToMeanStrategyUpdateDenominator(sequenceEntry.getValue()[0] + sequenceEntry.getValue()[1]);
                    }
                    ((CFRBRData) newIS.getData()).updateMeanStrategy();
                    System.err.println("!!!BR creating IS in it " + iteration);
                }
            } else {
                for (Map.Entry<Integer, Map<Sequence, double[]>> entry : informationSetMapEntry.getValue().entrySet()) {
                    CFRBRData data = (CFRBRData) ((IRCFRInformationSet) informationSetMapEntry.getKey()).getData();
                    double[] meanStrategy = data.getMp();
                    int actionIndex = entry.getKey();

                    for (Map.Entry<Sequence, double[]> sequenceEntry : entry.getValue().entrySet()) {
                        for (int i = 0; i < ((IRCFRInformationSet) informationSetMapEntry.getKey()).getData().getActions().size(); i++) {
                            data.addToMeanStrategyUpdateNumerator(i, sequenceEntry.getValue()[0] * ((i == actionIndex ? 1 : 0) - meanStrategy[i]));
                        }
                        data.addToMeanStrategyUpdateDenominator(sequenceEntry.getValue()[0] + sequenceEntry.getValue()[1]);
                    }
                    data.updateMeanStrategy();
                }
            }
        }
//        GambitEFG gambit = new GambitEFG();
//
//        gambit.write("cfrbriteration" + iteration + ".gbt", rootState, expander);

    }

    protected void splitISsToPR(Map<InformationSet, Map<Integer, Map<Sequence, double[]>>> toSplit, Player player) {
        for (Map.Entry<InformationSet, Map<Integer, Map<Sequence, double[]>>> actionMapEntry : toSplit.entrySet()) {
            for (Map.Entry<Integer, Map<Sequence, double[]>> entry : actionMapEntry.getValue().entrySet()) {
                for (Map.Entry<Sequence, double[]> sequenceEntry : entry.getValue().entrySet()) {
                    Set<GameState> isStates = actionMapEntry.getKey().getAllStates();
                    Set<GameState> toRemove = new HashSet<>();

                    isStates.stream().filter(isState -> isState.getSequenceForPlayerToMove().equals(sequenceEntry.getKey())).forEach(toRemove::add);
                    if (toRemove.isEmpty())
                        continue;
                    int actionIndex = entry.getKey();

                    assert actionIndex > -1;
                    IRCFRInformationSet newIS;
                    if (toRemove.size() == isStates.size())
                        newIS = (IRCFRInformationSet) actionMapEntry.getKey();
                    else {
                        isStates.removeAll(toRemove);
                        newIS = createNewIS(toRemove, player, (CFRBRData) ((IRCFRInformationSet) actionMapEntry.getKey()).getData());
                    }
                    double[] meanStrategy = newIS.getData().getMp();

                    for (int i = 0; i < newIS.getData().getActions().size(); i++) {
                        ((CFRBRData) newIS.getData()).addToMeanStrategyUpdateNumerator(i, sequenceEntry.getValue()[0] * ((i == actionIndex ? 1 : 0) - meanStrategy[i]));
                    }
                    ((CFRBRData) newIS.getData()).addToMeanStrategyUpdateDenominator(sequenceEntry.getValue()[0] + sequenceEntry.getValue()[1]);
                    ((CFRBRData) newIS.getData()).updateMeanStrategy();
                    System.err.println("PR " + newIS.getPlayer() + " creating IS in it " + iteration);
                }
            }
        }
//        GambitEFG gambit = new GambitEFG();
//
//        gambit.write("cfrbriteration" + iteration + ".gbt", rootState, expander);
    }

//    protected void splitISsToPR(Map<InformationSet, Map<Integer, Map<Sequence, double[]>>> toSplit, Player player) {
//        List<Set<GameState>> newISs = new ArrayList<>();
//        List<InformationSet> oldISSs = new ArrayList<>();
//        List<double[]> newISUpdates = new ArrayList<>();
//        List<double[]> newISInitMeanStrat = new ArrayList<>();
//        List<Integer> actionIndices = new ArrayList<>();
//
//        for (Map.Entry<InformationSet, Map<Integer, Map<Sequence, double[]>>> actionMapEntry : toSplit.entrySet()) {
//            for (Map.Entry<Integer, Map<Sequence, double[]>> entry : actionMapEntry.getValue().entrySet()) {
//                for (Map.Entry<Sequence, double[]> sequenceEntry : entry.getValue().entrySet()) {
//                    Set<GameState> isStates = actionMapEntry.getKey().getAllStates();
//                    Set<GameState> toRemove = new HashSet<>();
//
//                    isStates.stream().filter(isState -> isState.getSequenceForPlayerToMove().equals(sequenceEntry.getKey())).forEach(toRemove::add);
//                    if (toRemove.isEmpty())
//                        continue;
//                    int actionIndex = entry.getKey();
//
//                    assert actionIndex > -1;
//
//                    if (toRemove.size() == isStates.size()) {
//                        IRCFRInformationSet newIS = (IRCFRInformationSet) actionMapEntry.getKey();
//                        double[] meanStrategy = newIS.getData().getMp();
//
//                        for (int i = 0; i < newIS.getData().getActions().size(); i++) {
//                            ((CFRBRData) newIS.getData()).addToMeanStrategyUpdateNumerator(i, sequenceEntry.getValue()[0] * ((i == actionIndex ? 1 : 0) - meanStrategy[i]));
//                        }
//                        ((CFRBRData) newIS.getData()).addToMeanStrategyUpdateDenominator(sequenceEntry.getValue()[0] + sequenceEntry.getValue()[1]);
//                        ((CFRBRData) newIS.getData()).updateMeanStrategy();
//                    } else {
//                        newISs.add(toRemove);
//                        newISUpdates.add(sequenceEntry.getValue());
//                        newISInitMeanStrat.add(((IRCFRInformationSet) actionMapEntry.getKey()).getData().getMp());
//                        oldISSs.add(actionMapEntry.getKey());
//                        actionIndices.add(entry.getKey());
//                    }
//                }
//            }
//        }
//
//        for (int i = 0; i < newISs.size(); i++) {
//            Set<GameState> isStates = oldISSs.get(i).getAllStates();
//            Set<GameState> toRemove = newISs.get(i);
//
//            if (toRemove.isEmpty())
//                continue;
//            int actionIndex = actionIndices.get(i);
//
//            assert actionIndex > -1;
//            IRCFRInformationSet newIS;
//
//            if (toRemove.size() == isStates.size()) {
//                newIS = (IRCFRInformationSet) oldISSs.get(i);
//            } else {
//                isStates.removeAll(toRemove);
//                newIS = createNewIS(toRemove, player, (CFRBRData) ((IRCFRInformationSet) oldISSs.get(i)).getData());
//            }
//            double[] meanStrategy = newIS.getData().getMp();
//
//            for (int j = 0; j < newIS.getData().getActions().size(); j++) {
//                ((CFRBRData) newIS.getData()).addToMeanStrategyUpdateNumerator(j, newISUpdates.get(i)[0] * ((j == actionIndex ? 1 : 0) - meanStrategy[j]));
//            }
//            ((CFRBRData) newIS.getData()).addToMeanStrategyUpdateDenominator(newISUpdates.get(i)[0] + newISUpdates.get(i)[1]);
//            ((CFRBRData) newIS.getData()).updateMeanStrategy();
//            System.err.println("PR " + newIS.getPlayer() + " creating IS in it " + iteration);
//        }
//    }

    protected IRCFRInformationSet createNewIS(Set<GameState> states, Player player, CFRBRData data) {
        ImperfectRecallISKey newISKey = createNewISKey(player);
        GameState state = states.stream().findAny().get();

        states.forEach(s -> isKeys.put(s, newISKey));
        IRCFRInformationSet is = config.createInformationSetFor(state);

        config.addInformationSetFor(state, is);
        is.addAllStatesToIS(states);
        informationSets.put(newISKey, is);
        is.setData(new CFRBRData(data));
        return is;
    }


    protected StrategyDiffs getStrategyDiffs(Map<InformationSet, Map<Integer, Map<Sequence, double[]>>> toSplit) {
        StrategyDiffs strategyDiffs = new StrategyDiffs();

        for (Map.Entry<InformationSet, Map<Integer, Map<Sequence, double[]>>> actionMapEntry : toSplit.entrySet()) {
            for (Map.Entry<Integer, Map<Sequence, double[]>> entry : actionMapEntry.getValue().entrySet()) {
                List<Action> actions = ((IRCFRInformationSet) actionMapEntry.getKey()).getData().getActions();
                double[] meanStratDiffForAction = new double[actions.size()];
                double[] meanStrategy = ((IRCFRInformationSet) actionMapEntry.getKey()).getData().getMp();
                double meanStratDiffForActionNormalizer = 0;
                int actionIndex = entry.getKey();

                for (Map.Entry<Sequence, double[]> sequenceValuesEntry : entry.getValue().entrySet()) {
                    double[] meanStratDiffForSequence = new double[actions.size()];

//                    if(sequenceValuesEntry.getValue()[0] + sequenceValuesEntry.getValue()[1] < 1e-3)
//                        continue;
                    for (int i = 0; i < actions.size(); i++) {
                        meanStratDiffForSequence[i] = sequenceValuesEntry.getValue()[0] * ((i == actionIndex ? 1 : 0) - meanStrategy[i]);
                        meanStratDiffForAction[i] += meanStratDiffForSequence[i];
                        meanStratDiffForSequence[i] /= sequenceValuesEntry.getValue()[0] + sequenceValuesEntry.getValue()[1];
                    }
                    meanStratDiffForActionNormalizer += sequenceValuesEntry.getValue()[0] + sequenceValuesEntry.getValue()[1];
                    strategyDiffs.prStrategyDiff.put(sequenceValuesEntry.getKey(), toMapNoNorm(actions, meanStratDiffForSequence));
                }
//                if(meanStratDiffForActionNormalizer < 1e-3)
//                    continue;
                for (int i = 0; i < meanStratDiffForAction.length; i++) {
                    meanStratDiffForAction[i] /= meanStratDiffForActionNormalizer;
                }
                actionMapEntry.getKey().getAllStates().stream().map(s -> s.getSequenceForPlayerToMove()).forEach(sequence -> {
                    strategyDiffs.irStrategyDiff.put(sequence, toMapNoNorm(actions, meanStratDiffForAction));
                });
            }
        }
        return strategyDiffs;
    }

    private Map<Action, Double> toMap(List<Action> actions, double[] meanStrat) {
        int index = 0;
        double sum = Arrays.stream(meanStrat).sum();
        Map<Action, Double> actionMap = new HashMap<>(meanStrat.length);

        for (Action action : actions) {
            actionMap.put(action, meanStrat[index++] / sum);
        }
        return actionMap;
    }

    private Map<Action, Double> toMapNoNorm(List<Action> actions, double[] meanStrat) {
        int index = 0;
        Map<Action, Double> actionMap = new HashMap<>(meanStrat.length);

        for (Action action : actions) {
            actionMap.put(action, meanStrat[index++]);
        }
        return actionMap;
    }
}
