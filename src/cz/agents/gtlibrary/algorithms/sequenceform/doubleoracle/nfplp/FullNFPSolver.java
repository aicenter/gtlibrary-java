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


package cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.nfplp;

import cz.agents.gtlibrary.algorithms.sequenceform.FullSequenceEFG;
import cz.agents.gtlibrary.algorithms.sequenceform.FullSequenceFormLP;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.domain.aceofspades.AoSExpander;
import cz.agents.gtlibrary.domain.aceofspades.AoSGameInfo;
import cz.agents.gtlibrary.domain.aceofspades.AoSGameState;
import cz.agents.gtlibrary.domain.clairvoyance.ClairvoyanceExpander;
import cz.agents.gtlibrary.domain.clairvoyance.ClairvoyanceGameState;
import cz.agents.gtlibrary.domain.clairvoyance.ClairvoyanceInfo;
import cz.agents.gtlibrary.domain.mpochm.MPoCHMExpander;
import cz.agents.gtlibrary.domain.mpochm.MPoCHMGameInfo;
import cz.agents.gtlibrary.domain.mpochm.MPoCHMGameState;
import cz.agents.gtlibrary.iinodes.ArrayListSequenceImpl;
import cz.agents.gtlibrary.iinodes.ConfigImpl;
import cz.agents.gtlibrary.interfaces.*;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FullNFPSolver implements FullSequenceFormLP {

    private Double p1Value;
    private Double p2Value;
    private Player[] players;
    private InitialPBuilder initPBuilderP1;
    private InitialQBuilder initQBuilderP1;
    private PBuilder pBuilderP1;
    private QBuilder qBuilderP1;
    private InitialP2PBuilder initPBuilderP2;
    private InitialP2QBuilder initQBuilderP2;
    private P2PBuilder pBuilderP2;
    private P2QBuilder qBuilderP2;
    private Map<Sequence, Double> p1RealizationPlan;
    private Map<Sequence, Double> p2RealizationPlan;
    private Set<Sequence> p1SequencesToAdd;
    private Set<Sequence> p2SequencesToAdd;
    private GameInfo info;

    public static void main(String[] args) {
        runClairvoyanceGame();
//        runAoS();
//        runMPoCHM();
    }

    private static void runAoS() {
        GameState root = new AoSGameState();
        SequenceFormConfig<SequenceInformationSet> config = new SequenceFormConfig<>();
        Expander<SequenceInformationSet> expander = new AoSExpander<>(config);

        FullSequenceEFG efg = new FullSequenceEFG(root, expander, new AoSGameInfo(), config);

        efg.generateCompleteGame();
        FullNFPSolver solver = new FullNFPSolver(root.getAllPlayers(), new AoSGameInfo());

        solver.calculateBothPlStrategy(root, config);

        System.out.println("---------------------");
        for (Map.Entry<Sequence, Double> entry : solver.getResultStrategiesForPlayer(AoSGameInfo.FIRST_PLAYER).entrySet()) {
            if(entry.getValue() > 0)
                System.out.println(entry);
        }
        System.out.println("---------------------");
        for (Map.Entry<Sequence, Double> entry : solver.getResultStrategiesForPlayer(AoSGameInfo.SECOND_PLAYER).entrySet()) {
            if(entry.getValue() > 0)
                System.out.println(entry);
        }
    }

    private static void runMPoCHM() {
        GameState root = new MPoCHMGameState();
        SequenceFormConfig<SequenceInformationSet> config = new SequenceFormConfig<>();
        Expander<SequenceInformationSet> expander = new MPoCHMExpander<>(config);

        FullSequenceEFG efg = new FullSequenceEFG(root, expander, new MPoCHMGameInfo(), config);

        efg.generateCompleteGame();
        FullNFPSolver solver = new FullNFPSolver(root.getAllPlayers(), new MPoCHMGameInfo());

        solver.calculateBothPlStrategy(root, config);

        System.out.println("---------------------");
        for (Map.Entry<Sequence, Double> entry : solver.getResultStrategiesForPlayer(MPoCHMGameInfo.FIRST_PLAYER).entrySet()) {
            if(entry.getValue() > 0)
                System.out.println(entry);
        }
        System.out.println("---------------------");
        for (Map.Entry<Sequence, Double> entry : solver.getResultStrategiesForPlayer(MPoCHMGameInfo.SECOND_PLAYER).entrySet()) {
            if(entry.getValue() > 0)
                System.out.println(entry);
        }
    }

    private static void runClairvoyanceGame() {
        GameState root = new ClairvoyanceGameState();
        SequenceFormConfig<SequenceInformationSet> config = new SequenceFormConfig<>();
        Expander<SequenceInformationSet> expander = new ClairvoyanceExpander<>(config);

        FullSequenceEFG efg = new FullSequenceEFG(root, expander, new ClairvoyanceInfo(), config);

        efg.generateCompleteGame();
        FullNFPSolver solver = new FullNFPSolver(root.getAllPlayers(), new ClairvoyanceInfo());

        solver.calculateBothPlStrategy(root, config);

        System.out.println("---------------------");
        for (Map.Entry<Sequence, Double> entry : solver.getResultStrategiesForPlayer(ClairvoyanceInfo.FIRST_PLAYER).entrySet()) {
            if(entry.getValue() > 0)
                System.out.println(entry);
        }
        System.out.println("---------------------");
        for (Map.Entry<Sequence, Double> entry : solver.getResultStrategiesForPlayer(ClairvoyanceInfo.SECOND_PLAYER).entrySet()) {
            if(entry.getValue() > 0)
                System.out.println(entry);
        }
    }

    public FullNFPSolver(Player[] players, GameInfo info) {
        this.players = players;
        this.info = info;
        p1Value = null;
        p2Value = null;
        p1RealizationPlan = new HashMap<Sequence, Double>();
        p2RealizationPlan = new HashMap<Sequence, Double>();
        p1SequencesToAdd = new HashSet<Sequence>();
        p2SequencesToAdd = new HashSet<Sequence>();
    }


    public Double getResultForPlayer(Player player) {
//        assert !Double.isNaN(p1Value);
        Double value = player.equals(players[0]) ? p1Value : p2Value;

        return value == null ? null : value / info.getUtilityStabilizer();
    }

    public Map<Sequence, Double> getResultStrategiesForPlayer(Player player) {
        return player.equals(players[0]) ? p1RealizationPlan : p2RealizationPlan;
    }

    public void calculateStrategyForPlayer(int playerIndex, GameState root, SequenceFormConfig<? extends SequenceInformationSet> config, double currentBoundSize) {
        long startTime = System.currentTimeMillis();

        if (playerIndex == 0)
            p1RealizationPlan = solveForP1(config);
        else
            p2RealizationPlan = solveForP2(config);
    }

    public Map<Sequence, Double> solveForP1(SequenceFormConfig<? extends SequenceInformationSet> config) {
        updateP1Sequences(config);
        updateP2Sequences(config);
        if (initPBuilderP1 == null)
            initPBuilderP1 = new InitialPBuilder(players, info);

        initPBuilderP1.buildLP(config, p1SequencesToAdd);
        PResult pResult = initPBuilderP1.solve();

        p2Value = -pResult.getGameValue();


        if (initQBuilderP1 == null)
            initQBuilderP1 = new InitialQBuilder(players, info);

        initQBuilderP1.buildLP(config, -p2Value, p1SequencesToAdd);
        QResult qResult = initQBuilderP1.solve();

//        System.out.println("Exploitable sequences: ");
//        for (Sequence exploitableSequence : qResult.getLastItSeq()) {
//            System.out.println(exploitableSequence);
//        }
        if (pBuilderP1 == null)
            pBuilderP1 = new PBuilder(players, info);
//        pBuilderP1.updateFromLastIteration(qResult, p1Value);
        pBuilderP1.buildLP(config, p1SequencesToAdd);
//        pBuilderP1.updateSolver();
        pBuilderP1.update(qResult, -p2Value, config);
        if (qBuilderP1 == null)
            qBuilderP1 = new QBuilder(players, info);

        qBuilderP1.buildLP(config, -p2Value, p1SequencesToAdd);
//        qBuilderP1.updateSolver();
        if (qResult.getGameValue() > 1e-6) {
            pResult = pBuilderP1.solve();

            qBuilderP1.update(pResult.getGameValue(), qResult, config);
//            qBuilderP1.buildLP(config, p1Value);
            qResult = qBuilderP1.solve();

//            System.out.println("Exploitable sequences: ");
//            for (Sequence exploitableSequence : qResult.getLastItSeq()) {
//                System.out.println(exploitableSequence);
//            }

            PUpdater pUpdater = new PUpdater(players, pBuilderP1.lpTable, info);
            QUpdater qUpdater = new QUpdater(players, qBuilderP1.lpTable, info);

            while (Math.abs(qResult.getGameValue()) > 1e-6) {
                assert !qResult.getLastItSeq().isEmpty();
//                System.out.println("Exploitable seq. count " + qResult.getLastItSeq().size());

                pUpdater.update(qResult, config);
                pResult = pUpdater.solve();

                qUpdater.update(pResult.getGameValue(), qResult, config);
                qResult = qUpdater.solve();
//                System.out.println("Exploitable sequences: ");
//                for (Sequence exploitableSequence : qResult.getLastItSeq()) {
//                    System.out.println(exploitableSequence);
//                }
            }
        }


        p1SequencesToAdd.clear();
        return pResult.getRealizationPlan();
//        return qResult.getRealizationPlan();
    }

    private void updateP2Sequences(SequenceFormConfig<? extends SequenceInformationSet> config) {
        p2SequencesToAdd.addAll(config.getSequencesFor(players[0]));
        p2SequencesToAdd.addAll(config.getSequencesFor(players[1]));
        p2SequencesToAdd.add(new ArrayListSequenceImpl(players[0]));
        p2SequencesToAdd.add(new ArrayListSequenceImpl(players[1]));
    }

    private void updateP1Sequences(SequenceFormConfig<? extends SequenceInformationSet> config) {
        p1SequencesToAdd.addAll(config.getSequencesFor(players[0]));
        p1SequencesToAdd.addAll(config.getSequencesFor(players[1]));
        p1SequencesToAdd.add(new ArrayListSequenceImpl(players[0]));
        p1SequencesToAdd.add(new ArrayListSequenceImpl(players[1]));
    }

    public void setDebugOutput(PrintStream debugOutput) {
        //To change body of created methods use File | Settings | File Templates.
    }

    public long getOverallGenerationTime() {
        return 0;
    }

    public long getOverallConstraintGenerationTime() {
        return 0;
    }

    public long getOverallConstraintLPSolvingTime() {
        return 0;
    }

    public Map<Sequence, Double> solveForP2(SequenceFormConfig<? extends SequenceInformationSet> config) {
        updateP1Sequences(config);
        updateP2Sequences(config);
        if (initPBuilderP2 == null)
            initPBuilderP2 = new InitialP2PBuilder(players, info);

        initPBuilderP2.buildLP(config, p2SequencesToAdd);
        PResult pResult = initPBuilderP2.solve();

        p1Value = -pResult.getGameValue();

        if (initQBuilderP2 == null)
            initQBuilderP2 = new InitialP2QBuilder(players, info);

        initQBuilderP2.buildLP(config, -p1Value, p2SequencesToAdd);
        QResult qResult = initQBuilderP2.solve();

//        System.out.println("Exploitable sequences: ");
//        for (Sequence exploitableSequence : qResult.getLastItSeq()) {
//            System.out.println(exploitableSequence);
//        }

        if (pBuilderP2 == null)
            pBuilderP2 = new P2PBuilder(players, info);
//        pBuilderP2.updateFromLastIteration(qResult, p2Value);
        pBuilderP2.buildLP(config, p2SequencesToAdd);
        pBuilderP2.update(qResult, -p1Value, config);
//        pBuilderP2.updateSolver();
        if (qBuilderP2 == null)
            qBuilderP2 = new P2QBuilder(players, info);

        qBuilderP2.buildLP(config, -p1Value, p2SequencesToAdd);
//        qBuilderP2.updateSolver();
        if (qResult.getGameValue() > 1e-6) {
            pResult = pBuilderP2.solve();

//            qBuilderP2.updateSum(pResult.getGameValue(), qResult);
            qBuilderP2.update(pResult.getGameValue(), qResult, config);
            qResult = qBuilderP2.solve();

//            System.out.println("Exploitable sequences: ");
//            for (Sequence exploitableSequence : qResult.getLastItSeq()) {
//                System.out.println(exploitableSequence);
//            }
            P2PUpdater pUpdater = new P2PUpdater(players, pBuilderP2.lpTable, info);
            P2QUpdater qUpdater = new P2QUpdater(players, qBuilderP2.lpTable, info);

            while (Math.abs(qResult.getGameValue()) > 1e-6) {
                assert !qResult.getLastItSeq().isEmpty();
//                System.out.println("Exploitable seq. count " + qResult.getLastItSeq().size());

                pUpdater.update(qResult, config);
                pResult = pUpdater.solve();

                qUpdater.update(pResult.getGameValue(), qResult, config);
                qResult = qUpdater.solve();
//                System.out.println("Exploitable sequences: ");
//                for (Sequence exploitableSequence : qResult.getLastItSeq()) {
//                    System.out.println(exploitableSequence);
//                }
            }
        }


        p2SequencesToAdd.clear();
        return pResult.getRealizationPlan();
//        return  qResult.getRealizationPlan();
    }

    public void calculateBothPlStrategy(GameState rootState, SequenceFormConfig<SequenceInformationSet> algConfig) {
        p1RealizationPlan = solveForP1(algConfig);
        p2RealizationPlan = solveForP2(algConfig);
    }
}
