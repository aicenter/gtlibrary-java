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

import cz.agents.gtlibrary.algorithms.sequenceform.FullSequenceFormLP;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FullUndominatedSolver implements FullSequenceFormLP {

    protected Double p1Value;
    protected Double p2Value;
    protected Player[] players;
    protected InitialP1Builder initPBuilderP1;
    protected InitialP2Builder initPBuilderP2;
    protected P1Builder p1Builder;
    protected P2Builder p2Builder;
    protected Map<Sequence, Double> p1RealizationPlan;
    protected Map<Sequence, Double> p2RealizationPlan;
    protected Set<Sequence> p1SequencesToAdd;
    protected Set<Sequence> p2SequencesToAdd;
    protected Map<Sequence, Double> p1FullyMixed;
    protected Map<Sequence, Double> p2FullyMixed;

    public FullUndominatedSolver(Player[] players) {
        this.players = players;
        p1Value = null;
        p2Value = null;
        p1RealizationPlan = new HashMap<Sequence, Double>();
        p2RealizationPlan = new HashMap<Sequence, Double>();
        p1SequencesToAdd = new HashSet<Sequence>();
        p2SequencesToAdd = new HashSet<Sequence>();
    }

    public FullUndominatedSolver(Player[] players, Map<Sequence, Double> p1Strategy, Map<Sequence, Double> p2Strategy) {
        this.players = players;
        p1Value = null;
        p2Value = null;
        p1RealizationPlan = new HashMap<Sequence, Double>();
        p2RealizationPlan = new HashMap<Sequence, Double>();
        p1SequencesToAdd = new HashSet<Sequence>();
        p2SequencesToAdd = new HashSet<Sequence>();
        p1FullyMixed = p1Strategy;
        p2FullyMixed = p2Strategy;
    }

    public Double getResultForPlayer(Player player) {
//        assert !Double.isNaN(p1Value);
        return player.equals(players[0]) ? p1Value : p2Value;
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
            initPBuilderP1 = new InitialP1Builder(players);

        initPBuilderP1.buildLP(config, p1SequencesToAdd);
        BuilderResult result = initPBuilderP1.solve();

        p2Value = -result.getGameValue();

//        return result.getRealizationPlan();
//
        if (p1Builder == null)
            p1Builder = new P1Builder(players);

        if (p2FullyMixed != null)
            p1Builder.buildLP(config, p1SequencesToAdd, -p2Value, p2FullyMixed);
        else
            p1Builder.buildLP(config, p1SequencesToAdd, -p2Value);
        p1SequencesToAdd.clear();
        return p1Builder.solve();
    }

    protected void updateP2Sequences(SequenceFormConfig<? extends SequenceInformationSet> config) {
        p2SequencesToAdd.addAll(config.getSequencesFor(players[0]));
        p2SequencesToAdd.addAll(config.getSequencesFor(players[1]));
    }

    protected void updateP1Sequences(SequenceFormConfig<? extends SequenceInformationSet> config) {
        p1SequencesToAdd.addAll(config.getSequencesFor(players[0]));
        p1SequencesToAdd.addAll(config.getSequencesFor(players[1]));
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
            initPBuilderP2 = new InitialP2Builder(players);

        initPBuilderP2.buildLP(config, p2SequencesToAdd);
        BuilderResult builderResult = initPBuilderP2.solve();

        p1Value = -builderResult.getGameValue();

//        return builderResult.getRealizationPlan();
        if (p2Builder == null)
            p2Builder = new P2Builder(players);

        if (p1FullyMixed != null)
            p2Builder.buildLP(config, p2SequencesToAdd, -p1Value, p1FullyMixed);
        else
            p2Builder.buildLP(config, p2SequencesToAdd, -p1Value);
        p2SequencesToAdd.clear();
        return p2Builder.solve();
    }

    public void calculateBothPlStrategy(GameState rootState, SequenceFormConfig<SequenceInformationSet> algConfig) {
        p1RealizationPlan = solveForP1(algConfig);
        p2RealizationPlan = solveForP2(algConfig);
    }


}
