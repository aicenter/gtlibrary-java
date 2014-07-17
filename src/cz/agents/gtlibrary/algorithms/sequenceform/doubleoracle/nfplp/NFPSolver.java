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

import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.DoubleOracleConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.DoubleOracleInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.DoubleOracleLPSolver;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class NFPSolver implements DoubleOracleLPSolver {

    protected Double p1Value;
    protected Double p2Value;
    protected Player[] players;
    protected InitialPBuilder initPBuilderP1;
    protected InitialQBuilder initQBuilderP1;
    protected PBuilder pBuilderP1;
    protected QBuilder qBuilderP1;
    protected InitialP2PBuilder initPBuilderP2;
    protected InitialP2QBuilder initQBuilderP2;
    protected P2PBuilder pBuilderP2;
    protected P2QBuilder qBuilderP2;
    protected Map<Sequence, Double> p1RealizationPlan;
    protected Map<Sequence, Double> p2RealizationPlan;
    protected Set<Sequence> p1SequencesToAdd;
    protected Set<Sequence> p2SequencesToAdd;
    protected GameInfo info;

    public NFPSolver(Player[] players, GameInfo info) {
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

    public void calculateStrategyForPlayer(int playerIndex, GameState root, DoubleOracleConfig config, double currentBoundSize) {
        long startTime = System.currentTimeMillis();

        if (playerIndex == 0)
            p1RealizationPlan = solveForP1(config);
        else
            p2RealizationPlan = solveForP2(config);
    }

    public Map<Sequence, Double> solveForP1(DoubleOracleConfig<DoubleOracleInformationSet> config) {
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

    protected void updateP2Sequences(DoubleOracleConfig<DoubleOracleInformationSet> config) {
        p2SequencesToAdd.addAll(config.getNewSequences());
        for (Sequence sequence : config.getNewSequences()) {
            p2SequencesToAdd.addAll(config.getCompatibleSequencesFor(sequence));
            p2SequencesToAdd.add(sequence.getSubSequence(sequence.size() - 1));
        }
    }

    protected void updateP1Sequences(DoubleOracleConfig<DoubleOracleInformationSet> config) {
        p1SequencesToAdd.addAll(config.getNewSequences());
        for (Sequence sequence : config.getNewSequences()) {
            p1SequencesToAdd.addAll(config.getCompatibleSequencesFor(sequence));
            p1SequencesToAdd.add(sequence.getSubSequence(sequence.size() - 1));
        }
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

    @Override
    public Set<Sequence> getNewSequencesSinceLastLPCalc(Player player) {
        if (player.equals(players[0]))
            return p1SequencesToAdd;
        return p2SequencesToAdd;
    }

    public Map<Sequence, Double> solveForP2(DoubleOracleConfig<DoubleOracleInformationSet> config) {
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

}
