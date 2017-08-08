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
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.Key;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.librarycom.resultparser.LemkeResultParser;
import cz.agents.gtlibrary.domain.aceofspades.AoSExpander;
import cz.agents.gtlibrary.domain.aceofspades.AoSGameInfo;
import cz.agents.gtlibrary.domain.aceofspades.AoSGameState;
import cz.agents.gtlibrary.domain.artificialchance.ACExpander;
import cz.agents.gtlibrary.domain.artificialchance.ACGameInfo;
import cz.agents.gtlibrary.domain.artificialchance.ACGameState;
import cz.agents.gtlibrary.domain.bpg.BPGExpander;
import cz.agents.gtlibrary.domain.bpg.BPGGameInfo;
import cz.agents.gtlibrary.domain.bpg.BPGGameState;
import cz.agents.gtlibrary.domain.poker.generic.GPGameInfo;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerExpander;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerGameState;
import cz.agents.gtlibrary.domain.poker.kuhn.KPGameInfo;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerExpander;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerGameState;
import cz.agents.gtlibrary.experimental.utils.UtilityCalculator;
import cz.agents.gtlibrary.iinodes.LinkedListSequenceImpl;
import cz.agents.gtlibrary.iinodes.PerfectRecallISKey;
import cz.agents.gtlibrary.iinodes.PlayerImpl;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.strategy.Strategy;
import cz.agents.gtlibrary.strategy.UniformStrategyForMissingSequences;
import cz.agents.gtlibrary.utils.Pair;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

public class BreadthFirstDataBuilder {

    protected GameInfo info;
    protected String fileName;
    protected Data data;

    protected GameState rootState;
    protected Expander<? extends InformationSet> expander;
    protected AlgorithmConfig<SequenceInformationSet> algConfig;
    protected Player[] players;

    public static void main(String[] args) {
//        runAC();
		runAoS();
//		runKuhnPoker();
//		runGenericPoker();
//		runBPG();
    }

    public static void runAC() {
        AlgorithmConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();

        runDataBuilder(new ACGameState(), new ACExpander<>(algConfig), algConfig, new ACGameInfo(), "ACRepr", "ACReprl1qp");
    }

    public static void runBPG() {
        AlgorithmConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();

        runDataBuilder(new BPGGameState(), new BPGExpander<>(algConfig), algConfig, new BPGGameInfo(), "BPGRepr", "BPGReprl1qp");
    }

    public static void runKuhnPoker() {
        AlgorithmConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();

        runDataBuilder(new KuhnPokerGameState(), new KuhnPokerExpander<>(algConfig), algConfig, new KPGameInfo(), "KuhnPokerRepr", "KuhnPokerReprl1qp");
    }

    public static void runGenericPoker() {
        AlgorithmConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();

        runDataBuilder(new GenericPokerGameState(), new GenericPokerExpander<>(algConfig), algConfig, new GPGameInfo(), "GenericPokerRepr", "GenericPokerReprl1qp");
    }

    public static void runAoS() {
        AlgorithmConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();

        runDataBuilder(new AoSGameState(), new AoSExpander<>(algConfig), algConfig, new AoSGameInfo(), "AoSRepr", "AoSReprl1qp");
    }

