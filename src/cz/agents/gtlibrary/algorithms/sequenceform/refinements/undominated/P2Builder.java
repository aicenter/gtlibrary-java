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


package cz.agents.gtlibrary.algorithms.sequenceform.refinements.undominated;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPData;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.TreeVisitor;
import cz.agents.gtlibrary.iinodes.ArrayListSequenceImpl;
import cz.agents.gtlibrary.iinodes.PerfectRecallISKey;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.Pair;
import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.UnknownObjectException;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class P2Builder extends TreeVisitor {

    protected String lpFileName;
    protected UndominatedTable lpTable;
    protected Map<Sequence, Double> uniformRealizationPlan;
    protected Map<Sequence, Integer> extensionCounts;
    protected double gameValue;

    public P2Builder(Expander<SequenceInformationSet> expander, GameState rootState, double gameValue) {
        super(rootState, expander);
        this.expander = expander;
        lpFileName = "P2Undominated.lp";
        this.gameValue = gameValue;
        uniformRealizationPlan = new HashMap<Sequence, Double>();
        extensionCounts = new HashMap<Sequence, Integer>();

        uniformRealizationPlan.put(new ArrayListSequenceImpl(players[0]), 1d);
        extensionCounts.put(new ArrayListSequenceImpl(players[0]), expander.getActions(rootState).size());
    }

    public void buildLP() {
        initTable();
        visitTree(rootState);
    }

    public Map<Sequence, Double> solve() {
        try {
            LPData lpData = lpTable.toCplex();
            boolean solved = false;

            lpData.getSolver().exportModel(lpFileName);
            for (int algorithm : lpData.getAlgorithms()) {
                lpData.getSolver().setParam(IloCplex.IntParam.RootAlg, algorithm);
                if (solved = trySolve(lpData))
                    break;
            }
            if (!solved)
                solveUnfeasibleLP(lpData);
            System.out.println(lpData.getSolver().getStatus());
            System.out.println(lpData.getSolver().getObjValue());

            return createSecondPlayerStrategy(lpData.getSolver(), lpData.getWatchedPrimalVariables());
        } catch (IloException e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean trySolve(LPData lpData) throws IloException {
        boolean solved;

        try {
            solved = lpData.getSolver().solve();
        } catch (IloException e) {
            return false;
        }

        System.out.println("P: " + solved);
        return solved;
    }

    private void solveUnfeasibleLP(LPData lpData) throws IloException {
        lpData.getSolver().setParam(IloCplex.IntParam.FeasOptMode, IloCplex.Relaxation.OptQuad);

        boolean solved = lpData.getSolver().feasOpt(lpData.getConstraints(), getPreferences(lpData));

        assert solved;
        System.out.println("Q feas: " + solved);
    }

    private double[] getPreferences(LPData lpData) {
        double[] preferences = new double[lpData.getConstraints().length];

        for (int i = 0; i < preferences.length; i++) {
            if (lpData.getRelaxableConstraints().values().contains(lpData.getConstraints()[i]))
                preferences[i] = 1;
            else
                preferences[i] = 0.5;
        }
        return preferences;
    }

    public Map<Sequence, Double> createSecondPlayerStrategy(IloCplex cplex, Map<Object, IloNumVar> watchedPrimalVars) throws IloException {
        Map<Sequence, Double> p1Strategy = new HashMap<Sequence, Double>();

        for (Entry<Object, IloNumVar> entry : watchedPrimalVars.entrySet()) {
            p1Strategy.put((Sequence) entry.getKey(), cplex.getValue(entry.getValue()));
        }
        return p1Strategy;
    }

    protected Map<Sequence, Double> getWatchedSequenceValues(LPData lpData) throws UnknownObjectException, IloException {
        Map<Sequence, Double> watchedSequenceValues = new HashMap<Sequence, Double>(lpData.getWatchedPrimalVariables().size());

        for (Entry<Object, IloNumVar> entry : lpData.getWatchedPrimalVariables().entrySet()) {
            watchedSequenceValues.put((Sequence) entry.getKey(), lpData.getSolver().getValue(entry.getValue()));
        }
        return watchedSequenceValues;
    }

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

        lpTable = new UndominatedTable();

//		initObjective(p1EmptySequence);
        addGameValueConstraint();
        initE(p1EmptySequence);
        initF(p2EmptySequence);
        initf();
    }

    private void addGameValueConstraint() {
        lpTable.setConstraint("gameValue", players[1], 1);
        lpTable.setConstraintType("gameValue", 1);
        lpTable.setConstant("gameValue", gameValue);
    }

    public void initf() {
        lpTable.setConstant(players[1], 1);//e for root
    }

    public void initE(Sequence p1EmptySequence) {
        lpTable.setConstraint(p1EmptySequence, players[0], 1);//F in root (only 1)
        lpTable.setConstraintType(p1EmptySequence, 0);
        lpTable.setLowerBound(players[0], Double.NEGATIVE_INFINITY);
    }

    public void initF(Sequence p2EmptySequence) {
        lpTable.setConstraint(players[1], p2EmptySequence, 1);//E in root (only 1)
        lpTable.setConstraintType(players[1], 1);
    }

//	public void initObjective(Sequence p1EmptySequence) {
//		lpTable.setObjective(players[0], 1);
//	}

    @Override
    protected void visitLeaf(GameState state) {
        updateParentLinks(state);
        lpTable.substractFromConstraint(state.getSequenceFor(players[0]), state.getSequenceFor(players[1]), state.getNatureProbability() * state.getUtilities()[1]);
        lpTable.addToObjective(state.getSequenceFor(players[1]), uniformRealizationPlan.get(state.getSequenceFor(players[0])) * state.getNatureProbability() * state.getUtilities()[1]);
    }

    @Override
    protected void visitNormalNode(GameState state) {
        if (state.getPlayerToMove().getId() == 0) {
            updateLPForFirstPlayer(state);
        } else {
            updateLPForSecondPlayer(state);
        }
        super.visitNormalNode(state);
    }

    public void updateLPForFirstPlayer(GameState state) {
        Object eqKey = state.getSequenceForPlayerToMove();

        updateParentLinks(state);
        lpTable.setConstraint(eqKey, state.getISKeyForPlayerToMove(), -1);//E
        lpTable.setConstraintType(eqKey, 0);
        lpTable.setLowerBound(state.getISKeyForPlayerToMove(), Double.NEGATIVE_INFINITY);
    }

    public void updateLPForSecondPlayer(GameState state) {
        Object varKey = state.getSequenceForPlayerToMove();

        updateParentLinks(state);
        lpTable.setConstraint(state.getISKeyForPlayerToMove(), varKey, -1);//F
        lpTable.setConstraintType(state.getISKeyForPlayerToMove(), 1);
        lpTable.setLowerBound(varKey, 0);
        lpTable.watchPrimalVariable(state.getSequenceFor(players[1]), state.getSequenceForPlayerToMove());
    }

    @Override
    protected void visitChanceNode(GameState state) {
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
            Sequence prefix = getSubsequence(p1Sequence);
            double probability = uniformRealizationPlan.get(prefix);
            int extensionCount = extensionCounts.get(prefix);

//            lpTable.setConstraint(new Key("r", p1Sequence), p1Sequence, 1);
//            lpTable.setConstant(new Key("r", p1Sequence), probability / extensionCount);
//            lpTable.setConstraintType(new Key("r", p1Sequence), 1);

            extensionCounts.put(p1Sequence, expander.getActions(state).size());
            uniformRealizationPlan.put(p1Sequence, probability / extensionCount);
        }
        if (p1Sequence.size() == 0)
            return;
        Object varKey = getLastISKey(p1Sequence);

        lpTable.setConstraint(p1Sequence, varKey, 1);//E child
        lpTable.setConstraintType(p1Sequence, 0);
        lpTable.setLowerBound(varKey, Double.NEGATIVE_INFINITY);

    }

    private Sequence getSubsequence(Sequence sequence) {
        return sequence.getSubSequence(0, sequence.size() - 1);
    }

    protected void updateP2Parent(GameState state) {
        Sequence p2Sequence = state.getSequenceFor(players[1]);

        if (p2Sequence.size() == 0)
            return;
        Object eqKey = getLastISKey(p2Sequence);

        lpTable.watchPrimalVariable(p2Sequence, p2Sequence);
        lpTable.setConstraint(eqKey, p2Sequence, 1);//F child
        lpTable.setLowerBound(p2Sequence, 0);
    }

    protected Object getLastISKey(Sequence sequence) {
        return sequence.getLastInformationSet().getISKey();
    }
}
