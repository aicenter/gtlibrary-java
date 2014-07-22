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


package cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.undominated;

import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.DoubleOracleConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.DoubleOracleInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.DoubleOracleLPSolver;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class UndominatedSolver implements DoubleOracleLPSolver {

    private Double p1Value;
    private Double p2Value;
    private Player[] players;
    private InitialP1Builder initPBuilderP1;
    private InitialP2Builder initPBuilderP2;
    private P1Builder p1Builder;
    private P2Builder p2Builder;
    private Map<Sequence, Double> p1RealizationPlan;
    private Map<Sequence, Double> p2RealizationPlan;
    private Set<Sequence> p1SequencesToAdd;
    private Set<Sequence> p2SequencesToAdd;
    private int normalLPCount;
    private int boundLPCount;
    public long overallLPSolvingTime;

    public UndominatedSolver(Player[] players) {
        this.players = players;
        p1Value = null;
        p2Value = null;
        p1RealizationPlan = new HashMap<Sequence, Double>();
        p2RealizationPlan = new HashMap<Sequence, Double>();
        p1SequencesToAdd = new HashSet<Sequence>();
        p2SequencesToAdd = new HashSet<Sequence>();
        normalLPCount = 0;
        boundLPCount = 0;
        overallLPSolvingTime = 0;
    }

    public Double getResultForPlayer(Player player) {
//        assert !Double.isNaN(p1Value);
        return player.equals(players[0]) ? p1Value : p2Value;
    }

    public Map<Sequence, Double> getResultStrategiesForPlayer(Player player) {
        return player.equals(players[0]) ? p1RealizationPlan : p2RealizationPlan;
    }

    public void calculateStrategyForPlayer(int playerIndex, GameState root, DoubleOracleConfig config, double bound) {
        long startTime = System.currentTimeMillis();

//        if(!addedSequences[0] && !addedSequences[1]) {
//        System.err.println("Normal invocation");
        normalLPCount++;
        if (playerIndex == 0)
            p1RealizationPlan = solveForP1(config);
        else
            p2RealizationPlan = solveForP2(config);
//        } else {
//            boundLPCount++;
//            if (playerIndex == 0)
//                p1RealizationPlan = solveForP1(config, bound);
//            else
//                p2RealizationPlan = solveForP2(config, bound);
//        }
    }

    public Map<Sequence, Double> solveForP1(DoubleOracleConfig<DoubleOracleInformationSet> config) {
        updateP1Sequences(config);
        updateP2Sequences(config);
        if (initPBuilderP1 == null)
            initPBuilderP1 = new InitialP1Builder(players);

        initPBuilderP1.buildLP(config, p1SequencesToAdd);
        BuilderResult result = initPBuilderP1.solve();

        overallLPSolvingTime += result.getLpSolvingTime();
        p2Value = -result.getGameValue();

//        return result.getRealizationPlan();
//
        if (p1Builder == null)
            p1Builder = new P1Builder(players);

        p1Builder.buildLP(config, p1SequencesToAdd, -p2Value);
        p1SequencesToAdd.clear();

        Map<Sequence, Double> realPlan = p1Builder.solve();

        overallLPSolvingTime += p1Builder.getLPTime();
        return realPlan;
    }

    public Map<Sequence, Double> solveForP1(DoubleOracleConfig<DoubleOracleInformationSet> config, double gameValue) {
        updateP1Sequences(config);
        updateP2Sequences(config);
        if (initPBuilderP1 == null)
            initPBuilderP1 = new InitialP1Builder(players);

        initPBuilderP1.buildLP(config, p1SequencesToAdd);
//        BuilderResult result = initPBuilderP1.solve();

//        assert result.getGameValue() > gameValue - 1e-8;
        p2Value = -gameValue;

//        return result.getRealizationPlan();
//
        if (p1Builder == null)
            p1Builder = new P1Builder(players);

        p1Builder.buildLP(config, p1SequencesToAdd, -p2Value);
        p1SequencesToAdd.clear();
        return p1Builder.solve();
    }

    private void updateP2Sequences(DoubleOracleConfig<DoubleOracleInformationSet> config) {
        p2SequencesToAdd.addAll(config.getNewSequences());
        for (Sequence sequence : config.getNewSequences()) {
            p2SequencesToAdd.addAll(config.getCompatibleSequencesFor(sequence));
            p2SequencesToAdd.add(sequence.getSubSequence(sequence.size() - 1));
        }
    }

    private void updateP1Sequences(DoubleOracleConfig<DoubleOracleInformationSet> config) {
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
        return overallLPSolvingTime;
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
            initPBuilderP2 = new InitialP2Builder(players);

        initPBuilderP2.buildLP(config, p2SequencesToAdd);
        BuilderResult builderResult = initPBuilderP2.solve();

        overallLPSolvingTime += builderResult.getLpSolvingTime();
        p1Value = -builderResult.getGameValue();
//        p2Value = builderResult.getGameValue();

//        return builderResult.getRealizationPlan();
        if (p2Builder == null)
            p2Builder = new P2Builder(players);

        p2Builder.buildLP(config, p2SequencesToAdd, -p1Value);
        p2SequencesToAdd.clear();

        Map<Sequence, Double> realPlan = p2Builder.solve();

        overallLPSolvingTime += p2Builder.getLPTime();
        return realPlan;
    }

    public Map<Sequence, Double> solveForP2(DoubleOracleConfig<DoubleOracleInformationSet> config, double gameValue) {
        updateP1Sequences(config);
        updateP2Sequences(config);
        if (initPBuilderP2 == null)
            initPBuilderP2 = new InitialP2Builder(players);
//
        initPBuilderP2.buildLP(config, p2SequencesToAdd);
//        BuilderResult builderResult = initPBuilderP2.solve();
//
//        assert builderResult.getGameValue() > gameValue -1e-8;
        p1Value = -gameValue;

//        return builderResult.getRealizationPlan();
        if (p2Builder == null)
            p2Builder = new P2Builder(players);

        p2Builder.buildLP(config, p2SequencesToAdd, -p1Value);
        p2SequencesToAdd.clear();
        return p2Builder.solve();
    }

    public Map<Sequence, Double> getOpponentValuesFor(Player player) {
        if (player.equals(players[0])) {
            if (p1Builder == null)
                return new HashMap<Sequence, Double>();
            return p1Builder.getOpponentValues();
        }
        if (p2Builder == null)
            return new HashMap<Sequence, Double>();
        return p2Builder.getOpponentValues();
    }

    public int getBoundLPCount() {
        return boundLPCount;
    }

    public int getNormalLPCount() {
        return normalLPCount;
    }
}