    public static void runDataBuilder(GameState rootState, Expander<SequenceInformationSet> expander, AlgorithmConfig<SequenceInformationSet> algConfig, GameInfo info, String inputFileName, String outputFileName) {
        BreadthFirstDataBuilder lpBuilder = new BreadthFirstDataBuilder(expander, rootState, algConfig, info, inputFileName);

        lpBuilder.build();
        try {
            Runtime.getRuntime().exec("./lemkeQP " + inputFileName).waitFor();
        } catch (IOException e) {
            System.err.println("Error during library invocation...");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LemkeResultParser parser = new LemkeResultParser(outputFileName, lpBuilder.getP1IndicesOfSequences(), lpBuilder.getP2IndicesOfSequences());

//		System.out.println(parser.getP1RealizationPlan());
//		System.out.println(parser.getP2RealizationPlan());

        for (Entry<Sequence, Double> entry : parser.getP1RealizationPlan().entrySet()) {
            if (entry.getValue() > 0)
                System.out.println(entry);
        }
        System.out.println("*************");
        for (Entry<Sequence, Double> entry : parser.getP2RealizationPlan().entrySet()) {
            if (entry.getValue() > 0)
                System.out.println(entry);
        }

        Strategy p1Strategy = new UniformStrategyForMissingSequences();
        Strategy p2Strategy = new UniformStrategyForMissingSequences();

        p1Strategy.putAll(parser.getP1RealizationPlan());
        p2Strategy.putAll(parser.getP2RealizationPlan());

        UtilityCalculator calculator = new UtilityCalculator(rootState, expander);
        UtilityCalculator calculator1 = new UtilityCalculator(rootState, expander);

        System.out.println(parser.getGameValue() / info.getUtilityStabilizer());
        System.out.println(calculator.computeUtility(p1Strategy, p2Strategy));
        System.out.println(calculator1.computeUtility(p1Strategy, p2Strategy));
    }

    public BreadthFirstDataBuilder(Expander<SequenceInformationSet> expander, GameState rootState, AlgorithmConfig<SequenceInformationSet> algConfig, GameInfo info, String inputFileName) {
        super();
        this.fileName = inputFileName;
        this.rootState = rootState;
        this.expander = expander;
        this.algConfig = algConfig;
        this.players = rootState.getAllPlayers();
        this.info = info;
    }

    public void build() {
        initData();

        LinkedList<GameState> queue = new LinkedList<GameState>();

        queue.add(rootState);
        while (!queue.isEmpty()) {
            GameState currentState = queue.removeFirst();

            addStateToConfig(currentState);
            if (currentState.isGameEnd()) {
                updateForLeaf(currentState);
                continue;
            }
            addContinuationStates(queue, currentState);
            if (currentState.isPlayerToMoveNature()) {
                updateForChanceNode(currentState);
                continue;
            }
            updateForNormalNode(currentState);
        }
        addInitialStrategy(rootState);
        try {
            data.exportLemkeData(fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addContinuationStates(LinkedList<GameState> queue, GameState currentState) {
        for (Action action : expander.getActions(currentState)) {
            queue.addLast(currentState.performAction(action));
        }
    }

    public void addStateToConfig(GameState currentState) {
        if (algConfig.getInformationSetFor(currentState) == null)
            algConfig.addInformationSetFor(currentState, new SequenceInformationSet(currentState));
        algConfig.getInformationSetFor(currentState).addStateToIS(currentState);
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

    protected void updateForLeaf(GameState state) {
        updateSequences(state);
        updateParentLinks(state);
        data.addToU1(state.getSequenceFor(players[0]), state.getSequenceFor(players[1]), info.getUtilityStabilizer() * state.getNatureProbability() * state.getUtilities()[0]);
        data.addToU2(state.getSequenceFor(players[0]), state.getSequenceFor(players[1]), info.getUtilityStabilizer() * state.getNatureProbability() * state.getUtilities()[1]);
    }

    protected void updateForNormalNode(GameState state) {
        data.addISKeyFor(state.getPlayerToMove(), state.getISKeyForPlayerToMove());
        updateSequences(state);
        if (state.getPlayerToMove().getId() == 0) {
            updateLPForFirstPlayer(state);
        } else {
            updateLPForSecondPlayer(state);
        }
    }

    public void updateLPForFirstPlayer(GameState state) {
        Key eqKey = new Key("P", new Key(state.getISKeyForPlayerToMove()));

        updateParentLinks(state);
        data.setE(eqKey, state.getSequenceFor(players[0]), -1);//E
    }

//	public void updateForFirstPlayerParent(GameState child, Player lastPlayer, Key eqKey) {
//		data.setE(eqKey, child.getSequenceFor(lastPlayer), 1);//E child
//		data.addP1PerturbationsFor(child.getSequenceFor(lastPlayer));
//	}

    public void updateLPForSecondPlayer(GameState state) {
        Key eqKey = new Key("Q", new Key(state.getISKeyForPlayerToMove()));

        updateParentLinks(state);
        data.setF(eqKey, state.getSequenceFor(players[1]), -1);//F
    }

//	public void updateForSecondPlayerParent(GameState child, Player lastPlayer, Key eqKey) {
//		data.setF(eqKey, child.getSequenceFor(lastPlayer), 1);//F child
//		data.addP2PerturbationsFor(child.getSequenceFor(lastPlayer));
//	}

    protected void updateForChanceNode(GameState state) {
        updateSequences(state);
        updateParentLinks(state);
    }

    public void updateParentLinks(GameState state) {
        updateP1Parent(state);
        updateP2Parent(state);
//		if (lastPlayer != null)
//			if (lastPlayer.getId() == 0) {
//				updateForFirstPlayerParent(state, lastPlayer, lastKey);
//			} else {
//				updateForSecondPlayerParent(state, lastPlayer, lastKey);
//			}
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

    public Object getLastISKey(Sequence sequence) {
        String string = sequence.getPlayer().equals(players[0]) ? "P" : "Q";

        return new Key(string, new Key(sequence.getLastInformationSet().getISKey()));
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
            p1Indices.put(entry.getValue(), (Sequence) entry.getKey());
        }
        return p1Indices;
    }

    protected void addInitialStrategy(GameState state) {
        data.addSequenceToInitialStrategy(state.getSequenceFor(new PlayerImpl(0)));
        data.addSequenceToInitialStrategy(state.getSequenceFor(new PlayerImpl(1)));
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


}
