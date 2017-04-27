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

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.Key;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPData;
import cz.agents.gtlibrary.domain.poker.generic.GPGameInfo;
import cz.agents.gtlibrary.iinodes.ArrayListSequenceImpl;
import cz.agents.gtlibrary.iinodes.PerfectRecallISKey;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.InformationSet;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.Pair;
import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.UnknownObjectException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class InitialP2QBuilder {

    protected SequenceFormConfig<? extends SequenceInformationSet> config;
    protected double initialValue;
    protected String lpFileName;
    public RecyclingNFPTable lpTable;
    protected Player[] players;
    protected GameInfo info;

    public InitialP2QBuilder(Player[] players, GameInfo info) {
        this.players = players;
        this.info = info;
        lpFileName = "P2DO_Q.lp";
        initTable();
    }

    public void buildLP(SequenceFormConfig<? extends SequenceInformationSet> config, double initialValue, Set<Sequence> sequencesToAdd) {
        this.config = config;
        this.initialValue = initialValue;
        addPreviousItConstraints(initialValue);
        for (Sequence sequence : sequencesToAdd) {
            if (sequence.getPlayer().equals(players[0]))
                updateForP1(sequence);
            else
                updateForP2(sequence);
        }
        updateUtilities(config);
    }

    protected void updateForP1(Sequence p1Sequence) {
        addU(p1Sequence);
        if(config.getReachableSets(p1Sequence) == null)
            return;
        for (SequenceInformationSet informationSet : config.getReachableSets(p1Sequence)) {
            for (Sequence outgoingSequence : informationSet.getOutgoingSequences()) {
                Object varKey = getKey(informationSet);

                addU(outgoingSequence);
                lpTable.setConstraint(p1Sequence, varKey, -1);//F
                lpTable.setConstraintType(p1Sequence, 0);
                lpTable.setLowerBound(varKey, Double.NEGATIVE_INFINITY);
                lpTable.setConstraint(outgoingSequence, varKey, 1);//F
                lpTable.setConstraintType(outgoingSequence, 0);
            }
        }
    }

    protected void updateForP2(Sequence p2Sequence) {
        lpTable.watchPrimalVariable(p2Sequence, p2Sequence);
        if(config.getReachableSets(p2Sequence) == null)
            return;
        for (SequenceInformationSet informationSet : config.getReachableSets(p2Sequence)) {
            for (Sequence outgoingSequence : informationSet.getOutgoingSequences()) {
                Object eqKey = getKey(informationSet);

                lpTable.watchPrimalVariable(outgoingSequence, outgoingSequence);
                lpTable.setConstraint(eqKey, p2Sequence, -1);//E
                lpTable.setConstraintType(eqKey, 1);
                lpTable.setLowerBound(p2Sequence, 0);
                lpTable.setConstraint(eqKey, outgoingSequence, 1);//E
                lpTable.setLowerBound(outgoingSequence, 0);
            }
        }
    }

    private Object getKey(SequenceInformationSet informationSet) {
        return new PerfectRecallISKey(informationSet.hashCode(), informationSet.getPlayersHistory());
    }

//    protected void updateForP1(Sequence p1Sequence) {
//        addU(p1Sequence);
//        if (p1Sequence.size() == 0)
//            return;
//        Object eqKey = getSubsequence(p1Sequence);
//        Object varKey = getLastISKey(p1Sequence);
//
//        addU(eqKey);
//        lpTable.setConstraint(eqKey, varKey, -1);//F
//        lpTable.setConstraintType(eqKey, 0);
//        lpTable.setLowerBound(varKey, Double.NEGATIVE_INFINITY);
////        lpTable.setConstraint(p1Sequence, varKey, 1);//F
////        lpTable.setConstraintType(p1Sequence, 0);
//        addLinksToPrevISForP1(p1Sequence, varKey);
//    }
//
//    public void addLinksToPrevISForP1(Sequence sequence, Object varKey) {
//        SequenceInformationSet set = (SequenceInformationSet) sequence.getLastInformationSet();
//
//        for (Sequence outgoingSequence : set.getOutgoingSequences()) {
//            lpTable.setConstraint(outgoingSequence, varKey, 1);//F child
//            lpTable.setLowerBound(varKey, Double.NEGATIVE_INFINITY);
//            lpTable.setConstraintType(outgoingSequence, 0);
//
////            lpTable.setConstraint(outgoingSequence, tmpKey, -1);//u (eye)
////			lpTable.setObjective(tmpKey, 0);//k(\epsilon)
//        }
//    }
//
//    protected void updateForP2(Sequence p2Sequence) {
////        lpTable.watchPrimalVariable(p2Sequence, p2Sequence);
//        if (p2Sequence.size() == 0)
//            return;
//        Object varKey = getSubsequence(p2Sequence);
//        Object eqKey = getLastISKey(p2Sequence);
//
//        lpTable.setConstraint(eqKey, varKey, -1);//E
//        lpTable.setConstraintType(eqKey, 1);
//        lpTable.setLowerBound(varKey, 0);
////        lpTable.setConstraint(eqKey, p2Sequence, 1);//E
////        lpTable.setLowerBound(p2Sequence, 0);
//        addLinksToPrevISForP2(p2Sequence, eqKey);
//    }
//
//    protected void addLinksToPrevISForP2(Sequence sequence, Object eqKey) {
//        SequenceInformationSet set = (SequenceInformationSet) sequence.getLastInformationSet();
//
//        for (Sequence outgoingSequence : set.getOutgoingSequences()) {
////            Key tmpKey = new Key("V", outgoingSequence);
//
//            lpTable.setConstraint(eqKey, outgoingSequence, 1);//E child
//            lpTable.setConstraintType(eqKey, 1);
//            lpTable.setLowerBound(outgoingSequence, 0);
////			lpTable.setConstant(tmpKey, 0);//l(\epsilon)
//        }
//
//    }

    protected Sequence getSubsequence(Sequence sequence) {
        return sequence.getSubSequence(sequence.size() - 1);
    }

    private void updateUtilities(SequenceFormConfig<? extends SequenceInformationSet> config) {
        for (Sequence p1Sequence : config.getSequencesFor(players[0])) {
            for (Sequence compatibleSequence : config.getCompatibleSequencesFor(p1Sequence)) {
                Double utility = config.getUtilityFor(p1Sequence, compatibleSequence);

                if (utility == null)
                    lpTable.removeFromConstraint(p1Sequence, compatibleSequence);
                else if(info instanceof GPGameInfo)
                    lpTable.setConstraint(p1Sequence, compatibleSequence, Math.round(info.getUtilityStabilizer()*utility));
                else
                    lpTable.setConstraint(p1Sequence, compatibleSequence, info.getUtilityStabilizer()*utility);
            }
        }
        for (Sequence p2Sequence : config.getSequencesFor(players[1])) {
            for (Sequence compatibleSequence : config.getCompatibleSequencesFor(p2Sequence)) {
                Double utility = config.getUtilityFor(compatibleSequence, p2Sequence);

                if (utility == null)
                    lpTable.removeFromConstraint(compatibleSequence, p2Sequence);
                else
                    lpTable.setConstraint(compatibleSequence, p2Sequence, info.getUtilityStabilizer()*utility);
            }
        }
    }

//    private void clearUtilities(SequenceFormConfig<? extends SequenceInformationSet> config) {
//        Set<Sequence> newSequences = config.getNewSequences();
//
//        for (Sequence p1Sequence : config.getSequencesFor(players[0])) {
//            if (!newSequences.contains(p1Sequence))
//                for (Sequence p2Sequence : config.getSequencesFor(players[1])) {
//                    if (!newSequences.contains(p2Sequence))
//                        lpTable.removeFromConstraint(p1Sequence, p2Sequence);
//                }
//        }
//    }
//
//    protected void addUtilities(Iterable<Sequence> p1Sequences, Iterable<Sequence> p2Sequences) {
//        for (Sequence p1Sequence : p1Sequences) {
//            for (Sequence p2Sequence : p2Sequences) {
//                Double utility = config.getUtilityFor(p1Sequence, p2Sequence);
//
//                if (utility != null) {
//                    lpTable.substractFromConstraint(p1Sequence, p2Sequence, -utility);
//                }
//            }
//        }
//    }

//    protected void addUtilities(Iterable<Sequence> newSequences) {
//        Set<Pair<Sequence, Sequence>> blackList = new HashSet<Pair<Sequence, Sequence>>();
//
//        for (Sequence newSequence : newSequences) {
//            if (newSequence.getPlayer().equals(players[0]))
//                for (Sequence compatibleSequence : config.getCompatibleSequencesFor(newSequence)) {
//                    Double utility = config.getUtilityFor(newSequence, compatibleSequence);
//                    Pair<Sequence, Sequence> sequencePair = new Pair<Sequence, Sequence>(newSequence, compatibleSequence);
//
//                    if (utility != null)
//                        if (!blackList.contains(sequencePair)) {
//                            lpTable.substractFromConstraint(newSequence, compatibleSequence, -utility);
//                            blackList.add(sequencePair);
//                        }
//                    clearForAllP1Prefixes(newSequence);
//                }
//            else
//                for (Sequence compatibleSequence : config.getCompatibleSequencesFor(newSequence)) {
//                    Double utility = config.getUtilityFor(newSequence, compatibleSequence);
//                    Pair<Sequence, Sequence> sequencePair = new Pair<Sequence, Sequence>(compatibleSequence, newSequence);
//
//                    if (utility != null)
//                        if (!blackList.contains(sequencePair)) {
//                            lpTable.substractFromConstraint(compatibleSequence, newSequence, -utility);
//                            blackList.add(sequencePair);
//                        }
//                    clearForAllP2Prefixes(newSequence);
//                }
//        }
//    }
//
//    private void clearForAllP1Prefixes(Sequence newSequence) {
//        for (Sequence prefix : newSequence.getAllPrefixes()) {
//            for (Sequence compatibleSequence : config.getCompatibleSequencesFor(prefix)) {
//                if (config.getUtilityFor(prefix, compatibleSequence) == null)
//                    lpTable.removeFromConstraint(prefix, compatibleSequence);
//            }
//        }
//
//    }
//
//    private void clearForAllP2Prefixes(Sequence newSequence) {
//        for (Sequence prefix : newSequence.getAllPrefixes()) {
//            for (Sequence compatibleSequence : config.getCompatibleSequencesFor(prefix)) {
//                if (config.getUtilityFor(prefix, compatibleSequence) == null)
//                    lpTable.removeFromConstraint(compatibleSequence, prefix);
//            }
//        }
//
//    }

    public QResult solve() {
        try {
            LPData lpData = lpTable.toCplex();
            boolean solved = false;

//            lpData.getSolver().exportModel(lpFileName);
            for (int algorithm : lpData.getAlgorithms()) {
                lpData.getSolver().setParam(IloCplex.IntParam.RootAlg, algorithm);
                if (solved = trySolve(lpData))
                    break;
            }
            if (!solved)
                solveUnfeasibleLP(lpData);
            System.out.println(lpData.getSolver().getStatus());
            System.out.println(lpData.getSolver().getObjValue());
            //			System.out.println(Arrays.toString(lpData.getSolver().getValues(lpData.getVariables())));
//            for (int i = 0; i < lpData.getVariables().length; i++) {
//                System.out.println(lpData.getVariables()[i] + ": " + lpData.getSolver().getValue(lpData.getVariables()[i]));
//            }
            return createResult(lpData);
        } catch (IloException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected QResult createResult(LPData lpData) throws IloException {
        Map<Sequence, Double> watchedSequenceValues = getWatchedUSequenceValues(lpData);
        Set<Sequence> exploitableSequences = getExploitableSequences(watchedSequenceValues);
        Map<Sequence, Double> updatedSum = getSum(exploitableSequences, null, initialValue);

        return new QResult(lpData.getSolver().getObjValue(), updatedSum, exploitableSequences, getRealizationPlan(lpData));
    }

    protected boolean trySolve(LPData lpData) throws IloException {
        boolean solved;

        try {
            solved = lpData.getSolver().solve();
        } catch (IloException e) {
            e.printStackTrace();
            return false;
        }

        System.out.println("Q: " + solved);
        System.out.println(lpData.getSolver().getObjValue());
        return solved;
    }

    protected void solveUnfeasibleLP(LPData lpData) throws IloException {
        lpData.getSolver().setParam(IloCplex.IntParam.FeasOptMode, IloCplex.Relaxation.OptQuad);

        boolean solved = lpData.getSolver().feasOpt(lpData.getConstraints(), getPreferences(lpData));

        assert solved;
        System.out.println("Q feas: " + solved);
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

    protected Map<Sequence, Double> getSum(Set<Sequence> exploitableSequences, Map<Sequence, Double> explSeqSum, double valueOfGame) {
        Map<Sequence, Double> sum = new HashMap<Sequence, Double>();

        //		for (Sequence sequence : exploitableSequences) {
        //			sum.put(sequence, valueOfGame);
        //		}
        return sum;
    }

    protected Set<Sequence> getExploitableSequences(Map<Sequence, Double> watchedSequenceValues) {
        Set<Sequence> exploitableSequences = new HashSet<Sequence>();

        for (Entry<Sequence, Double> entry : watchedSequenceValues.entrySet()) {
            //			assert Math.abs(entry.getValue()) < 1e-5 || Math.abs(entry.getValue() - 1) < 1e-5;
            if (entry.getValue() > 0.5 && entry.getKey().size() > 0) {
                Sequence subSequence = entry.getKey().getSubSequence(entry.getKey().size() - 1);

                if (watchedSequenceValues.get(subSequence) < 0.5)
                    exploitableSequences.add(entry.getKey());
            }
        }
        return exploitableSequences;
    }

    protected Map<Sequence, Double> getUValues(Map<Sequence, Double> watchedSequenceValues) {
        Map<Sequence, Double> uValues = new HashMap<Sequence, Double>();

        for (Entry<Sequence, Double> entry : watchedSequenceValues.entrySet()) {
            if (entry.getValue() > 1e-2)
                uValues.put(entry.getKey(), entry.getValue());
        }
        return uValues;
    }

    protected Map<Sequence, Double> getWatchedUSequenceValues(LPData lpData) throws UnknownObjectException, IloException {
        Map<Sequence, Double> watchedSequenceValues = new HashMap<Sequence, Double>(lpData.getWatchedPrimalVariables().size());

        for (Entry<Object, IloNumVar> entry : lpData.getWatchedPrimalVariables().entrySet()) {
            if (entry.getKey() instanceof Key)
                watchedSequenceValues.put(getSequence(entry), lpData.getSolver().getValue(entry.getValue()));
        }
        return watchedSequenceValues;
    }

    protected Sequence getSequence(Entry<Object, IloNumVar> entry) {
        return (Sequence) ((Key) entry.getKey()).getObject();
    }

    protected Map<Sequence, Double> getRealizationPlan(LPData lpData) throws UnknownObjectException, IloException {
        Map<Sequence, Double> watchedSequenceValues = new HashMap<Sequence, Double>(lpData.getWatchedPrimalVariables().size());

        for (Entry<Object, IloNumVar> entry : lpData.getWatchedPrimalVariables().entrySet()) {
            if (entry.getKey() instanceof Sequence)
                watchedSequenceValues.put((Sequence) entry.getKey(), lpData.getSolver().getValue(entry.getValue()));
        }
        return watchedSequenceValues;
    }

//	public Map<Sequence, Double> createSecondPlayerStrategy(IloCplex cplex, Map<Object, IloNumVar> watchedPrimalVars) throws IloException {
//		Map<Sequence, Double> p1Strategy = new HashMap<Sequence, Double>();
//
//		for (Entry<Object, IloNumVar> entry : watchedPrimalVars.entrySet()) {
//			p1Strategy.put((Sequence) entry.getKey(), cplex.getValue(entry.getValue()));
//		}
//		return p1Strategy;
//	}

    //	public Map<Sequence, Double> createSecondPlayerStrategy(IloCplex cplex, Map<Object, IloNumVar> watchedPrimalVars) throws IloException {
    //		Map<Sequence, Double> p2Strategy = new HashMap<Sequence, Double>();
    //
    //		for (Entry<Object, IloNumVar> entry : watchedPrimalVars.entrySet()) {
    //			p2Strategy.put((Sequence) entry.getKey(), cplex.getValue(entry.getValue()));
    //		}
    //		return p2Strategy;
    //	}

    public void initTable() {
        Sequence p1EmptySequence = new ArrayListSequenceImpl(players[0]);
        Sequence p2EmptySequence = new ArrayListSequenceImpl(players[1]);

        lpTable = new RecyclingNFPTable();

        initE(p1EmptySequence);
        initF(p2EmptySequence);
        initf();
    }

    protected void addPreviousItConstraints(double initialValue) {
        lpTable.setConstraint("prevIt", players[0], 1);
        lpTable.setConstraint("prevIt", "s", -initialValue);
        lpTable.setConstraintType("prevIt", 1);
        lpTable.setLowerBound("s", 1);
    }

    public void initf() {
        lpTable.setConstraint(players[1], "s", -1);//e for root
    }

    public void initE(Sequence p1EmptySequence) {
        addU(p1EmptySequence);
        lpTable.setConstraint(p1EmptySequence, players[0], 1);//F in root (only 1)
        lpTable.setConstraintType(p1EmptySequence, 0);
        lpTable.setLowerBound(players[0], Double.NEGATIVE_INFINITY);
    }

    public void initF(Sequence p2EmptySequence) {
        lpTable.setConstraint(players[1], p2EmptySequence, 1);//E in root (only 1)
        lpTable.setConstraintType(players[1], 1);
    }

    protected void addU(Object eqKey) {
        Key uKey = new Key("u", eqKey);

        lpTable.setConstraint(eqKey, uKey, 1);
        lpTable.setLowerBound(uKey, 0);
        lpTable.setUpperBound(uKey, 1);
        lpTable.setObjective(uKey, 1);
        lpTable.watchPrimalVariable(uKey, uKey);
        lpTable.markRelaxableConstraint(eqKey);
    }

    protected Object getLastISKey(Sequence sequence) {
        return sequence.getLastInformationSet().getISKey();
    }

}
