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


package cz.agents.gtlibrary.algorithms.sequenceform.refinements.librarycom;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.gensum.SolverResult;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.Key;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.TreeVisitor;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.librarycom.resultparser.LemkeResultParser;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.librarycom.resultparser.ResultParser;
import cz.agents.gtlibrary.domain.aceofspades.AoSExpander;
import cz.agents.gtlibrary.domain.aceofspades.AoSGameInfo;
import cz.agents.gtlibrary.domain.aceofspades.AoSGameState;
import cz.agents.gtlibrary.domain.artificialchance.ACExpander;
import cz.agents.gtlibrary.domain.artificialchance.ACGameInfo;
import cz.agents.gtlibrary.domain.artificialchance.ACGameState;
import cz.agents.gtlibrary.domain.bpg.BPGExpander;
import cz.agents.gtlibrary.domain.bpg.BPGGameInfo;
import cz.agents.gtlibrary.domain.bpg.BPGGameState;
import cz.agents.gtlibrary.domain.goofspiel.GSGameInfo;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielExpander;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielGameState;
import cz.agents.gtlibrary.domain.mpochm.MPoCHMExpander;
import cz.agents.gtlibrary.domain.mpochm.MPoCHMGameInfo;
import cz.agents.gtlibrary.domain.mpochm.MPoCHMGameState;
import cz.agents.gtlibrary.domain.poker.generic.GPGameInfo;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerExpander;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerGameState;
import cz.agents.gtlibrary.domain.poker.kuhn.KPGameInfo;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerExpander;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerGameState;
import cz.agents.gtlibrary.domain.randomgame.GeneralSumRandomGameState;
import cz.agents.gtlibrary.domain.randomgame.RandomGameExpander;
import cz.agents.gtlibrary.domain.randomgame.RandomGameInfo;
import cz.agents.gtlibrary.experimental.utils.UtilityCalculator;
import cz.agents.gtlibrary.iinodes.LinkedListSequenceImpl;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.strategy.Strategy;
import cz.agents.gtlibrary.strategy.UniformStrategyForMissingSequences;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class DataBuilder extends TreeVisitor {

    public enum Alg {
        lemkeQuasiPerfect, lemkeNash, lemkeQuasiPerfect2, lemkeNash2, simplexNash, simplexQuasiPerfect;
    }

    public static Alg alg = Alg.simplexQuasiPerfect;
    protected String fileName;
    protected GameInfo info;
    protected Data data;


    public static void main(String[] args) {
//		runAC();
        runAoS();
//		runGoofSpiel();
//        runMPoCHM();
//		runKuhnPoker();
//        runGenSumRandomGames();
//		runGenericPoker();
//		runBPG();
    }

    private static void runGenSumRandomGames() {
        AlgorithmConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<>();

        runDataBuilder(new GeneralSumRandomGameState(), new RandomGameExpander<>(algConfig), algConfig, new RandomGameInfo(), "GenSumRandomGameRepr");
    }

    public static void runAC() {
        AlgorithmConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<>();

        runDataBuilder(new ACGameState(), new ACExpander<>(algConfig), algConfig, new ACGameInfo(), "ACRepr");
    }

    public static void runMPoCHM() {
        AlgorithmConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<>();

        runDataBuilder(new MPoCHMGameState(), new MPoCHMExpander<>(algConfig), algConfig, new MPoCHMGameInfo(), "MPoCHMRepr");
    }

    public static void runBPG() {
        AlgorithmConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<>();

        runDataBuilder(new BPGGameState(), new BPGExpander<>(algConfig), algConfig, new BPGGameInfo(), "BPGRepr");
    }

    public static void runKuhnPoker() {
        AlgorithmConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<>();

        runDataBuilder(new KuhnPokerGameState(), new KuhnPokerExpander<>(algConfig), algConfig, new KPGameInfo(), "KuhnPokerRepr");
    }

    public static void runGenericPoker() {
        AlgorithmConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<>();

        runDataBuilder(new GenericPokerGameState(), new GenericPokerExpander<>(algConfig), algConfig, new GPGameInfo(), "GenericPokerRepr");
    }

    public static void runAoS() {
        AlgorithmConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<>();

        runDataBuilder(new AoSGameState(), new AoSExpander<>(algConfig), algConfig, new AoSGameInfo(), "AoSRepr");
    }

    public static void runGoofSpiel() {
        AlgorithmConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<>();

        runDataBuilder(new GoofSpielGameState(), new GoofSpielExpander<>(algConfig), algConfig, new GSGameInfo(), "GoofspielRepr");
    }

    public static SolverResult runDataBuilder(GameState rootState, Expander<SequenceInformationSet> expander, AlgorithmConfig<SequenceInformationSet> algConfig, GameInfo info, String inputFileName) {
        DataBuilder lpBuilder = new DataBuilder(expander, rootState, info, inputFileName);
        long time = 0;

        lpBuilder.build();
        System.out.println("Game build...");
        try {
            long start = System.currentTimeMillis();

            Runtime.getRuntime().exec("./" + getSolverName() + " " + inputFileName).waitFor();
            time = (System.currentTimeMillis() - start);

            System.out.println("LP time: " + time);
        } catch (IOException e) {
            System.err.println("Error during library invocation...");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ResultParser parser = new LemkeResultParser(inputFileName + getSuffix(), lpBuilder.getP1IndicesOfSequences(), lpBuilder.getP2IndicesOfSequences());
//        ResultParser parser = new SimplexResultParser(inputFileName + getSuffix(), lpBuilder.getP1IndicesOfSequences(), lpBuilder.getP2IndicesOfSequences());
//		System.out.println(parser.getP1RealizationPlan());
//		System.out.println(parser.getP2RealizationPlan());

        for (Entry<Sequence, Double> entry : parser.getP1RealizationPlan().entrySet()) {
            if (entry.getValue() > 0)
                System.out.println(entry);
        }
        System.out.println("**************");
        for (Entry<Sequence, Double> entry : parser.getP2RealizationPlan().entrySet()) {
            if (entry.getValue() > 0)
                System.out.println(entry);
        }

        Strategy p1Strategy = new UniformStrategyForMissingSequences();
        Strategy p2Strategy = new UniformStrategyForMissingSequences();
//
        p1Strategy.putAll(parser.getP1RealizationPlan());
        p2Strategy.putAll(parser.getP2RealizationPlan());

        UtilityCalculator calculator = new UtilityCalculator(rootState, expander);
//
//        System.out.println(parser.getGameValue() / info.getUtilityStabilizer());
        System.out.println(calculator.computeUtility(p1Strategy, p2Strategy));

//        System.out.println("ExternalTime: " + parser.getTime());

//        GambitEFG exporter = new GambitEFG();

//        exporter.write("game.gbt", rootState, expander);
        return new SolverResult(parser.getP1RealizationPlan(), parser.getP2RealizationPlan(), time);
    }

    private static String getSuffix() {
        if (alg == Alg.lemkeNash)
            return "l1n";
        if (alg == Alg.lemkeNash2)
            return "l2n";
        if (alg == Alg.lemkeQuasiPerfect)
            return "l1qp";
        if (alg == Alg.lemkeQuasiPerfect2)
            return "l2qp";
        if (alg == Alg.simplexNash)
            return "sn";
        return "sqp";
    }

    private static String getSolverName() {
        if (alg == Alg.lemkeNash)
            return "lemkeNash";
        if (alg == Alg.lemkeNash2)
            return "lemkeSolver2";
        if (alg == Alg.lemkeQuasiPerfect)
            return "lemkeQP";
        if (alg == Alg.lemkeQuasiPerfect2)
            return "lemkeQP2";
        if (alg == Alg.simplexNash)
            return "simplexNash";
        return "simplexQP";
    }

    public DataBuilder(Expander<SequenceInformationSet> expander, GameState rootState, GameInfo info, String fileName) {
        super(rootState, expander);
        this.fileName = fileName;
        this.info = info;
    }

    public void build() {
        initData();
        visitTree(rootState);
        addInitialStrategy(rootState);
        try {
//            if (alg == Alg.simplexNash || alg == Alg.simplexQuasiPerfect)
                data.exportSimplexData(fileName);
//            else
//                data.exportLemkeData(fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void addInitialStrategy(GameState state) {
        data.addSequenceToInitialStrategy(state.getSequenceFor(players[0]));
        data.addSequenceToInitialStrategy(state.getSequenceFor(players[1]));
        if (state.isGameEnd()) {
            return;
        }
        if (state.isPlayerToMoveNature()) {
            for (Action action : expander.getActions(state)) {
                addInitialStrategy(state.performAction(action));
            }
        } else {
            addInitialStrategy(state.performAction(expander.getActions(state).get(0)));
        }
    }

    public void initData() {
        data = new Data();

        initE();
        initF();
    }

    public void initF() {
        data.setF(new Key("Q", new LinkedListSequenceImpl(players[1])), new LinkedListSequenceImpl(players[1]), 1);//F in root (only 1)
        data.addSequenceToInitialStrategy(new LinkedListSequenceImpl(players[1]));//empty sequence representation for initial strategy profile
    }

    public void initE() {
        data.setE(new Key("P", new LinkedListSequenceImpl(players[0])), new LinkedListSequenceImpl(players[0]), 1);//E in root (only 1)
        data.addSequenceToInitialStrategy(new LinkedListSequenceImpl(players[0]));//empty sequence representation for initial strategy profile
    }

    @Override
    protected void visitLeaf(GameState state) {
        updateSequences(state);
        updateParentLinks(state);
        data.addToU1(state.getSequenceFor(players[0]), state.getSequenceFor(players[1]), info.getUtilityStabilizer() * state.getNatureProbability() * (state.getUtilities()[0] + info.getMaxUtility() + 1));
        data.addToU2(state.getSequenceFor(players[0]), state.getSequenceFor(players[1]), info.getUtilityStabilizer() * state.getNatureProbability() * (state.getUtilities()[1] + info.getMaxUtility() + 1));
    }

    @Override
    protected void visitNormalNode(GameState state) {
        data.addISKeyFor(state.getPlayerToMove(), state.getISKeyForPlayerToMove());
        updateSequences(state);
        if (state.getPlayerToMove().getId() == 0) {
            updateLPForFirstPlayer(state);
        } else {
            updateLPForSecondPlayer(state);
        }
        super.visitNormalNode(state);
    }

    public void updateLPForFirstPlayer(GameState state) {
        Key eqKey = new Key("P", new Key(state.getISKeyForPlayerToMove()));

        updateParentLinks(state);
        data.setE(eqKey, state.getSequenceFor(players[0]), -1);//E
    }

    public void updateLPForSecondPlayer(GameState state) {
        Key eqKey = new Key("Q", new Key(state.getISKeyForPlayerToMove()));

        updateParentLinks(state);
        data.setF(eqKey, state.getSequenceFor(players[1]), -1);//F
    }

    public void updateForSecondPlayerParent(GameState child, Player lastPlayer, Key eqKey) {
        data.setF(eqKey, child.getSequenceFor(lastPlayer), 1);//F child
        data.addP2PerturbationsFor(child.getSequenceFor(lastPlayer));
    }

    @Override
    protected void visitChanceNode(GameState state) {
        updateSequences(state);
        updateParentLinks(state);
        super.visitChanceNode(state);
    }

    public void updateParentLinks(GameState state) {
        updateP1Parent(state);
        updateP2Parent(state);
    }

    protected void updateP1Parent(GameState state) {
        Sequence p1Sequence = state.getSequenceFor(players[0]);

        if (p1Sequence.size() > 0) {
            data.setE(getLastISKey(p1Sequence), p1Sequence, 1);//E child
            data.addP1PerturbationsFor(p1Sequence);
        }
    }

    protected void updateP2Parent(GameState state) {
        Sequence p2Sequence = state.getSequenceFor(players[1]);

        if (p2Sequence.size() > 0) {
            data.setF(getLastISKey(p2Sequence), p2Sequence, 1);//F child
            data.addP2PerturbationsFor(p2Sequence);
        }
    }

    public void updateSequences(GameState state) {
        data.addSequence(state.getSequenceFor(state.getAllPlayers()[0]));
        data.addSequence(state.getSequenceFor(state.getAllPlayers()[1]));
    }

    public Map<Integer, Sequence> getP1IndicesOfSequences() {
        return getRevertedMapping(data.getColumnIndicesE(), players[0]);
    }

    public Map<Integer, Sequence> getP2IndicesOfSequences() {
        return getRevertedMapping(data.getColumnIndicesF(), players[1]);
    }

    public Map<Integer, Sequence> getRevertedMapping(Map<Object, Integer> map, Player player) {
        Map<Integer, Sequence> p1Indices = new HashMap<>();

        for (Entry<Object, Integer> entry : map.entrySet()) {
            if (entry.getKey() instanceof Sequence)
                p1Indices.put(entry.getValue(), (Sequence) entry.getKey());
        }
        return p1Indices;
    }
}
