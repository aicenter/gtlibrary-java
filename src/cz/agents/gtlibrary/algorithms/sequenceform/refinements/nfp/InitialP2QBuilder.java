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


package cz.agents.gtlibrary.algorithms.sequenceform.refinements.nfp;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.Key;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPData;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.TreeVisitor;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerGameState;
import cz.agents.gtlibrary.iinodes.ArrayListSequenceImpl;
import cz.agents.gtlibrary.iinodes.PerfectRecallISKey;
import cz.agents.gtlibrary.interfaces.*;
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

public class InitialP2QBuilder extends TreeVisitor {

	protected String lpFileName;
	protected NFPTable lpTable;
    protected GameInfo info;
	protected double initialValue;

	public InitialP2QBuilder(Expander<SequenceInformationSet> expander, GameState rootState, GameInfo info, double initialValue) {
		super(rootState, expander);
        this.info = info;
		this.expander = expander;
		this.initialValue = initialValue;
		lpFileName = "initialP2Q.lp";
	}

	public void buildLP() {
		initTable();
		visitTree(rootState);
	}

	public IterationData solve() {
		try {
			LPData lpData = lpTable.toCplex();
			boolean solved = false;

			lpData.getSolver().exportModel(lpFileName);
			for (int algorithm : lpData.getAlgorithms()) {
				lpData.getSolver().setParam(IloCplex.IntParam.RootAlg, algorithm);
				if(solved = trySolve(lpData))
					break;
			}
			if(!solved)
				solveUnfeasibleLP(lpData);
			System.out.println(lpData.getSolver().getStatus());
			System.out.println(lpData.getSolver().getObjValue());
			//			System.out.println(Arrays.toString(lpData.getSolver().getValues(lpData.getVariables())));
			//			for (int i = 0; i < lpData.getVariables().length; i++) {
			//				System.out.println(lpData.getVariables()[i] + ": " + lpData.getSolver().getValue(lpData.getVariables()[i]));
			//			}
			return createIterationData(lpData);
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

		System.out.println("Q: " + solved);
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

	protected IterationData createIterationData(LPData lpData) throws UnknownObjectException, IloException {
		Map<Sequence, Double> watchedSequenceValues = getWatchedUSequenceValues(lpData);
		Set<Sequence> exploitableSequences = getExploitableSequences(watchedSequenceValues);
		Map<Sequence, Double> updatedSum = getSum(exploitableSequences, null, initialValue);
		Map<Sequence, Double> realizationPlan = getRealizationPlan(lpData);

		return new IterationData(lpData.getSolver().getObjValue(), updatedSum, exploitableSequences, realizationPlan, getUValues(watchedSequenceValues));
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

	private Sequence getSequence(Entry<Object, IloNumVar> entry) {
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

	public Map<Sequence, Double> createSecondPlayerStrategy(IloCplex cplex, Map<Object, IloNumVar> watchedPrimalVars) throws IloException {
		Map<Sequence, Double> p1Strategy = new HashMap<Sequence, Double>();

		for (Entry<Object, IloNumVar> entry : watchedPrimalVars.entrySet()) {
			p1Strategy.put((Sequence) entry.getKey(), cplex.getValue(entry.getValue()));
		}
		return p1Strategy;
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

		lpTable = new NFPTable();

		initE(p1EmptySequence);
		initF(p2EmptySequence);
		initf();
		addPreviousItConstraints(p2EmptySequence);
	}

	protected void addPreviousItConstraints(Sequence p2EmptySequence) {
		lpTable.setConstraint("prevIt", players[0], 1);
		lpTable.setConstraint("prevIt", "s", -initialValue);
		lpTable.setConstraintType("prevIt", 1);
		lpTable.setLowerBound("s", 1);
	}

	public void initf() {
		lpTable.setConstraint(players[1], "s", -1);//e for root
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

	public void initCost(Sequence p1EmptySequence) {
		lpTable.setObjective(players[0], 1);
	}

	@Override
	protected void visitLeaf(GameState state) {
		updateParentLinks(state);

		lpTable.substractFromConstraint(state.getSequenceFor(players[0]), state.getSequenceFor(players[1]), info.getUtilityStabilizer() * state.getNatureProbability() * state.getUtilities()[1]);
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
		Object p1Sequence = state.getSequenceForPlayerToMove();

		updateParentLinks(state);
		lpTable.setConstraint(p1Sequence, state.getISKeyForPlayerToMove(), -1);//E
		lpTable.setConstraintType(p1Sequence, 0);
		lpTable.setLowerBound(state.getISKeyForPlayerToMove(), Double.NEGATIVE_INFINITY);
		addU(p1Sequence);

	}

	public void updateLPForSecondPlayer(GameState state) {
		Object p2Sequence = state.getSequenceForPlayerToMove();

		updateParentLinks(state);
		lpTable.setConstraint(state.getISKeyForPlayerToMove(), p2Sequence, -1);//F
		lpTable.setConstraintType(state.getISKeyForPlayerToMove(), 1);
		lpTable.setLowerBound(p2Sequence, 0);
		lpTable.watchPrimalVariable(p2Sequence, p2Sequence);
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

	@Override
	protected void visitChanceNode(GameState state) {
		updateParentLinks(state);
		super.visitChanceNode(state);
	}

	protected void updateParentLinks(GameState state) {
		updateP1Parent(state);
		updateP2Parent(state);
	}

	protected void updateP1Parent(GameState state) {
		Sequence p1Sequence = state.getSequenceFor(players[0]);

		if (p1Sequence.size() == 0)
			return;
		Object varKey = getLastISKey(p1Sequence);

		lpTable.setConstraint(p1Sequence, varKey, 1);//E child
		lpTable.setConstraintType(p1Sequence, 0);
		lpTable.setLowerBound(varKey, Double.NEGATIVE_INFINITY);
		addU(p1Sequence);
	}

	protected void updateP2Parent(GameState state) {
		Sequence p2Sequence = state.getSequenceFor(players[1]);

		if (p2Sequence.size() == 0)
			return;
		Object eqKey = getLastISKey(p2Sequence);

		lpTable.setConstraint(eqKey, p2Sequence, 1);//F child
		lpTable.setLowerBound(p2Sequence, 0);
		lpTable.watchPrimalVariable(p2Sequence, p2Sequence);
	}

	protected Object getLastISKey(Sequence sequence) {
		return sequence.getLastInformationSet().getISKey();
	}

}
