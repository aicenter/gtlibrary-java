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

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPData;
import cz.agents.gtlibrary.iinodes.ArrayListSequenceImpl;
import cz.agents.gtlibrary.iinodes.PerfectRecallISKey;
import cz.agents.gtlibrary.interfaces.InformationSet;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.Pair;
import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class P1Builder {

    protected SequenceFormConfig<? extends SequenceInformationSet> config;
    protected Player[] players;
    protected UndomRecyclingTable lpTable;
    protected String lpFileName;
    protected Map<Sequence, Double> fullyMixedStrategy;
    protected Map<Sequence, Double> opponentValues;
    private long time;

    public P1Builder(Player[] players) {
        this.players = players;
        lpFileName = "UndominatedP12.lp";
        fullyMixedStrategy = new HashMap<Sequence, Double>();
        opponentValues = new HashMap<Sequence, Double>();
        initTable();
    }

    public void buildLP(SequenceFormConfig<? extends SequenceInformationSet> config, Set<Sequence> sequencesToAdd, double gameValue) {
        this.config = config;
        lpTable.clearObjective();
        updateGameValueConstraint(gameValue);
        if (fullyMixedStrategy.isEmpty())
            updateUniformRealPlan(new ArrayListSequenceImpl(players[1]));
        updateTable(config, sequencesToAdd);
    }

    public void buildLP(SequenceFormConfig<? extends SequenceInformationSet> config, Set<Sequence> sequencesToAdd, double gameValue, Map<Sequence, Double> strategy) {
        this.config = config;
        lpTable.clearObjective();
        updateGameValueConstraint(gameValue);
        fullyMixedStrategy = strategy;
        updateTable(config, sequencesToAdd);
    }

    private void updateTable(SequenceFormConfig<? extends SequenceInformationSet> config, Set<Sequence> sequencesToAdd) {
        for (Sequence sequence : sequencesToAdd) {
            if (sequence.getPlayer().equals(players[0]))
                updateForP1(sequence);
            else
                updateForP2(sequence);
        }
        updateUtilities(config);
        updateObjective(config, fullyMixedStrategy);
    }

    private void updateUniformRealPlan(Sequence sequence) {
        if (sequence.size() == 0)
            fullyMixedStrategy.put(sequence, 1d);
        Set<? extends SequenceInformationSet> reachableSets = config.getReachableSets(sequence);

        if (reachableSets == null || reachableSets.isEmpty())
            return;
        double currentProbability = fullyMixedStrategy.get(sequence);

        for (SequenceInformationSet reachableSet : reachableSets) {
            for (Sequence outgoingSequence : reachableSet.getOutgoingSequences()) {
                fullyMixedStrategy.put(outgoingSequence, currentProbability / reachableSet.getOutgoingSequences().size());
                updateUniformRealPlan(outgoingSequence);
            }
        }
    }

    private void updateGameValueConstraint(double gameValue) {
        lpTable.setConstraint("prevIt", players[1], 1);
        lpTable.setConstraintType("prevIt", 2);
        lpTable.setConstant("prevIt", gameValue);
    }

    private void updateUtilities(SequenceFormConfig<? extends SequenceInformationSet> config) {
        Set<Pair<Sequence, Sequence>> blackList = new HashSet<Pair<Sequence, Sequence>>();

        for (Sequence p1Sequence : config.getSequencesFor(players[0])) {
            for (Sequence compatibleSequence : config.getCompatibleSequencesFor(p1Sequence)) {
                Double utility = config.getUtilityFor(p1Sequence, compatibleSequence);

                if (utility == null) {
                    lpTable.removeFromConstraint(compatibleSequence, p1Sequence);
                } else {
                    Pair<Sequence, Sequence> sequencePair = new Pair<>(p1Sequence, compatibleSequence);

                    lpTable.setConstraint(compatibleSequence, p1Sequence, -utility);
//                    lpTable.addToObjective(p1Sequence, fullyMixedStrategy.get(compatibleSequence) * utility);
                    blackList.add(sequencePair);
                }
            }
        }
//        for (Sequence p2Sequence : config.getSequencesFor(players[1])) {
//            for (Sequence compatibleSequence : config.getCompatibleSequencesFor(p2Sequence)) {
//                Double utility = config.getUtilityFor(compatibleSequence, p2Sequence);
//
//                if (utility == null) {
//                    lpTable.removeFromConstraint(p2Sequence, compatibleSequence);
//                } else {
//                    Pair<Sequence, Sequence> sequencePair = new Pair<Sequence, Sequence>(compatibleSequence, p2Sequence);
//
//                    if (!blackList.contains(sequencePair)) {
//                        assert false;
////                        lpTable.addToObjective(compatibleSequence, fullyMixedStrategy.get(p2Sequence) * utility);
//                        lpTable.setConstraint(p2Sequence, compatibleSequence, -utility);
//                    }
//                }
//            }
//        }
    }

    public void updateObjective(Map<Sequence, Double> strategy) {
        updateObjective(config, strategy);
    }

    public void updateObjective(SequenceFormConfig<? extends SequenceInformationSet> config, Map<Sequence, Double> fullyMixedStrategy) {
        lpTable.clearObjective();
//        for (Sequence p1Sequence : config.getSequencesFor(players[0])) {
//            for (Sequence compatibleSequence : config.getCompatibleSequencesFor(p1Sequence)) {
//                Double utility = config.getUtilityFor(p1Sequence, compatibleSequence);
//
//                if (utility != null) {
//                    Pair<Sequence, Sequence> sequencePair = new Pair<Sequence, Sequence>(p1Sequence, compatibleSequence);
//
//                    lpTable.addToObjective(p1Sequence, fullyMixedStrategy.get(compatibleSequence) * utility);
//                }
//            }
//        }
        for (Sequence p1Sequence : config.getSequencesFor(players[0])) {
            for (Sequence compatibleSequence : fullyMixedStrategy.keySet()) {
                Double utility = config.getUtilityFor(p1Sequence, compatibleSequence);

                if (utility != null) {
                    lpTable.addToObjective(p1Sequence, fullyMixedStrategy.get(compatibleSequence) * utility);
                }
            }
        }
    }

    protected void updateForP1(Sequence p1Sequence) {
        lpTable.watchPrimalVariable(p1Sequence, p1Sequence);
        if (config.getReachableSets(p1Sequence) == null)
            return;
        for (SequenceInformationSet informationSet : config.getReachableSets(p1Sequence)) {
            for (Sequence outgoingSequence : informationSet.getOutgoingSequences()) {
                Object eqKey = getKey(informationSet);

                lpTable.watchPrimalVariable(outgoingSequence, outgoingSequence);
                lpTable.setConstraint(eqKey, p1Sequence, -1);//E
                lpTable.setConstraintType(eqKey, 1);
                lpTable.setLowerBound(p1Sequence, 0);
                lpTable.setConstraint(eqKey, outgoingSequence, 1);//E
                lpTable.setLowerBound(outgoingSequence, 0);
            }
        }
    }

    protected void updateForP2(Sequence p2Sequence) {
        if (config.getReachableSets(p2Sequence) == null)
            return;
        for (SequenceInformationSet informationSet : config.getReachableSets(p2Sequence)) {
            for (Sequence outgoingSequence : informationSet.getOutgoingSequences()) {
                Object varKey = getKey(informationSet);

                lpTable.setConstraint(p2Sequence, varKey, -1);//F
                lpTable.setConstraintType(p2Sequence, 0);
                lpTable.setLowerBound(varKey, Double.NEGATIVE_INFINITY);
                lpTable.setConstraint(outgoingSequence, varKey, 1);//F
                lpTable.setConstraintType(outgoingSequence, 0);
                lpTable.watchPrimalVariable(varKey, informationSet.getPlayersHistory());
            }
        }
    }

    private Object getKey(SequenceInformationSet informationSet) {
        return new PerfectRecallISKey(informationSet.hashCode(), informationSet.getPlayersHistory());
    }

    protected Sequence getSubsequence(Sequence sequence) {
        return sequence.getSubSequence(sequence.size() - 1);
    }

    public Map<Sequence, Double> solve() {
        try {
            LPData lpData = lpTable.toCplex();
            boolean solved = false;

//            lpData.getSolver().exportModel(lpFileName);
            for (int algorithm : lpData.getAlgorithms()) {
                lpData.getSolver().setParam(IloCplex.IntParam.RootAlg, algorithm);
                if (solved = trySolve(lpData))
                    break;
            }
            opponentValues  = createOpponentValues(lpData.getSolver(), lpData.getWatchedPrimalVariables());
            return createFirstPlayerStrategy(lpData.getSolver(), lpData.getWatchedPrimalVariables());
        } catch (IloException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected boolean trySolve(LPData lpData) throws IloException {
        boolean solved;

        try {
            time = System.currentTimeMillis();
            solved = lpData.getSolver().solve();
            time = System.currentTimeMillis() - time;
        } catch (IloException e) {
            e.printStackTrace();
            return false;
        }

//        System.out.println("P: " + solved);
        return solved;
    }

    public long getLPTime() {
        return time;
    }

    protected void solveUnfeasibleLP(LPData lpData) throws IloException {
        lpData.getSolver().setParam(IloCplex.IntParam.FeasOptMode, IloCplex.Relaxation.OptQuad);

        boolean solved = lpData.getSolver().feasOpt(lpData.getConstraints(), getPreferences(lpData));

        assert solved;
        System.out.println("P feas: " + solved);
    }

    protected double[] getPreferences(LPData lpData) {
        double[] preferences = new double[lpData.getConstraints().length];

        for (int i = 0; i < preferences.length; i++) {
            if (lpData.getRelaxableConstraints().values().contains(lpData.getConstraints()[i]))
                preferences[i] = 1;
            else
                preferences[i] = 0.5;
        }
        return preferences;
    }

    public Map<Sequence, Double> createFirstPlayerStrategy(IloCplex cplex, Map<Object, IloNumVar> watchedPrimalVars) throws IloException {
        Map<Sequence, Double> p1Strategy = new HashMap<Sequence, Double>();

        for (Entry<Object, IloNumVar> entry : watchedPrimalVars.entrySet()) {
            if (((Sequence) entry.getKey()).getPlayer().equals(players[0]))
                p1Strategy.put((Sequence) entry.getKey(), cplex.getValue(entry.getValue()));
        }
        return p1Strategy;
    }

    public Map<Sequence, Double> createOpponentValues(IloCplex cplex, Map<Object, IloNumVar> watchedPrimalVars) throws IloException {
        Map<Sequence, Double> opponentValues = new HashMap<Sequence, Double>();

        for (Entry<Object, IloNumVar> entry : watchedPrimalVars.entrySet()) {
            if (((Sequence) entry.getKey()).getPlayer().equals(players[1]))
                opponentValues.put((Sequence) entry.getKey(), cplex.getValue(entry.getValue()));
        }
        return opponentValues;
    }

    protected Map<Sequence, Double> getWatchedSequenceValues(LPData lpData) throws IloException {
        Map<Sequence, Double> watchedSequenceValues = new HashMap<Sequence, Double>(lpData.getWatchedPrimalVariables().size());

        for (Entry<Object, IloNumVar> entry : lpData.getWatchedPrimalVariables().entrySet()) {
            watchedSequenceValues.put((Sequence) entry.getKey(), lpData.getSolver().getValue(entry.getValue()));
        }
        return watchedSequenceValues;
    }

    public Map<Sequence, Double> getOpponentValues() {
        return opponentValues;
    }

    public void initTable() {
        Sequence p1EmptySequence = new ArrayListSequenceImpl(players[0]);
        Sequence p2EmptySequence = new ArrayListSequenceImpl(players[1]);

        lpTable = new UndomRecyclingTable();

        initObjective(p2EmptySequence);
        initE(p1EmptySequence);
        initF(p2EmptySequence);
        inite();
    }

    public void inite() {
        lpTable.setConstant(players[0], 1);//e for root
    }

    public void initF(Sequence p2EmptySequence) {
        lpTable.setConstraint(p2EmptySequence, players[1], 1);//F in root (only 1)
        lpTable.setConstraintType(p2EmptySequence, 0);
        lpTable.setLowerBound(players[1], Double.NEGATIVE_INFINITY);
    }

    public void initE(Sequence p1EmptySequence) {
        lpTable.setConstraint(players[0], p1EmptySequence, 1);//E in root (only 1)
        lpTable.setConstraintType(players[0], 1);
    }

    public void initObjective(Sequence p2EmptySequence) {
        lpTable.setObjective(players[1], 1);
    }

    protected Object getLastISKey(Sequence sequence) {
        return sequence.getLastInformationSet().getISKey();
    }

    public void maximize() {
        lpTable.setMaximize(true);
    }

    public void minimize() {
        lpTable.setMaximize(false);
    }
}
