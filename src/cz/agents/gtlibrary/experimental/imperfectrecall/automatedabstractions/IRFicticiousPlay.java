package cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions;

import cz.agents.gtlibrary.algorithms.cfr.ir.IRCFR;
import cz.agents.gtlibrary.algorithms.cfr.ir.IRCFRConfig;
import cz.agents.gtlibrary.algorithms.cfr.ir.IRCFRInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.distribution.MeanStratDist;
import cz.agents.gtlibrary.algorithms.sequenceform.FullSequenceEFG;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.domain.poker.kuhn.KPGameInfo;
import cz.agents.gtlibrary.domain.randomabstraction.P1RandomAbstractionGameStateFactory;
import cz.agents.gtlibrary.domain.randomabstraction.P1RandomAlossAbstractionGameStateFactory;
import cz.agents.gtlibrary.domain.randomabstraction.RandomAbstractionExpander;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameExpander;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameInfo;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameState;
import cz.agents.gtlibrary.domain.randomgameimproved.io.BasicGameBuilder;
import cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.flexibleisdomain.FlexibleISKeyGameState;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.br.ALossBestResponseAlgorithm;
import cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.cprr.CPRRExpander;
import cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.cprr.CPRRGameState;
import cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.flexibleisdomain.FlexibleISKeyExpander;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.io.GambitEFG;

import java.util.*;

public class IRFicticiousPlay extends ALossPRCFRBR {

    public static void main(String[] args) {
        runRandomAbstractionGame();
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
        ALossPRCFRBR cfr = new IRFicticiousPlay(root, expander, new KPGameInfo());

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
        ALossPRCFRBR cfr = new IRFicticiousPlay(root, expander, new KPGameInfo());

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
        ALossPRCFRBR cfr = new IRFicticiousPlay(cprrRoot, cprrExpander, new KPGameInfo());

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
        ALossPRCFRBR cfr = new IRFicticiousPlay(cprrRoot, cprrExpander, new KPGameInfo());

        cfr.runIterations(1000);
        GambitEFG gambit = new GambitEFG();

        gambit.write("cfrbrtest.gbt", cprrRoot, cprrExpander);
        System.out.println("Unabstracted IS count: " + config.getAllInformationSets().size());
    }

    protected final ALossBestResponseAlgorithm p0BR;
    protected final ALossBestResponseAlgorithm p1BR;

    public IRFicticiousPlay(GameState rootState, Expander<IRCFRInformationSet> expander, GameInfo info) {
        super(rootState.getAllPlayers()[0], rootState, expander, info);
        BasicGameBuilder.build(this.rootState, this.expander.getAlgorithmConfig(), this.expander);
        informationSets.putAll(this.expander.getAlgorithmConfig().getAllInformationSets());
        addData(informationSets.values());
        p0BR = new ALossBestResponseAlgorithm(this.rootState, this.expander, 0, new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]}, config, info, false);
        p1BR = new ALossBestResponseAlgorithm(this.rootState, this.expander, 1, new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]}, config, info, false);
    }

    private void addData(Collection<IRCFRInformationSet> informationSets) {
        informationSets.forEach(i -> i.setData(new CFRBRData(this.expander.getActions(i))));
    }

    protected void createNewIS(GameState state) {
        Set<GameState> set = new HashSet<>(1);

        set.add(state);
        createNewIS(set);
    }


    public Action runIterations(int iterations) {
        for (int i = 0; i < iterations; i++) {
            this.iteration++;
            bestResponseIteration(rootState.getAllPlayers()[1], p0BR);
            bestResponseIteration(rootState.getAllPlayers()[0], p1BR);
            if (i % 50 == 0) {
                Map<Action, Double> strategy = IRCFR.getStrategyFor(rootState, regretMatchingPlayer, new MeanStratDist(), config.getAllInformationSets(), expander);

//                System.out.println(strategy);
//                System.out.println(IRCFR.getStrategyFor(rootState, rootState.getAllPlayers()[1 - regretMatchingPlayer.getId()], new MeanStratDist(), config.getAllInformationSets(), expander));
                System.out.println("exp val against br: " + -br.calculateBR(rootState, strategy));

                Map<Action, Double> bestResponse = br.getBestResponse();
                Map<Action, Double> averageBestResponse = IRCFR.getStrategyFor(rootState, brPlayer, new MeanStratDist(), config.getAllInformationSets(), expander);
                System.out.println("exp val avg vs avg: " + computeExpectedValue(rootState, strategy, averageBestResponse));
                System.out.println("Current IS count: " + config.getAllInformationSets().size());
            }
        }
        firstIteration = false;
        System.out.println("Orig IS count: " + ((FlexibleISKeyExpander) expander).getWrappedExpander().getAlgorithmConfig().getAllInformationSets().size());
        System.out.println("New IS count: " + expander.getAlgorithmConfig().getAllInformationSets().size());
        return null;
    }

    @Override
    protected Map<Action, Double> getOpponentStrategyForBR(Player opponent, FlexibleISKeyGameState rootState, Expander<IRCFRInformationSet> expander) {
        return IRCFR.getStrategyFor(rootState, opponent, new MeanStratDist(), config.getAllInformationSets(), expander);
    }
}
